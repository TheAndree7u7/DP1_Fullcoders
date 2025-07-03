package com.plg.utils.simulacion;

import com.plg.entity.Almacen;
import com.plg.entity.Bloqueo;
import com.plg.entity.Camion;
import com.plg.entity.Pedido;
import com.plg.entity.TipoAlmacen;
import com.plg.entity.TipoNodo;
import com.plg.entity.Nodo;
import com.plg.entity.Coordenada;
import com.plg.entity.Mapa;
import com.plg.config.DataLoader;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SimulacionEstadoUtils {
    public static void actualizarPedidos(List<Pedido> pedidos) {
        for (int i = 0; i < Mapa.getInstance().getFilas(); i++) {
            for (int j = 0; j < Mapa.getInstance().getColumnas(); j++) {
                Nodo nodo = Mapa.getInstance().getMatriz().get(i).get(j);
                if (nodo instanceof Pedido) {
                    Nodo nodoaux = Nodo.builder().coordenada(new Coordenada(i, j)).tipoNodo(TipoNodo.NORMAL).build();
                    Mapa.getInstance().setNodo(nodo.getCoordenada(), nodoaux);
                }
            }
        }
        for (Pedido pedido : pedidos) {
            Mapa.getInstance().setNodo(pedido.getCoordenada(), pedido);
        }
    }

    public static List<Bloqueo> actualizarBloqueos(LocalDateTime fechaActual) {
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

    public static void actualizarRepositorios(LocalDateTime fechaActual) {
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

    public static void actualizarCamiones(LocalDateTime fechaActual, List<Camion> camiones, java.util.Set<Pedido> pedidosPorAtender, java.util.Set<Pedido> pedidosPlanificados, java.util.Set<Pedido> pedidosEntregados, int intervaloTiempo) {
        for (Camion camion : camiones) {
            camion.actualizarEstado(intervaloTiempo, pedidosPorAtender, pedidosPlanificados, pedidosEntregados, fechaActual);
        }
    }
} 