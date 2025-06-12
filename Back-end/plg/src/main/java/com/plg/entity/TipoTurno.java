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

    public TipoTurno(String tipo) {
        this.tipo = tipo;
        switch (tipo) {
            case "T1":
                this.horaInicio = 0;
                this.horaFin = 8;
                break;
            case "T2":
                this.horaInicio = 8;
                this.horaFin = 16;
                break;
            case "T3":
                this.horaInicio = 16;
                this.horaFin = 24;
                break;
            default:
                throw new IllegalArgumentException("Tipo de turno no válido: " + tipo);
        }
    }
}
