package com.plg.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.plg.dto.AgrupamientoAPResultadoDTO;
import com.plg.dto.AlgoritmoGeneticoResultadoDTO;
import com.plg.service.AgrupamientoAPService;
import com.plg.service.AlgoritmoGeneticoService;

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
            AlgoritmoGeneticoResultadoDTO resultado = algoritmoGeneticoService.generarRutas(params);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/affinity-propagation")
    public ResponseEntity<AgrupamientoAPResultadoDTO> generarGruposAP(@RequestBody Map<String, Object> params) {
        try {
            AgrupamientoAPResultadoDTO resultado = agrupamientoAPService.generarGrupos(params);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
