package com.plg;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.plg.entity.Almacen;
import com.plg.entity.Averia;
import com.plg.entity.Bloqueo;
import com.plg.entity.Camion;
import com.plg.entity.Coordenada;
import com.plg.entity.Mantenimiento;
import com.plg.entity.Mapa;
import com.plg.entity.Pedido;
import com.plg.utils.AlgoritmoGenetico;
import com.plg.utils.Individuo;
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
        Mapa mapa = new Mapa(25, 25);
        List<Almacen> almacenes = dataLoader.initializeAlmacenes();
        List<Camion> camiones = dataLoader.initializeCamiones();
        List<Pedido> pedidos = dataLoader.initializePedidos();
        dataLoader.initializeBloqueos(mapa);
        AlgoritmoGenetico algoritmoGenetico = new AlgoritmoGenetico(mapa, pedidos, camiones, almacenes);
        algoritmoGenetico.ejecutarAlgoritmo();
        Individuo mejorIndividuo = algoritmoGenetico.getMejorIndividuo();
        mapa.imprimirMapa(mejorIndividuo, pedidos);

    }
}
