package com.plg.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import com.plg.dto.ParametrosDto;
import com.plg.dto.request.ParametrosRequest;
import com.plg.service.ParametrosService;

/**
 * Controlador para operaciones sobre parámetros de simulación.
 * Permite leer y actualizar los parámetros desde el frontend.
 */
@RestController
@RequestMapping("/api/parametros")
@CrossOrigin(origins = "*") // O usa "http://localhost:5173" para mayor seguridad
public class ParametrosController {

    private final ParametrosService parametrosService;

    public ParametrosController(ParametrosService parametrosService) {
        this.parametrosService = parametrosService;
    }

    /**
     * Obtiene todos los parámetros actuales de la simulación.
     * 
     * @return DTO con todos los parámetros
     */
    @GetMapping
    public ResponseEntity<ParametrosDto> obtenerParametros() {
        System.out.println("🌐 ENDPOINT LLAMADO: /api/parametros");
        try {
            ParametrosDto parametros = parametrosService.obtenerParametros();
            System.out.println("✅ ENDPOINT RESPUESTA: Parámetros obtenidos correctamente");
            return ResponseEntity.ok(parametros);
        } catch (Exception e) {
            System.err.println("❌ Error al obtener parámetros: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene un parámetro específico por su nombre.
     * 
     * @param nombreParametro Nombre del parámetro a obtener
     * @return Valor del parámetro como String
     */
    @GetMapping("/{nombreParametro}")
    public ResponseEntity<String> obtenerParametroEspecifico(@PathVariable String nombreParametro) {
        System.out.println("🌐 ENDPOINT LLAMADO: /api/parametros/" + nombreParametro);
        try {
            String valor = parametrosService.obtenerParametroEspecifico(nombreParametro);
            System.out.println("✅ ENDPOINT RESPUESTA: " + nombreParametro + " = " + valor);
            return ResponseEntity.ok(valor);
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Parámetro no encontrado: " + nombreParametro);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("❌ Error al obtener parámetro " + nombreParametro + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Actualiza los parámetros de simulación con los valores proporcionados.
     * Solo actualiza los campos que no son null en el request.
     * 
     * @param request Request con los nuevos valores de parámetros
     * @return DTO con los parámetros actualizados
     */
    @PutMapping
    public ResponseEntity<ParametrosDto> actualizarParametros(@RequestBody ParametrosRequest request) {
        System.out.println("🌐 ENDPOINT LLAMADO: /api/parametros (PUT)");
        System.out.println("📝 Datos recibidos: " + request);

        try {
            ParametrosDto parametrosActualizados = parametrosService.actualizarParametros(request);
            System.out.println("✅ ENDPOINT RESPUESTA: Parámetros actualizados correctamente");
            return ResponseEntity.ok(parametrosActualizados);
        } catch (Exception e) {
            System.err.println("❌ Error al actualizar parámetros: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Actualiza un parámetro específico.
     * 
     * @param nombreParametro Nombre del parámetro a actualizar
     * @param valor           Nuevo valor del parámetro
     * @return DTO con todos los parámetros actualizados
     */
    @PutMapping("/{nombreParametro}")
    public ResponseEntity<ParametrosDto> actualizarParametroEspecifico(
            @PathVariable String nombreParametro,
            @RequestBody String valor) {
        System.out.println("🌐 ENDPOINT LLAMADO: /api/parametros/" + nombreParametro + " (PUT)");
        System.out.println("📝 Valor recibido: " + valor);

        try {
            // Crear un request con solo el parámetro específico
            ParametrosRequest request = new ParametrosRequest();

            switch (nombreParametro.toLowerCase()) {
                case "dia":
                    request.setDia(valor);
                    break;
                case "mes":
                    request.setMes(valor);
                    break;
                case "anho":
                    request.setAnho(valor);
                    break;
                case "intervalotiempo":
                    request.setIntervaloTiempo(Integer.parseInt(valor));
                    break;
                case "contadorprueba":
                    request.setContadorPrueba(Integer.parseInt(valor));
                    break;
                case "kilometrosrecorridos":
                    request.setKilometrosRecorridos(Double.parseDouble(valor));
                    break;
                case "fitnessglobal":
                    request.setFitnessGlobal(Double.parseDouble(valor));
                    break;
                case "semillaaleatoria":
                    request.setSemillaAleatoria(Long.parseLong(valor));
                    break;
                case "primerallamada":
                    request.setPrimeraLlamada(Boolean.parseBoolean(valor));
                    break;
                case "tiposimulacion":
                    request.setTipoSimulacion(com.plg.utils.TipoSimulacion.valueOf(valor.toUpperCase()));
                    break;
                default:
                    return ResponseEntity.badRequest().build();
            }

            ParametrosDto parametrosActualizados = parametrosService.actualizarParametros(request);
            System.out.println("✅ ENDPOINT RESPUESTA: Parámetro " + nombreParametro + " actualizado correctamente");
            return ResponseEntity.ok(parametrosActualizados);
        } catch (NumberFormatException e) {
            System.err.println("❌ Error de formato en valor: " + valor);
            return ResponseEntity.badRequest().build();
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Valor inválido para " + nombreParametro + ": " + valor);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("❌ Error al actualizar parámetro " + nombreParametro + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Reinicia todos los parámetros a sus valores por defecto.
     * 
     * @return DTO con los parámetros reiniciados
     */
    @DeleteMapping
    public ResponseEntity<ParametrosDto> reiniciarParametros() {
        System.out.println("🌐 ENDPOINT LLAMADO: /api/parametros (DELETE)");
        try {
            ParametrosDto parametrosReiniciados = parametrosService.reiniciarParametros();
            System.out.println("✅ ENDPOINT RESPUESTA: Parámetros reiniciados correctamente");
            return ResponseEntity.ok(parametrosReiniciados);
        } catch (Exception e) {
            System.err.println("❌ Error al reiniciar parámetros: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}