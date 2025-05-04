package com.plg.utils;

import com.plg.entity.Almacen;
import com.plg.entity.Camion;
import com.plg.entity.Mapa;
import com.plg.entity.Nodo;
import com.plg.entity.Pedido;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Gen {
    private Camion camion;
    private List<Nodo> nodos;
    private List<Nodo> ruta_final;

    private double calcularFitness() {
        double fitness = 0.0;
        for(int i=0; i<nodos.size()-1; i++){
            Nodo nodo1 = nodos.get(i);
            Nodo nodo2 = nodos.get(i+1);
            List<Nodo> ruta = new ArrayList<>();
            if (nodo2 instanceof Pedido) {
                Pedido pedido = (Pedido) nodo2;
                ruta = Mapa.getInstance().aStar(nodo2, pedido);
                double tiempoEntrega = ruta.size() / camion.getVelocidadPromedio();
                double combustibleUsado = camion.getConsumoCombustible() * tiempoEntrega;
                if(tiempoEntrega <= pedido.getHorasLimite()){
                    fitness += pedido.getHorasLimite() - tiempoEntrega; 
                } else {
                    fitness -= Double.MAX_VALUE;
                }
            } else if (nodo2 instanceof Almacen) {
                // Si es un almacén, no se suma nada al fitness
            } else {
                // Si es un camión averiado, se penaliza el fitness
                fitness -= 10.0; // Penalización por camión averiado
            }
        }
        return fitness;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(camion.toString()).append(" ");
        for (Nodo nodo : nodos) {
            sb.append(nodo.toString()).append(" ");
        }
        return sb.toString();
    }
}