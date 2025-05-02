package com.plg.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.plg.entity.Averia;
import com.plg.entity.EstadoAveria;

/**
 * Repositorio JPA para la entidad Averia.
 * Permite realizar operaciones CRUD y consultas personalizadas sobre las averías registradas.
 */
@Repository
public interface AveriaRepository extends JpaRepository<Averia, Long> {

    /**
     * Encuentra averías asociadas a un camión por su código.
     */
    List<Averia> findByCamion_Codigo(String codigoCamion);

    /**
     * Encuentra averías por estado (usando el tipo enum).
     */
    List<Averia> findByEstado(EstadoAveria estado);

    /**
     * Encuentra averías reportadas en un rango de fechas.
     */
    List<Averia> findByFechaHoraReporteBetween(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Consulta por tipo de incidente.
     */
    List<Averia> findByTipoIncidente(String tipoIncidente);

    /**
     * Consulta por turno.
     */
    List<Averia> findByTurno(String turno);

    /**
     * Consulta si requiere traslado.
     */
    List<Averia> findByRequiereTraslado(boolean requiereTraslado);

    /**
     * Consulta por validez de la avería.
     */
    List<Averia> findByEsValida(boolean esValida);

    /**
     * Consulta por si el camión tenía carga.
     */
    List<Averia> findByConCarga(boolean conCarga);

    /**
     * Consulta por estado y tipo de incidente.
     */
    List<Averia> findByEstadoAndTipoIncidente(EstadoAveria estado, String tipoIncidente);

    /**
     * Consulta por turno y tipo de incidente.
     */
    List<Averia> findByTurnoAndTipoIncidente(String turno, String tipoIncidente);

    /**
     * Consulta para camiones que siguen inoperativos en base a fecha actual.
     */
    @Query("SELECT a FROM Averia a WHERE a.tiempoFinInoperatividad > :fecha")
    List<Averia> findCamionesInoperativos(@Param("fecha") LocalDateTime fecha);

    /**
     * Consulta si un camión específico está inoperativo en base a fecha actual.
     */
    @Query("SELECT a FROM Averia a WHERE a.camion.codigo = :codigoCamion AND a.tiempoFinInoperatividad > :fecha")
    List<Averia> verificarInoperatividadCamion(@Param("codigoCamion") String codigoCamion,
                                               @Param("fecha") LocalDateTime fecha);

    /**
     * Consulta por posición exacta (X, Y).
     */
    List<Averia> findByPosXAndPosY(double posX, double posY);

    /**
     * Consulta por rango de kilómetros de ocurrencia.
     */
    @Query("SELECT a FROM Averia a WHERE a.kilometroOcurrencia BETWEEN :kmInicio AND :kmFin")
    List<Averia> findByRangoKilometro(@Param("kmInicio") double kmInicio,
                                      @Param("kmFin") double kmFin);
}
