package com.plg.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RutaDTO {
    private String idRuta;
    private Double distanciaTotal;
    private Integer tiempoEstimado;
    private List<PedidoDTO> pedidos;
    private Integer numeroPedidos;
    private List<PuntoRutaDTO> puntos;
    private String camionCodigo;
}