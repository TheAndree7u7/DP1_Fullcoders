package com.plg.controller;

import com.plg.service.SimulacionService;
import com.plg.service.SimulacionTiempoRealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/simulacion")
public class SimulacionController {

    @Autowired
    private SimulacionService simulacionService;
    
    @Autowired
    private SimulacionTiempoRealService simulacionTiempoRealService;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping("/diario")
    public ResponseEntity<?> simularEscenarioDiario() {
        return ResponseEntity.ok(simulacionService.simularEscenarioDiario());
    }
    
    @GetMapping("/semanal")
    public ResponseEntity<?> simularEscenarioSemanal(
            @RequestParam(defaultValue = "7") int dias) {
        return ResponseEntity.ok(simulacionService.simularEscenarioSemanal(dias));
    }
    
    @GetMapping("/colapso")
    public ResponseEntity<?> simularEscenarioColapso() {
        return ResponseEntity.ok(simulacionService.simularEscenarioColapso());
    }
    
    @PostMapping("/personalizado")
    public ResponseEntity<?> simularEscenarioPersonalizado(@RequestBody Map<String, Object> params) {
        // Esta implementación es más avanzada y requeriría configuración adicional
        // Por ahora, delegamos al escenario diario con algunas modificaciones
        return ResponseEntity.ok(simulacionService.simularEscenarioDiario());
    }
    
    // NUEVOS ENDPOINTS PARA TIEMPO REAL
    
    @PostMapping("/iniciar-tiempo-real")
    public ResponseEntity<Map<String, Object>> iniciarSimulacionTiempoReal() {
        try {
            return ResponseEntity.ok(simulacionTiempoRealService.iniciarSimulacion());
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error al iniciar la simulación");
            error.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/detener-tiempo-real")
    public ResponseEntity<Map<String, Object>> detenerSimulacionTiempoReal() {
        try {
            return ResponseEntity.ok(simulacionTiempoRealService.detenerSimulacion());
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error al detener la simulación");
            error.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/ajustar-velocidad")
    public ResponseEntity<Map<String, Object>> ajustarVelocidadSimulacion(@RequestParam int factor) {
        try {
            return ResponseEntity.ok(simulacionTiempoRealService.ajustarVelocidad(factor));
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error al ajustar la velocidad");
            error.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/estado")
    public ResponseEntity<Map<String, Object>> obtenerEstadoSimulacion() {
        try {
            return ResponseEntity.ok(simulacionTiempoRealService.obtenerEstadoSimulacion());
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error al obtener el estado de la simulación");
            error.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    // Endpoint para obtener posiciones simplificadas (para evitar el problema de anidamiento)
    @GetMapping("/posiciones")
    public ResponseEntity<Map<String, Object>> obtenerPosicionesSimplificadas() {
        try {
            return ResponseEntity.ok(simulacionTiempoRealService.obtenerPosicionesSimplificadas());
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error al obtener las posiciones");
            error.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}