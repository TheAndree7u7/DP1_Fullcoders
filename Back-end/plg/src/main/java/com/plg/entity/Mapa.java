package com.plg.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import com.plg.utils.Gen;
import com.plg.utils.Individuo;
import com.plg.utils.Parametros;

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

    public void limpiarMapa() {
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                Nodo nodo = Nodo.builder().coordenada(new Coordenada(i, j)).tipoNodo(TipoNodo.NORMAL).build();
                this.matriz.get(i).set(j, nodo);
            }
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
        // PRUEBA DEPURACIÓN
        // 41 - 47
        Coordenada coordenada1 = new Coordenada(9,14);
        Coordenada coordenada2 = new Coordenada(8,12);
        System.out.println("🔄 HOLLAAAAA" + coordenada1 + " " + coordenada2);

        System.out.println("ALMACENES");
        List<Almacen> almacenes = Parametros.dataLoader.almacenes;
        for (Almacen almacen : almacenes) {
            System.out.println("Almacen Nombre: " + almacen.getNombre() + " Almacen: " + almacen.getCoordenada() + " Capacidad GLP: " + almacen.getCapacidadActualGLP());
        }

        Nodo inicio = getNodo(coordenada1);
        Nodo destino = getNodo(coordenada2);
        // Si origen y destino son el mismo, devolver ruta directa
        if (inicio.equals(destino)) {
            return Collections.singletonList(inicio);
        }

        // Verificar si el destino está bloqueado y buscar el nodo libre más cercano
        Nodo destinoFinal = destino;
        if (destino.isBloqueado()) {
            System.out.println("🔄 A*: El destino está bloqueado");
        }

        // Intentar A* sin pasar por nodos bloqueados
        List<Nodo> ruta = intentarAStar(inicio, destinoFinal);
        return ruta;
    }

    private List<Nodo> intentarAStar(Nodo inicio, Nodo destino) {
        PriorityQueue<Nodo> openSet = new PriorityQueue<>((a, b) -> Double.compare(a.getFScore(), b.getFScore()));
        Map<Nodo, Nodo> cameFrom = new HashMap<>();
        Map<Nodo, Integer> cost_so_far = new HashMap<>();

        // Inicializar scores
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                Nodo nodo = getNodo(i, j);
                nodo.setGScore(Double.POSITIVE_INFINITY);
                nodo.setFScore(Double.POSITIVE_INFINITY);
            }
        }

        inicio.setGScore(0);
        inicio.setFScore(calcularHeuristica(inicio, destino));
        cost_so_far.put(inicio, 0);
        openSet.add(inicio);

        while (!openSet.isEmpty()) {
   
            Nodo nodoActual = openSet.poll();

            if (nodoActual.equals(destino)) {
                List<Nodo> ruta = reconstruirRuta(cameFrom, nodoActual);

                return ruta;
            }

            for (Nodo vecino : getAdj(nodoActual.getCoordenada())) {

                // Permitir llegar a un nodo bloqueado solo si es el destino
                if (vecino.isBloqueado() && !vecino.equals(destino)) {
                    continue;
                }

                double tentativeGScore = nodoActual.getGScore() + 1;
                // Validamos que el vecino no esta en cost_so_far
                boolean valido1 = cost_so_far.containsKey(vecino);
                if (!valido1 || tentativeGScore < vecino.getGScore()) {
                    cameFrom.put(vecino, nodoActual);
                    vecino.setGScore(tentativeGScore);
                    vecino.setFScore(tentativeGScore + calcularHeuristica(vecino, destino));
                    openSet.add(vecino);
                }
            }
        }


        System.out.println(
                "⚠️ A*: No se encontró ruta entre " + inicio.getCoordenada() + " y " + destino.getCoordenada());
        // Imprimir el el cameFrom para depuración
        System.out.println("CameFrom: " + cameFrom);
        return Collections.singletonList(destino);
    }

    private List<Nodo> reconstruirRuta(Map<Nodo, Nodo> cameFrom, Nodo nodoActual) {
        List<Nodo> ruta = new ArrayList<>();
           System.out.println("ALMACENES");
        List<Almacen> almacenes = Parametros.dataLoader.almacenes;
        for (Almacen almacen : almacenes) {
            System.out.println("Almacen Nombre: " + almacen.getNombre() + " Almacen: " + almacen.getCoordenada() + " Capacidad GLP: " + almacen.getCapacidadActualGLP());
        }

        while (nodoActual != null) {
            // Imprimimos el nodo actual para depuración
            System.out.println("Nodo:" + nodoActual);
            ruta.add(nodoActual);
            nodoActual = cameFrom.get(nodoActual);
        }
        Collections.reverse(ruta);
        return ruta;
    }
}
