package com.plg.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    private Individuo mejorIndividuo;

    public AlgoritmoGenetico(Mapa mapa, List<Pedido> pedidos, List<Camion> camiones) {
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

            List<Individuo> hijos = padres;
            for (Individuo hijo : hijos) {
                hijo.mutar();
            }
            poblacion = seleccionar_mejores(padres, hijos);
        }
        poblacion.sort((ind1, ind2) -> Double.compare(ind1.getFitness(), ind2.getFitness()));
        mejorIndividuo = poblacion.get(0);
        mejorIndividuo.limpiarCromosoma();
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
            Individuo individuo = new Individuo(pedidos, camiones, mapa);
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
        // Lógica de cruce entre dos padres para crear un nuevo individuo
        List<Integer> hijo1 = new ArrayList<>();
        List<Integer> hijo2 = new ArrayList<>();

        List<Integer> cromosoma1 = padre1.getCromosomaNumerico();
        List<Integer> cromosoma2 = padre2.getCromosomaNumerico();

        // Realizamos el cruce de un punto
        int puntoCruce = (int) (Math.random() * camiones.size());
        for (int i = 0; i < cromosoma1.size(); i++) {
            if (i < puntoCruce * pedidos.size()) {
                hijo1.add(cromosoma1.get(i));
                hijo2.add(cromosoma2.get(i));
            } else {
                hijo1.add(cromosoma2.get(i));
                hijo2.add(cromosoma1.get(i));
            }
        }

        hijo1 = eliminarDuplicados(hijo1);

        hijo2 = eliminarDuplicados(hijo2);

        List<Individuo> hijos = new ArrayList<>();
        hijos.add(new Individuo(pedidos, camiones, hijo1, mapa));
        hijos.add(new Individuo(pedidos, camiones, hijo2, mapa));

        return hijos;
    }

    private List<Integer> eliminarDuplicados(List<Integer> hijo1) {
        // Inicializamos una lista para llevar el registro de los números usados
        List<Integer> numeros = new ArrayList<>(Collections.nCopies(pedidos.size(), 0));

        // Primera pasada: recorremos y marcamos duplicados
        for (int i = 0; i < hijo1.size(); i++) {
            if (hijo1.get(i) == -1)
                continue; // Si es -1, lo ignoramos
            int gene = hijo1.get(i);
            // Si el gene es válido y no está usado, lo marcamos
            if (gene >= 0 && gene < numeros.size() && numeros.get(gene) == 0) {
                numeros.set(gene, 1);
            } else {
                // Si ya se ha usado o es inválido, buscamos el primer número libre
                boolean encontrado = false;
                for (int j = 0; j < numeros.size(); j++) {
                    if (numeros.get(j) == 0) {
                        hijo1.set(i, j);
                        numeros.set(j, 1);
                        encontrado = true;
                        break;
                    }
                }
                if (!encontrado) {
                    hijo1.set(i, -1);
                }
            }
        }

        // Segunda pasada: Reemplazar los -1 de forma balanceada
        // Primero, recopilamos todos los índices que aún no se usaron en "numeros"
        List<Integer> freeIndices = new ArrayList<>();
        for (int j = 0; j < numeros.size(); j++) {
            if (numeros.get(j) == 0) {
                freeIndices.add(j);
            }
        }
        // Mezclamos aleatoriamente los índices libres
        Collections.shuffle(freeIndices);

        // Usamos los índices mezclados para reemplazar los -1 restantes
        int freeIndex = 0;
        for (int i = 0; i < hijo1.size(); i++) {
            if (hijo1.get(i) == -1 && freeIndex < freeIndices.size()) {
                hijo1.set(i, freeIndices.get(freeIndex));
                freeIndex++;
            }
        }

        return hijo1;
    }
}
