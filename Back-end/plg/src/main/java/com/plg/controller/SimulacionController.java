package com.plg.controller;

import com.plg.utils.Simulacion;
import com.plg.dto.IndividuoDto;



import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@RequestMapping("/api/simulacion")
@CrossOrigin(origins = "*") // O usa "http://localhost:5173" para mayor seguridad

public class SimulacionController {

    @GetMapping("/mejor")
    public IndividuoDto obtenerMejorIndividuo() {

        IndividuoDto mejorIndividuoDto = null;
        try {
            Simulacion.iniciar.release();
            mejorIndividuoDto = Simulacion.gaResultQueue.poll(5, java.util .concurrent.TimeUnit.SECONDS);
            Simulacion.continuar.release(); // Liberar el semáforo para continuar la simulación 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return mejorIndividuoDto;
    }
}
