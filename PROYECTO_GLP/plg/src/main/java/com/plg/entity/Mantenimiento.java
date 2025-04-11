package com.plg.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mantenimiento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "camion_codigo")
    private Camion camion;
    
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String tipo; // preventivo, correctivo
    private String descripcion;
    private int estado; // 0: programado, 1: en proceso, 2: finalizado
}