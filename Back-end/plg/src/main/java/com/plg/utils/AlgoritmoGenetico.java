package com.plg.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;

import com.plg.config.DataLoader;
import com.plg.entity.Almacen;
import com.plg.entity.Camion;
import com.plg.entity.Mapa;
import com.plg.entity.Pedido;
import com.plg.entity.Nodo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlgoritmoGenetico {
    private int poblacionTamano;
    private int generaciones;
    private Mapa mapa;
    private List<Pedido> pedidos;
    private Individuo mejorIndividuo;
    private final Random random = new Random();

    public AlgoritmoGenetico(Mapa mapa, List<Pedido> pedidos) {
        this.mapa = mapa;
        this.pedidos = pedidos;
        generaciones = 10;
        poblacionTamano = 100;
    }

    public void ejecutarAlgoritmo() {
        List<Individuo> poblacion = inicializarPoblacion();
        for (int i = 0; i < generaciones; i++) {
            List<Individuo> padres = seleccionar_padres(poblacion);
            List<Individuo> hijos = cruzar(padres);
            for (Individuo hijo : hijos) {
                hijo.mutar();
            }
            poblacion = seleccionar_mejores(padres, hijos);
        }
        // Ordenar población
        poblacion.sort((ind1, ind2) -> Double.compare(ind1.getFitness(), ind2.getFitness()));
        mejorIndividuo = poblacion.get(0);
        verificarMejorIndividuo(mejorIndividuo);
        actualizarParametrosGlobales(mejorIndividuo);
        System.out.println("Fitness algoritmo genético: " + Parametros.contadorPrueba + " " + mejorIndividuo.getFitness());
        for (Gen gen : mejorIndividuo.getCromosoma()) {
            Camion camion = gen.getCamion();
            camion.setGen(gen);
        }
    }

    private List<Individuo> seleccionar_mejores(List<Individuo> padres, List<Individuo> hijos) {
        List<Individuo> nuevaPoblacion = new ArrayList<>();
        nuevaPoblacion.addAll(padres);
        nuevaPoblacion.addAll(hijos);
        nuevaPoblacion.sort((ind1, ind2) -> Double.compare(ind1.getFitness(), ind2.getFitness()));
        return nuevaPoblacion.subList(0, poblacionTamano);
    }

    private List<Individuo> inicializarPoblacion() {
        List<Individuo> poblacion = new ArrayList<>();
        for (int i = 0; i < poblacionTamano; i++) {
            Individuo individuo = new Individuo(pedidos);
            poblacion.add(individuo);
        }
        return poblacion;
    }

    private List<Individuo> seleccionar_padres(List<Individuo> poblacion) {
        List<Individuo> seleccionados = new ArrayList<>();
        // Puede que haya población impar por tanto redondeamos hacia arriba
        int seleccionadosTamano = (int) Math.ceil(poblacion.size() / 2.0);
        for (int i = 0; i < seleccionadosTamano; i++) {
            int index1 = (int) (Math.random() * poblacion.size());
            int index2 = (int) (Math.random() * poblacion.size());
            Individuo padre1 = poblacion.get(index1);
            Individuo padre2 = poblacion.get(index2);
            seleccionados.add(padre1.getFitness() > padre2.getFitness() ? padre1 : padre2);
        }
        return seleccionados;
    }

    private List<Individuo> cruzar(List<Individuo> seleccionados) {

        List<Individuo> nuevaPoblacion = new ArrayList<>();
        for (int i = 0; i < seleccionados.size() - 1; i++) {
            Individuo padre1 = seleccionados.get(i);
            Individuo padre2 = seleccionados.get(i + 1);
            List<Individuo> hijos = cruzar(padre1, padre2);
            nuevaPoblacion.add(hijos.get(0));
            nuevaPoblacion.add(hijos.get(1));
        }
        return nuevaPoblacion;
    }

    private List<Individuo> cruzar(Individuo p1, Individuo p2) {
        List<Individuo> hijos = new ArrayList<>();
        hijos.add(p1);
        hijos.add(p2);
        return hijos;
        // int size = p1.getCromosoma().size();
        // int cut = 1 + random.nextInt(size - 1);
        // Individuo c1 = new Individuo();
        // Individuo c2 = new Individuo();
        // c1.setPedidos(p1.getPedidos());
        // c2.setPedidos(p2.getPedidos());
        // List<Gen> genes1 = new ArrayList<>();
        // List<Gen> genes2 = new ArrayList<>();
        // for (int i = 0; i < size; i++) {
        //     if (i < cut) {
        //         genes1.add(cloneGen(p1.getCromosoma().get(i)));
        //         genes2.add(cloneGen(p2.getCromosoma().get(i)));
        //     } else {
        //         genes1.add(cloneGen(p2.getCromosoma().get(i)));
        //         genes2.add(cloneGen(p1.getCromosoma().get(i)));
        //     }
        // }
        // // Reparar duplicados y pedidos faltantes
        // repair(genes1, p1.getPedidos());
        // repair(genes2, p1.getPedidos());
        // c1.setCromosoma(genes1);
        // c2.setCromosoma(genes2);
        // c1.setFitness(c1.calcularFitness());
        // c2.setFitness(c2.calcularFitness());
        // return List.of(c1, c2);
    }

    // // Clona un Gen (camion, nodos y ruta)
    // private Gen cloneGen(Gen gen) {
    //     Gen copy = new Gen();
    //     copy.setPosNodo(gen.getPosNodo());
    //     copy.setDescripcion(gen.getDescripcion());
    //     copy.setCamion(gen.getCamion().getClone());
    //     List<Nodo> nodesCopy = new ArrayList<>();
    //     for (Nodo nodo : gen.getNodos()) {
    //         if (nodo instanceof Pedido) {
    //             nodesCopy.add(((Pedido) nodo).getClone());
    //         } else {
    //             nodesCopy.add(nodo);
    //         }
    //     }
    //     copy.setNodos(nodesCopy);
    //     copy.setRutaFinal(new ArrayList<>());
    //     copy.setFitness(gen.getFitness());
    //     return copy;
    // }

    // // Repara duplicados y agrega pedidos faltantes (round-robin)
    // private void repair(List<Gen> genes, List<Pedido> originalPedidos) {
    //     Set<String> seen = new HashSet<>();
    //     List<Pedido> missing = new ArrayList<>(originalPedidos);
    //     for (Gen gen : genes) {
    //         List<Nodo> newNodes = new ArrayList<>();
    //         for (Nodo nodo : gen.getNodos()) {
    //             if (nodo instanceof Pedido) {
    //                 String code = ((Pedido) nodo).getCodigo();
    //                 if (!seen.contains(code)) {
    //                     seen.add(code);
    //                     newNodes.add(nodo);
    //                     missing.removeIf(p -> p.getCodigo().equals(code));
    //                 }
    //             } else {
    //                 newNodes.add(nodo);
    //             }
    //         }
    //         gen.setNodos(newNodes);
    //     }
    //     int idx = 0;
    //     for (Pedido ped : missing) {
    //         Gen g = genes.get(idx++ % genes.size());
    //         g.getNodos().add(ped);
    //     }
    //     // Asegurar que cada ruta termina en el almacén central
    //     Almacen central = DataLoader.almacenes.get(0);
    //     for (Gen gen : genes) {
    //         List<Nodo> nodeList = gen.getNodos();
    //         if (nodeList.isEmpty() || !central.equals(nodeList.get(nodeList.size() - 1))) {
    //             nodeList.add(central);
    //         }
    //     }
    // }

    public void verificarMejorIndividuo(Individuo individuo) {
        if (individuo.getFitness() == Double.MIN_VALUE || individuo.getFitness() == 0.0) {
            System.out.println("No se ha encontrado una solución");
            System.exit(0);
        }
    }

    public void actualizarParametrosGlobales(Individuo individuo) {
        Parametros.fitnessGlobal = individuo.getFitness();
        Parametros.kilometrosRecorridos = individuo.getCromosoma().stream()
                .mapToDouble(gen -> gen.getRutaFinal().size()).sum();
        Parametros.contadorPrueba++;
    }
}
