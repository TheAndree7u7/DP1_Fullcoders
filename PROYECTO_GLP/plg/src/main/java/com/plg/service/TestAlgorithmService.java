package com.plg.service;

import com.plg.dto.AlgoritmoGeneticoResultadoDTO;
import com.plg.dto.AgrupamientoAPResultadoDTO;
import com.plg.entity.*;
import com.plg.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for testing all algorithm flows with real data
 * Only runs when "test-algorithm" profile is active
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
        logger.info("STARTING ALGORITHM TESTING SEQUENCE");
        logger.info("======================================================");
        
        // Check if we have required data
        verifyDataLoaded();
        
        // Test AP Clustering
        testAffinityPropagation();
        
        // Test Genetic Algorithm
        testGeneticAlgorithm();
        
        // Test Real-time simulation
        testRealTimeSimulation();
        
        logger.info("======================================================");
        logger.info("ALGORITHM TESTING SEQUENCE COMPLETED");
        logger.info("======================================================");
    }
    
    private void verifyDataLoaded() {
        logger.info("Verifying data loaded for algorithm testing");
        
        // Check pedidos
        long pedidosCount = pedidoRepository.count();
        logger.info("Found {} orders in database", pedidosCount);
        if (pedidosCount == 0) {
            logger.error("No orders found! Tests will fail. Make sure DataLoader is running correctly.");
        }
        
        // Log some example orders for verification
        List<Pedido> pedidos = pedidoRepository.findAll();
        if (!pedidos.isEmpty()) {
            logger.info("Sample orders:");
            pedidos.stream().limit(3).forEach(p -> 
                logger.info(" - Pedido {}: Cliente {}, Posición ({},{}), Volumen {} m3", 
                    p.getCodigo(), p.getCliente().getId(), p.getPosX(), p.getPosY(), p.getVolumenGLPAsignado())
            );
        }
        
        // Check trucks
        long camionesCount = camionRepository.count();
        logger.info("Found {} trucks in database", camionesCount);
        if (camionesCount == 0) {
            logger.error("No trucks found! Tests will fail. Make sure DataLoader is running correctly.");
        }
        
        // Check warehouses
        long almacenesCount = almacenRepository.count();
        logger.info("Found {} warehouses in database", almacenesCount);
        if (almacenesCount == 0) {
            logger.error("No warehouses found! Tests will fail. Make sure DataLoader is running correctly.");
        } else {
            Almacen almacenCentral = almacenRepository.findByEsCentralAndActivoTrue(true);
            if (almacenCentral != null) {
                logger.info("Central warehouse found at position ({},{})", 
                    almacenCentral.getPosX(), almacenCentral.getPosY());
            } else {
                logger.error("No central warehouse found! Tests will fail.");
            }
        }
    }
    
    private void testAffinityPropagation() {
        logger.info("===== TESTING AFFINITY PROPAGATION =====");
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("alpha", 0.8);
            params.put("beta", 0.2);
            params.put("damping", 0.9);
            params.put("maxIter", 100);
            
            logger.info("Calling AP service with parameters: {}", params);
            
            var result = agrupamientoAPService.generarGrupos(params);
            
            logger.info("AP result: clusters={}, totalPedidos={}", 
                result.getClusters().size(), result.getTotalPedidos());
            
            // Log detailed cluster information
            for (int i = 0; i < result.getClusters().size(); i++) {
                var cluster = result.getClusters().get(i);
                logger.info("Cluster {}: centroId={}, centroX={}, centroY={}, puntos={}", 
                    i+1, cluster.getIdGrupo(), cluster.getCentroX(), cluster.getCentroY(), 
                    cluster.getPuntos().size());
                
                // Log some sample points in this cluster
                if (!cluster.getPuntos().isEmpty()) {
                    var punto = cluster.getPuntos().get(0);
                    logger.info("  - Sample point: id={}, posición=({},{}), distanciaCentro={}", 
                        punto.getId(), punto.getX(), punto.getY(), punto.getDistanciaCentro());
                }
            }
            
            logger.info("Affinity Propagation computation time: {} ms", result.getTiempoComputo());
            logger.info("Affinity Propagation test completed successfully");
            
        } catch (Exception e) {
            logger.error("Error testing Affinity Propagation", e);
        }
    }
    
    @Transactional
    private void testGeneticAlgorithm() {
        logger.info("===== TESTING GENETIC ALGORITHM =====");
        try {
            // Ensure we have pending orders
            List<Pedido> pendientes = pedidoRepository.findByEstado(EstadoPedido.PENDIENTE_PLANIFICACION);
            logger.info("Found {} pending orders for route generation", pendientes.size());
            
            if (pendientes.isEmpty()) {
                // If no pending orders, update some orders to make them pending
                logger.info("No pending orders found. Setting some orders as PENDIENTE_PLANIFICACION");
                List<Pedido> orders = pedidoRepository.findAll();
                int count = 0;
                for (Pedido p : orders) {
                    if (count < 10) {  // Update up to 10 orders
                        p.setEstado(EstadoPedido.PENDIENTE_PLANIFICACION);
                        pedidoRepository.save(p);
                        count++;
                    } else {
                        break;
                    }
                }
                logger.info("Updated {} orders to PENDIENTE_PLANIFICACION", count);
            }
            
            // Set up parameters for genetic algorithm
            Map<String, Object> params = new HashMap<>();
            params.put("numeroRutas", 3);  // Try with 3 routes
            params.put("algoritmo", "genetico");
            params.put("poblacion", 50);
            params.put("maxIter", 100);
            
            logger.info("Calling GA service with parameters: {}", params);
            
            AlgoritmoGeneticoResultadoDTO result = algoritmoGeneticoService.generarRutas(params);
            
            logger.info("GA result: totalPedidos={}, pedidosAsignados={}, rutasGeneradas={}", 
                result.getTotalPedidos(), result.getPedidosAsignados(), result.getRutasGeneradas());
            
            // Log detailed route information
            for (int i = 0; i < result.getRutas().size(); i++) {
                var ruta = result.getRutas().get(i);
                logger.info("Route {}: idRuta={}, camion={}, distancia={}, numeroPedidos={}", 
                    i+1, ruta.getIdRuta(), ruta.getCamionCodigo(), 
                    ruta.getDistanciaTotal(), ruta.getNumeroPedidos());
                
                // Log individual orders in this route
                logger.info("  Orders in route {}:", i+1);
                for (int j = 0; j < ruta.getPedidos().size(); j++) {
                    var pedido = ruta.getPedidos().get(j);
                    logger.info("  - Order {}: id={}, codigo={}, pos=({},{}), volumen={}", 
                        j+1, pedido.getId(), pedido.getCodigo(), 
                        pedido.getPosX(), pedido.getPosY(), pedido.getVolumenGLPAsignado());
                }
                
                // Log route points
                logger.info("  Exact route path points: {}", ruta.getPuntos().size());
                logger.info("  First 3 and last 3 points:");
                // Log first 3 points
                for (int j = 0; j < Math.min(3, ruta.getPuntos().size()); j++) {
                    var punto = ruta.getPuntos().get(j);
                    logger.info("   - Point {}: ({},{}) type={}", 
                        j, punto.getPosX(), punto.getPosY(), punto.getTipo());
                }
                // Log last 3 points if more than 6 points
                if (ruta.getPuntos().size() > 6) {
                    for (int j = ruta.getPuntos().size() - 3; j < ruta.getPuntos().size(); j++) {
                        var punto = ruta.getPuntos().get(j);
                        logger.info("   - Point {}: ({},{}) type={}", 
                            j, punto.getPosX(), punto.getPosY(), punto.getTipo());
                    }
                }
            }
            
            // Check that routes were saved in database
            List<Ruta> savedRoutes = rutaRepository.findAll();
            logger.info("Total routes in database after GA: {}", savedRoutes.size());
            
            // Check that trucks have been assigned
            List<Camion> enRuta = camionRepository.findByEstado(EstadoCamion.EN_RUTA);
            logger.info("Trucks assigned to routes: {}", enRuta.size());
            
            logger.info("Genetic Algorithm test completed successfully");
            
        } catch (Exception e) {
            logger.error("Error testing Genetic Algorithm", e);
        }
    }
    
    private void testRealTimeSimulation() {
        logger.info("===== TESTING REAL TIME SIMULATION =====");
        try {
            // Check if we have routes to simulate
            List<Ruta> routes = rutaRepository.findAll();
            logger.info("Found {} routes for simulation", routes.size());
            
            if (routes.isEmpty()) {
                logger.error("No routes found for simulation. Please run the genetic algorithm first.");
                return;
            }
            
            // Find trucks in route
            List<Camion> enRuta = camionRepository.findByEstado(EstadoCamion.EN_RUTA);
            logger.info("Trucks in route status: {}", enRuta.size());
            
            if (enRuta.isEmpty()) {
                logger.warn("No trucks in route status. Setting some trucks to EN_RUTA");
                List<Camion> trucks = camionRepository.findAll();
                for (int i = 0; i < Math.min(3, trucks.size()); i++) {
                    trucks.get(i).setEstado(EstadoCamion.EN_RUTA);
                    camionRepository.save(trucks.get(i));
                }
                logger.info("Set {} trucks to EN_RUTA status", Math.min(3, trucks.size()));
            }
            
            // Start real-time simulation
            logger.info("Starting real-time simulation");
            Map<String, Object> result = simulacionTiempoRealService.iniciarSimulacion();
            logger.info("Simulation start result: {}", result);
            
            // Wait for a few updates
            logger.info("Waiting for 5 seconds to observe simulation updates...");
            Thread.sleep(5000);
            
            // Adjust speed
            logger.info("Adjusting simulation speed to 3x");
            result = simulacionTiempoRealService.ajustarVelocidad(3);
            logger.info("Speed adjustment result: {}", result);
            
            // Wait for a few more updates
            logger.info("Waiting for 5 more seconds with increased speed...");
            Thread.sleep(5000);
            
            // Stop simulation
            logger.info("Stopping simulation");
            result = simulacionTiempoRealService.detenerSimulacion();
            logger.info("Simulation stop result: {}", result);
            
            logger.info("Real-time simulation test completed successfully");
            
        } catch (Exception e) {
            logger.error("Error testing real-time simulation", e);
        }
    }
}
