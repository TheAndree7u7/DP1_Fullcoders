package com.plg.service;

import com.plg.config.MapaConfig;
import com.plg.entity.Bloqueo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Servicio para la navegación en mapa reticular Proporciona funciones para
 * calcular rutas en un mapa de calles perpendiculares
 */
@Service
public class MapaReticularService {

    @Autowired
    private MapaConfig mapaConfig;

    @Autowired
    private BloqueoService bloqueoService;

    /**
     * Calcula la ruta más corta entre dos puntos en un mapa reticular
     * utilizando el algoritmo A* considerando bloqueos
     *
     * @param xInicio Coordenada X del punto de inicio
     * @param yInicio Coordenada Y del punto de inicio
     * @param xFin Coordenada X del punto de destino
     * @param yFin Coordenada Y del punto de destino
     * @param bloqueos Lista de bloqueos activos a evitar
     * @return Lista de nodos que forman la ruta óptima
     */
    public List<double[]> calcularRutaOptima(double xInicio, double yInicio, double xFin, double yFin, List<Bloqueo> bloqueos) {
        // Verificar que las coordenadas estén dentro del mapa
        if (!mapaConfig.estaEnMapa(xInicio, yInicio) || !mapaConfig.estaEnMapa(xFin, yFin)) {
            throw new IllegalArgumentException("Coordenadas fuera de los límites del mapa");
        }

        // Implementación del algoritmo A*
        // Conjuntos para el algoritmo
        Set<String> nodosAbiertos = new HashSet<>();
        Set<String> nodosCerrados = new HashSet<>();
        Map<String, String> caminoPadre = new HashMap<>();
        Map<String, Double> gScore = new HashMap<>(); // Costo real desde inicio
        Map<String, Double> fScore = new HashMap<>(); // Costo estimado total

        // Inicialización
        String inicio = coordenadaAKey(xInicio, yInicio);
        String fin = coordenadaAKey(xFin, yFin);

        nodosAbiertos.add(inicio);
        gScore.put(inicio, 0.0);
        fScore.put(inicio, heuristica(xInicio, yInicio, xFin, yFin));

        // Cola de prioridad para seleccionar el nodo con menor fScore
        PriorityQueue<String> colaPrioridad = new PriorityQueue<>(
                Comparator.comparingDouble(nodo -> fScore.getOrDefault(nodo, Double.MAX_VALUE))
        );
        colaPrioridad.add(inicio);

        while (!nodosAbiertos.isEmpty()) {
            // Obtener el nodo con menor fScore
            String nodoActual = colaPrioridad.poll();

            // Si llegamos al destino, reconstruir y devolver el camino
            if (nodoActual.equals(fin)) {
                return reconstruirCamino(caminoPadre, nodoActual);
            }

            // Mover el nodo actual de abiertos a cerrados
            nodosAbiertos.remove(nodoActual);
            nodosCerrados.add(nodoActual);

            // Extraer coordenadas del nodo actual
            double[] coords = parseKeyToCoords(nodoActual);
            double x = coords[0], y = coords[1];
 

            // Obtener vecinos
            double[][] nodosAdyacentes = mapaConfig.obtenerNodosAdyacentes(x, y);

            for (double[] vecino : nodosAdyacentes) {
                String vecinoKey = coordenadaAKey(vecino[0], vecino[1]);

                // Si el vecino ya fue evaluado, continuar
                if (nodosCerrados.contains(vecinoKey)) {
                    continue;
                }

                // Verificar si hay un bloqueo entre el nodo actual y el vecino
                if (verificarBloqueoEntrePuntos(x, y, vecino[0], vecino[1], bloqueos)) {
                    continue; // Omitir este vecino si el camino está bloqueado
                }

                // Calcular el costo hasta el vecino
                double tentativeGScore = gScore.get(nodoActual) + mapaConfig.getDistanciaNodos();

                // Si el vecino no está en abiertos o encontramos un mejor camino
                if (!nodosAbiertos.contains(vecinoKey) || tentativeGScore < gScore.getOrDefault(vecinoKey, Double.MAX_VALUE)) {
                    // Actualizar el camino y los puntajes
                    caminoPadre.put(vecinoKey, nodoActual);
                    gScore.put(vecinoKey, tentativeGScore);
                    fScore.put(vecinoKey, tentativeGScore + heuristica(vecino[0], vecino[1], xFin, yFin));

                    // Agregar a conjunto de nodos abiertos si no está ya
                    if (!nodosAbiertos.contains(vecinoKey)) {
                        nodosAbiertos.add(vecinoKey);
                        colaPrioridad.add(vecinoKey);
                    }
                }
            }
        }

        // Si llegamos aquí, no encontramos un camino
        return new ArrayList<>();
    }

