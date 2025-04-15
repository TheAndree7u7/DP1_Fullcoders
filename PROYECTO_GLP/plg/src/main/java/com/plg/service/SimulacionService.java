package com.plg.service;

import com.plg.entity.Almacen;
import com.plg.entity.Averia;
import com.plg.entity.Bloqueo;
import com.plg.entity.Camion;
import com.plg.entity.Mantenimiento;
import com.plg.entity.Pedido;
import com.plg.repository.AlmacenRepository;
import com.plg.repository.AveriaRepository;
import com.plg.repository.BloqueoRepository;
import com.plg.repository.CamionRepository;
import com.plg.repository.MantenimientoRepository;
import com.plg.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;

@Service
public class SimulacionService {

    @Autowired
    private PedidoRepository pedidoRepository;
    
    @Autowired
    private CamionRepository camionRepository;
    
    @Autowired
    private RutaService rutaService;

    @Autowired
    private BloqueoRepository bloqueoRepository;
    
    @Autowired
    private AveriaRepository averiaRepository;
    
    @Autowired
    private AlmacenRepository almacenRepository;
    
    @Autowired
    private MantenimientoRepository mantenimientoRepository;

    @Autowired
    private BloqueoService bloqueoService;
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
            
            // Usar tipoIncidente en lugar de severidad
            String[] tiposIncidente = {"TI1", "TI2", "TI3"};
            averia.setTipoIncidente(tiposIncidente[ThreadLocalRandom.current().nextInt(0, 3)]);
            
            // Asignar turno aleatorio
            String[] turnos = {"T1", "T2", "T3"};
            averia.setTurno(turnos[ThreadLocalRandom.current().nextInt(0, 3)]);
            
            averia.setPosX(ThreadLocalRandom.current().nextInt(0, 100));
            averia.setPosY(ThreadLocalRandom.current().nextInt(0, 100));
            averia.setEstado(0); // Reportada
            
            // Simulamos una referencia a un camión
            Camion camion = new Camion();
            camion.setCodigo("T" + ThreadLocalRandom.current().nextInt(1, 3) + "0" + ThreadLocalRandom.current().nextInt(1, 6));
            averia.setCamion(camion);
            
            // Establecer conCarga a true para simulación
            averia.setConCarga(true);
            
            // Calcular tiempos de inmovilización
            averia.calcularValidezAveria(true);
            averia.calcularKilometroOcurrencia(100.0); // Valor por defecto
            averia.calcularTiemposInoperatividad();
            
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
            
            // Crear lista de coordenadas (polígono abierto)
            List<Bloqueo.Coordenada> coordenadas = new ArrayList<>();
            
            // Punto inicial del bloqueo
            int x1 = ThreadLocalRandom.current().nextInt(10, 90);
            int y1 = ThreadLocalRandom.current().nextInt(10, 90);
            coordenadas.add(new Bloqueo.Coordenada(x1, y1));
            
            // Agregar 1-3 puntos más para formar un polígono abierto
            int numPuntos = ThreadLocalRandom.current().nextInt(1, 4);
            int xActual = x1;
            int yActual = y1;
            
            for (int j = 0; j < numPuntos; j++) {
                // Generar un punto cercano al anterior (máximo 10 unidades de distancia)
                int xNuevo = xActual + ThreadLocalRandom.current().nextInt(-10, 11);
                int yNuevo = yActual + ThreadLocalRandom.current().nextInt(-10, 11);
                
                // Mantener los puntos dentro de los límites del mapa
                xNuevo = Math.max(0, Math.min(xNuevo, 100));
                yNuevo = Math.max(0, Math.min(yNuevo, 100));
                
                coordenadas.add(new Bloqueo.Coordenada(xNuevo, yNuevo));
                
                xActual = xNuevo;
                yActual = yNuevo;
            }
            
            bloqueo.setCoordenadas(coordenadas);
            
            // Fechas y horas (formato correcto para el nuevo modelo)
            LocalDateTime ahora = LocalDateTime.now();
            
            // El bloqueo inicia entre ahora y 3 días después
            LocalDateTime fechaInicio = ahora.plusMinutes(ThreadLocalRandom.current().nextInt(0, 72 * 60));
            
            // El bloqueo dura entre 4 horas y 7 días
            LocalDateTime fechaFin = fechaInicio.plusMinutes(
                ThreadLocalRandom.current().nextInt(4 * 60, 7 * 24 * 60)
            );
            
            bloqueo.setFechaInicio(fechaInicio);
            bloqueo.setFechaFin(fechaFin);
            
