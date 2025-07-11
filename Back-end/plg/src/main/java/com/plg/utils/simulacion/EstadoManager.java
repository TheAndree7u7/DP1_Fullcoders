package com.plg.utils.simulacion;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.plg.config.DataLoader;
import com.plg.entity.Almacen;
import com.plg.entity.Bloqueo;
import com.plg.entity.Camion;
import com.plg.entity.Coordenada;
import com.plg.entity.EstadoCamion;
import com.plg.entity.EstadoPedido;
import com.plg.entity.Mapa;
import com.plg.entity.Nodo;
import com.plg.entity.Pedido;
import com.plg.entity.TipoAlmacen;
import com.plg.entity.TipoNodo;
import com.plg.utils.Parametros;
import com.plg.utils.Simulacion;

/**
 * Clase para manejar la actualización del estado global de la simulación.
 * Proporciona funcionalidad para actualizar pedidos, bloqueos, almacenes y
 * camiones.
 */
public class EstadoManager {

    /**
     * Actualiza el estado global de la simulación incluyendo pedidos, repositorios,
     * camiones y mantenimientos.
     * 
     * @param fechaActual   Fecha y hora actual de la simulación
     * @param pedidosEnviar Lista de pedidos a procesar en este ciclo
     */
    public static void actualizarEstadoGlobal(LocalDateTime fechaActual, List<Pedido> pedidosEnviar) {
        actualizarPedidos(pedidosEnviar);
        actualizarRepositorios(fechaActual);
        actualizarCamiones(fechaActual);
        MantenimientoManager.verificarYActualizarMantenimientos(DataLoader.camiones, fechaActual);
        AveriasManager.actualizarCamionesEnAveria(fechaActual);
    }

    /**
     * Actualiza la posición de los pedidos en el mapa, removiendo los anteriores y
     * colocando los nuevos.
     * 
     * @param pedidos Lista de pedidos a colocar en el mapa
     */
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

    /**
     * Actualiza los bloqueos activos basándose en la fecha actual.
     * 
     * @param fechaActual Fecha y hora actual para verificar qué bloqueos deben
     *                    estar activos
     * @return Lista de bloqueos que están activos en la fecha actual
     */
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

    /**
     * Actualiza las capacidades de los almacenes secundarios al inicio de cada día.
     * 
     * @param fechaActual Fecha y hora actual para verificar si es medianoche
     */
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

    /**
     * Actualiza el estado de todos los camiones en la simulación.
     * 
     * @param fechaActual Fecha y hora actual de la simulación
     */
    private static void actualizarCamiones(LocalDateTime fechaActual) {
        List<Camion> camiones = DataLoader.camiones;

        for (Camion camion : camiones) {
            camion.actualizarEstado(Parametros.intervaloTiempo,
                    Simulacion.pedidosPorAtender,
                    Simulacion.pedidosPlanificados,
                    Simulacion.pedidosEntregados,
                    fechaActual);
        }
    }

    /**
     * Imprime un resumen detallado de los estados de todos los camiones y pedidos.
     */
    public static void imprimirResumenEstados() {
        // Obtener lista de camiones
        List<Camion> camiones = DataLoader.camiones;

        // Contar camiones por estado
        Map<EstadoCamion, Integer> contadorCamiones = new HashMap<>();
        for (EstadoCamion estado : EstadoCamion.values()) {
            contadorCamiones.put(estado, 0);
        }

        for (Camion camion : camiones) {
            EstadoCamion estado = camion.getEstado();
            contadorCamiones.put(estado, contadorCamiones.get(estado) + 1);
        }

        // Contar pedidos por estado
        Map<EstadoPedido, Integer> contadorPedidos = new HashMap<>();
        for (EstadoPedido estado : EstadoPedido.values()) {
            contadorPedidos.put(estado, 0);
        }

        // Contar pedidos entregados
        contadorPedidos.put(EstadoPedido.ENTREGADO, Simulacion.pedidosEntregados.size());

        // Contar pedidos planificados
        contadorPedidos.put(EstadoPedido.PLANIFICADO, Simulacion.pedidosPlanificados.size());

        // Contar pedidos por atender (registrados)
        contadorPedidos.put(EstadoPedido.REGISTRADO, Simulacion.pedidosPorAtender.size());

        // Imprimir resumen de camiones
        System.out.println("🚛 RESUMEN DE CAMIONES:");
        for (EstadoCamion estado : EstadoCamion.values()) {
            int cantidad = contadorCamiones.get(estado);
            if (cantidad > 0) {
                System.out.println("   • " + cantidad + " camiones " + estado.name() + " - " + estado.getDescripcion());
            }
        }

        // Imprimir resumen de pedidos
        System.out.println("📦 RESUMEN DE PEDIDOS:");
        for (EstadoPedido estado : EstadoPedido.values()) {
            int cantidad = contadorPedidos.get(estado);
            System.out.println("   • " + cantidad + " pedidos " + estado.name());
        }

        // Línea separadora
        System.out.println("------------------------------------------------");
    }
}