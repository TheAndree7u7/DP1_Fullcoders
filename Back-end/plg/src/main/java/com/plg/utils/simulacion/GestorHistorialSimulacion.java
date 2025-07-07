package com.plg.utils.simulacion;

import java.util.ArrayList;
import java.util.List;

import com.plg.dto.IndividuoDto;

import lombok.Getter;
import lombok.Setter;

/**
 * Gestiona el historial de paquetes generados por la simulación y proporciona
 * utilidades para el frontend.
 */
@Getter
@Setter
public class GestorHistorialSimulacion {

    private static final List<IndividuoDto> historialSimulacion = new ArrayList<>();
    private static int indiceActualFrontend = 0;
    private static int contadorPaquetes = 0;
    private static boolean simulacionEnProceso = false;
    private static boolean simulacionPausada = false; // Nuevo flag para pausa

    /* --------------------------- MARCA DE ESTADO --------------------------- */
    public static synchronized void setEnProceso(boolean enProceso) {
        simulacionEnProceso = enProceso;
    }

    public static synchronized boolean isEnProceso() {
        return simulacionEnProceso;
    }

    // Nuevos métodos para pausa
    public static synchronized void setPausada(boolean pausada) {
        simulacionPausada = pausada;
        System.out.println("🔄 SIMULACIÓN " + (pausada ? "PAUSADA" : "REANUDADA"));
    }

    public static synchronized boolean isPausada() {
        return simulacionPausada;
    }

    /* ------------------------ OPERACIONES DE HISTORIAL --------------------- */

    public static synchronized void agregarPaquete(IndividuoDto paquete) {
        contadorPaquetes++;
        historialSimulacion.add(paquete);
        System.out.println("📦 PAQUETE AGREGADO #" + contadorPaquetes + " | Tiempo: "
                + paquete.getFechaHoraSimulacion() + " | Pedidos: " + paquete.getPedidos().size());
    }

    /**
     * Inserta un paquete parche en una posición específica del historial.
     * Este método se usa cuando se genera un paquete de emergencia debido a una
     * avería.
     * 
     * @param paquete  El paquete parche a insertar
     * @param posicion La posición donde insertar el paquete (típicamente
     *                 paqueteActual + 1)
     */
    public static synchronized void insertarPaqueteParche(IndividuoDto paquete, int posicion) {
        // Validar que la posición sea válida
        if (posicion < 0 || posicion > historialSimulacion.size()) {
            System.err.println("❌ PAQUETE PARCHE: Posición inválida " + posicion + ", historial size: "
                    + historialSimulacion.size());
            return;
        }

        contadorPaquetes++;
        historialSimulacion.add(posicion, paquete);

        // Calcular fitness del paquete parche (por ahora usar valor fijo)
        double fitness = 0.0; // Se podría mejorar para calcular el fitness real

        // Mostrar información completa como los otros paquetes
        System.out.println("------------------------");
        System.out.println("Tiempo actual: " + paquete.getFechaHoraSimulacion());
        System.out.println("Fitness algoritmo genético (paquete parche): " + String.format("%.1f", fitness));
        System.out.println("🩹 PAQUETE PARCHE INSERTADO #" + contadorPaquetes + " en posición " + posicion +
                " | Tiempo: " + paquete.getFechaHoraSimulacion() +
                " | Pedidos: " + paquete.getPedidos().size());

        // Mostrar estado de pedidos semanales (aproximado)
        List<com.plg.entity.Pedido> pedidosSemanal = com.plg.utils.Simulacion.getPedidosSemanal();
        int pedidosRestantes = pedidosSemanal != null ? pedidosSemanal.size() : 0;

        // Por ahora usamos valores aproximados para por atender y planificados
        int porAtender = paquete.getPedidos().size();
        int planificados = 0;

        System.out.println("📊 Estado: Pedidos semanales restantes: " + pedidosRestantes +
                ", Por atender: " + porAtender +
                ", Planificados: " + planificados);

        System.out.println("📊 DESPUÉS DEL PARCHE: Total paquetes=" + historialSimulacion.size() +
                ", Posición actual frontend=" + indiceActualFrontend);
    }

