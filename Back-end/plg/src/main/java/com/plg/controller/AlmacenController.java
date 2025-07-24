package com.plg.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.plg.dto.request.AlmacenRequest;
import com.plg.service.AlmacenService;

/**
 * Controlador REST para almacenes.
 */
@RestController
@RequestMapping("/api/almacenes")
@CrossOrigin(origins = "*")
public class AlmacenController {

    private final AlmacenService almacenService;

    public AlmacenController(AlmacenService almacenService) {
        this.almacenService = almacenService;
    }

    /**
     * Lista todos los almacenes.
     */
    @GetMapping
    public ResponseEntity<?> listar() {
        try {
            return ResponseEntity.ok(almacenService.listar());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener almacenes: " + e.getMessage());
        }
    }

    /**
     * Resumen de almacenes por tipo.
     */
    @GetMapping("/resumen")
    public ResponseEntity<?> resumen() {
        try {
            return ResponseEntity.ok(almacenService.resumen());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener resumen: " + e.getMessage());
        }
    }

    
}
