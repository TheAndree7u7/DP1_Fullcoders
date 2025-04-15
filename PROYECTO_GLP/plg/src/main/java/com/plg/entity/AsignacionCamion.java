package com.plg.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignacionCamion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "camion_id")
    private Camion camion;
    
    @ManyToOne
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;
    
    private double volumenAsignado;
    private double porcentajeAsignado;
    
    @ManyToOne
    @JoinColumn(name = "ruta_id")
    private Ruta ruta;
    
    private boolean entregado;
    private LocalDateTime fechaEntregaParcial;
    
    // Constructor para compatibilidad con código existente
    public AsignacionCamion(Camion camion, double volumenAsignado, double porcentajeAsignado) {
        this.camion = camion;
        this.volumenAsignado = volumenAsignado;
        this.porcentajeAsignado = porcentajeAsignado;
        this.entregado = false;
    }
}