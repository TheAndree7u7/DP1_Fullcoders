package com.plg.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.plg.dto.request.AlmacenRequest;
import com.plg.entity.Almacen;
import com.plg.entity.Coordenada;
import com.plg.entity.TipoAlmacen;
import com.plg.factory.AlmacenFactory;
import com.plg.repository.AlmacenRepository;
import com.plg.utils.ExcepcionesPerzonalizadas.InvalidInputException;

/**
 * Servicio para operaciones sobre almacenes.
 */
@Service
public class AlmacenService {

    private final AlmacenRepository almacenRepository;

    public AlmacenService(AlmacenRepository almacenRepository) {
        this.almacenRepository = almacenRepository;
    }

    /**
     * Lista todos los almacenes.
     */
    public List<Almacen> listar() {
        return almacenRepository.findAll();
    }

    /**
     * Resumen de almacenes por tipo.
     */
    public Map<String, Object> resumen() {
        List<Almacen> almacenes = almacenRepository.findAll();
        Map<String, Object> datos = new HashMap<>();
        datos.put("total", almacenes.size());
        datos.put("porTipo",
                almacenes.stream()
                        .collect(Collectors.groupingBy(a -> a.getTipo().name(), Collectors.counting())));
        return datos;
    }

    /**
     * Crea un almacén nuevo.
     */
    public Almacen agregar(AlmacenRequest request) {
        try {
            Coordenada coord = new Coordenada(request.getY(), request.getX());
            TipoAlmacen tipo = request.getTipo();
            Almacen almacen = AlmacenFactory.crearAlmacen(tipo, coord, request.getCapacidadMaxGLP(),
                    request.getCapacidadMaxCombustible());
            return almacenRepository.save(almacen);
        } catch (Exception e) {
            throw new InvalidInputException("No se pudo crear el almacén", e);
        }
    }

    /**
     * Actualiza un almacén con los datos del frontend durante procesamiento de
     * averías.
     *
     * @param coordenadaX                Coordenada X del almacén
     * @param coordenadaY                Coordenada Y del almacén
     * @param capacidadActualGLP         Capacidad actual de GLP
     * @param capacidadActualCombustible Capacidad actual de combustible
     * @return El almacén actualizado
     */
    public Almacen actualizarDesdeEstadoFrontend(Integer coordenadaX, Integer coordenadaY,
            Double capacidadActualGLP, Double capacidadActualCombustible) {
        try {
            Almacen almacen = almacenRepository.findAll().stream()
                    .filter(a -> a.getCoordenada().getFila() == coordenadaX &&
                            a.getCoordenada().getColumna() == coordenadaY)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Almacén no encontrado en coordenadas: (" +
                            coordenadaX + "," + coordenadaY + ")"));

            // Actualizar capacidad actual de GLP
            if (capacidadActualGLP != null && capacidadActualGLP >= 0) {
                almacen.setCapacidadActualGLP(capacidadActualGLP);
            }

            // Actualizar capacidad actual de combustible
            if (capacidadActualCombustible != null && capacidadActualCombustible >= 0) {
                almacen.setCapacidadCombustible(capacidadActualCombustible);
            }

            System.out.println("🏪 Almacén en (" + coordenadaX + "," + coordenadaY + ") actualizado desde frontend: " +
                    "GLP=" + capacidadActualGLP + ", combustible=" + capacidadActualCombustible);

            return almacen;

        } catch (Exception e) {
            System.err.println("❌ Error al actualizar almacén en (" + coordenadaX + "," + coordenadaY +
                    ") desde frontend: " + e.getMessage());
            throw new RuntimeException("Error al actualizar almacén desde frontend", e);
        }
    }
}
