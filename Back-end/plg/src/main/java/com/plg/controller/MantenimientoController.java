package com.plg.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.plg.dto.request.MantenimientoRequest;
import com.plg.service.MantenimientoService;

/**
 * Controlador REST para mantenimientos.
 */
@RestController
@RequestMapping("/api/mantenimientos")
@CrossOrigin(origins = "*")
public class MantenimientoController {

    private final MantenimientoService mantenimientoService;

    public MantenimientoController(MantenimientoService mantenimientoService) {
        this.mantenimientoService = mantenimientoService;
    }

    /**
     * Lista todos los mantenimientos.
     */
    @GetMapping
    public ResponseEntity<?> listar() {
        try {
            return ResponseEntity.ok(mantenimientoService.listar());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener mantenimientos: " + e.getMessage());
        }
    }

    /**
     * Lista mantenimientos por mes.
     */
    @GetMapping("/mes")
    public ResponseEntity<?> listarPorMes(@RequestParam int mes) {
        try {
            return ResponseEntity.ok(mantenimientoService.listarPorMes(mes));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Mes inválido: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al filtrar mantenimientos por mes: " + e.getMessage());
        }
    }

    /**
     * Lista mantenimientos por día y mes específicos.
     */
    @GetMapping("/fecha")
    public ResponseEntity<?> listarPorDiaYMes(@RequestParam int dia, @RequestParam int mes) {
        try {
            return ResponseEntity.ok(mantenimientoService.listarPorDiaYMes(dia, mes));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Fecha inválida: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al filtrar mantenimientos por fecha: " + e.getMessage());
        }
    }

    /**
     * Lista mantenimientos por camión.
     */
    @GetMapping("/camion")
    public ResponseEntity<?> listarPorCamion(@RequestParam String codigoCamion) {
        try {
            // Para este endpoint necesitaríamos obtener el camión primero
            // Por simplicidad, retornamos los mantenimientos filtrados por código
            return ResponseEntity.ok(mantenimientoService.listar()
                    .stream()
                    .filter(m -> m.getCamion() != null
                    && m.getCamion().getCodigo().equals(codigoCamion))
                    .toList());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al filtrar mantenimientos por camión: " + e.getMessage());
        }
    }

    /**
     * Obtiene mantenimientos programados para una fecha específica.
     */
    @GetMapping("/programados")
    public ResponseEntity<?> obtenerProgramados(@RequestParam int dia, @RequestParam int mes) {
        try {
            return ResponseEntity.ok(mantenimientoService.obtenerMantenimientosProgramados(dia, mes));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Fecha inválida: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener mantenimientos programados: " + e.getMessage());
        }
    }

    /**
     * Resumen de mantenimientos por mes y camión.
     */
    @GetMapping("/resumen")
    public ResponseEntity<?> resumen() {
        try {
            return ResponseEntity.ok(mantenimientoService.resumen());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener resumen: " + e.getMessage());
        }
    }

