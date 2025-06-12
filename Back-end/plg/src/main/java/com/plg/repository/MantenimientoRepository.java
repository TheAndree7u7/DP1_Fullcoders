package com.plg.repository;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.plg.config.DataLoader;
import com.plg.entity.Camion;
import com.plg.entity.Mantenimiento;

/**
 * Repositorio en memoria para gestionar mantenimientos.
 */
@Repository
public class MantenimientoRepository {

    /**
     * Obtiene todos los mantenimientos almacenados.
     *
     * @return Lista de todos los mantenimientos
     */
    public List<Mantenimiento> findAll() {
        return DataLoader.mantenimientos;
    }

    /**
     * Obtiene mantenimientos por mes.
     *
     * @param mes el mes (1-12)
     * @return Lista de mantenimientos del mes
     */
    public List<Mantenimiento> findByMes(int mes) {
        return DataLoader.mantenimientos.stream()
                .filter(m -> m.getMes() == mes)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene mantenimientos por día y mes específicos.
     *
     * @param dia el día del mes
     * @param mes el mes (1-12)
     * @return Lista de mantenimientos en esa fecha
     */
    public List<Mantenimiento> findByDiaAndMes(int dia, int mes) {
        return DataLoader.mantenimientos.stream()
                .filter(m -> m.getDia() == dia && m.getMes() == mes)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene mantenimientos por camión.
     *
     * @param camion el camión del cual obtener los mantenimientos
     * @return Lista de mantenimientos del camión
     */
    public List<Mantenimiento> findByCamion(Camion camion) {
        try {
            if (camion == null) {
                System.out.println("ERROR: Camión es null en findByCamion");
                return List.of();
            }

            System.out.println("DEBUG: Buscando mantenimientos para camión: " + camion.getCodigo());
            System.out.println("DEBUG: Total mantenimientos en memoria: " + DataLoader.mantenimientos.size());

            List<Mantenimiento> resultado = DataLoader.mantenimientos.stream()
                    .filter(m -> {
                        if (m == null) {
                            System.out.println("WARNING: Mantenimiento null encontrado");
                            return false;
                        }
                        if (m.getCamion() == null) {
                            System.out.println("WARNING: Mantenimiento con camión null encontrado");
                            return false;
                        }

                        // Comparar por código en lugar de por referencia de objeto
                        boolean coincide = m.getCamion().getCodigo().equals(camion.getCodigo());
                        if (coincide) {
                            System.out.println("DEBUG: Mantenimiento encontrado - Día: " + m.getDia()
                                    + ", Mes: " + m.getMes() + ", Camión: " + m.getCamion().getCodigo());
                        }
                        return coincide;
                    })
                    .collect(Collectors.toList());

            System.out.println("DEBUG: Mantenimientos encontrados para " + camion.getCodigo() + ": " + resultado.size());
            return resultado;

        } catch (Exception e) {
            System.out.println("ERROR en findByCamion: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Guarda un nuevo mantenimiento en memoria.
     *
     * @param mantenimiento el mantenimiento a guardar
     * @return el mantenimiento guardado
     */
    public Mantenimiento save(Mantenimiento mantenimiento) {
        DataLoader.mantenimientos.add(mantenimiento);
        return mantenimiento;
    }
}
