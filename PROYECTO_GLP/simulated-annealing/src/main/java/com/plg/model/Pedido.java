
package com.plg.model;

public class Pedido {
    private String clienteId;
    private Coordenada destino;
    private int volumen; // m3
    private int dia, hora, minuto;
    private int horasLimite;

    public Pedido(String clienteId, Coordenada destino, int volumen, int dia, int hora, int minuto, int horasLimite) {
        this.clienteId = clienteId;
        this.destino = destino;
        this.volumen = volumen;
        this.dia = dia;
        this.hora = hora;
        this.minuto = minuto;
        this.horasLimite = horasLimite;
    }

    public Coordenada getDestino() { return destino; }
    public int getVolumen() { return volumen; }
    public int getHoraTotal() { return dia * 24 * 60 + hora * 60 + minuto; }
    public int getPlazoLimiteEnMinutos() { return horasLimite * 60; }

    @Override
    public String toString() {
        return clienteId + " - " + destino + " (" + volumen + "m3)";
    }
}
