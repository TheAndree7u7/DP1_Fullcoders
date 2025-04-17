package com.plg.controller;

import com.plg.service.RutaService;
import com.plg.service.AlgoritmoGeneticoService;
import com.plg.service.AgrupamientoAPService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rutas")
public class RutaController {

    @Autowired
    private RutaService rutaService;
    
    @Autowired
    private AlgoritmoGeneticoService algoritmoGeneticoService;
    
    @Autowired
    private AgrupamientoAPService agrupamientoAPService;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> obtenerTodasLasRutas() {
        return ResponseEntity.ok(rutaService.obtenerResumeneRutas());
    }

    @PostMapping("/generar")
    public ResponseEntity<?> generarRutas(@RequestBody Map<String, Object> params) {
        String algoritmo = (String) params.getOrDefault("algoritmo", "genetico");
        
        if ("genetico".equals(algoritmo)) {
            return ResponseEntity.ok(algoritmoGeneticoService.generarRutas(params));
        } else if ("agrupamiento".equals(algoritmo)) {
            return ResponseEntity.ok(agrupamientoAPService.generarGrupos(params));
        } else {
            return ResponseEntity.badRequest().body("Algoritmo no soportado");
        }
    }
    
    @GetMapping("/optimizar/{idRuta}")
    public ResponseEntity<?> optimizarRuta(@PathVariable String idRuta, 
                                           @RequestParam(required = false) Boolean considerarBloqueos) {
        boolean usarBloqueos = considerarBloqueos != null ? considerarBloqueos : true;
        return ResponseEntity.ok(rutaService.optimizarRuta(idRuta, usarBloqueos));
    }
    
    @GetMapping("/distancia")
    public ResponseEntity<?> calcularDistancia(@RequestParam int x1, 
                                             @RequestParam int y1, 
                                             @RequestParam int x2, 
                                             @RequestParam int y2) {
        return ResponseEntity.ok(rutaService.calcularDistancia(x1, y1, x2, y2));
    }
}