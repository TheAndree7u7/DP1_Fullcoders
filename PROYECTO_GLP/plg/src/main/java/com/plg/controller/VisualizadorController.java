package com.plg.controller;

import com.plg.service.VisualizadorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/visualizador")
public class VisualizadorController {

    @Autowired
    private VisualizadorService visualizadorService;

    @GetMapping("/mapa")
    public ResponseEntity<?> obtenerDatosMapa(
            @RequestParam(required = false) Boolean mostrarPedidos,
            @RequestParam(required = false) Boolean mostrarCamiones,
            @RequestParam(required = false) Boolean mostrarBloqueos) {
        
        return ResponseEntity.ok(visualizadorService.obtenerDatosMapa(
                mostrarPedidos != null ? mostrarPedidos : true,
                mostrarCamiones != null ? mostrarCamiones : true,
                mostrarBloqueos != null ? mostrarBloqueos : true));
    }
    
    @GetMapping("/estado-general")
    public ResponseEntity<?> obtenerEstadoGeneral() {
        return ResponseEntity.ok(visualizadorService.obtenerEstadoGeneral());
    }
    
    @PostMapping("/filtrar")
    public ResponseEntity<?> filtrarVisualizacion(@RequestBody Map<String, Object> filtros) {
        return ResponseEntity.ok(visualizadorService.aplicarFiltros(filtros));
    }
}