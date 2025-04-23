package com.plg.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar un cluster del algoritmo Affinity Propagation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClusterDTO {
    
    /**
     * ID del punto centro (ejemplar) de este cluster
     */
    private Long centroId;
    
    /**
     * Coordenada X del centro
     */
    private double centroX;
    
    /**
     * Coordenada Y del centro
     */
    private double centroY;
    
    /**
     * Puntos pertenecientes a este cluster
     */
    private List<PuntoClusterDTO> puntos;
    
    /**
     * ID del grupo al que pertenece este cluster
     */
    private String idGrupo;
    
    /**
     * Distancia total desde los puntos al centro
     */
    private double distanciaTotal;
    
    /**
     * Distancia promedio desde los puntos al centro
     */
    private double distanciaPromedio;
}
