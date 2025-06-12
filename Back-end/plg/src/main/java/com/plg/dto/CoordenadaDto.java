package com.plg.dto;

import com.plg.entity.Coordenada;

import lombok.Data;

@Data
public class CoordenadaDto {
    private int x;
    private int y;

    public CoordenadaDto(Coordenada coordenada) {
        this.x = coordenada.getColumna();
        this.y = coordenada.getFila();
    }
}
