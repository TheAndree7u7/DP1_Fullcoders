package com.plg.repository;

import com.plg.entity.Pedido;
import com.plg.entity.EstadoPedido;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByEstado(EstadoPedido estado);
    List<Pedido> findByEstadoNot(EstadoPedido estado);
    List<Pedido> findByCamion_Codigo(String codigoCamion);
    List<Pedido> findByCamion_CodigoAndEstado(String codigoCamion, EstadoPedido estado);
    List<Pedido> findByEstadoAndFechaEntregaRequeridaNotNull(EstadoPedido estado);
    long countByEstado(EstadoPedido estado);
    List<Pedido> findByCliente_Id(String clienteId);
    List<Pedido> findByCliente_IdAndEstado(String clienteId, EstadoPedido estado);
    List<Pedido> findByCamion_CodigoAndEstadoAndFechaEntregaRequeridaNotNull(String codigoCamion, EstadoPedido estado);
    List<Pedido> findByCamion_CodigoAndEstadoAndFechaEntregaRequeridaNull(String codigoCamion, EstadoPedido estado);
    
    @Query("SELECT p FROM Pedido p JOIN p.asignaciones a WHERE a.ruta.codigo = :idRuta")
    List<Pedido> findByCodigoRuta(@Param("idRuta") String idRuta);
    List<Pedido> findByEstadoIn(List<EstadoPedido> estados);

}