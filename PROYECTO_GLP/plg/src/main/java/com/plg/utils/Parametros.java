package com.plg.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Parametros {
    public String mes = "04";
    public DateTimeFormatter formatoFechaHora = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    public LocalDateTime fecha_inicial = LocalDateTime.parse("29/"+mes+"/2025 00:00", formatoFechaHora);

    
    private static Parametros instance;

    public static Parametros getInstance( ) {
        if (instance == null) {
            instance = new Parametros();
        }
        return instance;
    }
}
