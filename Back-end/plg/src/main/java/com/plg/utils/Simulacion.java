package com.plg.utils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
import com.plg.factory.AlmacenFactory;
import com.plg.factory.CamionFactory;
import com.plg.utils.simulacion.ConfiguracionSimulacion;
import com.plg.utils.simulacion.MantenimientoManager;
import com.plg.utils.simulacion.AveriasManager;
import com.plg.utils.simulacion.UtilesSimulacion;


public class Simulacion {

    private static List<Pedido> pedidosSemanal;
    private static LocalDateTime fechaActual;
    public static Set<Pedido> pedidosPorAtender = new LinkedHashSet<>();
    public static Set<Pedido> pedidosPlanificados = new LinkedHashSet<>();
    public static Set<Pedido> pedidosEntregados = new LinkedHashSet<>();
    public static Individuo mejorIndividuo = null;

    // Variable global para pedidosEnviar
    public static List<Pedido> pedidosEnviar = new ArrayList<>();

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
        // Contenido eliminado por solicitud del usuario
    }

    public static void actualizarEstadoGlobal(LocalDateTime fechaActual) {
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

    public static List<Pedido> obtenerPedidosEnRango(LocalDateTime fecha) {
        List<Pedido> pedidosEnRango = new ArrayList<>();
        LocalDateTime fechaLimite = fecha.plusHours(2);

        if (pedidosSemanal == null) {
            return pedidosEnRango; // Devuelve lista vacía si no hay pedidos
        }

        for (Pedido pedido : pedidosSemanal) {
            LocalDateTime fechaRegistro = pedido.getFechaRegistro();
            // Comprueba si la fecha de registro está en el intervalo [fecha, fecha + 2h)
            if (!fechaRegistro.isBefore(fecha) && UtilesSimulacion.pedidoConFechaMenorAFechaActual(pedido, fechaLimite)) {
                pedidosEnRango.add(pedido);
            }
        }
        return pedidosEnRango;
    }

    public static void detenerSimulacion() {
        System.out.println("🛑 Deteniendo simulación y limpiando datos...");
        
        // Limpiar pedidos de la simulación
        pedidosSemanal = null;
        pedidosEnviar.clear();
        pedidosPorAtender.clear();
        pedidosPlanificados.clear();
        pedidosEntregados.clear();
        mejorIndividuo = null;
        
        // Limpiar DataLoader
        DataLoader.pedidos.clear();
        DataLoader.almacenes.clear();
        DataLoader.camiones.clear();
        DataLoader.averias.clear();
        DataLoader.bloqueos.clear();
        DataLoader.mantenimientos.clear();
        
        
        
        // Reinicializar el mapa
        Mapa.initializeInstance();
        
        // Resetear fecha actual
        fechaActual = null;
        
        System.out.println("✅ Simulación detenida y datos limpiados correctamente");
    }

}
