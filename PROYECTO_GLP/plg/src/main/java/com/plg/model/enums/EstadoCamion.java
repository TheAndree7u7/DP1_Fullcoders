package com.plg.model.enums;

public enum EstadoCamion {
    DISPONIBLE,
    EN_RUTA,
    AVERIADO,
    EN_MANTENIMIENTO;

    public static EstadoCamion fromInt(int value) {
        return switch (value) {
            case 0 -> DISPONIBLE;
            case 1 -> EN_RUTA;
            case 2 -> AVERIADO;
            case 3 -> EN_MANTENIMIENTO;
            default -> throw new IllegalArgumentException("Valor inválido para EstadoCamion: " + value);
        };
    }
}