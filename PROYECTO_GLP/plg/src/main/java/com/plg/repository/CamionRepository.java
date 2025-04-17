package com.plg.repository;

import com.plg.entity.Camion;
import com.plg.enums.EstadoCamion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CamionRepository extends JpaRepository<Camion, Long> {
    List<Camion> findByEstado(EstadoCamion estado);
    List<Camion> findByTipo(String tipo);
    long countByEstado(EstadoCamion estado);
    List<Camion> findByTipoAndEstado(String tipo, EstadoCamion estado);
 
    //FindfindByEstadoNot
    List<Camion> findByEstadoNot(EstadoCamion estado);
    //FindByCodigo es como id  
    //Usa 
    Optional<Camion> findByCodigo(String codigo); // Esto es para buscar por id, pero el id es un  String

}