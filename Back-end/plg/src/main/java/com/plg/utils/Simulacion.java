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
import com.plg.entity.EstadoPedido;
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

    public static void inicializarSimulacion() {
        // ! ACA SE SIMULA LA SEMANA COMPLETA
        try {
            // ! ACA SE SIMULA LA SEMANA COMPLETA
            GestorHistorialSimulacion.setEnProceso(true);
            ConfiguracionSimulacion.imprimirDatosSimulacion();
            Parametros.fecha_inicial = fechaActual;
            fechaLimite = fechaActual.plusDays(7);
            System.out.println("🚀 Iniciando simulación hasta: " + fechaLimite);
            System.out.println("📅 Fecha de inicio (desde frontend): " + fechaActual);
            System.out.println("📦 Pedidos semanales iniciales: " + pedidosSemanal.size());
        } catch (Exception e) {
            System.err.println("💥 ERROR CRÍTICO EN LA SIMULACIÓN:");
            System.err.println("Tiempo actual cuando ocurrió el error: " + fechaActual);
            System.err.println("Mensaje de error: " + e.getMessage());
            System.err.println("Tipo de excepción: " + e.getClass().getSimpleName());
            e.printStackTrace();
        }
    }

    public static void simularIntervalo(LocalDateTime fechaActual) {
        // Verificar que la fecha inicial esté configurada
        System.out.println("******************INICIO DE LA ITERACION**************");
        if (Parametros.fecha_inicial == null) {
            System.err.println("❌ Error: Parametros.fecha_inicial es null. Configurando con fecha actual.");
            Parametros.fecha_inicial = fechaActual;
            // !DEBERIA COLOCAR LA FECHA FINAL EN FUNCION AL TIPO DE SIMULACION
        }
        // !BUSCA TODOS LOS PEDIDOS con fecha menor a la fecha actual pero mayor a la
        // fecha de inicio
        // Arreglado: corregida la sintaxis de los paréntesis y la lógica del filtro
        List<Pedido> pedidosDelIntervalo = DataLoader.pedidos.stream()
                .filter(pedido -> pedido.getFechaRegistro().isAfter(Parametros.fecha_inicial) &&
                        pedido.getFechaRegistro().isBefore(fechaActual.minusMinutes(Parametros.intervaloTiempo)) &&
                        !pedido.getEstado().equals(EstadoPedido.ENTREGADO))
                .collect(Collectors.toList());

        // !BUSCA TODOS LOS PEDIDOS con fecha menor a la fecha actual pero mayor a la
        // fecha de inicio
        // !BUSCA TODOS LOS PEDIDOS con fecha menor a la fecha actual pero mayor a la
        // fecha de inicio

        System.out.println("Tiempo actual: " + fechaActual);
        System.out.println("Pedidos por enviar al algoritmo genetico: " + pedidosDelIntervalo.size());
        Camion.imprimirDatosCamiones(DataLoader.camiones);

        // !Actualiza con respecto al gen anterior de cada camion
        List<Bloqueo> bloqueosActivos = EstadoManager.actualizarBloqueos(fechaActual);
        EstadoManager.actualizarEstadoGlobal(fechaActual, pedidosDelIntervalo);
        if (pedidosDelIntervalo.isEmpty()) {
            System.out.println("No hay pedidos por atender en este momento.");
        }
        try {
            // ! Quiero saber las posiciones actuales de los camiones en el mapa
            Camion.imprimirDatosCamiones(DataLoader.camiones);
            // ?====Crear el algoritmo genetico====
            AlgoritmoGenetico algoritmoGenetico = new AlgoritmoGenetico(Mapa.getInstance(),
                    pedidosDelIntervalo);
            // ? Ejecutar el algoritmo genetico
            algoritmoGenetico.ejecutarAlgoritmo();
            // ? Crear el paquete de mejor individuo
            IndividuoDto mejorIndividuoDto = new IndividuoDto(
                    algoritmoGenetico.getMejorIndividuo(),
                    pedidosDelIntervalo, bloqueosActivos, fechaActual);
            mejorIndividuoDto.setFechaHoraInicioIntervalo(fechaActual);
            mejorIndividuoDto.setFechaHoraFinIntervalo(
                    fechaActual.plusMinutes(Parametros.intervaloTiempo));
            mejorIndividuoDto.cortarNodos(Parametros.intervaloTiempo);
            // Aplicar el estado final de los camiones permanentemente
            // CamionStateApplier.aplicarEstadoFinalCamiones(algoritmoGenetico.getMejorIndividuo());

            // Agregar al historial para el frontend
            GestorHistorialSimulacion.agregarPaquete(mejorIndividuoDto);
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
                        pedidosDelIntervalo, bloqueosActivos, fechaActual);

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
        // ! Desactivar los bloqueos activos
        for (Bloqueo bloqueo : bloqueosActivos) {
            bloqueo.desactivarBloqueo();
        }
        EstadoManager.imprimirResumenEstados();
        System.out.println("********************FIN DE ITERACION****************************");
    }

    /**
     * Simula un intervalo de tiempo y devuelve el DTO con los resultados.
     * Esta función es síncrona y bloquea hasta completar la simulación del
     * intervalo.
     * 
     * @param fechaActual Fecha y hora actual para la simulación
     * @return IndividuoDto con los resultados de la simulación del intervalo
     */
    public static IndividuoDto simularIntervaloDto(LocalDateTime fechaActual) {
        // Verificar que la fecha inicial esté configurada
        System.out.println("******************INICIO DE LA ITERACION**************");
        if (Parametros.fecha_inicial == null) {
            System.err.println("❌ Error: Parametros.fecha_inicial es null. Configurando con fecha actual.");
            Parametros.fecha_inicial = fechaActual;
            System.out.println("Fecha inicial: " + Parametros.fecha_inicial);
            System.out.println("-----------------------------");
            // !DEBERIA COLOCAR LA FECHA FINAL EN FUNCION AL TIPO DE SIMULACION
        }

        // !BUSCA TODOS LOS PEDIDOS con fecha menor a la fecha actual pero mayor a la
        // fecha de inicio
        // Arreglado: corregida la sintaxis de los paréntesis y la lógica del filtro
        // Cantidad de pedisdos sin entregar en todo el dataloader
        // Corregir el contador de debug (línea 323)
        Long cantidadPedidosDelIntervalo = DataLoader.pedidos.stream()
                .filter(pedido -> (pedido.getFechaRegistro().isAfter(Parametros.fecha_inicial) ||
                        pedido.getFechaRegistro().isEqual(Parametros.fecha_inicial)) &&
                        pedido.getFechaRegistro().isBefore(fechaActual))
                .count();
        System.out.println("Cantidad de pedidos del intervalo: " + cantidadPedidosDelIntervalo);

        // Quiero saber la fecha minima y maxima de los pedidos
        LocalDateTime fechaMinimaPedidos = DataLoader.pedidos.stream()
                .map(Pedido::getFechaRegistro)
                .min(LocalDateTime::compareTo)
                .orElse(null);
        LocalDateTime fechaMaximaPedidos = DataLoader.pedidos.stream()
                .map(Pedido::getFechaRegistro)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        System.out.println("Fecha minima de los pedidos: " + fechaMinimaPedidos);
        System.out.println("Fecha maxima de los pedidos: " + fechaMaximaPedidos);

        System.out.println("-----------------------------");

        // Agregar más logs de debug para entender qué está pasando
        System.out.println("🔍 DEBUG: Analizando pedidos...");
        System.out.println("   • Total de pedidos en DataLoader: " + DataLoader.pedidos.size());
        System.out.println("   • Fecha inicial: " + Parametros.fecha_inicial);
        System.out.println("   • Fecha actual: " + fechaActual);

        // Mostrar algunos ejemplos de pedidos para debug
        DataLoader.pedidos.stream()
                .limit(5)
                .forEach(pedido -> {
                    System.out.println("   • Pedido " + pedido.getCodigo() +
                            " - Fecha: " + pedido.getFechaRegistro() +
                            " - Estado: " + pedido.getEstado() +
                            " - Cumple condición: " +
                            ((pedido.getFechaRegistro().isAfter(Parametros.fecha_inicial) ||
                                    pedido.getFechaRegistro().isEqual(Parametros.fecha_inicial)) &&
                                    pedido.getFechaRegistro().isBefore(fechaActual) &&
                                    !pedido.getEstado().equals(EstadoPedido.ENTREGADO)));
                });

        // Corregir el filtro principal
        List<Pedido> pedidosDelIntervalo = DataLoader.pedidos.stream()
                .filter(pedido -> (pedido.getFechaRegistro().isAfter(Parametros.fecha_inicial) ||
                        pedido.getFechaRegistro().isEqual(Parametros.fecha_inicial)) &&
                        pedido.getFechaRegistro().isBefore(fechaActual) &&
                        !pedido.getEstado().equals(EstadoPedido.ENTREGADO))
                .collect(Collectors.toList());

        System.out.println("Fecha inicial: " + Parametros.fecha_inicial);
        System.out.println("-----------------------------");
        System.out.println("Tiempo actual: " + fechaActual);
        System.out.println("Pedidos por enviar al algoritmo genetico: " + pedidosDelIntervalo.size());
        Camion.imprimirDatosCamiones(DataLoader.camiones);

        // !Actualiza con respecto al gen anterior de cada camion
        List<Bloqueo> bloqueosActivos = EstadoManager.actualizarBloqueos(fechaActual);
        EstadoManager.actualizarEstadoGlobal(fechaActual, pedidosDelIntervalo);

        if (pedidosDelIntervalo.isEmpty()) {
            System.out.println("No hay pedidos por atender en este momento.");
        }

        IndividuoDto resultadoDto = null;

        try {
            // ! Quiero saber las posiciones actuales de los camiones en el mapa
            Camion.imprimirDatosCamiones(DataLoader.camiones);
            // ?====Crear el algoritmo genetico====
            AlgoritmoGenetico algoritmoGenetico = new AlgoritmoGenetico(Mapa.getInstance(),
                    pedidosDelIntervalo);
            // ? Ejecutar el algoritmo genetico
            algoritmoGenetico.ejecutarAlgoritmo();
            // ? Crear el paquete de mejor individuo
            IndividuoDto mejorIndividuoDto = new IndividuoDto(
                    algoritmoGenetico.getMejorIndividuo(),
                    pedidosDelIntervalo, bloqueosActivos, fechaActual);
            mejorIndividuoDto.setFechaHoraInicioIntervalo(fechaActual);
            mejorIndividuoDto.setFechaHoraFinIntervalo(
                    fechaActual.plusMinutes(Parametros.intervaloTiempo));
            mejorIndividuoDto.cortarNodos(Parametros.intervaloTiempo);

            // Asignar el resultado
            resultadoDto = mejorIndividuoDto;

            // Aplicar el estado final de los camiones permanentemente
            // CamionStateApplier.aplicarEstadoFinalCamiones(algoritmoGenetico.getMejorIndividuo());

            // Agregar al historial para el frontend
            // GestorHistorialSimulacion.agregarPaquete(mejorIndividuoDto);

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
                        pedidosDelIntervalo, bloqueosActivos, fechaActual);

                paqueteEmergencia.setFechaHoraInicioIntervalo(fechaActual);
                paqueteEmergencia.setFechaHoraFinIntervalo(
                        fechaActual.plusMinutes(Parametros.intervaloTiempo));

                // Asignar el resultado de emergencia
                resultadoDto = paqueteEmergencia;

                GestorHistorialSimulacion.agregarPaquete(paqueteEmergencia);
            } catch (Exception e2) {
                System.err
                        .println("❌ Error al crear paquete de emergencia: " + e2.getMessage());
                e2.printStackTrace();

                // Crear un DTO vacío como último recurso
                Individuo individuoVacio = IndividuoFactory.crearIndividuoVacio();
                resultadoDto = new IndividuoDto(individuoVacio,
                        new ArrayList<>(), new ArrayList<>(), fechaActual);
                resultadoDto.setFechaHoraInicioIntervalo(fechaActual);
                resultadoDto.setFechaHoraFinIntervalo(
                        fechaActual.plusMinutes(Parametros.intervaloTiempo));
            }
        }

        // ! Desactivar los bloqueos activos
        for (Bloqueo bloqueo : bloqueosActivos) {
            bloqueo.desactivarBloqueo();
        }
        EstadoManager.imprimirResumenEstados();

        System.out.println("===================FIN DE ITERACION========================");

        return resultadoDto;
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
