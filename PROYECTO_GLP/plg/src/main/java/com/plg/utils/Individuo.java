package com.plg.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.plg.entity.Almacen;
import com.plg.entity.Camion;
import com.plg.entity.Coordenada;
import com.plg.entity.EstadoCamion;
import com.plg.entity.Mapa;
import com.plg.entity.Nodo;
import com.plg.entity.Pedido;
import com.plg.entity.TipoNodo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Individuo {
    private double fitness;
    private List<Gen> cromosoma;
    private List<Pedido> pedidos; // Lista de pedidos
    private List<Camion> camionesOperativos; // List camiones disponibles
    private List<Camion> camionesAveriados; // Lista de camiones averiados
    private List<Almacen> almacenes; // Lista de almacenes disponibles
    private Mapa mapa; // Mapa que representa el entorno de entrega

    public Individuo(List<Pedido> pedidos, List<Camion> camionesOperativos, Mapa mapa, List<Almacen> almacenes) {
        this.camionesOperativos = camionesOperativos;
        this.camionesAveriados = new ArrayList<>();
        this.pedidos = pedidos;
        this.almacenes = almacenes;
        this.mapa = mapa;
        this.cromosoma = inicializarCromosoma(); 
        this.fitness = calcularFitness();
    }

    private List<Gen> inicializarCromosoma() {
        List<Gen> cromosomaInicial = new ArrayList<>();

        // Inicializar un gen para cada camión operativo
        for (Camion camion : camionesOperativos) {
            cromosomaInicial.add(new Gen(camion, new ArrayList<>()));
        }

        Almacen almacenCentral = almacenes.get(0); // Almacén central

        // Crear una lista de nodos que incluye pedidos, camiones averiados y almacenes
        List<Nodo> nodos = new ArrayList<>();
        nodos.addAll(pedidos);

        // // Con probabilidad de un 20% agrego camionesAveriados
        // if (Math.random() < 0.2) {
        //     nodos.addAll(camionesAveriados);
        // }

    
        // Con probabilidad de un 20% agrego almacenes 2 veces
        if (Math.random() < 0.1) {
            nodos.addAll(almacenes);
            nodos.addAll(almacenes);
        }

        // Mezclar los nodos para garantizar aleatoriedad
        Collections.shuffle(nodos);

        // Asignar nodos de forma uniforme a los camiones operativos
        int numCamiones = camionesOperativos.size();
        for (int i = 0; i < nodos.size(); i++) {
            int indiceCamion = i % numCamiones; // Distribuir nodos de forma cíclica
            cromosomaInicial.get(indiceCamion).getNodos().add(nodos.get(i));
        }

        // Asignar el almacén central a cada gen para que cada camion retorne al almacén central
        for (Gen gen: cromosomaInicial) {
            gen.getNodos().add(almacenCentral);
        }

        return cromosomaInicial;
    }

    private double calcularFitness() {
        double fitness = 0.0;
        for (Gen gen : cromosoma) {
            double fitnessGen = gen.calcularFitness();
            if (fitnessGen == Double.MIN_VALUE) {
                return Double.MIN_VALUE; // Si algún gen tiene fitness mínimo, el individuo es inválido
            }
            fitness += fitnessGen; // Sumar el fitness de cada gen
        }
        return fitness;
    }

    public void mutar() {
        // Seleccionar dos genes al azarw
        int genIndex1 = (int) (Math.random() * cromosoma.size());
        int genIndex2 = (int) (Math.random() * cromosoma.size());

        // Obtener los nodos de los genes seleccionados
        Gen gen1 = cromosoma.get(genIndex1);
        Gen gen2 = cromosoma.get(genIndex2);


        List<Nodo> nodosGen1 = gen1.getNodos();
        List<Nodo> nodosGen2 = gen2.getNodos();

        boolean valido1 = !nodosGen1.isEmpty() && !nodosGen2.isEmpty();
        boolean valido2 = nodosGen1.size() > 1 && nodosGen2.size() > 1;


        // Verificar que ambos genes tengan nodos para intercambiar
        if (valido1 && valido2) {
            // Seleccionar un nodo al azar de cada gen pero asegurando que no sea el almacén central
            int nodoIndex1 = (int) (Math.random() * (nodosGen1.size()-1));
            int nodoIndex2 = (int) (Math.random() * (nodosGen2.size()-1));

            // Intercambiar los nodos seleccionados
            Nodo temp = nodosGen1.get(nodoIndex1);
            nodosGen1.set(nodoIndex1, nodosGen2.get(nodoIndex2));
            nodosGen2.set(nodoIndex2, temp);

            this.fitness = calcularFitness(); // Recalcular el fitness después de la mutación
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Individuo: \n");
        for (Gen gen : cromosoma) {
            sb.append(gen.toString()).append("\n");
        }
        return sb.toString();
    }
}
