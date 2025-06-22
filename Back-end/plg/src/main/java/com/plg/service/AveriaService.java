package com.plg.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.plg.dto.request.AveriaRequest;
import com.plg.entity.Averia;
import com.plg.entity.Camion;
import com.plg.entity.TipoIncidente;
import com.plg.entity.TipoIncidente;
import com.plg.factory.CamionFactory;
import com.plg.repository.AveriaRepository;
import com.plg.utils.ExcepcionesPerzonalizadas.InvalidInputException;

/**
 * Servicio para operaciones sobre averías.
 */
@Service
public class AveriaService {

    private final AveriaRepository averiaRepository;

    public AveriaService(AveriaRepository averiaRepository) {
        this.averiaRepository = averiaRepository;
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
     * Lista averías registradas entre dos fechas.
     *
     * @param inicio fecha y hora inicial
     * @param fin fecha y hora final
     * @return lista de averías en el rango
     */
    public List<Averia> listarPorFecha(LocalDateTime inicio, LocalDateTime fin) {
        return averiaRepository.findAllBetween(inicio, fin);
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
            Camion camion = CamionFactory.getCamionPorCodigo(request.getCodigoCamion());
            TipoIncidente tipoIncidente = request.getTipoIncidente();
            // Crear la avería solo con los campos requeridos
            Averia averia = new Averia();
            averia = request
            // Los demás campos se ignoran
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
}
