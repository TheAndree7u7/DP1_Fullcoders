package com.plg.dto.request;

import java.time.LocalDateTime;

import com.plg.entity.Coordenada;
import com.plg.entity.TipoIncidente;
import com.plg.entity.Averia;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.plg.entity.Camion;

/**
 * DTO para la creación de una nueva avería.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AveriaRequest {

    private String codigoCamion;
    private TipoIncidente tipoIncidente; // TI1, TI2, TI3
 
    private Coordenada coordenada;
    private LocalDateTime fechaHoraReparacion;
    private LocalDateTime fechaHoraDisponible;
    private int turnoOcurrencia;
    private double tiempoReparacionEstimado;
    private Boolean estado; // true: activo, false: inactivo

    //Contructor que pasa los campos 
    //Request to averia
    Averia toAveria() {
        return Averia.builder()
                .camion(Camion.builder().codigo(codigoCamion).build())
                .tipoIncidente(tipoIncidente)
                .coordenada(coordenada)
                .fechaHoraReparacion(fechaHoraReparacion)
                .fechaHoraDisponible(fechaHoraDisponible)
                .turnoOcurrencia(turnoOcurrencia)
                .tiempoReparacionEstimado(tiempoReparacionEstimado)
                .estado(estado)
                .build();
    }

}
