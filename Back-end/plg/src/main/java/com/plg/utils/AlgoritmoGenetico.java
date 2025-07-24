package com.plg.utils;

import java.util.ArrayList;

import java.util.List;
import java.util.Random;

import com.plg.entity.Camion;

import com.plg.entity.Mapa;


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
    private Mapa mapa;
    private Individuo mejorIndividuo;
    private final Random random = new Random();

    public AlgoritmoGenetico(Mapa mapa) {
        this.mapa = mapa;
        poblacionTamano = 30;
    }

    public void ejecutarAlgoritmo() {
        List<Individuo> poblacion = inicializarPoblacion();
        poblacion.sort((ind1, ind2) -> Double.compare(ind1.getFitness(), ind2.getFitness()));
        mejorIndividuo = poblacion.get(0);
        verificarMejorIndividuo(mejorIndividuo);    
        actualizarParametrosGlobales(mejorIndividuo);
        System.out.println("Fitness algoritmo genético: " + Parametros.contadorPrueba + " Valor: " + mejorIndividuo.getFitness());
        if(mejorIndividuo.getFitness() == Double.POSITIVE_INFINITY) {
            System.out.println("Detalles del individuo: " + mejorIndividuo.getDescripcion());
        }
        for (Gen gen : mejorIndividuo.getCromosoma()) {
            Camion camion = gen.getCamion();
            camion.setGen(gen);
        }
    }


    private List<Individuo> inicializarPoblacion() {
        List<Individuo> poblacion = new ArrayList<>();
        for (int i = 0; i < poblacionTamano; i++) {
            Individuo individuo = new Individuo(Simulacion.pedidosEnviar);
            poblacion.add(individuo);
        }
        return poblacion;
    }



    public void verificarMejorIndividuo(Individuo individuo) {
        if (individuo.getFitness() == Double.POSITIVE_INFINITY) {
            LoggerUtil.logWarning("⚠️ Fitness infinito detectado en el mejor individuo. Esto puede ocurrir cuando no hay soluciones válidas en esta iteración.");
            LoggerUtil.logWarning("Detalles del individuo: " + individuo.getDescripcion());
            // En lugar de lanzar una excepción, registramos el problema y continuamos
            // Esto permite que el algoritmo genético continue evolucionando
        }
    }

    public void actualizarParametrosGlobales(Individuo individuo) {
        Parametros.fitnessGlobal = individuo.getFitness();
        Parametros.kilometrosRecorridos = individuo.getCromosoma().stream()
                .mapToDouble(gen -> gen.getRutaFinal().size()).sum();
        Parametros.contadorPrueba++;
    }

}
