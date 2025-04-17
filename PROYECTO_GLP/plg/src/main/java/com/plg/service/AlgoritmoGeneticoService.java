package com.plg.service;

import com.plg.dto.*;
import com.plg.entity.Pedido;
import com.plg.repository.PedidoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AlgoritmoGeneticoService {

    private static final Logger logger = LoggerFactory.getLogger(AlgoritmoGeneticoService.class);
    
    @Autowired
    private PedidoRepository pedidoRepository;
    
    // Parámetros configurables del algoritmo genético
    private final int POBLACION_INICIAL = 50;
    private final int MAX_GENERACIONES = 100;
    private final double TASA_MUTACION = 0.1;
    private final double TASA_CRUCE = 0.8;
    
    public AlgoritmoGeneticoResultadoDTO generarRutas(Map<String, Object> params) {
        logger.info("Iniciando generación de rutas con algoritmo genético. Parámetros: {}", params);
        
        // Obtener pedidos pendientes
        List<Pedido> pedidos = pedidoRepository.findByEstado(0);
        logger.info("Pedidos pendientes encontrados: {}", pedidos.size());
        
        // Verificar si hay suficientes pedidos para optimizar
        if (pedidos.isEmpty()) {
            logger.warn("No hay pedidos pendientes para generar rutas");
            return AlgoritmoGeneticoResultadoDTO.builder()
                .metodo("algoritmoGenetico")
                .totalPedidos(0)
                .pedidosAsignados(0)
                .rutas(Collections.emptyList())
                .build();
        }
        
        // Parámetros opcionales
        int numeroRutas = params.containsKey("numeroRutas") ? 
                         (int) params.get("numeroRutas") : 3;
        logger.info("Generando {} rutas para {} pedidos", numeroRutas, pedidos.size());
        
        // Implementación simplificada - en un caso real aquí iría el algoritmo genético completo
        // que optimizaría las rutas considerando capacidades de camiones, restricciones temporales, etc.
        
        // Generamos rutas simuladas
        List<RutaDTO> rutas = generarRutasSimuladas(pedidos, numeroRutas);
        logger.info("Rutas generadas exitosamente: {}", rutas.size());
        
        // Preparamos el resultado usando DTO
        AlgoritmoGeneticoResultadoDTO resultado = AlgoritmoGeneticoResultadoDTO.builder()
            .rutas(rutas)
            .metodo("algoritmoGenetico")
            .totalPedidos(pedidos.size())
            .pedidosAsignados(pedidos.size())
            .build();
        
        logger.info("Generación de rutas completada. Pedidos asignados: {}/{}", 
            resultado.getPedidosAsignados(), resultado.getTotalPedidos());
        
        return resultado;
    }
    
    // Método que simula la generación de rutas - en un caso real esto implementaría el AG completo
    private List<RutaDTO> generarRutasSimuladas(List<Pedido> pedidos, int numeroRutas) {
        logger.debug("Iniciando generación de rutas simuladas con {} pedidos", pedidos.size());
        List<RutaDTO> rutas = new ArrayList<>();
        
        // Dividir los pedidos en grupos (clusters) para simular las rutas
        List<List<Pedido>> grupos = dividirEnGrupos(pedidos, numeroRutas);
        logger.debug("Pedidos divididos en {} grupos", grupos.size());
        
        // Para cada grupo creamos una ruta
        for (int i = 0; i < grupos.size(); i++) {
            logger.debug("Generando ruta {} con {} pedidos", (i+1), grupos.get(i).size());
            
            // Convertir pedidos a DTOs
            List<PedidoDTO> pedidosDTO = grupos.get(i).stream()
                .map(this::convertirAPedidoDTO)
                .collect(Collectors.toList());
            
            // Generar puntos de la ruta
            List<PuntoRutaDTO> puntosRuta = generarPuntosRuta(grupos.get(i));
            
            // Crear la ruta como DTO
            RutaDTO ruta = RutaDTO.builder()
                .idRuta("R" + (i + 1))
                .distanciaTotal(120.0 + (20 * Math.random())) // Valor simulado
                .tiempoEstimado(180 + (i * 30)) // Minutos (simulado)
                .pedidos(pedidosDTO)
                .numeroPedidos(grupos.get(i).size())
                .puntos(puntosRuta)
                .build();
            
            rutas.add(ruta);
            logger.debug("Ruta R{} generada con éxito", (i+1));
        }
        
        return rutas;
    }
    
    // Método auxiliar para dividir pedidos en grupos (simulado)
    private List<List<Pedido>> dividirEnGrupos(List<Pedido> pedidos, int numeroGrupos) {
        logger.debug("Dividiendo {} pedidos en {} grupos", pedidos.size(), numeroGrupos);
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
    
    // Convierte un pedido a un DTO
    private PedidoDTO convertirAPedidoDTO(Pedido pedido) {
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
    
    // Genera puntos de ruta (simulado - en un escenario real se utilizaría el AG)
    private List<PuntoRutaDTO> generarPuntosRuta(List<Pedido> pedidos) {
        logger.debug("Generando puntos de ruta para {} pedidos", pedidos.size());
        List<PuntoRutaDTO> puntos = new ArrayList<>();
        
        // El primer punto es el almacén (origen)
        puntos.add(PuntoRutaDTO.builder()
            .tipo("ALMACEN")
            .posX(0)
            .posY(0)
            .build());
        
        // Agregamos cada pedido como un punto de la ruta
        for (Pedido pedido : pedidos) {
            puntos.add(PuntoRutaDTO.builder()
                .tipo("CLIENTE")
                .posX(pedido.getPosX())
                .posY(pedido.getPosY())
                .idPedido(pedido.getId())
                .build());
        }
        
        // El último punto es el retorno al almacén
        puntos.add(PuntoRutaDTO.builder()
            .tipo("ALMACEN")
            .posX(0)
            .posY(0)
            .build());
        
        logger.debug("Generados {} puntos de ruta", puntos.size());
        return puntos;
    }
}