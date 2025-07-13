package com.plg.controller;

import com.plg.utils.Simulacion;
import com.plg.dto.IndividuoDto;
import com.plg.dto.request.SimulacionRequest;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/simulacion")
@CrossOrigin(origins = "*") // O usa "http://localhost:5173" para mayor seguridad

public class SimulacionController {

    // Referencia al hilo actual de simulación para poder detenerlo
    private static Thread hiloSimulacionActual = null;

    /**
     * Detiene la simulación actual si está en progreso
     */
    private static void detenerSimulacionActual() {
        if (hiloSimulacionActual != null && hiloSimulacionActual.isAlive()) {
            System.out.println("🛑 Deteniendo simulación anterior...");
            hiloSimulacionActual.interrupt();
            try {
                // Esperar un poco para que el hilo termine
                hiloSimulacionActual.join(2000); // Esperar máximo 2 segundos
                if (hiloSimulacionActual.isAlive()) {
                    System.out.println("⚠️ El hilo de simulación no terminó completamente");
                } else {
                    System.out.println("✅ Simulación anterior detenida correctamente");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("❌ Error al esperar que termine la simulación anterior");
            }
        }
        hiloSimulacionActual = null;
    }

    /**
     * Método público para detener la simulación actual por averías.
     * Este método puede ser llamado desde otros servicios como AveriaService.
     */
    public static void detenerSimulacionPorAveria() {
        System.out.println("🚨 DETENER SIMULACIÓN POR AVERÍA");

        // Marcar la simulación como no en proceso
        com.plg.utils.simulacion.GestorHistorialSimulacion.setEnProceso(false);

        // Detener el hilo de simulación
        detenerSimulacionActual();

        System.out.println("✅ Simulación detenida por avería");
    }

    /**
     * Pausa la simulación actual sin detener completamente el hilo.
     * Este método puede ser llamado desde otros servicios como AveriaService.
     */
    public static void pausarSimulacionPorAveria() {
        System.out.println("⏸️ PAUSAR SIMULACIÓN POR AVERÍA");

        // Marcar la simulación como pausada
        com.plg.utils.simulacion.GestorHistorialSimulacion.setPausada(true);

        System.out.println("✅ Simulación pausada por avería");
    }

    /**
     * Reanuda la simulación después de una pausa.
     * Este método puede ser llamado desde otros servicios como AveriaService.
     */
    public static void reanudarSimulacionDespuesDeAveria() {
        System.out.println("▶️ REANUDAR SIMULACIÓN DESPUÉS DE AVERÍA");

        // Marcar la simulación como no pausada
        com.plg.utils.simulacion.GestorHistorialSimulacion.setPausada(false);

        System.out.println("✅ Simulación reanudada después de avería");
    }

    @GetMapping("/mejor/{fecha}")
    public IndividuoDto obtenerMejorIndividuo(@PathVariable String fecha) {
        System.out.println("🌐 ENDPOINT LLAMADO: /api/simulacion/mejor");
        // Este método debe aceptar @PathVariable String fecha como parámetro.
        // Ejemplo de firma correcta:
        // @GetMapping("/mejor/{fecha}")
        // public IndividuoDto obtenerMejorIndividuo(@PathVariable String fecha) { ... }

        LocalDateTime fechaSimulacion;
        try {
            fechaSimulacion = LocalDateTime.parse(fecha);
        } catch (Exception e) {
            System.err.println("❌ Error al parsear la fecha recibida: " + fecha);
            return null;
        }
        Simulacion.simularIntervalo(fechaSimulacion);

        IndividuoDto siguientePaquete = Simulacion.obtenerSiguientePaquete();

        // Ejemplo de uso desde el frontend o Postman:
        // GET http://localhost:8080/api/simulacion/mejor/2024-06-10T12:00:00

        if (siguientePaquete == null) {
            System.out.println("⏳ No hay paquetes disponibles, esperando...");
            // Si no hay más paquetes, esperar un poco por si se está generando uno nuevo
            try {
                Thread.sleep(100); // Espera breve
                siguientePaquete = Simulacion.obtenerSiguientePaquete();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            if (siguientePaquete == null) {
                System.out.println("❌ ENDPOINT RESPUESTA: null (sin paquetes disponibles)");
            }
        }

        if (siguientePaquete != null) {
            System.out.println("✅ ENDPOINT RESPUESTA: Paquete enviado al frontend");
        }

        return siguientePaquete;
    }

    @GetMapping("/reiniciar")
    public ResponseEntity<String> reiniciarSimulacion() {
        System.out.println("🌐 ENDPOINT LLAMADO: /api/simulacion/reiniciar");

        try {
            // Detener la simulación anterior si existe
            detenerSimulacionActual();

            // Limpiar completamente el historial para generar nueva simulación
            com.plg.utils.simulacion.GestorHistorialSimulacion.limpiarHistorialCompleto();
            System.out.println("🧹 Historial limpiado completamente");

            // Usar la fecha actual para reiniciar la simulación
            LocalDateTime fechaActual = LocalDateTime.now();
            System.out.println("🔧 Reiniciando simulación con fecha: " + fechaActual);

            // Configurar nueva simulación
            Simulacion.configurarSimulacion(fechaActual);

            // Ejecutar la nueva simulación en un hilo separado
            Thread nuevoHiloSimulacion = new Thread(() -> {
                try {
                    System.out.println("🚀 Iniciando nueva simulación después de reiniciar...");
                    Simulacion.ejecutarSimulacion();
                    System.out.println("✅ Nueva simulación completada exitosamente");
                } catch (Exception e) {
                    System.err.println("💥 Error durante la ejecución de la nueva simulación:");
                    System.err.println("   • Mensaje: " + e.getMessage());
                    System.err.println("   • Tipo: " + e.getClass().getSimpleName());
                    e.printStackTrace();
                }
            });

            nuevoHiloSimulacion.setName("SimulacionThread-Reinicio-" + fechaActual);
            nuevoHiloSimulacion.start();
            hiloSimulacionActual = nuevoHiloSimulacion;

            String mensaje = "Simulación reiniciada y nueva simulación generándose con fecha: " + fechaActual;
            System.out.println("✅ ENDPOINT RESPUESTA: " + mensaje);

            return ResponseEntity.ok(mensaje);

        } catch (Exception e) {
            String errorMsg = "Error al reiniciar simulación: " + e.getMessage();
            System.err.println("❌ " + errorMsg);
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMsg);
        }
    }

    @GetMapping("/info")
    public Simulacion.SimulacionInfo obtenerInfoSimulacion() {
        System.out.println("🌐 ENDPOINT LLAMADO: /api/simulacion/info");
        Simulacion.SimulacionInfo info = Simulacion.obtenerInfoSimulacion();
        System.out.println("✅ ENDPOINT RESPUESTA: Total=" + info.totalPaquetes +
                ", Actual=" + info.paqueteActual +
                ", EnProceso=" + info.enProceso);
        return info;
    }

    @PostMapping("/pedir_creacion_paquete")
    public ResponseEntity<String> pedirCreacionPaquete(@RequestBody SimulacionRequest request) {
        System.out.println("🌐 ENDPOINT LLAMADO: /api/simulacion/pedir_creacion_paquete");
        System.out.println("🌐 ENDPOINT LLAMADO: /api/simulacion/iniciar");
        System.out.println("📅 Fecha recibida: " + request.getFechaInicio());
        try {
            // Validar que la fecha no sea nula
            if (request.getFechaInicio() == null) {
                System.out.println("❌ Error: Fecha de inicio es nula");
                return ResponseEntity.badRequest().body("Error: La fecha de inicio no puede ser nula");
            }

            // Detener cualquier simulación anterior
            detenerSimulacionActual();
            System.out.println("🛑 Simulación anterior detenida (si existía)");

            // Verificar estado del sistema antes de iniciar
            System.out.println("🔍 DIAGNÓSTICO DEL SISTEMA:");
            System.out.println("   • Almacenes disponibles: " + com.plg.config.DataLoader.almacenes.size());
            System.out.println("   • Camiones disponibles: " + com.plg.config.DataLoader.camiones.size());
            System.out.println("   • Mapa inicializado: " + (com.plg.entity.Mapa.getInstance() != null));

            // Verificar camiones disponibles (no en mantenimiento)
            long camionesDisponibles = com.plg.config.DataLoader.camiones.stream()
                    .filter(camion -> camion.getEstado() != com.plg.entity.EstadoCamion.EN_MANTENIMIENTO_PREVENTIVO)
                    .count();
            System.out.println("   • Camiones no en mantenimiento: " + camionesDisponibles);

            if (camionesDisponibles == 0) {
                System.out.println("⚠️ ADVERTENCIA: Todos los camiones están en mantenimiento");
            }

            System.out.println("🔧 Configurando simulación con fecha: " + request.getFechaInicio());

            // Limpiar historial anterior antes de iniciar nueva simulación
            com.plg.utils.simulacion.GestorHistorialSimulacion.limpiarHistorialCompleto();

            // Configurar la simulación con la fecha enviada desde el frontend
            Simulacion.configurarSimulacion(request.getFechaInicio());

            // Ejecutar la simulación en un hilo separado para no bloquear la respuesta HTTP
            Thread simulacionThread = new Thread(() -> {
                try {
                    System.out.println("🚀 Iniciando simulación en hilo separado...");
                    Simulacion.ejecutarSimulacion();
                    System.out.println("✅ Simulación completada exitosamente");
                } catch (Exception e) {
                    System.err.println("💥 Error durante la ejecución de la simulación:");
                    System.err.println("   • Mensaje: " + e.getMessage());
                    System.err.println("   • Tipo: " + e.getClass().getSimpleName());
                    System.err.println("   • Stack trace completo:");
                    e.printStackTrace();
                }
            });

            simulacionThread.setName("SimulacionThread-" + request.getFechaInicio());
            simulacionThread.start();
            hiloSimulacionActual = simulacionThread;

            String mensaje = "Simulación iniciada correctamente con fecha: " + request.getFechaInicio();
            System.out.println("✅ ENDPOINT RESPUESTA: " + mensaje);

            return ResponseEntity.ok(mensaje);

        } catch (Exception e) {
            String errorMsg = "Error al pedir creación de paquete: " + e.getMessage();
            System.err.println("❌ " + errorMsg);
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMsg);
        }

    }

