package com.plg.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.plg.dto.request.AveriaRequest;
import com.plg.dto.IndividuoDto;
import com.plg.dto.request.AveriaConEstadoRequest;
import com.plg.entity.Averia;
import com.plg.entity.Camion;
import com.plg.entity.Coordenada;
import com.plg.entity.EstadoCamion;
import com.plg.entity.EstadoPedido;
import com.plg.entity.Nodo;
import com.plg.entity.Pedido;
import com.plg.entity.TipoAlmacen;
import com.plg.entity.TipoNodo;
import com.plg.entity.TipoIncidente;
import com.plg.repository.AveriaRepository;
import com.plg.utils.ExcepcionesPerzonalizadas.InvalidInputException;
import com.plg.utils.simulacion.GestorHistorialSimulacion;

/**
 * Servicio para operaciones sobre averías.
 */
@Service
public class AveriaService {

    private final AveriaRepository averiaRepository;
    private final CamionService camionService;
    private final AlmacenService almacenService;

    public AveriaService(AveriaRepository averiaRepository, CamionService camionService,
            AlmacenService almacenService) {
        this.averiaRepository = averiaRepository;
        this.camionService = camionService;
        this.almacenService = almacenService;
    }

    /**
     * Lista todas las averías registradas.
     *
     * @return Lista de todas las averías
     */
    public List<Averia> listar() {
        return averiaRepository.findAll();
    }

    /**
     * Lista todas las averías activas.
     *
     * @return Lista de averías activas
     */
    public List<Averia> listarActivas() {
        return averiaRepository.findAllActive();
    }

    /**
     * Lista averías por camión.
     *
     * @param camion el camión del cual obtener las averías
     * @return Lista de averías del camión
     */
    public List<Averia> listarPorCamion(Camion camion) {
        return averiaRepository.findByCamion(camion);
    }

    /**
     * Lista averías por camión y tipo de incidente.
     *
     * @param codigoCamion  código del camión
     * @param tipoIncidente tipo de incidente ("TI1", "TI2", "TI3")
     * @return Lista de averías filtradas
     */
    public List<Averia> listarPorCamionYTipo(String codigoCamion, String tipoIncidente) {
        return averiaRepository.findByCamionAndTipo(codigoCamion, tipoIncidente);
    }

    /**
     * Crea una nueva avería utilizando los datos de la solicitud.
     *
     * @param request datos de la avería a crear
     * @return la avería creada
     * @throws InvalidInputException si los datos son inválidos
     */
    public Averia agregar(AveriaRequest request) throws InvalidInputException {
        // Validaciones
        if (request.getCodigoCamion() == null || request.getCodigoCamion().trim().isEmpty()) {
            throw new InvalidInputException("El código del camión es obligatorio");
        }
        if (request.getTipoIncidente() == null) {
            throw new InvalidInputException("El tipo de incidente es obligatorio");
        }
        try {
            // Crear la avería solo con los campos requeridos
            Averia averia = request.toAveria();
            // !CALCULA LOS DATOS DE LA AVERIA EN BASE A LOS DATOS DEL CAMION Y TIPO DE
            // INCIDENTE
            averia.calcularTurnoOcurrencia();

            averia.getTipoIncidente().initDefaultAverias();
            averia.setFechaHoraFinEsperaEnRuta(averia.calcularFechaHoraFinEsperaEnRuta());
            averia.setFechaHoraDisponible(averia.calcularFechaHoraDisponible());
            averia.setTiempoReparacionEstimado(averia.calcularTiempoInoperatividad());
            // ! CALCULA LOS DATOS DE LA AVERIA EN BASE A LOS DATOS DEL CAMION Y TIPO DE
            // INCIDENTE //?--------------------------------------
            // ! Ahora se actualiza el estado del camión a INMOVILIZADO_POR_AVERIA
            camionService.cambiarEstado(request.getCodigoCamion(), EstadoCamion.INMOVILIZADO_POR_AVERIA);

            // ! Cambiar la posición del camión con la coordenada del request
            if (request.getCoordenada() != null) {
                camionService.cambiarCoordenada(request.getCodigoCamion(), request.getCoordenada());
            }

            // ! Si el tipo de incidente NO es TI1, cambiar el estado de los pedidos
            // asociados a REGISTRADO
            if (!"TI1".equals(averia.getTipoIncidente().getCodigo())) {
                actualizarPedidosACamionAveriado(request.getCodigoCamion());
            }
            averia.setEstado(true); // Asegurarse de que la avería esté activa
            return averiaRepository.save(averia);
        } catch (NoSuchElementException e) {
            throw new InvalidInputException("Camión no encontrado: " + request.getCodigoCamion());
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Datos inválidos: " + e.getMessage());
        } catch (Exception e) {
            throw new InvalidInputException("Error al crear la avería: " + e.getMessage());
        }
    }

