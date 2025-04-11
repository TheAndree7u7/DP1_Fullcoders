package com.plg.repository;

import com.plg.entity.Camion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CamionRepository extends JpaRepository<Camion, String> {
    List<Camion> findByEstado(int estado);
    List<Camion> findByTipo(String tipo);
}