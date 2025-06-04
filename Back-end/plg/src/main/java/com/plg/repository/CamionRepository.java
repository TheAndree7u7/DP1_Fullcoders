package com.plg.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.plg.config.DataLoader;
import com.plg.entity.Camion;

/**
 * Repositorio en memoria para camiones.
 */
@Repository
public class CamionRepository {

    /**
     * Obtiene la lista completa de camiones.
     */
    public List<Camion> findAll() {
        return DataLoader.camiones;
    }

    /**
     * Guarda un nuevo camión en memoria.
     */
    public Camion save(Camion camion) {
        DataLoader.camiones.add(camion);
        return camion;
    }
}
