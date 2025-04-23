package com.plg.model;

public class Camion {
    private String codigo; // e.g., TA01
    private String tipo;
    private double tara;   // Ton
    private int capacidad; // m3
    private double pesoCarga; // Ton

    public Camion(String codigo, String tipo, double tara, int capacidad, double pesoCarga) {
        this.codigo = codigo;
        this.tipo = tipo;
        this.tara = tara;
        this.capacidad = capacidad;
        this.pesoCarga = pesoCarga;
    }

    public double getPesoCombinado(double volumen) {
        return tara + (volumen / capacidad) * pesoCarga;
    }

    public String getCodigo() { return codigo; }
    public int getCapacidad() { return capacidad; }

    @Override
    public String toString() {
        return codigo + " (" + capacidad + "m3)";
    }
}
