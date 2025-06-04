package com.plg.utils;

import java.time.LocalDateTime;
import java.util.ArrayList;

import java.util.LinkedHashSet;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.plg.config.DataLoader;
import com.plg.dto.IndividuoDto;
import com.plg.entity.Almacen;
import com.plg.entity.Bloqueo;
import com.plg.entity.Camion;
import com.plg.entity.Mapa;
import com.plg.entity.Pedido;
import com.plg.entity.TipoAlmacen;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.SynchronousQueue;

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

        System.out.println("Ejecutando simulacion...");
        System.out.println("Fecha inicial: " + fechaActual);
        System.out.println("Fecha final: " + fechaActual.plusDays(3));
        System.out.println("--------------------------");
        LocalDateTime fechaLimite = LocalDateTime.parse("2025-02-07T00:00:00");
        // Tiempo inicial de ejcución
        LocalDateTime tiempoInicial = LocalDateTime.now();

        while (!pedidosSemanal.isEmpty()) {
            Pedido pedido = pedidosSemanal.get(0);
            if (!pedido.getFechaRegistro().isAfter(fechaActual)) {
                pedidosSemanal.remove(0);
                pedidosPorAtender.add(pedido);
            } else {
                actualizarEstadoGlobal(fechaActual);
                if (!pedidosPorAtender.isEmpty()) {
                    // // Tiempo de ejecucion
                    System.out.println("------------------------");
                    System.out.println("Tiempo actual: " + fechaActual);

                    List<Pedido> pedidosEnviar = unirPedidosSinRepetidos(pedidosPlanificados, pedidosPorAtender);

                    try {
                        iniciar.acquire(); 
                        AlgoritmoGenetico algoritmoGenetico = new AlgoritmoGenetico(mapa, pedidosEnviar);
                        algoritmoGenetico.ejecutarAlgoritmo();
            
                        IndividuoDto mejorIndividuoDto = new IndividuoDto(algoritmoGenetico.getMejorIndividuo());
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

        LocalDateTime tiempoFinal = LocalDateTime.now();
        System.out.println("Tiempo final: " + tiempoInicial);
        // Tiempo de ejecución en segundos
        java.time.Duration tiempoEjecucion = java.time.Duration.between(tiempoInicial, tiempoFinal);
        // Imprimimos los kilometros totales recorridos
        System.out.println("Kilometros recorridos: " + Parametros.kilometrosRecorridos);
        System.out.println("Tiempo total de ejecución en segundos : " + tiempoEjecucion.getSeconds());
        System.out.println("Fitness global: " + Parametros.fitnessGlobal);

        System.out.println("\n=== RESUMEN FINAL DE LA SIMULACIÓN ===");
        System.out.println("📈 Métricas de rendimiento:");
        System.out.println("   • Tiempo de inicio: " + tiempoInicial);
        System.out.println("   • Tiempo de finalización: " + tiempoFinal);
        System.out.println("   • Duración total: " + tiempoEjecucion.getSeconds() + " segundos");
        System.out.println("   • Kilómetros totales recorridos: " + Parametros.kilometrosRecorridos);
        System.out.println("   • Fitness global alcanzado: " + Parametros.fitnessGlobal);
        System.out.println("\n=== FIN DE LA SIMULACIÓN ===\n");
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
        // actualizarBloqueos(fechaActual);
        actualizarCamiones(fechaActual);
    }

    private static void actualizarBloqueos(LocalDateTime fechaActual) {
        List<Bloqueo> bloqueos = DataLoader.bloqueos;
        for (Bloqueo bloqueo : bloqueos) {
            if (bloqueo.getFechaInicio().isBefore(fechaActual) && bloqueo.getFechaFin().isAfter(fechaActual)) {
                bloqueo.activarBloqueo();
            } else {
                bloqueo.desactivarBloqueo();
            }
        }
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
