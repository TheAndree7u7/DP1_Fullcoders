package com.plg.dto;

import com.plg.entity.Camion;

import lombok.Data;

@Data
public class CamionDto {

    private String codigo;
    private String estado;
    private String tipo;
    private int fila;
    private int columna;
    // GLP
    private double capacidadMaximaGLP;              // Capacidad en m3 de GLP
    private double capacidadActualGLP;    // Capacidad disponible actual (m3)

    private double tara;                   // Peso del camión vacío en toneladas
    private double pesoCarga;              // Peso actual de la carga en toneladas
    private double pesoCombinado;          // Peso total (tara + carga)
        // Combustible
    private double combustibleMaximo;   // Capacidad del tanque en galones
    private double combustibleActual;        // Combustible actual en galones
    private double velocidadPromedio; // Velocidad promedio en km/h

    // Consumo de combustible
    private double distanciaMaxima;
        
    public CamionDto(Camion camion) {
        this.codigo = camion.getCodigo();
        this.estado = camion.getEstado() != null ? camion.getEstado().name() : null;
        this.tipo = camion.getTipo() != null ? camion.getTipo().name() : null;
        if (camion.getCoordenada() != null) {
            this.fila = camion.getCoordenada().getFila();
            this.columna = camion.getCoordenada().getColumna();
        }
        
        // GLP
        this.capacidadMaximaGLP = camion.getCapacidadMaximaGLP();
        this.capacidadActualGLP = camion.getCapacidadActualGLP();
        
        // Peso
        this.tara = camion.getTara();
        this.pesoCarga = camion.getPesoCarga();
        this.pesoCombinado = camion.getPesoCombinado();
        
        // Combustible
        this.combustibleMaximo = camion.getCombustibleMaximo();
        this.combustibleActual = camion.getCombustibleActual();
        this.velocidadPromedio = camion.getVelocidadPromedio();
        this.distanciaMaxima = camion.getDistanciaMaxima();
    }
}
