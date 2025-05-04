package com.plg.entity;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
public class Nodo {
    private Coordenada coordenada;
    private boolean bloqueado;
    private double gScore;
    private double fScore;
    private TipoNodo tipoNodo;

    public Nodo(Coordenada coordenada, boolean bloqueado, double gScore, double fScore, TipoNodo tipoNodo) {
        this.coordenada = coordenada;
        this.bloqueado = bloqueado;
        this.gScore = gScore;
        this.tipoNodo = tipoNodo;
        this.fScore = fScore;
    }
}
