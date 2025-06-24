package com.plg.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.plg.dto.request.AveriaRequest;
import com.plg.entity.Averia;
import com.plg.entity.Camion;
import com.plg.entity.EstadoCamion;
import com.plg.entity.EstadoPedido;
import com.plg.entity.Nodo;
import com.plg.entity.Pedido;
import com.plg.entity.TipoNodo;
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

            averia.setFechaHoraDisponible(averia.calcularFechaHoraDisponible());
            averia.setTiempoReparacionEstimado(averia.calcularTiempoInoperatividad());
            //! CALCULA LOS DATOS DE LA AVERIA EN BASE A LOS DATOS DEL CAMION Y TIPO DE INCIDENTE            //?--------------------------------------
            //! Ahora se actualiza el estado del camión a INMOVILIZADO_POR_AVERIA
            camionService.cambiarEstado(request.getCodigoCamion(), EstadoCamion.INMOVILIZADO_POR_AVERIA);

            //! Si el tipo de incidente NO es TI1, cambiar el estado de los pedidos asociados a REGISTRADO
            if (!"TI1".equals(averia.getTipoIncidente().getCodigo())) {
                actualizarPedidosACamionAveriado(request.getCodigoCamion());
            }

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
}
