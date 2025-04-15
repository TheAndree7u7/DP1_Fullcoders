package com.plg.repository;

import com.plg.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByEstado(int estado);
    List<Pedido> findByCamion_Codigo(String codigoCamion);
    List<Pedido> findByCamion_CodigoAndEstado(String codigoCamion, int estado);
List<Pedido> findByEstadoAndFechaEntregaNotNull(int estado);
long countByEstado(int estado);
    List<Pedido> findByCliente_Id(String clienteId);
    List<Pedido> findByCliente_IdAndEstado(String clienteId, int estado);
    List<Pedido> findByCamion_CodigoAndEstadoAndFechaEntregaNotNull(String codigoCamion, int estado);
    List<Pedido> findByCamion_CodigoAndEstadoAndFechaEntregaNull(String codigoCamion, int estado);
    List<Pedido> findByCodigoRuta(String idRuta);
}