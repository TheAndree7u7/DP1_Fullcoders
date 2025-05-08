package com.plg.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class Pedido extends Nodo {

    private String codigo;
    private double horasLimite; 
    private double volumenGLPAsignado;
    private EstadoPedido estado;

    public Pedido(Coordenada coordenada, boolean bloqueado, double gScore, double fScore, TipoNodo tipoNodo) {
        super(coordenada, bloqueado, gScore, fScore, tipoNodo);
    }

    @Override
    public String toString() {
        return String.format(
            "Pedido [%s]%n" +
            "  - Volumen asignado:   %.2f m3%n" +
            "  - Estado:             %s%n",
            codigo, 
            volumenGLPAsignado,
            estado
        );
    }
}
