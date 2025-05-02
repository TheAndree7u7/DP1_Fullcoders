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
        logger.info("Loading preventive maintenances from file: {}", fileName);
    
        try {
            ClassPathResource resource = new ClassPathResource("data/mantenimientos/" + fileName);
    
            // Formato del nombre de fecha que viene en el archivo (20250401 → 1 abril 2025)
            DateTimeFormatter formatter = DateTimeFormatter.BASIC_ISO_DATE;   // yyyyMMdd
    
            try (BufferedReader reader = new BufferedReader(new FileReader(resource.getFile()))) {
                String line;
    
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) {                    // Línea vacía ⇒ saltar
                        continue;
                    }
    
                    // Esperamos exactamente “FECHA:CODIGO”
                    String[] parts = line.split(":");
                    if (parts.length != 2) {
                        logger.warn("Línea con formato inválido (se esperaba FECHA:CODIGO): {}", line);
                        continue;
                    }
    
                    // ─── parseo de campos ──────────────────────────────────────────────
                    String fechaRaw      = parts[0].trim();  // 20250401
                    String codigoCamion  = parts[1].trim();  // TA01, TD01, …
    
                    LocalDate fechaInicio;
                    try {
                        fechaInicio = LocalDate.parse(fechaRaw, formatter);
                    } catch (Exception ex) {
                        logger.warn("Fecha inválida en línea '{}': {}", line, ex.getMessage());
                        continue;
                    }
    
                    // Para mantenimiento de 1 solo día usamos la misma fecha como fin
                    LocalDate fechaFin = fechaInicio;
    
                    // ─── persistencia ──────────────────────────────────────────────────
                    Optional<Camion> camionOpt = camionRepository.findByCodigo(codigoCamion);
                    if (camionOpt.isEmpty()) {
                        logger.warn("Camión no encontrado: {}", codigoCamion);
                        continue;
                    }
    
                    Camion camion = camionOpt.get();
    
                    Mantenimiento mantenimiento = Mantenimiento.builder()
                            .camion(camion)
                            .fechaInicio(fechaInicio)
                            .fechaFin(fechaFin)
                            .tipo("preventivo")
                            .descripcion("Mantenimiento preventivo programado")
                            .estado(0) // 0 = Programado
                            .build();
    
                    // Nos aseguramos de que la lista no sea nula
                    if (camion.getMantenimientos() == null) {
                        camion.setMantenimientos(new ArrayList<>());
                    }
                    camion.getMantenimientos().add(mantenimiento);
    
                    mantenimientoRepository.save(mantenimiento);
                    logger.info("Mantenimiento creado: {} – {}", codigoCamion, fechaInicio);
                }
            }
    
            logger.info("Todos los mantenimientos preventivos cargados correctamente");
        } catch (IOException e) {
            logger.error("Error cargando mantenimientos: {}", e.getMessage(), e);
        }
    }
    


}