    /**
     * Verifica si hay un bloqueo entre dos puntos adyacentes
     */
    private boolean verificarBloqueoEntrePuntos(double x1, double y1, double x2, double y2, List<Bloqueo> bloqueos) {
        // Los puntos deben ser adyacentes (diferencia en solo una coordenada y valor 1)
        boolean sonAdyacentes = (Math.abs(x1 - x2) + Math.abs(y1 - y2)) == 1;
        if (!sonAdyacentes) {
            throw new IllegalArgumentException("Los puntos deben ser adyacentes para verificar bloqueo");
        }

        for (Bloqueo bloqueo : bloqueos) {
            // Si el bloqueo está activo y su tramo intersecta con la línea entre los puntos
            if (bloqueo.isActivo() && intersectaConTramo(x1, y1, x2, y2, bloqueo)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Verifica si un bloqueo intersecta un tramo de calle
     */
    private boolean intersectaConTramo(double x1, double y1, double x2, double y2, Bloqueo bloqueo) {
        List<Bloqueo.Coordenada> coordenadasBloqueo = bloqueo.getCoordenadas();

        // Si hay menos de 2 coordenadas en el bloqueo, no puede haber intersección
        if (coordenadasBloqueo.size() < 2) {
            return false;
        }

        // Para cada par de coordenadas consecutivas en el bloqueo
        for (int i = 0; i < coordenadasBloqueo.size() - 1; i++) {
            Bloqueo.Coordenada c1 = coordenadasBloqueo.get(i);
            Bloqueo.Coordenada c2 = coordenadasBloqueo.get(i + 1);

            // En un mapa reticular, los tramos de bloqueo también deben ser horizontales o verticales
            // Verificar si el tramo del bloqueo y el tramo de calle son iguales
            boolean mismo_tramo_horizontal
                    = (y1 == y2 && c1.getY() == c2.getY() && y1 == c1.getY())
                    && ((x1 <= c1.getX() && c1.getX() <= x2) || (x1 <= c2.getX() && c2.getX() <= x2)
                    || (c1.getX() <= x1 && x1 <= c2.getX()) || (c1.getX() <= x2 && x2 <= c2.getX()));

            boolean mismo_tramo_vertical
                    = (x1 == x2 && c1.getX() == c2.getX() && x1 == c1.getX())
                    && ((y1 <= c1.getY() && c1.getY() <= y2) || (y1 <= c2.getY() && c2.getY() <= y2)
                    || (c1.getY() <= y1 && y1 <= c2.getY()) || (c1.getY() <= y2 && y2 <= c2.getY()));

            if (mismo_tramo_horizontal || mismo_tramo_vertical) {
                return true;
            }
        }

        return false;
    }

    /**
     * Heurística para el algoritmo A* (distancia Manhattan)
     */
    private double heuristica(double x1, double y1, double x2, double y2) {
        return mapaConfig.calcularDistanciaReticular(x1, y1, x2, y2);
    }

    /**
     * Reconstruye el camino desde el mapa de padres
     */
    private List<double[]> reconstruirCamino(Map<String, String> caminoPadre, String nodoActual) {
        List<double[]> camino = new ArrayList<>();
    
        // Agregar el nodo final
        camino.add(parseKeyToCoords(nodoActual));
    
        // Reconstruir desde el final hasta el inicio
        while (caminoPadre.containsKey(nodoActual)) {
            nodoActual = caminoPadre.get(nodoActual);
            camino.add(0, parseKeyToCoords(nodoActual)); // Insertar al inicio
        }
    
        return camino;
    }

    /**
     * Convierte coordenadas (x,y) a un string clave
     */
    private String coordenadaAKey(double x, double y) {
        return x + "," + y;
    }

    /**
     * Convierte una clave string a coordenadas
     */
    /**
     * Dado un string "12.0, 5.0" o un array ["12.0","5.0"], parsea cada
     * coordenada como double y la convierte a int (redondeando o truncando
     * según lo que necesites).
     */
    private int getIndexFromKey(String coordStr) {
        String[] parts = coordStr.split(",");
        int x = (int) Math.round(Double.parseDouble(parts[0]));
        int y = (int) Math.round(Double.parseDouble(parts[1]));
        // Número de columnas = ancho en nodos
        int cols = (int) (mapaConfig.getAncho() / mapaConfig.getDistanciaNodos()) + 1;
        return x * cols + y;
    }

    private double[] parseKeyToCoords(String key) {
        String[] parts = key.split(",");
        double x = Double.parseDouble(parts[0]);
        double y = Double.parseDouble(parts[1]);
        return new double[]{x, y};
    }

    /**
     * Calcula la longitud total de una ruta en km
     */
    public double calcularLongitudRuta(List<double[]> ruta) {
        // Si la ruta está vacía o tiene un solo punto, la longitud es 0
        if (ruta == null || ruta.size() <= 1) {
            return 0;
        }

        double longitud = 0;

        // Sumar las distancias entre nodos consecutivos
        for (int i = 0; i < ruta.size() - 1; i++) {
            double[] puntoActual = ruta.get(i);
            double[] puntoSiguiente = ruta.get(i + 1);

            longitud += mapaConfig.calcularDistanciaRealKm(
                    puntoActual[0], puntoActual[1],
                    puntoSiguiente[0], puntoSiguiente[1]
            );
        }

        return longitud;
    }

    /**
     * Obtiene todos los bloqueos activos y los utiliza para calcular una ruta
     * óptima
     */
    public List<double[]> calcularRutaOptimaConsiderandoBloqueos(double xInicio, double yInicio, double xFin, double yFin) {
        // Obtener bloqueos activos
        List<Bloqueo> bloqueosActivos = bloqueoService.obtenerBloqueosActivos(java.time.LocalDateTime.now());

        // Calcular ruta evitando los bloqueos
        return calcularRutaOptima(xInicio, yInicio, xFin, yFin, bloqueosActivos);
    }

    /**
     * Estima el tiempo de viaje en minutos para una ruta
     *
     * @param ruta La ruta a evaluar
     * @param velocidadKmh Velocidad promedio en km/h
     * @return Tiempo estimado en minutos
     */
    public double estimarTiempoViajeMinutos(List<double[]> ruta, double velocidadKmh) {
        double longitudKm = calcularLongitudRuta(ruta);

        // Tiempo = distancia / velocidad (en horas)
        double tiempoHoras = longitudKm / velocidadKmh;

        // Convertir horas a minutos
        return tiempoHoras * 60;
    }
}
