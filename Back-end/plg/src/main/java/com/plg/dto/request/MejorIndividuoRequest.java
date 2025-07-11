package com.plg.dto.request;

import java.time.LocalDateTime;

public class MejorIndividuoRequest {
    private LocalDateTime fecha;

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}
