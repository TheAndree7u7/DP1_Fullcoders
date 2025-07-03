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
import com.plg.entity.EstadoCamion;
import com.plg.entity.Mapa;
import com.plg.entity.Nodo;
import com.plg.entity.Pedido;
import com.plg.entity.TipoAlmacen;
import com.plg.entity.TipoNodo;

public class Simulacion {

    private static List<Pedido> pedidosSemanal;
    private static LocalDateTime fechaActual;
    public static Set<Pedido> pedidosPorAtender = new LinkedHashSet<>();
    public static Set<Pedido> pedidosPlanificados = new LinkedHashSet<>();
    public static Set<Pedido> pedidosEntregados = new LinkedHashSet<>();
    public static Individuo mejorIndividuo = null;
    
    // Queue de paquetes generados para el frontend
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

    // Variables para controlar el estado post-avería
    private static boolean recalculandoPorAveria = false;
    private static LocalDateTime fechaInicioRecalculo = null;
    private static LocalDateTime fechaFinRecalculo = null;
    private static String camionAveriado = null;

    public static volatile boolean simulacionInterrumpida = false;

    public static void configurarSimulacion(LocalDateTime startDate) {
        fechaActual = startDate;
        DataLoader.initializeAlmacenes();
        DataLoader.initializeCamiones();
        try {
            DataLoader.initializeMantenimientos();
            DataLoader.initializeAverias();
            DataLoader.initializePedidos();
            DataLoader.initializeBloqueos();
        } catch (java.io.IOException | ExcepcionesPerzonalizadas.InvalidDataFormatException e) {
            e.printStackTrace();
        }
        LocalDateTime fechaFin = fechaActual.plusDays(7);
        pedidosSemanal = DataLoader.pedidos.stream()
                .filter(pedido -> pedido.getFechaRegistro().isAfter(fechaActual)
                        && pedido.getFechaRegistro().isBefore(fechaFin))
                .collect(Collectors.toList());
        System.out.println("\n=== INICIO DE LA SIMULACIÓN ===");
        System.out.println("📊 Estadísticas iniciales:");
        System.out.println("   • Total de pedidos en el sistema: " + DataLoader.pedidos.size());
        System.out.println("   • Pedidos a procesar en esta semana: " + pedidosSemanal.size());
        System.out.println("\n⏰ Configuración temporal:");
        System.out.println("   • Fecha de inicio: " + fechaActual);
        System.out.println("   • Fecha de finalización: " + fechaActual.plusDays(7));
        System.out.println("\n=== INICIANDO PROCESO DE SIMULACIÓN ===\n");
    }

