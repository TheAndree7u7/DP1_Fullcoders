package com.plg.controller;

import com.plg.utils.Simulacion;
import com.plg.dto.IndividuoDto;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/simulacion")
@CrossOrigin(origins = "*") // O usa "http://localhost:5173" para mayor seguridad

public class SimulacionController {
    
    private static final Logger logger = LoggerFactory.getLogger(SimulacionController.class);

    @GetMapping(value = "/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> obtenerEstado() {
        logger.info("🔍 Solicitud de estado recibida");
        
        Map<String, Object> estado = new HashMap<>();
        estado.put("status", "running");
        estado.put("timestamp", System.currentTimeMillis());
        estado.put("simulacionConfigurada", Simulacion.mejorIndividuo != null);
        estado.put("pedidosPorAtender", Simulacion.pedidosPorAtender.size());
        estado.put("pedidosPlanificados", Simulacion.pedidosPlanificados.size());
        estado.put("pedidosEntregados", Simulacion.pedidosEntregados.size());
        
        logger.info("✅ Estado del servidor: {}", estado);
        
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(estado);
    }

    @GetMapping(value = "/mejor", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> obtenerMejorIndividuo() {
        logger.info("🔍 Solicitud recibida para obtener mejor individuo");
        
        IndividuoDto mejorIndividuoDto = null;
        try {
            logger.info("⏳ Esperando señal de inicio...");
            Simulacion.iniciar.release();
            
            logger.info("🔄 Esperando resultado del algoritmo genético...");
            mejorIndividuoDto = Simulacion.gaResultQueue.poll(30, java.util.concurrent.TimeUnit.SECONDS);
            
            logger.info("✅ Señal de continuar liberada");
            Simulacion.continuar.release(); 
            
            if (mejorIndividuoDto == null) {
                logger.warn("⚠️ No se obtuvo resultado del algoritmo genético (timeout)");
                return ResponseEntity.status(503)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\": \"No hay datos disponibles en este momento\", \"timestamp\": " + System.currentTimeMillis() + "}");
            }
            
            logger.info("✅ Mejor individuo obtenido exitosamente");
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(mejorIndividuoDto);
        
        } catch (InterruptedException e) {
            logger.error("❌ Error de interrupción: {}", e.getMessage());
            Thread.currentThread().interrupt();
            return ResponseEntity.status(500)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"error\": \"Error interno del servidor\", \"timestamp\": " + System.currentTimeMillis() + "}");
        } catch (Exception e) {
            logger.error("❌ Error inesperado: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"error\": \"Error interno del servidor\", \"timestamp\": " + System.currentTimeMillis() + "}");
        }
    }
}
