package com.plg.dto;

import com.plg.entity.Camion;

import lombok.Data;

@Data
public class CamionDto {

    private String codigo;
    private String estado;
    private String tipo;
    private int fila;
    private int columna;

    public CamionDto(Camion camion) {
        this.codigo = camion.getCodigo();
        this.estado = camion.getEstado() != null ? camion.getEstado().name() : null;
        this.tipo = camion.getTipo() != null ? camion.getTipo().name() : null;
        if (camion.getCoordenada() != null) {
            this.fila = camion.getCoordenada().getFila();
            this.columna = camion.getCoordenada().getColumna();
        }
    }
}
