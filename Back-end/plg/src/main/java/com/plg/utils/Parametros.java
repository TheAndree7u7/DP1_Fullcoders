package com.plg.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.plg.config.DataLoader;

public class Parametros {

    public static String dia = "01";
    public static String mes = "02";
    public static String anho = "2025";
    public static DateTimeFormatter formatoFechaHora = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    public static LocalDateTime fecha_inicial = LocalDateTime.parse(dia + "/" + mes + "/" + anho + " 00:00",
            formatoFechaHora);
    public static int intervaloTiempo = 120; // minutos
    public static int diferenciaTiempoMinRequest = 0; // Es la diferencia entre request
    private static Parametros instance;
    public static int contadorPrueba = 0;
    public static double kilometrosRecorridos = 0;
    public static double fitnessGlobal = 0;
    public static DataLoader dataLoader;
    public static TipoDeSimulacion tipoDeSimulacion = TipoDeSimulacion.SEMANAL;
    public static LocalDateTime fecha_inicio_simulacion;

    public static int cantNodosEnPedidos =  10; // Cantidad de nodos generados extra por un pedido
    public static double diferenciaParaPedidoEntregado = 0.5; // Diferencia para considerar un pedido como entregado

    public static double velocidadCamion = 70.0; // Velocidad promedio de los camiones en km/h

    public static Parametros getInstance() {
        if (instance == null) {
            instance = new Parametros();
        }
        return instance;
    }

    public static void actualizarParametrosGlobales(LocalDateTime fechaInicio) {
        // Extraer año, mes y día de la fecha de inicio
        Parametros.anho = String.valueOf(fechaInicio.getYear());
        Parametros.mes = String.format("%02d", fechaInicio.getMonthValue());
        Parametros.dia = String.format("%02d", fechaInicio.getDayOfMonth());

        // Actualizar fecha_inicial en Parametros
        Parametros.fecha_inicial = fechaInicio;
        System.out.println("📅 Parámetros actualizados:");
        System.out.println("   • Año: " + Parametros.anho);
        System.out.println("   • Mes: " + Parametros.mes);
        System.out.println("   • Día: " + Parametros.dia);
    }

}