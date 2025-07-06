package com.plg.controller;

import com.plg.utils.Simulacion;
import com.plg.utils.AlgoritmoGenetico;
import com.plg.dto.IndividuoDto;
import com.plg.dto.request.SimulacionRequest;
import com.plg.dto.request.MejorIndividuoRequest;
import java.time.LocalDateTime;
import java.util.List;
import com.plg.entity.Bloqueo;
import com.plg.entity.Mapa;
import com.plg.entity.Pedido;
import com.plg.utils.simulacion.UtilesSimulacion;
import java.util.LinkedHashSet;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;



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

    @PostMapping("/mejor")
    public IndividuoDto obtenerMejorIndividuoPorFecha(@RequestBody MejorIndividuoRequest request) {
        System.out.println("🌐 ENDPOINT LLAMADO: /api/simulacion/mejor (por fecha)");
        if (request == null || request.getFecha() == null) {
            System.out.println("❌ Error: Fecha no proporcionada en la solicitud");
            return null;
        }
        LocalDateTime fecha = request.getFecha();
        System.out.println("🔄 Actualizando estado global para la fecha: " + fecha);

        // Obtener pedidos en el rango de dos horas y unir con pedidos planificados
        List<Pedido> pedidosAT = Simulacion.obtenerPedidosEnRango(fecha);
        List<Pedido> pedidosEnviar = UtilesSimulacion.unirPedidosSinRepetidos(
                Simulacion.pedidosPlanificados,
                new LinkedHashSet<>(pedidosAT));
        Simulacion.pedidosEnviar = pedidosEnviar; // Actualizar la lista de pedidos a enviar
        Simulacion.actualizarEstadoGlobal(fecha);
        List<Bloqueo> bloqueosActivos = Simulacion.actualizarBloqueos(fecha);

        System.out.println("🧩 Pedidos a enviar unidos para la fecha: " + pedidosEnviar.size());
        System.out.println("🧬 Ejecutando algoritmo genético para la fecha: " + fecha);
        AlgoritmoGenetico algoritmoGenetico = new AlgoritmoGenetico(Mapa.getInstance(), pedidosEnviar);
        IndividuoDto mejorIndividuoDto = new IndividuoDto(
                algoritmoGenetico.getMejorIndividuo(),
                pedidosEnviar,
                bloqueosActivos,
                fecha);

        for (Bloqueo bloqueo : bloqueosActivos) {
            bloqueo.desactivarBloqueo();
        }

        System.out.println("✅ Mejor individuo generado y retornado para la fecha: " + fecha);
        return mejorIndividuoDto;
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
            String errorMsg = "Error al iniciar simulación: " + e.getMessage();
            System.err.println("❌ " + errorMsg);
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMsg);
        }
    }
}