    /**
     * Agrega un nuevo mantenimiento.
     */
    @PostMapping
    public ResponseEntity<?> agregar(@RequestBody MantenimientoRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(mantenimientoService.agregar(request));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al crear mantenimiento: " + e.getMessage());
        }
    }

    /**
     * Verifica si un camión tiene mantenimiento programado en una fecha
     * específica usando el cálculo basado en ciclos de mantenimiento.
     */
    @GetMapping("/verificar-ciclo")
    public ResponseEntity<?> verificarMantenimientoCiclo(@RequestParam String codigoCamion,
            @RequestParam int dia,
            @RequestParam int mes) {
        try {
            // Obtener el camión usando CamionFactory
            com.plg.entity.Camion camion = com.plg.factory.CamionFactory.getCamionPorCodigo(codigoCamion);

            boolean tieneMantenimiento = mantenimientoService.tieneMantenimientoProgramado(camion, dia, mes);

            return ResponseEntity.ok(Map.of(
                    "codigoCamion", codigoCamion,
                    "dia", dia,
                    "mes", mes,
                    "tieneMantenimientoCiclo", tieneMantenimiento,
                    "metodo", "calculo_basado_en_ciclo"
            ));
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Camión no encontrado: " + codigoCamion);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al verificar mantenimiento: " + e.getMessage());
        }
    }

    /**
     * Obtiene todas las fechas de mantenimiento de un camión para el año actual
     * basándose en el ciclo de mantenimiento cada 2 meses.
     */
    @GetMapping("/fechas-camion")
    public ResponseEntity<?> obtenerFechasMantenimientoCamion(@RequestParam String codigoCamion,
            @RequestParam(defaultValue = "2025") int año) {
        try {
            // Obtener el camión usando CamionFactory
            com.plg.entity.Camion camion = com.plg.factory.CamionFactory.getCamionPorCodigo(codigoCamion);

            List<Map<String, Integer>> fechas = mantenimientoService.obtenerFechasMantenimientoCamion(camion, año);

            return ResponseEntity.ok(Map.of(
                    "codigoCamion", codigoCamion,
                    "año", año,
                    "fechasMantenimiento", fechas,
                    "totalMantenimientos", fechas.size()
            ));
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Camión no encontrado: " + codigoCamion);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al obtener fechas de mantenimiento: " + e.getMessage());
        }
    }

    /**
     * Verifica si un camión tiene mantenimiento programado en una fecha
     * específica (método directo original para comparación).
     */
    @GetMapping("/verificar-directo")
    public ResponseEntity<?> verificarMantenimientoDirecto(@RequestParam String codigoCamion,
            @RequestParam int dia,
            @RequestParam int mes) {
        try {
            // Obtener el camión usando CamionFactory
            com.plg.entity.Camion camion = com.plg.factory.CamionFactory.getCamionPorCodigo(codigoCamion);

            boolean tieneMantenimiento = mantenimientoService.tieneMantenimientoProgramadoDirecto(camion, dia, mes);

            return ResponseEntity.ok(Map.of(
                    "codigoCamion", codigoCamion,
                    "dia", dia,
                    "mes", mes,
                    "tieneMantenimientoDirecto", tieneMantenimiento,
                    "metodo", "busqueda_directa"
            ));
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Camión no encontrado: " + codigoCamion);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al verificar mantenimiento: " + e.getMessage());
        }
    }

    /**
     * Verifica si un camión tiene mantenimiento programado en una fecha
     * específica usando el método de cálculo por ciclos (método principal).
     */
    @GetMapping("/verificar")
    public ResponseEntity<?> verificarMantenimiento(@RequestParam String codigoCamion,
            @RequestParam int dia,
            @RequestParam int mes) {
        try {
            System.out.println("DEBUG: Verificando mantenimiento - Camión: " + codigoCamion
                    + ", Día: " + dia + ", Mes: " + mes);

            // Validar parámetros
            if (codigoCamion == null || codigoCamion.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Código de camión es requerido");
            }

            if (dia < 1 || dia > 31) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Día debe estar entre 1 y 31");
            }

            if (mes < 1 || mes > 12) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Mes debe estar entre 1 y 12");
            }

            // Obtener el camión usando CamionFactory
            com.plg.entity.Camion camion = com.plg.factory.CamionFactory.getCamionPorCodigo(codigoCamion);

            if (camion == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Camión no encontrado: " + codigoCamion);
            }

            System.out.println("DEBUG: Camión encontrado: " + camion.getCodigo());

            // Usar el método principal de cálculo por ciclos
            boolean tieneMantenimiento = mantenimientoService.tieneMantenimientoProgramado(camion, dia, mes);

            return ResponseEntity.ok(Map.of(
                    "codigoCamion", codigoCamion,
                    "dia", dia,
                    "mes", mes,
                    "tieneMantenimiento", tieneMantenimiento,
                    "metodo", "calculo_por_ciclos"
            ));
        } catch (java.util.NoSuchElementException e) {
            System.out.println("ERROR: Camión no encontrado - " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Camión no encontrado: " + codigoCamion);
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al verificar mantenimiento: " + e.getMessage());
        }
    }

    /**
     * Verifica si un camión tiene mantenimiento programado en una fecha
     * específica usando búsqueda directa (método alternativo para comparación).
     */
    @GetMapping("/verificar-simple")
    public ResponseEntity<?> verificarMantenimientoSimple(@RequestParam String codigoCamion,
            @RequestParam int dia,
            @RequestParam int mes) {
        try {
            // Búsqueda simple por filtro directo
            boolean tieneMantenimiento = mantenimientoService.listarPorDiaYMes(dia, mes)
                    .stream()
                    .anyMatch(m -> m.getCamion() != null
                    && m.getCamion().getCodigo().equals(codigoCamion));

            return ResponseEntity.ok(Map.of(
                    "codigoCamion", codigoCamion,
                    "dia", dia,
                    "mes", mes,
                    "tieneMantenimiento", tieneMantenimiento,
                    "metodo", "busqueda_simple"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al verificar mantenimiento: " + e.getMessage());
        }
    }
}
