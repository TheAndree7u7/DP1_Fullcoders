package com.plg.utils.simulacion;

import com.plg.entity.Pedido;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SimulacionPedidoUtils {
    public static List<Pedido> unirPedidosSinRepetidos(Set<Pedido> set1, Set<Pedido> set2) {
        List<Pedido> listaUnida = new ArrayList<>(set1);
        for (Pedido pedido : set2) {
            if (!listaUnida.contains(pedido)) {
                listaUnida.add(pedido);
            }
        }
        return listaUnida;
    }
} 