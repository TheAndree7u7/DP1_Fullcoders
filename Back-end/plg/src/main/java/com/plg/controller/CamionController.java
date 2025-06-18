package com.plg.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.plg.dto.request.CamionRequest;
import com.plg.service.CamionService;

/**
 * Controlador REST para camiones.
 */
@RestController
@RequestMapping("/api/camiones")
@CrossOrigin(origins = "*")
public class CamionController {

    private final CamionService camionService;

    public CamionController(CamionService camionService) {
        this.camionService = camionService;
    }

    /**
     * Lista todos los camiones.
     */
    @GetMapping
    public ResponseEntity<?> listar() {
        try {
            return ResponseEntity.ok(camionService.listar());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener camiones: " + e.getMessage());
        }
    }

    /**
     * Resumen de camiones por tipo.
     */
    @GetMapping("/resumen")
    public ResponseEntity<?> resumen() {
        try {
            return ResponseEntity.ok(camionService.resumen());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener resumen: " + e.getMessage());
        }
    }

    /**
     * Agrega un nuevo camión.
     */
    @PostMapping
    public ResponseEntity<?> agregar(@RequestBody CamionRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(camionService.agregar(request));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al crear camión: " + e.getMessage());
        }
    }

    /**
     * Resumen de camiones por estado.
     */
    @GetMapping("/resumen-estado")
    public ResponseEntity<?> resumenPorEstado() {
        try {
            return ResponseEntity.ok(camionService.resumenPorEstado());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener resumen por estado: " + e.getMessage());
        }
    }

    /**
     * Lista los estados posibles de los camiones con su descripción.
     */
    @GetMapping("/estados")
    public ResponseEntity<?> listarEstados() {
        try {
            return ResponseEntity.ok(camionService.listarEstados());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener estados: " + e.getMessage());
        }
    }

    /**
     * Lista todos los camiones con sus datos principales (estado, id, tipo,
     * coordenada).
     */
    @GetMapping("/camiones-estado")
    public ResponseEntity<?> listarCamionesEstado() {
        try {
            return ResponseEntity.ok(camionService.listarCamionesEstado());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al listar camiones estado: " + e.getMessage());
        }
    }

    /**
     * Obtiene información detallada de cada camión incluyendo: - Número de
     * pedidos asociados - Cantidad de GLP - Cantidad de gasolina - Kilómetros
     * restantes por recorrer - Estado actual
     */
    @GetMapping("/info-detallada")
    public ResponseEntity<?> obtenerInfoDetallada() {
        try {
            return ResponseEntity.ok(camionService.obtenerInfoDetallada());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener información detallada de camiones: " + e.getMessage());
        }
    }
}
