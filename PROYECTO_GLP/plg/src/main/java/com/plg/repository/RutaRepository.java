package com.plg.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.plg.entity.Camion;
import com.plg.entity.Pedido;
import com.plg.entity.Ruta;

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
     * Busca rutas con todos sus nodos inicializados por estado
     */
    @Query("SELECT DISTINCT r FROM Ruta r LEFT JOIN FETCH r.nodos WHERE r.estado = :estado")
    List<Ruta> findByEstadoWithNodos(@Param("estado") int estado);
    
    /**
     * Busca rutas asignadas a un camión específico
     */
    List<Ruta> findByCamionId(Long camionId);
    
    /**
     * Busca rutas en curso (estado = 1) para un camión específico
     */
    List<Ruta> findByCamionIdAndEstado(Long camionId, int estado);
    
    /**
     * Busca rutas en curso con sus nodos cargados para un camión específico
     */
    @Query("SELECT DISTINCT r FROM Ruta r LEFT JOIN FETCH r.nodos WHERE r.camion.id = :camionId AND r.estado = :estado")
    List<Ruta> findByCamionIdAndEstadoWithNodos(@Param("camionId") Long camionId, @Param("estado") int estado);
    
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

    /**
     * Busca rutas que tengan alguno de los estados especificados
     */
    List<Ruta> findByEstadoIn(List<Integer> estados);
    
    /**
     * Busca rutas con todos sus nodos inicializados que tengan alguno de los estados especificados
     */
    @Query("SELECT DISTINCT r FROM Ruta r LEFT JOIN FETCH r.nodos WHERE r.estado IN :estados")
    List<Ruta> findByEstadoInWithNodos(@Param("estados") List<Integer> estados);

    List<Ruta> findByCamion(Camion camion);

    /**
     * Busca rutas que contengan un pedido específico en alguno de sus nodos
     */
    @Query("SELECT DISTINCT r FROM Ruta r JOIN r.nodos n WHERE n.pedido = :pedido")
    List<Ruta> findByPedidosContaining(@Param("pedido") Pedido pedido);
}