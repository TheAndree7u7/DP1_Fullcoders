package com.plg.utils.simulacion;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

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
    // NUEVO: Historial de paquetes ya consumidos
    private static final List<IndividuoDto> historialConsumidos = new ArrayList<>();
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
        System.out.println("📦 PAQUETE AGREGADO #" + contadorPaquetes
                + " | Tiempo: " + paquete.getFechaHoraSimulacion()
                + " | Pedidos: " + paquete.getPedidos().size()
                + " | Fecha inicio: " + paquete.getFechaHoraInicioIntervalo()
                + " | Fecha fin: " + paquete.getFechaHoraFinIntervalo());
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
        int tamanio_historial_paquetesAntesDeEliminar = historialSimulacion.size();
        int paqueteActualIndex = indiceActualFrontend;

        System.out.println(" ==================🗑️ ELIMINANDO PAQUETES FUTUROS:=========================");
        System.out.println("   • Total paquetes antes: " + tamanio_historial_paquetesAntesDeEliminar);
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

    /**
     * Elimina todos los paquetes futuros cuya fecha de inicio del individuo sea
     * superior a la fecha actual.
     * Mantiene solo los paquetes cuya fecha de inicio sea menor o igual a la fecha
     * actual.
     * 
     * @param fechaActual La fecha actual de referencia para la comparación
     * @return Número de paquetes eliminados
     */
    public static synchronized int eliminarPaquetesFuturosPorFecha(LocalDateTime fechaActual) {
        int tamanio_historial_paquetesAntesDeEliminar = historialSimulacion.size();
        int paquetesEliminados = 0;

        System.out.println(" ==================🗑️ ELIMINANDO PAQUETES FUTUROS POR FECHA:=========================");
        System.out.println("   • Total paquetes antes: " + tamanio_historial_paquetesAntesDeEliminar);
        System.out.println("   • Fecha actual de referencia: " + fechaActual);
        System.out.println("   • Índice actual frontend: " + indiceActualFrontend);

        if (historialSimulacion.isEmpty()) {
            System.out.println("⚠️ No hay paquetes en el historial para eliminar");
            return 0;
        }

        // Recorrer el historial desde el final hacia el principio para evitar problemas
        // con índices
        for (int i = historialSimulacion.size() - 1; i >= 0; i--) {
            IndividuoDto paquete = historialSimulacion.get(i);
            LocalDateTime fechaInicioPaquete = paquete.getFechaHoraInicioIntervalo();

            // boolean esFechaIgual = fechaInicioPaquete != null &&
            // fechaInicioPaquete.isEqual(fechaActual);
            boolean esFechaPosterior = fechaInicioPaquete != null && fechaInicioPaquete.isAfter(fechaActual);
            // Si la fecha de inicio del paquete es posterior a la fecha actual, eliminarlo
            if (esFechaPosterior) {
                historialSimulacion.remove(i);
                paquetesEliminados++;

                System.out.println("   • Eliminado paquete #" + i +
                        " | Fecha inicio: " + fechaInicioPaquete +
                        " | Es posterior a: " + fechaActual);

                // Si eliminamos un paquete que está antes o igual al índice actual del
                // frontend,
                // necesitamos ajustar el índice
                if (i <= indiceActualFrontend) {
                    indiceActualFrontend--;
                    System.out.println("   • Ajustado índice frontend a: " + indiceActualFrontend);
                }
            }
        }

        int paquetesDespuesDeEliminar = historialSimulacion.size();

        System.out.println("✅ PAQUETES FUTUROS POR FECHA ELIMINADOS:");
        System.out.println("   • Paquetes eliminados: " + paquetesEliminados);
        System.out.println("   • Total paquetes después: " + paquetesDespuesDeEliminar);
        System.out.println("   • Índice frontend ajustado: " + indiceActualFrontend);

        return paquetesEliminados;
    }

    public static synchronized IndividuoDto obtenerSiguientePaquete() {
        if (indiceActualFrontend < historialSimulacion.size()) {
            IndividuoDto paquete = historialSimulacion.get(indiceActualFrontend);
            int paqueteConsumido = indiceActualFrontend; // Guardar el índice antes de incrementar

            // NUEVO: Agregar el paquete al historial de consumidos ANTES de incrementar el
            // índice
            historialConsumidos.add(paquete);

            indiceActualFrontend++;
            System.out.println("🔥 PAQUETE CONSUMIDO #" + paqueteConsumido
                    + " | Hora de simulación: " + paquete.getFechaHoraSimulacion()
                    + " | Inicio intervalo: " + paquete.getFechaHoraInicioIntervalo()
                    + " | Fin intervalo: " + paquete.getFechaHoraFinIntervalo()
                    + " | Total disponibles: " + historialSimulacion.size()
                    + " | Total consumidos: " + historialConsumidos.size());
            return paquete;
        }
        return null;
    }

    public static synchronized void reiniciarReproduccion() {
        int total = historialSimulacion.size();
        indiceActualFrontend = 0;
        // NO limpiar el historial de consumidos al reiniciar - mantener el registro
        // histórico
        System.out.println("🔄 REPRODUCCIÓN REINICIADA | Volviendo al paquete #1 | Total disponibles: " + total
                + " | Historial de consumidos preservado (" + historialConsumidos.size() + " paquetes)");
    }

    public static synchronized void limpiarHistorialCompleto() {
        int paquetesEliminados = historialSimulacion.size();
        historialSimulacion.clear();
        // NUEVO: Limpiar también el historial de consumidos
        historialConsumidos.clear();
        indiceActualFrontend = 0;
        contadorPaquetes = 0;
        simulacionEnProceso = false;
        simulacionPausada = false; // Reiniciar también el flag de pausa
        System.out.println("🧹 HISTORIAL LIMPIADO COMPLETAMENTE | Paquetes eliminados: " + paquetesEliminados
                + " | Estado reiniciado | Pausa reiniciada | Historial de consumidos limpiado");
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

    /**
     * Ajusta el contador de paquetes para que coincida con el número real de
     * paquetes
     * en el historial. Se usa después de eliminar paquetes futuros para mantener
     * la numeración consecutiva.
     */
    public static synchronized void ajustarContadorPaquetes() {
        int contadorAnterior = contadorPaquetes;
        contadorPaquetes = historialSimulacion.size();

        System.out.println("🔢 AJUSTANDO CONTADOR DE PAQUETES:");
        System.out.println("   • Contador anterior: " + contadorAnterior);
        System.out.println("   • Contador ajustado: " + contadorPaquetes);
        System.out.println("   • Total paquetes en historial: " + historialSimulacion.size());
        System.out.println("   • Próximo paquete será: #" + (contadorPaquetes + 1));
    }

    /**
     * NUEVO: Limpia solo el historial de paquetes consumidos.
     * Útil cuando se quiere mantener el historial de simulación pero limpiar
     * el registro de consumo.
     */
    public static synchronized void limpiarHistorialConsumidos() {
        int consumidosEliminados = historialConsumidos.size();
        historialConsumidos.clear();
        System.out.println("🧹 HISTORIAL DE CONSUMIDOS LIMPIADO | Paquetes eliminados: " + consumidosEliminados);
    }

    /**
     * NUEVO: Obtiene estadísticas del historial de consumidos.
     * 
     * @return String con estadísticas detalladas del historial de consumidos
     */
    public static synchronized String obtenerEstadisticasConsumidos() {
        if (historialConsumidos.isEmpty()) {
            return "📊 ESTADÍSTICAS: No hay paquetes consumidos";
        }

        StringBuilder stats = new StringBuilder();
        stats.append("📊 ESTADÍSTICAS DEL HISTORIAL DE CONSUMIDOS\n");
        stats.append("==========================================\n");
        stats.append("Total paquetes consumidos: ").append(historialConsumidos.size()).append("\n");

        // Calcular estadísticas de pedidos
        int totalPedidos = 0;
        int minPedidos = Integer.MAX_VALUE;
        int maxPedidos = 0;

        for (IndividuoDto paquete : historialConsumidos) {
            int pedidosEnPaquete = paquete.getPedidos().size();
            totalPedidos += pedidosEnPaquete;
            minPedidos = Math.min(minPedidos, pedidosEnPaquete);
            maxPedidos = Math.max(maxPedidos, pedidosEnPaquete);
        }

        double promedioPedidos = (double) totalPedidos / historialConsumidos.size();

        stats.append("Total pedidos procesados: ").append(totalPedidos).append("\n");
        stats.append("Promedio pedidos por paquete: ").append(String.format("%.1f", promedioPedidos)).append("\n");
        stats.append("Mínimo pedidos en paquete: ").append(minPedidos).append("\n");
        stats.append("Máximo pedidos en paquete: ").append(maxPedidos).append("\n");

        // Rango de fechas
        if (!historialConsumidos.isEmpty()) {
            LocalDateTime primeraFecha = historialConsumidos.get(0).getFechaHoraSimulacion();
            LocalDateTime ultimaFecha = historialConsumidos.get(historialConsumidos.size() - 1)
                    .getFechaHoraSimulacion();
            stats.append("Primera fecha: ").append(primeraFecha).append("\n");
            stats.append("Última fecha: ").append(ultimaFecha).append("\n");
        }

        stats.append("==========================================");

        return stats.toString();
    }

    /*
     * ------------------------ NUEVOS MÉTODOS PARA HISTORIAL DE CONSUMIDOS
     * ---------------------
     */

    /**
     * NUEVO: Obtiene el historial completo de paquetes ya consumidos.
     * 
     * @return Lista de todos los paquetes que han sido consumidos por el frontend
     */
    public static synchronized List<IndividuoDto> getHistorialConsumidos() {
        return new ArrayList<>(historialConsumidos);
    }

    /**
     * NUEVO: Obtiene el número total de paquetes consumidos.
     * 
     * @return Número de paquetes que han sido consumidos
     */
    public static synchronized int getTotalConsumidos() {
        return historialConsumidos.size();
    }

    /**
     * NUEVO: Obtiene un paquete específico del historial de consumidos.
     * 
     * @param indice La posición del paquete en el historial de consumidos
     * @return El paquete consumido en la posición especificada, o null si no existe
     */
    public static synchronized IndividuoDto obtenerPaqueteConsumidoPorIndice(int indice) {
        if (indice >= 0 && indice < historialConsumidos.size()) {
            return historialConsumidos.get(indice);
        }
        return null;
    }

    /**
     * NUEVO: Obtiene el último paquete consumido.
     * 
     * @return El último paquete que fue consumido, o null si no hay ninguno
     */
    public static synchronized IndividuoDto obtenerUltimoPaqueteConsumido() {
        if (!historialConsumidos.isEmpty()) {
            return historialConsumidos.get(historialConsumidos.size() - 1);
        }
        return null;
    }

    /**
     * NUEVO: Obtiene información detallada del estado de consumo.
     * 
     * @return Información completa sobre paquetes disponibles y consumidos
     */
    public static synchronized String obtenerInfoConsumo() {
        return String.format("📊 ESTADO DE CONSUMO: Disponibles=%d, Consumidos=%d, Próximo=%d, Total=%d",
                historialSimulacion.size() - indiceActualFrontend,
                historialConsumidos.size(),
                indiceActualFrontend,
                historialSimulacion.size());
    }

    /**
     * NUEVO: Busca paquetes consumidos por rango de fechas.
     * 
     * @param fechaInicio Fecha de inicio del rango
     * @param fechaFin    Fecha de fin del rango
     * @return Lista de paquetes consumidos en el rango de fechas especificado
     */
    public static synchronized List<IndividuoDto> buscarPaquetesConsumidosPorFecha(LocalDateTime fechaInicio,
            LocalDateTime fechaFin) {
        List<IndividuoDto> paquetesEnRango = new ArrayList<>();

        for (IndividuoDto paquete : historialConsumidos) {
            LocalDateTime fechaPaquete = paquete.getFechaHoraSimulacion();
            if (fechaPaquete.isAfter(fechaInicio) && fechaPaquete.isBefore(fechaFin)) {
                paquetesEnRango.add(paquete);
            }
        }

        return paquetesEnRango;
    }

    /**
     * NUEVO: Exporta el historial de consumidos a un formato legible.
     * 
     * @return String con información detallada de todos los paquetes consumidos
     */
    public static synchronized String exportarHistorialConsumidos() {
        StringBuilder sb = new StringBuilder();
        sb.append("📋 HISTORIAL DE PAQUETES CONSUMIDOS\n");
        sb.append("=====================================\n");

        for (int i = 0; i < historialConsumidos.size(); i++) {
            IndividuoDto paquete = historialConsumidos.get(i);
            sb.append(String.format("#%d | Tiempo: %s | Pedidos: %d | Cromosoma: %d genes\n",
                    i + 1,
                    paquete.getFechaHoraSimulacion(),
                    paquete.getPedidos().size(),
                    paquete.getCromosoma().size()));
        }

        sb.append("=====================================\n");
        sb.append("Total consumidos: ").append(historialConsumidos.size());

        return sb.toString();
    }
}