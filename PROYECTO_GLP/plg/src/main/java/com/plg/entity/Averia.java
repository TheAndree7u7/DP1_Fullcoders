package com.plg.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Random;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Averia {
    private Long id;
    private Camion camion;
    private TipoTurno turno;           // T1, T2, T3
    private TipoIncidente tipoIncidente;   // TI1, TI2, TI3
    private int posX;
    private int posY;
    private int estado;             // 0: reportada, 1: atendida, 2: reparada

}
