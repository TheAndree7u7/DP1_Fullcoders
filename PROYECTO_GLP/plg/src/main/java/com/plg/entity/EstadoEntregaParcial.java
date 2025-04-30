package com.plg.entity;

/**
 * Enumerado que representa los posibles estados de una entrega parcial
 */
public enum EstadoEntregaParcial {
    ASIGNADO("Entrega asignada a un camión", "#CCCCCC"),
    EN_RUTA("Camión en ruta para realizar la entrega", "#3399FF"),
    ENTREGADO("Entrega completada satisfactoriamente", "#00CC66"),
    CANCELADO("Entrega cancelada por alguna razón", "#FF3333");

    private final String descripcion;
    private final String colorHex;

    EstadoEntregaParcial(String descripcion, String colorHex) {
        this.descripcion = descripcion;
        this.colorHex = colorHex;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getColorHex() {
        return colorHex;
    }
    
    /**
     * Converts an integer value to corresponding EstadoEntregaParcial enum
     * @param value Integer value representing the enum ordinal
     * @return The corresponding EstadoEntregaParcial enum value
     * @throws IllegalArgumentException if no enum constant with the specified ordinal exists
     */
    public static EstadoEntregaParcial fromValue(int value) {
        for (EstadoEntregaParcial estado : EstadoEntregaParcial.values()) {
            if (estado.ordinal() == value) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Invalid EstadoEntregaParcial value: " + value);
    }
}