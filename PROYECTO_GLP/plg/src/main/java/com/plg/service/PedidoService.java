package com.plg.service;

import com.plg.dto.PedidoDTO;
import com.plg.entity.Pedido;
import com.plg.entity.EstadoPedido;
import com.plg.repository.PedidoRepository;
import com.plg.util.DtoConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    public List<Pedido> findAll() {
        return pedidoRepository.findAll();
    }
    
    public List<PedidoDTO> findAllDTO() {
        return pedidoRepository.findAll().stream()
                .map(DtoConverter::toPedidoDTO)
                .collect(Collectors.toList());
    }

    public Optional<Pedido> findById(Long id) {
        return pedidoRepository.findById(id);
    }
    
    public PedidoDTO findByIdDTO(Long id) {
        return pedidoRepository.findById(id)
                .map(DtoConverter::toPedidoDTO)
                .orElse(null);
    }

    public Pedido save(PedidoDTO pedidoDTO) {
        // Usar el convertidor para crear una entidad Pedido desde el DTO
        Pedido pedido = DtoConverter.toPedido(pedidoDTO);
        
        // Si no se especificó un estado, establecerlo como REGISTRADO
        if (pedido.getEstado() == null) {
            pedido.setEstado(EstadoPedido.REGISTRADO);
        }
        
        return pedidoRepository.save(pedido);
    }

    public Pedido update(Long id, PedidoDTO pedidoDTO) {
        // Verificar si el pedido existe
        Pedido pedidoExistente = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        // Usar el convertidor para actualizar los campos del pedido existente
        Pedido pedidoActualizado = DtoConverter.toPedido(pedidoDTO);
        pedidoActualizado.setId(id); // Asegurar que el ID sea el correcto
        
        return pedidoRepository.save(pedidoActualizado);
    }

    public void delete(Long id) {
        pedidoRepository.deleteById(id);
    }
    
    /**
     * Encuentra pedidos por su estado usando valores enteros (para compatibilidad)
     */
    public List<Pedido> findByEstado(EstadoPedido estado) {
        return pedidoRepository.findByEstado(estado);
    }
    
    /**
     * Encuentra pedidos por su estado usando el enum (método preferido)
     */
    public List<Pedido> findByEstadoEnum(EstadoPedido estado) {
        return pedidoRepository.findByEstado(estado);
    }
    
    /**
     * Encuentra pedidos por su estado y los convierte a DTO (usando valores enteros)
     */
    public List<PedidoDTO> findByEstadoDTO(EstadoPedido estado) {
        return pedidoRepository.findByEstado(estado).stream()
                .map(DtoConverter::toPedidoDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Encuentra pedidos por su estado y los convierte a DTO (usando enum - método preferido)
     */
    public List<PedidoDTO> findByEstadoEnumDTO(EstadoPedido estado) {
        return pedidoRepository.findByEstado(estado).stream()
                .map(DtoConverter::toPedidoDTO)
                .collect(Collectors.toList());
    }
}