package com.plg.dto;

import java.util.ArrayList;
import java.util.List;

import com.plg.entity.Bloqueo;
import com.plg.entity.Nodo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
@Data
@Getter
@Setter
public class BloqueoDto {
    private List<CoordenadaDto> coordenadas;
    private String fechaInicio;
    private String fechaFin;

    public BloqueoDto(Bloqueo bloqueo) {
        this.coordenadas = new ArrayList<>();
        for (Nodo nodo : bloqueo.getNodosBloqueados()) {
            this.coordenadas.add(new CoordenadaDto(nodo.getCoordenada()));
        }
        // Formato ISO para facilitar el parseo en el frontend
        this.fechaInicio = bloqueo.getFechaInicio() != null ? bloqueo.getFechaInicio().toString() : null;
        this.fechaFin = bloqueo.getFechaFin() != null ? bloqueo.getFechaFin().toString() : null;
    }
}
