package com.plg.controller;

import com.plg.service.BloqueoService;
import com.plg.service.ConversionArchivoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/bloqueos")
public class BloqueoController {

    @Autowired
    private BloqueoService bloqueoService;
    
    @Autowired
    private ConversionArchivoService conversionArchivoService;
    
    /**
     * Endpoint para listar todos los bloqueos
     */
    @GetMapping("/listar")
    public ResponseEntity<Map<String, Object>> listarBloqueos() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            var bloqueos = bloqueoService.listarBloqueos();
            response.put("success", true);
            response.put("bloqueosCount", bloqueos.size());
            response.put("bloqueos", bloqueos);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    /**
     * Endpoint para cargar bloqueos de un mes específico
     */
    @GetMapping("/cargar/{anio}/{mes}")
    public ResponseEntity<Map<String, Object>> cargarBloqueosDelMes(
            @PathVariable("anio") int anio,
            @PathVariable("mes") int mes) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            var bloqueos = bloqueoService.cargarBloqueosMensuales(anio, mes);
            response.put("success", true);
            response.put("bloqueosCount", bloqueos.size());
            response.put("message", "Se cargaron " + bloqueos.size() + " bloqueos para " + anio + "-" + mes);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Endpoint para convertir un archivo de bloqueos al nuevo formato
     */
    @GetMapping("/convertir/{anio}/{mes}")
    public ResponseEntity<Map<String, Object>> convertirArchivoBloqueos(
            @PathVariable("anio") int anio,
            @PathVariable("mes") int mes) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean exito = conversionArchivoService.convertirArchivoBloqueos(anio, mes);
            
            if (exito) {
                response.put("success", true);
                response.put("message", "Archivo convertido exitosamente para " + anio + "-" + mes);
            } else {
                response.put("success", false);
                response.put("message", "No se pudo convertir el archivo. Verifique que exista.");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Endpoint para verificar bloqueos entre dos puntos
     */
    @GetMapping("/verificar")
    public ResponseEntity<Map<String, Object>> verificarRutaBloqueada(
            @RequestParam("x1") int x1,
            @RequestParam("y1") int y1,
            @RequestParam("x2") int x2,
            @RequestParam("y2") int y2) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean estaBloqueada = bloqueoService.esRutaBloqueada(x1, y1, x2, y2);
            
            response.put("success", true);
            response.put("bloqueada", estaBloqueada);
            response.put("desde", x1 + "," + y1);
            response.put("hasta", x2 + "," + y2);
            
            if (estaBloqueada) {
                response.put("message", "La ruta entre (" + x1 + "," + y1 + ") y (" + x2 + "," + y2 + ") está bloqueada");
            } else {
                response.put("message", "La ruta entre (" + x1 + "," + y1 + ") y (" + x2 + "," + y2 + ") está libre");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Endpoint para actualizar estado de todos los bloqueos
     */
    @PostMapping("/actualizar-estado")
    public ResponseEntity<Map<String, Object>> actualizarEstadoBloqueos() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            bloqueoService.actualizarEstadoBloqueos();
            
            response.put("success", true);
            response.put("message", "Se ha actualizado el estado de todos los bloqueos");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
}