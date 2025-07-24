package com.plg.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Coordenada {
    private int fila;
    private int columna;
    
    @Override
    public boolean equals(Object obj) {
        Coordenada that = (Coordenada) obj;
        return fila == that.fila && columna == that.columna;
    }

    @Override
    public int hashCode() {
        return 1000 * fila + columna;
    }

}
