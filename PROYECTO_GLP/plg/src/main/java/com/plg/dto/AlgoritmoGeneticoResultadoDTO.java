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
public class AlgoritmoGeneticoResultadoDTO {
    private List<RutaDTO> rutas;
    private String metodo;
    private Integer totalPedidos;
    private Integer pedidosAsignados;
}