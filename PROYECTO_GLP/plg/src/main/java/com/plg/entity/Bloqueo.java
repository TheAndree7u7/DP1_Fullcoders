package com.plg.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bloqueo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Ya no usamos posXInicio, posYInicio, posXFin, posYFin, sino una lista de coordenadas
    @ElementCollection
    @CollectionTable(name = "bloqueo_coordenadas", joinColumns = @JoinColumn(name = "bloqueo_id"))
    private List<Coordenada> coordenadas = new ArrayList<>();
    
    // Cambiamos de LocalDate a LocalDateTime para manejar horas y minutos
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String descripcion;
    private boolean activo;
    
    /**
     * Método para determinar si un punto está en un tramo bloqueado
     * @param x Coordenada X a verificar
     * @param y Coordenada Y a verificar
     * @return true si el punto está en un tramo bloqueado
     */
    public boolean contienePunto(int x, int y) {
        // Si hay menos de 2 coordenadas, no hay tramos
        if (coordenadas.size() < 2) {
            return false;
        }
        
        // Verificar cada tramo (par de coordenadas consecutivas)
        for (int i = 0; i < coordenadas.size() - 1; i++) {
            Coordenada inicio = coordenadas.get(i);
            Coordenada fin = coordenadas.get(i + 1);
            
            // Verificar si el punto está en la línea entre inicio y fin
            if (estaPuntoEnLinea(x, y, inicio.getX(), inicio.getY(), fin.getX(), fin.getY())) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Verifica si un punto está en una línea, con una pequeña tolerancia
     */
    private boolean estaPuntoEnLinea(int x, int y, int x1, int y1, int x2, int y2) {
        // Calculamos la distancia del punto a la línea
        double distanciaALinea = distanciaPuntoALinea(x, y, x1, y1, x2, y2);
        
        // Tolerancia para considerar que está en la línea (por ejemplo, 0.5 unidades)
        double tolerancia = 0.5;
        
        // Verificar si la distancia es menor a la tolerancia y el punto está dentro del segmento
        return distanciaALinea < tolerancia && estaPuntoEnSegmento(x, y, x1, y1, x2, y2);
    }
    
    /**
     * Calcula la distancia de un punto a una línea
     */
    private double distanciaPuntoALinea(int x, int y, int x1, int y1, int x2, int y2) {
        // Si los puntos de la línea son iguales, la distancia es simplemente la distancia al punto
        if (x1 == x2 && y1 == y2) {
            return Math.sqrt(Math.pow(x - x1, 2) + Math.pow(y - y1, 2));
        }
        
        // Cálculo de la distancia de un punto a una línea
        return Math.abs((y2 - y1) * x - (x2 - x1) * y + x2 * y1 - y2 * x1) / 
               Math.sqrt(Math.pow(y2 - y1, 2) + Math.pow(x2 - x1, 2));
    }
    
    /**
     * Verifica si un punto está dentro del segmento de línea
     */
    private boolean estaPuntoEnSegmento(int x, int y, int x1, int y1, int x2, int y2) {
        // Calculamos el rango de coordenadas del segmento
        int minX = Math.min(x1, x2);
        int maxX = Math.max(x1, x2);
        int minY = Math.min(y1, y2);
        int maxY = Math.max(y1, y2);
        
        // Verificamos si el punto está dentro del rango del segmento
        return x >= minX && x <= maxX && y >= minY && y <= maxY;
    }
    
    /**
     * Convierte el bloqueo a formato de registro para archivo
     */
    public String convertirARegistro() {
        // Formato: ##d##h##m-##d##h##m:x1,y1,x2,y2,...,xn,yn
        StringBuilder registro = new StringBuilder();
        
        // Formatear fechas
        registro.append(formatearFecha(fechaInicio));
        registro.append("-");
        registro.append(formatearFecha(fechaFin));
        registro.append(":");
        
        // Añadir coordenadas
        for (int i = 0; i < coordenadas.size(); i++) {
            Coordenada coord = coordenadas.get(i);
            registro.append(coord.getX()).append(",").append(coord.getY());
            
            // Añadir coma si no es la última coordenada
            if (i < coordenadas.size() - 1) {
                registro.append(",");
            }
        }
        
        return registro.toString();
    }
    
    /**
     * Formatea una fecha en el formato ##d##h##m
     */
    private String formatearFecha(LocalDateTime fecha) {
        return String.format("%02dd%02dh%02dm", 
            fecha.getDayOfMonth(), fecha.getHour(), fecha.getMinute());
    }
        /**
     * Verifica si un segmento de ruta intersecta con este bloqueo
     * @param x1 Coordenada X del punto inicial del segmento
     * @param y1 Coordenada Y del punto inicial del segmento
     * @param x2 Coordenada X del punto final del segmento
     * @param y2 Coordenada Y del punto final del segmento
     * @return true si el segmento intersecta con algún tramo bloqueado
     */
    public boolean intersectaConSegmento(int x1, int y1, int x2, int y2) {
        // Si hay menos de 2 coordenadas, no hay tramos
        if (coordenadas.size() < 2) {
            return false;
        }
        
        // Verificar cada tramo del bloqueo (par de coordenadas consecutivas)
        for (int i = 0; i < coordenadas.size() - 1; i++) {
            Coordenada inicioBloqueo = coordenadas.get(i);
            Coordenada finBloqueo = coordenadas.get(i + 1);
            
            // Verificar si los segmentos se intersecan
            if (seIntersecaConSegmento(
                    x1, y1, x2, y2,
                    inicioBloqueo.getX(), inicioBloqueo.getY(),
                    finBloqueo.getX(), finBloqueo.getY())) {
                return true;
            }
        }
        
        // Verificar también si algún extremo del segmento está dentro del bloqueo
        if (contienePunto(x1, y1) || contienePunto(x2, y2)) {
            return true;
        }
        
        return false;
    }

    /**
     * Verifica si dos segmentos de línea se intersecan
     */
    private boolean seIntersecaConSegmento(
            int x1, int y1, int x2, int y2,
            int x3, int y3, int x4, int y4) {
        
 
        
        // Primero calculamos los denominadores para las ecuaciones
        int denominator = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
        
        // Si el denominador es 0, las líneas son paralelas
        if (denominator == 0) {
            // Verificamos si un punto del segmento 1 está en el segmento 2 (colineales)
            return estaPuntoEnSegmento(x1, y1, x3, y3, x4, y4) || 
                estaPuntoEnSegmento(x2, y2, x3, y3, x4, y4) ||
                estaPuntoEnSegmento(x3, y3, x1, y1, x2, y2) || 
                estaPuntoEnSegmento(x4, y4, x1, y1, x2, y2);
        }
        
        // Calculamos los valores de t y u
        double t = ((x3 - x1) * (y4 - y3) - (y3 - y1) * (x4 - x3)) / (double) denominator;
        double u = ((x3 - x1) * (y2 - y1) - (y3 - y1) * (x2 - x1)) / (double) denominator;
        
        // Si t y u están entre 0 y 1, los segmentos se intersecan
        return t >= 0 && t <= 1 && u >= 0 && u <= 1;
    }
    // Clase interna para representar una coordenada
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Coordenada {
        private int x;
        private int y;
    }
    
}