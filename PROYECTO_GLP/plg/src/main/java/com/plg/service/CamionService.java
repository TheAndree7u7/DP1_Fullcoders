package com.plg.service;

import com.plg.dto.CamionDTO;
import com.plg.entity.Camion;
import com.plg.entity.EstadoCamion;
import com.plg.entity.Pedido;
import com.plg.entity.EstadoPedido;
import com.plg.repository.CamionRepository;
import com.plg.repository.PedidoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * Obtiene todos los camiones como DTOs para evitar LazyInitializationException
     */
    @Transactional(readOnly = true)
    public List<CamionDTO> findAllDTO() {
        List<Camion> camiones = camionRepository.findAll();
        return CamionDTO.fromEntities(camiones, false);
    }
    
    /**
     * Obtiene un camión por su código
     */
    public Optional<Camion> findById(String codigo) {
        return camionRepository.findByCodigo(codigo);
    }
    
    /**
     * Obtiene un camión por su código como DTO para evitar LazyInitializationException
     */
    @Transactional(readOnly = true)
    public CamionDTO findDTOById(String codigo) {
        Optional<Camion> camionOpt = camionRepository.findByCodigo(codigo);
        if (camionOpt.isPresent()) {
            return new CamionDTO(camionOpt.get(), true);
        }
        return null;
    }
    
    /**
     * Obtiene camiones por estado
     */
    public List<Camion> findByEstado(EstadoCamion estado) {
        return camionRepository.findByEstado(estado);
    }
    
    /**
     * Obtiene camiones por valor numérico del estado
     */
    public List<Camion> findByEstadoValue(int estadoValue) {
        EstadoCamion estado = EstadoCamion.fromValue(estadoValue);
        return camionRepository.findByEstado(estado);
    }
    
    /**
     * Obtiene camiones por estado como DTOs para evitar LazyInitializationException
     */
    @Transactional(readOnly = true)
    public List<CamionDTO> findByEstadoDTO(EstadoCamion estado) {
        List<Camion> camiones = camionRepository.findByEstado(estado);
        return CamionDTO.fromEntities(camiones, false);
    }
    
    /**
     * Obtiene camiones por valor numérico del estado como DTOs
     */
    @Transactional(readOnly = true)
    public List<CamionDTO> findByEstadoValueDTO(int estadoValue) {
        EstadoCamion estado = EstadoCamion.fromValue(estadoValue);
        List<Camion> camiones = camionRepository.findByEstado(estado);
        return CamionDTO.fromEntities(camiones, false);
    }
    
    /**
     * Obtiene camiones por tipo
     */
    public List<Camion> findByTipo(String tipo) {
        return camionRepository.findByTipo(tipo);
    }
    
    /**
     * Obtiene camiones por tipo como DTOs para evitar LazyInitializationException
     */
    @Transactional(readOnly = true)
    public List<CamionDTO> findByTipoDTO(String tipo) {
        List<Camion> camiones = camionRepository.findByTipo(tipo);
        return CamionDTO.fromEntities(camiones, false);
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
            .collect(Collectors.groupingBy(c -> c.getEstado().ordinal(), Collectors.counting()));
        
        stats.put("porEstado", mapEstadosToNombres(camionesEstado));
        
        // Información por tipo
        Map<String, Long> camionesTipo = allCamiones.stream()
            .collect(Collectors.groupingBy(Camion::getTipo, Collectors.counting()));
        stats.put("porTipo", camionesTipo);
        
        return stats;
    }
    
    /**
     * Obtiene información detallada del camión incluyendo pedidos, mantenimientos y averías asociadas
     */
    @Transactional(readOnly = true)
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
        detalle.put("estado", camion.getEstado().ordinal());
        detalle.put("estadoNombre", camion.getEstadoTexto());
        
        // Pedidos asignados
        List<Pedido> pedidos = pedidoRepository.findByCamion_Codigo(codigo);
        detalle.put("pedidosAsignados", pedidos.size());
        detalle.put("pedidos", pedidos);
        
        // Cálculo de carga actual
        double cargaActual = pedidos.stream()
            .filter(p -> p.getEstado() == EstadoPedido.PENDIENTE_PLANIFICACION || p.getEstado() == EstadoPedido.ENTREGADO_PARCIALMENTE) // Asignados o en ruta
            .mapToDouble(Pedido::getVolumenGLPAsignado)
            .sum();
        detalle.put("cargaActual", cargaActual);
        detalle.put("porcentajeOcupacion", camion.getCapacidad() > 0 ? 
                                (cargaActual / camion.getCapacidad()) * 100 : 0);
        
        // Mantenimientos
        detalle.put("mantenimientos", camion.getMantenimientos());
        
        // Averías
        detalle.put("averias", camion.getAverias());
        
        // Combustible
        detalle.put("combustibleActual", camion.getCombustibleActual());
        detalle.put("capacidadTanque", camion.getCapacidadTanque());
        detalle.put("porcentajeCombustible", (camion.getCombustibleActual() / camion.getCapacidadTanque()) * 100);
        detalle.put("distanciaMaxima", camion.calcularDistanciaMaxima());
        
        // Posición actual
        detalle.put("posX", camion.getPosX());
        detalle.put("posY", camion.getPosY());
        
        return detalle;
    }
    
    // Métodos auxiliares
    
    private double getCapacidadPromedio(List<Camion> camiones) {
        if (camiones.isEmpty()) {
            return 0.0;
        }
        
        double sumCapacidad = camiones.stream()
            .mapToDouble(Camion::getCapacidad)
            .sum();
            
        return sumCapacidad / camiones.size();
    }
    
    private Map<String, Long> mapEstadosToNombres(Map<Integer, Long> estadosCount) {
        Map<String, Long> result = new HashMap<>();
        
        for (Map.Entry<Integer, Long> entry : estadosCount.entrySet()) {
            String nombre = mapEstadoToNombre(entry.getKey());
            result.put(nombre, entry.getValue());
        }
        
        return result;
    }
    
    private String mapEstadoToNombre(int estado) {
        return switch (estado) {
            case 0 -> "Disponible";
            case 1 -> "En ruta";
            case 2 -> "En mantenimiento preventivo";
            case 3 -> "En mantenimiento por avería";
            case 4 -> "Sin combustible";
            case 5 -> "De baja";
            default -> "Desconocido";
        };
    }
}