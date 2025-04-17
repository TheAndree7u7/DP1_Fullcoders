package com.plg.service;

import com.plg.entity.Almacen;
import com.plg.entity.Camion;
import com.plg.entity.EntregaParcial;
import com.plg.entity.Pedido;
import com.plg.entity.Ruta;
import com.plg.repository.AlmacenRepository;
import com.plg.repository.CamionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AlmacenCombustibleService {

    @Autowired
    private AlmacenRepository almacenRepository;
    
    @Autowired
    private CamionRepository camionRepository;
    
    // Mapa para almacenar la fecha del último reabastecimiento por almacén
    private Map<Long, LocalDate> ultimoReabastecimientoMap = new HashMap<>();

    /**
     * Inicializa los almacenes según la configuración del sistema
     */
    @Transactional
    public void inicializarAlmacenes() {
        if (almacenRepository.count() == 0) {
            List<Almacen> almacenes = new ArrayList<>();
            
            // Almacén central
            Almacen central = new Almacen();
            central.setNombre("Almacén Central");
            central.setPosX(12);
            central.setPosY(8);
            central.setEsCentral(true);
            central.setPermiteCamionesEstacionados(true);
            central.setCapacidadGLP(Double.MAX_VALUE); // Capacidad ilimitada
            central.setCapacidadActualGLP(Double.MAX_VALUE);
            central.setCapacidadMaximaGLP(Double.MAX_VALUE);
            central.setCapacidadCombustible(Double.MAX_VALUE);
            central.setCapacidadActualCombustible(Double.MAX_VALUE);
            central.setCapacidadMaximaCombustible(Double.MAX_VALUE);
            central.setHoraReabastecimiento(LocalTime.MIDNIGHT);
            central.setActivo(true);
            almacenes.add(central);
            
            // Almacén intermedio Norte
            Almacen norte = new Almacen();
            norte.setNombre("Almacén Intermedio Norte");
            norte.setPosX(42);
            norte.setPosY(42);
            norte.setEsCentral(false);
            norte.setPermiteCamionesEstacionados(false);
            norte.setCapacidadGLP(160);
            norte.setCapacidadActualGLP(160);
            norte.setCapacidadMaximaGLP(160);
            norte.setCapacidadCombustible(160);
            norte.setCapacidadActualCombustible(160);
            norte.setCapacidadMaximaCombustible(160);
            norte.setHoraReabastecimiento(LocalTime.MIDNIGHT);
            norte.setActivo(true);
            almacenes.add(norte);
            
            // Almacén intermedio Este
            Almacen este = new Almacen();
            este.setNombre("Almacén Intermedio Este");
            este.setPosX(63);
            este.setPosY(3);
            este.setEsCentral(false);
            este.setPermiteCamionesEstacionados(false);
            este.setCapacidadGLP(160);
            este.setCapacidadActualGLP(160);
            este.setCapacidadMaximaGLP(160);
            este.setCapacidadCombustible(160);
            este.setCapacidadActualCombustible(160);
            este.setCapacidadMaximaCombustible(160);
            este.setHoraReabastecimiento(LocalTime.MIDNIGHT);
            este.setActivo(true);
            almacenes.add(este);
            
            almacenRepository.saveAll(almacenes);
        }
    }
    
    /**
     * Obtiene la lista de todos los almacenes activos
     */
    public List<Almacen> obtenerAlmacenesActivos() {
        return almacenRepository.findByActivo(true);
    }
    
    /**
     * Obtiene un almacén por su ID
     */
    public Almacen obtenerAlmacenPorId(Long id) {
        return almacenRepository.findById(id).orElse(null);
    }
    
    /**
     * Obtiene el almacén central activo
     */
    public Almacen obtenerAlmacenCentral() {
        return almacenRepository.findByEsCentralAndActivoTrue(true);
    }
    
    /**
     * Obtiene los almacenes intermedios activos
     */
    public List<Almacen> obtenerAlmacenesIntermedios() {
        return almacenRepository.findByEsCentralAndActivo(false, true);
    }
    
    /**
     * Obtiene estadísticas de almacenes para mostrar en la simulación
     */
    public Map<String, Object> obtenerEstadisticasAlmacenes() {
        Map<String, Object> estadisticas = new HashMap<>();
        
        List<Almacen> todosAlmacenes = almacenRepository.findByActivo(true);
        Almacen central = obtenerAlmacenCentral();
        List<Almacen> intermedios = obtenerAlmacenesIntermedios();
        
        estadisticas.put("totalAlmacenes", todosAlmacenes.size());
        estadisticas.put("almacenesCentrales", central != null ? 1 : 0);
        estadisticas.put("almacenesIntermedios", intermedios.size());
        
        // Estadísticas de capacidad de GLP
        double totalCapacidadGLP = todosAlmacenes.stream()
                .mapToDouble(Almacen::getCapacidadGLP)
                .sum();
        double totalActualGLP = todosAlmacenes.stream()
                .mapToDouble(Almacen::getCapacidadActualGLP)
                .sum();
        
        estadisticas.put("capacidadTotalGLP", totalCapacidadGLP);
        estadisticas.put("capacidadActualGLP", totalActualGLP);
        estadisticas.put("porcentajeOcupacionGLP", 
                totalCapacidadGLP > 0 ? (totalActualGLP / totalCapacidadGLP) * 100 : 0);
        
        // Estadísticas de capacidad de combustible
        double totalCapacidadCombustible = todosAlmacenes.stream()
                .mapToDouble(Almacen::getCapacidadCombustible)
                .sum();
        double totalActualCombustible = todosAlmacenes.stream()
                .mapToDouble(Almacen::getCapacidadActualCombustible)
                .sum();
        
        estadisticas.put("capacidadTotalCombustible", totalCapacidadCombustible);
        estadisticas.put("capacidadActualCombustible", totalActualCombustible);
        estadisticas.put("porcentajeOcupacionCombustible", 
                totalCapacidadCombustible > 0 ? (totalActualCombustible / totalCapacidadCombustible) * 100 : 0);
        
        // Información detallada por almacén
        List<Map<String, Object>> detalleAlmacenes = todosAlmacenes.stream()
                .map(this::obtenerDetalleAlmacen)
                .collect(Collectors.toList());
        
        estadisticas.put("detalleAlmacenes", detalleAlmacenes);
        
        return estadisticas;
    }
    
    /**
     * Obtiene detalles específicos de un almacén
     */
    private Map<String, Object> obtenerDetalleAlmacen(Almacen almacen) {
        Map<String, Object> detalle = new HashMap<>();
        
        detalle.put("id", almacen.getId());
        detalle.put("nombre", almacen.getNombre());
        detalle.put("posX", almacen.getPosX());
        detalle.put("posY", almacen.getPosY());
        detalle.put("esCentral", almacen.isEsCentral());
        
        // Detalles de GLP
        detalle.put("capacidadGLP", almacen.getCapacidadGLP());
        detalle.put("capacidadActualGLP", almacen.getCapacidadActualGLP());
        detalle.put("porcentajeGLP", 
                almacen.getCapacidadGLP() > 0 ? (almacen.getCapacidadActualGLP() / almacen.getCapacidadGLP()) * 100 : 0);
        
        // Detalles de combustible
        detalle.put("capacidadCombustible", almacen.getCapacidadCombustible());
        detalle.put("capacidadActualCombustible", almacen.getCapacidadActualCombustible());
        detalle.put("porcentajeCombustible", 
                almacen.getCapacidadCombustible() > 0 ? 
                (almacen.getCapacidadActualCombustible() / almacen.getCapacidadCombustible()) * 100 : 0);
        
        // Información de reabastecimiento para almacenes intermedios
        if (!almacen.isEsCentral()) {
            detalle.put("horaReabastecimiento", almacen.getHoraReabastecimiento());
            
            // Verificar si ya se realizó el reabastecimiento hoy
            LocalDate ultimoReabastecimiento = ultimoReabastecimientoMap.getOrDefault(almacen.getId(), null);
            boolean reabastecidoHoy = ultimoReabastecimiento != null && 
                    ultimoReabastecimiento.equals(LocalDate.now());
            
            detalle.put("reabastecidoHoy", reabastecidoHoy);
            detalle.put("ultimoReabastecimiento", ultimoReabastecimiento);
        }
        
        return detalle;
    }
    
    /**
     * Encuentra el almacén más cercano a una posición
     */
    public Almacen obtenerAlmacenMasCercano(int posX, int posY) {
        List<Almacen> almacenes = obtenerAlmacenesActivos();
        if (almacenes.isEmpty()) {
            return null;
        }
        
        Almacen masCercano = null;
        double distanciaMinima = Double.MAX_VALUE;
        
        for (Almacen almacen : almacenes) {
            double distancia = almacen.calcularDistancia(posX, posY);
            if (distancia < distanciaMinima) {
                distanciaMinima = distancia;
                masCercano = almacen;
            }
        }
        
        return masCercano;
    }
    
    /**
     * Calcula si el camión tiene suficiente combustible para completar una ruta
     * @return Map con estado (boolean) y detalles (String)
     */
    public Map<String, Object> verificarCombustibleSuficiente(Camion camion, List<Pedido> pedidos) {
        Map<String, Object> resultado = new HashMap<>();
        
        // Obtener posición actual del camión
        double posXActual = camion.getPosX();
        double posYActual = camion.getPosY();
        
        double distanciaTotal = 0;
        double combustibleNecesario = 0;
        List<Map<String, Object>> rutaDetallada = new ArrayList<>();
        
        // Si hay pedidos, calcular ruta
        if (pedidos != null && !pedidos.isEmpty()) {
            // Ordenar pedidos (aquí se podría aplicar un algoritmo de ordenamiento más sofisticado)
            List<Pedido> pedidosOrdenados = pedidos;
            
            // Calcular distancia y consumo para cada tramo
            for (Pedido pedido : pedidosOrdenados) {
                // Distancia Manhattan desde posición actual al pedido
                double distanciaTramo = calcularDistanciaReticular(posXActual, posYActual, pedido.getPosX(), pedido.getPosY());
                
                // Verificar el peso combinado (camión + carga actual)
                double pesoCombinado = camion.getTara() + camion.getPesoCarga();
                
                // Calcular consumo para este tramo
                double consumoTramo = camion.calcularConsumoCombustible(distanciaTramo);
                
                Map<String, Object> tramo = new HashMap<>();
                tramo.put("desde_x", posXActual);
                tramo.put("desde_y", posYActual);
                tramo.put("hasta_x", pedido.getPosX());
                tramo.put("hasta_y", pedido.getPosY());
                tramo.put("distancia", distanciaTramo);
                tramo.put("consumo", consumoTramo);
                
                rutaDetallada.add(tramo);
                
                // Actualizar valores para siguiente iteración
                distanciaTotal += distanciaTramo;
                combustibleNecesario += consumoTramo;
                posXActual = pedido.getPosX();
                posYActual = pedido.getPosY();
            }
            
            // Calcular regreso al almacén central
            Almacen central = almacenRepository.findByEsCentralAndActivoTrue(true);
            
            if (central != null) {
                double distanciaRegreso = calcularDistanciaReticular(
                        posXActual, posYActual, central.getPosX(), central.getPosY());
                
                double consumoRegreso = camion.calcularConsumoCombustible(distanciaRegreso);
                
                Map<String, Object> tramoRegreso = new HashMap<>();
                tramoRegreso.put("desde_x", posXActual);
                tramoRegreso.put("desde_y", posYActual);
                tramoRegreso.put("hasta_x", central.getPosX());
                tramoRegreso.put("hasta_y", central.getPosY());
                tramoRegreso.put("distancia", distanciaRegreso);
                tramoRegreso.put("consumo", consumoRegreso);
                tramoRegreso.put("es_regreso", true);
                
                rutaDetallada.add(tramoRegreso);
                distanciaTotal += distanciaRegreso;
                combustibleNecesario += consumoRegreso;
            }
        }
        
        // Verificar si tiene suficiente combustible
        boolean esSuficiente = camion.getCombustibleActual() >= combustibleNecesario;
        double deficit = esSuficiente ? 0 : combustibleNecesario - camion.getCombustibleActual();
        
        resultado.put("suficienteCombustible", esSuficiente);
        resultado.put("distanciaTotal", distanciaTotal);
        resultado.put("combustibleNecesario", combustibleNecesario);
        resultado.put("combustibleActual", camion.getCombustibleActual());
        resultado.put("deficit", deficit);
        resultado.put("rutaDetallada", rutaDetallada);
        
        if (!esSuficiente) {
            resultado.put("almacenesRecomendados", obtenerAlmacenesRecomendados(camion, pedidos, distanciaTotal));
        }
        
        return resultado;
    }
    
    /**
     * Calcula las paradas recomendadas para recargar combustible
     * Considera el mapa reticular, la posición actual del camión,
     * la carga de GLP actual y los pedidos pendientes
     */
    private List<Map<String, Object>> obtenerAlmacenesRecomendados(Camion camion, List<Pedido> pedidos, double distanciaTotal) {
        List<Map<String, Object>> recomendaciones = new ArrayList<>();
        
        // Obtener todos los almacenes activos
        List<Almacen> almacenes = obtenerAlmacenesActivos();
        
        // Posición actual del camión
        double posXActual = camion.getPosX();
        double posYActual = camion.getPosY();
        
        // Combustible actual
        double combustibleActual = camion.getCombustibleActual();
        
        // Calcular distancia máxima con combustible actual
        double distanciaMaxima = camion.calcularDistanciaMaxima();
        
        // Calcular el volumen total de GLP asignado al camión
        double volumenGLPAsignado = camion.getVolumenTotalAsignado();
        
        // Obtener entregas parciales pendientes
        List<EntregaParcial> entregasPendientes = camion.getEntregasPendientes();
        
        // Obtener rutas asignadas a este camión
        List<Ruta> rutasAsignadas = camion.getRutas().stream()
                .filter(r -> r.getEstado() < 2) // Estados 0=Planificada, 1=En curso
                .collect(Collectors.toList());
        
        // Para cada almacén, verificar si está lo suficientemente cerca para llegar
        for (Almacen almacen : almacenes) {
            // Usar distancia Manhattan para mapa reticular
            double distanciaAlAlmacen = Math.abs(almacen.getPosX() - posXActual) + 
                                       Math.abs(almacen.getPosY() - posYActual);
            
            if (distanciaAlAlmacen <= distanciaMaxima && almacen.puedeRecargarCombustible(1.0)) {
                Map<String, Object> recomendacion = new HashMap<>();
                recomendacion.put("almacen", almacen);
                recomendacion.put("distancia", distanciaAlAlmacen);
                recomendacion.put("combustibleNecesario", camion.calcularConsumoCombustible(distanciaAlAlmacen));
                
                // Calculo de cuánto cargar 
                double combustibleParaCargar = camion.getCapacidadTanque() - camion.getCombustibleActual() + 
                                              camion.calcularConsumoCombustible(distanciaAlAlmacen);
                
                // Limitar por la capacidad disponible del almacén
                combustibleParaCargar = Math.min(combustibleParaCargar, almacen.getCapacidadActualCombustible());
                
                recomendacion.put("combustibleACargar", combustibleParaCargar);
                
                // Añadir información sobre capacidad de GLP si es relevante
                if (volumenGLPAsignado > 0 && almacen.puedeProveerGLP(1.0)) {
                    recomendacion.put("puedeRecargarGLP", true);
                    recomendacion.put("capacidadGLPDisponible", almacen.getCapacidadActualGLP());
                }
                
                // Verificar impacto en rutas existentes
                if (!rutasAsignadas.isEmpty()) {
                    // Verificar si este almacén está cerca de alguna ruta existente
                    List<Map<String, Object>> rutasCercanas = new ArrayList<>();
                    
                    for (Ruta ruta : rutasAsignadas) {
                        // Obtener nodos de la ruta
                        for (int i = 0; i < ruta.getNodos().size() - 1; i++) {
                            double nodoX = ruta.getNodos().get(i).getPosX();
                            double nodoY = ruta.getNodos().get(i).getPosY();
                            
                            // Distancia Manhattan desde el nodo al almacén
                            double distanciaNodoAlmacen = Math.abs(almacen.getPosX() - nodoX) + 
                                                        Math.abs(almacen.getPosY() - nodoY);
                            
                            // Si está cerca (dentro de 10 unidades), es una buena parada
                            if (distanciaNodoAlmacen <= 10) {
                                Map<String, Object> rutaCercana = new HashMap<>();
                                rutaCercana.put("rutaCodigo", ruta.getCodigo());
                                rutaCercana.put("distanciaDesvio", distanciaNodoAlmacen);
                                rutaCercana.put("nodoIndice", i);
                                rutasCercanas.add(rutaCercana);
                                break; // Solo agregar una vez por ruta
                            }
                        }
                    }
                    
                    if (!rutasCercanas.isEmpty()) {
                        recomendacion.put("rutasCercanas", rutasCercanas);
                        // Si está muy cerca de una ruta, dar prioridad
                        if (rutasCercanas.stream().anyMatch(r -> (double)r.get("distanciaDesvio") <= 5)) {
                            recomendacion.put("prioridad", "ALTA");
                        }
                    }
                }
                
                // Verificar bloqueos que puedan afectar el camino al almacén
                boolean rutaTieneBloqueos = false;
                // Aquí podría implementarse lógica para verificar bloqueos
                // entre la posición actual y el almacén usando el repositorio de bloqueos
                
                recomendacion.put("rutaTieneBloqueos", rutaTieneBloqueos);
                
                recomendaciones.add(recomendacion);
            }
        }
        
        // Ordenar por prioridad y luego por cercanía
        recomendaciones.sort((r1, r2) -> {
            // Primero por prioridad
            String p1 = (String) r1.getOrDefault("prioridad", "NORMAL");
            String p2 = (String) r2.getOrDefault("prioridad", "NORMAL");
            
            if (!p1.equals(p2)) {
                return p1.equals("ALTA") ? -1 : 1;
            }
            
            // Luego por cercanía
            Double d1 = (Double) r1.get("distancia");
            Double d2 = (Double) r2.get("distancia");
            return d1.compareTo(d2);
        });
        
        return recomendaciones;
    }
    
 
    
    /**
     * Calcula la distancia en mapa reticular entre dos puntos (Manhattan)
     */
    private double calcularDistanciaReticular(double x1, double y1, double x2, double y2) {
        // Distancia Manhattan = |x1 - x2| + |y1 - y2|
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }
    
    /**
     * Convierte distancias del mapa a kilómetros
     * Suponiendo que 1 unidad del mapa = 1 km
     */
    private double convertirAKilometros(double distancia) {
        return distancia;
    }
    
    /**
     * Analiza un caso específico de transporte de GLP
     */
    public Map<String, Object> analizarCasoTransporte(String codigoCamion, double cantidadM3) {
        Map<String, Object> analisis = new HashMap<>();
        
        Optional<Camion> optCamion = camionRepository.findByCodigo(codigoCamion);
        if (!optCamion.isPresent()) {
            analisis.put("error", "Camión no encontrado");
            return analisis;
        }
        
        Camion camion = optCamion.get();
        
        // Calcular el peso de la carga de GLP (2.5 Ton por cada 5m3)
        double pesoGLP = cantidadM3 * 0.5; // 0.5 Ton por m3
        
        // Calcular el peso total
        double pesoTotal = camion.getTara() + pesoGLP;
        
        // Calcular la distancia máxima con tanque lleno
        double consumoPorKm = pesoTotal / 180.0;
        double distanciaMaxima = camion.getCapacidadTanque() / consumoPorKm;
        
        analisis.put("camion", camion.getCodigo());
        analisis.put("tipo", camion.getTipo());
        analisis.put("tara", camion.getTara());
        analisis.put("cargaM3", cantidadM3);
        analisis.put("pesoGLP", pesoGLP);
        analisis.put("pesoTotal", pesoTotal);
        analisis.put("consumoPorKm", consumoPorKm);
        analisis.put("capacidadTanque", camion.getCapacidadTanque());
        analisis.put("distanciaMaxima", distanciaMaxima);
        
        return analisis;
    }
    
    /**
     * Actualiza la hora de reabastecimiento de un almacén
     */
    @Transactional
    public Map<String, Object> actualizarHoraReabastecimiento(Long idAlmacen, LocalTime nuevaHora) {
        Map<String, Object> resultado = new HashMap<>();
        
        Optional<Almacen> optAlmacen = almacenRepository.findById(idAlmacen);
        if (!optAlmacen.isPresent()) {
            resultado.put("exito", false);
            resultado.put("mensaje", "Almacén no encontrado");
            return resultado;
        }
        
        Almacen almacen = optAlmacen.get();
        
        // Solo se puede cambiar la hora de reabastecimiento para almacenes intermedios
        if (almacen.isEsCentral()) {
            resultado.put("exito", false);
            resultado.put("mensaje", "No se puede cambiar la hora de reabastecimiento para el almacén central");
            return resultado;
        }
        
        almacen.setHoraReabastecimiento(nuevaHora);
        almacenRepository.save(almacen);
        
        resultado.put("exito", true);
        resultado.put("mensaje", "Hora de reabastecimiento actualizada correctamente");
        resultado.put("almacen", almacen);
        
        return resultado;
    }
    
    /**
     * Método para verificar y realizar el reabastecimiento programado
     * Se ejecuta cada minuto para verificar si es hora de reabastecer algún almacén
     */
    @Scheduled(fixedRate = 60000) // Ejecuta cada minuto
    @Transactional
    public void verificarReabastecimiento() {
        LocalDateTime ahora = LocalDateTime.now();
        LocalTime horaActual = ahora.toLocalTime();
        LocalDate fechaActual = ahora.toLocalDate();
        
        List<Almacen> almacenesIntermedios = almacenRepository.findByEsCentralAndActivo(false, true);
        
        for (Almacen almacen : almacenesIntermedios) {
            // Verificar si ya se ha realizado el reabastecimiento hoy
            LocalDate ultimoReabastecimiento = ultimoReabastecimientoMap.getOrDefault(almacen.getId(), null);
            
            // Si no se ha reabastecido hoy y es la hora programada (con una tolerancia de 60 segundos)
            if ((ultimoReabastecimiento == null || !ultimoReabastecimiento.equals(fechaActual)) && 
                Math.abs(horaActual.toSecondOfDay() - almacen.getHoraReabastecimiento().toSecondOfDay()) < 60) {
                
                // Realizar el reabastecimiento
                almacen.reabastecer();
                
                // Actualizar el registro del último reabastecimiento
                ultimoReabastecimientoMap.put(almacen.getId(), fechaActual);
                
                // Guardar los cambios
                almacenRepository.save(almacen);
                
                // Registrar el evento de reabastecimiento (podría ser en un log o en otra tabla)
                System.out.println("Reabastecimiento del almacén " + almacen.getNombre() + " realizado a las " + horaActual);
            }
        }
    }
    
    /**
     * Realizar reabastecimiento manual de un almacén
     */
    @Transactional
    public Map<String, Object> reabastecerManual(Long idAlmacen) {
        Map<String, Object> resultado = new HashMap<>();
        
        Optional<Almacen> optAlmacen = almacenRepository.findById(idAlmacen);
        if (!optAlmacen.isPresent()) {
            resultado.put("exito", false);
            resultado.put("mensaje", "Almacén no encontrado");
            return resultado;
        }
        
        Almacen almacen = optAlmacen.get();
        
        if (almacen.isEsCentral()) {
            resultado.put("exito", false);
            resultado.put("mensaje", "El almacén central no necesita reabastecimiento");
            return resultado;
        }
        
        almacen.reabastecer();
        almacenRepository.save(almacen);
        
        // Actualizar registro de último reabastecimiento
        ultimoReabastecimientoMap.put(almacen.getId(), LocalDate.now());
        
        resultado.put("exito", true);
        resultado.put("mensaje", "Reabastecimiento manual realizado con éxito");
        resultado.put("almacen", almacen);
        
        return resultado;
    }
    
    /**
     * Obtener almacenes intermedios con su información de reabastecimiento
     */
    public List<Map<String, Object>> obtenerInfoReabastecimiento() {
        List<Map<String, Object>> resultado = new ArrayList<>();
        
        List<Almacen> almacenesIntermedios = almacenRepository.findByEsCentralAndActivo(false, true);
        
        for (Almacen almacen : almacenesIntermedios) {
            Map<String, Object> info = new HashMap<>();
            info.put("id", almacen.getId());
            info.put("nombre", almacen.getNombre());
            info.put("horaReabastecimiento", almacen.getHoraReabastecimiento());
            info.put("capacidadActualGLP", almacen.getCapacidadActualGLP());
            info.put("capacidadMaximaGLP", almacen.getCapacidadMaximaGLP());
            info.put("porcentajeGLP", (almacen.getCapacidadActualGLP() / almacen.getCapacidadMaximaGLP()) * 100);
            info.put("capacidadActualCombustible", almacen.getCapacidadActualCombustible());
            info.put("capacidadMaximaCombustible", almacen.getCapacidadMaximaCombustible());
            info.put("porcentajeCombustible", (almacen.getCapacidadActualCombustible() / almacen.getCapacidadMaximaCombustible()) * 100);
            
            // Verificar si ya se realizó el reabastecimiento hoy
            LocalDate ultimoReabastecimiento = ultimoReabastecimientoMap.getOrDefault(almacen.getId(), null);
            boolean reabastecidoHoy = ultimoReabastecimiento != null && ultimoReabastecimiento.equals(LocalDate.now());
            
            info.put("reabastecidoHoy", reabastecidoHoy);
            info.put("ultimoReabastecimiento", ultimoReabastecimiento);
            
            resultado.add(info);
        }
        
        return resultado;
    }
    
    /**
     * Recarga combustible en un camión desde un almacén específico
     * @param codigoCamion Código del camión a recargar
     * @param idAlmacen ID del almacén donde se recarga
     * @param cantidadSolicitada Cantidad de combustible solicitada en galones
     * @return Mapa con resultado de la operación
     */
    @Transactional
    public Map<String, Object> recargarCombustible(String codigoCamion, Long idAlmacen, double cantidadSolicitada) {
        Map<String, Object> resultado = new HashMap<>();
        
        Optional<Camion> optCamion = camionRepository.findByCodigo(codigoCamion);
        if (!optCamion.isPresent()) {
            resultado.put("exito", false);
            resultado.put("mensaje", "Camión no encontrado");
            return resultado;
        }
        
        Optional<Almacen> optAlmacen = almacenRepository.findById(idAlmacen);
        if (!optAlmacen.isPresent()) {
            resultado.put("exito", false);
            resultado.put("mensaje", "Almacén no encontrado");
            return resultado;
        }
        
        Camion camion = optCamion.get();
        Almacen almacen = optAlmacen.get();
        
        // Verificar si el camión está físicamente cerca del almacén
        double distanciaAlAlmacen = calcularDistanciaReticular(
                camion.getPosX(), camion.getPosY(), 
                almacen.getPosX(), almacen.getPosY());
        
        if (distanciaAlAlmacen > 5) {  // Si está a más de 5 unidades (5 km) no puede recargar
            resultado.put("exito", false);
            resultado.put("mensaje", "El camión debe estar cerca del almacén para recargar (máx. 5 km). " +
                    "Distancia actual: " + String.format("%.2f", distanciaAlAlmacen) + " km");
            resultado.put("distanciaActual", distanciaAlAlmacen);
            resultado.put("distanciaMaxima", 5);
            return resultado;
        }
        
        // Verificar si es un almacén intermedio y si está recargando GLP (para cumplir regla de negocio)
        if (!almacen.isEsCentral()) {
            // Si es un almacén intermedio, verificar si el camión tiene entregas pendientes
            List<EntregaParcial> entregasPendientes = camion.getEntregasPendientes();
            
            boolean estaRecargandoGLP = false;
            double volumenGLPPendiente = camion.getVolumenTotalAsignado();
            
            // Si el camión no tiene entregas pendientes o no está cargando GLP, no puede recargar en almacén intermedio
            if (entregasPendientes.isEmpty() || volumenGLPPendiente <= 0) {
                resultado.put("exito", false);
                resultado.put("mensaje", "En almacenes intermedios solo se puede recargar combustible cuando hay entregas de GLP pendientes");
                return resultado;
            }
            
            // Si el almacén no tiene GLP suficiente para las entregas pendientes
            if (!almacen.puedeProveerGLP(volumenGLPPendiente)) {
                resultado.put("exito", false);
                resultado.put("mensaje", "El almacén intermedio no tiene suficiente GLP para atender las entregas pendientes");
                resultado.put("volumenGLPDisponible", almacen.getCapacidadActualGLP());
                resultado.put("volumenGLPRequerido", volumenGLPPendiente);
                return resultado;
            }
            
            // Si llega aquí, entonces sí puede recargar en el almacén intermedio
        }
        
        // Calcular cuánto combustible se puede recargar efectivamente
        double espacioDisponibleCamion = camion.getCapacidadTanque() - camion.getCombustibleActual();
        double cantidadEfectiva = Math.min(cantidadSolicitada, 
                                          Math.min(espacioDisponibleCamion, 
                                                  almacen.getCapacidadActualCombustible()));
        
        // Verificar si hay suficiente combustible
        if (cantidadEfectiva <= 0) {
            String mensaje = espacioDisponibleCamion <= 0 
                ? "El tanque del camión está lleno" 
                : "No hay suficiente combustible disponible en el almacén";
                
            resultado.put("exito", false);
            resultado.put("mensaje", mensaje);
            resultado.put("espacioDisponibleCamion", espacioDisponibleCamion);
            resultado.put("combustibleDisponibleAlmacen", almacen.getCapacidadActualCombustible());
            return resultado;
        }
        
        // Realizar la recarga
        almacen.setCapacidadActualCombustible(almacen.getCapacidadActualCombustible() - cantidadEfectiva);
        camion.recargarCombustible(cantidadEfectiva);
        
        // Actualizar las coordenadas del camión a las del almacén si está haciendo una recarga completa
        if (cantidadEfectiva > 10 || cantidadEfectiva >= espacioDisponibleCamion * 0.8) {
            camion.setUltimoAlmacen(almacen);
            camion.setFechaUltimaCarga(LocalDateTime.now());
            
            // Si el camión está en un almacén, actualizar sus coordenadas
            if (distanciaAlAlmacen <= 1) {
                camion.setPosX(almacen.getPosX());
                camion.setPosY(almacen.getPosY());
            }
        }
        
        // Guardar cambios
        almacenRepository.save(almacen);
        camionRepository.save(camion);
        
        // Preparar resultado
        resultado.put("exito", true);
        resultado.put("mensaje", "Recarga de combustible exitosa");
        resultado.put("cantidadRecargada", cantidadEfectiva);
        resultado.put("combustibleActual", camion.getCombustibleActual());
        resultado.put("capacidadTanque", camion.getCapacidadTanque());
        resultado.put("porcentajeTanque", (camion.getCombustibleActual() / camion.getCapacidadTanque()) * 100);
        resultado.put("combustibleRestanteAlmacen", almacen.getCapacidadActualCombustible());
        resultado.put("distanciaMaximaNueva", camion.calcularDistanciaMaxima());
        
        if (almacen.isEsCentral()) {
            resultado.put("tipoAlmacen", "central");
        } else {
            resultado.put("tipoAlmacen", "intermedio");
            // Incluir información de GLP para almacenes intermedios
            resultado.put("glpDisponibleAlmacen", almacen.getCapacidadActualGLP());
        }
        
        return resultado;
    }
}