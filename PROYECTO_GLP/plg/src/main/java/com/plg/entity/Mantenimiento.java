package com.plg.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
 
import java.time.LocalDate;

@Entity
@Data
@Getter
@Setter
@Builder
@Table(name = "mantenimiento")
@NoArgsConstructor
@AllArgsConstructor
public class Mantenimiento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "camion_codigo")
    @JsonBackReference(value="camion-mantenimiento")
    private Camion camion;
    
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String tipo; // preventivo, correctivo
    private String descripcion;
    private int estado; // 0: programado, 1: en proceso, 2: finalizado
    
    
}