package com.plg.service;

import com.plg.entity.Bloqueo;
import com.plg.repository.BloqueoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.plg.config.MapaConfig;
@Service
public class BloqueoService {

    @Autowired
    private BloqueoRepository bloqueoRepository;

    @Autowired
    private MapaConfig mapaConfig;

    /**
     * Listar bloqueos activos
     * 
    /**
     *
     * @return Lista de bloqueos activos
     */
    public List<Bloqueo> listarBloqueos() {
        return bloqueoRepository.findByActivoTrue();
    }
    /** 
     * Carga los bloqueos desde un archivo para un mes específico
     * @param anio Año (ej. 2025)
     * @param mes Mes (1-12)
     * @return Lista de bloqueos cargados
     */
    public List<Bloqueo> cargarBloqueosMensuales(int anio, int mes) {
        List<Bloqueo> bloqueos = new ArrayList<>();
        
        // Formato del nombre del archivo: aaaamm.bloqueadas
        String nombreArchivo = String.format("%04d%02d.bloqueadas", anio, mes);
        Path rutaArchivo = Paths.get("src/main/resources/data/bloqueos/", nombreArchivo);
        
        try {
            if (!Files.exists(rutaArchivo)) {
                System.out.println("Archivo de bloqueos no encontrado: " + nombreArchivo);
                return bloqueos;
            }
            
            List<String> lineas = Files.readAllLines(rutaArchivo);
            
            // Formato esperado del archivo:
            // Para el formato antiguo: x1,y1,x2,y2,fechaInicio,fechaFin,descripcion
            // Para el formato nuevo: ##d##h##m-##d##h##m:x1,y1,x2,y2,...,xn,yn
            for (String linea : lineas) {
                if (linea.trim().isEmpty()) continue;
                
                // Determinar el formato
                if (linea.contains("-") && linea.contains(":")) {
                    // Formato nuevo
                    bloqueos.add(procesarLineaNuevoFormatoReticular(linea, anio, mes));
                } else {
                    // Formato antiguo (compatibilidad)
                    bloqueos.add(procesarLineaFormatoAntiguo(linea));
                }
            }
            
            // Guardar los bloqueos en la base de datos
            bloqueoRepository.saveAll(bloqueos);
            
            System.out.println("Cargados " + bloqueos.size() + " bloqueos para " + YearMonth.of(anio, mes));
            
        } catch (IOException e) {
            System.err.println("Error al leer el archivo de bloqueos: " + e.getMessage());
        }
        
        return bloqueos;
    }
    
    /**
     * Procesa una línea en el formato nuevo
     * ##d##h##m-##d##h##m:x1,y1,x2,y2,...,xn,yn
     */
    private Bloqueo procesarLineaNuevoFormato(String linea, int anio, int mes) {
        // Separar la línea en partes: tiempo y coordenadas
        String[] partes = linea.split(":");
        if (partes.length != 2) {
            throw new IllegalArgumentException("Formato de línea inválido: " + linea);
        }
        
        String tiempos = partes[0];
        String coordenadasStr = partes[1];
        
        // Separar los tiempos de inicio y fin
        String[] tiempoPartes = tiempos.split("-");
        if (tiempoPartes.length != 2) {
            throw new IllegalArgumentException("Formato de tiempo inválido: " + tiempos);
        }
        
        // Parsear fechas de inicio y fin
        LocalDateTime fechaInicio = parsearFechaHora(tiempoPartes[0], anio, mes);
        LocalDateTime fechaFin = parsearFechaHora(tiempoPartes[1], anio, mes);
        
        // Parsear coordenadas
        String[] coordValores = coordenadasStr.split(",");
        if (coordValores.length < 4 || coordValores.length % 2 != 0) {
            throw new IllegalArgumentException("Formato de coordenadas inválido: " + coordenadasStr);
        }
        
        List<Bloqueo.Coordenada> coordenadas = new ArrayList<>();
        for (int i = 0; i < coordValores.length; i += 2) {
            int x = Integer.parseInt(coordValores[i]);
            int y = Integer.parseInt(coordValores[i + 1]);
            coordenadas.add(new Bloqueo.Coordenada(x, y));
        }
        
        // Crear el objeto Bloqueo
        Bloqueo bloqueo = new Bloqueo();
        bloqueo.setFechaInicio(fechaInicio);
        bloqueo.setFechaFin(fechaFin);
        bloqueo.setCoordenadas(coordenadas);
        bloqueo.setDescripcion("Bloqueo programado " + fechaInicio.toLocalDate());
        bloqueo.setActivo(LocalDateTime.now().isAfter(fechaInicio) && LocalDateTime.now().isBefore(fechaFin));
        
        return bloqueo;
    }
    
