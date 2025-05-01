package com.plg.service;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.plg.dto.AlgoritmoGeneticoResultadoDTO;
import com.plg.dto.RutaDTO;
import com.plg.entity.Camion;
import com.plg.entity.EstadoCamion;
import com.plg.entity.Pedido;
import com.plg.repository.CamionRepository;
@Service
public class GeneticService {

    private final Random rnd = new Random();
    @Autowired
    private  AlgoritmoGeneticoService algoritmoGeneticoService;
    @Autowired
    private  CamionRepository camionRepository;
    /**
     * Construye la población inicial de N_IND cromosomas
     * a partir de los clusters obtenidos.
     *
     * @param apClusters lista de clusters (cada uno: lista de pedidos)
     * @param nInd       tamaño de la población
     * @return población: lista de cromosomas (cada cromosoma = lista de pedidos)
     */
 

    // Aquí implementas:
    // - selectParents (torneo / ruleta…)
    // - doOXCrossover (Order Crossover)
    // - swapRandomGenes
    // - evaluateIndividual: decodifica el cromosoma a rutas, llama a tu SimulacionService o RutaService,
    //   calcula consumo (fitness1) y % entregas (fitness2).
    // - selectNextGeneration: non-dominated sort + crowding distance
    // - findBestInFront1 e isBetter…
    /** Representa un individuo + sus dos métricas (f1 consumo, f2 cumplimiento) + rank + crowdingDistance */
    private static class Individual {
        List<Pedido> chromosome;
        double f1, f2;
        int rank;
        double crowdingDistance;

        Individual(List<Pedido> chromosome, double f1, double f2) {
            this.chromosome = chromosome;
            this.f1 = f1;
            this.f2 = f2;
        }
    }

    /**
     * Inicializa la población genética a partir de los clusters del AP.
     */
    public List<List<Pedido>> inicializarPoblacion(List<List<Pedido>> apClusters, int nInd) {
        List<List<Pedido>> poblacion = new ArrayList<>();
        for (int p = 0; p < nInd; p++) {
            List<Pedido> cromosoma = new ArrayList<>();
            for (List<Pedido> grupo : apClusters) {
                List<Pedido> copia = new ArrayList<>(grupo);
                Collections.shuffle(copia, rnd);
                cromosoma.addAll(copia);
            }
            poblacion.add(cromosoma);
        }
        return poblacion;
    }

    /**
     * Ejecuta el bucle principal del Algoritmo Genético multiobjetivo.
     */
    public GeneticResult evolvePopulation(
            List<List<Pedido>> initialPop,
            int maxGen,
            double crossoverRate,
            double mutationRate) {

        // 1) convierte a individuos evaluados
        List<Individual> pop = initialPop.stream()
            .map(this::evaluateIndividual)
            .collect(Collectors.toList());

        Individual bestEver = null;

        for (int gen = 1; gen <= maxGen; gen++) {
            // 2) Selección (torneo)
            List<Individual> padres = tournamentSelection(pop, pop.size());

            // 3) Cruce OX
            List<Individual> hijos = new ArrayList<>();
            for (int i = 0; i < padres.size(); i += 2) {
                Individual p1 = padres.get(i);
                Individual p2 = padres.get((i+1)%padres.size());
                if (rnd.nextDouble() < crossoverRate) {
                    List<Pedido>[] offs = doOXCrossover(p1.chromosome, p2.chromosome);
                    hijos.add(evaluateIndividual(offs[0]));
                    hijos.add(evaluateIndividual(offs[1]));
                } else {
                    hijos.add(new Individual(new ArrayList<>(p1.chromosome), p1.f1, p1.f2));
                    hijos.add(new Individual(new ArrayList<>(p2.chromosome), p2.f1, p2.f2));
                }
            }

            // 4) Mutación
            for (Individual h : hijos) {
                if (rnd.nextDouble() < mutationRate) {
                    swapRandomGenes(h.chromosome);
                    // re-evalúa tras mutar
                    Individual re = evaluateIndividual(h.chromosome);
                    h.f1 = re.f1;
                    h.f2 = re.f2;
                }
            }

            // 5) Unir padres + hijos y seleccionar siguiente generación
            List<Individual> combined = new ArrayList<>(pop);
            combined.addAll(hijos);
            pop = selectNextGeneration(combined, initialPop.size());

            // 6) Actualizar mejor de P1
            Individual currBest = pop.stream()
                .filter(ind -> ind.rank == 1)
                .min(Comparator.comparingDouble(ind -> ind.f1)) // el que minimiza consumo
                .orElse(pop.get(0));

            if (bestEver == null || isBetter(currBest, bestEver)) {
                bestEver = currBest;
            }
        }

        return new GeneticResult(bestEver.chromosome, bestEver.f1, bestEver.f2);
    }