    /**
     * Crea una nueva avería con estado completo de la simulación.
     * Este método maneja tanto la creación de la avería como el procesamiento
     * del estado completo de la simulación en el momento de la avería.
     *
     * @param request datos de la avería con estado completo de la simulación
     * @return la avería creada
     * @throws InvalidInputException si los datos son inválidos
     */
    public Averia agregarConEstadoCompleto(AveriaConEstadoRequest request) throws InvalidInputException {
        // Validaciones básicas
        if (request.getCodigoCamion() == null || request.getCodigoCamion().trim().isEmpty()) {
            throw new InvalidInputException("El código del camión es obligatorio");
        }
        if (request.getTipoIncidente() == null) {
            throw new InvalidInputException("El tipo de incidente es obligatorio");
        }
        if (request.getEstadoSimulacion() == null) {
            throw new InvalidInputException("El estado de la simulación es obligatorio");
        }

        try {
            // Log del estado recibido
            System.out.println("🚛💥 BACKEND: Procesando avería con estado completo");
            System.out.println("📊 BACKEND: Camión: " + request.getCodigoCamion());
            System.out.println("📊 BACKEND: Tipo: " + request.getTipoIncidente());
            System.out.println("📊 BACKEND: Timestamp: " + request.getEstadoSimulacion().getTimestamp());
            System.out.println("📊 BACKEND: Hora simulación: " + request.getEstadoSimulacion().getHoraSimulacion());
            System.out.println("📊 BACKEND: Camiones en estado: " +
                    (request.getEstadoSimulacion().getCamiones() != null
                            ? request.getEstadoSimulacion().getCamiones().size()
                            : 0));
            System.out.println("📊 BACKEND: Rutas en estado: " +
                    (request.getEstadoSimulacion().getRutasCamiones() != null
                            ? request.getEstadoSimulacion().getRutasCamiones().size()
                            : 0));
            System.out.println("📊 BACKEND: Almacenes en estado: " +
                    (request.getEstadoSimulacion().getAlmacenes() != null
                            ? request.getEstadoSimulacion().getAlmacenes().size()
                            : 0));
            System.out.println("📊 BACKEND: Bloqueos en estado: " +
                    (request.getEstadoSimulacion().getBloqueos() != null
                            ? request.getEstadoSimulacion().getBloqueos().size()
                            : 0));

            // Procesar el estado completo de la simulación
            procesarEstadoCompleto(request.getEstadoSimulacion(), request);

            // Crear la avería usando el método estándar
            AveriaRequest averiaRequest = request.toAveriaRequest();
            Averia averia = agregar(averiaRequest);

            System.out.println("✅ BACKEND: Avería creada exitosamente para camión: " + averia.getCamion().getCodigo());
            System.out.println("✅ BACKEND: Estado completo procesado correctamente");

            return averia;

        } catch (Exception e) {
            System.err.println("❌ BACKEND: Error al procesar avería con estado completo: " + e.getMessage());
            throw new InvalidInputException("Error al crear la avería con estado completo: " + e.getMessage());
        }
    }

