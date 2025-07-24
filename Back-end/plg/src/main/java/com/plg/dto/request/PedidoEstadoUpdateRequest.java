package com.plg.dto.request;

import com.plg.entity.EstadoPedido;

import lombok.Data;

/**
 * DTO para actualizar el estado de un pedido.
 */
@Data
public class PedidoEstadoUpdateRequest {
    private String codigo;
    private EstadoPedido estado;
}
