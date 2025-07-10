package com.plg.dto.request;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * DTO para la creación de un nuevo pedido.
 */
@Data
public class PedidoRequest {
    private int x; // columna
    private int y; // fila
    private double volumenGLP;
    private double horasLimite;
    private LocalDateTime fechaRegistro;
}
