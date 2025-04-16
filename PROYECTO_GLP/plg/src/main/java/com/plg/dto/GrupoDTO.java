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
public class GrupoDTO {
    private String idGrupo;
    private PedidoDTO ejemplar;
    private Double centroideX;
    private Double centroideY;
    private List<PedidoDTO> pedidos;
    private Integer numeroPedidos;
    private Double radio;
    private Double densidad;
}