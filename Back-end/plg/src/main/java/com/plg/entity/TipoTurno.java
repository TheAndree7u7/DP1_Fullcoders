package com.plg.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representa un tipo de turno para los camiones. Cada tipo de turno tiene un
 * nombre y un rango horario.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class TipoTurno {

    private String tipo;

}
