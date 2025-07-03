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
            simulacionEnProceso = true;
            ConfiguracionSimulacion.imprimirDatosSimulacion();
            LocalDateTime fechaLimite = Parametros.fecha_inicial.plusDays(7);
            System.out.println("🚀 Iniciando simulación hasta: " + fechaLimite);
            System.out.println("📦 Pedidos semanales iniciales: " + pedidosSemanal.size());
            
            while (!pedidosSemanal.isEmpty() && (fechaActual.isBefore(fechaLimite) || fechaActual.isEqual(fechaLimite))) {
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
                    
                    // Crear paquete vacío para las horas sin pedidos
                    if (modoStandalone) {
                        try {
                            // Crear un individuo vacío con rutas de retorno al almacén
                            Individuo individuoVacio = crearIndividuoVacio();
                            
                            IndividuoDto paqueteVacio = new IndividuoDto(individuoVacio,
                                    new ArrayList<>(), bloqueosActivos, fechaActual);
                            
                            // Agregar al historial para el frontend
                            synchronized (historialSimulacion) {
                                contadorPaquetes++;
                                historialSimulacion.add(paqueteVacio);
                                System.out.println("📦 PAQUETE VACÍO CONSTRUIDO #" + contadorPaquetes + 
                                                 " | Tiempo: " + fechaActual + 
                                                 " | Sin pedidos activos");
                            }
                            
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
     * Cada llamada devuelve el siguiente paso en secuencia
     */
    public static IndividuoDto obtenerSiguientePaquete() {
        synchronized (historialSimulacion) {
            if (indiceActualFrontend < historialSimulacion.size()) {
                IndividuoDto paquete = historialSimulacion.get(indiceActualFrontend);
                int numeroPaquete = indiceActualFrontend + 1; // +1 porque el índice empieza en 0
                indiceActualFrontend++;
                
                System.out.println("🔥 PAQUETE CONSUMIDO #" + numeroPaquete + 
                                 " | Tiempo: " + paquete.getFechaHoraSimulacion() + 
                                 " | Total disponibles: " + historialSimulacion.size());
                
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
        MantenimientoManager.verificarYActualizarMantenimientos(DataLoader.camiones, fechaActual);
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
    private static com.plg.entity.Camion buscarCamionPorCodigo(String codigoCamion) {
        return DataLoader.camiones.stream()
                .filter(c -> c.getCodigo().equals(codigoCamion))
                .findFirst()
                .orElse(null);
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

    /**
     * Crea un individuo vacío que representa el estado de los camiones cuando no hay pedidos por atender.
     * Cada camión se mantiene en su posición actual sin realizar entregas.
     */
    private static Individuo crearIndividuoVacio() {
        List<Pedido> pedidosVacios = new ArrayList<>();
        Individuo individuoVacio = new Individuo(pedidosVacios);
        
        // Crear cromosoma con cada camión en su posición actual
        List<Gen> cromosoma = new ArrayList<>();
        for (Camion camion : DataLoader.camiones) {
            // Verificar que el camión esté disponible
            if (camion.getEstado() == com.plg.entity.EstadoCamion.DISPONIBLE) {
                Gen gen = new Gen(camion, new ArrayList<>());
                
                // Crear ruta que solo contiene la posición actual del camión
                List<Nodo> rutaActual = new ArrayList<>();
                rutaActual.add(camion); // El camión mismo es un nodo
                
                gen.setRutaFinal(rutaActual);
                gen.setPedidos(new ArrayList<>());
                gen.setFitness(0.0); // Sin recorrido, fitness = 0
                
                cromosoma.add(gen);
            }
        }
        
        individuoVacio.setCromosoma(cromosoma);
        individuoVacio.setFitness(0.0);
        
        return individuoVacio;
    }

    private static boolean pedidoConFechaMenorAFechaActual(Pedido pedido, LocalDateTime fechaActual) {
        return pedido.getFechaRegistro().isBefore(fechaActual) || pedido.getFechaRegistro().isEqual(fechaActual);
    }

}
