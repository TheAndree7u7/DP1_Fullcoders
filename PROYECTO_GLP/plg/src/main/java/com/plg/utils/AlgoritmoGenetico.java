package com.plg.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.plg.entity.Almacen;
import com.plg.entity.Camion;
import com.plg.entity.Mapa;
import com.plg.entity.Pedido;

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
    private List<Camion> camiones;
    private List<Almacen> almacenes;
    private Individuo mejorIndividuo;

    public AlgoritmoGenetico(Mapa mapa, List<Pedido> pedidos, List<Camion> camiones, List<Almacen> almacenes) {
        this.mapa = mapa;
        this.pedidos = pedidos;
        this.camiones = camiones;
        generaciones = 100;
        poblacionTamano = 48;
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
        poblacion.sort((ind1, ind2) -> Double.compare(ind1.getFitness(), ind2.getFitness()));
        mejorIndividuo = poblacion.get(0);
        System.out.println("Mejor individuo: ");
        System.out.println(mejorIndividuo);
        System.out.println("Fitness: " + mejorIndividuo.getFitness());

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
            Individuo individuo = new Individuo(pedidos, camiones, mapa, almacenes);
            poblacion.add(individuo);
        }
        return poblacion;
    }

    private List<Individuo> seleccionar_padres(List<Individuo> poblacion) {
        List<Individuo> seleccionados = new ArrayList<>();
        for (int i = 0; i < poblacion.size() / 2; i++) {
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

    private List<Individuo> cruzar(Individuo padre1, Individuo padre2) {
        // Aún no implementado
        return null;
    }
}
