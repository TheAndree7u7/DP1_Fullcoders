package com.plg.entity;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.NoSuchElementException;

import com.plg.factory.CamionFactory;
import com.plg.utils.ExcepcionesPerzonalizadas.InvalidDataFormatException;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Averia {

    private Camion camion;

    private TipoIncidente tipoIncidente; // TI1, TI2, TI3
    private LocalDateTime fechaHoraReporte;
    private LocalDateTime fechaHoraReparacion;
    private LocalDateTime fechaHoraDisponible;
    private LocalDateTime fechaHoraFinEsperaEnRuta;
    private int turnoOcurrencia;
    private double tiempoReparacionEstimado;
    private Boolean estado = true; // true: activo, false: inactivo
    private Coordenada coordenada;

    @Override
    public String toString() {
        return String.format("%s_%s_%s", turnoOcurrencia, camion.getCodigo(), tipoIncidente.getCodigo());
    }

    public Averia(String line) throws InvalidDataFormatException {
        if (line == null || line.trim().isEmpty()) {
            throw new InvalidDataFormatException("La línea de avería no puede ser nula o vacía.");
        }

        String[] partes = line.split("_");
        if (partes.length != 3) {
            throw new InvalidDataFormatException(
                    "Formato de línea de avería incorrecto. Se esperaban 3 partes separadas por '_'. Línea: " + line);
        }

        String turnoStr = partes[0];
        String codigoCamion = partes[1];
        String tipoIncidenteStr = partes[2];

        if (turnoStr.trim().isEmpty() || codigoCamion.trim().isEmpty() || tipoIncidenteStr.trim().isEmpty()) {
            throw new InvalidDataFormatException(
                    "Los componentes de la avería (turno, código de camión, tipo de incidente) no pueden estar vacíos. Línea: "
                            + line);
        }

        this.tipoIncidente = new TipoIncidente(tipoIncidenteStr); // El constructor de TipoIncidente ya valida

        turnoStr = turnoStr.replace("T", "");
        this.turnoOcurrencia = Integer.parseInt(turnoStr);
        try {
            this.camion = CamionFactory.getCamionPorCodigo(codigoCamion);
        } catch (NoSuchElementException e) {
            throw new InvalidDataFormatException(
                    "Error en la línea de avería: " + line + ". Detalles: " + e.getMessage(), e);
        }
    }

    public void calcularTurnoOcurrencia() {
        LocalTime hora = fechaHoraReporte.toLocalTime();
        if (hora.isBefore(LocalTime.of(8, 0))) {
            this.turnoOcurrencia = 1; // Turno 1: 00:00 - 08:00
        } else if (hora.isBefore(LocalTime.of(16, 0))) {
            this.turnoOcurrencia = 2; // Turno 2: 08:00 - 16:00
        } else {
            this.turnoOcurrencia = 3; // Turno 3: 16:00 - 24:00
        }
    }

    // calcula el tiempo que no estara disponible segun si importa turno o si s va
    // trasladar a almacen central
    public double calcularTiempoInoperatividad() {
        double tiempoInoperatividad = 0;

        if (this.tipoIncidente.isRequiereTraslado()) {
            if (this.tipoIncidente.isImportaTurnoDiaAveria()) {
                tiempoInoperatividad += this.tipoIncidente.getNTurnosEnReparacion() * 8.0;
            } else {
                tiempoInoperatividad += this.tipoIncidente.getHorasReparacionTaller()
                        + this.tipoIncidente.getHorasEsperaEnRuta();
            }
        } else {
            tiempoInoperatividad += this.tipoIncidente.getHorasReparacionRuta();
        }
        return tiempoInoperatividad;
    }

    // calcula la fecha y hora de disponibilidad
    public LocalDateTime calcularFechaHoraDisponible() {
        LocalDateTime fechaHoraDisponibleLocal = this.fechaHoraReporte;
        // si requiere traslado
        if (this.tipoIncidente.isRequiereTraslado()) {
            // si no importa el turno
            if (!this.tipoIncidente.isImportaTurnoDiaAveria()) {
                fechaHoraDisponibleLocal = fechaHoraDisponibleLocal.toLocalDate().plusDays(1).atStartOfDay();
                fechaHoraDisponibleLocal = fechaHoraDisponibleLocal
                        .plusHours((long) this.tipoIncidente.getHorasReparacionTaller());
                fechaHoraDisponibleLocal = fechaHoraDisponibleLocal
                        .plusHours((long) this.tipoIncidente.getHorasEsperaEnRuta());
            } else {
                // aca solamente si es turno uno
                switch (this.turnoOcurrencia) {
                    case 1 ->
                        fechaHoraDisponibleLocal = fechaHoraDisponibleLocal.toLocalDate().atStartOfDay().plusHours(8);
                    case 2 ->
                        fechaHoraDisponibleLocal = fechaHoraDisponibleLocal.toLocalDate().atStartOfDay().plusHours(16);
                    case 3 ->
                        fechaHoraDisponibleLocal = fechaHoraDisponibleLocal.toLocalDate().atStartOfDay().plusHours(24);
                    default -> {
                    }
                }
                fechaHoraDisponibleLocal = fechaHoraDisponibleLocal
                        .plusHours((long) this.tipoIncidente.getHorasReparacionTaller());
                fechaHoraDisponibleLocal = fechaHoraDisponibleLocal
                        .plusHours((long) this.tipoIncidente.getHorasEsperaEnRuta());
            }
        } else {
            // si no requiere traslado
            fechaHoraDisponibleLocal = fechaHoraDisponibleLocal
                    .plusHours((long) this.tipoIncidente.getHorasReparacionRuta());
        }
        return fechaHoraDisponibleLocal;
    }

    public LocalDateTime calcularFechaHoraFinEsperaEnRuta() {
        LocalDateTime fechaHoraFinEspera = this.fechaHoraReporte;
        if (this.tipoIncidente.isRequiereTraslado()) {
            fechaHoraFinEspera = fechaHoraFinEspera.plusHours((long) this.tipoIncidente.getHorasEsperaEnRuta());
        } else {
            fechaHoraFinEspera = fechaHoraFinEspera.plusHours((long) this.tipoIncidente.getHorasReparacionRuta());
        }
        return fechaHoraFinEspera;
    }
}
