package com.plg.service;

import com.plg.config.MapaConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para realizar conversiones de formatos de archivos
 */
@Service
public class ConversionArchivoService {

    @Autowired
    private MapaConfig mapaConfig;
    
    /**
     * Convierte el archivo de bloqueos del formato antiguo al nuevo formato
     * Compatible con mapa reticular
     * 
     * @param anio Año del archivo
     * @param mes Mes del archivo
     * @return true si la conversión fue exitosa
     */
    public boolean convertirArchivoBloqueos(int anio, int mes) {
        // Formato del nombre del archivo: aaaamm.bloqueadas
        String nombreArchivo = String.format("%04d%02d.bloqueadas", anio, mes);
        Path rutaArchivo = Paths.get("src/main/resources/data/bloqueos/", nombreArchivo);
        
        if (!Files.exists(rutaArchivo)) {
            System.out.println("Archivo de bloqueos no encontrado: " + nombreArchivo);
            return false;
        }
        
        // Ruta para el nuevo archivo
        Path rutaNuevoArchivo = Paths.get("src/main/resources/data/bloqueos/", 
                                         String.format("%04d%02d.bloqueadas.nuevo", anio, mes));
        
        try {
            List<String> lineasAntiguas = Files.readAllLines(rutaArchivo);
            List<String> lineasNuevas = new ArrayList<>();
            
            for (String linea : lineasAntiguas) {
                if (linea.trim().isEmpty()) {
                    lineasNuevas.add("");
                    continue;
                }
                
                // Si la línea ya está en el nuevo formato, copiarla tal cual
                if (linea.contains("-") && linea.contains(":")) {
                    lineasNuevas.add(linea);
                    continue;
                }
                
                // Convertir del formato antiguo al nuevo
                String lineaNueva = convertirLineaFormatoAntiguo(linea);
                lineasNuevas.add(lineaNueva);
            }
            
            // Escribir al nuevo archivo
            Files.write(rutaNuevoArchivo, lineasNuevas, StandardOpenOption.CREATE, 
                       StandardOpenOption.TRUNCATE_EXISTING);
            
            System.out.println("Archivo convertido exitosamente: " + rutaNuevoArchivo);
            return true;
            
        } catch (IOException e) {
            System.err.println("Error al convertir archivo: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Convierte una línea del formato antiguo al nuevo formato
     * Formato antiguo: x1,y1,x2,y2,fechaInicio,fechaFin,descripcion
     * Formato nuevo: ##d##h##m-##d##h##m:x1,y1,x2,y2
     */
    private String convertirLineaFormatoAntiguo(String linea) {
        String[] partes = linea.split(",");
        if (partes.length < 7) {
            throw new IllegalArgumentException("Formato de línea antiguo inválido: " + linea);
        }
        
        int x1 = Integer.parseInt(partes[0]);
        int y1 = Integer.parseInt(partes[1]);
        int x2 = Integer.parseInt(partes[2]);
        int y2 = Integer.parseInt(partes[3]);
        
        // Verificar que las coordenadas estén dentro del mapa reticular
        ajustarCoordenadasALimites(x1, y1);
        ajustarCoordenadasALimites(x2, y2);
        
        // Parsear fechas
        LocalDate fechaInicio = LocalDate.parse(partes[4]);
        LocalDate fechaFin = LocalDate.parse(partes[5]);
        
        // Convertir a formato nuevo (asumimos hora inicial 06:00 y hora final 18:00)
        LocalDateTime fechaInicioConHora = fechaInicio.atTime(6, 0);
        LocalDateTime fechaFinConHora = fechaFin.atTime(18, 0);
        
        String tiempoInicio = String.format("%02dd%02dh%02dm", 
            fechaInicioConHora.getDayOfMonth(), fechaInicioConHora.getHour(), fechaInicioConHora.getMinute());
        
        String tiempoFin = String.format("%02dd%02dh%02dm", 
            fechaFinConHora.getDayOfMonth(), fechaFinConHora.getHour(), fechaFinConHora.getMinute());
        
        // Verificar si es un tramo horizontal o vertical
        // Si es diagonal, ajustarlo a dos tramos (horizontal y vertical)
        String coordenadas;
        if (x1 == x2 || y1 == y2) {
            // Tramo ya es horizontal o vertical, formato válido
            coordenadas = String.format("%d,%d,%d,%d", x1, y1, x2, y2);
        } else {
            // Tramo diagonal, convertir a dos tramos (horizontal + vertical)
            coordenadas = String.format("%d,%d,%d,%d,%d,%d", x1, y1, x1, y2, x2, y2);
        }
        
        return tiempoInicio + "-" + tiempoFin + ":" + coordenadas;
    }
    
    /**
     * Ajusta las coordenadas para que estén dentro de los límites del mapa
     */
    private double[] ajustarCoordenadasALimites(double x, double y) {
        double xAjustado = Math.max(mapaConfig.getOrigenX(), 
                       Math.min(x, mapaConfig.getOrigenX() + mapaConfig.getLargo()));
        
                       double yAjustado = Math.max(mapaConfig.getOrigenY(), 
                       Math.min(y, mapaConfig.getOrigenY() + mapaConfig.getAncho()));
        
        return new double[]{xAjustado, yAjustado};
    }
}