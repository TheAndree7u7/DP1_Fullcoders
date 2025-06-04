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
            Almacen almacen = AlmacenFactory.crearAlmacen(tipo, coord, request.getCapacidadMaxGLP(), request.getCapacidadMaxCombustible());
            return almacenRepository.save(almacen);
        } catch (Exception e) {
            throw new InvalidInputException("No se pudo crear el almacén", e);
        }
    }
}
