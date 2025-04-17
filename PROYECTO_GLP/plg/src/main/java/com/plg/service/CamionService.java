package com.plg.service;

import com.plg.entity.Camion;
import com.plg.entity.Pedido;
import com.plg.repository.CamionRepository;
import com.plg.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CamionService {

    @Autowired
    private CamionRepository camionRepository;
    
    @Autowired
    private PedidoRepository pedidoRepository;
    
    /**
     * Obtiene todos los camiones
     */
    public List<Camion> findAll() {
        return camionRepository.findAll();
    }
    
    /**
     * Obtiene un camión por su código
     */
    public Optional<Camion> findById(String codigo) {
        return camionRepository.findByCodigo(codigo);
    }
    
    /**
     * Obtiene camiones por estado
     */
    public List<Camion> findByEstado(int estado) {
        return camionRepository.findByEstado(estado);
    }
    
    /**
     * Obtiene camiones por tipo
     */
    public List<Camion> findByTipo(String tipo) {
        return camionRepository.findByTipo(tipo);
    }
    
    /**
     * Obtiene pedidos asignados a un camión
     */
    public List<Pedido> findPedidosByCamion(String codigoCamion) {
        return pedidoRepository.findByCamion_Codigo(codigoCamion);
    }
    
    /**
     * Obtiene estadísticas de camiones
     */
    public Map<String, Object> getEstadisticasCamiones() {
        List<Camion> allCamiones = camionRepository.findAll();
        Map<String, Object> stats = new HashMap<>();
        
        // Información general
        stats.put("totalCamiones", allCamiones.size());
        stats.put("capacidadPromedio", getCapacidadPromedio(allCamiones));
        
        // Información por estado
        Map<Integer, Long> camionesEstado = allCamiones.stream()
            .collect(Collectors.groupingBy(Camion::getEstado, Collectors.counting()));
        
        stats.put("porEstado", mapEstadosToNombres(camionesEstado));
        
        // Información por tipo
        Map<String, Long> camionesTipo = allCamiones.stream()
            .collect(Collectors.groupingBy(Camion::getTipo, Collectors.counting()));
        
        stats.put("porTipo", camionesTipo);
        
        // Información de capacidad disponible
        double capacidadTotal = allCamiones.stream()
            .filter(c -> c.getEstado() == 0) // Solo disponibles
            .mapToDouble(Camion::getCapacidad)
            .sum();
        stats.put("capacidadDisponible", capacidadTotal);
        
        return stats;
    }
    
    /**
     * Obtiene información detallada del camión incluyendo pedidos, mantenimientos y averías asociadas
     */
    public Map<String, Object> getDetalleCamion(String codigo) {
        Optional<Camion> optCamion = camionRepository.findByCodigo(codigo);
        if (!optCamion.isPresent()) {
            return null;
        }
        
        Camion camion = optCamion.get();
        Map<String, Object> detalle = new HashMap<>();
        
        // Información básica
        detalle.put("codigo", camion.getCodigo());
        detalle.put("tipo", camion.getTipo());
        detalle.put("capacidad", camion.getCapacidad());
        detalle.put("tara", camion.getTara());
        detalle.put("estado", camion.getEstado());
        detalle.put("estadoNombre", mapEstadoToNombre(camion.getEstado()));
        
        // Pedidos asignados
        List<Pedido> pedidos = pedidoRepository.findByCamion_Codigo(codigo);
        detalle.put("pedidosAsignados", pedidos.size());
        detalle.put("pedidos", pedidos);
        
        // Cálculo de carga actual
        double cargaActual = pedidos.stream()
            .filter(p -> p.getEstado() == 1 || p.getEstado() == 2) // Asignados o en ruta
            .mapToDouble(Pedido::getVolumenGLPAsignado)
            .sum();
        detalle.put("cargaActual", cargaActual);
        detalle.put("porcentajeOcupacion", camion.getCapacidad() > 0 ? 
                                         (cargaActual / camion.getCapacidad()) * 100 : 0);
        
        return detalle;
    }
    
    // Métodos auxiliares
    
    private double getCapacidadPromedio(List<Camion> camiones) {
        if (camiones.isEmpty()) return 0;
        return camiones.stream()
            .mapToDouble(Camion::getCapacidad)
            .average()
            .orElse(0);
    }
    
    private Map<String, Long> mapEstadosToNombres(Map<Integer, Long> estadosCount) {
        Map<String, Long> result = new HashMap<>();
        estadosCount.forEach((estado, count) -> {
            result.put(mapEstadoToNombre(estado), count);
        });
        return result;
    }
    
    private String mapEstadoToNombre(int estado) {
        switch(estado) {
            case 0: return "Disponible";
            case 1: return "En Ruta";
            case 2: return "En Mantenimiento";
            case 3: return "Averiado";
            default: return "Desconocido";
        }
    }
}