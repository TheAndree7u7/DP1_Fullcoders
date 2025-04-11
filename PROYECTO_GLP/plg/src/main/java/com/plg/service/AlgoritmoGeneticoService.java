package com.plg.service;

import com.plg.entity.Pedido;
import com.plg.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AlgoritmoGeneticoService {

    @Autowired
    private PedidoRepository pedidoRepository;
    
    // Parámetros configurables del algoritmo genético
    private final int POBLACION_INICIAL = 50;
    private final int MAX_GENERACIONES = 100;
    private final double TASA_MUTACION = 0.1;
    private final double TASA_CRUCE = 0.8;
    
    public Map<String, Object> generarRutas(Map<String, Object> params) {
        // Obtener pedidos pendientes
        List<Pedido> pedidos = pedidoRepository.findByEstado(0);
        
        // Verificar si hay suficientes pedidos para optimizar
        if (pedidos.isEmpty()) {
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("mensaje", "No hay pedidos pendientes para optimizar");
            return resultado;
        }
        
        // Parámetros opcionales
        int numeroRutas = params.containsKey("numeroRutas") ? 
                         (int) params.get("numeroRutas") : 3;
        
        // Implementación simplificada - en un caso real aquí iría el algoritmo genético completo
        // que optimizaría las rutas considerando capacidades de camiones, restricciones temporales, etc.
        
        // Generamos rutas simuladas
        List<Map<String, Object>> rutas = generarRutasSimuladas(pedidos, numeroRutas);
        
        // Preparamos el resultado
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("rutas", rutas);
        resultado.put("metodo", "algoritmoGenetico");
        resultado.put("totalPedidos", pedidos.size());
        resultado.put("pedidosAsignados", pedidos.size());
        
        return resultado;
    }
    
    // Método que simula la generación de rutas - en un caso real esto implementaría el AG completo
    private List<Map<String, Object>> generarRutasSimuladas(List<Pedido> pedidos, int numeroRutas) {
        List<Map<String, Object>> rutas = new ArrayList<>();
        
        // Dividir los pedidos en grupos (clusters) para simular las rutas
        List<List<Pedido>> grupos = dividirEnGrupos(pedidos, numeroRutas);
        
        // Para cada grupo creamos una ruta
        for (int i = 0; i < grupos.size(); i++) {
            Map<String, Object> ruta = new HashMap<>();
            ruta.put("idRuta", "R" + (i + 1));
            ruta.put("distanciaTotal", 120.0 + (20 * Math.random())); // Valor simulado
            ruta.put("tiempoEstimado", 180 + (i * 30)); // Minutos (simulado)
            
            // Convertir pedidos a su representación simple para la API
            List<Map<String, Object>> pedidosRuta = grupos.get(i).stream()
                .map(this::convertirPedidoAMapa)
                .collect(Collectors.toList());
            
            ruta.put("pedidos", pedidosRuta);
            ruta.put("numeroPedidos", grupos.get(i).size());
            
            // Generar puntos de la ruta
            List<Map<String, Object>> puntosRuta = generarPuntosRuta(grupos.get(i));
            ruta.put("puntos", puntosRuta);
            
            rutas.add(ruta);
        }
        
        return rutas;
    }
    
    // Método auxiliar para dividir pedidos en grupos (simulado)
    private List<List<Pedido>> dividirEnGrupos(List<Pedido> pedidos, int numeroGrupos) {
        List<List<Pedido>> grupos = new ArrayList<>();
        
        // Asegurar que no intentamos crear más grupos que pedidos disponibles
        numeroGrupos = Math.min(numeroGrupos, pedidos.size());
        
        // Crear grupos vacíos
        for (int i = 0; i < numeroGrupos; i++) {
            grupos.add(new ArrayList<>());
        }
        
        // Distribuir pedidos (enfoque simple por turnos)
        for (int i = 0; i < pedidos.size(); i++) {
            grupos.get(i % numeroGrupos).add(pedidos.get(i));
        }
        
        return grupos;
    }
    
    // Convierte un pedido a un mapa para la API REST
    private Map<String, Object> convertirPedidoAMapa(Pedido pedido) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", pedido.getId());
        map.put("posX", pedido.getPosX());
        map.put("posY", pedido.getPosY());
        map.put("m3", pedido.getM3());
        map.put("horasLimite", pedido.getHorasLimite());
        map.put("cliente", pedido.getCliente() != null ? pedido.getCliente().getId() : null);
        return map;
    }
    
    // Genera puntos de ruta (simulado - en un escenario real se utilizaría el AG)
    private List<Map<String, Object>> generarPuntosRuta(List<Pedido> pedidos) {
        List<Map<String, Object>> puntos = new ArrayList<>();
        
        // El primer punto es el almacén (origen)
        Map<String, Object> origen = new HashMap<>();
        origen.put("tipo", "ALMACEN");
        origen.put("posX", 0);
        origen.put("posY", 0);
        puntos.add(origen);
        
        // Agregamos cada pedido como un punto de la ruta
        for (Pedido pedido : pedidos) {
            Map<String, Object> punto = new HashMap<>();
            punto.put("tipo", "CLIENTE");
            punto.put("posX", pedido.getPosX());
            punto.put("posY", pedido.getPosY());
            punto.put("idPedido", pedido.getId());
            puntos.add(punto);
        }
        
        // El último punto es el retorno al almacén
        Map<String, Object> retorno = new HashMap<>();
        retorno.put("tipo", "ALMACEN");
        retorno.put("posX", 0);
        retorno.put("posY", 0);
        puntos.add(retorno);
        
        return puntos;
    }
}