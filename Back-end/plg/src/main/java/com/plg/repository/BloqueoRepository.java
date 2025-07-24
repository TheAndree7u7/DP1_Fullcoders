package com.plg.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.plg.utils.Parametros;
import com.plg.entity.Bloqueo;

/**
 * Repositorio en memoria para gestionar bloqueos.
 */
@Repository
public class BloqueoRepository {

    /**
     * Obtiene todos los bloqueos almacenados.
     *
     * @return Lista de todos los bloqueos
     */
    public List<Bloqueo> findAll() {
        return Parametros.dataLoader.bloqueos;
    }

    /**
     * Devuelve los bloqueos registrados dentro del rango de fechas indicado.
     *
     * @param inicio fecha y hora de inicio (inclusive)
     * @param fin fecha y hora final (exclusive)
     * @return lista de bloqueos en el rango
     */
    public List<Bloqueo> findAllBetween(LocalDateTime inicio, LocalDateTime fin) {
        return Parametros.dataLoader.bloqueos.stream()
                .filter(b -> {
                    LocalDateTime fechaInicio = b.getFechaInicio();
                    LocalDateTime fechaFin = b.getFechaFin();
                    // El bloqueo está activo si se superpone con el rango solicitado
                    return (fechaInicio.isBefore(fin) || fechaInicio.isEqual(fin))
                            && (fechaFin.isAfter(inicio) || fechaFin.isEqual(inicio));
                })
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todos los bloqueos activos.
     *
     * @return Lista de bloqueos activos
     */
    public List<Bloqueo> findAllActive() {
        return Parametros.dataLoader.bloqueos.stream()
                .filter(Bloqueo::getActivo)
                .collect(Collectors.toList());
    }

    /**
     * Guarda un nuevo bloqueo en memoria.
     *
     * @param bloqueo el bloqueo a guardar
     * @return el bloqueo guardado
     */
    public Bloqueo save(Bloqueo bloqueo) {
        Parametros.dataLoader.bloqueos.add(bloqueo);
        return bloqueo;
    }
}
