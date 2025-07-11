package com.plg.utils.simulacion;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.plg.entity.Pedido;

/**
 * Funciones utilitarias repetidas dentro de la simulación.
 */
public class UtilesSimulacion {

    /**
     * Devuelve una lista con la unión de dos colecciones de pedidos sin repetidos.
     */
    public static List<Pedido> unirPedidosSinRepetidos(Set<Pedido> set1, Set<Pedido> set2) {
        List<Pedido> resultado = new ArrayList<>(set1);
        for (Pedido p : set2) {
            if (!resultado.contains(p)) {
                resultado.add(p);
            }
        }
        return resultado;
    }

    /**
     * Comprueba si la fechaRegistro de un pedido es anterior o igual a la fechaActual.
     */
    public static boolean pedidoConFechaMenorAFechaActual(Pedido pedido, LocalDateTime fechaActual) {
        return pedido.getFechaRegistro().isBefore(fechaActual) || pedido.getFechaRegistro().isEqual(fechaActual);
    }
} 