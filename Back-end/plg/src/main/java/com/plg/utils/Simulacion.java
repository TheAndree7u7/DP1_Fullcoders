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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class Simulacion {

    private static final Logger logger = LoggerFactory.getLogger(Simulacion.class);
    
    private static List<Pedido> pedidosSemanal;
    private static LocalDateTime fechaActual;
    public static Set<Pedido> pedidosPorAtender = new LinkedHashSet<>();
    public static Set<Pedido> pedidosPlanificados = new LinkedHashSet<>();
    public static Set<Pedido> pedidosEntregados = new LinkedHashSet<>();
    public static Individuo mejorIndividuo = null;

    // Colas para simulación
    public static BlockingQueue<Object> gaTriggerQueue = new SynchronousQueue<>();
    public static BlockingQueue<IndividuoDto> gaResultQueue = new SynchronousQueue<>();
    public static Semaphore iniciar = new Semaphore(0);
    public static Semaphore continuar = new Semaphore(0);

    public static void configurarSimulacion(LocalDateTime startDate) {
        // Prueba de logging al inicio
        logger.info("=== INICIANDO CONFIGURACIÓN DE SIMULACIÓN ===");
        
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
        
        // Log de resumen importante
        logger.info("🚀 INICIO SIMULACIÓN");
        logger.info("📅 Período: {} a {}", fechaActual.toLocalDate(), fechaActual.plusDays(3).toLocalDate());
        logger.info("📦 Pedidos a procesar: {} de {} totales", pedidosSemanal.size(), DataLoader.pedidos.size());
        logger.info("🚛 Camiones disponibles: {}", DataLoader.camiones.size());
        logger.info("🏪 Almacenes: {}", DataLoader.almacenes.size());
        logger.info("==================================================");
    }

    public static void ejecutarSimulacion() {

        imprimirDatosSimulacion();
        LocalDateTime fechaLimite = Parametros.fecha_inicial.plusDays(7);
        while (!pedidosSemanal.isEmpty()) {
            Pedido pedido = pedidosSemanal.get(0);
            // Voy agregando pedidos a la lista de pedidos
            if (pedidoConFechaMenorAFechaActual(pedido, fechaActual)) {
                pedidosSemanal.remove(0);
                pedidosPorAtender.add(pedido);
            } else {
                List<Pedido> pedidosEnviar = unirPedidosSinRepetidos(pedidosPlanificados, pedidosPorAtender);
                actualizarEstadoGlobal(fechaActual, pedidosEnviar);
                List<Bloqueo> bloqueosActivos = actualizarBloqueos(fechaActual);
                logger.info("------------------------");
                logger.info("Tiempo actual: {}", fechaActual);

                if (!pedidosPorAtender.isEmpty()) {

                    
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
                } else {
                    logger.info("No hay pedidos por atender en este momento.");
                }
                for (Bloqueo bloqueo : bloqueosActivos) {
                    bloqueo.desactivarBloqueo();
                }

                fechaActual = fechaActual.plusMinutes(Parametros.intervaloTiempo);
                if (fechaActual.isEqual(fechaLimite) || fechaActual.isAfter(fechaLimite)) {
                    break;
                }
            }
        }
        logger.info("-------------------------");
        logger.info("Reporte de la simulación");
        logger.info("Kilometros recorridos: {}", Parametros.kilometrosRecorridos);
        logger.info("Fitness global: {}", Parametros.fitnessGlobal);

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
        logger.info("🔧 Verificando mantenimientos programados para: {} - INICIO DEL DÍA", fechaActual.toLocalDate());

        if (camiones == null) {
            logger.warn("[LOG] La lista de camiones es NULL");
            return;
        }
        if (camiones.isEmpty()) {
            logger.warn("[LOG] La lista de camiones está VACÍA");
        }
        int dia = fechaActual.getDayOfMonth();
        int mes = fechaActual.getMonthValue();

        for (Camion camion : camiones) {
            if (camion == null) {
                logger.warn("[LOG] Camión NULL encontrado en la lista");
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
                logger.info("   • Camión {} → EN MANTENIMIENTO", camion.getCodigo());
            } else {
                if (camion.getEstado() == com.plg.entity.EstadoCamion.EN_MANTENIMIENTO_PREVENTIVO) {
                    camion.setEstado(com.plg.entity.EstadoCamion.DISPONIBLE);
                    logger.info("   • Camión {} → DISPONIBLE (fin mantenimiento)", camion.getCodigo());
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
            logger.error("Error verificando mantenimiento para {}: {}", 
                (camion != null ? camion.getCodigo() : "null"), e.getMessage());
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
            logger.error("Error al actualizar camiones en avería: {}", e.getMessage());
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
                    logger.info("✅ Camión {} recuperado de avería TI1 - Estado: DISPONIBLE", codigoCamion);
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

                        logger.info("🚛 Camión {} trasladado al taller - Estado: EN_MANTENIMIENTO_POR_AVERIA", codigoCamion);
                    }
                }

                // Fase 2: Verificar si debe volver a estar disponible
                if (averia.getFechaHoraDisponible() != null
                        && esFechaAnteriorOIgualSinSegundos(averia.getFechaHoraDisponible(), fechaActual)) {

                    // PRIMERO: Desactivar la avería poniendo su estado en false
                    averia.setEstado(false);
                    // SEGUNDO: Modificar el camión a Habilitado (DISPONIBLE)
                    camionService.cambiarEstado(codigoCamion, com.plg.entity.EstadoCamion.DISPONIBLE);
                    logger.info("✅ Camión {} recuperado de avería {} - Estado: DISPONIBLE", 
                        codigoCamion, averia.getTipoIncidente().getCodigo());
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

}
