package com.plg.utils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.SynchronousQueue;
import java.util.stream.Collectors;

import com.plg.config.DataLoader;
import com.plg.dto.IndividuoDto;
import com.plg.dto.request.AveriaConEstadoRequest;
import com.plg.entity.Almacen;
import com.plg.entity.Bloqueo;
import com.plg.entity.Camion;
import com.plg.entity.Coordenada;
import com.plg.entity.EstadoCamion;
import com.plg.entity.EstadoPedido;
import com.plg.utils.Individuo;
import com.plg.entity.Mapa;
import com.plg.entity.Nodo;
import com.plg.entity.Pedido;
import com.plg.entity.TipoAlmacen;
import com.plg.entity.TipoNodo;
import com.plg.utils.simulacion.ConfiguracionSimulacion;
import com.plg.utils.simulacion.MantenimientoManager;
import com.plg.utils.simulacion.AveriasManager;
import com.plg.utils.simulacion.UtilesSimulacion;
import com.plg.utils.simulacion.CamionStateApplier;
import com.plg.utils.simulacion.IndividuoFactory;
import com.plg.utils.simulacion.GestorHistorialSimulacion;

/**
 * Clase principal que maneja la simulación de logística y algoritmos genéticos.
 */
public class Simulacion {

    private static List<Pedido> pedidosSemanal;
    private static LocalDateTime fechaActual;
    public static Set<Pedido> pedidosPorAtender = new LinkedHashSet<>();
    public static Set<Pedido> pedidosPlanificados = new LinkedHashSet<>();
    public static Set<Pedido> pedidosEntregados = new LinkedHashSet<>();
    public static Individuo mejorIndividuo = null;
    
    // Queue de paquetes generados para el frontend
    // Administrado por GestorHistorialSimulacion
    public static List<IndividuoDto> historialSimulacion = new ArrayList<>();
    private static int indiceActualFrontend = 0;
    private static boolean simulacionEnProceso = false;
    private static int contadorPaquetes = 0; // Contador secuencial de paquetes
    
    // Modo de ejecución: true para standalone (generar paquetes continuamente)
    public static boolean modoStandalone = true;

    // Colas para simulación
    public static BlockingQueue<Object> gaTriggerQueue = new SynchronousQueue<>();
    public static BlockingQueue<IndividuoDto> gaResultQueue = new SynchronousQueue<>();
    public static Semaphore iniciar = new Semaphore(0);
    public static Semaphore continuar = new Semaphore(0);

    // Getters y setters para permitir acceso desde clases auxiliares
    public static List<Pedido> getPedidosSemanal() {
        return pedidosSemanal;
    }

    public static void setPedidosSemanal(List<Pedido> pedidos) {
        pedidosSemanal = pedidos;
    }

    public static LocalDateTime getFechaActual() {
        return fechaActual;
    }

    public static void setFechaActual(LocalDateTime fecha) {
        fechaActual = fecha;
    }

    public static void configurarSimulacion(LocalDateTime startDate) {
        ConfiguracionSimulacion.configurarSimulacion(startDate);
    }

