package com.plg.dto.request;

import java.time.LocalDateTime;

import com.plg.utils.TipoSimulacion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO para actualizar parámetros de simulación.
 * Permite actualizar parámetros específicos desde el frontend.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParametrosRequest {

    private String dia;
    private String mes;
    private String anho;
    private LocalDateTime fechaInicial;
    private LocalDateTime fechaFinal;
    private Integer intervaloTiempo;
    private Integer contadorPrueba;
    private Double kilometrosRecorridos;
    private Double fitnessGlobal;
    private Long semillaAleatoria;
    private Boolean primeraLlamada;
    private TipoSimulacion tipoSimulacion;
}