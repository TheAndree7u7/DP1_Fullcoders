package com.plg.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class TipoIncidente {

    /**
     * Código del tipo de avería (TI1, TI2, TI3)
     */
    private String codigo;

    /**
     * Descripción detallada del tipo de avería
     */
    private String descripcion;

    /**
     * Tiempo de inmovilización en el lugar de la avería (en horas)
     */
    private double horasEsperaEnRuta;

    /**
     * Tiempo de reparación en ruta
     */
    private double horasReparacionRuta;

    /**
     * Tiempo necesario en el taller para reparación completa (en horas)
     */
    private double horasReparacionTaller;

    /**
     * Indica si la unidad debe ser llevada al almacén después de la
     * inmovilización
     */
    private boolean requiereTraslado;

    /**
     * Indica si la unidad puede continuar su ruta después de la reparación in
     * situ
     */
    private boolean puedeContinuarRuta;

    /**
     * Ejemplo: 1-leve (Tipo 1), 2-moderada (Tipo 2), 3-grave (Tipo 3)
     */
    private int gravedad;

    /**
     * Indica si durante la inmovilización se puede transferir la carga a otra
     * unidad
     */
    private boolean permiteTrasvase;

    /**
     * Indica si importa o no el turno en el que ocurrió la avería
     */
    private boolean importaTurnoDiaAveria;

    /**
     * Indica si el turno en que ocurrió la avería es el mismo en que se repara
     */
    private boolean esTurnoReparacion;

    /**
     * Número de turnos de reparación
     */
    private int nTurnosEnReparacion;

    /**
     * Número de días de reparación
     */
    private int diasReparacion;

    public TipoIncidente(String codigo) {
        this.codigo = codigo;
        initDefaultAverias();
    }

    public void initDefaultAverias() {

        switch (this.codigo) {
            case "TI1" -> {
                descripcion = "Reparable en el lugar por el conductor";
                horasEsperaEnRuta = 0;
                horasReparacionRuta = 2;
                horasReparacionTaller = 0;
                requiereTraslado = false;
                puedeContinuarRuta = true;
                gravedad = 1;
                permiteTrasvase = false;
                importaTurnoDiaAveria = false;
                esTurnoReparacion = false;
                nTurnosEnReparacion = 0;
                diasReparacion = 0;
            }
            case "TI2" -> {
                descripcion = "Problema que requiere taller";
                horasEsperaEnRuta = 2;
                horasReparacionTaller = 8;
                requiereTraslado = true;
                puedeContinuarRuta = false;
                gravedad = 2;
                permiteTrasvase = true;
                importaTurnoDiaAveria = true;
                esTurnoReparacion = true;
                nTurnosEnReparacion = 1;
                diasReparacion = 0;
            }
            case "TI3" -> {
                descripcion = "Problema grave como un choque";
                horasEsperaEnRuta = 4;
                horasReparacionTaller = 48;
                requiereTraslado = true;
                puedeContinuarRuta = false;
                gravedad = 3;
                permiteTrasvase = true;
                importaTurnoDiaAveria = false;
                esTurnoReparacion = true;
                nTurnosEnReparacion = 0;
                diasReparacion = 3;
            }
            default ->
                throw new IllegalArgumentException("Código de avería no reconocido: " + codigo);
        }
    }

}
