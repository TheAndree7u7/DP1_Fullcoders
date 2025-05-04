package com.plg.entity;

import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Pedido extends Nodo {

    private int id;
    private String codigo;
    private double horasLimite; 
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaEntregaRequerida;
    private LocalDateTime fechaEntregaReal;
    private double volumenGLPAsignado;
    private double volumenGLPEntregado;
    private double volumenGLPPendiente;
    private int prioridad;
    private EstadoPedido estado;
    private String fechaHora;
    private String fechaAsignaciones;

    private List<EntregaParcial> entregasParciales = new ArrayList<>();
    private List<NodoRuta> nodos = new ArrayList<>();
    private Camion camion;
    private List<AsignacionCamion> asignaciones = new ArrayList<>();

    public Pedido(Coordenada coordenada, boolean bloqueado, double gScore, double fScore, TipoNodo tipoNodo) {
        super(coordenada, bloqueado, gScore, fScore, tipoNodo);
    }

    @Override
    public String toString() {
        return "Pedido[id=" + id + ", codigo=" + codigo + ", volumenGLPAsignado=" + volumenGLPAsignado + "]";
    }
}
