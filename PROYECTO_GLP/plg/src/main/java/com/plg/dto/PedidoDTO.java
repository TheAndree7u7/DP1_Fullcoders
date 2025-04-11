package com.plg.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoDTO {
    private String fechaHora; // 11d13h31m
    private int posX;
    private int posY;
    private String idCliente;
    private int m3;
    private int horasLimite;
}