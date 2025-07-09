package com.plg.utils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.SynchronousQueue;
import java.util.stream.Collectors;

import com.plg.config.DataLoader;
import com.plg.dto.IndividuoDto;
import com.plg.dto.request.AveriaConEstadoRequest;
import com.plg.entity.Bloqueo;
import com.plg.entity.Camion;
import com.plg.entity.Mapa;
import com.plg.entity.Pedido;
import com.plg.repository.AlmacenRepository;
import com.plg.repository.CamionRepository;
import com.plg.utils.simulacion.ConfiguracionSimulacion;
import com.plg.utils.simulacion.MantenimientoManager;
import com.plg.utils.simulacion.AveriasManager;
import com.plg.utils.simulacion.UtilesSimulacion;
import com.plg.utils.simulacion.BackupManager;
import com.plg.utils.simulacion.EstadoManager;
import com.plg.utils.simulacion.AveriaManager;
import com.plg.utils.simulacion.PaqueteManager;

import lombok.Getter;
import lombok.Setter;

import com.plg.utils.simulacion.CamionStateApplier;
import com.plg.utils.simulacion.IndividuoFactory;
import com.plg.utils.simulacion.GestorHistorialSimulacion;

/**
 * Clase principal que maneja la simulación de logística y algoritmos genéticos.
 */
@Getter
@Setter
public class Simulacion {

    private static List<Pedido> pedidosSemanal;
    private static LocalDateTime fechaActual;
    public static LocalDateTime fechaLimite;
    public static Set<Pedido> pedidosPorAtender = new LinkedHashSet<>();
    public static Set<Pedido> pedidosPlanificados = new LinkedHashSet<>();
    public static Set<Pedido> pedidosEntregados = new LinkedHashSet<>();
    public static Individuo mejorIndividuo = null;
    public static boolean faltacrearparche = false;
    // Queue de paquetes generados para el frontend
    // rango de fechas del parche
    public static LocalDateTime fechaInicioParche;
    public static LocalDateTime fechaFinParche;
    // Administrado por GestorHistorialSimulacion
    public static List<IndividuoDto> historialSimulacion = new ArrayList<>();
    public static int indiceActualFrontend = 0;
    private static boolean simulacionEnProceso = false;
    private static int contadorPaquetes = 0; // Contador secuencial de paquetes
    private static boolean simulacionTerminadaporelfront = false;
    // Modo de ejecución: true para standalone (generar paquetes continuamente)
    public static boolean modoStandalone = true;

    // Colas para simulación
    public static BlockingQueue<Object> gaTriggerQueue = new SynchronousQueue<>();
    public static BlockingQueue<IndividuoDto> gaResultQueue = new SynchronousQueue<>();
    public static Semaphore iniciar = new Semaphore(0);
    public static Semaphore continuar = new Semaphore(0);

    // repostory camion
    public static CamionRepository camionRepository = new CamionRepository();
    // repostory almacen
    public static AlmacenRepository almacenRepository = new AlmacenRepository();

    /**
     * Obtiene la lista de pedidos semanales actual.
     * 
     * @return Lista de pedidos programados para la semana
     */
    public static List<Pedido> getPedidosSemanal() {
        return pedidosSemanal;
    }

    /**
     * Establece la lista de pedidos semanales para la simulación.
     * 
     * @param pedidos Lista de pedidos a procesar durante la semana
     */
    public static void setPedidosSemanal(List<Pedido> pedidos) {
        pedidosSemanal = pedidos;
    }

    /**
     * Obtiene la fecha y hora actual de la simulación.
     * 
     * @return Fecha y hora actual en el contexto de la simulación
     */
    public static LocalDateTime getFechaActual() {
        return fechaActual;
    }

    /**
     * Establece la fecha y hora actual de la simulación.
     * 
     * @param fecha Nueva fecha y hora para la simulación
     */
    public static void setFechaActual(LocalDateTime fecha) {
        fechaActual = fecha;
    }

    /**
     * Crea un backup del estado actual de la simulación.
     * Incluye pedidosSemanal y fechaActual.
     */
    public static void crearBackupSimulacion() {
        BackupManager.crearBackupSimulacion(pedidosSemanal, fechaActual);
    }

