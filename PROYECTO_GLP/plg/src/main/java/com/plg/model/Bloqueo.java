package com.plg.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Bloqueo {
    private Coordenada inicio;
    private Coordenada fin;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String descripcion;

    public Bloqueo(Coordenada inicio, Coordenada fin, LocalDate fechaInicio, LocalDate fechaFin, String descripcion) {
        this.inicio = inicio;
        this.fin = fin;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.descripcion = descripcion;
    }

    public Bloqueo(String line) {
        String[] partes = line.split(",", 7);
        if (partes.length == 7) {
            int x1 = Integer.parseInt(partes[0]);
            int y1 = Integer.parseInt(partes[1]);
            int x2 = Integer.parseInt(partes[2]);
            int y2 = Integer.parseInt(partes[3]);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            this.inicio = new Coordenada(x1, y1);
            this.fin = new Coordenada(x2, y2);
            this.fechaInicio = LocalDate.parse(partes[4], formatter);
            this.fechaFin = LocalDate.parse(partes[5], formatter);
            this.descripcion = partes[6];
        } else {
            throw new IllegalArgumentException("Formato inválido en bloqueo: " + line);
        }
    }

    public Coordenada getInicio() {
        return inicio;
    }

    public Coordenada getFin() {
        return fin;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public String getDescripcion() {
        return descripcion;
    }

    // ✅ Agrega esto para imprimir bonito
    @Override
    public String toString() {
        return "Bloqueo{" +
                "inicio=" + inicio +
                ", fin=" + fin +
                ", fechaInicio=" + fechaInicio +
                ", fechaFin=" + fechaFin +
                ", descripcion='" + descripcion + '\'' +
                '}';
    }
}
