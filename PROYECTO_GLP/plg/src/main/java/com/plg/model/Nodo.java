package com.plg.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Nodo {
    private Coordenada coordenada;
    private LinkedList<Nodo> vecinos;
    private boolean bloqueado;

    public Nodo(Coordenada coordenada) {
        this.coordenada = coordenada;
        this.vecinos = new LinkedList<>();
        this.bloqueado = false;
    }

    public Coordenada getCoordenada() {
        return coordenada;
    }

    public void addVecino(Nodo nodo) {
        vecinos.add(nodo);
    }

    public List<Nodo> getVecinos() {
        return vecinos;
    }

    public boolean isBloqueado() {
        return bloqueado;
    }

    public void setBloqueado(boolean bloqueado) {
        this.bloqueado = bloqueado;
    }
}
