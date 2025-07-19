package com.plg.entity;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder
@Getter
@Setter
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
                        "  - Tipo de nodo:     %s\n" +
                        "  - ¿Bloqueado?:      %s\n",
                coordenada != null ? coordenada : "N/A",
                coordenada != null ? coordenada : "N/A",
                tipoNodo != null ? tipoNodo.getTipo() : "N/A",
                bloqueado ? "Sí" : "No");
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

    // El siguiente método clone se puede sobreescribir en las subclases
    @JsonIgnore
    public Nodo getClone() {
        return Nodo.builder()
                .coordenada(getCoordenada())
                .bloqueado(isBloqueado())
                .gScore(getGScore())
                .fScore(getFScore())
                .tipoNodo(getTipoNodo())
                .build();
    }
}
