package com.plg.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar un punto en un cluster
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PuntoClusterDTO {
    
    /**
     * ID del punto (típicamente el ID del pedido)
     */
    private Long id;
    
    /**
     * Coordenada X del punto
     */
    private double x;
    
    /**
     * Coordenada Y del punto
     */
    private double y;
    
    /**
     * Distancia al centro del cluster
     */
    private double distanciaCentro;
    
    /**
     * ID del pedido que este punto representa
     */
    private Long pedidoId;
    
    /**
     * Código del pedido que este punto representa
     */
    private String pedidoCodigo;
}