    /**
     * Elimina todos los paquetes futuros después del paquete actual.
     * Mantiene solo el paquete que está siendo consumido actualmente.
     * 
     * @return Número de paquetes eliminados
     */
    public static synchronized int eliminarPaquetesFuturos() {
        int paquetesAntesDeEliminar = historialSimulacion.size();
        int paqueteActualIndex = indiceActualFrontend;

        System.out.println("🗑️ ELIMINANDO PAQUETES FUTUROS:");
        System.out.println("   • Total paquetes antes: " + paquetesAntesDeEliminar);
        System.out.println("   • Índice actual frontend (próximo a consumir): " + paqueteActualIndex);
        System.out.println(
                "   • Paquete actual siendo consumido: "
                        + (paqueteActualIndex > 0 ? (paqueteActualIndex - 1) : "ninguno"));

        if (paqueteActualIndex >= historialSimulacion.size()) {
            System.out.println("⚠️ No hay paquetes futuros para eliminar (frontend al final del historial)");
            return 0;
        }

        // Eliminar todos los paquetes después del índice actual
        // El frontend ya consumió hasta indiceActualFrontend-1,
        // así que eliminamos desde indiceActualFrontend en adelante
        int paquetesAEliminar = historialSimulacion.size() - paqueteActualIndex;

        for (int i = historialSimulacion.size() - 1; i >= paqueteActualIndex; i--) {
            historialSimulacion.remove(i);
        }

        int paquetesDespuesDeEliminar = historialSimulacion.size();

        System.out.println("✅ PAQUETES FUTUROS ELIMINADOS:");
        System.out.println("   • Paquetes eliminados: " + paquetesAEliminar);
        System.out.println("   • Total paquetes después: " + paquetesDespuesDeEliminar);
        System.out.println("   • Próximo índice a consumir: " + indiceActualFrontend);

        return paquetesAEliminar;
    }

    public static synchronized IndividuoDto obtenerSiguientePaquete() {
        if (indiceActualFrontend < historialSimulacion.size()) {
            IndividuoDto paquete = historialSimulacion.get(indiceActualFrontend);
            int paqueteConsumido = indiceActualFrontend; // Guardar el índice antes de incrementar
            indiceActualFrontend++;
            System.out.println("🔥 PAQUETE CONSUMIDO #" + paqueteConsumido + " | Tiempo: "
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
        simulacionPausada = false; // Reiniciar también el flag de pausa
        System.out.println("🧹 HISTORIAL LIMPIADO COMPLETAMENTE | Paquetes eliminados: " + paquetesEliminados
                + " | Estado reiniciado | Pausa reiniciada");
    }

    /* ------------------------------ GETTERS -------------------------------- */

    public static synchronized int getTotalPaquetes() {
        return historialSimulacion.size();
    }

    public static synchronized int getPaqueteActual() {
        // Devolver el paquete que se está consumiendo actualmente
        // Si indiceActualFrontend es 0, no se ha consumido ningún paquete aún
        // Si indiceActualFrontend es N, se han consumido N paquetes, el actual es el
        // N-1
        return Math.max(0, indiceActualFrontend - 1);
    }

    public static synchronized int getContadorPaquetes() {
        return contadorPaquetes;
    }

    public static synchronized List<IndividuoDto> getHistorial() {
        return new ArrayList<>(historialSimulacion);
    }

    /**
     * Obtiene el paquete en una posición específica sin modificar el índice actual.
     * Útil para obtener información de paquetes para generar paquetes parche.
     * 
     * @param indice La posición del paquete a obtener
     * @return El paquete en la posición especificada, o null si no existe
     */
    public static synchronized IndividuoDto obtenerPaquetePorIndice(int indice) {
        if (indice >= 0 && indice < historialSimulacion.size()) {
            return historialSimulacion.get(indice);
        }
        return null;
    }
}