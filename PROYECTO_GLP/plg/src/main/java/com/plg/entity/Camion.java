package com.plg.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Camion extends Nodo {

    private String codigo;
    private String tipo; // TA, TB, TC, TD, etc.

    // GLP
    private double capacidad;              // Capacidad en m3 de GLP
    private double capacidadDisponible;    // Capacidad disponible actual (m3)
    private double tara;                   // Peso del camión vacío en toneladas
    private double pesoCarga;              // Peso actual de la carga en toneladas
    private double pesoCombinado;          // Peso total (tara + carga)

    private EstadoCamion estado;

    // Combustible
    private double capacidadTanque = 25.0;   // Capacidad del tanque en galones
    private double combustibleActual;        // Combustible actual en galones
    private double velocidadPromedio = 50.0; // Velocidad promedio en km/h

    // Ditancia máxima
    private double distanciaMaxima; 


    public Camion(Coordenada coordenada, boolean bloqueado, double gScore, TipoNodo tipoNodo, double fScore) {
        super(coordenada, bloqueado, gScore, fScore, tipoNodo);
    }

    @Override
    public String toString() {
        return "Camion[codigo=" + codigo + ", tipo=" + tipo + ", capacidad=" + capacidad + "]";
    }
}
