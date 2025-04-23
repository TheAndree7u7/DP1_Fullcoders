package com.plg.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.plg.dto.AgrupamientoAPResultadoDTO;
import com.plg.dto.ClusterDTO;
import com.plg.dto.GrupoDTO;
import com.plg.dto.PedidoDTO;
import com.plg.dto.PuntoClusterDTO;
import com.plg.entity.EstadoPedido;
import com.plg.entity.Pedido;
import com.plg.repository.PedidoRepository;
import com.plg.service.ap.AffinityPropagationService;

@Service
public class AgrupamientoAPService {

    private static final Logger logger = LoggerFactory.getLogger(AgrupamientoAPService.class);
    
    @Autowired
    private AffinityPropagationService affinityPropagationService;
    
    @Autowired
    private PedidoRepository pedidoRepository;
    
    /**
     * Genera grupos de pedidos utilizando el algoritmo Affinity Propagation
     * @param params Parámetros de configuración para el algoritmo
     * @return DTO con los resultados del agrupamiento
     */
    public AgrupamientoAPResultadoDTO generarGrupos(Map<String, Object> params) {
        logger.info("Iniciando generación de grupos con Affinity Propagation. Parámetros: {}", params);
        Instant inicio = Instant.now();
        
        // Extraer parámetros con valores por defecto
        double alpha = params.containsKey("alpha") ? Double.parseDouble(params.get("alpha").toString()) : 0.8;
        double beta = params.containsKey("beta") ? Double.parseDouble(params.get("beta").toString()) : 0.2;
        double damping = params.containsKey("damping") ? Double.parseDouble(params.get("damping").toString()) : 0.9;
        int maxIter = params.containsKey("maxIter") ? Integer.parseInt(params.get("maxIter").toString()) : 100;
        
        logger.info("Configuración AP: alpha={}, beta={}, damping={}, maxIter={}", alpha, beta, damping, maxIter);
        
        // Obtener pedidos pendientes de planificación
        List<Pedido> pedidos = pedidoRepository.findByEstado(EstadoPedido.PENDIENTE_PLANIFICACION);
        logger.info("Pedidos pendientes encontrados: {}", pedidos.size());
        
        if (pedidos.isEmpty()) {
            logger.warn("No hay pedidos pendientes para agrupar");
            return AgrupamientoAPResultadoDTO.builder()
                    .metodo("affinityPropagation")
                    .totalPedidos(0)
                    .pedidosAgrupados(0)
                    .clusters(new ArrayList<>())
                    .mensaje("No hay pedidos pendientes para agrupar")
                    .build();
        }
        
        // Ejecutar el algoritmo de Affinity Propagation
        logger.info("Ejecutando algoritmo Affinity Propagation...");
        List<GrupoDTO> gruposGenerados = affinityPropagationService.clusterizar(alpha, beta, damping, maxIter);
        
        // Convertir los grupos a ClusterDTO para el resultado
        List<ClusterDTO> clusters = convertirGruposAClusters(gruposGenerados);
        
        // Contabilizar pedidos agrupados
        int totalPedidosAgrupados = clusters.stream()
                .mapToInt(c -> c.getPuntos().size())
                .sum();
        
        // Calcular tiempo de procesamiento
        Instant fin = Instant.now();
        long tiempoMs = Duration.between(inicio, fin).toMillis();
        
        logger.info("Agrupamiento completado. Se generaron {} clusters con {} pedidos en {} ms", 
                    clusters.size(), totalPedidosAgrupados, tiempoMs);
        
        // Construir y retornar el resultado
        return AgrupamientoAPResultadoDTO.builder()
                .metodo("affinityPropagation")
                .totalPedidos(pedidos.size())
                .pedidosAgrupados(totalPedidosAgrupados)
                .clusters(clusters)
                .iteraciones(maxIter) // Podríamos mejorar esto para obtener las iteraciones reales
                .tiempoComputo(tiempoMs)
                .mensaje("Agrupamiento completado exitosamente")
                .build();
    }
    
    /**
     * Convierte los GrupoDTO generados por el algoritmo AP a ClusterDTO para el resultado
     */
    private List<ClusterDTO> convertirGruposAClusters(List<GrupoDTO> grupos) {
        List<ClusterDTO> clusters = new ArrayList<>();
        
        for (GrupoDTO grupo : grupos) {
            // Convertir pedidos a puntos del cluster
            List<PuntoClusterDTO> puntos = grupo.getPedidos().stream()
                    .map(p -> PuntoClusterDTO.builder()
                            .id(p.getId())
                            .pedidoId(p.getId())
                            .pedidoCodigo(p.getCodigo())
                            .x(p.getPosX())
                            .y(p.getPosY())
                            // Calcular distancia Manhattan al centro
                            .distanciaCentro(Math.abs(p.getPosX() - grupo.getCentroideX()) + 
                                            Math.abs(p.getPosY() - grupo.getCentroideY()))
                            .build())
                    .collect(Collectors.toList());
            
            // Calcular distancia total y promedio
            double distanciaTotal = puntos.stream()
                    .mapToDouble(PuntoClusterDTO::getDistanciaCentro)
                    .sum();
            
            double distanciaPromedio = puntos.isEmpty() ? 0 : 
                    distanciaTotal / puntos.size();
            
            // Crear el ClusterDTO
            ClusterDTO cluster = ClusterDTO.builder()
                    .idGrupo(grupo.getIdGrupo())
                    .centroX(grupo.getCentroideX())
                    .centroY(grupo.getCentroideY())
                    .puntos(puntos)
                    .distanciaTotal(distanciaTotal)
                    .distanciaPromedio(distanciaPromedio)
                    .build();
            
            clusters.add(cluster);
        }
        
        return clusters;
    }
}
