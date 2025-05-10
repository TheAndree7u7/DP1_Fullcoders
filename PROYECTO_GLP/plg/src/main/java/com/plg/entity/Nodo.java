package com.plg.entity;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.util.Objects;

@Data
@NoArgsConstructor
@SuperBuilder
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

    @Override
    public String toString() {
        return String.format(
                "Nodo [%s]%n" +
                        "  - Coordenada:       %s%n" +
                        "  - Tipo de nodo:     %s \n",
                coordenada != null ? coordenada : "N/A",
                coordenada != null ? coordenada : "N/A",
                tipoNodo != null ? tipoNodo.getTipo() : "N/A");
    }

    @Override
    public boolean equals(Object obj) {
        Nodo nodo = (Nodo) obj;
        return Objects.equals(coordenada, nodo.coordenada);
    }

    @Override
    public int hashCode() {
        return Objects.hash(coordenada);
    }
}
