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

    private LocalDateTime fechaHoraReporte; // Fecha y hora del reporte
    private Coordenada coordenada;
    private LocalDateTime fechaHoraReparacion;
    private LocalDateTime fechaHoraDisponible;
    private int turnoOcurrencia;
    private double tiempoReparacionEstimado;
    private Boolean estado; // true: activo, false: inactivo

    /**
     * Constructor copia que crea una nueva instancia de AveriaRequest copiando
     * los valores de otra instancia.
     *
     * @param other La instancia de AveriaRequest cuyos valores se copiarán
     */
    public AveriaRequest(AveriaRequest other) {

        this.codigoCamion = other.codigoCamion;
        this.tipoIncidente = other.tipoIncidente;

        this.coordenada = other.coordenada;
        this.fechaHoraReparacion = other.fechaHoraReparacion;
        this.fechaHoraDisponible = other.fechaHoraDisponible;
        this.fechaHoraReporte = other.fechaHoraReporte;
        
        this.turnoOcurrencia = other.turnoOcurrencia;
        this.tiempoReparacionEstimado = other.tiempoReparacionEstimado;

        this.estado = other.estado;
    }

    //Contructor que pasa los campos 
    //Request to averia
    public Averia toAveria() {
        return Averia.builder()
                .camion(Camion.builder().codigo(codigoCamion).build())
                .tipoIncidente(tipoIncidente)
                .coordenada(coordenada)
                .fechaHoraReporte(fechaHoraReporte)
                .fechaHoraReparacion(fechaHoraReparacion)
                .fechaHoraDisponible(fechaHoraDisponible)
                .turnoOcurrencia(turnoOcurrencia)
                .tiempoReparacionEstimado(tiempoReparacionEstimado)
                .estado(estado)
                .build();
    }

}
