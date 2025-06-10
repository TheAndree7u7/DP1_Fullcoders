package com.plg.dto;

import java.util.ArrayList;
import java.util.List;

import com.plg.entity.Bloqueo;
import com.plg.entity.Nodo;

public class BloqueoDto {

    private List<CoordenadaDto> coordenadas;

    public BloqueoDto(Bloqueo bloqueo) {
        this.coordenadas = new ArrayList<>();
        for (Nodo nodo : bloqueo.getNodosBloqueados()) {
            this.coordenadas.add(new CoordenadaDto(nodo.getCoordenada()));
        }
    }

    public List<CoordenadaDto> getCoordenadas() {
        return coordenadas;
    }
}
