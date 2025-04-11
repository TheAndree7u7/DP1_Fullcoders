package com.plg.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {
    @Id
    private String id; // código único del cliente
    
    private String nombre;
    private String direccion;
    private String telefono;
    private String email;
    private int posX;
    private int posY;
    
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    private List<Pedido> pedidos;
}