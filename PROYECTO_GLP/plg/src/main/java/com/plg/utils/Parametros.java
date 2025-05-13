package com.plg.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Parametros {

    public static String dia = "01";
    public static String mes = "01";
    public static String anho = "2025";
    public static DateTimeFormatter formatoFechaHora = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    public static LocalDateTime fecha_inicial = LocalDateTime.parse(dia + "/" + mes + "/" + anho + " 00:00", formatoFechaHora);
    public static int intervaloTiempo = 10; // minutos
    private static Parametros instance;

    public static Parametros getInstance( ) {
        if (instance == null) {
            instance = new Parametros();
        }
        return instance;
    }
}
