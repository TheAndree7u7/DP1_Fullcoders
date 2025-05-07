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
    private List<Almacen> almacenes = new ArrayList<>();

    private static Mapa instance;

    public static Mapa getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Mapa instance has not been initialized.");
        }
        return instance;
    }

    public static void initializeInstance(int columnas, int filas) {
        if (instance != null) {
            throw new IllegalStateException("Mapa instance has already been initialized.");
        }
        instance = new Mapa(columnas, filas);
    }

    // Construye el mapa como un grafo
    public Mapa(int columnas, int filas) {
        int totalNodos = filas * columnas;
        this.columnas = columnas;
        this.filas = filas;
        this.matriz = new ArrayList<>();

        // Creamos loa almacenes
        Almacen almacenCentral = Almacen.builder()
                .coordenada(new Coordenada(5, 5))
                .bloqueado(false)
                .tipoNodo(TipoNodo.ALMACEN)
                .capacidadActualGLP(1000.0)
                .capacidadMaximaGLP(1000.0)
                .capacidadCombustible(1000.0)
                .capacidadActualCombustible(1000.0)
                .capacidadMaximaCombustible(1000.0)
                .esCentral(true)
                .permiteCamionesEstacionados(true)
                .tipo("Central")
                .build();



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
                Nodo nodoActual = getNodo(i, j);
                String cell = " . "; // Valor por defecto
                if (nodoActual.isBloqueado()) {
                    cell = " X "; // Nodo bloqueado
                } else {
                    if (rutas != null) {
                        for (int k = 0; k < rutas.size(); k++) {
                            if (rutas.get(k).contains(nodoActual.getCoordenada())) {
                                cell = String.format(" %d ", k);
                                break;
                            }
                        }
                    }
                    if (pedidos != null) {
                        for (Pedido pedido : pedidos) {
                            if (pedido.getCoordenada().equals(nodoActual.getCoordenada())) {
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

    private double calcularHeuristica(Nodo a, Nodo b) {
        return Math.abs(a.getCoordenada().getColumna() - b.getCoordenada().getColumna()) +
               Math.abs(a.getCoordenada().getFila() - b.getCoordenada().getFila());
    }

    public List<Nodo> aStar(Nodo inicio, Nodo destino) {
        PriorityQueue<Nodo> openSet = new PriorityQueue<>((a, b) -> Double.compare(a.getFScore(), b.getFScore()));
        Map<Nodo, Nodo> cameFrom = new HashMap<>();

        for (Nodo nodo : list_nodos) {
            nodo.setGScore(Double.POSITIVE_INFINITY);
            nodo.setFScore(Double.POSITIVE_INFINITY);
        }

        inicio.setGScore(0);
        inicio.setFScore(calcularHeuristica(inicio, destino));
        openSet.add(inicio);
        while (!openSet.isEmpty()) {
            Nodo nodoActual = openSet.poll();
            if (nodoActual.equals(destino)) {
                return reconstruirRuta(cameFrom, nodoActual);
            }
            for (Nodo vecino : getAdj(nodoActual.getCoordenada())) {
                if (vecino.isBloqueado()) {
                    continue;
                }
                double tentativeGScore = nodoActual.getGScore() + 1;
                if (tentativeGScore < vecino.getGScore()) {
                    cameFrom.put(vecino, nodoActual);
                    vecino.setGScore(tentativeGScore);
                    vecino.setFScore(tentativeGScore + calcularHeuristica(vecino, destino));

                    if (!openSet.contains(vecino)) {
                        openSet.add(vecino);
                    }
                }
            }
        }
        return Collections.emptyList();
    }

    private List<Nodo> reconstruirRuta(Map<Nodo, Nodo> cameFrom, Nodo nodoActual) {
        List<Nodo> ruta = new ArrayList<>();
        while (nodoActual != null) {
            ruta.add(nodoActual);
            nodoActual = cameFrom.get(nodoActual);
        }
        Collections.reverse(ruta);
        return ruta;
    }

}
