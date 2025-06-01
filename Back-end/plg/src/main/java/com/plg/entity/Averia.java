package com.plg.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

import com.plg.factory.CamionFactory;
import com.plg.utils.ExcepcionesPerzonalizadas.InvalidDataFormatException;


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


    public Averia(String line) throws InvalidDataFormatException {
        if (line == null || line.trim().isEmpty()) {
            throw new InvalidDataFormatException("La línea de avería no puede ser nula o vacía.");
        }

        String[] partes = line.split("_");
        if (partes.length != 3) {
            throw new InvalidDataFormatException("Formato de línea de avería incorrecto. Se esperaban 3 partes separadas por '_'. Línea: " + line);
        }

        String turnoStr = partes[0];
        String codigoCamion = partes[1];
        String tipoIncidenteStr = partes[2];

        if (turnoStr.trim().isEmpty() || codigoCamion.trim().isEmpty() || tipoIncidenteStr.trim().isEmpty()) {
            throw new InvalidDataFormatException("Los componentes de la avería (turno, código de camión, tipo de incidente) no pueden estar vacíos. Línea: " + line);
        }
        
        this.turno = new TipoTurno(turnoStr); // El constructor de TipoTurno ya valida
        this.tipoIncidente = new TipoIncidente(tipoIncidenteStr); // El constructor de TipoIncidente ya valida

        try {
            this.camion = CamionFactory.getCamionPorCodigo(codigoCamion);
        } catch (NoSuchElementException e) {
            throw new InvalidDataFormatException("Error en la línea de avería: " + line + ". Detalles: " + e.getMessage(), e);
        }
    }


}
