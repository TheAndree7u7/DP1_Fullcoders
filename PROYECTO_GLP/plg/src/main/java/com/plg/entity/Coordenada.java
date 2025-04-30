package com.plg.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coordenada {
    private int fila;
    private int columna;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Coordenada))
            return false;
        Coordenada that = (Coordenada) o;
        return this.fila == that.fila && this.columna == that.columna;
    }

    @Override
    public int hashCode() {
        return 31 * fila + columna;
    }

}
