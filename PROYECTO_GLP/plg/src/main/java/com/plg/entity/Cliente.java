package com.plg.entity;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    private String id;        // código único del cliente
    private String nombre;
    private String direccion;
    private String telefono;
    private String email;
    private double posX;
    private double posY;

    // Historial de pedidos del cliente
    private List<Pedido> pedidos;
}
