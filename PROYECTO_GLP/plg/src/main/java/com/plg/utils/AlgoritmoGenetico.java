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
        poblacionTamano = 12;
    }


    public void ejecutarAlgoritmo() {
        // Inicializar la población
        List<Individuo> poblacion = inicializarPoblacion();
        for (int i = 0; i < generaciones; i++) {
            


            List<Individuo> padres = seleccionar_padres(poblacion);
            // Cruzar 
            List<Individuo> hijos = cruzar(padres);
            
            // Mutamos
            for (Individuo hijo : hijos) {
                hijo.mutar(); // Método de mutación en la clase Individuo
            }
            poblacion = seleccionar_mejores(padres, hijos); // Seleccionamos los mejores individuos de la población actual y los hijos
        }

        // Retornamos el mejor individuo de la última generación
        // Ordenamos
        poblacion.sort((ind1, ind2) -> Double.compare(ind2.getFitness(), ind1.getFitness()));
        mejorIndividuo = poblacion.get(0); 


        System.out.println("Mejor individuo: ");
        System.out.println(mejorIndividuo);
        System.out.println("Fitness: " + mejorIndividuo.getFitness());


    }

    private List<Individuo> seleccionar_mejores(List<Individuo> padres, List<Individuo> hijos) {
        List<Individuo> nuevaPoblacion = new ArrayList<>();
        nuevaPoblacion.addAll(padres); // Agregar los padres a la nueva población
        nuevaPoblacion.addAll(hijos); // Agregar los hijos a la nueva población

        // Ordenar la población por fitness y seleccionar los mejores
        nuevaPoblacion.sort((ind1, ind2) -> Double.compare(ind2.getFitness(), ind1.getFitness()));
        return nuevaPoblacion.subList(0, poblacionTamano); // Retornar la mejor parte de la población
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
        // Selección de padres utilizando el método de torneo
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
        for (int i = 0; i < seleccionados.size()-1; i ++) {
            Individuo padre1 = seleccionados.get(i);
            Individuo padre2 = seleccionados.get(i + 1);
            List<Individuo> hijos = cruzar(padre1, padre2);
            nuevaPoblacion.add(hijos.get(0)); // Agregar el primer hijo a la nueva población
            nuevaPoblacion.add(hijos.get(1)); // Agregar el segundo hijo a la nueva población
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
            if (i < puntoCruce*pedidos.size()) {
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
        List<Integer> numeros = new ArrayList<>(Collections.nCopies(pedidos.size(), 0));
        for(int i= 0; i<hijo1.size(); i++){
            if (hijo1.get(i) == -1) continue; // Si es -1, no lo consideramos
            if(numeros.get(hijo1.get(i)) == 0){
                numeros.set(hijo1.get(i), 1);
            }else{
                // Buscamos algún número que tenga un 0
                boolean encontrado = false;
                for(int j = 0; j < numeros.size(); j++){
                    if(numeros.get(j) == 0){
                        hijo1.set(i, j);
                        numeros.set(j, 1); 
                        encontrado = true;
                        break;
                    }
                }
                if(!encontrado){
                    hijo1.set(i, -1); // Marcamos el número como no utilizado
                }
               
            }
        }
        // Reemplazamos los -1 por números que no están en la lista
        for(int i = 0; i < hijo1.size(); i++){
            if(hijo1.get(i) == -1){
                for(int j = 0; j < numeros.size(); j++){
                    if(numeros.get(j) == 0){
                        hijo1.set(i, j);
                        numeros.set(j, 1); 
                        break;
                    }
                }
            }
        }
        return hijo1;
    }
}
