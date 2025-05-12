package com.plg.utils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import com.plg.config.DataLoader;
import com.plg.entity.Almacen;
import com.plg.entity.Bloqueo;
import com.plg.entity.Mapa;
import com.plg.entity.Pedido;
import com.plg.entity.TipoAlmacen;

public class Simulacion {

    private static List<Pedido> pedidosSemanal;
    private static Mapa mapa = Mapa.getInstance();
    private static LocalDateTime fechaActual;
    public static ArrayList<Pedido> pedidosPorAtender = new ArrayList<>();
    public static LinkedList<Pedido> pedidosPlanificados = new LinkedList<>();
    public static ArrayList<Pedido> pedidosEntregados = new ArrayList<>();

    public static void configurarSimulacion(LocalDateTime startDate) {

        fechaActual = startDate;
        DataLoader.initializeAlmacenes();
        DataLoader.initializeCamiones();
        DataLoader.initializeMantenimientos();
        DataLoader.initializeAverias();
        LocalDateTime fechaFin = fechaActual.plusDays(7);
        pedidosSemanal = DataLoader.pedidos.stream()
                .filter(pedido -> pedido.getFechaRegistro().isAfter(fechaActual)
                        && pedido.getFechaRegistro().isBefore(fechaFin))
                .collect(Collectors.toList());

    }

    public static void ejecutarSimulacion() {
        actualizarBloqueos(fechaActual);

        while (!pedidosSemanal.isEmpty()) {
            Pedido pedido = pedidosSemanal.get(0);
            if (!pedido.getFechaRegistro().isAfter(fechaActual)) {
                pedidosSemanal.remove(0);
                pedidosPorAtender.add(pedido);
            } else {
                actualizarEstadoGlobal(fechaActual);
                if (!pedidosPorAtender.isEmpty()) {
                    AlgoritmoGenetico algoritmoGenetico = new AlgoritmoGenetico(mapa, pedidosPorAtender,
                            DataLoader.camiones, DataLoader.almacenes);
                    algoritmoGenetico.ejecutarAlgoritmo();
                }
                fechaActual = fechaActual.plusMinutes(Parametros.intervaloTiempo);
            }
        }
    }

    public static void actualizarEstadoGlobal(LocalDateTime fechaActual){
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
        
    }

}
