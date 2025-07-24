package com.plg.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.plg.dto.request.ArchivoPedidosRequest;
import com.plg.dto.response.ArchivoPedidosResponse;
import com.plg.service.ArchivosService;

/**
 * Controlador REST para manejar archivos de pedidos.
 */
@RestController
@RequestMapping("/api/archivos")
@CrossOrigin(origins = "*")
public class ArchivosController {

    private final ArchivosService archivosService;

    public ArchivosController(ArchivosService archivosService) {
        this.archivosService = archivosService;
    }

    /**
     * Procesa un archivo de pedidos y agrega los pedidos al sistema.
     * 
     * @param request Solicitud con el archivo de pedidos
     * @return Respuesta con los pedidos agregados
     */
    @PostMapping("/pedidos")
    public ResponseEntity<?> procesarArchivoPedidos(@RequestBody ArchivoPedidosRequest request) {
        try {
            // Validar que la solicitud no sea nula
            if (request == null) {
                return ResponseEntity.badRequest()
                        .body("La solicitud no puede estar vacía");
            }

            // Validar campos requeridos
            if (request.getNombre() == null || request.getNombre().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("El nombre del archivo es requerido");
            }

            if (request.getContenido() == null || request.getContenido().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("El contenido del archivo es requerido");
            }

            // Procesar el archivo
            ArchivoPedidosResponse response = archivosService.procesarArchivoPedidos(request);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body("Error de validación: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor: " + e.getMessage());
        }
    }

    /**
     * Procesa pedidos individuales usando los datos parseados.
     * 
     * @param request Solicitud con los datos de pedidos
     * @return Respuesta con los pedidos agregados
     */
    @PostMapping("/pedidos/individuales")
    public ResponseEntity<?> procesarPedidosIndividuales(@RequestBody ArchivoPedidosRequest request) {
        try {
            // Validar que la solicitud no sea nula
            if (request == null) {
                return ResponseEntity.badRequest()
                        .body("La solicitud no puede estar vacía");
            }

            // Validar campos requeridos
            if (request.getNombre() == null || request.getNombre().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("El nombre del archivo es requerido");
            }

            if (request.getDatos() == null || request.getDatos().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Los datos de pedidos son requeridos");
            }

            // Procesar los pedidos individuales
            ArchivoPedidosResponse response = archivosService.procesarPedidosIndividuales(request);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body("Error de validación: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor: " + e.getMessage());
        }
    }
}