package com.plg.service;

import com.plg.dto.PedidoDTO;
import com.plg.entity.Pedido;
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
        
        // Si no se especificó un estado, establecerlo como pendiente (0)
        if (pedido.getEstado() == 0 && pedidoDTO.getEstado() == null) {
            pedido.setEstado(0);
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
    
    public List<Pedido> findByEstado(int estado) {
        return pedidoRepository.findByEstado(estado);
    }
    
    public List<PedidoDTO> findByEstadoDTO(int estado) {
        return pedidoRepository.findByEstado(estado).stream()
                .map(DtoConverter::toPedidoDTO)
                .collect(Collectors.toList());
    }
}