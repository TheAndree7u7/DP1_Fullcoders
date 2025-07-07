package com.plg.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.plg.config.DataLoader;
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
        return DataLoader.almacenes;
    }

    /**
     * Almacena un nuevo almacén en memoria.
     */
    public Almacen save(Almacen almacen) {
        DataLoader.almacenes.add(almacen);
        return almacen;
    }

    // actualizar
    public void update(Almacen almacen) {
        for (int i = 0; i < DataLoader.almacenes.size(); i++) {
            if (DataLoader.almacenes.get(i).getNombre().equals(almacen.getNombre())) {
                DataLoader.almacenes.set(i, almacen);
                return;
            }
        }
        throw new RuntimeException("Almacen no encontrado: " + almacen.getNombre());
    }
}
