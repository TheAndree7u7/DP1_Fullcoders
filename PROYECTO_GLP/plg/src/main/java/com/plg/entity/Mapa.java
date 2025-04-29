package com.plg.entity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Mapa {

    private int columnas;
    private int filas;

    private Map<Integer, List<Nodo>> adj;

    // Construye el mapa como un grafo
    public Mapa(int columnas, int filas) {
        int totalNodos = filas * columnas;
        this.columnas = columnas;
        this.filas = filas;
        this.adj = new HashMap<>(totalNodos);


        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                int nodo = i * columnas + j;
                // Inicializa la lista de adyacencia para cada nodo
                Nodo nodoActual = Nodo.builder().indice(nodo).posX(i).posY(j).build();
                if (j < columnas - 1) {
                    int vecinoDerecha = i * columnas + (j + 1);
                    Nodo nodoDerecha = Nodo.builder().indice(vecinoDerecha).posX(i).posY(j + 1).build();
                    // El nodo actual será el primer nodo de la lista de adyacencia
                    this.adj.get(nodo).add(nodoActual);
                    this.adj.get(nodo).add(nodoDerecha);
                    this.adj.get(vecinoDerecha).add(nodoActual);
                }
                if (i < filas - 1) {
                    int vecinoAbajo = (i + 1) * columnas + j;
                    Nodo nodoAbajo = Nodo.builder().indice(vecinoAbajo).posX(i + 1).posY(j).build();
                    this.adj.get(nodo).add(nodoAbajo);
                    this.adj.get(vecinoAbajo).add(nodoActual);
                }
            }
        }
    }

    public List<Nodo> getAdj(int i) {
        return adj.get(i);
    }
    
    public void imprimirMapa() {
        for (int i = 0; i < adj.size(); i++) {
            System.out.println("Nodo " + i + " conectado a " + adj.get(i));
        }
    }
    public int getValorNumerico(Coordenada coordenada) {
        return coordenada.getX() * columnas + coordenada.getY();
    }

    public Nodo getNodo(int i) {
        return adj.get(i).get(0);
    }

}