    /**
     * Procesa el estado completo de la simulación capturado durante la avería.
     * Este método maneja el análisis, almacenamiento del estado completo y
     * la generación del paquete parche para manejar la interrupción temporal.
     *
     * @param estadoSimulacion el estado completo de la simulación
     * @param request          el request completo con el timestamp correcto de la
     *                         avería
     */
    private void procesarEstadoCompleto(AveriaConEstadoRequest.EstadoSimulacion estadoSimulacion,
            AveriaConEstadoRequest request) {
        try {
            System.out.println("==========================================================");
            System.out.println("==========================================================");
            System.out.println("🔄 BACKEND: Procesando estado completo de la simulación...");
            // System.out.println("🔄 BACKEND: Estado simulación: " + estadoSimulacion);
            // System.out.println("🔄 BACKEND: Request: " + request);
            System.out.println("🔄 BACKEND: Procesando estado completo de la simulación...");

            // Paso 0: PAUSAR la simulación inmediatamente (en lugar de detener)
            System.out.println("⏸️ BACKEND: Pausando simulación del backend por avería...");
            com.plg.controller.SimulacionController.pausarSimulacionPorAveria();

            // Paso 1: Detener la generación de paquetes futuros inmediatamente
            System.out.println("🛑 BACKEND: Eliminando paquetes futuros...");
            int paquetesEliminados = com.plg.utils.Simulacion.eliminarPaquetesFuturos();

            // PASO 1.1: Ajustar el número de los paquetes agregados en el historial de
            // simulación
            System.out.println("🔢 BACKEND: Ajustando contador de paquetes para numeración consecutiva...");
            GestorHistorialSimulacion.ajustarContadorPaquetes();

            System.out.println("✅ BACKEND: Paquetes futuros eliminados: " + paquetesEliminados);
            System.out.println("✅ BACKEND: Contador de paquetes ajustado correctamente");

            // Obtener el timestamp de la avería para usar en múltiples pasos
            String timestampString = request.getFechaHoraReporte();
            if (timestampString.endsWith("Z")) {
                timestampString = timestampString.substring(0, timestampString.length() - 1);
            }
            LocalDateTime timestampAveria = LocalDateTime.parse(timestampString);

            // Paso 1.1: Actualizar los pedidos de la semana
            System.out.println("🔄 BACKEND: Actualizando pedidos de la semana...");
            actualizarPedidosSemanalesConBackup(timestampAveria, estadoSimulacion);
            // Paso 1.2: Actualizar los camiones almacenes con los datos enviados desde el
            // frontend
            System.out.println("🔄 BACKEND: Actualizando camiones y almacenes...");
            actualizarCamionesYAlmacenesConEstadoCapturado(estadoSimulacion);
            com.plg.utils.Simulacion.fechaInicioParche = timestampAveria;
            int paqueteActualNumero = GestorHistorialSimulacion.getPaqueteActual();
            IndividuoDto paqueteActual = GestorHistorialSimulacion.obtenerPaquetePorIndice(paqueteActualNumero);
            com.plg.utils.Simulacion.fechaFinParche = paqueteActual.getFechaHoraSimulacion();
            // Suma a la fecha fin la fecha del intervalo de tiempo de la simulacion que
            // esta en parametros
            com.plg.utils.Simulacion.fechaFinParche = com.plg.utils.Simulacion.fechaFinParche.plusMinutes(
                    com.plg.utils.Parametros.intervaloTiempo * 2);
            // !actualiaz la fecha actual de la simulacion

            System.out.println("🔄 BACKEND: Fecha inicio parche: " + com.plg.utils.Simulacion.fechaInicioParche);
            System.out.println("🔄 BACKEND: Fecha fin parche: " + com.plg.utils.Simulacion.fechaFinParche);

            // Paso 2: Generar paquete parche con el estado capturado
            System.out.println("🩹 BACKEND: Generando paquete parche para manejar la avería...");
            com.plg.utils.Simulacion.faltacrearparche = true;
            com.plg.utils.Simulacion.crearPaqueteParche(request.getEstadoSimulacion());
            System.out.println("📅 BACKEND: Usando timestamp de avería correcto: " + timestampAveria);
            System.out.println("📅 BACKEND: (No el timestamp del estado: " + estadoSimulacion.getTimestamp() + ")");

            // ! colocar QUE FALTA CREAR PARCHE

            System.out.println("▶️ BACKEND: Reanudando simulación después de procesaravería...");
            com.plg.controller.SimulacionController.reanudarSimulacionDespuesDeAveria();
            // Paso 5: Análisis del estado para logs y reportes
            analizarEstadoCapturado(estadoSimulacion);

            System.out.println("✅ BACKEND: Estado procesado y simulación reanudada exitosamente");
            System.out.println("==========================================================");
            System.out.println("==========================================================");
        } catch (Exception e) {
            System.err.println("⚠️ BACKEND: Error al procesar estado completo: " + e.getMessage());
            e.printStackTrace();
            // En caso de error, asegurar que la simulación se reanude
            System.out.println("🔄 BACKEND: Reanudando simulación debido a error...");
            // com.plg.controller.SimulacionController.reanudarSimulacionDespuesDeAveria();
        }
    }

