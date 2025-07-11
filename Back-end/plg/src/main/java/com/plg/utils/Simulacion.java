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
import java.util.concurrent.atomic.AtomicBoolean;

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
    private static final AtomicBoolean faltacrearparche = new AtomicBoolean(false);
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

    // Variables para el modo iterativo
    private static boolean simulacionConfigurada = false;
    private static boolean simulacionFinalizada = false;
    private static int contadorIteraciones = 0;

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
     * Configura la simulación para el modo iterativo.
     * Inicializa todos los parámetros necesarios sin ejecutar el bucle completo.
     * 
     * @param startDate Fecha de inicio de la simulación
     * @return true si la configuración fue exitosa, false en caso contrario
     */
    public static boolean configurarSimulacionIterativa(LocalDateTime startDate) {
        try {
            System.out.println("🔧 Configurando simulación iterativa...");

            // Configurar parámetros iniciales
            ConfiguracionSimulacion.configurarSimulacion(startDate);

            // Inicializar variables de simulación
            fechaActual = startDate;
            fechaLimite = fechaActual.plusDays(7);
            Parametros.fecha_inicial = fechaActual;

            // Reiniciar contadores y estado
            contadorIteraciones = 0;
            simulacionFinalizada = false;
            GestorHistorialSimulacion.setEnProceso(true);

            // Crear backup del estado inicial
            crearBackupSimulacion();

            // Imprimir información de configuración
            ConfiguracionSimulacion.imprimirDatosSimulacion();
            System.out.println("🚀 Simulación configurada hasta: " + fechaLimite);
            System.out.println("📅 Fecha de inicio: " + fechaActual);
            System.out.println("📦 Pedidos semanales iniciales: " + pedidosSemanal.size());

            simulacionConfigurada = true;
            System.out.println("✅ Configuración de simulación completada exitosamente");

            return true;

        } catch (Exception e) {
            System.err.println("❌ Error configurando simulación iterativa: " + e.getMessage());
            e.printStackTrace();
            simulacionConfigurada = false;
            return false;
        }
    }

    /**
     * Obtiene la siguiente solución de la simulación en modo iterativo.
     * Ejecuta una sola iteración del algoritmo genético y retorna el mejor
     * individuo.
     * 
     * @return IndividuoDto con la solución de la iteración actual, o null si la
     *         simulación ha terminado
     */
    public static IndividuoDto obtenerSiguienteSolucion() {
        try {
            // Verificar si la simulación está configurada
            if (!simulacionConfigurada) {
                System.err.println(
                        "❌ Error: La simulación no ha sido configurada. Llame a configurarSimulacionIterativa() primero.");
                return null;
            }

            // Verificar si la simulación ha terminado
            if (simulacionFinalizada || pedidosSemanal.isEmpty() || fechaActual.isAfter(fechaLimite)) {
                if (!simulacionFinalizada) {
                    finalizarSimulacion();
                }
                return null;
            }

            // Verificar si la simulación está pausada
            if (GestorHistorialSimulacion.isPausada() || isFaltaCrearParche()) {
                System.out.println("⏸️ Simulación pausada, no se puede obtener siguiente solución");
                return null;
            }

            contadorIteraciones++;
            System.out.println("******************INICIO DE ITERACIÓN " + contadorIteraciones + "**************");
            System.out.println("Tiempo actual: " + fechaActual);

            // Procesar pedidos que corresponden al tiempo actual
            procesarPedidosParaTiempoActual();

            // Obtener pedidos para enviar al algoritmo genético
            List<Pedido> pedidosEnviar = UtilesSimulacion.unirPedidosSinRepetidos(pedidosPlanificados,
                    pedidosPorAtender);
            System.out.println("Pedidos por enviar al algoritmo genético: " + pedidosEnviar.size());

            // Actualizar estado de camiones y bloqueos
            Camion.imprimirDatosCamiones(DataLoader.camiones);
            List<Bloqueo> bloqueosActivos = EstadoManager.actualizarBloqueos(fechaActual);

            // Solo usar actualizarEstadoGlobal si no es la primera iteración
            if (!isFaltaCrearParche() && contadorIteraciones > 1) {
                EstadoManager.actualizarEstadoGlobal(fechaActual, pedidosEnviar);
            }

            IndividuoDto resultado = null;

            // Ejecutar algoritmo genético
            try {
                if (!isFaltaCrearParche()) {
                    System.out.println("🧬 Ejecutando algoritmo genético...");

                    // Crear y ejecutar algoritmo genético
                    AlgoritmoGenetico algoritmoGenetico = new AlgoritmoGenetico(Mapa.getInstance(), pedidosEnviar);
                    algoritmoGenetico.ejecutarAlgoritmo();

                    // Crear paquete con el mejor individuo
                    resultado = new IndividuoDto(
                            algoritmoGenetico.getMejorIndividuo(),
                            pedidosEnviar,
                            bloqueosActivos,
                            fechaActual);

                    resultado.setFechaHoraInicioIntervalo(fechaActual);
                    resultado.setFechaHoraFinIntervalo(fechaActual.plusMinutes(Parametros.intervaloTiempo));

                    // Agregar al historial para el frontend
                    GestorHistorialSimulacion.agregarPaquete(resultado);

                    System.out.println("✅ Algoritmo genético ejecutado exitosamente");
                }

            } catch (Exception e) {
                System.err.println("❌ Error en algoritmo genético en tiempo " + fechaActual + ": " + e.getMessage());
                e.printStackTrace();

                // Crear paquete de emergencia
                try {
                    System.out.println("🚑 Creando paquete de emergencia para tiempo " + fechaActual);
                    Individuo individuoEmergencia = IndividuoFactory.crearIndividuoVacio();
                    resultado = new IndividuoDto(individuoEmergencia, pedidosEnviar, bloqueosActivos, fechaActual);
                    resultado.setFechaHoraInicioIntervalo(fechaActual);
                    resultado.setFechaHoraFinIntervalo(fechaActual.plusMinutes(Parametros.intervaloTiempo));
                    GestorHistorialSimulacion.agregarPaquete(resultado);
                } catch (Exception e2) {
                    System.err.println("❌ Error al crear paquete de emergencia: " + e2.getMessage());
                    e2.printStackTrace();
                }
            }

            // Desactivar bloqueos
            for (Bloqueo bloqueo : bloqueosActivos) {
                bloqueo.desactivarBloqueo();
            }

            // Avanzar el tiempo
            fechaActual = fechaActual.plusMinutes(Parametros.intervaloTiempo);

            // Imprimir estado actual
            System.out.println("📊 Estado: Pedidos semanales restantes: " + pedidosSemanal.size() +
                    ", Por atender: " + pedidosPorAtender.size() +
                    ", Planificados: " + pedidosPlanificados.size());

            EstadoManager.imprimirResumenEstados();
            System.out.println(
                    "********************FIN DE ITERACIÓN " + contadorIteraciones + "****************************");

            return resultado;

        } catch (Exception e) {
            System.err.println("❌ Error obteniendo siguiente solución: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Procesa los pedidos que corresponden al tiempo actual.
     * Mueve pedidos de pedidosSemanal a pedidosPorAtender según su fecha de
     * registro.
     */
    private static void procesarPedidosParaTiempoActual() {
        while (!pedidosSemanal.isEmpty()) {
            Pedido pedido = pedidosSemanal.get(0);

            if (UtilesSimulacion.pedidoConFechaMenorAFechaActual(pedido, fechaActual)) {
                pedidosSemanal.remove(0);
                pedidosPorAtender.add(pedido);
                System.out.println("📦 Pedido agregado a por atender: " + pedido.getCodigo());
            } else {
                // Los pedidos están ordenados por fecha, si este no aplica, los siguientes
                // tampoco
                break;
            }
        }
    }

    /**
     * Finaliza la simulación y genera el reporte final.
     */
    private static void finalizarSimulacion() {
        if (simulacionFinalizada) {
            return;
        }

        System.out.println("🏁 Finalizando simulación...");

        // Explicar por qué terminó la simulación
        if (pedidosSemanal.isEmpty()) {
            System.out.println("✅ Simulación terminada: Todos los pedidos semanales han sido procesados");
        } else if (fechaActual.isAfter(fechaLimite)) {
            System.out.println("⏰ Simulación terminada: Se alcanzó el límite de tiempo (" + fechaLimite + ")");
            System.out.println("📦 Pedidos semanales no procesados: " + pedidosSemanal.size());
        }

        // Generar reporte final
        System.out.println("************************************************");
        System.out.println("Reporte final de la simulación");
        System.out.println("Fecha final: " + fechaActual);
        System.out.println("Iteraciones ejecutadas: " + contadorIteraciones);
        System.out.println("Kilómetros recorridos: " + Parametros.kilometrosRecorridos);
        System.out.println("Fitness global: " + Parametros.fitnessGlobal);
        System.out.println("Pedidos entregados: " + pedidosEntregados.size());
        System.out.println("Pedidos pendientes: " + pedidosPorAtender.size());
        System.out.println("************************************************");

        GestorHistorialSimulacion.setEnProceso(false);
        simulacionFinalizada = true;
        simulacionConfigurada = false;

        System.out.println("✅ Simulación completada. Total de paquetes generados: "
                + GestorHistorialSimulacion.getTotalPaquetes());
    }

    /**
     * Obtiene el estado actual de la simulación iterativa.
     * 
     * @return Información del estado de la simulación
     */
    public static EstadoSimulacionIterativa obtenerEstadoSimulacionIterativa() {
        return new EstadoSimulacionIterativa(
                simulacionConfigurada,
                simulacionFinalizada,
                contadorIteraciones,
                fechaActual,
                fechaLimite,
                pedidosSemanal != null ? pedidosSemanal.size() : 0,
                pedidosPorAtender.size(),
                pedidosPlanificados.size(),
                pedidosEntregados.size());
    }

    /**
     * Reinicia la simulación iterativa.
     * Limpia el estado y permite configurar una nueva simulación.
     */
    public static void reiniciarSimulacionIterativa() {
        System.out.println("🔄 Reiniciando simulación iterativa...");

        simulacionConfigurada = false;
        simulacionFinalizada = false;
        contadorIteraciones = 0;

        GestorHistorialSimulacion.setEnProceso(false);

        // Limpiar colecciones
        if (pedidosPorAtender != null) {
            pedidosPorAtender.clear();
        }
        if (pedidosPlanificados != null) {
            pedidosPlanificados.clear();
        }
        if (pedidosEntregados != null) {
            pedidosEntregados.clear();
        }

        System.out.println("✅ Simulación iterativa reiniciada");
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
                    while (GestorHistorialSimulacion.isPausada() || isFaltaCrearParche()) {
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
                        List<Bloqueo> bloqueosActivos = EstadoManager.actualizarBloqueos(fechaActual);
                        if (!isFaltaCrearParche()) {
                            EstadoManager.actualizarEstadoGlobal(fechaActual, pedidosEnviar);
                        }
                        if (!pedidosEnviar.isEmpty()) {
                            // camiones
                            // almacenes
                            if (modoStandalone) {
                                // Modo standalone: ejecutar sin esperar semáforos
                                try {
                                    if (!isFaltaCrearParche()) {

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
                                    }

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
                                    if (!isFaltaCrearParche()) {
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
                                    }

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
                            if (modoStandalone || !isFaltaCrearParche()) {
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
                                    if (!isFaltaCrearParche()) {
                                        GestorHistorialSimulacion.agregarPaquete(mejorIndividuoDto);
                                    }

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
        if (isFaltaCrearParche()) {
            fechaActual = fechaInicioParche;
            AveriaManager.crearPaqueteParche(estadoCapturado, fechaInicioParche, fechaFinParche, pedidosSemanal,
                    fechaActual);

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

    /**
     * Obtiene el valor de faltacrearparche de forma thread-safe.
     * 
     * @return true si falta crear parche, false en caso contrario
     */
    public static boolean isFaltaCrearParche() {
        boolean value = faltacrearparche.get();
        System.out.println("🔍 [" + Thread.currentThread().getName() + "] Leyendo faltacrearparche: " + value);
        return value;
    }

    /**
     * Establece el valor de faltacrearparche de forma thread-safe.
     * 
     * @param value el nuevo valor a establecer
     */
    public static void setFaltaCrearParche(boolean value) {
        boolean oldValue = faltacrearparche.getAndSet(value);
        System.out.println("✏️ [" + Thread.currentThread().getName() + "] Cambiando faltacrearparche: " + oldValue
                + " → " + value);
    }

    /**
     * Establece faltacrearparche a true de forma thread-safe.
     */
    public static void activarFaltaCrearParche() {
        boolean oldValue = faltacrearparche.getAndSet(true);
        System.out.println(
                "🚨 [" + Thread.currentThread().getName() + "] ACTIVANDO faltacrearparche: " + oldValue + " → true");
    }

    /**
     * Establece faltacrearparche a false de forma thread-safe.
     */
    public static void desactivarFaltaCrearParche() {
        boolean oldValue = faltacrearparche.getAndSet(false);
        System.out.println(
                "✅ [" + Thread.currentThread().getName() + "] DESACTIVANDO faltacrearparche: " + oldValue + " → false");
    }

    /**
     * Obtiene la solución de la simulación para una fecha específica.
     * No avanza automáticamente el tiempo, solo calcula la solución para la fecha
     * dada.
     * 
     * @param fechaEspecifica Fecha para la cual se desea obtener la solución
     * @return IndividuoDto con la solución para la fecha especificada, o null si no
     *         se puede calcular
     */
    public static IndividuoDto obtenerSolucionParaFecha(LocalDateTime fechaEspecifica) {
        try {
            // Verificar si la simulación está configurada
            if (!simulacionConfigurada) {
                System.err.println(
                        "❌ Error: La simulación no ha sido configurada. Llame a configurarSimulacionIterativa() primero.");
                return null;
            }

            // Verificar si la fecha está dentro del rango válido
            if (fechaEspecifica.isBefore(fechaActual) || fechaEspecifica.isAfter(fechaLimite)) {
                System.err.println("❌ Error: La fecha especificada (" + fechaEspecifica
                        + ") está fuera del rango válido [" + fechaActual + ", " + fechaLimite + "]");
                return null;
            }

            // Verificar si la simulación está pausada
            if (GestorHistorialSimulacion.isPausada() || isFaltaCrearParche()) {
                System.out.println("⏸️ Simulación pausada, no se puede obtener solución");
                return null;
            }

            System.out.println("🎯 Calculando solución para fecha específica: " + fechaEspecifica);

            // Crear una copia temporal del estado para calcular la solución
            LocalDateTime fechaActualTemp = fechaActual;
            Set<Pedido> pedidosPorAtenderTemp = new LinkedHashSet<>(pedidosPorAtender);
            Set<Pedido> pedidosPlanificadosTemp = new LinkedHashSet<>(pedidosPlanificados);
            List<Pedido> pedidosSemanalTemp = new ArrayList<>(pedidosSemanal);

            // Procesar pedidos hasta la fecha especificada (sin cambiar el estado real)
            while (fechaActualTemp.isBefore(fechaEspecifica)) {
                // Procesar pedidos que corresponden al tiempo actual
                while (!pedidosSemanalTemp.isEmpty()) {
                    Pedido pedido = pedidosSemanalTemp.get(0);
                    if (UtilesSimulacion.pedidoConFechaMenorAFechaActual(pedido, fechaActualTemp)) {
                        pedidosSemanalTemp.remove(0);
                        pedidosPorAtenderTemp.add(pedido);
                    } else {
                        break;
                    }
                }
                fechaActualTemp = fechaActualTemp.plusMinutes(Parametros.intervaloTiempo);
            }

            // Obtener pedidos para enviar al algoritmo genético
            List<Pedido> pedidosEnviar = UtilesSimulacion.unirPedidosSinRepetidos(pedidosPlanificadosTemp,
                    pedidosPorAtenderTemp);
            System.out.println("Pedidos por enviar al algoritmo genético para fecha " + fechaEspecifica + ": "
                    + pedidosEnviar.size());

            // Actualizar estado de camiones y bloqueos para la fecha especificada
            Camion.imprimirDatosCamiones(DataLoader.camiones);
            List<Bloqueo> bloqueosActivos = EstadoManager.actualizarBloqueos(fechaEspecifica);

            // Solo usar actualizarEstadoGlobal si no es la primera iteración
            if (!isFaltaCrearParche() && contadorIteraciones > 0) {
                EstadoManager.actualizarEstadoGlobal(fechaEspecifica, pedidosEnviar);
            }

            IndividuoDto resultado = null;

            // Ejecutar algoritmo genético
            try {
                if (!isFaltaCrearParche()) {
                    System.out.println("🧬 Ejecutando algoritmo genético para fecha " + fechaEspecifica);

                    // Crear y ejecutar algoritmo genético
                    AlgoritmoGenetico algoritmoGenetico = new AlgoritmoGenetico(Mapa.getInstance(), pedidosEnviar);
                    algoritmoGenetico.ejecutarAlgoritmo();

                    // Crear paquete con el mejor individuo
                    resultado = new IndividuoDto(
                            algoritmoGenetico.getMejorIndividuo(),
                            pedidosEnviar,
                            bloqueosActivos,
                            fechaEspecifica);

                    resultado.setFechaHoraInicioIntervalo(fechaEspecifica);
                    resultado.setFechaHoraFinIntervalo(fechaEspecifica.plusMinutes(Parametros.intervaloTiempo));

                    // Agregar al historial para el frontend
                    GestorHistorialSimulacion.agregarPaquete(resultado);

                    System.out.println("✅ Algoritmo genético ejecutado exitosamente para fecha " + fechaEspecifica);
                }

            } catch (Exception e) {
                System.err
                        .println("❌ Error en algoritmo genético para fecha " + fechaEspecifica + ": " + e.getMessage());
                e.printStackTrace();

                // Crear paquete de emergencia
                try {
                    System.out.println("🚑 Creando paquete de emergencia para fecha " + fechaEspecifica);
                    Individuo individuoEmergencia = IndividuoFactory.crearIndividuoVacio();
                    resultado = new IndividuoDto(individuoEmergencia, pedidosEnviar, bloqueosActivos, fechaEspecifica);
                    resultado.setFechaHoraInicioIntervalo(fechaEspecifica);
                    resultado.setFechaHoraFinIntervalo(fechaEspecifica.plusMinutes(Parametros.intervaloTiempo));
                    GestorHistorialSimulacion.agregarPaquete(resultado);
                } catch (Exception e2) {
                    System.err.println("❌ Error al crear paquete de emergencia: " + e2.getMessage());
                    e2.printStackTrace();
                }
            }

            // Desactivar bloqueos
            for (Bloqueo bloqueo : bloqueosActivos) {
                bloqueo.desactivarBloqueo();
            }

            // Imprimir estado actual
            System.out.println("📊 Estado después de calcular solución para " + fechaEspecifica + ":");
            System.out.println("   Pedidos semanales restantes: " + pedidosSemanal.size());
            System.out.println("   Pedidos por atender: " + pedidosPorAtender.size());
            System.out.println("   Pedidos planificados: " + pedidosPlanificados.size());
            System.out.println("   Fecha actual real: " + fechaActual);

            return resultado;

        } catch (Exception e) {
            System.err.println("❌ Error obteniendo solución para fecha " + fechaEspecifica + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Avanza la simulación hasta una fecha específica y obtiene la solución.
     * Similar a obtenerSolucionParaFecha pero actualiza la fecha actual
     * permanentemente.
     * 
     * @param fechaEspecifica Fecha hasta la cual avanzar la simulación
     * @return IndividuoDto con la solución para la fecha especificada, o null si no
     *         se puede calcular
     */
    public static IndividuoDto avanzarHastaFecha(LocalDateTime fechaEspecifica) {
        try {
            // Verificar si la simulación está configurada
            if (!simulacionConfigurada) {
                System.err.println(
                        "❌ Error: La simulación no ha sido configurada. Llame a configurarSimulacionIterativa() primero.");
                return null;
            }

            // Verificar si la fecha está dentro del rango válido
            if (fechaEspecifica.isBefore(fechaActual) || fechaEspecifica.isAfter(fechaLimite)) {
                System.err.println("❌ Error: La fecha especificada (" + fechaEspecifica
                        + ") está fuera del rango válido [" + fechaActual + ", " + fechaLimite + "]");
                return null;
            }

            // Verificar si la simulación está pausada
            if (GestorHistorialSimulacion.isPausada() || isFaltaCrearParche()) {
                System.out.println("⏸️ Simulación pausada, no se puede avanzar");
                return null;
            }

            System.out.println("⏩ Avanzando simulación hasta fecha: " + fechaEspecifica);

            // Avanzar hasta la fecha especificada procesando pedidos
            while (fechaActual.isBefore(fechaEspecifica)) {
                procesarPedidosParaTiempoActual();
                fechaActual = fechaActual.plusMinutes(Parametros.intervaloTiempo);
                contadorIteraciones++;
            }

            // Obtener pedidos para enviar al algoritmo genético
            List<Pedido> pedidosEnviar = UtilesSimulacion.unirPedidosSinRepetidos(pedidosPlanificados,
                    pedidosPorAtender);
            System.out.println("Pedidos por enviar al algoritmo genético para fecha " + fechaEspecifica + ": "
                    + pedidosEnviar.size());

            // Actualizar estado de camiones y bloqueos para la fecha especificada
            Camion.imprimirDatosCamiones(DataLoader.camiones);
            List<Bloqueo> bloqueosActivos = EstadoManager.actualizarBloqueos(fechaEspecifica);

            // Solo usar actualizarEstadoGlobal si no es la primera iteración
            if (!isFaltaCrearParche() && contadorIteraciones > 1) {
                EstadoManager.actualizarEstadoGlobal(fechaEspecifica, pedidosEnviar);
            }

            IndividuoDto resultado = null;

            // Ejecutar algoritmo genético
            try {
                if (!isFaltaCrearParche()) {
                    System.out.println("🧬 Ejecutando algoritmo genético para fecha " + fechaEspecifica);

                    // Crear y ejecutar algoritmo genético
                    AlgoritmoGenetico algoritmoGenetico = new AlgoritmoGenetico(Mapa.getInstance(), pedidosEnviar);
                    algoritmoGenetico.ejecutarAlgoritmo();

                    // Crear paquete con el mejor individuo
                    resultado = new IndividuoDto(
                            algoritmoGenetico.getMejorIndividuo(),
                            pedidosEnviar,
                            bloqueosActivos,
                            fechaEspecifica);

                    resultado.setFechaHoraInicioIntervalo(fechaEspecifica);
                    resultado.setFechaHoraFinIntervalo(fechaEspecifica.plusMinutes(Parametros.intervaloTiempo));

                    // Agregar al historial para el frontend
                    GestorHistorialSimulacion.agregarPaquete(resultado);

                    System.out.println("✅ Algoritmo genético ejecutado exitosamente para fecha " + fechaEspecifica);
                }

            } catch (Exception e) {
                System.err
                        .println("❌ Error en algoritmo genético para fecha " + fechaEspecifica + ": " + e.getMessage());
                e.printStackTrace();

                // Crear paquete de emergencia
                try {
                    System.out.println("🚑 Creando paquete de emergencia para fecha " + fechaEspecifica);
                    Individuo individuoEmergencia = IndividuoFactory.crearIndividuoVacio();
                    resultado = new IndividuoDto(individuoEmergencia, pedidosEnviar, bloqueosActivos, fechaEspecifica);
                    resultado.setFechaHoraInicioIntervalo(fechaEspecifica);
                    resultado.setFechaHoraFinIntervalo(fechaEspecifica.plusMinutes(Parametros.intervaloTiempo));
                    GestorHistorialSimulacion.agregarPaquete(resultado);
                } catch (Exception e2) {
                    System.err.println("❌ Error al crear paquete de emergencia: " + e2.getMessage());
                    e2.printStackTrace();
                }
            }

            // Desactivar bloqueos
            for (Bloqueo bloqueo : bloqueosActivos) {
                bloqueo.desactivarBloqueo();
            }

            // Avanzar al siguiente intervalo después de procesar
            fechaActual = fechaEspecifica.plusMinutes(Parametros.intervaloTiempo);

            // Imprimir estado actual
            System.out.println("📊 Estado después de avanzar hasta " + fechaEspecifica + ":");
            System.out.println("   Iteraciones ejecutadas: " + contadorIteraciones);
            System.out.println("   Pedidos semanales restantes: " + pedidosSemanal.size());
            System.out.println("   Pedidos por atender: " + pedidosPorAtender.size());
            System.out.println("   Pedidos planificados: " + pedidosPlanificados.size());
            System.out.println("   Nueva fecha actual: " + fechaActual);

            return resultado;

        } catch (Exception e) {
            System.err.println("❌ Error avanzando hasta fecha " + fechaEspecifica + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
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

    // Clase auxiliar para el estado de la simulación iterativa
    public static class EstadoSimulacionIterativa {
        public final boolean configurada;
        public final boolean finalizada;
        public final int iteraciones;
        public final LocalDateTime fechaActual;
        public final LocalDateTime fechaLimite;
        public final int pedidosSemanales;
        public final int pedidosPorAtender;
        public final int pedidosPlanificados;
        public final int pedidosEntregados;

        public EstadoSimulacionIterativa(boolean configurada, boolean finalizada, int iteraciones,
                LocalDateTime fechaActual, LocalDateTime fechaLimite,
                int pedidosSemanales, int pedidosPorAtender,
                int pedidosPlanificados, int pedidosEntregados) {
            this.configurada = configurada;
            this.finalizada = finalizada;
            this.iteraciones = iteraciones;
            this.fechaActual = fechaActual;
            this.fechaLimite = fechaLimite;
            this.pedidosSemanales = pedidosSemanales;
            this.pedidosPorAtender = pedidosPorAtender;
            this.pedidosPlanificados = pedidosPlanificados;
            this.pedidosEntregados = pedidosEntregados;
        }
    }
}
