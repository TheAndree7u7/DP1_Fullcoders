package com.plg.controller;

import com.plg.dto.PedidoDTO;
import com.plg.entity.Pedido;
import com.plg.enums.EstadoPedido;
import com.plg.service.PedidoService;
import com.plg.util.DtoConverter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @GetMapping
    public ResponseEntity<List<PedidoDTO>> getAllPedidos() {
        return ResponseEntity.ok(pedidoService.findAllDTO());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoDTO> getPedidoById(@PathVariable Long id) {
        PedidoDTO pedidoDTO = pedidoService.findByIdDTO(id);
        if (pedidoDTO != null) {
            return ResponseEntity.ok(pedidoDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Pedido> createPedido(@RequestBody PedidoDTO pedidoDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.save(pedidoDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Pedido> updatePedido(@PathVariable Long id, @RequestBody PedidoDTO pedidoDTO) {
        try {
            return ResponseEntity.ok(pedidoService.update(id, pedidoDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePedido(@PathVariable Long id) {
        Optional<Pedido> pedido = pedidoService.findById(id);
        if (pedido.isPresent()) {
            pedidoService.delete(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Endpoint que devuelve pedidos por estado usando el enum EstadoPedido
     */
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<PedidoDTO>> getPedidosByEstado(@PathVariable EstadoPedido estado) {
        return ResponseEntity.ok(pedidoService.findByEstadoEnumDTO(estado));
    }
    
 
    /**
     * Actualiza solo el estado de un pedido
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> updateEstadoPedido(@PathVariable Long id, @RequestBody Map<String, Object> estadoMap) {
        try {
            Pedido pedido = pedidoService.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
                
            // Si el cuerpo contiene 'estado' como enum (string)
            if (estadoMap.containsKey("estado")) {
                String estadoStr = estadoMap.get("estado").toString();
                try {
                    EstadoPedido nuevoEstado = EstadoPedido.valueOf(estadoStr);
                    pedido.setEstado(nuevoEstado);
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest()
                        .body("Estado inválido: " + estadoStr + ". Valores válidos: " + 
                              java.util.Arrays.toString(EstadoPedido.values()));
                }
            } 
  
            
            pedidoService.update(id, DtoConverter.toPedidoDTO(pedido));
            return ResponseEntity.ok(DtoConverter.toPedidoDTO(pedido));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest()
                .body("El valor de estadoInt debe ser un número entero");
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}