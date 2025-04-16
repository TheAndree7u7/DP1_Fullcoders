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
        return ResponseEntity.ok(simulacionTiempoRealService.iniciarSimulacion());
    }
    
    @PostMapping("/detener-tiempo-real")
    public ResponseEntity<Map<String, Object>> detenerSimulacionTiempoReal() {
        return ResponseEntity.ok(simulacionTiempoRealService.detenerSimulacion());
    }
    
    @PostMapping("/ajustar-velocidad")
    public ResponseEntity<Map<String, Object>> ajustarVelocidadSimulacion(@RequestParam int factor) {
        return ResponseEntity.ok(simulacionTiempoRealService.ajustarVelocidad(factor));
    }
    
    @GetMapping("/estado")
    public ResponseEntity<Map<String, Object>> obtenerEstadoSimulacion() {
        return ResponseEntity.ok(simulacionTiempoRealService.obtenerEstadoSimulacion());
    }
}