package com.plg.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Herramientas {
    // Método genérico para leer todas las líneas de un archivo de recursos
    public static List<String> readAllLines(String resourcePath) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Herramientas.class.getClassLoader().getResourceAsStream(resourcePath)))) {
            return reader.lines().collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    // Método genérico para leer fechas del siguiente formato ##d##h##m
    public static LocalDateTime readFecha(String fecha) {
        String[] partes = fecha.split("[dhm]");
        Long minutosAcumulados = (Integer.parseInt(partes[0]) - 1) * 24 * 60L +
                Integer.parseInt(partes[1]) * 60L +
                Integer.parseInt(partes[2]);
        return Parametros.getInstance().fecha_inicial.plusMinutes(minutosAcumulados);
    }

    public static LocalDateTime fechaNameArchivo(String file_name){
        String mes = file_name.substring(10, 12);
        String anho = file_name.substring(6, 10);
        DateTimeFormatter formatoFechaHora = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return LocalDateTime.parse("01/"+mes+"/"+anho+" 00:00", formatoFechaHora);
    }

}
