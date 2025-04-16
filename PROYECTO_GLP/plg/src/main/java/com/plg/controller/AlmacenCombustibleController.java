package com.plg.controller;

import com.plg.entity.Almacen;
import com.plg.entity.Camion;
import com.plg.entity.Pedido;
import com.plg.repository.CamionRepository;
import com.plg.repository.PedidoRepository;
import com.plg.service.AlmacenCombustibleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/combustible")
public class AlmacenCombustibleController {

    @Autowired
    private AlmacenCombustibleService almacenCombustibleService;
    
    @Autowired
    private CamionRepository camionRepository;
    
    @Autowired
    private PedidoRepository pedidoRepository;
    
    /**
     * Obtiene todos los almacenes activos
     */
    @GetMapping("/almacenes")
    public ResponseEntity<List<Almacen>> getAlmacenesActivos() {
        return ResponseEntity.ok(almacenCombustibleService.obtenerAlmacenesActivos());
    }
    
    /**
     * Obtiene el almacén más cercano a una posición
     */
    @GetMapping("/almacen-cercano")
    public ResponseEntity<?> getAlmacenMasCercano(@RequestParam int posX, @RequestParam int posY) {
        Almacen almacen = almacenCombustibleService.obtenerAlmacenMasCercano(posX, posY);
        if (almacen != null) {
            return ResponseEntity.ok(almacen);
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("error", "No hay almacenes activos disponibles");
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Verifica si un camión tiene suficiente combustible para una ruta
     */
    @GetMapping("/verificar/{codigoCamion}")
    public ResponseEntity<?> verificarCombustible(@PathVariable String codigoCamion) {
        Optional<Camion> optCamion = camionRepository.findByCodigo(codigoCamion);
        if (!optCamion.isPresent()) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Camión no encontrado con código: " + codigoCamion);
            return ResponseEntity.badRequest().body(response);
        }
        
        Camion camion = optCamion.get();
        List<Pedido> pedidos = pedidoRepository.findByCamion_Codigo(codigoCamion);
        
        Map<String, Object> resultado = almacenCombustibleService.verificarCombustibleSuficiente(camion, pedidos);
        return ResponseEntity.ok(resultado);
    }
    
    /**
     * Recarga combustible en un camión desde un almacén
     */
    @PostMapping("/recargar")
    public ResponseEntity<?> recargarCombustible(
            @RequestParam String codigoCamion, 
            @RequestParam Long idAlmacen,
            @RequestParam double cantidad) {
        
        Map<String, Object> resultado = almacenCombustibleService.recargarCombustible(codigoCamion, idAlmacen, cantidad);
        
        if ((Boolean) resultado.getOrDefault("exito", false)) {
            return ResponseEntity.ok(resultado);
        } else {
            return ResponseEntity.badRequest().body(resultado);
        }
    }
    
    /**
     * Analiza un caso específico de transporte de GLP
     */
    @GetMapping("/analizar-caso")
    public ResponseEntity<?> analizarCasoTransporte(
            @RequestParam String codigoCamion,
            @RequestParam double cantidadM3) {
        
        Map<String, Object> analisis = almacenCombustibleService.analizarCasoTransporte(codigoCamion, cantidadM3);
        
        if (analisis.containsKey("error")) {
            return ResponseEntity.badRequest().body(analisis);
        } else {
            return ResponseEntity.ok(analisis);
        }
    }
    
    /**
     * Inicializa los almacenes en el sistema
     */
    @PostMapping("/inicializar-almacenes")
    public ResponseEntity<?> inicializarAlmacenes() {
        almacenCombustibleService.inicializarAlmacenes();
        
        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Almacenes inicializados correctamente");
        return ResponseEntity.ok(response);
    }

    /**
     * Obtener todos los almacenes activos
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> obtenerInfoReabastecimiento() {
        List<Map<String, Object>> info = almacenCombustibleService.obtenerInfoReabastecimiento();
        return ResponseEntity.ok(info);
    }

    /**
     * Actualizar la hora de reabastecimiento de un almacén
     */
    @PutMapping("/{idAlmacen}/hora-reabastecimiento")
    public ResponseEntity<Map<String, Object>> actualizarHoraReabastecimiento(
            @PathVariable Long idAlmacen,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime hora) {
        
        Map<String, Object> resultado = almacenCombustibleService.actualizarHoraReabastecimiento(idAlmacen, hora);
        
        if ((boolean) resultado.get("exito")) {
            return ResponseEntity.ok(resultado);
        } else {
            return ResponseEntity.badRequest().body(resultado);
        }
    }

    /**
     * Realizar reabastecimiento manual de un almacén
     */
    @PostMapping("/{idAlmacen}/reabastecer")
    public ResponseEntity<Map<String, Object>> reabastecerManual(@PathVariable Long idAlmacen) {
        Map<String, Object> resultado = almacenCombustibleService.reabastecerManual(idAlmacen);
        
        if ((boolean) resultado.get("exito")) {
            return ResponseEntity.ok(resultado);
        } else {
            return ResponseEntity.badRequest().body(resultado);
        }
    }
}