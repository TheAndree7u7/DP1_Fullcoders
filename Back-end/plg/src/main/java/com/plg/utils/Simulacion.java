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
import com.plg.entity.Mapa;
import com.plg.entity.Pedido;
import com.plg.entity.TipoAlmacen;

public class Simulacion {

    private static List<Pedido> pedidosSemanal;
    private static Mapa mapa = Mapa.getInstance();
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
        System.out.println("   • Fecha de finalización: " + fechaActual.plusDays(3));
        System.out.println("\n=== INICIANDO PROCESO DE SIMULACIÓN ===\n");
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
                List<Bloqueo> bloqueosActivos = actualizarBloqueos(fechaActual);
                actualizarEstadoGlobal(fechaActual);
                if (!pedidosPorAtender.isEmpty()) {
                    System.out.println("------------------------");
                    System.out.println("Tiempo actual: " + fechaActual);

                    //! ACTUALIZAR CAMIONES SEGUN SU MANTENIMIENTO 
                    //?SOLO SI LA FECHA ACTUAL ES EL INICIO DEL DÍA
                    // VERIFICAR MANTENIMIENTOS: Solo una vez al inicio del día (00:00)
                    // Actualiza TODOS los camiones según corresponda
                    if (fechaActual.getHour() == 0 && fechaActual.getMinute() == 0) {
                        //Inicializar la lista de camuiones
                        List<Camion> camiones = new ArrayList<>();
                        //COLCAR UN LOG 
                        System.out.println("🔧 Verificando mantenimientos programados para: " + fechaActual.toLocalDate());
                        //OBTENER LOS CAMUIONES
                        camiones = DataLoader.camiones;
                        verificarYActualizarMantenimientos(camiones, fechaActual);
                    }

                    //!ACTULIZAR EL MANTENIMIENTO 
                    List<Pedido> pedidosEnviar = unirPedidosSinRepetidos(pedidosPlanificados, pedidosPorAtender);
                    try {
                        iniciar.acquire();
                        AlgoritmoGenetico algoritmoGenetico = new AlgoritmoGenetico(mapa, pedidosEnviar);
                        algoritmoGenetico.ejecutarAlgoritmo();
                        // SimulatedAnnealing simulatedAnnealing = new SimulatedAnnealing(mapa, pedidosEnviar);
                        // simulatedAnnealing.ejecutarAlgoritmo();          
                        IndividuoDto mejorIndividuoDto = new IndividuoDto(algoritmoGenetico.getMejorIndividuo(), pedidosEnviar, bloqueosActivos);
                        gaResultQueue.offer(mejorIndividuoDto);
                        continuar.acquire();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        System.err.println("Error al esperar el disparador del algoritmo genético: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                fechaActual = fechaActual.plusMinutes(Parametros.intervaloTiempo);
                if (fechaActual.isEqual(fechaLimite) || fechaActual.isAfter(fechaLimite)) {
                    break;
                }
            }
        }
        System.out.println("-------------------------");
        System.out.println("Reporte de la simulación");
        System.out.println("Kilometros recorridos: " + Parametros.kilometrosRecorridos);
        System.out.println("Fitness global: " + Parametros.fitnessGlobal);

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

    public static void actualizarEstadoGlobal(LocalDateTime fechaActual) {
        actualizarRepositorios(fechaActual);
        actualizarCamiones(fechaActual);
    }

    private static List<Bloqueo> actualizarBloqueos(LocalDateTime fechaActual) {
        List<Bloqueo> bloqueos = DataLoader.bloqueos;
        List<Bloqueo> bloqueosActivos = new ArrayList<>();
        for (Bloqueo bloqueo : bloqueos) {
            if (bloqueo.getFechaInicio().isBefore(fechaActual) && bloqueo.getFechaFin().isAfter(fechaActual)) {
                bloqueo.activarBloqueo();
                bloqueosActivos.add(bloqueo);
            } else {
                bloqueo.desactivarBloqueo();
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
        System.out.println("🔧 Verificando mantenimientos programados para: " + fechaActual.toLocalDate() + " - INICIO DEL DÍA");

        int dia = fechaActual.getDayOfMonth();
        int mes = fechaActual.getMonthValue();

        for (Camion camion : camiones) {
            if (tieneMantenimientoProgramado(camion, dia, mes)) {
                // Camión debe estar en mantenimiento HOY
                camion.setEstado(com.plg.entity.EstadoCamion.EN_MANTENIMIENTO_PREVENTIVO);
                System.out.println("   • Camión " + camion.getCodigo() + " → EN MANTENIMIENTO");
            } else {
                // Camión NO debe estar en mantenimiento HOY - asegurar que esté disponible
                if (camion.getEstado() == com.plg.entity.EstadoCamion.EN_MANTENIMIENTO_PREVENTIVO) {
                    camion.setEstado(com.plg.entity.EstadoCamion.DISPONIBLE);
                    System.out.println("   • Camión " + camion.getCodigo() + " → DISPONIBLE (fin mantenimiento)");
                }
                // Si ya estaba disponible u otro estado, no cambiar nada
            }
        }
    }

    /**
     * Verifica si un camión tiene mantenimiento programado usando la lógica de
     * ciclos
     */
    private static boolean tieneMantenimientoProgramado(Camion camion, int dia, int mes) {
        try {
            // Buscar el primer mantenimiento del camión en los datos cargados
            return DataLoader.mantenimientos.stream()
                    .filter(m -> m.getCamion() != null
                    && m.getCamion().getCodigo().equals(camion.getCodigo()))
                    .findFirst()
                    .map(primerMantenimiento -> {
                        // Verificar si el día coincide
                        if (primerMantenimiento.getDia() != dia) {
                            return false;
                        }

                        // Calcular si el mes está en el ciclo (cada 2 meses)
                        int mesInicial = primerMantenimiento.getMes();
                        int diferenciaMeses = Math.abs(mes - mesInicial);
                        return diferenciaMeses % 2 == 0;
                    })
                    .orElse(false);

        } catch (Exception e) {
            System.err.println("Error verificando mantenimiento para " + camion.getCodigo() + ": " + e.getMessage());
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
        //! ACTUALIZAR CAMIONES SEGUN SU MANTENIMIENTO 
        //?SOLO SI LA FECHA ACTUAL ES EL INICIO DEL DÍA
        // VERIFICAR MANTENIMIENTOS: Solo una vez al inicio del día (00:00)
        // Actualiza TODOS los camiones según corresponda
        if (fechaActual.getHour() == 0 && fechaActual.getMinute() == 0) {
            //Inicializar la lista de camuiones
            List<Camion> camiones = new ArrayList<>();
            //COLCAR UN LOG 
            System.out.println("🔧 Verificando mantenimientos programados para: " + fechaActual.toLocalDate());
            //OBTENER LOS CAMUIONES
            camiones = DataLoader.camiones;
            verificarYActualizarMantenimientos(camiones, fechaActual);
        }

        //!ACTULIZAR EL MANTENIMIENTO 
    }

    private static boolean pedidoConFechaMenorAFechaActual(Pedido pedido, LocalDateTime fechaActual) {
        return pedido.getFechaRegistro().isBefore(fechaActual) || pedido.getFechaRegistro().isEqual(fechaActual);
    }

}
