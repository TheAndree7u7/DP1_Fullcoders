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

    private Camion camion;
    private TipoTurno turno; // T1, T2, T3
    private TipoIncidente tipoIncidente; // TI1, TI2, TI3
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private Boolean estado; // true: activo, false: inactivo


    public Averia(String line) {
        String[] partes = line.split("_");
        String turno = partes[0];
        String codigoCamion = partes[1];
        String tipoIncidente = partes[2];
        TipoTurno tipoTurno = new TipoTurno(turno);
        TipoIncidente tipoIncidenteObj = new TipoIncidente(tipoIncidente);

        Camion camion = CamionFactory.camiones.stream()
                .filter(c -> c.getCodigo().equals(codigoCamion))
                .findFirst()
                .orElse(null);

        this.camion = camion;
        this.turno = tipoTurno;
        this.tipoIncidente = tipoIncidenteObj;

        
    }

}