    public static void ejecutarSimulacion() {
        try {
            GestorHistorialSimulacion.setEnProceso(true);
            ConfiguracionSimulacion.imprimirDatosSimulacion();
            LocalDateTime fechaLimite = fechaActual.plusDays(7);
            System.out.println("🚀 Iniciando simulación hasta: " + fechaLimite);
            System.out.println("📅 Fecha de inicio (desde frontend): " + fechaActual);
            System.out.println("📦 Pedidos semanales iniciales: " + pedidosSemanal.size());
            
            while (!pedidosSemanal.isEmpty() && (fechaActual.isBefore(fechaLimite) || fechaActual.isEqual(fechaLimite))) {
                // Verificar si el hilo ha sido interrumpido
                if (Thread.currentThread().isInterrupted()) {
                    System.out.println("🛑 Simulación interrumpida por solicitud externa");
                    GestorHistorialSimulacion.setEnProceso(false);
                    return;
                }
                
                // Verificar si la simulación fue detenida por una avería
                if (!GestorHistorialSimulacion.isEnProceso()) {
                    System.out.println("🚨 Simulación detenida por avería - finalizando bucle de simulación");
                    return;
                }
                
                Pedido pedido = pedidosSemanal.get(0);
            // Voy agregando pedidos a la lista de pedidos
            if (UtilesSimulacion.pedidoConFechaMenorAFechaActual(pedido, fechaActual)) {
                pedidosSemanal.remove(0);
                pedidosPorAtender.add(pedido);
            } else {
                List<Pedido> pedidosEnviar = UtilesSimulacion.unirPedidosSinRepetidos(pedidosPlanificados, pedidosPorAtender);
                actualizarEstadoGlobal(fechaActual, pedidosEnviar);
                List<Bloqueo> bloqueosActivos = actualizarBloqueos(fechaActual);
                System.out.println("------------------------");
                System.out.println("Tiempo actual: " + fechaActual);

                if (!pedidosPorAtender.isEmpty()) {
                    
                    if (modoStandalone) {
                        // Modo standalone: ejecutar sin esperar semáforos
                        try {
                            AlgoritmoGenetico algoritmoGenetico = new AlgoritmoGenetico(Mapa.getInstance(), pedidosEnviar);
                            algoritmoGenetico.ejecutarAlgoritmo();

                            IndividuoDto mejorIndividuoDto = new IndividuoDto(algoritmoGenetico.getMejorIndividuo(),
                                    pedidosEnviar, bloqueosActivos, fechaActual);
                            
                            // Aplicar el estado final de los camiones permanentemente
                            CamionStateApplier.aplicarEstadoFinalCamiones(algoritmoGenetico.getMejorIndividuo());
                            
                            // Agregar al historial para el frontend
                            GestorHistorialSimulacion.agregarPaquete(mejorIndividuoDto);
                        } catch (Exception e) {
                            System.err.println("❌ Error en algoritmo genético en tiempo " + fechaActual + ": " + e.getMessage());
                            e.printStackTrace();
                            
                            // Crear un paquete de emergencia en lugar de no generar nada
                            try {
                                System.out.println("🚑 Creando paquete de emergencia para tiempo " + fechaActual);
                                Individuo individuoEmergencia = IndividuoFactory.crearIndividuoVacio();
                                IndividuoDto paqueteEmergencia = new IndividuoDto(individuoEmergencia,
                                        pedidosEnviar, bloqueosActivos, fechaActual);
                                GestorHistorialSimulacion.agregarPaquete(paqueteEmergencia);
                            } catch (Exception e2) {
                                System.err.println("❌ Error al crear paquete de emergencia: " + e2.getMessage());
                                e2.printStackTrace();
                            }
                        }
                    } else {
                        // Modo web interactivo: esperar semáforos
                        try {
                            iniciar.acquire();
                            AlgoritmoGenetico algoritmoGenetico = new AlgoritmoGenetico(Mapa.getInstance(), pedidosEnviar);
                            algoritmoGenetico.ejecutarAlgoritmo();

                            IndividuoDto mejorIndividuoDto = new IndividuoDto(algoritmoGenetico.getMejorIndividuo(),
                                    pedidosEnviar, bloqueosActivos, fechaActual);
                            gaResultQueue.offer(mejorIndividuoDto);
                            continuar.acquire();

                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            System.err.println("Error al esperar el disparador del algoritmo genético: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                } else {
                    System.out.println("No hay pedidos por atender en este momento.");
                    
                    // Crear paquete vacío para las horas sin pedidos
                    if (modoStandalone) {
                        try {
                            // Crear un individuo vacío con rutas de retorno al almacén
                            Individuo individuoVacio = IndividuoFactory.crearIndividuoVacio();
                            
                            IndividuoDto paqueteVacio = new IndividuoDto(individuoVacio,
                                    new ArrayList<>(), bloqueosActivos, fechaActual);
                            
                            // Agregar al historial para el frontend
                            GestorHistorialSimulacion.agregarPaquete(paqueteVacio);
                        } catch (Exception e) {
                            System.err.println("❌ Error creando paquete vacío en tiempo " + fechaActual + ": " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
                for (Bloqueo bloqueo : bloqueosActivos) {
                    bloqueo.desactivarBloqueo();
                }

                fechaActual = fechaActual.plusMinutes(Parametros.intervaloTiempo);
                System.out.println("📊 Estado: Pedidos semanales restantes: " + pedidosSemanal.size() + 
                                 ", Por atender: " + pedidosPorAtender.size() + 
                                 ", Planificados: " + pedidosPlanificados.size());
            }
        }
        
        // Explicar por qué terminó la simulación
        if (pedidosSemanal.isEmpty()) {
            System.out.println("✅ Simulación terminada: Todos los pedidos semanales han sido procesados");
        } else if (fechaActual.isAfter(fechaLimite)) {
            System.out.println("⏰ Simulación terminada: Se alcanzó el límite de tiempo (" + fechaLimite + ")");
            System.out.println("📦 Pedidos semanales no procesados: " + pedidosSemanal.size());
        }
        
        System.out.println("-------------------------");
        System.out.println("Reporte de la simulación");
        System.out.println("Fecha final: " + fechaActual);
        System.out.println("Kilometros recorridos: " + Parametros.kilometrosRecorridos);
        System.out.println("Fitness global: " + Parametros.fitnessGlobal);
        System.out.println("Pedidos entregados: " + pedidosEntregados.size());
        System.out.println("Pedidos pendientes: " + pedidosPorAtender.size());
        
        GestorHistorialSimulacion.setEnProceso(false);
        System.out.println("✅ Simulación completada. Total de paquetes generados: " + GestorHistorialSimulacion.getTotalPaquetes());
        
        } catch (Exception e) {
            System.err.println("💥 ERROR CRÍTICO EN LA SIMULACIÓN:");
            System.err.println("Tiempo actual cuando ocurrió el error: " + fechaActual);
            System.err.println("Mensaje de error: " + e.getMessage());
            System.err.println("Tipo de excepción: " + e.getClass().getSimpleName());
            e.printStackTrace();
            
            System.err.println("\n📊 Estado al momento del error:");
            System.err.println("   • Pedidos semanales restantes: " + (pedidosSemanal != null ? pedidosSemanal.size() : "null"));
            System.err.println("   • Pedidos por atender: " + (pedidosPorAtender != null ? pedidosPorAtender.size() : "null"));
            System.err.println("   • Pedidos planificados: " + (pedidosPlanificados != null ? pedidosPlanificados.size() : "null"));
        }
    }

    /**
     * Obtiene el siguiente paquete de la simulación para el frontend
     * Cada llamada devuelve el siguiente paso en secuencia
     */
    public static IndividuoDto obtenerSiguientePaquete() {
        return GestorHistorialSimulacion.obtenerSiguientePaquete();
    }
    
    /**
     * Reinicia la reproducción desde el inicio para el frontend
     */
    public static void reiniciarReproduccion() {
        GestorHistorialSimulacion.reiniciarReproduccion();
    }
    
    /**
     * Elimina todos los paquetes futuros mantiendo solo el paquete actual.
     * Se utiliza cuando ocurre una avería para detener la simulación futura.
     * 
     * @return Número de paquetes eliminados
     */
    public static int eliminarPaquetesFuturos() {
        return GestorHistorialSimulacion.eliminarPaquetesFuturos();
    }
    
    /**
     * Genera un paquete parche cuando ocurre una avería.
     * Este paquete cubre desde el momento de la avería hasta completar la ventana temporal.
     * 
     * @param timestampAveria Momento cuando ocurrió la avería
     * @param estadoSimulacionActual Estado completo de la simulación capturado durante la avería
     * @return El paquete parche generado
     */
    public static IndividuoDto generarPaqueteParche(LocalDateTime timestampAveria, AveriaConEstadoRequest.EstadoSimulacion estadoSimulacionActual) {
        try {
            System.out.println("🩹 GENERANDO PAQUETE PARCHE para avería en: " + timestampAveria);
            
            // Obtener información del paquete actual
            int paqueteActualNumero = GestorHistorialSimulacion.getPaqueteActual();
            IndividuoDto paqueteActual = GestorHistorialSimulacion.obtenerPaquetePorIndice(paqueteActualNumero - 1);
            
            if (paqueteActual == null) {
                System.err.println("❌ No se pudo obtener el paquete actual para generar el parche");
                return null;
            }
            
            System.out.println("📊 DATOS PARA PAQUETE PARCHE:");
            System.out.println("   • Paquete actual número: " + paqueteActualNumero);
            System.out.println("   • Timestamp avería: " + timestampAveria);
            System.out.println("   • Paquete actual inicia: " + paqueteActual.getFechaHoraSimulacion());
            
            // Calcular el tiempo de inicio del paquete parche = timestamp de la avería
            LocalDateTime inicioParche = timestampAveria;
            
            // Calcular el tiempo de fin = inicio del paquete actual + 2 horas (ventana normal)  
            // getFechaHoraSimulacion() devuelve un LocalDateTime directamente
            LocalDateTime inicioPaqueteActual = paqueteActual.getFechaHoraSimulacion();
            LocalDateTime finParche = inicioPaqueteActual.plusHours(2);
            
            System.out.println("⏰ VENTANA TEMPORAL DEL PARCHE:");
            System.out.println("   • Inicio parche: " + inicioParche);
            System.out.println("   • Fin parche: " + finParche);
            System.out.println("   • Duración: " + java.time.Duration.between(inicioParche, finParche).toMinutes() + " minutos");
            
            // Crear un individuo con el estado capturado durante la avería
            Individuo individuoParche = crearIndividuoDesdeEstadoCapturado(estadoSimulacionActual);
            
            // Obtener pedidos y bloqueos para el parche (usar los del estado capturado)
            List<Pedido> pedidosParche = obtenerPedidosDesdeEstadoCapturado(estadoSimulacionActual);
            List<Bloqueo> bloqueosParche = obtenerBloqueosDesdeEstadoCapturado(estadoSimulacionActual);
            
            // Crear el paquete parche con la fecha de inicio de la avería
            IndividuoDto paqueteParche = new IndividuoDto(
                individuoParche,
                pedidosParche,
                bloqueosParche,
                inicioParche
            );
            
            System.out.println("✅ PAQUETE PARCHE GENERADO:");
            System.out.println("   • Camiones: " + (individuoParche.getCromosoma() != null ? individuoParche.getCromosoma().size() : 0));
            System.out.println("   • Pedidos: " + pedidosParche.size());
            System.out.println("   • Bloqueos: " + bloqueosParche.size());
            System.out.println("   • Fecha simulación: " + paqueteParche.getFechaHoraSimulacion());
            
            return paqueteParche;
            
        } catch (Exception e) {
            System.err.println("❌ ERROR al generar paquete parche: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Inserta un paquete parche en el historial en la posición correcta.
     * 
     * @param paqueteParche El paquete parche a insertar
     */
    public static void insertarPaqueteParche(IndividuoDto paqueteParche) {
        if (paqueteParche == null) {
            System.err.println("❌ No se puede insertar un paquete parche nulo");
            return;
        }
        
        // Insertar en la posición paqueteActual + 1
        int posicionInsercion = GestorHistorialSimulacion.getPaqueteActual();
        GestorHistorialSimulacion.insertarPaqueteParche(paqueteParche, posicionInsercion);
        
        System.out.println("🩹 Paquete parche insertado en posición: " + posicionInsercion);
    }
    
    /**
     * Crea un individuo desde el estado capturado durante la avería.
     * 
     * @param estadoCapturado Estado de la simulación capturado
     * @return Individuo con los datos del estado capturado
     */
    private static Individuo crearIndividuoDesdeEstadoCapturado(AveriaConEstadoRequest.EstadoSimulacion estadoCapturado) {
        try {
            // Crear un individuo vacío y luego poblarlo con los datos capturados
            Individuo individuo = IndividuoFactory.crearIndividuoVacio();
            
            // Aquí se podría implementar lógica más sofisticada para convertir
            // el estado capturado en un individuo válido
            // Por ahora, usamos un individuo básico
            
            System.out.println("🔄 Individuo parche creado desde estado capturado");
            return individuo;
            
        } catch (Exception e) {
            System.err.println("❌ Error al crear individuo desde estado capturado: " + e.getMessage());
            // Fallback: crear individuo vacío
            return IndividuoFactory.crearIndividuoVacio();
        }
    }
    
    /**
     * Obtiene la lista de pedidos desde el estado capturado.
     * 
     * @param estadoCapturado Estado de la simulación capturado
     * @return Lista de pedidos
     */
    private static List<Pedido> obtenerPedidosDesdeEstadoCapturado(AveriaConEstadoRequest.EstadoSimulacion estadoCapturado) {
        List<Pedido> pedidos = new ArrayList<>();
        
        try {
            // Extraer pedidos de las rutas de camiones capturadas
            if (estadoCapturado.getRutasCamiones() != null) {
                for (var rutaCamion : estadoCapturado.getRutasCamiones()) {
                    if (rutaCamion.getPedidos() != null) {
                        // Convertir los pedidos del estado capturado a objetos Pedido
                        // Por ahora usamos una lista vacía como fallback
                        System.out.println("📦 Procesando " + rutaCamion.getPedidos().size() + " pedidos de camión " + rutaCamion.getId());
                    }
                }
            }
            
            System.out.println("📋 Pedidos extraídos del estado capturado: " + pedidos.size());
            
        } catch (Exception e) {
            System.err.println("❌ Error al extraer pedidos del estado capturado: " + e.getMessage());
        }
        
        return pedidos;
    }
    
    /**
     * Obtiene la lista de bloqueos desde el estado capturado.
     * 
     * @param estadoCapturado Estado de la simulación capturado
     * @return Lista de bloqueos
     */
    private static List<Bloqueo> obtenerBloqueosDesdeEstadoCapturado(AveriaConEstadoRequest.EstadoSimulacion estadoCapturado) {
        List<Bloqueo> bloqueos = new ArrayList<>();
        
        try {
            if (estadoCapturado.getBloqueos() != null) {
                // Convertir bloqueos del estado capturado
                System.out.println("🚧 Procesando " + estadoCapturado.getBloqueos().size() + " bloqueos del estado capturado");
                // Por ahora retornamos lista vacía como fallback
            }
            
            System.out.println("🚧 Bloqueos extraídos del estado capturado: " + bloqueos.size());
            
        } catch (Exception e) {
            System.err.println("❌ Error al extraer bloqueos del estado capturado: " + e.getMessage());
        }
        
        return bloqueos;
    }
    
    /**
     * Obtiene información del estado actual de la simulación
     */
    public static SimulacionInfo obtenerInfoSimulacion() {
        return new SimulacionInfo(
            GestorHistorialSimulacion.getTotalPaquetes(),
            GestorHistorialSimulacion.getPaqueteActual(),
            GestorHistorialSimulacion.isEnProceso(),
            fechaActual
        );
    }
    
    // Clase auxiliar para información de la simulación
    public static class SimulacionInfo {
        public final int totalPaquetes;
        public final int paqueteActual;
        public final boolean enProceso;
        public final LocalDateTime tiempoActual;
        
        public SimulacionInfo(int totalPaquetes, int paqueteActual, boolean enProceso, LocalDateTime tiempoActual) {
            this.totalPaquetes = totalPaquetes;
            this.paqueteActual = paqueteActual;
            this.enProceso = enProceso;
            this.tiempoActual = tiempoActual;
        }
    }

    public static void actualizarEstadoGlobal(LocalDateTime fechaActual, List<Pedido> pedidosEnviar) {
        actualizarPedidos(pedidosEnviar);
        actualizarRepositorios(fechaActual);
        actualizarCamiones(fechaActual);
        MantenimientoManager.verificarYActualizarMantenimientos(DataLoader.camiones, fechaActual);
        AveriasManager.actualizarCamionesEnAveria(fechaActual);
    }

    private static void actualizarPedidos(List<Pedido> pedidos) {
        // Borramos los pedidos del mapa
        for (int i = 0; i < Mapa.getInstance().getFilas(); i++) {
            for (int j = 0; j < Mapa.getInstance().getColumnas(); j++) {
                Nodo nodo = Mapa.getInstance().getMatriz().get(i).get(j);
                if (nodo instanceof Pedido) {
                    Nodo nodoaux = Nodo.builder().coordenada(new Coordenada(i, j)).tipoNodo(TipoNodo.NORMAL).build();
                    Mapa.getInstance().setNodo(nodo.getCoordenada(), nodoaux);
                }
            }
        }
        // Colocamos todos los nuevos pedidos en el mapa
        for (Pedido pedido : pedidos) {
            Mapa.getInstance().setNodo(pedido.getCoordenada(), pedido);
        }
    }

    private static List<Bloqueo> actualizarBloqueos(LocalDateTime fechaActual) {
        List<Bloqueo> bloqueos = DataLoader.bloqueos;
        List<Bloqueo> bloqueosActivos = new ArrayList<>();
        for (Bloqueo bloqueo : bloqueos) {
            if (bloqueo.getFechaInicio().isBefore(fechaActual) && bloqueo.getFechaFin().isAfter(fechaActual)) {
                bloqueo.activarBloqueo();
                bloqueosActivos.add(bloqueo);
            }
        }
        return bloqueosActivos;
    }

    private static void actualizarRepositorios(LocalDateTime fechaActual) {
        List<Almacen> almacenes = DataLoader.almacenes;
        if (fechaActual.getHour() == 0 && fechaActual.getMinute() == 0) {
            for (Almacen almacen : almacenes) {
                if (almacen.getTipo() == TipoAlmacen.SECUNDARIO) {
                    almacen.setCapacidadActualGLP(almacen.getCapacidadMaximaGLP());
                    almacen.setCapacidadCombustible(almacen.getCapacidadMaximaCombustible());
                }
            }
        }
    }

    private static void actualizarCamiones(LocalDateTime fechaActual) {
        List<Camion> camiones = DataLoader.camiones;

        for (Camion camion : camiones) {
            camion.actualizarEstado(Parametros.intervaloTiempo, pedidosPorAtender, pedidosPlanificados,
                    pedidosEntregados, fechaActual);
        }
    }

}
