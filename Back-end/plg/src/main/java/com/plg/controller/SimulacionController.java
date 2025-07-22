package com.plg.controller;

import com.plg.utils.Simulacion;
import com.plg.utils.TipoDeSimulacion;
import com.plg.utils.AlgoritmoGenetico;
import com.plg.utils.Parametros;
import com.plg.config.DataLoader;
import com.plg.dto.IndividuoDto;
import com.plg.dto.request.SimulacionRequest;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import com.plg.entity.Bloqueo;
import com.plg.entity.Mapa;
import com.plg.entity.Pedido;
import com.plg.utils.simulacion.UtilesSimulacion;
import java.util.LinkedHashSet;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/simulacion")
@CrossOrigin(origins = "*") // O usa "http://localhost:5173" para mayor seguridad

public class SimulacionController {

    // Variable de control para verificar si la simulación ha sido iniciada
    private static boolean simulacionIniciada = false;

    // Variables para almacenar archivos cargados
    private static String archivoVentas = null;
    private static String archivoBloqueos = null;
    private static String archivoCamiones = null;
    private static String archivoMantenimiento = null;

    @GetMapping("/mejor")
    public IndividuoDto obtenerMejorIndividuoPorFecha(@RequestParam String fecha) {
        System.out.println("==========INICIO==========");
        System.out.println("🌐 ENDPOINT LLAMADO: /api/simulacion/mejor (por fecha)"); // Validar que la fecha no sea
                                                                                      // nula o vacía
        if (fecha == null || fecha.isEmpty()) {
            System.out.println("❌ Error: Fecha no proporcionada en la solicitud");
            return null;
        }

        LocalDateTime fechaDateTime;
        try {
            // Parsear la fecha del parámetro
            fechaDateTime = LocalDateTime.parse(fecha);
        } catch (Exception e) {
            System.out.println("❌ Error: Formato de fecha inválido: " + fecha);
            return null;
        }
        System.out.println("==========Fecha de inicio del intervalo: " + fechaDateTime);
        System.out.println(
                "==========Fecha de fin del intervalo: " + fechaDateTime.plusMinutes(Parametros.intervaloTiempo));
        if (!simulacionIniciada) {
            // Brindamos una advertencia e iniciamos la simulación
            System.out.println("⚠️ Advertencia: La simulación no ha sido iniciada. Iniciando simulación...");
            Simulacion.configurarSimulacionSemanal(fechaDateTime);
            simulacionIniciada = true; // Marcar que la simulación ha sido iniciada
            Parametros.fecha_inicio_simulacion = fechaDateTime;
        } else {
            System.out.println("✅ Continuando con la fecha: " + fechaDateTime);
        }

        // Calcular el intervalo de tiempo en minutos entre la fecha inicial y la fecha
        // actual
        Parametros.diferenciaTiempoMinRequest = (int) ChronoUnit.MINUTES.between(Parametros.fecha_inicial,
                fechaDateTime);
        Parametros.actualizarParametrosGlobales(fechaDateTime);
        Simulacion.actualizarEstadoGlobal(fechaDateTime);
        System.out.println("🧩 Pedidos a enviar unidos para la fecha: " +
                Simulacion.pedidosEnviar.size());
        System.out.println("🧬 Ejecutando algoritmo genético para la fecha: " + fechaDateTime);

        // Algoritmo Genético
        AlgoritmoGenetico algoritmoGenetico = new AlgoritmoGenetico(Mapa.getInstance());
        algoritmoGenetico.ejecutarAlgoritmo();
        IndividuoDto mejorIndividuoDto = new IndividuoDto(
                algoritmoGenetico.getMejorIndividuo(),
                Simulacion.pedidosEnviar,
                Simulacion.bloqueosActivos,
                fechaDateTime);
        // Esta es la fecha que se envia al front
        mejorIndividuoDto.setFechaHoraInicioIntervalo(fechaDateTime);
        mejorIndividuoDto.setFechaHoraFinIntervalo(fechaDateTime.plusMinutes(Parametros.intervaloTiempo));
        for (Bloqueo bloqueo : Simulacion.bloqueosActivos) {
            bloqueo.desactivarBloqueo();
        }
        //! Aca se podria agregar averias en la ruta del mejor individuo
        if(Parametros.tipoDeSimulacion == TipoDeSimulacion.SEMANAL){
            //! Aca se podria agregar averias en la ruta del mejor individuo
            
        }
        System.out.println("✅ Mejor individuo generado y retornado para la fecha: " + fechaDateTime);
        System.out.println("____________FIN____________");
        return mejorIndividuoDto;
    }

    @GetMapping("/iniciar")
    public ResponseEntity<String> iniciarSimulacion(@RequestParam String fecha) {
        System.out.println("🌐 ENDPOINT LLAMADO: /api/simulacion/iniciar (GET)");
        System.out.println("📅 Fecha recibida: " + fecha);
        try {
            // Validar que la fecha no sea nula
            if (fecha == null) {
                System.out.println("❌ Error: Fecha de inicio es nula");
                return ResponseEntity.badRequest().body("Error: La fecha de inicio no puede ser nula");
            }
            LocalDateTime fechaDateTime = LocalDateTime.parse(fecha);
            Parametros.fecha_inicio_simulacion = fechaDateTime;
            Simulacion.configurarSimulacionSemanal(fechaDateTime);
            simulacionIniciada = true; // Marcar que la simulación ha sido iniciada
            String mensaje = "Simulación iniciada correctamente con fecha: " + fecha;
            System.out.println("✅ ENDPOINT RESPUESTA: " + mensaje);
            return ResponseEntity.ok(mensaje);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("fatal inicializar");
        }
    }

