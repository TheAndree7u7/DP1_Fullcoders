package com.plg.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidad que representa una entrega parcial de un pedido.
 * Permite que un pedido pueda ser entregado en partes por diferentes camiones.
 */
@Entity
@AllArgsConstructor
@Getter
@Setter
@Table(name = "entregas_parciales")
public class EntregaParcial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "camion_id")
    @JsonBackReference(value="camion-entregaparcial")
    private Camion camion;
    
    @ManyToOne
    @JoinColumn(name = "pedido_id")
    @JsonBackReference(value="pedido-entregaparcial")
    private Pedido pedido;
    
    @Column(name = "volumen_glp")
    private double volumenGLP; // Volumen asignado para esta entrega parcial
    
    @Column(name = "porcentaje_pedido")
    private double porcentajePedido; // Porcentaje del pedido que representa esta entrega
    
    @Column(name = "fecha_asignacion")
    private LocalDateTime fechaAsignacion;
    
    @Column(name = "fecha_entrega")
    private LocalDateTime fechaEntrega;
    
    @Column(name = "estado")
    private int estado; // 0: Asignado, 1: En ruta, 2: Entregado, 3: Cancelado
    
    @Column(name = "observaciones")
    private String observaciones;
    
    /**
     * Constructor por defecto
     */
    public EntregaParcial() {
        this.fechaAsignacion = LocalDateTime.now();
        this.estado = 0; // Asignado por defecto
    }
    
    /**
     * Constructor con información básica
     */
    public EntregaParcial(Pedido pedido, double volumenGLP, double porcentajePedido) {
        this();
        this.pedido = pedido;
        this.volumenGLP = volumenGLP;
        this.porcentajePedido = porcentajePedido;
    } 
  
  
    /**
     * Builder pattern para crear instancias de EntregaParcial
     */
    public static EntregaParcialBuilder builder() {
        return new EntregaParcialBuilder();
    }
    
    /**
     * Builder para EntregaParcial
     */
    public static class EntregaParcialBuilder {
        private Pedido pedido;
        private double volumenGLP;
        private double porcentajePedido;
        private int estado;
        private String observaciones;
        
        public EntregaParcialBuilder pedido(Pedido pedido) {
            this.pedido = pedido;
            return this;
        }
        
        public EntregaParcialBuilder volumenGLP(double volumenGLP) {
            this.volumenGLP = volumenGLP;
            return this;
        }
        
        public EntregaParcialBuilder porcentajePedido(double porcentajePedido) {
            this.porcentajePedido = porcentajePedido;
            return this;
        }
        
        public EntregaParcialBuilder estado(int estado) {
            this.estado = estado;
            return this;
        }
        
        public EntregaParcialBuilder observaciones(String observaciones) {
            this.observaciones = observaciones;
            return this;
        }
        
        public EntregaParcial build() {
            EntregaParcial entrega = new EntregaParcial(pedido, volumenGLP, porcentajePedido);
            if (estado != 0) {
                entrega.setEstado(estado);
            }
            if (observaciones != null) {
                entrega.setObservaciones(observaciones);
            }
            return entrega;
        }
    }
}