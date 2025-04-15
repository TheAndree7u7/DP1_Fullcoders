package com.plg.service;

import com.plg.config.MapaConfig;
import com.plg.entity.Bloqueo;
import com.plg.entity.Pedido;
import com.plg.repository.BloqueoRepository;
import com.plg.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class RutaService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private BloqueoRepository bloqueoRepository;

    @Autowired
    private MapaConfig mapaConfig;
    
    @Autowired
    private MapaReticularService mapaReticularService;
    
    @Autowired
    private BloqueoService bloqueoService;

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
        int xInicio = mapaConfig.getAlmacenCentralX();
        int yInicio = mapaConfig.getAlmacenCentralY();
        
        // Inicializamos la lista de puntos de la ruta con el almacén
        List<Map<String, Object>> puntosRuta = new ArrayList<>();
        puntosRuta.add(createPunto(xInicio, yInicio, "ALMACEN"));
        
        // Si hay que considerar bloqueos, usamos el servicio de mapa reticular
        List<Bloqueo> bloqueosActivos = new ArrayList<>();
        if (considerarBloqueos) {
            bloqueosActivos = bloqueoRepository.findByActivoTrue();
        }
        
        int xActual = xInicio;
        int yActual = yInicio;
        double distanciaTotal = 0;
        
        // Recorremos todos los pedidos añadiendo rutas óptimas entre ellos
        for (Pedido pedido : pedidosRuta) {
            int xDestino = pedido.getPosX();
            int yDestino = pedido.getPosY();
            
            // Calcular la ruta óptima entre el punto actual y el pedido
            List<int[]> rutaOptima;
            if (considerarBloqueos) {
                rutaOptima = mapaReticularService.calcularRutaOptima(
                    xActual, yActual, xDestino, yDestino, bloqueosActivos);
            } else {
                // Si no consideramos bloqueos, la ruta es directa en el mapa reticular
                // (movimientos horizontales y verticales)
                rutaOptima = calcularRutaDirectaReticular(xActual, yActual, xDestino, yDestino);
            }
            
            // Si no se pudo encontrar una ruta, continuar con el siguiente pedido
            if (rutaOptima.isEmpty()) {
                continue;
            }
            
            // Añadir todos los puntos de la ruta excepto el primero (ya está incluido)
            for (int i = 1; i < rutaOptima.size(); i++) {
                int[] punto = rutaOptima.get(i);
                
                // Si es el último punto (destino), marcarlo como CLIENTE
                String tipo = (i == rutaOptima.size() - 1) ? "CLIENTE_" + pedido.getId() : "RUTA";
                puntosRuta.add(createPunto(punto[0], punto[1], tipo));
                
                // Calcular distancia con el punto anterior
                int[] puntoAnterior = rutaOptima.get(i-1);
                distanciaTotal += mapaConfig.calcularDistanciaRealKm(
                    puntoAnterior[0], puntoAnterior[1], punto[0], punto[1]);
            }
            
            // Actualizar punto actual
            xActual = xDestino;
            yActual = yDestino;
        }
        
        // Añadir ruta de regreso al almacén
        List<int[]> rutaRegreso;
        if (considerarBloqueos) {
            rutaRegreso = mapaReticularService.calcularRutaOptima(
                xActual, yActual, xInicio, yInicio, bloqueosActivos);
        } else {
            rutaRegreso = calcularRutaDirectaReticular(xActual, yActual, xInicio, yInicio);
        }
        
        // Si se encontró una ruta de regreso, añadirla
        if (!rutaRegreso.isEmpty()) {
            for (int i = 1; i < rutaRegreso.size(); i++) {
                int[] punto = rutaRegreso.get(i);
                String tipo = (i == rutaRegreso.size() - 1) ? "ALMACEN" : "RUTA";
                puntosRuta.add(createPunto(punto[0], punto[1], tipo));
                
                int[] puntoAnterior = rutaRegreso.get(i-1);
                distanciaTotal += mapaConfig.calcularDistanciaRealKm(
                    puntoAnterior[0], puntoAnterior[1], punto[0], punto[1]);
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
     * Calcula una ruta directa en el mapa reticular, moviéndose primero horizontal
     * y luego verticalmente entre dos puntos
     */
    private List<int[]> calcularRutaDirectaReticular(int x1, int y1, int x2, int y2) {
        List<int[]> ruta = new ArrayList<>();
        
        // Añadir punto de inicio
        ruta.add(new int[]{x1, y1});
        
        // Moverse horizontalmente primero
        if (x1 != x2) {
            for (int x = x1 + (x2 > x1 ? 1 : -1); x2 > x1 ? x <= x2 : x >= x2; x += (x2 > x1 ? 1 : -1)) {
                ruta.add(new int[]{x, y1});
            }
        }
        
        // Luego moverse verticalmente
        int xFinal = ruta.get(ruta.size() - 1)[0];
        if (y1 != y2) {
            for (int y = y1 + (y2 > y1 ? 1 : -1); y2 > y1 ? y <= y2 : y >= y2; y += (y2 > y1 ? 1 : -1)) {
                if (y != y1) { // Evitar duplicar el punto inicial
                    ruta.add(new int[]{xFinal, y});
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
    private Map<String, Object> createPunto(int x, int y, String tipo) {
        Map<String, Object> punto = new HashMap<>();
        punto.put("x", x);
        punto.put("y", y);
        punto.put("tipo", tipo);
        return punto;
    }
    
    /**
     * Verifica si una ruta entre dos puntos está bloqueada
     * Esta función ahora usa la nueva estructura de Bloqueo
     */
    public boolean estaRutaBloqueada(int x1, int y1, int x2, int y2, List<Bloqueo> bloqueos) {
        // En un mapa reticular, debemos verificar cada segmento del recorrido
        
        // Si los puntos no están alineados horizontal o verticalmente,
        // calculamos una ruta reticular entre ellos
        if (x1 != x2 && y1 != y2) {
            List<int[]> ruta = calcularRutaDirectaReticular(x1, y1, x2, y2);
            
            // Verificamos cada segmento de la ruta
            for (int i = 0; i < ruta.size() - 1; i++) {
                int[] p1 = ruta.get(i);
                int[] p2 = ruta.get(i + 1);
                
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
    public boolean estaSegmentoBloqueado(int x1, int y1, int x2, int y2, List<Bloqueo> bloqueos) {
        // Validar que el segmento es horizontal o vertical
        if (x1 != x2 && y1 != y2) {
            throw new IllegalArgumentException("El segmento debe ser horizontal o vertical en un mapa reticular");
        }
        
        for (Bloqueo bloqueo : bloqueos) {
            if (bloqueo.isActivo()) {
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
        
        return false;
    }
    
    /**
     * Verifica si dos segmentos rectilíneos (horizontales o verticales) se intersectan
     */
    private boolean intersectaSegmentosReticulares(int x1, int y1, int x2, int y2, 
                                                  int x3, int y3, int x4, int y4) {
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
    private boolean estaPuntoEnSegmento(int x, int y, int x1, int y1, int x2, int y2) {
        return x >= Math.min(x1, x2) && x <= Math.max(x1, x2) && 
               y >= Math.min(y1, y2) && y <= Math.max(y1, y2);
    }
    
    /**
     * Verifica si hay solapamiento entre dos rangos
     */
    private boolean hayOverlapEnRango(int a1, int a2, int b1, int b2) {
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
        List<int[]> rutaAlternativa = mapaReticularService.calcularRutaOptima(
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
}