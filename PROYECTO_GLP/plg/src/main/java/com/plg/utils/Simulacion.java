package com.plg.utils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import com.plg.config.DataLoader;
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

    public static void configurarSimulacion(LocalDateTime startDate) {
        fechaActual = startDate;
        DataLoader.initializeAlmacenes();
        DataLoader.initializeCamiones();
        DataLoader.initializeMantenimientos();
        DataLoader.initializeAverias();
        DataLoader.initializePedidos();
        LocalDateTime fechaFin = fechaActual.plusDays(7);
        pedidosSemanal = DataLoader.pedidos.stream()
                .filter(pedido -> pedido.getFechaRegistro().isAfter(fechaActual)
                        && pedido.getFechaRegistro().isBefore(fechaFin))
                .collect(Collectors.toList());
        System.out.println("Pedidos totales: " + DataLoader.pedidos.size());
        System.out.println("Pedidos cargados en la semana: " + pedidosSemanal.size());
    }

    public static void ejecutarSimulacion() {

        System.out.println("Ejecutando simulacion...");
        System.out.println("Fecha inicial: " + fechaActual);
        System.out.println("Fecha final: " + fechaActual.plusDays(7));

        List<Camion> camiones = DataLoader.camiones;
        actualizarBloqueos(fechaActual);
        while (!pedidosSemanal.isEmpty()) {
            Pedido pedido = pedidosSemanal.get(0);
            if (!pedido.getFechaRegistro().isAfter(fechaActual)) {
                pedidosSemanal.remove(0);
                pedidosPorAtender.add(pedido);
            } else {
                actualizarEstadoGlobal(fechaActual);
                if (!pedidosPorAtender.isEmpty()) {
                    List<Pedido> pedidosEnviar = unirPedidosSinRepetidos(pedidosPlanificados, pedidosPorAtender);
                    AlgoritmoGenetico algoritmoGenetico = new AlgoritmoGenetico(mapa, pedidosEnviar);
                    algoritmoGenetico.ejecutarAlgoritmo();
                }
                fechaActual = fechaActual.plusMinutes(Parametros.intervaloTiempo);
            }
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


    public static void actualizarEstadoGlobal(LocalDateTime fechaActual) {
        actualizarRepositorios(fechaActual);
        actualizarBloqueos(fechaActual);
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
                }
            }
        }
    }

    private static void actualizarCamiones(LocalDateTime fechaActual) {
        List<Camion> camiones = DataLoader.camiones;
        for (Camion camion : camiones) {
            camion.actualizarEstado(Parametros.intervaloTiempo, pedidosPorAtender, pedidosPlanificados, pedidosEntregados);
        }
    }

}
