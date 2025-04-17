package com.plg.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Cliente {
    @Id
    private String id; // código único del cliente
    
    private String nombre;
    private String direccion;
    private String telefono;
    private String email;
    private double posX;
    private double posY;
    
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    @JsonManagedReference(value="cliente-pedido")
    private List<Pedido> pedidos;
}