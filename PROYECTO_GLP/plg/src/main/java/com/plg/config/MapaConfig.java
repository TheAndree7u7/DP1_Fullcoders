package com.plg.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Configuración del mapa para la aplicación
 * Contiene las dimensiones y características del mapa reticular
 */
@Getter
@Setter
@Component
@Configuration
@NoArgsConstructor
public class MapaConfig {
    
    // Dimensiones del mapa
    @Value("${mapa.largo:70}")
    private double largo; // Largo del mapa en km (eje X)
    
    @Value("${mapa.ancho:60}")
    private double ancho; // Ancho del mapa en km (eje Y)
    
    // Origen del mapa
    @Value("${mapa.origen.x:0}")
    private double origenX; // Coordenada X del origen
    
    @Value("${mapa.origen.y:0}")
    private double origenY; // Coordenada Y del origen
    
    // Distancia entre nodos
    @Value("${mapa.distancia.nodos:1}")
    private double distanciaNodos; // Distancia entre nodos en km
    
 
  
    
    /**
     * Constructor con parámetros
     */
    public MapaConfig(int largo, int ancho, int origenX, int origenY, double distanciaNodos) {
        this.largo = largo;
        this.ancho = ancho;
        this.origenX = origenX;
        this.origenY = origenY;
        this.distanciaNodos = distanciaNodos;
    }
    
    /**
     * Verifica si unas coordenadas están dentro de los límites del mapa
     */
    public boolean estaEnMapa(double x, double y) {
        return x >= origenX && x <= origenX + largo &&
               y >= origenY && y <= origenY + ancho;
    }
    
    /**
     * Calcula la distancia entre dos puntos en el mapa reticular (Manhattan)
     * En un mapa reticular, sólo se puede mover horizontal y verticalmente
     */
    public double calcularDistanciaReticular(double x1, double y1, double x2, double y2) {
        // Distancia Manhattan = |x1 - x2| + |y1 - y2|
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }
    
    /**
     * Calcula la distancia en km entre dos puntos en coordenadas
     */
    public double calcularDistanciaRealKm(double x1, double y1, double x2, double y2) {
        // La distancia real es la distancia Manhattan multiplicada por la distancia entre nodos
        return calcularDistanciaReticular(x1, y1, x2, y2) * distanciaNodos;
    }
    
    /**
     * Obtiene los nodos adyacentes a un nodo dado (hasta 4: arriba, abajo, izquierda, derecha)
     */
    public double[][] obtenerNodosAdyacentes(double x, double y) {
        // Posibles movimientos: arriba, derecha, abajo, izquierda
        double[][] movimientos = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
        
        // Inicializar lista para almacenar nodos válidos
        java.util.List<double[]> nodosAdyacentes = new java.util.ArrayList<>();
        
        // Verificar cada posible movimiento
        for (double[] mov : movimientos) {
            double newX = x + mov[0];
            double newY = y + mov[1];
            
            // Verificar si el nuevo nodo está dentro del mapa
            if (estaEnMapa(newX, newY)) {
                nodosAdyacentes.add(new double[]{newX, newY});
            }
        }
        
        // Convertir lista a array 2D
        return nodosAdyacentes.toArray(new double[0][]);
    } 
}