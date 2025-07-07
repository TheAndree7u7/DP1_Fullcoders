package com.plg.dto.request;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para recibir un lote de pedidos desde el frontend.
 */
@Data
public class PedidosLoteRequest {
    
    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDateTime fechaInicio;
    
    @NotEmpty(message = "La lista de pedidos no puede estar vacía")
    @Valid
    private List<PedidoLoteItem> pedidos;
    
    private String descripcion; // Descripción opcional del lote (ej: "Pedidos Enero 2025")
    
    /**
     * Item individual dentro del lote de pedidos
     */
    @Data
    public static class PedidoLoteItem {
        @NotNull(message = "La fecha del pedido es obligatoria")
        private LocalDateTime fechaPedido;
        
        @NotNull(message = "Las coordenadas X son obligatorias")
        private Integer x; // columna
        
        @NotNull(message = "Las coordenadas Y son obligatorias") 
        private Integer y; // fila
        
        @NotNull(message = "El volumen de GLP es obligatorio")
        private Double volumenGLP;
        
        @NotNull(message = "Las horas límite son obligatorias")
        private Double horasLimite;
        
        private String cliente; // Código del cliente (opcional)
    }
}