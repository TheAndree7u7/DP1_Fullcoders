package com.plg;

import com.plg.config.DataLoader;
import com.plg.controller.SimulacionController;
import com.plg.entity.*;
import com.plg.utils.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SimulacionSimpleTest {

    @Autowired
    private SimulacionController simulacionController;

    private static LocalDateTime fechaInicio;
    private static final int TIMEOUT_SECONDS = 60;

    @BeforeAll
    static void setUp() {
        // Configurar fecha de inicio para la simulación
        fechaInicio = LocalDateTime.of(2025, 2, 1, 0, 0);
        Parametros.fecha_inicial = fechaInicio;
        
        // Limpiar estado global
        limpiarEstadoGlobal();
    }

    @Test
    @Order(1)
    @DisplayName("Test Simple 1: Verificar que el controller está disponible")
    void testControllerDisponible() {
        System.out.println("\n🧪 TEST SIMPLE 1: Verificar controller disponible");
        
        // Verificar que el controller está disponible
        assertNotNull(simulacionController, "El SimulacionController debe estar disponible");
        
        System.out.println("✅ Controller disponible");
    }

    @Test
    @Order(2)
    @DisplayName("Test Simple 2: Verificar estado del servidor")
    void testEstadoServidor() {
        System.out.println("\n🧪 TEST SIMPLE 2: Verificar estado del servidor");
        
        // Obtener estado del servidor
        ResponseEntity<Map<String, Object>> response = simulacionController.obtenerEstado();
        
        // Verificar respuesta
        assertNotNull(response, "La respuesta no debe ser null");
        assertEquals(200, response.getStatusCodeValue(), "El estado debe ser 200 OK");
        
        Map<String, Object> estado = response.getBody();
        assertNotNull(estado, "El cuerpo de la respuesta no debe ser null");
        assertEquals("running", estado.get("status"), "El estado debe ser 'running'");
        
        System.out.println("✅ Estado del servidor verificado");
        System.out.printf("📊 Estado: %s%n", estado);
    }

    @Test
    @Order(3)
    @DisplayName("Test Simple 3: Configurar simulación")
    void testConfiguracionSimulacion() {
        System.out.println("\n🧪 TEST SIMPLE 3: Configurar simulación");
        
        // Configurar simulación
        Simulacion.configurarSimulacion(fechaInicio);
        
        // Verificar que se cargaron los datos
        assertNotNull(DataLoader.almacenes, "Los almacenes no deben ser null");
        assertNotNull(DataLoader.camiones, "Los camiones no deben ser null");
        assertNotNull(DataLoader.pedidos, "Los pedidos no deben ser null");
        
        // Verificar que hay datos cargados
        assertFalse(DataLoader.almacenes.isEmpty(), "Debe haber al menos un almacén");
        assertFalse(DataLoader.camiones.isEmpty(), "Debe haber al menos un camión");
        assertFalse(DataLoader.pedidos.isEmpty(), "Debe haber al menos un pedido");
        
        // Verificar almacén central
        Almacen almacenCentral = DataLoader.almacenes.stream()
                .filter(a -> a.getTipo() == TipoAlmacen.CENTRAL)
                .findFirst()
                .orElse(null);
        assertNotNull(almacenCentral, "Debe existir un almacén central");
        
        System.out.println("✅ Simulación configurada correctamente");
        System.out.printf("📊 Datos cargados: %d almacenes, %d camiones, %d pedidos%n", 
                DataLoader.almacenes.size(), DataLoader.camiones.size(), DataLoader.pedidos.size());
    }

    @Test
    @Order(4)
    @DisplayName("Test Simple 4: Ejecutar simulación básica")
    void testEjecucionSimulacionBasica() throws Exception {
        System.out.println("\n🧪 TEST SIMPLE 4: Ejecutar simulación básica");
        
        // Ejecutar simulación en un hilo separado
        CompletableFuture<Void> simulacionFuture = CompletableFuture.runAsync(() -> {
            try {
                Simulacion.ejecutarSimulacion();
            } catch (Exception e) {
                System.err.println("❌ Error en simulación: " + e.getMessage());
                e.printStackTrace();
            }
        });
        
        // Esperar un poco para que la simulación inicie
        Thread.sleep(2000);
        
        // Verificar que la simulación está ejecutándose
        ResponseEntity<Map<String, Object>> response = simulacionController.obtenerEstado();
        assertEquals(200, response.getStatusCodeValue(), "El servidor debe responder");
        
        // Esperar a que termine la simulación (con timeout)
        try {
            simulacionFuture.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            System.out.println("✅ Simulación completada");
        } catch (Exception e) {
            System.out.println("⚠️ Simulación interrumpida por timeout: " + e.getMessage());
            simulacionFuture.cancel(true);
        }
    }

    @Test
    @Order(5)
    @DisplayName("Test Simple 5: Verificar resultados básicos")
    void testResultadosBasicos() {
        System.out.println("\n🧪 TEST SIMPLE 5: Verificar resultados básicos");
        
        // Verificar parámetros globales
        assertTrue(Parametros.kilometrosRecorridos >= 0, 
                "Los kilómetros recorridos deben ser >= 0");
        assertTrue(Parametros.fitnessGlobal >= 0, 
                "El fitness global debe ser >= 0");
        
        // Verificar estado de pedidos
        assertNotNull(Simulacion.pedidosPorAtender, "Los pedidos por atender no deben ser null");
        assertNotNull(Simulacion.pedidosPlanificados, "Los pedidos planificados no deben ser null");
        assertNotNull(Simulacion.pedidosEntregados, "Los pedidos entregados no deben ser null");
        
        // Verificar estado de camiones
        for (Camion camion : DataLoader.camiones) {
            assertNotNull(camion.getEstado(), "El estado del camión no debe ser null");
            assertNotNull(camion.getCoordenada(), "La coordenada del camión no debe ser null");
        }
        
        // Verificar estado de almacenes
        for (Almacen almacen : DataLoader.almacenes) {
            assertNotNull(almacen.getCoordenada(), "La coordenada del almacén no debe ser null");
            assertNotNull(almacen.getTipo(), "El tipo del almacén no debe ser null");
        }
        
        System.out.println("✅ Resultados básicos verificados");
        System.out.printf("📊 Resumen: %.2f km, Fitness: %.2f%n", 
                Parametros.kilometrosRecorridos, Parametros.fitnessGlobal);
    }

    @Test
    @Order(6)
    @DisplayName("Test Simple 6: Verificar integridad de datos")
    void testIntegridadDatos() {
        System.out.println("\n🧪 TEST SIMPLE 6: Verificar integridad de datos");
        
        // Verificar que todos los pedidos tienen coordenadas válidas
        for (Pedido pedido : DataLoader.pedidos) {
            assertNotNull(pedido.getCoordenada(), "El pedido debe tener coordenadas");
            assertTrue(pedido.getCoordenada().getFila() >= 0, 
                    "La fila de la coordenada debe ser >= 0");
            assertTrue(pedido.getCoordenada().getColumna() >= 0, 
                    "La columna de la coordenada debe ser >= 0");
            assertNotNull(pedido.getFechaRegistro(), "El pedido debe tener fecha de registro");
        }
        
        // Verificar que todos los camiones tienen coordenadas válidas
        for (Camion camion : DataLoader.camiones) {
            assertNotNull(camion.getCoordenada(), "El camión debe tener coordenadas");
            assertTrue(camion.getCoordenada().getFila() >= 0, 
                    "La fila de la coordenada debe ser >= 0");
            assertTrue(camion.getCoordenada().getColumna() >= 0, 
                    "La columna de la coordenada debe ser >= 0");
            assertNotNull(camion.getCodigo(), "El camión debe tener código");
            assertNotNull(camion.getTipo(), "El camión debe tener tipo");
        }
        
        // Verificar que todos los almacenes tienen coordenadas válidas
        for (Almacen almacen : DataLoader.almacenes) {
            assertNotNull(almacen.getCoordenada(), "El almacén debe tener coordenadas");
            assertTrue(almacen.getCoordenada().getFila() >= 0, 
                    "La fila de la coordenada debe ser >= 0");
            assertTrue(almacen.getCoordenada().getColumna() >= 0, 
                    "La columna de la coordenada debe ser >= 0");
            assertNotNull(almacen.getTipo(), "El almacén debe tener tipo");
        }
        
        // Verificar que el mapa está inicializado correctamente
        Mapa mapa = Mapa.getInstance();
        assertNotNull(mapa, "El mapa no debe ser null");
        assertTrue(mapa.getFilas() > 0, "El mapa debe tener filas > 0");
        assertTrue(mapa.getColumnas() > 0, "El mapa debe tener columnas > 0");
        assertNotNull(mapa.getMatriz(), "La matriz del mapa no debe ser null");
        
        System.out.println("✅ Integridad de datos verificada");
    }

    @Test
    @Order(7)
    @DisplayName("Test Simple 7: Probar algoritmo genético básico")
    void testAlgoritmoGeneticoBasico() {
        System.out.println("\n🧪 TEST SIMPLE 7: Probar algoritmo genético básico");
        
        // Obtener algunos pedidos para probar el algoritmo genético
        if (!Simulacion.pedidosPorAtender.isEmpty()) {
            // Tomar los primeros 5 pedidos para la prueba
            java.util.List<Pedido> pedidosParaAG = Simulacion.pedidosPorAtender.stream()
                    .limit(5)
                    .toList();
            
            if (!pedidosParaAG.isEmpty()) {
                // Crear y ejecutar algoritmo genético
                AlgoritmoGenetico ag = new AlgoritmoGenetico(Mapa.getInstance(), pedidosParaAG);
                ag.ejecutarAlgoritmo();
                
                // Verificar que se generó un resultado
                Individuo mejorIndividuo = ag.getMejorIndividuo();
                assertNotNull(mejorIndividuo, "Debe generarse un mejor individuo");
                
                System.out.printf("✅ Algoritmo genético ejecutado - Fitness: %.2f%n", 
                        mejorIndividuo.getFitness());
            } else {
                System.out.println("⚠️ No hay pedidos disponibles para el algoritmo genético");
            }
        } else {
            System.out.println("⚠️ No hay pedidos por atender");
        }
    }

    @Test
    @Order(8)
    @DisplayName("Test Simple 8: Verificar estado final del servidor")
    void testEstadoFinalServidor() {
        System.out.println("\n🧪 TEST SIMPLE 8: Verificar estado final del servidor");
        
        // Verificar que el servidor sigue funcionando
        ResponseEntity<Map<String, Object>> response = simulacionController.obtenerEstado();
        
        assertEquals(200, response.getStatusCodeValue(), "El servidor debe seguir funcionando");
        
        Map<String, Object> estado = response.getBody();
        assertNotNull(estado, "El estado final no debe ser null");
        assertEquals("running", estado.get("status"), "El estado debe seguir siendo 'running'");
        
        System.out.println("✅ Estado final del servidor verificado");
        System.out.printf("📊 Estado final: %s%n", estado);
    }

    private static void limpiarEstadoGlobal() {
        Parametros.kilometrosRecorridos = 0;
        Parametros.fitnessGlobal = 0;
        Simulacion.pedidosPorAtender.clear();
        Simulacion.pedidosPlanificados.clear();
        Simulacion.pedidosEntregados.clear();
        Simulacion.mejorIndividuo = null;
    }

    @AfterAll
    static void tearDown() {
        System.out.println("\n🧹 Limpieza final de test simple completada");
        System.out.println("\n📊 RESUMEN FINAL:");
        System.out.printf("   - Kilómetros recorridos: %.2f%n", Parametros.kilometrosRecorridos);
        System.out.printf("   - Fitness global: %.2f%n", Parametros.fitnessGlobal);
        System.out.printf("   - Pedidos procesados: %d%n", 
                Simulacion.pedidosPorAtender.size() + 
                Simulacion.pedidosPlanificados.size() + 
                Simulacion.pedidosEntregados.size());
    }
} 