package com.plg.dto.request;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO para la creación de una nueva avería.
 */
@Data
public class AveriaRequest {

    private String codigoCamion;
    private String turno; // T1, T2, T3
    private String tipoIncidente; // TI1, TI2, TI3
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
}
