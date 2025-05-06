package com.plg.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Almacen extends Nodo {

    private String nombre;

    // GLP
    private double capacidadActualGLP;
    private double capacidadMaximaGLP;

    // Combustible
    private double capacidadCombustible;
    private double capacidadActualCombustible;
    private double capacidadMaximaCombustible;

    // Tipo de almacén
    private boolean esCentral;
    private boolean permiteCamionesEstacionados;
    private String tipo;

    private LocalTime horaReabastecimiento = LocalTime.MIDNIGHT;
    private boolean ultimoReabastecimientoRealizado = false;
    private boolean activo;

    public Almacen(Coordenada coordenada, boolean bloqueado, double gScore, double fScore, TipoNodo tipoNodo) {
        super(coordenada, bloqueado, gScore, fScore, tipoNodo);
    }

    public boolean puedeRecargarCombustible(double cantidadRequerida) {
        return activo && capacidadActualCombustible >= cantidadRequerida;
    }

    public boolean puedeProveerGLP(double cantidadRequerida) {
        return activo && capacidadActualGLP >= cantidadRequerida;
    }

    public boolean recargarCombustible(Camion camion, double cantidad) {
        if (!puedeRecargarCombustible(cantidad)) return false;
        double espacio = camion.getCapacidadTanque() - camion.getCombustibleActual();
        double efectivo = Math.min(cantidad, Math.min(espacio, capacidadActualCombustible));
        if (efectivo <= 0) return false;
        capacidadActualCombustible -= efectivo;
        camion.setCombustibleActual(camion.getCombustibleActual() + efectivo);
        return true;
    }

    public void reabastecer() {
        if (!esCentral) {
            capacidadActualGLP = capacidadMaximaGLP;
            capacidadActualCombustible = capacidadMaximaCombustible;
            ultimoReabastecimientoRealizado = true;
        }
    }

    @Override
    public String toString() {
        return "Almacen[nombre=" + nombre + ", capacidadActualGLP=" + capacidadActualGLP + "]";
    }
}
