package com.plg.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Representa un mantenimiento de un camión (preventivo o correctivo).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mantenimiento {

    private LocalDateTime fecha;
    private Camion camion;
}
