package com.plg.planner;

import com.plg.simulator.*;
import com.plg.model.*;

import java.util.List;

public class Planner {
    public void ejecutar() {
        System.out.println("Cargando datos...");
        DataLoader loader = new DataLoader();
        loader.cargarDatos(); // todavía sin uso real

        List<Camion> camiones = TestData.obtenerCamionesDePrueba();
        List<Pedido> pedidos = TestData.obtenerPedidosDePrueba();

        System.out.println("Camiones:");
        camiones.forEach(c -> System.out.println("- " + c));
        System.out.println("Pedidos:");
        pedidos.forEach(p -> System.out.println("- " + p));

        SimulatedAnnealing sa = new SimulatedAnnealing();
        sa.planificar();
    }
}
