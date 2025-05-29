package com.plg.controller;

import com.plg.utils.Simulacion;
import com.plg.utils.Individuo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/simulacion")
public class SimulacionController {

    @GetMapping("/mejor")
    public Individuo obtenerMejorIndividuo() {
        System.out.println("CONTROLLER: Se recibe solicitud GET /api/simulacion/mejor");
        try{
            if(!)
        }
        Individuo mejorIndividuo = Simulacion.mejorIndividuo;
        Simulacion.ejecucionTerminada = true;

        return mejorIndividuo;
    }
}
