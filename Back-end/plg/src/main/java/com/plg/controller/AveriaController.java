package com.plg.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.plg.dto.request.AveriaConEstadoRequest;
import com.plg.dto.request.AveriaRequest;
import com.plg.entity.Averia;
import com.plg.entity.EstadoCamion;
import com.plg.service.AveriaService;
import com.plg.service.CamionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Controlador REST para averías.
 */
@RestController
@RequestMapping("/api/averias")
@CrossOrigin(origins = "*")
public class AveriaController {

    private final AveriaService averiaService;
    private final CamionService camionService;

    public AveriaController(AveriaService averiaService, CamionService camionService) {
        this.averiaService = averiaService;
        this.camionService = camionService;
    }

    /**
     * Lista todas las averías.
     */
    @GetMapping
    public ResponseEntity<?> listar() {
        try {
            return ResponseEntity.ok(averiaService.listar());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener averías: " + e.getMessage());
        }
    }

    /**
     * Lista todas las averías activas.
     */
    @GetMapping("/activas")
    public ResponseEntity<?> listarActivas() {
        try {
            return ResponseEntity.ok(averiaService.listarActivas());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener averías activas: " + e.getMessage());
        }
    }

    /**
     * Lista averías por camión.
     */
    @GetMapping("/camion")
    public ResponseEntity<?> listarPorCamion(@RequestParam String codigoCamion) {
        try {
            // Para este endpoint necesitaríamos obtener el camión primero
            // Por simplicidad, retornamos las averías filtradas por código
            return ResponseEntity.ok(averiaService.listar()
                    .stream()
                    .filter(a -> a.getCamion() != null
                            && a.getCamion().getCodigo().equals(codigoCamion))
                    .toList());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al filtrar averías por camión: " + e.getMessage());
        }
    }

    /**
     * Agrega una nueva avería solo con código de camión y tipo de incidente.
     */
    @PostMapping
    public ResponseEntity<?> agregar(@RequestBody AveriaRequest request) {
        try {
            if (request.getCodigoCamion() == null || request.getCodigoCamion().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("El código del camión es obligatorio");
            }
            if (request.getTipoIncidente() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("El tipo de incidente es obligatorio");
            }
            request.setEstado(true);
            // Solo se pasan los campos requeridos, los demás se ignoran
            AveriaRequest minimalRequest = new AveriaRequest(request);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(averiaService.agregar(minimalRequest));

        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al crear avería: " + e.getMessage());
        }

    }

    /**
     *
     * Lista los códigos de camiones actualmente averiados (solo códigos, sin
     * duplicados).
     *
     *
     *
     * @return Lista de códigos de camiones con avería activa.
     */
    @GetMapping("/camiones-averiados")

    public ResponseEntity<?> listarCodigosCamionesAveriados() {

        try {

            return ResponseEntity.ok(averiaService.listarCodigosCamionesAveriados());
        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener camiones averiados: " + e.getMessage());

        }
    }

    /**
     * Lista averías por camión y tipo de incidente.
     */
    @GetMapping("/camion-tipo")
    public ResponseEntity<?> listarPorCamionYTipo(@RequestParam String codigoCamion,
            @RequestParam String tipoIncidente) {
        try {
            return ResponseEntity.ok(
                    averiaService.listarPorCamionYTipo(codigoCamion, tipoIncidente));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al filtrar averías por camión y tipo: " + e.getMessage());
        }
    }

    /**
     * Agrega una nueva avería y cambia el estado del camión a
     * EN_MANTENIMIENTO_POR_AVERIA.
     */
    @PostMapping("/averiar-camion")
    public ResponseEntity<?> averiarCamion(@RequestBody AveriaRequest request) {
        try {
            Averia averia = averiaService.agregar(request);
            camionService.cambiarEstado(request.getCodigoCamion(), EstadoCamion.EN_MANTENIMIENTO_POR_AVERIA);
            return ResponseEntity.status(HttpStatus.CREATED).body(averia);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al crear avería y cambiar estado: " + e.getMessage());
        }
    }

