package com.plg.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import com.plg.utils.Gen;
import com.plg.utils.Individuo;

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

    private List<List<Nodo>> matriz;

    private static Mapa instance;

    public static Mapa getInstance() {
        if (instance == null) {
            initializeInstance();
        }
        return instance;
    }

    public static void initializeInstance() {
        int columnas = 71;
        int filas = 51;
        instance = new Mapa(columnas, filas);
    }

    // Construye el mapa como un grafo
    public Mapa(int columnas, int filas) {
        this.columnas = columnas;
        this.filas = filas;
        this.matriz = new ArrayList<>();
        for (int i = 0; i < filas; i++) {
            List<Nodo> fila = new ArrayList<>(columnas);
            for (int j = 0; j < columnas; j++) {
                Nodo nodo = Nodo.builder().coordenada(new Coordenada(i, j)).tipoNodo(TipoNodo.NORMAL).build();
                fila.add(nodo);
            }
            this.matriz.add(fila);
        }
    }

    public void imprimirMapa() {
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
                } else if (nodoActual.getTipoNodo() == TipoNodo.ALMACEN) {
                    cell = " A "; // Almacén
                } else if (nodoActual.getTipoNodo() == TipoNodo.PEDIDO) {
                    cell = " P "; // Pedido
                } else if (nodoActual.getTipoNodo() == TipoNodo.CAMION_AVERIADO) {
                    cell = " C "; // Camión
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

    public void imprimirMapa(List<Nodo> nodos) {
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
                } else if (nodos != null && nodos.contains(nodoActual)) {
                    cell = " * "; // Nodo especial
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

    public void imprimirMapa(Individuo individuo) {
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
                } else if (nodoActual.getTipoNodo() == TipoNodo.ALMACEN) {
                    cell = " A "; // Almacén
                } else if (nodoActual.getTipoNodo() == TipoNodo.PEDIDO) {
                    cell = " P "; // Pedido
                } else if (nodoActual.getTipoNodo() == TipoNodo.CAMION_AVERIADO) {
                    cell = " C "; // Camión
                } else {
                    if (individuo != null) {
                        for (int k = 0; k < individuo.getCromosoma().size(); k++) {
                            Gen gen = individuo.getCromosoma().get(k);
                            List<Nodo> nodosGen = gen.getRutaFinal();
                            if (nodosGen.contains(nodoActual)) {
                                cell = " " + (k + 1) + " ";
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

    public void setNodo(Coordenada coordenada, Nodo nodo) {
        matriz.get(coordenada.getFila()).set(coordenada.getColumna(), nodo);
    }

    public Nodo getNodo(Coordenada coordenada) {
        return getNodo(coordenada.getFila(), coordenada.getColumna());
    }

    public List<Nodo> getAdj(Coordenada x) {
        List<Nodo> adyacentes = new ArrayList<>();
        int fila = x.getFila();
        int columna = x.getColumna();

        // Movimientos posibles: arriba, abajo, izquierda, derecha
        if (fila > 0) {
            adyacentes.add(getNodo(fila - 1, columna)); // Arriba
        }
        if (fila < filas - 1) {
            adyacentes.add(getNodo(fila + 1, columna)); // Abajo
        }
        if (columna > 0) {
            adyacentes.add(getNodo(fila, columna - 1)); // Izquierda
        }
        if (columna < columnas - 1) {
            adyacentes.add(getNodo(fila, columna + 1)); // Derecha
        }

        return adyacentes;
    }

    // Cambiado a public para poder usarlo desde fuera
    public double calcularHeuristica(Nodo a, Nodo b) {
        return Math.abs(a.getCoordenada().getColumna() - b.getCoordenada().getColumna()) +
                Math.abs(a.getCoordenada().getFila() - b.getCoordenada().getFila());
    }

    public List<Nodo> aStar(Nodo nodo1, Nodo nodo2) {
        // Validaciones de entrada
        if (nodo1 == null || nodo2 == null || nodo1.getCoordenada() == null || nodo2.getCoordenada() == null) {
            System.err.println("⚠️ A*: Nodos de entrada inválidos");
            return Collections.emptyList();
        }
        
        Nodo inicio = getNodo(nodo1.getCoordenada());
        Nodo destino = getNodo(nodo2.getCoordenada());
        
        // Si origen y destino son el mismo, devolver ruta directa
        if (inicio.equals(destino)) {
            return Collections.singletonList(inicio);
        }
        
        PriorityQueue<Nodo> openSet = new PriorityQueue<>((a, b) -> Double.compare(a.getFScore(), b.getFScore()));
        Map<Nodo, Nodo> cameFrom = new HashMap<>(); // Cambiar a HashMap para mejor rendimiento
        Set<Nodo> closedSet = new HashSet<>(); // Agregar conjunto cerrado
        
        // Límites de seguridad para prevenir OutOfMemoryError
        final int MAX_ITERATIONS = 10000;
        final int MAX_NODES_IN_PATH = 500;
        int iteraciones = 0;
        
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                Nodo nodo = getNodo(i, j);
                nodo.setGScore(Double.POSITIVE_INFINITY);
                nodo.setFScore(Double.POSITIVE_INFINITY);
            }
        }
        
        inicio.setGScore(0);
        inicio.setFScore(calcularHeuristica(inicio, destino));
        openSet.add(inicio);
        
        while (!openSet.isEmpty() && iteraciones < MAX_ITERATIONS) {
            iteraciones++;
            
            Nodo nodoActual = openSet.poll();
            
            // Verificar si ya procesamos este nodo
            if (closedSet.contains(nodoActual)) {
                continue;
            }
            closedSet.add(nodoActual);
            
            if (nodoActual.equals(destino)) {
                List<Nodo> ruta = reconstruirRuta(cameFrom, nodoActual);
                // Verificar que la ruta no sea demasiado larga
                if (ruta.size() > MAX_NODES_IN_PATH) {
                    System.err.println("⚠️ A*: Ruta demasiado larga (" + ruta.size() + " nodos), devolviendo ruta directa");
                    return Collections.singletonList(destino);
                }
                return ruta;
            }
            
            for (Nodo vecino : getAdj(nodoActual.getCoordenada())) {
                // Saltar nodos ya procesados
                if (closedSet.contains(vecino)) {
                    continue;
                }
                
                // Permitir llegar a un nodo bloqueado solo si es el destino
                if (vecino.isBloqueado() && !vecino.equals(destino)) {
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
        
        // Si se agotaron las iteraciones o no se encontró ruta
        if (iteraciones >= MAX_ITERATIONS) {
            System.err.println("⚠️ A*: Se alcanzó el límite de iteraciones (" + MAX_ITERATIONS + "), devolviendo ruta directa");
        } else {
            System.err.println("⚠️ A*: No se encontró ruta entre " + inicio.getCoordenada() + " y " + destino.getCoordenada());
        }
        
        return Collections.singletonList(destino); // Ruta directa como fallback
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
