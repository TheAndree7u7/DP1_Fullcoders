package com.plg.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Almacen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nombre;
    private int posX; // Posición X en el mapa
    private int posY; // Posición Y en el mapa
    
    // Capacidades para GLP
    private double capacidadGLP; // Capacidad total de GLP en m3
    private double capacidadActualGLP; // Capacidad actual disponible de GLP en m3
    private double capacidadMaximaGLP; // Capacidad máxima para restaurar en reabastecimiento
    
    // Capacidades para combustible (gasolina/petróleo)
    private double capacidadCombustible; // Capacidad total de combustible en galones
    private double capacidadActualCombustible; // Capacidad actual disponible de combustible en galones
    private double capacidadMaximaCombustible; // Capacidad máxima para restaurar en reabastecimiento
    
    private boolean esCentral; // Indica si es el almacén central (true) o intermedio (false)
    private boolean permiteCamionesEstacionados; // Solo el central permite esto por defecto
    
    // Hora de reabastecimiento para almacenes intermedios
    private LocalTime horaReabastecimiento = LocalTime.MIDNIGHT; // Por defecto a las 00:00
    private boolean ultimoReabastecimientoRealizado = false; // Indica si ya se realizó el reabastecimiento hoy
    
    private boolean activo; // Estado del almacén (activo/inactivo)
    
    // Método para verificar si el almacén puede recargar combustible
    public boolean puedeRecargarCombustible(double cantidadRequerida) {
        return capacidadActualCombustible >= cantidadRequerida && activo;
    }
    
    // Método para verificar si el almacén puede suplir GLP
    public boolean puedeProveerGLP(double cantidadRequerida) {
        return capacidadActualGLP >= cantidadRequerida && activo;
    }
    
    // Método para recargar combustible a un camión
    public boolean recargarCombustible(Camion camion, double cantidad) {
        if (!puedeRecargarCombustible(cantidad)) {
            return false;
        }
        
        // Verificar capacidad disponible en el tanque del camión
        double espacioDisponibleCamion = camion.getCapacidadTanque() - camion.getCombustibleActual();
        double cantidadEfectiva = Math.min(cantidad, Math.min(espacioDisponibleCamion, capacidadActualCombustible));
        
        if (cantidadEfectiva <= 0) {
            return false;
        }
        
        // Realizar la recarga
        this.capacidadActualCombustible -= cantidadEfectiva;
        camion.setCombustibleActual(camion.getCombustibleActual() + cantidadEfectiva);
        return true;
    }
    
    // Método para calcular la distancia desde este almacén hasta una posición
    public double calcularDistancia(int posX2, int posY2) {
        // Distancia Manhattan: suma de las diferencias absolutas en cada dimensión
        return Math.abs(posX - posX2) + Math.abs(posY - posY2);
    }
    
    // Método para reabastecer el almacén
    public void reabastecer() {
        if (!esCentral) {
            // Solo reabastecemos los almacenes intermedios
            this.capacidadActualGLP = this.capacidadMaximaGLP;
            this.capacidadActualCombustible = this.capacidadMaximaCombustible;
            this.ultimoReabastecimientoRealizado = true;
        }
    }
}