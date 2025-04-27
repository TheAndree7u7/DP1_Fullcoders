package com.plg.model;

import java.util.ArrayList;
import java.time.LocalDateTime;

public class Ruta {
    private ArrayList<Coordenada> recorrido;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;

    public Ruta(ArrayList<Coordenada> recorrido, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        this.recorrido = recorrido;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    public ArrayList<Coordenada> getRecorrido() {
        return recorrido;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }
}
