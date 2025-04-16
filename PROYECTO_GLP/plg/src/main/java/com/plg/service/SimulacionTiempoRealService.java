package com.plg.service;

import com.plg.entity.Almacen;
import com.plg.entity.Camion;
import com.plg.entity.Pedido;
import com.plg.repository.AlmacenRepository;
import com.plg.repository.CamionRepository;
import com.plg.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SimulacionTiempoRealService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private PedidoRepository pedidoRepository;
    
    @Autowired
    private CamionRepository camionRepository;
    
    @Autowired
    private AlmacenRepository almacenRepository;
    
    private boolean simulacionActiva = false;
    private LocalDateTime tiempoSimulado = LocalDateTime.now();
    private int factorVelocidad = 60; // Simulación a 60x por defecto
    
    /**
     * Inicia la simulación en tiempo real
     */
    public Map<String, Object> iniciarSimulacion() {
        simulacionActiva = true;
        tiempoSimulado = LocalDateTime.now(); // Reiniciar tiempo
        
        Map<String, Object> response = new HashMap<>();
        response.put("activa", simulacionActiva);
        response.put("tiempo", formatearTiempo(tiempoSimulado));
        
        return response;
    }
    
    /**
     * Detiene la simulación en tiempo real
     */
    public Map<String, Object> detenerSimulacion() {
        simulacionActiva = false;
        
        Map<String, Object> response = new HashMap<>();
        response.put("activa", simulacionActiva);
        response.put("tiempo", formatearTiempo(tiempoSimulado));
        
        return response;
    }
    
    /**
     * Obtiene el estado actual de la simulación
     */
    public Map<String, Object> obtenerEstadoSimulacion() {
        Map<String, Object> estado = new HashMap<>();
        estado.put("activa", simulacionActiva);
        estado.put("tiempo", formatearTiempo(tiempoSimulado));
        estado.put("factorVelocidad", factorVelocidad);
        
        return estado;
    }
    
    /**
     * Ajusta la velocidad de la simulación
     */
    public Map<String, Object> ajustarVelocidad(int factor) {
        this.factorVelocidad = factor;
        
        Map<String, Object> response = new HashMap<>();
        response.put("factorVelocidad", factorVelocidad);
        
        return response;
    }
    
    /**
     * Actualiza la simulación periódicamente 
     * y envía los datos actualizados a través de WebSocket
     */
    @Scheduled(fixedRate = 1000) // Actualizar cada segundo
    public void actualizarSimulacion() {
        if (!simulacionActiva) {
            return;
        }
        
        // Avanzar el tiempo simulado según el factor de velocidad
        tiempoSimulado = tiempoSimulado.plusSeconds(factorVelocidad);
        
        // Obtener datos actualizados de la simulación
        Map<String, Object> datosActualizados = generarDatosSimulacion();
        
        // Enviar datos a través de WebSocket
        messagingTemplate.convertAndSend("/topic/simulacion", datosActualizados);
    }
    
    /**
     * Genera los datos actualizados para enviar al cliente
     */
    private Map<String, Object> generarDatosSimulacion() {
        Map<String, Object> datos = new HashMap<>();
        
        // Información de tiempo
        datos.put("tiempo", formatearTiempo(tiempoSimulado));
        
        // Obtener estadísticas de pedidos y camiones
        datos.put("estadisticas", obtenerEstadisticas());
        
        // Obtener posiciones actuales de elementos en el mapa
        datos.put("posiciones", obtenerPosiciones());
        
        return datos;
    }
    
    /**
     * Obtiene estadísticas generales para la simulación
     */
    private Map<String, Object> obtenerEstadisticas() {
        Map<String, Object> estadisticas = new HashMap<>();
        
        // Estadísticas de pedidos
        List<Pedido> pedidos = pedidoRepository.findAll();
        estadisticas.put("totalPedidos", pedidos.size());
        estadisticas.put("pedidosPendientes", pedidos.stream().filter(p -> p.getEstado() == 0).count());
        estadisticas.put("pedidosAsignados", pedidos.stream().filter(p -> p.getEstado() == 1).count());
        estadisticas.put("pedidosEnRuta", pedidos.stream().filter(p -> p.getEstado() == 2).count());
        estadisticas.put("pedidosEntregados", pedidos.stream().filter(p -> p.getEstado() == 3).count());
        
        // Estadísticas de camiones
        List<Camion> camiones = camionRepository.findAll();
        estadisticas.put("totalCamiones", camiones.size());
        estadisticas.put("camionesDisponibles", camiones.stream().filter(c -> c.getEstado() == 0).count());
        estadisticas.put("camionesEnRuta", camiones.stream().filter(c -> c.getEstado() == 1).count());
        estadisticas.put("camionesEnMantenimiento", camiones.stream().filter(c -> c.getEstado() == 2).count());
        estadisticas.put("camionesAveriados", camiones.stream().filter(c -> c.getEstado() == 3).count());
        
        // Estadísticas de almacenes
        List<Almacen> almacenes = almacenRepository.findByActivo(true);
        estadisticas.put("totalAlmacenes", almacenes.size());
        estadisticas.put("almacenesCentrales", almacenes.stream().filter(Almacen::isEsCentral).count());
        estadisticas.put("almacenesIntermedios", almacenes.stream().filter(a -> !a.isEsCentral()).count());
        
        // Capacidad total y actual de GLP
        double capacidadTotalGLP = almacenes.stream().mapToDouble(Almacen::getCapacidadGLP).sum();
        double capacidadActualGLP = almacenes.stream().mapToDouble(Almacen::getCapacidadActualGLP).sum();
        estadisticas.put("capacidadTotalGLP", capacidadTotalGLP);
        estadisticas.put("capacidadActualGLP", capacidadActualGLP);
        estadisticas.put("porcentajeOcupacionGLP", 
                capacidadTotalGLP > 0 ? (capacidadActualGLP / capacidadTotalGLP) * 100 : 0);
        
        // Estadísticas de eficiencia (simuladas)
        estadisticas.put("tiempoPromedioEntrega", calcularTiempoPromedioEntrega());
        estadisticas.put("eficienciaEntrega", calcularEficienciaEntrega());
        estadisticas.put("consumoTotalCombustible", calcularConsumoTotalCombustible());
        
        return estadisticas;
    }
    
    /**
     * Obtiene las posiciones actuales de elementos para mostrar en el mapa
     */
    private Map<String, Object> obtenerPosiciones() {
        Map<String, Object> posiciones = new HashMap<>();
        
        // Obtener posiciones de almacenes
        List<Almacen> almacenes = almacenRepository.findByActivo(true);
        posiciones.put("almacenes", convertirAlmacenesAMapa(almacenes));
        
        // Obtener posiciones de camiones (en un sistema real, estas vendrían de un servicio GPS)
        List<Camion> camiones = camionRepository.findAll();
        posiciones.put("camiones", convertirCamionesAMapa(camiones));
        
        // Obtener posiciones de pedidos pendientes y en curso
        List<Pedido> pedidos = pedidoRepository.findAll().stream()
                .filter(p -> p.getEstado() < 3) // Solo pendientes, asignados o en ruta
                .collect(Collectors.toList());
        posiciones.put("pedidos", convertirPedidosAMapa(pedidos));
        
        return posiciones;
    }
    
    /**
     * Convierte una lista de almacenes a formato de mapa
     */
    private List<Map<String, Object>> convertirAlmacenesAMapa(List<Almacen> almacenes) {
        return almacenes.stream()
                .map(this::convertirAlmacenAMapa)
                .collect(Collectors.toList());
    }
    
    /**
     * Convierte un almacén a formato de mapa
     */
    private Map<String, Object> convertirAlmacenAMapa(Almacen almacen) {
        Map<String, Object> mapa = new HashMap<>();
        mapa.put("id", almacen.getId());
        mapa.put("nombre", almacen.getNombre());
        mapa.put("posX", almacen.getPosX());
        mapa.put("posY", almacen.getPosY());
        mapa.put("esCentral", almacen.isEsCentral());
        mapa.put("capacidadGLP", almacen.getCapacidadGLP());
        mapa.put("capacidadActualGLP", almacen.getCapacidadActualGLP());
        
        // Calcular porcentaje de ocupación
        double porcentajeGLP = 0;
        if (almacen.getCapacidadGLP() > 0) {
            porcentajeGLP = (almacen.getCapacidadActualGLP() / almacen.getCapacidadGLP()) * 100;
        }
        mapa.put("porcentajeGLP", porcentajeGLP);
        
        return mapa;
    }
    
    /**
     * Convierte una lista de camiones a formato de mapa
     */
    private List<Map<String, Object>> convertirCamionesAMapa(List<Camion> camiones) {
        return camiones.stream()
                .map(this::convertirCamionAMapa)
                .collect(Collectors.toList());
    }
    
    /**
     * Convierte un camión a formato de mapa
     */
    private Map<String, Object> convertirCamionAMapa(Camion camion) {
        Map<String, Object> mapa = new HashMap<>();
        mapa.put("codigo", camion.getCodigo());
        mapa.put("tipo", camion.getTipo());
        mapa.put("estado", camion.getEstado());
        mapa.put("capacidad", camion.getCapacidad());
        
        // Simular posición del camión (en un sistema real obtendríamos estos datos del GPS)
        // Si el camión está en un almacén, usamos las coordenadas del almacén
        if (camion.getUltimoAlmacen() != null) {
            mapa.put("posX", camion.getUltimoAlmacen().getPosX());
            mapa.put("posY", camion.getUltimoAlmacen().getPosY());
        } else {
            // De lo contrario, usar la posición almacenada del camión o una simulada
            mapa.put("posX", camion.getPosX());
            mapa.put("posY", camion.getPosY());
        }
        
        return mapa;
    }
    
    /**
     * Convierte una lista de pedidos a formato de mapa
     */
    private List<Map<String, Object>> convertirPedidosAMapa(List<Pedido> pedidos) {
        return pedidos.stream()
                .map(this::convertirPedidoAMapa)
                .collect(Collectors.toList());
    }
    
    /**
     * Convierte un pedido a formato de mapa
     */
    private Map<String, Object> convertirPedidoAMapa(Pedido pedido) {
        Map<String, Object> mapa = new HashMap<>();
        mapa.put("id", pedido.getId());
        mapa.put("estado", pedido.getEstado());
        mapa.put("posX", pedido.getPosX());
        mapa.put("posY", pedido.getPosY());
        mapa.put("m3", pedido.getM3());
        
        if (pedido.getCliente() != null) {
            mapa.put("cliente", pedido.getCliente().getId());
        }
        
        return mapa;
    }
    
    /**
     * Formatea un LocalDateTime para mostrarlo en la simulación
     */
    private String formatearTiempo(LocalDateTime tiempo) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return tiempo.format(formatter);
    }
    
    // Métodos simulados para estadísticas (en un sistema real estos datos provendrían de cálculos reales)
    
    private double calcularTiempoPromedioEntrega() {
        List<Pedido> pedidosEntregados = pedidoRepository.findAll().stream()
                .filter(p -> p.getEstado() == 3) // Solo entregados
                .collect(Collectors.toList());
        
        if (pedidosEntregados.isEmpty()) {
            return 0;
        }
        
        // En un sistema real, calcularíamos basados en fechas reales de entrega y fecha de pedido
        return 120.0; // Valor simulado: 120 minutos
    }
    
    private double calcularEficienciaEntrega() {
        // En un sistema real, calcularíamos basados en tiempos estimados vs. tiempos reales
        return 85.5; // Valor simulado: 85.5%
    }
    
    private double calcularConsumoTotalCombustible() {
        // En un sistema real, calcularíamos basados en consumo registrado
        List<Camion> camiones = camionRepository.findAll();
        return camiones.size() * 25.0; // Valor simulado: 25 galones por camión
    }
}