    /**
     * Procesa una línea en el formato nuevo adaptado a mapa reticular
     * ##d##h##m-##d##h##m:x1,y1,x2,y2,...,xn,yn
     * Este método asegura que las coordenadas son nodos válidos en el mapa reticular
     */
    private Bloqueo procesarLineaNuevoFormatoReticular(String linea, int anio, int mes) {
        // Separar la línea en partes: tiempo y coordenadas
        String[] partes = linea.split(":");
        if (partes.length != 2) {
            throw new IllegalArgumentException("Formato de línea inválido: " + linea);
        }
        
        String tiempos = partes[0];
        String coordenadasStr = partes[1];
        
        // Separar los tiempos de inicio y fin
        String[] tiempoPartes = tiempos.split("-");
        if (tiempoPartes.length != 2) {
            throw new IllegalArgumentException("Formato de tiempo inválido: " + tiempos);
        }
        
        // Parsear fechas de inicio y fin
        LocalDateTime fechaInicio = parsearFechaHora(tiempoPartes[0], anio, mes);
        LocalDateTime fechaFin = parsearFechaHora(tiempoPartes[1], anio, mes);
        
        // Parsear coordenadas
        String[] coordValores = coordenadasStr.split(",");
        if (coordValores.length < 4 || coordValores.length % 2 != 0) {
            throw new IllegalArgumentException("Formato de coordenadas inválido: " + coordenadasStr);
        }
        
        List<Bloqueo.Coordenada> coordenadas = new ArrayList<>();
        for (int i = 0; i < coordValores.length; i += 2) {
            double x = Integer.parseInt(coordValores[i]);
            double y = Integer.parseInt(coordValores[i + 1]);
            
            // Verificar que la coordenada esté dentro del mapa reticular
            if (!mapaConfig.estaEnMapa(x, y)) {
                System.out.println("Advertencia: Coordenada fuera del mapa (" + x + "," + y + ") - ajustando al límite más cercano");
                x = Math.max(mapaConfig.getOrigenX(), Math.min(x, mapaConfig.getOrigenX() + mapaConfig.getLargo()));
                y = Math.max(mapaConfig.getOrigenY(), Math.min(y, mapaConfig.getOrigenY() + mapaConfig.getAncho()));
            }
            
            coordenadas.add(new Bloqueo.Coordenada(x, y));
        }
        
        // Validar que las coordenadas formen tramos horizontales o verticales válidos
        validarTramosReticularesValidos(coordenadas);
        
        // Crear el objeto Bloqueo
        Bloqueo bloqueo = new Bloqueo();
        bloqueo.setFechaInicio(fechaInicio);
        bloqueo.setFechaFin(fechaFin);
        bloqueo.setCoordenadas(coordenadas);
        bloqueo.setDescripcion("Bloqueo programado " + fechaInicio.toLocalDate());
        bloqueo.setActivo(LocalDateTime.now().isAfter(fechaInicio) && LocalDateTime.now().isBefore(fechaFin));
        
        return bloqueo;
    }

