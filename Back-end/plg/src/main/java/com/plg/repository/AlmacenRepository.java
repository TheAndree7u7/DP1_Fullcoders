package com.plg.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.plg.utils.Parametros;
import com.plg.entity.Almacen;

/**
 * Repositorio en memoria para almacenes.
 */
@Repository
public class AlmacenRepository {

    /**
     * Devuelve todos los almacenes disponibles.
     */
    public List<Almacen> findAll() {
        return Parametros.dataLoader.almacenes;
    }

    /**
     * Almacena un nuevo almacén en memoria.
     */
    public Almacen save(Almacen almacen) {
        Parametros.dataLoader.almacenes.add(almacen);
        return almacen;
    }
}
