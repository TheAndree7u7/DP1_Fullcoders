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

    /**
     * Agrega un paquete recalculado al historial debido a una avería.
     * Este método reemplaza o actualiza el paquete en la posición especificada.
     * 
     * @param paqueteRecalculado el paquete recalculado
     * @param indicePaquete el índice del paquete que se está reemplazando
     */
    public static synchronized void agregarPaqueteRecalculado(IndividuoDto paqueteRecalculado, int indicePaquete) {
        if (indicePaquete >= 0 && indicePaquete < historialSimulacion.size()) {
            // Reemplazar el paquete existente con el recalculado
            historialSimulacion.set(indicePaquete, paqueteRecalculado);
            System.out.println("🔄 PAQUETE RECALCULADO #" + (indicePaquete + 1) + " | Tiempo: "
                    + paqueteRecalculado.getFechaHoraSimulacion() + " | Pedidos: " + paqueteRecalculado.getPedidos().size());
        } else if (indicePaquete == historialSimulacion.size()) {
            // Agregar el paquete recalculado al final si es una extensión
            historialSimulacion.add(paqueteRecalculado);
            contadorPaquetes++;
            System.out.println("🔄 PAQUETE RECALCULADO AGREGADO #" + contadorPaquetes + " | Tiempo: "
                    + paqueteRecalculado.getFechaHoraSimulacion() + " | Pedidos: " + paqueteRecalculado.getPedidos().size());
        } else {
            System.err.println("❌ Índice de paquete inválido para recálculo: " + indicePaquete + 
                             " (total: " + historialSimulacion.size() + ")");
        }
    }

    /**
     * Invalida todos los paquetes posteriores al paquete especificado.
     * Esto se usa cuando ocurre una avería y los paquetes futuros ya no son válidos.
     * 
     * @param indicePaqueteActual índice del paquete actual donde ocurrió la avería
     * @return número de paquetes invalidados
     */
    public static synchronized int invalidarPaquetesFuturos(int indicePaqueteActual) {
        int paquetesInvalidados = 0;
        int tamaañoOriginal = historialSimulacion.size();
        
        // Eliminar todos los paquetes posteriores al actual
        if (indicePaqueteActual + 1 < historialSimulacion.size()) {
            paquetesInvalidados = historialSimulacion.size() - (indicePaqueteActual + 1);
            historialSimulacion.subList(indicePaqueteActual + 1, historialSimulacion.size()).clear();
            
            System.out.println("🗑️ PAQUETES INVALIDADOS: " + paquetesInvalidados + " paquetes eliminados");
            System.out.println("📦 HISTORIAL AJUSTADO: De " + tamaañoOriginal + " a " + historialSimulacion.size() + " paquetes");
        }
        
        return paquetesInvalidados;
    }

    /**
     * Agrega un paquete parche que cubre el tiempo restante del paquete actual.
     * Este paquete parche permite continuar la simulación desde el punto de avería.
     * 
     * @param paqueteParche el paquete parche con la nueva solución
     */
    public static synchronized void agregarPaqueteParche(IndividuoDto paqueteParche) {
        historialSimulacion.add(paqueteParche);
        contadorPaquetes++;
        System.out.println("🩹 PAQUETE PARCHE AGREGADO #" + contadorPaquetes + " | Tiempo: "
                + paqueteParche.getFechaHoraSimulacion() + " | Pedidos: " + paqueteParche.getPedidos().size());
        System.out.println("⏱️ Este paquete parche cubre el tiempo restante del paquete afectado por la avería");
    }

    /**
     * Obtiene el índice del paquete actual basado en el índice del frontend.
     * 
     * @return índice del paquete actual (0-based)
     */
    public static synchronized int getIndicePaqueteActual() {
        return Math.max(0, indiceActualFrontend - 1);
    }

    /**
     * Verifica si hay paquetes futuros después del paquete especificado.
     * 
     * @param indicePaquete índice del paquete a verificar
     * @return true si hay paquetes futuros, false en caso contrario
     */
    public static synchronized boolean hayPaquetesFuturos(int indicePaquete) {
        return indicePaquete + 1 < historialSimulacion.size();
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