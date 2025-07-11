package com.plg.dto;

import java.time.LocalDateTime;

import com.plg.utils.TipoSimulacion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar los parámetros de simulación.
 * Permite leer y actualizar los parámetros desde el frontend.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParametrosDto {

    private String dia;
    private String mes;
    private String anho;
    private LocalDateTime fechaInicial;
    private LocalDateTime fechaFinal;
    private int intervaloTiempo;
    private int contadorPrueba;
    private double kilometrosRecorridos;
    private double fitnessGlobal;
    private long semillaAleatoria;
    private boolean primeraLlamada;
    private TipoSimulacion tipoSimulacion;
}