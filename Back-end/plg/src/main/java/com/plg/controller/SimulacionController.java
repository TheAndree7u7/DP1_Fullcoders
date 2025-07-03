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
        System.out.println("🌐 ENDPOINT LLAMADO: /api/simulacion/mejor");
        
        // Obtener el siguiente paquete en secuencia
        IndividuoDto siguientePaquete = Simulacion.obtenerSiguientePaquete();
        
        if (siguientePaquete == null) {
            System.out.println("⏳ No hay paquetes disponibles, esperando...");
            // Si no hay más paquetes, esperar un poco por si se está generando uno nuevo
            try {
                Thread.sleep(100); // Espera breve
                siguientePaquete = Simulacion.obtenerSiguientePaquete();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            if (siguientePaquete == null) {
                System.out.println("❌ ENDPOINT RESPUESTA: null (sin paquetes disponibles)");
            }
        }
        
        if (siguientePaquete != null) {
            System.out.println("✅ ENDPOINT RESPUESTA: Paquete enviado al frontend");
        }
        
        return siguientePaquete;
    }
    
    @GetMapping("/reiniciar")
    public String reiniciarSimulacion() {
        System.out.println("🌐 ENDPOINT LLAMADO: /api/simulacion/reiniciar");
        Simulacion.reiniciarReproduccion();
        System.out.println("✅ ENDPOINT RESPUESTA: Simulación reiniciada");
        return "Simulación reiniciada desde el inicio";
    }
    
    @GetMapping("/info")
    public Simulacion.SimulacionInfo obtenerInfoSimulacion() {
        System.out.println("🌐 ENDPOINT LLAMADO: /api/simulacion/info");
        Simulacion.SimulacionInfo info = Simulacion.obtenerInfoSimulacion();
        System.out.println("✅ ENDPOINT RESPUESTA: Total=" + info.totalPaquetes + 
                          ", Actual=" + info.paqueteActual + 
                          ", EnProceso=" + info.enProceso);
        return info;
    }
}
