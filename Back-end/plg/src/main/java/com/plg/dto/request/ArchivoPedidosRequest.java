package com.plg.dto.request;

import java.util.List;

import com.plg.dto.DatosVentas;

/**
 * DTO para la solicitud de archivos de pedidos.
 */
public class ArchivoPedidosRequest {

    private String nombre;
    private String contenido;
    private List<DatosVentas> datos;

    // Constructores
    public ArchivoPedidosRequest() {
    }

    public ArchivoPedidosRequest(String nombre, String contenido, List<DatosVentas> datos) {
        this.nombre = nombre;
        this.contenido = contenido;
        this.datos = datos;
    }

    // Getters y Setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public List<DatosVentas> getDatos() {
        return datos;
    }

    public void setDatos(List<DatosVentas> datos) {
        this.datos = datos;
    }

    @Override
    public String toString() {
        return "ArchivoPedidosRequest{" +
                "nombre='" + nombre + '\'' +
                ", contenido='" + contenido + '\'' +
                ", datos=" + datos +
                '}';
    }
}