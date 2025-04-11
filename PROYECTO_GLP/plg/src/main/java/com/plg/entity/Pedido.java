package com.plg.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String fechaHora; // formato: 11d13h31m
    private int posX;
    private int posY;
    
    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;
    
    private int m3;
    private int horasLimite;
    private int estado; // 0: pendiente, 1: asignado, 2: en ruta, 3: entregado, 4: cancelado
    
    @ManyToOne
    @JoinColumn(name = "camion_codigo")
    private Camion camion;
}