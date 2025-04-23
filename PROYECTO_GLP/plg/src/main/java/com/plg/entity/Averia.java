package com.plg.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.Random;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Averia {

    private Long id;
    private Camion camion;

    private LocalDateTime fechaHoraReporte;
    private String descripcion;
    private String turno;           // T1, T2, T3
    private String tipoIncidente;   // TI1, TI2, TI3
    private double posX;
    private double posY;
    private double kilometroOcurrencia;
    private int estado;             // 0: reportada, 1: atendida, 2: reparada
    private boolean conCarga;

    // Campos adicionales
    private LocalDateTime tiempoInmovilizacion;
    private LocalDateTime tiempoFinInoperatividad;
    private boolean requiereTraslado;
    private boolean esValida;

    /**
     * Calcula si una avería es válida según las condiciones:
     * - La unidad debe estar en operación
     * - La avería solo tiene sentido si lleva carga
     */
    public boolean calcularValidezAveria(boolean estaEnOperacion) {
        esValida = estaEnOperacion && conCarga;
        return esValida;
    }

    /**
     * Calcula el kilómetro de ocurrencia de la avería en el rango de 5% a 35% del tramo total
     */
    public void calcularKilometroOcurrencia(double distanciaTotal) {
        if (!esValida) return;
        Random random = new Random();
        double minKm = distanciaTotal * 0.05;
        double maxKm = distanciaTotal * 0.35;
        kilometroOcurrencia = minKm + (maxKm - minKm) * random.nextDouble();
    }

    /** Genera el registro según especificación */
    public String generarRegistro() {
        if (camion == null) return "";
        return String.format("%s_%s_%s",
                             turno,
                             camion.getCodigo(),
                             tipoIncidente);
    }

    /**
     * Calcula los tiempos de inmovilización y fin de inoperatividad
     * @param duracionTurnoHoras duración de cada turno en horas
     */
    public void calcularTiemposInoperatividad(int duracionTurnoHoras) {
        if (tipoIncidente == null || fechaHoraReporte == null) return;

        LocalDateTime ahora = fechaHoraReporte;

        switch (tipoIncidente) {
            case "TI1":
                // 2 horas, continúa ruta
                tiempoInmovilizacion     = ahora.plusHours(2);
                tiempoFinInoperatividad  = tiempoInmovilizacion;
                requiereTraslado         = false;
                break;

            case "TI2":
                // 2 horas + un turno
                tiempoInmovilizacion = ahora.plusHours(2);
                requiereTraslado     = true;
                tiempoFinInoperatividad = ahora.plusHours(duracionTurnoHoras * 2);
                break;

            case "TI3":
                // 4 horas + tres días completos
                tiempoInmovilizacion = ahora.plusHours(4);
                requiereTraslado     = true;
                tiempoFinInoperatividad = ahora
                    .plusDays(3)
                    .withHour(0)
                    .plusHours(duracionTurnoHoras);
                break;

            default:
                // Otro tipo: no hace nada
                break;
        }
    }

    /** Sobrecarga con turno de 8 h por defecto */
    public void calcularTiemposInoperatividad() {
        calcularTiemposInoperatividad(8);
    }
}
