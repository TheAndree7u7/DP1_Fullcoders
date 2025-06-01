package com.plg.dto;

import com.plg.entity.Pedido;

import lombok.Data;

@Data
public class PedidoDto {
    private String codigo;    

    public PedidoDto(Pedido pedido) {
        this.codigo = pedido.getCodigo();
    }
}
