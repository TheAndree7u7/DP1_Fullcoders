package com.plg.repository;

import com.plg.entity.Camion;
import com.plg.entity.EntregaParcial;
import com.plg.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones CRUD de entidades EntregaParcial.
 * Permite gestionar la relación entre camiones, pedidos y entregas parciales.
 */
@Repository
public interface EntregaParcialRepository extends JpaRepository<EntregaParcial, Long> {
    
    /**
     * Encuentra todas las entregas parciales asociadas a un camión
     */
    List<EntregaParcial> findByCamion(Camion camion);
    
    /**
     * Encuentra todas las entregas parciales asociadas a un pedido
     */
    List<EntregaParcial> findByPedido(Pedido pedido);
    
    /**
     * Encuentra todas las entregas parciales asociadas a un pedido por su ID
     */
    List<EntregaParcial> findByPedidoId(Long pedidoId);
    
    /**
     * Encuentra todas las entregas parciales asociadas a un camión por su código
     */
    List<EntregaParcial> findByCamionCodigo(String codigoCamion);
    
    /**
     * Encuentra todas las entregas parciales con un estado específico
     */
    List<EntregaParcial> findByEstado(int estado);
    
    /**
     * Encuentra todas las entregas parciales con un estado específico asociadas a un camión
     */
    List<EntregaParcial> findByCamionAndEstado(Camion camion, int estado);
    
    /**
     * Encuentra todas las entregas parciales con un estado específico asociadas a un pedido
     */
    List<EntregaParcial> findByPedidoAndEstado(Pedido pedido, int estado);
    
    /**
     * Encuentra una entrega parcial específica por camión y pedido
     */
    Optional<EntregaParcial> findByCamionAndPedido(Camion camion, Pedido pedido);
    
    /**
     * Encuentra una entrega parcial específica por camión y pedido donde la entrega no ha sido completada
     */
    Optional<EntregaParcial> findByCamionAndPedidoAndEstadoNot(Camion camion, Pedido pedido, int estadoNoDeseado);
    
    /**
     * Encuentra entregas parciales creadas en un rango de fechas
     */
    List<EntregaParcial> findByFechaAsignacionBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    /**
     * Encuentra entregas parciales entregadas en un rango de fechas
     */
    List<EntregaParcial> findByFechaEntregaBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    /**
     * Calcula el volumen total de GLP entregado por un camión en todas sus entregas completadas
     */
    @Query("SELECT SUM(e.volumenGLP) FROM EntregaParcial e WHERE e.camion = :camion AND e.estado = 2")
    Double calcularVolumenTotalEntregadoPorCamion(@Param("camion") Camion camion);
    
    /**
     * Calcula el volumen total de GLP entregado a un pedido en todas sus entregas parciales completadas
     */
    @Query("SELECT SUM(e.volumenGLP) FROM EntregaParcial e WHERE e.pedido = :pedido AND e.estado = 2")
    Double calcularVolumenTotalEntregadoAPedido(@Param("pedido") Pedido pedido);
    
    /**
     * Calcula el porcentaje total completado de un pedido sumando todas sus entregas parciales
     */
    @Query("SELECT SUM(e.porcentajePedido) FROM EntregaParcial e WHERE e.pedido = :pedido AND e.estado = 2")
    Double calcularPorcentajeCompletadoDePedido(@Param("pedido") Pedido pedido);
    
    /**
     * Encuentra entregas pendientes (asignadas o en ruta) que deberían haberse entregado ya
     * (fecha de asignación anterior a la fecha actual menos un margen de tiempo)
     */
    @Query("SELECT e FROM EntregaParcial e WHERE e.estado IN (0, 1) AND e.fechaAsignacion < :fechaLimite")
    List<EntregaParcial> findEntregasRetrasadas(@Param("fechaLimite") LocalDateTime fechaLimite);
    
    /**
     * Encuentra entregas parciales para un conjunto de IDs de pedidos
     */
    @Query("SELECT e FROM EntregaParcial e WHERE e.pedido.id IN :pedidoIds")
    List<EntregaParcial> findByPedidoIds(@Param("pedidoIds") List<Long> pedidoIds);
    
    /**
     * Actualiza el estado de todas las entregas parciales de un camión
     */
    @Query("UPDATE EntregaParcial e SET e.estado = :nuevoEstado WHERE e.camion = :camion AND e.estado = :estadoActual")
    int actualizarEstadoEntregasPorCamion(
            @Param("camion") Camion camion, 
            @Param("estadoActual") int estadoActual, 
            @Param("nuevoEstado") int nuevoEstado);
    
    /**
     * Cuenta las entregas parciales por estado para un camión específico
     */
    @Query("SELECT e.estado, COUNT(e) FROM EntregaParcial e WHERE e.camion = :camion GROUP BY e.estado")
    List<Object[]> contarEntregasPorEstadoYCamion(@Param("camion") Camion camion);
}