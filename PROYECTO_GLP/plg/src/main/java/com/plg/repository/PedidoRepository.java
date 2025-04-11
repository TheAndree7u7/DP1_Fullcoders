package com.plg.repository;

import com.plg.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByEstado(int estado);
    List<Pedido> findByCamion_Codigo(String codigoCamion);
}