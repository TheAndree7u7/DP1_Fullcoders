package com.plg;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.plg.entity.Averia;
import com.plg.entity.Bloqueo;
import com.plg.entity.Camion;
import com.plg.entity.Mantenimiento;
import com.plg.entity.Pedido;
import com.plg.config.DataLoader;

@SpringBootApplication
public class PlgApplication implements CommandLineRunner {

    @Autowired
    private DataLoader dataLoader;

    public static void main(String[] args) {
        SpringApplication.run(PlgApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // Llamamos al método initializeCamiones() de DataLoader para obtener la lista de camiones
        List<Camion> camiones = dataLoader.initializeCamiones();
        List<Averia> averias = dataLoader.initializeAverias(camiones);
        //List<Pedido> pedidos = dataLoader.initializePedidos();
        // List<Mantenimiento> mantenimientos = dataLoader.initializeMantenimientos();
        // List<Bloqueo> bloqueos = dataLoader.initializeBloqueos();

    

        // Aquí puedes hacer lo que necesites con los camiones
        System.out.println("Camiones cargados: " + camiones.size());
        System.out.println("Averias cargadas: " + averias.size());

    }
}
