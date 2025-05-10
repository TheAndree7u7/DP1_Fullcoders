package com.plg.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
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

    /**
     * Retorna un clon de este almacén conservando todos sus atributos.
     */
    public Almacen getClone() {
        return Almacen.builder()
            .coordenada(getCoordenada())
            .bloqueado(isBloqueado())
            .gScore(getGScore())
            .fScore(getFScore())
            .tipoNodo(getTipoNodo())
            .nombre(nombre)
            .capacidadActualGLP(capacidadActualGLP)
            .capacidadMaximaGLP(capacidadMaximaGLP)
            .capacidadActualCombustible(capacidadActualCombustible)
            .capacidadMaximaCombustible(capacidadMaximaCombustible)
            .esCentral(esCentral)
            .permiteCamionesEstacionados(permiteCamionesEstacionados)
            .tipo(tipo)
            .horaReabastecimiento(horaReabastecimiento)
            .ultimoReabastecimientoRealizado(ultimoReabastecimientoRealizado)
            .activo(activo)
            .build();
    }

    @Override
    public String toString() {
        return String.format(
            "Almacén %s (%s)%n" +
            "  - Coordenada:            %s%n" +
            "  - GLP (m3):              %.2f / %.2f%n" +
            "  - Combustible (gal):     %.2f / %.2f%n" +
            "  - Central:               %s%n" +
            "  - Permite estacionarse:  %s%n" +
            "  - Hora reabastecimiento: %s%n" +
            "  - Activo:                %s",
            nombre,
            tipo != null ? tipo : "N/A",
            getCoordenada() != null ? getCoordenada() : "N/A",
            capacidadActualGLP,
            capacidadMaximaGLP,
            capacidadActualCombustible,
            capacidadMaximaCombustible,
            esCentral ? "Sí" : "No",
            permiteCamionesEstacionados ? "Sí" : "No",
            horaReabastecimiento != null ? horaReabastecimiento : "N/A",
            activo ? "Sí" : "No"
        );
    }
}
