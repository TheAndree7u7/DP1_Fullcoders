package com.plg.utils.simulacion;

import java.time.LocalDateTime;
import java.util.List;

import com.plg.entity.Camion;

/**
 * Administrador de mantenimientos. La lógica completa se migrará desde
 * Simulacion en una fase posterior. Por ahora se deja como stub para mantener
 * la compilación.
 */
public class MantenimientoManager {

    /**
     * Verifica y actualiza los estados de los camiones según mantenimientos
     * preventivos programados.
     *
     * Se debe invocar al inicio de cada día (00:00). Si se llama en otro horario
     * simplemente retorna sin hacer nada.
     */
    public static void verificarYActualizarMantenimientos(List<Camion> camiones, LocalDateTime fechaActual) {
        // Asegurarnos de que sólo se ejecute al inicio del día
        if (fechaActual.getHour() != 0 || fechaActual.getMinute() != 0) {
            return;
        }

        // System.out.println("🔧 Verificando mantenimientos programados para: " + fechaActual.toLocalDate()
        //         + " - INICIO DEL DÍA");

        if (camiones == null) {
            // System.out.println("[LOG] La lista de camiones es NULL");
            return;
        }
        if (camiones.isEmpty()) {
            // System.out.println("[LOG] La lista de camiones está VACÍA");
        }

        int dia = fechaActual.getDayOfMonth();
        int mes = fechaActual.getMonthValue();

        for (Camion camion : camiones) {
            if (camion == null) {
                // System.out.println("[LOG] Camión NULL encontrado en la lista");
                continue;
            }

            boolean tieneMantenimiento = tieneMantenimientoProgramado(camion, dia, mes);

            if (tieneMantenimiento) {
                camion.setEstado(com.plg.entity.EstadoCamion.EN_MANTENIMIENTO_PREVENTIVO);
                // System.out.println("   • Camión " + camion.getCodigo() + " → EN MANTENIMIENTO");
            } else {
                if (camion.getEstado() == com.plg.entity.EstadoCamion.EN_MANTENIMIENTO_PREVENTIVO) {
                    camion.setEstado(com.plg.entity.EstadoCamion.DISPONIBLE);
                    // System.out.println(
                    //         "   • Camión " + camion.getCodigo() + " → DISPONIBLE (fin mantenimiento)");
                }
            }
        }
    }

    /**
     * Determina si el camión tiene un mantenimiento programado para la fecha
     * (día, mes) indicada.
     */
    private static boolean tieneMantenimientoProgramado(Camion camion, int dia, int mes) {
        try {
            if (camion == null) {
                // System.out.println("[LOG] tieneMantenimientoProgramado: Camión es NULL");
                return false;
            }

            // Buscar el primer mantenimiento registrado para este camión
            return com.plg.utils.Parametros.dataLoader.mantenimientos.stream()
                    .filter(m -> m.getCamion() != null && m.getCamion().getCodigo().equals(camion.getCodigo()))
                    .findFirst()
                    .map(primerMantenimiento -> {
                        // Verificar que el día coincida
                        if (primerMantenimiento.getDia() != dia) {
                            return false;
                        }

                        // Calcular ciclos bimestrales: mantenimiento cada 2 meses a partir del mes
                        int mesInicial = primerMantenimiento.getMes();
                        int diferenciaMeses = Math.abs(mes - mesInicial);
                        return diferenciaMeses % 2 == 0; // se mantiene el ciclo bimestral
                    })
                    .orElse(false);
        } catch (Exception e) {
            // System.err.println("Error verificando mantenimiento para "
            //         + (camion != null ? camion.getCodigo() : "null") + ": " + e.getMessage());
            return false;
        }
    }
} 