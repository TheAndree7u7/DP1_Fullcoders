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
        if (dto.getPosX() != null) {
            cliente.setPosX(dto.getPosX());
        }
        if (dto.getPosY() != null) {
            cliente.setPosY(dto.getPosY());
        }
        
        return cliente;
    }
    
    /**
     * Convierte un Pedido a PedidoDTO
     */
    public static PedidoDTO toPedidoDTO(Pedido pedido) {
        if (pedido == null) {
            return null;
        }
        
        return PedidoDTO.builder()
                .id(pedido.getId())
                .codigo(pedido.getCodigo())
                .posX(pedido.getPosX())
                .posY(pedido.getPosY())
                .m3(pedido.getM3())
                .horasLimite(pedido.getHorasLimite())
                .clienteId(pedido.getCliente() != null ? pedido.getCliente().getId() : null)
                .clienteNombre(pedido.getCliente() != null ? pedido.getCliente().getNombre() : null)
                .fechaHora(pedido.getFechaHora())
                .estado(pedido.getEstado())
                .build();
    }
    
    /**
     * Convierte un PedidoDTO a Pedido
     */
    public static Pedido toPedido(PedidoDTO dto) {
        if (dto == null) {
            return null;
        }
        
        Pedido pedido = new Pedido();
        
        // Establecer ID solo si no es nulo
        if (dto.getId() != null) {
            pedido.setId(dto.getId());
        }
        
        // Establecer código
        pedido.setCodigo(dto.getCodigo());
        
        // Convertir Integer a int para posX y posY
        if (dto.getPosX() != null) {
            pedido.setPosX(dto.getPosX());
        }
        if (dto.getPosY() != null) {
            pedido.setPosY(dto.getPosY());
        }
        
        // Convertir Integer a int para m3
        if (dto.getM3() != null) {
            pedido.setM3(dto.getM3());
        }
        
        // Establecer horasLimite si no es nulo
        if (dto.getHorasLimite() != null) {
            pedido.setHorasLimite(dto.getHorasLimite());
        }
        
        // Establecer fechaHora como String
        pedido.setFechaHora(dto.getFechaHora());
        
        // Establecer estado solo si no es nulo
        if (dto.getEstado() != null) {
            pedido.setEstado(dto.getEstado());
        }
        
        // Si hay clienteId, creamos un cliente con ese ID
        if (dto.getClienteId() != null) {
            Cliente cliente = new Cliente();
            cliente.setId(dto.getClienteId());
            pedido.setCliente(cliente);
        }
        
        return pedido;
    }
}