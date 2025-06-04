package com.plg.controller;

import com.plg.utils.Simulacion;
import com.plg.dto.IndividuoDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/simulacion")
@CrossOrigin(origins = "*") // O usa "http://localhost:5173" para mayor seguridad

public class SimulacionController {

    @GetMapping("/mejor")
    public ResponseEntity<?> obtenerMejorIndividuo() {
        try {
            // Señalar que queremos obtener datos
            Simulacion.iniciar.release();
            
            // Esperar máximo 10 segundos por datos (aumentado de 5)
            IndividuoDto mejorIndividuoDto = Simulacion.gaResultQueue.poll(10, java.util.concurrent.TimeUnit.SECONDS);
            
            // Siempre liberar el semáforo para evitar deadlocks
            Simulacion.continuar.release();
            
            if (mejorIndividuoDto == null) {
                System.out.println("⏳ API: Timeout esperando datos del algoritmo genético");
                // En lugar de NO_CONTENT, devolver JSON con mensaje de espera
                return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .body("{\"status\":\"waiting\",\"message\":\"Simulación en progreso, esperando datos...\",\"timestamp\":" + System.currentTimeMillis() + "}");
            }
            
            System.out.println("✅ API: Datos enviados al frontend");
            return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body(mejorIndividuoDto);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("❌ API: Error de interrupción - " + e.getMessage());
            
            // Asegurar que liberamos el semáforo incluso en caso de error
            try {
                Simulacion.continuar.release();
            } catch (Exception ex) {
                System.err.println("❌ Error liberando semáforo: " + ex.getMessage());
            }
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("Content-Type", "application/json")
                .body("{\"status\":\"error\",\"message\":\"Error en la simulación: " + e.getMessage().replace("\"", "'") + "\"}");
        } catch (Exception e) {
            System.err.println("❌ API: Error inesperado - " + e.getMessage());
            
            // Asegurar que liberamos el semáforo incluso en caso de error
            try {
                Simulacion.continuar.release();
            } catch (Exception ex) {
                System.err.println("❌ Error liberando semáforo: " + ex.getMessage());
            }
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("Content-Type", "application/json")
                .body("{\"status\":\"error\",\"message\":\"Error inesperado: " + e.getMessage().replace("\"", "'") + "\"}");
        }
    }

    @GetMapping("/estado-rapido")
    public ResponseEntity<?> obtenerEstadoRapido() {
        try {
            // Endpoint ligero que no requiere algoritmo genético
            // Solo devuelve el estado actual basado en las rutas existentes
            
            return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body("{\"status\":\"interpolating\",\"message\":\"Usando interpolación local\",\"timestamp\":" + System.currentTimeMillis() + "}");
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("Content-Type", "application/json")
                .body("{\"status\":\"error\",\"message\":\"Error obteniendo estado: " + e.getMessage().replace("\"", "'") + "\"}");
        }
    }
}
