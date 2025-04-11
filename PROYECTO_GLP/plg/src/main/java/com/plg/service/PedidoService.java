package com.plg.service;

import com.plg.dto.PedidoDTO;
import com.plg.entity.Cliente;
import com.plg.entity.Pedido;
import com.plg.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    public List<Pedido> findAll() {
        return pedidoRepository.findAll();
    }

    public Optional<Pedido> findById(Long id) {
        return pedidoRepository.findById(id);
    }

    public Pedido save(PedidoDTO pedidoDTO) {
        Pedido pedido = new Pedido();
        pedido.setFechaHora(pedidoDTO.getFechaHora());
        pedido.setPosX(pedidoDTO.getPosX());
        pedido.setPosY(pedidoDTO.getPosY());
        
        // Crear un cliente temporal con el ID proporcionado
        // En un escenario real, buscarías el cliente en la base de datos
        Cliente cliente = new Cliente();
        cliente.setId(pedidoDTO.getIdCliente());
        pedido.setCliente(cliente);
        
        pedido.setM3(pedidoDTO.getM3());
        pedido.setHorasLimite(pedidoDTO.getHorasLimite());
        pedido.setEstado(0); // Por defecto: pendiente
        
        return pedidoRepository.save(pedido);
    }

    public Pedido update(Long id, PedidoDTO pedidoDTO) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        pedido.setFechaHora(pedidoDTO.getFechaHora());
        pedido.setPosX(pedidoDTO.getPosX());
        pedido.setPosY(pedidoDTO.getPosY());

        // Actualizar cliente si es necesario
        if (pedidoDTO.getIdCliente() != null) {
            Cliente cliente = new Cliente();
            cliente.setId(pedidoDTO.getIdCliente());
            pedido.setCliente(cliente);
        }
        
        pedido.setM3(pedidoDTO.getM3());
        pedido.setHorasLimite(pedidoDTO.getHorasLimite());
        
        return pedidoRepository.save(pedido);
    }

    public void delete(Long id) {
        pedidoRepository.deleteById(id);
    }
    
    public List<Pedido> findByEstado(int estado) {
        return pedidoRepository.findByEstado(estado);
    }
}