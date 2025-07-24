package com.plg.dto.request;

import lombok.Data;

/**
 * DTO para la creación de un nuevo mantenimiento.
 */
@Data
public class MantenimientoRequest {

    private int dia;
    private int mes;
    private String codigoCamion;
}
