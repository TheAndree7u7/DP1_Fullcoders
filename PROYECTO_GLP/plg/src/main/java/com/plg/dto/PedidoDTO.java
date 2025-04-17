package com.plg.dto;

import com.plg.entity.EstadoPedido;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PedidoDTO {
    private Long id;
    private String codigo;
    private double posX;
    private double posY;
    private double volumenGLPAsignado; // Cambiado de Double a Integer para coincidir con la entidad
    private double horasLimite;
    private String clienteId; // Cambiado de Long a String para coincidir con la entidad
    private String clienteNombre;
    private String fechaHora; // Cambiado de LocalDateTime a String para coincidir con la entidad
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaEntregaRequerida;
    private LocalDateTime fechaEnregaReal;
    private EstadoPedido estado;
}