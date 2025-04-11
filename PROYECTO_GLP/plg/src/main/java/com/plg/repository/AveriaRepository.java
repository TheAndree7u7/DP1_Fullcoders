package com.plg.repository;

import com.plg.entity.Averia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AveriaRepository extends JpaRepository<Averia, Long> {
    List<Averia> findByCamion_Codigo(String codigoCamion);
    List<Averia> findByEstado(int estado);
    List<Averia> findBySeveridad(int severidad);
    List<Averia> findByFechaHoraReporteBetween(LocalDateTime inicio, LocalDateTime fin);
}