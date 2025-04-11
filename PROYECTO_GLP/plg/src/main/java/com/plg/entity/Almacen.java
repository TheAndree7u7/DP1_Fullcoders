package com.plg.entity;

import jakarta.persistence.*;
import lombok.*;

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
    private double capacidad; // Capacidad total en m3
    private double capacidadActual; // Capacidad actual ocupada en m3
    private boolean activo; // Estado del almacén (activo/inactivo)
}