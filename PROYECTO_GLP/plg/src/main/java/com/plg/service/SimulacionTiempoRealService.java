package com.plg.service;

import com.plg.entity.*;
import com.plg.entity.EstadoCamion;
import com.plg.entity.EstadoPedido;
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
        List<Camion> camionesEnRuta = camionRepository.findByEstado(EstadoCamion.EN_RUTA); // Camiones en ruta
        
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
            List<Camion> camionesEnRuta = camionRepository.findByEstado(EstadoCamion.EN_RUTA); // 1 = En ruta
            // Agregar también los camiones en estado 0 (disponible) para verificar si tienen rutas pendientes
            List<Camion> camionesDisponibles = camionRepository.findByEstado(EstadoCamion.EN_RUTA);
            
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
            estadisticas.put("camionesEnRuta", camionRepository.findByEstado(EstadoCamion.EN_RUTA).size());
            estadisticas.put("almacenesTotal", almacenRepository.count());
            estadisticas.put("pedidosTotal", pedidoRepository.count());
            estadisticas.put("pedidosPendientes", pedidoRepository.findByEstado(EstadoPedido.REGISTRADO).size());
            estadisticas.put("pedidosEnRuta", pedidoRepository.findByEstado(EstadoPedido.EN_RUTA).size());
            estadisticas.put("pedidosEntregados", pedidoRepository.findByEstado(EstadoPedido.ENTREGADO_TOTALMENTE).size());
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
            List<Ruta> rutasActivas = rutaRepository.findByCamionIdAndEstadoWithNodos(camion.getId(), 1); // Usar findByCamionIdAndEstadoWithNodos
            
            if (rutasActivas.isEmpty()) {
                logger.warn("Camión {} está en ruta (estado {}) pero no tiene rutas activas", camion.getCodigo(), camion.getEstado());
                
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
                    camion.setEstado(EstadoCamion.DISPONIBLE); // Cambiar a disponible
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
                
                // Para asegurar que el camión empiece en el primer nodo de la ruta
                NodoRuta nodoInicial = nodos.get(0);
                camion.setPosX(nodoInicial.getPosX());
                camion.setPosY(nodoInicial.getPosY());
                camionRepository.save(camion);
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
            
            // Verificar si hay combustible suficiente para el movimiento
            double distanciaRecorrida = nodoActual.distanciaA(nodoSiguiente) * incremento;
            double consumoPrevisto = camion.calcularConsumoCombustible(distanciaRecorrida);
            
            if (camion.getCombustibleActual() < consumoPrevisto) {
                // No hay combustible suficiente para continuar
                if (camion.getEstado() != EstadoCamion.SIN_COMBUSTIBLE) { // Solo cambiar si no estaba ya marcado sin combustible
                    camion.setEstado(EstadoCamion.SIN_COMBUSTIBLE); // Sin combustible
                    camionRepository.save(camion);
                    
                    // Enviar notificación de falta de combustible
                    Map<String, Object> notificacion = new HashMap<>();
                    notificacion.put("tipo", "sinCombustible");
                    notificacion.put("camionId", camion.getId());
                    notificacion.put("camionCodigo", camion.getCodigo());
                    notificacion.put("posX", camion.getPosX());
                    notificacion.put("posY", camion.getPosY());
                    notificacion.put("combustibleRestante", camion.getCombustibleActual());
                    notificacion.put("mensaje", "El camión " + camion.getCodigo() + " se ha quedado sin combustible");
                    messagingTemplate.convertAndSend("/topic/alertas", notificacion);
                    
                    logger.warn("Camión {} sin combustible en pos X:{} Y:{}", camion.getCodigo(), camion.getPosX(), camion.getPosY());
                }
                return; // No seguir procesando el camión
            }
            
            // Hay suficiente combustible, continuar con el movimiento
            progreso += incremento;
            
            // Verificar si llegamos al siguiente nodo
            if (progreso >= 1.0) {
                progreso = 0.0; // Reiniciar progreso para el próximo tramo
                
                // Actualizar posición del camión al llegar al nodo siguiente
                camion.setPosX(nodoSiguiente.getPosX());
                camion.setPosY(nodoSiguiente.getPosY());
                
                // Consumir combustible por el tramo recorrido
                double consumo = camion.calcularConsumoCombustible(distanciaRecorrida * (1.0/incremento)); // Consumo total del tramo
                camion.setCombustibleActual(Math.max(0, camion.getCombustibleActual() - consumo));
                
                // Verificar si después de consumir nos quedamos sin combustible
                if (camion.getCombustibleActual() <= 0.1) { // Un umbral mínimo para considerar "sin combustible"
                    camion.setCombustibleActual(0);
                    camion.setEstado(EstadoCamion.SIN_COMBUSTIBLE); // Sin combustible
                    
                    // Enviar notificación de falta de combustible
                    Map<String, Object> notificacion = new HashMap<>();
                    notificacion.put("tipo", "sinCombustible");
                    notificacion.put("camionId", camion.getId());
                    notificacion.put("camionCodigo", camion.getCodigo());
                    notificacion.put("posX", camion.getPosX());
                    notificacion.put("posY", camion.getPosY());
                    notificacion.put("mensaje", "El camión " + camion.getCodigo() + " se ha quedado sin combustible");
                    messagingTemplate.convertAndSend("/topic/alertas", notificacion);
                    
                    logger.warn("Camión {} sin combustible en pos X:{} Y:{}", camion.getCodigo(), camion.getPosX(), camion.getPosY());
                }
                
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
            
            logger.debug("Calculando posición intermedia para camión {} - Origen: ({},{}) Destino: ({},{}) - Progreso: {}", 
                camion.getCodigo(), origen.getPosX(), origen.getPosY(), destino.getPosX(), destino.getPosY(), progreso);
            
            // Movimiento solo horizontal y vertical (reticular), nunca diagonal
            double deltaX = destino.getPosX() - origen.getPosX();
            double deltaY = destino.getPosY() - origen.getPosY();
            
            double nuevaX = origen.getPosX();
            double nuevaY = origen.getPosY();
            
            // En un mapa reticular, primero movemos horizontalmente, luego verticalmente
            if (Math.abs(deltaX) > 0.01) { // Si hay distancia horizontal
                // Primera fase: mover horizontalmente hasta completarlo
                if (progreso <= 0.5) {
                    // De 0 a 0.5, solo movemos en X (horizontal)
                    // Ajustar el progreso para que vaya de 0 a 1 en esta fase
                    double progresoHorizontal = progreso * 2.0;
                    nuevaX = origen.getPosX() + (deltaX * progresoHorizontal);
                    nuevaY = origen.getPosY(); // Y permanece constante
                    
                    logger.debug("FASE HORIZONTAL: Camión {} - Progreso real: {}%, horizontal ajustado: {}%, Nueva posición X: {}", 
                        camion.getCodigo(), (progreso * 100), (progresoHorizontal * 100), nuevaX);
                } else {
                    // De 0.5 a 1, X ya está en destino, movemos Y
                    nuevaX = destino.getPosX(); // X ya llegó al destino
                    
                    // Ajustar el progreso para Y, de 0 a 1
                    double progresoVertical = (progreso - 0.5) * 2.0;
                    nuevaY = origen.getPosY() + (deltaY * progresoVertical);
                    
                    logger.debug("FASE VERTICAL: Camión {} - Progreso real: {}%, vertical ajustado: {}%, Nueva posición Y: {}", 
                        camion.getCodigo(), (progreso * 100), (progresoVertical * 100), nuevaY);
                }
            } else {
                // Si no hay movimiento horizontal, solo movemos verticalmente de principio a fin
                nuevaY = origen.getPosY() + (deltaY * progreso);
                logger.debug("SOLO VERTICAL: Camión {} - Progreso: {}%, Nueva posición Y: {}", 
                    camion.getCodigo(), (progreso * 100), nuevaY);
            }
            
            logger.info("Camión {} - Actualización de posición: De ({},{}) a ({},{})", 
                camion.getCodigo(), camion.getPosX(), camion.getPosY(), nuevaX, nuevaY);
            
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
        
        // Liberar capacidad del camión - lo hacemos primero directamente aquí
        // ya que tenemos la referencia correcta al camión
        double volumenEntregado = nodo.getVolumenGLP();
        camion.liberarCapacidad(volumenEntregado);
        
        // Si el pedido está presente, actualizar su estado
        Pedido pedido = nodo.getPedido();
        if (pedido != null) {
            // Registrar la entrega parcial con el volumen del nodo
            // Esta función ya no maneja liberación de capacidad del camión
            pedido.registrarEntregaParcial(camion.getCodigo(), volumenEntregado, LocalDateTime.now());
            
            // Verificar si todas las entregas para este pedido han sido completadas
            boolean todasEntregasCompletadas = true;
            double volumenTotalEntregado = 0.0;
            
            // Buscar este pedido en todas las rutas activas - Usar método con FETCH JOIN
            List<Ruta> rutasConPedido = rutaRepository.findByEstadoInWithNodos(Arrays.asList(1, 2)); // En curso o completada
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
            
            // Asegurar que los volúmenes estén actualizados
            pedido.setVolumenGLPEntregado(volumenTotalEntregado);
            pedido.setVolumenGLPPendiente(pedido.getVolumenGLPAsignado() - volumenTotalEntregado);
            
            // Si todas las entregas están completadas o el volumen entregado es suficiente, marcar pedido como completado
            if (todasEntregasCompletadas || Math.abs(volumenTotalEntregado - pedido.getVolumenGLPAsignado()) < 0.01) {
                pedido.setEstado(EstadoPedido.ENTREGADO_TOTALMENTE); // 2 = Entregado
                pedido.setFechaEntregaReal(LocalDateTime.now());
                pedido.setVolumenGLPPendiente(0); // Asegurar que no queda pendiente
            }
            
            pedidoRepository.save(pedido);
            
            // Crear una notificación de entrega
            Map<String, Object> notificacion = new HashMap<>();
            notificacion.put("tipo", "entrega");
            notificacion.put("camionId", camion.getId());
            notificacion.put("camionCodigo", camion.getCodigo());
            notificacion.put("pedidoId", pedido.getId());
            notificacion.put("pedidoCodigo", pedido.getCodigo());
            notificacion.put("posX", camion.getPosX());
            notificacion.put("posY", camion.getPosY());
            notificacion.put("volumenEntregado", Optional.of(volumenEntregado));
            notificacion.put("porcentajeEntregado", Optional.of(nodo.getPorcentajePedido()));
            notificacion.put("volumenTotalEntregado", Optional.of(volumenTotalEntregado));
            notificacion.put("volumenTotal", Optional.of(pedido.getVolumenGLPAsignado()));
            notificacion.put("fechaEntrega", LocalDateTime.now().toString());
            notificacion.put("estado", pedido.getEstado());
            
            // Enviar notificación por WebSocket
            messagingTemplate.convertAndSend("/topic/entregas", notificacion);
            
            logger.info("Pedido {} entregado por camión {} - Volumen: {}/{} m³ - Estado: {}", 
                pedido.getCodigo(), camion.getCodigo(), 
                volumenTotalEntregado, pedido.getVolumenGLPAsignado(),
                pedido.getEstado());
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
            // No hay más rutas planificadas
            // Verificar si quedan pedidos pendientes para este camión antes de marcarlo como disponible
            boolean quedanPedidos = pedidoRepository
                .findByEstadoIn(Arrays.asList(EstadoPedido.REGISTRADO, EstadoPedido.PLANIFICADO_PARCIALMENTE,
                            EstadoPedido.PLANIFICADO_TOTALMENTE)) // Pendientes o asignados
                .stream()
                .filter(p -> p.getCamion() != null) // Solo pedidos con camión asignado
                .filter(p -> p.getCamion().getId().equals(camion.getId())) // De este camión
                .filter(p -> p.getEstado() != EstadoPedido.NO_ENTREGADO_EN_TIEMPO) // No entregados
                .filter(p -> p.getVolumenGLPPendiente() > 0.01) // Con volumen pendiente significativo
                .anyMatch(p -> true); // ¿Hay alguno que cumpla todas las condiciones?
                
            if (quedanPedidos) {
                // Mantener el camión en estado "en ruta" si todavía tiene pedidos pendientes
                camion.setEstado(EstadoCamion.EN_RUTA); // 1 = En ruta
                logger.info("Camión {} sigue en ruta porque tiene pedidos pendientes", camion.getCodigo());
            } else {
                // No hay pedidos pendientes, el camión debe regresar al almacén central
                camion.setEstado(EstadoCamion.DISPONIBLE); // 0 = Disponible
                // Posición del almacén central (normalmente en 12,8)
                // Si existe un almacén central en la BD, usamos su posición
                List<Almacen> almacenesCentrales = almacenRepository.findByEsCentral(true);
                if (!almacenesCentrales.isEmpty()) {
                    Almacen almacenCentral = almacenesCentrales.get(0);
                    camion.setPosX(almacenCentral.getPosX());
                    camion.setPosY(almacenCentral.getPosY());
                } else {
                    // Posición predeterminada si no hay almacén central
                    camion.setPosX(12); 
                    camion.setPosY(8);
                }
                logger.info("Camión {} marcado como disponible y posicionado en almacén central", camion.getCodigo());
            }
            
            camionRepository.save(camion);
            
            // Notificar fin de todas las rutas
            Map<String, Object> notificacion = new HashMap<>();
            notificacion.put("tipo", "finRutas");
            notificacion.put("camionId", camion.getId());
            notificacion.put("camionCodigo", camion.getCodigo());
            notificacion.put("nuevoEstado", camion.getEstado());
            notificacion.put("nuevoEstadoTexto", camion.getEstadoTexto());
            notificacion.put("posX", camion.getPosX());
            notificacion.put("posY", camion.getPosY());
            
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
        estadisticas.put("camionesEnRuta", Optional.of(camionRepository.findByEstado(EstadoCamion.EN_RUTA).size()));
        estadisticas.put("almacenesTotal", Optional.of(almacenRepository.count()));
        estadisticas.put("pedidosTotal", Optional.of(pedidoRepository.count()));
        estadisticas.put("pedidosPendientes", Optional.of(pedidoRepository.findByEstado(EstadoPedido.REGISTRADO).size()));
        estadisticas.put("pedidosEnRuta", Optional.of(pedidoRepository.findByEstado(EstadoPedido.EN_RUTA).size()));
        estadisticas.put("pedidosEntregados", Optional.of(pedidoRepository.findByEstado(EstadoPedido.ENTREGADO_TOTALMENTE).size()));
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
                List<Map<String, Object>> camionesEnRutaList = new ArrayList<>();
                List<Map<String, Object>> camionesDisponiblesList = new ArrayList<>();
                List<Map<String, Object>> camionesSinCombustibleList = new ArrayList<>();
                List<Map<String, Object>> camionesOtrosList = new ArrayList<>();
                
                // Obtener camiones por estado
                List<Camion> enRuta = camionRepository.findByEstado(EstadoCamion.EN_RUTA); // En ruta
                List<Camion> disponibles = camionRepository.findByEstado(EstadoCamion.DISPONIBLE); // Disponibles
                List<Camion> sinCombustible = camionRepository.findByEstado(EstadoCamion.SIN_COMBUSTIBLE); // Sin combustible
                List<Camion> otros = new ArrayList<>(); // Otros estados (mantenimiento, averiado)
                otros.addAll(camionRepository.findByEstado(EstadoCamion.EN_MANTENIMIENTO_CORRECTIVO)); // En mantenimiento
                otros.addAll(camionRepository.findByEstado(EstadoCamion.INMOVILIZADO_POR_AVERIA)); // Averiado
                
                // Procesar camiones en ruta
                for (Camion camion : enRuta) {
                    try {
                        Map<String, Object> camionMap = new HashMap<>();
                        camionMap.put("id", camion.getId());
                        camionMap.put("codigo", camion.getCodigo());
                        camionMap.put("estado", camion.getEstado());
                        camionMap.put("estadoTexto", camion.getEstadoTexto());
                        camionMap.put("capacidad", camion.getCapacidad());
                        camionMap.put("combustibleActual", camion.getCombustibleActual());
                        camionMap.put("posX", camion.getPosX());
                        camionMap.put("posY", camion.getPosY());
                        
                        // Solo añadir camiones con posición válida
                        if (!Double.isNaN(camion.getPosX()) && !Double.isNaN(camion.getPosY())) {
                            camionesEnRutaList.add(camionMap);
                        }
                    } catch (Exception e) {
                        logger.error("Al procesar camión en ruta {}: {}", camion.getId(), e.getMessage(), e);
                    }
                }
                
                // Procesar camiones disponibles
                for (Camion camion : disponibles) {
                    try {
                        Map<String, Object> camionMap = new HashMap<>();
                        camionMap.put("id", camion.getId());
                        camionMap.put("codigo", camion.getCodigo());
                        camionMap.put("estado", camion.getEstado());
                        camionMap.put("estadoTexto", camion.getEstadoTexto());
                        camionMap.put("capacidad", camion.getCapacidad());
                        camionMap.put("combustibleActual", camion.getCombustibleActual());
                        camionMap.put("posX", camion.getPosX());
                        camionMap.put("posY", camion.getPosY());
                        
                        // Solo añadir camiones con posición válida
                        if (!Double.isNaN(camion.getPosX()) && !Double.isNaN(camion.getPosY())) {
                            camionesDisponiblesList.add(camionMap);
                        }
                    } catch (Exception e) {
                        logger.error("Al procesar camión disponible {}: {}", camion.getId(), e.getMessage(), e);
                    }
                }
                
                // Procesar camiones sin combustible
                for (Camion camion : sinCombustible) {
                    try {
                        Map<String, Object> camionMap = new HashMap<>();
                        camionMap.put("id", camion.getId());
                        camionMap.put("codigo", camion.getCodigo());
                        camionMap.put("estado", camion.getEstado());
                        camionMap.put("estadoTexto", camion.getEstadoTexto());
                        camionMap.put("capacidad", camion.getCapacidad());
                        camionMap.put("combustibleActual", camion.getCombustibleActual());
                        camionMap.put("posX", camion.getPosX());
                        camionMap.put("posY", camion.getPosY());
                        
                        // Solo añadir camiones con posición válida
                        if (!Double.isNaN(camion.getPosX()) && !Double.isNaN(camion.getPosY())) {
                            camionesSinCombustibleList.add(camionMap);
                        }
                    } catch (Exception e) {
                        logger.error("Al procesar camión sin combustible {}: {}", camion.getId(), e.getMessage(), e);
                    }
                }
                
                // Procesar otros camiones (mantenimiento, averiado)
                for (Camion camion : otros) {
                    try {
                        Map<String, Object> camionMap = new HashMap<>();
                        camionMap.put("id", camion.getId());
                        camionMap.put("codigo", camion.getCodigo());
                        camionMap.put("estado", camion.getEstado());
                        camionMap.put("estadoTexto", camion.getEstadoTexto());
                        camionMap.put("capacidad", camion.getCapacidad());
                        camionMap.put("combustibleActual", camion.getCombustibleActual());
                        camionMap.put("posX", camion.getPosX());
                        camionMap.put("posY", camion.getPosY());
                        
                        // Solo añadir camiones con posición válida
                        if (!Double.isNaN(camion.getPosX()) && !Double.isNaN(camion.getPosY())) {
                            camionesOtrosList.add(camionMap);
                        }
                    } catch (Exception e) {
                        logger.error("Al procesar camión en otro estado {}: {}", camion.getId(), e.getMessage(), e);
                    }
                }
                
                // Añadir todas las categorías de camiones al resultado
                result.put("camionesEnRuta", camionesEnRutaList);
                result.put("camionesDisponibles", camionesDisponiblesList);
                result.put("camionesSinCombustible", camionesSinCombustibleList);
                result.put("camionesOtros", camionesOtrosList);
                
                // También añadir una lista completa para retrocompatibilidad
                List<Map<String, Object>> camionesList = new ArrayList<>();
                camionesList.addAll(camionesEnRutaList);
                camionesList.addAll(camionesDisponiblesList);
                camionesList.addAll(camionesSinCombustibleList);
                camionesList.addAll(camionesOtrosList);
                result.put("camiones", camionesList);
                
                logger.debug("Camiones procesados: {} en ruta, {} disponibles, {} sin combustible, {} otros, {} total en {}ms", 
                    camionesEnRutaList.size(), camionesDisponiblesList.size(), camionesSinCombustibleList.size(),
                    camionesOtrosList.size(), camionesList.size(), (System.currentTimeMillis() - startCamiones));
            } catch (Exception e) {
                logger.error("Al procesar camiones: {}", e.getMessage(), e);
                result.put("camiones", new ArrayList<>());
                result.put("camionesEnRuta", new ArrayList<>());
                result.put("camionesDisponibles", new ArrayList<>());
                result.put("camionesSinCombustible", new ArrayList<>());
                result.put("camionesOtros", new ArrayList<>());
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
                
                for (Pedido pedido : pedidoRepository.findByEstadoIn(Arrays.asList(EstadoPedido.REGISTRADO, EstadoPedido.EN_RUTA))) {
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
            logger.error("Error general obteniendo posiciones: {}", e.getMessage());
            
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
        List<Camion> camionesEnRuta = camionRepository.findByEstado(EstadoCamion.EN_RUTA);
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
        List<Pedido> pedidosPendientes = pedidoRepository.findByEstado(EstadoPedido.PENDIENTE_PLANIFICACION);
        List<Pedido> pedidosEnRuta = pedidoRepository.findByEstado(EstadoPedido.EN_RUTA);
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