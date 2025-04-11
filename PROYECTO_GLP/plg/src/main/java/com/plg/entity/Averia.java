package com.plg.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Averia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "camion_codigo")
    private Camion camion;
    
    private LocalDateTime fechaHoraReporte;
    private String descripcion;
    private int severidad; // 1: leve, 2: moderada, 3: grave
    private int posX;
    private int posY;
    private int estado; // 0: reportada, 1: atendida, 2: reparada
}