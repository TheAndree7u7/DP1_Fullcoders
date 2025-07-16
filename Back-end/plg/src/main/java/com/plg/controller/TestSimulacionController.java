package com.plg.controller;

import com.plg.utils.Simulacion;
import com.plg.utils.AlgoritmoGenetico;
import com.plg.utils.Parametros;
import com.plg.dto.IndividuoDto;
import com.plg.dto.EstadisticasPedidosDto;
import com.plg.utils.simulacion.GestorHistorialSimulacion;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/test-simulacion")
@CrossOrigin(origins = "*")
public class TestSimulacionController {

    private static final AtomicBoolean simulacionEnProceso = new AtomicBoolean(false);
    private static final AtomicInteger contadorSimulaciones = new AtomicInteger(0);
    private static final List<String> historialLogs = new ArrayList<>();
    private static final List<EstadisticasPedidosDto> historialEstadisticas = new ArrayList<>();
    private static CompletableFuture<Void> simulacionFuture;

    /**
     * Ejecuta simulaciones en bucle entre dos fechas
     * 
     * @param fechaInicio      Fecha de inicio en formato ISO (yyyy-MM-ddTHH:mm:ss)
     * @param fechaFin         Fecha de fin en formato ISO (yyyy-MM-ddTHH:mm:ss)
     * @param intervaloMinutos Intervalo entre simulaciones en minutos (por defecto
     *                         30)
     * @return Respuesta con el estado de la ejecución
     */
    @PostMapping("/ejecutar-bucle")
    public ResponseEntity<String> ejecutarSimulacionBucle(
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin,
            @RequestParam(defaultValue = "30") int intervaloMinutos) {

        System.out.println("🌐 ENDPOINT LLAMADO: /api/test-simulacion/ejecutar-bucle");
        System.out.println("📅 Fecha inicio: " + fechaInicio);
        System.out.println("📅 Fecha fin: " + fechaFin);
        System.out.println("⏱️ Intervalo: " + intervaloMinutos + " minutos");

        // Validar que no haya una simulación en proceso
        if (simulacionEnProceso.get()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Ya hay una simulación en proceso. Espere a que termine o cancele la actual.");
        }

        try {
            // Parsear fechas
            LocalDateTime inicio = LocalDateTime.parse(fechaInicio);
            LocalDateTime fin = LocalDateTime.parse(fechaFin);

            // Validar que la fecha de inicio sea anterior a la de fin
            if (inicio.isAfter(fin)) {
                return ResponseEntity.badRequest()
                        .body("La fecha de inicio debe ser anterior a la fecha de fin.");
            }

            // Validar intervalo
            if (intervaloMinutos <= 0) {
                return ResponseEntity.badRequest()
                        .body("El intervalo debe ser mayor a 0 minutos.");
            }

            // Iniciar simulación en bucle de forma asíncrona
            simulacionEnProceso.set(true);
            contadorSimulaciones.set(0);
            historialLogs.clear();
            historialEstadisticas.clear();

            simulacionFuture = CompletableFuture.runAsync(() -> {
                ejecutarBucleSimulacion(inicio, fin, intervaloMinutos);
            });

            String mensaje = String.format(
                    "Simulación en bucle iniciada desde %s hasta %s con intervalo de %d minutos",
                    fechaInicio, fechaFin, intervaloMinutos);

            System.out.println("✅ " + mensaje);
            return ResponseEntity.ok(mensaje);

        } catch (Exception e) {
            simulacionEnProceso.set(false);
            String errorMsg = "Error al iniciar simulación en bucle: " + e.getMessage();
            System.err.println("❌ " + errorMsg);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMsg);
        }
    }

    /**
     * Cancela la simulación en bucle en proceso
     */
    @PostMapping("/cancelar")
    public ResponseEntity<String> cancelarSimulacion() {
        System.out.println("🌐 ENDPOINT LLAMADO: /api/test-simulacion/cancelar");

        if (!simulacionEnProceso.get()) {
            return ResponseEntity.ok("No hay simulación en proceso para cancelar.");
        }

        simulacionEnProceso.set(false);

        if (simulacionFuture != null && !simulacionFuture.isDone()) {
            simulacionFuture.cancel(true);
        }

        String mensaje = "Simulación en bucle cancelada. Total ejecutadas: " + contadorSimulaciones.get();
        System.out.println("🛑 " + mensaje);
        return ResponseEntity.ok(mensaje);
    }

    /**
     * Obtiene el estado actual de la simulación en bucle
     */
    @GetMapping("/estado")
    public ResponseEntity<String> obtenerEstado() {
        boolean enProceso = simulacionEnProceso.get();
        int totalEjecutadas = contadorSimulaciones.get();

        String estado = String.format(
                "En proceso: %s | Total simulaciones ejecutadas: %d",
                enProceso ? "SÍ" : "NO", totalEjecutadas);

        return ResponseEntity.ok(estado);
    }

    /**
     * Obtiene el historial de logs de la simulación en bucle
     */
    @GetMapping("/logs")
    public ResponseEntity<List<String>> obtenerLogs() {
        return ResponseEntity.ok(new ArrayList<>(historialLogs));
    }

    /**
     * Obtiene las estadísticas de pedidos de la simulación en bucle
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<List<EstadisticasPedidosDto>> obtenerEstadisticas() {
        return ResponseEntity.ok(new ArrayList<>(historialEstadisticas));
    }

    /**
     * Obtiene las estadísticas de pedidos de la última simulación ejecutada
     */
    @GetMapping("/estadisticas/ultima")
    public ResponseEntity<EstadisticasPedidosDto> obtenerUltimaEstadistica() {
        if (historialEstadisticas.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(historialEstadisticas.get(historialEstadisticas.size() - 1));
    }

    /**
     * Obtiene un resumen de todas las estadísticas de pedidos
     */
    @GetMapping("/estadisticas/resumen")
    public ResponseEntity<String> obtenerResumenEstadisticas() {
        if (historialEstadisticas.isEmpty()) {
            return ResponseEntity.ok("No hay estadísticas disponibles");
        }

        StringBuilder resumen = new StringBuilder();
        resumen.append("📊 RESUMEN DE ESTADÍSTICAS DE PEDIDOS\n");
        resumen.append("=====================================\n\n");

        int totalSimulaciones = historialEstadisticas.size();
        int totalPedidosAsignados = historialEstadisticas.stream()
                .mapToInt(EstadisticasPedidosDto::getTotalPedidosAsignados)
                .sum();
        int totalPedidosEntregados = historialEstadisticas.stream()
                .mapToInt(e -> e.getPedidosEntregadosCompletamente() + e.getPedidosEntregadosParcialmente())
                .sum();
        int totalPedidosNoEntregados = historialEstadisticas.stream()
                .mapToInt(EstadisticasPedidosDto::getPedidosNoEntregados)
                .sum();
        double volumenTotalAsignado = historialEstadisticas.stream()
                .mapToDouble(EstadisticasPedidosDto::getVolumenTotalAsignado)
                .sum();
        double volumenTotalEntregado = historialEstadisticas.stream()
                .mapToDouble(EstadisticasPedidosDto::getVolumenTotalEntregado)
                .sum();

        resumen.append(String.format("📈 TOTALES ACUMULADOS:\n"));
        resumen.append(String.format("   • Simulaciones ejecutadas: %d\n", totalSimulaciones));
        resumen.append(String.format("   • Pedidos asignados: %d\n", totalPedidosAsignados));
        resumen.append(String.format("   • Pedidos entregados: %d\n", totalPedidosEntregados));
        resumen.append(String.format("   • Pedidos no entregados: %d\n", totalPedidosNoEntregados));
        resumen.append(String.format("   • Volumen asignado: %.2f m³\n", volumenTotalAsignado));
        resumen.append(String.format("   • Volumen entregado: %.2f m³\n", volumenTotalEntregado));
        resumen.append(String.format("   • Porcentaje entrega global: %.2f%%\n\n",
                volumenTotalAsignado > 0 ? (volumenTotalEntregado / volumenTotalAsignado) * 100 : 0));

        resumen.append("📋 DETALLE POR SIMULACIÓN:\n");
        resumen.append("========================\n");
        for (int i = 0; i < historialEstadisticas.size(); i++) {
            EstadisticasPedidosDto stats = historialEstadisticas.get(i);
            resumen.append(String.format("\n%d. %s\n", i + 1, stats.toString()));
        }

        return ResponseEntity.ok(resumen.toString());
    }

    /**
     * Ejecuta el bucle de simulaciones
     */
    private void ejecutarBucleSimulacion(LocalDateTime fechaInicio, LocalDateTime fechaFin, int intervaloMinutos) {
        LocalDateTime fechaActual = fechaInicio;

        try {
            // Configurar simulación inicial
            Simulacion.configurarSimulacionSemanal(fechaInicio);
            agregarLog("🚀 Simulación en bucle iniciada");
            agregarLog("📅 Rango: " + fechaInicio + " hasta " + fechaFin);
            agregarLog("⏱️ Intervalo: " + intervaloMinutos + " minutos");

            while (fechaActual.isBefore(fechaFin) && simulacionEnProceso.get()) {
                try {
                    agregarLog("🔄 Ejecutando simulación para: " + fechaActual);

                    // Actualizar parámetros globales
                    Parametros.diferenciaTiempoMinRequest = (int) ChronoUnit.MINUTES.between(Parametros.fecha_inicial,
                            fechaActual);
                    Parametros.actualizarParametrosGlobales(fechaActual);
                    Simulacion.actualizarEstadoGlobal(fechaActual);

                    // Ejecutar algoritmo genético
                    AlgoritmoGenetico algoritmoGenetico = new AlgoritmoGenetico(com.plg.entity.Mapa.getInstance());
                    algoritmoGenetico.ejecutarAlgoritmo();

                    // Crear y guardar individuo
                    IndividuoDto mejorIndividuoDto = new IndividuoDto(
                            algoritmoGenetico.getMejorIndividuo(),
                            Simulacion.pedidosEnviar,
                            Simulacion.bloqueosActivos,
                            fechaActual);

                    // Agregar al historial
                    GestorHistorialSimulacion.agregarPaquete(mejorIndividuoDto);

                    // Calcular y guardar estadísticas de pedidos
                    EstadisticasPedidosDto estadisticas = new EstadisticasPedidosDto(Simulacion.pedidosEnviar,
                            fechaActual);
                    historialEstadisticas.add(estadisticas);

                    // Desactivar bloqueos
                    for (com.plg.entity.Bloqueo bloqueo : Simulacion.bloqueosActivos) {
                        bloqueo.desactivarBloqueo();
                    }

                    contadorSimulaciones.incrementAndGet();
                    agregarLog("✅ Simulación #" + contadorSimulaciones.get() + " completada para: " + fechaActual);
                    agregarLog("📦 Pedidos procesados: " + Simulacion.pedidosEnviar.size());
                    agregarLog("🧬 Fitness: " + algoritmoGenetico.getMejorIndividuo().getFitness());
                    agregarLog("📊 " + estadisticas.toString());

                } catch (Exception e) {
                    agregarLog("❌ Error en simulación para " + fechaActual + ": " + e.getMessage());
                    System.err.println("Error en simulación: " + e.getMessage());
                    e.printStackTrace();
                }

                // Avanzar al siguiente intervalo
                fechaActual = fechaActual.plusMinutes(intervaloMinutos);

                // Pequeña pausa para no saturar el sistema
                Thread.sleep(100);
            }

            if (simulacionEnProceso.get()) {
                agregarLog("🎉 Simulación en bucle completada exitosamente");
                agregarLog("📊 Total simulaciones ejecutadas: " + contadorSimulaciones.get());
            } else {
                agregarLog("🛑 Simulación en bucle cancelada por el usuario");
                agregarLog("📊 Simulaciones ejecutadas antes de cancelar: " + contadorSimulaciones.get());
            }

        } catch (Exception e) {
            agregarLog("💥 Error crítico en bucle de simulación: " + e.getMessage());
            System.err.println("Error crítico en bucle: " + e.getMessage());
            e.printStackTrace();
        } finally {
            simulacionEnProceso.set(false);
        }
    }

    /**
     * Agrega un log al historial
     */
    private void agregarLog(String mensaje) {
        String timestamp = LocalDateTime.now().toString();
        String logEntry = "[" + timestamp + "] " + mensaje;
        historialLogs.add(logEntry);
        System.out.println(logEntry);

        // Mantener solo los últimos 1000 logs
        if (historialLogs.size() > 1000) {
            historialLogs.remove(0);
        }
    }
}