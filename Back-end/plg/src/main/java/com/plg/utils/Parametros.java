package com.plg.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Parametros {

    public static String dia = "01";
    public static String mes = "02";
    public static String anho = "2025";
    public static DateTimeFormatter formatoFechaHora = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    public static LocalDateTime fecha_inicial = LocalDateTime.parse(dia + "/" + mes + "/" + anho + " 00:00",
            formatoFechaHora);
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

    /**
     * Actualiza la fecha inicial de la simulación y recalcula los parámetros dependientes.
     * 
     * @param nuevaFecha Nueva fecha inicial para la simulación
     */
    public static void setFechaInicial(LocalDateTime nuevaFecha) {
        fecha_inicial = nuevaFecha;
        
        // Actualizar día, mes y año para mantener compatibilidad
        dia = String.format("%02d", nuevaFecha.getDayOfMonth());
        mes = String.format("%02d", nuevaFecha.getMonthValue());
        anho = String.valueOf(nuevaFecha.getYear());
        
        System.out.println("📅 Parámetros actualizados - Fecha inicial: " + fecha_inicial);
        System.out.println("📅 Día: " + dia + ", Mes: " + mes + ", Año: " + anho);
    }

    /**
     * Obtiene la fecha inicial actual de la simulación.
     * 
     * @return Fecha inicial de la simulación
     */
    public static LocalDateTime getFechaInicial() {
        return fecha_inicial;
    }
}
