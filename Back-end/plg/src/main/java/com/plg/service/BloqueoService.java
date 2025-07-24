package com.plg.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.plg.dto.request.BloqueoRequest;
import com.plg.entity.Bloqueo;
import com.plg.entity.Coordenada;
import com.plg.entity.Mapa;
import com.plg.entity.Nodo;
import com.plg.repository.BloqueoRepository;
import com.plg.utils.ExcepcionesPerzonalizadas.InvalidInputException;

/**
 * Servicio para operaciones sobre bloqueos.
 */
@Service
public class BloqueoService {

    private final BloqueoRepository bloqueoRepository;

    public BloqueoService(BloqueoRepository bloqueoRepository) {
        this.bloqueoRepository = bloqueoRepository;
    }

    /**
     * Lista todos los bloqueos registrados.
     *
     * @return Lista de todos los bloqueos
     */
    public List<Bloqueo> listar() {
        return bloqueoRepository.findAll();
    }

    /**
     * Lista bloqueos registrados entre dos fechas.
     *
     * @param inicio fecha y hora inicial
     * @param fin fecha y hora final
     * @return lista de bloqueos en el rango
     */
    public List<Bloqueo> listarPorFecha(LocalDateTime inicio, LocalDateTime fin) {
        return bloqueoRepository.findAllBetween(inicio, fin);
    }

    /**
     * Lista todos los bloqueos activos.
     *
     * @return Lista de bloqueos activos
     */
    public List<Bloqueo> listarActivos() {
        return bloqueoRepository.findAllActive();
    }

    /**
     * Resumen de bloqueos por estado.
     *
     * @return Mapa con estadísticas de bloqueos
     */
    public Map<String, Object> resumen() {
        List<Bloqueo> bloqueos = bloqueoRepository.findAll();
        Map<String, Object> datos = new HashMap<>();

        datos.put("total", bloqueos.size());
        datos.put("activos", bloqueos.stream()
                .mapToLong(b -> b.getActivo() ? 1 : 0)
                .sum());
        datos.put("inactivos", bloqueos.stream()
                .mapToLong(b -> !b.getActivo() ? 1 : 0)
                .sum());

        // Agrupar por mes de inicio
        Map<String, Long> porMes = bloqueos.stream()
                .collect(Collectors.groupingBy(
                        b -> b.getFechaInicio().getMonth().toString(),
                        Collectors.counting()));
        datos.put("porMes", porMes);

        return datos;
    }

    /**
     * Crea un nuevo bloqueo utilizando los datos de la solicitud.
     *
     * @param request datos del bloqueo a crear
     * @return el bloqueo creado
     * @throws InvalidInputException si los datos son inválidos
     */
    public Bloqueo agregar(BloqueoRequest request) throws InvalidInputException {
        // Validaciones
        if (request.getFechaInicio() == null || request.getFechaFin() == null) {
            throw new InvalidInputException("Las fechas de inicio y fin son obligatorias");
        }

        if (request.getFechaInicio().isAfter(request.getFechaFin())) {
            throw new InvalidInputException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }

        if (request.getCoordenadas() == null || request.getCoordenadas().isEmpty()) {
            throw new InvalidInputException("Debe proporcionar al menos una coordenada");
        }

        if (request.getCoordenadas().size() % 2 != 0) {
            throw new InvalidInputException("Debe proporcionar un número par de coordenadas para formar segmentos");
        }

        try {
            // Crear lista de coordenadas
            List<Coordenada> coordenadas = new ArrayList<>();
            for (BloqueoRequest.CoordenadaRequest coordReq : request.getCoordenadas()) {
                Coordenada coordenada = new Coordenada(coordReq.getY(), coordReq.getX());
                coordenadas.add(coordenada);
            }

            // Crear lista de nodos bloqueados
            List<Nodo> nodosBloqueados = new ArrayList<>();
            for (int i = 0; i < coordenadas.size() - 1; i++) {
                Coordenada start = coordenadas.get(i);
                Coordenada end = coordenadas.get(i + 1);

                if (start.getColumna() == end.getColumna()) {
                    // Bloqueo vertical
                    int startRow = Math.min(start.getFila(), end.getFila());
                    int endRow = Math.max(start.getFila(), end.getFila());
                    for (int j = startRow; j <= endRow; j++) {
                        Nodo nodo = Mapa.getInstance().getNodo(j, start.getColumna());
                        nodosBloqueados.add(nodo);
                    }
                } else if (start.getFila() == end.getFila()) {
                    // Bloqueo horizontal
                    int startCol = Math.min(start.getColumna(), end.getColumna());
                    int endCol = Math.max(start.getColumna(), end.getColumna());
                    for (int j = startCol; j <= endCol; j++) {
                        Nodo nodo = Mapa.getInstance().getNodo(start.getFila(), j);
                        nodosBloqueados.add(nodo);
                    }
                } else {
                    throw new InvalidInputException("Los bloqueos solo pueden ser horizontales o verticales");
                }
            }

            // Crear el bloqueo
            Bloqueo bloqueo = new Bloqueo();
            bloqueo.setFechaInicio(request.getFechaInicio());
            bloqueo.setFechaFin(request.getFechaFin());
            bloqueo.setNodosBloqueados(nodosBloqueados);
            bloqueo.setActivo(false); // Por defecto inactivo

            return bloqueoRepository.save(bloqueo);

        } catch (Exception e) {
            throw new InvalidInputException("Error al crear el bloqueo: " + e.getMessage());
        }
    }

    /**
     * Activa un bloqueo específico.
     *
     * @param bloqueo el bloqueo a activar
     */
    public void activarBloqueo(Bloqueo bloqueo) {
        bloqueo.activarBloqueo();
    }

    /**
     * Desactiva un bloqueo específico.
     *
     * @param bloqueo el bloqueo a desactivar
     */
    public void desactivarBloqueo(Bloqueo bloqueo) {
        bloqueo.desactivarBloqueo();
    }
}
