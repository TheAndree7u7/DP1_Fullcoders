package com.plg.config;

import com.plg.entity.*;
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



    @Value("${app.data.pedidos-path}")
    private String pedidosPath;
    
    @Value("${app.data.mantenimientos-path}")
    private String mantenimientosPath;
    
    @Value("${app.data.bloqueos-path}")
    private String bloqueosPath;
    
    @Value("${app.data.averias-path}")
    private String averiasPath;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Initializing test data...");
        
        // Initialize warehouses (almacenes)
        initializeAlmacenes();
        
        // Initialize trucks (camiones)
        initializeCamiones();
        
        // Initialize demo data from files
        loadPedidosFromFile("ventas202504.txt");
        loadMantenimientosFromFile("mantpreventivo.txt");
        
        System.out.println("Data initialization completed!");
    }
    
    private void initializeCamiones() {
        List<Camion> camiones = new ArrayList<>();
        
        // Create different truck types
        String[] tipos = {"TA", "TB", "TC", "TD"};
        double[] capacidades = {25.0, 20.0, 15.0, 10.0};
        double[] taras = {15.0, 12.0, 9.0, 7.0};
        
        for (int i = 0; i < tipos.length; i++) {
            for (int j = 1; j <= 3; j++) {
                Camion camion = new Camion();
                camion.setCodigo(tipos[i] + "0" + j);
                camion.setTipo(tipos[i]);
                camion.setCapacidad(capacidades[i]);
                camion.setTara(taras[i]);
                camion.setPesoCarga(0);
                camion.setPesoCombinado(taras[i]);
                camion.setEstado(0); // Disponible
                camiones.add(camion);
            }
        }
        
        camionRepository.saveAll(camiones);
    }
    
    private void initializeAlmacenes() {
        List<Almacen> almacenes = new ArrayList<>();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        
        try {
            Path path = Paths.get("src/main/resources/data/almacenes/almacenes.txt");
            BufferedReader reader = new BufferedReader(new FileReader(path.toFile()));
            
            String line;
            while ((line = reader.readLine()) != null) {
                String[] datos = line.split("\t");
                if (datos.length >= 15) {
                    Almacen almacen = new Almacen();
                    // almacen.setId(Long.parseLong(datos[0])); // ID es autogenerado
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
                    almacen.setHoraReabastecimiento(LocalTime.parse(datos[12], timeFormatter));
                    LocalDateTime ultimoReabastecimiento = LocalDateTime.parse(datos[13], dateTimeFormatter);
                    almacen.setUltimoReabastecimientoRealizado(false); // Se reinicia al cargar
                    almacen.setActivo(Boolean.parseBoolean(datos[14]));
                    
                    almacenes.add(almacen);
                }
            }
            
            reader.close();
            
            if (!almacenes.isEmpty()) {
                almacenRepository.saveAll(almacenes);
                System.out.println("Almacenes inicializados: " + almacenes.size());
            } else {
                System.out.println("No se encontraron almacenes para inicializar");
            }
            
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
            while ((line = reader.readLine()) != null) {
                String[] datos = line.split(",");
                if (datos.length >= 6) {
                    Pedido pedido = new Pedido();
                    pedido.setFechaHora(datos[0]);
                    pedido.setPosX(Integer.parseInt(datos[1]));
                    pedido.setPosY(Integer.parseInt(datos[2]));
                    
                    Cliente cliente = new Cliente();
                    cliente.setId(datos[3]);
                    // Additional client data would be loaded from another source
                    pedido.setCliente(cliente);
                    
                    pedido.setM3(Integer.parseInt(datos[4]));
                    pedido.setHorasLimite(Integer.parseInt(datos[5]));
                    pedido.setEstado(0); // Pendiente
                    
                    pedidoRepository.save(pedido);
                }
            }
            
            reader.close();
            
        } catch (Exception e) {
            System.err.println("Error loading pedidos: " + e.getMessage());
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
                    String codigoCamion = datos[0];
                    LocalDate fechaInicio = LocalDate.parse(datos[1], formatter);
                    LocalDate fechaFin = LocalDate.parse(datos[2], formatter);
                    
                    // Verificar si existe el camión
                    Optional<Camion> camionOpt = camionRepository.findById(codigoCamion);
                    if (camionOpt.isPresent()) {
                        Mantenimiento mantenimiento = new Mantenimiento();
                        mantenimiento.setCamion(camionOpt.get());
                        mantenimiento.setFechaInicio(fechaInicio);
                        mantenimiento.setFechaFin(fechaFin);
                        mantenimiento.setTipo("preventivo");
                        mantenimiento.setDescripcion("Mantenimiento preventivo programado");
                        mantenimiento.setEstado(0); // Programado
                        
                        mantenimientoRepository.save(mantenimiento);
                    }
                }
            }
            
            reader.close();
            
        } catch (Exception e) {
            System.err.println("Error loading mantenimientos: " + e.getMessage());
        }
    }
}