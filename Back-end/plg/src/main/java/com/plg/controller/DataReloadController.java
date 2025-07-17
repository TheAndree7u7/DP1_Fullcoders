package com.plg.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import com.plg.service.DataReloadService;
import com.plg.service.DataReloadService.DataReloadResult;
import com.plg.dto.DataReloadDto;
import com.plg.utils.ExcepcionesPerzonalizadas.InvalidDataFormatException;

import java.io.IOException;

/**
 * Controlador para operaciones de recarga de datos del sistema.
 * Permite recargar todos los datos desde el frontend.
 * Implementa el principio de responsabilidad única (SRP) al encargarse
 * únicamente de las operaciones de recarga de datos.
 */
@RestController
@RequestMapping("/api/data-reload")
@CrossOrigin(origins = "*")
public class DataReloadController {

    private final DataReloadService dataReloadService;

    public DataReloadController(DataReloadService dataReloadService) {
        this.dataReloadService = dataReloadService;
    }

    /**
     * Recarga todos los datos del sistema.
     * Limpia las listas existentes y carga nuevos datos desde los archivos.
     * 
     * @return Resultado de la recarga con estadísticas de los datos cargados
     */
    @PostMapping("/recargar-todos")
    public ResponseEntity<DataReloadDto> recargarTodosLosDatos() {
        System.out.println("🌐 ENDPOINT LLAMADO: /api/data-reload/recargar-todos");

        try {
            DataReloadResult resultado = dataReloadService.recargarTodosLosDatos();

            DataReloadDto dto = DataReloadDto.crearExitoso(
                    resultado.getCantidadAlmacenes(),
                    resultado.getCantidadCamiones(),
                    resultado.getCantidadPedidos(),
                    resultado.getCantidadAverias(),
                    resultado.getCantidadMantenimientos(),
                    resultado.getCantidadBloqueos(),
                    resultado.getFechaMinimaPedidos(),
                    resultado.getFechaMaximaPedidos());

            System.out.println("✅ ENDPOINT RESPUESTA: Recarga completada exitosamente");
            System.out.println("📊 Datos recargados: " + dto);

            return ResponseEntity.ok(dto);

        } catch (InvalidDataFormatException e) {
            System.err.println("❌ Error de formato en los datos: " + e.getMessage());
            e.printStackTrace();
            DataReloadDto dtoError = DataReloadDto.crearError("Error de formato en los datos: " + e.getMessage());
            return ResponseEntity.badRequest().body(dtoError);

        } catch (IOException e) {
            System.err.println("❌ Error de lectura de archivos: " + e.getMessage());
            e.printStackTrace();
            DataReloadDto dtoError = DataReloadDto.crearError("Error de lectura de archivos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(dtoError);

        } catch (Exception e) {
            System.err.println("❌ Error inesperado durante la recarga: " + e.getMessage());
            e.printStackTrace();
            DataReloadDto dtoError = DataReloadDto.crearError("Error inesperado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(dtoError);
        }
    }

    /**
     * Endpoint de salud para verificar que el controlador está funcionando.
     * 
     * @return Mensaje de confirmación
     */
    @PostMapping("/health")
    public ResponseEntity<String> healthCheck() {
        System.out.println("🌐 ENDPOINT LLAMADO: /api/data-reload/health");
        return ResponseEntity.ok("DataReloadController funcionando correctamente");
    }
}