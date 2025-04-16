package com.plg.controller;

import com.plg.entity.Almacen;
import com.plg.repository.AlmacenRepository;
import com.plg.service.AlmacenCombustibleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/almacenes")
public class AlmacenController {

    @Autowired
    private AlmacenCombustibleService almacenService;
    
    @Autowired
    private AlmacenRepository almacenRepository;
    
    /**
     * Inicializar almacenes del sistema
     * Debe ir ANTES del método obtenerAlmacenPorId para evitar conflictos de URL
     */
    @PostMapping("/inicializar")
    public ResponseEntity<?> inicializarAlmacenes() {
        almacenService.inicializarAlmacenes();
        List<Almacen> almacenes = almacenRepository.findAll();
        
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Almacenes inicializados correctamente");
        response.put("total", almacenes.size());
        response.put("almacenes", almacenes);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Nuevo método para crear un almacén de prueba
     * Debe ir ANTES del método obtenerAlmacenPorId para evitar conflictos de URL
     */
    @PostMapping("/crear-prueba")
    public ResponseEntity<?> crearAlmacenPrueba() {
        Almacen almacen = new Almacen();
        almacen.setNombre("Almacén de Prueba");
        almacen.setPosX(10);
        almacen.setPosY(20);
        almacen.setCapacidadGLP(1000.0);
        almacen.setCapacidadActualGLP(800.0);
        almacen.setCapacidadMaximaGLP(1000.0);
        almacen.setCapacidadCombustible(2000.0);
        almacen.setCapacidadActualCombustible(1500.0);
        almacen.setCapacidadMaximaCombustible(2000.0);
        almacen.setEsCentral(false);
        almacen.setPermiteCamionesEstacionados(false);
        almacen.setHoraReabastecimiento(LocalTime.of(6, 0)); // 6:00 AM
        almacen.setActivo(true);
        
        almacenRepository.save(almacen);
        
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Almacén de prueba creado correctamente");
        response.put("almacen", almacen);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Obtiene todos los almacenes activos
     */
    @GetMapping
    public ResponseEntity<?> obtenerAlmacenesActivos() {
        List<Almacen> almacenes = almacenService.obtenerAlmacenesActivos();
        Map<String, Object> response = new HashMap<>();
        response.put("total", almacenes.size());
        response.put("almacenes", almacenes);
        response.put("mensaje", "Total de almacenes activos: " + almacenes.size());
        
        // Obtener todos los almacenes independientemente del estado activo
        List<Almacen> todosLosAlmacenes = almacenRepository.findAll();
        response.put("totalEnBD", todosLosAlmacenes.size());
        response.put("todosLosAlmacenes", todosLosAlmacenes);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Obtiene un almacén por su ID
     * Este método debe ir DESPUÉS de los endpoints específicos como /inicializar y /crear-prueba
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerAlmacenPorId(@PathVariable Long id) {
        Almacen almacen = almacenService.obtenerAlmacenPorId(id);
        if (almacen != null) {
            return ResponseEntity.ok(almacen);
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Almacén no encontrado con ID: " + id);
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Obtiene el almacén central
     */
    @GetMapping("/central")
    public ResponseEntity<?> obtenerAlmacenCentral() {
        Almacen almacen = almacenService.obtenerAlmacenCentral();
        if (almacen != null) {
            return ResponseEntity.ok(almacen);
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Almacén central no encontrado");
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Obtiene almacenes intermedios
     */
    @GetMapping("/intermedios")
    public ResponseEntity<List<Almacen>> obtenerAlmacenesIntermedios() {
        return ResponseEntity.ok(almacenService.obtenerAlmacenesIntermedios());
    }
    
    /**
     * Obtiene estadísticas de almacenes para mostrar en la simulación
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasAlmacenes() {
        return ResponseEntity.ok(almacenService.obtenerEstadisticasAlmacenes());
    }
}