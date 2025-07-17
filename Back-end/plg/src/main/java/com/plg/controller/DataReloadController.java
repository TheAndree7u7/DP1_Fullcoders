package com.plg.controller;

import com.plg.config.DataLoader;
import com.plg.dto.DataReloadResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/data-reload")
@CrossOrigin(origins = "*") // Habilitar CORS para todas las origenes
public class DataReloadController {

    @PostMapping("/recargar-todos")
    public ResponseEntity<DataReloadResponse> recargarTodosLosDatos() {
        System.out.println("🌐 ENDPOINT LLAMADO: /api/data-reload/recargar-todos");

        try {
            // Crear una nueva instancia de DataLoader para recargar todos los datos
            DataLoader dataLoader = new DataLoader();

            // Obtener las cantidades de datos cargados
            int pedidosCargados = dataLoader.pedidos.size();
            int camionesCargados = dataLoader.camiones.size();
            int almacenesCargados = dataLoader.almacenes.size();
            int averiasCargadas = dataLoader.averias.size();
            int mantenimientosCargados = dataLoader.mantenimientos.size();
            int bloqueosCargados = dataLoader.bloqueos.size();

            // Crear mensaje de éxito
            String mensaje = String.format(
                    "Datos recargados exitosamente. Pedidos: %d, Camiones: %d, Almacenes: %d, Averías: %d, Mantenimientos: %d, Bloqueos: %d",
                    pedidosCargados, camionesCargados, almacenesCargados, averiasCargadas, mantenimientosCargados,
                    bloqueosCargados);

            System.out.println("✅ " + mensaje);

            DataReloadResponse response = new DataReloadResponse(
                    true,
                    mensaje,
                    pedidosCargados,
                    camionesCargados,
                    almacenesCargados,
                    averiasCargadas,
                    mantenimientosCargados,
                    bloqueosCargados);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("❌ Error inesperado: " + e.getMessage());
            DataReloadResponse response = new DataReloadResponse(false, "Error inesperado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/estado")
    public ResponseEntity<DataReloadResponse> obtenerEstadoDatos() {
        System.out.println("🌐 ENDPOINT LLAMADO: /api/data-reload/estado");

        try {
            // Crear una instancia temporal para obtener el estado actual
            DataLoader dataLoader = new DataLoader();

            int pedidosCargados = dataLoader.pedidos.size();
            int camionesCargados = dataLoader.camiones.size();
            int almacenesCargados = dataLoader.almacenes.size();
            int averiasCargadas = dataLoader.averias.size();
            int mantenimientosCargados = dataLoader.mantenimientos.size();
            int bloqueosCargados = dataLoader.bloqueos.size();

            String mensaje = String.format(
                    "Estado actual de datos. Pedidos: %d, Camiones: %d, Almacenes: %d, Averías: %d, Mantenimientos: %d, Bloqueos: %d",
                    pedidosCargados, camionesCargados, almacenesCargados, averiasCargadas, mantenimientosCargados,
                    bloqueosCargados);

            DataReloadResponse response = new DataReloadResponse(
                    true,
                    mensaje,
                    pedidosCargados,
                    camionesCargados,
                    almacenesCargados,
                    averiasCargadas,
                    mantenimientosCargados,
                    bloqueosCargados);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("❌ Error al obtener estado: " + e.getMessage());
            DataReloadResponse response = new DataReloadResponse(false, "Error al obtener estado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}