package com.plg.repository;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.plg.config.DataLoader;
import com.plg.dto.CamionDto;
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

    /**
     * Obtiene la cantidad de camiones agrupados por estado.
     */
    public Map<String, Long> countByEstado() {
        return findAll().stream()
                .collect(java.util.stream.Collectors.groupingBy(c -> c.getEstado().name(), java.util.stream.Collectors.counting()));
    }

    /**
     * Retorna todos los estados posibles de camión con su descripción.
     */
    public java.util.List<java.util.Map<String, String>> listarEstadosPosibles() {
        return java.util.Arrays.stream(com.plg.entity.EstadoCamion.values())
                .map(e -> {
                    java.util.Map<String, String> map = new java.util.HashMap<>();
                    map.put("estado", e.name());
                    map.put("descripcion", e.getDescripcion());
                    return map;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Lista todos los camiones con sus datos principales (estado, id, tipo,
     * coordenada).
     */
    public List<CamionDto> listarCamionesEstado() {
        return findAll().stream().map(com.plg.dto.CamionDto::new).collect(java.util.stream.Collectors.toList());
    }
}
