package com.plg.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * Configuración del mapa para la aplicación
 * Contiene las dimensiones y características del mapa reticular
 */
@Component
@Configuration
public class MapaConfig {
    
    // Dimensiones del mapa
    @Value("${mapa.largo:70}")
    private int largo; // Largo del mapa en km (eje X)
    
    @Value("${mapa.ancho:50}")
    private int ancho; // Ancho del mapa en km (eje Y)
    
    // Origen del mapa
    @Value("${mapa.origen.x:0}")
    private int origenX; // Coordenada X del origen
    
    @Value("${mapa.origen.y:0}")
    private int origenY; // Coordenada Y del origen
    
    // Distancia entre nodos
    @Value("${mapa.distancia.nodos:1}")
    private double distanciaNodos; // Distancia entre nodos en km
    
    // Posición del almacén central
    @Value("${mapa.almacen.central.x:12}")
    private int almacenCentralX;
    
    @Value("${mapa.almacen.central.y:8}")
    private int almacenCentralY;
    
    /**
     * Constructor por defecto
     */
    public MapaConfig() {
        // Valores por defecto definidos arriba con @Value
    }
    
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
    public boolean estaEnMapa(int x, int y) {
        return x >= origenX && x <= origenX + largo &&
               y >= origenY && y <= origenY + ancho;
    }
    
    /**
     * Calcula la distancia entre dos puntos en el mapa reticular (Manhattan)
     * En un mapa reticular, sólo se puede mover horizontal y verticalmente
     */
    public double calcularDistanciaReticular(int x1, int y1, int x2, int y2) {
        // Distancia Manhattan = |x1 - x2| + |y1 - y2|
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }
    
    /**
     * Calcula la distancia en km entre dos puntos en coordenadas
     */
    public double calcularDistanciaRealKm(int x1, int y1, int x2, int y2) {
        // La distancia real es la distancia Manhattan multiplicada por la distancia entre nodos
        return calcularDistanciaReticular(x1, y1, x2, y2) * distanciaNodos;
    }
    
    /**
     * Obtiene los nodos adyacentes a un nodo dado (hasta 4: arriba, abajo, izquierda, derecha)
     */
    public int[][] obtenerNodosAdyacentes(int x, int y) {
        // Posibles movimientos: arriba, derecha, abajo, izquierda
        int[][] movimientos = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
        
        // Inicializar lista para almacenar nodos válidos
        java.util.List<int[]> nodosAdyacentes = new java.util.ArrayList<>();
        
        // Verificar cada posible movimiento
        for (int[] mov : movimientos) {
            int newX = x + mov[0];
            int newY = y + mov[1];
            
            // Verificar si el nuevo nodo está dentro del mapa
            if (estaEnMapa(newX, newY)) {
                nodosAdyacentes.add(new int[]{newX, newY});
            }
        }
        
        // Convertir lista a array 2D
        return nodosAdyacentes.toArray(new int[0][]);
    }
    
    // Getters y setters
    
    public int getLargo() {
        return largo;
    }
    
    public void setLargo(int largo) {
        this.largo = largo;
    }
    
    public int getAncho() {
        return ancho;
    }
    
    public void setAncho(int ancho) {
        this.ancho = ancho;
    }
    
    public int getOrigenX() {
        return origenX;
    }
    
    public void setOrigenX(int origenX) {
        this.origenX = origenX;
    }
    
    public int getOrigenY() {
        return origenY;
    }
    
    public void setOrigenY(int origenY) {
        this.origenY = origenY;
    }
    
    public double getDistanciaNodos() {
        return distanciaNodos;
    }
    
    public void setDistanciaNodos(double distanciaNodos) {
        this.distanciaNodos = distanciaNodos;
    }
    
    public int getAlmacenCentralX() {
        return almacenCentralX;
    }
    
    public void setAlmacenCentralX(int almacenCentralX) {
        this.almacenCentralX = almacenCentralX;
    }
    
    public int getAlmacenCentralY() {
        return almacenCentralY;
    }
    
    public void setAlmacenCentralY(int almacenCentralY) {
        this.almacenCentralY = almacenCentralY;
    }
}