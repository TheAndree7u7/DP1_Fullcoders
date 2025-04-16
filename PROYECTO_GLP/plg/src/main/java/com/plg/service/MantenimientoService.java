package com.plg.service;

import com.plg.entity.Camion;
import com.plg.entity.Mantenimiento;
import com.plg.repository.CamionRepository;
import com.plg.repository.MantenimientoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MantenimientoService {

    @Autowired
    private MantenimientoRepository mantenimientoRepository;
    
    @Autowired
    private CamionRepository camionRepository;

    public List<Mantenimiento> findAll() {
        return mantenimientoRepository.findAll();
    }

    public Optional<Mantenimiento> findById(Long id) {
        return mantenimientoRepository.findById(id);
    }

    public List<Mantenimiento> findByCamion(String codigoCamion) {
        return mantenimientoRepository.findByCamion_Codigo(codigoCamion);
    }
    
    public List<Mantenimiento> findByPeriodo(LocalDate inicio, LocalDate fin) {
        return mantenimientoRepository.findByFechaInicioBetween(inicio, fin);
    }

    public Mantenimiento save(Mantenimiento mantenimiento) {
        // Si es un nuevo mantenimiento, actualiza el estado del camión
        if (mantenimiento.getId() == null && mantenimiento.getEstado() == 1) { // En proceso
            Optional<Camion> camionOpt = camionRepository.findByCodigo(mantenimiento.getCamion().getCodigo());
            if (camionOpt.isPresent()) {
                Camion camion = camionOpt.get();
                camion.setEstado(2); // Camión en mantenimiento
                camionRepository.save(camion);
            }
        }
        
        return mantenimientoRepository.save(mantenimiento);
    }

    public Mantenimiento update(Long id, Mantenimiento mantenimiento) {
        Mantenimiento existingMantenimiento = mantenimientoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mantenimiento no encontrado"));
        
        existingMantenimiento.setCamion(mantenimiento.getCamion());
        existingMantenimiento.setFechaInicio(mantenimiento.getFechaInicio());
        existingMantenimiento.setFechaFin(mantenimiento.getFechaFin());
        existingMantenimiento.setTipo(mantenimiento.getTipo());
        existingMantenimiento.setDescripcion(mantenimiento.getDescripcion());
        
        // Si el estado cambia a finalizado, actualiza el estado del camión
        if (existingMantenimiento.getEstado() != 2 && mantenimiento.getEstado() == 2) { // Finalizado
            Optional<Camion> camionOpt = camionRepository.findByCodigo(mantenimiento.getCamion().getCodigo());
            if (camionOpt.isPresent()) {
                Camion camion = camionOpt.get();
                camion.setEstado(0); // Camión disponible nuevamente
                camionRepository.save(camion);
            }
        }
        
        existingMantenimiento.setEstado(mantenimiento.getEstado());
        
        return mantenimientoRepository.save(existingMantenimiento);
    }

    public void delete(Long id) {
        mantenimientoRepository.deleteById(id);
    }
    
    public List<Mantenimiento> programarMantenimientosPreventivos() {
        List<Mantenimiento> nuevosMantenimientos = new ArrayList<>();
        
        try {
            // Lee el archivo de mantenimientos preventivos programados
            Path path = Paths.get("src/main/resources/data/mantenimientos/mantpreventivo.txt");
            BufferedReader reader = new BufferedReader(new FileReader(path.toFile()));
            
            String line;
            while ((line = reader.readLine()) != null) {
                String[] datos = line.split(",");
                if (datos.length >= 3) {
                    String codigoCamion = datos[0];
                    LocalDate fechaInicio = LocalDate.parse(datos[1]);
                    LocalDate fechaFin = LocalDate.parse(datos[2]);
                    
                    // Verifica si existe el camión
                    Optional<Camion> camionOpt = camionRepository.findByCodigo(codigoCamion);
                    if (camionOpt.isPresent()) {
                        Mantenimiento mantenimiento = new Mantenimiento();
                        mantenimiento.setCamion(camionOpt.get());
                        mantenimiento.setFechaInicio(fechaInicio);
                        mantenimiento.setFechaFin(fechaFin);
                        mantenimiento.setTipo("preventivo");
                        mantenimiento.setDescripcion("Mantenimiento preventivo programado");
                        mantenimiento.setEstado(0); // Programado
                        
                        nuevosMantenimientos.add(mantenimientoRepository.save(mantenimiento));
                    }
                }
            }
            
            reader.close();
            
        } catch (IOException e) {
            throw new RuntimeException("Error al leer el archivo de mantenimientos preventivos", e);
        }
        
        return nuevosMantenimientos;
    }
}