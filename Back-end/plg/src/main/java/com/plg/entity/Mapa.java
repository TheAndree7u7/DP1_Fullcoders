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

        Nodo inicio = getNodo(nodo1.getCoordenada());
        Nodo destino = getNodo(nodo2.getCoordenada());

        // Si origen y destino son el mismo, devolver ruta directa
        if (inicio.equals(destino)) {
            return Collections.singletonList(inicio);
        }

        // Verificar si el destino está bloqueado y buscar el nodo libre más cercano
        Nodo destinoFinal = destino;
        if (destino.isBloqueado()) {
            destinoFinal = encontrarNodoLibreMasCercano(destino);
            if (destinoFinal == null) {
                System.err.println("⚠️ A*: No se puede encontrar un nodo libre cerca del destino");
                return Collections.singletonList(destino);
            }
        }

        // Intentar A* sin pasar por nodos bloqueados
        List<Nodo> ruta = intentarAStar(inicio, destinoFinal);

        // Si no se encuentra ruta, intentar con búsqueda de nodos intermedios
        if (ruta.size() <= 1) {
            System.out.println("🔄 A*: Intentando con nodos intermedios...");
            ruta = buscarRutaConIntermedios(inicio, destinoFinal);
        }

        return ruta;
    }

    private List<Nodo> intentarAStar(Nodo inicio, Nodo destino) {
        PriorityQueue<Nodo> openSet = new PriorityQueue<>((a, b) -> Double.compare(a.getFScore(), b.getFScore()));
        Map<Nodo, Nodo> cameFrom = new HashMap<>();
        Set<Nodo> closedSet = new HashSet<>();

        // Límites de seguridad mejorados
        final int MAX_ITERATIONS = 15000; // Aumentado de 10000
        final int MAX_NODES_IN_PATH = 1000; // Aumentado de 500
        int iteraciones = 0;

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
        openSet.add(inicio);

        while (!openSet.isEmpty() && iteraciones < MAX_ITERATIONS) {
            iteraciones++;

            Nodo nodoActual = openSet.poll();

            if (closedSet.contains(nodoActual)) {
                continue;
            }
            closedSet.add(nodoActual);

            if (nodoActual.equals(destino)) {
                List<Nodo> ruta = reconstruirRuta(cameFrom, nodoActual);
                if (ruta.size() > MAX_NODES_IN_PATH) {
                    System.err.println("⚠️ A*: Ruta demasiado larga (" + ruta.size() + " nodos)");
                    return Collections.singletonList(destino);
                }
                return ruta;
            }

            for (Nodo vecino : getAdj(nodoActual.getCoordenada())) {
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

        if (iteraciones >= MAX_ITERATIONS) {
            System.err.println("⚠️ A*: Se alcanzó el límite de iteraciones (" + MAX_ITERATIONS + ")");
        } else {
            System.err.println(
                    "⚠️ A*: No se encontró ruta entre " + inicio.getCoordenada() + " y " + destino.getCoordenada());
        }

        return Collections.singletonList(destino);
    }

    private Nodo encontrarNodoLibreMasCercano(Nodo nodoBloqueado) {
        int radio = 1;
        int maxRadio = 5; // Buscar en un radio máximo de 5 nodos

        while (radio <= maxRadio) {
            for (int i = -radio; i <= radio; i++) {
                for (int j = -radio; j <= radio; j++) {
                    int nuevaFila = nodoBloqueado.getCoordenada().getFila() + i;
                    int nuevaColumna = nodoBloqueado.getCoordenada().getColumna() + j;

                    if (esCoordenadaValida(nuevaFila, nuevaColumna)) {
                        Nodo candidato = getNodo(nuevaFila, nuevaColumna);
                        if (!candidato.isBloqueado()) {
                            return candidato;
                        }
                    }
                }
            }
            radio++;
        }
        return null;
    }

    private boolean esCoordenadaValida(int fila, int columna) {
        return fila >= 0 && fila < filas && columna >= 0 && columna < columnas;
    }

    private List<Nodo> buscarRutaConIntermedios(Nodo inicio, Nodo destino) {
        // Buscar nodos intermedios que puedan servir como "puentes"
        List<Nodo> nodosIntermedios = encontrarNodosIntermedios(inicio, destino);

        for (Nodo intermedio : nodosIntermedios) {
            List<Nodo> ruta1 = intentarAStar(inicio, intermedio);
            List<Nodo> ruta2 = intentarAStar(intermedio, destino);

            if (ruta1.size() > 1 && ruta2.size() > 1) {
                // Combinar rutas
                List<Nodo> rutaCompleta = new ArrayList<>(ruta1);
                rutaCompleta.remove(rutaCompleta.size() - 1); // Evitar duplicar el intermedio
                rutaCompleta.addAll(ruta2);
                return rutaCompleta;
            }
        }

        return Collections.singletonList(destino);
    }

    private List<Nodo> encontrarNodosIntermedios(Nodo inicio, Nodo destino) {
        List<Nodo> intermedios = new ArrayList<>();
        int distancia = (int) calcularHeuristica(inicio, destino);

        // Buscar nodos libres en el camino aproximado
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                Nodo candidato = getNodo(i, j);
                if (!candidato.isBloqueado()) {
                    double distInicio = calcularHeuristica(inicio, candidato);
                    double distDestino = calcularHeuristica(candidato, destino);

                    // Verificar si está en el camino aproximado
                    if (Math.abs((distInicio + distDestino) - distancia) <= 5) {
                        intermedios.add(candidato);
                        if (intermedios.size() >= 10) { // Limitar a 10 candidatos
                            break;
                        }
                    }
                }
            }
            if (intermedios.size() >= 10) {
                break;
            }
        }

        // Ordenar por distancia total
        intermedios.sort((a, b) -> {
            double distA = calcularHeuristica(inicio, a) + calcularHeuristica(a, destino);
            double distB = calcularHeuristica(inicio, b) + calcularHeuristica(b, destino);
            return Double.compare(distA, distB);
        });

        return intermedios;
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
