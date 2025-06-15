package com.plg.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.plg.config.DataLoader;
import com.plg.entity.Averia;
import com.plg.entity.Camion;

/**
 * Repositorio en memoria para gestionar averías.
 */
@Repository
public class AveriaRepository {

    /**
     * Obtiene todas las averías almacenadas.
     *
     * @return Lista de todas las averías
     */
    public List<Averia> findAll() {
        return DataLoader.averias;
    }

    /**
     * Devuelve las averías registradas dentro del rango de fechas indicado.
     *
     * @param inicio fecha y hora de inicio (inclusive)
     * @param fin fecha y hora final (exclusive)
     * @return lista de averías en el rango
     */
    public List<Averia> findAllBetween(LocalDateTime inicio, LocalDateTime fin) {
        return DataLoader.averias.stream()
                .filter(a -> {
                    LocalDateTime fechaInicio = a.getFechaInicio();
                    LocalDateTime fechaFin = a.getFechaFin();
                    // La avería está activa si se superpone con el rango solicitado
                    return fechaInicio != null && fechaFin != null
                            && (fechaInicio.isBefore(fin) || fechaInicio.isEqual(fin))
                            && (fechaFin.isAfter(inicio) || fechaFin.isEqual(inicio));
                })
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todas las averías activas.
     *
     * @return Lista de averías activas
     */
    public List<Averia> findAllActive() {
        return DataLoader.averias.stream()
                .filter(a -> a.getEstado() != null && a.getEstado())
                .collect(Collectors.toList());
    }

    /**
     * Obtiene averías por camión.
     *
     * @param camion el camión del cual obtener las averías
     * @return Lista de averías del camión
     */
    public List<Averia> findByCamion(Camion camion) {
        return DataLoader.averias.stream()
                .filter(a -> a.getCamion() != null && a.getCamion().equals(camion))
                .collect(Collectors.toList());
    }

    /**
     * Guarda una nueva avería en memoria.
     *
     * @param averia la avería a guardar
     * @return la avería guardada
     */
    public Averia save(Averia averia) {
        DataLoader.averias.add(averia);
        return averia;
    }

    /**
     * Obtiene los códigos únicos de camiones con avería activa.
     *
     * @return Lista de códigos de camiones averiados (sin duplicados)
     */
    public List<String> findCodigosCamionesAveriados() {
        return DataLoader.averias.stream()
                .filter(a -> a.getEstado() != null && a.getEstado() && a.getCamion() != null)
                .map(a -> a.getCamion().getCodigo())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Obtiene averías por camión y tipo de incidente.
     *
     * @param codigoCamion código del camión
     * @param tipoIncidente tipo de incidente ("TI1", "TI2", "TI3")
     * @return Lista de averías filtradas
     */
    public List<Averia> findByCamionAndTipo(String codigoCamion, String tipoIncidente) {
        return DataLoader.averias.stream()
                .filter(a -> a.getCamion() != null
                        && a.getCamion().getCodigo().equals(codigoCamion)
                        && a.getTipoIncidente() != null
                        && a.getTipoIncidente().getTipo().equals(tipoIncidente))
                .collect(Collectors.toList());
    }
}
