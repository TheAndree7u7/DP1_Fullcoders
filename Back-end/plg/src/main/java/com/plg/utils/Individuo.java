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

        // FILTRAR CAMIONES EN MANTENIMIENTO - Ubicación más eficiente
        List<Camion> camionesDisponibles = camiones.stream()
                .filter(camion -> camion.getEstado() != com.plg.entity.EstadoCamion.EN_MANTENIMIENTO_PREVENTIVO)
                .collect(java.util.stream.Collectors.toList());

        // Verificar que tengamos camiones disponibles
        if (camionesDisponibles.isEmpty()) {
            System.err.println("⚠️  ADVERTENCIA: No hay camiones disponibles (todos en mantenimiento)");
            // En caso de emergencia, usar todos los camiones
            camionesDisponibles = camiones;
        } else {
            System.out.println("🚛 Camiones disponibles para algoritmo: " + camionesDisponibles.size()
                    + " de " + camiones.size() + " totales");
        }

        cromosoma = new ArrayList<>();
        for (Camion camion : camionesDisponibles) {
            cromosoma.add(new Gen(camion, new ArrayList<>()));
        }
        Almacen almacenCentral = almacenes.get(0);
        List<Nodo> pedidosMezclados = new ArrayList<>();
        pedidosMezclados.addAll(pedidos);
        Collections.shuffle(pedidosMezclados, new Random());
        List<Gen> genesMezclados = new ArrayList<>(cromosoma);
        Collections.shuffle(genesMezclados, new Random());

        Random selectorDeGen = new Random();
        for (Nodo pedido : pedidosMezclados) {
            Gen gen = genesMezclados.get(selectorDeGen.nextInt(genesMezclados.size()));
            if (pedido instanceof Pedido) {
                gen.getPedidos().add((Pedido) pedido);
            }
            gen.getNodos().add(pedido);
        }
        for (Gen gen : cromosoma) {
            gen.getNodos().add(almacenCentral);
        }
        // Insertar almacenes intermedios (almacenes index 1 y 2) en rutas largas
        for (Gen gen : cromosoma) {
            List<Nodo> nodos = gen.getNodos();
            if (nodos.size() > 3) { // al menos dos pedidos y un almacén central
                // Insertar almacén intermedio con cierta probabilidad entre dos pedidos
                for (int i = 1; i < nodos.size() - 2; i++) { // entre el primer pedido y el penúltimo
                    if (selectorDeGen.nextDouble() < 0.5) { // 50% de probabilidad
                        // Elegir aleatoriamente entre almacén 1 o 2 si existen
                        Almacen almacenIntermedio = almacenes.size() > 2 ? almacenes.get(1 + selectorDeGen.nextInt(2)) : almacenes.get(1);
                        nodos.add(i + 1, almacenIntermedio);
                        i++; // saltar el almacén recién insertado para evitar inserciones consecutivas
                    }
                }
            }
        }
    }

    public double calcularFitness() {
        this.fitness = 0.0;
        this.descripcion = ""; // Reiniciar la descripción
        guardarEstadoActual(); // Guardar el estado actual antes de calcular el fitness
        for (Gen gen : cromosoma) {
            double fitnessGen = gen.calcularFitness();
            if (fitnessGen == Double.POSITIVE_INFINITY) {
                this.descripcion = gen.getDescripcion(); // Guardar la descripción del error
                return Double.POSITIVE_INFINITY; // Si algún gen tiene fitness máximo, el individuo es inválido
            }
            fitness += fitnessGen; // Sumar el fitness de cada gen
        }
        restaurarEstadoActual();
        return fitness;
    }

    public void guardarEstadoActual() {
        for (Pedido pedido : pedidos) {
            pedido.guardarCopia();
        }
        for (Almacen almacen : DataLoader.almacenes) {
            almacen.guardarCopia();
        }
        for (Camion camion : DataLoader.camiones) {
            camion.guardarCopia();
        }
    }

    public void restaurarEstadoActual() {
        for (Pedido pedido : pedidos) {
            pedido.restaurarCopia();
        }
        for (Almacen almacen : DataLoader.almacenes) {
            almacen.restaurarCopia();
        }
        for (Camion camion : DataLoader.camiones) {
            camion.restaurarCopia();
        }
    }

    public void mutar() {
        Random rnd = new Random();
        int g1 = rnd.nextInt(cromosoma.size());
        int g2 = rnd.nextInt(cromosoma.size());
        while (g2 == g1) {
            g2 = rnd.nextInt(cromosoma.size());
        }
        Gen gen1 = cromosoma.get(g1);
        Gen gen2 = cromosoma.get(g2);
        List<Nodo> route1 = gen1.getNodos();
        List<Nodo> route2 = gen2.getNodos();
        if (route1.size() > 1 && route2.size() > 1) {
            if (rnd.nextBoolean()) {
                int i1 = rnd.nextInt(route1.size() - 1);
                int i2 = rnd.nextInt(route2.size() - 1);
                Nodo temp = route1.get(i1);
                route1.set(i1, route2.get(i2));
                route2.set(i2, temp);
            } else {
                if (route1.size() > 2) {
                    int i1 = rnd.nextInt(route1.size() - 1);
                    Nodo nodo = route1.remove(i1);
                    route2.add(route2.size() - 1, nodo);
                }
            }
            List<Pedido> nuevosPedidos1 = new ArrayList<>();
            for (Nodo n : route1) {
                if (n instanceof Pedido) {
                    nuevosPedidos1.add((Pedido) n);
                }
            }
            gen1.setPedidos(nuevosPedidos1);
            List<Pedido> nuevosPedidos2 = new ArrayList<>();
            for (Nodo n : route2) {
                if (n instanceof Pedido) {
                    nuevosPedidos2.add((Pedido) n);
                }
            }
            gen2.setPedidos(nuevosPedidos2);
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
