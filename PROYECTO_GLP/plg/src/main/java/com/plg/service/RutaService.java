package com.plg.service;

import com.plg.config.MapaConfig;
import com.plg.dto.PedidoDTO;
import com.plg.dto.PuntoRutaDTO;
import com.plg.dto.RutaDTO;
import com.plg.entity.*;
import com.plg.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
//importar candidato

@Service
public class RutaService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private BloqueoRepository bloqueoRepository;
    
    @Autowired
    private RutaRepository rutaRepository;
    
    @Autowired
    private CamionRepository camionRepository;
    
    @Autowired
    private EntregaParcialRepository entregaParcialRepository;

    @Autowired
    private MapaConfig mapaConfig;
    
    @Autowired
    private MapaReticularService mapaReticularService;
    
    @Autowired
    private BloqueoService bloqueoService;
    //para obtener almacen principal
    @Autowired
    private AlmacenRepository almacenRepository;
    /**
     * Obtiene una ruta por su código
     */
    public Ruta findByCodigoRuta(String codigo) {
        return rutaRepository.findByCodigo(codigo)
            .orElseThrow(() -> new RuntimeException("Ruta no encontrada con código: " + codigo));
    }
    
    /**
     * Obtiene todas las rutas
     */
    public List<Ruta> getAllRutas() {
        return rutaRepository.findAll();
    }
    
    /**
     * Obtiene las rutas por estado
     */
    public List<Ruta> getRutasByEstado(int estado) {
        return rutaRepository.findByEstado(estado);
    }
    
    /**
     * Obtiene las rutas por camión
     */
    public List<Ruta> getRutasByCamion(String codigoCamion) {
        Camion camion = camionRepository.findByCodigo(codigoCamion)
            .orElseThrow(() -> new RuntimeException("Camión no encontrado con código: " + codigoCamion));
        return rutaRepository.findByCamion(camion);
    }
    
    /**
     * Crea una nueva ruta con información básica
     */
    @Transactional
    public Ruta crearRuta(String codigo, String codigoCamion, boolean consideraBloqueos) {
        // Verificar si ya existe una ruta con ese código
        if (rutaRepository.findByCodigo(codigo).isPresent()) {
            throw new RuntimeException("Ya existe una ruta con el código: " + codigo);
        }
        
        Ruta ruta = new Ruta(codigo);
        ruta.setConsideraBloqueos(consideraBloqueos);
        
        if (codigoCamion != null && !codigoCamion.isEmpty()) {
            Camion camion = camionRepository.findByCodigo(codigoCamion)
                .orElseThrow(() -> new RuntimeException("Camión no encontrado con código: " + codigoCamion));
            
            if (camion.getEstado() != EstadoCamion.DISPONIBLE) {
                throw new RuntimeException("El camión no está disponible. Estado actual: " + camion.getEstadoTexto());
            }
            
            ruta.setCamion(camion);
        }
        
        // Agregar nodo inicial (almacén)
        // Obtener coordenadas del almacén central
        Almacen almacenCentral = almacenRepository.findByEsCentralAndActivoTrue(true);
        if (almacenCentral == null) {
            throw new RuntimeException("No se encontró el almacén central");
        }
        // Obtener coordenadas del almacén central
        double x_almacenCentral = almacenCentral.getPosX();
        double y_almacenCentral = almacenCentral.getPosY(); 
        ruta.agregarNodo(x_almacenCentral, y_almacenCentral, "ALMACEN");
   
        return rutaRepository.save(ruta);
    }
    
    /**
     * Agrega un pedido a una ruta existente
     */
    @Transactional
    public Ruta agregarPedidoARuta(String codigoRuta, Long pedidoId, double volumenGLP, double porcentajePedido) {
        Ruta ruta = findByCodigoRuta(codigoRuta);
        Pedido pedido = pedidoRepository.findById(pedidoId)
            .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + pedidoId));
        
        // Verificar si el camión tiene capacidad
        if (ruta.getCamion() != null && !ruta.getCamion().tieneCapacidadPara(volumenGLP)) {
            throw new RuntimeException("El camión no tiene capacidad suficiente para este pedido");
        }
        
        // Verificar si la ruta está en un estado que permite modificación
        if (ruta.getEstado() != 0) { // Si no está "Planificada"
            throw new RuntimeException("No se puede modificar una ruta que ya está en curso o finalizada");
        }
        
        // Agregar nodo de cliente a la ruta
        ruta.agregarNodoCliente(pedido.getPosX(), pedido.getPosY(), pedido, volumenGLP, porcentajePedido);
        
        // Si hay camión asignado, asignar también el pedido al camión como entrega parcial
        if (ruta.getCamion() != null) {
            ruta.getCamion().asignarPedidoParcial(pedido, volumenGLP, porcentajePedido);
        }
        
        // Recalcular la distancia total
        ruta.calcularDistanciaTotal();
        
        // Si se considera bloqueos, verificar posibles bloqueos
        if (ruta.isConsideraBloqueos()) {
            List<Bloqueo> bloqueosActivos = bloqueoRepository.findByActivoTrue();
            ruta.verificarInterseccionConBloqueos(bloqueosActivos);
        }
        
        return rutaRepository.save(ruta);
    }
    
    /**
     * Asigna un camión a una ruta
     */
    @Transactional
    public Ruta asignarCamionARuta(String codigoRuta, String codigoCamion) {
        Ruta ruta = findByCodigoRuta(codigoRuta);
        Camion camion = camionRepository.findByCodigo(codigoCamion)
            .orElseThrow(() -> new RuntimeException("Camión no encontrado con código: " + codigoCamion));
        
        // Verificar si el camión está disponible
        if (camion.getEstado() != EstadoCamion.DISPONIBLE) {
            throw new RuntimeException("El camión no está disponible. Estado actual: " + camion.getEstadoTexto());
        }
        
        // Verificar si la ruta está en un estado que permite asignar camión
        if (ruta.getEstado() != 0) {
            throw new RuntimeException("No se puede asignar un camión a una ruta que ya está en curso o finalizada");
        }
        
        // Verificar si la capacidad del camión es suficiente
        if (!camion.tieneCapacidadPara(ruta.getVolumenTotalGLP())) {
            throw new RuntimeException("El camión no tiene capacidad suficiente para el volumen total de GLP de la ruta");
        }
        
        // Asignar el camión a la ruta
        ruta.setCamion(camion);
        
        // Asignar todos los pedidos de la ruta al camión como entregas parciales
        for (NodoRuta nodo : ruta.getNodos()) {
            if (nodo.getPedido() != null) {
                camion.asignarPedidoParcial(nodo.getPedido(), nodo.getVolumenGLP(), nodo.getPorcentajePedido());
            }
        }
        
        return rutaRepository.save(ruta);
    }
    
    /**
     * Iniciar una ruta
     */
    @Transactional
    public Ruta iniciarRuta(String codigoRuta) {
        Ruta ruta = findByCodigoRuta(codigoRuta);
        
        // Verificar que la ruta tenga un camión asignado
        if (ruta.getCamion() == null) {
            throw new RuntimeException("No se puede iniciar una ruta sin un camión asignado");
        }
        
        // Verificar que la ruta esté en estado "Planificada"
        if (ruta.getEstado() != 0) {
            throw new RuntimeException("La ruta no está en estado Planificada. Estado actual: " + ruta.getEstadoTexto());
        }
        
        // Verificar si la ruta tiene pedidos
        if (ruta.getNodos().stream().noneMatch(n -> n.getPedido() != null)) {
            throw new RuntimeException("La ruta no tiene pedidos asignados");
        }
        
        // Verificar si hay bloqueos activos que afecten la ruta
        if (ruta.isConsideraBloqueos()) {
            List<Bloqueo> bloqueosActivos = bloqueoRepository.findByActivoTrue();
            if (ruta.tieneBloqueoActivo(bloqueosActivos)) {
                throw new RuntimeException("La ruta tiene bloqueos activos en este momento. Revise el mapa o reprograme la ruta.");
            }
        }
        
        // Iniciar la ruta
        ruta.iniciarRuta();
        
        // Estimar tiempos de llegada
        ruta.estimarTiemposLlegada(ruta.getCamion().getVelocidadPromedio(), LocalDateTime.now());
        
        return rutaRepository.save(ruta);
    }
    
    /**
     * Marcar entrega de pedido en ruta
     */
    @Transactional
    public Ruta marcarEntregaPedido(String codigoRuta, Long pedidoId, String observaciones) {
        Ruta ruta = findByCodigoRuta(codigoRuta);
        
        // Verificar que la ruta esté en curso
        if (ruta.getEstado() != 1) {
            throw new RuntimeException("La ruta no está en curso. Estado actual: " + ruta.getEstadoTexto());
        }
        
        // Marcar el pedido como entregado
        boolean entregaExitosa = ruta.marcarPedidoComoEntregado(pedidoId, LocalDateTime.now(), observaciones);
        
        if (!entregaExitosa) {
            throw new RuntimeException("El pedido no se encuentra en esta ruta o ya ha sido entregado");
        }
        
        // Verificar si todos los pedidos han sido entregados para completar la ruta
        if (ruta.getEntregasPendientes().isEmpty()) {
            ruta.completarRuta();
        }
        
        return rutaRepository.save(ruta);
    }
    
    /**
     * Completar una ruta
     */
    @Transactional
    public Ruta completarRuta(String codigoRuta) {
        Ruta ruta = findByCodigoRuta(codigoRuta);
        
        // Verificar que la ruta esté en curso
        if (ruta.getEstado() != 1) {
            throw new RuntimeException("La ruta no está en curso. Estado actual: " + ruta.getEstadoTexto());
        }
        
        // Completar la ruta
        ruta.completarRuta();
        
        return rutaRepository.save(ruta);
    }
    
    /**
     * Cancelar una ruta
     */
    @Transactional
    public Ruta cancelarRuta(String codigoRuta, String motivo) {
        Ruta ruta = findByCodigoRuta(codigoRuta);
        
        // No se puede cancelar una ruta que ya está completada
        if (ruta.getEstado() == 2) {
            throw new RuntimeException("No se puede cancelar una ruta que ya está completada");
        }
        
        // Cancelar la ruta
        ruta.cancelarRuta(motivo);
        
        return rutaRepository.save(ruta);
    }
    
    /**
     * Optimiza una ruta considerando bloqueos si se especifica
     */
    public Map<String, Object> optimizarRuta(String idRuta, boolean considerarBloqueos) {
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("idRuta", idRuta);
        resultado.put("optimizada", true);
        resultado.put("consideraBloqueos", considerarBloqueos);
        
        // Obtener los pedidos asociados a la ruta
        List<Pedido> pedidosRuta = pedidoRepository.findByCodigoRuta(idRuta);
        
        // Si no hay pedidos, devolver una ruta vacía
        if (pedidosRuta.isEmpty()) {
            resultado.put("puntos", new ArrayList<>());
            resultado.put("distanciaTotal", 0.0);
            resultado.put("tiempoEstimado", 0);
            return resultado;
        }
        
        // Punto de inicio: almacén central
      
        //Obtener coordenadas de almacen central
        Almacen almacenCentral = almacenRepository.findByEsCentralAndActivoTrue(true);
        if (almacenCentral == null) {
            throw new RuntimeException("No hay almacén central activo configurado");
        }
        double xAlmacenCentral = almacenCentral.getPosX();
        double yAlmacenCentral = almacenCentral.getPosY();

        double xInicio = xAlmacenCentral;
        double yInicio = yAlmacenCentral;
 
        // Inicializamos la lista de puntos de la ruta con el almacén
        List<Map<String, Object>> puntosRuta = new ArrayList<>();
        puntosRuta.add(createPunto(xInicio, yInicio, "ALMACEN"));
        
        // Si hay que considerar bloqueos, usamos el servicio de mapa reticular
        List<Bloqueo> bloqueosActivos = new ArrayList<>();
        if (considerarBloqueos) {
            bloqueosActivos = bloqueoRepository.findByActivoTrue();
        }
        
        double xActual = xInicio;
        double yActual = yInicio;
        double distanciaTotal = 0;
        
        // Recorremos todos los pedidos añadiendo rutas óptimas entre ellos
        for (Pedido pedido : pedidosRuta) {
            List<double[]> rutaSegmento;
            
            if (considerarBloqueos && !bloqueosActivos.isEmpty()) {
                // Usar el servicio de mapa reticular para encontrar ruta óptima evitando bloqueos
                rutaSegmento = mapaReticularService.calcularRutaOptima(
                    (int)xActual, (int)yActual,
                    (int)pedido.getPosX(), (int)pedido.getPosY(),
                    bloqueosActivos);
            } else {
                // Si no hay bloqueos, usar ruta reticular directa (no diagonal)
                rutaSegmento = calcularRutaDirectaReticular(xActual, yActual, pedido.getPosX(), pedido.getPosY());
            }
            
            // Si no se encontró una ruta válida, seguir con el siguiente pedido
            if (rutaSegmento.isEmpty()) {
                continue;
            }
            
            // Añadir todos los puntos de la ruta excepto el primero (ya está incluido)
            for (int i = 1; i < rutaSegmento.size(); i++) {
                double[] punto = rutaSegmento.get(i);
                
                // Calcular distancia desde el punto anterior
                double[] puntoAnterior = rutaSegmento.get(i-1);
                // En un mapa reticular, la distancia es Manhattan (suma de diferencias absolutas)
                double distanciaSegmento = Math.abs(punto[0] - puntoAnterior[0]) + Math.abs(punto[1] - puntoAnterior[1]);
                distanciaTotal += distanciaSegmento;
                
                // Si este es el punto final, marcarlo como cliente
                String tipo = i == rutaSegmento.size() - 1 ? "CLIENTE" : "NODO";
                puntosRuta.add(createPunto(punto[0], punto[1], tipo));
            }
            
            // Actualizar posición actual
            double[] ultimoPunto = rutaSegmento.get(rutaSegmento.size() - 1);
            xActual = ultimoPunto[0];
            yActual = ultimoPunto[1];
        }
        
        // Añadir ruta de regreso al almacén
        List<double[]> rutaRegreso;
        if (considerarBloqueos && !bloqueosActivos.isEmpty()) {
            rutaRegreso = mapaReticularService.calcularRutaOptima(
                (int)xActual, (int)yActual, 
                (int)xInicio, (int)yInicio,
                bloqueosActivos);
        } else {
            rutaRegreso = calcularRutaDirectaReticular(xActual, yActual, xInicio, yInicio);
        }
        
        // Si se encontró una ruta de regreso, añadirla
        if (!rutaRegreso.isEmpty()) {
            // Añadir todos los puntos de la ruta excepto el primero (ya está incluido)
            for (int i = 1; i < rutaRegreso.size(); i++) {
                double[] punto = rutaRegreso.get(i);
                
                // Calcular distancia desde el punto anterior
                double[] puntoAnterior = rutaRegreso.get(i-1);
                // En un mapa reticular, la distancia es Manhattan (suma de diferencias absolutas)
                double distanciaSegmento = Math.abs(punto[0] - puntoAnterior[0]) + Math.abs(punto[1] - puntoAnterior[1]);
                distanciaTotal += distanciaSegmento;
                
                // Si este es el último punto, marcarlo como almacén de regreso
                String tipo = i == rutaRegreso.size() - 1 ? "ALMACEN" : "NODO";
                puntosRuta.add(createPunto(punto[0], punto[1], tipo));
            }
        }
        
        // Estimar tiempo de viaje (asumiendo velocidad promedio de 50 km/h)
        double tiempoHoras = distanciaTotal / 50.0;
        int tiempoMinutos = (int) Math.round(tiempoHoras * 60);
        
        resultado.put("puntos", puntosRuta);
        resultado.put("distanciaTotal", Math.round(distanciaTotal * 100) / 100.0);
        resultado.put("tiempoEstimado", tiempoMinutos);
        
        return resultado;
    }
    
    /**
     * Actualiza la ruta existente con una ruta optimizada
     */
    @Transactional
    public Ruta actualizarRutaConOptimizacion(String codigoRuta, List<Map<String, Object>> puntosOptimizados) {
        Ruta ruta = findByCodigoRuta(codigoRuta);
        
        // Verificar que la ruta esté en estado Planificada
        if (ruta.getEstado() != 0) {
            throw new RuntimeException("No se puede modificar una ruta que ya está en curso o finalizada");
        }
        
        // Preservar información de pedidos
        Map<Long, NodoRuta> nodosCliente = ruta.getNodos().stream()
            .filter(n -> n.getPedido() != null)
            .collect(Collectors.toMap(n -> n.getPedido().getId(), n -> n));
        
        // Limpiar nodos existentes
        ruta.getNodos().clear();
        
        // Agregar nuevos nodos basados en la ruta optimizada
        for (Map<String, Object> punto : puntosOptimizados) {
            int x = (int) punto.get("x");
            int y = (int) punto.get("y");
            String tipo = (String) punto.get("tipo");
            
            if (tipo.startsWith("CLIENTE_")) {
                // Es un nodo de cliente
                String pedidoIdStr = tipo.substring("CLIENTE_".length());
                Long pedidoId = Long.parseLong(pedidoIdStr);
                
                if (nodosCliente.containsKey(pedidoId)) {
                    // Recuperar la información original del pedido
                    NodoRuta nodoOriginal = nodosCliente.get(pedidoId);
                    ruta.agregarNodoCliente(x, y, nodoOriginal.getPedido(), 
                        nodoOriginal.getVolumenGLP(), nodoOriginal.getPorcentajePedido());
                }
            } else {
                // Es un nodo de tipo ALMACEN o RUTA
                ruta.agregarNodo(x, y, tipo);
            }
        }
        
        // Recalcular distancia total
        ruta.calcularDistanciaTotal();
        
        // Verificar bloqueos si es necesario
        if (ruta.isConsideraBloqueos()) {
            List<Bloqueo> bloqueosActivos = bloqueoRepository.findByActivoTrue();
            ruta.verificarInterseccionConBloqueos(bloqueosActivos);
        }
        
        return rutaRepository.save(ruta);
    }
    
    /**
     * Calcula una ruta directa en el mapa reticular, moviéndose primero horizontal
     * y luego verticalmente entre dos puntos
     */
    private List<double[]> calcularRutaDirectaReticular(double x1, double y1, double x2, double y2) {
        List<double[]> ruta = new ArrayList<>();
        
        // Añadir punto de inicio
        ruta.add(new double[]{x1, y1});
        
        // Moverse horizontalmente primero
        if (x1 != x2) {
            for (double x = x1 + (x2 > x1 ? 1 : -1); x2 > x1 ? x <= x2 : x >= x2; x += (x2 > x1 ? 1 : -1)) {
                ruta.add(new double[]{x, y1});
            }
        }
        
        // Luego moverse verticalmente
        double xFinal = ruta.get(ruta.size() - 1)[0];
        if (y1 != y2) {
            for (double y = y1 + (y2 > y1 ? 1 : -1); y2 > y1 ? y <= y2 : y >= y2; y += (y2 > y1 ? 1 : -1)) {
                if (y != y1) { // Evitar duplicar el punto inicial
                    ruta.add(new double[]{xFinal, y});
                }
            }
        }
        
        return ruta;
    }
    
    /**
     * Calcula la distancia entre dos puntos (física, no reticular)
     */
    public double calcularDistancia(int x1, int y1, int x2, int y2) {
        // Utilizamos la distancia euclidiana para distancias físicas
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }
    
    /**
     * Crea un objeto punto para la respuesta JSON
     */
    private Map<String, Object> createPunto(double x, double y, String tipo) {
        Map<String, Object> punto = new HashMap<>();
        punto.put("x", x);
        punto.put("y", y);
        punto.put("tipo", tipo);
        return punto;
    }
    
    /**
     * Verifica si una ruta entre dos puntos está bloqueada
     */
    public boolean estaRutaBloqueada(int x1, int y1, int x2, int y2, List<Bloqueo> bloqueos) {
        // En un mapa reticular, debemos verificar cada segmento del recorrido
        
        // Si los puntos no están alineados horizontal o verticalmente,
        // calculamos una ruta reticular entre ellos
        if (x1 != x2 && y1 != y2) {
            List<double[]> ruta = calcularRutaDirectaReticular(x1, y1, x2, y2);
            
            // Verificamos cada segmento de la ruta
            for (int i = 0; i < ruta.size() - 1; i++) {
                double[] p1 = ruta.get(i);
                double[] p2 = ruta.get(i + 1);
                
                if (estaSegmentoBloqueado(p1[0], p1[1], p2[0], p2[1], bloqueos)) {
                    return true;
                }
            }
            
            return false;
        } else {
            // Si los puntos están alineados, verificamos directamente
            return estaSegmentoBloqueado(x1, y1, x2, y2, bloqueos);
        }
    }
    
    /**
     * Verifica si un segmento específico está bloqueado
     * Este método asume que el segmento es horizontal o vertical
     */
    public boolean estaSegmentoBloqueado(double x1, double y1, double x2, double y2, List<Bloqueo> bloqueos) {
        // Validar que el segmento es horizontal o vertical
        if (x1 != x2 && y1 != y2) {
            throw new IllegalArgumentException("El segmento debe ser horizontal o vertical en un mapa reticular");
        }
        
        for (Bloqueo bloqueo : bloqueos) {
            if (bloqueo.isActivo()) {
                try {
                    if (bloqueo.intersectaConSegmento(x1, y1, x2, y2)) {
                        return true;
                    }
                } catch (Exception e) {
                    // Usar método alternativo si hay error
                    // Verificar intersección con cada tramo del bloqueo
                    List<Bloqueo.Coordenada> coordenadas = bloqueo.getCoordenadas();
                    
                    if (coordenadas.size() < 2) continue;
                    
                    for (int i = 0; i < coordenadas.size() - 1; i++) {
                        Bloqueo.Coordenada c1 = coordenadas.get(i);
                        Bloqueo.Coordenada c2 = coordenadas.get(i + 1);
                        
                        // En mapa reticular, verificamos la superposición de segmentos
                        if (intersectaSegmentosReticulares(x1, y1, x2, y2, c1.getX(), c1.getY(), c2.getX(), c2.getY())) {
                            return true;
                        }
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Verifica si dos segmentos rectilíneos (horizontales o verticales) se intersectan
     */
    private boolean intersectaSegmentosReticulares(double x1, double y1, double x2, double y2, 
    double x3, double y3, double x4, double y4) {
        // En un mapa reticular, los segmentos son horizontales o verticales
        
        // Segmento horizontal intersecta con segmento vertical
        if (x1 == x2 && y3 == y4) { // Seg1 vertical, Seg2 horizontal
            return estaPuntoEnSegmento(x1, y3, x1, y1, x1, y2) && 
                   estaPuntoEnSegmento(x1, y3, x3, y3, x4, y3);
        } 
        else if (y1 == y2 && x3 == x4) { // Seg1 horizontal, Seg2 vertical
            return estaPuntoEnSegmento(x3, y1, x1, y1, x2, y1) && 
                   estaPuntoEnSegmento(x3, y1, x3, y3, x3, y4);
        }
        // Segmentos paralelos (ambos horizontales o ambos verticales)
        else if (x1 == x2 && x3 == x4) { // Ambos verticales
            return x1 == x3 && hayOverlapEnRango(y1, y2, y3, y4);
        }
        else if (y1 == y2 && y3 == y4) { // Ambos horizontales
            return y1 == y3 && hayOverlapEnRango(x1, x2, x3, x4);
        }
        
        return false;
    }
    
    /**
     * Verifica si un punto está dentro de un segmento
     */
    private boolean estaPuntoEnSegmento(double x, double y, double x1, double y1, double x2, double y2) {
        return x >= Math.min(x1, x2) && x <= Math.max(x1, x2) && 
               y >= Math.min(y1, y2) && y <= Math.max(y1, y2);
    }
    
    /**
     * Verifica si hay solapamiento entre dos rangos
     */
    private boolean hayOverlapEnRango(double a1, double a2, double b1, double b2) {
        return Math.max(a1, a2) >= Math.min(b1, b2) && 
               Math.min(a1, a2) <= Math.max(b1, b2);
    }
    
    /**
     * Verifica si hay rutas alternativas disponibles entre dos puntos
     * cuando la ruta directa está bloqueada
     */
    public boolean hayRutaAlternativa(int x1, int y1, int x2, int y2) {
        List<Bloqueo> bloqueosActivos = bloqueoRepository.findByActivoTrue();
        
        // Si la ruta directa no está bloqueada, no necesitamos alternativa
        if (!estaRutaBloqueada(x1, y1, x2, y2, bloqueosActivos)) {
            return true;
        }
        
        // Usar el servicio de mapa reticular para buscar ruta alternativa
        List<double[]> rutaAlternativa = mapaReticularService.calcularRutaOptima(
            x1, y1, x2, y2, bloqueosActivos);
        
        // Si encontramos una ruta válida, hay alternativa
        return !rutaAlternativa.isEmpty();
    }
    
    /**
     * Obtiene todas las rutas bloqueadas actualmente en el mapa
     */
    public List<Map<String, Object>> obtenerRutasBloqueadas() {
        List<Map<String, Object>> rutasBloqueadas = new ArrayList<>();
        List<Bloqueo> bloqueosActivos = bloqueoRepository.findByActivoTrue();
        
        for (Bloqueo bloqueo : bloqueosActivos) {
            if (bloqueo.getCoordenadas().size() < 2) continue;
            
            for (int i = 0; i < bloqueo.getCoordenadas().size() - 1; i++) {
                Map<String, Object> segmento = new HashMap<>();
                Bloqueo.Coordenada c1 = bloqueo.getCoordenadas().get(i);
                Bloqueo.Coordenada c2 = bloqueo.getCoordenadas().get(i + 1);
                
                segmento.put("x1", c1.getX());
                segmento.put("y1", c1.getY());
                segmento.put("x2", c2.getX());
                segmento.put("y2", c2.getY());
                segmento.put("descripcion", bloqueo.getDescripcion());
                segmento.put("fechaInicio", bloqueo.getFechaInicio().toString());
                segmento.put("fechaFin", bloqueo.getFechaFin().toString());
                
                rutasBloqueadas.add(segmento);
            }
        }
        
        return rutasBloqueadas;
    }
    
    /**
     * Obtiene un resumen de todas las rutas
     */
    public List<Map<String, Object>> obtenerResumeneRutas() {
        List<Ruta> rutas = rutaRepository.findAll();
        
        return rutas.stream()
            .map(Ruta::getResumenRuta)
            .collect(Collectors.toList());
    }
    
    /**
     * Verifica si alguna ruta activa pasa por ciertos tramos bloqueados
     */
    public List<Map<String, Object>> verificarRutasAfectadasPorBloqueos(List<Bloqueo> nuevosBloqueos) {
        List<Map<String, Object>> rutasAfectadas = new ArrayList<>();
        
        // Obtener solo rutas planificadas o en curso
        List<Ruta> rutasActivas = rutaRepository.findByEstadoIn(List.of(0, 1));
        
        for (Ruta ruta : rutasActivas) {
            if (ruta.isConsideraBloqueos() && ruta.verificarInterseccionConBloqueos(nuevosBloqueos)) {
                Map<String, Object> info = new HashMap<>();
                info.put("rutaId", ruta.getId());
                info.put("codigo", ruta.getCodigo());
                info.put("estado", ruta.getEstado());
                info.put("estadoTexto", ruta.getEstadoTexto());
                
                if (ruta.getCamion() != null) {
                    info.put("camionCodigo", ruta.getCamion().getCodigo());
                }
                
                rutasAfectadas.add(info);
            }
        }
        
        return rutasAfectadas;
    }
        /**
     * Recorre las rutas en curso e intenta insertar cada pedido en
     * la posición de menor costo; si no hay sitio, crea una nueva ruta.
     */
    @Transactional
    public List<RutaDTO> insertarPedidosDinamicos(List<PedidoDTO> nuevosPedidos) {
        List<RutaDTO> rutasActualizadas = new ArrayList<>();

        for (PedidoDTO pedidoDTO : nuevosPedidos) {
            // 1. Buscar camiones en estado DISPONIBLE
            var disponibles = camionRepository.findByEstado(EstadoCamion.DISPONIBLE);
            List<Candidate> candidatos = new ArrayList<>();

            for (Camion cam : disponibles) {
                Ruta ruta = rutaRepository.findByCamionIdAndEstado(cam.getId(), 1)  // rutas en curso
                                           .stream().findFirst().orElse(null);
                if (ruta == null) continue;

                // 2. Generar posiciones de inserción en cada arista de ruta.getNodos()
                for (int i = 0; i < ruta.getNodos().size() - 1; i++) {
                    // copia de ruta, insertar el pedido entre nodo i y i+1
                     
                    Pedido pedidoEnt = pedidoRepository.findById(pedidoDTO.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));
                    Ruta prueba = new Ruta(ruta);  // ya existe ctor copia
                    prueba.insertarNodoEn(
                        i + 1,
                        pedidoEnt.getPosX(),
                        pedidoEnt.getPosY(),
                        pedidoEnt,
                        pedidoEnt.getVolumenGLPAsignado(),
                        100.0     
                    );
                    prueba.calcularDistanciaTotal();
                    double consumo = cam.calcularConsumoCombustible(prueba.getDistanciaTotal());
                    // sólo válidas si cabe en tanque
                    if (prueba.getDistanciaTotal() <= cam.calcularDistanciaMaxima()) {
                        candidatos.add(new Candidate(cam, prueba, consumo, i + 1));
                    }
                }
            }

            if (candidatos.isEmpty()) {
                // 3. No hay inserciones viables: crear una nueva ruta desde central
                var nuevaRuta = crearRutaDesdeCentral(pedidoDTO);
                rutasActualizadas.add(toDto(nuevaRuta));
            } else {
                // 4. Escoge la menor
                Candidate mejor = candidatos.stream()
                                            .min(Comparator.comparingDouble(Candidate::getCosto))
                                            .get();
                // Guarda la ruta modificada
                rutaRepository.save(mejor.getRuta());
                rutasActualizadas.add(toDto(mejor.getRuta()));
            }
        }

        return rutasActualizadas;
    }
        /**
     * Crea una ruta nueva partiendo del almacén central y
     * asigna el pedido completo.
     */
    private Ruta crearRutaDesdeCentral(PedidoDTO pedidoDTO) {
        Almacen central = almacenRepository.findByEsCentralAndActivoTrue(true);
        if (central == null) {
            throw new IllegalStateException("No hay almacén central activo");
        }

        List<Camion> disponibles = camionRepository.findByEstado(EstadoCamion.DISPONIBLE);
        if (disponibles.isEmpty()) {
            throw new IllegalStateException("No hay camiones disponibles");
        }
        Camion camion = disponibles.get(0);

        // Construcción de la ruta
        Ruta ruta = new Ruta("RD-" + System.currentTimeMillis());
        ruta.setCamion(camion);
        ruta.setFechaCreacion(LocalDateTime.now());
        ruta.setEstado(1); // En curso

        // Nodo inicial: almacén central
        ruta.agregarNodo(central.getPosX(), central.getPosY(), "ALMACEN");

        // Nodo cliente
        Pedido pedidoEnt = pedidoRepository.findById(pedidoDTO.getId())
                                           .orElseThrow();
        ruta.agregarNodoCliente(
            pedidoEnt.getPosX(),
            pedidoEnt.getPosY(),
            pedidoEnt,
            pedidoEnt.getVolumenGLPAsignado(),
            100.0
        );

        // Retorno al central
        ruta.agregarNodo(central.getPosX(), central.getPosY(), "ALMACEN");
        ruta.calcularDistanciaTotal();

        return rutaRepository.save(ruta);
    }

    /**
     * Transforma una entidad Ruta en un DTO listo para el controlador.
     */
    private RutaDTO toDto(Ruta ruta) {
        List<PuntoRutaDTO> puntos = ruta.getNodos().stream()
            .map(n -> PuntoRutaDTO.builder()
                .tipo(n.getTipo())
                .posX(n.getPosX())
                .posY(n.getPosY())
                .idPedido(n.getPedido() != null ? n.getPedido().getId() : null)
                .build())
            .collect(Collectors.toList());

        List<PedidoDTO> pedidos = ruta.getNodos().stream()
            .filter(n -> n.getPedido() != null)
            .map(n -> {
                Pedido p = n.getPedido();
                return PedidoDTO.builder()
                    .id(p.getId())
                    .codigo(p.getCodigo())
                    .posX(p.getPosX())
                    .posY(p.getPosY())
                    .volumenGLPAsignado(p.getVolumenGLPAsignado())
                    .horasLimite(p.getHorasLimite())
                    .clienteId(p.getCliente().getId())
                    .clienteNombre(p.getCliente().getNombre())
                    .build();
            })
            .collect(Collectors.toList());

        return RutaDTO.builder()
            .idRuta(ruta.getCodigo())
            .distanciaTotal(ruta.getDistanciaTotal())
            .tiempoEstimado((int) ruta.getTiempoEstimadoMinutos())
            .pedidos(pedidos)
            .numeroPedidos(pedidos.size())
            .puntos(puntos)
            .camionCodigo(ruta.getCamion().getCodigo())
            .build();
    }

    /**
     * Candidate helper para tabular costo de cada inserción.
     */
    private static class Candidate {
        private final Camion camion;
        private final Ruta ruta;
        private final double costo;
        @SuppressWarnings("unused")
        private final int posicion;

        public Candidate(Camion camion, Ruta ruta, double costo, int posicion) {
            this.camion = camion;
            this.ruta = ruta;
            this.costo = costo;
            this.posicion = posicion;
        }

        public double getCosto() {
            return costo;
        }

        public Ruta getRuta() {
            return ruta;
        }
    }
}
 