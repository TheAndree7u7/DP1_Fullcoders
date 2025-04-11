package com.plg.service;

import com.plg.entity.Almacen;
import com.plg.entity.Camion;
import com.plg.entity.Pedido;
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
        int posXActual = camion.getPosX();
        int posYActual = camion.getPosY();
        
        double distanciaTotal = 0;
        double combustibleNecesario = 0;
        List<Map<String, Object>> rutaDetallada = new ArrayList<>();
        
        // Si hay pedidos, calcular ruta
        if (pedidos != null && !pedidos.isEmpty()) {
            // Ordenar pedidos (aquí se podría aplicar un algoritmo de ordenamiento más sofisticado)
            List<Pedido> pedidosOrdenados = pedidos;
            
            // Calcular distancia y consumo para cada tramo
            for (Pedido pedido : pedidosOrdenados) {
                // Distancia desde posición actual al pedido
                double distanciaTramo = calcularDistancia(posXActual, posYActual, pedido.getPosX(), pedido.getPosY());
                
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
            Almacen central = almacenRepository.findAll().stream()
                    .filter(Almacen::isEsCentral)
                    .findFirst()
                    .orElse(null);
            
            if (central != null) {
                double distanciaRegreso = calcularDistancia(
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
     */
    private List<Map<String, Object>> obtenerAlmacenesRecomendados(Camion camion, List<Pedido> pedidos, double distanciaTotal) {
        List<Map<String, Object>> recomendaciones = new ArrayList<>();
        
        // Obtener todos los almacenes activos
        List<Almacen> almacenes = obtenerAlmacenesActivos();
        
        // Posición actual del camión
        int posXActual = camion.getPosX();
        int posYActual = camion.getPosY();
        
        // Combustible actual
        double combustibleActual = camion.getCombustibleActual();
        
        // Calcular distancia máxima con combustible actual
        double distanciaMaxima = camion.calcularDistanciaMaxima();
        
        // Para cada almacén, verificar si está lo suficientemente cerca para llegar
        for (Almacen almacen : almacenes) {
            double distanciaAlAlmacen = calcularDistancia(
                    posXActual, posYActual, almacen.getPosX(), almacen.getPosY());
            
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
                
                recomendaciones.add(recomendacion);
            }
        }
        
        // Ordenar por cercanía
        recomendaciones.sort(Comparator.comparingDouble(r -> (Double) r.get("distancia")));
        
        return recomendaciones;
    }
    
    /**
     * Recarga combustible en un camión desde un almacén específico
     */
    @Transactional
    public Map<String, Object> recargarCombustible(String codigoCamion, Long idAlmacen, double cantidadSolicitada) {
        Map<String, Object> resultado = new HashMap<>();
        
        Optional<Camion> optCamion = camionRepository.findById(codigoCamion);
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
        
        // Verificar si es un almacén intermedio y si está recargando GLP (para cumplir regla de negocio)
        boolean estaRecargandoGLP = camion.getPesoCarga() < camion.getCapacidad();
        if (!almacen.isEsCentral() && !estaRecargandoGLP) {
            resultado.put("exito", false);
            resultado.put("mensaje", "En almacenes intermedios solo se puede recargar combustible cuando también se recarga GLP");
            return resultado;
        }
        
        // Calcular cuánto combustible se puede recargar
        double espacioDisponibleCamion = camion.getCapacidadTanque() - camion.getCombustibleActual();
        double cantidadEfectiva = Math.min(cantidadSolicitada, Math.min(espacioDisponibleCamion, almacen.getCapacidadActualCombustible()));
        
        // Verificar si hay suficiente combustible
        if (cantidadEfectiva <= 0) {
            resultado.put("exito", false);
            resultado.put("mensaje", "No hay suficiente combustible disponible o el tanque está lleno");
            return resultado;
        }
        
        // Realizar la recarga
        almacen.setCapacidadActualCombustible(almacen.getCapacidadActualCombustible() - cantidadEfectiva);
        camion.setCombustibleActual(camion.getCombustibleActual() + cantidadEfectiva);
        
        almacenRepository.save(almacen);
        camionRepository.save(camion);
        
        resultado.put("exito", true);
        resultado.put("mensaje", "Recarga de combustible exitosa");
        resultado.put("cantidadRecargada", cantidadEfectiva);
        resultado.put("combustibleActual", camion.getCombustibleActual());
        resultado.put("capacidadTanque", camion.getCapacidadTanque());
        
        return resultado;
    }
    
    /**
     * Calcula la distancia euclidiana entre dos puntos
     */
    private double calcularDistancia(int x1, int y1, int x2, int y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
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
        
        Optional<Camion> optCamion = camionRepository.findById(codigoCamion);
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
}