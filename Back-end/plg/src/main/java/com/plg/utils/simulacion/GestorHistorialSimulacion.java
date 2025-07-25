package com.plg.utils.simulacion;

import java.util.ArrayList;
import java.util.List;

import com.plg.dto.IndividuoDto;

/**
 * Gestiona el historial de paquetes generados por la simulación y proporciona
 * utilidades para el frontend.
 */
public class GestorHistorialSimulacion {

    private static final List<IndividuoDto> historialSimulacion = new ArrayList<>();
    private static int indiceActualFrontend = 0;
    private static int contadorPaquetes = 0;
    private static boolean simulacionEnProceso = false;

    /* --------------------------- MARCA DE ESTADO --------------------------- */
    public static synchronized void setEnProceso(boolean enProceso) {
        simulacionEnProceso = enProceso;
    }

    public static synchronized boolean isEnProceso() {
        return simulacionEnProceso;
    }

    /* ------------------------ OPERACIONES DE HISTORIAL --------------------- */

    public static synchronized void agregarPaquete(IndividuoDto paquete) {
        contadorPaquetes++;
        historialSimulacion.add(paquete);
        System.out.println("📦 PAQUETE AGREGADO #" + contadorPaquetes + " | Tiempo: "
                + paquete.getFechaHoraSimulacion() + " | Pedidos: " + paquete.getPedidos().size());
    }

    public static synchronized IndividuoDto obtenerSiguientePaquete() {
        if (indiceActualFrontend < historialSimulacion.size()) {
            IndividuoDto paquete = historialSimulacion.get(indiceActualFrontend);
            indiceActualFrontend++;
            System.out.println("🔥 PAQUETE CONSUMIDO #" + indiceActualFrontend + " | Tiempo: "
                    + paquete.getFechaHoraSimulacion() + " | Total disponibles: " + historialSimulacion.size());
            return paquete;
        }
        return null;
    }

    public static synchronized void reiniciarReproduccion() {
        int total = historialSimulacion.size();
        indiceActualFrontend = 0;
        System.out.println("🔄 REPRODUCCIÓN REINICIADA | Volviendo al paquete #1 | Total disponibles: " + total);
    }

    public static synchronized void limpiarHistorialCompleto() {
        int paquetesEliminados = historialSimulacion.size();
        historialSimulacion.clear();
        indiceActualFrontend = 0;
        contadorPaquetes = 0;
        simulacionEnProceso = false;
        System.out.println("🧹 HISTORIAL LIMPIADO COMPLETAMENTE | Paquetes eliminados: " + paquetesEliminados + " | Estado reiniciado");
    }


    /**
     * Busca el paquete (IndividuoDto) cuya fecha de simulación coincida exactamente con la fecha dada.
     * Si no hay coincidencia exacta, retorna null.
     */
    public static synchronized IndividuoDto obtenerPaquetePorFecha(java.time.LocalDateTime fecha) {
        if (fecha == null) return null;
        for (IndividuoDto paquete : historialSimulacion) {
            if (fecha.equals(paquete.getFechaHoraSimulacion())) {
                return paquete;
            }
        }
        return null;
    }

    /* ------------------------------ GETTERS -------------------------------- */

    public static synchronized int getTotalPaquetes() {
        return historialSimulacion.size();
    }

    public static synchronized int getPaqueteActual() {
        return indiceActualFrontend;
    }

    public static synchronized int getContadorPaquetes() {
        return contadorPaquetes;
    }

    public static synchronized List<IndividuoDto> getHistorial() {
        return new ArrayList<>(historialSimulacion);
    }
} 