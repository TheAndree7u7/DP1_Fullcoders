package com.plg.repository;

import com.plg.entity.Bloqueo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BloqueoRepository extends JpaRepository<Bloqueo, Long> {
    // Métodos existentes
    List<Bloqueo> findByActivo(boolean activo);
    List<Bloqueo> findByFechaInicioBetween(LocalDate inicio, LocalDate fin);
    List<Bloqueo> findByFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(LocalDate fecha, LocalDate mismaFecha);
    List<Bloqueo> findByFechaInicio(LocalDate fecha);
    List<Bloqueo> findByFechaFin(LocalDate fecha);
    
    // Nuevos métodos para LocalDateTime
    List<Bloqueo> findByActivoTrue();
    List<Bloqueo> findByFechaInicioBeforeAndFechaFinAfter(LocalDateTime momento, LocalDateTime mismoMomento);
    
    // Método para encontrar bloqueos activos en una fecha específica
    default List<Bloqueo> findActivosEnFecha(LocalDate fecha) {
        LocalDateTime inicioDia = fecha.atStartOfDay();
        LocalDateTime finDia = fecha.atTime(23, 59, 59);
        return findByFechaInicioBeforeAndFechaFinAfter(finDia, inicioDia);
    }
}