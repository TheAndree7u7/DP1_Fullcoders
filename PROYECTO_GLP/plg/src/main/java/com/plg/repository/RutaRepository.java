package com.plg.repository;

import com.plg.entity.Ruta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones de base de datos relacionadas con Rutas
 */
@Repository
public interface RutaRepository extends JpaRepository<Ruta, Long> {
    
    /**
     * Busca una ruta por su código único
     */
    Optional<Ruta> findByCodigo(String codigo);
    
    /**
     * Busca rutas por estado
     */
    List<Ruta> findByEstado(int estado);
    
    /**
     * Busca rutas asignadas a un camión específico
     */
    List<Ruta> findByCamionId(Long camionId);
    
    /**
     * Busca rutas en curso (estado = 1) para un camión específico
     */
    List<Ruta> findByCamionIdAndEstado(Long camionId, int estado);
    
    /**
     * Busca rutas que incluyen un pedido específico
     */
    @Query("SELECT r FROM Ruta r JOIN r.nodos n WHERE n.pedido.id = :pedidoId")
    List<Ruta> findByPedidoId(@Param("pedidoId") Long pedidoId);
    
    /**
     * Busca rutas creadas en un rango de fechas
     */
    List<Ruta> findByFechaCreacionBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    /**
     * Busca rutas que pasan por un punto específico
     */
    @Query("SELECT DISTINCT r FROM Ruta r JOIN r.nodos n WHERE n.posX = :x AND n.posY = :y")
    List<Ruta> findByRutaQueIncluyePunto(@Param("x") int x, @Param("y") int y);
    
    /**
     * Cuenta cuántas rutas planificadas (estado = 0) hay actualmente
     */
    long countByEstado(int estado);
}