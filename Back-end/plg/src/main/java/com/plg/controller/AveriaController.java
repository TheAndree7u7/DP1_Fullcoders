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

import com.plg.dto.request.AveriaRequest;
import com.plg.entity.Averia;
import com.plg.entity.EstadoCamion;
import com.plg.service.AveriaService;
import com.plg.service.CamionService;

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
        public ResponseEntity<?> listarPorCamionYTipo
        (@RequestParam String codigoCamion, @RequestParam String tipoIncidente ) {
        try {
                return ResponseEntity.ok(
                        averiaService.listarPorCamionYTipo(codigoCamion, tipoIncidente)
                );
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
        public ResponseEntity<?> averiarCamion
        (@RequestBody

        AveriaRequest request
            
        
        
        
            ) {
        try {
                Averia averia = averiaService.agregar(request);
                camionService.cambiarEstado(request.getCodigoCamion(), EstadoCamion.EN_MANTENIMIENTO_POR_AVERIA);
                return ResponseEntity.status(HttpStatus.CREATED).body(averia);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Error al crear avería y cambiar estado: " + e.getMessage());
            }
        }
    }
