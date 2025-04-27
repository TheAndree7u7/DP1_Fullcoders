package com.plg.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Mantenimiento {
    private String codigoCamion;
    private LocalDate fechaInicio;

    public Mantenimiento(String codigoCamion, LocalDate fechaInicio) {
        this.codigoCamion = codigoCamion;
        this.fechaInicio = fechaInicio;
    }

    public Mantenimiento(String line) {
        String[] partes = line.split(",");
        if (partes.length >= 2) {
            this.codigoCamion = partes[0];
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            this.fechaInicio = LocalDate.parse(partes[1], formatter);
        } else {
            throw new IllegalArgumentException("Formato inválido en mantenimiento: " + line);
        }
    }

    public String getCodigoCamion() {
        return codigoCamion;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    @Override
    public String toString() {
        return "Mantenimiento{" +
                "codigoCamion='" + codigoCamion + '\'' +
                ", fechaInicio=" + fechaInicio +
                '}';
    }
}