            bloqueo.setDescripcion("Bloqueo simulado #" + i);
            
            // Un bloqueo está activo si ya comenzó pero aún no ha terminado
            bloqueo.setActivo(ahora.isAfter(fechaInicio) && ahora.isBefore(fechaFin));
            
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

    /**
     * Inicializa la simulación (estado inicial)
     */
    public void inicializarSimulacion() {
        // Reiniciar estado de camiones, pedidos, etc.
        // Similar a lo que ya haces al inicio del método simularEscenarioDiario
    }

    /**
     * Ejecuta un paso de la simulación con el tiempo proporcionado
     */
    public Map<String, Object> ejecutarPasoSimulacion(LocalDateTime tiempo) {
        Map<String, Object> resultado = new HashMap<>();
        
        // Procesar eventos programados para este tiempo
        procesarEventos(tiempo);
        
        // Procesar movimientos de camiones
        actualizarPosicionCamiones(tiempo);
        
        // Procesar entregas de pedidos
        procesarEntregas(tiempo);
        
        // Verificar niveles de combustible
        verificarCombustible(tiempo);
        
        // Recopilar estadísticas
        Map<String, Object> estadisticas = recopilarEstadisticas(tiempo);
        resultado.put("estadisticas", estadisticas);
        resultado.put("tiempo", tiempo);
        
        return resultado;
    }

    // Implementa los métodos auxiliares (procesarEventos, actualizarPosicionCamiones, etc.)
        /**
     * Procesa los eventos programados para el tiempo específico
     * (mantenimientos, reabastecimientos, etc.)
     */
    private void procesarEventos(LocalDateTime tiempo) {
        // Verificar mantenimientos programados
        verificarMantenimientosProgramados(tiempo);
        
        // Verificar bloqueos vigentes
        actualizarBloqueos(tiempo);
        
        // Verificar y procesar nuevos pedidos para este tiempo
        procesarNuevosPedidos(tiempo);
        
        // Verificar y procesar eventos aleatorios (averías con cierta probabilidad)
        procesarEventosAleatorios(tiempo);
    }
    
    /**
     * Verifica si hay mantenimientos programados para el tiempo actual
     */
    private void verificarMantenimientosProgramados(LocalDateTime tiempo) {
        LocalDate fechaActual = tiempo.toLocalDate();
        
        // Buscar mantenimientos programados para hoy
        List<Mantenimiento> mantenimientosDia = mantenimientoRepository.findByFechaInicio(fechaActual);
        
        for (Mantenimiento mantenimiento : mantenimientosDia) {
            Camion camion = mantenimiento.getCamion();
            
            // Si el camión está disponible (no en ruta), enviarlo a mantenimiento
            if (camion.getEstado() == 0) { // 0: disponible
                camion.setEstado(2); // 2: en mantenimiento
                camionRepository.save(camion);
                
                // Actualizar estado del mantenimiento
                mantenimiento.setEstado(1); // 1: en ejecución
                mantenimientoRepository.save(mantenimiento);
                
                System.out.println(String.format(
                    "Camión %s enviado a mantenimiento en tiempo %s", 
                    camion.getCodigo(), tiempo
                ));
            }
        }
        
        // Verificar mantenimientos que finalizan hoy
        List<Mantenimiento> mantenimientosFinalizados = mantenimientoRepository.findByFechaFin(fechaActual);
        
        for (Mantenimiento mantenimiento : mantenimientosFinalizados) {
            if (mantenimiento.getEstado() == 1) { // En ejecución
                Camion camion = mantenimiento.getCamion();
                camion.setEstado(0); // 0: disponible
                camionRepository.save(camion);
                
                // Actualizar estado del mantenimiento
                mantenimiento.setEstado(2); // 2: finalizado
                mantenimientoRepository.save(mantenimiento);
                
                System.out.println(String.format(
                    "Mantenimiento de camión %s finalizado en tiempo %s", 
                    camion.getCodigo(), tiempo
                ));
            }
        }
    }
    