    /**
     * Restaura el estado de la simulación desde el backup.
     * Restaura pedidosSemanal y fechaActual a su estado inicial.
     * 
     * @return true si se restauró exitosamente, false en caso contrario
     */
    public static boolean restaurarBackupSimulacion() {
        boolean resultado = BackupManager.restaurarBackupSimulacion(pedidosPorAtender, pedidosPlanificados,
                pedidosEntregados);
        if (resultado) {
            // Restaurar pedidosSemanal y fechaActual desde el backup
            List<Pedido> pedidosBackup = BackupManager.getPedidosBackup();
            LocalDateTime fechaBackup = BackupManager.getFechaActualBackup();
            if (pedidosBackup != null) {
                pedidosSemanal = new ArrayList<>(pedidosBackup);
                System.out.println("🔄 pedidosSemanal restaurado con " + pedidosSemanal.size() + " pedidos");
            }
            if (fechaBackup != null) {
                fechaActual = fechaBackup;
                System.out.println("🔄 fechaActual restaurada a: " + fechaActual);
            }
        }
        return resultado;
    }

    /**
     * Verifica si existe un backup válido de la simulación.
     * 
     * @return true si existe backup, false en caso contrario
     */
    public static boolean existeBackupSimulacion() {
        return BackupManager.existeBackupSimulacion();
    }

    /**
     * Obtiene información del backup actual.
     * 
     * @return Información del backup o null si no existe
     */
    public static BackupInfo obtenerInfoBackup() {
        BackupManager.BackupInfo info = BackupManager.obtenerInfoBackup();
        if (info == null) {
            return null;
        }
        return new BackupInfo(info.totalPedidosBackup, info.fechaBackup, info.timestampCreacion);
    }

    /**
     * Limpia el backup actual de la simulación.
     */
    public static void limpiarBackupSimulacion() {
        BackupManager.limpiarBackupSimulacion();
    }

    /**
     * Configura los parámetros iniciales de la simulación.
     * 
     * @param startDate Fecha de inicio de la simulación
     */
    public static void configurarSimulacion(LocalDateTime startDate) {
        ConfiguracionSimulacion.configurarSimulacion(startDate);

        // Crear backup del estado inicial después de la configuración
        crearBackupSimulacion();
        System.out.println("🔒 Backup inicial de simulación creado tras configuración");
    }

