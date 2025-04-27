package com.plg.model;

public class Almacen {
    private String nombre;
    private Coordenada ubicacion;

    public Almacen(String nombre, Coordenada ubicacion) {
        this.nombre = nombre;
        this.ubicacion = ubicacion;
    }

    // 🔥 NUEVO: Constructor para crear Almacen desde una línea de texto
    public Almacen(String line) {
        String[] partes = line.split(";");
        if (partes.length >= 3) {
            this.nombre = partes[0];
            int x = Integer.parseInt(partes[1]);
            int y = Integer.parseInt(partes[2]);
            this.ubicacion = new Coordenada(x, y);
        } else {
            throw new IllegalArgumentException("Formato de línea inválido para Almacen: " + line);
        }
    }

    public String getNombre() {
        return nombre;
    }

    public Coordenada getUbicacion() {
        return ubicacion;
    }

    @Override
    public String toString() {
        return "Almacen{" +
                "nombre='" + nombre + '\'' +
                ", ubicacion=" + ubicacion +
                '}';
    }
}
