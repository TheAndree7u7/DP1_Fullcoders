package com.plg.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bloqueo {

    private Long id;
    private List<Coordenada> coordenadas = new ArrayList<>();
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String descripcion;
    private boolean activo;

    /**
     * Determina si un punto está en un tramo bloqueado
     */
    public boolean contienePunto(double x, double y) {
        if (coordenadas.size() < 2) {
            return false;
        }
        for (int i = 0; i < coordenadas.size() - 1; i++) {
            Coordenada inicio = coordenadas.get(i);
            Coordenada fin    = coordenadas.get(i + 1);
            if (estaPuntoEnSegmento(x, y,
                    inicio.getX(), inicio.getY(),
                    fin.getX(), fin.getY())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica si un segmento de ruta intersecta con este bloqueo
     */
    public boolean intersectaConSegmento(double x1, double y1, double x2, double y2) {
        if (coordenadas.size() < 2) {
            return false;
        }
        for (int i = 0; i < coordenadas.size() - 1; i++) {
            Coordenada b0 = coordenadas.get(i);
            Coordenada b1 = coordenadas.get(i + 1);
            if (seIntersecaConSegmento(
                    x1, y1, x2, y2,
                    b0.getX(), b0.getY(),
                    b1.getX(), b1.getY())) {
                return true;
            }
        }
        // También verifica si algún extremo está dentro
        return contienePunto(x1, y1) || contienePunto(x2, y2);
    }

    /**
     * Convierte el bloqueo a registro de texto
     * Formato: ##d##h##m-##d##h##m:x1,y1,x2,y2,...,xn,yn
     */
    public String convertirARegistro() {
        StringBuilder sb = new StringBuilder();
        sb.append(formatearFecha(fechaInicio))
          .append("-")
          .append(formatearFecha(fechaFin))
          .append(":");
        for (int i = 0; i < coordenadas.size(); i++) {
            Coordenada c = coordenadas.get(i);
            sb.append(c.getX()).append(",").append(c.getY());
            if (i < coordenadas.size() - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    // --- Métodos auxiliares privados ---

    private String formatearFecha(LocalDateTime fecha) {
        return String.format("%02dd%02dh%02dm",
                fecha.getDayOfMonth(),
                fecha.getHour(),
                fecha.getMinute());
    }

    private boolean estaPuntoEnSegmento(
            double px, double py,
            double x1, double y1,
            double x2, double y2) {
        double minX = Math.min(x1, x2), maxX = Math.max(x1, x2);
        double minY = Math.min(y1, y2), maxY = Math.max(y1, y2);
        return px >= minX && px <= maxX && py >= minY && py <= maxY;
    }

    private boolean seIntersecaConSegmento(
            double x1, double y1, double x2, double y2,
            double x3, double y3, double x4, double y4) {

        double denom = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
        if (denom == 0) {
            // Líneas paralelas o colineales: comprueba puntos en segmento
            return estaPuntoEnSegmento(x1,y1,x3,y3,x4,y4)
                || estaPuntoEnSegmento(x2,y2,x3,y3,x4,y4)
                || estaPuntoEnSegmento(x3,y3,x1,y1,x2,y2)
                || estaPuntoEnSegmento(x4,y4,x1,y1,x2,y2);
        }
        double t = ((x3 - x1)*(y4 - y3) - (y3 - y1)*(x4 - x3)) / denom;
        double u = ((x3 - x1)*(y2 - y1) - (y3 - y1)*(x2 - x1)) / denom;
        return t >= 0 && t <= 1 && u >= 0 && u <= 1;
    }

}
