package com.plg.utils;
import java.util.ArrayList;
import java.util.List;

import com.plg.entity.Camion;
import com.plg.entity.Coordenada;
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
public class Individuo {
    private double fitness; // Valor de aptitud del individuo
    private List<List<Integer>> cromosoma; // Representación del individuo como una lista de enteros (cromosoma)
    private List<Pedido> pedidos; // Lista de pedidos
    private List<Camion> camiones; // Lista de camiones
    private Mapa mapa; // Mapa que representa el entorno de entrega

    public Individuo(List<Pedido> pedidos, List<Camion> camiones, Mapa mapa) {
        this.pedidos = pedidos;
        this.camiones = camiones;
        this.mapa = mapa;
        inicializarCromosoma();
        this.fitness = calcularFitness();
    }

    public Individuo(List<Pedido> pedidos, List<Camion> camiones, List<Integer> cromosoma_num, Mapa mapa) {
        this.pedidos = pedidos;
        this.camiones = camiones;
        this.mapa = mapa;
        this.cromosoma = cromosoma_a_partir_cromosoma_num(cromosoma_num); // Inicializa el cromosoma a partir de la lista de enteros
        this.fitness = calcularFitness();
    }

    public List<List<Integer>> cromosoma_a_partir_cromosoma_num(List<Integer> cromosoma_num) {
        List<List<Integer>> aux1  = new ArrayList<>();
        for (int i = 0; i < camiones.size(); i++) {
            List<Integer> gen = new ArrayList<>();
            for (int j = 0; j < pedidos.size(); j++) {
                gen.add(cromosoma_num.get(i * pedidos.size() + j));
            }
            aux1.add(gen);
        }
        return aux1; // Retorna el cromosoma inicializado
    }


    private List<List<Integer>> inicializarCromosoma() {
        cromosoma = new ArrayList<>(camiones.size());
        for (int i = 0; i < camiones.size(); i++) {
            cromosoma.add(new ArrayList<>());
        }
        // asignamos de forma aleatoria a cada camion un pedido
        for (int i = 0; i < pedidos.size(); i++) {
            int camionIndex = (int) (Math.random() * camiones.size()); // Selecciona un camión aleatoriodo
            cromosoma.get(camionIndex).add(pedidos.get(i).getId()); 
        }
        for(List<Integer> gen : cromosoma) {
            // completamos con ceros
            while(gen.size() < pedidos.size()) {
                gen.add(0); // Agrega un 0 al cromosoma para completar el tamaño
            }
        }
        return cromosoma; // Retorna el cromosoma inicializado
    }

    public List<Integer> getCromosomaNumerico(){
        List<Integer> cromosomaNumerico = new ArrayList<>();
        for (List<Integer> gen : cromosoma) {
            cromosomaNumerico.addAll(gen); 
        }
        return cromosomaNumerico; 
    }

    private double calcularFitness() {
        double fitness = 0.0;
        for (int i = 0; i < camiones.size(); i++) {
            List<Integer> gen = cromosoma.get(i);

            // Para el primer pedido
            Coordenada coordCamion = camiones.get(i).getCoordenadaActual(); 
            Coordenada coordPedido = pedidos.get(gen.get(0)).getCoordenada(); 
            
            fitness += mapa.aStar(coordCamion, coordPedido).size(); 
            for(int j=0; j<gen.size()-1; j++){
                Coordenada coord1 = pedidos.get(gen.get(j)).getCoordenada(); 
                Coordenada coord2 = pedidos.get(gen.get(j+1)).getCoordenada(); 
             
                fitness += mapa.aStar(coord1, coord2).size();
            }
        }
        return fitness; 
    }    

    public void mutar() {
        
        List<Integer> cromosomaNumerico = getCromosomaNumerico();
        int index1 = (int) (Math.random() * cromosomaNumerico.size());
        int index2 = (int) (Math.random() * cromosomaNumerico.size());
        // Intercambiar dos genes en el cromosoma
        int temp = cromosomaNumerico.get(index1);
        cromosomaNumerico.set(index1, cromosomaNumerico.get(index2));
        cromosomaNumerico.set(index2, temp);
        // Actualizar el cromosoma
        this.cromosoma = cromosoma_a_partir_cromosoma_num(cromosomaNumerico);
        // Recalcular el fitness
        this.fitness = calcularFitness();
        return;
    }

    // Override string
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Individuo: \n");
        for (int i = 0; i < camiones.size(); i++) {
            sb.append("Camion ").append(i).append(": ");
            for (Integer pedido : cromosoma.get(i)) {
                sb.append(pedido).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
