package com.plg.service;

import com.plg.entity.Pedido;
import com.plg.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;//
//INICIA MODIFICACION DE
@Service
public class AgrupamientoAPService {

    @Autowired
    private PedidoRepository pedidoRepository;
    
    // Parámetros para el algoritmo de Affinity Propagation
    private final double DAMPING = 0.9;
    private final int MAX_ITERATIONS = 200;
    private final double CONVERGENCE_THRESHOLD = 0.001;
    
    public Map<String, Object> generarGrupos(Map<String, Object> params) {
        // Obtener pedidos pendientes
        List<Pedido> pedidos = pedidoRepository.findByEstado(0);
        
        // Verificar si hay suficientes pedidos para agrupar
        if (pedidos.isEmpty()) {
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("mensaje", "No hay pedidos pendientes para agrupar");
            return resultado;
        }
        
        // Parámetros opcionales
        int numeroClusterDeseado = params.containsKey("numeroClusters") ? 
                                  (int) params.get("numeroClusters") : 3;
        
        // Implementación simplificada - en un caso real aquí iría el algoritmo AP completo
        // que agruparía los pedidos basado en distancias y otras métricas
        
        // Generamos grupos simulados
        List<Map<String, Object>> grupos = generarGruposSimulados(pedidos, numeroClusterDeseado);
        
        // Preparamos el resultado
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("grupos", grupos);
        resultado.put("metodo", "affinityPropagation");
        resultado.put("totalPedidos", pedidos.size());
        resultado.put("totalGrupos", grupos.size());
        
        return resultado;
    }
    
    // Método que simula la generación de grupos - en un caso real esto implementaría el AP completo
    private List<Map<String, Object>> generarGruposSimulados(List<Pedido> pedidos, int numeroGruposDeseado) {
        List<Map<String, Object>> grupos = new ArrayList<>();
        
        // Dividir los pedidos en grupos (clusters)
        // En un caso real, esto se haría con el algoritmo AP que encuentra automáticamente el número óptimo
        // Aquí simplemente dividimos equitativamente
        List<List<Pedido>> clustersSimulados = dividirEnGrupos(pedidos, numeroGruposDeseado);
        
        // Para cada cluster creamos un grupo
        for (int i = 0; i < clustersSimulados.size(); i++) {
            Map<String, Object> grupo = new HashMap<>();
            grupo.put("idGrupo", "G" + (i + 1));
            
            // Encontrar el pedido más central del grupo (ejemplar) - simulado
            Pedido ejemplar = encontrarEjemplar(clustersSimulados.get(i));
            grupo.put("ejemplar", convertirPedidoAMapa(ejemplar));
            
            // Calcular centroide del grupo
            Map<String, Double> centroide = calcularCentroide(clustersSimulados.get(i));
            grupo.put("centroideX", centroide.get("x"));
            grupo.put("centroideY", centroide.get("y"));
            
            // Convertir pedidos a su representación simple para la API
            List<Map<String, Object>> pedidosGrupo = clustersSimulados.get(i).stream()
                .map(this::convertirPedidoAMapa)
                .collect(Collectors.toList());
            
            grupo.put("pedidos", pedidosGrupo);
            grupo.put("numeroPedidos", clustersSimulados.get(i).size());
            
            // Calcular radio del grupo (distancia máxima desde el centroide)
            double radio = calcularRadioGrupo(clustersSimulados.get(i), centroide.get("x"), centroide.get("y"));
            grupo.put("radio", radio);
            
            // Calcular densidad (pedidos por área)
            double densidad = clustersSimulados.get(i).size() / (Math.PI * Math.pow(radio, 2));
            grupo.put("densidad", densidad);
            
            grupos.add(grupo);
        }
        
        return grupos;
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
        
        // Distribuir pedidos (enfoque simple espacial - en un caso real
        // se usaría AP o K-means para una mejor agrupación por proximidad)
        
        // Simulación de agrupamiento espacial
        // Ordenamos por coordenada X para simular cercanía
        pedidos.sort(Comparator.comparingInt(Pedido::getPosX));
        
        // Distribuir pedidos aproximadamente igual en cada grupo
        int pedidosPorGrupo = pedidos.size() / numeroGrupos;
        int resto = pedidos.size() % numeroGrupos;
        
        int indice = 0;
        for (int i = 0; i < numeroGrupos; i++) {
            int tamañoGrupo = pedidosPorGrupo + (i < resto ? 1 : 0);
            for (int j = 0; j < tamañoGrupo && indice < pedidos.size(); j++) {
                grupos.get(i).add(pedidos.get(indice++));
            }
        }
        
        return grupos;
    }
    
    // Simula encontrar el ejemplar de un grupo (en AP sería el punto más representativo)
    private Pedido encontrarEjemplar(List<Pedido> grupo) {
        // En un caso real, el ejemplar sería el punto determinado por el algoritmo AP
        // que mejor representa al grupo. Aquí simplemente elegimos uno al azar.
        if (grupo.isEmpty()) return null;
        
        // Por simulación, elegimos el pedido más cercano al centroide
        Map<String, Double> centroide = calcularCentroide(grupo);
        Pedido ejemplar = null;
        double minDistancia = Double.MAX_VALUE;
        
        for (Pedido pedido : grupo) {
            double distancia = Math.sqrt(
                Math.pow(pedido.getPosX() - centroide.get("x"), 2) + 
                Math.pow(pedido.getPosY() - centroide.get("y"), 2)
            );
            
            if (distancia < minDistancia) {
                minDistancia = distancia;
                ejemplar = pedido;
            }
        }
        
        return ejemplar;
    }
    
    // Calcula el centroide (punto medio) de un grupo de pedidos
    private Map<String, Double> calcularCentroide(List<Pedido> grupo) {
        Map<String, Double> centroide = new HashMap<>();
        
        if (grupo.isEmpty()) {
            centroide.put("x", 0.0);
            centroide.put("y", 0.0);
            return centroide;
        }
        
        double sumX = 0, sumY = 0;
        for (Pedido pedido : grupo) {
            sumX += pedido.getPosX();
            sumY += pedido.getPosY();
        }
        
        centroide.put("x", sumX / grupo.size());
        centroide.put("y", sumY / grupo.size());
        
        return centroide;
    }
    
    // Calcula el radio del grupo (distancia máxima desde el centroide a cualquier punto)
    private double calcularRadioGrupo(List<Pedido> grupo, double centroideX, double centroideY) {
        if (grupo.isEmpty()) return 0;
        
        double maxDistancia = 0;
        
        for (Pedido pedido : grupo) {
            double distancia = Math.sqrt(
                Math.pow(pedido.getPosX() - centroideX, 2) + 
                Math.pow(pedido.getPosY() - centroideY, 2)
            );
            
            if (distancia > maxDistancia) {
                maxDistancia = distancia;
            }
        }
        
        return maxDistancia;
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
}