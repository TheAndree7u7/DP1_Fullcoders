package com.plg;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
        List<Camion> camiones = dataLoader.initializeCamiones();
        //List<Averia> averias = dataLoader.initializeAverias(camiones);
        List<Pedido> pedidos = dataLoader.initializePedidos();
        //List<Mantenimiento> mantenimientos = dataLoader.initializeMantenimientos(camiones);
        dataLoader.initializeBloqueos(mapa);
        AlgoritmoGenetico algoritmoGenetico = new AlgoritmoGenetico(mapa, pedidos, camiones);
        algoritmoGenetico.ejecutarAlgoritmo();
        Individuo mejorIndividuo = algoritmoGenetico.getMejorIndividuo();

        // NO EXISTE NO LO MIREN 
        List<List<Coordenada>> rutas = new ArrayList<>();
        for (List<Integer> pedidos_gen : mejorIndividuo.getCromosoma()) {
            List<Coordenada> ruta = new ArrayList<>();
            for (int i = 0; i < pedidos_gen.size(); i++) {
                if (pedidos_gen.get(i) != -1) {
                    if(i == 0){
                        Camion camion = camiones.get(i);
                        Coordenada ini = camion.getCoordenadaActual();
                        Coordenada fin = pedidos.get(pedidos_gen.get(i)).getCoordenada();
                        ruta.addAll(mapa.aStar(ini, fin));
                    }else{
                        Coordenada ini = pedidos.get(pedidos_gen.get(i - 1)).getCoordenada();
                        Coordenada fin = pedidos.get(pedidos_gen.get(i)).getCoordenada();
                        ruta.addAll(mapa.aStar(ini, fin));
                    }
                }else{
                    break;
                }
            }
            rutas.add(ruta);
        }
        mapa.imprimirMapa(rutas, pedidos);

    }
}
