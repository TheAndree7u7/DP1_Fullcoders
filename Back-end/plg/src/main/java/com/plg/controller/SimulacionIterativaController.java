package com.plg.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.plg.dto.IndividuoDto;
import com.plg.dto.request.SimulacionRequest;
import com.plg.dto.request.SolucionFechaRequest;
import com.plg.utils.Simulacion;
import com.plg.utils.Simulacion.EstadoSimulacionIterativa;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller para manejar la simulación iterativa.
 * Permite configurar la simulación y obtener soluciones paso a paso.
 */
@RestController
@RequestMapping("/api/simulacion-iterativa")
@CrossOrigin(origins = "*")
public class SimulacionIterativaController {

    /**
     * Configura la simulación iterativa con una fecha de inicio.
     * 
     * @param request Datos de configuración de la simulación
     * @return ResponseEntity con el resultado de la configuración
     */
    @PostMapping("/configurar")
    public ResponseEntity<Map<String, Object>> configurarSimulacion(@RequestBody SimulacionRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("🔧 Recibida petición de configuración de simulación iterativa");

            // Validar parámetros
            if (request.getFechaInicio() == null) {
                response.put("success", false);
                response.put("message", "La fecha de inicio es requerida");
                return ResponseEntity.badRequest().body(response);
            }

            // Los pedidos semanales deben configurarse previamente usando otros endpoints
            // Este endpoint solo configura la simulación con la fecha de inicio

            // Configurar la simulación
            boolean configuracionExitosa = Simulacion.configurarSimulacionIterativa(request.getFechaInicio());

            if (configuracionExitosa) {
                // Obtener estado inicial
                EstadoSimulacionIterativa estado = Simulacion.obtenerEstadoSimulacionIterativa();

                response.put("success", true);
                response.put("message", "Simulación configurada exitosamente");
                response.put("estado", Map.of(
                        "configurada", estado.configurada,
                        "finalizada", estado.finalizada,
                        "iteraciones", estado.iteraciones,
                        "fechaActual", estado.fechaActual,
                        "fechaLimite", estado.fechaLimite,
                        "pedidosSemanales", estado.pedidosSemanales,
                        "pedidosPorAtender", estado.pedidosPorAtender,
                        "pedidosPlanificados", estado.pedidosPlanificados,
                        "pedidosEntregados", estado.pedidosEntregados));

                System.out.println("✅ Simulación configurada exitosamente");
                return ResponseEntity.ok(response);

            } else {
                response.put("success", false);
                response.put("message", "Error al configurar la simulación");
                return ResponseEntity.internalServerError().body(response);
            }

        } catch (Exception e) {
            System.err.println("❌ Error configurando simulación: " + e.getMessage());
            e.printStackTrace();

            response.put("success", false);
            response.put("message", "Error interno: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Obtiene la siguiente solución de la simulación iterativa.
     * 
     * @return ResponseEntity con la solución del algoritmo genético
     */
    @PostMapping("/obtener-solucion")
    public ResponseEntity<Map<String, Object>> obtenerSiguienteSolucion() {
        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("🧬 Recibida petición de siguiente solución");

            // Obtener siguiente solución
            IndividuoDto solucion = Simulacion.obtenerSiguienteSolucion();

            if (solucion != null) {
                // Obtener estado actual
                EstadoSimulacionIterativa estado = Simulacion.obtenerEstadoSimulacionIterativa();

                response.put("success", true);
                response.put("solucion", solucion);
                response.put("estado", Map.of(
                        "configurada", estado.configurada,
                        "finalizada", estado.finalizada,
                        "iteraciones", estado.iteraciones,
                        "fechaActual", estado.fechaActual,
                        "fechaLimite", estado.fechaLimite,
                        "pedidosSemanales", estado.pedidosSemanales,
                        "pedidosPorAtender", estado.pedidosPorAtender,
                        "pedidosPlanificados", estado.pedidosPlanificados,
                        "pedidosEntregados", estado.pedidosEntregados));

                System.out.println("✅ Solución obtenida exitosamente (iteración " + estado.iteraciones + ")");
                return ResponseEntity.ok(response);

            } else {
                // Simulación terminada o no configurada
                EstadoSimulacionIterativa estado = Simulacion.obtenerEstadoSimulacionIterativa();

                response.put("success", false);
                response.put("solucion", null);

                if (!estado.configurada) {
                    response.put("message", "La simulación no ha sido configurada");
                } else if (estado.finalizada) {
                    response.put("message", "La simulación ha terminado");
                } else {
                    response.put("message", "No se pudo obtener la siguiente solución");
                }

                response.put("estado", Map.of(
                        "configurada", estado.configurada,
                        "finalizada", estado.finalizada,
                        "iteraciones", estado.iteraciones,
                        "fechaActual", estado.fechaActual,
                        "fechaLimite", estado.fechaLimite,
                        "pedidosSemanales", estado.pedidosSemanales,
                        "pedidosPorAtender", estado.pedidosPorAtender,
                        "pedidosPlanificados", estado.pedidosPlanificados,
                        "pedidosEntregados", estado.pedidosEntregados));

                return ResponseEntity.ok(response);
            }

        } catch (Exception e) {
            System.err.println("❌ Error obteniendo siguiente solución: " + e.getMessage());
            e.printStackTrace();

            response.put("success", false);
            response.put("message", "Error interno: " + e.getMessage());
            response.put("solucion", null);
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Obtiene la solución de la simulación para una fecha específica.
     * Permite especificar si se debe avanzar la simulación hasta esa fecha o solo
     * calcular la solución.
     * 
     * @param request Datos con la fecha específica y opción de avanzar
     * @return ResponseEntity con la solución del algoritmo genético para la fecha
     *         especificada
     */
    @PostMapping("/obtener-solucion-fecha")
    public ResponseEntity<Map<String, Object>> obtenerSolucionParaFecha(@RequestBody SolucionFechaRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("🎯 Recibida petición de solución para fecha específica: " + request.getFecha());

            // Validar parámetros
            if (request.getFecha() == null) {
                response.put("success", false);
                response.put("message", "La fecha es requerida");
                return ResponseEntity.badRequest().body(response);
            }

            // Obtener solución según el tipo de operación
            IndividuoDto solucion;
            if (request.isAvanzarSimulacion()) {
                System.out.println("⏩ Avanzando simulación hasta fecha: " + request.getFecha());
                solucion = Simulacion.avanzarHastaFecha(request.getFecha());
            } else {
                System.out.println("🎯 Calculando solución para fecha: " + request.getFecha());
                solucion = Simulacion.obtenerSolucionParaFecha(request.getFecha());
            }

            if (solucion != null) {
                // Obtener estado actual
                EstadoSimulacionIterativa estado = Simulacion.obtenerEstadoSimulacionIterativa();

                response.put("success", true);
                response.put("solucion", solucion);
                response.put("fechaSolicitada", request.getFecha());
                response.put("avanzarSimulacion", request.isAvanzarSimulacion());
                response.put("estado", Map.of(
                        "configurada", estado.configurada,
                        "finalizada", estado.finalizada,
                        "iteraciones", estado.iteraciones,
                        "fechaActual", estado.fechaActual,
                        "fechaLimite", estado.fechaLimite,
                        "pedidosSemanales", estado.pedidosSemanales,
                        "pedidosPorAtender", estado.pedidosPorAtender,
                        "pedidosPlanificados", estado.pedidosPlanificados,
                        "pedidosEntregados", estado.pedidosEntregados));

                String operacion = request.isAvanzarSimulacion() ? "avanzado hasta" : "calculado para";
                System.out.println("✅ Solución " + operacion + " fecha " + request.getFecha() + " exitosamente");
                return ResponseEntity.ok(response);

            } else {
                // Error al obtener solución
                EstadoSimulacionIterativa estado = Simulacion.obtenerEstadoSimulacionIterativa();

                response.put("success", false);
                response.put("solucion", null);
                response.put("fechaSolicitada", request.getFecha());
                response.put("avanzarSimulacion", request.isAvanzarSimulacion());

                if (!estado.configurada) {
                    response.put("message", "La simulación no ha sido configurada");
                } else if (estado.finalizada) {
                    response.put("message", "La simulación ha terminado");
                } else {
                    response.put("message", "No se pudo obtener la solución para la fecha especificada");
                }

                response.put("estado", Map.of(
                        "configurada", estado.configurada,
                        "finalizada", estado.finalizada,
                        "iteraciones", estado.iteraciones,
                        "fechaActual", estado.fechaActual,
                        "fechaLimite", estado.fechaLimite,
                        "pedidosSemanales", estado.pedidosSemanales,
                        "pedidosPorAtender", estado.pedidosPorAtender,
                        "pedidosPlanificados", estado.pedidosPlanificados,
                        "pedidosEntregados", estado.pedidosEntregados));

                return ResponseEntity.ok(response);
            }

        } catch (Exception e) {
            System.err.println("❌ Error obteniendo solución para fecha " + request.getFecha() + ": " + e.getMessage());
            e.printStackTrace();

            response.put("success", false);
            response.put("message", "Error interno: " + e.getMessage());
            response.put("solucion", null);
            response.put("fechaSolicitada", request.getFecha());
            response.put("avanzarSimulacion", request.isAvanzarSimulacion());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Obtiene el estado actual de la simulación iterativa.
     * 
     * @return ResponseEntity con el estado actual
     */
    @GetMapping("/estado")
    public ResponseEntity<Map<String, Object>> obtenerEstado() {
        Map<String, Object> response = new HashMap<>();

        try {
            EstadoSimulacionIterativa estado = Simulacion.obtenerEstadoSimulacionIterativa();

            response.put("success", true);
            response.put("estado", Map.of(
                    "configurada", estado.configurada,
                    "finalizada", estado.finalizada,
                    "iteraciones", estado.iteraciones,
                    "fechaActual", estado.fechaActual,
                    "fechaLimite", estado.fechaLimite,
                    "pedidosSemanales", estado.pedidosSemanales,
                    "pedidosPorAtender", estado.pedidosPorAtender,
                    "pedidosPlanificados", estado.pedidosPlanificados,
                    "pedidosEntregados", estado.pedidosEntregados));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Error obteniendo estado: " + e.getMessage());
            e.printStackTrace();

            response.put("success", false);
            response.put("message", "Error interno: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Reinicia la simulación iterativa.
     * 
     * @return ResponseEntity con el resultado del reinicio
     */
    @PostMapping("/reiniciar")
    public ResponseEntity<Map<String, Object>> reiniciarSimulacion() {
        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("🔄 Recibida petición de reinicio de simulación");

            Simulacion.reiniciarSimulacionIterativa();

            response.put("success", true);
            response.put("message", "Simulación reiniciada exitosamente");

            // Obtener estado después del reinicio
            EstadoSimulacionIterativa estado = Simulacion.obtenerEstadoSimulacionIterativa();
            response.put("estado", Map.of(
                    "configurada", estado.configurada,
                    "finalizada", estado.finalizada,
                    "iteraciones", estado.iteraciones,
                    "fechaActual", estado.fechaActual,
                    "fechaLimite", estado.fechaLimite,
                    "pedidosSemanales", estado.pedidosSemanales,
                    "pedidosPorAtender", estado.pedidosPorAtender,
                    "pedidosPlanificados", estado.pedidosPlanificados,
                    "pedidosEntregados", estado.pedidosEntregados));

            System.out.println("✅ Simulación reiniciada exitosamente");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Error reiniciando simulación: " + e.getMessage());
            e.printStackTrace();

            response.put("success", false);
            response.put("message", "Error interno: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Obtiene información de Postman para facilitar las pruebas.
     * Devuelve todos los endpoints disponibles con ejemplos de uso.
     * 
     * @return ResponseEntity con información de Postman
     */
    @GetMapping("/postman-info")
    public ResponseEntity<Map<String, Object>> obtenerInfoPostman() {
        Map<String, Object> response = new HashMap<>();

        try {
            Map<String, Object> postmanInfo = new HashMap<>();

            // Información general
            postmanInfo.put("baseUrl", "http://localhost:8080");
            postmanInfo.put("description",
                    "API de Simulación Iterativa - Endpoints para configurar y ejecutar simulaciones paso a paso");

            // Variables de entorno recomendadas
            Map<String, String> variables = new HashMap<>();
            variables.put("baseUrl", "http://localhost:8080");
            variables.put("fechaInicio", "2024-01-15T08:00:00");
            variables.put("fechaEspecifica", "2024-01-15T14:30:00");
            postmanInfo.put("variables", variables);

            // Endpoints disponibles
            Map<String, Object> endpoints = new HashMap<>();

            // 1. Configurar
            Map<String, Object> configurar = new HashMap<>();
            configurar.put("method", "POST");
            configurar.put("url", "{{baseUrl}}/api/simulacion-iterativa/configurar");
            configurar.put("description", "Configura la simulación iterativa con una fecha de inicio");
            configurar.put("body", Map.of(
                    "fechaInicio", "{{fechaInicio}}"));
            configurar.put("headers", Map.of(
                    "Content-Type", "application/json"));
            endpoints.put("configurar", configurar);

            // 2. Obtener siguiente solución
            Map<String, Object> siguienteSolucion = new HashMap<>();
            siguienteSolucion.put("method", "POST");
            siguienteSolucion.put("url", "{{baseUrl}}/api/simulacion-iterativa/obtener-solucion");
            siguienteSolucion.put("description", "Obtiene la siguiente solución del algoritmo genético (secuencial)");
            siguienteSolucion.put("body", "No requiere body");
            siguienteSolucion.put("headers", Map.of(
                    "Content-Type", "application/json"));
            endpoints.put("obtenerSiguienteSolucion", siguienteSolucion);

            // 3. Obtener solución para fecha específica
            Map<String, Object> solucionFecha = new HashMap<>();
            solucionFecha.put("method", "POST");
            solucionFecha.put("url", "{{baseUrl}}/api/simulacion-iterativa/obtener-solucion-fecha");
            solucionFecha.put("description", "Obtiene la solución para una fecha específica");
            solucionFecha.put("body", Map.of(
                    "fecha", "{{fechaEspecifica}}",
                    "avanzarSimulacion", false));
            solucionFecha.put("headers", Map.of(
                    "Content-Type", "application/json"));
            endpoints.put("obtenerSolucionFecha", solucionFecha);

            // 4. Obtener estado
            Map<String, Object> estado = new HashMap<>();
            estado.put("method", "GET");
            estado.put("url", "{{baseUrl}}/api/simulacion-iterativa/estado");
            estado.put("description", "Obtiene el estado actual de la simulación");
            estado.put("body", "No requiere body");
            estado.put("headers", "No requiere headers especiales");
            endpoints.put("estado", estado);

            // 5. Reiniciar
            Map<String, Object> reiniciar = new HashMap<>();
            reiniciar.put("method", "POST");
            reiniciar.put("url", "{{baseUrl}}/api/simulacion-iterativa/reiniciar");
            reiniciar.put("description", "Reinicia la simulación iterativa");
            reiniciar.put("body", "No requiere body");
            reiniciar.put("headers", Map.of(
                    "Content-Type", "application/json"));
            endpoints.put("reiniciar", reiniciar);

            postmanInfo.put("endpoints", endpoints);

            // Flujos de prueba recomendados
            Map<String, Object> flujos = new HashMap<>();

            Map<String, Object> flujoSecuencial = new HashMap<>();
            flujoSecuencial.put("nombre", "Simulación Secuencial Completa");
            flujoSecuencial.put("pasos", new String[] {
                    "1. POST /configurar",
                    "2. GET /estado",
                    "3. POST /obtener-solucion (repetir hasta que termine)",
                    "4. GET /estado"
            });
            flujos.put("secuencial", flujoSecuencial);

            Map<String, Object> flujoFechas = new HashMap<>();
            flujoFechas.put("nombre", "Navegación por Fechas Específicas");
            flujoFechas.put("pasos", new String[] {
                    "1. POST /configurar",
                    "2. POST /obtener-solucion-fecha (con diferentes fechas, avanzarSimulacion: false)",
                    "3. POST /obtener-solucion-fecha (con avanzarSimulacion: true)"
            });
            flujos.put("fechas", flujoFechas);

            postmanInfo.put("flujos", flujos);

            // Ejemplos de respuestas
            Map<String, Object> ejemplos = new HashMap<>();

            Map<String, Object> respuestaExito = new HashMap<>();
            respuestaExito.put("success", true);
            respuestaExito.put("message", "Operación exitosa");
            respuestaExito.put("estado", Map.of(
                    "configurada", true,
                    "finalizada", false,
                    "iteraciones", 5,
                    "fechaActual", "2024-01-15T10:30:00",
                    "fechaLimite", "2024-01-22T08:00:00",
                    "pedidosSemanales", 145,
                    "pedidosPorAtender", 2,
                    "pedidosPlanificados", 3,
                    "pedidosEntregados", 0));
            ejemplos.put("respuestaExito", respuestaExito);

            Map<String, Object> respuestaError = new HashMap<>();
            respuestaError.put("success", false);
            respuestaError.put("message", "La simulación no ha sido configurada");
            respuestaError.put("estado", Map.of(
                    "configurada", false,
                    "finalizada", false,
                    "iteraciones", 0,
                    "fechaActual", null,
                    "fechaLimite", null,
                    "pedidosSemanales", 0,
                    "pedidosPorAtender", 0,
                    "pedidosPlanificados", 0,
                    "pedidosEntregados", 0));
            ejemplos.put("respuestaError", respuestaError);

            postmanInfo.put("ejemplos", ejemplos);

            response.put("success", true);
            response.put("postmanInfo", postmanInfo);
            response.put("message", "Información de Postman obtenida exitosamente");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Error obteniendo información de Postman: " + e.getMessage());
            e.printStackTrace();

            response.put("success", false);
            response.put("message", "Error interno: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}