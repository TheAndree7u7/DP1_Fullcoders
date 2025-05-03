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

 
}
