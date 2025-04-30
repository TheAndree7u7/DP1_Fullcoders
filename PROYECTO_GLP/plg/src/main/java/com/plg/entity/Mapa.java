package com.plg.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

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

        // Initialize the list for every node
        for (int n = 0; n < totalNodos; n++) {
            this.adj.put(n, new ArrayList<>());
        }

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
        for (int i = 0; i < this.filas; i++) {
            for (int j = 0; j < this.columnas; j++) {
                int nodo = i * columnas + j;
                Nodo nodoActual = getNodo(nodo);
                if (nodoActual.isBloqueado()) {
                    System.out.print("X ");
                } else {
                    System.out.print("O ");
                }
            }
            System.out.println();
        }

    }

    public int getValorNumerico(Coordenada coordenada) {
        return coordenada.getX() * columnas + coordenada.getY();
    }

    public Nodo getNodo(int i) {
        return adj.get(i).get(0);
    }

    public List<Coordenada> aStar(Coordenada inicio, Coordenada destino) {
        int startIndex = getValorNumerico(inicio);
        int goalIndex = getValorNumerico(destino);
        Nodo startNodo = getNodo(startIndex);

        // Maps for the scores and the path reconstruction
        Map<Integer, Double> gScore = new HashMap<>();
        Map<Integer, Double> fScore = new HashMap<>();
        Map<Integer, Integer> cameFrom = new HashMap<>();

        gScore.put(startIndex, 0.0);
        fScore.put(startIndex, heuristic(startNodo, destino));

        // Priority queue ordered by fScore of node indices
        PriorityQueue<Integer> openSet = new PriorityQueue<>(
                (a, b) -> Double.compare(fScore.getOrDefault(a, Double.POSITIVE_INFINITY),
                        fScore.getOrDefault(b, Double.POSITIVE_INFINITY)));
        openSet.add(startIndex);

        while (!openSet.isEmpty()) {
            int currentIndex = openSet.poll();
            Nodo currentNodo = getNodo(currentIndex);

            // Check if we reached the destination
            if (currentNodo.getPosX() == destino.getX() && currentNodo.getPosY() == destino.getY()) {
                return reconstructPath(cameFrom, currentIndex);
            }

            // Explore neighbors
            List<Nodo> neighbors = adj.get(currentIndex);
            if (neighbors == null)
                continue;
            for (Nodo neighbor : neighbors) {
                int neighborIndex = neighbor.getIndice();
                double tentativeG = gScore.get(currentIndex) + 1; // each move costs 1
                if (tentativeG < gScore.getOrDefault(neighborIndex, Double.POSITIVE_INFINITY)) {
                    cameFrom.put(neighborIndex, currentIndex);
                    gScore.put(neighborIndex, tentativeG);
                    double tentativeF = tentativeG + heuristic(neighbor, destino);
                    fScore.put(neighborIndex, tentativeF);
                    if (!openSet.contains(neighborIndex)) {
                        openSet.add(neighborIndex);
                    }
                }
            }
        }
        // If no path was found, return an empty list.
        return new ArrayList<>();
    }

    private double heuristic(Nodo nodo, Coordenada destino) {
        return Math.abs(nodo.getPosX() - destino.getX()) + Math.abs(nodo.getPosY() - destino.getY());
    }

    private List<Coordenada> reconstructPath(Map<Integer, Integer> cameFrom, int currentIndex) {
        List<Coordenada> totalPath = new ArrayList<>();
        totalPath.add(new Coordenada(getNodo(currentIndex).getPosX(), getNodo(currentIndex).getPosY()));
        while (cameFrom.containsKey(currentIndex)) {
            currentIndex = cameFrom.get(currentIndex);
            totalPath.add(new Coordenada(getNodo(currentIndex).getPosX(), getNodo(currentIndex).getPosY()));
        }
        Collections.reverse(totalPath);
        return totalPath;
    }

}
