package com.plg.repository;

import com.plg.entity.Mantenimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface MantenimientoRepository extends JpaRepository<Mantenimiento, Long> {
    List<Mantenimiento> findByCamion_Codigo(String codigoCamion);
    List<Mantenimiento> findByEstado(int estado);
    List<Mantenimiento> findByFechaInicioBetween(LocalDate fechaInicio, LocalDate fechaFin);
    List<Mantenimiento> findByFechaInicio(LocalDate fecha);
List<Mantenimiento> findByFechaFin(LocalDate fecha);
}