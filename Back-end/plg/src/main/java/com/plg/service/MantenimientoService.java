package com.plg.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.plg.dto.request.MantenimientoRequest;
import com.plg.entity.Camion;
import com.plg.entity.Mantenimiento;
import com.plg.factory.CamionFactory;
import com.plg.repository.MantenimientoRepository;
import com.plg.utils.ExcepcionesPerzonalizadas.InvalidInputException;

/**
 * Servicio para operaciones sobre mantenimientos.
 */
@Service
public class MantenimientoService {

    private final MantenimientoRepository mantenimientoRepository;

    public MantenimientoService(MantenimientoRepository mantenimientoRepository) {
        this.mantenimientoRepository = mantenimientoRepository;
    }

    /**
     * Lista todos los mantenimientos registrados.
     *
     * @return Lista de todos los mantenimientos
     */
    public List<Mantenimiento> listar() {
        return mantenimientoRepository.findAll();
    }

    /**
     * Lista mantenimientos por mes.
     *
     * @param mes el mes (1-12)
     * @return Lista de mantenimientos del mes
     */
    public List<Mantenimiento> listarPorMes(int mes) {
        if (mes < 1 || mes > 12) {
            throw new IllegalArgumentException("El mes debe estar entre 1 y 12");
        }
        return mantenimientoRepository.findByMes(mes);
    }

    /**
     * Lista mantenimientos por día y mes específicos.
     *
     * @param dia el día del mes
     * @param mes el mes (1-12)
     * @return Lista de mantenimientos en esa fecha
     */
    public List<Mantenimiento> listarPorDiaYMes(int dia, int mes) {
        if (mes < 1 || mes > 12) {
            throw new IllegalArgumentException("El mes debe estar entre 1 y 12");
        }
        if (dia < 1 || dia > 31) {
            throw new IllegalArgumentException("El día debe estar entre 1 y 31");
        }
        return mantenimientoRepository.findByDiaAndMes(dia, mes);
    }

    /**
     * Lista mantenimientos por camión.
     *
     * @param camion el camión del cual obtener los mantenimientos
     * @return Lista de mantenimientos del camión
     */
    public List<Mantenimiento> listarPorCamion(Camion camion) {
        return mantenimientoRepository.findByCamion(camion);
    }

    /**
     * Resumen de mantenimientos por mes y camión.
     *
     * @return Mapa con estadísticas de mantenimientos
     */
    public Map<String, Object> resumen() {
        List<Mantenimiento> mantenimientos = mantenimientoRepository.findAll();
        Map<String, Object> datos = new HashMap<>();

        datos.put("total", mantenimientos.size());

        // Agrupar por mes
        Map<String, Long> porMes = mantenimientos.stream()
                .collect(Collectors.groupingBy(
                        m -> "Mes " + m.getMes(),
                        Collectors.counting()));
        datos.put("porMes", porMes);

        // Agrupar por día del mes (solo días 1-31)
        Map<String, Long> porDia = mantenimientos.stream()
                .collect(Collectors.groupingBy(
                        m -> "Día " + m.getDia(),
                        Collectors.counting()));
        datos.put("porDia", porDia);

        // Agrupar por camión
        Map<String, Long> porCamion = mantenimientos.stream()
                .filter(m -> m.getCamion() != null)
                .collect(Collectors.groupingBy(
                        m -> m.getCamion().getCodigo(),
                        Collectors.counting()));
        datos.put("porCamion", porCamion);

        // Estadísticas por mes
        Map<Integer, Integer> mantenimientosPorMes = new HashMap<>();
        for (int mes = 1; mes <= 12; mes++) {
            final int mesActual = mes; // Variable efectivamente final para usar en lambda
            int count = (int) mantenimientos.stream()
                    .filter(m -> m.getMes() == mesActual)
                    .count();
            mantenimientosPorMes.put(mes, count);
        }
        datos.put("estadisticasMensuales", mantenimientosPorMes);

        return datos;
    }

    /**
     * Crea un nuevo mantenimiento utilizando los datos de la solicitud.
     *
     * @param request datos del mantenimiento a crear
     * @return el mantenimiento creado
     * @throws InvalidInputException si los datos son inválidos
     */
    public Mantenimiento agregar(MantenimientoRequest request) throws InvalidInputException {
        // Validaciones
        if (request.getCodigoCamion() == null || request.getCodigoCamion().trim().isEmpty()) {
            throw new InvalidInputException("El código del camión es obligatorio");
        }

        if (request.getDia() < 1 || request.getDia() > 31) {
            throw new InvalidInputException("El día debe estar entre 1 y 31");
        }

        if (request.getMes() < 1 || request.getMes() > 12) {
            throw new InvalidInputException("El mes debe estar entre 1 y 12");
        }

        try {
            // Validar y obtener el camión
            Camion camion = CamionFactory.getCamionPorCodigo(request.getCodigoCamion());

            // Crear el mantenimiento
            Mantenimiento mantenimiento = Mantenimiento.builder()
                    .dia(request.getDia())
                    .mes(request.getMes())
                    .camion(camion)
                    .build();

            return mantenimientoRepository.save(mantenimiento);

        } catch (NoSuchElementException e) {
            throw new InvalidInputException("Camión no encontrado: " + request.getCodigoCamion());
        } catch (Exception e) {
            throw new InvalidInputException("Error al crear el mantenimiento: " + e.getMessage());
        }
    }

