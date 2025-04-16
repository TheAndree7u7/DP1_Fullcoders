package com.plg.repository;

import com.plg.entity.Averia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AveriaRepository extends JpaRepository<Averia, Long> {
    List<Averia> findByCamion_Codigo(String codigoCamion);
    List<Averia> findByEstado(int estado); 
    List<Averia> findByFechaHoraReporteBetween(LocalDateTime inicio, LocalDateTime fin);
    
    // Nuevos métodos de consulta
    List<Averia> findByTipoIncidente(String tipoIncidente);
    List<Averia> findByTurno(String turno);
    List<Averia> findByRequiereTraslado(boolean requiereTraslado);
    List<Averia> findByEsValida(boolean esValida);
    List<Averia> findByConCarga(boolean conCarga);
    
    // Combinaciones útiles
    List<Averia> findByEstadoAndTipoIncidente(int estado, String tipoIncidente);
    List<Averia> findByTurnoAndTipoIncidente(String turno, String tipoIncidente);
    
    // Consultas para verificar inoperatividad
    @Query("SELECT a FROM Averia a WHERE a.tiempoFinInoperatividad > :fecha")
    List<Averia> findCamionesInoperativos(@Param("fecha") LocalDateTime fecha);
    
    @Query("SELECT a FROM Averia a WHERE a.camion.codigo = :codigoCamion AND a.tiempoFinInoperatividad > :fecha")
    List<Averia> verificarInoperatividadCamion(@Param("codigoCamion") String codigoCamion, @Param("fecha") LocalDateTime fecha);
    
    // Consultas por ubicación
    List<Averia> findByPosXAndPosY(int posX, int posY);
    
    // Consulta para encontrar averías en un rango de kilómetros
    @Query("SELECT a FROM Averia a WHERE a.kilometroOcurrencia BETWEEN :kmInicio AND :kmFin")
    List<Averia> findByRangoKilometro(@Param("kmInicio") double kmInicio, @Param("kmFin") double kmFin);
}