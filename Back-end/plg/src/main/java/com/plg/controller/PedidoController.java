package com.plg.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.plg.dto.request.PedidoRequest;
import com.plg.service.PedidoService;

/**
 * Controlador REST para pedidos.
 */
@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "*")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    /**
     * Lista todos los pedidos.
     */
    @GetMapping
    public ResponseEntity<?> listar() {
        try {
            return ResponseEntity.ok(pedidoService.listar());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener pedidos: " + e.getMessage());
        }
    }

    /**
     * Resumen de pedidos por estado.
     */
    @GetMapping("/resumen")
    public ResponseEntity<?> resumen() {
        try {
            return ResponseEntity.ok(pedidoService.resumen());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener resumen: " + e.getMessage());
        }
    }

    /**
     * Agrega un nuevo pedido.
     */
    @PostMapping
    public ResponseEntity<?> agregar(@RequestBody PedidoRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.agregar(request));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al crear pedido: " + e.getMessage());
        }
    }
}
