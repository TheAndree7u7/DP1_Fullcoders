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
        if (instance != null) {
            throw new IllegalStateException("Mapa instance has already been initialized.");
        }
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
            System.err.println("❌ A*: Nodos de entrada inválidos - nodo1=" + nodo1 + ", nodo2=" + nodo2);
            return Collections.emptyList();
        }
        
        // Log silenciado para evitar spam en consola
        // System.out.println("🔍 A*: Iniciando búsqueda de " + nodo1.getCoordenada() + " a " + nodo2.getCoordenada());
        
        Nodo inicio = getNodo(nodo1.getCoordenada());
        Nodo destino = getNodo(nodo2.getCoordenada());
        
        // Verificar que las coordenadas están dentro del mapa
        if (inicio == null || destino == null) {
            System.err.println("❌ A*: Coordenadas fuera del mapa - inicio=" + inicio + ", destino=" + destino);
            return Collections.emptyList();
        }
        
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
            System.err.println("❌ A*: CRÍTICO - Límite de iteraciones para " + inicio.getCoordenada() + " → " + destino.getCoordenada());
        } else {
            System.err.println("❌ A*: CRÍTICO - Sin ruta válida entre " + inicio.getCoordenada() + " y " + destino.getCoordenada());
        }
        
        // Loguear información de debugging
        System.err.println("   📊 DEBUG: Inicio bloqueado=" + inicio.isBloqueado() + ", Destino bloqueado=" + destino.isBloqueado());
        System.err.println("   📊 DEBUG: Mapa tamaño=" + filas + "x" + columnas + ", Iteraciones ejecutadas=" + iteraciones);
        System.err.println("   📊 DEBUG: Nodos en openSet al fallar=" + openSet.size() + ", Nodos en closedSet=" + closedSet.size());
        
        // Intentar crear una ruta alternativa como último recurso
        System.err.println("🔄 A*: Intentando ruta alternativa como último recurso");
        return crearRutaAlternativa(inicio, destino);
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

    /**
     * Crea una ruta alternativa cuando A* no puede encontrar una ruta directa.
     * Intenta encontrar un nodo cercano al destino que sea alcanzable.
     */
    private List<Nodo> crearRutaAlternativa(Nodo inicio, Nodo destino) {
        // Prevenir recursión infinita - si ya estamos en una llamada recursiva, devolver ruta directa
        if (Thread.currentThread().getStackTrace().length > 50) {
            List<Nodo> rutaDirecta = new ArrayList<>();
            rutaDirecta.add(inicio);
            if (!inicio.equals(destino)) {
                rutaDirecta.add(destino);
            }
            return rutaDirecta;
        }
        
        // Si el destino está bloqueado, intentar encontrar un nodo no bloqueado cerca
        if (destino.isBloqueado()) {
            Nodo nodoAlternativo = encontrarNodoNoBloquadoCercano(destino);
            if (nodoAlternativo != null && !nodoAlternativo.equals(inicio)) {
                // Usar un algoritmo más simple para evitar recursión
                List<Nodo> rutaAlternativa = crearRutaSimple(inicio, nodoAlternativo);
                if (rutaAlternativa.size() > 1) {
                    // Agregar el destino original al final
                    rutaAlternativa.add(destino);
                    return rutaAlternativa;
                }
            }
        }
        
        // Como último recurso, crear una ruta directa simple
        List<Nodo> rutaDirecta = new ArrayList<>();
        rutaDirecta.add(inicio);
        if (!inicio.equals(destino)) {
            rutaDirecta.add(destino);
        }
        return rutaDirecta;
    }

    /**
     * Crea una ruta simple sin usar A* para evitar recursión.
     * Intenta una ruta paso a paso evitando nodos bloqueados.
     */
    private List<Nodo> crearRutaSimple(Nodo inicio, Nodo destino) {
        List<Nodo> ruta = new ArrayList<>();
        ruta.add(inicio);
        
        int filaActual = inicio.getCoordenada().getFila();
        int columnaActual = inicio.getCoordenada().getColumna();
        int filaDestino = destino.getCoordenada().getFila();
        int columnaDestino = destino.getCoordenada().getColumna();
        
        // Moverse paso a paso hacia el destino
        while (filaActual != filaDestino || columnaActual != columnaDestino) {
            // Decidir hacia dónde moverse
            int nuevaFila = filaActual;
            int nuevaColumna = columnaActual;
            
            // Priorizar el movimiento que más se acerque al destino
            if (filaActual < filaDestino) {
                nuevaFila = filaActual + 1;
            } else if (filaActual > filaDestino) {
                nuevaFila = filaActual - 1;
            } else if (columnaActual < columnaDestino) {
                nuevaColumna = columnaActual + 1;
            } else if (columnaActual > columnaDestino) {
                nuevaColumna = columnaActual - 1;
            }
            
            // Verificar límites del mapa
            if (nuevaFila >= 0 && nuevaFila < filas && 
                nuevaColumna >= 0 && nuevaColumna < columnas) {
                
                Nodo siguienteNodo = getNodo(nuevaFila, nuevaColumna);
                
                // Si el siguiente nodo está bloqueado, intentar una ruta alternativa
                if (siguienteNodo.isBloqueado() && !siguienteNodo.equals(destino)) {
                    // Intentar moverse primero en la otra dirección
                    if (nuevaFila != filaActual) {
                        // Estábamos moviendo en fila, intentar columna
                        if (columnaActual < columnaDestino) {
                            nuevaColumna = columnaActual + 1;
                            nuevaFila = filaActual;
                        } else if (columnaActual > columnaDestino) {
                            nuevaColumna = columnaActual - 1;
                            nuevaFila = filaActual;
                        }
                    } else {
                        // Estábamos moviendo en columna, intentar fila
                        if (filaActual < filaDestino) {
                            nuevaFila = filaActual + 1;
                            nuevaColumna = columnaActual;
                        } else if (filaActual > filaDestino) {
                            nuevaFila = filaActual - 1;
                            nuevaColumna = columnaActual;
                        }
                    }
                    
                    // Verificar de nuevo
                    if (nuevaFila >= 0 && nuevaFila < filas && 
                        nuevaColumna >= 0 && nuevaColumna < columnas) {
                        siguienteNodo = getNodo(nuevaFila, nuevaColumna);
                    }
                }
                
                // Si aún está bloqueado, salir del bucle para evitar loops infinitos
                if (siguienteNodo.isBloqueado() && !siguienteNodo.equals(destino)) {
                    System.err.println("⚠️ crearRutaSimple: No se puede evitar nodo bloqueado, terminando ruta");
                    break;
                }
                
                ruta.add(siguienteNodo);
                filaActual = nuevaFila;
                columnaActual = nuevaColumna;
                
                // Evitar bucles infinitos
                if (ruta.size() > 1000) {
                    System.err.println("⚠️ crearRutaSimple: Ruta demasiado larga, terminando");
                    break;
                }
            } else {
                // Fuera del mapa, salir
                System.err.println("⚠️ crearRutaSimple: Coordenadas fuera del mapa");
                break;
            }
        }
        
        return ruta;
    }

    /**
     * Encuentra un nodo no bloqueado cerca del nodo especificado.
     */
    private Nodo encontrarNodoNoBloquadoCercano(Nodo nodo) {
        Coordenada coord = nodo.getCoordenada();
        int fila = coord.getFila();
        int columna = coord.getColumna();
        
        // Buscar en un radio creciente hasta encontrar un nodo no bloqueado
        for (int radio = 1; radio <= 5; radio++) {
            for (int df = -radio; df <= radio; df++) {
                for (int dc = -radio; dc <= radio; dc++) {
                    if (df == 0 && dc == 0) continue; // Saltar el nodo original
                    
                    int nuevaFila = fila + df;
                    int nuevaColumna = columna + dc;
                    
                    // Verificar límites del mapa
                    if (nuevaFila >= 0 && nuevaFila < filas && 
                        nuevaColumna >= 0 && nuevaColumna < columnas) {
                        
                        Nodo candidato = getNodo(nuevaFila, nuevaColumna);
                        if (!candidato.isBloqueado()) {
                            return candidato;
                        }
                    }
                }
            }
        }
        
        return null; // No se encontró ningún nodo no bloqueado cerca
    }
}
