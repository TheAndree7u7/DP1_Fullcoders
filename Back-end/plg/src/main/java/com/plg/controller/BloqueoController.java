package com.plg.controller;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.plg.dto.request.BloqueoRequest;
import com.plg.service.BloqueoService;

/**
 * Controlador REST para bloqueos.
 */
@RestController
@RequestMapping("/api/bloqueos")
@CrossOrigin(origins = "*")
public class BloqueoController {

    private final BloqueoService bloqueoService;

    public BloqueoController(BloqueoService bloqueoService) {
        this.bloqueoService = bloqueoService;
    }

    /**
     * Lista todos los bloqueos.
     */
    @GetMapping
    public ResponseEntity<?> listar() {
        try {
            return ResponseEntity.ok(bloqueoService.listar());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener bloqueos: " + e.getMessage());
        }
    }

    /**
     * Lista los bloqueos dentro de un rango de fechas.
     */
    @GetMapping("/rango")
    public ResponseEntity<?> listarPorFecha(@RequestParam String inicio,
            @RequestParam String fin) {
        try {
            LocalDateTime start = LocalDateTime.parse(inicio);
            LocalDateTime end = LocalDateTime.parse(fin);
            return ResponseEntity.ok(bloqueoService.listarPorFecha(start, end));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al filtrar bloqueos: " + e.getMessage());
        }
    }

    /**
     * Lista todos los bloqueos activos.
     */
    @GetMapping("/activos")
    public ResponseEntity<?> listarActivos() {
        try {
            return ResponseEntity.ok(bloqueoService.listarActivos());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener bloqueos activos: " + e.getMessage());
        }
    }

    /**
     * Resumen de bloqueos por estado.
     */
    @GetMapping("/resumen")
    public ResponseEntity<?> resumen() {
        try {
            return ResponseEntity.ok(bloqueoService.resumen());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener resumen: " + e.getMessage());
        }
    }

    /**
     * Agrega un nuevo bloqueo.
     */
    @PostMapping
    public ResponseEntity<?> agregar(@RequestBody BloqueoRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(bloqueoService.agregar(request));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al crear bloqueo: " + e.getMessage());
        }
    }
}
