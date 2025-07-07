package com.plg.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Parametros {

    public static String dia = "01";
    public static String mes = "02";
    public static String anho = "2025";
    public static DateTimeFormatter formatoFechaHora = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    public static LocalDateTime fecha_inicial = LocalDateTime.parse(dia + "/" + mes + "/" + anho + " 00:00",
            formatoFechaHora);
    public static LocalDateTime fecha_final;
    public static int intervaloTiempo = 120; // !minutos
    private static Parametros instance;
    public static int contadorPrueba = 0;
    public static double kilometrosRecorridos = 0;
    public static double fitnessGlobal = 0;
    public static long semillaAleatoria = 12345L;

    public static Parametros getInstance() {
        if (instance == null) {
            instance = new Parametros();
        }
        return instance;
    }
}
