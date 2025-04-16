package com.plg.repository;

import com.plg.entity.Camion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CamionRepository extends JpaRepository<Camion, Long> {
    List<Camion> findByEstado(int estado);
    List<Camion> findByTipo(String tipo);
    long countByEstado(int estado);
    List<Camion> findByTipoAndEstado(String tipo, int estado);
 
    //FindfindByEstadoNot
    List<Camion> findByEstadoNot(int estado);
    //FindByCodigo es como id  
    //Usa 
    Optional<Camion> findByCodigo(String codigo); // Esto es para buscar por id, pero el id es un  String

}