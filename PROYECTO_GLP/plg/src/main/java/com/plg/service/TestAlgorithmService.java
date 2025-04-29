package com.plg.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.plg.entity.Camion;
import com.plg.entity.EstadoCamion;
import com.plg.entity.EstadoPedido;
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
                for (int j = 0; j < ruta.getPedidos().size(); j++) {
                    var pedido = ruta.getPedidos().get(j);
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
            
            // Ajustar velocidad
            logger.info("Ajustando velocidad de simulación a 3x");
            result = simulacionTiempoRealService.ajustarVelocidad(3);
            logger.info("Resultado de ajuste de velocidad: {}", result);
            
            // Esperar algunas actualizaciones más
            logger.info("Esperando 5 segundos más con velocidad aumentada...");
            Thread.sleep(5000);
            
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
        }
    }
}
