package com.plg.model;

import java.util.List;

public class Bloqueo {
    private int inicioMinuto;
    private int finMinuto;
    private List<Coordenada> nodos;

    public Bloqueo(int inicioMinuto, int finMinuto, List<Coordenada> nodos) {
        this.inicioMinuto = inicioMinuto;
        this.finMinuto = finMinuto;
        this.nodos = nodos;
    }

    public int getInicioMinuto() { return inicioMinuto; }
    public int getFinMinuto() { return finMinuto; }
    public List<Coordenada> getNodos() { return nodos; }
}
