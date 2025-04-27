package com.plg.model;

import java.time.LocalDateTime;

public class Pedido {
    private Coordenada coordenada;
    private String idCliente;
    private int cantidadM3;
    private int horasLimite;
    private LocalDateTime fechaRegistro;

    public Pedido(Coordenada coordenada, String idCliente, int cantidadM3, int horasLimite,
            LocalDateTime fechaRegistro) {
        this.coordenada = coordenada;
        this.idCliente = idCliente;
        this.cantidadM3 = cantidadM3;
        this.horasLimite = horasLimite;
        this.fechaRegistro = fechaRegistro;
    }

    public Pedido(String line, LocalDateTime fechaInicialSimulacion) {
        // Ejemplo de línea: 11d13h31m:25,40,c-CLI001,15m3,10h
        String[] partes = line.split(":");
        if (partes.length == 2) {
            String tiempo = partes[0]; // "11d13h31m"
            String datos = partes[1]; // "25,40,c-CLI001,15m3,10h"

            // Procesar tiempo
            int dia = Integer.parseInt(tiempo.substring(0, 2));
            int hora = Integer.parseInt(tiempo.substring(3, 5));
            int minuto = Integer.parseInt(tiempo.substring(6, 8));
            this.fechaRegistro = fechaInicialSimulacion.plusDays(dia - 1).plusHours(hora).plusMinutes(minuto);

            // Procesar datos
            String[] datosSeparados = datos.split(",");
            this.coordenada = new Coordenada(Integer.parseInt(datosSeparados[0]), Integer.parseInt(datosSeparados[1]));
            this.idCliente = datosSeparados[2];
            this.cantidadM3 = Integer.parseInt(datosSeparados[3].replace("m3", ""));
            this.horasLimite = Integer.parseInt(datosSeparados[4].replace("h", ""));
        } else {
            throw new IllegalArgumentException("Formato inválido en pedido: " + line);
        }
    }

    public Coordenada getCoordenada() {
        return coordenada;
    }

    public String getIdCliente() {
        return idCliente;
    }

    public int getCantidadM3() {
        return cantidadM3;
    }

    public int getHorasLimite() {
        return horasLimite;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public LocalDateTime getFechaLimiteEntrega() {
        return fechaRegistro.plusHours(horasLimite);
    }

    @Override
    public String toString() {
        return "Pedido{" +
                "coordenada=" + coordenada +
                ", idente='" + idCliente + '\'' +
                ", cantidadM3=" + cantidadM3 +
                ", horasLimite=" + horasLimite +
                ", fechaRegistro=" + fechaRegistro +
                '}';
    }
}