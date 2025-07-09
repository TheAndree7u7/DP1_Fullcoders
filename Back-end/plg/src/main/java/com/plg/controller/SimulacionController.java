package com.plg.controller;

import com.plg.utils.Simulacion;
import com.plg.utils.AlgoritmoGenetico;
import com.plg.utils.Parametros;
import com.plg.config.DataLoader;
import com.plg.dto.IndividuoDto;
import com.plg.dto.request.SimulacionRequest;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import com.plg.entity.Bloqueo;
import com.plg.entity.Mapa;
import com.plg.entity.Pedido;
import com.plg.utils.simulacion.UtilesSimulacion;
import java.util.LinkedHashSet;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;



@RestController
@RequestMapping("/api/simulacion")
@CrossOrigin(origins = "*") // O usa "http://localhost:5173" para mayor seguridad

public class SimulacionController {

    // Variable de control para verificar si la simulación ha sido iniciada
    private static boolean simulacionIniciada = false;


    @GetMapping("/mejor")
    public IndividuoDto obtenerMejorIndividuoPorFecha(@RequestParam String fecha) {
        System.out.println("🌐 ENDPOINT LLAMADO: /api/simulacion/mejor (por fecha)");
        
        // Verificar si la simulación ha sido iniciada
        if (!simulacionIniciada) {
            System.out.println("❌ Error: No se puede obtener el mejor individuo sin antes iniciar la simulación");
            throw new IllegalStateException("La simulación debe ser iniciada antes de poder obtener el mejor individuo. Llame primero al endpoint /api/simulacion/iniciar");
        }
        
        if (fecha == null || fecha.isEmpty()) {
            System.out.println("❌ Error: Fecha no proporcionada en la solicitud");
            return null;
        }
        
        LocalDateTime fechaDateTime;
        try {
            // Parsear la fecha del parámetro
            fechaDateTime = LocalDateTime.parse(fecha);
        } catch (Exception e) {
            System.out.println("❌ Error: Formato de fecha inválido: " + fecha);
            throw new IllegalArgumentException("Formato de fecha inválido. Use el formato ISO: yyyy-MM-ddTHH:mm:ss");
        }
        // Calcular el intervalo de tiempo en minutos entre la fecha inicial y la fecha actual
        Parametros.intervaloTiempo = (int) ChronoUnit.MINUTES.between(Parametros.fecha_inicial, fechaDateTime);
        Parametros.fecha_inicial = fechaDateTime; // Actualizar la fecha inicial de la simulación
        System.out.println("🔄 Actualizando estado global para la fecha: " + fechaDateTime);
        // Obtener pedidos en el rango de dos horas y unir con pedidos planificados
        List<Pedido> pedidosAT = Simulacion.obtenerPedidosEnRango(fechaDateTime);
        List<Pedido> pedidosEnviar = UtilesSimulacion.unirPedidosSinRepetidos(
                Simulacion.pedidosPlanificados,
                new LinkedHashSet<>(pedidosAT));
        Simulacion.pedidosEnviar = pedidosEnviar; // Actualizar la lista de pedidos a enviar
        Simulacion.actualizarEstadoGlobal(fechaDateTime);
        List<Bloqueo> bloqueosActivos = Simulacion.actualizarBloqueos(fechaDateTime);

        System.out.println("🧩 Pedidos a enviar unidos para la fecha: " + pedidosEnviar.size());
        System.out.println("🧬 Ejecutando algoritmo genético para la fecha: " + fechaDateTime);
        AlgoritmoGenetico algoritmoGenetico = new AlgoritmoGenetico(Mapa.getInstance(), pedidosEnviar);
        algoritmoGenetico.ejecutarAlgoritmo();
        IndividuoDto mejorIndividuoDto = new IndividuoDto(
                algoritmoGenetico.getMejorIndividuo(),
                pedidosEnviar,
                bloqueosActivos,
                fechaDateTime);

        for (Bloqueo bloqueo : bloqueosActivos) {
            bloqueo.desactivarBloqueo();
        }

        System.out.println("✅ Mejor individuo generado y retornado para la fecha: " + fechaDateTime);
        return mejorIndividuoDto;
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
            Simulacion.detenerSimulacion();
            
            // Resetear el estado de simulación iniciada
            simulacionIniciada = false;

            System.out.println("🛑 Simulación anterior detenida (si existía)");

            // Verificar estado del sistema antes de iniciar
            System.out.println("🔍 DIAGNÓSTICO DEL SISTEMA:");
            System.out.println("   • Almacenes disponibles: " + DataLoader.almacenes.size());
            System.out.println("   • Camiones disponibles: " + DataLoader.camiones.size());
            System.out.println("   • Mapa inicializado: " + (Mapa.getInstance() != null));

            // Verificar camiones disponibles (no en mantenimiento)
            long camionesDisponibles = DataLoader.camiones.stream()
                    .filter(camion -> camion.getEstado() != com.plg.entity.EstadoCamion.EN_MANTENIMIENTO_PREVENTIVO)
                    .count();
            System.out.println("   • Camiones no en mantenimiento: " + camionesDisponibles);

            if (camionesDisponibles == 0) {
                System.out.println("⚠️ ADVERTENCIA: Todos los camiones están en mantenimiento");
            }
            System.out.println("🔧 Configurando simulación con fecha: " + request.getFechaInicio());
            // Configurar la simulación con la fecha enviada desde el frontend
            Simulacion.configurarSimulacion(request.getFechaInicio());
            
            // Marcar que la simulación ha sido iniciada exitosamente
            simulacionIniciada = true;
            
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

    @GetMapping("/estado")
    public ResponseEntity<String> obtenerEstadoSimulacion() {
        System.out.println("🌐 ENDPOINT LLAMADO: /api/simulacion/estado");
        
        if (simulacionIniciada) {
            return ResponseEntity.ok("Simulación iniciada y lista para procesar solicitudes");
        } else {
            return ResponseEntity.ok("Simulación no iniciada. Debe llamar al endpoint /iniciar primero");
        }
    }
}
