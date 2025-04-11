package com.plg.controller;

import com.plg.entity.Mantenimiento;
import com.plg.service.MantenimientoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/mantenimientos")
public class MantenimientoController {

    @Autowired
    private MantenimientoService mantenimientoService;

    @GetMapping
    public ResponseEntity<List<Mantenimiento>> getAllMantenimientos() {
        return ResponseEntity.ok(mantenimientoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mantenimiento> getMantenimientoById(@PathVariable Long id) {
        return mantenimientoService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/camion/{codigoCamion}")
    public ResponseEntity<List<Mantenimiento>> getMantenimientosByCamion(@PathVariable String codigoCamion) {
        return ResponseEntity.ok(mantenimientoService.findByCamion(codigoCamion));
    }
    
    @GetMapping("/periodo")
    public ResponseEntity<List<Mantenimiento>> getMantenimientosPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        return ResponseEntity.ok(mantenimientoService.findByPeriodo(inicio, fin));
    }

    @PostMapping
    public ResponseEntity<Mantenimiento> createMantenimiento(@RequestBody Mantenimiento mantenimiento) {
        return ResponseEntity.ok(mantenimientoService.save(mantenimiento));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Mantenimiento> updateMantenimiento(@PathVariable Long id, @RequestBody Mantenimiento mantenimiento) {
        return ResponseEntity.ok(mantenimientoService.update(id, mantenimiento));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMantenimiento(@PathVariable Long id) {
        mantenimientoService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/programar")
    public ResponseEntity<List<Mantenimiento>> programarMantenimientosPreventivos() {
        return ResponseEntity.ok(mantenimientoService.programarMantenimientosPreventivos());
    }
}