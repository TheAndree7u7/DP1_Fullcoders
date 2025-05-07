package com.plg.entity;

import com.plg.entity.Coordenada;
import com.plg.entity.TipoNodo;

/**
 * Patrón fábrica para crear almacenes CENTRALs o secundarios.
 */
public class AlmacenFactory {

    /**
     * Crea un Almacen de tipo CENTRAL o secundario.
     *
     * @param tipoAlmacen        Tipo del almacén (CENTRAL o SECUNDARIO)
     * @param coordenada         Coordenada del almacén
     * @param capacidadMaxGLP    Capacidad máxima de GLP
     * @param capacidadMaxComb   Capacidad máxima de combustible
     * @return instancia de Almacen configurada
     */
    public static Almacen crearAlmacen(
            TipoAlmacen tipoAlmacen,
            Coordenada coordenada,
            double capacidadMaxGLP,
            double capacidadMaxComb) {
        boolean esCentral = tipoAlmacen == TipoAlmacen.CENTRAL;
        boolean permiteCamiones = esCentral;
        return Almacen.builder()
                .coordenada(coordenada)
                .bloqueado(false)
                .gScore(0)
                .fScore(0)
                .tipoNodo(TipoNodo.ALMACEN)
                .nombre(tipoAlmacen == TipoAlmacen.CENTRAL ? "Almacén Central" : "Almacén Secundario")
                .capacidadActualGLP(capacidadMaxGLP)
                .capacidadMaximaGLP(capacidadMaxGLP)
                .capacidadActualCombustible(capacidadMaxComb)
                .capacidadMaximaCombustible(capacidadMaxComb)
                .esCentral(esCentral)
                .permiteCamionesEstacionados(permiteCamiones)
                .tipo(tipoAlmacen.name())
                .activo(true)
                .build();
    }

}
