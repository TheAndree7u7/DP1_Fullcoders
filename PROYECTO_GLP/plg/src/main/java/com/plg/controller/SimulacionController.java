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
    public ResponseEntity<?> iniciarSimulacionTiempoReal() {
        simulacionTiempoRealService.iniciarSimulacion();
        
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Simulación en tiempo real iniciada");
        response.put("activa", true);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/detener-tiempo-real")
    public ResponseEntity<?> detenerSimulacionTiempoReal() {
        simulacionTiempoRealService.detenerSimulacion();
        
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Simulación en tiempo real detenida");
        response.put("activa", false);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/ajustar-velocidad")
    public ResponseEntity<?> ajustarVelocidadSimulacion(@RequestParam int factor) {
        simulacionTiempoRealService.ajustarFactorAceleracion(factor);
        
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Velocidad de simulación ajustada");
        response.put("factor", factor);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/estado")
    public ResponseEntity<?> obtenerEstadoSimulacion() {
        Map<String, Object> estado = new HashMap<>();
        estado.put("activa", simulacionTiempoRealService.isSimulacionActiva());
        estado.put("tiempo", simulacionTiempoRealService.getTiempoSimulacion());
        
        return ResponseEntity.ok(estado);
    }
}