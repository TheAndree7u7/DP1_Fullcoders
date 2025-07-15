package com.plg.dto;

/**
 * DTO para representar el resultado de la recarga de datos del sistema.
 * Implementa el principio de encapsulación al agrupar datos relacionados
 * y proporcionar una interfaz clara para el frontend.
 */
public class DataReloadDto {

    private final int cantidadAlmacenes;
    private final int cantidadCamiones;
    private final int cantidadPedidos;
    private final int cantidadAverias;
    private final int cantidadMantenimientos;
    private final int cantidadBloqueos;
    private final String mensaje;
    private final boolean exito;

    public DataReloadDto(int cantidadAlmacenes, int cantidadCamiones, int cantidadPedidos,
            int cantidadAverias, int cantidadMantenimientos, int cantidadBloqueos,
            String mensaje, boolean exito) {
        this.cantidadAlmacenes = cantidadAlmacenes;
        this.cantidadCamiones = cantidadCamiones;
        this.cantidadPedidos = cantidadPedidos;
        this.cantidadAverias = cantidadAverias;
        this.cantidadMantenimientos = cantidadMantenimientos;
        this.cantidadBloqueos = cantidadBloqueos;
        this.mensaje = mensaje;
        this.exito = exito;
    }

    // Constructor para caso de éxito
    public static DataReloadDto crearExitoso(int cantidadAlmacenes, int cantidadCamiones,
            int cantidadPedidos, int cantidadAverias,
            int cantidadMantenimientos, int cantidadBloqueos) {
        return new DataReloadDto(
                cantidadAlmacenes, cantidadCamiones, cantidadPedidos,
                cantidadAverias, cantidadMantenimientos, cantidadBloqueos,
                "Recarga de datos completada exitosamente", true);
    }

    // Constructor para caso de error
    public static DataReloadDto crearError(String mensajeError) {
        return new DataReloadDto(0, 0, 0, 0, 0, 0, mensajeError, false);
    }

    // Getters
    public int getCantidadAlmacenes() {
        return cantidadAlmacenes;
    }

    public int getCantidadCamiones() {
        return cantidadCamiones;
    }

    public int getCantidadPedidos() {
        return cantidadPedidos;
    }

    public int getCantidadAverias() {
        return cantidadAverias;
    }

    public int getCantidadMantenimientos() {
        return cantidadMantenimientos;
    }

    public int getCantidadBloqueos() {
        return cantidadBloqueos;
    }

    public String getMensaje() {
        return mensaje;
    }

    public boolean isExito() {
        return exito;
    }

    @Override
    public String toString() {
        return String.format(
                "DataReloadDto{exito=%s, mensaje='%s', almacenes=%d, camiones=%d, pedidos=%d, averias=%d, mantenimientos=%d, bloqueos=%d}",
                exito, mensaje, cantidadAlmacenes, cantidadCamiones, cantidadPedidos,
                cantidadAverias, cantidadMantenimientos, cantidadBloqueos);
    }
}