    /**
     * Valida que las coordenadas formen tramos horizontales o verticales válidos
     * en un mapa reticular (no diagonales)
     */
    private void validarTramosReticularesValidos(List<Bloqueo.Coordenada> coordenadas) {
        if (coordenadas.size() < 2) {
            return; // No hay suficientes puntos para formar un tramo
        }
        
        for (int i = 0; i < coordenadas.size() - 1; i++) {
            Bloqueo.Coordenada c1 = coordenadas.get(i);
            Bloqueo.Coordenada c2 = coordenadas.get(i + 1);
            
            // En un mapa reticular, un tramo debe ser horizontal o vertical
            boolean esHorizontal = c1.getY() == c2.getY();
            boolean esVertical = c1.getX() == c2.getX();
            
            if (!esHorizontal && !esVertical) {
                throw new IllegalArgumentException(
                    "Error: Tramo diagonal no permitido en mapa reticular - " +
                    "Desde (" + c1.getX() + "," + c1.getY() + ") hasta (" + c2.getX() + "," + c2.getY() + ")"
                );
            }
        }
    }
    
    /**
     * Parsea un string en formato ##d##h##m a LocalDateTime
     */
    private LocalDateTime parsearFechaHora(String tiempo, int anio, int mes) {
        // Expresión regular para extraer día, hora y minuto
        Pattern pattern = Pattern.compile("(\\d{2})d(\\d{2})h(\\d{2})m");
        Matcher matcher = pattern.matcher(tiempo);
        
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Formato de tiempo inválido: " + tiempo);
        }
        
        int dia = Integer.parseInt(matcher.group(1));
        int hora = Integer.parseInt(matcher.group(2));
        int minuto = Integer.parseInt(matcher.group(3));
        
