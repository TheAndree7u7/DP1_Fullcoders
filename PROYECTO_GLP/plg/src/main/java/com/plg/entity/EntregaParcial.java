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
    
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20)
    private EstadoEntregaParcial estado; // ASIGNADO, EN_RUTA, ENTREGADO, CANCELADO
    
    @Column(name = "observaciones")
    private String observaciones;
    
    /**
     * Constructor por defecto
     */
    public EntregaParcial() {
        this.fechaAsignacion = LocalDateTime.now();
        this.estado = EstadoEntregaParcial.ASIGNADO; // Asignado por defecto
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
        private EstadoEntregaParcial estado;
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
        
        public EntregaParcialBuilder estado(EstadoEntregaParcial estado) {
            this.estado = estado;
            return this;
        }
        
        public EntregaParcialBuilder observaciones(String observaciones) {
            this.observaciones = observaciones;
            return this;
        }
        
        public EntregaParcial build() {
            EntregaParcial entrega = new EntregaParcial(pedido, volumenGLP, porcentajePedido);
            if (estado != null) {
                entrega.setEstado(estado);
            }
            if (observaciones != null) {
                entrega.setObservaciones(observaciones);
            }
            return entrega;
        }
    }
    
    /**
     * Método para mantener compatibilidad con código existente que use valores enteros
     * @param estadoInt valor entero del estado
     * @deprecated Use setEstado(EstadoEntregaParcial) instead
     */
    @Deprecated
    public void setEstadoInt(int estadoInt) {
        this.estado = EstadoEntregaParcial.fromValue(estadoInt);
    }
    
    /**
     * Método para mantener compatibilidad con código existente que use valores enteros
     * @return valor entero correspondiente al estado actual
     * @deprecated Use getEstado() instead to get the enum value
     */
    @Deprecated
    public int getEstadoInt() {
        return this.estado.ordinal();
    }
}