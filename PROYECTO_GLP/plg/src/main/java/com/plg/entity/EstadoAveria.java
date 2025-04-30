package com.plg.entity;

/**
 * Enumerado que representa los posibles estados de una avería
 */
public enum EstadoAveria {
    REPORTADA("Avería reportada, pendiente de atención", "#FF3333"),
    ATENDIDA("Avería siendo atendida por personal técnico", "#FFCC00"),
    REPARADA("Avería reparada, camión operativo", "#00CC66");

    private final String descripcion;
    private final String colorHex;

    EstadoAveria(String descripcion, String colorHex) {
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
     * Converts an integer value to corresponding EstadoAveria enum
     * @param value Integer value representing the enum ordinal
     * @return The corresponding EstadoAveria enum value
     * @throws IllegalArgumentException if no enum constant with the specified ordinal exists
     */
    public static EstadoAveria fromValue(int value) {
        for (EstadoAveria estado : EstadoAveria.values()) {
            if (estado.ordinal() == value) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Invalid EstadoAveria value: " + value);
    }
}