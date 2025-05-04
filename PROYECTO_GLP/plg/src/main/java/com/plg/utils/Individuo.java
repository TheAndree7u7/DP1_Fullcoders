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
        this.pedidos = pedidos;
        for (Camion camion : camionesOperativos) {
            if (camion.getEstado() == EstadoCamion.INMOVILIZADO_POR_AVERIA) {
                this.camionesAveriados.add(camion);
            } else {
                this.camionesOperativos.add(camion);
            }
        }
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

        // Crear una lista de nodos que incluye pedidos, camiones averiados y almacenes
        List<Nodo> nodos = new ArrayList<>();
        nodos.addAll(pedidos);
        nodos.addAll(camionesAveriados);
        nodos.addAll(almacenes);

        // Mezclar los nodos para garantizar aleatoriedad
        Collections.shuffle(nodos);

        // Asignar nodos de forma uniforme a los camiones operativos
        int numCamiones = camionesOperativos.size();
        for (int i = 0; i < nodos.size(); i++) {
            int indiceCamion = i % numCamiones; // Distribuir nodos de forma cíclica
            cromosomaInicial.get(indiceCamion).getNodos().add(nodos.get(i));
        }

        return cromosomaInicial;
    }

    private double calcularFitness() {
        double fitness = 0.0;
        for (Gen gen : cromosoma) {
            List<Nodo> nodos = gen.getNodos();
            if (!nodos.isEmpty()) {
                for (int j = 0; j < nodos.size() - 1; j++) {
                    Nodo nodo1 = nodos.get(j);
                    Nodo nodo2 = nodos.get(j + 1);
                    fitness += mapa.aStar(nodo1, nodo2);
                }
            }
        }
        return fitness;
    }

    public void mutar() {
        // Seleccionar dos genes al azar
        int genIndex1 = (int) (Math.random() * cromosoma.size());
        int genIndex2 = (int) (Math.random() * cromosoma.size());

        // Obtener los nodos de los genes seleccionados
        List<Nodo> nodosGen1 = cromosoma.get(genIndex1).getNodos();
        List<Nodo> nodosGen2 = cromosoma.get(genIndex2).getNodos();

        // Verificar que ambos genes tengan nodos para intercambiar
        if (!nodosGen1.isEmpty() && !nodosGen2.isEmpty()) {
            // Seleccionar un nodo al azar de cada gen
            int nodoIndex1 = (int) (Math.random() * nodosGen1.size());
            int nodoIndex2 = (int) (Math.random() * nodosGen2.size());

            // Intercambiar los nodos seleccionados
            Nodo temp = nodosGen1.get(nodoIndex1);
            nodosGen1.set(nodoIndex1, nodosGen2.get(nodoIndex2));
            nodosGen2.set(nodoIndex2, temp);
        }

        // Recalcular el fitness
        this.fitness = calcularFitness();
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
