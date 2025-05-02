package com.plg.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

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
    private double volumenGLP;

    @Column(name = "porcentaje_pedido")
    private double porcentajePedido;

    @Column(name = "fecha_asignacion")
    private LocalDateTime fechaAsignacion;

    @Column(name = "fecha_entrega")
    private LocalDateTime fechaEntrega;

    @Column(name = "fecha_entrega_requerida")
    private LocalDateTime fechaEntregaRequerida;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20)
    private EstadoEntregaParcial estado;

    public EntregaParcial() {
        this.fechaAsignacion = LocalDateTime.now();
        this.estado = EstadoEntregaParcial.ASIGNADO;
    }

    public EntregaParcial(Pedido pedido, double volumenGLP, double porcentajePedido) {
        this();
        this.pedido = pedido;
        this.volumenGLP = volumenGLP;
        this.porcentajePedido = porcentajePedido;
    }

    public static EntregaParcialBuilder builder() {
        return new EntregaParcialBuilder();
    }

    public static class EntregaParcialBuilder {
        private Pedido pedido;
        private double volumenGLP;
        private double porcentajePedido;
        private EstadoEntregaParcial estado;

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

        public EntregaParcial build() {
            EntregaParcial entrega = new EntregaParcial(pedido, volumenGLP, porcentajePedido);
            if (estado != null) {
                entrega.setEstado(estado);
            }
            return entrega;
        }
    }

    @Deprecated
    public void setEstadoInt(int estadoInt) {
        this.estado = EstadoEntregaParcial.fromValue(estadoInt);
    }

    @Deprecated
    public int getEstadoInt() {
        return this.estado.ordinal();
    }
}
