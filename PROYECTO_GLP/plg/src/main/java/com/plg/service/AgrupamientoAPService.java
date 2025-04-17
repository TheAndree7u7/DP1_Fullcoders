package com.plg.service;

import com.plg.dto.*;
import com.plg.entity.Pedido;
import com.plg.enums.EstadoPedido;
import com.plg.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AgrupamientoAPService {

    @Autowired
    private PedidoRepository pedidoRepository;
    
    // Parámetros para el algoritmo de Affinity Propagation
    private final double DAMPING = 0.9;
    private final int MAX_ITERATIONS = 200;
    private final double CONVERGENCE_THRESHOLD = 0.001;
    
    public AgrupamientoAPResultadoDTO generarGrupos(Map<String, Object> params) {
        // Obtener pedidos pendientes
        List<Pedido> pedidos = pedidoRepository.findByEstado(EstadoPedido.PENDIENTE_PLANIFICACION);
         
        // Verificar si hay suficientes pedidos para agrupar
        if (pedidos.isEmpty()) {
            return AgrupamientoAPResultadoDTO.builder()
                .metodo("affinityPropagation")
                .totalPedidos(0)
                .totalGrupos(0)
                .grupos(Collections.emptyList())
                .build();
        }
        
        // Parámetros opcionales
        int numeroClusterDeseado = params.containsKey("numeroClusters") ? 
                                  (int) params.get("numeroClusters") : 3;
        
        // Implementación simplificada - en un caso real aquí iría el algoritmo AP completo
        // que agruparía los pedidos basado en distancias y otras métricas
        
        // Generamos grupos simulados
        List<GrupoDTO> grupos = generarGruposSimulados(pedidos, numeroClusterDeseado);
        
        // Preparamos el resultado usando DTO
        return AgrupamientoAPResultadoDTO.builder()
            .grupos(grupos)
            .metodo("affinityPropagation")
            .totalPedidos(pedidos.size())
            .totalGrupos(grupos.size())
            .build();
    }
    
    // Método que simula la generación de grupos - en un caso real esto implementaría el AP completo
    private List<GrupoDTO> generarGruposSimulados(List<Pedido> pedidos, int numeroGruposDeseado) {
        List<GrupoDTO> grupos = new ArrayList<>();
        
        // Dividir los pedidos en grupos (clusters)
        // En un caso real, esto se haría con el algoritmo AP que encuentra automáticamente el número óptimo
        // Aquí simplemente dividimos equitativamente
        List<List<Pedido>> clustersSimulados = dividirEnGrupos(pedidos, numeroGruposDeseado);
        
        // Para cada cluster creamos un grupo
        for (int i = 0; i < clustersSimulados.size(); i++) {
            // Encontrar el pedido más central del grupo (ejemplar) - simulado
            Pedido ejemplar = encontrarEjemplar(clustersSimulados.get(i));
            
            // Calcular centroide del grupo
            Map<String, Double> centroide = calcularCentroide(clustersSimulados.get(i));
            
            // Convertir pedidos a DTOs
            List<PedidoDTO> pedidosDTO = clustersSimulados.get(i).stream()
                .map(this::convertirAPedidoDTO)
                .collect(Collectors.toList());
            
            // Calcular radio del grupo (distancia máxima desde el centroide)
            double radio = calcularRadioGrupo(clustersSimulados.get(i), centroide.get("x"), centroide.get("y"));
            
            // Calcular densidad (pedidos por área)
            double densidad = clustersSimulados.get(i).size() / (Math.PI * Math.pow(radio, 2));
            
            // Crear grupo como DTO
            GrupoDTO grupo = GrupoDTO.builder()
                .idGrupo("G" + (i + 1))
                .ejemplar(convertirAPedidoDTO(ejemplar))
                .centroideX(centroide.get("x"))
                .centroideY(centroide.get("y"))
                .pedidos(pedidosDTO)
                .numeroPedidos(pedidosDTO.size())
                .radio(radio)
                .densidad(densidad)
                .build();
            
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
        pedidos.sort(Comparator.comparingDouble(Pedido::getPosX));
        
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
    
    // Convierte un pedido a un DTO
    private PedidoDTO convertirAPedidoDTO(Pedido pedido) {
        if (pedido == null) return null;
        
        return PedidoDTO.builder()
            .id(pedido.getId())
            .codigo(pedido.getCodigo())
            .posX(pedido.getPosX())
            .posY(pedido.getPosY())
            .volumenGLPAsignado(pedido.getVolumenGLPAsignado())
            .horasLimite(pedido.getHorasLimite())
            .clienteId(pedido.getCliente() != null ? pedido.getCliente().getId() : null)
            .clienteNombre(pedido.getCliente() != null ? pedido.getCliente().getNombre() : null)
            .build();
    }
}