package com.plg.model;

import com.plg.model.enums.EstadoCamion;

public class Camion {
    private String codigo;
    private double capacidadTotal;
    private double cargaActual;
    private Coordenada posicionActual;
    private EstadoCamion estado;

    public Camion(String codigo, double capacidadTotal, Coordenada posicionInicial) {
        this.codigo = codigo;
        this.capacidadTotal = capacidadTotal;
        this.cargaActual = capacidadTotal;
        this.posicionActual = posicionInicial;
        this.estado = EstadoCamion.DISPONIBLE;
    }

    // 🔥 NUEVO: Constructor para cargar desde línea del archivo
    public Camion(String line) {
        String[] partes = line.split(";");
        if (partes.length >= 11) {
            this.codigo = partes[0];
            this.capacidadTotal = Double.parseDouble(partes[2]);
            this.cargaActual = Double.parseDouble(partes[3]);
            int x = Integer.parseInt(partes[7]);
            int y = Integer.parseInt(partes[8]);
            this.posicionActual = new Coordenada(x, y);
            int estadoInt = Integer.parseInt(partes[5]);
            this.estado = EstadoCamion.fromInt(estadoInt);
        } else {
            throw new IllegalArgumentException("Formato inválido en Camion: " + line);
        }
    }

    public String getCodigo() {
        return codigo;
    }

    public double getCapacidadTotal() {
        return capacidadTotal;
    }

    public double getCargaActual() {
        return cargaActual;
    }

    public void setCargaActual(double cargaActual) {
        this.cargaActual = cargaActual;
    }

    public Coordenada getPosicionActual() {
        return posicionActual;
    }

    public void setPosicionActual(Coordenada posicionActual) {
        this.posicionActual = posicionActual;
    }

    public EstadoCamion getEstado() {
        return estado;
    }

    public void setEstado(EstadoCamion estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Camion{" +
                "codigo='" + codigo + '\'' +
                ", capacidadTotal=" + capacidadTotal +
                ", cargaActual=" + cargaActual +
                ", posicionActual=" + posicionActual +
                ", estado=" + estado +
                '}';
    }
}
