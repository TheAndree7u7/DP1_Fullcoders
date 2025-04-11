package com.plg.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Camion {
    @Id
    private String codigo;
    
    private String tipo; // TA, TB, TC, TD, etc.
    private double capacidad; // Capacidad en m3
    private double tara; // Peso del camión vacío en toneladas
    private double pesoCarga; // Peso actual de la carga en toneladas
    private double pesoCombinado; // Peso total (tara + carga)
    private int estado; // 0: disponible, 1: en ruta, 2: en mantenimiento, 3: averiado
    
    // Atributos relacionados con combustible
    private double capacidadTanque = 25.0; // Capacidad del tanque en galones
    private double combustibleActual; // Combustible actual en galones
    private double velocidadPromedio = 50.0; // Velocidad promedio en km/h
    
    // Posición actual del camión (para calcular distancia a recorrer)
    private int posX;
    private int posY;
    
    @OneToMany(mappedBy = "camion", cascade = CascadeType.ALL)
    @JsonManagedReference(value="camion-mantenimiento")
    private List<Mantenimiento> mantenimientos;
    
    @OneToMany(mappedBy = "camion", cascade = CascadeType.ALL)
    @JsonManagedReference(value="camion-averia")
    private List<Averia> averias;
    
    @OneToMany(mappedBy = "camion", cascade = CascadeType.ALL)
    @JsonManagedReference(value="camion-pedido")
    private List<Pedido> pedidos;
    
    // Método para calcular consumo de combustible
    public double calcularConsumoCombustible(double distanciaKm) {
        // Consumo (Gal) = Distancia (km) × Peso combinado (Ton) / 180
        return distanciaKm * pesoCombinado / 180.0;
    }
    
    // Método para calcular la distancia máxima que puede recorrer con el combustible actual
    public double calcularDistanciaMaxima() {
        // Dist Max = Combustible (Gal) * 180 / Peso combinado (Ton)
        return combustibleActual * 180.0 / pesoCombinado;
    }
}