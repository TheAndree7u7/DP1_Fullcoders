package com.plg.service;

import com.plg.entity.*;
import com.plg.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

@Service
public class SimulacionTiempoRealService {

    private static final Logger logger = LoggerFactory.getLogger(SimulacionTiempoRealService.class);

    @Autowired
    private CamionRepository camionRepository;
    
    @Autowired
    private RutaRepository rutaRepository;
    
    @Autowired
    private PedidoRepository pedidoRepository;
    
    @Autowired
    private AlmacenRepository almacenRepository;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    private ScheduledExecutorService scheduler;
    private boolean simulacionEnCurso = false;
    private int factorVelocidad = 1;
    
    // Almacena el progreso del nodo actual en cada ruta (0 a 100%)
    private Map<Long, Double> progresoNodoActual = new HashMap<>();
    
    // Método para iniciar la simulación en tiempo real
    public Map<String, Object> iniciarSimulacion() {
        if (simulacionEnCurso) {
            return crearRespuesta("La simulación ya está en curso");
        }
        
        logger.info("Iniciando simulación de tiempo real a las {}", LocalDateTime.now());
        
        simulacionEnCurso = true;
        scheduler = Executors.newSingleThreadScheduledExecutor();
        progresoNodoActual.clear();
        
        // Activar las rutas pendientes de los camiones en ruta si no tienen rutas activas
        activarRutasPendientes();
        
        // Iniciar simulación - actualizar cada segundo ajustado por el factor de velocidad
        scheduler.scheduleAtFixedRate(this::actualizarSimulacion, 0, 1000 / factorVelocidad, TimeUnit.MILLISECONDS);
        
        logger.info("Simulación iniciada con factor de velocidad: {}", factorVelocidad);
        
        // Enviar notificación de inicio
        Map<String, Object> notificacion = new HashMap<>();
        notificacion.put("tipo", "simulacion");
        notificacion.put("accion", "iniciada");
        notificacion.put("factorVelocidad", factorVelocidad);
        notificacion.put("timestamp", LocalDateTime.now().toString());
        messagingTemplate.convertAndSend("/topic/simulacion", notificacion);
        
        return crearRespuesta("Simulación iniciada correctamente");
    }
    
    // Nuevo método para activar rutas pendientes
    private void activarRutasPendientes() {
        List<Camion> camionesEnRuta = camionRepository.findByEstado(1); // Camiones en ruta
        
        for (Camion camion : camionesEnRuta) {
            // Verificar si tiene alguna ruta activa
            List<Ruta> rutasActivas = rutaRepository.findByCamionIdAndEstado(camion.getId(), 1);
            
            if (rutasActivas.isEmpty()) {
                // Buscar rutas pendientes para este camión
                List<Ruta> rutasPendientes = rutaRepository.findByCamionIdAndEstado(camion.getId(), 0);
                
                if (!rutasPendientes.isEmpty()) {
                    // Activar la primera ruta pendiente
                    Ruta rutaParaActivar = rutasPendientes.get(0);
                    rutaParaActivar.setEstado(1); // 1 = En curso
                    rutaParaActivar.setFechaInicioRuta(LocalDateTime.now());
                    rutaRepository.save(rutaParaActivar);
                    
                    logger.info("Activada ruta {} para camión {}", rutaParaActivar.getCodigo(), camion.getCodigo());
                    
                    // Inicializar progreso
                    progresoNodoActual.put(rutaParaActivar.getId(), Double.valueOf(0.0));
                }
            }
        }
    }
    
