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

import com.plg.dto.request.PedidoRequest;
import com.plg.dto.request.PedidosLoteRequest;
import com.plg.service.PedidoService;
import jakarta.validation.Valid;

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
     * Lista los pedidos dentro de un rango de fechas.
     */
    @GetMapping("/rango")
    public ResponseEntity<?> listarPorFecha(@RequestParam String inicio,
            @RequestParam String fin) {
        try {
            java.time.LocalDateTime start = java.time.LocalDateTime.parse(inicio);
            java.time.LocalDateTime end = java.time.LocalDateTime.parse(fin);
            return ResponseEntity.ok(pedidoService.listarPorFecha(start, end));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al filtrar pedidos: " + e.getMessage());
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

    /**
     * Endpoint para actualizar solo el estado de un pedido por su código.
     */
    @PostMapping("/actualizar-estado")
    public ResponseEntity<?> actualizarEstado(@RequestBody com.plg.dto.request.PedidoEstadoUpdateRequest request) {
        try {
            return ResponseEntity.ok(pedidoService.actualizarEstado(request));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al actualizar estado del pedido: " + e.getMessage());
        }
    }

    /**
     * Endpoint para cargar un lote de pedidos desde el frontend.
     * Reemplaza la carga desde archivos planos.
     */
    @PostMapping("/cargar-lote")
    public ResponseEntity<?> cargarLotePedidos(@Valid @RequestBody PedidosLoteRequest request) {
        try {
            System.out.println("🌐 ENDPOINT LLAMADO: /api/pedidos/cargar-lote");
            System.out.println("📦 Recibidos " + request.getPedidos().size() + " pedidos para procesar");
            System.out.println("📅 Fecha inicio: " + request.getFechaInicio());
            
            var resultado = pedidoService.cargarLotePedidos(request);
            
            System.out.println("✅ ENDPOINT RESPUESTA: Lote procesado exitosamente");
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            String errorMsg = "Error al cargar lote de pedidos: " + e.getMessage();
            System.err.println("❌ " + errorMsg);
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMsg);
        }
    }

    /**
     * Endpoint para validar el formato de un lote de pedidos sin procesarlos.
     */
    @PostMapping("/validar-lote")
    public ResponseEntity<?> validarLotePedidos(@Valid @RequestBody PedidosLoteRequest request) {
        try {
            System.out.println("🌐 ENDPOINT LLAMADO: /api/pedidos/validar-lote");
            System.out.println("🔍 Validando " + request.getPedidos().size() + " pedidos");
            
            var resultado = pedidoService.validarLotePedidos(request);
            
            System.out.println("✅ ENDPOINT RESPUESTA: Validación completada");
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            String errorMsg = "Error al validar lote de pedidos: " + e.getMessage();
            System.err.println("❌ " + errorMsg);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMsg);
        }
    }
}
