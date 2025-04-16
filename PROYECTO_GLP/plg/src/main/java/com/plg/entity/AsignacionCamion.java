package com.plg.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
    @JsonBackReference(value="camion-asignacion")
    private Camion camion;
    
    @ManyToOne
    @JoinColumn(name = "pedido_id")
    @JsonBackReference(value="pedido-asignacion")
    private Pedido pedido;
    
    private double volumenAsignado;
    private double porcentajeAsignado;
    
    @ManyToOne
    @JoinColumn(name = "ruta_id")
    @JsonBackReference(value="ruta-asignacion")
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