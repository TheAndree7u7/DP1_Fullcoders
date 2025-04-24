package com.plg.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Representa un tipo de turno para los camiones.
 * Cada tipo de turno tiene un nombre y un rango horario.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoTurno {
    private String tipo;
    private int horaInicio;
    private int horaFin;    
}
