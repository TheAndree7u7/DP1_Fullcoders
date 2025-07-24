package com.plg.dto.request;

import java.time.LocalDateTime;

public class SimulacionRequest {
    private LocalDateTime fechaInicio;
    
    public SimulacionRequest() {}
    
    public SimulacionRequest(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }
    
    // Getters y setters
    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }
    
    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }
    
    @Override
    public String toString() {
        return "SimulacionRequest{" +
                "fechaInicio=" + fechaInicio +
                '}';
    }
} 