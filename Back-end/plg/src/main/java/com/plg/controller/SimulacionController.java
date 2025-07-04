package com.plg.controller;

import com.plg.utils.Simulacion;
import com.plg.dto.IndividuoDto;
import com.plg.dto.request.SimulacionRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;


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
        // Limpiar completamente el historial, no solo reiniciar la reproducción
        com.plg.utils.simulacion.GestorHistorialSimulacion.limpiarHistorialCompleto();
        System.out.println("✅ ENDPOINT RESPUESTA: Simulación reiniciada y historial limpiado");
        return "Simulación reiniciada desde el inicio - historial limpiado";
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

    @PostMapping("/iniciar")
    public ResponseEntity<String> iniciarSimulacion(@RequestBody SimulacionRequest request) {
        System.out.println("🌐 ENDPOINT LLAMADO: /api/simulacion/iniciar");
        System.out.println("📅 Fecha recibida: " + request.getFechaInicio());
        
        try {
            // Validar que la fecha no sea nula
            if (request.getFechaInicio() == null) {
                System.out.println("❌ Error: Fecha de inicio es nula");
                return ResponseEntity.badRequest().body("Error: La fecha de inicio no puede ser nula");
            }
            
            // Verificar si ya hay una simulación en proceso
            if (Simulacion.obtenerInfoSimulacion().enProceso) {
                System.out.println("⚠️ Ya hay una simulación en proceso");
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Error: Ya hay una simulación en proceso. Debe esperar a que termine o reiniciarla.");
            }
            
            System.out.println("🔧 Configurando simulación con fecha: " + request.getFechaInicio());
            
            // Limpiar historial anterior antes de iniciar nueva simulación
            com.plg.utils.simulacion.GestorHistorialSimulacion.limpiarHistorialCompleto();
            
            // Configurar la simulación con la fecha enviada desde el frontend
            Simulacion.configurarSimulacion(request.getFechaInicio());
            
            // Ejecutar la simulación en un hilo separado para no bloquear la respuesta HTTP
            Thread simulacionThread = new Thread(() -> {
                try {
                    System.out.println("🚀 Iniciando simulación en hilo separado...");
                    Simulacion.ejecutarSimulacion();
                    System.out.println("✅ Simulación completada exitosamente");
                } catch (Exception e) {
                    System.err.println("💥 Error durante la ejecución de la simulación: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            
            simulacionThread.setName("SimulacionThread-" + request.getFechaInicio());
            simulacionThread.start();
            
            String mensaje = "Simulación iniciada correctamente con fecha: " + request.getFechaInicio();
            System.out.println("✅ ENDPOINT RESPUESTA: " + mensaje);
            
            return ResponseEntity.ok(mensaje);
            
        } catch (Exception e) {
            String errorMsg = "Error al iniciar simulación: " + e.getMessage();
            System.err.println("❌ " + errorMsg);
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMsg);
        }
    }
}
