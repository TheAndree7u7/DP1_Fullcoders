package com.plg.config;

import com.plg.entity.*;
import com.plg.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class DataLoader implements CommandLineRunner {

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
        System.out.println("Initializing test data...");
        
        // Initialize warehouses (almacenes)
        loadAlmacenesFromFile("almacenes.txt");
        
        // Initialize trucks (camiones)
        loadCamionesFromFile("camiones.txt");
        
        // Initialize demo data from files
        loadPedidosFromFile("ventas202504.txt");
        loadMantenimientosFromFile("mantpreventivo.txt");
        
        System.out.println("Data initialization completed!");
    }
    
    private void loadCamionesFromFile(String fileName) {
        try {
            Path path = Paths.get(camionesPath + fileName);
            BufferedReader reader = new BufferedReader(new FileReader(path.toFile()));
            
            String line;
            // Skip header line
            reader.readLine();
            
            while ((line = reader.readLine()) != null) {
                String[] datos = line.split(";");
                if (datos.length >= 9) {
                    String codigo = datos[0].trim();
                    String tipo = datos[1].trim();
                    double capacidad = Double.parseDouble(datos[2].trim());
                    double cargaActual = Double.parseDouble(datos[3].trim());
                    double capacidadDisponible = capacidad - cargaActual;
                    double tara = Double.parseDouble(datos[4].trim());
                    double pesoCarga = cargaActual * 0.5; // Convertir volumen a peso aprox.
                    int estado = Integer.parseInt(datos[5].trim());
                    double combustibleActual = Double.parseDouble(datos[6].trim());
                    int posX = Integer.parseInt(datos[7].trim());
                    int posY = Integer.parseInt(datos[8].trim());
                    
                    // Datos opcionales
                    double velocidadPromedio = 50.0; // Valor predeterminado
                    double capacidadTanque = 25.0; // Valor predeterminado
                    
                    if (datos.length >= 10) {
                        velocidadPromedio = Double.parseDouble(datos[9].trim());
                    }
                    
                    if (datos.length >= 11) {
                        capacidadTanque = Double.parseDouble(datos[10].trim());
                    }
                    
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
                    
                    // Asignar almacén inicial (central por defecto)
                    Optional<Almacen> almacenCentral = almacenRepository.findById(1L);
                    if (almacenCentral.isPresent()) {
                        camion.setUltimoAlmacen(almacenCentral.get());
                    }
                    
                    camionRepository.save(camion);
                }
            }
            
            reader.close();
            System.out.println("Camiones cargados desde archivo");
            
        } catch (Exception e) {
            System.err.println("Error loading camiones: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadAlmacenesFromFile(String fileName) {
        try {
            Path path = Paths.get(almacenesPath + fileName);
            BufferedReader reader = new BufferedReader(new FileReader(path.toFile()));
            
            String line;
            while ((line = reader.readLine()) != null) {
                String[] datos = line.split("\t");
                if (datos.length >= 14) {
                    Almacen almacen = new Almacen();
                    almacen.setId(Long.parseLong(datos[0]));
                    almacen.setNombre(datos[1]);
                    almacen.setPosX(Integer.parseInt(datos[2]));
                    almacen.setPosY(Integer.parseInt(datos[3]));
                    almacen.setCapacidadGLP(Double.parseDouble(datos[4]));
                    almacen.setCapacidadActualGLP(Double.parseDouble(datos[5]));
                    almacen.setCapacidadMaximaGLP(Double.parseDouble(datos[6]));
                    almacen.setCapacidadCombustible(Double.parseDouble(datos[7]));
                    almacen.setCapacidadActualCombustible(Double.parseDouble(datos[8]));
                    almacen.setCapacidadMaximaCombustible(Double.parseDouble(datos[9]));
                    almacen.setEsCentral(Boolean.parseBoolean(datos[10]));
                    almacen.setPermiteCamionesEstacionados(Boolean.parseBoolean(datos[11]));
                    almacen.setHoraReabastecimiento(LocalTime.parse(datos[12]));
                    
                    // Establecer activo siempre a true si el campo no existe o usar el valor del archivo si existe
                    if (datos.length > 14) {
                        almacen.setActivo(Boolean.parseBoolean(datos[14]));
                    } else {
                        // Si no viene en el archivo, usar el valor por defecto (true)
                        // El valor predeterminado ya está configurado en la clase Almacen
                    }
                    
                    almacenRepository.save(almacen);
                }
            }
            
            reader.close();
            System.out.println("Almacenes cargados desde archivo");
            
        } catch (Exception e) {
            System.err.println("Error loading almacenes: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadPedidosFromFile(String fileName) {
        try {
            Path path = Paths.get(pedidosPath + fileName);
            BufferedReader reader = new BufferedReader(new FileReader(path.toFile()));
            
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
                    
                    // Cliente
                    String clienteId = datos[3].trim();
                    Cliente cliente = new Cliente();
                    cliente.setId(clienteId);
                    pedido.setCliente(cliente);
                    
                    // Volumen solicitado
                    int volumenM3 = Integer.parseInt(datos[4].trim());
                    pedido.setM3(volumenM3);
                    pedido.setM3Pendientes(volumenM3); // Inicialmente todo está pendiente
                    pedido.setM3Asignados(0.0);
                    pedido.setM3Entregados(0.0);
                    
                    // Horas límite para la entrega
                    pedido.setHorasLimite(Integer.parseInt(datos[5].trim()));
                    
                    // Estado inicial: pendiente
                    pedido.setEstado(0);
                    
                    // Fecha de creación
                    pedido.setFechaCreacion(LocalDateTime.now());
                    
                    // Lista de asignaciones vacía inicialmente
                    pedido.setAsignaciones(new ArrayList<>());
                    
                    pedidoRepository.save(pedido);
                }
            }
            
            reader.close();
            System.out.println("Pedidos cargados desde archivo");
            
        } catch (Exception e) {
            System.err.println("Error loading pedidos: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadMantenimientosFromFile(String fileName) {
        try {
            Path path = Paths.get(mantenimientosPath + fileName);
            BufferedReader reader = new BufferedReader(new FileReader(path.toFile()));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            
            String line;
            while ((line = reader.readLine()) != null) {
                String[] datos = line.split(",");
                if (datos.length >= 3) {
                    String codigoCamion = datos[0].trim();
                    LocalDate fechaInicio = LocalDate.parse(datos[1].trim(), formatter);
                    LocalDate fechaFin = LocalDate.parse(datos[2].trim(), formatter);
                    
                    // Verificar si existe el camión
                    Optional<Camion> camionOpt = camionRepository.findById(codigoCamion);
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
                        
                        // Agregar a la lista de mantenimientos del camión
                        if (camion.getMantenimientos() == null) {
                            camion.setMantenimientos(new ArrayList<>());
                        }
                        camion.getMantenimientos().add(mantenimiento);
                        
                        mantenimientoRepository.save(mantenimiento);
                    } else {
                        System.out.println("Camión no encontrado: " + codigoCamion);
                    }
                }
            }
            
            reader.close();
            System.out.println("Mantenimientos cargados desde archivo");
            
        } catch (Exception e) {
            System.err.println("Error loading mantenimientos: " + e.getMessage());
            e.printStackTrace();
        }
    }
}