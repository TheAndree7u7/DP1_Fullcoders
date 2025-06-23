package com.plg.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.plg.config.DataLoader;
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
        return DataLoader.pedidos;
    }

    /**
     * Devuelve los pedidos registrados dentro del rango de fechas indicado.
     *
     * @param inicio fecha y hora de inicio (inclusive)
     * @param fin    fecha y hora final (exclusive)
     * @return lista de pedidos en el rango
     */
    public List<Pedido> findAllBetween(LocalDateTime inicio, LocalDateTime fin) {
        return DataLoader.pedidos.stream()
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
        DataLoader.pedidos.add(pedido);
        return pedido;
    }

    /**
     * Actualiza el estado de un pedido existente.
     */
    public Pedido update(Pedido pedido) {
        for (int i = 0; i < DataLoader.pedidos.size(); i++) {
            if (DataLoader.pedidos.get(i).getCodigo().equals(pedido.getCodigo())) {
                DataLoader.pedidos.set(i, pedido);
                return pedido;
            }
        }
        throw new RuntimeException("Pedido no encontrado: " + pedido.getCodigo());
    }
}
