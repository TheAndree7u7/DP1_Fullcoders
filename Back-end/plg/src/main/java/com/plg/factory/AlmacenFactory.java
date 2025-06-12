package com.plg.factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.plg.entity.Almacen;
import com.plg.entity.Coordenada;
import com.plg.entity.Mapa;
import com.plg.entity.TipoAlmacen;
import com.plg.entity.TipoNodo;

/**
 * Patrón fábrica para crear almacenes CENTRALs o secundarios.
 */
public class AlmacenFactory {

    public static final List<Almacen> almacenes = new ArrayList<>();
    private static final Map<TipoAlmacen, Integer> contadorAlmacenes = new HashMap<>();

    // Mapa que representa el entorno de entrega
    private static Mapa mapa = Mapa.getInstance();

    static {
        // Inicializamos el contador para cada tipo de almacén
        for (TipoAlmacen tipo : TipoAlmacen.values()) {
            contadorAlmacenes.put(tipo, 0);
        }
    }

    /**
     * Crea un Almacen de tipo CENTRAL o secundario y lo agrega al mapa.
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
            double capacidadMaxComb
        ) {

        // Actualizamos el contador y generamos un nombre único
        int numeroAlmacen = contadorAlmacenes.get(tipoAlmacen) + 1;
        contadorAlmacenes.put(tipoAlmacen, numeroAlmacen);
        String nombre = (tipoAlmacen == TipoAlmacen.CENTRAL ? "Almacén Central " : "Almacén Secundario ") + numeroAlmacen;

        boolean esCentral = tipoAlmacen == TipoAlmacen.CENTRAL;
        boolean permiteCamiones = esCentral;

        // Crear el almacén
        Almacen almacen = Almacen.builder()
                .coordenada(coordenada)
                .bloqueado(false)
                .gScore(0)
                .fScore(0)
                .tipoNodo(TipoNodo.ALMACEN)
                .nombre(nombre)
                .capacidadActualGLP(capacidadMaxGLP)
                .capacidadMaximaGLP(capacidadMaxGLP)
                .capacidadActualCombustible(capacidadMaxComb)
                .capacidadMaximaCombustible(capacidadMaxComb)
                .esCentral(esCentral)
                .permiteCamionesEstacionados(permiteCamiones)
                .tipo(tipoAlmacen)
                .activo(true)
                .build();

        // Agregar el almacén a la lista y al mapa
        almacenes.add(almacen);
        mapa.setNodo(coordenada, almacen);

        return almacen;
    }

    /**
     * Obtiene el número de almacenes creados por tipo.
     *
     * @param tipoAlmacen Tipo de almacén
     * @return número de almacenes creados de ese tipo
     */
    public static int getCantidadAlmacenesPorTipo(TipoAlmacen tipoAlmacen) {
        return contadorAlmacenes.getOrDefault(tipoAlmacen, 0);
    }
}
