package com.plg.utils.simulacion;

import java.time.LocalDateTime;
import java.util.List;

import com.plg.config.DataLoader;
import com.plg.entity.Averia;
import com.plg.entity.Camion;
import com.plg.entity.Coordenada;
import com.plg.entity.EstadoCamion;
import com.plg.entity.Pedido;

/**
 * Maneja la actualización de estados de camiones en función de averías (TI1, TI2, TI3).
 */
public class AveriasManager {

    /**
     * Actualiza los estados de los camiones involucrados en averías activas según la fecha actual.
     * Este método puede invocarse cada intervalo de simulación.
     */
    public static void actualizarCamionesEnAveria(LocalDateTime fechaActual) {
        try {
            // Crear instancias de los repositorios y servicios necesarios
            com.plg.repository.AveriaRepository averiaRepository = new com.plg.repository.AveriaRepository();
            com.plg.repository.CamionRepository camionRepository = new com.plg.repository.CamionRepository();
            com.plg.service.CamionService camionService = new com.plg.service.CamionService(camionRepository);
            com.plg.service.AveriaService averiaService = new com.plg.service.AveriaService(averiaRepository,
                    camionService);

            // Obtener todas las averías activas
            List<Averia> averiasActivas = averiaService.listarActivas();

            // 1. Procesar averías que NO requieren traslado (TI1)
            procesarAveriasNoRequierenTraslado(averiasActivas, fechaActual, camionService);

            // 2. Procesar averías que SÍ requieren traslado (TI2, TI3)
            procesarAveriasRequierenTraslado(averiasActivas, fechaActual, camionService);

        } catch (Exception e) {
            System.err.println("Error al actualizar camiones en avería: " + e.getMessage());
        }
    }

    /* -------------------- LÓGICA DE CADA TIPO DE AVERÍA -------------------- */

    /**
     * 1. Lista averías TI1 (no requieren traslado).
     * 2. Si la fechaHoraDisponible < fechaActual (ignorando segundos) → desactiva avería y pone camión DISPONIBLE.
     */
    private static void procesarAveriasNoRequierenTraslado(List<Averia> averiasActivas, LocalDateTime fechaActual,
            com.plg.service.CamionService camionService) {
        for (Averia averia : averiasActivas) {
            if (averia.getTipoIncidente() != null && !averia.getTipoIncidente().isRequiereTraslado()) {
                if (averia.getFechaHoraDisponible() != null
                        && esFechaAnteriorSinSegundos(averia.getFechaHoraDisponible(), fechaActual)) {

                    String codigoCamion = averia.getCamion().getCodigo();
                    // Desactivar avería
                    averia.setEstado(false);
                    // Camión vuelve a estar disponible
                    camionService.cambiarEstado(codigoCamion, EstadoCamion.DISPONIBLE);
                    System.out.println("✅ Camión " + codigoCamion + " recuperado de avería TI1 - Estado: DISPONIBLE");
                }
            }
        }
    }

    /**
     * 1. Procesa averías TI2/TI3 (requieren traslado).
     * 2. Si fecha fin espera en ruta < fechaActual → traslada camión a taller y lo marca EN_MANTENIMIENTO_POR_AVERIA.
     * 3. Si fecha disponible <= fechaActual → desactiva avería y pone camión DISPONIBLE.
     */
    private static void procesarAveriasRequierenTraslado(List<Averia> averiasActivas, LocalDateTime fechaActual,
            com.plg.service.CamionService camionService) {
        for (Averia averia : averiasActivas) {
            if (averia.getTipoIncidente() != null && averia.getTipoIncidente().isRequiereTraslado()) {
                String codigoCamion = averia.getCamion().getCodigo();

                // Fase 1: traslado al taller
                if (averia.getFechaHoraFinEsperaEnRuta() != null
                        && esFechaAnteriorSinSegundos(averia.getFechaHoraFinEsperaEnRuta(), fechaActual)) {

                    Camion camion = buscarCamionPorCodigo(codigoCamion);
                    if (camion != null && camion.getEstado() == EstadoCamion.INMOVILIZADO_POR_AVERIA) {
                        camionService.cambiarEstado(codigoCamion, EstadoCamion.EN_MANTENIMIENTO_POR_AVERIA);
                        Coordenada coordTaller = obtenerCoordenadaAlmacenCentral();
                        camionService.cambiarCoordenada(codigoCamion, coordTaller);
                        System.out.println("🚛 Camión " + codigoCamion
                                + " trasladado al taller - Estado: EN_MANTENIMIENTO_POR_AVERIA");
                    }
                }

                // Fase 2: fin de mantenimiento
                if (averia.getFechaHoraDisponible() != null
                        && esFechaAnteriorOIgualSinSegundos(averia.getFechaHoraDisponible(), fechaActual)) {

                    averia.setEstado(false);
                    camionService.cambiarEstado(codigoCamion, EstadoCamion.DISPONIBLE);
                    System.out.println("✅ Camión " + codigoCamion + " recuperado de avería "
                            + averia.getTipoIncidente().getCodigo() + " - Estado: DISPONIBLE");
                }
            }
        }
    }

    /* ----------------------------- UTILIDADES ----------------------------- */

    private static Camion buscarCamionPorCodigo(String codigoCamion) {
        return DataLoader.camiones.stream().filter(c -> c.getCodigo().equals(codigoCamion)).findFirst().orElse(null);
    }

    private static Coordenada obtenerCoordenadaAlmacenCentral() {
        return DataLoader.almacenes.stream().filter(a -> a.getTipo() == com.plg.entity.TipoAlmacen.CENTRAL)
                .map(a -> a.getCoordenada()).findFirst().orElse(new Coordenada(8, 12));
    }

    // Comparaciones de fecha ignorando segundos
    private static boolean esFechaAnteriorSinSegundos(LocalDateTime f1, LocalDateTime f2) {
        LocalDateTime t1 = f1.withSecond(0).withNano(0);
        LocalDateTime t2 = f2.withSecond(0).withNano(0);
        return t1.isBefore(t2);
    }

    private static boolean esFechaAnteriorOIgualSinSegundos(LocalDateTime f1, LocalDateTime f2) {
        LocalDateTime t1 = f1.withSecond(0).withNano(0);
        LocalDateTime t2 = f2.withSecond(0).withNano(0);
        return t1.isBefore(t2) || t1.isEqual(t2);
    }
} 