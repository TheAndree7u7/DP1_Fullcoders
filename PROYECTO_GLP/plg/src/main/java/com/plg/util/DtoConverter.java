package com.plg.util;

import com.plg.dto.ClienteDTO;
import com.plg.dto.PedidoDTO;
import com.plg.entity.Cliente;
import com.plg.entity.Pedido;

/**
 * Clase de utilidad para convertir entre entidades y DTOs
 */
public class DtoConverter {

    /**
     * Convierte un Cliente a ClienteDTO
     */
    public static ClienteDTO toClienteDTO(Cliente cliente) {
        if (cliente == null) {
            return null;
        }
        
        return ClienteDTO.builder()
                .id(cliente.getId())
                .nombre(cliente.getNombre())
                .telefono(cliente.getTelefono())
                .email(cliente.getEmail())
                .direccion(cliente.getDireccion())
                .posX(cliente.getPosX())
                .posY(cliente.getPosY())
                .build();
    }
    
    /**
     * Convierte un ClienteDTO a Cliente
     */
    public static Cliente toCliente(ClienteDTO dto) {
        if (dto == null) {
            return null;
        }
        
        Cliente cliente = new Cliente();
        cliente.setId(dto.getId());
        cliente.setNombre(dto.getNombre());
        cliente.setTelefono(dto.getTelefono());
        cliente.setEmail(dto.getEmail());
        cliente.setDireccion(dto.getDireccion());
        
        // Convertir Integer a int para posX y posY
        if (dto.getPosX() != 0.0) {
            cliente.setPosX(dto.getPosX());
        }
        if (dto.getPosY() != 0.0) {
            cliente.setPosY(dto.getPosY());
        }
        
        return cliente;
    }
    
    /**
     * Convierte una entidad Pedido a un DTO
     */
    public static PedidoDTO toPedidoDTO(Pedido pedido) {
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
            .fechaHora(pedido.getFechaHora())
            .estado(pedido.getEstado()) // Usar directamente el enum
            .build();
    }
    
    /**
     * Convierte un PedidoDTO a una entidad Pedido
     */
    public static Pedido toPedido(PedidoDTO dto) {
        if (dto == null) return null;
        
        Pedido pedido = new Pedido();
        pedido.setId(dto.getId());
        pedido.setCodigo(dto.getCodigo());
        pedido.setPosX(dto.getPosX());
        pedido.setPosY(dto.getPosY());
        pedido.setVolumenGLPAsignado(dto.getVolumenGLPAsignado());
        pedido.setHorasLimite(dto.getHorasLimite());
        pedido.setFechaHora(dto.getFechaHora());
        
        // Asignar estado usando el enum
        pedido.setEstado(dto.getEstado());
        
        // Si tenemos un cliente, lo asignaremos en el servicio
        // ya que necesitamos buscarlo en la base de datos
        
        return pedido;
    }
}