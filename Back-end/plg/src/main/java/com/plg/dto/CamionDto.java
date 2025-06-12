package com.plg.dto;

import com.plg.entity.Camion;


import lombok.Data;

@Data
public class CamionDto {
    private String codigo;

    public CamionDto(Camion camion) {
        this.codigo = camion.getCodigo();
    }
}
