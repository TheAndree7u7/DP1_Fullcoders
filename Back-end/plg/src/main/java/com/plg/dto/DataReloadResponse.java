package com.plg.dto;

public class DataReloadResponse {
    private boolean exito;
    private String mensaje;
    private int pedidosCargados;
    private int camionesCargados;
    private int almacenesCargados;
    private int averiasCargadas;
    private int mantenimientosCargados;
    private int bloqueosCargados;

    public DataReloadResponse() {
    }

    public DataReloadResponse(boolean exito, String mensaje) {
        this.exito = exito;
        this.mensaje = mensaje;
    }

    public DataReloadResponse(boolean exito, String mensaje, int pedidosCargados, int camionesCargados,
            int almacenesCargados, int averiasCargadas, int mantenimientosCargados, int bloqueosCargados) {
        this.exito = exito;
        this.mensaje = mensaje;
        this.pedidosCargados = pedidosCargados;
        this.camionesCargados = camionesCargados;
        this.almacenesCargados = almacenesCargados;
        this.averiasCargadas = averiasCargadas;
        this.mantenimientosCargados = mantenimientosCargados;
        this.bloqueosCargados = bloqueosCargados;
    }

    // Getters y Setters
    public boolean isExito() {
        return exito;
    }

    public void setExito(boolean exito) {
        this.exito = exito;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public int getPedidosCargados() {
        return pedidosCargados;
    }

    public void setPedidosCargados(int pedidosCargados) {
        this.pedidosCargados = pedidosCargados;
    }

    public int getCamionesCargados() {
        return camionesCargados;
    }

    public void setCamionesCargados(int camionesCargados) {
        this.camionesCargados = camionesCargados;
    }

    public int getAlmacenesCargados() {
        return almacenesCargados;
    }

    public void setAlmacenesCargados(int almacenesCargados) {
        this.almacenesCargados = almacenesCargados;
    }

    public int getAveriasCargadas() {
        return averiasCargadas;
    }

    public void setAveriasCargadas(int averiasCargadas) {
        this.averiasCargadas = averiasCargadas;
    }

    public int getMantenimientosCargados() {
        return mantenimientosCargados;
    }

    public void setMantenimientosCargados(int mantenimientosCargados) {
        this.mantenimientosCargados = mantenimientosCargados;
    }

    public int getBloqueosCargados() {
        return bloqueosCargados;
    }

    public void setBloqueosCargados(int bloqueosCargados) {
        this.bloqueosCargados = bloqueosCargados;
    }
}