    /**
     * Obtiene mantenimientos programados para una fecha específica.
     *
     * @param dia día del mes
     * @param mes mes del año
     * @return Lista de mantenimientos programados
     */
    public List<Mantenimiento> obtenerMantenimientosProgramados(int dia, int mes) {
        return listarPorDiaYMes(dia, mes);
    }

    //!IMPORTANTE: Este método es una versión optimizada de tieneMantenimientoProgramado, que utiliza
    //! una lógica más eficiente para determinar si un camión tiene mantenimiento programado.
    /**
     * Verifica si un camión tiene mantenimiento programado en una fecha
     * específica basándose en el ciclo de mantenimiento cada 2 meses.
     *
     * @param camion el camión a verificar
     * @param dia día del mes
     * @param mes mes del año
     * @return true si tiene mantenimiento programado, false en caso contrario
     */
    public boolean tieneMantenimientoProgramado(Camion camion, int dia, int mes) {
        try {
            // Validar parámetros de entrada
            if (camion == null) {
                System.out.println("ERROR: Camión es null");
                return false;
            }

            System.out.println("DEBUG: Verificando mantenimiento para camión: " + camion.getCodigo()
                    + " en fecha: " + dia + "/" + mes);

            // Obtener el primer mantenimiento del camión (mantenimiento base)
            List<Mantenimiento> mantenimientosCamion = mantenimientoRepository.findByCamion(camion);

            System.out.println("DEBUG: Mantenimientos encontrados para " + camion.getCodigo() + ": " + mantenimientosCamion.size());

            if (mantenimientosCamion.isEmpty()) {
                System.out.println("DEBUG: No hay mantenimientos para el camión " + camion.getCodigo());
                return false;
            }

            // Encontrar el mantenimiento con el mes más bajo (el primero registrado)
            Mantenimiento primerMantenimiento = mantenimientosCamion.stream()
                    .filter(m -> m != null) // Filtrar mantenimientos null
                    .min((m1, m2) -> Integer.compare(m1.getMes(), m2.getMes()))
                    .orElse(null);

            if (primerMantenimiento == null) {
                System.out.println("DEBUG: No se encontró primer mantenimiento válido para " + camion.getCodigo());
                return false;
            }

            System.out.println("DEBUG: Primer mantenimiento - Día: " + primerMantenimiento.getDia()
                    + ", Mes: " + primerMantenimiento.getMes());

            // Verificar si el día coincide con el día del mantenimiento
            if (primerMantenimiento.getDia() != dia) {
                System.out.println("DEBUG: Día no coincide. Esperado: " + primerMantenimiento.getDia()
                        + ", Recibido: " + dia);
                return false;
            }

            // Calcular si el mes actual está en el ciclo de mantenimiento (cada 2 meses)
            int mesInicial = primerMantenimiento.getMes();
            int diferenciaMeses = mes - mesInicial;

            System.out.println("DEBUG: Mes inicial: " + mesInicial
                    + ", Mes consultado: " + mes
                    + ", Diferencia: " + diferenciaMeses);

            // El mantenimiento ocurre cada 2 meses: mesInicial, mesInicial+2, mesInicial+4, etc.
            boolean resultado = diferenciaMeses >= 0 && diferenciaMeses % 2 == 0;

            System.out.println("DEBUG: Resultado final: " + resultado);

            return resultado;

        } catch (Exception e) {
            System.out.println("ERROR en tieneMantenimientoProgramado: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Verifica si un camión tiene mantenimiento programado en una fecha
     * específica usando búsqueda directa (método original para compatibilidad).
     *
     * @param camion el camión a verificar
     * @param dia día del mes
     * @param mes mes del año
     * @return true si tiene mantenimiento programado, false en caso contrario
     */
    public boolean tieneMantenimientoProgramadoDirecto(Camion camion, int dia, int mes) {
        return mantenimientoRepository.findByDiaAndMes(dia, mes)
                .stream()
                .anyMatch(m -> m.getCamion() != null && m.getCamion().equals(camion));
    }

    /**
     * Obtiene todas las fechas de mantenimiento de un camión para un año
     * específico basándose en el ciclo de mantenimiento cada 2 meses.
     *
     * @param camion el camión
     * @param año el año para calcular (opcional, se usa para validaciones
     * futuras)
     * @return Lista de mantenimientos calculados para el año
     */
    public List<Map<String, Integer>> obtenerFechasMantenimientoCamion(Camion camion, int año) {
        List<Mantenimiento> mantenimientosCamion = mantenimientoRepository.findByCamion(camion);

        if (mantenimientosCamion.isEmpty()) {
            return List.of();
        }

        // Encontrar el primer mantenimiento del camión
        Mantenimiento primerMantenimiento = mantenimientosCamion.stream()
                .min((m1, m2) -> Integer.compare(m1.getMes(), m2.getMes()))
                .orElse(null);

        if (primerMantenimiento == null) {
            return List.of();
        }

        List<Map<String, Integer>> fechasMantenimiento = new java.util.ArrayList<>();
        int diaMantenimiento = primerMantenimiento.getDia();
        int mesInicial = primerMantenimiento.getMes();

        // Generar todas las fechas de mantenimiento para el año (cada 2 meses)
        for (int mes = mesInicial; mes <= 12; mes += 2) {
            Map<String, Integer> fecha = new HashMap<>();
            fecha.put("dia", diaMantenimiento);
            fecha.put("mes", mes);
            fechasMantenimiento.add(fecha);
        }

        return fechasMantenimiento;
    }
}
