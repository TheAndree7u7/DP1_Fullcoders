package com.plg.dto;

import java.time.LocalDateTime;

import com.plg.entity.EstadoPedido;
import com.plg.entity.Pedido;

import lombok.Data;

@Data
public class PedidoDto {

    private String codigo;
    private CoordenadaDto coordenada;
    private double horasLimite;
    private double volumenGLPAsignado;
    private EstadoPedido estado;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaLimite;

    public PedidoDto(Pedido pedido) {
        this.codigo = pedido.getCodigo();
        this.coordenada = new CoordenadaDto(pedido.getCoordenada());
        this.horasLimite = pedido.getHorasLimite();
        this.volumenGLPAsignado = pedido.getVolumenGLPAsignado();
        this.estado = pedido.getEstado();
        this.fechaRegistro = pedido.getFechaRegistro();
        this.fechaLimite = pedido.getFechaLimite();
    }
}
