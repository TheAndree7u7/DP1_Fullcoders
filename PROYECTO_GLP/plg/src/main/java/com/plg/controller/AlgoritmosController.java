package com.plg.controller;

import com.plg.dto.AgrupamientoAPResultadoDTO;
import com.plg.dto.AlgoritmoGeneticoResultadoDTO;
import com.plg.service.AgrupamientoAPService;
import com.plg.service.AlgoritmoGeneticoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/algoritmos")
public class AlgoritmosController {

    @Autowired
    private AlgoritmoGeneticoService algoritmoGeneticoService;
    
    @Autowired
    private AgrupamientoAPService agrupamientoAPService;
    
    @PostMapping("/genetic")
    public ResponseEntity<AlgoritmoGeneticoResultadoDTO> generarRutasGenetico(@RequestBody Map<String, Object> params) {
        try {
            // Llamar al servicio y devolver el resultado como DTO
            AlgoritmoGeneticoResultadoDTO resultado = algoritmoGeneticoService.generarRutas(params);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            // En caso de error, devolver una respuesta de error
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/affinity-propagation")
    public ResponseEntity<AgrupamientoAPResultadoDTO> generarGruposAP(@RequestBody Map<String, Object> params) {
        try {
            // Llamar al servicio y devolver el resultado como DTO
            AgrupamientoAPResultadoDTO resultado = agrupamientoAPService.generarGrupos(params);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            // En caso de error, devolver una respuesta de error
            return ResponseEntity.badRequest().build();
        }
    }
}