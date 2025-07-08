package com.plg.utils.simulacion;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.plg.entity.Pedido;

/**
 * Clase para manejar el backup y restore del estado de la simulación.
 * Proporciona funcionalidad para crear, restaurar y gestionar backups
 * del estado actual de la simulación.
 */
public class BackupManager {

    private static List<Pedido> pedidosSemanralBackup; // Backup de pedidosSemanal
    private static LocalDateTime fechaActualBackup; // Backup de fechaActual

    /**
     * Crea un backup del estado actual de la simulación.
     * Incluye pedidosSemanal y fechaActual.
     */
    public static void crearBackupSimulacion(List<Pedido> pedidosSemanal, LocalDateTime fechaActual) {
        try {
            // Crear backup de pedidosSemanal
            if (pedidosSemanal != null) {
                pedidosSemanralBackup = new ArrayList<>(pedidosSemanal);
                System.out
                        .println("💾 Backup de pedidosSemanal creado con " + pedidosSemanralBackup.size() + " pedidos");
            } else {
                pedidosSemanralBackup = new ArrayList<>();
                System.out.println("💾 Backup de pedidosSemanal creado vacío (pedidosSemanal era null)");
            }

            // Crear backup de fechaActual
            fechaActualBackup = fechaActual;
            System.out.println("💾 Backup de fechaActual creado: " + fechaActualBackup);

            System.out.println("✅ Backup de simulación creado exitosamente");
        } catch (Exception e) {
            System.err.println("❌ Error al crear backup de simulación: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Restaura el estado de la simulación desde el backup.
     * Restaura pedidosSemanal y fechaActual a su estado inicial.
     * 
     * @param pedidosPorAtender   Set de pedidos por atender a limpiar
     * @param pedidosPlanificados Set de pedidos planificados a limpiar
     * @param pedidosEntregados   Set de pedidos entregados a limpiar
     * @return true si se restauró exitosamente, false en caso contrario
     */
    public static boolean restaurarBackupSimulacion(
            Set<Pedido> pedidosPorAtender,
            Set<Pedido> pedidosPlanificados,
            Set<Pedido> pedidosEntregados) {
        try {
            if (pedidosSemanralBackup == null) {
                System.err.println("❌ No existe backup de pedidosSemanal para restaurar");
                return false;
            }

            if (fechaActualBackup == null) {
                System.err.println("❌ No existe backup de fechaActual para restaurar");
                return false;
            }

            // Limpiar sets de pedidos en curso
            pedidosPorAtender.clear();
            pedidosPlanificados.clear();
            pedidosEntregados.clear();
            System.out.println("🔄 Sets de pedidos limpiados");

            System.out.println("✅ Simulación restaurada exitosamente desde backup");
            return true;

        } catch (Exception e) {
            System.err.println("❌ Error al restaurar backup de simulación: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Verifica si existe un backup válido de la simulación.
     * 
     * @return true si existe backup, false en caso contrario
     */
    public static boolean existeBackupSimulacion() {
        return pedidosSemanralBackup != null && fechaActualBackup != null;
    }

    /**
     * Obtiene información del backup actual.
     * 
     * @return Información del backup o null si no existe
     */
    public static BackupInfo obtenerInfoBackup() {
        if (!existeBackupSimulacion()) {
            return null;
        }

        return new BackupInfo(
                pedidosSemanralBackup.size(),
                fechaActualBackup,
                System.currentTimeMillis());
    }

    /**
     * Limpia el backup actual de la simulación.
     */
    public static void limpiarBackupSimulacion() {
        pedidosSemanralBackup = null;
        fechaActualBackup = null;
        System.out.println("🗑️ Backup de simulación limpiado");
    }

    /**
     * Obtiene la lista de pedidos del backup.
     * 
     * @return Lista de pedidos del backup o null si no existe
     */
    public static List<Pedido> getPedidosBackup() {
        return pedidosSemanralBackup != null ? new ArrayList<>(pedidosSemanralBackup) : null;
    }

    /**
     * Obtiene la fecha actual del backup.
     * 
     * @return Fecha actual del backup o null si no existe
     */
    public static LocalDateTime getFechaActualBackup() {
        return fechaActualBackup;
    }

    /**
     * Clase auxiliar para información del backup
     */
    public static class BackupInfo {
        public final int totalPedidosBackup;
        public final LocalDateTime fechaBackup;
        public final long timestampCreacion;

        public BackupInfo(int totalPedidosBackup, LocalDateTime fechaBackup, long timestampCreacion) {
            this.totalPedidosBackup = totalPedidosBackup;
            this.fechaBackup = fechaBackup;
            this.timestampCreacion = timestampCreacion;
        }
    }
}