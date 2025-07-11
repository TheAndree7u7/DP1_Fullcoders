package com.plg.dto.request;

import java.time.LocalDateTime;

/**
 * DTO para peticiones que requieren una fecha específica para obtener
 * soluciones.
 */
public class SolucionFechaRequest {
    private LocalDateTime fecha;
    private boolean avanzarSimulacion; // true para avanzar hasta la fecha, false para solo calcular

    public SolucionFechaRequest() {
    }

    public SolucionFechaRequest(LocalDateTime fecha) {
        this.fecha = fecha;
        this.avanzarSimulacion = false; // Por defecto solo calcular
    }

    public SolucionFechaRequest(LocalDateTime fecha, boolean avanzarSimulacion) {
        this.fecha = fecha;
        this.avanzarSimulacion = avanzarSimulacion;
    }

    // Getters y setters
    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public boolean isAvanzarSimulacion() {
        return avanzarSimulacion;
    }

    public void setAvanzarSimulacion(boolean avanzarSimulacion) {
        this.avanzarSimulacion = avanzarSimulacion;
    }

    @Override
    public String toString() {
        return "SolucionFechaRequest{" +
                "fecha=" + fecha +
                ", avanzarSimulacion=" + avanzarSimulacion +
                '}';
    }
}