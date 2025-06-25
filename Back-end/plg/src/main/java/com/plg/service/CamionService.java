package com.plg.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.plg.dto.CamionDto;
import com.plg.dto.request.CamionRequest;
import com.plg.entity.Camion;
import com.plg.entity.Coordenada;
import com.plg.entity.EstadoCamion;
import com.plg.entity.TipoCamion;
import com.plg.entity.TipoNodo;
import com.plg.factory.CamionFactory;
import com.plg.repository.CamionRepository;
import com.plg.utils.ExcepcionesPerzonalizadas.InvalidInputException;

/**
 * Servicio para operaciones sobre camiones.
 */
@Service
public class CamionService {

    private final CamionRepository camionRepository;

    public CamionService(CamionRepository camionRepository) {
        this.camionRepository = camionRepository;
    }

    /**
     * Lista todos los camiones registrados.
     */
    public List<Camion> listar() {
        return camionRepository.findAll();
    }

    /**
     * Resumen de camiones por tipo.
     */
    public Map<String, Object> resumen() {
        List<Camion> camiones = camionRepository.findAll();
        Map<String, Object> datos = new HashMap<>();
        datos.put("total", camiones.size());
        datos.put("porTipo",
                camiones.stream()
                        .collect(Collectors.groupingBy(c -> c.getTipo().name(), Collectors.counting())));
        return datos;
    }

    /**
     * Resumen de camiones por estado (usando repository).
     */
    public Map<String, Object> resumenPorEstado() {
        Map<String, Long> porEstado = camionRepository.countByEstado();
        Map<String, Object> datos = new HashMap<>();
        datos.put("total", porEstado.values().stream().mapToLong(Long::longValue).sum());
        datos.put("porEstado", porEstado);
        return datos;
    }

    /**
     * Lista los estados posibles de los camiones con su descripción (usando
     * repository).
     */
    public List<Map<String, String>> listarEstados() {
        return camionRepository.listarEstadosPosibles();
    }

    /**
     * Crea un camión nuevo.
     */
    public Camion agregar(CamionRequest request) {
        try {
            Coordenada coordenada = new Coordenada(request.getY(), request.getX());
            TipoCamion tipo = request.getTipo();
            Camion camion = CamionFactory.crearCamionesPorTipo(tipo, request.isOperativo(), coordenada);
            return camionRepository.save(camion);
        } catch (Exception e) {
            throw new InvalidInputException("No se pudo crear el camión", e);
        }
    }

    /**
     * Lista todos los camiones con sus datos principales (estado, id, tipo,
     * coordenada).
     */
    public List<CamionDto> listarCamionesEstado() {
        return camionRepository.listarCamionesEstado();
    }

    /**
     * Cambia el estado de un camión por su código.
     *
     * @param codigoCamion Código del camión
     * @param nuevoEstado Nuevo estado a asignar
     * @return El camión actualizado
     */
    public Camion cambiarEstado(String codigoCamion, EstadoCamion nuevoEstado) {
        Camion camion = camionRepository.findAll().stream()
                .filter(c -> c.getCodigo().equals(codigoCamion))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Camión no encontrado: " + codigoCamion));
        camion.setEstado(nuevoEstado);
        return camion;
    }

    /**
     * Cambia la coordenada de un camión por su código.
     *
     * @param codigoCamion Código del camión
     * @param nuevaCoordenada Nueva coordenada a asignar
     * @return El camión actualizado
     */
    public Camion cambiarCoordenada(String codigoCamion, Coordenada nuevaCoordenada) {
        Camion camion = camionRepository.findAll().stream()
                .filter(c -> c.getCodigo().equals(codigoCamion))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Camión no encontrado: " + codigoCamion));
        camion.setCoordenada(nuevaCoordenada);
        return camion;
    }

