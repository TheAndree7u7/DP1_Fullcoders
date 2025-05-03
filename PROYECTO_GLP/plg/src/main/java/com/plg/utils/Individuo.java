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
    private List<List<Nodo>> cromosoma;
    private List<Pedido> pedidos; // Lista de pedidos
    private List<Camion> camiones; // List camiones disponibles
    private List<Camion> camionesAveriados; // Lista de camiones averiados
    private List<Almacen> almacenes; // Lista de almacenes disponibles
    private Mapa mapa; // Mapa que representa el entorno de entrega

    public Individuo(List<Pedido> pedidos, List<Camion> camiones, Mapa mapa, List<Almacen> almacenes) {
        this.pedidos = pedidos;
        for (Camion camion : camiones) {
            if (camion.getEstado() == EstadoCamion.INMOVILIZADO_POR_AVERIA) {
                this.camionesAveriados.add(camion);
            } else {
                this.camiones.add(camion);
            }
        }
        this.almacenes = almacenes;
        this.mapa = mapa;
        this.cromosoma = inicializarCromosoma(); 
        this.fitness = calcularFitness();
    }

    public Individuo(List<Pedido> pedidos, List<Camion> camiones, List<Integer> cromosoma_num, Mapa mapa) {
        this.pedidos = pedidos;
        this.camiones = camiones;
        this.mapa = mapa;
        this.fitness = calcularFitness();
    }

    private List<List<Nodo>> inicializarCromosoma() {
        List<List<Nodo>> cromosomaInicial = new ArrayList<>();
        for (int i = 0; i < camiones.size(); i++) {
            List<Nodo> gen = new ArrayList<>();
            cromosomaInicial.add(gen);
        }
        // Asignamos pedidos
        List<Pedido> pedidos = this.pedidos;
        Collections.shuffle(pedidos);
        for (int i = 0; i < pedidos.size(); i++) {
            int camionIndex = (int) (Math.random() * camiones.size());
            Nodo nodo = Nodo.builder()
                    .coordenada(pedidos.get(i).getCoordenada())
                    .bloqueado(false)
                    .gScore(0)
                    .fScore(0)
                    .tipoNodo(TipoNodo.CLIENTE)
                    .pedido(pedidos.get(i))
                    .build();
            cromosomaInicial.get(camionIndex).add(nodo);
        }
        // Asignamos camiones averiados
        for (Camion camion : camionesAveriados) {
            int camionIndex = (int) (Math.random() * camiones.size());
            Nodo nodo = Nodo.builder()
                    .coordenada(camion.getCoordenadaActual())
                    .bloqueado(false)
                    .gScore(0)
                    .fScore(0)
                    .tipoNodo(TipoNodo.CAMION_AVERIADO)
                    .camion(camion)
                    .build();
            cromosomaInicial.get(camionIndex).add(nodo);
        }

        // Asignamos almacenes
        for (Almacen almacen : almacenes) {
            int camionIndex = (int) (Math.random() * camiones.size());
            Nodo nodo = Nodo.builder()
                    .coordenada(almacen.getCoordenada())
                    .bloqueado(false)
                    .gScore(0)
                    .fScore(0)
                    .tipoNodo(TipoNodo.ALMACEN)
                    .build();
            cromosomaInicial.get(camionIndex).add(nodo);
        }

        return cromosomaInicial;
    }

    private double calcularFitness() {
        double fitness = 0.0;
        // Itera por cada camión
        for (int i = 0; i < camiones.size(); i++) {
            List<Nodo> gen = cromosoma.get(i);

            if (!gen.isEmpty()) {
                // Buscar el pedido correspondiente al primer pedido asignado
                Pedido primerPedido = buscarPedido(gen.get(0));
                if (primerPedido != null) {
                    Coordenada coordCamion = camiones.get(i).getCoordenadaActual();
                    Coordenada coordPrimerPedido = primerPedido.getCoordenada();
                    fitness += mapa.aStar(coordCamion, coordPrimerPedido).size();
                }
                // Sumar distancias entre pedidos consecutivos
                for (int j = 0; j < asignados.size() - 1; j++) {
                    Pedido pedidoActual = buscarPedido(asignados.get(j));
                    Pedido pedidoSiguiente = buscarPedido(asignados.get(j + 1));
                    if (pedidoActual != null && pedidoSiguiente != null) {
                        Coordenada coord1 = pedidoActual.getCoordenada();
                        Coordenada coord2 = pedidoSiguiente.getCoordenada();
                        fitness += mapa.aStar(coord1, coord2).size();
                    }
                }
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
