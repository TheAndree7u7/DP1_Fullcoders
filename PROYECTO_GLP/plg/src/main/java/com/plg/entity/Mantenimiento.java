package com.plg.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;

/**
 * Representa un mantenimiento de un camión (preventivo o correctivo).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mantenimiento {

    private Long id;
    private Camion camion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String tipo;       // "preventivo" o "correctivo"
    private String descripcion;
    private int estado;        // 0: programado, 1: en proceso, 2: finalizado

}
