package com.plg.controller;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.plg.dto.IndividuoDto;
import com.plg.entity.Bloqueo;
import com.plg.entity.Mapa;
import com.plg.utils.AlgoritmoGenetico;
import com.plg.utils.Parametros;
import com.plg.utils.Simulacion;



@RestController
@RequestMapping("/api/simulacion")
@CrossOrigin(origins = "*") // O usa "http://localhost:5173" para mayor seguridad

public class SimulacionController {

    // Variable de control para verificar si la simulación ha sido iniciada
    private static boolean simulacionIniciada = false;


    @GetMapping("/mejor")
    public ResponseEntity<IndividuoDto> obtenerMejorIndividuoPorFecha(@RequestParam(required = false) String fecha) {
        System.out.println("🌐 ENDPOINT LLAMADO: /api/simulacion/mejor");
        
        // Si no se proporciona fecha, usar la fecha actual
        LocalDateTime fechaDateTime;
        if (fecha == null || fecha.isEmpty()) {
            System.out.println("⚠️ No se proporcionó fecha, usando fecha actual");
            fechaDateTime = LocalDateTime.now();
        } else {
            try {
                // Parsear la fecha del parámetro
                fechaDateTime = LocalDateTime.parse(fecha);
                System.out.println("✅ Fecha parseada correctamente: " + fechaDateTime);
            } catch (Exception e) {
                System.out.println("❌ Error: Formato de fecha inválido: " + fecha);
                System.out.println("   Formato esperado: YYYY-MM-DDTHH:MM:SS (ejemplo: 2025-01-01T00:45:00)");
                return ResponseEntity.badRequest().body(null);
            }
        }

        try {
            if(!simulacionIniciada){
                // Brindamos una advertencia e iniciamos la simulación
                System.out.println("⚠️ Advertencia: La simulación no ha sido iniciada. Iniciando simulación...");
                Simulacion.configurarSimulacionSemanal(fechaDateTime);
                simulacionIniciada = true; // Marcar que la simulación ha sido iniciada
            } else {
                System.out.println("✅ Simulación ya iniciada, continuando con la fecha: " + fechaDateTime);
            }

            // Calcular el intervalo de tiempo en minutos entre la fecha inicial y la fecha actual
            Parametros.intervaloTiempo = (int) ChronoUnit.MINUTES.between(Parametros.fecha_inicial, fechaDateTime);
            Parametros.actualizarParametrosGlobales(fechaDateTime);
            
            // Obtener pedidos en el rango de dos horas y unir con pedidos planificados
            Simulacion.actualizarEstadoGlobal(fechaDateTime);

            System.out.println("🧩 Pedidos a enviar unidos para la fecha: " + Simulacion.pedidosEnviar.size());
            System.out.println("🧬 Ejecutando algoritmo genético para la fecha: " + fechaDateTime);

            // Algoritmo Genético
            AlgoritmoGenetico algoritmoGenetico = new AlgoritmoGenetico(Mapa.getInstance());
            algoritmoGenetico.ejecutarAlgoritmo();
            
            IndividuoDto mejorIndividuoDto = new IndividuoDto(
                    algoritmoGenetico.getMejorIndividuo(),
                    Simulacion.pedidosEnviar,
                    Simulacion.bloqueosActivos,
                    fechaDateTime);
            
            // Desactivar bloqueos después de procesarlos
            for (Bloqueo bloqueo : Simulacion.bloqueosActivos) {
                bloqueo.desactivarBloqueo();
            }
            
            System.out.println("✅ Mejor individuo generado y retornado para la fecha: " + fechaDateTime);
            System.out.println("📊 Datos retornados: " + mejorIndividuoDto.getCromosoma().size() + " camiones, " 
                + mejorIndividuoDto.getPedidos().size() + " pedidos, " 
                + mejorIndividuoDto.getBloqueos().size() + " bloqueos");
            
            return ResponseEntity.ok(mejorIndividuoDto);
            
        } catch (Exception e) {
            System.out.println("❌ Error interno al procesar la simulación: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }



    @PostMapping("/iniciar")
    public ResponseEntity<String> iniciarSimulacion(@RequestParam String fechaInicio) {
        System.out.println("🌐 ENDPOINT LLAMADO: /api/simulacion/iniciar");
        System.out.println("📅 Fecha recibida: " + fechaInicio);
        try {
            // Validar que la fecha no sea nula
            if (fechaInicio == null) {
                System.out.println("❌ Error: Fecha de inicio es nula");
                return ResponseEntity.badRequest().body("Error: La fecha de inicio no puede ser nula");
            }
            LocalDateTime fechaDateTime = LocalDateTime.parse(fechaInicio);
            Simulacion.configurarSimulacionSemanal(fechaDateTime);
            simulacionIniciada = true; // Marcar que la simulación ha sido iniciada
            String mensaje = "Simulación iniciada correctamente con fecha: " + fechaInicio;
            System.out.println("✅ ENDPOINT RESPUESTA: " + mensaje);
            return ResponseEntity.ok(mensaje);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("fatal inicializar");
        }
    }
}
