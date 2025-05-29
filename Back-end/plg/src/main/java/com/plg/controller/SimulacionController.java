package com.plg.controller;

import com.plg.utils.Simulacion;
import com.plg.utils.Individuo;

import java.util.concurrent.TimeUnit;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/simulacion")
public class SimulacionController {

    private static final long TIMEOUT_SECONDS = 30;

    @GetMapping("/mejor")
    public Individuo obtenerMejorIndividuo() {
        Individuo mejorIndividuo = null;
        try {
            Simulacion.gaTriggerQueue.offer(new Object(), 5, TimeUnit.SECONDS);
            mejorIndividuo = Simulacion.gaResultQueue.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            // Optionally log or handle the exception as needed
        }
        return mejorIndividuo;
    }
}   
