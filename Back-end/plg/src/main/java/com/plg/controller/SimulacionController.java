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
            Simulacion.iniciar.release();
            IndividuoDto mejorIndividuoDto = Simulacion.gaResultQueue.poll(5, java.util.concurrent.TimeUnit.SECONDS);
            Simulacion.continuar.release(); // Liberar el semáforo para continuar la simulación 
            
            if (mejorIndividuoDto == null) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body("No hay datos disponibles en este momento");
            }
            
            return ResponseEntity.ok(mejorIndividuoDto);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error en la simulación: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error inesperado: " + e.getMessage());
        }
    }
}