    /**
     * Actualiza los estados de camiones con averías según las fechas de
     * disponibilidad. Este endpoint permite forzar la actualización de estados
     * de camiones averiados.
     */
    @PostMapping("/actualizar-estados")
    public ResponseEntity<?> actualizarEstadosCamiones() {
        try {
            java.time.LocalDateTime fechaActual = java.time.LocalDateTime.now();
            averiaService.actualizarEstadosCamionesAveriados(fechaActual);
            return ResponseEntity.ok("Estados de camiones averiados actualizados correctamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar estados de camiones averiados: " + e.getMessage());
        }
    }

    /**
     * Endpoint temporal para debugging - captura el JSON raw que llega
     */
    @PostMapping("/debug-averia-raw")
    public ResponseEntity<?> debugAveriaRaw(@RequestBody String jsonRaw) {
        try {
            System.out.println("🔍 DEBUG BACKEND: JSON Raw recibido:");
            System.out.println(jsonRaw);

            // Intentar parsear el JSON para ver si hay errores
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(jsonRaw);

            System.out.println("🔍 DEBUG BACKEND: JSON parseado correctamente");
            System.out.println("   - Tiene coordenada: " + jsonNode.has("coordenada"));
            if (jsonNode.has("coordenada")) {
                JsonNode coordenadaNode = jsonNode.get("coordenada");
                System.out.println("   - Coordenada es null: " + coordenadaNode.isNull());
                if (!coordenadaNode.isNull()) {
                    System.out.println("   - Coordenada tiene fila: " + coordenadaNode.has("fila"));
                    System.out.println("   - Coordenada tiene columna: " + coordenadaNode.has("columna"));
                    if (coordenadaNode.has("fila")) {
                        System.out.println("   - Valor fila: " + coordenadaNode.get("fila"));
                    }
                    if (coordenadaNode.has("columna")) {
                        System.out.println("   - Valor columna: " + coordenadaNode.get("columna"));
                    }
                }
            }

            return ResponseEntity.ok("JSON recibido y analizado correctamente");

        } catch (Exception e) {
            System.err.println("❌ DEBUG BACKEND: Error al analizar JSON: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al analizar JSON: " + e.getMessage());
        }
    }

    /**
     * Agrega una nueva avería con estado completo de la simulación.
     * Este endpoint recibe tanto los datos de la avería como el estado
     * completo de la simulación en el momento de la avería.
     */
    @PostMapping("/averiar-camion-con-estado")
    public ResponseEntity<?> averiarCamionConEstado(@RequestBody AveriaConEstadoRequest request) {
        try {
    
            // Validaciones básicas
            if (request.getCodigoCamion() == null || request.getCodigoCamion().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("El código del camión es obligatorio");
            }
            if (request.getTipoIncidente() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("El tipo de incidente es obligatorio");
            }
            if (request.getEstadoSimulacion() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("El estado de la simulación es obligatorio");
            }
            AveriaRequest averiaRequest = request.toAveriaRequest();

            Averia averia = averiaService.agregar(averiaRequest);


            return ResponseEntity.status(HttpStatus.CREATED).body(new AveriaConEstadoResponse(
                    averia,
                    "Avería creada exitosamente con estado completo de la simulación",
                    request.getEstadoSimulacion().getTimestamp(),
                    request.getEstadoSimulacion().getHoraSimulacion()));

        } catch (Exception e) {
            System.err.println("❌ AVERÍA BACKEND: Error al crear avería con estado completo: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al crear avería con estado completo: " + e.getMessage());
        }
    }

    public static class AveriaConEstadoResponse {
        private Averia averia;
        private String mensaje;
        private String timestampEstado;
        private String horaSimulacion;

        public AveriaConEstadoResponse(Averia averia, String mensaje, String timestampEstado, String horaSimulacion) {
            this.averia = averia;
            this.mensaje = mensaje;
            this.timestampEstado = timestampEstado;
            this.horaSimulacion = horaSimulacion;
        }

        // Getters
        public Averia getAveria() {
            return averia;
        }

        public String getMensaje() {
            return mensaje;
        }

        public String getTimestampEstado() {
            return timestampEstado;
        }

        public String getHoraSimulacion() {
            return horaSimulacion;
        }
    }
}
