package com.plg.dto;

import com.plg.entity.Almacen;
import com.plg.entity.TipoAlmacen;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class AlmacenDto {

    private CoordenadaDto coordenada;
    private String nombre;
    private double capacidadActualGLP;
    private double capacidadMaximaGLP;
    private double capacidadActualCombustible;
    private double capacidadMaximaCombustible;
    private boolean esCentral;
    private boolean permiteCamionesEstacionados;
    private TipoAlmacen tipo;
    private boolean activo;

    public AlmacenDto(Almacen almacen) {
        this.coordenada = new CoordenadaDto(almacen.getCoordenada());
        this.nombre = almacen.getNombre();
        this.capacidadActualGLP = almacen.getCapacidadActualGLP();
        this.capacidadMaximaGLP = almacen.getCapacidadMaximaGLP();
        this.capacidadActualCombustible = almacen.getCapacidadActualCombustible();
        this.capacidadMaximaCombustible = almacen.getCapacidadMaximaCombustible();
        this.esCentral = almacen.isEsCentral();
        this.permiteCamionesEstacionados = almacen.isPermiteCamionesEstacionados();
        this.tipo = almacen.getTipo();
        this.activo = almacen.isActivo();
    }
}
