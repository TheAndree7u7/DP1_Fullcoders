package com.plg.entity;

import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Pedido {

    private int id;
    private String codigo;
    private Coordenada coordenada;
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

  

}
