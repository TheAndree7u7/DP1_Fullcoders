package com.plg.service;

import com.plg.entity.Bloqueo;
import com.plg.entity.Pedido;
import com.plg.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RutaService {

    @Autowired
    private PedidoRepository pedidoRepository;

    public Map<String, Object> optimizarRuta(String idRuta, boolean considerarBloqueos) {
        // Implementación simplificada - en un caso real se utilizaría un algoritmo
        // más complejo para la optimización de rutas considerando bloqueos
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("idRuta", idRuta);
        resultado.put("optimizada", true);
        resultado.put("consideraBloqueos", considerarBloqueos);
        
        // Aquí se incluiría la lógica real de optimización
        List<Map<String, Object>> puntos = new ArrayList<>();
        puntos.add(createPunto(0, 0, "ALMACEN"));
        puntos.add(createPunto(10, 15, "CLIENTE_1"));
        puntos.add(createPunto(25, 30, "CLIENTE_2"));
        puntos.add(createPunto(0, 0, "ALMACEN"));
        
        resultado.put("puntos", puntos);
        resultado.put("distanciaTotal", 100.5);
        resultado.put("tiempoEstimado", 120);
        
        return resultado;
    }
    
    public double calcularDistancia(int x1, int y1, int x2, int y2) {
        // Utilizamos la distancia euclidiana
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }
    
    // Método auxiliar para crear un punto de la ruta
    private Map<String, Object> createPunto(int x, int y, String tipo) {
        Map<String, Object> punto = new HashMap<>();
        punto.put("x", x);
        punto.put("y", y);
        punto.put("tipo", tipo);
        return punto;
    }
    
    // Método para comprobar si un segmento de ruta está bloqueado
    public boolean estaRutaBloqueada(int x1, int y1, int x2, int y2, List<Bloqueo> bloqueos) {
        // Implementación simplificada - en un caso real se utilizaría
        // un algoritmo de detección de intersección de segmentos
        for (Bloqueo bloqueo : bloqueos) {
            if (bloqueo.isActivo()) {
                // Comprobar si la ruta atraviesa el bloqueo
                // Esta es una implementación muy simplificada
                if (intersecta(x1, y1, x2, y2, 
                              bloqueo.getPosXInicio(), bloqueo.getPosYInicio(),
                              bloqueo.getPosXFin(), bloqueo.getPosYFin())) {
                    return true;
                }
            }
        }
        return false;
    }
    
    // Método simplificado para detectar intersección de dos segmentos
    private boolean intersecta(int x1, int y1, int x2, int y2, 
                              int x3, int y3, int x4, int y4) {
        // Esta es una implementación simplificada
        // En un caso real se utilizaría un algoritmo más preciso
        return false;
    }
}