    @PostMapping("/iniciar")
    public ResponseEntity<String> iniciarSimulacionPost(@RequestBody SimulacionRequest request) {
        System.out.println("🌐 ENDPOINT LLAMADO: /api/simulacion/iniciar (POST)");
        System.out.println("📅 Fecha recibida: " + request.getFechaInicio());
        try {
            // Validar que la fecha no sea nula
            if (request.getFechaInicio() == null) {
                System.out.println("❌ Error: Fecha de inicio es nula");
                return ResponseEntity.badRequest().body("Error: La fecha de inicio no puede ser nula");
            }
            LocalDateTime fechaDateTime = request.getFechaInicio();
            Parametros.fecha_inicio_simulacion = fechaDateTime;
            Simulacion.configurarSimulacionSemanal(fechaDateTime);
            simulacionIniciada = true; // Marcar que la simulación ha sido iniciada
            String mensaje = "SIMULACION INICIADA: " + request.getFechaInicio();
            System.out.println("✅ ENDPOINT RESPUESTA: " + mensaje);
            return ResponseEntity.ok(mensaje);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("fatal inicializar");
        }
    }

    // ========== ENDPOINTS PARA CARGA DE ARCHIVOS ==========

    @DeleteMapping("/limpiar-archivos")
    public ResponseEntity<Map<String, Object>> limpiarArchivos() {
        System.out.println("🌐 ENDPOINT LLAMADO: /api/simulacion/limpiar-archivos");
        try {
            archivoVentas = null;
            archivoBloqueos = null;
            archivoCamiones = null;
            archivoMantenimiento = null;

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Archivos limpiados exitosamente");

            System.out.println("✅ Archivos limpiados exitosamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al limpiar archivos: " + e.getMessage());

            System.out.println("❌ Error al limpiar archivos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping(value = "/cargar-ventas", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> cargarArchivoVentas(@RequestParam("archivo") MultipartFile archivo) {
        System.out.println("🌐 ENDPOINT LLAMADO: /api/simulacion/cargar-ventas");
        System.out.println("📁 Archivo recibido: " + archivo.getOriginalFilename());

        try {
            String contenido = new String(archivo.getBytes(), StandardCharsets.UTF_8);
            archivoVentas = contenido;

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Archivo de ventas cargado exitosamente");

            System.out.println("✅ Archivo de ventas cargado exitosamente");
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al cargar archivo de ventas: " + e.getMessage());

            System.out.println("❌ Error al cargar archivo de ventas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping(value = "/cargar-bloqueos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> cargarArchivoBloqueos(@RequestParam("archivo") MultipartFile archivo) {
        System.out.println("🌐 ENDPOINT LLAMADO: /api/simulacion/cargar-bloqueos");
        System.out.println("📁 Archivo recibido: " + archivo.getOriginalFilename());

        try {
            String contenido = new String(archivo.getBytes(), StandardCharsets.UTF_8);
            archivoBloqueos = contenido;

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Archivo de bloqueos cargado exitosamente");

            System.out.println("✅ Archivo de bloqueos cargado exitosamente");
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al cargar archivo de bloqueos: " + e.getMessage());

            System.out.println("❌ Error al cargar archivo de bloqueos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping(value = "/cargar-camiones", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> cargarArchivoCamiones(@RequestParam("archivo") MultipartFile archivo) {
        System.out.println("🌐 ENDPOINT LLAMADO: /api/simulacion/cargar-camiones");
        System.out.println("📁 Archivo recibido: " + archivo.getOriginalFilename());

        try {
            String contenido = new String(archivo.getBytes(), StandardCharsets.UTF_8);
            archivoCamiones = contenido;

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Archivo de camiones cargado exitosamente");

            System.out.println("✅ Archivo de camiones cargado exitosamente");
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al cargar archivo de camiones: " + e.getMessage());

            System.out.println("❌ Error al cargar archivo de camiones: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping(value = "/cargar-mantenimiento", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> cargarArchivoMantenimiento(
            @RequestParam("archivo") MultipartFile archivo) {
        System.out.println("🌐 ENDPOINT LLAMADO: /api/simulacion/cargar-mantenimiento");
        System.out.println("📁 Archivo recibido: " + archivo.getOriginalFilename());

        try {
            String contenido = new String(archivo.getBytes(), StandardCharsets.UTF_8);
            archivoMantenimiento = contenido;

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Archivo de mantenimiento cargado exitosamente");

            System.out.println("✅ Archivo de mantenimiento cargado exitosamente");
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al cargar archivo de mantenimiento: " + e.getMessage());

            System.out.println("❌ Error al cargar archivo de mantenimiento: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/estado-archivos")
    public ResponseEntity<Map<String, Boolean>> obtenerEstadoArchivos() {
        System.out.println("🌐 ENDPOINT LLAMADO: /api/simulacion/estado-archivos");

        Map<String, Boolean> estado = new HashMap<>();
        estado.put("ventas", archivoVentas != null);
        estado.put("bloqueos", archivoBloqueos != null);
        estado.put("camiones", archivoCamiones != null);
        estado.put("mantenimiento", archivoMantenimiento != null);

        System.out.println("✅ Estado de archivos: " + estado);
        return ResponseEntity.ok(estado);
    }
}