    /**
     * Analiza el estado capturado durante la avería para generar logs y reportes.
     *
     * @param estadoSimulacion el estado completo de la simulación
     */
    private void analizarEstadoCapturado(AveriaConEstadoRequest.EstadoSimulacion estadoSimulacion) {
        try {
            System.out.println("📊 BACKEND: ANÁLISIS DEL ESTADO CAPTURADO:");

            // Análisis de camiones
            if (estadoSimulacion.getCamiones() != null) {
                System.out.println("📈 BACKEND: Analizando " + estadoSimulacion.getCamiones().size() + " camiones");
                long camionesEnCamino = estadoSimulacion.getCamiones().stream()
                        .filter(c -> "En Camino".equals(c.getEstado()))
                        .count();
                long camionesAveriados = estadoSimulacion.getCamiones().stream()
                        .filter(c -> c.getEstado() != null && c.getEstado().contains("Averiado"))
                        .count();
                long camionesDisponibles = estadoSimulacion.getCamiones().stream()
                        .filter(c -> "Disponible".equals(c.getEstado()))
                        .count();

                System.out.println("   • Camiones en camino: " + camionesEnCamino);
                System.out.println("   • Camiones averiados: " + camionesAveriados);
                System.out.println("   • Camiones disponibles: " + camionesDisponibles);
            }

            // Análisis de rutas y pedidos
            if (estadoSimulacion.getRutasCamiones() != null) {
                System.out.println("📈 BACKEND: Analizando " + estadoSimulacion.getRutasCamiones().size() + " rutas");
                int totalPedidos = estadoSimulacion.getRutasCamiones().stream()
                        .mapToInt(ruta -> ruta.getPedidos() != null ? ruta.getPedidos().size() : 0)
                        .sum();
                System.out.println("   • Total de pedidos en rutas: " + totalPedidos);
            }

            // Análisis de almacenes
            if (estadoSimulacion.getAlmacenes() != null) {
                System.out.println("📈 BACKEND: Analizando " + estadoSimulacion.getAlmacenes().size() + " almacenes");
                // Aquí se podría agregar más análisis de almacenes
            }

            // Análisis temporal
            System.out.println("⏰ BACKEND: Datos temporales:");
            System.out.println("   • Timestamp avería: " + estadoSimulacion.getTimestamp());
            System.out.println("   • Hora simulación: " + estadoSimulacion.getHoraSimulacion());
            System.out.println("   • Hora actual: " + estadoSimulacion.getHoraActual());

        } catch (Exception e) {
            System.err.println("❌ BACKEND: Error al analizar estado capturado: " + e.getMessage());
        }
    }

    /**
     * Activa una avería específica.
     *
     * @param averia la avería a activar
     */
    public void activarAveria(Averia averia) {
        averia.setEstado(true);
    }

    /**
     * Desactiva una avería específica.
     *
     * @param averia la avería a desactivar
     */
    public void desactivarAveria(Averia averia) {
        averia.setEstado(false);
    }

    /**
     * Obtiene los códigos únicos de camiones con avería activa.
     *
     * @return Lista de códigos de camiones averiados (sin duplicados)
     */
    public List<String> listarCodigosCamionesAveriados() {
        return averiaRepository.findCodigosCamionesAveriados();
    }

