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

    private List<List<Integer>> adj; 

    // Construye el mapa como un grafo
    public Mapa(int columnas, int filas) {
        int totalNodos = filas * columnas;
        this.columnas = columnas;
        this.filas = filas;
        this.adj = new ArrayList<>(totalNodos);

        for (int i = 0; i < totalNodos; i++) {
            this.adj.add(new ArrayList<>());
        }

        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                int nodo = i * columnas + j;
                if (j < columnas - 1) {
                    int vecinoDerecha = i * columnas + (j + 1);
                    this.adj.get(nodo).add(vecinoDerecha);
                    this.adj.get(vecinoDerecha).add(nodo);
                }
                if (i < filas - 1) {
                    int vecinoAbajo = (i + 1) * columnas + j;
                    this.adj.get(nodo).add(vecinoAbajo);
                    this.adj.get(vecinoAbajo).add(nodo);
                }
            }
        }
    }
    public List<Integer> getAdj(Integer i) {
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
}

