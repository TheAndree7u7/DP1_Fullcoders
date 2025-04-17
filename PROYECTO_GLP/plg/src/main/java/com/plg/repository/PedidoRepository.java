package com.plg.repository;

import com.plg.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByEstado(int estado);
    List<Pedido> findByCamion_Codigo(String codigoCamion);
    List<Pedido> findByCamion_CodigoAndEstado(String codigoCamion, int estado);
    List<Pedido> findByEstadoAndFechaEntregaRequeridaNotNull(int estado);
    long countByEstado(int estado);
    List<Pedido> findByCliente_Id(String clienteId);
    List<Pedido> findByCliente_IdAndEstado(String clienteId, int estado);
    List<Pedido> findByCamion_CodigoAndEstadoAndFechaEntregaRequeridaNotNull(String codigoCamion, int estado);
    List<Pedido> findByCamion_CodigoAndEstadoAndFechaEntregaRequeridaNull(String codigoCamion, int estado);
    
    @Query("SELECT p FROM Pedido p JOIN p.asignaciones a WHERE a.ruta.codigo = :idRuta")
    List<Pedido> findByCodigoRuta(@Param("idRuta") String idRuta);
    List<Pedido> findByEstadoIn(List<Integer> asList);
}