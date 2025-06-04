package com.plg.repository;

import java.util.List;

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
     * Guarda un nuevo pedido en memoria.
     */
    public Pedido save(Pedido pedido) {
        DataLoader.pedidos.add(pedido);
        return pedido;
    }
}
