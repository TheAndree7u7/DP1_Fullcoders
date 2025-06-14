package com.plg.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.plg.dto.CamionDto;
import com.plg.dto.request.CamionRequest;
import com.plg.entity.Camion;
import com.plg.entity.Coordenada;
import com.plg.entity.TipoCamion;
import com.plg.factory.CamionFactory;
import com.plg.repository.CamionRepository;
import com.plg.utils.ExcepcionesPerzonalizadas.InvalidInputException;

/**
 * Servicio para operaciones sobre camiones.
 */
@Service
public class CamionService {

    private final CamionRepository camionRepository;

    public CamionService(CamionRepository camionRepository) {
        this.camionRepository = camionRepository;
    }

    /**
     * Lista todos los camiones registrados.
     */
    public List<Camion> listar() {
        return camionRepository.findAll();
    }

    /**
     * Resumen de camiones por tipo.
     */
    public Map<String, Object> resumen() {
        List<Camion> camiones = camionRepository.findAll();
        Map<String, Object> datos = new HashMap<>();
        datos.put("total", camiones.size());
        datos.put("porTipo",
                camiones.stream()
                        .collect(Collectors.groupingBy(c -> c.getTipo().name(), Collectors.counting())));
        return datos;
    }

    /**
     * Resumen de camiones por estado (usando repository).
     */
    public Map<String, Object> resumenPorEstado() {
        Map<String, Long> porEstado = camionRepository.countByEstado();
        Map<String, Object> datos = new HashMap<>();
        datos.put("total", porEstado.values().stream().mapToLong(Long::longValue).sum());
        datos.put("porEstado", porEstado);
        return datos;
    }

    /**
     * Lista los estados posibles de los camiones con su descripción (usando
     * repository).
     */
    public List<Map<String, String>> listarEstados() {
        return camionRepository.listarEstadosPosibles();
    }

    /**
     * Crea un camión nuevo.
     */
    public Camion agregar(CamionRequest request) {
        try {
            Coordenada coordenada = new Coordenada(request.getY(), request.getX());
            TipoCamion tipo = request.getTipo();
            Camion camion = CamionFactory.crearCamionesPorTipo(tipo, request.isOperativo(), coordenada);
            return camionRepository.save(camion);
        } catch (Exception e) {
            throw new InvalidInputException("No se pudo crear el camión", e);
        }
    }

    /**
     * Lista todos los camiones con sus datos principales (estado, id, tipo,
     * coordenada).
     */
    public List<CamionDto> listarCamionesEstado() {
        return camionRepository.listarCamionesEstado();
    }
}
