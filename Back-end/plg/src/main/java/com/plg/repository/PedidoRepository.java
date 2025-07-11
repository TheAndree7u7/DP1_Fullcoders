package com.plg.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.plg.utils.Parametros;
import com.plg.entity.Pedido;

/**
 * Repositorio en memoria para gestionar pedidos.
 */
@Repository
public class PedidoRepository {

    /**
     * Obtiene todos los pedidos almacenados.
     */
    public List<Pedido> findAll() {
        return Parametros.dataLoader.pedidos;
    }

    /**
     * Devuelve los pedidos registrados dentro del rango de fechas indicado.
     *
     * @param inicio fecha y hora de inicio (inclusive)
     * @param fin    fecha y hora final (exclusive)
     * @return lista de pedidos en el rango
     */
    public List<Pedido> findAllBetween(LocalDateTime inicio, LocalDateTime fin) {
        return Parametros.dataLoader.pedidos.stream()
                .filter(p -> {
                    LocalDateTime fecha = p.getFechaRegistro();
                    return (fecha.isEqual(inicio) || fecha.isAfter(inicio))
                            && fecha.isBefore(fin);
                })
                .collect(Collectors.toList());
    }

    /**
     * Guarda un nuevo pedido en memoria.
     */
    public Pedido save(Pedido pedido) {
        Parametros.dataLoader.pedidos.add(pedido);
        return pedido;
    }

    /**
     * Actualiza el estado de un pedido existente.
     */
    public Pedido update(Pedido pedido) {
        for (int i = 0; i < Parametros.dataLoader.pedidos.size(); i++) {
            if (Parametros.dataLoader.pedidos.get(i).getCodigo().equals(pedido.getCodigo())) {
                Parametros.dataLoader.pedidos.set(i, pedido);
                return pedido;
            }
        }
        throw new RuntimeException("Pedido no encontrado: " + pedido.getCodigo());
    }
}