    @PostMapping("/iniciar")
    public ResponseEntity<String> iniciarSimulacion(@RequestBody SimulacionRequest request) {
        System.out.println("🌐 ENDPOINT LLAMADO: /api/simulacion/iniciar");
        System.out.println("📅 Fecha recibida: " + request.getFechaInicio());

        try {
            // Validar que la fecha no sea nula
            if (request.getFechaInicio() == null) {
                System.out.println("❌ Error: Fecha de inicio es nula");
                return ResponseEntity.badRequest().body("Error: La fecha de inicio no puede ser nula");
            }
            // Pedir mejor individuo
            Simulacion.simularIntervalo(request.getFechaInicio());
            
            // Detener cualquier simulación anterior
            //// detenerSimulacionActual();
            System.out.println("🛑 Simulación anterior detenida (si existía)");

            // Verificar estado del sistema antes de iniciar
            System.out.println("🔍 DIAGNÓSTICO DEL SISTEMA:");
            System.out.println("   • Almacenes disponibles: " + com.plg.config.DataLoader.almacenes.size());
            System.out.println("   • Camiones disponibles: " + com.plg.config.DataLoader.camiones.size());
            System.out.println("   • Mapa inicializado: " + (com.plg.entity.Mapa.getInstance() != null));

            // Verificar camiones disponibles (no en mantenimiento)
            long camionesDisponibles = com.plg.config.DataLoader.camiones.stream()
                    .filter(camion -> camion.getEstado() != com.plg.entity.EstadoCamion.EN_MANTENIMIENTO_PREVENTIVO)
                    .count();
            System.out.println("   • Camiones no en mantenimiento: " + camionesDisponibles);

            if (camionesDisponibles == 0) {
                System.out.println("⚠️ ADVERTENCIA: Todos los camiones están en mantenimiento");
            }

            System.out.println("🔧 Configurando simulación con fecha: " + request.getFechaInicio());

            // Limpiar historial anterior antes de iniciar nueva simulación
            com.plg.utils.simulacion.GestorHistorialSimulacion.limpiarHistorialCompleto();

            // Configurar la simulación con la fecha enviada desde el frontend
            Simulacion.configurarSimulacion(request.getFechaInicio());

            String mensaje = "Simulación iniciada correctamente con fecha: " + request.getFechaInicio();
            System.out.println("✅ ENDPOINT RESPUESTA: " + mensaje);

            return ResponseEntity.ok(mensaje);

        } catch (Exception e) {
            String errorMsg = "Error al iniciar simulación: " + e.getMessage();
            System.err.println("❌ " + errorMsg);
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMsg);
        }
    }

    @GetMapping("/pausar")
    public ResponseEntity<String> pausarSimulacion() {
        System.out.println("🌐 ENDPOINT LLAMADO: /api/simulacion/pausar");

        try {
            pausarSimulacionPorAveria();
            String mensaje = "Simulación pausada exitosamente";
            System.out.println("✅ ENDPOINT RESPUESTA: " + mensaje);
            return ResponseEntity.ok(mensaje);
        } catch (Exception e) {
            String errorMsg = "Error al pausar simulación: " + e.getMessage();
            System.err.println("❌ " + errorMsg);
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMsg);
        }
    }

    @GetMapping("/reanudar")
    public ResponseEntity<String> reanudarSimulacion() {
        System.out.println("🌐 ENDPOINT LLAMADO: /api/simulacion/reanudar");

        try {
            reanudarSimulacionDespuesDeAveria();
            String mensaje = "Simulación reanudada exitosamente";
            System.out.println("✅ ENDPOINT RESPUESTA: " + mensaje);
            return ResponseEntity.ok(mensaje);
        } catch (Exception e) {
            String errorMsg = "Error al reanudar simulación: " + e.getMessage();
            System.err.println("❌ " + errorMsg);
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMsg);
        }
    }

    @DeleteMapping("/eliminar-paquetes-futuros")
    public ResponseEntity<String> eliminarPaquetesFuturos() {
        System.out.println("🌐 ENDPOINT LLAMADO: /api/simulacion/eliminar-paquetes-futuros");

        try {
            // Obtener información actual antes de eliminar
            Simulacion.SimulacionInfo infoAntes = Simulacion.obtenerInfoSimulacion();
            System.out.println("📊 ANTES: Total=" + infoAntes.totalPaquetes +
                    ", Actual=" + infoAntes.paqueteActual);

            // Eliminar paquetes futuros (mantener solo el actual)
            System.out.println("🗑️ Eliminando paquetes futuros...");

            // Usar el método implementado en la clase Simulacion
            int paquetesEliminados = Simulacion.eliminarPaquetesFuturos();

            // Obtener información después de eliminar
            Simulacion.SimulacionInfo infoDespues = Simulacion.obtenerInfoSimulacion();
            System.out.println("📊 DESPUÉS: Total=" + infoDespues.totalPaquetes +
                    ", Actual=" + infoDespues.paqueteActual);

            String mensaje = "Paquetes futuros eliminados exitosamente. " +
                    "Paquetes eliminados: " + paquetesEliminados +
                    ". Total actual: " + infoDespues.totalPaquetes;

            System.out.println("✅ ENDPOINT RESPUESTA: " + mensaje);

            return ResponseEntity.ok(mensaje);

        } catch (Exception e) {
            String errorMsg = "Error al eliminar paquetes futuros: " + e.getMessage();
            System.err.println("❌ " + errorMsg);
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMsg);
        }
    }

    /*
     * ------------------------ NUEVOS ENDPOINTS PARA HISTORIAL DE CONSUMIDOS
     * ---------------------
     */

    @GetMapping("/historial-consumidos")
    public List<IndividuoDto> obtenerHistorialConsumidos() {
        System.out.println("🌐 ENDPOINT LLAMADO: /api/simulacion/historial-consumidos");
        List<IndividuoDto> historial = com.plg.utils.simulacion.GestorHistorialSimulacion.getHistorialConsumidos();
        System.out.println("✅ ENDPOINT RESPUESTA: " + historial.size() + " paquetes consumidos");
        return historial;
    }

    @GetMapping("/info-consumo")
    public ResponseEntity<String> obtenerInfoConsumo() {
        System.out.println("🌐 ENDPOINT LLAMADO: /api/simulacion/info-consumo");
        String info = com.plg.utils.simulacion.GestorHistorialSimulacion.obtenerInfoConsumo();
        System.out.println("✅ ENDPOINT RESPUESTA: " + info);
        return ResponseEntity.ok(info);
    }

    @GetMapping("/exportar-consumidos")
    public ResponseEntity<String> exportarHistorialConsumidos() {
        System.out.println("🌐 ENDPOINT LLAMADO: /api/simulacion/exportar-consumidos");
        String exportacion = com.plg.utils.simulacion.GestorHistorialSimulacion.exportarHistorialConsumidos();
        System.out.println("✅ ENDPOINT RESPUESTA: Historial exportado");
        return ResponseEntity.ok(exportacion);
    }

    @GetMapping("/ultimo-consumido")
    public ResponseEntity<IndividuoDto> obtenerUltimoPaqueteConsumido() {
        System.out.println("🌐 ENDPOINT LLAMADO: /api/simulacion/ultimo-consumido");
        IndividuoDto ultimo = com.plg.utils.simulacion.GestorHistorialSimulacion.obtenerUltimoPaqueteConsumido();
        if (ultimo != null) {
            System.out.println("✅ ENDPOINT RESPUESTA: Último paquete consumido encontrado");
            return ResponseEntity.ok(ultimo);
        } else {
            System.out.println("⚠️ ENDPOINT RESPUESTA: No hay paquetes consumidos");
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/consumido/{indice}")
    public ResponseEntity<IndividuoDto> obtenerPaqueteConsumidoPorIndice(@PathVariable int indice) {
        System.out.println("🌐 ENDPOINT LLAMADO: /api/simulacion/consumido/" + indice);
        IndividuoDto paquete = com.plg.utils.simulacion.GestorHistorialSimulacion
                .obtenerPaqueteConsumidoPorIndice(indice);
        if (paquete != null) {
            System.out.println("✅ ENDPOINT RESPUESTA: Paquete consumido #" + indice + " encontrado");
            return ResponseEntity.ok(paquete);
        } else {
            System.out.println("❌ ENDPOINT RESPUESTA: Paquete consumido #" + indice + " no encontrado");
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/total-consumidos")
    public ResponseEntity<Integer> obtenerTotalConsumidos() {
        System.out.println("🌐 ENDPOINT LLAMADO: /api/simulacion/total-consumidos");
        int total = com.plg.utils.simulacion.GestorHistorialSimulacion.getTotalConsumidos();
        System.out.println("✅ ENDPOINT RESPUESTA: Total consumidos = " + total);
        return ResponseEntity.ok(total);
    }

    @DeleteMapping("/limpiar-consumidos")
    public ResponseEntity<String> limpiarHistorialConsumidos() {
        System.out.println("🌐 ENDPOINT LLAMADO: /api/simulacion/limpiar-consumidos");

        try {
            int totalAntes = com.plg.utils.simulacion.GestorHistorialSimulacion.getTotalConsumidos();
            com.plg.utils.simulacion.GestorHistorialSimulacion.limpiarHistorialConsumidos();

            String mensaje = "Historial de consumidos limpiado exitosamente. Paquetes eliminados: " + totalAntes;
            System.out.println("✅ ENDPOINT RESPUESTA: " + mensaje);
            return ResponseEntity.ok(mensaje);

        } catch (Exception e) {
            String errorMsg = "Error al limpiar historial de consumidos: " + e.getMessage();
            System.err.println("❌ " + errorMsg);
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMsg);
        }
    }

    @GetMapping("/estadisticas-consumidos")
    public ResponseEntity<String> obtenerEstadisticasConsumidos() {
        System.out.println("🌐 ENDPOINT LLAMADO: /api/simulacion/estadisticas-consumidos");

        try {
            String estadisticas = com.plg.utils.simulacion.GestorHistorialSimulacion.obtenerEstadisticasConsumidos();
            System.out.println("✅ ENDPOINT RESPUESTA: Estadísticas generadas");
            return ResponseEntity.ok(estadisticas);

        } catch (Exception e) {
            String errorMsg = "Error al obtener estadísticas: " + e.getMessage();
            System.err.println("❌ " + errorMsg);
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMsg);
        }
    }
}
