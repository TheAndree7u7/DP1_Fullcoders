package com.plg.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.plg.entity.Camion;
import com.plg.entity.EntregaParcial;
import com.plg.entity.EstadoEntregaParcial;
import com.plg.entity.Pedido;

@Repository
public interface EntregaParcialRepository extends JpaRepository<EntregaParcial, Long> {

    List<EntregaParcial> findByCamion(Camion camion);

    List<EntregaParcial> findByPedido(Pedido pedido);

    List<EntregaParcial> findByPedidoId(Long pedidoId);

    List<EntregaParcial> findByCamionCodigo(String codigoCamion);

    List<EntregaParcial> findByEstado(EstadoEntregaParcial estado);

    List<EntregaParcial> findByCamionAndEstado(Camion camion, EstadoEntregaParcial estado);

    List<EntregaParcial> findByPedidoAndEstado(Pedido pedido, EstadoEntregaParcial estado);

    Optional<EntregaParcial> findByCamionAndPedido(Camion camion, Pedido pedido);

    Optional<EntregaParcial> findByCamionAndPedidoAndEstadoNot(Camion camion, Pedido pedido, EstadoEntregaParcial estadoNoDeseado);

    List<EntregaParcial> findByFechaAsignacionBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    List<EntregaParcial> findByFechaEntregaBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    @Query("SELECT SUM(e.volumenGLP) FROM EntregaParcial e WHERE e.camion = :camion AND e.estado = 'ENTREGADO'")
    Double calcularVolumenTotalEntregadoPorCamion(@Param("camion") Camion camion);

    @Query("SELECT SUM(e.volumenGLP) FROM EntregaParcial e WHERE e.pedido = :pedido AND e.estado = 'ENTREGADO'")
    Double calcularVolumenTotalEntregadoAPedido(@Param("pedido") Pedido pedido);

    @Query("SELECT SUM(e.porcentajePedido) FROM EntregaParcial e WHERE e.pedido = :pedido AND e.estado = 'ENTREGADO'")
    Double calcularPorcentajeCompletadoDePedido(@Param("pedido") Pedido pedido);

    @Query("SELECT e FROM EntregaParcial e WHERE e.estado = 'ASIGNADO' AND e.fechaEntregaRequerida < :fechaLimite")
    List<EntregaParcial> findEntregasRetrasadas(@Param("fechaLimite") LocalDateTime fechaLimite);

    @Query("SELECT e FROM EntregaParcial e WHERE e.pedido.id IN :pedidoIds")
    List<EntregaParcial> findByPedidoIds(@Param("pedidoIds") List<Long> pedidoIds);

    @Modifying
    @Query("UPDATE EntregaParcial e SET e.estado = :nuevoEstado WHERE e.camion = :camion AND e.estado = :estadoActual")
    int actualizarEstadoEntregasPorCamion(@Param("camion") Camion camion,
                                          @Param("estadoActual") EstadoEntregaParcial estadoActual,
                                          @Param("nuevoEstado") EstadoEntregaParcial nuevoEstado);

    @Query("SELECT e.estado, COUNT(e) FROM EntregaParcial e WHERE e.camion = :camion GROUP BY e.estado")
    List<Object[]> contarEntregasPorEstadoYCamion(@Param("camion") Camion camion);
}
