package com.plg.service;

import com.plg.dto.*;
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
    
    public AlgoritmoGeneticoResultadoDTO generarRutas(Map<String, Object> params) {
        // Obtener pedidos pendientes
        List<Pedido> pedidos = pedidoRepository.findByEstado(0);
        
        // Verificar si hay suficientes pedidos para optimizar
        if (pedidos.isEmpty()) {
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
        
        // Implementación simplificada - en un caso real aquí iría el algoritmo genético completo
        // que optimizaría las rutas considerando capacidades de camiones, restricciones temporales, etc.
        
        // Generamos rutas simuladas
        List<RutaDTO> rutas = generarRutasSimuladas(pedidos, numeroRutas);
        
        // Preparamos el resultado usando DTO
        return AlgoritmoGeneticoResultadoDTO.builder()
            .rutas(rutas)
            .metodo("algoritmoGenetico")
            .totalPedidos(pedidos.size())
            .pedidosAsignados(pedidos.size())
            .build();
    }
    
    // Método que simula la generación de rutas - en un caso real esto implementaría el AG completo
    private List<RutaDTO> generarRutasSimuladas(List<Pedido> pedidos, int numeroRutas) {
        List<RutaDTO> rutas = new ArrayList<>();
        
        // Dividir los pedidos en grupos (clusters) para simular las rutas
        List<List<Pedido>> grupos = dividirEnGrupos(pedidos, numeroRutas);
        
        // Para cada grupo creamos una ruta
        for (int i = 0; i < grupos.size(); i++) {
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
        
        return puntos;
    }
}