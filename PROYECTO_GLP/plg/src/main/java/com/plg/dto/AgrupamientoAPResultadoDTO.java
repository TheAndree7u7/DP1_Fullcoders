package com.plg.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * DTO para los resultados del algoritmo de Affinity Propagation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgrupamientoAPResultadoDTO {
    
    /**
     * Método usado para el agrupamiento
     */
    private String metodo;
    
    /**
     * Total de pedidos considerados
     */
    private int totalPedidos;
    
    /**
     * Cantidad de pedidos agrupados
     */
    private int pedidosAgrupados;
    
    /**
     * Clusters formados por el algoritmo
     */
    private List<ClusterDTO> clusters;
    
    /**
     * Mensaje adicional sobre el proceso de agrupamiento
     */
    private String mensaje;
    
    /**
     * Número de iteraciones realizadas
     */
    private int iteraciones;
    
    /**
     * Tiempo de cómputo en milisegundos
     */
    private long tiempoComputo;
}