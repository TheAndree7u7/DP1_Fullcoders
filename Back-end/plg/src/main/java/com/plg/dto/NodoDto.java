package com.plg.dto;

import com.plg.entity.Nodo;

import lombok.Data;

@Data
public class NodoDto {
    private CoordenadaDto coordenada;

    public NodoDto(Nodo nodo) {
        this.coordenada = new CoordenadaDto(nodo.getCoordenada());
    }
}