    /**
     * Actualiza el estado de los pedidos asignados a un camión a REGISTRADO.
     * Esto se utiliza cuando un camión sufre una avería TI1 y los pedidos
     * quedan "sueltos".
     *
     * @param codigoCamion Código del camión averiado
     */
    private void actualizarPedidosACamionAveriado(String codigoCamion) {
        try {
            // Obtener el camión por su código
            Camion camion = camionService.listar().stream()
                    .filter(c -> c.getCodigo().equals(codigoCamion))
                    .findFirst()
                    .orElse(null);

            if (camion != null && camion.getGen() != null && camion.getGen().getRutaFinal() != null) {
                // Buscar todos los pedidos en la ruta del camión y cambiar su estado a
                // REGISTRADO
                for (Nodo nodo : camion.getGen().getRutaFinal()) {
                    if (nodo.getTipoNodo() == TipoNodo.PEDIDO) {
                        Pedido pedido = (Pedido) nodo;
                        // Solo actualizar si el pedido no está ya entregado
                        if (pedido.getEstado() != EstadoPedido.ENTREGADO) {
                            pedido.setEstado(EstadoPedido.REGISTRADO);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // En caso de error, registrar pero no fallar la creación de la avería
            System.err.println("Error al actualizar pedidos del camión " + codigoCamion + ": " + e.getMessage());
        }
    }

    /**
     * Actualiza los estados de los camiones con averías según las fechas de
     * disponibilidad y traslado. Este método se ejecuta durante la simulación
     * para gestionar automáticamente la recuperación de camiones averiados.
     *
     * @param fechaActual Fecha y hora actual de la simulación
     */
    public void actualizarEstadosCamionesAveriados(LocalDateTime fechaActual) {
        List<Averia> averiasActivas = listarActivas();

        for (Averia averia : averiasActivas) {
            if (averia.getCamion() == null || averia.getTipoIncidente() == null) {
                continue;
            }

            String codigoCamion = averia.getCamion().getCodigo();
            TipoIncidente tipoIncidente = averia.getTipoIncidente();

            // Procesar averías que NO requieren traslado (TI1)
            if (!tipoIncidente.isRequiereTraslado()) {
                procesarAveriasSinTraslado(averia, codigoCamion, fechaActual);
            } else {
                // Procesar averías que requieren traslado (TI2, TI3)
                procesarAveriasConTraslado(averia, codigoCamion, fechaActual);
            }
        }
    }

    /**
     * Procesa averías que no requieren traslado (generalmente TI1). Si la fecha
     * de disponibilidad ya pasó, marca el camión como disponible.
     */
    private void procesarAveriasSinTraslado(Averia averia, String codigoCamion, LocalDateTime fechaActual) {
        if (averia.getFechaHoraDisponible() != null
                && esFechaAnteriorSinSegundos(averia.getFechaHoraDisponible(), fechaActual)) {

            // Camión listo para operar
            camionService.cambiarEstado(codigoCamion, EstadoCamion.DISPONIBLE);
            desactivarAveria(averia);

            System.out.println("✅ Camión " + codigoCamion + " recuperado de avería TI1 - Estado: DISPONIBLE");
        }
    }

    /**
     * Procesa averías que requieren traslado (TI2, TI3). Maneja dos fases:
     * traslado al taller y finalización de reparación.
     */
    private void procesarAveriasConTraslado(Averia averia, String codigoCamion, LocalDateTime fechaActual) {
        // Fase 1: Si terminó el tiempo de espera en ruta, trasladar al taller
        if (averia.getFechaHoraFinEsperaEnRuta() != null
                && esFechaAnteriorSinSegundos(averia.getFechaHoraFinEsperaEnRuta(), fechaActual)) {

            // Verificar si el camión aún está en el lugar de la avería
            if (esCamionEnLugarAveria(codigoCamion)) {
                trasladarCamionAlTaller(codigoCamion);
                System.out.println(
                        "🚛 Camión " + codigoCamion + " trasladado al taller - Estado: EN_MANTENIMIENTO_POR_AVERIA");
            }
        }

        // Fase 2: Si terminó la reparación en taller, camión disponible
        if (averia.getFechaHoraDisponible() != null
                && esFechaAnteriorSinSegundos(averia.getFechaHoraDisponible(), fechaActual)) {

            camionService.cambiarEstado(codigoCamion, EstadoCamion.DISPONIBLE);
            desactivarAveria(averia);

            System.out.println("✅ Camión " + codigoCamion + " recuperado de avería "
                    + averia.getTipoIncidente().getCodigo() + " - Estado: DISPONIBLE");
        }
    }

    /**
     * Traslada un camión al almacén central (taller) para reparación.
     */
    private void trasladarCamionAlTaller(String codigoCamion) {
        try {
            // Buscar el almacén central
            Coordenada coordenadaAlmacenCentral = obtenerCoordenadaAlmacenCentral();

            // Cambiar estado y posición del camión
            camionService.cambiarEstado(codigoCamion, EstadoCamion.EN_MANTENIMIENTO_POR_AVERIA);
            camionService.cambiarCoordenada(codigoCamion, coordenadaAlmacenCentral);

        } catch (Exception e) {
            System.err.println("Error al trasladar camión " + codigoCamion + " al taller: " + e.getMessage());
        }
    }

    /**
     * Obtiene la coordenada del almacén central para traslado de camiones
     * averiados.
     */
    private Coordenada obtenerCoordenadaAlmacenCentral() {
        // Buscar en los almacenes el de tipo CENTRAL
        return com.plg.config.DataLoader.almacenes.stream()
                .filter(almacen -> almacen.getTipo() == TipoAlmacen.CENTRAL)
                .map(almacen -> almacen.getCoordenada())
                .findFirst()
                .orElse(new Coordenada(8, 12)); // Coordenada por defecto si no se encuentra
    }

    /**
     * Verifica si un camión está actualmente en el lugar donde ocurrió la
     * avería.
     */
    private boolean esCamionEnLugarAveria(String codigoCamion) {
        try {
            Camion camion = camionService.listar().stream()
                    .filter(c -> c.getCodigo().equals(codigoCamion))
                    .findFirst()
                    .orElse(null);

            return camion != null
                    && camion.getEstado() == EstadoCamion.INMOVILIZADO_POR_AVERIA;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Compara dos fechas ignorando los segundos.
     *
     * @param fecha1 Primera fecha
     * @param fecha2 Segunda fecha
     * @return true si fecha1 es anterior o igual a fecha2 (sin considerar
     *         segundos)
     */
    private boolean esFechaAnteriorSinSegundos(LocalDateTime fecha1, LocalDateTime fecha2) {
        // Truncar a minutos para ignorar segundos
        LocalDateTime fecha1Truncada = fecha1.withSecond(0).withNano(0);
        LocalDateTime fecha2Truncada = fecha2.withSecond(0).withNano(0);

        return fecha1Truncada.isBefore(fecha2Truncada) || fecha1Truncada.isEqual(fecha2Truncada);
    }

    /**
     * Actualiza la lista de pedidos semanales utilizando el backup y el estado
     * capturado.
     * 
     * @param timestampAveria  timestamp cuando ocurrió la avería
     * @param estadoSimulacion estado completo de la simulación capturado
     */
    private void actualizarPedidosSemanalesConBackup(LocalDateTime timestampAveria,
            AveriaConEstadoRequest.EstadoSimulacion estadoSimulacion) {
        try {
            System.out.println("🔄 BACKEND: Iniciando actualización de pedidos semanales con backup...");

            // Verificar que existe backup
            if (!com.plg.utils.Simulacion.existeBackupSimulacion()) {
                System.err.println("❌ BACKEND: No existe backup de simulación para restaurar pedidos");
                return;
            }

            // Obtener información del backup
            com.plg.utils.Simulacion.BackupInfo backupInfo = com.plg.utils.Simulacion.obtenerInfoBackup();
            if (backupInfo == null) {
                System.err.println("❌ BACKEND: No se pudo obtener información del backup");
                return;
            }

            System.out.println("💾 BACKEND: Backup disponible - " + backupInfo.totalPedidosBackup + " pedidos, fecha: "
                    + backupInfo.fechaBackup);

            // La fecha actual es la fecha del paquete actual
            int paqueteActualNumero = GestorHistorialSimulacion.getPaqueteActual();
            IndividuoDto paqueteActual = GestorHistorialSimulacion.obtenerPaquetePorIndice(paqueteActualNumero);

            LocalDateTime fechaActual = paqueteActual.getFechaHoraSimulacion();

            LocalDateTime fechaBackup = backupInfo.fechaBackup;

            System.out.println("📅 BACKEND: Rango de fechas para filtrar pedidos:");
            System.out.println("   • Fecha backup: " + fechaBackup);
            System.out.println("   • Fecha actual: " + fechaActual);
            System.out.println("   • Timestamp avería: " + timestampAveria);

            // Paso 1: Restaurar pedidos del backup
            boolean restaurado = com.plg.utils.Simulacion.restaurarBackupSimulacion();
            if (!restaurado) {
                System.err.println("❌ BACKEND: No se pudo restaurar el backup de simulación");
                return;
            }

            // Paso 2: Obtener pedidos restaurados
            List<com.plg.entity.Pedido> pedidosRestaurados = com.plg.utils.Simulacion.getPedidosSemanal();
            System.out.println("📦 BACKEND: Pedidos restaurados del backup: " + pedidosRestaurados.size());

            // Paso 3: Filtrar pedidos que ya fueron procesados (entre fechaBackup y
            // fechaActual)
            List<com.plg.entity.Pedido> pedidosParaRemover = new ArrayList<>();
            for (com.plg.entity.Pedido pedido : pedidosRestaurados) {
                if (pedido.getFechaRegistro() != null &&
                        !pedido.getFechaRegistro().isBefore(fechaBackup) &&
                        !pedido.getFechaRegistro().isAfter(fechaActual)) {
                    pedidosParaRemover.add(pedido);
                }
            }

            pedidosRestaurados.removeAll(pedidosParaRemover);
            System.out.println("🗑️ BACKEND: Pedidos filtrados (ya procesados): " + pedidosParaRemover.size());
            System.out.println("📦 BACKEND: Pedidos restantes después del filtrado: " + pedidosRestaurados.size());

            // Paso 4: Agregar pedidos del frontend que no se completaron
            List<com.plg.entity.Pedido> pedidosDelFrontend = extraerPedidosDelEstadoCapturado(estadoSimulacion);
            System.out.println("📱 BACKEND: Pedidos extraídos del frontend: " + pedidosDelFrontend.size());

            // Unir pedidos evitando duplicados
            List<com.plg.entity.Pedido> pedidosFinales = new ArrayList<>(pedidosRestaurados);
            for (com.plg.entity.Pedido pedidoFrontend : pedidosDelFrontend) {
                boolean yaExiste = pedidosFinales.stream()
                        .anyMatch(p -> p.getCodigo() != null && p.getCodigo().equals(pedidoFrontend.getCodigo()));
                if (!yaExiste) {
                    pedidosFinales.add(pedidoFrontend);
                }
            }

            System.out.println("📦 BACKEND: Total de pedidos después de unir: " + pedidosFinales.size());

            // Paso 5: Actualizar la lista de pedidos semanales
            com.plg.utils.Simulacion.setPedidosSemanal(pedidosFinales);

            // Paso 6: Crear nuevo backup con el estado actualizado
            com.plg.utils.Simulacion.crearBackupSimulacion();
            System.out.println("💾 BACKEND: Nuevo backup creado con " + pedidosFinales.size() + " pedidos");

            System.out.println("✅ BACKEND: Pedidos semanales actualizados exitosamente");

        } catch (Exception e) {
            System.err.println("❌ BACKEND: Error al actualizar pedidos semanales con backup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Extrae pedidos del estado capturado del frontend que no se completaron.
     * 
     * @param estadoSimulacion estado completo de la simulación capturado
     * @return lista de pedidos extraídos del frontend
     */
    private List<com.plg.entity.Pedido> extraerPedidosDelEstadoCapturado(
            AveriaConEstadoRequest.EstadoSimulacion estadoSimulacion) {
        List<com.plg.entity.Pedido> pedidosExtraidos = new ArrayList<>();

        try {
            if (estadoSimulacion.getRutasCamiones() != null) {
                System.out.println("🚛 BACKEND: Extrayendo pedidos de " + estadoSimulacion.getRutasCamiones().size()
                        + " rutas de camiones");

                for (var rutaCamion : estadoSimulacion.getRutasCamiones()) {
                    if (rutaCamion.getPedidos() != null) {
                        System.out.println("📦 BACKEND: Procesando " + rutaCamion.getPedidos().size()
                                + " pedidos de camión " + rutaCamion.getId());

                        for (var pedidoFrontend : rutaCamion.getPedidos()) {
                            try {
                                // Crear pedido basado en los datos del frontend
                                com.plg.entity.Pedido pedido = crearPedidoDesdeEstadoCapturado(pedidoFrontend);
                                if (pedido != null) {
                                    pedidosExtraidos.add(pedido);
                                }
                            } catch (Exception e) {
                                System.err.println(
                                        "❌ BACKEND: Error al crear pedido desde estado capturado: " + e.getMessage());
                            }
                        }
                    }
                }
            }

            System.out.println("📦 BACKEND: Total de pedidos extraídos del frontend: " + pedidosExtraidos.size());

        } catch (Exception e) {
            System.err.println("❌ BACKEND: Error al extraer pedidos del estado capturado: " + e.getMessage());
            e.printStackTrace();
        }

        return pedidosExtraidos;
    }

    /**
     * Crea un pedido desde los datos capturados del frontend.
     * 
     * @param pedidoFrontend datos del pedido del frontend
     * @return pedido creado o null si no se pudo crear
     */
    private com.plg.entity.Pedido crearPedidoDesdeEstadoCapturado(Object pedidoFrontend) {
        try {
            // Convertir el objeto a PedidoSimple
            if (!(pedidoFrontend instanceof AveriaConEstadoRequest.PedidoSimple)) {
                System.err.println("❌ BACKEND: Objeto no es del tipo PedidoSimple");
                return null;
            }

            AveriaConEstadoRequest.PedidoSimple pedidoSimple = (AveriaConEstadoRequest.PedidoSimple) pedidoFrontend;

            System.out.println("🔄 BACKEND: Creando pedido desde estado capturado: " + pedidoSimple.getCodigo());

            // Validar datos esenciales
            if (pedidoSimple.getCodigo() == null || pedidoSimple.getCodigo().trim().isEmpty()) {
                System.err.println("❌ BACKEND: Pedido sin código válido");
                return null;
            }

            if (pedidoSimple.getCoordenadaX() == null || pedidoSimple.getCoordenadaY() == null) {
                System.err.println("❌ BACKEND: Pedido sin coordenadas válidas");
                return null;
            }

            // Crear el pedido
            com.plg.entity.Pedido pedido = new com.plg.entity.Pedido();

            // Establecer código
            pedido.setCodigo(pedidoSimple.getCodigo());

            // Establecer coordenadas
            Coordenada coordenada = new Coordenada(pedidoSimple.getCoordenadaX(), pedidoSimple.getCoordenadaY());
            pedido.setCoordenada(coordenada);

            // Establecer volumen GLP
            if (pedidoSimple.getVolumenGLPAsignado() != null) {
                pedido.setVolumenGLPAsignado(pedidoSimple.getVolumenGLPAsignado());
            } else {
                pedido.setVolumenGLPAsignado(0.0); // Valor por defecto
            }

            // Establecer horas límite
            if (pedidoSimple.getHorasLimite() != null) {
                pedido.setHorasLimite(pedidoSimple.getHorasLimite());
            } else {
                pedido.setHorasLimite(24.0); // Valor por defecto
            }

            // Establecer estado del pedido
            if (pedidoSimple.getEstado() != null) {
                try {
                    EstadoPedido estado = EstadoPedido.valueOf(pedidoSimple.getEstado().toUpperCase());
                    pedido.setEstado(estado);
                } catch (IllegalArgumentException e) {
                    // Mapear estados comunes del frontend
                    switch (pedidoSimple.getEstado().toLowerCase()) {
                        case "registrado":
                        case "pendiente":
                            pedido.setEstado(EstadoPedido.REGISTRADO);
                            break;
                        case "planificado":
                            pedido.setEstado(EstadoPedido.PLANIFICADO);
                            break;
                        case "entregado":
                            pedido.setEstado(EstadoPedido.ENTREGADO);
                            break;
                        default:
                            System.err.println("⚠️ BACKEND: Estado no reconocido para pedido " +
                                    pedidoSimple.getCodigo() + ": " + pedidoSimple.getEstado());
                            pedido.setEstado(EstadoPedido.REGISTRADO); // Por defecto
                    }
                }
            } else {
                pedido.setEstado(EstadoPedido.REGISTRADO); // Por defecto
            }

            // Establecer fechas si están disponibles
            if (pedidoSimple.getFechaRegistro() != null) {
                try {
                    LocalDateTime fechaRegistro = LocalDateTime.parse(pedidoSimple.getFechaRegistro());
                    pedido.setFechaRegistro(fechaRegistro);
                } catch (Exception e) {
                    System.err.println("⚠️ BACKEND: Error al parsear fecha de registro para pedido " +
                            pedidoSimple.getCodigo() + ": " + e.getMessage());
                }
            }

            if (pedidoSimple.getFechaLimite() != null) {
                try {
                    LocalDateTime fechaLimite = LocalDateTime.parse(pedidoSimple.getFechaLimite());
                    pedido.setFechaLimite(fechaLimite);
                } catch (Exception e) {
                    System.err.println("⚠️ BACKEND: Error al parsear fecha límite para pedido " +
                            pedidoSimple.getCodigo() + ": " + e.getMessage());
                }
            }

            // Establecer tipo de nodo
            pedido.setTipoNodo(TipoNodo.PEDIDO);

            System.out.println("✅ BACKEND: Pedido creado exitosamente: " + pedido.getCodigo() +
                    " en (" + pedido.getCoordenada().getFila() + "," +
                    pedido.getCoordenada().getColumna() + ") - Estado: " + pedido.getEstado());

            return pedido;

        } catch (Exception e) {
            System.err.println("❌ BACKEND: Error al crear pedido desde estado capturado: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Actualiza camiones y almacenes usando los datos del estado capturado del
     * frontend.
     * 
     * @param estadoSimulacion estado completo de la simulación capturado
     */
    private void actualizarCamionesYAlmacenesConEstadoCapturado(
            AveriaConEstadoRequest.EstadoSimulacion estadoSimulacion) {
        try {
            System.out.println("🔄 BACKEND: Iniciando actualización de camiones y almacenes desde estado frontend...");

            // Actualizar camiones
            if (estadoSimulacion.getCamiones() != null) {
                System.out.println("🚛 BACKEND: Actualizando " + estadoSimulacion.getCamiones().size() + " camiones");

                for (AveriaConEstadoRequest.CamionEstado camionEstado : estadoSimulacion.getCamiones()) {
                    try {
                        camionService.actualizarDesdeEstadoFrontend(
                                camionEstado.getId(),
                                camionEstado.getUbicacion(),
                                camionEstado.getEstado(),
                                camionEstado.getCapacidadActualGLP(),
                                camionEstado.getCombustibleActual());
                    } catch (Exception e) {
                        System.err.println("❌ BACKEND: Error al actualizar camión " + camionEstado.getId() +
                                ": " + e.getMessage());
                        // Continuar con el siguiente camión
                    }
                }

                System.out.println("✅ BACKEND: Camiones actualizados desde estado frontend");
            } else {
                System.out.println("⚠️ BACKEND: No hay datos de camiones en el estado capturado");
            }

            // Actualizar almacenes
            if (estadoSimulacion.getAlmacenes() != null) {
                System.out.println("🏪 BACKEND: Actualizando " + estadoSimulacion.getAlmacenes().size() + " almacenes");

                for (AveriaConEstadoRequest.AlmacenSimple almacenEstado : estadoSimulacion.getAlmacenes()) {
                    try {
                        almacenService.actualizarDesdeEstadoFrontend(
                                almacenEstado.getCoordenadaX(),
                                almacenEstado.getCoordenadaY(),
                                almacenEstado.getCapacidadActualGLP(),
                                almacenEstado.getCapacidadActualCombustible());
                    } catch (Exception e) {
                        System.err.println("❌ BACKEND: Error al actualizar almacén en (" +
                                almacenEstado.getCoordenadaX() + "," + almacenEstado.getCoordenadaY() +
                                "): " + e.getMessage());
                        // Continuar con el siguiente almacén
                    }
                }

                System.out.println("✅ BACKEND: Almacenes actualizados desde estado frontend");
            } else {
                System.out.println("⚠️ BACKEND: No hay datos de almacenes en el estado capturado");
            }

            System.out.println("✅ BACKEND: Actualización de camiones y almacenes completada exitosamente");

        } catch (Exception e) {
            System.err.println("❌ BACKEND: Error general al actualizar camiones y almacenes desde estado frontend: " +
                    e.getMessage());
            e.printStackTrace();
        }
    }
}