    // Método para detener la simulación
    public Map<String, Object> detenerSimulacion() {
        if (!simulacionEnCurso) {
            return crearRespuesta("No hay simulación en curso");
        }
        
        simulacionEnCurso = false;
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
            }
        }
        
        return crearRespuesta("Simulación detenida correctamente");
    }
    
    // Método para ajustar la velocidad de la simulación
    public Map<String, Object> ajustarVelocidad(int factor) {
        if (factor < 1) {
            factor = 1;
        } else if (factor > 10) {
            factor = 10;
        }
        
        this.factorVelocidad = factor;
        
        // Si hay una simulación en curso, reiniciarla con la nueva velocidad
        if (simulacionEnCurso) {
            detenerSimulacion();
            iniciarSimulacion();
        }
        
        Map<String, Object> respuesta = crearRespuesta("Velocidad ajustada correctamente");
        respuesta.put("factorVelocidad", Optional.of(factorVelocidad));
        return respuesta;
    }
    
    // Método principal que se ejecuta periódicamente para actualizar la simulación
    private void actualizarSimulacion() {
        try {
            // Verificar si hay simulación en curso
            if (!simulacionEnCurso) {
                logger.warn("actualizarSimulacion() llamado pero simulacionEnCurso=false");
                return;
            }
            
            // Obtener todos los camiones en ruta (estado 1)
            List<Camion> camionesEnRuta = camionRepository.findByEstado(1); // 1 = En ruta
            // Agregar también los camiones en estado 0 (disponible) para verificar si tienen rutas pendientes
            List<Camion> camionesDisponibles = camionRepository.findByEstado(0);
            
            camionesEnRuta.addAll(camionesDisponibles);
            
            // Si no hay camiones en ruta o disponibles, no hay nada que simular
            if (camionesEnRuta.isEmpty()) {
                logger.info("No hay camiones para simular");
                return;
            }
            
            logger.debug("Procesando {} camiones en la simulación", camionesEnRuta.size());
            
            // Procesar cada camión
            for (Camion camion : camionesEnRuta) {
                procesarMovimientoCamion(camion);
            }
            
            // Contar estadísticas para incluir en la actualización
            Map<String, Object> estadisticas = new HashMap<>();
            estadisticas.put("camionesTotal", camionRepository.count());
            estadisticas.put("camionesEnRuta", camionRepository.findByEstado(1).size());
            estadisticas.put("almacenesTotal", almacenRepository.count());
            estadisticas.put("pedidosTotal", pedidoRepository.count());
            estadisticas.put("pedidosPendientes", pedidoRepository.findByEstado(0).size());
            estadisticas.put("pedidosEnRuta", pedidoRepository.findByEstado(1).size());
            estadisticas.put("pedidosEntregados", pedidoRepository.findByEstado(2).size());
            estadisticas.put("rutasTotal", rutaRepository.count());
            estadisticas.put("rutasActivas", rutaRepository.findByEstado(1).size());
            
            // Enviar actualización a los clientes conectados vía WebSocket
            enviarActualizacionPosiciones(estadisticas);
            
        } catch (Exception e) {
            // Registrar error pero no detener la simulación
            logger.error("Error en la simulación: {}", e.getMessage(), e);
        }
    }

    // Procesa el movimiento de un camión específico
    private void procesarMovimientoCamion(Camion camion) {
        try {
            // Obtener las rutas activas del camión
            List<Ruta> rutasActivas = rutaRepository.findByCamionIdAndEstado(camion.getId(), 1); // 1 = En curso
            
            if (rutasActivas.isEmpty()) {
                logger.warn("Camión {} está en ruta (estado 1) pero no tiene rutas activas", camion.getCodigo());
                
                // Intentar activar una ruta pendiente
                List<Ruta> rutasPendientes = rutaRepository.findByCamionIdAndEstado(camion.getId(), 0);
                if (!rutasPendientes.isEmpty()) {
                    Ruta rutaParaActivar = rutasPendientes.get(0);
                    rutaParaActivar.setEstado(1); // Activar la ruta
                    rutaParaActivar.setFechaInicioRuta(LocalDateTime.now());
                    rutaRepository.save(rutaParaActivar);
                    
                    // Inicializar progreso
                    progresoNodoActual.put(rutaParaActivar.getId(), Double.valueOf(0.0));
                    
                    logger.info("Activada ruta pendiente {} para camión {}", rutaParaActivar.getCodigo(), camion.getCodigo());
                    
                    // Evitar cambiar el estado del camión, continuará en ruta
                    return;
                } else {
                    // El camión está marcado en ruta pero no tiene rutas activas ni pendientes
                    logger.info("Camión {} sin rutas, cambiando a disponible", camion.getCodigo());
                    camion.setEstado(0); // Cambiar a disponible
                    camionRepository.save(camion);
                    return;
                }
            }
            
            // Procesar la primera ruta activa (asumiendo que un camión solo puede tener una ruta activa a la vez)
            Ruta rutaActual = rutasActivas.get(0);
            List<NodoRuta> nodos = rutaActual.getNodos();
            
            if (nodos == null || nodos.size() < 2) {
                logger.warn("Ruta {} no tiene suficientes nodos", rutaActual.getCodigo());
                return; // La ruta debe tener al menos un origen y un destino
            }
            
            logger.debug("Procesando movimiento de camión {} en ruta {} con {} nodos", camion.getCodigo(), rutaActual.getCodigo(), nodos.size());
            
            // Inicializar el progreso si es la primera vez que procesamos esta ruta
            if (!progresoNodoActual.containsKey(rutaActual.getId())) {
                progresoNodoActual.put(rutaActual.getId(), Double.valueOf(0.0)); // Comenzamos en 0%
                logger.info("Inicializando progreso para ruta {}", rutaActual.getCodigo());
            }
            
            // Determinar el nodo actual y siguiente
            int indiceNodoActual = encontrarIndiceNodoActual(rutaActual, nodos);
            
            // Si hemos llegado al último nodo, la ruta está completa
            if (indiceNodoActual >= nodos.size() - 1) {
                logger.info("Ruta {} completada", rutaActual.getCodigo());
                completarRuta(rutaActual, camion);
                return;
            }
            
            NodoRuta nodoActual = nodos.get(indiceNodoActual);
            NodoRuta nodoSiguiente = nodos.get(indiceNodoActual + 1);
            
            logger.debug("Camión {} moviéndose del nodo {} al nodo {} - Progreso: {}", camion.getCodigo(), indiceNodoActual, (indiceNodoActual + 1), progresoNodoActual.get(rutaActual.getId()));
            
            // Aumentar el progreso hacia el siguiente nodo
            double progreso = progresoNodoActual.get(rutaActual.getId());
            double incremento = (5.0 * factorVelocidad) / 100.0; // Avance del 5% * factor de velocidad
            progreso += incremento;
            
            // Verificar si llegamos al siguiente nodo
            if (progreso >= 1.0) {
                progreso = 0.0; // Reiniciar progreso para el próximo tramo
                
                // Actualizar posición del camión al llegar al nodo siguiente
                camion.setPosX(nodoSiguiente.getPosX());
                camion.setPosY(nodoSiguiente.getPosY());
                
                // Consumir combustible por el tramo recorrido
                double distanciaRecorrida = nodoActual.distanciaA(nodoSiguiente);
                double consumo = camion.calcularConsumoCombustible(distanciaRecorrida);
                camion.setCombustibleActual(Math.max(0, camion.getCombustibleActual() - consumo));
                
                // Si es un nodo cliente, procesar entrega
                if ("CLIENTE".equals(nodoSiguiente.getTipo()) && !nodoSiguiente.isEntregado() && nodoSiguiente.getPedido() != null) {
                    procesarEntrega(camion, rutaActual, nodoSiguiente);
                }
                
                // Si es un nodo de tipo ALMACEN y no es el primero, podría ser retorno al almacén para recargar
                if ("ALMACEN".equals(nodoSiguiente.getTipo()) && indiceNodoActual > 0) {
                    recargarCamion(camion);
                }
                
                // Guardar el camión con su nueva posición
                camionRepository.save(camion);
                
                // Enviar notificación de llegada a nodo
                enviarNotificacionLlegadaNodo(camion, nodoSiguiente, rutaActual);
                
            } else {
                // Estamos en medio del camino entre nodos, calcular posición intermedia
                calcularPosicionIntermedia(camion, nodoActual, nodoSiguiente, progreso);
                camionRepository.save(camion);
                
                logger.debug("Camión {} en posición intermedia X:{} Y:{} - Progreso: {}", camion.getCodigo(), camion.getPosX(), camion.getPosY(), progreso);
            }
            
            // Guardar el progreso actualizado
            progresoNodoActual.put(rutaActual.getId(), Double.valueOf(progreso));
            
        } catch (Exception e) {
            logger.error("Error procesando movimiento de camión {}: {}", camion.getId(), e.getMessage(), e);
        }
    }
    
    // Encuentra el índice del nodo actual en la ruta
    private int encontrarIndiceNodoActual(Ruta ruta, List<NodoRuta> nodos) {
        // Si la ruta acaba de iniciar, estamos en el primer nodo
        if (progresoNodoActual.get(ruta.getId()) == 0.0) {
            return 0;
        }
        
        // Buscar el último nodo que hemos visitado completamente
        int indice = 0;
        for (int i = 0; i < nodos.size() - 1; i++) {
            NodoRuta nodo = nodos.get(i);
            
            // Si el camión está exactamente en este nodo y el progreso es 0, estamos iniciando desde aquí
            if (indice == i && progresoNodoActual.get(ruta.getId()) == 0.0) {
                return i;
            }
            
            // Si la posición del camión coincide con la del nodo siguiente, hemos pasado este nodo
            Camion camion = ruta.getCamion();
            NodoRuta nodoSiguiente = nodos.get(i + 1);
            
            if (camion.getPosX() == nodoSiguiente.getPosX() && camion.getPosY() == nodoSiguiente.getPosY()) {
                indice = i + 1;
            }
        }
        
        return indice;
    }
    
    // Calcular posición intermedia entre dos nodos
    private void calcularPosicionIntermedia(Camion camion, NodoRuta origen, NodoRuta destino, double progreso) {
        try {
            // Validar coordenadas para evitar NaN
            if (Double.isNaN(origen.getPosX()) || Double.isNaN(origen.getPosY()) || 
                Double.isNaN(destino.getPosX()) || Double.isNaN(destino.getPosY())) {
                logger.warn("Coordenadas NaN detectadas en nodos de ruta");
                return;
            }
            
            // Calculamos la posición intermedia usando interpolación lineal
            double deltaX = destino.getPosX() - origen.getPosX();
            double deltaY = destino.getPosY() - origen.getPosY();
            
            // Movimiento directo (interpolación lineal simple)
            double nuevaX = origen.getPosX() + (deltaX * progreso);
            double nuevaY = origen.getPosY() + (deltaY * progreso);
            
            // Actualizar posición del camión
            camion.setPosX(nuevaX);
            camion.setPosY(nuevaY);
            
        } catch (Exception e) {
            logger.error("Error calculando posición intermedia: {}", e.getMessage(), e);
        }
    }
    
    // Procesa la entrega de un pedido
    private void procesarEntrega(Camion camion, Ruta ruta, NodoRuta nodo) {
        // Marcar nodo como entregado
        nodo.setEntregado(true);
        nodo.setTiempoLlegadaReal(LocalDateTime.now());
        
        // Liberar capacidad del camión
        camion.liberarCapacidad(nodo.getVolumenGLP());
        
        // Si el pedido está presente, actualizar su estado
        Pedido pedido = nodo.getPedido();
        if (pedido != null) {
            // Verificar si todas las entregas para este pedido han sido completadas
            boolean todasEntregasCompletadas = true;
            double volumenTotalEntregado = 0.0;
            
            // Buscar este pedido en todas las rutas activas
            List<Ruta> rutasConPedido = rutaRepository.findByEstadoIn(Arrays.asList(1, 2)); // En curso o completada
            for (Ruta r : rutasConPedido) {
                for (NodoRuta n : r.getNodos()) {
                    if (n.getPedido() != null && n.getPedido().getId().equals(pedido.getId())) {
                        if (!n.isEntregado()) {
                            todasEntregasCompletadas = false;
                        } else {
                            volumenTotalEntregado += n.getVolumenGLP();
                        }
                    }
                }
            }
            
            // Si todas las entregas están completadas o el volumen entregado es suficiente, marcar pedido como completado
            if (todasEntregasCompletadas || Math.abs(volumenTotalEntregado - pedido.getVolumenGLPAsignado()) < 0.01) {
                pedido.setEstado(2); // 2 = Entregado
                pedido.setFechaEntregaReal(LocalDateTime.now());
                pedidoRepository.save(pedido);
            }
            
            // Crear una notificación de entrega
            Map<String, Object> notificacion = new HashMap<>();
            notificacion.put("tipo", "entrega");
            notificacion.put("camionId", camion.getId());
            notificacion.put("camionCodigo", camion.getCodigo());
            notificacion.put("pedidoId", pedido.getId());
            notificacion.put("pedidoCodigo", pedido.getCodigo());
            notificacion.put("posX", camion.getPosX());
            notificacion.put("posY", camion.getPosY());
            notificacion.put("volumenEntregado", Optional.of(nodo.getVolumenGLP()));
            notificacion.put("porcentajeEntregado", Optional.of(nodo.getPorcentajePedido()));
            notificacion.put("fechaEntrega", LocalDateTime.now().toString());
            
            // Enviar notificación por WebSocket
            messagingTemplate.convertAndSend("/topic/entregas", notificacion);
        }
    }
    
    // Recarga combustible y GLP si el camión está en un almacén
    private void recargarCamion(Camion camion) {
        // Buscar si hay un almacén en esta posición
        List<Almacen> almacenes = almacenRepository.findByPosXAndPosY(camion.getPosX(), camion.getPosY());
        
        if (!almacenes.isEmpty()) {
            Almacen almacen = almacenes.get(0);
            
            // Recargar combustible al máximo
            double combustibleNecesario = camion.getCapacidadTanque() - camion.getCombustibleActual();
            if (combustibleNecesario > 0) {
                camion.recargarCombustible(combustibleNecesario);
            }
            
            // Recargar GLP si es un almacén de GLP
            if (true) {
                double glpNecesario = camion.getCapacidad() - (camion.getCapacidad() - camion.getCapacidadDisponible());
                if (glpNecesario > 0) {
                    camion.recargarGLP(glpNecesario);
                }
            }
            
            // Actualizar el último almacén visitado
            camion.setUltimoAlmacen(almacen);
            camion.setFechaUltimaCarga(LocalDateTime.now());
            
            // Enviar notificación de recarga
            Map<String, Object> notificacion = new HashMap<>();
            notificacion.put("tipo", "recarga");
            notificacion.put("camionId", camion.getId());
            notificacion.put("camionCodigo", camion.getCodigo());
            notificacion.put("almacenId", almacen.getId());
            notificacion.put("almacenNombre", almacen.getNombre());
            notificacion.put("combustibleRecargado", Optional.of(combustibleNecesario));
            notificacion.put("combustibleActual", Optional.of(camion.getCombustibleActual()));
            
            messagingTemplate.convertAndSend("/topic/recargas", notificacion);
        }
    }
    
    // Marca una ruta como completada
    private void completarRuta(Ruta ruta, Camion camion) {
        ruta.setEstado(2); // 2 = Completada
        ruta.setFechaFinRuta(LocalDateTime.now());
        rutaRepository.save(ruta);
        
        // Verificar si hay más rutas pendientes para este camión
        List<Ruta> rutasPendientes = rutaRepository.findByCamionIdAndEstado(camion.getId(), 0); // 0 = Planificada
        
        if (!rutasPendientes.isEmpty()) {
            // Iniciar la siguiente ruta planificada
            Ruta siguienteRuta = rutasPendientes.get(0);
            siguienteRuta.setEstado(1); // 1 = En curso
            siguienteRuta.setFechaInicioRuta(LocalDateTime.now());
            rutaRepository.save(siguienteRuta);
            
            // Inicializar progreso para la nueva ruta
            progresoNodoActual.put(siguienteRuta.getId(), Double.valueOf(0.0));
            
            // Notificar inicio de nueva ruta
            Map<String, Object> notificacion = new HashMap<>();
            notificacion.put("tipo", "inicioRuta");
            notificacion.put("camionId", camion.getId());
            notificacion.put("camionCodigo", camion.getCodigo());
            notificacion.put("rutaId", siguienteRuta.getId());
            notificacion.put("rutaCodigo", siguienteRuta.getCodigo());
            
            messagingTemplate.convertAndSend("/topic/rutas", notificacion);
        } else {
            // No hay más rutas, cambiar estado del camión a disponible
            camion.setEstado(0); // 0 = Disponible
            camionRepository.save(camion);
            
            // Notificar fin de todas las rutas
            Map<String, Object> notificacion = new HashMap<>();
            notificacion.put("tipo", "finRutas");
            notificacion.put("camionId", camion.getId());
            notificacion.put("camionCodigo", camion.getCodigo());
            
            messagingTemplate.convertAndSend("/topic/rutas", notificacion);
        }
    }
    
    // Envía una notificación cuando un camión llega a un nodo de la ruta
    private void enviarNotificacionLlegadaNodo(Camion camion, NodoRuta nodo, Ruta ruta) {
        Map<String, Object> notificacion = new HashMap<>();
        notificacion.put("tipo", "llegadaNodo");
        notificacion.put("camionId", camion.getId());
        notificacion.put("camionCodigo", camion.getCodigo());
        notificacion.put("rutaId", ruta.getId());
        notificacion.put("rutaCodigo", ruta.getCodigo());
        notificacion.put("nodoId", nodo.getId());
        notificacion.put("nodoTipo", nodo.getTipo());
        notificacion.put("posX", nodo.getPosX());
        notificacion.put("posY", nodo.getPosY());
        
        if (nodo.getPedido() != null) {
            notificacion.put("pedidoId", nodo.getPedido().getId());
            notificacion.put("pedidoCodigo", nodo.getPedido().getCodigo());
        }
        
        messagingTemplate.convertAndSend("/topic/nodos", notificacion);
    }
    
    @Scheduled(fixedRate = 1000)
    public void enviarActualizacionProgramada() {
        // Genera estadísticas como lo haces en actualizarSimulacion
        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("camionesTotal", Optional.of(camionRepository.count()));
        estadisticas.put("camionesEnRuta", Optional.of(camionRepository.findByEstado(1).size()));
        estadisticas.put("almacenesTotal", Optional.of(almacenRepository.count()));
        estadisticas.put("pedidosTotal", Optional.of(pedidoRepository.count()));
        estadisticas.put("pedidosPendientes", Optional.of(pedidoRepository.findByEstado(0).size()));
        estadisticas.put("pedidosEnRuta", Optional.of(pedidoRepository.findByEstado(1).size()));
        estadisticas.put("pedidosEntregados", Optional.of(pedidoRepository.findByEstado(2).size()));
        estadisticas.put("rutasTotal", Optional.of(rutaRepository.count()));
        estadisticas.put("rutasActivas", Optional.of(rutaRepository.findByEstado(1).size()));
    
        // Llama al método que realmente hace el trabajo
        enviarActualizacionPosiciones(estadisticas);
    }
    
    public void enviarActualizacionPosiciones(Map<String, Object> estadisticas) {
        try {
            long startTime = System.currentTimeMillis();
            
            // Medida de seguridad para evitar bloqueos indefinidos
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<Map<String, Object>> future = executor.submit(() -> obtenerPosicionesSimplificadas());
            
            Map<String, Object> posiciones;
            try {
                posiciones = future.get(10, TimeUnit.SECONDS); // Timeout de 10 segundos
                
                // Añade timestamp para rastreo en el cliente
                posiciones.put("timestamp", LocalDateTime.now().toString());
                
                // Evita enviar si no hay datos importantes
                boolean hayDatos = 
                    (posiciones.containsKey("camiones") && !((List<?>)posiciones.get("camiones")).isEmpty()) ||
                    (posiciones.containsKey("almacenes") && !((List<?>)posiciones.get("almacenes")).isEmpty()) ||
                    (posiciones.containsKey("pedidos") && !((List<?>)posiciones.get("pedidos")).isEmpty()) ||
                    (posiciones.containsKey("rutas") && !((List<?>)posiciones.get("rutas")).isEmpty());
                
                if (hayDatos) {
                    messagingTemplate.convertAndSend("/topic/posiciones", posiciones);
                } else {
                    // Si no hay datos, enviamos un mensaje mínimo para que el cliente sepa que estamos vivos
                    Map<String, Object> heartbeat = new HashMap<>();
                    heartbeat.put("timestamp", LocalDateTime.now().toString());
                    heartbeat.put("status", "heartbeat");
                    messagingTemplate.convertAndSend("/topic/posiciones", heartbeat);
                }
                
                long endTime = System.currentTimeMillis();
                if (endTime - startTime > 500) { // Solo logueamos si toma más de 500ms
                    logger.info("Actualización posiciones completada en {}ms", (endTime - startTime));
                }
            } catch (TimeoutException e) {
                logger.error("Timeout obteniendoPosicionesSimplificadas (>10s): {}", e.getMessage());
                // Enviar mensaje de error al cliente
                Map<String, Object> error = new HashMap<>();
                error.put("timestamp", LocalDateTime.now().toString());
                error.put("error", "Timeout procesando datos de posición");
                error.put("status", "error");
                messagingTemplate.convertAndSend("/topic/posiciones", error);
            } catch (Exception e) {
                logger.error("Error obteniendoPosicionesSimplificadas: {}", e.getMessage(), e);
                // Enviar mensaje de error al cliente
                Map<String, Object> error = new HashMap<>();
                error.put("timestamp", LocalDateTime.now().toString());
                error.put("error", "Error procesando datos: " + e.getMessage());
                error.put("status", "error");
                messagingTemplate.convertAndSend("/topic/posiciones", error);
            } finally {
                executor.shutdownNow(); // Asegurarse de cerrar el executor
            }
        } catch (Exception e) {
            logger.error("Error general en enviarActualizacionPosiciones: {}", e.getMessage(), e);
            try {
                // Último intento para informar al cliente
                Map<String, Object> error = new HashMap<>();
                error.put("timestamp", LocalDateTime.now().toString());
                error.put("error", "Error crítico del sistema: " + e.getMessage());
                error.put("status", "criticalError");
                messagingTemplate.convertAndSend("/topic/posiciones", error);
            } catch (Exception ex) {
                logger.error("No se pudo enviar mensaje de error al cliente: {}", ex.getMessage(), ex);
            }
        }
    }
    
    // NUEVO MÉTODO: Obtiene posiciones simplificadas para evitar el problema de anidamiento JSON
    public Map<String, Object> obtenerPosicionesSimplificadas() {
        Map<String, Object> result = new HashMap<>();
        long startGlobal = System.currentTimeMillis();
        
        try {
            logger.debug("Iniciando obtenerPosicionesSimplificadas: {}", LocalDateTime.now());
            
            // 1. PROCESAR CAMIONES
            long startCamiones = System.currentTimeMillis();
            try {
                List<Map<String, Object>> camionesList = new ArrayList<>();
                
                for (Camion camion : camionRepository.findAll()) {
                    try {
                        Map<String, Object> camionMap = new HashMap<>();
                        camionMap.put("id", camion.getId());
                        camionMap.put("codigo", camion.getCodigo());
                        camionMap.put("estado", Optional.of(camion.getEstado()));
                        camionMap.put("capacidad", camion.getCapacidad());
                        camionMap.put("combustibleActual", Optional.of(camion.getCombustibleActual()));
                        camionMap.put("posX", camion.getPosX());
                        camionMap.put("posY", camion.getPosY());
                        
                        // Solo añadir camiones con posición válida
                        if (!Double.isNaN(camion.getPosX()) && !Double.isNaN(camion.getPosY())) {
                            camionesList.add(camionMap);
                        }
                    } catch (Exception e) {
                        logger.error("Al procesar camión {}: {}", camion.getId(), e.getMessage(), e);
                    }
                }
                result.put("camiones", camionesList);
                logger.debug("Camiones procesados: {} en {}ms", camionesList.size(), (System.currentTimeMillis() - startCamiones));
            } catch (Exception e) {
                logger.error("Al procesar camiones: {}", e.getMessage(), e);
                result.put("camiones", new ArrayList<>());
                result.put("error_camiones", e.getMessage());
            }

            // 2. PROCESAR ALMACENES
            long startAlmacenes = System.currentTimeMillis();
            try {
                List<Map<String, Object>> almacenesList = new ArrayList<>();
                
                for (Almacen almacen : almacenRepository.findAll()) {
                    try {
                        Map<String, Object> almacenMap = new HashMap<>();
                        almacenMap.put("id", almacen.getId());
                        almacenMap.put("nombre", almacen.getNombre());
                        almacenMap.put("posX", almacen.getPosX());
                        almacenMap.put("posY", almacen.getPosY());
                        
                        // Solo añadir almacenes con posición válida
                        if (!Double.isNaN(almacen.getPosX()) && !Double.isNaN(almacen.getPosY())) {
                            almacenesList.add(almacenMap);
                        }
                    } catch (Exception e) {
                        logger.error("Al procesar almacén {}: {}", almacen.getId(), e.getMessage(), e);
                    }
                }
                result.put("almacenes", almacenesList);
                logger.debug("Almacenes procesados: {} en {}ms", almacenesList.size(), (System.currentTimeMillis() - startAlmacenes));
            } catch (Exception e) {
                logger.error("Al procesar almacenes: {}", e.getMessage(), e);
                result.put("almacenes", new ArrayList<>());
                result.put("error_almacenes", e.getMessage());
            }

            // 3. PROCESAR PEDIDOS
            long startPedidos = System.currentTimeMillis();
            try {
                List<Map<String, Object>> pedidosList = new ArrayList<>();
                
                for (Pedido pedido : pedidoRepository.findByEstadoIn(Arrays.asList(0, 1))) {
                    try {
                        if (pedido.getCliente() == null) {
                            continue; // Ignorar pedidos sin cliente
                        }
                        
                        Map<String, Object> pedidoMap = new HashMap<>();
                        pedidoMap.put("id", pedido.getId());
                        pedidoMap.put("estado", Optional.of(pedido.getEstado()));
                        pedidoMap.put("m3", Optional.of(pedido.getVolumenGLPAsignado()));
                        
                        // Solo enviar la ubicación del cliente si tenemos el cliente
                        if (pedido.getCliente() != null) {
                            pedidoMap.put("clienteId", pedido.getCliente().getId());
                            pedidoMap.put("posX", pedido.getCliente().getPosX());
                            pedidoMap.put("posY", pedido.getCliente().getPosY());
                        }
                        
                        // Solo añadir pedidos con posición válida
                        if (pedido.getCliente() != null && 
                            pedido.getCliente() != null &&
                            pedido.getCliente().getPosX() != Double.NaN &&
                            pedido.getCliente().getPosY() != Double.NaN) {
                            pedidosList.add(pedidoMap);
                        }
                    } catch (Exception e) {
                        logger.error("Al procesar pedido {}: {}", pedido.getId(), e.getMessage(), e);
                    }
                }
                result.put("pedidos", pedidosList);
                logger.debug("Pedidos procesados: {} en {}ms", pedidosList.size(), (System.currentTimeMillis() - startPedidos));
            } catch (Exception e) {
                logger.error("Al procesar pedidos: {}", e.getMessage(), e);
                result.put("pedidos", new ArrayList<>());
                result.put("error_pedidos", e.getMessage());
            }

            // 4. PROCESAR RUTAS
            long startRutas = System.currentTimeMillis();
            try {
                List<Map<String, Object>> rutasList = new ArrayList<>();
                
                // Usar el nuevo método que incluye un FETCH JOIN para evitar LazyInitializationException
                for (Ruta ruta : rutaRepository.findByEstadoWithNodos(1)) {
                    try {
                        Map<String, Object> rutaMap = new HashMap<>();
                        rutaMap.put("id", ruta.getId());
                        rutaMap.put("codigo", ruta.getCodigo());
                        rutaMap.put("estado", ruta.getEstado());
                        
                        // Ahora los nodos ya vienen inicializados, evitando el LazyInitializationException
                        List<NodoRuta> nodos = ruta.getNodos();
                        if (nodos != null && !nodos.isEmpty()) {
                            List<Map<String, Object>> nodosList = new ArrayList<>();
                            
                            for (NodoRuta nodo : nodos) {
                                try {
                                    if (Double.isNaN(nodo.getPosX()) || Double.isNaN(nodo.getPosY())) {
                                        continue; // Ignorar nodos sin coordenadas
                                    }
                                    
                                    Map<String, Object> nodoMap = new HashMap<>();
                                    nodoMap.put("id", nodo.getId());
                                    nodoMap.put("orden", nodo.getOrden());
                                    nodoMap.put("posX", nodo.getPosX());
                                    nodoMap.put("posY", nodo.getPosY());
                                    nodoMap.put("tipo", nodo.getTipo());
                                    
                                    nodosList.add(nodoMap);
                                } catch (Exception e) {
                                    logger.error("Al procesar nodo {} de ruta {}: {}", nodo.getId(), ruta.getId(), e.getMessage(), e);
                                }
                            }
                            
                            // Ordenar nodos por el campo 'orden'
                            nodosList.sort(Comparator.comparing(m -> ((Integer) m.get("orden"))));
                            rutaMap.put("nodos", nodosList);
                        } else {
                            rutaMap.put("nodos", new ArrayList<>());
                        }
                        
                        // Solo añadir rutas con al menos un nodo
                        if (rutaMap.containsKey("nodos") && !((List<?>) rutaMap.get("nodos")).isEmpty()) {
                            rutasList.add(rutaMap);
                        }
                    } catch (Exception e) {
                        logger.error("Al procesar ruta {}: {}", ruta.getId(), e.getMessage(), e);
                    }
                }
                result.put("rutas", rutasList);
                logger.debug("Rutas procesadas: {} en {}ms", rutasList.size(), (System.currentTimeMillis() - startRutas));
            } catch (Exception e) {
                logger.error("Al procesar rutas: {}", e.getMessage(), e);
                result.put("rutas", new ArrayList<>());
                result.put("error_rutas", e.getMessage());
            }
            
            logger.debug("Finalizado obtenerPosicionesSimplificadas en {}ms", (System.currentTimeMillis() - startGlobal));
            return result;
            
        } catch (Exception e) {
            logger.error("Error general obteniendo posiciones: {}", e.getMessage(), e);
            
            // Devolver al menos un objeto vacío pero válido
            result.put("error", "Error general obteniendo posiciones: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now().toString());
            return result;
        }
    }
    
    // Obtiene el estado actual de la simulación
    public Map<String, Object> obtenerEstadoSimulacion() {
        Map<String, Object> estado = new HashMap<>();
        estado.put("simulacionEnCurso", simulacionEnCurso);
        estado.put("factorVelocidad", factorVelocidad);
        
        // Contar camiones en ruta
        List<Camion> camionesEnRuta = camionRepository.findByEstado(1);
        estado.put("camionesEnRuta", camionesEnRuta.size());
        
        // Contar rutas activas
        List<Ruta> rutasActivas = rutaRepository.findByEstado(1);
        estado.put("rutasActivas", rutasActivas.size());
        
        // Información adicional sobre las rutas activas
        if (!rutasActivas.isEmpty()) {
            List<Map<String, Object>> rutasInfo = new ArrayList<>();
            for (Ruta ruta : rutasActivas) {
                Map<String, Object> rutaInfo = new HashMap<>();
                rutaInfo.put("id", ruta.getId());
                rutaInfo.put("codigo", ruta.getCodigo());
                if (ruta.getCamion() != null) {
                    rutaInfo.put("camionCodigo", ruta.getCamion().getCodigo());
                }
                if (progresoNodoActual.containsKey(ruta.getId())) {
                    rutaInfo.put("progreso", progresoNodoActual.get(ruta.getId()));
                }
                rutasInfo.add(rutaInfo);
            }
            estado.put("detalleRutasActivas", rutasInfo);
        }
        
        // Contar pedidos pendientes y en ruta
        List<Pedido> pedidosPendientes = pedidoRepository.findByEstado(0);
        List<Pedido> pedidosEnRuta = pedidoRepository.findByEstado(1);
        estado.put("pedidosPendientes", pedidosPendientes.size());
        estado.put("pedidosEnRuta", pedidosEnRuta.size());
        
        // Información de diagnóstico
        estado.put("schedulerActivo", scheduler != null && !scheduler.isShutdown());
        estado.put("timestamp", LocalDateTime.now().toString());
        
        return estado;
    }
    
    // Crea una respuesta estándar para la API
    private Map<String, Object> crearRespuesta(String mensaje) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", mensaje);
        respuesta.put("fecha", LocalDateTime.now().toString());
        respuesta.put("status", "success");
        return respuesta;
    }
}