    /**
     * Actualiza el estado de los bloqueos según el tiempo actual
     */
    private void actualizarBloqueos(LocalDateTime tiempo) {
        // Usar el servicio de bloqueos para actualizar los estados
        bloqueoService.actualizarEstadoBloqueos();
        
        // Obtener bloqueos activos para el tiempo actual
        List<Bloqueo> bloqueosActivos = bloqueoService.obtenerBloqueosActivos(tiempo);
        
        // Log de actividades de bloqueos
        for (Bloqueo bloqueo : bloqueosActivos) {
            // Verificar si este bloqueo acaba de activarse (para limitar logs repetidos)
            if (tiempo.isAfter(bloqueo.getFechaInicio()) && 
                tiempo.isBefore(bloqueo.getFechaInicio().plusMinutes(5))) {
                System.out.println(String.format(
                    "Bloqueo activo en %s hasta %s con %d puntos - %s", 
                    bloqueo.getFechaInicio(), bloqueo.getFechaFin(), 
                    bloqueo.getCoordenadas().size(), bloqueo.getDescripcion()
                ));
            }
        }
        
        // Obtener bloqueos que finalizan pronto (en los próximos 5 minutos)
        LocalDateTime tiempoFuturo = tiempo.plusMinutes(5);
        List<Bloqueo> bloqueosFinalizando = bloqueoRepository.findByFechaInicioBeforeAndFechaFinAfter(
            tiempo, tiempo).stream()
            .filter(b -> b.getFechaFin().isBefore(tiempoFuturo))
            .collect(java.util.stream.Collectors.toList());
            
        for (Bloqueo bloqueo : bloqueosFinalizando) {
            System.out.println(String.format(
                "Bloqueo finalizando pronto: %s a las %s", 
                bloqueo.getDescripcion(), bloqueo.getFechaFin()
            ));
        }
    }
    
    /**
     * Procesa pedidos nuevos que se generan en el tiempo actual
     */
    private void procesarNuevosPedidos(LocalDateTime tiempo) {
        // En un escenario realista, buscaríamos pedidos con fechaHora igual al tiempo actual
        // Para simulación, generamos pedidos aleatorios con cierta probabilidad
        
        // Probabilidad de nuevos pedidos según la hora del día
        int hora = tiempo.getHour();
        
        double probabilidad;
        if (hora >= 8 && hora < 12) {
            // Mañana: Alta probabilidad
            probabilidad = 0.4;
        } else if (hora >= 12 && hora < 18) {
            // Tarde: Media probabilidad
            probabilidad = 0.3;
        } else if (hora >= 18 && hora < 22) {
            // Noche: Baja probabilidad
            probabilidad = 0.2;
        } else {
            // Madrugada: Muy baja probabilidad
            probabilidad = 0.05;
        }
        
        // Generar pedidos aleatorios según probabilidad
        if (Math.random() < probabilidad) {
            int cantidadPedidos = ThreadLocalRandom.current().nextInt(1, 4); // 1-3 pedidos
            List<Pedido> nuevosPedidos = generarPedidosAleatorios(cantidadPedidos, cantidadPedidos);
            
            // Ajustar tiempo de creación
            for (Pedido pedido : nuevosPedidos) {
                pedido.setFechaCreacion(tiempo);
                pedidoRepository.save(pedido);
            }
            
            System.out.println(String.format(
                "Generados %d nuevos pedidos en tiempo %s", 
                nuevosPedidos.size(), tiempo
            ));
            
            // Intentar asignar pedidos a camiones disponibles
            asignarPedidosACamiones(nuevosPedidos);
        }
    }
    
    /**
     * Asigna pedidos nuevos a camiones disponibles
     */
    private void asignarPedidosACamiones(List<Pedido> pedidos) {
        // Obtener camiones disponibles
        List<Camion> camionesDisponibles = camionRepository.findByEstado(0); // 0: disponible
        
        if (camionesDisponibles.isEmpty()) {
            System.out.println("No hay camiones disponibles para asignar pedidos");
            return;
        }
        
        // Implementación simple: asignar pedidos a los camiones disponibles en orden
        int camionIndex = 0;
        
        for (Pedido pedido : pedidos) {
            if (camionIndex >= camionesDisponibles.size()) {
                // Reiniciar a primer camión si se acabaron
                camionIndex = 0;
            }
            
            Camion camion = camionesDisponibles.get(camionIndex);
            
            // Verificar si el camión tiene capacidad
            if (camion.getCapacidad() >= pedido.getM3()) {
                // Asignar pedido al camión
                pedido.setCamion(camion);
                pedido.setEstado(1); // 1: Asignado
                pedidoRepository.save(pedido);
                
                // Actualizar estado del camión
                camion.setEstado(1); // 1: en ruta
                camion.setPesoCarga(camion.getPesoCarga() + (pedido.getM3() * 0.5)); // GLP pesa 0.5 Ton por m3
                camion.setPesoCombinado(camion.getTara() + camion.getPesoCarga());
                camionRepository.save(camion);
                
                System.out.println(String.format(
                    "Pedido %d asignado a camión %s", 
                    pedido.getId(), camion.getCodigo()
                ));
                
                camionIndex++;
            }
        }
    }
    