    /** Estructura de resultado que devuelve el mejor cromosoma y sus métricas */
    public static class GeneticResult {
        public final List<Pedido> bestChromosome;
        public final double bestF1, bestF2;
        public GeneticResult(List<Pedido> bestChromosome, double bestF1, double bestF2) {
            this.bestChromosome = bestChromosome;
            this.bestF1 = bestF1;
            this.bestF2 = bestF2;
        }
    }

    //––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
    // 1) Selección por torneo
    private List<Individual> tournamentSelection(List<Individual> pop, int k) {
        List<Individual> selected = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            Individual a = pop.get(rnd.nextInt(pop.size()));
            Individual b = pop.get(rnd.nextInt(pop.size()));
            selected.add( tournamentWinner(a, b) );
        }
        return selected;
    }
    private Individual tournamentWinner(Individual a, Individual b) {
        // Menor rank gana; si empatan, mayor crowdingDistance
        if (a.rank != b.rank) return a.rank < b.rank ? a : b;
        return a.crowdingDistance > b.crowdingDistance ? a : b;
    }

    //––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
    // 2) Order Crossover (OX)
    @SuppressWarnings("unchecked")
    private List<Pedido>[] doOXCrossover(List<Pedido> p1, List<Pedido> p2) {
        int len = p1.size();
        int i = rnd.nextInt(len), j = rnd.nextInt(len);
        int start = Math.min(i,j), end = Math.max(i,j);

        List<Pedido> o1 = new ArrayList<>(Collections.nCopies(len, null));
        List<Pedido> o2 = new ArrayList<>(Collections.nCopies(len, null));

        // copia segmento central
        for (int k = start; k <= end; k++) {
            o1.set(k, p1.get(k));
            o2.set(k, p2.get(k));
        }

        // Helper para rellenar
        fillOX(o1, p2, end, len);
        fillOX(o2, p1, end, len);

        return new List[]{o1, o2};
    }
    private void fillOX(List<Pedido> out, List<Pedido> src, int end, int len) {
        int idx = (end+1)%len;
        for(int k = 0; k < len; k++) {
            Pedido candidato = src.get((end+1+k)%len);
            if (!out.contains(candidato)) {
                out.set(idx, candidato);
                idx = (idx+1)%len;
            }
        }
    }

    //––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
    // 3) Mutación: swap de dos genes aleatorios
    private void swapRandomGenes(List<Pedido> crom) {
        int i = rnd.nextInt(crom.size()), j = rnd.nextInt(crom.size());
        Collections.swap(crom, i, j);
    }

    //––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
    /**
     * 4) Evaluación multi‐objetivo
     */
    private Individual evaluateIndividual(List<Pedido> chromosome) {
        // 1) Construir lista de IDs de pedidos en el orden del cromosoma
        List<Long> pedidoOrder = chromosome.stream()
            .map(Pedido::getId)
            .collect(Collectors.toList());

        // 2) Llamar a tu servicio de generación de rutas genético
        Map<String, Object> params = new HashMap<>();
        params.put("algoritmo", "genetico");
        // Número de rutas = número de camiones disponibles
        List<Camion> disponibles = camionRepository.findByEstado(EstadoCamion.DISPONIBLE);
        params.put("numeroRutas", disponibles.size());
        // Este key lo debes recoger en tu servicio para respetar el orden de pedidos
        params.put("pedidoOrder", pedidoOrder);

        AlgoritmoGeneticoResultadoDTO resultado = algoritmoGeneticoService.generarRutas(params);
        List<RutaDTO> rutas = resultado.getRutas();

        // 3) Calcular f1 = consumo total de petróleo
        double totalConsumo = 0;
        for (RutaDTO ruta : rutas) {
            Camion cam = camionRepository
                .findByCodigo(ruta.getCamionCodigo())
                .orElseThrow(() -> new RuntimeException("Camión no encontrado"));
            // Consumo = distancia (km) × pesoCombinado (t) / 180
            double consumo = ruta.getDistanciaTotal() * (cam.getTara() + cam.getPesoCarga()) / 180.0;
            totalConsumo += consumo;
        }

        // 4) Calcular f2 = % pedidos entregados en ≤4h
        int cumplidos = 0;
        int totalPedidos = chromosome.size();
        for (RutaDTO ruta : rutas) {
            // tiempoEstimado está en minutos
            double horas = ruta.getTiempoEstimado() / 60.0;
            if (horas <= 4.0) {
                // todos los pedidos de esta ruta cuentan como cumplidos
                cumplidos += ruta.getNumeroPedidos(); // Reemplazamos ruta.getPedidos().size() por ruta.getNumeroPedidos()
            }
        }
        double porcentajeCumplimiento = (double) cumplidos / totalPedidos;

        // 5) Devolver individuo con sus dos métricas
        return new Individual(chromosome, totalConsumo, porcentajeCumplimiento);
    }

    //––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
    // 5) Non-dominated sort + crowding distance
    private List<Individual> selectNextGeneration(List<Individual> all, int targetSize) {
        List<List<Individual>> fronts = fastNonDominatedSort(all);
        List<Individual> next = new ArrayList<>();

        int i = 0;
        while (next.size() + fronts.get(i).size() <= targetSize) {
            assignCrowdingDistance(fronts.get(i));
            next.addAll(fronts.get(i));
            i++;
        }
        // llenar el último frente por crowding distance
        List<Individual> last = fronts.get(i);
        assignCrowdingDistance(last);
        last.sort(Comparator.comparingDouble((Individual ind) -> ind.crowdingDistance).reversed());

        int remaining = targetSize - next.size();
        next.addAll(last.subList(0, remaining));
        return next;
    }

    /** Fast non-dominated sort (NSGA-II) */
    private List<List<Individual>> fastNonDominatedSort(List<Individual> pop) {
        List<List<Individual>> fronts = new ArrayList<>();
        Map<Individual, List<Individual>> dominatesMap = new HashMap<>();
        Map<Individual, Integer> dominationCount = new HashMap<>();

        List<Individual> firstFront = new ArrayList<>();

        for (Individual p : pop) {
            dominatesMap.put(p, new ArrayList<>());
            dominationCount.put(p, 0);
            for (Individual q : pop) {
                if (p == q) continue;
                if (dominates(p, q)) {
                    dominatesMap.get(p).add(q);
                } else if (dominates(q, p)) {
                    dominationCount.put(p, dominationCount.get(p)+1);
                }
            }
            if (dominationCount.get(p) == 0) {
                p.rank = 1;
                firstFront.add(p);
            }
        }
        fronts.add(firstFront);
        int rank = 1;
        while (!fronts.get(rank-1).isEmpty()) {
            List<Individual> next = new ArrayList<>();
            for (Individual p : fronts.get(rank-1)) {
                for (Individual q : dominatesMap.get(p)) {
                    dominationCount.put(q, dominationCount.get(q)-1);
                    if (dominationCount.get(q) == 0) {
                        q.rank = rank+1;
                        next.add(q);
                    }
                }
            }
            fronts.add(next);
            rank++;
        }
        return fronts;
    }

    /** true si p domina a q: (f1<=q.f1 && f2>=q.f2) y al menos uno estrictamente */
    private boolean dominates(Individual p, Individual q) {
        return (p.f1 <= q.f1 && p.f2 >= q.f2)
            && (p.f1 < q.f1 || p.f2 > q.f2);
    }

    /** Asigna crowding distance en un frente */
    private void assignCrowdingDistance(List<Individual> front) {
        int l = front.size();
        if (l == 0) return;
        for (Individual ind : front) ind.crowdingDistance = 0;

        // Para f1
        front.sort(Comparator.comparingDouble(ind -> ind.f1));
        front.get(0).crowdingDistance = Double.POSITIVE_INFINITY;
        front.get(l-1).crowdingDistance = Double.POSITIVE_INFINITY;
        double f1Min = front.get(0).f1, f1Max = front.get(l-1).f1;
        for (int i = 1; i < l-1; i++) {
            front.get(i).crowdingDistance += 
                (front.get(i+1).f1 - front.get(i-1).f1)/(f1Max - f1Min);
        }

        // Para f2
        front.sort(Comparator.comparingDouble(ind -> ind.f2));
        front.get(0).crowdingDistance = Double.POSITIVE_INFINITY;
        front.get(l-1).crowdingDistance = Double.POSITIVE_INFINITY;
        double f2Min = front.get(0).f2, f2Max = front.get(l-1).f2;
        for (int i = 1; i < l-1; i++) {
            front.get(i).crowdingDistance += 
                (front.get(i+1).f2 - front.get(i-1).f2)/(f2Max - f2Min);
        }
    }

    /** Compara dos indivuos: mejor es el que minimiza f1 y maximiza f2 */
    private boolean isBetter(Individual a, Individual b) {
        if (a.f1 != b.f1) return a.f1 < b.f1;
        return a.f2 > b.f2;
    }
}
