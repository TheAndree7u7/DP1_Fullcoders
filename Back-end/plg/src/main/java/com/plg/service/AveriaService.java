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
import com.plg.entity.TipoTurno;
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
     * Resumen de averías por estado y tipo.
     *
     * @return Mapa con estadísticas de averías
     */
    public Map<String, Object> resumen() {
        List<Averia> averias = averiaRepository.findAll();
        Map<String, Object> datos = new HashMap<>();

        datos.put("total", averias.size());
        datos.put("activas", averias.stream()
                .mapToLong(a -> a.getEstado() != null && a.getEstado() ? 1 : 0)
                .sum());
        datos.put("inactivas", averias.stream()
                .mapToLong(a -> a.getEstado() == null || !a.getEstado() ? 1 : 0)
                .sum());

        // Agrupar por tipo de turno
        Map<String, Long> porTurno = averias.stream()
                .filter(a -> a.getTurno() != null)
                .collect(Collectors.groupingBy(
                        a -> a.getTurno().getTipo(),
                        Collectors.counting()));
        datos.put("porTurno", porTurno);

        // Agrupar por tipo de incidente
        Map<String, Long> porTipoIncidente = averias.stream()
                .filter(a -> a.getTipoIncidente() != null)
                .collect(Collectors.groupingBy(
                        a -> a.getTipoIncidente().getTipo(),
                        Collectors.counting()));
        datos.put("porTipoIncidente", porTipoIncidente);

        // Agrupar por camión
        Map<String, Long> porCamion = averias.stream()
                .filter(a -> a.getCamion() != null)
                .collect(Collectors.groupingBy(
                        a -> a.getCamion().getCodigo(),
                        Collectors.counting()));
        datos.put("porCamion", porCamion);

        return datos;
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

        if (request.getTurno() == null || request.getTurno().trim().isEmpty()) {
            throw new InvalidInputException("El turno es obligatorio");
        }

        if (request.getTipoIncidente() == null || request.getTipoIncidente().trim().isEmpty()) {
            throw new InvalidInputException("El tipo de incidente es obligatorio");
        }

        try {
            // Validar y obtener el camión
            Camion camion = CamionFactory.getCamionPorCodigo(request.getCodigoCamion());

            // Validar turno
            TipoTurno turno = new TipoTurno(request.getTurno());

            // Validar tipo de incidente
            TipoIncidente tipoIncidente = new TipoIncidente(request.getTipoIncidente());

            // Crear la avería
            Averia averia = new Averia();
            averia.setCamion(camion);
            averia.setTurno(turno);
            averia.setTipoIncidente(tipoIncidente);
            averia.setFechaInicio(request.getFechaInicio());
            averia.setFechaFin(request.getFechaFin());
            averia.setEstado(false); // Por defecto inactiva

            // Validar fechas si se proporcionan
            if (request.getFechaInicio() != null && request.getFechaFin() != null) {
                if (request.getFechaInicio().isAfter(request.getFechaFin())) {
                    throw new InvalidInputException("La fecha de inicio no puede ser posterior a la fecha de fin");
                }
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
}
