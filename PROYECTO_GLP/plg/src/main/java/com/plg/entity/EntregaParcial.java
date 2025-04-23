package com.plg.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class EntregaParcial {

    private Long id;
    private Camion camion;
    private Pedido pedido;
    private double volumenGLP;        // Volumen asignado para esta entrega parcial
    private double porcentajePedido;  // Porcentaje del pedido que representa esta entrega
    private LocalDateTime fechaAsignacion;
    private LocalDateTime fechaEntrega;
    private int estado;               // 0: Asignado, 1: En ruta, 2: Entregado, 3: Cancelado
    private String observaciones;

    /**
     * Constructor por defecto:
     * asigna fechaActual y estado = 0 (Asignado)
     */
    public EntregaParcial() {
        this.fechaAsignacion = LocalDateTime.now();
        this.estado = 0;
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
}
