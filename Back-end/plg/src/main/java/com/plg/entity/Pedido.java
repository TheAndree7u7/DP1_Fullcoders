package com.plg.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class Pedido extends Nodo {

    private String codigo;
    private double horasLimite; 
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaLimite; // Nueva variable para la fecha límite de entrega
    private double volumenGLPAsignado;
    @Builder.Default
    private double volumenGLPEntregado = 0.0;
    private EstadoPedido estado;
    private Pedido pedidoCopia;

    public Pedido(Coordenada coordenada, boolean bloqueado, double gScore, double fScore, TipoNodo tipoNodo) {
        super(coordenada, bloqueado, gScore, fScore, tipoNodo);
        this.volumenGLPEntregado = 0.0;
    }
    @Override
    public String toString() {
        return String.format(
            "Pedido [%s]%n" +
            "  - Coordenada:       %s%n" +
            "  - Volumen asignado:   %.2f m3%n" +
            "  - Estado:             %s%n",
            codigo,
            getCoordenada() != null ? getCoordenada() : "N/A", 
            volumenGLPAsignado,
            estado
        );
    }

    @JsonIgnore
    public Pedido getClone() {
        return Pedido.builder()
            .coordenada(getCoordenada())
            .bloqueado(isBloqueado())
            .gScore(getGScore())
            .fScore(getFScore())
            .tipoNodo(getTipoNodo())
            .codigo(codigo)
            .horasLimite(horasLimite)
            .fechaRegistro(fechaRegistro)
            .fechaLimite(fechaLimite)
            .volumenGLPAsignado(volumenGLPAsignado)
            .volumenGLPEntregado(volumenGLPEntregado)
            .estado(estado)
            .fechaLimite(fechaLimite)
            .build();
    }


    public void guardarCopia() {
        this.pedidoCopia = this.getClone();
    }
    
    public void restaurarCopia() {
        if (this.pedidoCopia != null) {
            this.setCoordenada(this.pedidoCopia.getCoordenada());
            this.setBloqueado(this.pedidoCopia.isBloqueado());
            this.setGScore(this.pedidoCopia.getGScore());
            this.setFScore(this.pedidoCopia.getFScore());
            this.setTipoNodo(this.pedidoCopia.getTipoNodo());
            this.codigo = this.pedidoCopia.getCodigo();
            this.horasLimite = this.pedidoCopia.getHorasLimite();
            this.fechaRegistro = this.pedidoCopia.getFechaRegistro();
            this.fechaLimite = this.pedidoCopia.getFechaLimite();
            this.volumenGLPAsignado = this.pedidoCopia.getVolumenGLPAsignado();
            this.volumenGLPEntregado = this.pedidoCopia.getVolumenGLPEntregado();
            this.estado = this.pedidoCopia.getEstado();
            this.fechaLimite = this.pedidoCopia.getFechaLimite();
        }
    }

}
