package com.plg.entity;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignacionCamion {

    private Long id;
    private Camion camion;
    private Pedido pedido;
    private double volumenAsignado;
    private double porcentajeAsignado;
    private boolean entregado;
    private LocalDateTime fechaEntregaParcial;

    /**
     * Constructor para compatibilidad con código existente
     */
    public AsignacionCamion(Camion camion, double volumenAsignado, double porcentajeAsignado) {
        this.camion = camion;
        this.volumenAsignado = volumenAsignado;
        this.porcentajeAsignado = porcentajeAsignado;
        this.entregado = false;
    }
}
