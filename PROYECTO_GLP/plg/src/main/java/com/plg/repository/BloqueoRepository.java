package com.plg.repository;

import com.plg.entity.Bloqueo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface BloqueoRepository extends JpaRepository<Bloqueo, Long> {
    List<Bloqueo> findByActivo(boolean activo);
    List<Bloqueo> findByFechaInicioBetween(LocalDate inicio, LocalDate fin);
    List<Bloqueo> findByFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(LocalDate fecha, LocalDate mismaFecha);
    List<Bloqueo> findByFechaInicio(LocalDate fecha);
List<Bloqueo> findByFechaFin(LocalDate fecha);
}