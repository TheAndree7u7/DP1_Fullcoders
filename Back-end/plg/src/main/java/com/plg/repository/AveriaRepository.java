package com.plg.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.plg.utils.Parametros;
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
        return Parametros.dataLoader.averias;
    }

 
    /**
     * Obtiene todas las averías activas.
     *
     * @return Lista de averías activas
     */
    public List<Averia> findAllActive() {
        return Parametros.dataLoader.averias.stream()
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
        return Parametros.dataLoader.averias.stream()
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
        Parametros.dataLoader.averias.add(averia);
        return averia;
    }

    /**
     * Obtiene los códigos únicos de camiones con avería activa.
     *
     * @return Lista de códigos de camiones averiados (sin duplicados)
     */
    public List<String> findCodigosCamionesAveriados() {
        return Parametros.dataLoader.averias.stream()
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
        return Parametros.dataLoader.averias.stream()
                .filter(a -> a.getCamion() != null
                && a.getCamion().getCodigo().equals(codigoCamion)
                && a.getTipoIncidente() != null
                && a.getTipoIncidente().getCodigo().equals(tipoIncidente))
                .collect(Collectors.toList());
    }
}
