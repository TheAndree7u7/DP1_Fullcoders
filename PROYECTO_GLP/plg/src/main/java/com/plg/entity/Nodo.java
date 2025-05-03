package com.plg.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Nodo {
    private Coordenada coordenada;
    private boolean bloqueado;
    private double gScore;
    private double fScore;
    private TipoNodo tipoNodo; // Tipo de nodo (almacen, cliente, intermedio, normal)
    private Pedido pedido; // Solo para nodos de tipo "CLIENTE"
    private Camion camion;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Nodo))
            return false;
        Nodo nodo = (Nodo) o;
        // Suponiendo que la coordenada es inmutable y define la identidad del nodo
        return this.getCoordenada().equals(nodo.getCoordenada());
    }

    @Override
    public int hashCode() {
        return getCoordenada().hashCode();
    }
}
