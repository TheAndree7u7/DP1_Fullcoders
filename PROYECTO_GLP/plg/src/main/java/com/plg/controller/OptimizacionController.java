package com.plg.controller;

import java.util.Map;
import java.util.List;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RequestMapping("/api/optimizacion")
public class OptimizacionController {

    private static final Logger logger = LoggerFactory.getLogger(OptimizacionController.class);
    
    @Autowired
    private AgrupamientoAPService apService;
    
    @Autowired
    private AlgoritmoGeneticoService geneticoService;
    
    /**
     * Ejecuta el algoritmo de Affinity Propagation para agrupar pedidos
     * 
     * @param params Parámetros del algoritmo: alpha, beta, damping
     * @return Resultado con grupos generados
     */
    @PostMapping("/ap")
    public ResponseEntity<?> ejecutarAP(@RequestBody Map<String, Object> params) {
        logger.info("Recibida solicitud para ejecutar Affinity Propagation con parámetros: {}", params);
        
        try {
            // Validar y convertir parámetros
            Double alpha = params.containsKey("alpha") ? 
                Double.valueOf(params.get("alpha").toString()) : 1.0;
            Double beta = params.containsKey("beta") ? 
                Double.valueOf(params.get("beta").toString()) : 0.5;
            Double damping = params.containsKey("damping") ? 
                Double.valueOf(params.get("damping").toString()) : 0.9;
            Integer maxIter = params.containsKey("maxIter") ? 
                Integer.valueOf(params.get("maxIter").toString()) : 100;
                
            // Validaciones adicionales
            if (damping < 0.5 || damping > 1.0) {
                logger.error("Parámetro damping inválido: {}, debe estar entre 0.5 y 1.0", damping);
                return ResponseEntity.badRequest().body(
                    Map.of("error", "El parámetro damping debe estar entre 0.5 y 1.0")
                );
            }
            
            if (maxIter <= 0 || maxIter > 1000) {
                logger.error("Parámetro maxIter inválido: {}, debe estar entre 1 y 1000", maxIter);
                return ResponseEntity.badRequest().body(
                    Map.of("error", "El parámetro maxIter debe estar entre 1 y 1000")
                );
            }
            
            // Reconstruir mapa con parámetros validados
            Map<String, Object> validatedParams = Map.of(
                "alpha", alpha,
                "beta", beta,
                "damping", damping,
                "maxIter", maxIter
            );
            
            AgrupamientoAPResultadoDTO resultado = apService.generarGrupos(validatedParams);
            return ResponseEntity.ok(resultado);
        } catch (NumberFormatException e) {
            logger.error("Error de formato en parámetros para AP", e);
            return ResponseEntity.badRequest().body(
                Map.of("error", "Error en formato de parámetros: " + e.getMessage())
            );
        } catch (Exception e) {
            logger.error("Error al ejecutar Affinity Propagation", e);
            return ResponseEntity.internalServerError().body(
                Map.of("error", "Error en el servidor: " + e.getMessage())
            );
        }
    }
    
    /**
     * Ejecuta el algoritmo Genético para optimizar rutas dentro de grupos
     * 
     * @param params Parámetros del algoritmo
     * @return Resultado con rutas optimizadas
     */
    @PostMapping("/genetico")
    public ResponseEntity<?> ejecutarGenetico(@RequestBody Map<String, Object> params) {
        logger.info("Recibida solicitud para ejecutar Algoritmo Genético con parámetros: {}", params);
        
        try {
            // Validar parámetros requeridos
            if (!params.containsKey("clusters") || !(params.get("clusters") instanceof List)) {
                logger.error("Parámetro 'clusters' no encontrado o inválido");
                return ResponseEntity.badRequest().body(
                    Map.of("error", "Se requiere el parámetro 'clusters' como una lista")
                );
            }
            
            // Validar y convertir parámetros opcionales
            Map<String, Object> validatedParams = new HashMap<>();
            validatedParams.put("clusters", params.get("clusters"));
            
            // Parámetros opcionales con valores por defecto
            validatedParams.put("poblacionInicial", 
                params.containsKey("poblacionInicial") ? 
                Integer.valueOf(params.get("poblacionInicial").toString()) : 50);
            validatedParams.put("maxGeneraciones", 
                params.containsKey("maxGeneraciones") ? 
                Integer.valueOf(params.get("maxGeneraciones").toString()) : 100);
            validatedParams.put("tasaMutacion", 
                params.containsKey("tasaMutacion") ? 
                Double.valueOf(params.get("tasaMutacion").toString()) : 0.1);
            validatedParams.put("tasaCruce", 
                params.containsKey("tasaCruce") ? 
                Double.valueOf(params.get("tasaCruce").toString()) : 0.8);
            
            // Obtener número de rutas basado en los clusters
            List<?> clusters = (List<?>) params.get("clusters");
            validatedParams.put("numeroRutas", clusters.size());
            
            logger.info("Parámetros validados para algoritmo genético: {}", validatedParams);
            
            AlgoritmoGeneticoResultadoDTO resultado = geneticoService.generarRutas(validatedParams);
            return ResponseEntity.ok(resultado);
        } catch (NumberFormatException e) {
            logger.error("Error de formato en parámetros para algoritmo genético", e);
            return ResponseEntity.badRequest().body(
                Map.of("error", "Error en formato de parámetros: " + e.getMessage())
            );
        } catch (Exception e) {
            logger.error("Error al ejecutar algoritmo genético", e);
            return ResponseEntity.internalServerError().body(
                Map.of("error", "Error en el servidor: " + e.getMessage())
            );
        }
    }
    
    /**
     * Ejecuta el proceso completo de optimización: AP + GA + Generación de rutas
     * 
     * @param params Parámetros combinados
     * @return Resultado final
     */
    @PostMapping("/completo")
    public ResponseEntity<Map<String, Object>> optimizacionCompleta(@RequestBody Map<String, Object> params) {
        logger.info("Recibida solicitud para ejecutar optimización completa con parámetros: {}", params);
        
        // La implementación real sería una coordinación de los tres pasos
        // Pero por simplicidad, solo redirigimos al método individual más completo
        try {
            AlgoritmoGeneticoResultadoDTO resultado = geneticoService.generarRutas(params);
            return ResponseEntity.ok(Map.of(
                "resultado", "success",
                "rutas", resultado.getRutas(),
                "pedidosAsignados", resultado.getPedidosAsignados(),
                "mensaje", "Optimización completa finalizada correctamente"
            ));
        } catch (Exception e) {
            logger.error("Error en optimización completa", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
