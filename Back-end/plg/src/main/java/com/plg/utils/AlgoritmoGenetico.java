package com.plg.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;

import com.plg.config.DataLoader;
import com.plg.entity.Almacen;
import com.plg.entity.Camion;
import com.plg.entity.Coordenada;
import com.plg.entity.Mapa;
import com.plg.entity.Pedido;
import com.plg.entity.TipoNodo;
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
        generaciones = 50;
        poblacionTamano = 200;
        // Borramos los pedidos del mapa
        for(int i=0; i<mapa.getFilas(); i++){
            for(int j=0; j<mapa.getColumnas(); j++){
                Nodo nodo = mapa.getMatriz().get(i).get(j);
                if(nodo instanceof Pedido){
                    Nodo nodoaux = Nodo.builder().coordenada(new Coordenada(i, j)).tipoNodo(TipoNodo.NORMAL).build();
                    mapa.setNodo(nodo.getCoordenada(), nodoaux);
                }
            }
        }
        // Colocamos todos los nuevos pedidos en el mapa
        for (Pedido pedido : pedidos) {
            mapa.setNodo(pedido.getCoordenada(), pedido);
        }

    }

    public void ejecutarAlgoritmo() {
        List<Individuo> poblacion = inicializarPoblacion();
        double mejorFitness = Double.MIN_VALUE;
        int generacionesSinMejora = 0;
        
        for (int i = 0; i < generaciones && generacionesSinMejora < 3; i++) {
            List<Individuo> padres = seleccionar_padres(poblacion);
            // List<Individuo> hijos = cruzar(padres);
            List<Individuo> hijos = padres;
            // Mutación selectiva - solo mutamos algunos hijos
            for (int j = 0; j < hijos.size(); j++) {
                if (random.nextDouble() < 0.3) { // 30% de probabilidad de mutación
                    hijos.get(j).mutar();
                }
            }
            
            poblacion = seleccionar_mejores(padres, hijos);
            
            // Verificamos si hay mejora
            double fitnessActual = poblacion.get(0).getFitness();
            if (fitnessActual > mejorFitness) {
                mejorFitness = fitnessActual;
                generacionesSinMejora = 0;
            } else {
                generacionesSinMejora++;
            }
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
        
        // Ordenamos la población por fitness
        nuevaPoblacion.sort((ind1, ind2) -> Double.compare(ind1.getFitness(), ind2.getFitness()));
        
        // Aseguramos que no intentemos tomar más elementos de los disponibles
        int tamanoSeleccion = Math.min(poblacionTamano, nuevaPoblacion.size());
        
        // Si no hay suficientes individuos, devolvemos todos los disponibles
        if (tamanoSeleccion == nuevaPoblacion.size()) {
            return nuevaPoblacion;
        }
        
        // Devolvemos los mejores individuos hasta el tamaño de la población
        return nuevaPoblacion.subList(0, tamanoSeleccion);
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
        int seleccionadosTamano = (int) Math.ceil(poblacion.size() / 2.0);
        
        // Ordenamos la población por fitness
        poblacion.sort((ind1, ind2) -> Double.compare(ind1.getFitness(), ind2.getFitness()));
        
        // Seleccionamos los mejores individuos directamente
        for (int i = 0; i < seleccionadosTamano; i++) {
            seleccionados.add(poblacion.get(i));
        }
        
        return seleccionados;
    }

    private List<Individuo> cruzar(List<Individuo> seleccionados) {
   

        List<Individuo> nuevaPoblacion = new ArrayList<>();
        
        // Aseguramos que haya al menos 2 individuos para cruzar
        if (seleccionados.size() < 2) {
            return seleccionados;
        }
        
        // Cruzamos los individuos en pares
        for (int i = 0; i < seleccionados.size() - 1; i += 2) {
            Individuo padre1 = seleccionados.get(i);
            Individuo padre2 = seleccionados.get(i + 1);
            List<Individuo> hijos = cruzar(padre1, padre2);
            nuevaPoblacion.addAll(hijos);
        }
        
        // Si el número de seleccionados es impar, agregamos el último individuo sin cruzar
        if (seleccionados.size() % 2 != 0) {
            nuevaPoblacion.add(seleccionados.get(seleccionados.size() - 1));
        }
        
        return nuevaPoblacion;
    }

    private List<Individuo> cruzar(Individuo p1, Individuo p2) {
        List<Individuo> hijos = new ArrayList<>();
        Individuo hijo1 = new Individuo(p1.getPedidos());
        Individuo hijo2 = new Individuo(p2.getPedidos());
        
        hijo1.setCromosoma(new ArrayList<>());
        hijo2.setCromosoma(new ArrayList<>());
        
        for (int i = 0; i < p1.getCromosoma().size(); i++) {
            Gen gen1 = p1.getCromosoma().get(i);
            Gen gen2 = p2.getCromosoma().get(i);
            
            Gen nuevoGen1 = new Gen(gen1.getCamion(), new ArrayList<>());
            Gen nuevoGen2 = new Gen(gen2.getCamion(), new ArrayList<>());
            
            List<Nodo> ruta1 = new ArrayList<>(gen1.getNodos());
            List<Nodo> ruta2 = new ArrayList<>(gen2.getNodos());
            
            Almacen almacenCentral = DataLoader.almacenes.get(0);
            ruta1.remove(almacenCentral);
            ruta2.remove(almacenCentral);
            
            List<Nodo> nuevaRuta1 = new ArrayList<>();
            List<Nodo> nuevaRuta2 = new ArrayList<>();
            
            if (!ruta1.isEmpty() && !ruta2.isEmpty()) {
                // Cruzamos solo si hay suficientes elementos
                int puntoCruce = random.nextInt(Math.min(ruta1.size(), ruta2.size()));
                
                // Cruzamos las rutas de manera más eficiente
                nuevaRuta1.addAll(ruta1.subList(0, puntoCruce));
                nuevaRuta1.addAll(ruta2.subList(puntoCruce, ruta2.size()));
                
                nuevaRuta2.addAll(ruta2.subList(0, puntoCruce));
                nuevaRuta2.addAll(ruta1.subList(puntoCruce, ruta1.size()));
            } else {
                nuevaRuta1.addAll(ruta1);
                nuevaRuta2.addAll(ruta2);
            }
            
            // Optimizamos la eliminación de duplicados
            nuevaRuta1 = eliminarDuplicados(nuevaRuta1);
            nuevaRuta2 = eliminarDuplicados(nuevaRuta2);
            
            nuevaRuta1.add(almacenCentral);
            nuevaRuta2.add(almacenCentral);
            
            nuevoGen1.setNodos(nuevaRuta1);
            nuevoGen2.setNodos(nuevaRuta2);
            
            hijo1.getCromosoma().add(nuevoGen1);
            hijo2.getCromosoma().add(nuevoGen2);
        }
        
        hijo1.setFitness(hijo1.calcularFitness());
        hijo2.setFitness(hijo2.calcularFitness());
        
        hijos.add(hijo1);
        hijos.add(hijo2);
        return hijos;
    }
    
    private List<Nodo> eliminarDuplicados(List<Nodo> ruta) {
        List<Nodo> resultado = new ArrayList<>();
        Set<String> codigosVistos = new HashSet<>();
        
        for (Nodo nodo : ruta) {
            if (nodo instanceof Pedido) {
                String codigo = ((Pedido) nodo).getCodigo();
                if (!codigosVistos.contains(codigo)) {
                    codigosVistos.add(codigo);
                    resultado.add(nodo);
                }
            } else {
                resultado.add(nodo);
            }
        }
        
        return resultado;
    }

    public void verificarMejorIndividuo(Individuo individuo) {
        if (individuo.getFitness() == Double.POSITIVE_INFINITY) {
            System.out.println(individuo.getDescripcion());
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
