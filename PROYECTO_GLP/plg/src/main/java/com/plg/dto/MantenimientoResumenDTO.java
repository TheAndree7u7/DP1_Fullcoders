package com.plg.dto;

import com.plg.entity.Mantenimiento;

/**
 * DTO para representar un resumen básico de mantenimiento
 */
public class MantenimientoResumenDTO {
    private Long id;
    private String tipo;
    private String fechaInicio;
    private String fechaFin;
    private String descripcion;
    private int estado;
    
    public MantenimientoResumenDTO() {
        // Constructor vacío necesario para la serialización
    }
    
    public MantenimientoResumenDTO(Mantenimiento mantenimiento) {
        this.id = mantenimiento.getId();
        this.tipo = mantenimiento.getTipo();
        this.descripcion = mantenimiento.getDescripcion();
        
        if (mantenimiento.getFechaInicio() != null) {
            this.fechaInicio = mantenimiento.getFechaInicio().toString();
        }
        
        if (mantenimiento.getFechaFin() != null) {
            this.fechaFin = mantenimiento.getFechaFin().toString();
        }
        
        this.estado = mantenimiento.getEstado();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(String fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public String getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(String fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }
}