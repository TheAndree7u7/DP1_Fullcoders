package com.plg.controller;

import com.plg.entity.Camion;
import com.plg.entity.Pedido;
import com.plg.service.CamionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/camiones")
public class CamionController {

    @Autowired
    private CamionService camionService;

    /**
     * Lista todos los camiones disponibles
     */
    @GetMapping
    public ResponseEntity<List<Camion>> getAllCamiones() {
        return ResponseEntity.ok(camionService.findAll());
    }
    
    /**
     * Obtiene un camión específico por su código
     */
    @GetMapping("/{codigo}")
    public ResponseEntity<?> getCamionByCodigo(@PathVariable String codigo) {
        Optional<Camion> camion = camionService.findById(codigo);
        if (camion.isPresent()) {
            return ResponseEntity.ok(camion.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Lista los camiones filtrados por estado
     */
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<Camion>> getCamionesByEstado(@PathVariable int estado) {
        return ResponseEntity.ok(camionService.findByEstado(estado));
    }
    
    /**
     * Lista los camiones filtrados por tipo
     */
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<Camion>> getCamionesByTipo(@PathVariable String tipo) {
        return ResponseEntity.ok(camionService.findByTipo(tipo));
    }
    
    /**
     * Obtiene todos los pedidos asignados a un camión específico
     */
    @GetMapping("/{codigo}/pedidos")
    public ResponseEntity<?> getPedidosByCamion(@PathVariable String codigo) {
        // Verificar primero si el camión existe
        Optional<Camion> camion = camionService.findById(codigo);
        if (!camion.isPresent()) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Camión no encontrado con código: " + codigo);
            return ResponseEntity.badRequest().body(response);
        }
        
        // Obtener los pedidos asignados a este camión
        List<Pedido> pedidos = camionService.findPedidosByCamion(codigo);
        return ResponseEntity.ok(pedidos);
    }
    
    /**
     * Obtiene un resumen de la disponibilidad de camiones
     */
    @GetMapping("/resumen")
    public ResponseEntity<?> getResumenCamiones() {
        return ResponseEntity.ok(camionService.getEstadisticasCamiones());
    }
    
    /**
     * Obtiene información detallada de un camión específico
     */
    @GetMapping("/{codigo}/detalle")
    public ResponseEntity<?> getDetalleCamion(@PathVariable String codigo) {
        Map<String, Object> detalle = camionService.getDetalleCamion(codigo);
        if (detalle == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(detalle);
    }
}