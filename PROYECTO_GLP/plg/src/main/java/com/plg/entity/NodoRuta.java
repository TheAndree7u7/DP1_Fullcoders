package com.plg.entity;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * Entidad que representa un punto o nodo en una ruta.
 * Un nodo puede ser un punto de origen (almacén), destino (cliente) o punto intermedio.
 */
@Getter
@Setter
public class NodoRuta {

    // private Long id;
    // private int orden;
    private int posX;
    private int posY;
    // private String tipo; // "ALMACEN", "CLIENTE", "INTERMEDIO"
    // private Pedido pedido; // Solo para nodos de tipo "CLIENTE"
    // private double volumenGLP; // Volumen a entregar en este nodo
    // private double porcentajePedido; // Porcentaje del pedido que se entrega en este nodo
    // private boolean entregado;
    // private LocalDateTime tiempoLlegadaEstimado;
    private LocalDateTime tiempoLlegadaReal;
    private String observaciones;

    // public NodoRuta() {
    //     this.entregado = false;
    // }

    // public NodoRuta(double posX, double posY, String tipo) {
    //     this();
    //     this.posX = posX;
    //     this.posY = posY;
    //     this.tipo = tipo;
    // }

    // public NodoRuta(double posX, double posY, String tipo, Pedido pedido, double volumenGLP, double porcentajePedido) {
    //     this(posX, posY, tipo);
    //     this.pedido = pedido;
    //     this.volumenGLP = volumenGLP;
    //     this.porcentajePedido = porcentajePedido;
    // }

    // public double distanciaA(NodoRuta otro) {
    //     return Math.abs(this.posX - otro.posX) + Math.abs(this.posY - otro.posY);
    // }

    // @Override
    // public String toString() {
    //     return "NodoRuta{" +
    //            "id=" + id +
    //            ", orden=" + orden +
    //            ", pos=(" + posX + "," + posY + ")" +
    //            ", tipo='" + tipo + '\'' +
    //            (pedido != null ? ", pedidoId=" + pedido.getId() : "") +
    //            '}';
    // }
}
