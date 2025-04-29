package com.plg.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.plg.config.MapaConfig;
import com.plg.entity.Bloqueo;

/**
 * Servicio para la navegación en mapa reticular Proporciona funciones para
 * calcular rutas en un mapa de calles perpendiculares
 */
@Service
public class MapaReticularService {
    
    private static final Logger logger = LoggerFactory.getLogger(MapaReticularService.class);

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

        logger.info("Iniciando cálculo de ruta óptima con A*: desde ({},{}) hasta ({},{})", 
                xInicio, yInicio, xFin, yFin);
        logger.info("Bloqueos activos considerados: {}", bloqueos.size());
        
        long startTime = System.currentTimeMillis();

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
        
        int iteraciones = 0;
        int nodosExpandidos = 0;
        int nodosEvaluados = 0;
        int bloqueosSorteados = 0;

        while (!nodosAbiertos.isEmpty()) {
            iteraciones++;
            
            // Obtener el nodo con menor fScore
            String nodoActual = colaPrioridad.poll();
            nodosExpandidos++;

            // Si llegamos al destino, reconstruir y devolver el camino
            if (nodoActual.equals(fin)) {
                List<double[]> rutaFinal = reconstruirCamino(caminoPadre, nodoActual);
                double distanciaRuta = calcularLongitudRuta(rutaFinal);
                long tiempoTotal = System.currentTimeMillis() - startTime;
                
                logger.info("Ruta óptima encontrada: {} puntos, distancia: {} km", rutaFinal.size(), distanciaRuta);
                logger.info("Estadísticas A*: {} iteraciones, {} nodos expandidos, {} nodos evaluados, {} bloqueos sorteados", 
                        iteraciones, nodosExpandidos, nodosEvaluados, bloqueosSorteados);
                logger.info("Tiempo de cálculo de ruta: {} ms", tiempoTotal);
                
                return rutaFinal;
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
                nodosEvaluados++;
                String vecinoKey = coordenadaAKey(vecino[0], vecino[1]);

                // Si el vecino ya fue evaluado, continuar
                if (nodosCerrados.contains(vecinoKey)) {
                    continue;
                }

                // Verificar si hay un bloqueo entre el nodo actual y el vecino
                boolean hayBloqueo = verificarBloqueoEntrePuntos(x, y, vecino[0], vecino[1], bloqueos);
                if (hayBloqueo) {
                    bloqueosSorteados++;
                    if (iteraciones % 50 == 0) {  // Loguear solo ocasionalmente para no saturar
                        logger.debug("Bloqueo detectado entre ({},{}) y ({},{})", 
                                x, y, vecino[0], vecino[1]);
                    }
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
                
                // Log de progreso ocasional
                if (iteraciones % 1000 == 0) {
                    logger.debug("Progreso A*: {} iteraciones, {} nodos abiertos, {} nodos cerrados", 
                            iteraciones, nodosAbiertos.size(), nodosCerrados.size());
                }
            }
        }

        // Si llegamos aquí, no encontramos un camino
        logger.warn("No se encontró ruta posible desde ({},{}) hasta ({},{})", 
                xInicio, yInicio, xFin, yFin);
        logger.info("Estadísticas A* fallido: {} iteraciones, {} nodos expandidos, {} nodos evaluados, {} bloqueos sorteados", 
                iteraciones, nodosExpandidos, nodosEvaluados, bloqueosSorteados);
        
        return new ArrayList<>();
    }
    
    /**
     * Obtiene todos los bloqueos activos y los utiliza para calcular una ruta
     * óptima
     */
    public List<double[]> calcularRutaOptimaConsiderandoBloqueos(double xInicio, double yInicio, double xFin, double yFin) {
        // Obtener bloqueos activos
        List<Bloqueo> bloqueosActivos = bloqueoService.obtenerBloqueosActivos(java.time.LocalDateTime.now());
        logger.info("Calculando ruta con {} bloqueos activos en el momento actual", bloqueosActivos.size());
        
        // Calcular ruta evitando los bloqueos
        List<double[]> ruta = calcularRutaOptima(xInicio, yInicio, xFin, yFin, bloqueosActivos);
        
        // Calcular métricas de la ruta
        if (!ruta.isEmpty()) {
            double distancia = calcularLongitudRuta(ruta);
            double tiempoEstimado = estimarTiempoViajeMinutos(ruta, 30); // 30 km/h promedio
            logger.info("Ruta generada: {} puntos, longitud: {} km, tiempo estimado: {} minutos", 
                    ruta.size(), distancia, tiempoEstimado);
        }
        
        return ruta;
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
