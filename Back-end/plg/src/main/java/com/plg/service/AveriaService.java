package com.plg.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.plg.dto.request.AveriaRequest;
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
     * @param codigoCamion código del camión
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
            //!CALCULA LOS DATOS DE LA AVERIA EN BASE A LOS DATOS DEL CAMION Y TIPO DE INCIDENTE
            averia.calcularTurnoOcurrencia();

            averia.getTipoIncidente().initDefaultAverias();
            averia.setFechaHoraFinEsperaEnRuta(averia.calcularFechaHoraFinEsperaEnRuta());
            averia.setFechaHoraDisponible(averia.calcularFechaHoraDisponible());
            averia.setTiempoReparacionEstimado(averia.calcularTiempoInoperatividad());
            //! CALCULA LOS DATOS DE LA AVERIA EN BASE A LOS DATOS DEL CAMION Y TIPO DE INCIDENTE            //?--------------------------------------
            //! Ahora se actualiza el estado del camión a INMOVILIZADO_POR_AVERIA
            camionService.cambiarEstado(request.getCodigoCamion(), EstadoCamion.INMOVILIZADO_POR_AVERIA);

            //! Cambiar la posición del camión con la coordenada del request
            if (request.getCoordenada() != null) {
                camionService.cambiarCoordenada(request.getCodigoCamion(), request.getCoordenada());
            }

            //! Si el tipo de incidente NO es TI1, cambiar el estado de los pedidos asociados a REGISTRADO
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
                // Buscar todos los pedidos en la ruta del camión y cambiar su estado a REGISTRADO
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
                System.out.println("🚛 Camión " + codigoCamion + " trasladado al taller - Estado: EN_MANTENIMIENTO_POR_AVERIA");
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
        return com.plg.utils.Parametros.dataLoader.almacenes.stream()
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
     * segundos)
     */
    private boolean esFechaAnteriorSinSegundos(LocalDateTime fecha1, LocalDateTime fecha2) {
        // Truncar a minutos para ignorar segundos
        LocalDateTime fecha1Truncada = fecha1.withSecond(0).withNano(0);
        LocalDateTime fecha2Truncada = fecha2.withSecond(0).withNano(0);

        return fecha1Truncada.isBefore(fecha2Truncada) || fecha1Truncada.isEqual(fecha2Truncada);
    }
}
