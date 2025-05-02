package com.plg.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private double posX;
    private double posY;
    
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    @JsonManagedReference(value="cliente-pedido")
    private List<Pedido> pedidos;
}