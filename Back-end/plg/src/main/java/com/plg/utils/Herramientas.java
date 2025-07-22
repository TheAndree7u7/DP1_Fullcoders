package com.plg.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.plg.utils.ExcepcionesPerzonalizadas.ResourceNotFoundException;

public class Herramientas {
    // Método genérico para leer todas las líneas de un archivo de recursos
    public static List<String> readAllLines(String resourcePath) throws IOException {
        if (resourcePath == null || resourcePath.isEmpty()) {
            throw new IllegalArgumentException("La ruta del recurso no puede ser nula o vacía.");
        }
        try (InputStream inputStream = Herramientas.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new ResourceNotFoundException("El archivo de recurso no se encontró en la ruta: " + resourcePath);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                return reader.lines().collect(Collectors.toList());
            }
        } catch (IOException e) {
            throw new IOException("Error al leer el archivo de recurso: " + resourcePath, e);
        }
    }

    // Método genérico para leer fechas del siguiente formato ##d##h##m
    public static LocalDateTime readFecha(String fecha) {
        String[] partes = fecha.split("[dhm]");
        DateTimeFormatter formatoFechaHora = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String anho = Parametros.anho;
        String mes = Parametros.mes;
        String dia = partes[0];
        String hora = partes[1];
        String minutos = partes[2];

        LocalDateTime fechaInicial = LocalDateTime.parse(dia + "/" + mes + "/" + anho + " " + hora + ":" + minutos,
                formatoFechaHora);

        return fechaInicial;
    }

    public static LocalDateTime fechaNameArchivo(String file_name) {
        String mes = file_name.substring(10, 12);
        String anho = file_name.substring(6, 10);
        DateTimeFormatter formatoFechaHora = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return LocalDateTime.parse("01/" + mes + "/" + anho + " 00:00", formatoFechaHora);
    }

    // !DETECTA EN QUE TURNO SE ENCUENTRA UNA FECHA (son 3 turnos) de 00:00 a 08:00,
    // 08:00 a 16:00, 16:00 a 00:00
    // ! RETORNA 1, 2 O 3   
    public static int detectarTurno(LocalDateTime fecha) {
        LocalTime hora = fecha.toLocalTime();
        if (hora.isBefore(LocalTime.of(8, 0))) {
            return 1;
        } else if (hora.isBefore(LocalTime.of(16, 0))) {
            return 2;
        } else {
            return 3;
        }
    }
}
