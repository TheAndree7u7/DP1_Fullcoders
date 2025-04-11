package com.plg.service;

import com.plg.entity.Almacen;
import com.plg.entity.Camion;
import com.plg.entity.Pedido;
import com.plg.repository.AlmacenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import com.plg.repository.CamionRepository;
import com.plg.repository.PedidoRepository;
@Service
public class SimulacionTiempoRealService {

    @Autowired
    private SimulacionService simulacionService;
    
    @Autowired
    private AlmacenRepository almacenRepository;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private CamionRepository camionRepository;
    @Autowired
    private PedidoRepository pedidoRepository;
    private AtomicBoolean simulacionActiva = new AtomicBoolean(false);
    private LocalDateTime tiempoSimulacion = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
    
    // Factor de aceleración: cuántas veces más rápido corre el tiempo simulado
    private int factorAceleracion = 60; // 1 minuto real = 60 minutos simulados
    
    /**
     * Inicia la simulación en tiempo real
     */
    public void iniciarSimulacion() {
        if (!simulacionActiva.getAndSet(true)) {
            tiempoSimulacion = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            simulacionService.inicializarSimulacion();
            System.out.println("Simulación en tiempo real iniciada a las " + tiempoSimulacion);
        }
    }
    
    /**
     * Detiene la simulación en tiempo real
     */
    public void detenerSimulacion() {
        simulacionActiva.set(false);
        System.out.println("Simulación en tiempo real detenida");
    }
    
    /**
     * Ajusta el factor de aceleración del tiempo
     */
    public void ajustarFactorAceleracion(int factor) {
        this.factorAceleracion = factor;
        System.out.println("Factor de aceleración ajustado a: " + factor);
    }
    
    /**
     * Ejecuta cada segundo para actualizar la simulación
     */
    @Scheduled(fixedRate = 1000) // Ejecuta cada segundo
    public void actualizarSimulacion() {
        if (simulacionActiva.get()) {
            // Avanzar el tiempo según el factor de aceleración
            tiempoSimulacion = tiempoSimulacion.plusMinutes(factorAceleracion);
            
            // Verificar reabastecimiento de almacenes intermedios
            LocalTime horaActual = tiempoSimulacion.toLocalTime();
            verificarReabastecimientoAlmacenes(horaActual);
            
            // Ejecutar un paso de simulación
            simulacionService.ejecutarPasoSimulacion(tiempoSimulacion);
            
            // Emitir eventos a los clientes conectados
            notificarClientes();
            
            // Si llegamos al final del día, podemos reiniciar o detener
            if (horaActual.isAfter(LocalTime.of(23, 59))) {
                tiempoSimulacion = tiempoSimulacion.plusDays(1).withHour(0).withMinute(0).withSecond(0);
                System.out.println("Nuevo día de simulación: " + tiempoSimulacion.toLocalDate());
            }
        }
    }
    
    /**
     * Verifica si es hora de reabastecer algún almacén intermedio
     */
    private void verificarReabastecimientoAlmacenes(LocalTime horaActual) {
        almacenRepository.findByEsCentralAndActivo(false, true).forEach(almacen -> {
            LocalTime horaReabastecimiento = almacen.getHoraReabastecimiento();
            
            // Verificar si es hora de reabastecer (con margen de ±factorAceleracion minutos)
            if (Math.abs(horaActual.toSecondOfDay() - horaReabastecimiento.toSecondOfDay()) < factorAceleracion * 60) {
                reabastecerAlmacen(almacen);
            }
        });
    }
    
    /**
     * Reabastece un almacén
     */
    private void reabastecerAlmacen(Almacen almacen) {
        almacen.reabastecer();
        almacenRepository.save(almacen);
        System.out.println(tiempoSimulacion + " - Reabastecido almacén: " + almacen.getNombre());
    }
    
    /**
     * Notifica a los clientes conectados sobre el estado actual
     */
    private void notificarClientes() {
        Map<String, Object> estadoActual = simulacionService.ejecutarPasoSimulacion(tiempoSimulacion);
        
        // Añadir información de posiciones para el mapa
        Map<String, Object> posiciones = new HashMap<>();
        
        // Obtener posiciones de almacenes
        List<Almacen> almacenes = almacenRepository.findByActivo(true);
        List<Map<String, Object>> posicionesAlmacenes = new ArrayList<>();
        
        for (Almacen almacen : almacenes) {
            Map<String, Object> pos = new HashMap<>();
            pos.put("id", almacen.getId());
            pos.put("nombre", almacen.getNombre());
            pos.put("posX", almacen.getPosX());
            pos.put("posY", almacen.getPosY());
            pos.put("esCentral", almacen.isEsCentral());
            posicionesAlmacenes.add(pos);
        }
        posiciones.put("almacenes", posicionesAlmacenes);
        
        // Obtener posiciones de camiones
        List<Camion> camiones = camionRepository.findByEstadoNot(0); // Todos menos los disponibles
        List<Map<String, Object>> posicionesCamiones = new ArrayList<>();
        
        for (Camion camion : camiones) {
            Map<String, Object> pos = new HashMap<>();
            pos.put("codigo", camion.getCodigo());
            pos.put("posX", camion.getPosX());
            pos.put("posY", camion.getPosY());
            pos.put("estado", camion.getEstado());
            posicionesCamiones.add(pos);
        }
        posiciones.put("camiones", posicionesCamiones);
        
        // Obtener posiciones de pedidos
        List<Pedido> pedidos = pedidoRepository.findByEstado(1); // Pedidos asignados
        List<Map<String, Object>> posicionesPedidos = new ArrayList<>();
        
        for (Pedido pedido : pedidos) {
            Map<String, Object> pos = new HashMap<>();
            pos.put("id", pedido.getId());
            pos.put("posX", pedido.getPosX());
            pos.put("posY", pedido.getPosY());
            posicionesPedidos.add(pos);
        }
        posiciones.put("pedidos", posicionesPedidos);
        
        // Agregar las posiciones al estado
        estadoActual.put("posiciones", posiciones);
        
        // Enviar el estado completo por WebSocket
        messagingTemplate.convertAndSend("/topic/simulacion", estadoActual);
    }
    
    /**
     * Obtiene el tiempo actual de la simulación
     */
    public LocalDateTime getTiempoSimulacion() {
        return tiempoSimulacion;
    }
    
    /**
     * Verifica si la simulación está activa
     */
    public boolean isSimulacionActiva() {
        return simulacionActiva.get();
    }
}