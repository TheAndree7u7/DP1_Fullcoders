package com.plg.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidad que representa un punto o nodo en una ruta.
 * Un nodo puede ser un punto de origen (almacén), destino (cliente) o punto intermedio.
 */
@Getter
@Setter
@Entity
@Table(name = "nodos_ruta")
public class NodoRuta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "ruta_id")
    @JsonBackReference(value="ruta-nodo")
    private Ruta ruta;
    
    @Column(name = "orden")
    private int orden;
    
    @Column(name = "pos_x")
    private double posX;
    
    @Column(name = "pos_y")
    private double posY;
    
    @Column(name = "tipo")
    private String tipo; // "ALMACEN", "CLIENTE", "INTERMEDIO"
    
    @ManyToOne
    @JoinColumn(name = "pedido_id")
    @JsonBackReference(value="pedido-nodo")
    private Pedido pedido; // Solo para nodos de tipo "CLIENTE"
    
    @Column(name = "volumen_glp")
    private double volumenGLP; // Volumen a entregar en este nodo
    
    @Column(name = "porcentaje_pedido")
    private double porcentajePedido; // Porcentaje del pedido que se entrega en este nodo
    
    @Column(name = "entregado")
    private boolean entregado;
    
    @Column(name = "tiempo_llegada_estimado")
    private LocalDateTime tiempoLlegadaEstimado;
    
    @Column(name = "tiempo_llegada_real")
    private LocalDateTime tiempoLlegadaReal;
    
    @Column(name = "observaciones")
    private String observaciones;
    
    /**
     * Constructor por defecto
     */
    public NodoRuta() {
        this.entregado = false;
    }
    
    /**
     * Constructor con coordenadas y tipo
     */
    public NodoRuta(double posX, double posY, String tipo) {
        this();
        this.posX = posX;
        this.posY = posY;
        this.tipo = tipo;
    }
    
    /**
     * Constructor con todos los campos relevantes para un nodo de cliente
     */
    public NodoRuta(double posX, double posY, String tipo, Pedido pedido, double volumenGLP, double porcentajePedido) {
        this(posX, posY, tipo);
        this.pedido = pedido;
        this.volumenGLP = volumenGLP;
        this.porcentajePedido = porcentajePedido;
    }
    
    
     
    /**
     * Calcula la distancia Manhattan a otro nodo
     */
    public double distanciaA(NodoRuta otro) {
        return Math.abs(this.posX - otro.posX) + Math.abs(this.posY - otro.posY);
    }
    
    /**
     * Convierte a representación de cadena
     */
    @Override
    public String toString() {
        return "NodoRuta{" +
               "id=" + id +
               ", orden=" + orden +
               ", pos=(" + posX + "," + posY + ")" +
               ", tipo='" + tipo + '\'' +
               (pedido != null ? ", pedidoId=" + pedido.getId() : "") +
               '}';
    }
}