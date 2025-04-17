package com.plg.controller;

import com.plg.config.MapaConfig;
import com.plg.service.MapaReticularService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mapa")
public class MapaReticularController {

    @Autowired
    private MapaReticularService mapaReticularService;
    
    @Autowired
    private MapaConfig mapaConfig;
    
    /**
     * Endpoint para obtener las propiedades del mapa
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> obtenerConfiguracionMapa() {
        Map<String, Object> config = new HashMap<>();
        
        config.put("largo", mapaConfig.getLargo());
        config.put("ancho", mapaConfig.getAncho());
        config.put("origenX", mapaConfig.getOrigenX());
        config.put("origenY", mapaConfig.getOrigenY());
        config.put("distanciaNodos", mapaConfig.getDistanciaNodos()); 
        
        return ResponseEntity.ok(config);
    }
    
    /**
     * Endpoint para calcular una ruta óptima entre dos puntos
     * considerando los bloqueos activos
     */
    @GetMapping("/ruta")
    public ResponseEntity<Map<String, Object>> calcularRutaOptima(
            @RequestParam("xInicio") double xInicio,
            @RequestParam("yInicio") double yInicio,
            @RequestParam("xFin") double xFin,
            @RequestParam("yFin") double yFin,
            @RequestParam(value = "velocidad", defaultValue = "50") double velocidad) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Verificar si las coordenadas están dentro del mapa
            if (!mapaConfig.estaEnMapa(xInicio, yInicio) || !mapaConfig.estaEnMapa(xFin, yFin)) {
                response.put("success", false);
                response.put("error", "Coordenadas fuera de los límites del mapa");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Calcular la ruta óptima
            List<double[]> ruta = mapaReticularService.calcularRutaOptimaConsiderandoBloqueos(
                xInicio, yInicio, xFin, yFin);
            
            if (ruta.isEmpty()) {
                response.put("success", false);
                response.put("error", "No se pudo encontrar una ruta entre los puntos especificados");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Convertir la ruta a formato más amigable para JSON
            List<Map<String, Double>> rutaFormateada = ruta.stream()
                .map(punto -> {
                    Map<String, Double> coordenada = new HashMap<>();
                    coordenada.put("x",  punto[0]);
                    coordenada.put("y",  punto[1]);
                    return coordenada;
                })
                .collect(java.util.stream.Collectors.toList());
            
            // Calcular la longitud de la ruta en km
            double longitudKm = mapaReticularService.calcularLongitudRuta(ruta);
            
            // Estimar el tiempo de viaje en minutos
            double tiempoMinutos = mapaReticularService.estimarTiempoViajeMinutos(ruta, velocidad);
            
            response.put("success", true);
            response.put("desde", new double[]{xInicio, yInicio});
            response.put("hasta", new double[]{xFin, yFin});
            response.put("ruta", rutaFormateada);
            response.put("nodos", ruta.size());
            response.put("longitudKm", Math.round(longitudKm * 100) / 100.0); // Redondear a 2 decimales
            response.put("tiempoEstimadoMinutos", Math.round(tiempoMinutos * 10) / 10.0); // Redondear a 1 decimal
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Endpoint para verificar si una posición está dentro del mapa
     */
    @GetMapping("/verificar-posicion")
    public ResponseEntity<Map<String, Object>> verificarPosicion(
            @RequestParam("x") int x,
            @RequestParam("y") int y) {
        
        Map<String, Object> response = new HashMap<>();
        boolean dentroMapa = mapaConfig.estaEnMapa(x, y);
        
        response.put("success", true);
        response.put("coordenada", new int[]{x, y});
        response.put("dentroMapa", dentroMapa);
        
        if (dentroMapa) {
            response.put("message", "La coordenada (" + x + "," + y + ") está dentro del mapa");
        } else {
            response.put("message", "La coordenada (" + x + "," + y + ") está fuera del mapa");
            
            // Sugerir la coordenada válida más cercana
            double xValido = Math.max(mapaConfig.getOrigenX(), 
                          Math.min(x, mapaConfig.getOrigenX() + mapaConfig.getLargo()));
            double yValido = Math.max(mapaConfig.getOrigenY(), 
                          Math.min(y, mapaConfig.getOrigenY() + mapaConfig.getAncho()));
            
            response.put("coordenadaValida", new double[]{xValido, yValido});
            response.put("sugerencia", "La coordenada válida más cercana es (" + xValido + "," + yValido + ")");
        }
        
        return ResponseEntity.ok(response);
    }
}