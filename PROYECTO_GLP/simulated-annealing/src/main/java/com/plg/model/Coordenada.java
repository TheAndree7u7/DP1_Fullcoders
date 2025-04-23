package com.plg.model;

public class Coordenada {
    private int x;
    private int y;

    public Coordenada(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Coordenada)) return false;
        Coordenada other = (Coordenada) obj;
        return this.x == other.x && this.y == other.y;
    }

    @Override
    public int hashCode() {
        return x * 31 + y;
    }
}