    /**
     * Procesa eventos aleatorios como averías
     */
    private void procesarEventosAleatorios(LocalDateTime tiempo) {
        // Probabilidad de avería en algún camión en ruta (0.5%)
        if (Math.random() < 0.005) {
            // Buscar camiones en ruta
            List<Camion> camionesEnRuta = camionRepository.findByEstado(1); // 1: en ruta
            
            if (!camionesEnRuta.isEmpty()) {
                // Seleccionar un camión aleatorio
                int randomIndex = ThreadLocalRandom.current().nextInt(0, camionesEnRuta.size());
                Camion camion = camionesEnRuta.get(randomIndex);
                
                // Crear avería
                Averia averia = new Averia();
                averia.setCamion(camion);
                averia.setFechaHoraReporte(tiempo);
                averia.setDescripcion("Avería aleatoria en ruta");
                
                // Usar tipoIncidente en lugar de severidad
                String[] tiposIncidente = {"TI1", "TI2", "TI3"};
                averia.setTipoIncidente(tiposIncidente[ThreadLocalRandom.current().nextInt(0, 3)]);
                
                // Asignar turno según la hora del día
                String turno;
                int hora = tiempo.getHour();
                if (hora >= 0 && hora < 8) {
                    turno = "T1";
                } else if (hora >= 8 && hora < 16) {
                    turno = "T2";
                } else {
                    turno = "T3";
                }
                averia.setTurno(turno);
                
                averia.setPosX(camion.getPosX());
                averia.setPosY(camion.getPosY());
                averia.setEstado(0); // 0: Reportada
                
                // Verificar si el camión tiene carga
                averia.setConCarga(camion.getPesoCarga() > 0);
                
                // Validar si la avería es válida según las condiciones
                boolean esValida = averia.calcularValidezAveria(true); // El camión ya está en operación
                
                if (esValida) {
                    // Calcular distancia total del recorrido
                    List<Pedido> pedidosCamion = pedidoRepository.findByCamion_CodigoAndEstado(camion.getCodigo(), 1);
                    double distanciaTotal = calcularDistanciaRecorrido(camion, pedidosCamion);
                    
                    // Calcular kilómetro de ocurrencia entre 5% y 35% del recorrido
                    averia.calcularKilometroOcurrencia(distanciaTotal);
                    
                    // Calcular tiempos de inmovilización según tipo de incidente
                    averia.calcularTiemposInoperatividad();
                    
                    // Guardar la avería
                    averiaRepository.save(averia);
                    
                    // Actualizar estado del camión
                    actualizarEstadoCamionPorAveria(camion, averia);
                    
                    System.out.println(String.format(
                        "Camión %s averiado con %s en turno %s en tiempo %s - KM: %.2f", 
                        camion.getCodigo(), averia.getTipoIncidente(), averia.getTurno(), 
                        tiempo, averia.getKilometroOcurrencia()
                    ));
                } else {
                    System.out.println(String.format(
                        "Avería ignorada para camión %s (sin carga)", 
                        camion.getCodigo()
                    ));
                }
            }
        }
    }
    
    /**
     * Actualiza la posición de los camiones en ruta
     */
    private void actualizarPosicionCamiones(LocalDateTime tiempo) {
        // Buscar camiones en ruta
        List<Camion> camionesEnRuta = camionRepository.findByEstado(1); // 1: en ruta
        
        for (Camion camion : camionesEnRuta) {
            // Obtener pedidos asignados al camión
            List<Pedido> pedidosCamion = pedidoRepository.findByCamion_CodigoAndEstado(camion.getCodigo(), 1); // 1: Asignado
            
            if (pedidosCamion.isEmpty()) {
                // No hay pedidos, camión debería volver al almacén central
                camion.setEstado(0); // 0: disponible
                camionRepository.save(camion);
                continue;
            }
            
            // Simular avance del camión hacia los pedidos
            // En una implementación real, calcularíamos la distancia y tiempo restante
            
            // Velocidad promedio del camión en km por minuto (50 km/h = 0.833 km/min)
            double velocidadKmPorMinuto = 50.0 / 60.0;
            
            // Avance en el último minuto
            double avanceEnKm = velocidadKmPorMinuto;
            
            // Actualizar consumo de combustible
            double consumoCombustible = camion.calcularConsumoCombustible(avanceEnKm);
            camion.setCombustibleActual(Math.max(0, camion.getCombustibleActual() - consumoCombustible));
            
            // En un escenario real, actualizaríamos la posición exacta
            // Para la simulación, aproximamos con probabilidades de llegada
            
            // Probabilidad de que el camión llegue a su siguiente destino
            if (Math.random() < 0.1) { // 10% de probabilidad por paso
                // Camión llegó al destino, procesar entrega
                Pedido pedidoActual = pedidosCamion.get(0); // Tomar el primer pedido de la lista
                
                // Actualizar posición del camión a la posición del pedido
                camion.setPosX(pedidoActual.getPosX());
                camion.setPosY(pedidoActual.getPosY());
                
                System.out.println(String.format(
                    "Camión %s llegó a la posición del pedido %d en tiempo %s", 
                    camion.getCodigo(), pedidoActual.getId(), tiempo
                ));
            }
            
            camionRepository.save(camion);
        }
    }
    
