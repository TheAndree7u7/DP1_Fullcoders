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

    private List<List<Nodo>> matriz = new ArrayList<>();
    private List<Nodo> list_nodos = new ArrayList<>();

    // Construye el mapa como un grafo
    public Mapa(int columnas, int filas) {
        int totalNodos = filas * columnas;
        this.columnas = columnas;
        this.filas = filas;
        this.matriz = new ArrayList<>();

        for (int i = 0; i < filas; i++) {
            List<Nodo> fila = new ArrayList<>(columnas);
            for (int j = 0; j < columnas; j++) {
                Nodo nodo = Nodo.builder().coordenada(new Coordenada(i, j)).build();
                this.list_nodos.add(nodo);
                fila.add(nodo);
            }
            this.matriz.add(fila);
        }
    }

    public void imprimirMapa() {
        imprimirMapa(null, null);
    }

    public void imprimirMapa(List<List<Coordenada>> rutas, List<Pedido> pedidos) {
        // Imprime cabecera de columnas
        System.out.print("     ");
        for (int j = 0; j < this.columnas; j++) {
            System.out.printf("%4d", j);
        }
        System.out.println();
        
        
        // Imprime cada fila desde la más alta hasta la 0
        for (int i = this.filas - 1; i >= 0; i--) {
            System.out.printf("%4d ", i); // Índice de fila
            for (int j = 0; j < this.columnas; j++) {
                if (i == 12 && j == 12) {
                    System.out.print("  A "); // Nodo especial
                    continue;
                }
                Nodo nodoActual = getNodo(i, j);
                String cell = " . "; // Valor por defecto
                if (nodoActual.isBloqueado()) {
                    cell = " X "; // Nodo bloqueado
                }else  {
                    // Si es un pedido, imprimimos la letra P
                    if (rutas != null){
                        for (int k = 0; k < rutas.size(); k++) {
                            if (rutas.get(k).contains(nodoActual.getCoordenada())) {
                                cell = String.format(" %d ", k);
                                break;
                            }
                        }
                    }

                    if (pedidos != null) {
                        for (int k = 0; k < pedidos.size(); k++) {
                            if (pedidos.get(k).getCoordenada().equals(nodoActual.getCoordenada())) {
                                cell = " P ";
                                break;
                            }
                        }
                    }
                    
                }    
                System.out.printf("%4s", cell);
            }
            System.out.println();
        }
        
        // Imprime la línea de índices de columna al pie
        System.out.print("     ");
        for (int j = 0; j < this.columnas; j++) {
            System.out.printf("%4d", j);
        }
        System.out.println();
    }

    public Nodo getNodo(int fila, int columna) {
        return matriz.get(fila).get(columna);
    }

    public Nodo getNodo(Coordenada coordenada) {
        return getNodo(coordenada.getFila(), coordenada.getColumna());
    }

    public List<Nodo> getAdj(Coordenada x) {
        List<Nodo> adyacentes = new ArrayList<>();
        int fila = x.getFila();
        int columna = x.getColumna();

        // Movimientos posibles: arriba, abajo, izquierda, derecha
        if (fila > 0)
            adyacentes.add(getNodo(fila - 1, columna)); // Arriba
        if (fila < filas - 1)
            adyacentes.add(getNodo(fila + 1, columna)); // Abajo
        if (columna > 0)
            adyacentes.add(getNodo(fila, columna - 1)); // Izquierda
        if (columna < columnas - 1)
            adyacentes.add(getNodo(fila, columna + 1)); // Derecha

        return adyacentes;
    }

    private double calcularHeuristica(Coordenada a, Coordenada b) {
        return Math.abs(a.getColumna() - b.getColumna()) + Math.abs(a.getFila() - b.getFila());
    }

    private double calcularFScore(Coordenada inicio, Coordenada destino) {
        Nodo nodo = getNodo(inicio);
        double g = nodo.getGScore();
        double h = calcularHeuristica(nodo.getCoordenada(), destino); // Heurística
        return g + h; // f(n) = g(n) + h(n)
    }

    public List<Coordenada> aStar(Coordenada inicio, Coordenada destino) {

        PriorityQueue<Nodo> openSet = new PriorityQueue<>((a, b) -> Double.compare(a.getFScore(), b.getFScore()));

        Map<Nodo, Nodo> cameFrom = new HashMap<>(); // Mapa para rastrear el camino

        // gscore de todos los nodos igual a infinito
        for (Nodo nodo : list_nodos) {
            nodo.setGScore(Double.POSITIVE_INFINITY);
            nodo.setFScore(Double.POSITIVE_INFINITY);
        }
        Nodo nodoInicio = getNodo(inicio);
        // iniciamos el nodo inicial con score 0
        nodoInicio.setGScore(0);
        nodoInicio.setFScore(calcularHeuristica(inicio, destino));

        openSet.add(nodoInicio); // Añadimos el nodo inicial a la cola de prioridad
        while (!openSet.isEmpty()) {
            Nodo nodoActual = openSet.poll(); // Nodo con el menor fScore
            if (nodoActual.getCoordenada().equals(destino)) {
                return reconstruirRuta(cameFrom, nodoActual);
            }
            for (Nodo vecino : getAdj(nodoActual.getCoordenada())) {
                if (vecino.isBloqueado()) {
                    continue; // Ignorar nodos bloqueados
                }
                double tentativeGScore = nodoActual.getGScore() + 1; // Asumimos un costo de 1 para cada movimiento

                if (tentativeGScore < vecino.getGScore()) {
                    cameFrom.put(vecino, nodoActual); // Actualizamos el camino más corto
                    vecino.setGScore(tentativeGScore);
                    vecino.setFScore(calcularFScore(vecino.getCoordenada(), destino)); // Actualizamos el fScore

                    if (!openSet.contains(vecino)) {
                        openSet.add(vecino); // Añadimos a la cola si no está ya
                    }
                }
            }
        }
        return Collections.emptyList(); // No se encontró ruta
    }

    private List<Coordenada> reconstruirRuta(Map<Nodo, Nodo> cameFrom, Nodo nodoActual) {
        List<Coordenada> ruta = new ArrayList<>();
        while (nodoActual != null) {
            ruta.add(nodoActual.getCoordenada());
            nodoActual = cameFrom.get(nodoActual);
        }
        Collections.reverse(ruta);
        return ruta;
    }

}
