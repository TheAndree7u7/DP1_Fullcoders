package com.plg.dto;

import com.plg.entity.Nodo;
import com.plg.entity.TipoNodo;

import lombok.Data;

@Data
public class NodoDto {
    private CoordenadaDto coordenada;
    private TipoNodo tipoNodo;
    public NodoDto(Nodo nodo) {
        this.coordenada = new CoordenadaDto(nodo.getCoordenada());
        this.tipoNodo = nodo.getTipoNodo();
    }
}
