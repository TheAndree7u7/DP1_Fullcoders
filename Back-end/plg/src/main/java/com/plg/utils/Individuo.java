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
    private String descripcion;
    private List<Gen> cromosoma;
    private List<Pedido> pedidos; // Lista de pedidos

    public Individuo(List<Pedido> pedidos) {
        this.pedidos = pedidos;
        this.descripcion = "";
        inicializarCromosoma();
        this.fitness = calcularFitness();
    }

    private void inicializarCromosoma() {
        List<Almacen> almacenes = DataLoader.almacenes;
        List<Camion> camiones = DataLoader.camiones;
        cromosoma = new ArrayList<>();
        for (Camion camion : camiones) {
            cromosoma.add(new Gen(camion, new ArrayList<>()));
        }
        Almacen almacenCentral = almacenes.get(0);
        List<Nodo> pedidosMezclados = new ArrayList<>();
        pedidosMezclados.addAll(pedidos);
        Collections.shuffle(pedidosMezclados);
        List<Gen> genesMezclados = new ArrayList<>(cromosoma);
        Collections.shuffle(genesMezclados);
        for (int i = pedidosMezclados.size()-1; i >= 0;  i--) {
            Gen gen = genesMezclados.get(i % genesMezclados.size());
            Nodo nodo = pedidosMezclados.get(i);

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
        this.fitness = 0.0; 
        this.descripcion = ""; // Reiniciar la descripción
        guardarEstadoActual(); // Guardar el estado actual antes de calcular el fitness
        for (Gen gen : cromosoma) {
            double fitnessGen = gen.calcularFitness();
            if (fitnessGen == Double.MIN_VALUE) {
                this.descripcion = gen.getDescripcion(); // Guardar la descripción del error
                return Double.MIN_VALUE; // Si algún gen tiene fitness mínimo, el individuo es inválido
            }
            fitness += fitnessGen; // Sumar el fitness de cada gen
        }
        restaurarEstadoActual();
        return fitness;
    }

    public void guardarEstadoActual(){
        for(Pedido pedido : pedidos){
            pedido.guardarCopia();
        }
        for(Almacen almacen : DataLoader.almacenes){
            almacen.guardarCopia();
        }
        for(Camion camion : DataLoader.camiones){
            camion.guardarCopia();
        }
    }

    public void restaurarEstadoActual(){
        for(Pedido pedido : pedidos){
            pedido.restaurarCopia();
        }
        for(Almacen almacen : DataLoader.almacenes){
            almacen.restaurarCopia();
        }
        for(Camion camion : DataLoader.camiones){
            camion.restaurarCopia();
        }
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
