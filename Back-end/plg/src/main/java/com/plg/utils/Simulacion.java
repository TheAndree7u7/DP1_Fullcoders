package com.plg.utils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.plg.config.DataLoader;
import com.plg.entity.Almacen;
import com.plg.entity.Bloqueo;
import com.plg.entity.Camion;
import com.plg.entity.Coordenada;
import com.plg.entity.Mapa;
import com.plg.entity.Nodo;
import com.plg.entity.Pedido;
import com.plg.entity.TipoAlmacen;
import com.plg.entity.TipoNodo;
import com.plg.utils.simulacion.MantenimientoManager;
import com.plg.utils.simulacion.AveriasManager;
import com.plg.utils.simulacion.UtilesSimulacion;



public class Simulacion {

    public static Set<Pedido> pedidosPorAtender = new LinkedHashSet<>();
    public static Set<Pedido> pedidosPlanificados = new LinkedHashSet<>();
    public static Set<Pedido> pedidosEntregados = new LinkedHashSet<>();
    public static List<Bloqueo> bloqueosActivos = new ArrayList<>();
    public static Individuo mejorIndividuo = null;
    // Variable global para pedidosEnviar
    public static List<Pedido> pedidosEnviar = new ArrayList<>();
    public static void configurarSimulacionDiaria(LocalDateTime startDate) {
        // Aun no implementado
    }
    public static void configurarSimulacionSemanal(LocalDateTime startDate) {
        // 1. Actualizar parámetros globales antes de cargar datos
        Parametros.actualizarParametrosGlobales(startDate);
        // 2. Limpiamos el mapa antes de iniciar la simulación
        Mapa.getInstance().limpiarMapa();
        // 3. Creamos un nuevo dataLoader para la simulación semanal
        Parametros.dataLoader = new DataLoader();

        // 4. Limpiamos las listas de pedidos
        pedidosPorAtender.clear();
        pedidosPlanificados.clear();
        pedidosEntregados.clear();
        pedidosEnviar.clear();
        
    }

    public static void actualizarEstadoGlobal(LocalDateTime fechaActual) {
        actualizarRepositorios(fechaActual);
        actualizarCamiones(fechaActual);
        MantenimientoManager.verificarYActualizarMantenimientos(Parametros.dataLoader.camiones, fechaActual);
        //AveriasManager.actualizarCamionesEnAveria(fechaActual);
        Simulacion.bloqueosActivos = Simulacion.actualizarBloqueos(fechaActual);
        actualizarPedidos();
    }

    public static List<Pedido> actualizarPedidosEnRango() {
        // 1. Obtenemos todos los pedidos del fechaActual < x < fechaActual + intervaloTiempo
        LocalDateTime fechaLimite = Parametros.fecha_inicial.plusMinutes(Parametros.intervaloTiempo);
        List<Pedido> pedidosEnRango = Parametros.dataLoader.pedidos.stream()
                .filter(pedido -> pedido.getFechaRegistro().isAfter(Parametros.fecha_inicial)
                        && pedido.getFechaRegistro().isBefore(fechaLimite))
                .collect(Collectors.toList());
        
        // 2. Unimos pedidosEnRango con pedidosPlanificados
        List<Pedido> pedidosUnidos = UtilesSimulacion.unirPedidosSinRepetidos(
                new LinkedHashSet<>(pedidosPlanificados), 
                new LinkedHashSet<>(pedidosEnRango));
        return pedidosUnidos;
    }

    private static void actualizarPedidos() {
        List<Pedido> pedidosActualizados = actualizarPedidosEnRango();
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
        for (Pedido pedido : pedidosActualizados) {
            Mapa.getInstance().setNodo(pedido.getCoordenada(), pedido);
        }
        pedidosEnviar = pedidosActualizados;
    }

    public static List<Bloqueo> actualizarBloqueos(LocalDateTime fechaActual) {
        List<Bloqueo> bloqueos = Parametros.dataLoader.bloqueos;
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
        List<Almacen> almacenes = Parametros.dataLoader.almacenes;
        if (fechaActual.getHour() == 0 && fechaActual.getMinute() == 0) {
            for (Almacen almacen : almacenes) {
                if (almacen.getTipo() == TipoAlmacen.SECUNDARIO) {
                    almacen.setCapacidadActualGLP(almacen.getCapacidadMaximaGLP());
                    almacen.setCapacidadActualCombustible(almacen.getCapacidadMaximaCombustible());
                }
            }
        }
    }

    private static void actualizarCamiones(LocalDateTime fechaActual) {
        List<Camion> camiones = Parametros.dataLoader.camiones;

        for (Camion camion : camiones) {
            camion.actualizarEstado(pedidosPorAtender, pedidosPlanificados,
                    pedidosEntregados);
        }
    }
}