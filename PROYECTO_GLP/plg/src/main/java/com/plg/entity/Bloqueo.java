package com.plg.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bloqueo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private int posXInicio;
    private int posYInicio;
    private int posXFin;
    private int posYFin;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String descripcion;
    private boolean activo;
}