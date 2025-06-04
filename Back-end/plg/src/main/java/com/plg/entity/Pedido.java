package com.plg.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class Pedido extends Nodo {

    private String codigo;
    private double horasLimite; 
    private LocalDateTime fechaRegistro;
    private double volumenGLPAsignado;
    private EstadoPedido estado;

    private Pedido pedidoCopia;

    public Pedido(Coordenada coordenada, boolean bloqueado, double gScore, double fScore, TipoNodo tipoNodo) {
        super(coordenada, bloqueado, gScore, fScore, tipoNodo);
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
            .volumenGLPAsignado(volumenGLPAsignado)
            .estado(estado)
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
            this.volumenGLPAsignado = this.pedidoCopia.getVolumenGLPAsignado();
            this.estado = this.pedidoCopia.getEstado();
        }
    }   

}
