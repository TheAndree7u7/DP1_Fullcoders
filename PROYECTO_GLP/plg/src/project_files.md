# Proyecto - Contenido de Archivos

## Estructura

```
Listado de rutas de carpetas
El número de serie del volumen es 000000C8 BCAB:BE00
E:\PROYECTOS\DP1\DP1_2025\DP1_FULLCODERS\PROYECTO_GLP\PLG\SRC
|   project_files.md
|   
+---main
|   +---java
|   |   \---com
|   |       \---plg
|   |           |   PlgApplication.java
|   |           |   
|   |           +---algorithm
|   |           |   +---clustering
|   |           |   +---dynamic
|   |           |   \---ga
|   |           +---config
|   |           |       DataLoader.java
|   |           |       DataLoaderConfig.java
|   |           |       JacksonConfig.java
|   |           |       MapaConfig.java
|   |           |       WebConfig.java
|   |           |       WebSocketConfig.java
|   |           |       
|   |           +---controller
|   |           |       AlgoritmosController.java
|   |           |       AlmacenCombustibleController.java
|   |           |       AlmacenController.java
|   |           |       BloqueoController.java
|   |           |       CamionController.java
|   |           |       MantenimientoController.java
|   |           |       MapaReticularController.java
|   |           |       PedidoController.java
|   |           |       RutaController.java
|   |           |       SimulacionController.java
|   |           |       VisualizadorController.java
|   |           |       
|   |           +---dto
|   |           |       AgrupamientoAPResultadoDTO.java
|   |           |       AlgoritmoGeneticoResultadoDTO.java
|   |           |       ClienteDTO.java
|   |           |       GrupoDTO.java
|   |           |       PedidoDTO.java
|   |           |       PuntoRutaDTO.java
|   |           |       RutaDTO.java
|   |           |       
|   |           +---entity
|   |           |       Almacen.java
|   |           |       AsignacionCamion.java
|   |           |       Averia.java
|   |           |       Bloqueo.java
|   |           |       Camion.java
|   |           |       Cliente.java
|   |           |       EntregaParcial.java
|   |           |       EstadoCamion.java
|   |           |       EstadoPedido.java
|   |           |       Mantenimiento.java
|   |           |       NodoRuta.java
|   |           |       Pedido.java
|   |           |       project_files.md
|   |           |       Ruta.java
|   |           |       
|   |           +---repository
|   |           |       AlmacenRepository.java
|   |           |       AveriaRepository.java
|   |           |       BloqueoRepository.java
|   |           |       CamionRepository.java
|   |           |       ClienteRepository.java
|   |           |       EntregaParcialRepository.java
|   |           |       MantenimientoRepository.java
|   |           |       PedidoRepository.java
|   |           |       RutaRepository.java
|   |           |       
|   |           +---service
|   |           |       AgrupamientoAPService.java
|   |           |       AlgoritmoGeneticoService.java
|   |           |       AlmacenCombustibleService.java
|   |           |       BloqueoService.java
|   |           |       CamionService.java
|   |           |       ConversionArchivoService.java
|   |           |       MantenimientoService.java
|   |           |       MapaReticularService.java
|   |           |       PedidoService.java
|   |           |       RutaService.java
|   |           |       SimulacionService.java
|   |           |       SimulacionTiempoRealService.java
|   |           |       VisualizadorService.java
|   |           |       
|   |           \---util
|   |                   DtoConverter.java
|   |                   
|   \---resources
|       |   application.properties
|       |   logback-spring.xml
|       |   logback.xml.bak
|       |   
|       +---data
|       |   +---almacenes
|       |   |       almacenes.txt
|       |   |       
|       |   +---averias
|       |   |       averias.v1.txt
|       |   |       
|       |   +---bloqueos
|       |   |       202504.bloqueadas
|       |   |       
|       |   +---camiones
|       |   |       camiones.txt
|       |   |       
|       |   +---mantenimientos
|       |   |       mantpreventivo.txt
|       |   |       
|       |   \---pedidos
|       |           ventas202504.txt
|       |           
|       \---static
|           |   index.html
|           |   
|           +---assets
|           +---css
|           |       style.css
|           |       
|           \---js
|                   script.js
|                   
\---test
    \---java
        \---com
            \---plg
                    PlgApplicationTests.java
                    

```

## main\java\com\plg\PlgApplication.java

```java
package com.plg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableScheduling  // Habilitar tareas programadas
public class PlgApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(PlgApplication.class, args);
        
        // Registrar un hook para el cierre de la aplicación
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                // Esperar a que todas las tareas asíncronas terminen (máximo 30 segundos)
                Thread.sleep(TimeUnit.SECONDS.toMillis(30));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));
    }
    
    @Bean
    public ConfigurableServletWebServerFactory webServerFactory() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        // En versiones anteriores de Spring Boot, no está disponible setGracefulShutdownTimeout
        // Configuramos otras propiedades útiles en su lugar
        factory.setContextPath("");
        factory.setPort(8085);
        return factory;
    }
}

```

## main\java\com\plg\config\DataLoader.java

```java
package com.plg.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.plg.entity.Almacen;
import com.plg.entity.Camion;
import com.plg.entity.Cliente;
import com.plg.entity.EstadoCamion;
import com.plg.entity.EstadoPedido;
import com.plg.entity.Mantenimiento;
import com.plg.entity.Pedido;
import com.plg.repository.AlmacenRepository;
import com.plg.repository.CamionRepository;
import com.plg.repository.ClienteRepository;
import com.plg.repository.MantenimientoRepository;
import com.plg.repository.PedidoRepository;

@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    @Autowired
    private PedidoRepository pedidoRepository;
    
    @Autowired
    private CamionRepository camionRepository;
    
    @Autowired
    private MantenimientoRepository mantenimientoRepository;

    @Autowired
    private AlmacenRepository almacenRepository;
    
    @Autowired
    private ClienteRepository clienteRepository;

    // @Value("${app.data.pedidos-path}")
    // private String pedidosPath;
    
    // @Value("${app.data.mantenimientos-path}")
    // private String mantenimientosPath;
    
    // @Value("${app.data.bloqueos-path}")
    // private String bloqueosPath;
    
    // @Value("${app.data.averias-path}")
    // private String averiasPath;
    
    // @Value("${app.data.almacenes-path}")
    // private String almacenesPath;
    
    // @Value("${app.data.camiones-path}")
    // private String camionesPath;

    @Override
    public void run(String... args) throws Exception {
        logger.info("Initializing test data...");

        try {
            // Initialize warehouses (almacenes)
            loadAlmacenesFromFile("almacenes.txt");

            // Initialize trucks (camiones)
            loadCamionesFromFile("camiones.txt");

            // Initialize demo data from files
            loadAllPedidos();
            loadMantenimientosFromFile("mantpreventivo.txt");

            logger.info("Data initialization completed!");

        } catch (Exception e) {
            logger.error("Error initializing data: " + e.getMessage(), e);
        }
    }
    
    private void loadCamionesFromFile(String fileName) {
        logger.info("Loading camiones from file: {}", fileName);

        try {
            // Use ClassPathResource to get the file from resources
            ClassPathResource resource = new ClassPathResource("data/camiones/" + fileName);
            logger.info("File located at: {}", resource.getURI());

            try ( // Read the file from the classpath
                BufferedReader reader = new BufferedReader(new FileReader(resource.getFile()))) {
                String line;
                reader.readLine(); // Skip header line
                
                while ((line = reader.readLine()) != null) {
                    String[] datos = line.split(";");
                    if (datos.length >= 9) {
                        String codigo = datos[0].trim();
                        String tipo = datos[1].trim();
                        double capacidad = Double.parseDouble(datos[2].trim());
                        double cargaActual = Double.parseDouble(datos[3].trim());
                        double capacidadDisponible = capacidad - cargaActual;
                        double tara = Double.parseDouble(datos[4].trim());
                        double pesoCarga = cargaActual * 0.5; // Approximate weight from volume
                        
                        double combustibleActual = Double.parseDouble(datos[6].trim());
                        int posX = Integer.parseInt(datos[7].trim());
                        int posY = Integer.parseInt(datos[8].trim());
                        
                        double velocidadPromedio = (datos.length >= 10) ? Double.parseDouble(datos[9].trim()) : 50.0;
                        double capacidadTanque = (datos.length >= 11) ? Double.parseDouble(datos[10].trim()) : 25.0;
                        
                        Camion camion = new Camion(codigo, tipo, capacidad, tara);
                        camion.setCapacidadDisponible(capacidadDisponible);
                        camion.setPesoCarga(pesoCarga);
                        camion.setPesoCombinado(tara + pesoCarga);
                        camion.setEstado(EstadoCamion.DISPONIBLE);
                        camion.setCombustibleActual(combustibleActual);
                        camion.setPosX(posX);
                        camion.setPosY(posY);
                        camion.setVelocidadPromedio(velocidadPromedio);
                        camion.setCapacidadTanque(capacidadTanque);
                        
                        // Assign default central warehouse
                        Optional<Almacen> almacenCentral = almacenRepository.findById(1L);
                        if (almacenCentral.isPresent()) {
                            camion.setUltimoAlmacen(almacenCentral.get());
                        }
                        
                        camionRepository.save(camion);
                        logger.info("Camion saved: {}", codigo);
                    }
                }
            }
            logger.info("Camiones loaded successfully");

        } catch (IOException | NumberFormatException e) {
            logger.error("Error loading camiones: " + e.getMessage(), e);
        }
    }
    
    private void loadAlmacenesFromFile(String fileName) {
        logger.info("Loading almacenes from file: {}", fileName);
    
        try (BufferedReader reader = new BufferedReader(
                new FileReader(new ClassPathResource("data/almacenes/" + fileName).getFile()))) {
    
            String line;
            while ((line = reader.readLine()) != null) {
                String[] d = line.trim().split(";");
                if (d.length < 13) {                       // ⬅️  AHORA 13
                    logger.warn("Línea inválida ({} columnas): {}", d.length, line);
                    continue;
                }
    
                Almacen a = new Almacen();
                // ─── índices ajustados ───────────────────────────────────────────────
                a.setNombre(d[0]);
                a.setPosX(Integer.parseInt(d[1]));
                a.setPosY(Integer.parseInt(d[2]));
    
                a.setCapacidadGLP(Double.parseDouble(d[3]));
                a.setCapacidadActualGLP(Double.parseDouble(d[4]));
                a.setCapacidadMaximaGLP(Double.parseDouble(d[5]));
    
                a.setCapacidadCombustible(Double.parseDouble(d[6]));
                a.setCapacidadActualCombustible(Double.parseDouble(d[7]));
                a.setCapacidadMaximaCombustible(Double.parseDouble(d[8]));
    
                a.setEsCentral(Boolean.parseBoolean(d[9]));
                a.setPermiteCamionesEstacionados(Boolean.parseBoolean(d[10]));
                a.setHoraReabastecimiento(LocalTime.parse(d[11]));
                a.setActivo(Boolean.parseBoolean(d[12]));
                ///colocar tipó  almacen
                if(a.isEsCentral()){
                    a.setTipo("CENTRAL");
                }else{
                    a.setTipo("INTERMEDIO");
                }


                almacenRepository.save(a);                 // se hace INSERT con id autogenerado
                logger.debug("Almacén guardado: {}", a.getNombre());
            }
            logger.info("Almacenes cargados correctamente");
        } catch (Exception e) {
            logger.error("Error cargando almacenes", e);
        }
    }
    
    public void loadAllPedidos() {
        logger.info("📦 Cargando todos los pedidos desde carpeta: data/pedidos/");
    
        try {
            File carpeta = new ClassPathResource("data/pedidos/").getFile();
            File[] archivos = carpeta.listFiles((dir, name) -> name.matches("ventas\\d{6}\\.txt"));
    
            if (archivos == null || archivos.length == 0) {
                logger.warn("⚠️ No se encontraron archivos con patrón 'ventasaaaamm'");
                return;
            }
    
            for (File archivo : archivos) {
                String fileName = archivo.getName();
    
                // Extraer año y mes del nombre: ventas202504
                int anio = Integer.parseInt(fileName.substring(6, 10));
                int mes = Integer.parseInt(fileName.substring(10, 12));
    
                logger.info("📝 Procesando archivo: {} (año: {}, mes: {})", fileName, anio, mes);
    
                loadPedidosFromFile(archivo, anio, mes);
            }
    
        } catch (IOException | NumberFormatException e) {
            logger.error("❌ Error leyendo archivos de pedidos: {}", e.getMessage(), e);
        }
    }
    
    
    
    
    
    private void loadPedidosFromFile(File archivo, int anio, int mes) {
        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            int contador = 1;
            String line;
    
            while ((line = reader.readLine()) != null) {
                try {
                    logger.info("📄 Línea leída: {}", line);

                    String[] partes = line.split(":");
                    logger.info("🧩 Partes detectadas: {}", Arrays.toString(partes));
                    if (partes.length != 2) {
                        logger.warn("❌ Línea ignorada por mal formato: {}", line);
                        continue;
                    }
    
                    // Parsear fecha de pedido
                    String fechaRaw = partes[0].trim(); // Ej: 11d13h31m
                    int dia = Integer.parseInt(fechaRaw.substring(0, 2));
                    int hora = Integer.parseInt(fechaRaw.substring(3, 5));
                    int minuto = Integer.parseInt(fechaRaw.substring(6, 8));
    
                    LocalDateTime fechaPedido = LocalDateTime.of(anio, mes, dia, hora, minuto);
    
                    // Parsear detalles
                    String[] datos = partes[1].split(",");
                    logger.info("🛠 Detalles extraídos: {}", Arrays.toString(datos));
                    if (datos.length != 5) {
                        logger.warn("❌ Datos incompletos: {}", line);
                        continue;
                    }
                    
    
                    int posX = Integer.parseInt(datos[0].trim());
                    int posY = Integer.parseInt(datos[1].trim());
                    String clienteId = datos[2].trim().replace("c-", "");
                    int volumen = Integer.parseInt(datos[3].trim().replace("m3", ""));
                    int horasLimite = Integer.parseInt(datos[4].trim().replace("h", ""));
    
                    LocalDateTime fechaEntrega = fechaPedido.plusHours(horasLimite);
    
                    // Buscar o crear cliente
                    Cliente cliente = clienteRepository.findById(clienteId).orElseGet(() -> {
                        Cliente nuevoCliente = new Cliente();
                        nuevoCliente.setId(clienteId);
                        nuevoCliente.setPosX(posX);
                        nuevoCliente.setPosY(posY);
                        clienteRepository.save(nuevoCliente);
                        return nuevoCliente;
                    });
    
                    // Crear pedido
                    Pedido pedido = new Pedido();
                    pedido.setCodigo("P" + String.format("%04d", contador++));
                    pedido.setCliente(cliente);
                    pedido.setPosX(posX);
                    pedido.setPosY(posY);
                    pedido.setVolumenGLPAsignado(volumen); 
                    pedido.setHorasLimite(horasLimite);
                    pedido.setEstado(EstadoPedido.PENDIENTE_PLANIFICACION);
                    pedido.setFechaRegistro(fechaPedido);
                    pedido.setFechaEntregaRequerida(fechaEntrega);
                    pedido.setAsignaciones(new ArrayList<>());
    
                    pedidoRepository.save(pedido);
                    logger.info("✅ Pedido guardado: {} - Cliente: {}", pedido.getCodigo(), clienteId);
    
                } catch (NumberFormatException e) {
                    logger.warn("⚠️ Línea inválida o error: [{}] - {}", line, e.getMessage());
                }
            }
    
        } catch (Exception e) {
            logger.error("❌ Error procesando archivo '{}': {}", archivo.getName(), e.getMessage(), e);
        }
    }
    
    
    
    
    private void loadMantenimientosFromFile(String fileName) {
        logger.info("Loading mantenimientos from file: {}", fileName);

        try {
            ClassPathResource resource = new ClassPathResource("data/mantenimientos/" + fileName);
            try (BufferedReader reader = new BufferedReader(new FileReader(resource.getFile()))) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] datos = line.split(",");
                    if (datos.length >= 3) {
                        String codigoCamion = datos[0].trim();
                        LocalDate fechaInicio = LocalDate.parse(datos[1].trim(), formatter);
                        LocalDate fechaFin = LocalDate.parse(datos[2].trim(), formatter);
                        
                        Optional<Camion> camionOpt = camionRepository.findByCodigo(codigoCamion);
                        if (camionOpt.isPresent()) {
                            Camion camion = camionOpt.get();
                            Mantenimiento mantenimiento = Mantenimiento.builder()
                                    .camion(camion)
                                    .fechaInicio(fechaInicio)
                                    .fechaFin(fechaFin)
                                    .tipo("preventivo")
                                    .descripcion("Mantenimiento preventivo programado")
                                    .estado(0) // Programado
                                    .build();
                            
                            if (camion.getMantenimientos() == null) {
                                camion.setMantenimientos(new ArrayList<>());
                            }
                            camion.getMantenimientos().add(mantenimiento);
                            
                            mantenimientoRepository.save(mantenimiento);
                            logger.info("Mantenimiento saved for camion: {}", codigoCamion);
                        } else {
                            logger.warn("Camión not found: {}", codigoCamion);
                        }
                    }
                }
            }
            logger.info("Mantenimientos loaded successfully");

        } catch (IOException e) {
            logger.error("Error loading mantenimientos: " + e.getMessage(), e);
        }
    }
}

```

## main\java\com\plg\config\DataLoaderConfig.java

```java
package com.plg.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

@Configuration
public class DataLoaderConfig {

    // Ensure database initialization is complete before data loading begins
    @Bean
    @DependsOn("entityManagerFactory")
    public boolean dataLoadingOrderConfiguration(LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        // This bean doesn't do much except ensure the dependency order
        return true;
    }
}
```

## main\java\com\plg\config\JacksonConfig.java

```java
package com.plg.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;          // <-- import
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // 1) Java Time (fechas y horas)
        objectMapper.registerModule(new JavaTimeModule());

        // 2) JDK 8 (Optional, OptionalInt, etc.)
        objectMapper.registerModule(new Jdk8Module());        // <-- añade esto

        // Evitar fechas como timestamps
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // Ignorar propiedades desconocidas al deserializar
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // No incluir valores nulos en la salida JSON
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        return objectMapper;
    }
}

```

## main\java\com\plg\config\MapaConfig.java

```java
package com.plg.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Configuración del mapa para la aplicación
 * Contiene las dimensiones y características del mapa reticular
 */
@Getter
@Setter
@Component
@Configuration
@NoArgsConstructor
public class MapaConfig {
    
    // Dimensiones del mapa
    @Value("${mapa.largo:70}")
    private double largo; // Largo del mapa en km (eje X)
    
    @Value("${mapa.ancho:50}")
    private double ancho; // Ancho del mapa en km (eje Y)
    
    // Origen del mapa
    @Value("${mapa.origen.x:0}")
    private double origenX; // Coordenada X del origen
    
    @Value("${mapa.origen.y:0}")
    private double origenY; // Coordenada Y del origen
    
    // Distancia entre nodos
    @Value("${mapa.distancia.nodos:1}")
    private double distanciaNodos; // Distancia entre nodos en km
    
 
  
    
    /**
     * Constructor con parámetros
     */
    public MapaConfig(int largo, int ancho, int origenX, int origenY, double distanciaNodos) {
        this.largo = largo;
        this.ancho = ancho;
        this.origenX = origenX;
        this.origenY = origenY;
        this.distanciaNodos = distanciaNodos;
    }
    
    /**
     * Verifica si unas coordenadas están dentro de los límites del mapa
     */
    public boolean estaEnMapa(double x, double y) {
        return x >= origenX && x <= origenX + largo &&
               y >= origenY && y <= origenY + ancho;
    }
    
    /**
     * Calcula la distancia entre dos puntos en el mapa reticular (Manhattan)
     * En un mapa reticular, sólo se puede mover horizontal y verticalmente
     */
    public double calcularDistanciaReticular(double x1, double y1, double x2, double y2) {
        // Distancia Manhattan = |x1 - x2| + |y1 - y2|
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }
    
    /**
     * Calcula la distancia en km entre dos puntos en coordenadas
     */
    public double calcularDistanciaRealKm(double x1, double y1, double x2, double y2) {
        // La distancia real es la distancia Manhattan multiplicada por la distancia entre nodos
        return calcularDistanciaReticular(x1, y1, x2, y2) * distanciaNodos;
    }
    
    /**
     * Obtiene los nodos adyacentes a un nodo dado (hasta 4: arriba, abajo, izquierda, derecha)
     */
    public double[][] obtenerNodosAdyacentes(double x, double y) {
        // Posibles movimientos: arriba, derecha, abajo, izquierda
        double[][] movimientos = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
        
        // Inicializar lista para almacenar nodos válidos
        java.util.List<double[]> nodosAdyacentes = new java.util.ArrayList<>();
        
        // Verificar cada posible movimiento
        for (double[] mov : movimientos) {
            double newX = x + mov[0];
            double newY = y + mov[1];
            
            // Verificar si el nuevo nodo está dentro del mapa
            if (estaEnMapa(newX, newY)) {
                nodosAdyacentes.add(new double[]{newX, newY});
            }
        }
        
        // Convertir lista a array 2D
        return nodosAdyacentes.toArray(new double[0][]);
    } 
}
```

## main\java\com\plg\config\WebConfig.java

```java
package com.plg.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")  // Usar allowedOriginPatterns en lugar de allowedOrigins
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
    
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // No usar allowedOrigins("*") con allowCredentials(true)
        config.setAllowedOriginPatterns(java.util.Collections.singletonList("*"));
        config.setAllowedHeaders(java.util.Arrays.asList("Authorization", "Content-Type", "Accept"));
        config.setAllowedMethods(java.util.Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Configurar rutas de vistas
        registry.addViewController("/").setViewName("redirect:/simulacion-tiempo-real.html");
        registry.addViewController("/simulacion").setViewName("redirect:/simulacion-tiempo-real.html");
    }
}
```

## main\java\com\plg\config\WebSocketConfig.java

```java
package com.plg.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Bean
    public TaskScheduler webSocketHeartbeatTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("ws-heartbeat-scheduler-");
        scheduler.initialize();
        return scheduler;
    }
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Configurar el broker con un TaskScheduler para los heartbeats
        config.enableSimpleBroker("/topic")
            .setHeartbeatValue(new long[] {10000, 10000})
            .setTaskScheduler(webSocketHeartbeatTaskScheduler());
        config.setApplicationDestinationPrefixes("/app");
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")  // Usar setAllowedOriginPatterns en lugar de setAllowedOrigins
                .withSockJS()
                .setStreamBytesLimit(512 * 1024) // 512KB
                .setHttpMessageCacheSize(1000)
                .setDisconnectDelay(30 * 1000); // 30 segundos
    }
    
    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(1024 * 1024); // 1MB
        registration.setSendBufferSizeLimit(1024 * 1024); // 1MB
        registration.setSendTimeLimit(20 * 1000); // 20 segundos
    }
    
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.taskExecutor(createExecutor("clientInbound", 8));
    }
    
    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.taskExecutor(createExecutor("clientOutbound", 8));
    }
    
    private ThreadPoolTaskExecutor createExecutor(String threadNamePrefix, int corePoolSize) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(corePoolSize * 2);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix(threadNamePrefix + "-");
        executor.setAllowCoreThreadTimeOut(true);
        executor.initialize();
        return executor;
    }
}
```

## main\java\com\plg\controller\AlgoritmosController.java

```java
package com.plg.controller;

import com.plg.dto.AgrupamientoAPResultadoDTO;
import com.plg.dto.AlgoritmoGeneticoResultadoDTO;
import com.plg.service.AgrupamientoAPService;
import com.plg.service.AlgoritmoGeneticoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/algoritmos")
public class AlgoritmosController {

    @Autowired
    private AlgoritmoGeneticoService algoritmoGeneticoService;
    
    @Autowired
    private AgrupamientoAPService agrupamientoAPService;
    
    @PostMapping("/genetic")
    public ResponseEntity<AlgoritmoGeneticoResultadoDTO> generarRutasGenetico(@RequestBody Map<String, Object> params) {
        try {
            // Llamar al servicio y devolver el resultado como DTO
            AlgoritmoGeneticoResultadoDTO resultado = algoritmoGeneticoService.generarRutas(params);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            // En caso de error, devolver una respuesta de error
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/affinity-propagation")
    public ResponseEntity<AgrupamientoAPResultadoDTO> generarGruposAP(@RequestBody Map<String, Object> params) {
        try {
            // Llamar al servicio y devolver el resultado como DTO
            AgrupamientoAPResultadoDTO resultado = agrupamientoAPService.generarGrupos(params);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            // En caso de error, devolver una respuesta de error
            return ResponseEntity.badRequest().build();
        }
    }
}
```

## main\java\com\plg\controller\AlmacenCombustibleController.java

```java
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
```

## main\java\com\plg\controller\AlmacenController.java

```java
package com.plg.controller;

import com.plg.entity.Almacen;
import com.plg.repository.AlmacenRepository;
import com.plg.service.AlmacenCombustibleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/almacenes")
public class AlmacenController {

    @Autowired
    private AlmacenCombustibleService almacenService;
    
    @Autowired
    private AlmacenRepository almacenRepository;
    
    /**
     * Inicializar almacenes del sistema
     * Debe ir ANTES del método obtenerAlmacenPorId para evitar conflictos de URL
     */
    @PostMapping("/inicializar")
    public ResponseEntity<?> inicializarAlmacenes() {
        almacenService.inicializarAlmacenes();
        List<Almacen> almacenes = almacenRepository.findAll();
        
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Almacenes inicializados correctamente");
        response.put("total", almacenes.size());
        response.put("almacenes", almacenes);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Nuevo método para crear un almacén de prueba
     * Debe ir ANTES del método obtenerAlmacenPorId para evitar conflictos de URL
     */
    @PostMapping("/crear-prueba")
    public ResponseEntity<?> crearAlmacenPrueba() {
        Almacen almacen = new Almacen();
        almacen.setNombre("Almacén de Prueba");
        almacen.setPosX(10);
        almacen.setPosY(20);
        almacen.setCapacidadGLP(1000.0);
        almacen.setCapacidadActualGLP(800.0);
        almacen.setCapacidadMaximaGLP(1000.0);
        almacen.setCapacidadCombustible(2000.0);
        almacen.setCapacidadActualCombustible(1500.0);
        almacen.setCapacidadMaximaCombustible(2000.0);
        almacen.setEsCentral(false);
        almacen.setPermiteCamionesEstacionados(false);
        almacen.setHoraReabastecimiento(LocalTime.of(6, 0)); // 6:00 AM
        almacen.setActivo(true);
        
        almacenRepository.save(almacen);
        
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Almacén de prueba creado correctamente");
        response.put("almacen", almacen);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Obtiene todos los almacenes activos
     */
    @GetMapping
    public ResponseEntity<?> obtenerAlmacenesActivos() {
        List<Almacen> almacenes = almacenService.obtenerAlmacenesActivos();
        Map<String, Object> response = new HashMap<>();
        response.put("total", almacenes.size());
        response.put("almacenes", almacenes);
        response.put("mensaje", "Total de almacenes activos: " + almacenes.size());
        
        // Obtener todos los almacenes independientemente del estado activo
        List<Almacen> todosLosAlmacenes = almacenRepository.findAll();
        response.put("totalEnBD", todosLosAlmacenes.size());
        response.put("todosLosAlmacenes", todosLosAlmacenes);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Obtiene un almacén por su ID
     * Este método debe ir DESPUÉS de los endpoints específicos como /inicializar y /crear-prueba
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerAlmacenPorId(@PathVariable Long id) {
        Almacen almacen = almacenService.obtenerAlmacenPorId(id);
        if (almacen != null) {
            return ResponseEntity.ok(almacen);
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Almacén no encontrado con ID: " + id);
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Obtiene el almacén central
     */
    @GetMapping("/central")
    public ResponseEntity<?> obtenerAlmacenCentral() {
        Almacen almacen = almacenService.obtenerAlmacenCentral();
        if (almacen != null) {
            return ResponseEntity.ok(almacen);
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Almacén central no encontrado");
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Obtiene almacenes intermedios
     */
    @GetMapping("/intermedios")
    public ResponseEntity<List<Almacen>> obtenerAlmacenesIntermedios() {
        return ResponseEntity.ok(almacenService.obtenerAlmacenesIntermedios());
    }
    
    /**
     * Obtiene estadísticas de almacenes para mostrar en la simulación
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasAlmacenes() {
        return ResponseEntity.ok(almacenService.obtenerEstadisticasAlmacenes());
    }
}
```

## main\java\com\plg\controller\BloqueoController.java

```java
package com.plg.controller;

import com.plg.service.BloqueoService;
import com.plg.service.ConversionArchivoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/bloqueos")
public class BloqueoController {

    @Autowired
    private BloqueoService bloqueoService;
    
    @Autowired
    private ConversionArchivoService conversionArchivoService;
    
    /**
     * Endpoint para cargar bloqueos de un mes específico
     */
    @GetMapping("/cargar/{anio}/{mes}")
    public ResponseEntity<Map<String, Object>> cargarBloqueosDelMes(
            @PathVariable("anio") int anio,
            @PathVariable("mes") int mes) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            var bloqueos = bloqueoService.cargarBloqueosMensuales(anio, mes);
            response.put("success", true);
            response.put("bloqueosCount", bloqueos.size());
            response.put("message", "Se cargaron " + bloqueos.size() + " bloqueos para " + anio + "-" + mes);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Endpoint para convertir un archivo de bloqueos al nuevo formato
     */
    @GetMapping("/convertir/{anio}/{mes}")
    public ResponseEntity<Map<String, Object>> convertirArchivoBloqueos(
            @PathVariable("anio") int anio,
            @PathVariable("mes") int mes) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean exito = conversionArchivoService.convertirArchivoBloqueos(anio, mes);
            
            if (exito) {
                response.put("success", true);
                response.put("message", "Archivo convertido exitosamente para " + anio + "-" + mes);
            } else {
                response.put("success", false);
                response.put("message", "No se pudo convertir el archivo. Verifique que exista.");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Endpoint para verificar bloqueos entre dos puntos
     */
    @GetMapping("/verificar")
    public ResponseEntity<Map<String, Object>> verificarRutaBloqueada(
            @RequestParam("x1") int x1,
            @RequestParam("y1") int y1,
            @RequestParam("x2") int x2,
            @RequestParam("y2") int y2) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean estaBloqueada = bloqueoService.esRutaBloqueada(x1, y1, x2, y2);
            
            response.put("success", true);
            response.put("bloqueada", estaBloqueada);
            response.put("desde", x1 + "," + y1);
            response.put("hasta", x2 + "," + y2);
            
            if (estaBloqueada) {
                response.put("message", "La ruta entre (" + x1 + "," + y1 + ") y (" + x2 + "," + y2 + ") está bloqueada");
            } else {
                response.put("message", "La ruta entre (" + x1 + "," + y1 + ") y (" + x2 + "," + y2 + ") está libre");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Endpoint para actualizar estado de todos los bloqueos
     */
    @PostMapping("/actualizar-estado")
    public ResponseEntity<Map<String, Object>> actualizarEstadoBloqueos() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            bloqueoService.actualizarEstadoBloqueos();
            
            response.put("success", true);
            response.put("message", "Se ha actualizado el estado de todos los bloqueos");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
}
```

## main\java\com\plg\controller\CamionController.java

```java
package com.plg.controller;

import com.plg.entity.Camion;
import com.plg.entity.Pedido;
import com.plg.entity.EstadoCamion;
import com.plg.service.CamionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/camiones")
public class CamionController {

    @Autowired
    private CamionService camionService;

    /**
     * Lista todos los camiones disponibles
     */
    @GetMapping
    public ResponseEntity<List<Camion>> getAllCamiones() {
        return ResponseEntity.ok(camionService.findAll());
    }
    
    /**
     * Obtiene un camión específico por su código
     */
    @GetMapping("/{codigo}")
    public ResponseEntity<?> getCamionByCodigo(@PathVariable String codigo) {
        Optional<Camion> camion = camionService.findById(codigo);
        if (camion.isPresent()) {
            return ResponseEntity.ok(camion.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Lista los camiones filtrados por estado
     */
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<Camion>> getCamionesByEstado(@PathVariable EstadoCamion estado) {
        return ResponseEntity.ok(camionService.findByEstado(estado));
    }
    
    /**
     * Lista los camiones filtrados por tipo
     */
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<Camion>> getCamionesByTipo(@PathVariable String tipo) {
        return ResponseEntity.ok(camionService.findByTipo(tipo));
    }
    
    /**
     * Obtiene todos los pedidos asignados a un camión específico
     */
    @GetMapping("/{codigo}/pedidos")
    public ResponseEntity<?> getPedidosByCamion(@PathVariable String codigo) {
        // Verificar primero si el camión existe
        Optional<Camion> camion = camionService.findById(codigo);
        if (!camion.isPresent()) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Camión no encontrado con código: " + codigo);
            return ResponseEntity.badRequest().body(response);
        }
        
        // Obtener los pedidos asignados a este camión
        List<Pedido> pedidos = camionService.findPedidosByCamion(codigo);
        return ResponseEntity.ok(pedidos);
    }
    
    /**
     * Obtiene un resumen de la disponibilidad de camiones
     */
    @GetMapping("/resumen")
    public ResponseEntity<?> getResumenCamiones() {
        return ResponseEntity.ok(camionService.getEstadisticasCamiones());
    }
    
    /**
     * Obtiene información detallada de un camión específico
     */
    @GetMapping("/{codigo}/detalle")
    public ResponseEntity<?> getDetalleCamion(@PathVariable String codigo) {
        Map<String, Object> detalle = camionService.getDetalleCamion(codigo);
        if (detalle == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(detalle);
    }
}
```

## main\java\com\plg\controller\MantenimientoController.java

```java
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
```

## main\java\com\plg\controller\MapaReticularController.java

```java
package com.plg.controller;

import com.plg.config.MapaConfig;
import com.plg.service.MapaReticularService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mapa")
public class MapaReticularController {

    @Autowired
    private MapaReticularService mapaReticularService;
    
    @Autowired
    private MapaConfig mapaConfig;
    
    /**
     * Endpoint para obtener las propiedades del mapa
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> obtenerConfiguracionMapa() {
        Map<String, Object> config = new HashMap<>();
        
        config.put("largo", mapaConfig.getLargo());
        config.put("ancho", mapaConfig.getAncho());
        config.put("origenX", mapaConfig.getOrigenX());
        config.put("origenY", mapaConfig.getOrigenY());
        config.put("distanciaNodos", mapaConfig.getDistanciaNodos()); 
        
        return ResponseEntity.ok(config);
    }
    
    /**
     * Endpoint para obtener la configuración requerida por el frontend
     */
    @GetMapping("/configuracion")
    public ResponseEntity<Map<String, Object>> obtenerConfiguracion() {
        Map<String, Object> configuracion = new HashMap<>();
        
        configuracion.put("ancho", mapaConfig.getAncho());
        configuracion.put("largo", mapaConfig.getLargo());
        configuracion.put("origenX", mapaConfig.getOrigenX());
        configuracion.put("origenY", mapaConfig.getOrigenY());
        configuracion.put("distanciaNodos", mapaConfig.getDistanciaNodos());
        configuracion.put("tamano", 1); // Tamaño para visualización
        
        return ResponseEntity.ok(configuracion);
    }
    
    /**
     * Endpoint para calcular una ruta óptima entre dos puntos
     * considerando los bloqueos activos
     */
    @GetMapping("/ruta")
    public ResponseEntity<Map<String, Object>> calcularRutaOptima(
            @RequestParam("xInicio") double xInicio,
            @RequestParam("yInicio") double yInicio,
            @RequestParam("xFin") double xFin,
            @RequestParam("yFin") double yFin,
            @RequestParam(value = "velocidad", defaultValue = "50") double velocidad) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Verificar si las coordenadas están dentro del mapa
            if (!mapaConfig.estaEnMapa(xInicio, yInicio) || !mapaConfig.estaEnMapa(xFin, yFin)) {
                response.put("success", false);
                response.put("error", "Coordenadas fuera de los límites del mapa");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Calcular la ruta óptima
            List<double[]> ruta = mapaReticularService.calcularRutaOptimaConsiderandoBloqueos(
                xInicio, yInicio, xFin, yFin);
            
            if (ruta.isEmpty()) {
                response.put("success", false);
                response.put("error", "No se pudo encontrar una ruta entre los puntos especificados");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Convertir la ruta a formato más amigable para JSON
            List<Map<String, Double>> rutaFormateada = ruta.stream()
                .map(punto -> {
                    Map<String, Double> coordenada = new HashMap<>();
                    coordenada.put("x",  punto[0]);
                    coordenada.put("y",  punto[1]);
                    return coordenada;
                })
                .collect(java.util.stream.Collectors.toList());
            
            // Calcular la longitud de la ruta en km
            double longitudKm = mapaReticularService.calcularLongitudRuta(ruta);
            
            // Estimar el tiempo de viaje en minutos
            double tiempoMinutos = mapaReticularService.estimarTiempoViajeMinutos(ruta, velocidad);
            
            response.put("success", true);
            response.put("desde", new double[]{xInicio, yInicio});
            response.put("hasta", new double[]{xFin, yFin});
            response.put("ruta", rutaFormateada);
            response.put("nodos", ruta.size());
            response.put("longitudKm", Math.round(longitudKm * 100) / 100.0); // Redondear a 2 decimales
            response.put("tiempoEstimadoMinutos", Math.round(tiempoMinutos * 10) / 10.0); // Redondear a 1 decimal
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Endpoint para verificar si una posición está dentro del mapa
     */
    @GetMapping("/verificar-posicion")
    public ResponseEntity<Map<String, Object>> verificarPosicion(
            @RequestParam("x") int x,
            @RequestParam("y") int y) {
        
        Map<String, Object> response = new HashMap<>();
        boolean dentroMapa = mapaConfig.estaEnMapa(x, y);
        
        response.put("success", true);
        response.put("coordenada", new int[]{x, y});
        response.put("dentroMapa", dentroMapa);
        
        if (dentroMapa) {
            response.put("message", "La coordenada (" + x + "," + y + ") está dentro del mapa");
        } else {
            response.put("message", "La coordenada (" + x + "," + y + ") está fuera del mapa");
            
            // Sugerir la coordenada válida más cercana
            double xValido = Math.max(mapaConfig.getOrigenX(), 
                          Math.min(x, mapaConfig.getOrigenX() + mapaConfig.getLargo()));
            double yValido = Math.max(mapaConfig.getOrigenY(), 
                          Math.min(y, mapaConfig.getOrigenY() + mapaConfig.getAncho()));
            
            response.put("coordenadaValida", new double[]{xValido, yValido});
            response.put("sugerencia", "La coordenada válida más cercana es (" + xValido + "," + yValido + ")");
        }
        
        return ResponseEntity.ok(response);
    }
}
```

## main\java\com\plg\controller\PedidoController.java

```java
package com.plg.controller;

import com.plg.dto.PedidoDTO;
import com.plg.entity.Pedido;
import com.plg.entity.EstadoPedido;
import com.plg.service.PedidoService;
import com.plg.util.DtoConverter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @GetMapping
    public ResponseEntity<List<PedidoDTO>> getAllPedidos() {
        return ResponseEntity.ok(pedidoService.findAllDTO());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoDTO> getPedidoById(@PathVariable Long id) {
        PedidoDTO pedidoDTO = pedidoService.findByIdDTO(id);
        if (pedidoDTO != null) {
            return ResponseEntity.ok(pedidoDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Pedido> createPedido(@RequestBody PedidoDTO pedidoDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.save(pedidoDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Pedido> updatePedido(@PathVariable Long id, @RequestBody PedidoDTO pedidoDTO) {
        try {
            return ResponseEntity.ok(pedidoService.update(id, pedidoDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePedido(@PathVariable Long id) {
        Optional<Pedido> pedido = pedidoService.findById(id);
        if (pedido.isPresent()) {
            pedidoService.delete(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Endpoint que devuelve pedidos por estado usando el enum EstadoPedido
     */
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<PedidoDTO>> getPedidosByEstado(@PathVariable EstadoPedido estado) {
        return ResponseEntity.ok(pedidoService.findByEstadoEnumDTO(estado));
    }
    
 
    /**
     * Actualiza solo el estado de un pedido
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> updateEstadoPedido(@PathVariable Long id, @RequestBody Map<String, Object> estadoMap) {
        try {
            Pedido pedido = pedidoService.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
                
            // Si el cuerpo contiene 'estado' como enum (string)
            if (estadoMap.containsKey("estado")) {
                String estadoStr = estadoMap.get("estado").toString();
                try {
                    EstadoPedido nuevoEstado = EstadoPedido.valueOf(estadoStr);
                    pedido.setEstado(nuevoEstado);
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest()
                        .body("Estado inválido: " + estadoStr + ". Valores válidos: " + 
                              java.util.Arrays.toString(EstadoPedido.values()));
                }
            } 
  
            
            pedidoService.update(id, DtoConverter.toPedidoDTO(pedido));
            return ResponseEntity.ok(DtoConverter.toPedidoDTO(pedido));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest()
                .body("El valor de estadoInt debe ser un número entero");
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
```

## main\java\com\plg\controller\RutaController.java

```java
package com.plg.controller;

import com.plg.service.RutaService;
import com.plg.service.AlgoritmoGeneticoService;
import com.plg.service.AgrupamientoAPService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rutas")
public class RutaController {

    @Autowired
    private RutaService rutaService;
    
    @Autowired
    private AlgoritmoGeneticoService algoritmoGeneticoService;
    
    @Autowired
    private AgrupamientoAPService agrupamientoAPService;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> obtenerTodasLasRutas() {
        return ResponseEntity.ok(rutaService.obtenerResumeneRutas());
    }

    @PostMapping("/generar")
    public ResponseEntity<?> generarRutas(@RequestBody Map<String, Object> params) {
        String algoritmo = (String) params.getOrDefault("algoritmo", "genetico");
        
        if ("genetico".equals(algoritmo)) {
            return ResponseEntity.ok(algoritmoGeneticoService.generarRutas(params));
        } else if ("agrupamiento".equals(algoritmo)) {
            return ResponseEntity.ok(agrupamientoAPService.generarGrupos(params));
        } else {
            return ResponseEntity.badRequest().body("Algoritmo no soportado");
        }
    }
    
    @GetMapping("/optimizar/{idRuta}")
    public ResponseEntity<?> optimizarRuta(@PathVariable String idRuta, 
                                           @RequestParam(required = false) Boolean considerarBloqueos) {
        boolean usarBloqueos = considerarBloqueos != null ? considerarBloqueos : true;
        return ResponseEntity.ok(rutaService.optimizarRuta(idRuta, usarBloqueos));
    }
    
    @GetMapping("/distancia")
    public ResponseEntity<?> calcularDistancia(@RequestParam int x1, 
                                             @RequestParam int y1, 
                                             @RequestParam int x2, 
                                             @RequestParam int y2) {
        return ResponseEntity.ok(rutaService.calcularDistancia(x1, y1, x2, y2));
    }
}
```

## main\java\com\plg\controller\SimulacionController.java

```java
package com.plg.controller;

import com.plg.service.SimulacionService;
import com.plg.service.SimulacionTiempoRealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/simulacion")
public class SimulacionController {

    @Autowired
    private SimulacionService simulacionService;
    
    @Autowired
    private SimulacionTiempoRealService simulacionTiempoRealService;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping("/diario")
    public ResponseEntity<?> simularEscenarioDiario() {
        return ResponseEntity.ok(simulacionService.simularEscenarioDiario());
    }
    
    @GetMapping("/semanal")
    public ResponseEntity<?> simularEscenarioSemanal(
            @RequestParam(defaultValue = "7") int dias) {
        return ResponseEntity.ok(simulacionService.simularEscenarioSemanal(dias));
    }
    
    @GetMapping("/colapso")
    public ResponseEntity<?> simularEscenarioColapso() {
        return ResponseEntity.ok(simulacionService.simularEscenarioColapso());
    }
    
    @PostMapping("/personalizado")
    public ResponseEntity<?> simularEscenarioPersonalizado(@RequestBody Map<String, Object> params) {
        // Esta implementación es más avanzada y requeriría configuración adicional
        // Por ahora, delegamos al escenario diario con algunas modificaciones
        return ResponseEntity.ok(simulacionService.simularEscenarioDiario());
    }
    
    // NUEVOS ENDPOINTS PARA TIEMPO REAL
    
    @PostMapping("/iniciar-tiempo-real")
    public ResponseEntity<Map<String, Object>> iniciarSimulacionTiempoReal() {
        try {
            return ResponseEntity.ok(simulacionTiempoRealService.iniciarSimulacion());
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error al iniciar la simulación");
            error.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/detener-tiempo-real")
    public ResponseEntity<Map<String, Object>> detenerSimulacionTiempoReal() {
        try {
            return ResponseEntity.ok(simulacionTiempoRealService.detenerSimulacion());
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error al detener la simulación");
            error.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/ajustar-velocidad")
    public ResponseEntity<Map<String, Object>> ajustarVelocidadSimulacion(@RequestParam int factor) {
        try {
            return ResponseEntity.ok(simulacionTiempoRealService.ajustarVelocidad(factor));
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error al ajustar la velocidad");
            error.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/estado")
    public ResponseEntity<Map<String, Object>> obtenerEstadoSimulacion() {
        try {
            return ResponseEntity.ok(simulacionTiempoRealService.obtenerEstadoSimulacion());
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error al obtener el estado de la simulación");
            error.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    // Endpoint para obtener posiciones simplificadas (para evitar el problema de anidamiento)
    @GetMapping("/posiciones")
    public ResponseEntity<Map<String, Object>> obtenerPosicionesSimplificadas() {
        try {
            return ResponseEntity.ok(simulacionTiempoRealService.obtenerPosicionesSimplificadas());
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error al obtener las posiciones");
            error.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
```

## main\java\com\plg\controller\VisualizadorController.java

```java
package com.plg.controller;

import com.plg.service.VisualizadorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/visualizador")
public class VisualizadorController {

    @Autowired
    private VisualizadorService visualizadorService;

    @GetMapping("/mapa")
    public ResponseEntity<?> obtenerDatosMapa(
            @RequestParam(required = false) Boolean mostrarPedidos,
            @RequestParam(required = false) Boolean mostrarCamiones,
            @RequestParam(required = false) Boolean mostrarBloqueos,
            @RequestParam(required = false) Boolean mostrarAlmacenes) {
        
        return ResponseEntity.ok(visualizadorService.obtenerDatosMapa(
                mostrarPedidos == null ? true : mostrarPedidos,
                mostrarCamiones == null ? true : mostrarCamiones,
                mostrarBloqueos == null ? true : mostrarBloqueos,
                mostrarAlmacenes == null ? true : mostrarAlmacenes));
    }
    
    @GetMapping("/estado-general")
    public ResponseEntity<?> obtenerEstadoGeneral() {
        return ResponseEntity.ok(visualizadorService.obtenerEstadoGeneral());
    }
    
    @PostMapping("/filtrar")
    public ResponseEntity<?> filtrarVisualizacion(@RequestBody Map<String, Object> filtros) {
        return ResponseEntity.ok(visualizadorService.aplicarFiltros(filtros));
    }
}
```

## main\java\com\plg\dto\AgrupamientoAPResultadoDTO.java

```java
package com.plg.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgrupamientoAPResultadoDTO {
    private List<GrupoDTO> grupos;
    private String metodo;
    private Integer totalPedidos;
    private Integer totalGrupos;
}
```

## main\java\com\plg\dto\AlgoritmoGeneticoResultadoDTO.java

```java
package com.plg.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlgoritmoGeneticoResultadoDTO {
    private List<RutaDTO> rutas;
    private String metodo;
    private Integer totalPedidos;
    private Integer pedidosAsignados;
    private String mensaje;
    private Integer rutasGeneradas;
}
```

## main\java\com\plg\dto\ClienteDTO.java

```java
package com.plg.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDTO {
    private String id; // Cambiado de Long a String para coincidir con la entidad
    private String nombre;
    private String dni;
    private String telefono;
    private String email;
    private String direccion;
    private double posX; // Añadido para completitud
    private double posY; // Añadido para completitud
}
```

## main\java\com\plg\dto\GrupoDTO.java

```java
package com.plg.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrupoDTO {
    private String idGrupo;
    private PedidoDTO ejemplar;
    private Double centroideX;
    private Double centroideY;
    private List<PedidoDTO> pedidos;
    private Integer numeroPedidos;
    private Double radio;
    private Double densidad;
}
```

## main\java\com\plg\dto\PedidoDTO.java

```java
package com.plg.dto;

import com.plg.entity.EstadoPedido;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PedidoDTO {
    private Long id;
    private String codigo;
    private double posX;
    private double posY;
    private double volumenGLPAsignado; // Cambiado de Double a Integer para coincidir con la entidad
    private double horasLimite;
    private String clienteId; // Cambiado de Long a String para coincidir con la entidad
    private String clienteNombre;
    private String fechaHora; // Cambiado de LocalDateTime a String para coincidir con la entidad
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaEntregaRequerida;
    private LocalDateTime fechaEnregaReal;
    private EstadoPedido estado;
}
```

## main\java\com\plg\dto\PuntoRutaDTO.java

```java
package com.plg.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PuntoRutaDTO {
    private String tipo;  // "ALMACEN", "CLIENTE"
    private double posX;
    private double posY;
    private Long idPedido;
}
```

## main\java\com\plg\dto\RutaDTO.java

```java
package com.plg.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RutaDTO {
    private String idRuta;
    private Double distanciaTotal;
    private Integer tiempoEstimado;
    private List<PedidoDTO> pedidos;
    private Integer numeroPedidos;
    private List<PuntoRutaDTO> puntos;
    private String camionCodigo;
}
```

## main\java\com\plg\entity\Almacen.java

```java
package com.plg.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;
//importar column default
import org.hibernate.annotations.ColumnDefault;
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Almacen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nombre;
    private double posX; // Posición X en el mapa
    private double posY; // Posición Y en el mapa
    
    // !Capacidades para GLPP
    private double capacidadGLP; // Capacidad total de GLP en m3
    private double capacidadActualGLP; // Capacidad actual disponible de GLP en m3
    private double capacidadMaximaGLP; // Capacidad máxima para restaurar en reabastecimiento
    
    //!Capacidades para combustible (gasolina/petróleo)
    private double capacidadCombustible; // Capacidad total de combustible en galones
    private double capacidadActualCombustible; // Capacidad actual disponible de combustible en galones 
    private double capacidadMaximaCombustible; // Capacidad máxima para restaurar en reabastecimiento
    

    //! Tipo de almacén
    private boolean esCentral; // Indica si es el almacén central (true) o intermedio (false)
    private boolean permiteCamionesEstacionados; // Solo el central permite esto por defecto
    private String tipo;
    //el tio se asigna segun el b
    // Hora de reabastecimiento para almacenes intermedios
    private LocalTime horaReabastecimiento = LocalTime.MIDNIGHT; // Por defecto a las 00:00
    private boolean ultimoReabastecimientoRealizado = false; // Indica si ya se realizó el reabastecimiento hoy
    @Column(name = "activo")
    @ColumnDefault("true")
    private boolean activo; // Estado del almacén (activo/inactivo)
     
    //!Puede recargar?
    // Método para verificar si el almacén puede recargar combustible
    public boolean puedeRecargarCombustible(double cantidadRequerida) {
        return capacidadActualCombustible >= cantidadRequerida && activo;
    }
    
    // Método para verificar si el almacén puede suplir GLP
    public boolean puedeProveerGLP(double cantidadRequerida) {
        return capacidadActualGLP >= cantidadRequerida && activo;
    }
    
    // Método para recargar combustible a un camión
    public boolean recargarCombustible(Camion camion, double cantidad) {
        if (!puedeRecargarCombustible(cantidad)) {
            return false;
        }
        
        // Verificar capacidad disponible en el tanque del camión
        double espacioDisponibleCamion = camion.getCapacidadTanque() - camion.getCombustibleActual();
        double cantidadEfectiva = Math.min(cantidad, Math.min(espacioDisponibleCamion, capacidadActualCombustible));
        
        if (cantidadEfectiva <= 0) {
            return false;
        }
        
        // Realizar la recarga
        this.capacidadActualCombustible -= cantidadEfectiva;
        camion.setCombustibleActual(camion.getCombustibleActual() + cantidadEfectiva);
        return true;
    }
    
    // Método para calcular la distancia desde este almacén hasta una posición
    public double calcularDistancia(double posX2, double posY2) {
        // Distancia Manhattan: suma de las diferencias absolutas en cada dimensión
        return Math.abs(posX - posX2) + Math.abs(posY - posY2);
    }
    
    // Método para reabastecer el almacén
    public void reabastecer() {
        if (!esCentral) {
            // Solo reabastecemos los almacenes intermedios
            this.capacidadActualGLP = this.capacidadMaximaGLP;
            this.capacidadActualCombustible = this.capacidadMaximaCombustible;
            this.ultimoReabastecimientoRealizado = true;
        }
    }
}
```

## main\java\com\plg\entity\AsignacionCamion.java

```java
package com.plg.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignacionCamion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "camion_id")
    @JsonBackReference(value="camion-asignacion")
    private Camion camion;
    
    @ManyToOne
    @JoinColumn(name = "pedido_id")
    @JsonBackReference(value="pedido-asignacion")
    private Pedido pedido;
    
    private double volumenAsignado;
    private double porcentajeAsignado;
    
    @ManyToOne
    @JoinColumn(name = "ruta_id")
    @JsonBackReference(value="ruta-asignacion")
    private Ruta ruta;
    
    private boolean entregado;
    private LocalDateTime fechaEntregaParcial;
    
    // Constructor para compatibilidad con código existente
    public AsignacionCamion(Camion camion, double volumenAsignado, double porcentajeAsignado) {
        this.camion = camion;
        this.volumenAsignado = volumenAsignado;
        this.porcentajeAsignado = porcentajeAsignado;
        this.entregado = false;
    }
}
```

## main\java\com\plg\entity\Averia.java

```java
package com.plg.entity;

import java.time.LocalDateTime;
import java.util.Random;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Averia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "camion_codigo")
    @JsonBackReference(value="camion-averia")
    private Camion camion;
    
    private LocalDateTime fechaHoraReporte;
    private String descripcion;
    private String turno; // T1, T2, T3
    private String tipoIncidente; // TI1, TI2, TI3
    private double posX;
    private double posY;
    private double kilometroOcurrencia; // Punto del trayecto donde ocurre la avería
    private int estado; // 0: reportada, 1: atendida, 2: reparada
    private boolean conCarga; // Indica si el camión llevaba carga cuando ocurrió la avería
    
    // Campos adicionales según las nuevas especificaciones
    private LocalDateTime tiempoInmovilizacion; // Tiempo que permanece inmovilizado
    private LocalDateTime tiempoFinInoperatividad; // Tiempo en que estará disponible nuevamente
    private boolean requiereTraslado; // Si debe ser trasladado al almacén (tipo 2 y 3)
    private boolean esValida; // Indica si la avería es válida (unidad en operación y con carga)
    
    /**
     * Calcula si una avería es válida según las condiciones:
     * - La unidad debe estar en operación
     * - La avería solo tiene sentido si lleva carga
     * @param estaEnOperacion indica si el camión está en operación
     * @return true si la avería es válida, false en caso contrario
     */
    public boolean calcularValidezAveria(boolean estaEnOperacion) {
        esValida = estaEnOperacion && conCarga;
        return esValida;
    }
    
    /**
     * Calcula el kilómetro de ocurrencia de la avería en el rango de 5% a 35% del tramo total
     * @param distanciaTotal la distancia total del recorrido (ida y vuelta)
     */
    public void calcularKilometroOcurrencia(double distanciaTotal) {
        if (!esValida) return;
        
        Random random = new Random();
        // Calcular kilómetro de ocurrencia entre 5% y 35% del tramo total
        double minKm = distanciaTotal * 0.05;
        double maxKm = distanciaTotal * 0.35;
        kilometroOcurrencia = minKm + (maxKm - minKm) * random.nextDouble();
    }
    
    // Método para generar el formato de registro según especificación
    public String generarRegistro() {
        if (camion == null) return "";
        return String.format("%s_%s_%s", 
                            turno, 
                            camion.getCodigo(), 
                            tipoIncidente);
    }
    
    /**
     * Método para calcular tiempo de inmovilización según tipo de incidente
     * Considera la duración de los turnos (por defecto 8 horas)
     * @param duracionTurnoHoras duración de cada turno en horas (por defecto 8)
     */
    public void calcularTiemposInoperatividad(int duracionTurnoHoras) {
        if (tipoIncidente == null || fechaHoraReporte == null) return;

        LocalDateTime ahora = fechaHoraReporte;

        switch (tipoIncidente) {
            case "TI1" -> {
                // Incidente tipo 1: inmoviliza 2 horas, continúa ruta
                tiempoInmovilizacion = ahora.plusHours(2);
                tiempoFinInoperatividad = tiempoInmovilizacion;
                requiereTraslado = false;
            }
            case "TI2" -> {
                // Incidente tipo 2: inmoviliza 2 horas + un turno completo
                tiempoInmovilizacion = ahora.plusHours(2);
                requiereTraslado = true;
                tiempoFinInoperatividad = ahora.plusHours(duracionTurnoHoras * 2);  
            }
            case "TI3" -> {
                // Incidente tipo 3: inmoviliza 4 horas + tres días completos
                tiempoInmovilizacion = ahora.plusHours(4);
                requiereTraslado = true;
                // Disponible en turno 1 del día A+3
                tiempoFinInoperatividad = ahora.plusDays(3).withHour(0).plusHours(duracionTurnoHoras);
            }
            default -> {
            }
        }
        // Tipo de incidente no reconocido, no se realiza ninguna acción
            }
    
    /**
     * Sobrecarga del método para usar la duración de turno por defecto (8 horas)
     */
    public void calcularTiemposInoperatividad() {
        calcularTiemposInoperatividad(8);
    }
}
```

## main\java\com\plg\entity\Bloqueo.java

```java
package com.plg.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bloqueo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    
    @ElementCollection
    @CollectionTable(name = "bloqueo_coordenadas", joinColumns = @JoinColumn(name = "bloqueo_id"))
    private List<Coordenada> coordenadas = new ArrayList<>();
    
    
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String descripcion;
    private boolean activo;
    
    /**
     * Método para determinar si un punto está en un tramo bloqueado
     * @param x Coordenada X a verificar
     * @param y Coordenada Y a verificar
     * @return true si el punto está en un tramo bloqueado
     */
    public boolean contienePunto(double x, double y) {
        // Si hay menos de 2 coordenadas, no hay tramos
        if (coordenadas.size() < 2) {
            return false;
        }
        
        // Verificar cada tramo (par de coordenadas consecutivas)
        for (int i = 0; i < coordenadas.size() - 1; i++) {
            Coordenada inicio = coordenadas.get(i);
            Coordenada fin = coordenadas.get(i + 1);
            
            // Verificar si el punto está en la línea entre inicio y fin
            if (estaPuntoEnLinea(x, y, inicio.getX(), inicio.getY(), fin.getX(), fin.getY())) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Verifica si un punto está en una línea dentro de un mapa reticular
     */
    private boolean estaPuntoEnLinea(double puntoX, double puntoY, double lineaInicioX, double lineaInicioY, double lineaFinX, double lineaFinY) {
        // En un mapa reticular, verificamos si el punto está exactamente en la línea
        return estaPuntoEnSegmento(puntoX, puntoY, lineaInicioX, lineaInicioY, lineaFinX, lineaFinY);
    }

    /**
     * Verifica si un punto está dentro del segmento de línea
     */
    private boolean estaPuntoEnSegmento(double puntoX, double puntoY, double segmentoInicioX, double segmentoInicioY, double segmentoFinX, double segmentoFinY) {
        // Calculamos el rango de coordenadas del segmento
        double rangoMinX = Math.min(segmentoInicioX, segmentoFinX);
        double rangoMaxX = Math.max(segmentoInicioX, segmentoFinX);
        double rangoMinY = Math.min(segmentoInicioY, segmentoFinY);
        double rangoMaxY = Math.max(segmentoInicioY, segmentoFinY);

        // Verificamos si el punto está dentro del rango del segmento
        return puntoX >= rangoMinX && puntoX <= rangoMaxX && puntoY >= rangoMinY && puntoY <= rangoMaxY;
    }
    /**
     * Convierte el bloqueo a formato de registro para archivo
     */
    public String convertirARegistro() {
        // Formato: ##d##h##m-##d##h##m:x1,y1,x2,y2,...,xn,yn
        StringBuilder registro = new StringBuilder();
        
        // Formatear fechas
        registro.append(formatearFecha(fechaInicio));
        registro.append("-");
        registro.append(formatearFecha(fechaFin));
        registro.append(":");
        
        // Añadir coordenadas
        for (int i = 0; i < coordenadas.size(); i++) {
            Coordenada coord = coordenadas.get(i);
            registro.append(coord.getX()).append(",").append(coord.getY());
            
            // Añadir coma si no es la última coordenada
            if (i < coordenadas.size() - 1) {
                registro.append(",");
            }
        }
        
        return registro.toString();
    }
    
    /**
     * Formatea una fecha en el formato ##d##h##m
     */
    private String formatearFecha(LocalDateTime fecha) {
        return String.format("%02dd%02dh%02dm", 
            fecha.getDayOfMonth(), fecha.getHour(), fecha.getMinute());
    }
        /**
     * Verifica si un segmento de ruta intersecta con este bloqueo
     * @param x1 Coordenada X del punto inicial del segmento
     * @param y1 Coordenada Y del punto inicial del segmento
     * @param x2 Coordenada X del punto final del segmento
     * @param y2 Coordenada Y del punto final del segmento
     * @return true si el segmento intersecta con algún tramo bloqueado
     */
    public boolean intersectaConSegmento(double x1, double y1, double x2, double y2) {
        // Si hay menos de 2 coordenadas, no hay tramos
        if (coordenadas.size() < 2) {
            return false;
        }
        
        // Verificar cada tramo del bloqueo (par de coordenadas consecutivas)
        for (int i = 0; i < coordenadas.size() - 1; i++) {
            Coordenada inicioBloqueo = coordenadas.get(i);
            Coordenada finBloqueo = coordenadas.get(i + 1);
            
            // Verificar si los segmentos se intersecan
            if (seIntersecaConSegmento(
                    x1, y1, x2, y2,
                    inicioBloqueo.getX(), inicioBloqueo.getY(),
                    finBloqueo.getX(), finBloqueo.getY())) {
                return true;
            }
        }
        
        // Verificar también si algún extremo del segmento está dentro del bloqueo
        if (contienePunto(x1, y1) || contienePunto(x2, y2)) {
            return true;
        }
        
        return false;
    }

    /**
     * Verifica si dos segmentos de línea se intersecan
     */
    private boolean seIntersecaConSegmento(
        double x1, double y1, double x2, double y2,
        double x3, double y3, double x4, double y4) {
        
 
        
        // Primero calculamos los denominadores para las ecuaciones
        double denominator = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
        
        // Si el denominador es 0, las líneas son paralelas
        if (denominator == 0) {
            // Verificamos si un punto del segmento 1 está en el segmento 2 (colineales)
            return estaPuntoEnSegmento(x1, y1, x3, y3, x4, y4) || 
                estaPuntoEnSegmento(x2, y2, x3, y3, x4, y4) ||
                estaPuntoEnSegmento(x3, y3, x1, y1, x2, y2) || 
                estaPuntoEnSegmento(x4, y4, x1, y1, x2, y2);
        }
        
        // Calculamos los valores de t y u
        double t = ((x3 - x1) * (y4 - y3) - (y3 - y1) * (x4 - x3)) / (double) denominator;
        double u = ((x3 - x1) * (y2 - y1) - (y3 - y1) * (x2 - x1)) / (double) denominator;
        
        // Si t y u están entre 0 y 1, los segmentos se intersecan
        return t >= 0 && t <= 1 && u >= 0 && u <= 1;
    }
    // Clase interna para representar una coordenada
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Coordenada {
        private double x;
        private double y;
    }
    
}
```

## main\java\com\plg\entity\Camion.java

```java
package com.plg.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "camiones")
public class Camion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ID único del camión

    private String codigo;
    
    private String tipo; // TA, TB, TC, TD, etc.

    //!GLP
    private double capacidad; // Capacidad en m3 de GLP
    private double capacidadDisponible; // Capacidad disponible actual (m3)
    private double tara; // Peso del camión vacío en toneladas
    private double pesoCarga; // Peso actual de la carga en toneladas
    private double pesoCombinado; // Peso total (tara + carga)
    
 
    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoCamion estado; 
    
    //!combustible Atributos relacionados con 
    @Column(name = "capacidad_tanque")
    private double capacidadTanque = 25.0; // Capacidad del tanque en galones
    
    @Column(name = "combustible_actual")
    private double combustibleActual; // Combustible actual en galones
    
    @Column(name = "velocidad_promedio")
    private double velocidadPromedio = 50.0; // Velocidad promedio en km/h
    
    // Posición actual del camión (para calcular distancia a recorrer)
    @Column(name = "pos_x")
    private double posX;
    
    @Column(name = "pos_y")
    private double posY;
    
    // Último almacén visitado
    @ManyToOne
    @JoinColumn(name = "ultimo_almacen_id")
    private Almacen ultimoAlmacen;
    
    // Fecha de la última carga de GLP
    @Column(name = "fecha_ultima_carga")
    private LocalDateTime fechaUltimaCarga;
    
    // Porcentaje de uso actual
    @Column(name = "porcentaje_uso")
    private double porcentajeUso;
    
    @OneToMany(mappedBy = "camion", cascade = CascadeType.ALL)
    @JsonManagedReference(value="camion-mantenimiento")
    private List<Mantenimiento> mantenimientos = new ArrayList<>();
    
    @OneToMany(mappedBy = "camion", cascade = CascadeType.ALL)
    @JsonManagedReference(value="camion-averia")
    private List<Averia> averias = new ArrayList<>();
    
    @OneToMany(mappedBy = "camion", cascade = CascadeType.ALL)
    @JsonManagedReference(value="camion-pedido")
    private List<Pedido> pedidos = new ArrayList<>();
    
    @OneToMany(mappedBy = "camion", cascade = CascadeType.ALL)
    @JsonManagedReference(value="camion-ruta")
    private List<Ruta> rutas = new ArrayList<>();
    
    @OneToMany(mappedBy = "camion", cascade = CascadeType.ALL)
    @JsonManagedReference(value="camion-entregaparcial")
    private List<EntregaParcial> entregasParciales = new ArrayList<>();
    
    /**
     * Constructor con parámetros básicos
     */
    public Camion(String codigo, String tipo, double capacidad, double tara) {
        this.codigo = codigo;
        this.tipo = tipo;
        this.capacidad = capacidad;
        this.capacidadDisponible = capacidad;
        this.tara = tara;
        this.setEstado(EstadoCamion.DISPONIBLE); // Disponible por defecto
        this.porcentajeUso = 0.0;
        inicializar();
    }
 
 

    /**
     * Asigna un volumen parcial de GLP de un pedido a este camión
     * @param pedido Pedido a asignar
     * @param volumen Volumen a entregar (en m3)
     * @param porcentaje Porcentaje del pedido que representa
     * @return true si se pudo asignar, false si no hay capacidad suficiente
     */
    public boolean asignarPedidoParcial(Pedido pedido, double volumen, double porcentaje) {
        // Verificar si hay capacidad disponible
        if (capacidadDisponible < volumen) {
            return false;
        }
        
        // Actualizar capacidad disponible
        capacidadDisponible -= volumen;
        
        // Actualizar porcentaje de uso
        actualizarPorcentajeUso();
        
        // Actualizar peso de carga y combinado
        actualizarPeso();
        
        // Crear nueva entrega parcial
        EntregaParcial entrega = new EntregaParcial();
        entrega.setCamion(this);
        entrega.setPedido(pedido);
        entrega.setVolumenGLP(volumen);
        entrega.setPorcentajePedido(porcentaje);
        entrega.setFechaAsignacion(LocalDateTime.now());
        entrega.setEstado(0); // Asignado
        
        // Agregar a la lista de entregas parciales
        entregasParciales.add(entrega);
        
        return true;
    }
    
    /**
     * Libera capacidad después de una entrega
     * @param volumen Volumen liberado (en m3)
     */
    public void liberarCapacidad(double volumen) {
        capacidadDisponible += volumen;
        if (capacidadDisponible > capacidad) {
            capacidadDisponible = capacidad;
        }
        
        // Actualizar porcentaje de uso
        actualizarPorcentajeUso();
        
        // Actualizar peso después de liberar capacidad
        actualizarPeso();
    }
    
    /**
     * Actualiza el porcentaje de uso
     */
    private void actualizarPorcentajeUso() {
        porcentajeUso = ((capacidad - capacidadDisponible) / capacidad) * 100;
    }
    
    /**
     * Actualiza el peso de carga y combinado
     * El peso del GLP es aproximadamente 0.55 ton/m3
     */
    private void actualizarPeso() {
        this.pesoCarga = (capacidad - capacidadDisponible) * 0.5; // Peso del GLP en toneladas
        this.pesoCombinado = tara + pesoCarga;
    }
    
    /**
     * Realiza una recarga   de GLP
     */
    public void recargarGLP(double volumenGLP) {
        capacidadDisponible += volumenGLP;
        if (capacidadDisponible > capacidad) {
            capacidadDisponible = capacidad;
        }
        
        // Actualizar porcentaje de uso
        actualizarPorcentajeUso();
        
        // Actualizar peso después de recargar
        actualizarPeso();
    }
    
    /**
     * Realiza una recarga de combustible
     * @param cantidadGalones Cantidad a recargar en galones
     */
    public void recargarCombustible(double cantidadGalones) {
        combustibleActual += cantidadGalones;
        if (combustibleActual > capacidadTanque) {
            combustibleActual = capacidadTanque;
        }
        
        // Si estaba sin combustible, actualizar su estado
        if (getEstado() == EstadoCamion.SIN_COMBUSTIBLE) {
            setEstado(EstadoCamion.DISPONIBLE);
        }
    }
    
    /**
     * Consume combustible durante un recorrido
     * @param cantidadGalones Cantidad a consumir en galones
     * @return true si se pudo consumir, false si no hay suficiente
     */
    public boolean consumirCombustible(double cantidadGalones) {
        if (combustibleActual < cantidadGalones) {
            return false;
        }
        
        combustibleActual -= cantidadGalones;
        
        // Si se quedó sin combustible, actualizar su estado
        if (combustibleActual <= 0.1) {
            setEstado(EstadoCamion.SIN_COMBUSTIBLE);
        }
        
        return true;
    }
    
    /**
     * Obtiene las entregas parciales pendientes
     */
    public List<EntregaParcial> getEntregasPendientes() {
        List<EntregaParcial> pendientes = new ArrayList<>();
        for (EntregaParcial entrega : entregasParciales) {
            if (entrega.getEstado() != 2) { // No entregado
                pendientes.add(entrega);
            }
        }
        
        return pendientes;
    }
    
    /**
     * Obtiene el volumen total de GLP asignado actualmente
     */
    public double getVolumenTotalAsignado() {
        double total = 0.0;
        for (EntregaParcial entrega : entregasParciales) {
            if (entrega.getEstado() != 2) { // No entregado
                total += entrega.getVolumenGLP();
            }
        }
        
        return total;
    }
    
    /**
     * Marca una entrega parcial como completada
     */
    public boolean completarEntregaParcial(Long pedidoId) {
        for (EntregaParcial entrega : entregasParciales) {
            if (entrega.getPedido().getId().equals(pedidoId) && entrega.getEstado() != 2) {
                entrega.setEstado(2); // Entregado
                entrega.setFechaEntrega(LocalDateTime.now());
                liberarCapacidad(entrega.getVolumenGLP());
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Verifica si el camión tiene capacidad para un volumen adicional
     */
    public boolean tieneCapacidadPara(double volumenAdicional) {
        return capacidadDisponible >= volumenAdicional;
    }
    
    /**
     * Calcula consumo de combustible para una distancia
     * @param distanciaKm Distancia a recorrer en kilómetros
     * @return Consumo en galones
     */
    public double calcularConsumoCombustible(double distanciaKm) {
        return distanciaKm * pesoCombinado / 180.0;
    }
    
    /**
     * Calcula la distancia máxima que puede recorrer con el combustible actual
     * @return Distancia máxima en kilómetros
     */
    public double calcularDistanciaMaxima() {
        if (pesoCombinado <= 0) {
            return 0.0; // Evitar división por cero
        }
        return combustibleActual * 180.0 / pesoCombinado;
    }
    
    /**
     * Inicializa el camión con valores por defecto
     */
    public void inicializar() {
        if (capacidadDisponible <= 0) {
            capacidadDisponible = capacidad;
        }
        
        if (combustibleActual <= 0) {
            combustibleActual = capacidadTanque * 1; // Inicializa con 100% del tanque
        }
        
        actualizarPorcentajeUso();
        actualizarPeso();
    }
    
    /**
     * Reporta una avería y cambia el estado del camión
     */
    public Averia reportarAveria(String descripcion) {
        Averia averia = new Averia();
        averia.setCamion(this);
        averia.setDescripcion(descripcion);
        averia.setFechaHoraReporte(LocalDateTime.now());
        averia.setEstado(0); // Pendiente
        
        this.setEstado(EstadoCamion.EN_MANTENIMIENTO_POR_AVERIA); // Averiado
        
        if (this.averias == null) {
            this.averias = new ArrayList<>();
        }
        this.averias.add(averia);
        
        return averia;
    }
    
 
    /**
     * Mover el camión a nuevas coordenadas
     */
    public void moverA(int nuevoX, int nuevoY) {
        this.posX = nuevoX;
        this.posY = nuevoY;
    }
    
    /**
     * Calcula la distancia desde la posición actual hasta un punto
     * usando distancia Manhattan
     */
    public double calcularDistanciaHasta(int destinoX, int destinoY) {
        return Math.abs(destinoX - this.posX) + Math.abs(destinoY - this.posY);
    }
    
    /**
     * Obtiene el estado del camión como texto
     */
    @Transient
    public String getEstadoTexto() {
        return getEstado().getDescripcion();
    }
    
    /**
     * Actualiza el estado de las entregas parciales cuando la ruta está en curso
     */
    public void actualizarEstadoEntregasARuta() {
        for (EntregaParcial entrega : entregasParciales) {
            if (entrega.getEstado() == 0) { // Si está asignada
                entrega.setEstado(1); // Cambiar a "En ruta"
            }
        }
    }
    
    /**
     * Obtiene información básica del camión para APIs
     */
    @Transient
    public Map<String, Object> getInfoBasica() {
        Map<String, Object> info = new HashMap<>();
        info.put("codigo", this.codigo);
        info.put("tipo", this.tipo);
        info.put("capacidad", this.capacidad);
        info.put("capacidadDisponible", this.capacidadDisponible);
        info.put("porcentajeUso", this.porcentajeUso); 
        info.put("estado", this.estado);
        info.put("estadoTexto", this.getEstadoTexto());
        info.put("posX", this.posX);
        info.put("posY", this.posY);
        info.put("combustibleActual", this.combustibleActual);
        info.put("distanciaMaxima", this.calcularDistanciaMaxima());
        return info;
    }
    
    /**
     * Obtiene información detallada de las entregas parciales para APIs
     */
    @Transient
    public List<Map<String, Object>> getInfoEntregasParciales() {
        List<Map<String, Object>> listaEntregas = new ArrayList<>();
        
        for (EntregaParcial entrega : entregasParciales) {
            Map<String, Object> infoEntrega = new HashMap<>();
            infoEntrega.put("id", entrega.getId());
            infoEntrega.put("pedidoId", entrega.getPedido().getId());
            infoEntrega.put("codigoPedido", entrega.getPedido().getCodigo());
            infoEntrega.put("volumenGLP", entrega.getVolumenGLP());
            infoEntrega.put("porcentaje", entrega.getPorcentajePedido());
            infoEntrega.put("estado", entrega.getEstado());
            
            switch (entrega.getEstado()) {
                case 0:
                    infoEntrega.put("estadoTexto", "Asignado");
                    break;
                case 1:
                    infoEntrega.put("estadoTexto", "En ruta");
                    break;
                case 2:
                    infoEntrega.put("estadoTexto", "Entregado");
                    break;
                case 3:
                    infoEntrega.put("estadoTexto", "Cancelado");
                    break;
                default:
                    infoEntrega.put("estadoTexto", "Desconocido");
            }
            
            if (entrega.getFechaAsignacion() != null) {
                infoEntrega.put("fechaAsignacion", entrega.getFechaAsignacion().toString());
            }
            
            if (entrega.getFechaEntrega() != null) {
                infoEntrega.put("fechaEntrega", entrega.getFechaEntrega().toString());
            }
            
            listaEntregas.add(infoEntrega);
        }
        
        return listaEntregas;
    }
}
```

## main\java\com\plg\entity\Cliente.java

```java
package com.plg.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Cliente {
    @Id
    private String id; // código único del cliente
    
    private String nombre;
    private String direccion;
    private String telefono;
    private String email;
    private double posX;
    private double posY;
    
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    @JsonManagedReference(value="cliente-pedido")
    private List<Pedido> pedidos;
}
```

## main\java\com\plg\entity\EntregaParcial.java

```java
package com.plg.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidad que representa una entrega parcial de un pedido.
 * Permite que un pedido pueda ser entregado en partes por diferentes camiones.
 */
@Entity
@AllArgsConstructor
@Getter
@Setter
@Table(name = "entregas_parciales")
public class EntregaParcial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "camion_id")
    @JsonBackReference(value="camion-entregaparcial")
    private Camion camion;
    
    @ManyToOne
    @JoinColumn(name = "pedido_id")
    @JsonBackReference(value="pedido-entregaparcial")
    private Pedido pedido;
    
    @Column(name = "volumen_glp")
    private double volumenGLP; // Volumen asignado para esta entrega parcial
    
    @Column(name = "porcentaje_pedido")
    private double porcentajePedido; // Porcentaje del pedido que representa esta entrega
    
    @Column(name = "fecha_asignacion")
    private LocalDateTime fechaAsignacion;
    
    @Column(name = "fecha_entrega")
    private LocalDateTime fechaEntrega;
    
    @Column(name = "estado")
    private int estado; // 0: Asignado, 1: En ruta, 2: Entregado, 3: Cancelado
    
    @Column(name = "observaciones")
    private String observaciones;
    
    /**
     * Constructor por defecto
     */
    public EntregaParcial() {
        this.fechaAsignacion = LocalDateTime.now();
        this.estado = 0; // Asignado por defecto
    }
    
    /**
     * Constructor con información básica
     */
    public EntregaParcial(Pedido pedido, double volumenGLP, double porcentajePedido) {
        this();
        this.pedido = pedido;
        this.volumenGLP = volumenGLP;
        this.porcentajePedido = porcentajePedido;
    } 
  
  
    /**
     * Builder pattern para crear instancias de EntregaParcial
     */
    public static EntregaParcialBuilder builder() {
        return new EntregaParcialBuilder();
    }
    
    /**
     * Builder para EntregaParcial
     */
    public static class EntregaParcialBuilder {
        private Pedido pedido;
        private double volumenGLP;
        private double porcentajePedido;
        private int estado;
        private String observaciones;
        
        public EntregaParcialBuilder pedido(Pedido pedido) {
            this.pedido = pedido;
            return this;
        }
        
        public EntregaParcialBuilder volumenGLP(double volumenGLP) {
            this.volumenGLP = volumenGLP;
            return this;
        }
        
        public EntregaParcialBuilder porcentajePedido(double porcentajePedido) {
            this.porcentajePedido = porcentajePedido;
            return this;
        }
        
        public EntregaParcialBuilder estado(int estado) {
            this.estado = estado;
            return this;
        }
        
        public EntregaParcialBuilder observaciones(String observaciones) {
            this.observaciones = observaciones;
            return this;
        }
        
        public EntregaParcial build() {
            EntregaParcial entrega = new EntregaParcial(pedido, volumenGLP, porcentajePedido);
            if (estado != 0) {
                entrega.setEstado(estado);
            }
            if (observaciones != null) {
                entrega.setObservaciones(observaciones);
            }
            return entrega;
        }
    }
}
```

## main\java\com\plg\entity\EstadoCamion.java

```java
package com.plg.entity;

public enum EstadoCamion {

    DISPONIBLE("Camión listo para operar y sin ninguna entrega en progreso", "#00FF00"),
    NO_DISPONIBLE("Camión no listo para operar", "#FF0000"),
    EN_RUTA("Camión actualmente en camino a realizar una entrega en movimiento", "#0000FF"),

    ENTREGANDO_GLP_A_CLIENTE("Camión en proceso de descarga de GLP al cliente", "#0066CC"),

    //!MANTENIMIENTO
    EN_MANTENIMIENTO("Camión en mantenimiento por diferentes motivos", "#000000"),
    
    EN_MANTENIMIENTO_PREVENTIVO("Mantenimiento preventivo programado (1 día)", "#FFCC00"),

    EN_MANTENIMIENTO_CORRECTIVO("Aun no especificado por el profesor", "#FF9900"),

    EN_MANTENIMIENTO_POR_AVERIA("Camión fuera de operación por avería (taller)", "#990000"), 
    //!INMOVILIZADO
    INMOVILIZADO_POR_AVERIA("Detenido en nodo por avería menor o incidente (2h o 4h)", "#CC3300"),

    SIN_COMBUSTIBLE("Camión sin gasolina suficiente para continuar", "#808080"),

    //!RECARGANDO_O_ENTREGANDO_COMBUSTIBLE
    RECIBIENDO_COMBUSTIBLE("Recargando gasolina (combustible de motor) en planta central o intermedio o desde otro camion ", "#6666FF"),
    
	ENTREGANDO_COMBUSTIBLE_A_CAMION(" Dando combustible a otrocamion ", "#6666FF"),

    RECIBIENDO_GLP("Recargando GLP para entregas en planta central o intermedio o desde otro camion", "#66CC00"),

    ENTREGANDO_GLP_A_CAMION("Transfiriendo GLP hacia  otro camión", "#33CCCC"), 

    ALMACEN_TEMPORAL("Unidad averiada actuando como depósito temporal de GLP", "#9933CC");

    private final String descripcion;
    private final String colorHex;

    EstadoCamion(String descripcion, String colorHex) {
        this.descripcion = descripcion;
        this.colorHex = colorHex;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getColorHex() {
        return colorHex;
    }
}

```

## main\java\com\plg\entity\EstadoPedido.java

```java
package com.plg.entity;

 
public enum EstadoPedido {
    //!SIN_ASIGNAR
    SIN_ASIGNAR("Pedido sin asignar, ya que recien seregistro el pedido", "#000000"),
    REGISTRADO(
            "Pedido ingresado. Aún no planificado", "#CCCCCC"),

    PENDIENTE_PLANIFICACION("En espera de planificación", "#FFCC00"),

    //!PLANIFICADO
    PLANIFICADO("Pedido planificado ya sea total o parcialmente", "#009900"),  

    PLANIFICADO_PARCIALMENTE("Solo una parte del pedido ha sido planificada", "#FFDD66"),

    PLANIFICADO_TOTALMENTE("El 100% del pedido ha sido planificado", "#00BFFF"),

    EN_RUTA("Pedido en tránsito hacia el cliente", "#3399FF"),

    //!RECIBIENDO GLP
    RECIBIENDO("El cliente está recibiendo el GLP solicitado ya sea todo el GLP o de forma parcialmente ", "#FFCC33"),
    RECIBIENDO_PARCIALMENTE("El cliente está recibiendo una parte del GLP solicitado", "#6699FF"),

    RECIBIENDO_TOTALMENTE("El cliente está recibiendo todo el GLP del pedido", "#0066CC"),
    //!ENTREGADO
    ENTREGADO_PARCIALMENTE("Entrega parcial completada. Aún falta parte del pedido", "#FF9966"),

    ENTREGADO_TOTALMENTE("Pedido completado. Se entregó el 100%", "#00CC66"),

    REPROGRAMADO("El pedido fue replanificado por logística o incidente", "#FF9900"),

    NO_ENTREGADO_EN_TIEMPO("El pedido no se cumplió en el plazo indicado", "#FF3333");

    private final String descripcion;
    private final String colorHex;

    EstadoPedido(String descripcion, String colorHex) {
        this.descripcion = descripcion;
        this.colorHex = colorHex;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getColorHex() {
        return colorHex;
    }
}

```

## main\java\com\plg\entity\Mantenimiento.java

```java
package com.plg.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
 
import java.time.LocalDate;

@Entity
@Data
@Getter
@Setter
@Builder
@Table(name = "mantenimiento")
@NoArgsConstructor
@AllArgsConstructor
public class Mantenimiento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "camion_codigo")
    @JsonBackReference(value="camion-mantenimiento")
    private Camion camion;
    
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String tipo; // preventivo, correctivo
    private String descripcion;
    private int estado; // 0: programado, 1: en proceso, 2: finalizado
    
    
}
```

## main\java\com\plg\entity\NodoRuta.java

```java
package com.plg.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidad que representa un punto o nodo en una ruta.
 * Un nodo puede ser un punto de origen (almacén), destino (cliente) o punto intermedio.
 */
@Getter
@Setter
@Entity
@Table(name = "nodos_ruta")
public class NodoRuta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "ruta_id")
    @JsonBackReference(value="ruta-nodo")
    private Ruta ruta;
    
    @Column(name = "orden")
    private int orden;
    
    @Column(name = "pos_x")
    private double posX;
    
    @Column(name = "pos_y")
    private double posY;
    
    @Column(name = "tipo")
    private String tipo; // "ALMACEN", "CLIENTE", "INTERMEDIO"
    
    @ManyToOne
    @JoinColumn(name = "pedido_id")
    @JsonBackReference(value="pedido-nodo")
    private Pedido pedido; // Solo para nodos de tipo "CLIENTE"
    
    @Column(name = "volumen_glp")
    private double volumenGLP; // Volumen a entregar en este nodo
    
    @Column(name = "porcentaje_pedido")
    private double porcentajePedido; // Porcentaje del pedido que se entrega en este nodo
    
    @Column(name = "entregado")
    private boolean entregado;
    
    @Column(name = "tiempo_llegada_estimado")
    private LocalDateTime tiempoLlegadaEstimado;
    
    @Column(name = "tiempo_llegada_real")
    private LocalDateTime tiempoLlegadaReal;
    
    @Column(name = "observaciones")
    private String observaciones;
    
    /**
     * Constructor por defecto
     */
    public NodoRuta() {
        this.entregado = false;
    }
    
    /**
     * Constructor con coordenadas y tipo
     */
    public NodoRuta(double posX, double posY, String tipo) {
        this();
        this.posX = posX;
        this.posY = posY;
        this.tipo = tipo;
    }
    
    /**
     * Constructor con todos los campos relevantes para un nodo de cliente
     */
    public NodoRuta(double posX, double posY, String tipo, Pedido pedido, double volumenGLP, double porcentajePedido) {
        this(posX, posY, tipo);
        this.pedido = pedido;
        this.volumenGLP = volumenGLP;
        this.porcentajePedido = porcentajePedido;
    }
    
    
     
    /**
     * Calcula la distancia Manhattan a otro nodo
     */
    public double distanciaA(NodoRuta otro) {
        return Math.abs(this.posX - otro.posX) + Math.abs(this.posY - otro.posY);
    }
    
    /**
     * Convierte a representación de cadena
     */
    @Override
    public String toString() {
        return "NodoRuta{" +
               "id=" + id +
               ", orden=" + orden +
               ", pos=(" + posX + "," + posY + ")" +
               ", tipo='" + tipo + '\'' +
               (pedido != null ? ", pedidoId=" + pedido.getId() : "") +
               '}';
    }
}
```

## main\java\com\plg\entity\Pedido.java

```java
package com.plg.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "pedidos")
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String codigo;
    
    @ManyToOne
    @JoinColumn(name = "cliente_id")
    @JsonBackReference(value="cliente-pedido")
    private Cliente cliente;
    private double posX; // Coordenada X del cliente
    private double posY; // Coordenada Y del cliente
    //horas limite
    private double horasLimite; // Hora límite para la entrega (en horas)
    //m3
    //fecha creacion
    // private LocalDateTime fechaCreacion; // Fecha de creación del pedido
    //fecha entrega
    private LocalDateTime fechaRegistro; // Fecha de registro del pedido
    
    private LocalDateTime fechaEntregaRequerida;
    private LocalDateTime fechaEntregaReal;
    
    private double volumenGLPAsignado; // Volumen total requerido (m3)
    private double volumenGLPEntregado; // Volumen ya entregado (m3)
    private double volumenGLPPendiente; // Volumen restante por asignar (m3) 
    private int prioridad; // 1: alta, 2: media, 3: baja
    
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "estado")
    private EstadoPedido estado;
    
 

    private String fechaHora; //formato "ddmmyyyy hh:mm:ss"
    private String fechaAsignaciones; //formato "ddmmyyyy hh:mm:ss" 
     
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
    @JsonManagedReference(value="pedido-entregaparcial")
    private List<EntregaParcial> entregasParciales = new ArrayList<>();
    
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
    @JsonManagedReference(value="pedido-nodo")
    private List<NodoRuta> nodos = new ArrayList<>();
    
    @ManyToOne
    @JoinColumn(name = "camion_id")
    @JsonBackReference(value="camion-pedido")
    private Camion camion;
    //Asignaciones
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
    @JsonManagedReference(value="pedido-asignacion")
    private List<AsignacionCamion> asignaciones = new ArrayList<>();

 

    /**
     * Asigna una parte del pedido a un camión
     * @param camion Camión al que se asigna
     * @param volumen Volumen asignado en m3
     * @return true si se pudo asignar, false si no hay volumen pendiente suficiente
     */
    public boolean asignarACamion(Camion camion, double volumen) {
        // Validar que haya volumen pendiente y que el camión tenga capacidad
        if (volumenGLPAsignado - volumenGLPEntregado < volumen || !camion.tieneCapacidadPara(volumen)) {
            return false;
        }
        
        // Calcular el porcentaje que representa del total
        double porcentaje = (volumen / volumenGLPAsignado) * 100;
        
        // Crear y agregar la asignación
        AsignacionCamion asignacion = new AsignacionCamion(camion, volumen, porcentaje);
        asignacion.setPedido(this);
        
        // Actualizar los volúmenes
        volumenGLPEntregado += volumen;
        
        // Asignar el volumen al camión
        camion.asignarPedidoParcial(this, volumen, porcentaje);
        
        // Actualizar estado del pedido
        actualizarEstadoDePedido();
        
        return true;
    }
    
    /**
     * Registra la entrega de una parte del pedido por un camión
     */
    public boolean registrarEntregaParcial(String codigoCamion, double volumenEntregado, LocalDateTime fechaEntrega) {
        if (volumenEntregado > volumenGLPAsignado) {
            return false; // No puede entregar más de lo solicitado
        }
        
        // Actualizar volúmenes
        this.volumenGLPEntregado += volumenEntregado;
        
        // Liberar capacidad del camión
        if (camion != null) {
            camion.liberarCapacidad(volumenEntregado);
        }
        
        // Actualizar estado del pedido
        actualizarEstadoDePedido();
        
        // Si el pedido está completamente entregado, actualizar la fecha de entrega
        if (estado == EstadoPedido.ENTREGADO_TOTALMENTE) {
            this.fechaEntregaReal = fechaEntrega;
        }
        
        return true;
    }
    
    /**
     * Actualiza el estado del pedido según las entregas
     */
    private void actualizarEstadoDePedido() {
        if (volumenGLPEntregado == 0) {
            estado = EstadoPedido.REGISTRADO;
        } else if (volumenGLPEntregado < volumenGLPAsignado) {
            // Si está parcialmente entregado
            if (volumenGLPEntregado > 0) {
                estado = EstadoPedido.ENTREGADO_PARCIALMENTE;
            } else {
                estado = EstadoPedido.PENDIENTE_PLANIFICACION;
            }
        } else if (volumenGLPEntregado == volumenGLPAsignado) {
            estado = EstadoPedido.ENTREGADO_TOTALMENTE;
        }
        
        // Actualizar el estadoInt para mantener compatibilidad
         
    }
    
    /**
     * Cancela el pedido, liberando capacidad del camión asignado
     */
    public void cancelar() {
        if (camion != null) {
            camion.liberarCapacidad(volumenGLPAsignado - volumenGLPEntregado);
        }
        
        estado = EstadoPedido.NO_ENTREGADO_EN_TIEMPO;
        
    }
    
    /**
     * Obtiene el porcentaje total entregado del pedido
     */
    public double getPorcentajeEntregado() {
        return (volumenGLPEntregado / volumenGLPAsignado) * 100;
    }
    
    /**
     * Verifica si el pedido está completamente entregado
     */
    public boolean isCompletamenteEntregado() {
        return Math.abs(volumenGLPEntregado - volumenGLPAsignado) < 0.01; // Comparación con tolerancia
    }
    
    /**
     * Método de utilidad para obtener la descripción del estado
     */
    @Transient
    public String getEstadoTexto() {
        return estado != null ? estado.getDescripcion() : "Desconocido";
    }
    
    /**
     * Método de utilidad para obtener el color asociado al estado
     */
    @Transient
    public String getEstadoColorHex() {
        return estado != null ? estado.getColorHex() : "#CCCCCC"; // Color por defecto
    }
}
```

## main\java\com\plg\entity\Ruta.java

```java
package com.plg.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa una ruta en el sistema.
 * Una ruta consiste en una secuencia ordenada de nodos que forman un camino
 * desde un origen hasta un destino, pasando por puntos intermedios.
 * 
 * La ruta permite gestionar entregas parciales de GLP a cada cliente,
 * permitiendo que un pedido pueda ser atendido por diferentes camiones
 * con porcentajes variables de la cantidad total solicitada.
 */
@Entity
@Table(name = "rutas")
@Getter
@Setter
@NoArgsConstructor
public class Ruta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", unique = true)
    private String codigo;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @ManyToOne
    @JoinColumn(name = "camion_id")
    @JsonBackReference(value="camion-ruta")
    private Camion camion;

    @Column(name = "estado")
    private int estado; // 0: Planificada, 1: En curso, 2: Completada, 3: Cancelada

    @Column(name = "distancia_total")
    private double distanciaTotal;
    
    @Column(name = "tiempo_estimado")
    private double tiempoEstimadoMinutos;
    
    @Column(name = "considera_bloqueos")
    private boolean consideraBloqueos;
    
    @Column(name = "volumen_total_glp")
    private double volumenTotalGLP;
    
    @Column(name = "capacidad_utilizada_porcentaje")
    private double capacidadUtilizadaPorcentaje;
    
    @Column(name = "fecha_inicio_ruta")
    private LocalDateTime fechaInicioRuta;
    
    @Column(name = "fecha_fin_ruta")
    private LocalDateTime fechaFinRuta;
    
    @OneToMany(mappedBy = "ruta", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "orden")
    @JsonManagedReference(value="ruta-nodo")
    private List<NodoRuta> nodos = new ArrayList<>();
    
    @OneToMany(mappedBy = "ruta", cascade = CascadeType.ALL)
    @JsonManagedReference(value="ruta-asignacion")
    private List<AsignacionCamion> asignaciones = new ArrayList<>();
    
    // Para almacenar IDs de bloqueos que afectan a esta ruta
    @Column(name = "bloqueos_ids", length = 255)
    private String bloqueosIds;
    
    /**
     * Constructor con código
     */
    public Ruta(String codigo) {
        this.codigo = codigo;
        this.fechaCreacion = LocalDateTime.now();
        this.estado = 0; // Planificada por defecto
        this.volumenTotalGLP = 0.0;
        this.capacidadUtilizadaPorcentaje = 0.0;
        this.consideraBloqueos = true; // Por defecto consideramos bloqueos
    }
    
    /**
     * Añade un nodo a la ruta
     */
    public void agregarNodo(double posX, double posY, String tipo) {
        NodoRuta nodo = new NodoRuta(posX, posY, tipo);
        nodo.setRuta(this);
        nodo.setOrden(nodos.size());
        nodos.add(nodo);
    }
    
    /**
     * Añade un nodo a la ruta con un pedido asociado y una entrega parcial
     */
    public void agregarNodoCliente(double posX, double posY, Pedido pedido, double volumenGLP, double porcentajePedido) {
        NodoRuta nodo = new NodoRuta(posX, posY, "CLIENTE");
        nodo.setRuta(this);
        nodo.setOrden(nodos.size());
        nodo.setPedido(pedido);
        nodo.setVolumenGLP(volumenGLP);
        nodo.setPorcentajePedido(porcentajePedido);
        nodos.add(nodo);
        
        // Actualizar el volumen total de GLP de la ruta
        this.volumenTotalGLP += volumenGLP;
        
        // Actualizar porcentaje de capacidad utilizada del camión
        actualizarCapacidadUtilizada();
    }
    
    /**
     * Método sobrecargado para mantener compatibilidad con código existente
     */
    public void agregarNodoCliente(int posX, int posY, Pedido pedido) {
        // Si no especificamos volumen ni porcentaje, asumimos que se entrega el pedido completo (100%)
        agregarNodoCliente(posX, posY, pedido, pedido.getVolumenGLPAsignado(), 100.0);
    }
    
    /**
     * Actualiza la información de entrega de un nodo específico
     */
    public boolean actualizarEntregaPedido(Long pedidoId, double nuevoVolumenGLP, double nuevoPorcentaje) {
        for (NodoRuta nodo : nodos) {
            if (nodo.getPedido() != null && nodo.getPedido().getId().equals(pedidoId)) {
                // Restar el volumen antiguo del total
                this.volumenTotalGLP -= nodo.getVolumenGLP();
                
                // Actualizar nodo
                nodo.setVolumenGLP(nuevoVolumenGLP);
                nodo.setPorcentajePedido(nuevoPorcentaje);
                
                // Actualizar el total
                this.volumenTotalGLP += nuevoVolumenGLP;
                
                // Recalcular el porcentaje de capacidad utilizada
                actualizarCapacidadUtilizada();
                
                return true;
            }
        }
        return false;
    }
    
    /**
     * Registra la entrega de un pedido
     */
    public boolean marcarPedidoComoEntregado(Long pedidoId, LocalDateTime fechaEntrega, String observaciones) {
        for (NodoRuta nodo : nodos) {
            if (nodo.getPedido() != null && nodo.getPedido().getId().equals(pedidoId)) {
                nodo.setEntregado(true);
                nodo.setTiempoLlegadaReal(fechaEntrega);
                nodo.setObservaciones(observaciones);
                
                // Si el camión está presente, liberamos su capacidad
                if (camion != null) {
                    camion.liberarCapacidad(nodo.getVolumenGLP());
                    // Actualizar EntregaParcial relacionada
                    camion.completarEntregaParcial(pedidoId);
                }
                
                return true;
            }
        }
        return false;
    }
    
    /**
     * Actualiza el porcentaje de capacidad utilizada del camión
     */
    private void actualizarCapacidadUtilizada() {
        if (camion != null && camion.getCapacidad() > 0) {
            this.capacidadUtilizadaPorcentaje = (this.volumenTotalGLP / camion.getCapacidad()) * 100;
        }
    }
    
    /**
     * Verifica si el camión tiene capacidad suficiente para la cantidad de GLP asignada
     */
    public boolean verificarCapacidadSuficiente() {
        if (camion == null) return false;
        return camion.getCapacidad() >= volumenTotalGLP;
    }
    
    /**
     * Obtiene las entregas pendientes de esta ruta
     */
    @Transient
    public List<NodoRuta> getEntregasPendientes() {
        return nodos.stream()
            .filter(n -> n.getPedido() != null && !n.isEntregado())
            .collect(Collectors.toList());
    }
    
    /**
     * Obtiene información agrupada por pedido, incluyendo las entregas parciales
     */
    @Transient
    public List<Map<String, Object>> getInfoEntregasPorPedido() {
        Map<Long, Map<String, Object>> infoPorPedido = new HashMap<>();
        
        for (NodoRuta nodo : nodos) {
            if (nodo.getPedido() != null) {
                Long pedidoId = nodo.getPedido().getId();
                
                // Si no existe entrada para este pedido, crear una nueva
                if (!infoPorPedido.containsKey(pedidoId)) {
                    Map<String, Object> info = new HashMap<>();
                    info.put("pedidoId", pedidoId);
                    info.put("codigoPedido", nodo.getPedido().getCodigo());
                    info.put("volumenTotalPedido", nodo.getPedido().getVolumenGLPAsignado());
                    info.put("volumenAsignado", 0.0);
                    info.put("porcentajeAsignado", 0.0);
                    info.put("entregas", new ArrayList<Map<String, Object>>());
                    
                    infoPorPedido.put(pedidoId, info);
                }
                
                // Actualizar información del pedido
                Map<String, Object> info = infoPorPedido.get(pedidoId);
                double volumenAsignado = (double) info.get("volumenAsignado") + nodo.getVolumenGLP();
                double porcentajeAsignado = (double) info.get("porcentajeAsignado") + nodo.getPorcentajePedido();
                
                info.put("volumenAsignado", volumenAsignado);
                info.put("porcentajeAsignado", porcentajeAsignado);
                
                // Agregar información de esta entrega específica
                Map<String, Object> infoEntrega = new HashMap<>();
                infoEntrega.put("nodoId", nodo.getId());
                infoEntrega.put("posX", nodo.getPosX());
                infoEntrega.put("posY", nodo.getPosY());
                infoEntrega.put("volumenGLP", nodo.getVolumenGLP());
                infoEntrega.put("porcentaje", nodo.getPorcentajePedido());
                infoEntrega.put("entregado", nodo.isEntregado());
                
                ((List<Map<String, Object>>) info.get("entregas")).add(infoEntrega);
            }
        }
        
        return new ArrayList<>(infoPorPedido.values());
    }
    
    /**
     * Obtiene la ruta como array bidimensional para algoritmos de navegación
     */
    @Transient
    public double[][] obtenerRutaComoArray() {
        double[][] rutaArray = new double[nodos.size()][2];
        for (int i = 0; i < nodos.size(); i++) {
            NodoRuta nodo = nodos.get(i);
            rutaArray[i][0] = nodo.getPosX();
            rutaArray[i][1] = nodo.getPosY();
        }
        return rutaArray;
    }
    
    /**
     * Calcula la distancia total de la ruta basada en distancia Manhattan
     * (adecuada para un mapa reticular)
     */
    public void calcularDistanciaTotal() {
        if (nodos.size() < 2) {
            this.distanciaTotal = 0;
            return;
        }
        
        double distancia = 0;
        for (int i = 0; i < nodos.size() - 1; i++) {
            NodoRuta nodoActual = nodos.get(i);
            NodoRuta nodoSiguiente = nodos.get(i + 1);
            
            // Distancia Manhattan: |x2-x1| + |y2-y1|
            distancia += Math.abs(nodoSiguiente.getPosX() - nodoActual.getPosX()) +
                         Math.abs(nodoSiguiente.getPosY() - nodoActual.getPosY());
        }
        
        this.distanciaTotal = distancia;
    }
    
    /**
     * Estima los tiempos de llegada para cada nodo
     * @param velocidadKmPorHora Velocidad promedio del vehículo en km/h
     * @param tiempoInicio Tiempo de inicio del recorrido
     */
    public void estimarTiemposLlegada(double velocidadKmPorHora, LocalDateTime tiempoInicio) {
        if (nodos.isEmpty()) return;
        
        double velocidadKmPorMinuto = velocidadKmPorHora / 60.0;
        LocalDateTime tiempoActual = tiempoInicio;
        
        // Para el primer nodo (origen), el tiempo estimado es el tiempo de inicio
        nodos.get(0).setTiempoLlegadaEstimado(tiempoActual);
        
        // Para los nodos siguientes, calcular en base a la distancia
        for (int i = 0; i < nodos.size() - 1; i++) {
            NodoRuta nodoActual = nodos.get(i);
            NodoRuta nodoSiguiente = nodos.get(i + 1);
            
            // Calcular distancia Manhattan entre los nodos
            double distanciaTramo = Math.abs(nodoSiguiente.getPosX() - nodoActual.getPosX()) +
                                    Math.abs(nodoSiguiente.getPosY() - nodoActual.getPosY());
            
            // Calcular tiempo en minutos para recorrer el tramo
            double tiempoTramoMinutos = distanciaTramo / velocidadKmPorMinuto;
            
            // Actualizar tiempo actual
            tiempoActual = tiempoActual.plusMinutes((long)tiempoTramoMinutos);
            
            // Establecer tiempo estimado de llegada al nodo siguiente
            nodoSiguiente.setTiempoLlegadaEstimado(tiempoActual);
        }
        
        // Actualizar tiempo estimado total en minutos
        if (nodos.size() > 1) {
            LocalDateTime tiempoFinal = nodos.get(nodos.size() - 1).getTiempoLlegadaEstimado();
            long minutosTotal = java.time.Duration.between(tiempoInicio, tiempoFinal).toMinutes();
            this.tiempoEstimadoMinutos = (int) minutosTotal;
        }
    }
    
    /**
     * Registra el tiempo real de llegada a un nodo
     */
    public void registrarLlegadaReal(int indiceNodo, LocalDateTime tiempoLlegada) {
        if (indiceNodo >= 0 && indiceNodo < nodos.size()) {
            nodos.get(indiceNodo).setTiempoLlegadaReal(tiempoLlegada);
        }
    }
    
    /**
     * Verifica si la ruta pasa por un punto específico
     */
    public boolean pasaPorPunto(int x, int y) {
        for (NodoRuta nodo : nodos) {
            if (nodo.getPosX() == x && nodo.getPosY() == y) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Obtiene los pedidos asociados a esta ruta
     */
    @Transient
    public List<Pedido> getPedidosAsociados() {
        return nodos.stream()
            .filter(n -> n.getPedido() != null)
            .map(NodoRuta::getPedido)
            .distinct()
            .collect(Collectors.toList());
    }
    
    /**
     * Verifica si un tramo de la ruta cruza un bloqueo
     */
    public boolean verificarInterseccionConBloqueos(List<Bloqueo> bloqueos) {
        if (nodos.size() < 2 || bloqueos == null || bloqueos.isEmpty()) {
            return false;
        }
        
        // Lista para almacenar los IDs de los bloqueos que afectan la ruta
        List<Long> idsBloqueos = new ArrayList<>();
        
        for (int i = 0; i < nodos.size() - 1; i++) {
            NodoRuta nodoActual = nodos.get(i);
            NodoRuta nodoSiguiente = nodos.get(i + 1);
            
            for (Bloqueo bloqueo : bloqueos) {
                if (bloqueo.isActivo() && intersectaBloqueo(
                        nodoActual.getPosX(), nodoActual.getPosY(),
                        nodoSiguiente.getPosX(), nodoSiguiente.getPosY(), bloqueo)) {
                    idsBloqueos.add(bloqueo.getId());
                }
            }
        }
        
        // Si encontramos bloqueos, almacenar sus IDs
        if (!idsBloqueos.isEmpty()) {
            this.bloqueosIds = idsBloqueos.stream()
                    .distinct()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
            return true;
        }
        
        return false;
    }
     
    /**
     * Verifica si un segmento de ruta intersecta con un bloqueo
     */
    private boolean intersectaBloqueo(double x1, double y1, double x2, double y2, Bloqueo bloqueo) {
        try {
            // Primero intentamos usar el método específico del bloqueo
            return bloqueo.intersectaConSegmento(x1, y1, x2, y2);
        } catch (Exception e) {
            // Implementación alternativa si hay error
            // Calculamos los puntos intermedios del segmento
            List<double[]> puntosIntermedios = generarPuntosIntermedios(x1, y1, x2, y2);
            
            // Verificamos si algún punto intermedio está en el bloqueo
            for (double[] punto : puntosIntermedios) {
                if (bloqueo.contienePunto(punto[0], punto[1])) {
                    return true;
                }
            }
            
            return false;
        }
    }
    
    /**
     * Genera una lista de puntos intermedios entre dos puntos (incluidos los extremos)
     */
    private List<double[]> generarPuntosIntermedios(double x1, double y1, double x2, double y2) {
        List<double[]> puntos = new ArrayList<>();
        
        // Añadir el punto inicial
        puntos.add(new double[]{x1, y1});
        
        // Si los puntos son iguales, no hay más que añadir
        if (x1 == x2 && y1 == y2) {
            return puntos;
        }
        
        // Calcular dirección del movimiento
        double dx = Double.compare(x2, x1);
        double dy = Double.compare(y2, y1);
        
        // Generar puntos intermedios
        double x = x1;
        double y = y1;
        
        while (x != x2 || y != y2) {
            // Si aún no hemos llegado al destino en X, movernos
            if (x != x2) {
                x += dx;
            }
            
            // Si aún no hemos llegado al destino en Y, movernos
            if (y != y2) {
                y += dy;
            }
            
            // Añadir el punto generado
            puntos.add(new double[]{x, y});
        }
        
        return puntos;
    }
    
    /**
     * Obtiene la lista de IDs de bloqueos que afectan a esta ruta
     */
    @Transient
    public List<Long> getBloqueoIdsComoLista() {
        if (bloqueosIds == null || bloqueosIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        return java.util.Arrays.stream(bloqueosIds.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }
    
    /**
     * Convierte la ruta a una representación para APIs REST
     */
    @Transient
    public List<Map<String, Object>> convertirARutaAPI() {
        List<Map<String, Object>> rutaAPI = new ArrayList<>();
        
        for (NodoRuta nodo : nodos) {
            Map<String, Object> punto = new HashMap<>();
            punto.put("x", nodo.getPosX());
            punto.put("y", nodo.getPosY());
            punto.put("tipo", nodo.getTipo());
            
            if (nodo.getPedido() != null) {
                punto.put("pedidoId", nodo.getPedido().getId());
                punto.put("volumenGLP", nodo.getVolumenGLP());
                punto.put("porcentajePedido", nodo.getPorcentajePedido());
                punto.put("entregado", nodo.isEntregado());
            }
            
            if (nodo.getTiempoLlegadaEstimado() != null) {
                punto.put("tiempoEstimado", nodo.getTiempoLlegadaEstimado().toString());
            }
            
            if (nodo.getTiempoLlegadaReal() != null) {
                punto.put("tiempoReal", nodo.getTiempoLlegadaReal().toString());
            }
            
            if (nodo.getObservaciones() != null && !nodo.getObservaciones().isEmpty()) {
                punto.put("observaciones", nodo.getObservaciones());
            }
            
            rutaAPI.add(punto);
        }
        
        return rutaAPI;
    }
    
    /**
     * Iniciar la ruta, cambiando su estado y registrando la fecha de inicio
     */
    public void iniciarRuta() {
        this.estado = 1; // En curso
        this.fechaInicioRuta = LocalDateTime.now();
        
        // Si hay camión asignado, actualizar su estado
        if (this.camion != null) {
            this.camion.setEstado(EstadoCamion.EN_RUTA); // Usar el enum
            this.camion.actualizarEstadoEntregasARuta();
        }
    }
    
    /**
     * Completar la ruta, cambiando su estado y registrando la fecha de fin
     */
    public void completarRuta() {
        this.estado = 2; // Completada
        this.fechaFinRuta = LocalDateTime.now();
        
        // Si hay camión asignado, actualizar su estado
        if (this.camion != null) {
            this.camion.setEstado(EstadoCamion.DISPONIBLE); // Usar el enum
        }
    }
    
    /**
     * Cancelar la ruta
     */
    public void cancelarRuta(String motivo) {
        this.estado = 3; // Cancelada
        
        // Si hay camión asignado, actualizar su estado
        if (this.camion != null) {
            this.camion.setEstado(EstadoCamion.DISPONIBLE); // Usar el enum
        }
    }
    
    /**
     * Obtiene el estado de la ruta como texto
     */
    @Transient
    public String getEstadoTexto() {
        switch (this.estado) {
            case 0: return "Planificada";
            case 1: return "En curso";
            case 2: return "Completada";
            case 3: return "Cancelada";
            default: return "Desconocido";
        }
    }
    
    /**
     * Actualiza el camión asignado a la ruta
     */
    public void setCamion(Camion camion) {
        this.camion = camion;
        actualizarCapacidadUtilizada();
    }
    
    /**
     * Genera información resumida de la ruta para APIs
     */
    @Transient
    public Map<String, Object> getResumenRuta() {
        Map<String, Object> resumen = new HashMap<>();
        resumen.put("id", this.id);
        resumen.put("codigo", this.codigo);
        resumen.put("fechaCreacion", this.fechaCreacion);
        resumen.put("estado", this.estado);
        resumen.put("estadoTexto", this.getEstadoTexto());
        resumen.put("distanciaTotal", this.distanciaTotal);
        resumen.put("tiempoEstimadoMinutos", this.tiempoEstimadoMinutos);
        resumen.put("volumenTotalGLP", this.volumenTotalGLP);
        
        if (this.camion != null) {
            resumen.put("camionCodigo", this.camion.getCodigo());
            resumen.put("capacidadUtilizadaPorcentaje", this.capacidadUtilizadaPorcentaje);
        }
        
        resumen.put("cantidadNodos", this.nodos.size());
        resumen.put("cantidadEntregas", this.getEntregasPendientes().size());
        
        if (this.fechaInicioRuta != null) {
            resumen.put("fechaInicioRuta", this.fechaInicioRuta.toString());
        }
        
        if (this.fechaFinRuta != null) {
            resumen.put("fechaFinRuta", this.fechaFinRuta.toString());
        }
        
        if (this.bloqueosIds != null && !this.bloqueosIds.isEmpty()) {
            resumen.put("tieneBloqueos", true);
            resumen.put("bloqueosIds", this.getBloqueoIdsComoLista());
        } else {
            resumen.put("tieneBloqueos", false);
        }
        
        return resumen;
    }
    
    /**
     * Verifica si la ruta tiene bloqueos activos para la hora actual
     */
    public boolean tieneBloqueoActivo(List<Bloqueo> bloqueos) {
        LocalDateTime ahora = LocalDateTime.now();
        
        // Obtener la lista de IDs de bloqueos que afectan a esta ruta
        List<Long> idsBloqueos = getBloqueoIdsComoLista();
        
        if (idsBloqueos.isEmpty() || bloqueos == null) {
            return false;
        }
        
        // Filtrar los bloqueos por ID y verificar si están activos en este momento
        return bloqueos.stream()
            .filter(b -> idsBloqueos.contains(b.getId()))
            .anyMatch(b -> b.isActivo() && 
                    ahora.isAfter(b.getFechaInicio()) && 
                    ahora.isBefore(b.getFechaFin()));
    }
}
```

## main\java\com\plg\repository\AlmacenRepository.java

```java
package com.plg.repository;

import com.plg.entity.Almacen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlmacenRepository extends JpaRepository<Almacen, Long> {
    List<Almacen> findByActivo(boolean activo);
    List<Almacen> findByEsCentralAndActivo(boolean esCentral, boolean activo);
    //busca el almacen central activo
    Almacen findByEsCentralAndActivoTrue(boolean esCentral);//Esto devuleve uno solo, el primero que encuentra
    List<Almacen> findByPosXAndPosY(double posX, double posY);
    // Añadido para buscar almacenes por atributo esCentral
    List<Almacen> findByEsCentral(boolean esCentral);
 
 
}
```

## main\java\com\plg\repository\AveriaRepository.java

```java
package com.plg.repository;

import com.plg.entity.Averia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AveriaRepository extends JpaRepository<Averia, Long> {
    List<Averia> findByCamion_Codigo(String codigoCamion);
    List<Averia> findByEstado(int estado); 
    List<Averia> findByFechaHoraReporteBetween(LocalDateTime inicio, LocalDateTime fin);
    
    // Nuevos métodos de consulta
    List<Averia> findByTipoIncidente(String tipoIncidente);
    List<Averia> findByTurno(String turno);
    List<Averia> findByRequiereTraslado(boolean requiereTraslado);
    List<Averia> findByEsValida(boolean esValida);
    List<Averia> findByConCarga(boolean conCarga);
    
    // Combinaciones útiles
    List<Averia> findByEstadoAndTipoIncidente(int estado, String tipoIncidente);
    List<Averia> findByTurnoAndTipoIncidente(String turno, String tipoIncidente);
    
    // Consultas para verificar inoperatividad
    @Query("SELECT a FROM Averia a WHERE a.tiempoFinInoperatividad > :fecha")
    List<Averia> findCamionesInoperativos(@Param("fecha") LocalDateTime fecha);
    
    @Query("SELECT a FROM Averia a WHERE a.camion.codigo = :codigoCamion AND a.tiempoFinInoperatividad > :fecha")
    List<Averia> verificarInoperatividadCamion(@Param("codigoCamion") String codigoCamion, @Param("fecha") LocalDateTime fecha);
    
    // Consultas por ubicación
    List<Averia> findByPosXAndPosY(int posX, int posY);
    
    // Consulta para encontrar averías en un rango de kilómetros
    @Query("SELECT a FROM Averia a WHERE a.kilometroOcurrencia BETWEEN :kmInicio AND :kmFin")
    List<Averia> findByRangoKilometro(@Param("kmInicio") double kmInicio, @Param("kmFin") double kmFin);
}
```

## main\java\com\plg\repository\BloqueoRepository.java

```java
package com.plg.repository;

import com.plg.entity.Bloqueo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BloqueoRepository extends JpaRepository<Bloqueo, Long> {
    // Métodos existentes
    List<Bloqueo> findByActivo(boolean activo);
    List<Bloqueo> findByFechaInicioBetween(LocalDate inicio, LocalDate fin);
    List<Bloqueo> findByFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(LocalDate fecha, LocalDate mismaFecha);
    List<Bloqueo> findByFechaInicio(LocalDate fecha);
    List<Bloqueo> findByFechaFin(LocalDate fecha);
    
    // Nuevos métodos para LocalDateTime
    List<Bloqueo> findByActivoTrue();
    List<Bloqueo> findByFechaInicioBeforeAndFechaFinAfter(LocalDateTime momento, LocalDateTime mismoMomento);
    
    // Método para encontrar bloqueos activos en una fecha específica
    default List<Bloqueo> findActivosEnFecha(LocalDate fecha) {
        LocalDateTime inicioDia = fecha.atStartOfDay();
        LocalDateTime finDia = fecha.atTime(23, 59, 59);
        return findByFechaInicioBeforeAndFechaFinAfter(finDia, inicioDia);
    }
}
```

## main\java\com\plg\repository\CamionRepository.java

```java
package com.plg.repository;

import com.plg.entity.Camion;
import com.plg.entity.EstadoCamion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CamionRepository extends JpaRepository<Camion, Long> {
    List<Camion> findByEstado(EstadoCamion estado);
    List<Camion> findByTipo(String tipo);
    long countByEstado(EstadoCamion estado);
    List<Camion> findByTipoAndEstado(String tipo, EstadoCamion estado);
 
    //FindfindByEstadoNot
    List<Camion> findByEstadoNot(EstadoCamion estado);
    //FindByCodigo es como id  
    //Usa 
    Optional<Camion> findByCodigo(String codigo); // Esto es para buscar por id, pero el id es un  String

}
```

## main\java\com\plg\repository\ClienteRepository.java

```java
package com.plg.repository;

import com.plg.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, String> {
    // Custom query methods can be added here if needed
}
```

## main\java\com\plg\repository\EntregaParcialRepository.java

```java
package com.plg.repository;

import com.plg.entity.Camion;
import com.plg.entity.EntregaParcial;
import com.plg.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones CRUD de entidades EntregaParcial.
 * Permite gestionar la relación entre camiones, pedidos y entregas parciales.
 */
@Repository
public interface EntregaParcialRepository extends JpaRepository<EntregaParcial, Long> {
    
    /**
     * Encuentra todas las entregas parciales asociadas a un camión
     */
    List<EntregaParcial> findByCamion(Camion camion);
    
    /**
     * Encuentra todas las entregas parciales asociadas a un pedido
     */
    List<EntregaParcial> findByPedido(Pedido pedido);
    
    /**
     * Encuentra todas las entregas parciales asociadas a un pedido por su ID
     */
    List<EntregaParcial> findByPedidoId(Long pedidoId);
    
    /**
     * Encuentra todas las entregas parciales asociadas a un camión por su código
     */
    List<EntregaParcial> findByCamionCodigo(String codigoCamion);
    
    /**
     * Encuentra todas las entregas parciales con un estado específico
     */
    List<EntregaParcial> findByEstado(int estado);
    
    /**
     * Encuentra todas las entregas parciales con un estado específico asociadas a un camión
     */
    List<EntregaParcial> findByCamionAndEstado(Camion camion, int estado);
    
    /**
     * Encuentra todas las entregas parciales con un estado específico asociadas a un pedido
     */
    List<EntregaParcial> findByPedidoAndEstado(Pedido pedido, int estado);
    
    /**
     * Encuentra una entrega parcial específica por camión y pedido
     */
    Optional<EntregaParcial> findByCamionAndPedido(Camion camion, Pedido pedido);
    
    /**
     * Encuentra una entrega parcial específica por camión y pedido donde la entrega no ha sido completada
     */
    Optional<EntregaParcial> findByCamionAndPedidoAndEstadoNot(Camion camion, Pedido pedido, int estadoNoDeseado);
    
    /**
     * Encuentra entregas parciales creadas en un rango de fechas
     */
    List<EntregaParcial> findByFechaAsignacionBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    /**
     * Encuentra entregas parciales entregadas en un rango de fechas
     */
    List<EntregaParcial> findByFechaEntregaBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    /**
     * Calcula el volumen total de GLP entregado por un camión en todas sus entregas completadas
     */
    @Query("SELECT SUM(e.volumenGLP) FROM EntregaParcial e WHERE e.camion = :camion AND e.estado = 2")
    Double calcularVolumenTotalEntregadoPorCamion(@Param("camion") Camion camion);
    
    /**
     * Calcula el volumen total de GLP entregado a un pedido en todas sus entregas parciales completadas
     */
    @Query("SELECT SUM(e.volumenGLP) FROM EntregaParcial e WHERE e.pedido = :pedido AND e.estado = 2")
    Double calcularVolumenTotalEntregadoAPedido(@Param("pedido") Pedido pedido);
    
    /**
     * Calcula el porcentaje total completado de un pedido sumando todas sus entregas parciales
     */
    @Query("SELECT SUM(e.porcentajePedido) FROM EntregaParcial e WHERE e.pedido = :pedido AND e.estado = 2")
    Double calcularPorcentajeCompletadoDePedido(@Param("pedido") Pedido pedido);
    
    /**
     * Encuentra entregas pendientes (asignadas o en ruta) que deberían haberse entregado ya
     * (fecha de asignación anterior a la fecha actual menos un margen de tiempo)
     */
    @Query("SELECT e FROM EntregaParcial e WHERE e.estado IN (0, 1) AND e.fechaAsignacion < :fechaLimite")
    List<EntregaParcial> findEntregasRetrasadas(@Param("fechaLimite") LocalDateTime fechaLimite);
    
    /**
     * Encuentra entregas parciales para un conjunto de IDs de pedidos
     */
    @Query("SELECT e FROM EntregaParcial e WHERE e.pedido.id IN :pedidoIds")
    List<EntregaParcial> findByPedidoIds(@Param("pedidoIds") List<Long> pedidoIds);
    
    /**
     * Actualiza el estado de todas las entregas parciales de un camión
     */
    @Query("UPDATE EntregaParcial e SET e.estado = :nuevoEstado WHERE e.camion = :camion AND e.estado = :estadoActual")
    int actualizarEstadoEntregasPorCamion(
            @Param("camion") Camion camion, 
            @Param("estadoActual") int estadoActual, 
            @Param("nuevoEstado") int nuevoEstado);
    
    /**
     * Cuenta las entregas parciales por estado para un camión específico
     */
    @Query("SELECT e.estado, COUNT(e) FROM EntregaParcial e WHERE e.camion = :camion GROUP BY e.estado")
    List<Object[]> contarEntregasPorEstadoYCamion(@Param("camion") Camion camion);
}
```

## main\java\com\plg\repository\MantenimientoRepository.java

```java
package com.plg.repository;

import com.plg.entity.Mantenimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface MantenimientoRepository extends JpaRepository<Mantenimiento, Long> {
    List<Mantenimiento> findByCamion_Codigo(String codigoCamion);
    List<Mantenimiento> findByEstado(int estado);
    List<Mantenimiento> findByFechaInicioBetween(LocalDate fechaInicio, LocalDate fechaFin);
    List<Mantenimiento> findByFechaInicio(LocalDate fecha);
List<Mantenimiento> findByFechaFin(LocalDate fecha);
}
```

## main\java\com\plg\repository\PedidoRepository.java

```java
package com.plg.repository;

import com.plg.entity.Pedido;
import com.plg.entity.EstadoPedido;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByEstado(EstadoPedido estado);
    List<Pedido> findByCamion_Codigo(String codigoCamion);
    List<Pedido> findByCamion_CodigoAndEstado(String codigoCamion, EstadoPedido estado);
    List<Pedido> findByEstadoAndFechaEntregaRequeridaNotNull(EstadoPedido estado);
    long countByEstado(EstadoPedido estado);
    List<Pedido> findByCliente_Id(String clienteId);
    List<Pedido> findByCliente_IdAndEstado(String clienteId, EstadoPedido estado);
    List<Pedido> findByCamion_CodigoAndEstadoAndFechaEntregaRequeridaNotNull(String codigoCamion, EstadoPedido estado);
    List<Pedido> findByCamion_CodigoAndEstadoAndFechaEntregaRequeridaNull(String codigoCamion, EstadoPedido estado);
    
    @Query("SELECT p FROM Pedido p JOIN p.asignaciones a WHERE a.ruta.codigo = :idRuta")
    List<Pedido> findByCodigoRuta(@Param("idRuta") String idRuta);
    List<Pedido> findByEstadoIn(List<EstadoPedido> estados);
}
```

## main\java\com\plg\repository\RutaRepository.java

```java
package com.plg.repository;

import com.plg.entity.Camion;
import com.plg.entity.Ruta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones de base de datos relacionadas con Rutas
 */
@Repository
public interface RutaRepository extends JpaRepository<Ruta, Long> {
    
    /**
     * Busca una ruta por su código único
     */
    Optional<Ruta> findByCodigo(String codigo);
    
    /**
     * Busca rutas por estado
     */
    List<Ruta> findByEstado(int estado);
    
    /**
     * Busca rutas con todos sus nodos inicializados por estado
     */
    @Query("SELECT DISTINCT r FROM Ruta r LEFT JOIN FETCH r.nodos WHERE r.estado = :estado")
    List<Ruta> findByEstadoWithNodos(@Param("estado") int estado);
    
    /**
     * Busca rutas asignadas a un camión específico
     */
    List<Ruta> findByCamionId(Long camionId);
    
    /**
     * Busca rutas en curso (estado = 1) para un camión específico
     */
    List<Ruta> findByCamionIdAndEstado(Long camionId, int estado);
    
    /**
     * Busca rutas en curso con sus nodos cargados para un camión específico
     */
    @Query("SELECT DISTINCT r FROM Ruta r LEFT JOIN FETCH r.nodos WHERE r.camion.id = :camionId AND r.estado = :estado")
    List<Ruta> findByCamionIdAndEstadoWithNodos(@Param("camionId") Long camionId, @Param("estado") int estado);
    
    /**
     * Busca rutas que incluyen un pedido específico
     */
    @Query("SELECT r FROM Ruta r JOIN r.nodos n WHERE n.pedido.id = :pedidoId")
    List<Ruta> findByPedidoId(@Param("pedidoId") Long pedidoId);
    
    /**
     * Busca rutas creadas en un rango de fechas
     */
    List<Ruta> findByFechaCreacionBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    /**
     * Busca rutas que pasan por un punto específico
     */
    @Query("SELECT DISTINCT r FROM Ruta r JOIN r.nodos n WHERE n.posX = :x AND n.posY = :y")
    List<Ruta> findByRutaQueIncluyePunto(@Param("x") int x, @Param("y") int y);
    
    /**
     * Cuenta cuántas rutas planificadas (estado = 0) hay actualmente
     */
    long countByEstado(int estado);

    /**
     * Busca rutas que tengan alguno de los estados especificados
     */
    List<Ruta> findByEstadoIn(List<Integer> estados);
    
    /**
     * Busca rutas con todos sus nodos inicializados que tengan alguno de los estados especificados
     */
    @Query("SELECT DISTINCT r FROM Ruta r LEFT JOIN FETCH r.nodos WHERE r.estado IN :estados")
    List<Ruta> findByEstadoInWithNodos(@Param("estados") List<Integer> estados);

    List<Ruta> findByCamion(Camion camion);
}
```

## main\java\com\plg\service\AgrupamientoAPService.java

```java
package com.plg.service;

import com.plg.dto.*;
import com.plg.entity.Pedido;
import com.plg.entity.EstadoPedido;
import com.plg.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AgrupamientoAPService {

    @Autowired
    private PedidoRepository pedidoRepository;
    
    // Parámetros para el algoritmo de Affinity Propagation
    private final double DAMPING = 0.9;
    private final int MAX_ITERATIONS = 200;
    private final double CONVERGENCE_THRESHOLD = 0.001;
    
    public AgrupamientoAPResultadoDTO generarGrupos(Map<String, Object> params) {
        // Obtener pedidos pendientes
        List<Pedido> pedidos = pedidoRepository.findByEstado(EstadoPedido.PENDIENTE_PLANIFICACION);
         
        // Verificar si hay suficientes pedidos para agrupar
        if (pedidos.isEmpty()) {
            return AgrupamientoAPResultadoDTO.builder()
                .metodo("affinityPropagation")
                .totalPedidos(0)
                .totalGrupos(0)
                .grupos(Collections.emptyList())
                .build();
        }
        
        // Parámetros opcionales
        int numeroClusterDeseado = params.containsKey("numeroClusters") ? 
                                  (int) params.get("numeroClusters") : 3;
        
        // Implementación simplificada - en un caso real aquí iría el algoritmo AP completo
        // que agruparía los pedidos basado en distancias y otras métricas
        
        // Generamos grupos simulados
        List<GrupoDTO> grupos = generarGruposSimulados(pedidos, numeroClusterDeseado);
        
        // Preparamos el resultado usando DTO
        return AgrupamientoAPResultadoDTO.builder()
            .grupos(grupos)
            .metodo("affinityPropagation")
            .totalPedidos(pedidos.size())
            .totalGrupos(grupos.size())
            .build();
    }
    
    // Método que simula la generación de grupos - en un caso real esto implementaría el AP completo
    private List<GrupoDTO> generarGruposSimulados(List<Pedido> pedidos, int numeroGruposDeseado) {
        List<GrupoDTO> grupos = new ArrayList<>();
        
        // Dividir los pedidos en grupos (clusters)
        // En un caso real, esto se haría con el algoritmo AP que encuentra automáticamente el número óptimo
        // Aquí simplemente dividimos equitativamente
        List<List<Pedido>> clustersSimulados = dividirEnGrupos(pedidos, numeroGruposDeseado);
        
        // Para cada cluster creamos un grupo
        for (int i = 0; i < clustersSimulados.size(); i++) {
            // Encontrar el pedido más central del grupo (ejemplar) - simulado
            Pedido ejemplar = encontrarEjemplar(clustersSimulados.get(i));
            
            // Calcular centroide del grupo
            Map<String, Double> centroide = calcularCentroide(clustersSimulados.get(i));
            
            // Convertir pedidos a DTOs
            List<PedidoDTO> pedidosDTO = clustersSimulados.get(i).stream()
                .map(this::convertirAPedidoDTO)
                .collect(Collectors.toList());
            
            // Calcular radio del grupo (distancia máxima desde el centroide)
            double radio = calcularRadioGrupo(clustersSimulados.get(i), centroide.get("x"), centroide.get("y"));
            
            // Calcular densidad (pedidos por área)
            double densidad = clustersSimulados.get(i).size() / (Math.PI * Math.pow(radio, 2));
            
            // Crear grupo como DTO
            GrupoDTO grupo = GrupoDTO.builder()
                .idGrupo("G" + (i + 1))
                .ejemplar(convertirAPedidoDTO(ejemplar))
                .centroideX(centroide.get("x"))
                .centroideY(centroide.get("y"))
                .pedidos(pedidosDTO)
                .numeroPedidos(pedidosDTO.size())
                .radio(radio)
                .densidad(densidad)
                .build();
            
            grupos.add(grupo);
        }
        
        return grupos;
    }
    
    // Método auxiliar para dividir pedidos en grupos (simulado)
    private List<List<Pedido>> dividirEnGrupos(List<Pedido> pedidos, int numeroGrupos) {
        List<List<Pedido>> grupos = new ArrayList<>();
        
        // Asegurar que no intentamos crear más grupos que pedidos disponibles
        numeroGrupos = Math.min(numeroGrupos, pedidos.size());
        
        // Crear grupos vacíos
        for (int i = 0; i < numeroGrupos; i++) {
            grupos.add(new ArrayList<>());
        }
        
        // Distribuir pedidos (enfoque simple espacial - en un caso real
        // se usaría AP o K-means para una mejor agrupación por proximidad)
        
        // Simulación de agrupamiento espacial
        // Ordenamos por coordenada X para simular cercanía
        pedidos.sort(Comparator.comparingDouble(Pedido::getPosX));
        
        // Distribuir pedidos aproximadamente igual en cada grupo
        int pedidosPorGrupo = pedidos.size() / numeroGrupos;
        int resto = pedidos.size() % numeroGrupos;
        
        int indice = 0;
        for (int i = 0; i < numeroGrupos; i++) {
            int tamañoGrupo = pedidosPorGrupo + (i < resto ? 1 : 0);
            for (int j = 0; j < tamañoGrupo && indice < pedidos.size(); j++) {
                grupos.get(i).add(pedidos.get(indice++));
            }
        }
        
        return grupos;
    }
    
    // Simula encontrar el ejemplar de un grupo (en AP sería el punto más representativo)
    private Pedido encontrarEjemplar(List<Pedido> grupo) {
        // En un caso real, el ejemplar sería el punto determinado por el algoritmo AP
        // que mejor representa al grupo. Aquí simplemente elegimos uno al azar.
        if (grupo.isEmpty()) return null;
        
        // Por simulación, elegimos el pedido más cercano al centroide
        Map<String, Double> centroide = calcularCentroide(grupo);
        Pedido ejemplar = null;
        double minDistancia = Double.MAX_VALUE;
        
        for (Pedido pedido : grupo) {
            double distancia = Math.sqrt(
                Math.pow(pedido.getPosX() - centroide.get("x"), 2) + 
                Math.pow(pedido.getPosY() - centroide.get("y"), 2)
            );
            
            if (distancia < minDistancia) {
                minDistancia = distancia;
                ejemplar = pedido;
            }
        }
        
        return ejemplar;
    }
    
    // Calcula el centroide (punto medio) de un grupo de pedidos
    private Map<String, Double> calcularCentroide(List<Pedido> grupo) {
        Map<String, Double> centroide = new HashMap<>();
        
        if (grupo.isEmpty()) {
            centroide.put("x", 0.0);
            centroide.put("y", 0.0);
            return centroide;
        }
        
        double sumX = 0, sumY = 0;
        for (Pedido pedido : grupo) {
            sumX += pedido.getPosX();
            sumY += pedido.getPosY();
        }
        
        centroide.put("x", sumX / grupo.size());
        centroide.put("y", sumY / grupo.size());
        
        return centroide;
    }
    
    // Calcula el radio del grupo (distancia máxima desde el centroide a cualquier punto)
    private double calcularRadioGrupo(List<Pedido> grupo, double centroideX, double centroideY) {
        if (grupo.isEmpty()) return 0;
        
        double maxDistancia = 0;
        
        for (Pedido pedido : grupo) {
            double distancia = Math.sqrt(
                Math.pow(pedido.getPosX() - centroideX, 2) + 
                Math.pow(pedido.getPosY() - centroideY, 2)
            );
            
            if (distancia > maxDistancia) {
                maxDistancia = distancia;
            }
        }
        
        return maxDistancia;
    }
    
    // Convierte un pedido a un DTO
    private PedidoDTO convertirAPedidoDTO(Pedido pedido) {
        if (pedido == null) return null;
        
        return PedidoDTO.builder()
            .id(pedido.getId())
            .codigo(pedido.getCodigo())
            .posX(pedido.getPosX())
            .posY(pedido.getPosY())
            .volumenGLPAsignado(pedido.getVolumenGLPAsignado())
            .horasLimite(pedido.getHorasLimite())
            .clienteId(pedido.getCliente() != null ? pedido.getCliente().getId() : null)
            .clienteNombre(pedido.getCliente() != null ? pedido.getCliente().getNombre() : null)
            .build();
    }
}
```

## main\java\com\plg\service\AlgoritmoGeneticoService.java

```java
package com.plg.service;

import com.plg.dto.*;
import com.plg.entity.Almacen;
import com.plg.entity.Camion;
import com.plg.entity.NodoRuta;
import com.plg.entity.Pedido;
import com.plg.entity.Ruta;
import com.plg.entity.EstadoCamion;
import com.plg.entity.EstadoPedido;
import com.plg.repository.AlmacenRepository;
import com.plg.repository.CamionRepository;
import com.plg.repository.PedidoRepository;
import com.plg.repository.RutaRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AlgoritmoGeneticoService {

    private static final Logger logger = LoggerFactory.getLogger(AlgoritmoGeneticoService.class);
    
    @Autowired
    private PedidoRepository pedidoRepository;
    
    @Autowired
    private CamionRepository camionRepository;
    
    @Autowired
    private RutaRepository rutaRepository;
    
    @Autowired
    private AlmacenRepository almacenRepository;
    
    // Parámetros configurables del algoritmo genético
    private final int POBLACION_INICIAL = 50;
    private final int MAX_GENERACIONES = 100;
    private final double TASA_MUTACION = 0.1;
    private final double TASA_CRUCE = 0.8;
    
    @Transactional
    public AlgoritmoGeneticoResultadoDTO generarRutas(Map<String, Object> params) {
        logger.info("Iniciando generación de rutas con algoritmo genético. Parámetros: {}", params);
        
        // Obtener pedidos pendientes
        List<Pedido> pedidos = pedidoRepository.findByEstado(EstadoPedido.PENDIENTE_PLANIFICACION);
        logger.info("Pedidos pendientes encontrados: {}", pedidos.size());
        
        // Verificar si hay suficientes pedidos para optimizar
        if (pedidos.isEmpty()) {
            logger.warn("No hay pedidos pendientes para generar rutas");
            return AlgoritmoGeneticoResultadoDTO.builder()
                .metodo("algoritmoGenetico")
                .totalPedidos(0)
                .pedidosAsignados(0)
                .rutas(Collections.emptyList())
                .build();
        }
        
        // Parámetros opcionales
        int numeroRutas = params.containsKey("numeroRutas") ? 
                         Integer.parseInt(params.get("numeroRutas").toString()) : 3;
        logger.info("Generando {} rutas para {} pedidos", numeroRutas, pedidos.size());
        
        // Obtener camiones disponibles
        List<Camion> camionesDisponibles = camionRepository.findByEstado(EstadoCamion.DISPONIBLE ); // Usar el enum
        logger.info("Camiones disponibles encontrados: {}", camionesDisponibles.size());
        
        if (camionesDisponibles.isEmpty()) {
            logger.warn("No hay camiones disponibles para asignar a las rutas");
            return AlgoritmoGeneticoResultadoDTO.builder()
                .metodo("algoritmoGenetico")
                .totalPedidos(pedidos.size())
                .pedidosAsignados(0)
                .mensaje("No hay camiones disponibles para asignar")
                .rutas(Collections.emptyList())
                .build();
        }
        
        // Ajustar el número de rutas según los camiones disponibles
        numeroRutas = Math.min(numeroRutas, camionesDisponibles.size());
        
        // Obtener almacén central
        Almacen almacenCentral = almacenRepository.findByEsCentralAndActivoTrue(true);
        if (almacenCentral == null) {
            logger.error("No se encontró el almacén central activo");
            return AlgoritmoGeneticoResultadoDTO.builder()
                .metodo("algoritmoGenetico")
                .totalPedidos(pedidos.size())
                .pedidosAsignados(0)
                .mensaje("Error: No se encontró el almacén central")
                .rutas(Collections.emptyList())
                .build();
        }
        
        // Generamos grupos de pedidos para las rutas
        List<List<Pedido>> gruposPedidos = dividirEnGrupos(pedidos, numeroRutas);
        logger.info("Pedidos divididos en {} grupos", gruposPedidos.size());
        
        // Crear rutas y asignar camiones
        List<RutaDTO> rutasDTO = new ArrayList<>();
        List<Ruta> rutasCreadas = new ArrayList<>();
        
        for (int i = 0; i < gruposPedidos.size(); i++) {
            if (i >= camionesDisponibles.size()) break;
            
            List<Pedido> pedidosGrupo = gruposPedidos.get(i);
            Camion camion = camionesDisponibles.get(i);
            
            logger.info("Creando ruta {} con {} pedidos para camión {}", (i+1), pedidosGrupo.size(), camion.getCodigo());
            
            // Crear una nueva entidad Ruta
            Ruta ruta = new Ruta("R" + System.currentTimeMillis() + "-" + (i+1));
            ruta.setCamion(camion);
            ruta.setEstado(1); // Estado 1 = En curso
            ruta.setFechaCreacion(LocalDateTime.now());
            ruta.setFechaInicioRuta(LocalDateTime.now());
            ruta.setConsideraBloqueos(true);
            
            // Agregar nodo inicial (almacén central)
            ruta.agregarNodo(almacenCentral.getPosX(), almacenCentral.getPosY(), "ALMACEN");
            
            // Agregar pedidos como nodos
            for (Pedido pedido : pedidosGrupo) {
                pedido.setEstado( EstadoPedido.PLANIFICADO_TOTALMENTE); // Estado 1 = En ruta
                pedidoRepository.save(pedido);
                
                // Agregar a la ruta como nodo cliente
                ruta.agregarNodoCliente(pedido.getPosX(), pedido.getPosY(), pedido, 
                                       pedido.getVolumenGLPAsignado(), 100.0);
            }
            
            // Agregar nodo final (retorno al almacén)
            ruta.agregarNodo(almacenCentral.getPosX(), almacenCentral.getPosY(), "ALMACEN");
            
            // Calcular distancia total y optimizar ruta si es necesario
            ruta.calcularDistanciaTotal();
            
            // Cambiar estado del camión a "En ruta"
            camion.setEstado(EstadoCamion.EN_RUTA); // Usar el enum
            camionRepository.save(camion);
            
            // Guardar la ruta
            rutaRepository.save(ruta);
            rutasCreadas.add(ruta);
            
            // Crear DTO para la respuesta
            List<PedidoDTO> pedidosDTO = pedidosGrupo.stream()
                .map(this::convertirAPedidoDTO)
                .collect(Collectors.toList());
            
            List<PuntoRutaDTO> puntosRuta = new ArrayList<>();
            for (NodoRuta nodo : ruta.getNodos()) {
                PuntoRutaDTO punto = PuntoRutaDTO.builder()
                    .tipo(nodo.getTipo())
                    .posX(nodo.getPosX())
                    .posY(nodo.getPosY())
                    .idPedido(nodo.getPedido() != null ? nodo.getPedido().getId() : null)
                    .build();
                puntosRuta.add(punto);
            }
            
            RutaDTO rutaDTO = RutaDTO.builder()
                .idRuta(ruta.getCodigo())
                .distanciaTotal(ruta.getDistanciaTotal())
                .tiempoEstimado(180) // Valor simulado en minutos
                .pedidos(pedidosDTO)
                .numeroPedidos(pedidosGrupo.size())
                .puntos(puntosRuta)
                .camionCodigo(camion.getCodigo())
                .build();
            
            rutasDTO.add(rutaDTO);
            logger.info("Ruta {} creada exitosamente con id: {}", (i+1), ruta.getId());
        }
        
        int pedidosAsignados = gruposPedidos.stream()
            .mapToInt(List::size)
            .sum();
        
        logger.info("Generación de rutas completada. Rutas creadas: {}, Pedidos asignados: {}/{}",
            rutasCreadas.size(), pedidosAsignados, pedidos.size());
        
        // Preparamos el resultado usando DTO
        AlgoritmoGeneticoResultadoDTO resultado = AlgoritmoGeneticoResultadoDTO.builder()
            .rutas(rutasDTO)
            .metodo("algoritmoGenetico")
            .totalPedidos(pedidos.size())
            .pedidosAsignados(pedidosAsignados)
            .rutasGeneradas(rutasCreadas.size())
            .build();
        
        return resultado;
    }
    
    // Método auxiliar para dividir pedidos en grupos (simulado)
    private List<List<Pedido>> dividirEnGrupos(List<Pedido> pedidos, int numeroGrupos) {
        logger.debug("Dividiendo {} pedidos en {} grupos", pedidos.size(), numeroGrupos);
        List<List<Pedido>> grupos = new ArrayList<>();
        
        // Asegurar que no intentamos crear más grupos que pedidos disponibles
        numeroGrupos = Math.min(numeroGrupos, pedidos.size());
        
        // Crear grupos vacíos
        for (int i = 0; i < numeroGrupos; i++) {
            grupos.add(new ArrayList<>());
        }
        
        // Distribuir pedidos (enfoque simple por turnos)
        for (int i = 0; i < pedidos.size(); i++) {
            grupos.get(i % numeroGrupos).add(pedidos.get(i));
        }
        
        return grupos;
    }
    
    // Convierte un pedido a un DTO
    private PedidoDTO convertirAPedidoDTO(Pedido pedido) {
        return PedidoDTO.builder()
            .id(pedido.getId())
            .codigo(pedido.getCodigo())
            .posX(pedido.getPosX())
            .posY(pedido.getPosY())
            .volumenGLPAsignado(pedido.getVolumenGLPAsignado())
            .horasLimite(pedido.getHorasLimite())
            .clienteId(pedido.getCliente() != null ? pedido.getCliente().getId() : null)
            .clienteNombre(pedido.getCliente() != null ? pedido.getCliente().getNombre() : null)
            .build();
    }
}
```

## main\java\com\plg\service\AlmacenCombustibleService.java

```java
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
```

## main\java\com\plg\service\BloqueoService.java

```java
package com.plg.service;

import com.plg.entity.Bloqueo;
import com.plg.repository.BloqueoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.plg.config.MapaConfig;
@Service
public class BloqueoService {

    @Autowired
    private BloqueoRepository bloqueoRepository;

    @Autowired
    private MapaConfig mapaConfig;

    /**
     * Carga los bloqueos desde un archivo para un mes específico
     * @param anio Año (ej. 2025)
     * @param mes Mes (1-12)
     * @return Lista de bloqueos cargados
     */
    public List<Bloqueo> cargarBloqueosMensuales(int anio, int mes) {
        List<Bloqueo> bloqueos = new ArrayList<>();
        
        // Formato del nombre del archivo: aaaamm.bloqueadas
        String nombreArchivo = String.format("%04d%02d.bloqueadas", anio, mes);
        Path rutaArchivo = Paths.get("src/main/resources/data/bloqueos/", nombreArchivo);
        
        try {
            if (!Files.exists(rutaArchivo)) {
                System.out.println("Archivo de bloqueos no encontrado: " + nombreArchivo);
                return bloqueos;
            }
            
            List<String> lineas = Files.readAllLines(rutaArchivo);
            
            // Formato esperado del archivo:
            // Para el formato antiguo: x1,y1,x2,y2,fechaInicio,fechaFin,descripcion
            // Para el formato nuevo: ##d##h##m-##d##h##m:x1,y1,x2,y2,...,xn,yn
            for (String linea : lineas) {
                if (linea.trim().isEmpty()) continue;
                
                // Determinar el formato
                if (linea.contains("-") && linea.contains(":")) {
                    // Formato nuevo
                    bloqueos.add(procesarLineaNuevoFormatoReticular(linea, anio, mes));
                } else {
                    // Formato antiguo (compatibilidad)
                    bloqueos.add(procesarLineaFormatoAntiguo(linea));
                }
            }
            
            // Guardar los bloqueos en la base de datos
            bloqueoRepository.saveAll(bloqueos);
            
            System.out.println("Cargados " + bloqueos.size() + " bloqueos para " + YearMonth.of(anio, mes));
            
        } catch (IOException e) {
            System.err.println("Error al leer el archivo de bloqueos: " + e.getMessage());
        }
        
        return bloqueos;
    }
    
    /**
     * Procesa una línea en el formato nuevo
     * ##d##h##m-##d##h##m:x1,y1,x2,y2,...,xn,yn
     */
    private Bloqueo procesarLineaNuevoFormato(String linea, int anio, int mes) {
        // Separar la línea en partes: tiempo y coordenadas
        String[] partes = linea.split(":");
        if (partes.length != 2) {
            throw new IllegalArgumentException("Formato de línea inválido: " + linea);
        }
        
        String tiempos = partes[0];
        String coordenadasStr = partes[1];
        
        // Separar los tiempos de inicio y fin
        String[] tiempoPartes = tiempos.split("-");
        if (tiempoPartes.length != 2) {
            throw new IllegalArgumentException("Formato de tiempo inválido: " + tiempos);
        }
        
        // Parsear fechas de inicio y fin
        LocalDateTime fechaInicio = parsearFechaHora(tiempoPartes[0], anio, mes);
        LocalDateTime fechaFin = parsearFechaHora(tiempoPartes[1], anio, mes);
        
        // Parsear coordenadas
        String[] coordValores = coordenadasStr.split(",");
        if (coordValores.length < 4 || coordValores.length % 2 != 0) {
            throw new IllegalArgumentException("Formato de coordenadas inválido: " + coordenadasStr);
        }
        
        List<Bloqueo.Coordenada> coordenadas = new ArrayList<>();
        for (int i = 0; i < coordValores.length; i += 2) {
            int x = Integer.parseInt(coordValores[i]);
            int y = Integer.parseInt(coordValores[i + 1]);
            coordenadas.add(new Bloqueo.Coordenada(x, y));
        }
        
        // Crear el objeto Bloqueo
        Bloqueo bloqueo = new Bloqueo();
        bloqueo.setFechaInicio(fechaInicio);
        bloqueo.setFechaFin(fechaFin);
        bloqueo.setCoordenadas(coordenadas);
        bloqueo.setDescripcion("Bloqueo programado " + fechaInicio.toLocalDate());
        bloqueo.setActivo(LocalDateTime.now().isAfter(fechaInicio) && LocalDateTime.now().isBefore(fechaFin));
        
        return bloqueo;
    }
    
    /**
     * Procesa una línea en el formato nuevo adaptado a mapa reticular
     * ##d##h##m-##d##h##m:x1,y1,x2,y2,...,xn,yn
     * Este método asegura que las coordenadas son nodos válidos en el mapa reticular
     */
    private Bloqueo procesarLineaNuevoFormatoReticular(String linea, int anio, int mes) {
        // Separar la línea en partes: tiempo y coordenadas
        String[] partes = linea.split(":");
        if (partes.length != 2) {
            throw new IllegalArgumentException("Formato de línea inválido: " + linea);
        }
        
        String tiempos = partes[0];
        String coordenadasStr = partes[1];
        
        // Separar los tiempos de inicio y fin
        String[] tiempoPartes = tiempos.split("-");
        if (tiempoPartes.length != 2) {
            throw new IllegalArgumentException("Formato de tiempo inválido: " + tiempos);
        }
        
        // Parsear fechas de inicio y fin
        LocalDateTime fechaInicio = parsearFechaHora(tiempoPartes[0], anio, mes);
        LocalDateTime fechaFin = parsearFechaHora(tiempoPartes[1], anio, mes);
        
        // Parsear coordenadas
        String[] coordValores = coordenadasStr.split(",");
        if (coordValores.length < 4 || coordValores.length % 2 != 0) {
            throw new IllegalArgumentException("Formato de coordenadas inválido: " + coordenadasStr);
        }
        
        List<Bloqueo.Coordenada> coordenadas = new ArrayList<>();
        for (int i = 0; i < coordValores.length; i += 2) {
            double x = Integer.parseInt(coordValores[i]);
            double y = Integer.parseInt(coordValores[i + 1]);
            
            // Verificar que la coordenada esté dentro del mapa reticular
            if (!mapaConfig.estaEnMapa(x, y)) {
                System.out.println("Advertencia: Coordenada fuera del mapa (" + x + "," + y + ") - ajustando al límite más cercano");
                x = Math.max(mapaConfig.getOrigenX(), Math.min(x, mapaConfig.getOrigenX() + mapaConfig.getLargo()));
                y = Math.max(mapaConfig.getOrigenY(), Math.min(y, mapaConfig.getOrigenY() + mapaConfig.getAncho()));
            }
            
            coordenadas.add(new Bloqueo.Coordenada(x, y));
        }
        
        // Validar que las coordenadas formen tramos horizontales o verticales válidos
        validarTramosReticularesValidos(coordenadas);
        
        // Crear el objeto Bloqueo
        Bloqueo bloqueo = new Bloqueo();
        bloqueo.setFechaInicio(fechaInicio);
        bloqueo.setFechaFin(fechaFin);
        bloqueo.setCoordenadas(coordenadas);
        bloqueo.setDescripcion("Bloqueo programado " + fechaInicio.toLocalDate());
        bloqueo.setActivo(LocalDateTime.now().isAfter(fechaInicio) && LocalDateTime.now().isBefore(fechaFin));
        
        return bloqueo;
    }

    /**
     * Valida que las coordenadas formen tramos horizontales o verticales válidos
     * en un mapa reticular (no diagonales)
     */
    private void validarTramosReticularesValidos(List<Bloqueo.Coordenada> coordenadas) {
        if (coordenadas.size() < 2) {
            return; // No hay suficientes puntos para formar un tramo
        }
        
        for (int i = 0; i < coordenadas.size() - 1; i++) {
            Bloqueo.Coordenada c1 = coordenadas.get(i);
            Bloqueo.Coordenada c2 = coordenadas.get(i + 1);
            
            // En un mapa reticular, un tramo debe ser horizontal o vertical
            boolean esHorizontal = c1.getY() == c2.getY();
            boolean esVertical = c1.getX() == c2.getX();
            
            if (!esHorizontal && !esVertical) {
                throw new IllegalArgumentException(
                    "Error: Tramo diagonal no permitido en mapa reticular - " +
                    "Desde (" + c1.getX() + "," + c1.getY() + ") hasta (" + c2.getX() + "," + c2.getY() + ")"
                );
            }
        }
    }
    
    /**
     * Parsea un string en formato ##d##h##m a LocalDateTime
     */
    private LocalDateTime parsearFechaHora(String tiempo, int anio, int mes) {
        // Expresión regular para extraer día, hora y minuto
        Pattern pattern = Pattern.compile("(\\d{2})d(\\d{2})h(\\d{2})m");
        Matcher matcher = pattern.matcher(tiempo);
        
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Formato de tiempo inválido: " + tiempo);
        }
        
        int dia = Integer.parseInt(matcher.group(1));
        int hora = Integer.parseInt(matcher.group(2));
        int minuto = Integer.parseInt(matcher.group(3));
        
        return LocalDateTime.of(anio, mes, dia, hora, minuto);
    }
    
    /**
     * Procesa una línea en el formato antiguo (para compatibilidad)
     * x1,y1,x2,y2,fechaInicio,fechaFin,descripcion
     */
    private Bloqueo procesarLineaFormatoAntiguo(String linea) {
        String[] partes = linea.split(",");
        if (partes.length < 7) {
            throw new IllegalArgumentException("Formato de línea antiguo inválido: " + linea);
        }
        
        int x1 = Integer.parseInt(partes[0]);
        int y1 = Integer.parseInt(partes[1]);
        int x2 = Integer.parseInt(partes[2]);
        int y2 = Integer.parseInt(partes[3]);
        
        LocalDate fechaInicio = LocalDate.parse(partes[4]);
        LocalDate fechaFin = LocalDate.parse(partes[5]);
        String descripcion = partes[6];
        
        // Crear lista de coordenadas
        List<Bloqueo.Coordenada> coordenadas = new ArrayList<>();
        coordenadas.add(new Bloqueo.Coordenada(x1, y1));
        coordenadas.add(new Bloqueo.Coordenada(x2, y2));
        
        // Crear objeto Bloqueo
        Bloqueo bloqueo = new Bloqueo();
        bloqueo.setFechaInicio(fechaInicio.atStartOfDay());
        bloqueo.setFechaFin(fechaFin.atTime(23, 59, 59));
        bloqueo.setCoordenadas(coordenadas);
        bloqueo.setDescripcion(descripcion);
        bloqueo.setActivo(LocalDate.now().isAfter(fechaInicio.minusDays(1)) && 
                          LocalDate.now().isBefore(fechaFin.plusDays(1)));
        
        return bloqueo;
    }
    
    /**
     * Verifica si una ruta está bloqueada entre dos puntos
     * @param x1 Coordenada X del punto inicial
     * @param y1 Coordenada Y del punto inicial
     * @param x2 Coordenada X del punto final
     * @param y2 Coordenada Y del punto final
     * @return true si la ruta está bloqueada
     */
    public boolean esRutaBloqueada(int x1, int y1, int x2, int y2) {
        // Obtener bloqueos activos en el momento actual
        List<Bloqueo> bloqueosActivos = bloqueoRepository.findByActivoTrue();
        
        // Si no hay bloqueos activos, la ruta está libre
        if (bloqueosActivos.isEmpty()) {
            return false;
        }
        
        // Para cada bloqueo, verificar si intersecta con la línea entre los puntos
        for (Bloqueo bloqueo : bloqueosActivos) {
            if (intersectaConRuta(bloqueo, x1, y1, x2, y2)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Verifica si un bloqueo intersecta con una ruta
     */
    private boolean intersectaConRuta(Bloqueo bloqueo, int x1, int y1, int x2, int y2) {
        List<Bloqueo.Coordenada> coordenadas = bloqueo.getCoordenadas();
        
        // Si hay menos de 2 coordenadas, no hay tramos
        if (coordenadas.size() < 2) {
            return false;
        }
        
        // Para cada tramo del bloqueo, verificar si intersecta con la ruta
        for (int i = 0; i < coordenadas.size() - 1; i++) {
            Bloqueo.Coordenada inicio = coordenadas.get(i);
            Bloqueo.Coordenada fin = coordenadas.get(i + 1);
            
            // Verificar intersección de líneas
            if (hayInterseccion(x1, y1, x2, y2, inicio.getX(), inicio.getY(), fin.getX(), fin.getY())) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Verifica si dos segmentos de línea se intersectan
     */
    private boolean hayInterseccion(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        // Calculamos orientación de los puntos
        int o1 = orientacion(x1, y1, x2, y2, x3, y3);
        int o2 = orientacion(x1, y1, x2, y2, x4, y4);
        int o3 = orientacion(x3, y3, x4, y4, x1, y1);
        int o4 = orientacion(x3, y3, x4, y4, x2, y2);
        
        // Caso general: orientaciones diferentes
        if (o1 != o2 && o3 != o4) {
            return true;
        }
        
        // Casos especiales para colinealidad
        if (o1 == 0 && estaPuntoEnSegmento(x3, y3, x1, y1, x2, y2)) return true;
        if (o2 == 0 && estaPuntoEnSegmento(x4, y4, x1, y1, x2, y2)) return true;
        if (o3 == 0 && estaPuntoEnSegmento(x1, y1, x3, y3, x4, y4)) return true;
        if (o4 == 0 && estaPuntoEnSegmento(x2, y2, x3, y3, x4, y4)) return true;
        
        return false;
    }
    
    /**
     * Calcula la orientación de tres puntos ordenados
     * Retorna:
     * 0 --> Colineales
     * 1 --> Sentido horario
     * 2 --> Sentido antihorario
     */
    private int orientacion(double x1, double y1, double x2, double y2, double x3, double y3) {
        double val = (y2 - y1) * (x3 - x2) - (x2 - x1) * (y3 - y2);
        
        if (val == 0) return 0;  // Colineal
        return (val > 0) ? 1 : 2; // Sentido horario o antihorario
    }
    
    /**
     * Verifica si un punto está dentro de un segmento de línea
     */
    private boolean estaPuntoEnSegmento(double x, double y, double x1, double y1, double x2, double y2) {
        return x >= Math.min(x1, x2) && x <= Math.max(x1, x2) &&
               y >= Math.min(y1, y2) && y <= Math.max(y1, y2);
    }
    
    /**
     * Obtiene los bloqueos activos en un momento dado
     */
    public List<Bloqueo> obtenerBloqueosActivos(LocalDateTime momento) {
        return bloqueoRepository.findByFechaInicioBeforeAndFechaFinAfter(momento, momento);
    }
    
    /**
     * Actualiza el estado activo de todos los bloqueos según el momento actual
     */
    public void actualizarEstadoBloqueos() {
        LocalDateTime ahora = LocalDateTime.now();
        List<Bloqueo> todosBloqueos = bloqueoRepository.findAll();
        
        for (Bloqueo bloqueo : todosBloqueos) {
            boolean debeEstarActivo = ahora.isAfter(bloqueo.getFechaInicio()) && 
                                     ahora.isBefore(bloqueo.getFechaFin());
            
            if (bloqueo.isActivo() != debeEstarActivo) {
                bloqueo.setActivo(debeEstarActivo);
                bloqueoRepository.save(bloqueo);
            }
        }
    }
}
```

## main\java\com\plg\service\CamionService.java

```java
package com.plg.service;

import com.plg.entity.Camion;
import com.plg.entity.Pedido;
import com.plg.entity.EstadoCamion;
import com.plg.entity.EstadoPedido;
import com.plg.repository.CamionRepository;
import com.plg.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CamionService {

    @Autowired
    private CamionRepository camionRepository;
    
    @Autowired
    private PedidoRepository pedidoRepository;
    
    /**
     * Obtiene todos los camiones
     */
    public List<Camion> findAll() {
        return camionRepository.findAll();
    }
    
    /**
     * Obtiene un camión por su código
     */
    public Optional<Camion> findById(String codigo) {
        return camionRepository.findByCodigo(codigo);
    }
    
    /**
     * Obtiene camiones por estado
     */
    public List<Camion> findByEstado(EstadoCamion estado) {
        return camionRepository.findByEstado(estado );
    }
    
    /**
     * Obtiene camiones por tipo
     */
    public List<Camion> findByTipo(String tipo) {
        return camionRepository.findByTipo(tipo);
    }
    
    /**
     * Obtiene pedidos asignados a un camión
     */
    public List<Pedido> findPedidosByCamion(String codigoCamion) {
        return pedidoRepository.findByCamion_Codigo(codigoCamion);
    }
    
    /**
     * Obtiene estadísticas de camiones
     */
    public Map<String, Object> getEstadisticasCamiones() {
        List<Camion> allCamiones = camionRepository.findAll();
        Map<String, Object> stats = new HashMap<>();
        
        // Información general
        stats.put("totalCamiones", allCamiones.size());
        stats.put("capacidadPromedio", getCapacidadPromedio(allCamiones));
        
        // Información por estado
        Map<EstadoCamion, Long> camionesEstado = allCamiones.stream()
            .collect(Collectors.groupingBy(Camion::getEstado, Collectors.counting()));
        
        Map<String, Long> estadosMap = new HashMap<>();
        camionesEstado.forEach((estado, count) -> {
            estadosMap.put(estado.name(), count);
        });
        stats.put("porEstado", estadosMap);
        
        // Información por tipo
        Map<String, Long> camionesTipo = allCamiones.stream()
            .collect(Collectors.groupingBy(Camion::getTipo, Collectors.counting()));
        
        stats.put("porTipo", camionesTipo);
        
        // Información de capacidad disponible
        double capacidadTotal = allCamiones.stream()
            .filter(c -> c.getEstado() == EstadoCamion.DISPONIBLE) // Solo disponibles
            .mapToDouble(Camion::getCapacidad)
            .sum();
        stats.put("capacidadDisponible", capacidadTotal);
        
        return stats;
    }
    
    /**
     * Obtiene información detallada del camión incluyendo pedidos, mantenimientos y averías asociadas
     */
    public Map<String, Object> getDetalleCamion(String codigo) {
        Optional<Camion> optCamion = camionRepository.findByCodigo(codigo);
        if (!optCamion.isPresent()) {
            return null;
        }
        
        Camion camion = optCamion.get();
        Map<String, Object> detalle = new HashMap<>();
        
        // Información básica
        detalle.put("codigo", camion.getCodigo());
        detalle.put("tipo", camion.getTipo());
        detalle.put("capacidad", camion.getCapacidad());
        detalle.put("tara", camion.getTara());
        detalle.put("estado", camion.getEstado());
        detalle.put("estadoNombre", camion.getEstado().name());
        
        // Pedidos asignados
        List<Pedido> pedidos = pedidoRepository.findByCamion_Codigo(codigo);
        detalle.put("pedidosAsignados", pedidos.size());
        detalle.put("pedidos", pedidos);
        
        // Cálculo de carga actual
        double cargaActual = pedidos.stream()
            .filter(p -> p.getEstado() == EstadoPedido.PLANIFICADO_TOTALMENTE || p.getEstado() ==  EstadoPedido.EN_RUTA) // Asignados o en ruta
            .mapToDouble(Pedido::getVolumenGLPAsignado)
            .sum();
        detalle.put("cargaActual", cargaActual);
        detalle.put("porcentajeOcupacion", camion.getCapacidad() > 0 ? 
                                         (cargaActual / camion.getCapacidad()) * 100 : 0);
        
        return detalle;
    }
    
    // Métodos auxiliares
    
    private double getCapacidadPromedio(List<Camion> camiones) {
        if (camiones.isEmpty()) return 0;
        return camiones.stream()
            .mapToDouble(Camion::getCapacidad)
            .average()
            .orElse(0);
    }
}
```

## main\java\com\plg\service\ConversionArchivoService.java

```java
package com.plg.service;

import com.plg.config.MapaConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para realizar conversiones de formatos de archivos
 */
@Service
public class ConversionArchivoService {

    @Autowired
    private MapaConfig mapaConfig;
    
    /**
     * Convierte el archivo de bloqueos del formato antiguo al nuevo formato
     * Compatible con mapa reticular
     * 
     * @param anio Año del archivo
     * @param mes Mes del archivo
     * @return true si la conversión fue exitosa
     */
    public boolean convertirArchivoBloqueos(int anio, int mes) {
        // Formato del nombre del archivo: aaaamm.bloqueadas
        String nombreArchivo = String.format("%04d%02d.bloqueadas", anio, mes);
        Path rutaArchivo = Paths.get("src/main/resources/data/bloqueos/", nombreArchivo);
        
        if (!Files.exists(rutaArchivo)) {
            System.out.println("Archivo de bloqueos no encontrado: " + nombreArchivo);
            return false;
        }
        
        // Ruta para el nuevo archivo
        Path rutaNuevoArchivo = Paths.get("src/main/resources/data/bloqueos/", 
                                         String.format("%04d%02d.bloqueadas.nuevo", anio, mes));
        
        try {
            List<String> lineasAntiguas = Files.readAllLines(rutaArchivo);
            List<String> lineasNuevas = new ArrayList<>();
            
            for (String linea : lineasAntiguas) {
                if (linea.trim().isEmpty()) {
                    lineasNuevas.add("");
                    continue;
                }
                
                // Si la línea ya está en el nuevo formato, copiarla tal cual
                if (linea.contains("-") && linea.contains(":")) {
                    lineasNuevas.add(linea);
                    continue;
                }
                
                // Convertir del formato antiguo al nuevo
                String lineaNueva = convertirLineaFormatoAntiguo(linea);
                lineasNuevas.add(lineaNueva);
            }
            
            // Escribir al nuevo archivo
            Files.write(rutaNuevoArchivo, lineasNuevas, StandardOpenOption.CREATE, 
                       StandardOpenOption.TRUNCATE_EXISTING);
            
            System.out.println("Archivo convertido exitosamente: " + rutaNuevoArchivo);
            return true;
            
        } catch (IOException e) {
            System.err.println("Error al convertir archivo: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Convierte una línea del formato antiguo al nuevo formato
     * Formato antiguo: x1,y1,x2,y2,fechaInicio,fechaFin,descripcion
     * Formato nuevo: ##d##h##m-##d##h##m:x1,y1,x2,y2
     */
    private String convertirLineaFormatoAntiguo(String linea) {
        String[] partes = linea.split(",");
        if (partes.length < 7) {
            throw new IllegalArgumentException("Formato de línea antiguo inválido: " + linea);
        }
        
        int x1 = Integer.parseInt(partes[0]);
        int y1 = Integer.parseInt(partes[1]);
        int x2 = Integer.parseInt(partes[2]);
        int y2 = Integer.parseInt(partes[3]);
        
        // Verificar que las coordenadas estén dentro del mapa reticular
        ajustarCoordenadasALimites(x1, y1);
        ajustarCoordenadasALimites(x2, y2);
        
        // Parsear fechas
        LocalDate fechaInicio = LocalDate.parse(partes[4]);
        LocalDate fechaFin = LocalDate.parse(partes[5]);
        
        // Convertir a formato nuevo (asumimos hora inicial 06:00 y hora final 18:00)
        LocalDateTime fechaInicioConHora = fechaInicio.atTime(6, 0);
        LocalDateTime fechaFinConHora = fechaFin.atTime(18, 0);
        
        String tiempoInicio = String.format("%02dd%02dh%02dm", 
            fechaInicioConHora.getDayOfMonth(), fechaInicioConHora.getHour(), fechaInicioConHora.getMinute());
        
        String tiempoFin = String.format("%02dd%02dh%02dm", 
            fechaFinConHora.getDayOfMonth(), fechaFinConHora.getHour(), fechaFinConHora.getMinute());
        
        // Verificar si es un tramo horizontal o vertical
        // Si es diagonal, ajustarlo a dos tramos (horizontal y vertical)
        String coordenadas;
        if (x1 == x2 || y1 == y2) {
            // Tramo ya es horizontal o vertical, formato válido
            coordenadas = String.format("%d,%d,%d,%d", x1, y1, x2, y2);
        } else {
            // Tramo diagonal, convertir a dos tramos (horizontal + vertical)
            coordenadas = String.format("%d,%d,%d,%d,%d,%d", x1, y1, x1, y2, x2, y2);
        }
        
        return tiempoInicio + "-" + tiempoFin + ":" + coordenadas;
    }
    
    /**
     * Ajusta las coordenadas para que estén dentro de los límites del mapa
     */
    private double[] ajustarCoordenadasALimites(double x, double y) {
        double xAjustado = Math.max(mapaConfig.getOrigenX(), 
                       Math.min(x, mapaConfig.getOrigenX() + mapaConfig.getLargo()));
        
                       double yAjustado = Math.max(mapaConfig.getOrigenY(), 
                       Math.min(y, mapaConfig.getOrigenY() + mapaConfig.getAncho()));
        
        return new double[]{xAjustado, yAjustado};
    }
}
```

## main\java\com\plg\service\MantenimientoService.java

```java
package com.plg.service;

import com.plg.entity.Camion;
import com.plg.entity.Mantenimiento;
import com.plg.entity.EstadoCamion;
import com.plg.repository.CamionRepository;
import com.plg.repository.MantenimientoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MantenimientoService {

    @Autowired
    private MantenimientoRepository mantenimientoRepository;
    
    @Autowired
    private CamionRepository camionRepository;

    public List<Mantenimiento> findAll() {
        return mantenimientoRepository.findAll();
    }

    public Optional<Mantenimiento> findById(Long id) {
        return mantenimientoRepository.findById(id);
    }

    public List<Mantenimiento> findByCamion(String codigoCamion) {
        return mantenimientoRepository.findByCamion_Codigo(codigoCamion);
    }
    
    public List<Mantenimiento> findByPeriodo(LocalDate inicio, LocalDate fin) {
        return mantenimientoRepository.findByFechaInicioBetween(inicio, fin);
    }

    public Mantenimiento save(Mantenimiento mantenimiento) {
        // Si es un nuevo mantenimiento, actualiza el estado del camión
        if (mantenimiento.getId() == null && mantenimiento.getEstado() == 1) { // En proceso
            Optional<Camion> camionOpt = camionRepository.findByCodigo(mantenimiento.getCamion().getCodigo());
            if (camionOpt.isPresent()) {
                Camion camion = camionOpt.get();
                camion.setEstado(EstadoCamion.EN_MANTENIMIENTO_PREVENTIVO); // Usar el enum apropiado
                camionRepository.save(camion);
            }
        }
        
        return mantenimientoRepository.save(mantenimiento);
    }

    public Mantenimiento update(Long id, Mantenimiento mantenimiento) {
        Mantenimiento existingMantenimiento = mantenimientoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mantenimiento no encontrado"));
        
        existingMantenimiento.setCamion(mantenimiento.getCamion());
        existingMantenimiento.setFechaInicio(mantenimiento.getFechaInicio());
        existingMantenimiento.setFechaFin(mantenimiento.getFechaFin());
        existingMantenimiento.setTipo(mantenimiento.getTipo());
        existingMantenimiento.setDescripcion(mantenimiento.getDescripcion());
        
        // Si el estado cambia a finalizado, actualiza el estado del camión
        if (existingMantenimiento.getEstado() != 2 && mantenimiento.getEstado() == 2) { // Finalizado
            Optional<Camion> camionOpt = camionRepository.findByCodigo(mantenimiento.getCamion().getCodigo());
            if (camionOpt.isPresent()) {
                Camion camion = camionOpt.get();
                camion.setEstado(EstadoCamion.DISPONIBLE); // Usar el enum 
                camionRepository.save(camion);
            }
        }
        
        existingMantenimiento.setEstado(mantenimiento.getEstado());
        
        return mantenimientoRepository.save(existingMantenimiento);
    }

    public void delete(Long id) {
        mantenimientoRepository.deleteById(id);
    }
    
    public List<Mantenimiento> programarMantenimientosPreventivos() {
        List<Mantenimiento> nuevosMantenimientos = new ArrayList<>();
        
        try {
            // Lee el archivo de mantenimientos preventivos programados
            Path path = Paths.get("src/main/resources/data/mantenimientos/mantpreventivo.txt");
            try (BufferedReader reader = new BufferedReader(new FileReader(path.toFile()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] datos = line.split(",");
                    if (datos.length >= 3) {
                        String codigoCamion = datos[0];
                        LocalDate fechaInicio = LocalDate.parse(datos[1]);
                        LocalDate fechaFin = LocalDate.parse(datos[2]);
                        
                        // Verifica si existe el camión
                        Optional<Camion> camionOpt = camionRepository.findByCodigo(codigoCamion);
                        if (camionOpt.isPresent()) {
                            Mantenimiento mantenimiento = new Mantenimiento();
                            mantenimiento.setCamion(camionOpt.get());
                            mantenimiento.setFechaInicio(fechaInicio);
                            mantenimiento.setFechaFin(fechaFin);
                            mantenimiento.setTipo("preventivo");
                            mantenimiento.setDescripcion("Mantenimiento preventivo programado");
                            mantenimiento.setEstado(0); // Programado
                            
                            nuevosMantenimientos.add(mantenimientoRepository.save(mantenimiento));
                        }
                    }
                }
            }
            
        } catch (IOException e) {
            throw new RuntimeException("Error al leer el archivo de mantenimientos preventivos", e);
        }
        
        return nuevosMantenimientos;
    }
}
```

## main\java\com\plg\service\MapaReticularService.java

```java
package com.plg.service;

import com.plg.config.MapaConfig;
import com.plg.entity.Bloqueo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Servicio para la navegación en mapa reticular
 * Proporciona funciones para calcular rutas en un mapa de calles perpendiculares
 */
@Service
public class MapaReticularService {

    @Autowired
    private MapaConfig mapaConfig;
    
    @Autowired
    private BloqueoService bloqueoService;
    
    /**
     * Calcula la ruta más corta entre dos puntos en un mapa reticular
     * utilizando el algoritmo A* considerando bloqueos
     * 
     * @param xInicio Coordenada X del punto de inicio
     * @param yInicio Coordenada Y del punto de inicio
     * @param xFin Coordenada X del punto de destino
     * @param yFin Coordenada Y del punto de destino
     * @param bloqueos Lista de bloqueos activos a evitar
     * @return Lista de nodos que forman la ruta óptima
     */
    public List<double[]> calcularRutaOptima(double xInicio, double yInicio, double xFin, double yFin, List<Bloqueo> bloqueos) {
        // Verificar que las coordenadas estén dentro del mapa
        if (!mapaConfig.estaEnMapa(xInicio, yInicio) || !mapaConfig.estaEnMapa(xFin, yFin)) {
            throw new IllegalArgumentException("Coordenadas fuera de los límites del mapa");
        }
        
        // Implementación del algoritmo A*
        // Conjuntos para el algoritmo
        Set<String> nodosAbiertos = new HashSet<>();
        Set<String> nodosCerrados = new HashSet<>();
        Map<String, String> caminoPadre = new HashMap<>();
        Map<String, Double> gScore = new HashMap<>(); // Costo real desde inicio
        Map<String, Double> fScore = new HashMap<>(); // Costo estimado total
        
        // Inicialización
        String inicio = coordenadaAKey(xInicio, yInicio);
        String fin = coordenadaAKey(xFin, yFin);
        
        nodosAbiertos.add(inicio);
        gScore.put(inicio, 0.0);
        fScore.put(inicio, heuristica(xInicio, yInicio, xFin, yFin));
        
        // Cola de prioridad para seleccionar el nodo con menor fScore
        PriorityQueue<String> colaPrioridad = new PriorityQueue<>(
            Comparator.comparingDouble(nodo -> fScore.getOrDefault(nodo, Double.MAX_VALUE))
        );
        colaPrioridad.add(inicio);
        
        while (!nodosAbiertos.isEmpty()) {
            // Obtener el nodo con menor fScore
            String nodoActual = colaPrioridad.poll();
            
            // Si llegamos al destino, reconstruir y devolver el camino
            if (nodoActual.equals(fin)) {
                return reconstruirCamino(caminoPadre, nodoActual);
            }
            
            // Mover el nodo actual de abiertos a cerrados
            nodosAbiertos.remove(nodoActual);
            nodosCerrados.add(nodoActual);
            
            // Extraer coordenadas del nodo actual
            double[] coords = keyACoordenada(nodoActual);
            double x = coords[0];
            double y = coords[1];
            
            // Obtener vecinos
            double[][] nodosAdyacentes = mapaConfig.obtenerNodosAdyacentes(x, y);
            
            for (double[] vecino : nodosAdyacentes) {
                String vecinoKey = coordenadaAKey(vecino[0], vecino[1]);
                
                // Si el vecino ya fue evaluado, continuar
                if (nodosCerrados.contains(vecinoKey)) {
                    continue;
                }
                
                // Verificar si hay un bloqueo entre el nodo actual y el vecino
                if (verificarBloqueoEntrePuntos(x, y, vecino[0], vecino[1], bloqueos)) {
                    continue; // Omitir este vecino si el camino está bloqueado
                }
                
                // Calcular el costo hasta el vecino
                double tentativeGScore = gScore.get(nodoActual) + mapaConfig.getDistanciaNodos();
                
                // Si el vecino no está en abiertos o encontramos un mejor camino
                if (!nodosAbiertos.contains(vecinoKey) || tentativeGScore < gScore.getOrDefault(vecinoKey, Double.MAX_VALUE)) {
                    // Actualizar el camino y los puntajes
                    caminoPadre.put(vecinoKey, nodoActual);
                    gScore.put(vecinoKey, tentativeGScore);
                    fScore.put(vecinoKey, tentativeGScore + heuristica(vecino[0], vecino[1], xFin, yFin));
                    
                    // Agregar a conjunto de nodos abiertos si no está ya
                    if (!nodosAbiertos.contains(vecinoKey)) {
                        nodosAbiertos.add(vecinoKey);
                        colaPrioridad.add(vecinoKey);
                    }
                }
            }
        }
        
        // Si llegamos aquí, no encontramos un camino
        return new ArrayList<>();
    }
    
    /**
     * Verifica si hay un bloqueo entre dos puntos adyacentes
     */
    private boolean verificarBloqueoEntrePuntos(double x1, double y1, double x2, double y2, List<Bloqueo> bloqueos) {
        // Los puntos deben ser adyacentes (diferencia en solo una coordenada y valor 1)
        boolean sonAdyacentes = (Math.abs(x1 - x2) + Math.abs(y1 - y2)) == 1;
        if (!sonAdyacentes) {
            throw new IllegalArgumentException("Los puntos deben ser adyacentes para verificar bloqueo");
        }
        
        for (Bloqueo bloqueo : bloqueos) {
            // Si el bloqueo está activo y su tramo intersecta con la línea entre los puntos
            if (bloqueo.isActivo() && intersectaConTramo(x1, y1, x2, y2, bloqueo)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Verifica si un bloqueo intersecta un tramo de calle
     */
    private boolean intersectaConTramo(double x1, double y1, double x2, double y2, Bloqueo bloqueo) {
        List<Bloqueo.Coordenada> coordenadasBloqueo = bloqueo.getCoordenadas();
        
        // Si hay menos de 2 coordenadas en el bloqueo, no puede haber intersección
        if (coordenadasBloqueo.size() < 2) {
            return false;
        }
        
        // Para cada par de coordenadas consecutivas en el bloqueo
        for (int i = 0; i < coordenadasBloqueo.size() - 1; i++) {
            Bloqueo.Coordenada c1 = coordenadasBloqueo.get(i);
            Bloqueo.Coordenada c2 = coordenadasBloqueo.get(i + 1);
            
            // En un mapa reticular, los tramos de bloqueo también deben ser horizontales o verticales
            // Verificar si el tramo del bloqueo y el tramo de calle son iguales
            
            boolean mismo_tramo_horizontal = 
                (y1 == y2 && c1.getY() == c2.getY() && y1 == c1.getY()) && 
                ((x1 <= c1.getX() && c1.getX() <= x2) || (x1 <= c2.getX() && c2.getX() <= x2) ||
                 (c1.getX() <= x1 && x1 <= c2.getX()) || (c1.getX() <= x2 && x2 <= c2.getX()));
                
            boolean mismo_tramo_vertical = 
                (x1 == x2 && c1.getX() == c2.getX() && x1 == c1.getX()) && 
                ((y1 <= c1.getY() && c1.getY() <= y2) || (y1 <= c2.getY() && c2.getY() <= y2) ||
                 (c1.getY() <= y1 && y1 <= c2.getY()) || (c1.getY() <= y2 && y2 <= c2.getY()));
            
            if (mismo_tramo_horizontal || mismo_tramo_vertical) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Heurística para el algoritmo A* (distancia Manhattan)
     */
    private double heuristica(double x1, double y1, double x2, double y2) {
        return mapaConfig.calcularDistanciaReticular(x1, y1, x2, y2);
    }
    
    /**
     * Reconstruye el camino desde el mapa de padres
     */
    private List<double[]> reconstruirCamino(Map<String, String> caminoPadre, String nodoActual) {
        List<double[]> camino = new ArrayList<>();
        
        // Agregar el nodo final
        camino.add(keyACoordenada(nodoActual));
        
        // Reconstruir desde el final hasta el inicio
        while (caminoPadre.containsKey(nodoActual)) {
            nodoActual = caminoPadre.get(nodoActual);
            camino.add(0, keyACoordenada(nodoActual)); // Insertar al inicio
        }
        
        return camino;
    }
    
    /**
     * Convierte coordenadas (x,y) a un string clave
     */
    private String coordenadaAKey(double x, double y) {
        return x + "," + y;
    }
    
    /**
     * Convierte una clave string a coordenadas
     */
    private double[] keyACoordenada(String key) {
        String[] parts = key.split(",");
        return new double[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
    }
    
    /**
     * Calcula la longitud total de una ruta en km
     */
    public double calcularLongitudRuta(List<double[]> ruta) {
        // Si la ruta está vacía o tiene un solo punto, la longitud es 0
        if (ruta == null || ruta.size() <= 1) {
            return 0;
        }
        
        double longitud = 0;
        
        // Sumar las distancias entre nodos consecutivos
        for (int i = 0; i < ruta.size() - 1; i++) {
            double[] puntoActual = ruta.get(i);
            double[] puntoSiguiente = ruta.get(i + 1);
            
            longitud += mapaConfig.calcularDistanciaRealKm(
                puntoActual[0], puntoActual[1], 
                puntoSiguiente[0], puntoSiguiente[1]
            );
        }
        
        return longitud;
    }
    
    /**
     * Obtiene todos los bloqueos activos y los utiliza para calcular una ruta óptima
     */
    public List<double[]> calcularRutaOptimaConsiderandoBloqueos(double xInicio, double yInicio, double xFin, double yFin) {
        // Obtener bloqueos activos
        List<Bloqueo> bloqueosActivos = bloqueoService.obtenerBloqueosActivos(java.time.LocalDateTime.now());
        
        // Calcular ruta evitando los bloqueos
        return calcularRutaOptima(xInicio, yInicio, xFin, yFin, bloqueosActivos);
    }
    
    /**
     * Estima el tiempo de viaje en minutos para una ruta
     * @param ruta La ruta a evaluar
     * @param velocidadKmh Velocidad promedio en km/h
     * @return Tiempo estimado en minutos
     */
    public double estimarTiempoViajeMinutos(List<double[]> ruta, double velocidadKmh) {
        double longitudKm = calcularLongitudRuta(ruta);
        
        // Tiempo = distancia / velocidad (en horas)
        double tiempoHoras = longitudKm / velocidadKmh;
        
        // Convertir horas a minutos
        return tiempoHoras * 60;
    }
}
```

## main\java\com\plg\service\PedidoService.java

```java
package com.plg.service;

import com.plg.dto.PedidoDTO;
import com.plg.entity.Pedido;
import com.plg.entity.EstadoPedido;
import com.plg.repository.PedidoRepository;
import com.plg.util.DtoConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    public List<Pedido> findAll() {
        return pedidoRepository.findAll();
    }
    
    public List<PedidoDTO> findAllDTO() {
        return pedidoRepository.findAll().stream()
                .map(DtoConverter::toPedidoDTO)
                .collect(Collectors.toList());
    }

    public Optional<Pedido> findById(Long id) {
        return pedidoRepository.findById(id);
    }
    
    public PedidoDTO findByIdDTO(Long id) {
        return pedidoRepository.findById(id)
                .map(DtoConverter::toPedidoDTO)
                .orElse(null);
    }

    public Pedido save(PedidoDTO pedidoDTO) {
        // Usar el convertidor para crear una entidad Pedido desde el DTO
        Pedido pedido = DtoConverter.toPedido(pedidoDTO);
        
        // Si no se especificó un estado, establecerlo como REGISTRADO
        if (pedido.getEstado() == null) {
            pedido.setEstado(EstadoPedido.REGISTRADO);
        }
        
        return pedidoRepository.save(pedido);
    }

    public Pedido update(Long id, PedidoDTO pedidoDTO) {
        // Verificar si el pedido existe
        Pedido pedidoExistente = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        // Usar el convertidor para actualizar los campos del pedido existente
        Pedido pedidoActualizado = DtoConverter.toPedido(pedidoDTO);
        pedidoActualizado.setId(id); // Asegurar que el ID sea el correcto
        
        return pedidoRepository.save(pedidoActualizado);
    }

    public void delete(Long id) {
        pedidoRepository.deleteById(id);
    }
    
    /**
     * Encuentra pedidos por su estado usando valores enteros (para compatibilidad)
     */
    public List<Pedido> findByEstado(EstadoPedido estado) {
        return pedidoRepository.findByEstado(estado);
    }
    
    /**
     * Encuentra pedidos por su estado usando el enum (método preferido)
     */
    public List<Pedido> findByEstadoEnum(EstadoPedido estado) {
        return pedidoRepository.findByEstado(estado);
    }
    
    /**
     * Encuentra pedidos por su estado y los convierte a DTO (usando valores enteros)
     */
    public List<PedidoDTO> findByEstadoDTO(EstadoPedido estado) {
        return pedidoRepository.findByEstado(estado).stream()
                .map(DtoConverter::toPedidoDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Encuentra pedidos por su estado y los convierte a DTO (usando enum - método preferido)
     */
    public List<PedidoDTO> findByEstadoEnumDTO(EstadoPedido estado) {
        return pedidoRepository.findByEstado(estado).stream()
                .map(DtoConverter::toPedidoDTO)
                .collect(Collectors.toList());
    }
}
```

## main\java\com\plg\service\RutaService.java

```java
package com.plg.service;

import com.plg.config.MapaConfig;
import com.plg.entity.*;
import com.plg.entity.EstadoCamion;
import com.plg.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RutaService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private BloqueoRepository bloqueoRepository;
    
    @Autowired
    private RutaRepository rutaRepository;
    
    @Autowired
    private CamionRepository camionRepository;
    
    @Autowired
    private EntregaParcialRepository entregaParcialRepository;

    @Autowired
    private MapaConfig mapaConfig;
    
    @Autowired
    private MapaReticularService mapaReticularService;
    
    @Autowired
    private BloqueoService bloqueoService;
    //para obtener almacen principal
    @Autowired
    private AlmacenRepository almacenRepository;
    /**
     * Obtiene una ruta por su código
     */
    public Ruta findByCodigoRuta(String codigo) {
        return rutaRepository.findByCodigo(codigo)
            .orElseThrow(() -> new RuntimeException("Ruta no encontrada con código: " + codigo));
    }
    
    /**
     * Obtiene todas las rutas
     */
    public List<Ruta> getAllRutas() {
        return rutaRepository.findAll();
    }
    
    /**
     * Obtiene las rutas por estado
     */
    public List<Ruta> getRutasByEstado(int estado) {
        return rutaRepository.findByEstado(estado);
    }
    
    /**
     * Obtiene las rutas por camión
     */
    public List<Ruta> getRutasByCamion(String codigoCamion) {
        Camion camion = camionRepository.findByCodigo(codigoCamion)
            .orElseThrow(() -> new RuntimeException("Camión no encontrado con código: " + codigoCamion));
        return rutaRepository.findByCamion(camion);
    }
    
    /**
     * Crea una nueva ruta con información básica
     */
    @Transactional
    public Ruta crearRuta(String codigo, String codigoCamion, boolean consideraBloqueos) {
        // Verificar si ya existe una ruta con ese código
        if (rutaRepository.findByCodigo(codigo).isPresent()) {
            throw new RuntimeException("Ya existe una ruta con el código: " + codigo);
        }
        
        Ruta ruta = new Ruta(codigo);
        ruta.setConsideraBloqueos(consideraBloqueos);
        
        if (codigoCamion != null && !codigoCamion.isEmpty()) {
            Camion camion = camionRepository.findByCodigo(codigoCamion)
                .orElseThrow(() -> new RuntimeException("Camión no encontrado con código: " + codigoCamion));
            
            if (camion.getEstado() != EstadoCamion.DISPONIBLE) {
                throw new RuntimeException("El camión no está disponible. Estado actual: " + camion.getEstadoTexto());
            }
            
            ruta.setCamion(camion);
        }
        
        // Agregar nodo inicial (almacén)
        // Obtener coordenadas del almacén central
        Almacen almacenCentral = almacenRepository.findByEsCentralAndActivoTrue(true);
        if (almacenCentral == null) {
            throw new RuntimeException("No se encontró el almacén central");
        }
        // Obtener coordenadas del almacén central
        double x_almacenCentral = almacenCentral.getPosX();
        double y_almacenCentral = almacenCentral.getPosY(); 
        ruta.agregarNodo(x_almacenCentral, y_almacenCentral, "ALMACEN");
   
        return rutaRepository.save(ruta);
    }
    
    /**
     * Agrega un pedido a una ruta existente
     */
    @Transactional
    public Ruta agregarPedidoARuta(String codigoRuta, Long pedidoId, double volumenGLP, double porcentajePedido) {
        Ruta ruta = findByCodigoRuta(codigoRuta);
        Pedido pedido = pedidoRepository.findById(pedidoId)
            .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + pedidoId));
        
        // Verificar si el camión tiene capacidad
        if (ruta.getCamion() != null && !ruta.getCamion().tieneCapacidadPara(volumenGLP)) {
            throw new RuntimeException("El camión no tiene capacidad suficiente para este pedido");
        }
        
        // Verificar si la ruta está en un estado que permite modificación
        if (ruta.getEstado() != 0) { // Si no está "Planificada"
            throw new RuntimeException("No se puede modificar una ruta que ya está en curso o finalizada");
        }
        
        // Agregar nodo de cliente a la ruta
        ruta.agregarNodoCliente(pedido.getPosX(), pedido.getPosY(), pedido, volumenGLP, porcentajePedido);
        
        // Si hay camión asignado, asignar también el pedido al camión como entrega parcial
        if (ruta.getCamion() != null) {
            ruta.getCamion().asignarPedidoParcial(pedido, volumenGLP, porcentajePedido);
        }
        
        // Recalcular la distancia total
        ruta.calcularDistanciaTotal();
        
        // Si se considera bloqueos, verificar posibles bloqueos
        if (ruta.isConsideraBloqueos()) {
            List<Bloqueo> bloqueosActivos = bloqueoRepository.findByActivoTrue();
            ruta.verificarInterseccionConBloqueos(bloqueosActivos);
        }
        
        return rutaRepository.save(ruta);
    }
    
    /**
     * Asigna un camión a una ruta
     */
    @Transactional
    public Ruta asignarCamionARuta(String codigoRuta, String codigoCamion) {
        Ruta ruta = findByCodigoRuta(codigoRuta);
        Camion camion = camionRepository.findByCodigo(codigoCamion)
            .orElseThrow(() -> new RuntimeException("Camión no encontrado con código: " + codigoCamion));
        
        // Verificar si el camión está disponible
        if (camion.getEstado() != EstadoCamion.DISPONIBLE) {
            throw new RuntimeException("El camión no está disponible. Estado actual: " + camion.getEstadoTexto());
        }
        
        // Verificar si la ruta está en un estado que permite asignar camión
        if (ruta.getEstado() != 0) {
            throw new RuntimeException("No se puede asignar un camión a una ruta que ya está en curso o finalizada");
        }
        
        // Verificar si la capacidad del camión es suficiente
        if (!camion.tieneCapacidadPara(ruta.getVolumenTotalGLP())) {
            throw new RuntimeException("El camión no tiene capacidad suficiente para el volumen total de GLP de la ruta");
        }
        
        // Asignar el camión a la ruta
        ruta.setCamion(camion);
        
        // Asignar todos los pedidos de la ruta al camión como entregas parciales
        for (NodoRuta nodo : ruta.getNodos()) {
            if (nodo.getPedido() != null) {
                camion.asignarPedidoParcial(nodo.getPedido(), nodo.getVolumenGLP(), nodo.getPorcentajePedido());
            }
        }
        
        return rutaRepository.save(ruta);
    }
    
    /**
     * Iniciar una ruta
     */
    @Transactional
    public Ruta iniciarRuta(String codigoRuta) {
        Ruta ruta = findByCodigoRuta(codigoRuta);
        
        // Verificar que la ruta tenga un camión asignado
        if (ruta.getCamion() == null) {
            throw new RuntimeException("No se puede iniciar una ruta sin un camión asignado");
        }
        
        // Verificar que la ruta esté en estado "Planificada"
        if (ruta.getEstado() != 0) {
            throw new RuntimeException("La ruta no está en estado Planificada. Estado actual: " + ruta.getEstadoTexto());
        }
        
        // Verificar si la ruta tiene pedidos
        if (ruta.getNodos().stream().noneMatch(n -> n.getPedido() != null)) {
            throw new RuntimeException("La ruta no tiene pedidos asignados");
        }
        
        // Verificar si hay bloqueos activos que afecten la ruta
        if (ruta.isConsideraBloqueos()) {
            List<Bloqueo> bloqueosActivos = bloqueoRepository.findByActivoTrue();
            if (ruta.tieneBloqueoActivo(bloqueosActivos)) {
                throw new RuntimeException("La ruta tiene bloqueos activos en este momento. Revise el mapa o reprograme la ruta.");
            }
        }
        
        // Iniciar la ruta
        ruta.iniciarRuta();
        
        // Estimar tiempos de llegada
        ruta.estimarTiemposLlegada(ruta.getCamion().getVelocidadPromedio(), LocalDateTime.now());
        
        return rutaRepository.save(ruta);
    }
    
    /**
     * Marcar entrega de pedido en ruta
     */
    @Transactional
    public Ruta marcarEntregaPedido(String codigoRuta, Long pedidoId, String observaciones) {
        Ruta ruta = findByCodigoRuta(codigoRuta);
        
        // Verificar que la ruta esté en curso
        if (ruta.getEstado() != 1) {
            throw new RuntimeException("La ruta no está en curso. Estado actual: " + ruta.getEstadoTexto());
        }
        
        // Marcar el pedido como entregado
        boolean entregaExitosa = ruta.marcarPedidoComoEntregado(pedidoId, LocalDateTime.now(), observaciones);
        
        if (!entregaExitosa) {
            throw new RuntimeException("El pedido no se encuentra en esta ruta o ya ha sido entregado");
        }
        
        // Verificar si todos los pedidos han sido entregados para completar la ruta
        if (ruta.getEntregasPendientes().isEmpty()) {
            ruta.completarRuta();
        }
        
        return rutaRepository.save(ruta);
    }
    
    /**
     * Completar una ruta
     */
    @Transactional
    public Ruta completarRuta(String codigoRuta) {
        Ruta ruta = findByCodigoRuta(codigoRuta);
        
        // Verificar que la ruta esté en curso
        if (ruta.getEstado() != 1) {
            throw new RuntimeException("La ruta no está en curso. Estado actual: " + ruta.getEstadoTexto());
        }
        
        // Completar la ruta
        ruta.completarRuta();
        
        return rutaRepository.save(ruta);
    }
    
    /**
     * Cancelar una ruta
     */
    @Transactional
    public Ruta cancelarRuta(String codigoRuta, String motivo) {
        Ruta ruta = findByCodigoRuta(codigoRuta);
        
        // No se puede cancelar una ruta que ya está completada
        if (ruta.getEstado() == 2) {
            throw new RuntimeException("No se puede cancelar una ruta que ya está completada");
        }
        
        // Cancelar la ruta
        ruta.cancelarRuta(motivo);
        
        return rutaRepository.save(ruta);
    }
    
    /**
     * Optimiza una ruta considerando bloqueos si se especifica
     */
    public Map<String, Object> optimizarRuta(String idRuta, boolean considerarBloqueos) {
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("idRuta", idRuta);
        resultado.put("optimizada", true);
        resultado.put("consideraBloqueos", considerarBloqueos);
        
        // Obtener los pedidos asociados a la ruta
        List<Pedido> pedidosRuta = pedidoRepository.findByCodigoRuta(idRuta);
        
        // Si no hay pedidos, devolver una ruta vacía
        if (pedidosRuta.isEmpty()) {
            resultado.put("puntos", new ArrayList<>());
            resultado.put("distanciaTotal", 0.0);
            resultado.put("tiempoEstimado", 0);
            return resultado;
        }
        
        // Punto de inicio: almacén central
      
        //Obtener coordenadas de almacen central
        Almacen almacenCentral = almacenRepository.findByEsCentralAndActivoTrue(true);
        if (almacenCentral == null) {
            throw new RuntimeException("No hay almacén central activo configurado");
        }
        double xAlmacenCentral = almacenCentral.getPosX();
        double yAlmacenCentral = almacenCentral.getPosY();

        double xInicio = xAlmacenCentral;
        double yInicio = yAlmacenCentral;
 
        // Inicializamos la lista de puntos de la ruta con el almacén
        List<Map<String, Object>> puntosRuta = new ArrayList<>();
        puntosRuta.add(createPunto(xInicio, yInicio, "ALMACEN"));
        
        // Si hay que considerar bloqueos, usamos el servicio de mapa reticular
        List<Bloqueo> bloqueosActivos = new ArrayList<>();
        if (considerarBloqueos) {
            bloqueosActivos = bloqueoRepository.findByActivoTrue();
        }
        
        double xActual = xInicio;
        double yActual = yInicio;
        double distanciaTotal = 0;
        
        // Recorremos todos los pedidos añadiendo rutas óptimas entre ellos
        for (Pedido pedido : pedidosRuta) {
            List<double[]> rutaSegmento;
            
            if (considerarBloqueos && !bloqueosActivos.isEmpty()) {
                // Usar el servicio de mapa reticular para encontrar ruta óptima evitando bloqueos
                rutaSegmento = mapaReticularService.calcularRutaOptima(
                    (int)xActual, (int)yActual,
                    (int)pedido.getPosX(), (int)pedido.getPosY(),
                    bloqueosActivos);
            } else {
                // Si no hay bloqueos, usar ruta reticular directa (no diagonal)
                rutaSegmento = calcularRutaDirectaReticular(xActual, yActual, pedido.getPosX(), pedido.getPosY());
            }
            
            // Si no se encontró una ruta válida, seguir con el siguiente pedido
            if (rutaSegmento.isEmpty()) {
                continue;
            }
            
            // Añadir todos los puntos de la ruta excepto el primero (ya está incluido)
            for (int i = 1; i < rutaSegmento.size(); i++) {
                double[] punto = rutaSegmento.get(i);
                
                // Calcular distancia desde el punto anterior
                double[] puntoAnterior = rutaSegmento.get(i-1);
                // En un mapa reticular, la distancia es Manhattan (suma de diferencias absolutas)
                double distanciaSegmento = Math.abs(punto[0] - puntoAnterior[0]) + Math.abs(punto[1] - puntoAnterior[1]);
                distanciaTotal += distanciaSegmento;
                
                // Si este es el punto final, marcarlo como cliente
                String tipo = i == rutaSegmento.size() - 1 ? "CLIENTE" : "NODO";
                puntosRuta.add(createPunto(punto[0], punto[1], tipo));
            }
            
            // Actualizar posición actual
            double[] ultimoPunto = rutaSegmento.get(rutaSegmento.size() - 1);
            xActual = ultimoPunto[0];
            yActual = ultimoPunto[1];
        }
        
        // Añadir ruta de regreso al almacén
        List<double[]> rutaRegreso;
        if (considerarBloqueos && !bloqueosActivos.isEmpty()) {
            rutaRegreso = mapaReticularService.calcularRutaOptima(
                (int)xActual, (int)yActual, 
                (int)xInicio, (int)yInicio,
                bloqueosActivos);
        } else {
            rutaRegreso = calcularRutaDirectaReticular(xActual, yActual, xInicio, yInicio);
        }
        
        // Si se encontró una ruta de regreso, añadirla
        if (!rutaRegreso.isEmpty()) {
            // Añadir todos los puntos de la ruta excepto el primero (ya está incluido)
            for (int i = 1; i < rutaRegreso.size(); i++) {
                double[] punto = rutaRegreso.get(i);
                
                // Calcular distancia desde el punto anterior
                double[] puntoAnterior = rutaRegreso.get(i-1);
                // En un mapa reticular, la distancia es Manhattan (suma de diferencias absolutas)
                double distanciaSegmento = Math.abs(punto[0] - puntoAnterior[0]) + Math.abs(punto[1] - puntoAnterior[1]);
                distanciaTotal += distanciaSegmento;
                
                // Si este es el último punto, marcarlo como almacén de regreso
                String tipo = i == rutaRegreso.size() - 1 ? "ALMACEN" : "NODO";
                puntosRuta.add(createPunto(punto[0], punto[1], tipo));
            }
        }
        
        // Estimar tiempo de viaje (asumiendo velocidad promedio de 50 km/h)
        double tiempoHoras = distanciaTotal / 50.0;
        int tiempoMinutos = (int) Math.round(tiempoHoras * 60);
        
        resultado.put("puntos", puntosRuta);
        resultado.put("distanciaTotal", Math.round(distanciaTotal * 100) / 100.0);
        resultado.put("tiempoEstimado", tiempoMinutos);
        
        return resultado;
    }
    
    /**
     * Actualiza la ruta existente con una ruta optimizada
     */
    @Transactional
    public Ruta actualizarRutaConOptimizacion(String codigoRuta, List<Map<String, Object>> puntosOptimizados) {
        Ruta ruta = findByCodigoRuta(codigoRuta);
        
        // Verificar que la ruta esté en estado Planificada
        if (ruta.getEstado() != 0) {
            throw new RuntimeException("No se puede modificar una ruta que ya está en curso o finalizada");
        }
        
        // Preservar información de pedidos
        Map<Long, NodoRuta> nodosCliente = ruta.getNodos().stream()
            .filter(n -> n.getPedido() != null)
            .collect(Collectors.toMap(n -> n.getPedido().getId(), n -> n));
        
        // Limpiar nodos existentes
        ruta.getNodos().clear();
        
        // Agregar nuevos nodos basados en la ruta optimizada
        for (Map<String, Object> punto : puntosOptimizados) {
            int x = (int) punto.get("x");
            int y = (int) punto.get("y");
            String tipo = (String) punto.get("tipo");
            
            if (tipo.startsWith("CLIENTE_")) {
                // Es un nodo de cliente
                String pedidoIdStr = tipo.substring("CLIENTE_".length());
                Long pedidoId = Long.parseLong(pedidoIdStr);
                
                if (nodosCliente.containsKey(pedidoId)) {
                    // Recuperar la información original del pedido
                    NodoRuta nodoOriginal = nodosCliente.get(pedidoId);
                    ruta.agregarNodoCliente(x, y, nodoOriginal.getPedido(), 
                        nodoOriginal.getVolumenGLP(), nodoOriginal.getPorcentajePedido());
                }
            } else {
                // Es un nodo de tipo ALMACEN o RUTA
                ruta.agregarNodo(x, y, tipo);
            }
        }
        
        // Recalcular distancia total
        ruta.calcularDistanciaTotal();
        
        // Verificar bloqueos si es necesario
        if (ruta.isConsideraBloqueos()) {
            List<Bloqueo> bloqueosActivos = bloqueoRepository.findByActivoTrue();
            ruta.verificarInterseccionConBloqueos(bloqueosActivos);
        }
        
        return rutaRepository.save(ruta);
    }
    
    /**
     * Calcula una ruta directa en el mapa reticular, moviéndose primero horizontal
     * y luego verticalmente entre dos puntos
     */
    private List<double[]> calcularRutaDirectaReticular(double x1, double y1, double x2, double y2) {
        List<double[]> ruta = new ArrayList<>();
        
        // Añadir punto de inicio
        ruta.add(new double[]{x1, y1});
        
        // Moverse horizontalmente primero
        if (x1 != x2) {
            for (double x = x1 + (x2 > x1 ? 1 : -1); x2 > x1 ? x <= x2 : x >= x2; x += (x2 > x1 ? 1 : -1)) {
                ruta.add(new double[]{x, y1});
            }
        }
        
        // Luego moverse verticalmente
        double xFinal = ruta.get(ruta.size() - 1)[0];
        if (y1 != y2) {
            for (double y = y1 + (y2 > y1 ? 1 : -1); y2 > y1 ? y <= y2 : y >= y2; y += (y2 > y1 ? 1 : -1)) {
                if (y != y1) { // Evitar duplicar el punto inicial
                    ruta.add(new double[]{xFinal, y});
                }
            }
        }
        
        return ruta;
    }
    
    /**
     * Calcula la distancia entre dos puntos (física, no reticular)
     */
    public double calcularDistancia(int x1, int y1, int x2, int y2) {
        // Utilizamos la distancia euclidiana para distancias físicas
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }
    
    /**
     * Crea un objeto punto para la respuesta JSON
     */
    private Map<String, Object> createPunto(double x, double y, String tipo) {
        Map<String, Object> punto = new HashMap<>();
        punto.put("x", x);
        punto.put("y", y);
        punto.put("tipo", tipo);
        return punto;
    }
    
    /**
     * Verifica si una ruta entre dos puntos está bloqueada
     */
    public boolean estaRutaBloqueada(int x1, int y1, int x2, int y2, List<Bloqueo> bloqueos) {
        // En un mapa reticular, debemos verificar cada segmento del recorrido
        
        // Si los puntos no están alineados horizontal o verticalmente,
        // calculamos una ruta reticular entre ellos
        if (x1 != x2 && y1 != y2) {
            List<double[]> ruta = calcularRutaDirectaReticular(x1, y1, x2, y2);
            
            // Verificamos cada segmento de la ruta
            for (int i = 0; i < ruta.size() - 1; i++) {
                double[] p1 = ruta.get(i);
                double[] p2 = ruta.get(i + 1);
                
                if (estaSegmentoBloqueado(p1[0], p1[1], p2[0], p2[1], bloqueos)) {
                    return true;
                }
            }
            
            return false;
        } else {
            // Si los puntos están alineados, verificamos directamente
            return estaSegmentoBloqueado(x1, y1, x2, y2, bloqueos);
        }
    }
    
    /**
     * Verifica si un segmento específico está bloqueado
     * Este método asume que el segmento es horizontal o vertical
     */
    public boolean estaSegmentoBloqueado(double x1, double y1, double x2, double y2, List<Bloqueo> bloqueos) {
        // Validar que el segmento es horizontal o vertical
        if (x1 != x2 && y1 != y2) {
            throw new IllegalArgumentException("El segmento debe ser horizontal o vertical en un mapa reticular");
        }
        
        for (Bloqueo bloqueo : bloqueos) {
            if (bloqueo.isActivo()) {
                try {
                    if (bloqueo.intersectaConSegmento(x1, y1, x2, y2)) {
                        return true;
                    }
                } catch (Exception e) {
                    // Usar método alternativo si hay error
                    // Verificar intersección con cada tramo del bloqueo
                    List<Bloqueo.Coordenada> coordenadas = bloqueo.getCoordenadas();
                    
                    if (coordenadas.size() < 2) continue;
                    
                    for (int i = 0; i < coordenadas.size() - 1; i++) {
                        Bloqueo.Coordenada c1 = coordenadas.get(i);
                        Bloqueo.Coordenada c2 = coordenadas.get(i + 1);
                        
                        // En mapa reticular, verificamos la superposición de segmentos
                        if (intersectaSegmentosReticulares(x1, y1, x2, y2, c1.getX(), c1.getY(), c2.getX(), c2.getY())) {
                            return true;
                        }
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Verifica si dos segmentos rectilíneos (horizontales o verticales) se intersectan
     */
    private boolean intersectaSegmentosReticulares(double x1, double y1, double x2, double y2, 
    double x3, double y3, double x4, double y4) {
        // En un mapa reticular, los segmentos son horizontales o verticales
        
        // Segmento horizontal intersecta con segmento vertical
        if (x1 == x2 && y3 == y4) { // Seg1 vertical, Seg2 horizontal
            return estaPuntoEnSegmento(x1, y3, x1, y1, x1, y2) && 
                   estaPuntoEnSegmento(x1, y3, x3, y3, x4, y3);
        } 
        else if (y1 == y2 && x3 == x4) { // Seg1 horizontal, Seg2 vertical
            return estaPuntoEnSegmento(x3, y1, x1, y1, x2, y1) && 
                   estaPuntoEnSegmento(x3, y1, x3, y3, x3, y4);
        }
        // Segmentos paralelos (ambos horizontales o ambos verticales)
        else if (x1 == x2 && x3 == x4) { // Ambos verticales
            return x1 == x3 && hayOverlapEnRango(y1, y2, y3, y4);
        }
        else if (y1 == y2 && y3 == y4) { // Ambos horizontales
            return y1 == y3 && hayOverlapEnRango(x1, x2, x3, x4);
        }
        
        return false;
    }
    
    /**
     * Verifica si un punto está dentro de un segmento
     */
    private boolean estaPuntoEnSegmento(double x, double y, double x1, double y1, double x2, double y2) {
        return x >= Math.min(x1, x2) && x <= Math.max(x1, x2) && 
               y >= Math.min(y1, y2) && y <= Math.max(y1, y2);
    }
    
    /**
     * Verifica si hay solapamiento entre dos rangos
     */
    private boolean hayOverlapEnRango(double a1, double a2, double b1, double b2) {
        return Math.max(a1, a2) >= Math.min(b1, b2) && 
               Math.min(a1, a2) <= Math.max(b1, b2);
    }
    
    /**
     * Verifica si hay rutas alternativas disponibles entre dos puntos
     * cuando la ruta directa está bloqueada
     */
    public boolean hayRutaAlternativa(int x1, int y1, int x2, int y2) {
        List<Bloqueo> bloqueosActivos = bloqueoRepository.findByActivoTrue();
        
        // Si la ruta directa no está bloqueada, no necesitamos alternativa
        if (!estaRutaBloqueada(x1, y1, x2, y2, bloqueosActivos)) {
            return true;
        }
        
        // Usar el servicio de mapa reticular para buscar ruta alternativa
        List<double[]> rutaAlternativa = mapaReticularService.calcularRutaOptima(
            x1, y1, x2, y2, bloqueosActivos);
        
        // Si encontramos una ruta válida, hay alternativa
        return !rutaAlternativa.isEmpty();
    }
    
    /**
     * Obtiene todas las rutas bloqueadas actualmente en el mapa
     */
    public List<Map<String, Object>> obtenerRutasBloqueadas() {
        List<Map<String, Object>> rutasBloqueadas = new ArrayList<>();
        List<Bloqueo> bloqueosActivos = bloqueoRepository.findByActivoTrue();
        
        for (Bloqueo bloqueo : bloqueosActivos) {
            if (bloqueo.getCoordenadas().size() < 2) continue;
            
            for (int i = 0; i < bloqueo.getCoordenadas().size() - 1; i++) {
                Map<String, Object> segmento = new HashMap<>();
                Bloqueo.Coordenada c1 = bloqueo.getCoordenadas().get(i);
                Bloqueo.Coordenada c2 = bloqueo.getCoordenadas().get(i + 1);
                
                segmento.put("x1", c1.getX());
                segmento.put("y1", c1.getY());
                segmento.put("x2", c2.getX());
                segmento.put("y2", c2.getY());
                segmento.put("descripcion", bloqueo.getDescripcion());
                segmento.put("fechaInicio", bloqueo.getFechaInicio().toString());
                segmento.put("fechaFin", bloqueo.getFechaFin().toString());
                
                rutasBloqueadas.add(segmento);
            }
        }
        
        return rutasBloqueadas;
    }
    
    /**
     * Obtiene un resumen de todas las rutas
     */
    public List<Map<String, Object>> obtenerResumeneRutas() {
        List<Ruta> rutas = rutaRepository.findAll();
        
        return rutas.stream()
            .map(Ruta::getResumenRuta)
            .collect(Collectors.toList());
    }
    
    /**
     * Verifica si alguna ruta activa pasa por ciertos tramos bloqueados
     */
    public List<Map<String, Object>> verificarRutasAfectadasPorBloqueos(List<Bloqueo> nuevosBloqueos) {
        List<Map<String, Object>> rutasAfectadas = new ArrayList<>();
        
        // Obtener solo rutas planificadas o en curso
        List<Ruta> rutasActivas = rutaRepository.findByEstadoIn(List.of(0, 1));
        
        for (Ruta ruta : rutasActivas) {
            if (ruta.isConsideraBloqueos() && ruta.verificarInterseccionConBloqueos(nuevosBloqueos)) {
                Map<String, Object> info = new HashMap<>();
                info.put("rutaId", ruta.getId());
                info.put("codigo", ruta.getCodigo());
                info.put("estado", ruta.getEstado());
                info.put("estadoTexto", ruta.getEstadoTexto());
                
                if (ruta.getCamion() != null) {
                    info.put("camionCodigo", ruta.getCamion().getCodigo());
                }
                
                rutasAfectadas.add(info);
            }
        }
        
        return rutasAfectadas;
    }
}
```

## main\java\com\plg\service\SimulacionService.java

```java
package com.plg.service;

import com.plg.entity.Almacen;
import com.plg.entity.Averia;
import com.plg.entity.Bloqueo;
import com.plg.entity.Camion;
import com.plg.entity.Mantenimiento;
import com.plg.entity.Pedido;
import com.plg.entity.EstadoCamion;
import com.plg.entity.EstadoPedido;
import com.plg.repository.AlmacenRepository;
import com.plg.repository.AveriaRepository;
import com.plg.repository.BloqueoRepository;
import com.plg.repository.CamionRepository;
import com.plg.repository.MantenimientoRepository;
import com.plg.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;

@Service
public class SimulacionService {

    @Autowired
    private PedidoRepository pedidoRepository;
    
    @Autowired
    private CamionRepository camionRepository;
    
    @Autowired
    private RutaService rutaService;

    @Autowired
    private BloqueoRepository bloqueoRepository;
    
    @Autowired
    private AveriaRepository averiaRepository;
    
    @Autowired
    private AlmacenRepository almacenRepository;
    
    @Autowired
    private MantenimientoRepository mantenimientoRepository;

    @Autowired
    private BloqueoService bloqueoService;
    /**
     * Simula un escenario diario con pedidos, rutas y posibles averías
     */
    public Map<String, Object> simularEscenarioDiario() {
        Map<String, Object> resultado = new HashMap<>();
        
        // 1. Generamos pedidos aleatorios para el día
        List<Pedido> pedidosGenerados = generarPedidosAleatorios(15, 30); // Entre 15 y 30 pedidos
        resultado.put("pedidosGenerados", pedidosGenerados.size());
        
        // 2. Asignamos camiones y simulamos rutas
        Map<String, Object> asignaciones = asignarCamionesYRutas(pedidosGenerados);
        resultado.put("camionesAsignados", asignaciones.get("camionesAsignados"));
        resultado.put("rutasGeneradas", asignaciones.get("rutas"));
        
        // 3. Simular posibles eventos aleatorios (averías, bloqueos)
        List<Object> eventos = simularEventosAleatorios();
        resultado.put("eventosGenerados", eventos);
        
        // 4. Calcular métricas de la simulación
        Map<String, Object> metricas = calcularMetricasSimulacion();
        resultado.put("metricas", metricas);
        
        return resultado;
    }
    
    /**
     * Simula un escenario semanal (múltiples días)
     */
    public Map<String, Object> simularEscenarioSemanal(int dias) {
        Map<String, Object> resultado = new HashMap<>();
        List<Map<String, Object>> resultadosDiarios = new ArrayList<>();
        
        // Simulamos cada día
        for (int i = 0; i < dias; i++) {
            Map<String, Object> resultadoDia = simularEscenarioDiario();
            resultadoDia.put("dia", i + 1);
            resultadosDiarios.add(resultadoDia);
        }
        
        resultado.put("resultadosDiarios", resultadosDiarios);
        
        // Métricas acumuladas
        int totalPedidos = 0;
        int totalCamiones = 0;
        int totalEventos = 0;
        
        for (Map<String, Object> dia : resultadosDiarios) {
            totalPedidos += (int) dia.get("pedidosGenerados");
            totalCamiones += ((List<?>) dia.get("camionesAsignados")).size();
            totalEventos += ((List<?>) dia.get("eventosGenerados")).size();
        }
        
        Map<String, Object> metricasAcumuladas = new HashMap<>();
        metricasAcumuladas.put("totalPedidos", totalPedidos);
        metricasAcumuladas.put("totalCamionesUsados", totalCamiones);
        metricasAcumuladas.put("totalEventos", totalEventos);
        metricasAcumuladas.put("diasSimulados", dias);
        
        resultado.put("metricasAcumuladas", metricasAcumuladas);
        
        return resultado;
    }
    
    /**
     * Simula un escenario de colapso (muchos pedidos, varias averías)
     */
    public Map<String, Object> simularEscenarioColapso() {
        Map<String, Object> resultado = new HashMap<>();
        
        // 1. Generamos un alto número de pedidos
        List<Pedido> pedidosGenerados = generarPedidosAleatorios(60, 80); // Muchos pedidos
        resultado.put("pedidosGenerados", pedidosGenerados.size());
        
        // 2. Asignamos camiones, pero algunos tendrán averías
        List<Camion> camionesDisponibles = (List<Camion>) camionRepository.findAll();
        
        // 3. Provocar averías en el 30% de los camiones
        int numAverias = Math.max(1, camionesDisponibles.size() * 30 / 100);
        List<Averia> averias = generarAveriasAleatorias(numAverias);
        resultado.put("averiasGeneradas", averias);
        
        // 4. Generar varios bloqueos para complicar las rutas
        List<Bloqueo> bloqueos = generarBloqueosAleatorios(3, 5);
        resultado.put("bloqueosGenerados", bloqueos);
        
        // 5. Asignamos camiones disponibles y rutas
        Map<String, Object> asignaciones = asignarCamionesYRutas(pedidosGenerados);
        resultado.put("camionesAsignados", asignaciones.get("camionesAsignados"));
        resultado.put("rutasGeneradas", asignaciones.get("rutas"));
        
        // 6. Calcular indicadores de colapso
        Map<String, Object> indicadoresColapso = new HashMap<>();
        indicadoresColapso.put("nivelSaturacion", calcularNivelSaturacion(pedidosGenerados, camionesDisponibles));
        indicadoresColapso.put("tiempoEstimadoNormalizacion", estimarTiempoNormalizacion());
        indicadoresColapso.put("pedidosNoAtendibles", estimarPedidosNoAtendibles(pedidosGenerados, camionesDisponibles));
        
        resultado.put("indicadoresColapso", indicadoresColapso);
        
        return resultado;
    }
    
    // ---------- Métodos auxiliares para simulación ----------
    
    private List<Pedido> generarPedidosAleatorios(int min, int max) {
        // Simulación - en un caso real se cargarían de la BD o archivos
        int cantidad = ThreadLocalRandom.current().nextInt(min, max + 1);
        List<Pedido> pedidos = new ArrayList<>();
        
        // Generamos pedidos simulados
        for (int i = 0; i < cantidad; i++) {
            Pedido pedido = new Pedido();
            pedido.setId(Long.valueOf(i));
            pedido.setPosX(ThreadLocalRandom.current().nextInt(0, 100));
            pedido.setPosY(ThreadLocalRandom.current().nextInt(0, 100));
            pedido.setVolumenGLPAsignado(ThreadLocalRandom.current().nextInt(5, 20));
            pedido.setHorasLimite(ThreadLocalRandom.current().nextInt(2, 24));
            pedido.setEstado(EstadoPedido.PENDIENTE_PLANIFICACION); // Pendiente
            pedido.setFechaHora("11d13h" + ThreadLocalRandom.current().nextInt(0, 60) + "m");
            
            pedidos.add(pedido);
        }
        
        return pedidos;
    }
    
    private Map<String, Object> asignarCamionesYRutas(List<Pedido> pedidos) {
        Map<String, Object> resultado = new HashMap<>();
        
        // Simulación de asignación de camiones y rutas
        List<Map<String, Object>> camionesAsignados = new ArrayList<>();
        List<Map<String, Object>> rutas = new ArrayList<>();
        
        // Simulación simplificada
        int numRutas = Math.min(5, pedidos.size() / 5 + 1); // Aproximadamente 5 pedidos por ruta
        
        // Dividimos pedidos en rutas
        for (int i = 0; i < numRutas; i++) {
            // Crear ruta
            Map<String, Object> ruta = new HashMap<>();
            ruta.put("idRuta", "R" + (i + 1));
            ruta.put("distanciaTotal", 100.0 + (50 * Math.random())); // Simulado
            ruta.put("tiempoEstimado", 120 + (i * 30)); // Minutos
            
            // Asignar pedidos a la ruta
            List<Map<String, Object>> pedidosRuta = new ArrayList<>();
            for (int j = i * (pedidos.size() / numRutas); 
                 j < (i + 1) * (pedidos.size() / numRutas) && j < pedidos.size(); 
                 j++) {
                pedidosRuta.add(convertirPedidoAMapa(pedidos.get(j)));
            }
            ruta.put("pedidos", pedidosRuta);
            ruta.put("numeroPedidos", pedidosRuta.size());
            
            // Asignar camión
            Map<String, Object> camion = new HashMap<>();
            camion.put("codigo", "T" + (i % 2 + 1) + "0" + (i + 1));
            camion.put("tipo", "T" + (i % 2 + 1));
            camion.put("capacidad", 20.0);
            
            camionesAsignados.add(camion);
            ruta.put("camion", camion.get("codigo"));
            
            rutas.add(ruta);
        }
        
        resultado.put("camionesAsignados", camionesAsignados);
        resultado.put("rutas", rutas);
        
        return resultado;
    }
    
    private List<Object> simularEventosAleatorios() {
        List<Object> eventos = new ArrayList<>();
        
        // Simulamos averías aleatorias (20% de probabilidad)
        if (Math.random() < 0.2) {
            List<Averia> averias = generarAveriasAleatorias(1);
            eventos.addAll(averias);
        }
        
        // Simulamos bloqueos aleatorios (10% de probabilidad)
        if (Math.random() < 0.1) {
            List<Bloqueo> bloqueos = generarBloqueosAleatorios(1, 1);
            eventos.addAll(bloqueos);
        }
        
        return eventos;
    }
    
    private List<Averia> generarAveriasAleatorias(int cantidad) {
        List<Averia> averias = new ArrayList<>();
        
        for (int i = 0; i < cantidad; i++) {
            Averia averia = new Averia();
            averia.setId(Long.valueOf(i));
            averia.setFechaHoraReporte(LocalDateTime.now());
            averia.setDescripcion("Avería simulada #" + i);
            
            // Usar tipoIncidente en lugar de severidad
            String[] tiposIncidente = {"TI1", "TI2", "TI3"};
            averia.setTipoIncidente(tiposIncidente[ThreadLocalRandom.current().nextInt(0, 3)]);
            
            // Asignar turno aleatorio
            String[] turnos = {"T1", "T2", "T3"};
            averia.setTurno(turnos[ThreadLocalRandom.current().nextInt(0, 3)]);
            
            averia.setPosX(ThreadLocalRandom.current().nextInt(0, 100));
            averia.setPosY(ThreadLocalRandom.current().nextInt(0, 100));
            averia.setEstado(0); // Reportada
            
            // Simulamos una referencia a un camión
            Camion camion = new Camion();
            camion.setCodigo("T" + ThreadLocalRandom.current().nextInt(1, 3) + "0" + ThreadLocalRandom.current().nextInt(1, 6));
            averia.setCamion(camion);
            
            // Establecer conCarga a true para simulación
            averia.setConCarga(true);
            
            // Calcular tiempos de inmovilización
            averia.calcularValidezAveria(true);
            averia.calcularKilometroOcurrencia(100.0); // Valor por defecto
            averia.calcularTiemposInoperatividad();
            
            averias.add(averia);
        }
        
        return averias;
    }
    
    private List<Bloqueo> generarBloqueosAleatorios(int min, int max) {
        int cantidad = ThreadLocalRandom.current().nextInt(min, max + 1);
        List<Bloqueo> bloqueos = new ArrayList<>();
        
        for (int i = 0; i < cantidad; i++) {
            Bloqueo bloqueo = new Bloqueo();
            bloqueo.setId(Long.valueOf(i));
            
            // Crear lista de coordenadas (polígono abierto)
            List<Bloqueo.Coordenada> coordenadas = new ArrayList<>();
            
            // Punto inicial del bloqueo
            int x1 = ThreadLocalRandom.current().nextInt(10, 90);
            int y1 = ThreadLocalRandom.current().nextInt(10, 90);
            coordenadas.add(new Bloqueo.Coordenada(x1, y1));
            
            // Agregar 1-3 puntos más para formar un polígono abierto
            int numPuntos = ThreadLocalRandom.current().nextInt(1, 4);
            int xActual = x1;
            int yActual = y1;
            
            for (int j = 0; j < numPuntos; j++) {
                // Generar un punto cercano al anterior (máximo 10 unidades de distancia)
                int xNuevo = xActual + ThreadLocalRandom.current().nextInt(-10, 11);
                int yNuevo = yActual + ThreadLocalRandom.current().nextInt(-10, 11);
                
                // Mantener los puntos dentro de los límites del mapa
                xNuevo = Math.max(0, Math.min(xNuevo, 100));
                yNuevo = Math.max(0, Math.min(yNuevo, 100));
                
                coordenadas.add(new Bloqueo.Coordenada(xNuevo, yNuevo));
                
                xActual = xNuevo;
                yActual = yNuevo;
            }
            
            bloqueo.setCoordenadas(coordenadas);
            
            // Fechas y horas (formato correcto para el nuevo modelo)
            LocalDateTime ahora = LocalDateTime.now();
            
            // El bloqueo inicia entre ahora y 3 días después
            LocalDateTime fechaInicio = ahora.plusMinutes(ThreadLocalRandom.current().nextInt(0, 72 * 60));
            
            // El bloqueo dura entre 4 horas y 7 días
            LocalDateTime fechaFin = fechaInicio.plusMinutes(
                ThreadLocalRandom.current().nextInt(4 * 60, 7 * 24 * 60)
            );
            
            bloqueo.setFechaInicio(fechaInicio);
            bloqueo.setFechaFin(fechaFin);
            
            bloqueo.setDescripcion("Bloqueo simulado #" + i);
            
            // Un bloqueo está activo si ya comenzó pero aún no ha terminado
            bloqueo.setActivo(ahora.isAfter(fechaInicio) && ahora.isBefore(fechaFin));
            
            bloqueos.add(bloqueo);
        }
        
        return bloqueos;
    }
    
    private Map<String, Object> calcularMetricasSimulacion() {
        Map<String, Object> metricas = new HashMap<>();
        
        // Simulación de métricas
        metricas.put("eficienciaRutas", 80 + (Math.random() * 15)); // 80-95%
        metricas.put("utilizacionCamiones", 75 + (Math.random() * 20)); // 75-95%
        metricas.put("tiempoPromedioEntrega", 90 + (Math.random() * 60)); // 90-150 min
        metricas.put("costoCombustible", 100 + (Math.random() * 50)); // 100-150 unidades
        
        return metricas;
    }
    
    private double calcularNivelSaturacion(List<Pedido> pedidos, List<Camion> camiones) {
        // Simulación - en un caso real dependería de la capacidad real vs demanda
        int capacidadTotal = 0;
        for (Camion camion : camiones) {
            if (camion.getEstado() == EstadoCamion.DISPONIBLE) { // Disponible
                capacidadTotal += camion.getCapacidad();
            }
        }
        
        int demandaTotal = 0;
        for (Pedido pedido : pedidos) {
            demandaTotal += pedido.getVolumenGLPAsignado();
        }
        
        // Si la demanda supera la capacidad, hay saturación
        return Math.min(200, demandaTotal * 100.0 / (capacidadTotal > 0 ? capacidadTotal : 1));
    }
    
    private int estimarTiempoNormalizacion() {
        // Simulación - en horas
        return ThreadLocalRandom.current().nextInt(24, 73); // 1-3 días
    }
    
    private int estimarPedidosNoAtendibles(List<Pedido> pedidos, List<Camion> camiones) {
        // Simulación
        int capacidadTotal = 0;
        for (Camion camion : camiones) {
            if (camion.getEstado() == EstadoCamion.DISPONIBLE) { // Disponible
                capacidadTotal += camion.getCapacidad();
            }
        }
        
        int demandaTotal = 0;
        for (Pedido pedido : pedidos) {
            demandaTotal += pedido.getVolumenGLPAsignado();
        }
        
        // Estimamos cuántos pedidos no se podrán atender
        if (capacidadTotal >= demandaTotal) return 0;
        
        // Asumimos pedido promedio de 10 m3
        return (demandaTotal - capacidadTotal) / 10;
    }
    
    // Método auxiliar para convertir pedidos a map
    private Map<String, Object> convertirPedidoAMapa(Pedido pedido) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", pedido.getId());
        map.put("posX", pedido.getPosX());
        map.put("posY", pedido.getPosY());
        map.put("m3", pedido.getVolumenGLPAsignado());
        map.put("horasLimite", pedido.getHorasLimite());
        map.put("fechaHora", pedido.getFechaHora());
        map.put("estado", pedido.getEstado());
        return map;
    }

    /**
     * Inicializa la simulación (estado inicial)
     */
    public void inicializarSimulacion() {
        // Reiniciar estado de camiones, pedidos, etc.
        // Similar a lo que ya haces al inicio del método simularEscenarioDiario
    }

    /**
     * Ejecuta un paso de la simulación con el tiempo proporcionado
     */
    public Map<String, Object> ejecutarPasoSimulacion(LocalDateTime tiempo) {
        Map<String, Object> resultado = new HashMap<>();
        
        // Procesar eventos programados para este tiempo
        procesarEventos(tiempo);
        
        // Procesar movimientos de camiones
        actualizarPosicionCamiones(tiempo);
        
        // Procesar entregas de pedidos
        procesarEntregas(tiempo);
        
        // Verificar niveles de combustible
        verificarCombustible(tiempo);
        
        // Recopilar estadísticas
        Map<String, Object> estadisticas = recopilarEstadisticas(tiempo);
        resultado.put("estadisticas", estadisticas);
        resultado.put("tiempo", tiempo);
        
        return resultado;
    }

    // Implementa los métodos auxiliares (procesarEventos, actualizarPosicionCamiones, etc.)
        /**
     * Procesa los eventos programados para el tiempo específico
     * (mantenimientos, reabastecimientos, etc.)
     */
    private void procesarEventos(LocalDateTime tiempo) {
        // Verificar mantenimientos programados
        verificarMantenimientosProgramados(tiempo);
        
        // Verificar bloqueos vigentes
        actualizarBloqueos(tiempo);
        
        // Verificar y procesar nuevos pedidos para este tiempo
        procesarNuevosPedidos(tiempo);
        
        // Verificar y procesar eventos aleatorios (averías con cierta probabilidad)
        procesarEventosAleatorios(tiempo);
    }
    
    /**
     * Verifica si hay mantenimientos programados para el tiempo actual
     */
    private void verificarMantenimientosProgramados(LocalDateTime tiempo) {
        LocalDate fechaActual = tiempo.toLocalDate();
        
        // Buscar mantenimientos programados para hoy
        List<Mantenimiento> mantenimientosDia = mantenimientoRepository.findByFechaInicio(fechaActual);
        
        for (Mantenimiento mantenimiento : mantenimientosDia) {
            Camion camion = mantenimiento.getCamion();
            
            // Si el camión está disponible (no en ruta), enviarlo a mantenimiento
            if (camion.getEstado() == EstadoCamion.DISPONIBLE) { // Disponible
                camion.setEstado(EstadoCamion.EN_MANTENIMIENTO_PREVENTIVO); // En mantenimiento
                camionRepository.save(camion);
                
                // Actualizar estado del mantenimiento
                mantenimiento.setEstado(1); // 1: en ejecución
                mantenimientoRepository.save(mantenimiento);
                
                System.out.println(String.format(
                    "Camión %s enviado a mantenimiento en tiempo %s", 
                    camion.getCodigo(), tiempo
                ));
            }
        }
        
        // Verificar mantenimientos que finalizan hoy
        List<Mantenimiento> mantenimientosFinalizados = mantenimientoRepository.findByFechaFin(fechaActual);
        
        for (Mantenimiento mantenimiento : mantenimientosFinalizados) {
            if (mantenimiento.getEstado() == 1) { // En ejecución
                Camion camion = mantenimiento.getCamion();
                camion.setEstado(EstadoCamion.DISPONIBLE); // Disponible
                camionRepository.save(camion);
                
                // Actualizar estado del mantenimiento
                mantenimiento.setEstado(2); // 2: finalizado
                mantenimientoRepository.save(mantenimiento);
                
                System.out.println(String.format(
                    "Mantenimiento de camión %s finalizado en tiempo %s", 
                    camion.getCodigo(), tiempo
                ));
            }
        }
    }
    
    /**
     * Actualiza el estado de los bloqueos según el tiempo actual
     */
    private void actualizarBloqueos(LocalDateTime tiempo) {
        // Usar el servicio de bloqueos para actualizar los estados
        bloqueoService.actualizarEstadoBloqueos();
        
        // Obtener bloqueos activos para el tiempo actual
        List<Bloqueo> bloqueosActivos = bloqueoService.obtenerBloqueosActivos(tiempo);
        
        // Log de actividades de bloqueos
        for (Bloqueo bloqueo : bloqueosActivos) {
            // Verificar si este bloqueo acaba de activarse (para limitar logs repetidos)
            if (tiempo.isAfter(bloqueo.getFechaInicio()) && 
                tiempo.isBefore(bloqueo.getFechaInicio().plusMinutes(5))) {
                System.out.println(String.format(
                    "Bloqueo activo en %s hasta %s con %d puntos - %s", 
                    bloqueo.getFechaInicio(), bloqueo.getFechaFin(), 
                    bloqueo.getCoordenadas().size(), bloqueo.getDescripcion()
                ));
            }
        }
        
        // Obtener bloqueos que finalizan pronto (en los próximos 5 minutos)
        LocalDateTime tiempoFuturo = tiempo.plusMinutes(5);
        List<Bloqueo> bloqueosFinalizando = bloqueoRepository.findByFechaInicioBeforeAndFechaFinAfter(
            tiempo, tiempo).stream()
            .filter(b -> b.getFechaFin().isBefore(tiempoFuturo))
            .collect(java.util.stream.Collectors.toList());
            
        for (Bloqueo bloqueo : bloqueosFinalizando) {
            System.out.println(String.format(
                "Bloqueo finalizando pronto: %s a las %s", 
                bloqueo.getDescripcion(), bloqueo.getFechaFin()
            ));
        }
    }
    
    /**
     * Procesa pedidos nuevos que se generan en el tiempo actual
     */
    private void procesarNuevosPedidos(LocalDateTime tiempo) {
        // En un escenario realista, buscaríamos pedidos con fechaHora igual al tiempo actual
        // Para simulación, generamos pedidos aleatorios con cierta probabilidad
        
        // Probabilidad de nuevos pedidos según la hora del día
        int hora = tiempo.getHour();
        
        double probabilidad;
        if (hora >= 8 && hora < 12) {
            // Mañana: Alta probabilidad
            probabilidad = 0.4;
        } else if (hora >= 12 && hora < 18) {
            // Tarde: Media probabilidad
            probabilidad = 0.3;
        } else if (hora >= 18 && hora < 22) {
            // Noche: Baja probabilidad
            probabilidad = 0.2;
        } else {
            // Madrugada: Muy baja probabilidad
            probabilidad = 0.05;
        }
        
        // Generar pedidos aleatorios según probabilidad
        if (Math.random() < probabilidad) {
            int cantidadPedidos = ThreadLocalRandom.current().nextInt(1, 4); // 1-3 pedidos
            List<Pedido> nuevosPedidos = generarPedidosAleatorios(cantidadPedidos, cantidadPedidos);
            
            // Ajustar tiempo de creación
            for (Pedido pedido : nuevosPedidos) {
                pedido.setFechaRegistro(tiempo);
                pedidoRepository.save(pedido);
            }
            
            System.out.println(String.format(
                "Generados %d nuevos pedidos en tiempo %s", 
                nuevosPedidos.size(), tiempo
            ));
            
            // Intentar asignar pedidos a camiones disponibles
            asignarPedidosACamiones(nuevosPedidos);
        }
    }
    
    /**
     * Asigna pedidos nuevos a camiones disponibles
     */
    private void asignarPedidosACamiones(List<Pedido> pedidos) {
        // Obtener camiones disponibles
        List<Camion> camionesDisponibles = camionRepository.findByEstado(EstadoCamion.DISPONIBLE );
        
        if (camionesDisponibles.isEmpty()) {
            System.out.println("No hay camiones disponibles para asignar pedidos");
            return;
        }
        
        // Implementación simple: asignar pedidos a los camiones disponibles en orden
        int camionIndex = 0;
        
        for (Pedido pedido : pedidos) {
            if (camionIndex >= camionesDisponibles.size()) {
                // Reiniciar a primer camión si se acabaron
                camionIndex = 0;
            }
            
            Camion camion = camionesDisponibles.get(camionIndex);
            
            // Verificar si el camión tiene capacidad
            if (camion.getCapacidad() >= pedido.getVolumenGLPAsignado()) {
                // Asignar pedido al camión
                pedido.setCamion(camion);
                pedido.setEstado(EstadoPedido.PLANIFICADO_TOTALMENTE); // 1: Asignado
                pedidoRepository.save(pedido);
                
                // Actualizar estado del camión
                camion.setEstado(EstadoCamion.EN_RUTA); // En ruta
                camion.setPesoCarga(camion.getPesoCarga() + (pedido.getVolumenGLPAsignado() * 0.5)); // GLP pesa 0.5 Ton por m3
                camion.setPesoCombinado(camion.getTara() + camion.getPesoCarga());
                camionRepository.save(camion);
                
                System.out.println(String.format(
                    "Pedido %d asignado a camión %s", 
                    pedido.getId(), camion.getCodigo()
                ));
                
                camionIndex++;
            }
        }
    }
    
    /**
     * Procesa eventos aleatorios como averías
     */
    private void procesarEventosAleatorios(LocalDateTime tiempo) {
        // Probabilidad de avería en algún camión en ruta (0.5%)
        if (Math.random() < 0.005) {
            // Buscar camiones en ruta
            List<Camion> camionesEnRuta = camionRepository.findByEstado(EstadoCamion.EN_RUTA );
            
            if (!camionesEnRuta.isEmpty()) {
                // Seleccionar un camión aleatorio
                int randomIndex = ThreadLocalRandom.current().nextInt(0, camionesEnRuta.size());
                Camion camion = camionesEnRuta.get(randomIndex);
                
                // Crear avería
                Averia averia = new Averia();
                averia.setCamion(camion);
                averia.setFechaHoraReporte(tiempo);
                averia.setDescripcion("Avería aleatoria en ruta");
                
                // Usar tipoIncidente en lugar de severidad
                String[] tiposIncidente = {"TI1", "TI2", "TI3"};
                averia.setTipoIncidente(tiposIncidente[ThreadLocalRandom.current().nextInt(0, 3)]);
                
                // Asignar turno según la hora del día
                String turno;
                int hora = tiempo.getHour();
                if (hora >= 0 && hora < 8) {
                    turno = "T1";
                } else if (hora >= 8 && hora < 16) {
                    turno = "T2";
                } else {
                    turno = "T3";
                }
                averia.setTurno(turno);
                
                averia.setPosX(camion.getPosX());
                averia.setPosY(camion.getPosY());
                averia.setEstado(0); // 0: Reportada
                
                // Verificar si el camión tiene carga
                averia.setConCarga(camion.getPesoCarga() > 0);
                
                // Validar si la avería es válida según las condiciones
                boolean esValida = averia.calcularValidezAveria(true); // El camión ya está en operación
                
                if (esValida) {
                    // Calcular distancia total del recorrido
                    List<Pedido> pedidosCamion = pedidoRepository.findByCamion_CodigoAndEstado(camion.getCodigo(), EstadoPedido.PLANIFICADO_TOTALMENTE);
                    double distanciaTotal = calcularDistanciaRecorrido(camion, pedidosCamion);
                    
                    // Calcular kilómetro de ocurrencia entre 5% y 35% del recorrido
                    averia.calcularKilometroOcurrencia(distanciaTotal);
                    
                    // Calcular tiempos de inmovilización según tipo de incidente
                    averia.calcularTiemposInoperatividad();
                    
                    // Guardar la avería
                    averiaRepository.save(averia);
                    
                    // Actualizar estado del camión
                    actualizarEstadoCamionPorAveria(camion, averia);
                    
                    System.out.println(String.format(
                        "Camión %s averiado con %s en turno %s en tiempo %s - KM: %.2f", 
                        camion.getCodigo(), averia.getTipoIncidente(), averia.getTurno(), 
                        tiempo, averia.getKilometroOcurrencia()
                    ));
                } else {
                    System.out.println(String.format(
                        "Avería ignorada para camión %s (sin carga)", 
                        camion.getCodigo()
                    ));
                }
            }
        }
    }
    
    /**
     * Actualiza la posición de los camiones en ruta
     */
    private void actualizarPosicionCamiones(LocalDateTime tiempo) {
        // Buscar camiones en ruta
        List<Camion> camionesEnRuta = camionRepository.findByEstado(EstadoCamion.EN_RUTA );
        
        for (Camion camion : camionesEnRuta) {
            // Obtener pedidos asignados al camión
            List<Pedido> pedidosCamion = pedidoRepository.findByCamion_CodigoAndEstado(camion.getCodigo(), EstadoPedido.PLANIFICADO_TOTALMENTE); // 1: Asignado
            
            if (pedidosCamion.isEmpty()) {
                // No hay pedidos, camión debería volver al almacén central
                camion.setEstado(EstadoCamion.DISPONIBLE);
                camionRepository.save(camion);
                continue;
            }
            
            // Simular avance del camión hacia los pedidos
            // En una implementación real, calcularíamos la distancia y tiempo restante
            
            // Velocidad promedio del camión en km por minuto (50 km/h = 0.833 km/min)
            double velocidadKmPorMinuto = 50.0 / 60.0;
            
            // Avance en el último minuto
            double avanceEnKm = velocidadKmPorMinuto;
            
            // Actualizar consumo de combustible
            double consumoCombustible = camion.calcularConsumoCombustible(avanceEnKm);
            camion.setCombustibleActual(Math.max(0, camion.getCombustibleActual() - consumoCombustible));
            
            // En un escenario real, actualizaríamos la posición exacta
            // Para la simulación, aproximamos con probabilidades de llegada
            
            // Probabilidad de que el camión llegue a su siguiente destino
            if (Math.random() < 0.1) { // 10% de probabilidad por paso
                // Camión llegó al destino, procesar entrega
                Pedido pedidoActual = pedidosCamion.get(0); // Tomar el primer pedido de la lista
                
                // Actualizar posición del camión a la posición del pedido
                camion.setPosX(pedidoActual.getPosX());
                camion.setPosY(pedidoActual.getPosY());
                
                System.out.println(String.format(
                    "Camión %s llegó a la posición del pedido %d en tiempo %s", 
                    camion.getCodigo(), pedidoActual.getId(), tiempo
                ));
            }
            
            camionRepository.save(camion);
        }
    }
    
    /**
     * Procesa las entregas de pedidos
     */
    private void procesarEntregas(LocalDateTime tiempo) {
        // Buscar camiones en ruta
        List<Camion> camionesEnRuta = camionRepository.findByEstado(EstadoCamion.EN_RUTA );
        
        for (Camion camion : camionesEnRuta) {
            // Obtener pedidos asignados al camión y que NO estén entregados
            List<Pedido> pedidosCamion = pedidoRepository.findByCamion_CodigoAndEstado(camion.getCodigo(), EstadoPedido.PLANIFICADO_TOTALMENTE); // 1: Asignado
            
            if (!pedidosCamion.isEmpty()) {
                // Verificar si el camión está en la posición del primer pedido
                Pedido primerPedido = pedidosCamion.get(0);
                
                if (camion.getPosX() == primerPedido.getPosX() && camion.getPosY() == primerPedido.getPosY()) {
                    // Camión está en la posición del pedido, realizar entrega
                    primerPedido.setEstado(EstadoPedido.ENTREGADO_TOTALMENTE); // 2: Entregado
                    // Actualizar fecha de entrega real
                    primerPedido.setFechaEntregaReal(tiempo);
                    pedidoRepository.save(primerPedido);
                    
                    // Actualizar carga del camión
                    double pesoEntregado = primerPedido.getVolumenGLPAsignado() * 0.5; // GLP pesa 0.5 Ton por m3
                    camion.setPesoCarga(Math.max(0, camion.getPesoCarga() - pesoEntregado));
                    camion.setPesoCombinado(camion.getTara() + camion.getPesoCarga());
                    
                    System.out.println(String.format(
                        "Pedido %d entregado por camión %s en tiempo %s", 
                        primerPedido.getId(), camion.getCodigo(), tiempo
                    ));
                    
                    // Verificar si hay más pedidos asignados a este camión
                    List<Pedido> pedidosRestantes = pedidoRepository.findByCamion_CodigoAndEstado(camion.getCodigo(), EstadoPedido.PLANIFICADO_TOTALMENTE); // 1: Asignado
                    if (pedidosRestantes.isEmpty()) {
                        // No hay más pedidos pendientes, regresar al almacén central
                        // En implementación real, calcularíamos ruta al almacén
                        if (Math.random() < 0.2) { // 20% de probabilidad por paso de volver al almacén
                            camion.setEstado(EstadoCamion.DISPONIBLE);
                            camion.setPosX(12); // Posición del almacén central
                            camion.setPosY(8);
                            camion.setPesoCarga(0);
                            camion.setPesoCombinado(camion.getTara());
                            
                            System.out.println(String.format(
                                "Camión %s regresó al almacén central en tiempo %s", 
                                camion.getCodigo(), tiempo
                            ));
                        }
                    } else {
                        // Hay más pedidos, dirigir el camión hacia el siguiente pedido
                        Pedido siguientePedido = pedidosRestantes.get(0);
                        System.out.println(String.format(
                            "Camión %s ahora se dirige al pedido %d en posición (%d, %d)", 
                            camion.getCodigo(), siguientePedido.getId(), 
                            siguientePedido.getPosX(), siguientePedido.getPosY()
                        ));
                        // La lógica de movimiento se maneja en actualizarPosicionCamiones
                    }
                    
                    camionRepository.save(camion);
                }
            }
        }
    }
    
    /**
     * Verifica los niveles de combustible y realiza recargas si es necesario
     */
    private void verificarCombustible(LocalDateTime tiempo) {
        // Buscar camiones en ruta
        List<Camion> camionesEnRuta = camionRepository.findByEstado(EstadoCamion.EN_RUTA );
        
        for (Camion camion : camionesEnRuta) {
            // Verificar si el nivel de combustible es bajo (menos del 20%)
            double nivelCritico = camion.getCapacidadTanque() * 0.2;
            
            if (camion.getCombustibleActual() < nivelCritico) {
                // Buscar el almacén más cercano para recargar
                Almacen almacenCercano = buscarAlmacenCercano(camion.getPosX(), camion.getPosY());
                
                if (almacenCercano != null) {
                    // Simular recarga de combustible
                    double cantidadRecargar = camion.getCapacidadTanque() - camion.getCombustibleActual();
                    
                    if (almacenCercano.puedeRecargarCombustible(cantidadRecargar)) {
                        boolean recargoExitoso = almacenCercano.recargarCombustible(camion, cantidadRecargar);
                        
                        if (recargoExitoso) {
                            System.out.println(String.format(
                                "Camión %s recargó %.2f galones en almacén %s en tiempo %s", 
                                camion.getCodigo(), cantidadRecargar, almacenCercano.getNombre(), tiempo
                            ));
                            
                            // Guardar cambios en almacén y camión
                            almacenRepository.save(almacenCercano);
                            camionRepository.save(camion);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Busca el almacén más cercano a una posición
     */
    private Almacen buscarAlmacenCercano(double posX, double posY) {
        List<Almacen> almacenes = almacenRepository.findByActivo(true);
        
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
     * Recopila estadísticas del estado actual de la simulación
     */
    private Map<String, Object> recopilarEstadisticas(LocalDateTime tiempo) {
        Map<String, Object> estadisticas = new HashMap<>();
        
        // Contar pedidos por estado
        long pedidosPendientes = pedidoRepository.countByEstado(EstadoPedido.REGISTRADO); // 0: Pendiente
        long pedidosAsignados = pedidoRepository.countByEstado(EstadoPedido.PLANIFICADO_TOTALMENTE);  // 1: Asignado
        long pedidosEntregados = pedidoRepository.countByEstado(EstadoPedido.ENTREGADO_TOTALMENTE); // 2: Entregado
        
        // Contar camiones por estado
        long camionesDisponibles = camionRepository.countByEstado(EstadoCamion.DISPONIBLE ); // Disponible
        long camionesEnRuta = camionRepository.countByEstado(EstadoCamion.EN_RUTA );     // En ruta
        long camionesEnMantenimiento = camionRepository.countByEstado(EstadoCamion.EN_MANTENIMIENTO_POR_AVERIA ); // En mantenimiento
        long camionesAveriados = camionRepository.countByEstado(EstadoCamion.INMOVILIZADO_POR_AVERIA);  // Averiado
        
        // Calcular tiempos de entrega
        double tiempoPromedioEntrega = calcularTiempoPromedioEntrega();
        
        // Calcular eficiencia de entrega
        double eficienciaEntrega = calcularEficienciaEntrega();
        
        // Estadísticas de combustible
        double consumoTotalCombustible = calcularConsumoTotalCombustible();
        
        // Estadísticas de almacenes
        Map<String, Object> estadisticasAlmacenes = recopilarEstadisticasAlmacenes();
        
        // Guardar todas las estadísticas
        estadisticas.put("fecha", tiempo.toLocalDate());
        estadisticas.put("hora", tiempo.toLocalTime());
        estadisticas.put("pedidosPendientes", pedidosPendientes);
        estadisticas.put("pedidosAsignados", pedidosAsignados);
        estadisticas.put("pedidosEntregados", pedidosEntregados);
        estadisticas.put("totalPedidos", pedidosPendientes + pedidosAsignados + pedidosEntregados);
        estadisticas.put("camionesDisponibles", camionesDisponibles);
        estadisticas.put("camionesEnRuta", camionesEnRuta);
        estadisticas.put("camionesEnMantenimiento", camionesEnMantenimiento);
        estadisticas.put("camionesAveriados", camionesAveriados);
        estadisticas.put("totalCamiones", camionesDisponibles + camionesEnRuta + camionesEnMantenimiento + camionesAveriados);
        estadisticas.put("tiempoPromedioEntrega", tiempoPromedioEntrega);
        estadisticas.put("eficienciaEntrega", eficienciaEntrega);
        estadisticas.put("consumoTotalCombustible", consumoTotalCombustible);
        estadisticas.put("almacenes", estadisticasAlmacenes);
        
        return estadisticas;
    }
    
    /**
     * Calcula el tiempo promedio de entrega en minutos
     */
    private double calcularTiempoPromedioEntrega() {
        List<Pedido> pedidosEntregados = pedidoRepository.findByEstadoAndFechaEntregaRequeridaNotNull(EstadoPedido.ENTREGADO_TOTALMENTE); // 2: Entregado
        
        if (pedidosEntregados.isEmpty()) {
            return 0;
        }
        
        double tiempoTotal = 0;
        int conteo = 0;
        
        for (Pedido pedido : pedidosEntregados) {
            if (pedido.getFechaRegistro() != null && pedido.getFechaEntregaRequerida() != null) {
                // Calcular diferencia en minutos
                long diferenciaMinutos = java.time.Duration.between(pedido.getFechaRegistro(), pedido.getFechaEntregaRequerida()).toMinutes();
                tiempoTotal += diferenciaMinutos;
                conteo++;
            }
        }
        
        return conteo > 0 ? tiempoTotal / conteo : 0;
    }
    
    /**
     * Calcula la eficiencia de entrega (porcentaje de pedidos entregados a tiempo)
     */
    private double calcularEficienciaEntrega() {
        List<Pedido> pedidosEntregados = pedidoRepository.findByEstado(EstadoPedido.ENTREGADO_TOTALMENTE); // 2: Entregado
        
        if (pedidosEntregados.isEmpty()) {
            return 100; // No hay pedidos entregados para evaluar
        }
        
        int pedidosATiempo = 0;
        
        for (Pedido pedido : pedidosEntregados) {
            if (pedido.getFechaRegistro() != null && pedido.getFechaEntregaRequerida() != null) {
                // Calcular diferencia en horas
                long diferenciaHoras = java.time.Duration.between(pedido.getFechaRegistro(), pedido.getFechaEntregaRequerida()).toHours();
                
                // Verificar si se entregó dentro del límite
                if (diferenciaHoras <= pedido.getHorasLimite()) {
                    pedidosATiempo++;
                }
            }
        }
        
        return pedidosEntregados.size() > 0 ? (pedidosATiempo * 100.0 / pedidosEntregados.size()) : 100;
    }
    
    /**
     * Calcula el consumo total de combustible en galones
     */
    private double calcularConsumoTotalCombustible() {
        // En una implementación real, esto se calcularía sumando todos los consumos registrados
        // Para simulación, usamos un valor aproximado basado en camiones en ruta
        List<Camion> camionesEnRuta = camionRepository.findByEstado(EstadoCamion.EN_RUTA);
        
        double consumoTotal = 0;
        for (Camion camion : camionesEnRuta) {
            // Asumimos un consumo promedio basado en el peso del camión
            consumoTotal += (camion.getPesoCombinado() / 180.0) * 10; // Estimado para 10 km
        }
        
        return consumoTotal;
    }
    
    /**
     * Recopila estadísticas de almacenes
     */
    private Map<String, Object> recopilarEstadisticasAlmacenes() {
        Map<String, Object> estadisticas = new HashMap<>();
        List<Almacen> almacenes = almacenRepository.findAll();
        
        for (Almacen almacen : almacenes) {
            Map<String, Object> estadisticasAlmacen = new HashMap<>();
            estadisticasAlmacen.put("nombre", almacen.getNombre());
            estadisticasAlmacen.put("capacidadGLP", almacen.getCapacidadGLP());
            estadisticasAlmacen.put("capacidadActualGLP", almacen.getCapacidadActualGLP());
            estadisticasAlmacen.put("porcentajeGLP", almacen.getCapacidadGLP() > 0 ? 
                    (almacen.getCapacidadActualGLP() * 100 / almacen.getCapacidadGLP()) : 0);
            estadisticasAlmacen.put("capacidadCombustible", almacen.getCapacidadCombustible());
            estadisticasAlmacen.put("capacidadActualCombustible", almacen.getCapacidadActualCombustible());
            estadisticasAlmacen.put("porcentajeCombustible", almacen.getCapacidadCombustible() > 0 ? 
                    (almacen.getCapacidadActualCombustible() * 100 / almacen.getCapacidadCombustible()) : 0);
            
            estadisticas.put("almacen_" + almacen.getId(), estadisticasAlmacen);
        }
        
        return estadisticas;
    }

    /**
     * Carga y procesa el archivo de averías
     * Aplica todas las reglas de validación especificadas:
     * 1. Verifica si la unidad está en operación
     * 2. Calcula el kilómetro de ocurrencia aleatoriamente entre 5% y 35% del tramo
     * 3. Solo considera averías cuando el camión lleva carga
     */
    public List<Averia> cargarArchivoAverias(String rutaArchivo) {
        List<Averia> averias = new ArrayList<>();
        
        try {
            Path path = Paths.get(rutaArchivo);
            if (!Files.exists(path)) {
                System.out.println("Archivo de averías no encontrado: " + rutaArchivo);
                return averias;
            }
            
            List<String> lines = Files.readAllLines(path);
            
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                
                // Formato esperado: T1_TA01_TI2
                String[] parts = line.split("_");
                if (parts.length != 3) {
                    System.out.println("Formato inválido en línea: " + line);
                    continue;
                }
                
                String turno = parts[0];       // T1, T2, T3
                String codigoCamion = parts[1]; // TA01, TB03, etc.
                String tipoIncidente = parts[2]; // TI1, TI2, TI3
                
                // Buscar el camión por su código
                Optional<Camion> optCamion = camionRepository.findByCodigo(codigoCamion);
                if (!optCamion.isPresent()) {
                    System.out.println("Camión no encontrado para avería: " + line);
                    continue;
                }
                
                Camion camion = optCamion.get();
                
                // Verificar si el camión está en operación (estado 1: en ruta)
                boolean estaEnOperacion = camion.getEstado() == EstadoCamion.EN_RUTA;
                
                // Crear la avería
                Averia averia = new Averia();
                averia.setCamion(camion);
                averia.setFechaHoraReporte(LocalDateTime.now());
                averia.setDescripcion("Avería importada: " + tipoIncidente);
                averia.setTurno(turno);
                averia.setTipoIncidente(tipoIncidente);
                averia.setPosX(camion.getPosX());
                averia.setPosY(camion.getPosY());
                averia.setEstado(0); // 0: reportada
                
                // Verificar si tiene carga asignada
                List<Pedido> pedidosCamion = pedidoRepository.findByCamion_CodigoAndEstado(camion.getCodigo(), EstadoPedido.PLANIFICADO_PARCIALMENTE);
                averia.setConCarga(!pedidosCamion.isEmpty() && camion.getPesoCarga() > 0);
                
                // Validar si la avería es válida según las condiciones
                boolean esValida = averia.calcularValidezAveria(estaEnOperacion);
                
                if (esValida) {
                    // Calcular la distancia total del recorrido (simplificado)
                    double distanciaTotal = 100.0; // Valor por defecto
                    
                    // Si hay pedidos, podríamos calcular la distancia real
                    if (!pedidosCamion.isEmpty()) {
                        // Simplificación: usamos una suma de distancias punto a punto
                        distanciaTotal = calcularDistanciaRecorrido(camion, pedidosCamion);
                    }
                    
                    // Calcular kilómetro aleatorio de ocurrencia (5% a 35% del recorrido)
                    averia.calcularKilometroOcurrencia(distanciaTotal);
                    
                    // Calcular tiempos de inmovilización según tipo de incidente
                    // Asumimos 3 turnos de 8 horas cada uno (configurable)
                    int duracionTurnoHoras = 8;
                    averia.calcularTiemposInoperatividad(duracionTurnoHoras);
                    
                    // Actualizar estado del camión según el tipo de incidente
                    actualizarEstadoCamionPorAveria(camion, averia);
                    
                    // Guardar la avería
                    averiaRepository.save(averia);
                    averias.add(averia);
                    
                    System.out.println("Avería procesada: " + averia.generarRegistro() + 
                                     " - KM: " + averia.getKilometroOcurrencia());
                } else {
                    System.out.println("Avería ignorada (unidad no en operación o sin carga): " + line);
                }
            }
            
        } catch (IOException e) {
            System.err.println("Error al leer el archivo de averías: " + e.getMessage());
        }
        
        return averias;
    }
    
    /**
     * Calcula la distancia aproximada del recorrido completo de un camión
     */
    private double calcularDistanciaRecorrido(Camion camion, List<Pedido> pedidos) {
        double distanciaTotal = 0;
        
        // Posición actual del camión
        double posXActual = camion.getPosX();
        double posYActual = camion.getPosY();
        
        // Sumar distancias entre puntos consecutivos
        for (Pedido pedido : pedidos) {
            double distancia = Math.sqrt(
                Math.pow(pedido.getPosX() - posXActual, 2) + 
                Math.pow(pedido.getPosY() - posYActual, 2)
            );
            
            distanciaTotal += distancia;
            
            // Actualizar posición para el siguiente cálculo
            posXActual = pedido.getPosX();
            posYActual = pedido.getPosY();
        }
        
        // Agregar distancia de regreso al almacén (simplificado)
        // Asumimos almacén central en posición (12, 8)
        double distanciaRegreso = Math.sqrt(
            Math.pow(12 - posXActual, 2) + 
            Math.pow(8 - posYActual, 2)
        );
        
        distanciaTotal += distanciaRegreso;
        
        // Multiplicar por 2 para simular ida y vuelta
        return distanciaTotal * 2;
    }
    
    /**
     * Actualiza el estado del camión según el tipo de avería
     */
    private void actualizarEstadoCamionPorAveria(Camion camion, Averia averia) {
        // Marcar el camión como averiado
        camion.setEstado(EstadoCamion.INMOVILIZADO_POR_AVERIA); // Averiado
        
        // Guardar el camión con su nuevo estado
        camionRepository.save(camion);
        
        System.out.println("Camión " + camion.getCodigo() + " marcado como averiado por " + 
                         averia.getTipoIncidente() + " en turno " + averia.getTurno());
    }
    
    /**
     * Método para iniciar la carga del archivo de averías durante el inicio de la simulación
     */
    public void inicializarAveriasDesdeArchivo() {
        String rutaArchivo = "src/main/resources/data/averias/averias.v1.txt";
        List<Averia> averiasCargadas = cargarArchivoAverias(rutaArchivo);
        System.out.println("Se cargaron " + averiasCargadas.size() + " averías válidas desde el archivo");
    }
}
```

## main\java\com\plg\service\SimulacionTiempoRealService.java

```java
package com.plg.service;

import com.plg.entity.*;
import com.plg.entity.EstadoCamion;
import com.plg.entity.EstadoPedido;
import com.plg.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

@Service
public class SimulacionTiempoRealService {

    private static final Logger logger = LoggerFactory.getLogger(SimulacionTiempoRealService.class);

    @Autowired
    private CamionRepository camionRepository;
    
    @Autowired
    private RutaRepository rutaRepository;
    
    @Autowired
    private PedidoRepository pedidoRepository;
    
    @Autowired
    private AlmacenRepository almacenRepository;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    private ScheduledExecutorService scheduler;
    private boolean simulacionEnCurso = false;
    private int factorVelocidad = 1;
    
    // Almacena el progreso del nodo actual en cada ruta (0 a 100%)
    private Map<Long, Double> progresoNodoActual = new HashMap<>();
    
    // Método para iniciar la simulación en tiempo real
    public Map<String, Object> iniciarSimulacion() {
        if (simulacionEnCurso) {
            return crearRespuesta("La simulación ya está en curso");
        }
        
        logger.info("Iniciando simulación de tiempo real a las {}", LocalDateTime.now());
        
        simulacionEnCurso = true;
        scheduler = Executors.newSingleThreadScheduledExecutor();
        progresoNodoActual.clear();
        
        // Activar las rutas pendientes de los camiones en ruta si no tienen rutas activas
        activarRutasPendientes();
        
        // Iniciar simulación - actualizar cada segundo ajustado por el factor de velocidad
        scheduler.scheduleAtFixedRate(this::actualizarSimulacion, 0, 1000 / factorVelocidad, TimeUnit.MILLISECONDS);
        
        logger.info("Simulación iniciada con factor de velocidad: {}", factorVelocidad);
        
        // Enviar notificación de inicio
        Map<String, Object> notificacion = new HashMap<>();
        notificacion.put("tipo", "simulacion");
        notificacion.put("accion", "iniciada");
        notificacion.put("factorVelocidad", factorVelocidad);
        notificacion.put("timestamp", LocalDateTime.now().toString());
        messagingTemplate.convertAndSend("/topic/simulacion", notificacion);
        
        return crearRespuesta("Simulación iniciada correctamente");
    }
    
    // Nuevo método para activar rutas pendientes
    private void activarRutasPendientes() {
        List<Camion> camionesEnRuta = camionRepository.findByEstado(EstadoCamion.EN_RUTA); // Camiones en ruta
        
        for (Camion camion : camionesEnRuta) {
            // Verificar si tiene alguna ruta activa
            List<Ruta> rutasActivas = rutaRepository.findByCamionIdAndEstado(camion.getId(), 1);
            
            if (rutasActivas.isEmpty()) {
                // Buscar rutas pendientes para este camión
                List<Ruta> rutasPendientes = rutaRepository.findByCamionIdAndEstado(camion.getId(), 0);
                
                if (!rutasPendientes.isEmpty()) {
                    // Activar la primera ruta pendiente
                    Ruta rutaParaActivar = rutasPendientes.get(0);
                    rutaParaActivar.setEstado(1); // 1 = En curso
                    rutaParaActivar.setFechaInicioRuta(LocalDateTime.now());
                    rutaRepository.save(rutaParaActivar);
                    
                    logger.info("Activada ruta {} para camión {}", rutaParaActivar.getCodigo(), camion.getCodigo());
                    
                    // Inicializar progreso
                    progresoNodoActual.put(rutaParaActivar.getId(), Double.valueOf(0.0));
                }
            }
        }
    }
    
    // Método para detener la simulación
    public Map<String, Object> detenerSimulacion() {
        if (!simulacionEnCurso) {
            return crearRespuesta("No hay simulación en curso");
        }
        
        simulacionEnCurso = false;
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
            }
        }
        
        return crearRespuesta("Simulación detenida correctamente");
    }
    
    // Método para ajustar la velocidad de la simulación
    public Map<String, Object> ajustarVelocidad(int factor) {
        if (factor < 1) {
            factor = 1;
        } else if (factor > 10) {
            factor = 10;
        }
        
        this.factorVelocidad = factor;
        
        // Si hay una simulación en curso, reiniciarla con la nueva velocidad
        if (simulacionEnCurso) {
            detenerSimulacion();
            iniciarSimulacion();
        }
        
        Map<String, Object> respuesta = crearRespuesta("Velocidad ajustada correctamente");
        respuesta.put("factorVelocidad", Optional.of(factorVelocidad));
        return respuesta;
    }
    
    // Método principal que se ejecuta periódicamente para actualizar la simulación
    private void actualizarSimulacion() {
        try {
            // Verificar si hay simulación en curso
            if (!simulacionEnCurso) {
                logger.warn("actualizarSimulacion() llamado pero simulacionEnCurso=false");
                return;
            }
            
            // Obtener todos los camiones en ruta (estado 1)
            List<Camion> camionesEnRuta = camionRepository.findByEstado(EstadoCamion.EN_RUTA); // 1 = En ruta
            // Agregar también los camiones en estado 0 (disponible) para verificar si tienen rutas pendientes
            List<Camion> camionesDisponibles = camionRepository.findByEstado(EstadoCamion.EN_RUTA);
            
            camionesEnRuta.addAll(camionesDisponibles);
            
            // Si no hay camiones en ruta o disponibles, no hay nada que simular
            if (camionesEnRuta.isEmpty()) {
                logger.info("No hay camiones para simular");
                return;
            }
            
            logger.debug("Procesando {} camiones en la simulación", camionesEnRuta.size());
            
            // Procesar cada camión
            for (Camion camion : camionesEnRuta) {
                procesarMovimientoCamion(camion);
            }
            
            // Contar estadísticas para incluir en la actualización
            Map<String, Object> estadisticas = new HashMap<>();
            estadisticas.put("camionesTotal", camionRepository.count());
            estadisticas.put("camionesEnRuta", camionRepository.findByEstado(EstadoCamion.EN_RUTA).size());
            estadisticas.put("almacenesTotal", almacenRepository.count());
            estadisticas.put("pedidosTotal", pedidoRepository.count());
            estadisticas.put("pedidosPendientes", pedidoRepository.findByEstado(EstadoPedido.REGISTRADO).size());
            estadisticas.put("pedidosEnRuta", pedidoRepository.findByEstado(EstadoPedido.EN_RUTA).size());
            estadisticas.put("pedidosEntregados", pedidoRepository.findByEstado(EstadoPedido.ENTREGADO_TOTALMENTE).size());
            estadisticas.put("rutasTotal", rutaRepository.count());
            estadisticas.put("rutasActivas", rutaRepository.findByEstado(1).size());
            
            // Enviar actualización a los clientes conectados vía WebSocket
            enviarActualizacionPosiciones(estadisticas);
            
        } catch (Exception e) {
            // Registrar error pero no detener la simulación
            logger.error("Error en la simulación: {}", e.getMessage(), e);
        }
    }

    // Procesa el movimiento de un camión específico
    private void procesarMovimientoCamion(Camion camion) {
        try {
            // Obtener las rutas activas del camión
            List<Ruta> rutasActivas = rutaRepository.findByCamionIdAndEstadoWithNodos(camion.getId(), 1); // Usar findByCamionIdAndEstadoWithNodos
            
            if (rutasActivas.isEmpty()) {
                logger.warn("Camión {} está en ruta (estado {}) pero no tiene rutas activas", camion.getCodigo(), camion.getEstado());
                
                // Intentar activar una ruta pendiente
                List<Ruta> rutasPendientes = rutaRepository.findByCamionIdAndEstado(camion.getId(), 0);
                if (!rutasPendientes.isEmpty()) {
                    Ruta rutaParaActivar = rutasPendientes.get(0);
                    rutaParaActivar.setEstado(1); // Activar la ruta
                    rutaParaActivar.setFechaInicioRuta(LocalDateTime.now());
                    rutaRepository.save(rutaParaActivar);
                    
                    // Inicializar progreso
                    progresoNodoActual.put(rutaParaActivar.getId(), Double.valueOf(0.0));
                    
                    logger.info("Activada ruta pendiente {} para camión {}", rutaParaActivar.getCodigo(), camion.getCodigo());
                    
                    // Evitar cambiar el estado del camión, continuará en ruta
                    return;
                } else {
                    // El camión está marcado en ruta pero no tiene rutas activas ni pendientes
                    logger.info("Camión {} sin rutas, cambiando a disponible", camion.getCodigo());
                    camion.setEstado(EstadoCamion.DISPONIBLE); // Cambiar a disponible
                    camionRepository.save(camion);
                    return;
                }
            }
            
            // Procesar la primera ruta activa (asumiendo que un camión solo puede tener una ruta activa a la vez)
            Ruta rutaActual = rutasActivas.get(0);
            List<NodoRuta> nodos = rutaActual.getNodos();
            
            if (nodos == null || nodos.size() < 2) {
                logger.warn("Ruta {} no tiene suficientes nodos", rutaActual.getCodigo());
                return; // La ruta debe tener al menos un origen y un destino
            }
            
            logger.debug("Procesando movimiento de camión {} en ruta {} con {} nodos", camion.getCodigo(), rutaActual.getCodigo(), nodos.size());
            
            // Inicializar el progreso si es la primera vez que procesamos esta ruta
            if (!progresoNodoActual.containsKey(rutaActual.getId())) {
                progresoNodoActual.put(rutaActual.getId(), Double.valueOf(0.0)); // Comenzamos en 0%
                logger.info("Inicializando progreso para ruta {}", rutaActual.getCodigo());
                
                // Para asegurar que el camión empiece en el primer nodo de la ruta
                NodoRuta nodoInicial = nodos.get(0);
                camion.setPosX(nodoInicial.getPosX());
                camion.setPosY(nodoInicial.getPosY());
                camionRepository.save(camion);
            }
            
            // Determinar el nodo actual y siguiente
            int indiceNodoActual = encontrarIndiceNodoActual(rutaActual, nodos);
            
            // Si hemos llegado al último nodo, la ruta está completa
            if (indiceNodoActual >= nodos.size() - 1) {
                logger.info("Ruta {} completada", rutaActual.getCodigo());
                completarRuta(rutaActual, camion);
                return;
            }
            
            NodoRuta nodoActual = nodos.get(indiceNodoActual);
            NodoRuta nodoSiguiente = nodos.get(indiceNodoActual + 1);
            
            logger.debug("Camión {} moviéndose del nodo {} al nodo {} - Progreso: {}", camion.getCodigo(), indiceNodoActual, (indiceNodoActual + 1), progresoNodoActual.get(rutaActual.getId()));
            
            // Aumentar el progreso hacia el siguiente nodo
            double progreso = progresoNodoActual.get(rutaActual.getId());
            double incremento = (5.0 * factorVelocidad) / 100.0; // Avance del 5% * factor de velocidad
            
            // Verificar si hay combustible suficiente para el movimiento
            double distanciaRecorrida = nodoActual.distanciaA(nodoSiguiente) * incremento;
            double consumoPrevisto = camion.calcularConsumoCombustible(distanciaRecorrida);
            
            if (camion.getCombustibleActual() < consumoPrevisto) {
                // No hay combustible suficiente para continuar
                if (camion.getEstado() != EstadoCamion.SIN_COMBUSTIBLE) { // Solo cambiar si no estaba ya marcado sin combustible
                    camion.setEstado(EstadoCamion.SIN_COMBUSTIBLE); // Sin combustible
                    camionRepository.save(camion);
                    
                    // Enviar notificación de falta de combustible
                    Map<String, Object> notificacion = new HashMap<>();
                    notificacion.put("tipo", "sinCombustible");
                    notificacion.put("camionId", camion.getId());
                    notificacion.put("camionCodigo", camion.getCodigo());
                    notificacion.put("posX", camion.getPosX());
                    notificacion.put("posY", camion.getPosY());
                    notificacion.put("combustibleRestante", camion.getCombustibleActual());
                    notificacion.put("mensaje", "El camión " + camion.getCodigo() + " se ha quedado sin combustible");
                    messagingTemplate.convertAndSend("/topic/alertas", notificacion);
                    
                    logger.warn("Camión {} sin combustible en pos X:{} Y:{}", camion.getCodigo(), camion.getPosX(), camion.getPosY());
                }
                return; // No seguir procesando el camión
            }
            
            // Hay suficiente combustible, continuar con el movimiento
            progreso += incremento;
            
            // Verificar si llegamos al siguiente nodo
            if (progreso >= 1.0) {
                progreso = 0.0; // Reiniciar progreso para el próximo tramo
                
                // Actualizar posición del camión al llegar al nodo siguiente
                camion.setPosX(nodoSiguiente.getPosX());
                camion.setPosY(nodoSiguiente.getPosY());
                
                // Consumir combustible por el tramo recorrido
                double consumo = camion.calcularConsumoCombustible(distanciaRecorrida * (1.0/incremento)); // Consumo total del tramo
                camion.setCombustibleActual(Math.max(0, camion.getCombustibleActual() - consumo));
                
                // Verificar si después de consumir nos quedamos sin combustible
                if (camion.getCombustibleActual() <= 0.1) { // Un umbral mínimo para considerar "sin combustible"
                    camion.setCombustibleActual(0);
                    camion.setEstado(EstadoCamion.SIN_COMBUSTIBLE); // Sin combustible
                    
                    // Enviar notificación de falta de combustible
                    Map<String, Object> notificacion = new HashMap<>();
                    notificacion.put("tipo", "sinCombustible");
                    notificacion.put("camionId", camion.getId());
                    notificacion.put("camionCodigo", camion.getCodigo());
                    notificacion.put("posX", camion.getPosX());
                    notificacion.put("posY", camion.getPosY());
                    notificacion.put("mensaje", "El camión " + camion.getCodigo() + " se ha quedado sin combustible");
                    messagingTemplate.convertAndSend("/topic/alertas", notificacion);
                    
                    logger.warn("Camión {} sin combustible en pos X:{} Y:{}", camion.getCodigo(), camion.getPosX(), camion.getPosY());
                }
                
                // Si es un nodo cliente, procesar entrega
                if ("CLIENTE".equals(nodoSiguiente.getTipo()) && !nodoSiguiente.isEntregado() && nodoSiguiente.getPedido() != null) {
                    procesarEntrega(camion, rutaActual, nodoSiguiente);
                }
                
                // Si es un nodo de tipo ALMACEN y no es el primero, podría ser retorno al almacén para recargar
                if ("ALMACEN".equals(nodoSiguiente.getTipo()) && indiceNodoActual > 0) {
                    recargarCamion(camion);
                }
                
                // Guardar el camión con su nueva posición
                camionRepository.save(camion);
                
                // Enviar notificación de llegada a nodo
                enviarNotificacionLlegadaNodo(camion, nodoSiguiente, rutaActual);
                
            } else {
                // Estamos en medio del camino entre nodos, calcular posición intermedia
                calcularPosicionIntermedia(camion, nodoActual, nodoSiguiente, progreso);
                camionRepository.save(camion);
                
                logger.debug("Camión {} en posición intermedia X:{} Y:{} - Progreso: {}", camion.getCodigo(), camion.getPosX(), camion.getPosY(), progreso);
            }
            
            // Guardar el progreso actualizado
            progresoNodoActual.put(rutaActual.getId(), Double.valueOf(progreso));
            
        } catch (Exception e) {
            logger.error("Error procesando movimiento de camión {}: {}", camion.getId(), e.getMessage(), e);
        }
    }
    
    // Encuentra el índice del nodo actual en la ruta
    private int encontrarIndiceNodoActual(Ruta ruta, List<NodoRuta> nodos) {
        // Si la ruta acaba de iniciar, estamos en el primer nodo
        if (progresoNodoActual.get(ruta.getId()) == 0.0) {
            return 0;
        }
        
        // Buscar el último nodo que hemos visitado completamente
        int indice = 0;
        for (int i = 0; i < nodos.size() - 1; i++) {
            NodoRuta nodo = nodos.get(i);
            
            // Si el camión está exactamente en este nodo y el progreso es 0, estamos iniciando desde aquí
            if (indice == i && progresoNodoActual.get(ruta.getId()) == 0.0) {
                return i;
            }
            
            // Si la posición del camión coincide con la del nodo siguiente, hemos pasado este nodo
            Camion camion = ruta.getCamion();
            NodoRuta nodoSiguiente = nodos.get(i + 1);
            
            if (camion.getPosX() == nodoSiguiente.getPosX() && camion.getPosY() == nodoSiguiente.getPosY()) {
                indice = i + 1;
            }
        }
        
        return indice;
    }
    
    // Calcular posición intermedia entre dos nodos
    private void calcularPosicionIntermedia(Camion camion, NodoRuta origen, NodoRuta destino, double progreso) {
        try {
            // Validar coordenadas para evitar NaN
            if (Double.isNaN(origen.getPosX()) || Double.isNaN(origen.getPosY()) || 
                Double.isNaN(destino.getPosX()) || Double.isNaN(destino.getPosY())) {
                logger.warn("Coordenadas NaN detectadas en nodos de ruta");
                return;
            }
            
            logger.debug("Calculando posición intermedia para camión {} - Origen: ({},{}) Destino: ({},{}) - Progreso: {}", 
                camion.getCodigo(), origen.getPosX(), origen.getPosY(), destino.getPosX(), destino.getPosY(), progreso);
            
            // Movimiento solo horizontal y vertical (reticular), nunca diagonal
            double deltaX = destino.getPosX() - origen.getPosX();
            double deltaY = destino.getPosY() - origen.getPosY();
            
            double nuevaX = origen.getPosX();
            double nuevaY = origen.getPosY();
            
            // En un mapa reticular, primero movemos horizontalmente, luego verticalmente
            if (Math.abs(deltaX) > 0.01) { // Si hay distancia horizontal
                // Primera fase: mover horizontalmente hasta completarlo
                if (progreso <= 0.5) {
                    // De 0 a 0.5, solo movemos en X (horizontal)
                    // Ajustar el progreso para que vaya de 0 a 1 en esta fase
                    double progresoHorizontal = progreso * 2.0;
                    nuevaX = origen.getPosX() + (deltaX * progresoHorizontal);
                    nuevaY = origen.getPosY(); // Y permanece constante
                    
                    logger.debug("FASE HORIZONTAL: Camión {} - Progreso real: {}%, horizontal ajustado: {}%, Nueva posición X: {}", 
                        camion.getCodigo(), (progreso * 100), (progresoHorizontal * 100), nuevaX);
                } else {
                    // De 0.5 a 1, X ya está en destino, movemos Y
                    nuevaX = destino.getPosX(); // X ya llegó al destino
                    
                    // Ajustar el progreso para Y, de 0 a 1
                    double progresoVertical = (progreso - 0.5) * 2.0;
                    nuevaY = origen.getPosY() + (deltaY * progresoVertical);
                    
                    logger.debug("FASE VERTICAL: Camión {} - Progreso real: {}%, vertical ajustado: {}%, Nueva posición Y: {}", 
                        camion.getCodigo(), (progreso * 100), (progresoVertical * 100), nuevaY);
                }
            } else {
                // Si no hay movimiento horizontal, solo movemos verticalmente de principio a fin
                nuevaY = origen.getPosY() + (deltaY * progreso);
                logger.debug("SOLO VERTICAL: Camión {} - Progreso: {}%, Nueva posición Y: {}", 
                    camion.getCodigo(), (progreso * 100), nuevaY);
            }
            
            logger.info("Camión {} - Actualización de posición: De ({},{}) a ({},{})", 
                camion.getCodigo(), camion.getPosX(), camion.getPosY(), nuevaX, nuevaY);
            
            // Actualizar posición del camión
            camion.setPosX(nuevaX);
            camion.setPosY(nuevaY);
            
        } catch (Exception e) {
            logger.error("Error calculando posición intermedia: {}", e.getMessage(), e);
        }
    }
    
    // Procesa la entrega de un pedido
    private void procesarEntrega(Camion camion, Ruta ruta, NodoRuta nodo) {
        // Marcar nodo como entregado
        nodo.setEntregado(true);
        nodo.setTiempoLlegadaReal(LocalDateTime.now());
        
        // Liberar capacidad del camión - lo hacemos primero directamente aquí
        // ya que tenemos la referencia correcta al camión
        double volumenEntregado = nodo.getVolumenGLP();
        camion.liberarCapacidad(volumenEntregado);
        
        // Si el pedido está presente, actualizar su estado
        Pedido pedido = nodo.getPedido();
        if (pedido != null) {
            // Registrar la entrega parcial con el volumen del nodo
            // Esta función ya no maneja liberación de capacidad del camión
            pedido.registrarEntregaParcial(camion.getCodigo(), volumenEntregado, LocalDateTime.now());
            
            // Verificar si todas las entregas para este pedido han sido completadas
            boolean todasEntregasCompletadas = true;
            double volumenTotalEntregado = 0.0;
            
            // Buscar este pedido en todas las rutas activas - Usar método con FETCH JOIN
            List<Ruta> rutasConPedido = rutaRepository.findByEstadoInWithNodos(Arrays.asList(1, 2)); // En curso o completada
            for (Ruta r : rutasConPedido) {
                for (NodoRuta n : r.getNodos()) {
                    if (n.getPedido() != null && n.getPedido().getId().equals(pedido.getId())) {
                        if (!n.isEntregado()) {
                            todasEntregasCompletadas = false;
                        } else {
                            volumenTotalEntregado += n.getVolumenGLP();
                        }
                    }
                }
            }
            
            // Asegurar que los volúmenes estén actualizados
            pedido.setVolumenGLPEntregado(volumenTotalEntregado);
            pedido.setVolumenGLPPendiente(pedido.getVolumenGLPAsignado() - volumenTotalEntregado);
            
            // Si todas las entregas están completadas o el volumen entregado es suficiente, marcar pedido como completado
            if (todasEntregasCompletadas || Math.abs(volumenTotalEntregado - pedido.getVolumenGLPAsignado()) < 0.01) {
                pedido.setEstado(EstadoPedido.ENTREGADO_TOTALMENTE); // 2 = Entregado
                pedido.setFechaEntregaReal(LocalDateTime.now());
                pedido.setVolumenGLPPendiente(0); // Asegurar que no queda pendiente
            }
            
            pedidoRepository.save(pedido);
            
            // Crear una notificación de entrega
            Map<String, Object> notificacion = new HashMap<>();
            notificacion.put("tipo", "entrega");
            notificacion.put("camionId", camion.getId());
            notificacion.put("camionCodigo", camion.getCodigo());
            notificacion.put("pedidoId", pedido.getId());
            notificacion.put("pedidoCodigo", pedido.getCodigo());
            notificacion.put("posX", camion.getPosX());
            notificacion.put("posY", camion.getPosY());
            notificacion.put("volumenEntregado", Optional.of(volumenEntregado));
            notificacion.put("porcentajeEntregado", Optional.of(nodo.getPorcentajePedido()));
            notificacion.put("volumenTotalEntregado", Optional.of(volumenTotalEntregado));
            notificacion.put("volumenTotal", Optional.of(pedido.getVolumenGLPAsignado()));
            notificacion.put("fechaEntrega", LocalDateTime.now().toString());
            notificacion.put("estado", pedido.getEstado());
            
            // Enviar notificación por WebSocket
            messagingTemplate.convertAndSend("/topic/entregas", notificacion);
            
            logger.info("Pedido {} entregado por camión {} - Volumen: {}/{} m³ - Estado: {}", 
                pedido.getCodigo(), camion.getCodigo(), 
                volumenTotalEntregado, pedido.getVolumenGLPAsignado(),
                pedido.getEstado());
        }
    }
    
    // Recarga combustible y GLP si el camión está en un almacén
    private void recargarCamion(Camion camion) {
        // Buscar si hay un almacén en esta posición
        List<Almacen> almacenes = almacenRepository.findByPosXAndPosY(camion.getPosX(), camion.getPosY());
        
        if (!almacenes.isEmpty()) {
            Almacen almacen = almacenes.get(0);
            
            // Recargar combustible al máximo
            double combustibleNecesario = camion.getCapacidadTanque() - camion.getCombustibleActual();
            if (combustibleNecesario > 0) {
                camion.recargarCombustible(combustibleNecesario);
            }
            
            // Recargar GLP si es un almacén de GLP
            if (true) {
                double glpNecesario = camion.getCapacidad() - (camion.getCapacidad() - camion.getCapacidadDisponible());
                if (glpNecesario > 0) {
                    camion.recargarGLP(glpNecesario);
                }
            }
            
            // Actualizar el último almacén visitado
            camion.setUltimoAlmacen(almacen);
            camion.setFechaUltimaCarga(LocalDateTime.now());
            
            // Enviar notificación de recarga
            Map<String, Object> notificacion = new HashMap<>();
            notificacion.put("tipo", "recarga");
            notificacion.put("camionId", camion.getId());
            notificacion.put("camionCodigo", camion.getCodigo());
            notificacion.put("almacenId", almacen.getId());
            notificacion.put("almacenNombre", almacen.getNombre());
            notificacion.put("combustibleRecargado", Optional.of(combustibleNecesario));
            notificacion.put("combustibleActual", Optional.of(camion.getCombustibleActual()));
            
            messagingTemplate.convertAndSend("/topic/recargas", notificacion);
        }
    }
    
    // Marca una ruta como completada
    private void completarRuta(Ruta ruta, Camion camion) {
        ruta.setEstado(2); // 2 = Completada
        ruta.setFechaFinRuta(LocalDateTime.now());
        rutaRepository.save(ruta);
        
        // Verificar si hay más rutas pendientes para este camión
        List<Ruta> rutasPendientes = rutaRepository.findByCamionIdAndEstado(camion.getId(), 0); // 0 = Planificada
        
        if (!rutasPendientes.isEmpty()) {
            // Iniciar la siguiente ruta planificada
            Ruta siguienteRuta = rutasPendientes.get(0);
            siguienteRuta.setEstado(1); // 1 = En curso
            siguienteRuta.setFechaInicioRuta(LocalDateTime.now());
            rutaRepository.save(siguienteRuta);
            
            // Inicializar progreso para la nueva ruta
            progresoNodoActual.put(siguienteRuta.getId(), Double.valueOf(0.0));
            
            // Notificar inicio de nueva ruta
            Map<String, Object> notificacion = new HashMap<>();
            notificacion.put("tipo", "inicioRuta");
            notificacion.put("camionId", camion.getId());
            notificacion.put("camionCodigo", camion.getCodigo());
            notificacion.put("rutaId", siguienteRuta.getId());
            notificacion.put("rutaCodigo", siguienteRuta.getCodigo());
            
            messagingTemplate.convertAndSend("/topic/rutas", notificacion);
        } else {
            // No hay más rutas planificadas
            // Verificar si quedan pedidos pendientes para este camión antes de marcarlo como disponible
            boolean quedanPedidos = pedidoRepository
                .findByEstadoIn(Arrays.asList(EstadoPedido.REGISTRADO, EstadoPedido.PLANIFICADO_PARCIALMENTE,
                            EstadoPedido.PLANIFICADO_TOTALMENTE)) // Pendientes o asignados
                .stream()
                .filter(p -> p.getCamion() != null) // Solo pedidos con camión asignado
                .filter(p -> p.getCamion().getId().equals(camion.getId())) // De este camión
                .filter(p -> p.getEstado() != EstadoPedido.NO_ENTREGADO_EN_TIEMPO) // No entregados
                .filter(p -> p.getVolumenGLPPendiente() > 0.01) // Con volumen pendiente significativo
                .anyMatch(p -> true); // ¿Hay alguno que cumpla todas las condiciones?
                
            if (quedanPedidos) {
                // Mantener el camión en estado "en ruta" si todavía tiene pedidos pendientes
                camion.setEstado(EstadoCamion.EN_RUTA); // 1 = En ruta
                logger.info("Camión {} sigue en ruta porque tiene pedidos pendientes", camion.getCodigo());
            } else {
                // No hay pedidos pendientes, el camión debe regresar al almacén central
                camion.setEstado(EstadoCamion.DISPONIBLE); // 0 = Disponible
                // Posición del almacén central (normalmente en 12,8)
                // Si existe un almacén central en la BD, usamos su posición
                List<Almacen> almacenesCentrales = almacenRepository.findByEsCentral(true);
                if (!almacenesCentrales.isEmpty()) {
                    Almacen almacenCentral = almacenesCentrales.get(0);
                    camion.setPosX(almacenCentral.getPosX());
                    camion.setPosY(almacenCentral.getPosY());
                } else {
                    // Posición predeterminada si no hay almacén central
                    camion.setPosX(12); 
                    camion.setPosY(8);
                }
                logger.info("Camión {} marcado como disponible y posicionado en almacén central", camion.getCodigo());
            }
            
            camionRepository.save(camion);
            
            // Notificar fin de todas las rutas
            Map<String, Object> notificacion = new HashMap<>();
            notificacion.put("tipo", "finRutas");
            notificacion.put("camionId", camion.getId());
            notificacion.put("camionCodigo", camion.getCodigo());
            notificacion.put("nuevoEstado", camion.getEstado());
            notificacion.put("nuevoEstadoTexto", camion.getEstadoTexto());
            notificacion.put("posX", camion.getPosX());
            notificacion.put("posY", camion.getPosY());
            
            messagingTemplate.convertAndSend("/topic/rutas", notificacion);
        }
    }
    
    // Envía una notificación cuando un camión llega a un nodo de la ruta
    private void enviarNotificacionLlegadaNodo(Camion camion, NodoRuta nodo, Ruta ruta) {
        Map<String, Object> notificacion = new HashMap<>();
        notificacion.put("tipo", "llegadaNodo");
        notificacion.put("camionId", camion.getId());
        notificacion.put("camionCodigo", camion.getCodigo());
        notificacion.put("rutaId", ruta.getId());
        notificacion.put("rutaCodigo", ruta.getCodigo());
        notificacion.put("nodoId", nodo.getId());
        notificacion.put("nodoTipo", nodo.getTipo());
        notificacion.put("posX", nodo.getPosX());
        notificacion.put("posY", nodo.getPosY());
        
        if (nodo.getPedido() != null) {
            notificacion.put("pedidoId", nodo.getPedido().getId());
            notificacion.put("pedidoCodigo", nodo.getPedido().getCodigo());
        }
        
        messagingTemplate.convertAndSend("/topic/nodos", notificacion);
    }
    
    @Scheduled(fixedRate = 1000)
    public void enviarActualizacionProgramada() {
        // Genera estadísticas como lo haces en actualizarSimulacion
        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("camionesTotal", Optional.of(camionRepository.count()));
        estadisticas.put("camionesEnRuta", Optional.of(camionRepository.findByEstado(EstadoCamion.EN_RUTA).size()));
        estadisticas.put("almacenesTotal", Optional.of(almacenRepository.count()));
        estadisticas.put("pedidosTotal", Optional.of(pedidoRepository.count()));
        estadisticas.put("pedidosPendientes", Optional.of(pedidoRepository.findByEstado(EstadoPedido.REGISTRADO).size()));
        estadisticas.put("pedidosEnRuta", Optional.of(pedidoRepository.findByEstado(EstadoPedido.EN_RUTA).size()));
        estadisticas.put("pedidosEntregados", Optional.of(pedidoRepository.findByEstado(EstadoPedido.ENTREGADO_TOTALMENTE).size()));
        estadisticas.put("rutasTotal", Optional.of(rutaRepository.count()));
        estadisticas.put("rutasActivas", Optional.of(rutaRepository.findByEstado(1).size()));
    
        // Llama al método que realmente hace el trabajo
        enviarActualizacionPosiciones(estadisticas);
    }
    
    public void enviarActualizacionPosiciones(Map<String, Object> estadisticas) {
        try {
            long startTime = System.currentTimeMillis();
            
            // Medida de seguridad para evitar bloqueos indefinidos
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<Map<String, Object>> future = executor.submit(() -> obtenerPosicionesSimplificadas());
            
            Map<String, Object> posiciones;
            try {
                posiciones = future.get(10, TimeUnit.SECONDS); // Timeout de 10 segundos
                
                // Añade timestamp para rastreo en el cliente
                posiciones.put("timestamp", LocalDateTime.now().toString());
                
                // Evita enviar si no hay datos importantes
                boolean hayDatos = 
                    (posiciones.containsKey("camiones") && !((List<?>)posiciones.get("camiones")).isEmpty()) ||
                    (posiciones.containsKey("almacenes") && !((List<?>)posiciones.get("almacenes")).isEmpty()) ||
                    (posiciones.containsKey("pedidos") && !((List<?>)posiciones.get("pedidos")).isEmpty()) ||
                    (posiciones.containsKey("rutas") && !((List<?>)posiciones.get("rutas")).isEmpty());
                
                if (hayDatos) {
                    messagingTemplate.convertAndSend("/topic/posiciones", posiciones);
                } else {
                    // Si no hay datos, enviamos un mensaje mínimo para que el cliente sepa que estamos vivos
                    Map<String, Object> heartbeat = new HashMap<>();
                    heartbeat.put("timestamp", LocalDateTime.now().toString());
                    heartbeat.put("status", "heartbeat");
                    messagingTemplate.convertAndSend("/topic/posiciones", heartbeat);
                }
                
                long endTime = System.currentTimeMillis();
                if (endTime - startTime > 500) { // Solo logueamos si toma más de 500ms
                    logger.info("Actualización posiciones completada en {}ms", (endTime - startTime));
                }
            } catch (TimeoutException e) {
                logger.error("Timeout obteniendoPosicionesSimplificadas (>10s): {}", e.getMessage());
                // Enviar mensaje de error al cliente
                Map<String, Object> error = new HashMap<>();
                error.put("timestamp", LocalDateTime.now().toString());
                error.put("error", "Timeout procesando datos de posición");
                error.put("status", "error");
                messagingTemplate.convertAndSend("/topic/posiciones", error);
            } catch (Exception e) {
                logger.error("Error obteniendoPosicionesSimplificadas: {}", e.getMessage(), e);
                // Enviar mensaje de error al cliente
                Map<String, Object> error = new HashMap<>();
                error.put("timestamp", LocalDateTime.now().toString());
                error.put("error", "Error procesando datos: " + e.getMessage());
                error.put("status", "error");
                messagingTemplate.convertAndSend("/topic/posiciones", error);
            } finally {
                executor.shutdownNow(); // Asegurarse de cerrar el executor
            }
        } catch (Exception e) {
            logger.error("Error general en enviarActualizacionPosiciones: {}", e.getMessage(), e);
            try {
                // Último intento para informar al cliente
                Map<String, Object> error = new HashMap<>();
                error.put("timestamp", LocalDateTime.now().toString());
                error.put("error", "Error crítico del sistema: " + e.getMessage());
                error.put("status", "criticalError");
                messagingTemplate.convertAndSend("/topic/posiciones", error);
            } catch (Exception ex) {
                logger.error("No se pudo enviar mensaje de error al cliente: {}", ex.getMessage(), ex);
            }
        }
    }
    
    // NUEVO MÉTODO: Obtiene posiciones simplificadas para evitar el problema de anidamiento JSON
    public Map<String, Object> obtenerPosicionesSimplificadas() {
        Map<String, Object> result = new HashMap<>();
        long startGlobal = System.currentTimeMillis();
        
        try {
            logger.debug("Iniciando obtenerPosicionesSimplificadas: {}", LocalDateTime.now());
            
            // 1. PROCESAR CAMIONES
            long startCamiones = System.currentTimeMillis();
            try {
                List<Map<String, Object>> camionesEnRutaList = new ArrayList<>();
                List<Map<String, Object>> camionesDisponiblesList = new ArrayList<>();
                List<Map<String, Object>> camionesSinCombustibleList = new ArrayList<>();
                List<Map<String, Object>> camionesOtrosList = new ArrayList<>();
                
                // Obtener camiones por estado
                List<Camion> enRuta = camionRepository.findByEstado(EstadoCamion.EN_RUTA); // En ruta
                List<Camion> disponibles = camionRepository.findByEstado(EstadoCamion.DISPONIBLE); // Disponibles
                List<Camion> sinCombustible = camionRepository.findByEstado(EstadoCamion.SIN_COMBUSTIBLE); // Sin combustible
                List<Camion> otros = new ArrayList<>(); // Otros estados (mantenimiento, averiado)
                otros.addAll(camionRepository.findByEstado(EstadoCamion.EN_MANTENIMIENTO_CORRECTIVO)); // En mantenimiento
                otros.addAll(camionRepository.findByEstado(EstadoCamion.INMOVILIZADO_POR_AVERIA)); // Averiado
                
                // Procesar camiones en ruta
                for (Camion camion : enRuta) {
                    try {
                        Map<String, Object> camionMap = new HashMap<>();
                        camionMap.put("id", camion.getId());
                        camionMap.put("codigo", camion.getCodigo());
                        camionMap.put("estado", camion.getEstado());
                        camionMap.put("estadoTexto", camion.getEstadoTexto());
                        camionMap.put("capacidad", camion.getCapacidad());
                        camionMap.put("combustibleActual", camion.getCombustibleActual());
                        camionMap.put("posX", camion.getPosX());
                        camionMap.put("posY", camion.getPosY());
                        
                        // Solo añadir camiones con posición válida
                        if (!Double.isNaN(camion.getPosX()) && !Double.isNaN(camion.getPosY())) {
                            camionesEnRutaList.add(camionMap);
                        }
                    } catch (Exception e) {
                        logger.error("Al procesar camión en ruta {}: {}", camion.getId(), e.getMessage(), e);
                    }
                }
                
                // Procesar camiones disponibles
                for (Camion camion : disponibles) {
                    try {
                        Map<String, Object> camionMap = new HashMap<>();
                        camionMap.put("id", camion.getId());
                        camionMap.put("codigo", camion.getCodigo());
                        camionMap.put("estado", camion.getEstado());
                        camionMap.put("estadoTexto", camion.getEstadoTexto());
                        camionMap.put("capacidad", camion.getCapacidad());
                        camionMap.put("combustibleActual", camion.getCombustibleActual());
                        camionMap.put("posX", camion.getPosX());
                        camionMap.put("posY", camion.getPosY());
                        
                        // Solo añadir camiones con posición válida
                        if (!Double.isNaN(camion.getPosX()) && !Double.isNaN(camion.getPosY())) {
                            camionesDisponiblesList.add(camionMap);
                        }
                    } catch (Exception e) {
                        logger.error("Al procesar camión disponible {}: {}", camion.getId(), e.getMessage(), e);
                    }
                }
                
                // Procesar camiones sin combustible
                for (Camion camion : sinCombustible) {
                    try {
                        Map<String, Object> camionMap = new HashMap<>();
                        camionMap.put("id", camion.getId());
                        camionMap.put("codigo", camion.getCodigo());
                        camionMap.put("estado", camion.getEstado());
                        camionMap.put("estadoTexto", camion.getEstadoTexto());
                        camionMap.put("capacidad", camion.getCapacidad());
                        camionMap.put("combustibleActual", camion.getCombustibleActual());
                        camionMap.put("posX", camion.getPosX());
                        camionMap.put("posY", camion.getPosY());
                        
                        // Solo añadir camiones con posición válida
                        if (!Double.isNaN(camion.getPosX()) && !Double.isNaN(camion.getPosY())) {
                            camionesSinCombustibleList.add(camionMap);
                        }
                    } catch (Exception e) {
                        logger.error("Al procesar camión sin combustible {}: {}", camion.getId(), e.getMessage(), e);
                    }
                }
                
                // Procesar otros camiones (mantenimiento, averiado)
                for (Camion camion : otros) {
                    try {
                        Map<String, Object> camionMap = new HashMap<>();
                        camionMap.put("id", camion.getId());
                        camionMap.put("codigo", camion.getCodigo());
                        camionMap.put("estado", camion.getEstado());
                        camionMap.put("estadoTexto", camion.getEstadoTexto());
                        camionMap.put("capacidad", camion.getCapacidad());
                        camionMap.put("combustibleActual", camion.getCombustibleActual());
                        camionMap.put("posX", camion.getPosX());
                        camionMap.put("posY", camion.getPosY());
                        
                        // Solo añadir camiones con posición válida
                        if (!Double.isNaN(camion.getPosX()) && !Double.isNaN(camion.getPosY())) {
                            camionesOtrosList.add(camionMap);
                        }
                    } catch (Exception e) {
                        logger.error("Al procesar camión en otro estado {}: {}", camion.getId(), e.getMessage(), e);
                    }
                }
                
                // Añadir todas las categorías de camiones al resultado
                result.put("camionesEnRuta", camionesEnRutaList);
                result.put("camionesDisponibles", camionesDisponiblesList);
                result.put("camionesSinCombustible", camionesSinCombustibleList);
                result.put("camionesOtros", camionesOtrosList);
                
                // También añadir una lista completa para retrocompatibilidad
                List<Map<String, Object>> camionesList = new ArrayList<>();
                camionesList.addAll(camionesEnRutaList);
                camionesList.addAll(camionesDisponiblesList);
                camionesList.addAll(camionesSinCombustibleList);
                camionesList.addAll(camionesOtrosList);
                result.put("camiones", camionesList);
                
                logger.debug("Camiones procesados: {} en ruta, {} disponibles, {} sin combustible, {} otros, {} total en {}ms", 
                    camionesEnRutaList.size(), camionesDisponiblesList.size(), camionesSinCombustibleList.size(),
                    camionesOtrosList.size(), camionesList.size(), (System.currentTimeMillis() - startCamiones));
            } catch (Exception e) {
                logger.error("Al procesar camiones: {}", e.getMessage(), e);
                result.put("camiones", new ArrayList<>());
                result.put("camionesEnRuta", new ArrayList<>());
                result.put("camionesDisponibles", new ArrayList<>());
                result.put("camionesSinCombustible", new ArrayList<>());
                result.put("camionesOtros", new ArrayList<>());
                result.put("error_camiones", e.getMessage());
            }

            // 2. PROCESAR ALMACENES
            long startAlmacenes = System.currentTimeMillis();
            try {
                List<Map<String, Object>> almacenesList = new ArrayList<>();
                
                for (Almacen almacen : almacenRepository.findAll()) {
                    try {
                        Map<String, Object> almacenMap = new HashMap<>();
                        almacenMap.put("id", almacen.getId());
                        almacenMap.put("nombre", almacen.getNombre());
                        almacenMap.put("posX", almacen.getPosX());
                        almacenMap.put("posY", almacen.getPosY());
                        
                        // Solo añadir almacenes con posición válida
                        if (!Double.isNaN(almacen.getPosX()) && !Double.isNaN(almacen.getPosY())) {
                            almacenesList.add(almacenMap);
                        }
                    } catch (Exception e) {
                        logger.error("Al procesar almacén {}: {}", almacen.getId(), e.getMessage(), e);
                    }
                }
                result.put("almacenes", almacenesList);
                logger.debug("Almacenes procesados: {} en {}ms", almacenesList.size(), (System.currentTimeMillis() - startAlmacenes));
            } catch (Exception e) {
                logger.error("Al procesar almacenes: {}", e.getMessage(), e);
                result.put("almacenes", new ArrayList<>());
                result.put("error_almacenes", e.getMessage());
            }

            // 3. PROCESAR PEDIDOS
            long startPedidos = System.currentTimeMillis();
            try {
                List<Map<String, Object>> pedidosList = new ArrayList<>();
                
                for (Pedido pedido : pedidoRepository.findByEstadoIn(Arrays.asList(EstadoPedido.REGISTRADO, EstadoPedido.EN_RUTA))) {
                    try {
                        if (pedido.getCliente() == null) {
                            continue; // Ignorar pedidos sin cliente
                        }
                        
                        Map<String, Object> pedidoMap = new HashMap<>();
                        pedidoMap.put("id", pedido.getId());
                        pedidoMap.put("estado", Optional.of(pedido.getEstado()));
                        pedidoMap.put("m3", Optional.of(pedido.getVolumenGLPAsignado()));
                        
                        // Solo enviar la ubicación del cliente si tenemos el cliente
                        if (pedido.getCliente() != null) {
                            pedidoMap.put("clienteId", pedido.getCliente().getId());
                            pedidoMap.put("posX", pedido.getCliente().getPosX());
                            pedidoMap.put("posY", pedido.getCliente().getPosY());
                        }
                        
                        // Solo añadir pedidos con posición válida
                        if (pedido.getCliente() != null && 
                            pedido.getCliente() != null &&
                            pedido.getCliente().getPosX() != Double.NaN &&
                            pedido.getCliente().getPosY() != Double.NaN) {
                            pedidosList.add(pedidoMap);
                        }
                    } catch (Exception e) {
                        logger.error("Al procesar pedido {}: {}", pedido.getId(), e.getMessage(), e);
                    }
                }
                result.put("pedidos", pedidosList);
                logger.debug("Pedidos procesados: {} en {}ms", pedidosList.size(), (System.currentTimeMillis() - startPedidos));
            } catch (Exception e) {
                logger.error("Al procesar pedidos: {}", e.getMessage(), e);
                result.put("pedidos", new ArrayList<>());
                result.put("error_pedidos", e.getMessage());
            }

            // 4. PROCESAR RUTAS
            long startRutas = System.currentTimeMillis();
            try {
                List<Map<String, Object>> rutasList = new ArrayList<>();
                
                // Usar el nuevo método que incluye un FETCH JOIN para evitar LazyInitializationException
                for (Ruta ruta : rutaRepository.findByEstadoWithNodos(1)) {
                    try {
                        Map<String, Object> rutaMap = new HashMap<>();
                        rutaMap.put("id", ruta.getId());
                        rutaMap.put("codigo", ruta.getCodigo());
                        rutaMap.put("estado", ruta.getEstado());
                        
                        // Ahora los nodos ya vienen inicializados, evitando el LazyInitializationException
                        List<NodoRuta> nodos = ruta.getNodos();
                        if (nodos != null && !nodos.isEmpty()) {
                            List<Map<String, Object>> nodosList = new ArrayList<>();
                            
                            for (NodoRuta nodo : nodos) {
                                try {
                                    if (Double.isNaN(nodo.getPosX()) || Double.isNaN(nodo.getPosY())) {
                                        continue; // Ignorar nodos sin coordenadas
                                    }
                                    
                                    Map<String, Object> nodoMap = new HashMap<>();
                                    nodoMap.put("id", nodo.getId());
                                    nodoMap.put("orden", nodo.getOrden());
                                    nodoMap.put("posX", nodo.getPosX());
                                    nodoMap.put("posY", nodo.getPosY());
                                    nodoMap.put("tipo", nodo.getTipo());
                                    
                                    nodosList.add(nodoMap);
                                } catch (Exception e) {
                                    logger.error("Al procesar nodo {} de ruta {}: {}", nodo.getId(), ruta.getId(), e.getMessage(), e);
                                }
                            }
                            
                            // Ordenar nodos por el campo 'orden'
                            nodosList.sort(Comparator.comparing(m -> ((Integer) m.get("orden"))));
                            rutaMap.put("nodos", nodosList);
                        } else {
                            rutaMap.put("nodos", new ArrayList<>());
                        }
                        
                        // Solo añadir rutas con al menos un nodo
                        if (rutaMap.containsKey("nodos") && !((List<?>) rutaMap.get("nodos")).isEmpty()) {
                            rutasList.add(rutaMap);
                        }
                    } catch (Exception e) {
                        logger.error("Al procesar ruta {}: {}", ruta.getId(), e.getMessage(), e);
                    }
                }
                result.put("rutas", rutasList);
                logger.debug("Rutas procesadas: {} en {}ms", rutasList.size(), (System.currentTimeMillis() - startRutas));
            } catch (Exception e) {
                logger.error("Al procesar rutas: {}", e.getMessage(), e);
                result.put("rutas", new ArrayList<>());
                result.put("error_rutas", e.getMessage());
            }
            
            logger.debug("Finalizado obtenerPosicionesSimplificadas en {}ms", (System.currentTimeMillis() - startGlobal));
            return result;
            
        } catch (Exception e) {
            logger.error("Error general obteniendo posiciones: {}", e.getMessage());
            
            // Devolver al menos un objeto vacío pero válido
            result.put("error", "Error general obteniendo posiciones: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now().toString());
            return result;
        }
    }
    
    // Obtiene el estado actual de la simulación
    public Map<String, Object> obtenerEstadoSimulacion() {
        Map<String, Object> estado = new HashMap<>();
        estado.put("simulacionEnCurso", simulacionEnCurso);
        estado.put("factorVelocidad", factorVelocidad);
        
        // Contar camiones en ruta
        List<Camion> camionesEnRuta = camionRepository.findByEstado(EstadoCamion.EN_RUTA);
        estado.put("camionesEnRuta", camionesEnRuta.size());
        
        // Contar rutas activas
        List<Ruta> rutasActivas = rutaRepository.findByEstado(1);
        estado.put("rutasActivas", rutasActivas.size());
        
        // Información adicional sobre las rutas activas
        if (!rutasActivas.isEmpty()) {
            List<Map<String, Object>> rutasInfo = new ArrayList<>();
            for (Ruta ruta : rutasActivas) {
                Map<String, Object> rutaInfo = new HashMap<>();
                rutaInfo.put("id", ruta.getId());
                rutaInfo.put("codigo", ruta.getCodigo());
                if (ruta.getCamion() != null) {
                    rutaInfo.put("camionCodigo", ruta.getCamion().getCodigo());
                }
                if (progresoNodoActual.containsKey(ruta.getId())) {
                    rutaInfo.put("progreso", progresoNodoActual.get(ruta.getId()));
                }
                rutasInfo.add(rutaInfo);
            }
            estado.put("detalleRutasActivas", rutasInfo);
        }
        
        // Contar pedidos pendientes y en ruta
        List<Pedido> pedidosPendientes = pedidoRepository.findByEstado(EstadoPedido.PENDIENTE_PLANIFICACION);
        List<Pedido> pedidosEnRuta = pedidoRepository.findByEstado(EstadoPedido.EN_RUTA);
        estado.put("pedidosPendientes", pedidosPendientes.size());
        estado.put("pedidosEnRuta", pedidosEnRuta.size());
        
        // Información de diagnóstico
        estado.put("schedulerActivo", scheduler != null && !scheduler.isShutdown());
        estado.put("timestamp", LocalDateTime.now().toString());
        
        return estado;
    }
    
    // Crea una respuesta estándar para la API
    private Map<String, Object> crearRespuesta(String mensaje) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", mensaje);
        respuesta.put("fecha", LocalDateTime.now().toString());
        respuesta.put("status", "success");
        return respuesta;
    }
}
```

## main\java\com\plg\service\VisualizadorService.java

```java
package com.plg.service;

import com.plg.entity.Almacen;
import com.plg.entity.Camion;
import com.plg.entity.Pedido;
import com.plg.repository.AlmacenRepository;
import com.plg.repository.CamionRepository;
import com.plg.repository.PedidoRepository;
import com.plg.entity.EstadoCamion;
import com.plg.entity.EstadoPedido;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VisualizadorService {

    @Autowired
    private PedidoRepository pedidoRepository;
    
    @Autowired
    private CamionRepository camionRepository;
    
    @Autowired
    private AlmacenRepository almacenRepository;

    public Map<String, Object> obtenerDatosMapa(boolean mostrarPedidos, boolean mostrarCamiones, boolean mostrarBloqueos, boolean mostrarAlmacenes) {
        Map<String, Object> datos = new HashMap<>();
        
        if (mostrarPedidos) {
            List<Pedido> pedidos = pedidoRepository.findAll();
            List<Map<String, Object>> pedidosData = pedidos.stream()
                .map(this::convertirPedidoAMapa)
                .collect(Collectors.toList());
            datos.put("pedidos", pedidosData);
        }
        
        if (mostrarCamiones) {
            List<Camion> camiones = camionRepository.findAll();
            List<Map<String, Object>> camionesData = camiones.stream()
                .map(this::convertirCamionAMapa)
                .collect(Collectors.toList());
            datos.put("camiones", camionesData);
        }
        
        if (mostrarBloqueos) {
            List<Map<String, Object>> bloqueosData = obtenerBloqueosEjemplo();
            datos.put("bloqueos", bloqueosData);
        }
        
        if (mostrarAlmacenes) {
            List<Almacen> almacenes = almacenRepository.findByActivo(true);
            List<Map<String, Object>> almacenesData = almacenes.stream()
                .map(this::convertirAlmacenAMapa)
                .collect(Collectors.toList());
            datos.put("almacenes", almacenesData);
        }
        
        return datos;
    }
    
    public Map<String, Object> obtenerEstadoGeneral() {
        Map<String, Object> estado = new HashMap<>();
        
        List<Pedido> pedidos = pedidoRepository.findAll();
        estado.put("totalPedidos", pedidos.size());
        estado.put("pedidosPendientes", pedidos.stream().filter(p -> p.getEstado() == EstadoPedido.PENDIENTE_PLANIFICACION).count());
        estado.put("pedidosEntregados", pedidos.stream().filter(p -> p.getEstado() == EstadoPedido.ENTREGADO_TOTALMENTE).count());
        
        List<Camion> camiones = camionRepository.findAll();
        estado.put("totalCamiones", camiones.size());
        estado.put("camionesDisponibles", camiones.stream().filter(c -> c.getEstado() == EstadoCamion.DISPONIBLE).count());
        estado.put("camionesEnRuta", camiones.stream().filter(c -> c.getEstado() == EstadoCamion.EN_RUTA).count());
        estado.put("camionesEnMantenimiento", camiones.stream().filter(c -> 
            c.getEstado() == EstadoCamion.EN_MANTENIMIENTO_PREVENTIVO || 
            c.getEstado() == EstadoCamion.EN_MANTENIMIENTO_CORRECTIVO || 
            c.getEstado() == EstadoCamion.EN_MANTENIMIENTO_POR_AVERIA).count());
        estado.put("camionesAveriados", camiones.stream().filter(c -> 
            c.getEstado() == EstadoCamion.INMOVILIZADO_POR_AVERIA).count());
        
        return estado;
    }
    
    public Map<String, Object> aplicarFiltros(Map<String, Object> filtros) {
        Map<String, Object> datosConFiltro = new HashMap<>();
        
        if (filtros.containsKey("estadoPedidos")) {
            List<Integer> estados = (List<Integer>) filtros.get("estadoPedidos");
            List<Pedido> pedidosFiltrados = pedidoRepository.findAll().stream()
                .filter(p -> estados.contains(p.getEstado()))
                .collect(Collectors.toList());
            datosConFiltro.put("pedidos", pedidosFiltrados.stream()
                .map(this::convertirPedidoAMapa)
                .collect(Collectors.toList()));
        }
        
        if (filtros.containsKey("tipoCamiones")) {
            List<String> tipos = (List<String>) filtros.get("tipoCamiones");
            List<Camion> camionesFiltrados = camionRepository.findAll().stream()
                .filter(c -> tipos.contains(c.getTipo()))
                .collect(Collectors.toList());
            datosConFiltro.put("camiones", camionesFiltrados.stream()
                .map(this::convertirCamionAMapa)
                .collect(Collectors.toList()));
        }
        
        return datosConFiltro;
    }
    
    private Map<String, Object> convertirPedidoAMapa(Pedido pedido) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", pedido.getId());
        map.put("posX", pedido.getPosX());
        map.put("posY", pedido.getPosY());
        map.put("fechaHora", pedido.getFechaHora());
        map.put("cliente", pedido.getCliente() != null ? pedido.getCliente().getId() : null);
        map.put("estado", pedido.getEstado());
        map.put("m3", pedido.getVolumenGLPAsignado());
        return map;
    }
    
    private Map<String, Object> convertirCamionAMapa(Camion camion) {
        Map<String, Object> map = new HashMap<>();
        map.put("codigo", camion.getCodigo());
        map.put("tipo", camion.getTipo());
        map.put("estado", camion.getEstado()); // Ahora retorna el enum directamente
        map.put("estadoTexto", camion.getEstadoTexto());
        map.put("estadoColorHex", camion.getEstado().getColorHex()); // Podemos usar el color asociado al enum
        map.put("capacidad", camion.getCapacidad());
        map.put("pesoCarga", camion.getPesoCarga());
        map.put("posX", 0);
        map.put("posY", 0);
        return map;
    }
    
    private Map<String, Object> convertirAlmacenAMapa(Almacen almacen) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", almacen.getId());
        map.put("nombre", almacen.getNombre());
        map.put("posX", almacen.getPosX());
        map.put("posY", almacen.getPosY());
        map.put("esCentral", almacen.isEsCentral());
        map.put("capacidadGLP", almacen.getCapacidadGLP());
        map.put("capacidadActualGLP", almacen.getCapacidadActualGLP());
        map.put("capacidadCombustible", almacen.getCapacidadCombustible());
        map.put("capacidadActualCombustible", almacen.getCapacidadActualCombustible());
        return map;
    }
    
    private List<Map<String, Object>> obtenerBloqueosEjemplo() {
        List<Map<String, Object>> bloqueos = new ArrayList<>();
        
        Map<String, Object> bloqueo1 = new HashMap<>();
        bloqueo1.put("posXInicio", 10);
        bloqueo1.put("posYInicio", 10);
        bloqueo1.put("posXFin", 20);
        bloqueo1.put("posYFin", 20);
        bloqueo1.put("descripcion", "Construcción de vía");
        bloqueo1.put("activo", true);
        bloqueos.add(bloqueo1);
        
        Map<String, Object> bloqueo2 = new HashMap<>();
        bloqueo2.put("posXInicio", 30);
        bloqueo2.put("posYInicio", 30);
        bloqueo2.put("posXFin", 35);
        bloqueo2.put("posYFin", 40);
        bloqueo2.put("descripcion", "Deslave en carretera");
        bloqueo2.put("activo", true);
        bloqueos.add(bloqueo2);
        
        return bloqueos;
    }
}
```

## main\java\com\plg\util\DtoConverter.java

```java
package com.plg.util;

import com.plg.dto.ClienteDTO;
import com.plg.dto.PedidoDTO;
import com.plg.entity.Cliente;
import com.plg.entity.Pedido;

/**
 * Clase de utilidad para convertir entre entidades y DTOs
 */
public class DtoConverter {

    /**
     * Convierte un Cliente a ClienteDTO
     */
    public static ClienteDTO toClienteDTO(Cliente cliente) {
        if (cliente == null) {
            return null;
        }
        
        return ClienteDTO.builder()
                .id(cliente.getId())
                .nombre(cliente.getNombre())
                .telefono(cliente.getTelefono())
                .email(cliente.getEmail())
                .direccion(cliente.getDireccion())
                .posX(cliente.getPosX())
                .posY(cliente.getPosY())
                .build();
    }
    
    /**
     * Convierte un ClienteDTO a Cliente
     */
    public static Cliente toCliente(ClienteDTO dto) {
        if (dto == null) {
            return null;
        }
        
        Cliente cliente = new Cliente();
        cliente.setId(dto.getId());
        cliente.setNombre(dto.getNombre());
        cliente.setTelefono(dto.getTelefono());
        cliente.setEmail(dto.getEmail());
        cliente.setDireccion(dto.getDireccion());
        
        // Convertir Integer a int para posX y posY
        if (dto.getPosX() != 0.0) {
            cliente.setPosX(dto.getPosX());
        }
        if (dto.getPosY() != 0.0) {
            cliente.setPosY(dto.getPosY());
        }
        
        return cliente;
    }
    
    /**
     * Convierte una entidad Pedido a un DTO
     */
    public static PedidoDTO toPedidoDTO(Pedido pedido) {
        if (pedido == null) return null;
        
        return PedidoDTO.builder()
            .id(pedido.getId())
            .codigo(pedido.getCodigo())
            .posX(pedido.getPosX())
            .posY(pedido.getPosY())
            .volumenGLPAsignado(pedido.getVolumenGLPAsignado())
            .horasLimite(pedido.getHorasLimite())
            .clienteId(pedido.getCliente() != null ? pedido.getCliente().getId() : null)
            .clienteNombre(pedido.getCliente() != null ? pedido.getCliente().getNombre() : null)
            .fechaHora(pedido.getFechaHora())
            .estado(pedido.getEstado()) // Usar directamente el enum
            .fechaRegistro(pedido.getFechaRegistro())
            .fechaEntregaRequerida(pedido.getFechaEntregaRequerida())
            .fechaEnregaReal(pedido.getFechaEntregaReal())
            .build();
    }
    
    /**
     * Convierte un PedidoDTO a una entidad Pedido
     */
    public static Pedido toPedido(PedidoDTO dto) {
        if (dto == null) return null;
        
        Pedido pedido = new Pedido();
        pedido.setId(dto.getId());
        pedido.setCodigo(dto.getCodigo());
        pedido.setPosX(dto.getPosX());
        pedido.setPosY(dto.getPosY());
        pedido.setVolumenGLPAsignado(dto.getVolumenGLPAsignado());
        pedido.setHorasLimite(dto.getHorasLimite());
        pedido.setFechaHora(dto.getFechaHora());
        
        // Asignar estado usando el enum
        pedido.setEstado(dto.getEstado());
        
        // Si tenemos un cliente, lo asignaremos en el servicio
        // ya que necesitamos buscarlo en la base de datos
        
        return pedido;
    }
}
```

## test\java\com\plg\PlgApplicationTests.java

```java
package com.plg;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PlgApplicationTests {

	@Test
	void contextLoads() {
		// This test verifies that the Spring application context loads successfully
	}

}

```

