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

/**
 * Simulated Annealing implementation to plan delivery routes.
 */
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
    private int maxCycles;  // número máximo de ciclos de enfriamiento

    private Individuo currentSolution;
    private Individuo bestSolution;

    private final Random random = new Random();

    public SimulatedAnnealing(Mapa mapa, List<Pedido> pedidos) {
        this.mapa = mapa;
        this.pedidos = pedidos;
        this.maxIterations = 10;         // reducir iteraciones para rapidez
        this.initialTemperature = 2;   // temperatura inicial moderada
        this.coolingRate = 0.01;        // tasa de enfriamiento moderada
        this.maxCycles = 1; // menos ciclos para no tardar tanto
        // Colocar pedidos en el mapa
        for (Pedido pedido : pedidos) {
            mapa.setNodo(pedido.getCoordenada(), pedido);
        }
        this.currentSolution = new Individuo(pedidos);
        this.bestSolution = this.currentSolution;
    }

    /**
     * Ejecuta el algoritmo de Simulated Annealing
     */
    public void ejecutarAlgoritmo() {
        // System.out.println("[SA] Starting Simulated Annealing: initialTemp=" + initialTemperature + ", maxIter=" + maxIterations);
        // System.out.println("[SA] Initial solution fitness: " + currentSolution.getFitness());
        double temp = initialTemperature;
        int cycle = 0;
        int noImprovCount = 0;

        while (temp > 1 && cycle < maxCycles) {
            double cycleBest = bestSolution.getFitness();
            // System.out.println("[SA] Cycle " + (++cycle) + ": temp=" + temp + ", currentFit=" + currentSolution.getFitness() + ", bestFit=" + bestSolution.getFitness());
            for (int i = 0; i < maxIterations; i++) {
                Individuo neighbor = cloneSolution(currentSolution);
                neighbor.mutar();
                neighbor.setFitness(neighbor.calcularFitness());

                double currentFitness = currentSolution.getFitness();
                double neighborFitness = neighbor.getFitness();
                // if (i % (maxIterations / 5) == 0) {
                //     System.out.println(" [SA]  iter " + i + ": currFit=" + currentFitness + ", neighFit=" + neighborFitness);
                // }
                double ap = acceptanceProbability(currentFitness, neighborFitness, temp);
                if (ap > random.nextDouble()) {
                    currentSolution = neighbor;
                }
                if (currentSolution.getFitness() < bestSolution.getFitness() && currentSolution.getFitness() != Double.NEGATIVE_INFINITY){
                    bestSolution = currentSolution;
                }
            }
            // System.out.println("[SA] End of cycle " + cycle + ": bestFit=" + bestSolution.getFitness());
            // Si no mejora en este ciclo
            if (bestSolution.getFitness() <= cycleBest) {
                if (++noImprovCount >= 5) {
                    // System.out.println("[SA] Stopped: no improvement in " + noImprovCount + " cycles.");
                    break;
                }
            } else {
                noImprovCount = 0;
            }
            // Enfriar
            temp *= 1 - coolingRate;
        }
        Parametros.kilometrosRecorridos += bestSolution.getCromosoma().stream()
                .mapToDouble(gen -> gen.getRutaFinal().size())
                .sum();
        Parametros.fitnessGlobal += bestSolution.getFitness();
        // System.out.println("[SA] Finished SA after " + cycle + " cycles and " + maxIterations + " iter each.");
        System.out.println("Best SA fitness: " + bestSolution.getFitness());
    }

    private double acceptanceProbability(double currentFitness, double neighborFitness, double temperature) {
        if (neighborFitness > currentFitness) {
            return 1.0;
        }
        return Math.exp((neighborFitness - currentFitness) / temperature);
    }

    /**
     * Genera una copia profunda de un individuo
     */
    private Individuo cloneSolution(Individuo solution) {
        Individuo copy = new Individuo();
        copy.setPedidos(solution.getPedidos());
        copy.setDescripcion(solution.getDescripcion());
        // Clonar cromosoma (genes)
        List<Gen> genesCopy = new ArrayList<>();
        for (Gen gen : solution.getCromosoma()) {
            Gen g = new Gen();
            // Inicializar rutaFinal para evitar NullPointerException en calcularFitness
            g.setRutaFinal(new ArrayList<>());
            g.setPosNodo(gen.getPosNodo());
            g.setDescripcion(gen.getDescripcion());
            // Clonar camion
            Camion camionCopy = gen.getCamion().getClone();
            g.setCamion(camionCopy);
            // Clonar nodos (Pedidos o Almacenes)
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