    public static void ejecutarSimulacion() {
        try {
            simulacionEnProceso = true;
            imprimirDatosSimulacion();
            LocalDateTime fechaLimite = Parametros.fecha_inicial.plusDays(7);
            System.out.println("🚀 Iniciando simulación hasta: " + fechaLimite);
            System.out.println("📦 Pedidos semanales iniciales: " + pedidosSemanal.size());
            
            while (!pedidosSemanal.isEmpty() && (fechaActual.isBefore(fechaLimite) || fechaActual.isEqual(fechaLimite)) && !simulacionInterrumpida) {
                Pedido pedido = pedidosSemanal.get(0);
                // Voy agregando pedidos a la lista de pedidos
                if (pedidoConFechaMenorAFechaActual(pedido, fechaActual)) {
                    pedidosSemanal.remove(0);
                    pedidosPorAtender.add(pedido);
                } else {
                    List<Pedido> pedidosEnviar = unirPedidosSinRepetidos(pedidosPlanificados, pedidosPorAtender);
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
                                aplicarEstadoFinalCamiones(algoritmoGenetico.getMejorIndividuo());
                                
                                // Agregar al historial para el frontend
                                synchronized (historialSimulacion) {
                                    contadorPaquetes++;
                                    historialSimulacion.add(mejorIndividuoDto);
                                    System.out.println("📦 PAQUETE CONSTRUIDO #" + contadorPaquetes + 
                                                     " | Tiempo: " + fechaActual + 
                                                     " | Pedidos: " + pedidosEnviar.size() + 
                                                     " | Fitness: " + algoritmoGenetico.getMejorIndividuo().getFitness());
                                }
                                
                                gaResultQueue.offer(mejorIndividuoDto);
                            } catch (Exception e) {
                                System.err.println("❌ Error en algoritmo genético en tiempo " + fechaActual + ": " + e.getMessage());
                                e.printStackTrace();
                                // Continuar con la simulación en lugar de terminar
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
            simulacionEnProceso = false;
            System.out.println("✅ Simulación completada. Total de paquetes generados: " + historialSimulacion.size());
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
     * Maneja tanto paquetes de recálculo como de continuación normal
     */
    public static IndividuoDto obtenerSiguientePaquete() {
        synchronized (historialSimulacion) {
            if (indiceActualFrontend < historialSimulacion.size()) {
                IndividuoDto paquete = historialSimulacion.get(indiceActualFrontend);
                int numeroPaquete = indiceActualFrontend + 1;
                indiceActualFrontend++;
                
                String tipoPaquete = recalculandoPorAveria ? "[POST-AVERÍA]" : "[CONTINUACIÓN NORMAL]";
                
                System.out.println("🔥 PAQUETE CONSUMIDO #" + numeroPaquete + 
                                 " | Tiempo: " + paquete.getFechaHoraSimulacion() + 
                                 " | Total disponibles: " + historialSimulacion.size() +
                                 " | " + tipoPaquete);
                
                return paquete;
            }
            return null; // No hay más paquetes disponibles aún
        }
    }
    
    /**
     * Reinicia la reproducción desde el inicio para el frontend
     */
    public static void reiniciarReproduccion() {
        synchronized (historialSimulacion) {
            int paquetesDisponibles = historialSimulacion.size();
            indiceActualFrontend = 0;
            System.out.println("🔄 REPRODUCCIÓN REINICIADA | Volviendo al paquete #1 | Total disponibles: " + paquetesDisponibles);
        }
    }
    
    /**
     * Obtiene información del estado actual de la simulación
     */
    public static SimulacionInfo obtenerInfoSimulacion() {
        synchronized (historialSimulacion) {
            return new SimulacionInfo(
                historialSimulacion.size(),
                indiceActualFrontend,
                simulacionEnProceso,
                fechaActual
            );
        }
    }
    
    /**
     * Obtiene la fecha actual de la simulación
     */
    public static LocalDateTime obtenerFechaActual() {
        return fechaActual;
    }

    /**
     * Obtiene la fecha del pedido más antiguo sin despachar
     */
    public static LocalDateTime obtenerFechaPedidoMasAntiguo() {
        LocalDateTime fechaMasAntigua = fechaActual;
        
        // Buscar en pedidos por atender
        for (Pedido pedido : pedidosPorAtender) {
            if (pedido.getFechaRegistro().isBefore(fechaMasAntigua)) {
                fechaMasAntigua = pedido.getFechaRegistro();
            }
        }
        
        // Buscar en pedidos planificados
        for (Pedido pedido : pedidosPlanificados) {
            if (pedido.getFechaRegistro().isBefore(fechaMasAntigua)) {
                fechaMasAntigua = pedido.getFechaRegistro();
            }
        }
        
        // Buscar en pedidos semanales si los otros están vacíos
        if (pedidosPorAtender.isEmpty() && pedidosPlanificados.isEmpty() && pedidosSemanal != null) {
            for (Pedido pedido : pedidosSemanal) {
                if (pedido.getFechaRegistro().isBefore(fechaMasAntigua)) {
                    fechaMasAntigua = pedido.getFechaRegistro();
                }
            }
        }
        
        // Si aún no encontramos nada, buscar en todos los pedidos del sistema
        if (fechaMasAntigua.equals(fechaActual) && DataLoader.pedidos != null) {
            for (Pedido pedido : DataLoader.pedidos) {
                if (pedido.getFechaRegistro().isBefore(fechaMasAntigua)) {
                    fechaMasAntigua = pedido.getFechaRegistro();
                }
            }
        }
        
        System.out.println("📅 Fecha del pedido más antiguo encontrada: " + fechaMasAntigua);
        return fechaMasAntigua;
    }

    /**
     * Descarta todas las simulaciones anticipadas y genera nueva simulación
     * desde la fecha especificada hasta la fecha límite
     */
    public static boolean recalcularSimulacionPorAveria(
        LocalDateTime fechaInicio, 
        LocalDateTime fechaFin, 
        String codigoCamionAveriado
    ) {
        try {
            System.out.println("🚨 RECÁLCULO POR AVERÍA: Iniciando...");
            System.out.println("🔄 Rango: " + fechaInicio + " → " + fechaFin);
            System.out.println("🚛 Camión averiado: " + codigoCamionAveriado);
            
            // 1. Activar modo de recálculo
            recalculandoPorAveria = true;
            fechaInicioRecalculo = fechaInicio;
            fechaFinRecalculo = fechaFin;
            camionAveriado = codigoCamionAveriado;
            
            // 2. Limpiar historial de simulaciones anticipadas
            synchronized (historialSimulacion) {
                historialSimulacion.clear();
                indiceActualFrontend = 0;
                contadorPaquetes = 0;
                System.out.println("🗑️ Historial de simulaciones anticipadas descartado");
            }
            
            // 3. Restaurar estado de pedidos al momento de la avería
            restaurarEstadoPedidos(fechaInicio);
            
            // 4. Marcar camión como averiado en el estado global
            marcarCamionAveriado(codigoCamionAveriado);
            
            // 5. Generar nueva simulación desde fechaInicio hasta fechaFin
            generarNuevaSimulacion(fechaInicio, fechaFin);
            
            // 6. Verificar que se generó al menos un paquete para el frontend
            synchronized (historialSimulacion) {
                if (historialSimulacion.isEmpty()) {
                    System.out.println("⚠️ RECÁLCULO: No se generaron paquetes, creando paquete vacío para continuar...");
                    // Crear un paquete vacío para que el frontend pueda continuar
                    IndividuoDto paqueteVacio = new IndividuoDto(
                        null, // Sin individuo
                        new ArrayList<>(), // Sin pedidos
                        new ArrayList<>(), // Sin bloqueos
                        fechaActual
                    );
                    contadorPaquetes++;
                    historialSimulacion.add(paqueteVacio);
                    System.out.println("📦 PAQUETE VACÍO #" + contadorPaquetes + 
                                     " | Tiempo: " + fechaActual + 
                                     " | [POST-AVERÍA - VACÍO]");
                }
            }
            
            // 7. Desactivar modo de recálculo y continuar normalmente
            recalculandoPorAveria = false;
            fechaInicioRecalculo = null;
            fechaFinRecalculo = null;
            camionAveriado = null;
            
            System.out.println("✅ RECÁLCULO COMPLETADO: Nueva simulación generada y modo normal restaurado");
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ ERROR EN RECÁLCULO: " + e.getMessage());
            System.err.println("❌ Tipo de excepción: " + e.getClass().getSimpleName());
            System.err.println("❌ Stack trace completo:");
            e.printStackTrace();
            
            // Restaurar estado en caso de error
            recalculandoPorAveria = false;
            fechaInicioRecalculo = null;
            fechaFinRecalculo = null;
            camionAveriado = null;
            
            return false;
        }
    }

    /**
     * Restaura el estado de pedidos al momento especificado
     */
    private static void restaurarEstadoPedidos(LocalDateTime fechaRestauracion) {
        System.out.println("🔄 Restaurando estado de pedidos a: " + fechaRestauracion);
        
        // Limpiar estados actuales
        pedidosPorAtender.clear();
        pedidosPlanificados.clear();
        pedidosEntregados.clear();
        
        // Si pedidosSemanal está vacío, intentar recargar desde DataLoader
        if (pedidosSemanal == null || pedidosSemanal.isEmpty()) {
            System.out.println("⚠️ pedidosSemanal está vacío, recargando desde DataLoader...");
            try {
                // Verificar si DataLoader.pedidos ya está cargado
                if (DataLoader.pedidos == null || DataLoader.pedidos.isEmpty()) {
                    DataLoader.initializePedidos();
                }
                
                if (DataLoader.pedidos != null && !DataLoader.pedidos.isEmpty()) {
                    LocalDateTime fechaFin = fechaActual.plusDays(7);
                    pedidosSemanal = DataLoader.pedidos.stream()
                        .filter(pedido -> pedido.getFechaRegistro().isAfter(fechaActual.minusDays(1))
                                && pedido.getFechaRegistro().isBefore(fechaFin))
                        .collect(Collectors.toList());
                    System.out.println("📦 Pedidos recargados: " + pedidosSemanal.size());
                } else {
                    System.err.println("❌ No se pudieron cargar pedidos desde DataLoader");
                }
            } catch (Exception e) {
                System.err.println("❌ Error recargando pedidos: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Restaurar pedidos según la fecha
        if (pedidosSemanal != null) {
            for (Pedido pedido : pedidosSemanal) {
                if (pedido.getFechaRegistro().isBefore(fechaRestauracion) || 
                    pedido.getFechaRegistro().isEqual(fechaRestauracion)) {
                    
                    if (pedido.getEstado() == com.plg.entity.EstadoPedido.ENTREGADO) {
                        pedidosEntregados.add(pedido);
                    } else if (pedido.getEstado() == com.plg.entity.EstadoPedido.PLANIFICADO) {
                        pedidosPlanificados.add(pedido);
                    } else {
                        pedidosPorAtender.add(pedido);
                    }
                }
            }
        }
        
        System.out.println("📊 Estado restaurado: " + 
                          pedidosPorAtender.size() + " por atender, " +
                          pedidosPlanificados.size() + " planificados, " +
                          pedidosEntregados.size() + " entregados");
    }

    /**
     * Marca un camión como averiado en el estado global
     */
    private static void marcarCamionAveriado(String codigoCamion) {
        for (Camion camion : DataLoader.camiones) {
            if (camion.getCodigo().equals(codigoCamion)) {
                camion.setEstado(EstadoCamion.EN_MANTENIMIENTO_POR_AVERIA);
                System.out.println("🚨 Camión " + codigoCamion + " marcado como averiado");
                break;
            }
        }
    }

    /**
     * Genera nueva simulación desde fechaInicio hasta fechaFin
     * y continúa con el flujo normal después
     */
    private static void generarNuevaSimulacion(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        System.out.println("🚀 Generando nueva simulación...");
        
        // Configurar fecha actual para el recálculo
        LocalDateTime fechaOriginal = fechaActual;
        fechaActual = fechaInicio;
        
        try {
            // Ejecutar simulación solo para el rango especificado
            while (fechaActual.isBefore(fechaFin) && !pedidosPorAtender.isEmpty()) {
                
                // Procesar pedidos que lleguen en este momento
                procesarPedidosEnMomento();
                
                // Generar solución si hay pedidos por atender
                if (!pedidosPorAtender.isEmpty()) {
                    List<Pedido> pedidosEnviar = unirPedidosSinRepetidos(pedidosPlanificados, pedidosPorAtender);
                    List<Bloqueo> bloqueosActivos = actualizarBloqueos(fechaActual);
                    
                    AlgoritmoGenetico algoritmoGenetico = new AlgoritmoGenetico(Mapa.getInstance(), pedidosEnviar);
                    algoritmoGenetico.ejecutarAlgoritmo();
                    
                    IndividuoDto mejorIndividuoDto = new IndividuoDto(
                        algoritmoGenetico.getMejorIndividuo(),
                        pedidosEnviar, 
                        bloqueosActivos, 
                        fechaActual
                    );
                    
                    // Agregar al nuevo historial
                    synchronized (historialSimulacion) {
                        contadorPaquetes++;
                        historialSimulacion.add(mejorIndividuoDto);
                        System.out.println("📦 NUEVO PAQUETE #" + contadorPaquetes + 
                                         " | Tiempo: " + fechaActual + 
                                         " | Pedidos: " + pedidosEnviar.size() +
                                         " | [POST-AVERÍA]");
                    }
                    
                    aplicarEstadoFinalCamiones(algoritmoGenetico.getMejorIndividuo());
                }
                
                // Avanzar al siguiente intervalo
                fechaActual = fechaActual.plusMinutes(Parametros.intervaloTiempo);
            }
            
            // IMPORTANTE: Continuar con la simulación normal desde fechaFin
            System.out.println("🔄 Continuando simulación normal desde: " + fechaFin);
            continuarSimulacionNormal(fechaFin);
            
        } finally {
            // Restaurar fecha original solo si no estamos en modo recálculo
            if (!recalculandoPorAveria) {
                fechaActual = fechaOriginal;
            }
        }
    }

    /**
     * Continúa la simulación normal desde la fecha especificada
     * Genera paquetes anticipados como antes
     */
    private static void continuarSimulacionNormal(LocalDateTime fechaContinuacion) {
        System.out.println("🔄 CONTINUACIÓN NORMAL: Generando paquetes anticipados desde " + fechaContinuacion);
        
        fechaActual = fechaContinuacion;
        
        // Continuar generando paquetes como en el flujo normal
        while ((!pedidosSemanal.isEmpty() || !pedidosPorAtender.isEmpty()) && fechaActual.isBefore(Parametros.fecha_inicial.plusDays(7))) {
            
            // Si hay pedidos semanales, procesarlos primero
            if (!pedidosSemanal.isEmpty()) {
                Pedido pedido = pedidosSemanal.get(0);
                
                if (pedidoConFechaMenorAFechaActual(pedido, fechaActual)) {
                    pedidosSemanal.remove(0);
                    pedidosPorAtender.add(pedido);
                    continue; // Continuar con el siguiente pedido
                }
            }
            
            // Si llegamos aquí, procesar los pedidos por atender
            if (!pedidosPorAtender.isEmpty()) {
                List<Pedido> pedidosEnviar = unirPedidosSinRepetidos(pedidosPlanificados, pedidosPorAtender);
                actualizarEstadoGlobal(fechaActual, pedidosEnviar);
                List<Bloqueo> bloqueosActivos = actualizarBloqueos(fechaActual);
                
                try {
                    AlgoritmoGenetico algoritmoGenetico = new AlgoritmoGenetico(Mapa.getInstance(), pedidosEnviar);
                    algoritmoGenetico.ejecutarAlgoritmo();

                    IndividuoDto mejorIndividuoDto = new IndividuoDto(algoritmoGenetico.getMejorIndividuo(),
                            pedidosEnviar, bloqueosActivos, fechaActual);
                    
                    aplicarEstadoFinalCamiones(algoritmoGenetico.getMejorIndividuo());
                    
                    // Agregar al historial para el frontend (continuación normal)
                    synchronized (historialSimulacion) {
                        contadorPaquetes++;
                        historialSimulacion.add(mejorIndividuoDto);
                        System.out.println("📦 PAQUETE CONTINUACIÓN #" + contadorPaquetes + 
                                         " | Tiempo: " + fechaActual + 
                                         " | Pedidos: " + pedidosEnviar.size() + 
                                         " | Fitness: " + algoritmoGenetico.getMejorIndividuo().getFitness() +
                                         " | [CONTINUACIÓN NORMAL]");
                    }
                    
                } catch (Exception e) {
                    System.err.println("❌ Error en algoritmo genético en tiempo " + fechaActual + ": " + e.getMessage());
                    e.printStackTrace();
                }
                
                for (Bloqueo bloqueo : bloqueosActivos) {
                    bloqueo.desactivarBloqueo();
                }
            }
            
            // Si no hay pedidos por atender ni semanales, salir del bucle
            if (pedidosPorAtender.isEmpty() && pedidosSemanal.isEmpty()) {
                break;
            }

            fechaActual = fechaActual.plusMinutes(Parametros.intervaloTiempo);
        }
        
        System.out.println("✅ CONTINUACIÓN NORMAL: Simulación completada hasta el final");
    }

    /**
     * Procesa pedidos que llegan en el momento actual
     */
    private static void procesarPedidosEnMomento() {
        List<Pedido> pedidosParaProcesar = new ArrayList<>();
        
        for (Pedido pedido : pedidosSemanal) {
            if (pedidoConFechaMenorAFechaActual(pedido, fechaActual)) {
                pedidosParaProcesar.add(pedido);
            }
        }
        
        // Remover de pedidosSemanal y agregar a pedidosPorAtender
        for (Pedido pedido : pedidosParaProcesar) {
            pedidosSemanal.remove(pedido);
            pedidosPorAtender.add(pedido);
        }
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

    /**
     * Aplica el estado final de los camiones después de una solución exitosa
     * Esto mantiene la continuidad de posiciones entre paquetes
     */
    private static void aplicarEstadoFinalCamiones(Individuo mejorIndividuo) {
        try {
            for (Gen gen : mejorIndividuo.getCromosoma()) {
                Camion camion = gen.getCamion();
                
                // Obtener la posición final del camión después de ejecutar su ruta
                if (gen.getRutaFinal() != null && !gen.getRutaFinal().isEmpty()) {
                    // La última posición de la ruta final es donde terminó el camión
                    Nodo posicionFinal = gen.getRutaFinal().get(gen.getRutaFinal().size() - 1);
                    
                    // Actualizar la posición del camión en DataLoader.camiones
                    for (Camion camionGlobal : DataLoader.camiones) {
                        if (camionGlobal.getCodigo().equals(camion.getCodigo())) {
                            Coordenada nuevaPosicion = posicionFinal.getCoordenada();
                            camionGlobal.setCoordenada(nuevaPosicion);
                            
                            // También actualizar el estado de combustible y GLP
                            camionGlobal.setCombustibleActual(camion.getCombustibleActual());
                            camionGlobal.setCapacidadActualGLP(camion.getCapacidadActualGLP());
                            
                            System.out.println("🚛 POSICIÓN ACTUALIZADA: " + camion.getCodigo() + 
                                             " → " + nuevaPosicion + 
                                             " | Combustible: " + String.format("%.2f", camion.getCombustibleActual()) +
                                             " | GLP: " + String.format("%.2f", camion.getCapacidadActualGLP()) +
                                             " | Distancia Máx: " + String.format("%.2f", camion.getDistanciaMaxima()));
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error aplicando estado final de camiones: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static List<Pedido> unirPedidosSinRepetidos(Set<Pedido> set1, Set<Pedido> set2) {
        List<Pedido> listaUnida = new ArrayList<>(set1);
        for (Pedido pedido : set2) {
            if (!listaUnida.contains(pedido)) {
                listaUnida.add(pedido);
            }
        }
        return listaUnida;
    }

    public static void actualizarEstadoGlobal(LocalDateTime fechaActual, List<Pedido> pedidosEnviar) {
        actualizarPedidos(pedidosEnviar);
        actualizarRepositorios(fechaActual);
        actualizarCamiones(fechaActual);
        verificarYActualizarMantenimientos(DataLoader.camiones, fechaActual);
        actualizarCamionesEnAveria(fechaActual);
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

    /**
     * Verifica mantenimientos programados y actualiza estados de camiones Se
     * ejecuta al inicio del día (00:00) y actualiza TODOS los camiones
     */
    private static void verificarYActualizarMantenimientos(List<Camion> camiones, LocalDateTime fechaActual) {

        if (fechaActual.getHour() != 0 && fechaActual.getMinute() != 0) {
            return; // Solo se ejecuta al inicio del día
        }
        System.out.println(
                "🔧 Verificando mantenimientos programados para: " + fechaActual.toLocalDate() + " - INICIO DEL DÍA");

        if (camiones == null) {
            System.out.println("[LOG] La lista de camiones es NULL");
            return;
        }
        if (camiones.isEmpty()) {
            System.out.println("[LOG] La lista de camiones está VACÍA");
        }
        int dia = fechaActual.getDayOfMonth();
        int mes = fechaActual.getMonthValue();

        for (Camion camion : camiones) {
            if (camion == null) {
                System.out.println("[LOG] Camión NULL encontrado en la lista");
                continue;
            }
            // Log de mantenimientos asociados a este camión
            long mantenimientosCount = com.plg.config.DataLoader.mantenimientos.stream()
                    .filter(m -> m.getCamion() != null && m.getCamion().getCodigo().equals(camion.getCodigo()))
                    .count();
            // System.out.println("[LOG] Camión " + camion.getCodigo() + " tiene " +
            // mantenimientosCount + " mantenimientos registrados en
            // DataLoader.mantenimientos");

            boolean resultado = tieneMantenimientoProgramado(camion, dia, mes);
            // System.out.println("[LOG] ¿Camión " + camion.getCodigo() + " tiene
            // mantenimiento el " + dia + "/" + mes + "? " + resultado);
            if (resultado) {
                camion.setEstado(com.plg.entity.EstadoCamion.EN_MANTENIMIENTO_PREVENTIVO);
                System.out.println("   • Camión " + camion.getCodigo() + " → EN MANTENIMIENTO");
            } else {
                if (camion.getEstado() == com.plg.entity.EstadoCamion.EN_MANTENIMIENTO_PREVENTIVO) {
                    camion.setEstado(com.plg.entity.EstadoCamion.DISPONIBLE);
                    System.out.println("   • Camión " + camion.getCodigo() + " → DISPONIBLE (fin mantenimiento)");
                }
            }
        }
    }

    /**
     * Verifica si un camión tiene mantenimiento programado usando la lógica de
     * ciclos
     */
    private static boolean tieneMantenimientoProgramado(Camion camion, int dia, int mes) {
        try {
            if (camion == null) {
                System.out.println("[LOG] tieneMantenimientoProgramado: Camión es NULL");
                return false;
            }
            // Buscar el primer mantenimiento del camión en los datos cargados
            return com.plg.config.DataLoader.mantenimientos.stream()
                    .filter(m -> m.getCamion() != null
                            && m.getCamion().getCodigo().equals(camion.getCodigo()))
                    .findFirst()
                    .map(primerMantenimiento -> {
                        // Verificar si el día coincide
                        if (primerMantenimiento.getDia() != dia) {
                            // System.out.println("[LOG] Camión " + camion.getCodigo() + ": Día no coincide.
                            // Esperado: " + primerMantenimiento.getDia() + ", Recibido: " + dia);
                            return false;
                        }
                        int mesInicial = primerMantenimiento.getMes();
                        int diferenciaMeses = Math.abs(mes - mesInicial);
                        boolean ciclo = diferenciaMeses % 2 == 0;
                        // System.out.println("[LOG] Camión " + camion.getCodigo() + ": Mes inicial: " +
                        // mesInicial + ", Mes consultado: " + mes + ", Diferencia: " + diferenciaMeses
                        // + ", ¿En ciclo?: " + ciclo);
                        return ciclo;
                    })
                    .orElseGet(() -> {
                        // System.out.println("[LOG] Camión " + camion.getCodigo() + ": No se encontró
                        // mantenimiento base");
                        return false;
                    });
        } catch (Exception e) {
            System.err.println("Error verificando mantenimiento para " + (camion != null ? camion.getCodigo() : "null")
                    + ": " + e.getMessage());
            return false;
        }
    }

    private static void imprimirDatosSimulacion() {
        System.out.println("Datos de la simulación:");
        System.out.println("Fecha inicial: " + Parametros.fecha_inicial);
        System.out.println("Intervalo de tiempo: " + Parametros.intervaloTiempo + " minutos");
        System.out.println("Cantidad de pedidos semanales: " + pedidosSemanal.size());
        System.out.println("Cantidad de almacenes: " + DataLoader.almacenes.size());
        System.out.println("Cantidad de camiones: " + DataLoader.camiones.size());
        // ! ACTUALIZAR CAMIONES SEGUN SU MANTENIMIENTO
        // ?SOLO SI LA FECHA ACTUAL ES EL INICIO DEL DÍA
        // VERIFICAR MANTENIMIENTOS: Solo una vez al inicio del día (00:00)
        // Actualiza TODOS los camiones según corresponda
        if (fechaActual.getHour() == 0 && fechaActual.getMinute() == 0) {
            // Inicializar la lista de camuiones
            List<Camion> camiones_en_mantenimiento = new ArrayList<>();
            // COLCAR UN LOG
            System.out.println("🔧 Verificando mantenimientos programados para: " + fechaActual.toLocalDate());
            // OBTENER LOS CAMUIONES
            if (DataLoader.camiones != null) {
                camiones_en_mantenimiento = DataLoader.camiones;
            } else {
                System.out.println("[LOG] La lista de camiones es NULL");
            }
            verificarYActualizarMantenimientos(camiones_en_mantenimiento, fechaActual);
        }

        // !ACTULIZAR EL MANTENIMIENTO
    }

    private static boolean pedidoConFechaMenorAFechaActual(Pedido pedido, LocalDateTime fechaActual) {
        return pedido.getFechaRegistro().isBefore(fechaActual) || pedido.getFechaRegistro().isEqual(fechaActual);
    }

    /**
     * Actualiza los estados de camiones con averías activas usando el
     * AveriaService. Sigue la lógica específica de los tipos de avería y sus
     * fechas reales.
     */
    private static void actualizarCamionesEnAveria(LocalDateTime fechaActual) {
        try {
            // Crear instancias de los servicios necesarios
            com.plg.repository.AveriaRepository averiaRepository = new com.plg.repository.AveriaRepository();
            com.plg.repository.CamionRepository camionRepository = new com.plg.repository.CamionRepository();
            com.plg.service.CamionService camionService = new com.plg.service.CamionService(camionRepository);
            com.plg.service.AveriaService averiaService = new com.plg.service.AveriaService(averiaRepository,
                    camionService);

            // Obtener todas las averías activas
            java.util.List<com.plg.entity.Averia> averiasActivas = averiaService.listarActivas();

            // 1. Procesar averías que NO requieren traslado (TI1)
            procesarAveriasNoRequierenTraslado(averiasActivas, fechaActual, camionService);

            // 2. Procesar averías que requieren traslado (TI2, TI3)
            procesarAveriasRequierenTraslado(averiasActivas, fechaActual, camionService);

        } catch (Exception e) {
            System.err.println("Error al actualizar camiones en avería: " + e.getMessage());
        }
    }

    /**
     * 1. Lista todas las averías que NO requieren traslado 2. En esas averías
     * si su fecha hora disponible es menor a la fecha actual sin contar los
     * segundos 3. Actualizar el estado del camión a disponible
     */
    private static void procesarAveriasNoRequierenTraslado(java.util.List<com.plg.entity.Averia> averiasActivas,
            LocalDateTime fechaActual,
            com.plg.service.CamionService camionService) {
        for (com.plg.entity.Averia averia : averiasActivas) {
            if (averia.getTipoIncidente() != null && !averia.getTipoIncidente().isRequiereTraslado()) {
                // Verificar si la fecha hora disponible es menor a la fecha actual (sin
                // segundos)
                if (averia.getFechaHoraDisponible() != null
                        && esFechaAnteriorSinSegundos(averia.getFechaHoraDisponible(), fechaActual)) {

                    String codigoCamion = averia.getCamion().getCodigo();
                    // PRIMERO: Desactivar la avería poniendo su estado en false
                    averia.setEstado(false);
                    // SEGUNDO: Actualizar el estado del camión a disponible
                    camionService.cambiarEstado(codigoCamion, com.plg.entity.EstadoCamion.DISPONIBLE);
                    System.out.println("✅ Camión " + codigoCamion + " recuperado de avería TI1 - Estado: DISPONIBLE");
                }
            }
        }
    }

    /**
     * 1. Lista todas las averías que requieren traslado 2. En esas averías si
     * su fecha hora fin espera en ruta es menor a la fecha actual sin contar
     * los segundos 3. Actualizar el estado del camión a
     * EN_MANTENIMIENTO_POR_AVERIA y cambiar su posición al almacén central 4.
     * Si su fecha hora disponible es menor o igual a la fechaActual entonces
     * modifica ese camión a Habilitado
     */
    private static void procesarAveriasRequierenTraslado(java.util.List<com.plg.entity.Averia> averiasActivas,
            LocalDateTime fechaActual,
            com.plg.service.CamionService camionService) {
        for (com.plg.entity.Averia averia : averiasActivas) {
            if (averia.getTipoIncidente() != null && averia.getTipoIncidente().isRequiereTraslado()) {
                String codigoCamion = averia.getCamion().getCodigo();

                // Fase 1: Verificar si debe trasladarse al taller
                if (averia.getFechaHoraFinEsperaEnRuta() != null
                        && esFechaAnteriorSinSegundos(averia.getFechaHoraFinEsperaEnRuta(), fechaActual)) {

                    // Verificar si el camión aún está en el lugar de la avería
                    com.plg.entity.Camion camion = buscarCamionPorCodigo(codigoCamion);
                    if (camion != null && camion.getEstado() == com.plg.entity.EstadoCamion.INMOVILIZADO_POR_AVERIA) {
                        // Actualizar estado a EN_MANTENIMIENTO_POR_AVERIA
                        camionService.cambiarEstado(codigoCamion,
                                com.plg.entity.EstadoCamion.EN_MANTENIMIENTO_POR_AVERIA);

                        // Cambiar posición al almacén central
                        com.plg.entity.Coordenada coordenadaAlmacenCentral = obtenerCoordenadaAlmacenCentral();
                        camionService.cambiarCoordenada(codigoCamion, coordenadaAlmacenCentral);

                        System.out.println("🚛 Camión " + codigoCamion
                                + " trasladado al taller - Estado: EN_MANTENIMIENTO_POR_AVERIA");
                    }
                }

                // Fase 2: Verificar si debe volver a estar disponible
                if (averia.getFechaHoraDisponible() != null
                        && esFechaAnteriorOIgualSinSegundos(averia.getFechaHoraDisponible(), fechaActual)) {

                    // PRIMERO: Desactivar la avería poniendo su estado en false
                    averia.setEstado(false);
                    // SEGUNDO: Modificar el camión a Habilitado (DISPONIBLE)
                    camionService.cambiarEstado(codigoCamion, com.plg.entity.EstadoCamion.DISPONIBLE);
                    System.out.println("✅ Camión " + codigoCamion + " recuperado de avería "
                            + averia.getTipoIncidente().getCodigo() + " - Estado: DISPONIBLE");
                }
            }
        }
    }

    /**
     * Busca un camión por su código en la lista de camiones de DataLoader.
     */
    public static com.plg.entity.Camion buscarCamionPorCodigo(String codigoCamion) {
        for (com.plg.entity.Camion camion : com.plg.config.DataLoader.camiones) {
            if (camion.getCodigo().equals(codigoCamion)) {
                return camion;
            }
        }
        return null;
    }

    /**
     * Compara dos fechas ignorando los segundos - fecha1 < fecha2.
     */
    private static boolean esFechaAnteriorSinSegundos(LocalDateTime fecha1, LocalDateTime fecha2) {
        LocalDateTime fecha1Truncada = fecha1.withSecond(0).withNano(0);
        LocalDateTime fecha2Truncada = fecha2.withSecond(0).withNano(0);
        return fecha1Truncada.isBefore(fecha2Truncada);
    }

    /**
     * Compara dos fechas ignorando los segundos - fecha1 <= fecha2.
     */
    private static boolean esFechaAnteriorOIgualSinSegundos(LocalDateTime fecha1, LocalDateTime fecha2) {
        LocalDateTime fecha1Truncada = fecha1.withSecond(0).withNano(0);
        LocalDateTime fecha2Truncada = fecha2.withSecond(0).withNano(0);
        return fecha1Truncada.isBefore(fecha2Truncada) || fecha1Truncada.isEqual(fecha2Truncada);
    }

    /**
     * Obtiene la coordenada del almacén central.
     */
    private static com.plg.entity.Coordenada obtenerCoordenadaAlmacenCentral() {
        return DataLoader.almacenes.stream()
                .filter(almacen -> almacen.getTipo() == com.plg.entity.TipoAlmacen.CENTRAL)
                .map(almacen -> almacen.getCoordenada())
                .findFirst()
                .orElse(new com.plg.entity.Coordenada(8, 12)); // Coordenada por defecto
    }

}
