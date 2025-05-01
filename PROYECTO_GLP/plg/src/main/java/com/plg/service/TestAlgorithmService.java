package com.plg.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.plg.dto.AlgoritmoGeneticoResultadoDTO;
import com.plg.entity.Almacen;
import com.plg.entity.Bloqueo;
import com.plg.entity.Camion;
import com.plg.entity.EstadoCamion;
import com.plg.entity.EstadoPedido;
import com.plg.entity.NodoRuta;
import com.plg.entity.Pedido;
import com.plg.entity.Ruta;
import com.plg.repository.AlmacenRepository;
import com.plg.repository.BloqueoRepository;
import com.plg.repository.CamionRepository;
import com.plg.repository.PedidoRepository;
import com.plg.repository.RutaRepository;

/**
 * Servicio para probar todos los flujos de algoritmos con datos reales
 * Solo se ejecuta cuando el perfil "test-algorithm" está activo
 */
@Service
@Profile("test-algorithm")
public class TestAlgorithmService implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(TestAlgorithmService.class);
    
    // Tamaño del mapa para visualización
    private static final int MAPA_TAMAÑO = 100;

    @Autowired
    private PedidoRepository pedidoRepository;
    
    @Autowired
    private CamionRepository camionRepository;
    
    @Autowired
    private RutaRepository rutaRepository;
    
    @Autowired
    private AlmacenRepository almacenRepository;
    
    @Autowired
    private BloqueoRepository bloqueoRepository;
    
    @Autowired
    private AgrupamientoAPService agrupamientoAPService;
    
    @Autowired
    private AlgoritmoGeneticoService algoritmoGeneticoService;
    
    @Autowired
    private SimulacionTiempoRealService simulacionTiempoRealService;
    
    @Autowired
    private MapaReticularService mapaReticularService;
    
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        logger.info("======================================================");
        logger.info("INICIANDO SECUENCIA DE PRUEBA DE ALGORITMOS");
        logger.info("======================================================");
        
        // Verificar si tenemos los datos requeridos
        verificarDatosCargados();
        
        // Probar Agrupamiento por Afinidad
        probarPropagacionAfinidad();
        
        // Probar Algoritmo Genético
        probarAlgoritmoGenetico();
        
        // Probar simulación en tiempo real
        probarSimulacionTiempoReal();
        
        probarTodosPedidosEntregados();
        
        logger.info("======================================================");
        logger.info("SECUENCIA DE PRUEBA DE ALGORITMOS COMPLETADA");
        logger.info("======================================================");
        
    }
    
    private void verificarDatosCargados() {
        logger.info("Verificando datos cargados para prueba de algoritmos");
        
        // Verificar pedidos
        long pedidosCount = pedidoRepository.count();
        logger.info("Se encontraron {} pedidos en la base de datos", pedidosCount);
        if (pedidosCount == 0) {
            logger.error("¡No se encontraron pedidos! Las pruebas fallarán. Asegúrese de que DataLoader se está ejecutando correctamente.");
        }
        
        // Registrar algunos pedidos de ejemplo para verificación
        List<Pedido> pedidos = pedidoRepository.findAll();
        if (!pedidos.isEmpty()) {
            logger.info("Pedidos de muestra:");
            pedidos.stream().limit(3).forEach(p -> 
                logger.info(" - Pedido {}: Cliente {}, Posición ({},{}), Volumen {} m3", 
                    p.getCodigo(), p.getCliente().getId(), p.getPosX(), p.getPosY(), p.getVolumenGLPAsignado())
            );
        }
        
        // Verificar camiones
        long camionesCount = camionRepository.count();
        logger.info("Se encontraron {} camiones en la base de datos", camionesCount);
        if (camionesCount == 0) {
            logger.error("¡No se encontraron camiones! Las pruebas fallarán. Asegúrese de que DataLoader se está ejecutando correctamente.");
        }
        
        // Verificar almacenes
        long almacenesCount = almacenRepository.count();
        logger.info("Se encontraron {} almacenes en la base de datos", almacenesCount);
        if (almacenesCount == 0) {
            logger.error("¡No se encontraron almacenes! Las pruebas fallarán. Asegúrese de que DataLoader se está ejecutando correctamente.");
        } else {
            Almacen almacenCentral = almacenRepository.findByEsCentralAndActivoTrue(true);
            if (almacenCentral != null) {
                logger.info("Almacén central encontrado en la posición ({},{})", 
                    almacenCentral.getPosX(), almacenCentral.getPosY());
            } else {
                logger.error("¡No se encontró almacén central! Las pruebas fallarán.");
            }
        }
    }
    
    private void probarPropagacionAfinidad() {
        logger.info("===== PROBANDO PROPAGACIÓN DE AFINIDAD =====");
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("alpha", 0.8);
            params.put("beta", 0.2);
            params.put("damping", 0.9);
            params.put("maxIter", 100);
            
            logger.info("Llamando al servicio AP con parámetros: {}", params);
            
            var result = agrupamientoAPService.generarGrupos(params);
            
            logger.info("Resultado AP: clusters={}, totalPedidos={}", 
                result.getClusters().size(), result.getTotalPedidos());
            
            // Registrar información detallada del cluster
            for (int i = 0; i < result.getClusters().size(); i++) {
                var cluster = result.getClusters().get(i);
                logger.info("Cluster {}: centroId={}, centroX={}, centroY={}, puntos={}", 
                    i+1, cluster.getIdGrupo(), cluster.getCentroX(), cluster.getCentroY(), 
                    cluster.getPuntos().size());
                
                // Registrar algunos puntos de muestra en este cluster
                if (!cluster.getPuntos().isEmpty()) {
                    var punto = cluster.getPuntos().get(0);
                    logger.info("  - Punto de muestra: id={}, posición=({},{}), distanciaCentro={}", 
                        punto.getId(), punto.getX(), punto.getY(), punto.getDistanciaCentro());
                }
            }
            
            logger.info("Tiempo de cómputo de Propagación de Afinidad: {} ms", result.getTiempoComputo());
            logger.info("Prueba de Propagación de Afinidad completada exitosamente");
            
        } catch (Exception e) {
            logger.error("Error al probar Propagación de Afinidad", e);
        }
    }
    
    @Transactional
    private void probarAlgoritmoGenetico() {
        logger.info("===== PROBANDO ALGORITMO GENÉTICO =====");
        try {
            // Asegurar que tenemos pedidos pendientes
            List<Pedido> pendientes = pedidoRepository.findByEstado(EstadoPedido.PENDIENTE_PLANIFICACION);
            logger.info("Se encontraron {} pedidos pendientes para generación de ruta", pendientes.size());
            
            if (pendientes.isEmpty()) {
                // Si no hay pedidos pendientes, actualizar algunos pedidos para hacerlos pendientes
                logger.info("No se encontraron pedidos pendientes. Configurando algunos pedidos como PENDIENTE_PLANIFICACION");
                List<Pedido> orders = pedidoRepository.findAll();
                int count = 0;
                for (Pedido p : orders) {
                    if (count < 10) {  // Actualizar hasta 10 pedidos
                        p.setEstado(EstadoPedido.PENDIENTE_PLANIFICACION);
                        pedidoRepository.save(p);
                        count++;
                    } else {
                        break;
                    }
                }
                logger.info("Se actualizaron {} pedidos a PENDIENTE_PLANIFICACION", count);
            }
            
            // Configurar parámetros para algoritmo genético
            Map<String, Object> params = new HashMap<>();
            params.put("numeroRutas", 3);  // Probar con 3 rutas
            params.put("algoritmo", "genetico");
            params.put("poblacion", 50);
            params.put("maxIter", 100);
            
            logger.info("Llamando al servicio AG con parámetros: {}", params);
            
            AlgoritmoGeneticoResultadoDTO result = algoritmoGeneticoService.generarRutas(params);
            
            logger.info("Resultado AG: totalPedidos={}, pedidosAsignados={}, rutasGeneradas={}", 
                result.getTotalPedidos(), result.getPedidosAsignados(), result.getRutasGeneradas());
            
            // Registrar información detallada de la ruta
            for (int i = 0; i < result.getRutas().size(); i++) {
                var ruta = result.getRutas().get(i);
                logger.info("Ruta {}: idRuta={}, camión={}, distancia={}, numeroPedidos={}", 
                    i+1, ruta.getIdRuta(), ruta.getCamionCodigo(), 
                    ruta.getDistanciaTotal(), ruta.getNumeroPedidos());
                
                // Registrar pedidos individuales en esta ruta
                logger.info("  Pedidos en ruta {}:", i+1);
                var pedidosList = ruta.getPedidos();
                for (int j = 0; j < pedidosList.size(); j++) {
                    var pedido = pedidosList.get(j);
                    logger.info("  - Pedido {}: id={}, código={}, pos=({},{}), volumen={}", 
                        j+1, pedido.getId(), pedido.getCodigo(), 
                        pedido.getPosX(), pedido.getPosY(), pedido.getVolumenGLPAsignado());
                }
                
                // Registrar puntos de la ruta
                logger.info("  Puntos exactos del camino de ruta: {}", ruta.getPuntos().size());
                logger.info("  Primeros 3 y últimos 3 puntos:");
                // Registrar primeros 3 puntos
                for (int j = 0; j < Math.min(3, ruta.getPuntos().size()); j++) {
                    var punto = ruta.getPuntos().get(j);
                    logger.info("   - Punto {}: ({},{}) tipo={}", 
                        j, punto.getPosX(), punto.getPosY(), punto.getTipo());
                }
                // Registrar últimos 3 puntos si hay más de 6 puntos
                if (ruta.getPuntos().size() > 6) {
                    for (int j = ruta.getPuntos().size() - 3; j < ruta.getPuntos().size(); j++) {
                        var punto = ruta.getPuntos().get(j);
                        logger.info("   - Punto {}: ({},{}) tipo={}", 
                            j, punto.getPosX(), punto.getPosY(), punto.getTipo());
                    }
                }
            }
            
            // Verificar que las rutas se guardaron en la base de datos
            List<Ruta> savedRoutes = rutaRepository.findAll();
            logger.info("Total de rutas en la base de datos después de AG: {}", savedRoutes.size());
            
            // Verificar que se han asignado camiones
            List<Camion> enRuta = camionRepository.findByEstado(EstadoCamion.EN_RUTA);
            logger.info("Camiones asignados a rutas: {}", enRuta.size());
            
            // NUEVO: Visualizar rutas en el mapa de consola
            visualizarRutasEnConsola();
            
            logger.info("Prueba de Algoritmo Genético completada exitosamente");
            
        } catch (Exception e) {
            logger.error("Error al probar Algoritmo Genético", e);
        }
    }
    
    private void probarSimulacionTiempoReal() {
        logger.info("===== PROBANDO SIMULACIÓN EN TIEMPO REAL =====");
        try {
            // Verificar si tenemos rutas para simular
            List<Ruta> routes = rutaRepository.findAll();
            logger.info("Se encontraron {} rutas para simulación", routes.size());
            
            if (routes.isEmpty()) {
                logger.error("No se encontraron rutas para simulación. Por favor ejecute primero el algoritmo genético.");
                return;
            }
            
            // Encontrar camiones en ruta
            List<Camion> enRuta = camionRepository.findByEstado(EstadoCamion.EN_RUTA);
            logger.info("Camiones en estado de ruta: {}", enRuta.size());
            
            // Registrar posiciones iniciales de los camiones antes de la simulación
            if (!enRuta.isEmpty()) {
                logger.info("Posiciones iniciales de camiones antes de la simulación:");
                for (Camion camion : enRuta) {
                    logger.info("  - Camión {}: Posición inicial ({},{}), Estado: {}, Combustible: {} litros", 
                        camion.getCodigo(), camion.getPosX(), camion.getPosY(), 
                        camion.getEstado(), camion.getCombustibleActual());
                    
                    // Verificar si el camión tiene ruta asignada
                    List<Ruta> rutasDeCamion = rutaRepository.findByCamionIdAndEstado(camion.getId(), 1); // 1 = En curso
                    if (rutasDeCamion.isEmpty()) {
                        logger.warn("  - ¡ALERTA! El camión {} está en estado EN_RUTA pero no tiene rutas activas asignadas", 
                            camion.getCodigo());
                    } else {
                        Ruta ruta = rutasDeCamion.get(0);
                        logger.info("  - Camión {} tiene asignada la ruta {} con {} nodos", 
                            camion.getCodigo(), ruta.getCodigo(), ruta.getNodos().size());
                            
                        // Verificar si hay nodos cliente en la ruta
                        long clienteNodes = ruta.getNodos().stream()
                            .filter(n -> "CLIENTE".equals(n.getTipo()))
                            .count();
                        if (clienteNodes == 0) {
                            logger.warn("  - ¡ALERTA! La ruta {} del camión {} no tiene nodos CLIENTE", 
                                ruta.getCodigo(), camion.getCodigo());
                        } else {
                            logger.info("  - La ruta incluye {} nodos de tipo CLIENTE para entregas", clienteNodes);
                        }
                    }
                }
            }
            
            if (enRuta.isEmpty()) {
                logger.warn("No hay camiones en estado de ruta. Configurando algunos camiones a EN_RUTA");
                List<Camion> trucks = camionRepository.findAll();
                for (int i = 0; i < Math.min(3, trucks.size()); i++) {
                    trucks.get(i).setEstado(EstadoCamion.EN_RUTA);
                    camionRepository.save(trucks.get(i));
                }
                logger.info("Se configuraron {} camiones al estado EN_RUTA", Math.min(3, trucks.size()));
            }
            
            // Iniciar simulación en tiempo real
            logger.info("Iniciando simulación en tiempo real");
            Map<String, Object> result = simulacionTiempoRealService.iniciarSimulacion();
            logger.info("Resultado de inicio de simulación: {}", result);
            
            // Esperar algunas actualizaciones
            logger.info("Esperando 5 segundos para observar actualizaciones de simulación...");
            Thread.sleep(5000);
            
            // Verificar posiciones después de 5 segundos
            logger.info("Verificando posiciones de camiones después de 5 segundos:");
            for (Camion camion : camionRepository.findByEstado(EstadoCamion.EN_RUTA)) {
                logger.info("  - Camión {} ahora en posición ({},{})", 
                    camion.getCodigo(), camion.getPosX(), camion.getPosY());
            }
            
            // Ajustar velocidad
            logger.info("Ajustando velocidad de simulación a 5x (más rápido para mejor observación)");
            result = simulacionTiempoRealService.ajustarVelocidad(5);
            logger.info("Resultado de ajuste de velocidad: {}", result);
            
            // Esperar algunas actualizaciones más
            logger.info("Esperando 10 segundos más con velocidad aumentada...");
            Thread.sleep(10000);
            
            // Verificar posiciones después de la velocidad aumentada
            logger.info("Verificando posiciones de camiones después de velocidad aumentada:");
            for (Camion camion : camionRepository.findByEstado(EstadoCamion.EN_RUTA)) {
                logger.info("  - Camión {} ahora en posición ({},{})", 
                    camion.getCodigo(), camion.getPosX(), camion.getPosY());
            }
            
            // Detener simulación
            logger.info("Deteniendo simulación");
            result = simulacionTiempoRealService.detenerSimulacion();
            logger.info("Resultado de detención de simulación: {}", result);
            
            logger.info("Prueba de simulación en tiempo real completada exitosamente");
            
        } catch (Exception e) {
            logger.error("Error al probar simulación en tiempo real", e);
        }
    }
    
    private void probarTodosPedidosEntregados() {
        logger.info("===== PROBANDO SI TODOS LOS PEDIDOS FUERON ENTREGADOS =====");
        List<Pedido> notDelivered = pedidoRepository.findByEstadoNot(EstadoPedido.ENTREGADO_TOTALMENTE);
        if (notDelivered.isEmpty()) {
            logger.info("Todos los pedidos han sido entregados");
        } else {
            logger.info("Hay {} pedidos no entregados", notDelivered.size());
            
            // Agrupar por estado para análisis
            Map<EstadoPedido, Long> contadorPorEstado = notDelivered.stream()
                .collect(Collectors.groupingBy(Pedido::getEstado, Collectors.counting()));
            
            // Mostrar conteo por cada tipo de estado
            logger.info("Distribución de estados de pedidos no entregados:");
            contadorPorEstado.forEach((estado, cantidad) -> 
                logger.info("  - Estado {}: {} pedidos", estado, cantidad));
            
            // Mostrar detalles de hasta 5 pedidos no entregados para análisis
            logger.info("Muestra de pedidos no entregados (max 5):");
            notDelivered.stream().limit(5).forEach(p -> {
                logger.info("  - Pedido {}: Cliente {}, Posición ({},{}), Volumen {} m3, Estado {}", 
                    p.getCodigo(), p.getCliente().getId(), p.getPosX(), p.getPosY(), 
                    p.getVolumenGLPAsignado(), p.getEstado());
                
                // Verificar si el pedido está en alguna ruta
                List<Ruta> rutasConPedido = rutaRepository.findByPedidosContaining(p);
                if (rutasConPedido.isEmpty()) {
                    logger.info("    * No está asignado a ninguna ruta");
                } else {
                    rutasConPedido.forEach(r -> {
                        Camion camion = r.getCamion();
                        logger.info("    * Asignado a ruta ID={}, Camión={}, Estado camión={}", 
                            r.getId(), camion != null ? camion.getCodigo() : "No asignado", 
                            camion != null ? camion.getEstado() : "N/A");
                    });
                }
            });
            
            // Verificar si hay problemas de capacidad
            boolean posibleProblemaCamiones = camionRepository.count() < notDelivered.size() / 5;
            if (posibleProblemaCamiones) {
                logger.warn("Posible problema de capacidad: hay pocos camiones ({}) para la cantidad de pedidos pendientes ({})", 
                    camionRepository.count(), notDelivered.size());
            }
            
            // Verificar si hay problemas con camiones en mantenimiento o no disponibles
            List<Camion> camionesDisponibles = camionRepository.findByEstado(EstadoCamion.DISPONIBLE);
            long totalCamiones = camionRepository.count();
            long camionesNoDisponibles = totalCamiones - camionesDisponibles.size();
            
            if (camionesNoDisponibles > 0) {
                logger.warn("Hay {} camiones no disponibles que podrían estar afectando las entregas", camionesNoDisponibles);
                
                // Contabilizar camiones por estado para diagnóstico detallado
                Map<EstadoCamion, Long> camionesEstadoCount = camionRepository.findAll().stream()
                    .filter(c -> c.getEstado() != EstadoCamion.DISPONIBLE)
                    .collect(Collectors.groupingBy(Camion::getEstado, Collectors.counting()));
                
                camionesEstadoCount.forEach((estado, cantidad) -> 
                    logger.info("  - {} camiones en estado: {}", cantidad, estado));
            }
        }
    }
    
    /**
     * Visualiza las rutas generadas en la consola para verificar que se están generando correctamente
     * Muestra los bloqueos, pedidos y las rutas de los camiones en un mapa ASCII simple
     */
    private void visualizarRutasEnConsola() {
        logger.info("===== VISUALIZACIÓN DE RUTAS EN EL MAPA =====");
        
        try {
            // Obtener todas las rutas activas
            List<Ruta> rutas = rutaRepository.findByEstado(1); // 1 = En curso
            if (rutas.isEmpty()) {
                logger.info("No hay rutas activas para visualizar");
                return;
            }
            
            // Obtener bloqueos activos
            var bloqueos = bloqueoRepository.findByActivoTrue();
            logger.info("Se encontraron {} bloqueos activos", bloqueos.size());
            
            // Obtener pedidos asignados a rutas
            List<Pedido> pedidosEnRutas = new ArrayList<>();
            for (Ruta ruta : rutas) {
                // Usar el repositorio de pedidos para encontrar los pedidos asociados a la ruta
                List<Pedido> pedidosRuta = ruta.getPedidosAsociados();
                pedidosEnRutas.addAll(pedidosRuta);
            }
            logger.info("Se encontraron {} pedidos asignados a rutas", pedidosEnRutas.size());
            
            // Obtener almacén central
            Almacen almacenCentral = almacenRepository.findByEsCentralAndActivoTrue(true);
            if (almacenCentral == null) {
                logger.warn("No se encontró el almacén central");
            }
            
            // Crear una matriz para representar el mapa (100x100)
            char[][] mapa = new char[MAPA_TAMAÑO][MAPA_TAMAÑO];
            
            // Inicializar mapa con espacios en blanco
            for (int i = 0; i < MAPA_TAMAÑO; i++) {
                for (int j = 0; j < MAPA_TAMAÑO; j++) {
                    mapa[i][j] = ' ';
                }
            }
            
            // Marcar bloqueos en el mapa
            for (var bloqueo : bloqueos) {
                // Marcar área de bloqueo (desde posición inicial a final)
                int x1 = (int)Math.min(bloqueo.getPosXInicio(), bloqueo.getPosXFin());
                int y1 = (int)Math.min(bloqueo.getPosYInicio(), bloqueo.getPosYFin());
                int x2 = (int)Math.max(bloqueo.getPosXInicio(), bloqueo.getPosXFin());
                int y2 = (int)Math.max(bloqueo.getPosYInicio(), bloqueo.getPosYFin());
                
                for (int x = x1; x <= x2 && x < MAPA_TAMAÑO; x++) {
                    for (int y = y1; y <= y2 && y < MAPA_TAMAÑO; y++) {
                        if (x >= 0 && y >= 0) {
                            mapa[y][x] = 'X'; // X representa bloqueo
                        }
                    }
                }
                
                logger.info("Bloqueo: ({},{}) a ({},{}) - {}", 
                    (int)bloqueo.getPosXInicio(), (int)bloqueo.getPosYInicio(), 
                    (int)bloqueo.getPosXFin(), (int)bloqueo.getPosYFin(),
                    bloqueo.getDescripcion());
            }
            
            // Marcar pedidos en el mapa
            for (Pedido pedido : pedidosEnRutas) {
                int x = pedido.getPosX();
                int y = pedido.getPosY();
                if (x >= 0 && x < MAPA_TAMAÑO && y >= 0 && y < MAPA_TAMAÑO) {
                    mapa[y][x] = 'P'; // P representa pedido
                }
            }
            
            // Marcar almacén central
            if (almacenCentral != null) {
                int x = almacenCentral.getPosX();
                int y = almacenCentral.getPosY();
                if (x >= 0 && x < MAPA_TAMAÑO && y >= 0 && y < MAPA_TAMAÑO) {
                    mapa[y][x] = 'A'; // A representa almacén
                }
                logger.info("Almacén central en: ({},{})", x, y);
            }
            
            // Para cada ruta, marcar el trayecto en el mapa
            char rutaMarca = '1'; // Usar números para identificar rutas diferentes
            for (Ruta ruta : rutas) {
                // Obtener los pedidos asociados a esta ruta específica
                List<Pedido> pedidosRuta = ruta.getPedidosAsociados();
                
                logger.info("Ruta {}: Camión {} - {} pedidos, {} nodos", 
                    ruta.getCodigo(), 
                    ruta.getCamion() != null ? ruta.getCamion().getCodigo() : "N/A", 
                    pedidosRuta.size(), 
                    ruta.getNodos().size());
                
                // Recorrer nodos y marcar el trayecto
                for (var nodo : ruta.getNodos()) {
                    int x = nodo.getPosX();
                    int y = nodo.getPosY();
                    if (x >= 0 && x < MAPA_TAMAÑO && y >= 0 && y < MAPA_TAMAÑO) {
                        // No sobrescribir pedidos o almacén
                        if (mapa[y][x] != 'P' && mapa[y][x] != 'A') {
                            mapa[y][x] = rutaMarca;
                        }
                    }
                }
                
                // Incrementar el identificador de ruta para la siguiente
                rutaMarca++;
                if (rutaMarca > '9') rutaMarca = '1'; // Reciclar dígitos
            }
            
            // Imprimir mapa en el log (reducido para legibilidad)
            logger.info("Mapa de Rutas (Leyenda: A=Almacén, P=Pedido, X=Bloqueo, 1-9=Ruta):");
            
            // Determinar límites efectivos del mapa (para no imprimir todo el espacio vacío)
            int minX = MAPA_TAMAÑO, maxX = 0, minY = MAPA_TAMAÑO, maxY = 0;
            for (int y = 0; y < MAPA_TAMAÑO; y++) {
                for (int x = 0; x < MAPA_TAMAÑO; x++) {
                    if (mapa[y][x] != ' ') {
                        minX = Math.min(minX, x);
                        maxX = Math.max(maxX, x);
                        minY = Math.min(minY, y);
                        maxY = Math.max(maxY, y);
                    }
                }
            }
            
            // Ampliar un poco los límites para contexto
            minX = Math.max(0, minX - 5);
            minY = Math.max(0, minY - 5);
            maxX = Math.min(MAPA_TAMAÑO - 1, maxX + 5);
            maxY = Math.min(MAPA_TAMAÑO - 1, maxY + 5);
            
            // Mostrar números de coordenadas en el eje X
            StringBuilder xCoords = new StringBuilder("   ");
            for (int x = minX; x <= maxX; x += 10) {
                xCoords.append(String.format("%10d", x));
            }
            logger.info(xCoords.toString());
            
            // Imprimir mapa reducido
            for (int y = minY; y <= maxY; y++) {
                StringBuilder linea = new StringBuilder();
                linea.append(String.format("%2d |", y));
                for (int x = minX; x <= maxX; x++) {
                    linea.append(mapa[y][x]);
                }
                logger.info(linea.toString());
            }
            
            // Resumen de verificación
            int totalBloqueos = 0, totalPedidos = 0, totalRutaPuntos = 0;
            for (int y = 0; y < MAPA_TAMAÑO; y++) {
                for (int x = 0; x < MAPA_TAMAÑO; x++) {
                    if (mapa[y][x] == 'X') totalBloqueos++;
                    else if (mapa[y][x] == 'P') totalPedidos++;
                    else if (mapa[y][x] >= '1' && mapa[y][x] <= '9') totalRutaPuntos++;
                }
            }
            
            logger.info("Resumen de visualización: {} celdas con bloqueos, {} pedidos, {} puntos de ruta", 
                totalBloqueos, totalPedidos, totalRutaPuntos);
            
            // Verificar si alguna ruta cruza por bloqueos (posible error)
            boolean rutasCruzanBloqueos = false;
            for (Ruta ruta : rutas) {
                for (var nodo : ruta.getNodos()) {
                    for (var bloqueo : bloqueos) {
                        int x = nodo.getPosX();
                        int y = nodo.getPosY();
                        
                        // Verificar si el nodo está en el área del bloqueo
                        boolean enAreaBloqueo = 
                            x >= (int)Math.min(bloqueo.getPosXInicio(), bloqueo.getPosXFin()) &&
                            x <= (int)Math.max(bloqueo.getPosXInicio(), bloqueo.getPosXFin()) &&
                            y >= (int)Math.min(bloqueo.getPosYInicio(), bloqueo.getPosYFin()) && 
                            y <= (int)Math.max(bloqueo.getPosYInicio(), bloqueo.getPosYFin());
                            
                        if (enAreaBloqueo) {
                            logger.warn("ALERTA: La ruta {} del camión {} cruza por un bloqueo en ({},{})!",
                                ruta.getCodigo(), 
                                ruta.getCamion() != null ? ruta.getCamion().getCodigo() : "N/A", 
                                x, y);
                            rutasCruzanBloqueos = true;
                        }
                    }
                }
            }
            
            if (!rutasCruzanBloqueos) {
                logger.info("Verificación exitosa: Ninguna ruta cruza por áreas bloqueadas");
            }
        } catch (Exception e) {
            logger.error("Error al visualizar rutas en consola", e);
        }
    }
}
