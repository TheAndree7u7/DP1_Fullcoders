package com.plg.service;

import com.plg.entity.*;
import com.plg.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class SimulacionTiempoRealService {

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
        
        simulacionEnCurso = true;
        scheduler = Executors.newSingleThreadScheduledExecutor();
        progresoNodoActual.clear();
        
        // Iniciar simulación - actualizar cada segundo ajustado por el factor de velocidad
        scheduler.scheduleAtFixedRate(this::actualizarSimulacion, 0, 1000 / factorVelocidad, TimeUnit.MILLISECONDS);
        
        return crearRespuesta("Simulación iniciada correctamente");
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
        respuesta.put("factorVelocidad", factorVelocidad);
        return respuesta;
    }
    
    // Método principal que se ejecuta periódicamente para actualizar la simulación
    private void actualizarSimulacion() {
        try {
            // Obtener todos los camiones en ruta
            List<Camion> camionesEnRuta = camionRepository.findByEstado(1); // 1 = En ruta
            
            // Si no hay camiones en ruta, no hay nada que simular
            if (camionesEnRuta.isEmpty()) {
                return;
            }
            
            // Procesar cada camión en ruta
            for (Camion camion : camionesEnRuta) {
                procesarMovimientoCamion(camion);
            }
            
            // Enviar actualización a los clientes conectados vía WebSocket
            enviarActualizacionPosiciones();
            
        } catch (Exception e) {
            // Registrar error pero no detener la simulación
            System.err.println("Error en la simulación: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Procesa el movimiento de un camión específico
    private void procesarMovimientoCamion(Camion camion) {
        // Obtener las rutas activas del camión
        List<Ruta> rutasActivas = rutaRepository.findByCamionIdAndEstado(camion.getId(), 1); // 1 = En curso
        
        if (rutasActivas.isEmpty()) {
            // El camión está marcado en ruta pero no tiene rutas activas
            camion.setEstado(0); // Cambiar a disponible
            camionRepository.save(camion);
            return;
        }
        
        // Procesar la primera ruta activa (asumiendo que un camión solo puede tener una ruta activa a la vez)
        Ruta rutaActual = rutasActivas.get(0);
        List<NodoRuta> nodos = rutaActual.getNodos();
        
        if (nodos.size() < 2) {
            return; // La ruta debe tener al menos un origen y un destino
        }
        
        // Inicializar el progreso si es la primera vez que procesamos esta ruta
        if (!progresoNodoActual.containsKey(rutaActual.getId())) {
            progresoNodoActual.put(rutaActual.getId(), 0.0); // Comenzamos en 0%
        }
        
        // Determinar el nodo actual y siguiente
        int indiceNodoActual = encontrarIndiceNodoActual(rutaActual, nodos);
        
        // Si hemos llegado al último nodo, la ruta está completa
        if (indiceNodoActual >= nodos.size() - 1) {
            completarRuta(rutaActual, camion);
            return;
        }
        
        NodoRuta nodoActual = nodos.get(indiceNodoActual);
        NodoRuta nodoSiguiente = nodos.get(indiceNodoActual + 1);
        
        // Aumentar el progreso hacia el siguiente nodo
        double progreso = progresoNodoActual.get(rutaActual.getId());
        double incremento = (5.0 * factorVelocidad) / 100.0; // Avance del 5% * factor de velocidad
        progreso += incremento;
        
        // Verificar si llegamos al siguiente nodo
        if (progreso >= 1.0) {
            // Hemos llegado al siguiente nodo
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
        }
        
        // Guardar el progreso actualizado
        progresoNodoActual.put(rutaActual.getId(), progreso);
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
    
    // Calcula una posición intermedia entre dos nodos
    private void calcularPosicionIntermedia(Camion camion, NodoRuta origen, NodoRuta destino, double progreso) {
        // Calculamos la posición intermedia usando interpolación lineal
        // Para un movimiento más realista, primero nos movemos en X y luego en Y (Manhattan)
        int deltaX = destino.getPosX() - origen.getPosX();
        int deltaY = destino.getPosY() - origen.getPosY();
        
        // Si hay distancia en X, primero movemos en X
        if (deltaX != 0) {
            double progresoEnX = Math.min(1.0, progreso * Math.abs(deltaX + deltaY) / Math.abs(deltaX));
            int nuevaX = origen.getPosX() + (int)(deltaX * progresoEnX);
            camion.setPosX(nuevaX);
            
            // Solo movemos en Y si hemos completado el movimiento en X
            if (progresoEnX >= 1.0) {
                double progresoEnY = (progreso * Math.abs(deltaX + deltaY) - Math.abs(deltaX)) / Math.abs(deltaY);
                int nuevaY = origen.getPosY() + (int)(deltaY * progresoEnY);
                camion.setPosY(nuevaY);
            } else {
                camion.setPosY(origen.getPosY());
            }
        } 
        // Si no hay distancia en X, solo movemos en Y
        else if (deltaY != 0) {
            int nuevaY = origen.getPosY() + (int)(deltaY * progreso);
            camion.setPosY(nuevaY);
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
            if (todasEntregasCompletadas || Math.abs(volumenTotalEntregado - pedido.getM3()) < 0.01) {
                pedido.setEstado(2); // 2 = Entregado
                pedido.setFechaEntrega(LocalDateTime.now());
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
            notificacion.put("volumenEntregado", nodo.getVolumenGLP());
            notificacion.put("porcentajeEntregado", nodo.getPorcentajePedido());
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
            notificacion.put("combustibleRecargado", combustibleNecesario);
            notificacion.put("combustibleActual", camion.getCombustibleActual());
            
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
            progresoNodoActual.put(siguienteRuta.getId(), 0.0);
            
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
    
    // Envía actualizaciones de posición simplificadas para evitar problemas de anidamiento
    private void enviarActualizacionPosiciones() {
        try {
            Map<String, Object> posiciones = obtenerPosicionesSimplificadas();
            messagingTemplate.convertAndSend("/topic/posiciones", posiciones);
        } catch (Exception e) {
            System.err.println("Error enviando actualizaciones de posición: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // NUEVO MÉTODO: Obtiene posiciones simplificadas para evitar el problema de anidamiento JSON
    public Map<String, Object> obtenerPosicionesSimplificadas() {
        Map<String, Object> posiciones = new HashMap<>();
        
        try {
            // Obtener camiones con posiciones simplificadas (sin objetos anidados)
            List<Camion> camiones = camionRepository.findAll();
            List<Map<String, Object>> camionesInfo = camiones.stream()
                .map(this::convertirCamionSimplificado)
                .collect(Collectors.toList());
            posiciones.put("camiones", camionesInfo);
            
            // Obtener almacenes
            List<Almacen> almacenes = almacenRepository.findAll();
            List<Map<String, Object>> almacenesInfo = almacenes.stream()
                .map(this::convertirAlmacenSimplificado)
                .collect(Collectors.toList());
            posiciones.put("almacenes", almacenesInfo);
            
            // Obtener pedidos pendientes o en ruta
            List<Pedido> pedidos = pedidoRepository.findByEstadoIn(Arrays.asList(0, 1)); // 0=Pendiente, 1=En ruta
            List<Map<String, Object>> pedidosInfo = pedidos.stream()
                .map(this::convertirPedidoSimplificado)
                .collect(Collectors.toList());
            posiciones.put("pedidos", pedidosInfo);
            
            // Obtener rutas activas con sus nodos (versión simplificada)
            List<Map<String, Object>> rutasSimplificadas = obtenerRutasSimplificadas();
            posiciones.put("rutas", rutasSimplificadas);
            
        } catch (Exception e) {
            System.err.println("Error obteniendo posiciones simplificadas: " + e.getMessage());
            e.printStackTrace();
            // En caso de error, devolver estructuras vacías para evitar fallos en el frontend
            posiciones.put("camiones", new ArrayList<>());
            posiciones.put("almacenes", new ArrayList<>());
            posiciones.put("pedidos", new ArrayList<>());
            posiciones.put("rutas", new ArrayList<>());
        }
        
        return posiciones;
    }
    
    // Método que solo extrae la información esencial de camiones
    private Map<String, Object> convertirCamionSimplificado(Camion camion) {
        Map<String, Object> info = new HashMap<>();
        info.put("id", camion.getId());
        info.put("codigo", camion.getCodigo());
        info.put("tipo", camion.getTipo());
        info.put("posX", camion.getPosX());
        info.put("posY", camion.getPosY());
        info.put("estado", camion.getEstado());
        info.put("estadoTexto", getEstadoCamionTexto(camion.getEstado()));
        info.put("combustibleActual", camion.getCombustibleActual());
        info.put("combustiblePorcentaje", (camion.getCombustibleActual() / camion.getCapacidadTanque()) * 100);
        info.put("capacidadDisponible", camion.getCapacidadDisponible());
        info.put("capacidadTotal", camion.getCapacidad());
        info.put("porcentajeUso", camion.getPorcentajeUso());
        
        // Si está en ruta, incluir información de la ruta activa
        if (camion.getEstado() == 1) { // 1=En ruta
            try {
                List<Ruta> rutasActivas = rutaRepository.findByCamionIdAndEstado(camion.getId(), 1);
                if (!rutasActivas.isEmpty()) {
                    Ruta rutaActiva = rutasActivas.get(0);
                    info.put("rutaId", rutaActiva.getId());
                    info.put("rutaCodigo", rutaActiva.getCodigo());
                    
                    // Agregar información de progreso
                    if (progresoNodoActual.containsKey(rutaActiva.getId())) {
                        double progreso = progresoNodoActual.get(rutaActiva.getId());
                        info.put("progresoNodoActual", progreso * 100); // Convertir a porcentaje
                        
                        try {
                            // Calcular progreso total de la ruta de manera segura
                            List<NodoRuta> nodos = rutaActiva.getNodos();
                            if (nodos != null && !nodos.isEmpty()) {
                                int indiceNodoActual = Math.min(encontrarIndiceNodoActual(rutaActiva, nodos), nodos.size() - 1);
                                double progresoTotal = (indiceNodoActual + progreso) / Math.max(1, (nodos.size() - 1)) * 100;
                                info.put("progresoRuta", progresoTotal);
                            }
                        } catch (Exception e) {
                            // Si hay errores en el cálculo, usar un valor por defecto
                            info.put("progresoRuta", 0.0);
                        }
                    }
                }
            } catch (Exception e) {
                // Ignorar errores al obtener rutas
                System.err.println("Error obteniendo ruta activa para camión: " + e.getMessage());
            }
        }
        
        return info;
    }
    
    // Método para obtener texto de estado de camión
    private String getEstadoCamionTexto(int estado) {
        switch (estado) {
            case 0: return "Disponible";
            case 1: return "En ruta";
            case 2: return "En mantenimiento";
            case 3: return "Averiado";
            default: return "Desconocido";
        }
    }
    
    // Método que solo extrae la información esencial de almacenes
    private Map<String, Object> convertirAlmacenSimplificado(Almacen almacen) {
        Map<String, Object> info = new HashMap<>();
        info.put("id", almacen.getId());
        info.put("nombre", almacen.getNombre());
        info.put("posX", almacen.getPosX());
        info.put("posY", almacen.getPosY());
        try {
            info.put("tipo", almacen.getTipo());
        } catch (Exception e) {
            info.put("tipo", "CENTRAL"); // Valor por defecto si no está disponible
        }
        return info;
    }
    
    // Método que solo extrae la información esencial de pedidos
    private Map<String, Object> convertirPedidoSimplificado(Pedido pedido) {
        Map<String, Object> info = new HashMap<>();
        info.put("id", pedido.getId());
        info.put("codigo", pedido.getCodigo());
        info.put("posX", pedido.getPosX());
        info.put("posY", pedido.getPosY());
        info.put("m3", pedido.getM3());
        info.put("estado", pedido.getEstado());
        info.put("horasLimite", pedido.getHorasLimite());
        
        if (pedido.getCliente() != null) {
            info.put("clienteId", pedido.getCliente().getId());
            info.put("clienteNombre", pedido.getCliente().getNombre());
        }
        
        return info;
    }
    
    // Método para obtener rutas simplificadas sin objetos anidados complejos
    private List<Map<String, Object>> obtenerRutasSimplificadas() {
        List<Map<String, Object>> rutasSimplificadas = new ArrayList<>();
        
        try {
            // Obtener solo rutas activas para reducir la cantidad de datos
            List<Ruta> rutas = rutaRepository.findByEstado(1); // 1=En curso
            
            for (Ruta ruta : rutas) {
                Map<String, Object> infoRuta = new HashMap<>();
                infoRuta.put("id", ruta.getId());
                infoRuta.put("codigo", ruta.getCodigo());
                infoRuta.put("estado", ruta.getEstado());
                
                // Información simplificada del camión
                if (ruta.getCamion() != null) {
                    infoRuta.put("camionId", ruta.getCamion().getId());
                    infoRuta.put("camionCodigo", ruta.getCamion().getCodigo());
                }
                
                // Extraer solo información esencial de los nodos
                List<Map<String, Object>> nodosSimplificados = new ArrayList<>();
                if (ruta.getNodos() != null) {
                    for (NodoRuta nodo : ruta.getNodos()) {
                        Map<String, Object> infoNodo = new HashMap<>();
                        infoNodo.put("id", nodo.getId());
                        infoNodo.put("orden", nodo.getOrden());
                        infoNodo.put("posX", nodo.getPosX());
                        infoNodo.put("posY", nodo.getPosY());
                        infoNodo.put("tipo", nodo.getTipo());
                        infoNodo.put("entregado", nodo.isEntregado());
                        
                        // Si hay pedido asociado, solo incluir su ID y código
                        if (nodo.getPedido() != null) {
                            infoNodo.put("pedidoId", nodo.getPedido().getId());
                            infoNodo.put("pedidoCodigo", nodo.getPedido().getCodigo());
                            infoNodo.put("volumenGLP", nodo.getVolumenGLP());
                            infoNodo.put("porcentajePedido", nodo.getPorcentajePedido());
                        }
                        
                        nodosSimplificados.add(infoNodo);
                    }
                }
                infoRuta.put("nodos", nodosSimplificados);
                
                rutasSimplificadas.add(infoRuta);
            }
        } catch (Exception e) {
            System.err.println("Error obteniendo rutas simplificadas: " + e.getMessage());
            e.printStackTrace();
        }
        
        return rutasSimplificadas;
    }
    
    // Envía actualizaciones de posición de todos los elementos en el mapa
    private void enviarActualizacionPosicionesOriginal() {
        Map<String, Object> posiciones = obtenerPosiciones();
        messagingTemplate.convertAndSend("/topic/posiciones", posiciones);
    }
    
    // Obtiene las posiciones actuales de todos los elementos para enviar al frontend
    private Map<String, Object> obtenerPosiciones() {
        Map<String, Object> posiciones = new HashMap<>();
        
        // Obtener camiones con sus posiciones actuales
        List<Camion> camiones = camionRepository.findAll();
        List<Map<String, Object>> camionesInfo = camiones.stream()
            .map(this::convertirCamionAMapa)
            .collect(Collectors.toList());
        posiciones.put("camiones", camionesInfo);
        
        // Obtener almacenes
        List<Almacen> almacenes = almacenRepository.findAll();
        List<Map<String, Object>> almacenesInfo = almacenes.stream()
            .map(this::convertirAlmacenAMapa)
            .collect(Collectors.toList());
        posiciones.put("almacenes", almacenesInfo);
        
        // Obtener pedidos pendientes o en ruta
        List<Pedido> pedidos = pedidoRepository.findByEstadoIn(Arrays.asList(0, 1)); // 0=Pendiente, 1=En ruta
        List<Map<String, Object>> pedidosInfo = pedidos.stream()
            .map(this::convertirPedidoAMapa)
            .collect(Collectors.toList());
        posiciones.put("pedidos", pedidosInfo);
        
        // Obtener rutas activas con sus nodos
        List<Ruta> rutas = rutaRepository.findByEstado(1); // 1=En curso
        List<Map<String, Object>> rutasInfo = rutas.stream()
            .map(this::convertirRutaAMapa)
            .collect(Collectors.toList());
        posiciones.put("rutas", rutasInfo);
        
        return posiciones;
    }
    
    // Convierte un camión a un mapa para el API
    private Map<String, Object> convertirCamionAMapa(Camion camion) {
        Map<String, Object> info = new HashMap<>();
        info.put("id", camion.getId());
        info.put("codigo", camion.getCodigo());
        info.put("tipo", camion.getTipo());
        info.put("posX", camion.getPosX());
        info.put("posY", camion.getPosY());
        info.put("estado", camion.getEstado());
        info.put("estadoTexto", camion.getEstadoTexto());
        info.put("combustibleActual", camion.getCombustibleActual());
        info.put("combustiblePorcentaje", (camion.getCombustibleActual() / camion.getCapacidadTanque()) * 100);
        info.put("capacidadDisponible", camion.getCapacidadDisponible());
        info.put("capacidadTotal", camion.getCapacidad());
        info.put("porcentajeUso", camion.getPorcentajeUso());
        
        // Si está en ruta, incluir información de la ruta activa
        if (camion.getEstado() == 1) { // 1=En ruta
            List<Ruta> rutasActivas = rutaRepository.findByCamionIdAndEstado(camion.getId(), 1);

            if (!rutasActivas.isEmpty()) {
                Ruta rutaActiva = rutasActivas.get(0);
                info.put("rutaId", rutaActiva.getId());
                info.put("rutaCodigo", rutaActiva.getCodigo());
                
                // Agregar información de progreso
                if (progresoNodoActual.containsKey(rutaActiva.getId())) {
                    double progreso = progresoNodoActual.get(rutaActiva.getId());
                    info.put("progresoNodoActual", progreso * 100); // Convertir a porcentaje
                    
                    // Calcular progreso total de la ruta
                    int indiceNodoActual = encontrarIndiceNodoActual(rutaActiva, rutaActiva.getNodos());
                    double progresoTotal = (indiceNodoActual + progreso) / (rutaActiva.getNodos().size() - 1) * 100;
                    info.put("progresoRuta", progresoTotal);
                }
            }
        }
        
        return info;
    }
    
    // Convierte un almacén a un mapa para el API
    private Map<String, Object> convertirAlmacenAMapa(Almacen almacen) {
        Map<String, Object> info = new HashMap<>();
        info.put("id", almacen.getId());
        //info.put("codigo", almacen.getCodigo());
        info.put("nombre", almacen.getNombre());
        //info.put("tipo", almacen.getTipo());
        info.put("posX", almacen.getPosX());
        info.put("posY", almacen.getPosY());
        return info;
    }
    
    // Convierte un pedido a un mapa para el API
    private Map<String, Object> convertirPedidoAMapa(Pedido pedido) {
        Map<String, Object> info = new HashMap<>();
        info.put("id", pedido.getId());
        info.put("codigo", pedido.getCodigo());
        info.put("posX", pedido.getPosX());
        info.put("posY", pedido.getPosY());
        info.put("m3", pedido.getM3());
        info.put("estado", pedido.getEstado());
        info.put("horasLimite", pedido.getHorasLimite());
        
        if (pedido.getCliente() != null) {
            info.put("clienteId", pedido.getCliente().getId());
            info.put("clienteNombre", pedido.getCliente().getNombre());
        }
        
        return info;
    }
    
    // Convierte una ruta a un mapa para el API
    private Map<String, Object> convertirRutaAMapa(Ruta ruta) {
        Map<String, Object> info = new HashMap<>();
        info.put("id", ruta.getId());
        info.put("codigo", ruta.getCodigo());
        info.put("estado", ruta.getEstado());
        info.put("estadoTexto", ruta.getEstadoTexto());
        
        if (ruta.getCamion() != null) {
            info.put("camionId", ruta.getCamion().getId());
            info.put("camionCodigo", ruta.getCamion().getCodigo());
        }
        
        // Convertir nodos de la ruta
        List<Map<String, Object>> nodosInfo = ruta.getNodos().stream()
            .map(this::convertirNodoAMapa)
            .collect(Collectors.toList());
        info.put("nodos", nodosInfo);
        
        return info;
    }
    
    // Convierte un nodo de ruta a un mapa para el API
    private Map<String, Object> convertirNodoAMapa(NodoRuta nodo) {
        Map<String, Object> info = new HashMap<>();
        info.put("id", nodo.getId());
        info.put("orden", nodo.getOrden());
        info.put("posX", nodo.getPosX());
        info.put("posY", nodo.getPosY());
        info.put("tipo", nodo.getTipo());
        info.put("entregado", nodo.isEntregado());
        
        if (nodo.getPedido() != null) {
            info.put("pedidoId", nodo.getPedido().getId());
            info.put("pedidoCodigo", nodo.getPedido().getCodigo());
            info.put("volumenGLP", nodo.getVolumenGLP());
            info.put("porcentajePedido", nodo.getPorcentajePedido());
        }
        
        return info;
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
        
        // Contar pedidos pendientes y en ruta
        List<Pedido> pedidosPendientes = pedidoRepository.findByEstado(0);
        List<Pedido> pedidosEnRuta = pedidoRepository.findByEstado(1);
        estado.put("pedidosPendientes", pedidosPendientes.size());
        estado.put("pedidosEnRuta", pedidosEnRuta.size());
        
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