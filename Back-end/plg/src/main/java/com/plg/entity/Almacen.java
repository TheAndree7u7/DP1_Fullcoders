package com.plg.entity;

import lombok.AllArgsConstructor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import com.fasterxml.jackson.annotation.JsonIgnore;

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
    private double capacidadActualCombustible;
    private double capacidadMaximaCombustible;

    // Tipo de almacén
    private boolean esCentral;
    private boolean permiteCamionesEstacionados;
    private TipoAlmacen tipo;

    private boolean activo;

    private Almacen almacenCopia;

    public Almacen(Coordenada coordenada, boolean bloqueado, double gScore, double fScore, TipoNodo tipoNodo) {
        super(coordenada, bloqueado, gScore, fScore, tipoNodo);
    }

    public boolean puedeRecargarCombustible(double cantidadRequerida) {
        return activo && capacidadActualCombustible >= cantidadRequerida;
    }

    public boolean puedeProveerGLP(double cantidadRequerida) {
        return activo && capacidadActualGLP >= cantidadRequerida;
    }

    /**
     * Retorna un clon de este almacén conservando todos sus atributos.
     */
    @JsonIgnore
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
                .activo(activo)
                .build();
    }

    public void guardarCopia() {
        this.almacenCopia = getClone();
    }

    public void restaurarCopia() {
        if (almacenCopia != null) {
            super.setCoordenada(almacenCopia.getCoordenada());
            super.setBloqueado(almacenCopia.isBloqueado());
            super.setGScore(almacenCopia.getGScore());
            super.setFScore(almacenCopia.getFScore());
            super.setTipoNodo(almacenCopia.getTipoNodo());
            this.nombre = almacenCopia.getNombre();
            this.capacidadActualGLP = almacenCopia.getCapacidadActualGLP();
            this.capacidadMaximaGLP = almacenCopia.getCapacidadMaximaGLP();
            this.capacidadActualCombustible = almacenCopia.getCapacidadActualCombustible();
            this.capacidadMaximaCombustible = almacenCopia.getCapacidadMaximaCombustible();
            this.esCentral = almacenCopia.isEsCentral();
            this.permiteCamionesEstacionados = almacenCopia.isPermiteCamionesEstacionados();
            this.tipo = almacenCopia.getTipo();
            this.activo = almacenCopia.isActivo();
        }
    }

    public boolean recargarGlPCamion(Camion camion){
        double glpRequerido = camion.getCapacidadMaximaGLP() - camion.getCapacidadActualGLP();
        double glpDisponible = this.getCapacidadActualGLP();
        if (glpDisponible <= 0) {
            return false; // No hay GLP para recargar o el camión ya está lleno
        }
        double glpRecargar = Math.min(glpRequerido, glpDisponible);
        camion.setCapacidadActualGLP(camion.getCapacidadActualGLP() + glpRecargar);
        this.setCapacidadActualGLP(this.getCapacidadActualGLP() - glpRecargar);
        return true;
    }

    public boolean recargarCombustible(Camion camion){
        // Todos los almacenes tiene combustible infinito
        camion.setCombustibleActual(camion.getCombustibleMaximo());
        return true;
    }

    @Override
    public String toString() {
        return String.format(
                "Almacén %s (%s)%n" +
                        "  - Coordenada:            %s%n" +
                        "  - GLP (m3):              %.2f / %.2f%n" +
                        "  - Combustible (gal):     %.2f / %.2f%n",
                nombre,
                tipo != null ? tipo : "N/A",
                getCoordenada() != null ? getCoordenada() : "N/A",
                capacidadActualGLP,
                capacidadMaximaGLP,
                capacidadActualCombustible,
                capacidadMaximaCombustible);
    }
}
