package com.plg.utils.simulacion;

import java.time.LocalDateTime;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.plg.entity.Pedido;
import com.plg.utils.Parametros;
import com.plg.utils.Simulacion;

/**
 * Funciones utilitarias repetidas dentro de la simulación.
 */
public class UtilesSimulacion {

    /**
     * Devuelve una lista con la unión de dos colecciones de pedidos sin repetidos.
     */
    public static List<Pedido> unirPedidosSinRepetidos(Set<Pedido> set1, Set<Pedido> set2) {
        List<Pedido> resultado = new ArrayList<>(set1);
        for (Pedido p : set2) {
            if (!resultado.contains(p)) {
                resultado.add(p);
            }
        }
        return resultado;
    }

    /**
     * Comprueba si la fechaRegistro de un pedido es anterior o igual a la
     * fechaActual.
     */
    public static boolean pedidoConFechaMenorAFechaActual(Pedido pedido, LocalDateTime fechaActual) {
        return pedido.getFechaRegistro().isBefore(fechaActual) || pedido.getFechaRegistro().isEqual(fechaActual);
    }

    /**
     * Compruva si la feha de registro de un pedido esta en el rango de fechas de la
     * ventana de tiempo actual la cual es fechaActual hasta fechaActual +
     * intervaloTiempo
     * 
     */
    public static boolean pedidoConFechaEnRango(Pedido pedido, LocalDateTime fechaActual) {
        return pedido.getFechaRegistro().isAfter(fechaActual)
                && pedido.getFechaRegistro().isBefore(fechaActual.plusMinutes(Parametros.intervaloTiempo));
    }

    /**
     * Calcula en qué iteración de la simulación se encuentra una fecha específica,
     * basándose en la fecha de inicio de la simulación y el intervalo en minutos.
     * 
     * Ejemplo:
     * - Fecha de inicio: 1 de enero de 2025 a las 00:00 horas
     * - Fecha actual: 1 de enero de 2025 a las 04:00 horas
     * - Intervalo de tiempo: 120 minutos (2 horas)
     * 
     * Cálculo:
     * 1. Diferencia en minutos: 240 minutos
     * 2. Número de iteración: 240 / 120 = 2
     * 
     * Resultado: Iteración 2 (considerando la primera iteración como 0)
     * 
     * @param fechaActual Fecha para la cual se desea calcular la iteración
     * @param fechaInicio Fecha de inicio de la simulación
     * @return Número de iteración (comenzando desde 0 para la primera iteración)
     */
    public static int calcularNumeroIteracion(LocalDateTime fechaActual, LocalDateTime fechaInicio) {
        if (fechaActual.isBefore(fechaInicio)) {
            return -1; // La fecha es anterior al inicio de la simulación
        }

        // Calcular la diferencia en minutos entre las dos fechas
        long diferenciaMinutos = ChronoUnit.MINUTES.between(fechaInicio, fechaActual);

        // Dividir por el intervalo para obtener el número de iteración
        int numeroIteracion = (int) (diferenciaMinutos / Parametros.intervaloTiempo);

        return numeroIteracion;
    }

    /**
     * Devuelve la feha inicio y fin de un paquete segun una fecha que pertenece a
     * ese paquete en base a la fecha de inicio de la simulacion va iterando hasta
     * llegar a ese numero de iteracion que pertenece
     */
    public static LocalDateTime calcularFechaInicioDelPaqueteQuePertenece(LocalDateTime fechaPaquete) {
        int numeroIteracion = calcularNumeroIteracion(fechaPaquete, Parametros.fecha_inicial);
        LocalDateTime fechaInicioPaquete = calcularFechaDeIteracion(numeroIteracion, Parametros.fecha_inicial);

        return fechaInicioPaquete;
    }

    public static LocalDateTime calcularFechaFinDelPaqueteQuePertenece(LocalDateTime fechaPaquete) {
        int numeroIteracion = calcularNumeroIteracion(fechaPaquete, Parametros.fecha_inicial);
        LocalDateTime fechaFinPaquete = calcularFechaDeIteracion(numeroIteracion + 1, Parametros.fecha_inicial);
        return fechaFinPaquete;
    }

    /**
     * Calcula la fecha en la que ocurre una iteración específica de la simulación.
     * 
     * @param numeroIteracion Número de iteración para la cual se desea calcular la
     *                        fecha
     * @param fechaInicio     Fecha de inicio de la simulación
     * @return Fecha correspondiente a la iteración especificada
     */
    public static LocalDateTime calcularFechaDeIteracion(int numeroIteracion, LocalDateTime fechaInicio) {
        if (numeroIteracion < 0) {
            throw new IllegalArgumentException("El número de iteración debe ser mayor o igual a 0");
        }

        // Calcular la fecha sumando el número de iteraciones por el intervalo en
        // minutos
        return fechaInicio.plusMinutes((long) numeroIteracion * Parametros.intervaloTiempo);
    }

    /**
     * Calcula el tiempo restante hasta la siguiente iteración.
     * 
     * @param fechaActual Fecha actual
     * @param fechaInicio Fecha de inicio de la simulación
     * @return Minutos restantes hasta la próxima iteración
     */
    public static int minutosHastaProximaIteracion(LocalDateTime fechaActual, LocalDateTime fechaInicio) {
        // Obtener el número de iteración actual
        int iteracionActual = calcularNumeroIteracion(fechaActual, fechaInicio);

        // Calcular la fecha de la siguiente iteración
        LocalDateTime fechaSiguienteIteracion = calcularFechaDeIteracion(iteracionActual + 1, fechaInicio);

        // Calcular los minutos restantes
        return (int) Duration.between(fechaActual, fechaSiguienteIteracion).toMinutes();
    }
}