    /**
     * Ejecuta el bucle principal de la simulación semanal.
     * Procesa pedidos, ejecuta algoritmos genéticos y maneja eventos durante una
     * semana completa.
     */
    public static void ejecutarSimulacion() {
        try {
            GestorHistorialSimulacion.setEnProceso(true);
            ConfiguracionSimulacion.imprimirDatosSimulacion();
            Parametros.fecha_inicial = fechaActual;
            fechaLimite = fechaActual.plusDays(7);
            System.out.println("🚀 Iniciando simulación hasta: " + fechaLimite);
            System.out.println("📅 Fecha de inicio (desde frontend): " + fechaActual);
            System.out.println("📦 Pedidos semanales iniciales: " + pedidosSemanal.size());
            // ! ACA SE SIMULA LA SEMANA COMPLETA

            while (!simulacionTerminadaporelfront) {
                // ! ACA SE SIMULA LA SEMANA COMPLETA
                // !TAMAÑO DE PEDIDPOS SEMANALES fecha actual y fecha limite
                int contadorIteraciones = 0;
                if (contadorIteraciones <= 1 && pedidosSemanal.size() > 0) {
                    System.out.println("Tamaño de pedidos semanales: " + pedidosSemanal.size());
                    System.out.println("Fecha actual: " + fechaActual);
                    System.out.println("Fecha limite: " + fechaLimite);
                    contadorIteraciones++;
                }

                while (!pedidosSemanal.isEmpty()
                        && (fechaActual.isBefore(fechaLimite) || fechaActual.isEqual(fechaLimite))) {
                    // Verificar si la simulación está pausada por una avería O FALTA PARCHE
                    while (GestorHistorialSimulacion.isPausada() || faltacrearparche) {
                        try {
                            System.out.println("⏸️ Simulación pausada, esperando...");
                            Thread.sleep(10000); // Esperar 100ms antes de verificar de nuevo

                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            System.out.println("🛑 Simulación interrumpida durante la pausa");
                            GestorHistorialSimulacion.setEnProceso(false);
                            return;
                        }
                    }
                    Pedido pedido = pedidosSemanal.get(0);
                    // ! Se agregan los pedisos hasta que el pedido tenga una fecha de registro
                    // mayor a la fecha actual
                    if (UtilesSimulacion.pedidoConFechaMenorAFechaActual(pedido, fechaActual)) {
                        pedidosSemanal.remove(0);
                        pedidosPorAtender.add(pedido);
                    } else {
                        // ! Aca se agregaron todos los pedidos que tienen una fecha de registro
                        // ! menor a la fecha actual

                        List<Pedido> pedidosEnviar = UtilesSimulacion.unirPedidosSinRepetidos(pedidosPlanificados,
                                pedidosPorAtender);
                        System.out.println("******************INICIO DE LA ITERACION**************");
                        System.out.println("Tiempo actual: " + fechaActual);
                        System.out.println("Pedidos por enviar al algoritmo genetico: " + pedidosEnviar.size());
                        Camion.imprimirDatosCamiones(DataLoader.camiones);

                        // !ojito esta funcion--> parece que no actualiza bien los estados de los
                        // camiones
                        EstadoManager.actualizarEstadoGlobal(fechaActual, pedidosEnviar);
                        List<Bloqueo> bloqueosActivos = EstadoManager.actualizarBloqueos(fechaActual);

                        if (!pedidosEnviar.isEmpty()) {
                            // camiones
                            // almacenes
                            if (modoStandalone) {
                                // Modo standalone: ejecutar sin esperar semáforos
                                try {
                                    // ! Quiero saber las posiciones actuales de los camiones en el mapa
                                    Camion.imprimirDatosCamiones(DataLoader.camiones);
                                    // ?====Crear el algoritmo genetico====
                                    AlgoritmoGenetico algoritmoGenetico = new AlgoritmoGenetico(Mapa.getInstance(),
                                            pedidosEnviar);
                                    // ? Ejecutar el algoritmo genetico
                                    algoritmoGenetico.ejecutarAlgoritmo();
                                    // ? Crear el paquete de mejor individuo
                                    IndividuoDto mejorIndividuoDto = new IndividuoDto(
                                            algoritmoGenetico.getMejorIndividuo(),
                                            pedidosEnviar, bloqueosActivos, fechaActual);
                                    mejorIndividuoDto.setFechaHoraInicioIntervalo(fechaActual);
                                    mejorIndividuoDto.setFechaHoraFinIntervalo(
                                            fechaActual.plusMinutes(Parametros.intervaloTiempo));
                                    // Aplicar el estado final de los camiones permanentemente
                                    // CamionStateApplier.aplicarEstadoFinalCamiones(algoritmoGenetico.getMejorIndividuo());

                                    // Agregar al historial para el frontend
                                    GestorHistorialSimulacion.agregarPaquete(mejorIndividuoDto);
                                    // ! Quiero saber las posiciones actuales de los camiones en el mapa

                                } catch (Exception e) {
                                    System.err.println("❌ Error en algoritmo genético en tiempo " + fechaActual + ": "
                                            + e.getMessage());
                                    e.printStackTrace();

                                    // Crear un paquete de emergencia en lugar de no generar nada
                                    try {
                                        System.out
                                                .println("🚑 Creando paquete de emergencia para tiempo " + fechaActual);
                                        Individuo individuoEmergencia = IndividuoFactory.crearIndividuoVacio();
                                        IndividuoDto paqueteEmergencia = new IndividuoDto(individuoEmergencia,
                                                pedidosEnviar, bloqueosActivos, fechaActual);

                                        paqueteEmergencia.setFechaHoraInicioIntervalo(fechaActual);
                                        paqueteEmergencia.setFechaHoraFinIntervalo(
                                                fechaActual.plusMinutes(Parametros.intervaloTiempo));
                                        GestorHistorialSimulacion.agregarPaquete(paqueteEmergencia);
                                    } catch (Exception e2) {
                                        System.err
                                                .println("❌ Error al crear paquete de emergencia: " + e2.getMessage());
                                        e2.printStackTrace();
                                    }
                                }
                                for (Bloqueo bloqueo : bloqueosActivos) {
                                    bloqueo.desactivarBloqueo();
                                }

                            } else {
                                // Modo web interactivo: esperar semáforos
                                try {
                                    iniciar.acquire();
                                    AlgoritmoGenetico algoritmoGenetico = new AlgoritmoGenetico(Mapa.getInstance(),
                                            pedidosEnviar);
                                    algoritmoGenetico.ejecutarAlgoritmo();

                                    IndividuoDto mejorIndividuoDto = new IndividuoDto(
                                            algoritmoGenetico.getMejorIndividuo(),
                                            pedidosEnviar, bloqueosActivos, fechaActual);
                                    mejorIndividuoDto.setFechaHoraInicioIntervalo(fechaActual);
                                    mejorIndividuoDto.setFechaHoraFinIntervalo(
                                            fechaActual.plusMinutes(Parametros.intervaloTiempo));
                                    gaResultQueue.offer(mejorIndividuoDto);
                                    continuar.acquire();

                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                    System.err.println(
                                            "Error al esperar el disparador del algoritmo genético: " + e.getMessage());
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            System.out.println("No hay pedidos por atender en este momento.");

                            // Crear paquete vacío para las horas sin pedidos
                            if (modoStandalone) {
                                try {
                                    // ! Quiero saber las posiciones actuales de los camiones en el mapa
                                    Camion.imprimirDatosCamiones(DataLoader.camiones);
                                    // ?====Crear el algoritmo genetico====
                                    AlgoritmoGenetico algoritmoGenetico = new AlgoritmoGenetico(Mapa.getInstance(),
                                            pedidosEnviar);
                                    // ? Ejecutar el algoritmo genetico
                                    algoritmoGenetico.ejecutarAlgoritmo();
                                    // ? Crear el paquete de mejor individuo
                                    IndividuoDto mejorIndividuoDto = new IndividuoDto(
                                            algoritmoGenetico.getMejorIndividuo(),
                                            pedidosEnviar, bloqueosActivos, fechaActual);

                                    // Aplicar el estado final de los camiones permanentemente
                                    // CamionStateApplier.aplicarEstadoFinalCamiones(algoritmoGenetico.getMejorIndividuo());
                                    mejorIndividuoDto.setFechaHoraInicioIntervalo(fechaActual);
                                    mejorIndividuoDto.setFechaHoraFinIntervalo(
                                            fechaActual.plusMinutes(Parametros.intervaloTiempo));
                                    // Agregar al historial para el frontend
                                    GestorHistorialSimulacion.agregarPaquete(mejorIndividuoDto);
                                    // ! Quiero saber las posiciones actuales de los camiones en el mapa
                                } catch (Exception e) {
                                    System.err.println("❌ Error creando paquete vacío en tiempo " + fechaActual + ": "
                                            + e.getMessage());
                                    e.printStackTrace();
                                }
                            }
                        }
                        for (Bloqueo bloqueo : bloqueosActivos) {
                            bloqueo.desactivarBloqueo();
                        }
                        // ! ACA CAMBIA LA FECHA ACTUAL
                        fechaActual = fechaActual.plusMinutes(Parametros.intervaloTiempo);
                        System.out.println("📊 Estado: Pedidos semanales restantes: " + pedidosSemanal.size() +
                                ", Por atender: " + pedidosPorAtender.size() +
                                ", Planificados: " + pedidosPlanificados.size());

                        // Imprimir resumen detallado de estados

                        EstadoManager.imprimirResumenEstados();
                        System.out.println("********************FIN DE ITERACION****************************");
                    }
                }

            }
            // Explicar por qué terminó la simulación
            if (pedidosSemanal.isEmpty()) {
                System.out.println("✅ Simulación terminada: Todos los pedidos semanales han sido procesados");
            } else if (fechaActual.isAfter(fechaLimite)) {
                System.out.println("⏰ Simulación terminada: Se alcanzó el límite de tiempo (" + fechaLimite + ")");
                System.out.println("📦 Pedidos semanales no procesados: " + pedidosSemanal.size());
            }

            System.out.println("************************************************");
            System.out.println("Reporte de la simulación");
            System.out.println("Fecha final: " + fechaActual);
            System.out.println("Kilometros recorridos: " + Parametros.kilometrosRecorridos);
            System.out.println("Fitness global: " + Parametros.fitnessGlobal);
            System.out.println("Pedidos entregados: " + pedidosEntregados.size());
            System.out.println("Pedidos pendientes: " + pedidosPorAtender.size());

            GestorHistorialSimulacion.setEnProceso(false);
            System.out.println("✅ Simulación completada. Total de paquetes generados: "
                    + GestorHistorialSimulacion.getTotalPaquetes());

        } catch (Exception e) {
            System.err.println("💥 ERROR CRÍTICO EN LA SIMULACIÓN:");
            System.err.println("Tiempo actual cuando ocurrió el error: " + fechaActual);
            System.err.println("Mensaje de error: " + e.getMessage());
            System.err.println("Tipo de excepción: " + e.getClass().getSimpleName());
            e.printStackTrace();

            System.err.println("\n📊 Estado al momento del error:");
            System.err.println(
                    "   • Pedidos semanales restantes: " + (pedidosSemanal != null ? pedidosSemanal.size() : "null"));
            System.err.println(
                    "   • Pedidos por atender: " + (pedidosPorAtender != null ? pedidosPorAtender.size() : "null"));
            System.out.println("   • Pedidos planificados: "
                    + (pedidosPlanificados != null ? pedidosPlanificados.size() : "null"));
        }
    }

    public static void crearPaqueteParche(AveriaConEstadoRequest.EstadoSimulacion estadoCapturado) {
        if (faltacrearparche) {
            fechaActual = fechaInicioParche;
            AveriaManager.crearPaqueteParche(estadoCapturado, fechaInicioParche, fechaFinParche, pedidosSemanal,
                    fechaActual);
            faltacrearparche = false;
            fechaActual = fechaFinParche;
        }
    }

    /**
     * Obtiene el siguiente paquete de la simulación para el frontend.
     * Cada llamada devuelve el siguiente paso en secuencia.
     * 
     * @return Paquete con datos del siguiente paso de la simulación
     */
    public static IndividuoDto obtenerSiguientePaquete() {
        return PaqueteManager.obtenerSiguientePaquete();
    }

    /**
     * Reinicia la reproducción desde el inicio para el frontend.
     * Permite volver a reproducir la simulación desde el primer paquete.
     */
    public static void reiniciarReproduccion() {
        PaqueteManager.reiniciarReproduccion();
    }

    public static int eliminarPaquetesFuturosPorFecha(LocalDateTime fechaActual) {
        return PaqueteManager.eliminarPaquetesFuturosPorFecha(fechaActual);
    }

    /**
     * Elimina todos los paquetes futuros mantiendo solo el paquete actual.
     * Se utiliza cuando ocurre una avería para detener la simulación futura.
     * 
     * @return Número de paquetes eliminados
     */
    public static int eliminarPaquetesFuturos() {
        return PaqueteManager.eliminarPaquetesFuturos();
    }

    /**
     * Obtiene información del estado actual de la simulación.
     * 
     * @return Información completa del estado de la simulación incluyendo total de
     *         paquetes, paquete actual, etc.
     */
    public static SimulacionInfo obtenerInfoSimulacion() {
        PaqueteManager.SimulacionInfo info = PaqueteManager.obtenerInfoSimulacion();
        return new SimulacionInfo(info.totalPaquetes, info.paqueteActual, info.enProceso, info.tiempoActual);
    }

    // Clase auxiliar para información de la simulación
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

    // Clase auxiliar para información del backup
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