        return LocalDateTime.of(anio, mes, dia, hora, minuto);
    }
    
    /**
     * Procesa una línea en el formato antiguo (para compatibilidad)
     * x1,y1,x2,y2,fechaInicio,fechaFin,descripcion
     */
    private Bloqueo procesarLineaFormatoAntiguo(String linea) {
        String[] partes = linea.split(",");
        if (partes.length < 7) {
            throw new IllegalArgumentException("Formato de línea antiguo inválido: " + linea);
        }
        
        int x1 = Integer.parseInt(partes[0]);
        int y1 = Integer.parseInt(partes[1]);
        int x2 = Integer.parseInt(partes[2]);
        int y2 = Integer.parseInt(partes[3]);
        
        LocalDate fechaInicio = LocalDate.parse(partes[4]);
        LocalDate fechaFin = LocalDate.parse(partes[5]);
        String descripcion = partes[6];
        
        // Crear lista de coordenadas
        List<Bloqueo.Coordenada> coordenadas = new ArrayList<>();
        coordenadas.add(new Bloqueo.Coordenada(x1, y1));
        coordenadas.add(new Bloqueo.Coordenada(x2, y2));
        
        // Crear objeto Bloqueo
        Bloqueo bloqueo = new Bloqueo();
        bloqueo.setFechaInicio(fechaInicio.atStartOfDay());
        bloqueo.setFechaFin(fechaFin.atTime(23, 59, 59));
        bloqueo.setCoordenadas(coordenadas);
        bloqueo.setDescripcion(descripcion);
        bloqueo.setActivo(LocalDate.now().isAfter(fechaInicio.minusDays(1)) && 
                          LocalDate.now().isBefore(fechaFin.plusDays(1)));
        
        return bloqueo;
    }
    
    /**
     * Verifica si una ruta está bloqueada entre dos puntos
     * @param x1 Coordenada X del punto inicial
     * @param y1 Coordenada Y del punto inicial
     * @param x2 Coordenada X del punto final
     * @param y2 Coordenada Y del punto final
     * @return true si la ruta está bloqueada
     */
    public boolean esRutaBloqueada(int x1, int y1, int x2, int y2) {
        // Obtener bloqueos activos en el momento actual
        List<Bloqueo> bloqueosActivos = bloqueoRepository.findByActivoTrue();
        
        // Si no hay bloqueos activos, la ruta está libre
        if (bloqueosActivos.isEmpty()) {
            return false;
        }
        
        // Para cada bloqueo, verificar si intersecta con la línea entre los puntos
        for (Bloqueo bloqueo : bloqueosActivos) {
            if (intersectaConRuta(bloqueo, x1, y1, x2, y2)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Verifica si un bloqueo intersecta con una ruta
     */
    private boolean intersectaConRuta(Bloqueo bloqueo, int x1, int y1, int x2, int y2) {
        List<Bloqueo.Coordenada> coordenadas = bloqueo.getCoordenadas();
        
        // Si hay menos de 2 coordenadas, no hay tramos
        if (coordenadas.size() < 2) {
            return false;
        }
        
        // Para cada tramo del bloqueo, verificar si intersecta con la ruta
        for (int i = 0; i < coordenadas.size() - 1; i++) {
            Bloqueo.Coordenada inicio = coordenadas.get(i);
            Bloqueo.Coordenada fin = coordenadas.get(i + 1);
            
            // Verificar intersección de líneas
            if (hayInterseccion(x1, y1, x2, y2, inicio.getX(), inicio.getY(), fin.getX(), fin.getY())) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Verifica si dos segmentos de línea se intersectan
     */
    private boolean hayInterseccion(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        // Calculamos orientación de los puntos
        int o1 = orientacion(x1, y1, x2, y2, x3, y3);
        int o2 = orientacion(x1, y1, x2, y2, x4, y4);
        int o3 = orientacion(x3, y3, x4, y4, x1, y1);
        int o4 = orientacion(x3, y3, x4, y4, x2, y2);
        
        // Caso general: orientaciones diferentes
        if (o1 != o2 && o3 != o4) {
            return true;
        }
        
        // Casos especiales para colinealidad
        if (o1 == 0 && estaPuntoEnSegmento(x3, y3, x1, y1, x2, y2)) return true;
        if (o2 == 0 && estaPuntoEnSegmento(x4, y4, x1, y1, x2, y2)) return true;
        if (o3 == 0 && estaPuntoEnSegmento(x1, y1, x3, y3, x4, y4)) return true;
        if (o4 == 0 && estaPuntoEnSegmento(x2, y2, x3, y3, x4, y4)) return true;
        
        return false;
    }
    
    /**
     * Calcula la orientación de tres puntos ordenados
     * Retorna:
     * 0 --> Colineales
     * 1 --> Sentido horario
     * 2 --> Sentido antihorario
     */
    private int orientacion(double x1, double y1, double x2, double y2, double x3, double y3) {
        double val = (y2 - y1) * (x3 - x2) - (x2 - x1) * (y3 - y2);
        
        if (val == 0) return 0;  // Colineal
        return (val > 0) ? 1 : 2; // Sentido horario o antihorario
    }
    
    /**
     * Verifica si un punto está dentro de un segmento de línea
     */
    private boolean estaPuntoEnSegmento(double x, double y, double x1, double y1, double x2, double y2) {
        return x >= Math.min(x1, x2) && x <= Math.max(x1, x2) &&
               y >= Math.min(y1, y2) && y <= Math.max(y1, y2);
    }
    
    /**
     * Obtiene los bloqueos activos en un momento dado
     */
    public List<Bloqueo> obtenerBloqueosActivos(LocalDateTime momento) {
        return bloqueoRepository.findByFechaInicioBeforeAndFechaFinAfter(momento, momento);
    }
    
    /**
     * Actualiza el estado activo de todos los bloqueos según el momento actual
     */
    public void actualizarEstadoBloqueos() {
        LocalDateTime ahora = LocalDateTime.now();
        List<Bloqueo> todosBloqueos = bloqueoRepository.findAll();
        
        for (Bloqueo bloqueo : todosBloqueos) {
            boolean debeEstarActivo = ahora.isAfter(bloqueo.getFechaInicio()) && 
                                     ahora.isBefore(bloqueo.getFechaFin());
            
            if (bloqueo.isActivo() != debeEstarActivo) {
                bloqueo.setActivo(debeEstarActivo);
                bloqueoRepository.save(bloqueo);
            }
        }
    }
}