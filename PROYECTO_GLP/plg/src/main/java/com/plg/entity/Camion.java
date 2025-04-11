package com.plg.entity;

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
    
    @OneToMany(mappedBy = "camion", cascade = CascadeType.ALL)
    private List<Mantenimiento> mantenimientos;
    
    @OneToMany(mappedBy = "camion", cascade = CascadeType.ALL)
    private List<Averia> averias;
    
    @OneToMany(mappedBy = "camion", cascade = CascadeType.ALL)
    private List<Pedido> pedidos;
}