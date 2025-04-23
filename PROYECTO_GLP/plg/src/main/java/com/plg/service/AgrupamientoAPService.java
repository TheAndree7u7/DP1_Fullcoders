package com.plg.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.plg.dto.AgrupamientoAPResultadoDTO;
import com.plg.dto.GrupoDTO;
import com.plg.dto.PedidoDTO;
import com.plg.entity.Pedido;
import com.plg.repository.PedidoRepository;
import com.plg.service.ap.AffinityPropagationService;

@Service
public class AgrupamientoAPService {

    private static final Logger logger = LoggerFactory.getLogger(AgrupamientoAPService.class);

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired 
    private AffinityPropagationService apService;

    // Parámetros para el algoritmo de Affinity Propagation
    private final double DAMPING = 0.9;
    private final int MAX_ITERATIONS = 200;
    private final double CONVERGENCE_THRESHOLD = 0.001;
    
    public AgrupamientoAPResultadoDTO generarGrupos(Map<String, Object> params) {
        logger.info("Iniciando el proceso de generación de grupos con los parámetros: {}", params);
        
        // Proteger contra null params
        if (params == null) {
            params = new HashMap<>();
        }
        
        try {
            double alpha       = params.containsKey("alpha") ? Double.parseDouble(params.get("alpha").toString()) : 1.0;
            double beta        = params.containsKey("beta") ? Double.parseDouble(params.get("beta").toString()) : 1.0;
            double damping     = params.containsKey("damping") ? Double.parseDouble(params.get("damping").toString()) : DAMPING;
            int maxIter        = params.containsKey("maxIter") ? Integer.parseInt(params.get("maxIter").toString()) : MAX_ITERATIONS;

            // Ejecutar el algoritmo de Affinity Propagation
            logger.info("Ejecutando el algoritmo de Affinity Propagation...");
            List<GrupoDTO> grupos = apService.clusterizar(alpha, beta, damping, maxIter);

            logger.info("Generación de grupos completada. Total de pedidos: {}, Total de grupos: {}", 
                        grupos.stream().mapToInt(GrupoDTO::getNumeroPedidos).sum(), grupos.size());

            return AgrupamientoAPResultadoDTO.builder()
                .metodo("affinityPropagation")
                .totalPedidos(grupos.stream().mapToInt(GrupoDTO::getNumeroPedidos).sum())
                .totalGrupos(grupos.size())
                .grupos(grupos)
                .build();
        } catch (Exception e) {
            logger.error("Error en la generación de grupos", e);
            throw new RuntimeException("Error en la generación de grupos: " + e.getMessage(), e);
        }
    }

    // Método que simula la generación de grupos - en un caso real esto implementaría el AP completo
    private List<GrupoDTO> generarGruposSimulados(List<Pedido> pedidos, int numeroGruposDeseado) {
        logger.info("Generando grupos simulados con {} grupos deseados...", numeroGruposDeseado);
        
        List<GrupoDTO> grupos = new ArrayList<>();
        
        // Dividir los pedidos en grupos (clusters)
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
        
        logger.info("Generación de grupos simulados completada. Total de grupos generados: {}", grupos.size());
        return grupos;
    }
    
    // Método auxiliar para dividir pedidos en grupos (simulado)
    private List<List<Pedido>> dividirEnGrupos(List<Pedido> pedidos, int numeroGrupos) {
        logger.info("Dividiendo los pedidos en {} grupos...", numeroGrupos);
        
        List<List<Pedido>> grupos = new ArrayList<>();
        
        // Asegurar que no intentamos crear más grupos que pedidos disponibles
        numeroGrupos = Math.min(numeroGrupos, pedidos.size());
        
        // Crear grupos vacíos
        for (int i = 0; i < numeroGrupos; i++) {
            grupos.add(new ArrayList<>());
        }
        
        // Simulación de agrupamiento espacial
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
