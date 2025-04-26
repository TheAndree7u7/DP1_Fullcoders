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

    private Long id;
    private String codigo;
    private Cliente cliente;
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

    public boolean asignarACamion(Camion camion, double volumen) {
        if (volumenGLPAsignado - volumenGLPEntregado < volumen || !camion.tieneCapacidadPara(volumen)) {
            return false;
        }
        double porcentaje = (volumen / volumenGLPAsignado) * 100;
        AsignacionCamion asignacion = new AsignacionCamion(camion, volumen, porcentaje);
        asignacion.setPedido(this);
        volumenGLPEntregado += volumen;
        camion.asignarPedidoParcial(this, volumen, porcentaje);
        actualizarEstadoDePedido();
        return true;
    }

    public boolean registrarEntregaParcial(String codigoCamion, double volumenEntregado, LocalDateTime fechaEntrega) {
        if (volumenEntregado > volumenGLPAsignado) return false;
        this.volumenGLPEntregado += volumenEntregado;
        if (camion != null) camion.liberarCapacidad(volumenEntregado);
        actualizarEstadoDePedido();
        if (estado == EstadoPedido.ENTREGADO_TOTALMENTE) this.fechaEntregaReal = fechaEntrega;
        return true;
    }

    private void actualizarEstadoDePedido() {
        if (volumenGLPEntregado == 0) {
            estado = EstadoPedido.REGISTRADO;
        } else if (volumenGLPEntregado < volumenGLPAsignado) {
            estado = EstadoPedido.ENTREGADO_PARCIALMENTE;
        } else {
            estado = EstadoPedido.ENTREGADO_TOTALMENTE;
        }
    }

    public void cancelar() {
        if (camion != null) {
            camion.liberarCapacidad(volumenGLPAsignado - volumenGLPEntregado);
        }
        estado = EstadoPedido.NO_ENTREGADO_EN_TIEMPO;
    }

    public double getPorcentajeEntregado() {
        return (volumenGLPEntregado / volumenGLPAsignado) * 100;
    }

    public boolean isCompletamenteEntregado() {
        return Math.abs(volumenGLPEntregado - volumenGLPAsignado) < 0.01;
    }

    public String getEstadoTexto() {
        return estado != null ? estado.getDescripcion() : "Desconocido";
    }

    public String getEstadoColorHex() {
        return estado != null ? estado.getColorHex() : "#CCCCCC";
    }
}