    /**
     * Procesa las entregas de pedidos
     */
    private void procesarEntregas(LocalDateTime tiempo) {
        // Buscar camiones en ruta
        List<Camion> camionesEnRuta = camionRepository.findByEstado(1); // 1: en ruta
        
        for (Camion camion : camionesEnRuta) {
            // Obtener pedidos asignados al camión
            List<Pedido> pedidosCamion = pedidoRepository.findByCamion_CodigoAndEstado(camion.getCodigo(), 1); // 1: Asignado
            
            if (!pedidosCamion.isEmpty()) {
                // Verificar si el camión está en la posición del primer pedido
                Pedido primerPedido = pedidosCamion.get(0);
                
                if (camion.getPosX() == primerPedido.getPosX() && camion.getPosY() == primerPedido.getPosY()) {
                    // Camión está en la posición del pedido, realizar entrega
                    primerPedido.setEstado(2); // 2: Entregado
                    primerPedido.setFechaEntrega(tiempo);
                    pedidoRepository.save(primerPedido);
                    
                    // Actualizar carga del camión
                    double pesoEntregado = primerPedido.getM3() * 0.5; // GLP pesa 0.5 Ton por m3
                    camion.setPesoCarga(Math.max(0, camion.getPesoCarga() - pesoEntregado));
                    camion.setPesoCombinado(camion.getTara() + camion.getPesoCarga());
                    
                    System.out.println(String.format(
                        "Pedido %d entregado por camión %s en tiempo %s", 
                        primerPedido.getId(), camion.getCodigo(), tiempo
                    ));
                    
                    // Verificar si hay más pedidos o debe volver al almacén
                    if (pedidosCamion.size() <= 1) {
                        // No hay más pedidos, regresar al almacén central
                        // En implementación real, calcularíamos ruta al almacén
                        if (Math.random() < 0.2) { // 20% de probabilidad por paso de volver al almacén
                            camion.setEstado(0); // 0: disponible
                            camion.setPosX(12); // Posición del almacén central
                            camion.setPosY(8);
                            camion.setPesoCarga(0);
                            camion.setPesoCombinado(camion.getTara());
                            
                            System.out.println(String.format(
                                "Camión %s regresó al almacén central en tiempo %s", 
                                camion.getCodigo(), tiempo
                            ));
                        }
                    }
                    
                    camionRepository.save(camion);
                }
            }
        }
    }
    
