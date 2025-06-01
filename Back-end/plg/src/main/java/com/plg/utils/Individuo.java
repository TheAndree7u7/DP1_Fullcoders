package com.plg.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import com.plg.config.DataLoader;
import com.plg.entity.Almacen;
import com.plg.entity.Camion;
import com.plg.entity.Nodo;
import com.plg.entity.Pedido;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Individuo {
    private double fitness;
    private String descripcion; // En caso el individuo tenga fitness negativo agregamos una descripcion del
                                // error
    private List<Gen> cromosoma;
    private List<Pedido> pedidos; // Lista de pedidos

    public Individuo(List<Pedido> pedidos) {
        this.pedidos = pedidos;
        inicializarCromosoma();
        this.fitness = calcularFitness();
    }

    private void inicializarCromosoma() {
        List<Almacen> almacenes = DataLoader.almacenes;
        List<Camion> camionesOperativos = DataLoader.camiones;

        cromosoma = new ArrayList<>();

        for (Camion camion : camionesOperativos) {
            cromosoma.add(new Gen(camion, new ArrayList<>()));
        }

        Almacen almacenCentral = almacenes.get(0);
        List<Nodo> pedidosMezclados = new ArrayList<>();
        pedidosMezclados.addAll(pedidos);
        Collections.shuffle(pedidosMezclados);
        List<Gen> camionesMezclados = new ArrayList<>(cromosoma);
        Collections.shuffle(camionesMezclados);
        for (int i = pedidosMezclados.size()-1; i >= 0;  i--) {
            Gen gen = camionesMezclados.get(i % camionesMezclados.size());
            Nodo nodo = pedidosMezclados.get(i);

            // Agregamos a la lista de pedidos
            if(nodo instanceof Pedido) {
                gen.getPedidos().add((Pedido) nodo);
            }
            gen.getNodos().add(nodo);
        }

        for (Gen gen : cromosoma) {
            gen.getNodos().add(almacenCentral);
        }
    }

    public double calcularFitness() {
        double fitness = 0.0;
        for (Gen gen : cromosoma) {
            double fitnessGen = gen.calcularFitness();
            if (fitnessGen == Double.MIN_VALUE) {
                this.descripcion = gen.getDescripcion(); // Guardar la descripción del error
                return Double.MIN_VALUE; // Si algún gen tiene fitness mínimo, el individuo es inválido
            }
            fitness += fitnessGen; // Sumar el fitness de cada gen
        }
        return fitness;
    }

 
    public void mutar() {
        // Simplified mutation: swap a random pedido between two random routes
        Random rnd = new Random();
        int g1 = rnd.nextInt(cromosoma.size());
        int g2 = rnd.nextInt(cromosoma.size());
        while (g2 == g1) {
            g2 = rnd.nextInt(cromosoma.size());
        }
        var route1 = cromosoma.get(g1).getNodos();
        var route2 = cromosoma.get(g2).getNodos();
        if (route1.size() > 2 && route2.size() > 2) {
            int i1 = rnd.nextInt(route1.size() - 2) + 1;
            int i2 = rnd.nextInt(route2.size() - 2) + 1;
            var temp = route1.get(i1);
            route1.set(i1, route2.get(i2));
            route2.set(i2, temp);
            this.fitness = calcularFitness();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Individuo: \n");
        for (Gen gen : cromosoma) {
            sb.append(gen.toString()).append("\n");
        }
        return sb.toString();
    }
}
