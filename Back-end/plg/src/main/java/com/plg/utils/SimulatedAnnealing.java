package com.plg.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.plg.entity.Mapa;
import com.plg.entity.Pedido;
import com.plg.entity.Nodo;
import com.plg.entity.Camion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulatedAnnealing {
    private int maxIterations;
    private double initialTemperature;
    private double coolingRate;
    private Mapa mapa;
    private List<Pedido> pedidos;
    private int maxCycles;
    private Individuo mejorIndividuo;
    private Individuo currentSolution;
    private Individuo bestSolution;

    private final Random random = new Random();

    public SimulatedAnnealing(Mapa mapa, List<Pedido> pedidos) {
        this.mapa = mapa;
        this.pedidos = pedidos;
        this.maxIterations = 10;
        this.initialTemperature = 2;
        this.coolingRate = 0.01;
        this.maxCycles = 1;
        for (Pedido pedido : pedidos) {
            mapa.setNodo(pedido.getCoordenada(), pedido);
        }
        this.currentSolution = new Individuo(pedidos);
        this.bestSolution = this.currentSolution;
    }

    public void ejecutarAlgoritmo() {
        double temp = initialTemperature;
        int cycle = 0;
        int noImprovCount = 0;

        while (temp > 1 && cycle < maxCycles) {
            double cycleBest = bestSolution.getFitness();
            for (int i = 0; i < maxIterations; i++) {
                Individuo neighbor = cloneSolution(currentSolution);
                neighbor.mutar();
                neighbor.setFitness(neighbor.calcularFitness());

                double currentFitness = currentSolution.getFitness();
                double neighborFitness = neighbor.getFitness();
                double ap = acceptanceProbability(currentFitness, neighborFitness, temp);
                if (ap > random.nextDouble()) {
                    currentSolution = neighbor;
                }
                if (currentSolution.getFitness() < bestSolution.getFitness() && currentSolution.getFitness() != Double.POSITIVE_INFINITY){
                    bestSolution = currentSolution;
                }
            }
            if (bestSolution.getFitness() <= cycleBest) {
                if (++noImprovCount >= 5) {
                    break;
                }
            } else {
                noImprovCount = 0;
            }
            temp *= 1 - coolingRate;
        }
        Parametros.kilometrosRecorridos += bestSolution.getCromosoma().stream()
                .mapToDouble(gen -> gen.getRutaFinal().size())
                .sum();
        Parametros.fitnessGlobal += bestSolution.getFitness();
        System.out.println("Best SA fitness: " + bestSolution.getFitness());
    }

    private double acceptanceProbability(double currentFitness, double neighborFitness, double temperature) {
        if (neighborFitness > currentFitness) {
            return 1.0;
        }
        return Math.exp((neighborFitness - currentFitness) / temperature);
    }

    private Individuo cloneSolution(Individuo solution) {
        Individuo copy = new Individuo();
        copy.setPedidos(solution.getPedidos());
        copy.setDescripcion(solution.getDescripcion());
        List<Gen> genesCopy = new ArrayList<>();
        for (Gen gen : solution.getCromosoma()) {
            Gen g = new Gen();
            g.setRutaFinal(new ArrayList<>());
            g.setPosNodo(gen.getPosNodo());
            g.setDescripcion(gen.getDescripcion());
            Camion camionCopy = gen.getCamion().getClone();
            g.setCamion(camionCopy);
            List<Nodo> nodesCopy = new ArrayList<>();
            for (Nodo nodo : gen.getNodos()) {
                if (nodo instanceof Pedido) {
                    nodesCopy.add(((Pedido) nodo).getClone());
                } else {
                    nodesCopy.add(nodo);
                }
            }
            g.setNodos(nodesCopy);
            genesCopy.add(g);
        }
        copy.setCromosoma(genesCopy);
        copy.setFitness(solution.getFitness());
        return copy;
    }
}
