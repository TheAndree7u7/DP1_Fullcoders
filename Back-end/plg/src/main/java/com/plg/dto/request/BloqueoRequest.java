package com.plg.dto.request;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para la creación de un nuevo bloqueo.
 */
@Data
public class BloqueoRequest {

    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private List<CoordenadaRequest> coordenadas;

    @Data
    public static class CoordenadaRequest {

        private int x; // columna
        private int y; // fila
    }
}
