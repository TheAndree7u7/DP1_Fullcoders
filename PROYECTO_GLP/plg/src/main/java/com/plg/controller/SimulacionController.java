package com.plg.controller;

import com.plg.service.SimulacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/simulacion")
public class SimulacionController {

    @Autowired
    private SimulacionService simulacionService;

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
}