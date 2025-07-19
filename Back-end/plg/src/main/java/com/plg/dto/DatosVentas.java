package com.plg.dto;

/**
 * DTO para representar los datos de ventas de un pedido.
 */
public class DatosVentas {

    private String fechaHora;
    private int coordenadaX;
    private int coordenadaY;
    private String codigoCliente;
    private int volumenGLP;
    private int horasLimite;

    // Constructores
    public DatosVentas() {
    }

    public DatosVentas(String fechaHora, int coordenadaX, int coordenadaY, String codigoCliente, int volumenGLP,
            int horasLimite) {
        this.fechaHora = fechaHora;
        this.coordenadaX = coordenadaX;
        this.coordenadaY = coordenadaY;
        this.codigoCliente = codigoCliente;
        this.volumenGLP = volumenGLP;
        this.horasLimite = horasLimite;
    }

    // Getters y Setters
    public String getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(String fechaHora) {
        this.fechaHora = fechaHora;
    }

    public int getCoordenadaX() {
        return coordenadaX;
    }

    public void setCoordenadaX(int coordenadaX) {
        this.coordenadaX = coordenadaX;
    }

    public int getCoordenadaY() {
        return coordenadaY;
    }

    public void setCoordenadaY(int coordenadaY) {
        this.coordenadaY = coordenadaY;
    }

    public String getCodigoCliente() {
        return codigoCliente;
    }

    public void setCodigoCliente(String codigoCliente) {
        this.codigoCliente = codigoCliente;
    }

    public int getVolumenGLP() {
        return volumenGLP;
    }

    public void setVolumenGLP(int volumenGLP) {
        this.volumenGLP = volumenGLP;
    }

    public int getHorasLimite() {
        return horasLimite;
    }

    public void setHorasLimite(int horasLimite) {
        this.horasLimite = horasLimite;
    }

    @Override
    public String toString() {
        return "DatosVentas{" +
                "fechaHora='" + fechaHora + '\'' +
                ", coordenadaX=" + coordenadaX +
                ", coordenadaY=" + coordenadaY +
                ", codigoCliente='" + codigoCliente + '\'' +
                ", volumenGLP=" + volumenGLP +
                ", horasLimite=" + horasLimite +
                '}';
    }
}