package com.plg.service;

import com.plg.entity.Averia;
import com.plg.entity.Bloqueo;
import com.plg.entity.Camion;
import com.plg.entity.Pedido;
import com.plg.repository.CamionRepository;
import com.plg.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class SimulacionService {

    @Autowired
    private PedidoRepository pedidoRepository;
    
    @Autowired
    private CamionRepository camionRepository;
    
    @Autowired
    private RutaService rutaService;

    /**
     * Simula un escenario diario con pedidos, rutas y posibles averías
     */
    public Map<String, Object> simularEscenarioDiario() {
        Map<String, Object> resultado = new HashMap<>();
        
        // 1. Generamos pedidos aleatorios para el día
        List<Pedido> pedidosGenerados = generarPedidosAleatorios(15, 30); // Entre 15 y 30 pedidos
        resultado.put("pedidosGenerados", pedidosGenerados.size());
        
        // 2. Asignamos camiones y simulamos rutas
        Map<String, Object> asignaciones = asignarCamionesYRutas(pedidosGenerados);
        resultado.put("camionesAsignados", asignaciones.get("camionesAsignados"));
        resultado.put("rutasGeneradas", asignaciones.get("rutas"));
        
        // 3. Simular posibles eventos aleatorios (averías, bloqueos)
        List<Object> eventos = simularEventosAleatorios();
        resultado.put("eventosGenerados", eventos);
        
        // 4. Calcular métricas de la simulación
        Map<String, Object> metricas = calcularMetricasSimulacion();
        resultado.put("metricas", metricas);
        
        return resultado;
    }
    
    /**
     * Simula un escenario semanal (múltiples días)
     */
    public Map<String, Object> simularEscenarioSemanal(int dias) {
        Map<String, Object> resultado = new HashMap<>();
        List<Map<String, Object>> resultadosDiarios = new ArrayList<>();
        
        // Simulamos cada día
        for (int i = 0; i < dias; i++) {
            Map<String, Object> resultadoDia = simularEscenarioDiario();
            resultadoDia.put("dia", i + 1);
            resultadosDiarios.add(resultadoDia);
        }
        
        resultado.put("resultadosDiarios", resultadosDiarios);
        
        // Métricas acumuladas
        int totalPedidos = 0;
        int totalCamiones = 0;
        int totalEventos = 0;
        
        for (Map<String, Object> dia : resultadosDiarios) {
            totalPedidos += (int) dia.get("pedidosGenerados");
            totalCamiones += ((List<?>) dia.get("camionesAsignados")).size();
            totalEventos += ((List<?>) dia.get("eventosGenerados")).size();
        }
        
        Map<String, Object> metricasAcumuladas = new HashMap<>();
        metricasAcumuladas.put("totalPedidos", totalPedidos);
        metricasAcumuladas.put("totalCamionesUsados", totalCamiones);
        metricasAcumuladas.put("totalEventos", totalEventos);
        metricasAcumuladas.put("diasSimulados", dias);
        
        resultado.put("metricasAcumuladas", metricasAcumuladas);
        
        return resultado;
    }
    
    /**
     * Simula un escenario de colapso (muchos pedidos, varias averías)
     */
    public Map<String, Object> simularEscenarioColapso() {
        Map<String, Object> resultado = new HashMap<>();
        
        // 1. Generamos un alto número de pedidos
        List<Pedido> pedidosGenerados = generarPedidosAleatorios(60, 80); // Muchos pedidos
        resultado.put("pedidosGenerados", pedidosGenerados.size());
        
        // 2. Asignamos camiones, pero algunos tendrán averías
        List<Camion> camionesDisponibles = (List<Camion>) camionRepository.findAll();
        
        // 3. Provocar averías en el 30% de los camiones
        int numAverias = Math.max(1, camionesDisponibles.size() * 30 / 100);
        List<Averia> averias = generarAveriasAleatorias(numAverias);
        resultado.put("averiasGeneradas", averias);
        
        // 4. Generar varios bloqueos para complicar las rutas
        List<Bloqueo> bloqueos = generarBloqueosAleatorios(3, 5);
        resultado.put("bloqueosGenerados", bloqueos);
        
        // 5. Asignamos camiones disponibles y rutas
        Map<String, Object> asignaciones = asignarCamionesYRutas(pedidosGenerados);
        resultado.put("camionesAsignados", asignaciones.get("camionesAsignados"));
        resultado.put("rutasGeneradas", asignaciones.get("rutas"));
        
        // 6. Calcular indicadores de colapso
        Map<String, Object> indicadoresColapso = new HashMap<>();
        indicadoresColapso.put("nivelSaturacion", calcularNivelSaturacion(pedidosGenerados, camionesDisponibles));
        indicadoresColapso.put("tiempoEstimadoNormalizacion", estimarTiempoNormalizacion());
        indicadoresColapso.put("pedidosNoAtendibles", estimarPedidosNoAtendibles(pedidosGenerados, camionesDisponibles));
        
        resultado.put("indicadoresColapso", indicadoresColapso);
        
        return resultado;
    }
    
    // ---------- Métodos auxiliares para simulación ----------
    
    private List<Pedido> generarPedidosAleatorios(int min, int max) {
        // Simulación - en un caso real se cargarían de la BD o archivos
        int cantidad = ThreadLocalRandom.current().nextInt(min, max + 1);
        List<Pedido> pedidos = new ArrayList<>();
        
        // Generamos pedidos simulados
        for (int i = 0; i < cantidad; i++) {
            Pedido pedido = new Pedido();
            pedido.setId(Long.valueOf(i));
            pedido.setPosX(ThreadLocalRandom.current().nextInt(0, 100));
            pedido.setPosY(ThreadLocalRandom.current().nextInt(0, 100));
            pedido.setM3(ThreadLocalRandom.current().nextInt(5, 20));
            pedido.setHorasLimite(ThreadLocalRandom.current().nextInt(2, 24));
            pedido.setEstado(0); // Pendiente
            pedido.setFechaHora("11d13h" + ThreadLocalRandom.current().nextInt(0, 60) + "m");
            
            pedidos.add(pedido);
        }
        
        return pedidos;
    }
    
    private Map<String, Object> asignarCamionesYRutas(List<Pedido> pedidos) {
        Map<String, Object> resultado = new HashMap<>();
        
        // Simulación de asignación de camiones y rutas
        List<Map<String, Object>> camionesAsignados = new ArrayList<>();
        List<Map<String, Object>> rutas = new ArrayList<>();
        
        // Simulación simplificada
        int numRutas = Math.min(5, pedidos.size() / 5 + 1); // Aproximadamente 5 pedidos por ruta
        
        // Dividimos pedidos en rutas
        for (int i = 0; i < numRutas; i++) {
            // Crear ruta
            Map<String, Object> ruta = new HashMap<>();
            ruta.put("idRuta", "R" + (i + 1));
            ruta.put("distanciaTotal", 100.0 + (50 * Math.random())); // Simulado
            ruta.put("tiempoEstimado", 120 + (i * 30)); // Minutos
            
            // Asignar pedidos a la ruta
            List<Map<String, Object>> pedidosRuta = new ArrayList<>();
            for (int j = i * (pedidos.size() / numRutas); 
                 j < (i + 1) * (pedidos.size() / numRutas) && j < pedidos.size(); 
                 j++) {
                pedidosRuta.add(convertirPedidoAMapa(pedidos.get(j)));
            }
            ruta.put("pedidos", pedidosRuta);
            ruta.put("numeroPedidos", pedidosRuta.size());
            
            // Asignar camión
            Map<String, Object> camion = new HashMap<>();
            camion.put("codigo", "T" + (i % 2 + 1) + "0" + (i + 1));
            camion.put("tipo", "T" + (i % 2 + 1));
            camion.put("capacidad", 20.0);
            
            camionesAsignados.add(camion);
            ruta.put("camion", camion.get("codigo"));
            
            rutas.add(ruta);
        }
        
        resultado.put("camionesAsignados", camionesAsignados);
        resultado.put("rutas", rutas);
        
        return resultado;
    }
    
    private List<Object> simularEventosAleatorios() {
        List<Object> eventos = new ArrayList<>();
        
        // Simulamos averías aleatorias (20% de probabilidad)
        if (Math.random() < 0.2) {
            List<Averia> averias = generarAveriasAleatorias(1);
            eventos.addAll(averias);
        }
        
        // Simulamos bloqueos aleatorios (10% de probabilidad)
        if (Math.random() < 0.1) {
            List<Bloqueo> bloqueos = generarBloqueosAleatorios(1, 1);
            eventos.addAll(bloqueos);
        }
        
        return eventos;
    }
    
    private List<Averia> generarAveriasAleatorias(int cantidad) {
        List<Averia> averias = new ArrayList<>();
        
        for (int i = 0; i < cantidad; i++) {
            Averia averia = new Averia();
            averia.setId(Long.valueOf(i));
            averia.setFechaHoraReporte(LocalDateTime.now());
            averia.setDescripcion("Avería simulada #" + i);
            averia.setSeveridad(ThreadLocalRandom.current().nextInt(1, 4)); // 1-3
            averia.setPosX(ThreadLocalRandom.current().nextInt(0, 100));
            averia.setPosY(ThreadLocalRandom.current().nextInt(0, 100));
            averia.setEstado(0); // Reportada
            
            // Simulamos una referencia a un camión
            Camion camion = new Camion();
            camion.setCodigo("T" + ThreadLocalRandom.current().nextInt(1, 3) + "0" + ThreadLocalRandom.current().nextInt(1, 6));
            averia.setCamion(camion);
            
            averias.add(averia);
        }
        
        return averias;
    }
    
    private List<Bloqueo> generarBloqueosAleatorios(int min, int max) {
        int cantidad = ThreadLocalRandom.current().nextInt(min, max + 1);
        List<Bloqueo> bloqueos = new ArrayList<>();
        
        for (int i = 0; i < cantidad; i++) {
            Bloqueo bloqueo = new Bloqueo();
            bloqueo.setId(Long.valueOf(i));
            
            // Punto inicial del bloqueo
            int x1 = ThreadLocalRandom.current().nextInt(10, 90);
            int y1 = ThreadLocalRandom.current().nextInt(10, 90);
            bloqueo.setPosXInicio(x1);
            bloqueo.setPosYInicio(y1);
            
            // Punto final del bloqueo (cercano al inicial)
            int x2 = x1 + ThreadLocalRandom.current().nextInt(-10, 11);
            int y2 = y1 + ThreadLocalRandom.current().nextInt(-10, 11);
            bloqueo.setPosXFin(x2);
            bloqueo.setPosYFin(y2);
            
            // Fechas
            bloqueo.setFechaInicio(LocalDate.now());
            bloqueo.setFechaFin(LocalDate.now().plusDays(ThreadLocalRandom.current().nextInt(1, 8)));
            
            bloqueo.setDescripcion("Bloqueo simulado #" + i);
            bloqueo.setActivo(true);
            
            bloqueos.add(bloqueo);
        }
        
        return bloqueos;
    }
    
    private Map<String, Object> calcularMetricasSimulacion() {
        Map<String, Object> metricas = new HashMap<>();
        
        // Simulación de métricas
        metricas.put("eficienciaRutas", 80 + (Math.random() * 15)); // 80-95%
        metricas.put("utilizacionCamiones", 75 + (Math.random() * 20)); // 75-95%
        metricas.put("tiempoPromedioEntrega", 90 + (Math.random() * 60)); // 90-150 min
        metricas.put("costoCombustible", 100 + (Math.random() * 50)); // 100-150 unidades
        
        return metricas;
    }
    
    private double calcularNivelSaturacion(List<Pedido> pedidos, List<Camion> camiones) {
        // Simulación - en un caso real dependería de la capacidad real vs demanda
        int capacidadTotal = 0;
        for (Camion camion : camiones) {
            if (camion.getEstado() == 0) { // Disponible
                capacidadTotal += camion.getCapacidad();
            }
        }
        
        int demandaTotal = 0;
        for (Pedido pedido : pedidos) {
            demandaTotal += pedido.getM3();
        }
        
        // Si la demanda supera la capacidad, hay saturación
        return Math.min(200, demandaTotal * 100.0 / (capacidadTotal > 0 ? capacidadTotal : 1));
    }
    
    private int estimarTiempoNormalizacion() {
        // Simulación - en horas
        return ThreadLocalRandom.current().nextInt(24, 73); // 1-3 días
    }
    
    private int estimarPedidosNoAtendibles(List<Pedido> pedidos, List<Camion> camiones) {
        // Simulación
        int capacidadTotal = 0;
        for (Camion camion : camiones) {
            if (camion.getEstado() == 0) { // Disponible
                capacidadTotal += camion.getCapacidad();
            }
        }
        
        int demandaTotal = 0;
        for (Pedido pedido : pedidos) {
            demandaTotal += pedido.getM3();
        }
        
        // Estimamos cuántos pedidos no se podrán atender
        if (capacidadTotal >= demandaTotal) return 0;
        
        // Asumimos pedido promedio de 10 m3
        return (demandaTotal - capacidadTotal) / 10;
    }
    
    // Método auxiliar para convertir pedidos a map
    private Map<String, Object> convertirPedidoAMapa(Pedido pedido) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", pedido.getId());
        map.put("posX", pedido.getPosX());
        map.put("posY", pedido.getPosY());
        map.put("m3", pedido.getM3());
        map.put("horasLimite", pedido.getHorasLimite());
        map.put("fechaHora", pedido.getFechaHora());
        map.put("estado", pedido.getEstado());
        return map;
    }
}