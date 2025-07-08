package com.plg.utils.simulacion;

import java.time.LocalDateTime;

import com.plg.dto.IndividuoDto;
import com.plg.utils.Simulacion;

/**
 * Clase para manejar los paquetes de simulación para el frontend.
 * Proporciona funcionalidad para obtener, reiniciar y eliminar paquetes
 * del historial de simulación.
 */
public class PaqueteManager {

    /**
     * Obtiene el siguiente paquete de la simulación para el frontend.
     * Cada llamada devuelve el siguiente paso en secuencia.
     * 
     * @return Paquete con datos del siguiente paso de la simulación
     */
    public static IndividuoDto obtenerSiguientePaquete() {
        return GestorHistorialSimulacion.obtenerSiguientePaquete();
    }

    /**
     * Reinicia la reproducción desde el inicio para el frontend.
     * Permite volver a reproducir la simulación desde el primer paquete.
     */
    public static void reiniciarReproduccion() {
        GestorHistorialSimulacion.reiniciarReproduccion();
    }

    /**
     * Elimina todos los paquetes futuros mantiendo solo el paquete actual.
     * Se utiliza cuando ocurre una avería para detener la simulación futura.
     * 
     * @return Número de paquetes eliminados
     */
    public static int eliminarPaquetesFuturos() {
        return GestorHistorialSimulacion.eliminarPaquetesFuturos();
    }

    /**
     * Obtiene información del estado actual de la simulación.
     * 
     * @return Información completa del estado de la simulación incluyendo total de
     *         paquetes, paquete actual, etc.
     */
    public static SimulacionInfo obtenerInfoSimulacion() {
        return new SimulacionInfo(
                GestorHistorialSimulacion.getTotalPaquetes(),
                GestorHistorialSimulacion.getPaqueteActual(),
                GestorHistorialSimulacion.isEnProceso(),
                Simulacion.getFechaActual());
    }

    /**
     * Clase auxiliar para información de la simulación
     */
    public static class SimulacionInfo {
        public final int totalPaquetes;
        public final int paqueteActual;
        public final boolean enProceso;
        public final LocalDateTime tiempoActual;

        public SimulacionInfo(int totalPaquetes, int paqueteActual, boolean enProceso, LocalDateTime tiempoActual) {
            this.totalPaquetes = totalPaquetes;
            this.paqueteActual = paqueteActual;
            this.enProceso = enProceso;
            this.tiempoActual = tiempoActual;
        }
    }
}