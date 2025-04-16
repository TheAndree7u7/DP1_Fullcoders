package com.plg.config;

import com.plg.entity.*;
import com.plg.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.core.io.ClassPathResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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

    @Value("${app.data.pedidos-path}")
    private String pedidosPath;
    
    @Value("${app.data.mantenimientos-path}")
    private String mantenimientosPath;
    
    @Value("${app.data.bloqueos-path}")
    private String bloqueosPath;
    
    @Value("${app.data.averias-path}")
    private String averiasPath;
    
    @Value("${app.data.almacenes-path}")
    private String almacenesPath;
    
    @Value("${app.data.camiones-path}")
    private String camionesPath;

    @Override
    public void run(String... args) throws Exception {
        logger.info("Initializing test data...");

        try {
            // Initialize warehouses (almacenes)
            loadAlmacenesFromFile("almacenes.txt");

            // Initialize trucks (camiones)
            loadCamionesFromFile("camiones.txt");

            // Initialize demo data from files
            loadPedidosFromFile("ventas202504.txt");
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

            // Read the file from the classpath
            BufferedReader reader = new BufferedReader(new FileReader(resource.getFile()));
            
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
                    int estado = Integer.parseInt(datos[5].trim());
                    double combustibleActual = Double.parseDouble(datos[6].trim());
                    int posX = Integer.parseInt(datos[7].trim());
                    int posY = Integer.parseInt(datos[8].trim());
                    
                    double velocidadPromedio = (datos.length >= 10) ? Double.parseDouble(datos[9].trim()) : 50.0;
                    double capacidadTanque = (datos.length >= 11) ? Double.parseDouble(datos[10].trim()) : 25.0;
                    
                    Camion camion = new Camion(codigo, tipo, capacidad, tara);
                    camion.setCapacidadDisponible(capacidadDisponible);
                    camion.setPesoCarga(pesoCarga);
                    camion.setPesoCombinado(tara + pesoCarga);
                    camion.setEstado(estado);
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
            
            reader.close();
            logger.info("Camiones loaded successfully");

        } catch (Exception e) {
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
    
    
    
    
    
    
    private void loadPedidosFromFile(String fileName) {
        logger.info("Cargando pedidos desde el archivo: {}", fileName);
    
        try {
            ClassPathResource resource = new ClassPathResource("data/pedidos/" + fileName);
            BufferedReader reader = new BufferedReader(new FileReader(resource.getFile()));
            
            String line;
            int contador = 1;
            while ((line = reader.readLine()) != null) {
                String[] datos = line.split(",");
                if (datos.length >= 6) {
                    Pedido pedido = new Pedido();
                    pedido.setFechaHora(datos[0].trim());
                    pedido.setPosX(Integer.parseInt(datos[1].trim()));
                    pedido.setPosY(Integer.parseInt(datos[2].trim()));
                    
                    // Generar código único para el pedido
                    String codigo = "P" + String.format("%04d", contador++);
                    pedido.setCodigo(codigo);
                    
                    // Verificar si el cliente ya existe, si no, crearlo y guardarlo
                    String clienteId = datos[3].trim();
                    Cliente cliente = clienteRepository.findById(clienteId).orElseGet(() -> {
                        Cliente nuevoCliente = new Cliente();
                        nuevoCliente.setId(clienteId);
                        clienteRepository.save(nuevoCliente);  // Guardar el cliente si es nuevo
                        return nuevoCliente;
                    });
                    pedido.setCliente(cliente);
                    
                    // Asignar volumen y estado al pedido
                    int volumenM3 = Integer.parseInt(datos[4].trim());
                    pedido.setM3(volumenM3);
                    pedido.setM3Pendientes(volumenM3);
                    pedido.setM3Asignados(0.0);
                    pedido.setM3Entregados(0.0);
                    
                    // Asignar hora límite y estado inicial
                    pedido.setHorasLimite(Integer.parseInt(datos[5].trim()));
                    pedido.setEstado(0); // Pendiente
                    pedido.setFechaCreacion(LocalDateTime.now());
                    pedido.setAsignaciones(new ArrayList<>());
                    
                    // Guardar el pedido
                    pedidoRepository.save(pedido);
                    logger.info("Pedido guardado: {}", codigo);
                }
            }
            
            reader.close();
            logger.info("Pedidos cargados exitosamente");
    
        } catch (Exception e) {
            logger.error("Error cargando pedidos: " + e.getMessage(), e);
        }
    }
    
    
    private void loadMantenimientosFromFile(String fileName) {
        logger.info("Loading mantenimientos from file: {}", fileName);

        try {
            ClassPathResource resource = new ClassPathResource("data/mantenimientos/" + fileName);
            BufferedReader reader = new BufferedReader(new FileReader(resource.getFile()));
            
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
            
            reader.close();
            logger.info("Mantenimientos loaded successfully");

        } catch (Exception e) {
            logger.error("Error loading mantenimientos: " + e.getMessage(), e);
        }
    }
}
