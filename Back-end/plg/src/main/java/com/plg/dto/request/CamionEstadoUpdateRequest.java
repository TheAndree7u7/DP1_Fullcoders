package com.plg.dto.request;

import com.plg.entity.Coordenada;
import com.plg.entity.EstadoCamion;
import lombok.Data;

/**
 * DTO para actualizar el estado de un camión.
 */
@Data
public class CamionEstadoUpdateRequest {
    private String codigo;
    private Coordenada coordenada;
    private double combustibleActual;
    private double capacidadActualGLP;
    private EstadoCamion estado;
}
