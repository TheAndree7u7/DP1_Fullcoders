package com.plg;

import com.plg.model.*;
import com.plg.service.DataLoaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
public class PlgApplication implements CommandLineRunner {

    @Autowired
    private DataLoaderService dataLoaderService;

    public static void main(String[] args) {
        SpringApplication.run(PlgApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Iniciando carga de archivos...");

        List<Almacen> almacenes = dataLoaderService.cargarAlmacenes();
        List<Camion> camiones = dataLoaderService.cargarCamiones();
        List<Averia> averias = dataLoaderService.cargarAverias();
        List<Bloqueo> bloqueos = dataLoaderService.cargarBloqueos();
        List<Mantenimiento> mantenimientos = dataLoaderService.cargarMantenimientos();
        List<Pedido> pedidos = dataLoaderService.cargarPedidos();

        System.out.println("📦 Almacenes:");
        almacenes.forEach(System.out::println);

        System.out.println("\n🚚 Camiones:");
        camiones.forEach(System.out::println);

        System.out.println("\n🔧 Averias:");
        averias.forEach(System.out::println);

        System.out.println("\n🚧 Bloqueos:");
        bloqueos.forEach(System.out::println);

        System.out.println("\n🛠️ Mantenimientos:");
        mantenimientos.forEach(System.out::println);

        System.out.println("\n🛒 Pedidos:");
        pedidos.forEach(System.out::println);

        System.out.println("\n✅ ¡Carga completa!");
    }
}
