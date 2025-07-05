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
import com.plg.entity.Almacen;
import com.plg.entity.Bloqueo;
import com.plg.entity.Camion;
import com.plg.entity.Coordenada;
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
    
    // Control de pausa por averías
    private static volatile boolean pausadaPorAveria = false;
    private static final Object pausaLock = new Object();
    private static volatile boolean paqueteParcheDisponible = false;
    private static volatile boolean algoritmoGeneticoEnEjecucion = false;

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

    /**
     * Pausa la simulación por avería. La simulación esperará hasta que se genere el paquete parche.
     */
    public static void pausarPorAveria() {
        synchronized (pausaLock) {
            System.out.println("⏸️ SOLICITANDO PAUSA DE SIMULACIÓN por avería...");
            
            // Si hay un algoritmo genético en ejecución, esperar a que termine
            while (algoritmoGeneticoEnEjecucion) {
                System.out.println("⏳ Esperando que termine el algoritmo genético actual...");
                try {
                    Thread.sleep(100); // Esperar 100ms antes de verificar de nuevo
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            
            pausadaPorAveria = true;
            paqueteParcheDisponible = false;
            System.out.println("⏸️ SIMULACIÓN PAUSADA CONFIRMADA: Esperando paquete parche por avería");
        }
    }

    /**
     * Notifica que el paquete parche está disponible y reanuda la simulación.
     */
    public static void notificarPaqueteParcheDisponible() {
        synchronized (pausaLock) {
            paqueteParcheDisponible = true;
            pausadaPorAveria = false;
            pausaLock.notifyAll();
            System.out.println("▶️ SIMULACIÓN REANUDADA: Paquete parche disponible, consumiendo inmediatamente");
        }
    }

    /**
     * Verifica si la simulación está pausada por avería.
     */
    public static boolean estaPausadaPorAveria() {
        return pausadaPorAveria;
    }

    /**
     * Marca el inicio de la ejecución del algoritmo genético.
     */
    public static void marcarInicioAlgoritmoGenetico() {
        algoritmoGeneticoEnEjecucion = true;
    }

    /**
     * Marca el fin de la ejecución del algoritmo genético.
     */
    public static void marcarFinAlgoritmoGenetico() {
        algoritmoGeneticoEnEjecucion = false;
    }

    /**
     * Verifica si el algoritmo genético está en ejecución.
     */
    public static boolean isAlgoritmoGeneticoEnEjecucion() {
        return algoritmoGeneticoEnEjecucion;
    }

    /**
     * Espera hasta que el paquete parche esté disponible.
     */
    private static void esperarPaqueteParche() throws InterruptedException {
        synchronized (pausaLock) {
            while (pausadaPorAveria && !paqueteParcheDisponible) {
                System.out.println("⏳ Simulación esperando paquete parche...");
                pausaLock.wait(1000); // Esperar máximo 1 segundo antes de verificar de nuevo
            }
        }
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
                
                // Verificar si la simulación está pausada por avería
                try {
                    if (estaPausadaPorAveria()) {
                        System.out.println("⏸️ Simulación pausada - Esperando paquete parche...");
                    }
                    esperarPaqueteParche();
                    if (!estaPausadaPorAveria()) {
                        System.out.println("▶️ Simulación reanudada - Continuando con paquete normal");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("🛑 Simulación interrumpida mientras esperaba paquete parche");
                    GestorHistorialSimulacion.setEnProceso(false);
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
                    
                    // Verificar nuevamente si la simulación está pausada antes de ejecutar el algoritmo genético
                    if (estaPausadaPorAveria()) {
                        System.out.println("⏸️ Algoritmo genético omitido - Simulación pausada por avería");
                        continue; // Saltar esta iteración y verificar pausa de nuevo
                    }
                    
                    if (modoStandalone) {
                        // Modo standalone: ejecutar sin esperar semáforos
                        try {
                            System.out.println("🧠 Ejecutando algoritmo genético para tiempo: " + fechaActual);
                            marcarInicioAlgoritmoGenetico();
                            AlgoritmoGenetico algoritmoGenetico = new AlgoritmoGenetico(Mapa.getInstance(), pedidosEnviar);
                            algoritmoGenetico.ejecutarAlgoritmo();
                            marcarFinAlgoritmoGenetico();

                            IndividuoDto mejorIndividuoDto = new IndividuoDto(algoritmoGenetico.getMejorIndividuo(),
                                    pedidosEnviar, bloqueosActivos, fechaActual);
                            
                            // Aplicar el estado final de los camiones permanentemente
                            CamionStateApplier.aplicarEstadoFinalCamiones(algoritmoGenetico.getMejorIndividuo());
                            
                            // Agregar al historial para el frontend
                            GestorHistorialSimulacion.agregarPaquete(mejorIndividuoDto);
                        } catch (Exception e) {
                            marcarFinAlgoritmoGenetico(); // Marcar fin incluso en caso de error
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