    /**
     * Verifica los niveles de combustible y realiza recargas si es necesario
     */
    private void verificarCombustible(LocalDateTime tiempo) {
        // Buscar camiones en ruta
        List<Camion> camionesEnRuta = camionRepository.findByEstado(1); // 1: en ruta
        
        for (Camion camion : camionesEnRuta) {
            // Verificar si el nivel de combustible es bajo (menos del 20%)
            double nivelCritico = camion.getCapacidadTanque() * 0.2;
            
            if (camion.getCombustibleActual() < nivelCritico) {
                // Buscar el almacén más cercano para recargar
                Almacen almacenCercano = buscarAlmacenCercano(camion.getPosX(), camion.getPosY());
                
                if (almacenCercano != null) {
                    // Simular recarga de combustible
                    double cantidadRecargar = camion.getCapacidadTanque() - camion.getCombustibleActual();
                    
                    if (almacenCercano.puedeRecargarCombustible(cantidadRecargar)) {
                        boolean recargoExitoso = almacenCercano.recargarCombustible(camion, cantidadRecargar);
                        
                        if (recargoExitoso) {
                            System.out.println(String.format(
                                "Camión %s recargó %.2f galones en almacén %s en tiempo %s", 
                                camion.getCodigo(), cantidadRecargar, almacenCercano.getNombre(), tiempo
                            ));
                            
                            // Guardar cambios en almacén y camión
                            almacenRepository.save(almacenCercano);
                            camionRepository.save(camion);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Busca el almacén más cercano a una posición
     */
    private Almacen buscarAlmacenCercano(int posX, int posY) {
        List<Almacen> almacenes = almacenRepository.findByActivo(true);
        
        if (almacenes.isEmpty()) {
            return null;
        }
        
        Almacen masCercano = null;
        double distanciaMinima = Double.MAX_VALUE;
        
        for (Almacen almacen : almacenes) {
            double distancia = almacen.calcularDistancia(posX, posY);
            if (distancia < distanciaMinima) {
                distanciaMinima = distancia;
                masCercano = almacen;
            }
        }
        
        return masCercano;
    }
    
    /**
     * Recopila estadísticas del estado actual de la simulación
     */
    private Map<String, Object> recopilarEstadisticas(LocalDateTime tiempo) {
        Map<String, Object> estadisticas = new HashMap<>();
        
        // Contar pedidos por estado
        long pedidosPendientes = pedidoRepository.countByEstado(0); // 0: Pendiente
        long pedidosAsignados = pedidoRepository.countByEstado(1);  // 1: Asignado
        long pedidosEntregados = pedidoRepository.countByEstado(2); // 2: Entregado
        
        // Contar camiones por estado
        long camionesDisponibles = camionRepository.countByEstado(0); // 0: disponible
        long camionesEnRuta = camionRepository.countByEstado(1);     // 1: en ruta
        long camionesEnMantenimiento = camionRepository.countByEstado(2); // 2: en mantenimiento
        long camionesAveriados = camionRepository.countByEstado(3);  // 3: averiado
        
        // Calcular tiempos de entrega
        double tiempoPromedioEntrega = calcularTiempoPromedioEntrega();
        
        // Calcular eficiencia de entrega
        double eficienciaEntrega = calcularEficienciaEntrega();
        
        // Estadísticas de combustible
        double consumoTotalCombustible = calcularConsumoTotalCombustible();
        
        // Estadísticas de almacenes
        Map<String, Object> estadisticasAlmacenes = recopilarEstadisticasAlmacenes();
        
        // Guardar todas las estadísticas
        estadisticas.put("fecha", tiempo.toLocalDate());
        estadisticas.put("hora", tiempo.toLocalTime());
        estadisticas.put("pedidosPendientes", pedidosPendientes);
        estadisticas.put("pedidosAsignados", pedidosAsignados);
        estadisticas.put("pedidosEntregados", pedidosEntregados);
        estadisticas.put("totalPedidos", pedidosPendientes + pedidosAsignados + pedidosEntregados);
        estadisticas.put("camionesDisponibles", camionesDisponibles);
        estadisticas.put("camionesEnRuta", camionesEnRuta);
        estadisticas.put("camionesEnMantenimiento", camionesEnMantenimiento);
        estadisticas.put("camionesAveriados", camionesAveriados);
        estadisticas.put("totalCamiones", camionesDisponibles + camionesEnRuta + camionesEnMantenimiento + camionesAveriados);
        estadisticas.put("tiempoPromedioEntrega", tiempoPromedioEntrega);
        estadisticas.put("eficienciaEntrega", eficienciaEntrega);
        estadisticas.put("consumoTotalCombustible", consumoTotalCombustible);
        estadisticas.put("almacenes", estadisticasAlmacenes);
        
        return estadisticas;
    }
    
    /**
     * Calcula el tiempo promedio de entrega en minutos
     */
    private double calcularTiempoPromedioEntrega() {
        List<Pedido> pedidosEntregados = pedidoRepository.findByEstadoAndFechaEntregaNotNull(2); // 2: Entregado
        
        if (pedidosEntregados.isEmpty()) {
            return 0;
        }
        
        double tiempoTotal = 0;
        int conteo = 0;
        
        for (Pedido pedido : pedidosEntregados) {
            if (pedido.getFechaCreacion() != null && pedido.getFechaEntrega() != null) {
                // Calcular diferencia en minutos
                long diferenciaMinutos = java.time.Duration.between(pedido.getFechaCreacion(), pedido.getFechaEntrega()).toMinutes();
                tiempoTotal += diferenciaMinutos;
                conteo++;
            }
        }
        
        return conteo > 0 ? tiempoTotal / conteo : 0;
    }
    
    /**
     * Calcula la eficiencia de entrega (porcentaje de pedidos entregados a tiempo)
     */
    private double calcularEficienciaEntrega() {
        List<Pedido> pedidosEntregados = pedidoRepository.findByEstado(2); // 2: Entregado
        
        if (pedidosEntregados.isEmpty()) {
            return 100; // No hay pedidos entregados para evaluar
        }
        
        int pedidosATiempo = 0;
        
        for (Pedido pedido : pedidosEntregados) {
            if (pedido.getFechaCreacion() != null && pedido.getFechaEntrega() != null) {
                // Calcular diferencia en horas
                long diferenciaHoras = java.time.Duration.between(pedido.getFechaCreacion(), pedido.getFechaEntrega()).toHours();
                
                // Verificar si se entregó dentro del límite
                if (diferenciaHoras <= pedido.getHorasLimite()) {
                    pedidosATiempo++;
                }
            }
        }
        
        return pedidosEntregados.size() > 0 ? (pedidosATiempo * 100.0 / pedidosEntregados.size()) : 100;
    }
    
    /**
     * Calcula el consumo total de combustible en galones
     */
    private double calcularConsumoTotalCombustible() {
        // En una implementación real, esto se calcularía sumando todos los consumos registrados
        // Para simulación, usamos un valor aproximado basado en camiones en ruta
        List<Camion> camionesEnRuta = camionRepository.findByEstado(1); // 1: en ruta
        
        double consumoTotal = 0;
        for (Camion camion : camionesEnRuta) {
            // Asumimos un consumo promedio basado en el peso del camión
            consumoTotal += (camion.getPesoCombinado() / 180.0) * 10; // Estimado para 10 km
        }
        
        return consumoTotal;
    }
    
    /**
     * Recopila estadísticas de almacenes
     */
    private Map<String, Object> recopilarEstadisticasAlmacenes() {
        Map<String, Object> estadisticas = new HashMap<>();
        List<Almacen> almacenes = almacenRepository.findAll();
        
        for (Almacen almacen : almacenes) {
            Map<String, Object> estadisticasAlmacen = new HashMap<>();
            estadisticasAlmacen.put("nombre", almacen.getNombre());
            estadisticasAlmacen.put("capacidadGLP", almacen.getCapacidadGLP());
            estadisticasAlmacen.put("capacidadActualGLP", almacen.getCapacidadActualGLP());
            estadisticasAlmacen.put("porcentajeGLP", almacen.getCapacidadGLP() > 0 ? 
                    (almacen.getCapacidadActualGLP() * 100 / almacen.getCapacidadGLP()) : 0);
            estadisticasAlmacen.put("capacidadCombustible", almacen.getCapacidadCombustible());
            estadisticasAlmacen.put("capacidadActualCombustible", almacen.getCapacidadActualCombustible());
            estadisticasAlmacen.put("porcentajeCombustible", almacen.getCapacidadCombustible() > 0 ? 
                    (almacen.getCapacidadActualCombustible() * 100 / almacen.getCapacidadCombustible()) : 0);
            
            estadisticas.put("almacen_" + almacen.getId(), estadisticasAlmacen);
        }
        
        return estadisticas;
    }

    /**
     * Carga y procesa el archivo de averías
     * Aplica todas las reglas de validación especificadas:
     * 1. Verifica si la unidad está en operación
     * 2. Calcula el kilómetro de ocurrencia aleatoriamente entre 5% y 35% del tramo
     * 3. Solo considera averías cuando el camión lleva carga
     */
    public List<Averia> cargarArchivoAverias(String rutaArchivo) {
        List<Averia> averias = new ArrayList<>();
        
        try {
            Path path = Paths.get(rutaArchivo);
            if (!Files.exists(path)) {
                System.out.println("Archivo de averías no encontrado: " + rutaArchivo);
                return averias;
            }
            
            List<String> lines = Files.readAllLines(path);
            
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                
                // Formato esperado: T1_TA01_TI2
                String[] parts = line.split("_");
                if (parts.length != 3) {
                    System.out.println("Formato inválido en línea: " + line);
                    continue;
                }
                
                String turno = parts[0];       // T1, T2, T3
                String codigoCamion = parts[1]; // TA01, TB03, etc.
                String tipoIncidente = parts[2]; // TI1, TI2, TI3
                
                // Buscar el camión por su código
                Optional<Camion> optCamion = camionRepository.findById(codigoCamion);
                if (!optCamion.isPresent()) {
                    System.out.println("Camión no encontrado para avería: " + line);
                    continue;
                }
                
                Camion camion = optCamion.get();
                
                // Verificar si el camión está en operación (estado 1: en ruta)
                boolean estaEnOperacion = camion.getEstado() == 1;
                
                // Crear la avería
                Averia averia = new Averia();
                averia.setCamion(camion);
                averia.setFechaHoraReporte(LocalDateTime.now());
                averia.setDescripcion("Avería importada: " + tipoIncidente);
                averia.setTurno(turno);
                averia.setTipoIncidente(tipoIncidente);
                averia.setPosX(camion.getPosX());
                averia.setPosY(camion.getPosY());
                averia.setEstado(0); // 0: reportada
                
                // Verificar si tiene carga asignada
                List<Pedido> pedidosCamion = pedidoRepository.findByCamion_CodigoAndEstado(camion.getCodigo(), 1);
                averia.setConCarga(!pedidosCamion.isEmpty() && camion.getPesoCarga() > 0);
                
                // Validar si la avería es válida según las condiciones
                boolean esValida = averia.calcularValidezAveria(estaEnOperacion);
                
                if (esValida) {
                    // Calcular la distancia total del recorrido (simplificado)
                    double distanciaTotal = 100.0; // Valor por defecto
                    
                    // Si hay pedidos, podríamos calcular la distancia real
                    if (!pedidosCamion.isEmpty()) {
                        // Simplificación: usamos una suma de distancias punto a punto
                        distanciaTotal = calcularDistanciaRecorrido(camion, pedidosCamion);
                    }
                    
                    // Calcular kilómetro aleatorio de ocurrencia (5% a 35% del recorrido)
                    averia.calcularKilometroOcurrencia(distanciaTotal);
                    
                    // Calcular tiempos de inmovilización según tipo de incidente
                    // Asumimos 3 turnos de 8 horas cada uno (configurable)
                    int duracionTurnoHoras = 8;
                    averia.calcularTiemposInoperatividad(duracionTurnoHoras);
                    
                    // Actualizar estado del camión según el tipo de incidente
                    actualizarEstadoCamionPorAveria(camion, averia);
                    
                    // Guardar la avería
                    averiaRepository.save(averia);
                    averias.add(averia);
                    
                    System.out.println("Avería procesada: " + averia.generarRegistro() + 
                                     " - KM: " + averia.getKilometroOcurrencia());
                } else {
                    System.out.println("Avería ignorada (unidad no en operación o sin carga): " + line);
                }
            }
            
        } catch (IOException e) {
            System.err.println("Error al leer el archivo de averías: " + e.getMessage());
        }
        
        return averias;
    }
    
    /**
     * Calcula la distancia aproximada del recorrido completo de un camión
     */
    private double calcularDistanciaRecorrido(Camion camion, List<Pedido> pedidos) {
        double distanciaTotal = 0;
        
        // Posición actual del camión
        int posXActual = camion.getPosX();
        int posYActual = camion.getPosY();
        
        // Sumar distancias entre puntos consecutivos
        for (Pedido pedido : pedidos) {
            double distancia = Math.sqrt(
                Math.pow(pedido.getPosX() - posXActual, 2) + 
                Math.pow(pedido.getPosY() - posYActual, 2)
            );
            
            distanciaTotal += distancia;
            
            // Actualizar posición para el siguiente cálculo
            posXActual = pedido.getPosX();
            posYActual = pedido.getPosY();
        }
        
        // Agregar distancia de regreso al almacén (simplificado)
        // Asumimos almacén central en posición (12, 8)
        double distanciaRegreso = Math.sqrt(
            Math.pow(12 - posXActual, 2) + 
            Math.pow(8 - posYActual, 2)
        );
        
        distanciaTotal += distanciaRegreso;
        
        // Multiplicar por 2 para simular ida y vuelta
        return distanciaTotal * 2;
    }
    
    /**
     * Actualiza el estado del camión según el tipo de avería
     */
    private void actualizarEstadoCamionPorAveria(Camion camion, Averia averia) {
        // Marcar el camión como averiado
        camion.setEstado(3); // 3: averiado
        
        // Guardar el camión con su nuevo estado
        camionRepository.save(camion);
        
        System.out.println("Camión " + camion.getCodigo() + " marcado como averiado por " + 
                         averia.getTipoIncidente() + " en turno " + averia.getTurno());
    }
    
    /**
     * Método para iniciar la carga del archivo de averías durante el inicio de la simulación
     */
    public void inicializarAveriasDesdeArchivo() {
        String rutaArchivo = "src/main/resources/data/averias/averias.v1.txt";
        List<Averia> averiasCargadas = cargarArchivoAverias(rutaArchivo);
        System.out.println("Se cargaron " + averiasCargadas.size() + " averías válidas desde el archivo");
    }
}