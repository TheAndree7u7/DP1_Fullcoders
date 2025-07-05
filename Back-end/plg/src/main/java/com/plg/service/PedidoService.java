package com.plg.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.plg.config.DataLoader;
import com.plg.dto.request.PedidoRequest;
import com.plg.entity.Camion;
import com.plg.entity.Coordenada;
import com.plg.entity.EstadoCamion;
import com.plg.entity.Pedido;
import com.plg.factory.PedidoFactory;
import com.plg.repository.PedidoRepository;
import com.plg.utils.ExcepcionesPerzonalizadas.InvalidInputException;

/**
 * Servicio para operaciones sobre pedidos.
 */
@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;

    public PedidoService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    /**
     * Retorna la lista de pedidos actuales.
     */
    public List<Pedido> listar() {
        return pedidoRepository.findAll();
    }

    /**
     * Lista los pedidos registrados entre dos fechas.
     *
     * @param inicio fecha y hora inicial
     * @param fin    fecha y hora final
     * @return lista de pedidos en el rango
     */
    public List<Pedido> listarPorFecha(LocalDateTime inicio, LocalDateTime fin) {
        return pedidoRepository.findAllBetween(inicio, fin);
    }

    /**
     * Calcula un resumen simple por estado de los pedidos.
     */
    public Map<String, Object> resumen() {
        List<Pedido> pedidos = pedidoRepository.findAll();
        Map<String, Object> datos = new HashMap<>();
        datos.put("total", pedidos.size());
        datos.put("porEstado",
                pedidos.stream()
                        .collect(Collectors.groupingBy(p -> p.getEstado().name(), Collectors.counting())));
        return datos;
    }

    /**
     * Crea un nuevo pedido utilizando los datos de la solicitud.
     * Si el pedido es demasiado grande para ser manejado por un solo camión,
     * se dividirá automáticamente en múltiples pedidos.
     */
    public List<Pedido> agregar(PedidoRequest request) {
        try {
            Coordenada coordenada = new Coordenada(request.getY(), request.getX());
            
            // Verificar si el pedido necesita ser dividido
            if (necesitaDivision(request.getVolumenGLP())) {
                return dividirPedido(coordenada, request.getVolumenGLP(), request.getHorasLimite());
            } else {
                // Crear pedido normal
                Pedido pedido = PedidoFactory.crearPedido(coordenada, request.getVolumenGLP(), request.getHorasLimite());
                return List.of(pedidoRepository.save(pedido));
            }
        } catch (Exception e) {
            throw new InvalidInputException("No se pudo crear el pedido", e);
        }
    }

    /**
     * Verifica si un pedido necesita ser dividido en función de las capacidades de los camiones.
     */
    private boolean necesitaDivision(double volumenGLP) {
        if (volumenGLP <= 0) {
            return false;
        }
        double capacidadMaxima = obtenerCapacidadMaximaCamion();
        return volumenGLP > capacidadMaxima;
    }

    /**
     * Obtiene la capacidad máxima de GLP de todos los camiones disponibles.
     */
    private double obtenerCapacidadMaximaCamion() {
        List<Camion> camionesDisponibles = DataLoader.camiones.stream()
                .filter(camion -> camion.getEstado() != EstadoCamion.EN_MANTENIMIENTO_PREVENTIVO)
                .collect(Collectors.toList());
        
        if (camionesDisponibles.isEmpty()) {
            camionesDisponibles = DataLoader.camiones;
        }
        
        return camionesDisponibles.stream()
                .mapToDouble(Camion::getCapacidadMaximaGLP)
                .max()
                .orElse(200.0); // Valor por defecto si no hay camiones
    }

    /**
     * Divide un pedido grande en múltiples pedidos más pequeños.
     */
    private List<Pedido> dividirPedido(Coordenada coordenada, double volumenTotal, double horasLimite) {
        List<Pedido> pedidosDivididos = new ArrayList<>();
        
        // Validar entrada
        if (volumenTotal <= 0) {
            throw new InvalidInputException("El volumen total debe ser mayor a cero");
        }
        
        // Obtener capacidades de todos los camiones disponibles
        List<Double> capacidadesCamiones = obtenerCapacidadesCamiones();
        
        if (capacidadesCamiones.isEmpty()) {
            throw new InvalidInputException("No hay camiones disponibles para dividir el pedido");
        }
        
        // Calcular la división óptima
        List<Double> volumenePorPedido = calcularDivisionOptima(volumenTotal, capacidadesCamiones);
        
        // Validar que la división sea exitosa
        double totalDividido = volumenePorPedido.stream().mapToDouble(Double::doubleValue).sum();
        if (Math.abs(totalDividido - volumenTotal) > 0.001) {
            throw new InvalidInputException("Error en la división del pedido: volumen total no coincide");
        }
        
        // Crear pedidos divididos
        for (int i = 0; i < volumenePorPedido.size(); i++) {
            double volumenPedido = volumenePorPedido.get(i);
            
            // Generar código único para cada pedido dividido
            String codigoBase = "PEDIDO-" + coordenada.getFila() + "-" + coordenada.getColumna();
            String codigoCompleto = codigoBase + "-DIV" + (i + 1);
            
            Pedido pedido = Pedido.builder()
                    .coordenada(coordenada)
                    .bloqueado(false)
                    .gScore(0)
                    .fScore(0)
                    .tipoNodo(com.plg.entity.TipoNodo.PEDIDO)
                    .codigo(codigoCompleto)
                    .horasLimite(horasLimite)
                    .volumenGLPAsignado(volumenPedido)
                    .estado(com.plg.entity.EstadoPedido.REGISTRADO)
                    .build();
            
            pedidosDivididos.add(pedidoRepository.save(pedido));
        }
        
        System.out.println("Pedido dividido exitosamente: " + volumenTotal + " m³ en " + pedidosDivididos.size() + " pedidos");
        return pedidosDivididos;
    }

    /**
     * Obtiene las capacidades de todos los camiones disponibles.
     */
    private List<Double> obtenerCapacidadesCamiones() {
        List<Camion> camionesDisponibles = DataLoader.camiones.stream()
                .filter(camion -> camion.getEstado() != EstadoCamion.EN_MANTENIMIENTO_PREVENTIVO)
                .collect(Collectors.toList());
        
        if (camionesDisponibles.isEmpty()) {
            camionesDisponibles = DataLoader.camiones;
        }
        
        return camionesDisponibles.stream()
                .map(Camion::getCapacidadMaximaGLP)
                .sorted((a, b) -> Double.compare(b, a)) // Ordenar de mayor a menor
                .collect(Collectors.toList());
    }

    /**
     * Calcula la división óptima del volumen total entre los camiones disponibles.
     * Utiliza un algoritmo greedy para maximizar la utilización de los camiones.
     */
    private List<Double> calcularDivisionOptima(double volumenTotal, List<Double> capacidadesCamiones) {
        List<Double> volumenPorPedido = new ArrayList<>();
        double volumenRestante = volumenTotal;
        
        // Usar algoritmo greedy: asignar primero a los camiones más grandes
        int indiceCamion = 0;
        
        while (volumenRestante > 0 && indiceCamion < capacidadesCamiones.size()) {
            double capacidadCamion = capacidadesCamiones.get(indiceCamion);
            double volumenAsignado = Math.min(volumenRestante, capacidadCamion);
            
            volumenPorPedido.add(volumenAsignado);
            volumenRestante -= volumenAsignado;
            indiceCamion++;
        }
        
        // Si aún queda volumen después de asignar a todos los camiones,
        // comenzar un nuevo ciclo con los camiones más grandes
        while (volumenRestante > 0) {
            indiceCamion = 0;
            while (volumenRestante > 0 && indiceCamion < capacidadesCamiones.size()) {
                double capacidadCamion = capacidadesCamiones.get(indiceCamion);
                double volumenAsignado = Math.min(volumenRestante, capacidadCamion);
                
                volumenPorPedido.add(volumenAsignado);
                volumenRestante -= volumenAsignado;
                indiceCamion++;
            }
        }
        
        return volumenPorPedido;
    }

    /**
     * Actualiza solo el estado de un pedido por su código.
     *
     * @param request DTO con el código y el nuevo estado
     * @return El pedido actualizado
     */
    public Pedido actualizarEstado(com.plg.dto.request.PedidoEstadoUpdateRequest request) {
        Pedido pedido = pedidoRepository.findAll().stream()
                .filter(p -> p.getCodigo().equals(request.getCodigo()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + request.getCodigo()));
        pedido.setEstado(request.getEstado());
        return pedido;
    }
}