    /**
     * Actualiza los datos principales de un camión por su código.
     *
     * @param request DTO con los datos a actualizar
     * @return El camión actualizado
     */
    public Camion actualizarDatosPrincipales(com.plg.dto.request.CamionEstadoUpdateRequest request) {
        Camion camion = camionRepository.findAll().stream()
                .filter(c -> c.getCodigo().equals(request.getCodigo()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Camión no encontrado: " + request.getCodigo()));
        if (request.getCoordenada() != null) {
            camion.setCoordenada(request.getCoordenada());
        }
        if (request.getCombustibleActual() >= 0) {
            camion.setCombustibleActual(request.getCombustibleActual());
        }
        if (request.getCapacidadActualGLP() >= 0) {
            camion.setCapacidadActualGLP(request.getCapacidadActualGLP());
        }
        if (request.getEstado() != null) {
            camion.setEstado(request.getEstado());
        }
        return camion;
    }

    /**
     * Obtiene información detallada de cada camión incluyendo número de pedidos
     * asociados, cantidad de GLP, combustible, kilómetros restantes y estado.
     *
     * @return Lista de mapas con la información detallada de cada camión
     */
    public List<Map<String, Object>> obtenerInfoDetallada() {
        List<Camion> camiones = camionRepository.findAll();
        List<Map<String, Object>> resultado = new ArrayList<>();

        for (Camion camion : camiones) {
            Map<String, Object> infoCamion = new HashMap<>();

            // Información básica del camión
            infoCamion.put("codigo", camion.getCodigo());
            infoCamion.put("tipo", camion.getTipo().name());
            infoCamion.put("estado", camion.getEstado().name());
            infoCamion.put("descripcionEstado", camion.getEstado().getDescripcion());

            // Coordenadas
            if (camion.getCoordenada() != null) {
                Map<String, Integer> coordenada = new HashMap<>();
                infoCamion.put("coordenada", coordenada);
            }

            // Cantidad de GLP
            infoCamion.put("capacidadMaximaGLP", camion.getCapacidadMaximaGLP());
            infoCamion.put("capacidadActualGLP", camion.getCapacidadActualGLP());
            infoCamion.put("porcentajeGLP", (camion.getCapacidadActualGLP() / camion.getCapacidadMaximaGLP()) * 100);

            // Cantidad de gasolina
            infoCamion.put("combustibleMaximo", camion.getCombustibleMaximo());
            infoCamion.put("combustibleActual", camion.getCombustibleActual());
            infoCamion.put("porcentajeCombustible", (camion.getCombustibleActual() / camion.getCombustibleMaximo()) * 100);

            // Kilómetros restantes
            infoCamion.put("distanciaMaxima", camion.getDistanciaMaxima());

            // Contar pedidos asociados al camión (verificando en el Gen y la ruta)
            long numeroPedidos = 0;
            if (camion.getGen() != null && camion.getGen().getRutaFinal() != null) {
                numeroPedidos = camion.getGen().getRutaFinal().stream()
                        .filter(nodo -> nodo.getTipoNodo() == TipoNodo.PEDIDO)
                        .count();
            }
            infoCamion.put("numeroPedidos", numeroPedidos);

            resultado.add(infoCamion);
        }

        // Ordenar primero por número de pedidos (de mayor a menor) y luego por combustible restante (de menor a mayor)
        resultado.sort((a, b) -> {
            // Primero comparar por número de pedidos (descendente)
            Long pedidosA = (Long) a.get("numeroPedidos");
            Long pedidosB = (Long) b.get("numeroPedidos");
            int comparePedidos = pedidosB.compareTo(pedidosA);

            if (comparePedidos != 0) {
                return comparePedidos;
            }

            // Si tienen el mismo número de pedidos, comparar por combustible restante (ascendente)
            Double combustibleA = (Double) a.get("combustibleActual");
            Double combustibleB = (Double) b.get("combustibleActual");
            return combustibleA.compareTo(combustibleB);
        });

        return resultado;
    }
}
