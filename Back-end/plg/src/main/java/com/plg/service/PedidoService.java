package com.plg.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.plg.dto.request.PedidoRequest;
import com.plg.entity.Coordenada;
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
     */
    public Pedido agregar(PedidoRequest request) {
        try {
            Coordenada coordenada = new Coordenada(request.getY(), request.getX());
            Pedido pedido = PedidoFactory.crearPedido(coordenada, request.getVolumenGLP(), request.getHorasLimite());
            return pedidoRepository.save(pedido);
        } catch (Exception e) {
            throw new InvalidInputException("No se pudo crear el pedido", e);
        }
    }
}
