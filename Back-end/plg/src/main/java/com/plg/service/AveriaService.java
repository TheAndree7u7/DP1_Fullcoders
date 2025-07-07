package com.plg.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.plg.dto.request.AveriaRequest;
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

/**
 * Servicio para operaciones sobre averías.
 */
@Service
public class AveriaService {

    private final AveriaRepository averiaRepository;
    private final CamionService camionService;

    public AveriaService(AveriaRepository averiaRepository, CamionService camionService) {
        this.averiaRepository = averiaRepository;
        this.camionService = camionService;
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
            System.out.println("🔄 BACKEND: Estado simulación: " + estadoSimulacion);
            System.out.println("🔄 BACKEND: Request: " + request);
            System.out.println("🔄 BACKEND: Procesando estado completo de la simulación...");

            // Paso 0: Detener la simulación inmediatamente para evitar más paquetes
            System.out.println("🚨 BACKEND: Deteniendo simulación del backend por avería...");
            com.plg.controller.SimulacionController.detenerSimulacionPorAveria();

            // Paso 1: Detener la generación de paquetes futuros inmediatamente
            System.out.println("🛑 BACKEND: Eliminando paquetes futuros...");
            int paquetesEliminados = com.plg.utils.Simulacion.eliminarPaquetesFuturos();
            System.out.println("✅ BACKEND: Paquetes futuros eliminados: " + paquetesEliminados);

            // Paso 2: Generar paquete parche con el estado capturado
            System.out.println("🩹 BACKEND: Generando paquete parche para manejar la avería...");
            // Usar el timestamp de la avería enviado desde el frontend, no el del estado de
            // simulación
            String timestampString = request.getFechaHoraReporte();
            if (timestampString.endsWith("Z")) {
                timestampString = timestampString.substring(0, timestampString.length() - 1);
            }
            LocalDateTime timestampAveria = LocalDateTime.parse(timestampString);

            System.out.println("📅 BACKEND: Usando timestamp de avería correcto: " + timestampAveria);
            System.out.println("📅 BACKEND: (No el timestamp del estado: " + estadoSimulacion.getTimestamp() + ")");

            com.plg.dto.IndividuoDto paqueteParche = com.plg.utils.Simulacion.generarPaqueteParche(
                    timestampAveria,
                    estadoSimulacion);

            if (paqueteParche != null) {
                // Paso 3: Insertar el paquete parche en el historial
                com.plg.utils.Simulacion.insertarPaqueteParche(paqueteParche);
                System.out.println("✅ BACKEND: Paquete parche insertado exitosamente");

                // Obtener información actualizada
                com.plg.utils.Simulacion.SimulacionInfo infoActual = com.plg.utils.Simulacion.obtenerInfoSimulacion();
                System.out.println("📊 BACKEND: Estado actual después del parche:");
                System.out.println("   • Total paquetes: " + infoActual.totalPaquetes);
                System.out.println("   • Paquete actual: " + infoActual.paqueteActual);
                System.out.println("   • En proceso: " + infoActual.enProceso);
            } else {
                System.err.println("❌ BACKEND: No se pudo generar el paquete parche");
            }

            // Paso 4: Análisis del estado para logs y reportes
            analizarEstadoCapturado(estadoSimulacion);

            System.out.println("✅ BACKEND: Estado completo procesado y paquete parche generado exitosamente");
            System.out.println("==========================================================");
            System.out.println("==========================================================");
        } catch (Exception e) {
            System.err.println("⚠️ BACKEND: Error al procesar estado completo: " + e.getMessage());
            e.printStackTrace();
            // No lanzamos excepción aquí para no fallar la creación de la avería
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
}
