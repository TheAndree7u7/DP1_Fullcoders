package com.plg.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PuntoRutaDTO {
    private String tipo;  // "ALMACEN", "CLIENTE"
    private double posX;
    private double posY;
    private Long idPedido;
}