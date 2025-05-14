package com.plg.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.plg.config.DataLoader;
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
    private String descripcion; // En caso el individuo tenga fitness negativo agregamos una descripcion del
                                // error
    private List<Gen> cromosoma;
    private List<Pedido> pedidos; // Lista de pedidos

    public Individuo(List<Pedido> pedidos) {
        this.pedidos = pedidos;
        inicializarCromosoma();
        this.fitness = calcularFitness();
    }

    private void inicializarCromosoma() {
        // Cargamos los almacenes
        List<Almacen> almacenes = DataLoader.almacenes;
        // Cargamos los camiones
        List<Camion> camionesOperativos = DataLoader.camiones;

        cromosoma = new ArrayList<>();

        // Inicializar un gen para cada camión operativo    
        for (Camion camion : camionesOperativos) {
            cromosoma.add(new Gen(camion, new ArrayList<>()));
        }

        Almacen almacenCentral = almacenes.get(0); // Almacén central
        List<Nodo> nodos = new ArrayList<>();
        nodos.addAll(pedidos);

        // // Con probabilidad de un 20% agrego camionesAveriados
        // if (Math.random() < 0.2) {
        // nodos.addAll(camionesAveriados);
        // }

        // Mezclar los nodos para garantizar aleatoriedad
        Collections.shuffle(nodos);

        // Mezclamos los genes para garantizar aleatoriedad
        List<Gen> camionesMezclados = new ArrayList<>(cromosoma);
        Collections.shuffle(camionesMezclados);

        // Recorremos todos los nodos = pedidos
        for (int i = nodos.size()-1; i >= 0;  i--) {
            // Obtener el gen correspondiente a un camion mezclado
            Gen gen = camionesMezclados.get(i % camionesMezclados.size());
            // Obtener el pedido
            Nodo nodo = nodos.get(i);

            // Con probabilidad de un 20% agrego almacen central o intermedio
            if (Math.random() < 0.1) {
                // Agregar el almacén central o intermedio
                if (Math.random() < 0.5) {
                    nodo = almacenCentral;
                } else {
                    // Selecionamos cualquiera de los almacenes intermedios
                    List<Almacen> almacenesIntermedios = new ArrayList<>(almacenes);
                    almacenesIntermedios.remove(almacenCentral);
                    int randomIndex = (int) (Math.random() * almacenesIntermedios.size());
                    nodo = almacenesIntermedios.get(randomIndex);
                }
                // Agregamos el nodo al gen antes de agregar el pedido
                gen.getNodos().add(nodo);
            }

            // Asignamos el pedido al gen
            gen.getNodos().add(nodo);
        }
        // Asignar el almacén central a cada gen para que cada camion retorne al almacén
        // central
        for (Gen gen : cromosoma) {
            gen.getNodos().add(almacenCentral);
        }
    }

    public double calcularFitness() {
        double fitness = 0.0;
        for (Gen gen : cromosoma) {
            double fitnessGen = gen.calcularFitness();
            if (fitnessGen == Double.MIN_VALUE) {
                this.descripcion = gen.getDescripcion(); // Guardar la descripción del error
                return Double.MIN_VALUE; // Si algún gen tiene fitness mínimo, el individuo es inválido
            }
            fitness += fitnessGen; // Sumar el fitness de cada gen
        }
        return fitness;
    }

    public void mutar() {
        // Realizaremos dos tipos de mutaciones cada una con un 50% de probabilidad
        // 1. Intercambiar dos nodos entre dos genes
        if (Math.random() < 0.5) {
            intercambiarNodos();
        } else {
            // 2. Pasar un nodo de un gen a otro
            pasarPedido();
        }

    }

    private void intercambiarNodos() {
        // Mutación híbrida: 2-opt en una ruta o swap entre rutas
        if (Math.random() < 0.5) {
            // 2-opt dentro de un gen
            int gi = (int) (Math.random() * cromosoma.size());
            List<Nodo> ruta = cromosoma.get(gi).getNodos();
            int n = ruta.size();
            if (n > 4) {
                // evitar extremos de almacén
                int a = 1 + (int) (Math.random() * (n - 3));
                int b = a + 1 + (int) (Math.random() * (n - a - 2));
                Collections.reverse(ruta.subList(a, b + 1));
                this.fitness = calcularFitness();
            }
        } else {
            // Swap de un nodo (no almacén) entre dos genes
            int g1 = (int) (Math.random() * cromosoma.size());
            int g2 = (int) (Math.random() * cromosoma.size());
            if (g1 != g2) {
                List<Nodo> r1 = cromosoma.get(g1).getNodos();
                List<Nodo> r2 = cromosoma.get(g2).getNodos();
                if (r1.size() > 2 && r2.size() > 2) {
                    int i1 = 1 + (int) (Math.random() * (r1.size() - 2));
                    int i2 = 1 + (int) (Math.random() * (r2.size() - 2));
                    Nodo tmp = r1.get(i1);
                    r1.set(i1, r2.get(i2));
                    r2.set(i2, tmp);
                    this.fitness = calcularFitness();
                }
            }
        }
    }

    private void pasarPedido() {
        // Mutación de relocación: mover un pedido al mejor hueco en otra ruta
        // Seleccionar gen y pedido al azar (no almacén)
        int gFrom = (int) (Math.random() * cromosoma.size());
        List<Nodo> fromRuta = cromosoma.get(gFrom).getNodos();
        if (fromRuta.size() <= 3) return; // nada que mover
        int idx = 1 + (int) (Math.random() * (fromRuta.size() - 2));
        Nodo ped = fromRuta.remove(idx);
        // Buscar mejor inserción en otras rutas
        double bestCost = Double.MAX_VALUE;
        int bestGen = -1, bestPos = -1;
        for (int gTo = 0; gTo < cromosoma.size(); gTo++) {
            if (gTo == gFrom) continue;
            List<Nodo> toRuta = cromosoma.get(gTo).getNodos();
            for (int pos = 1; pos < toRuta.size(); pos++) {
                Nodo prev = toRuta.get(pos - 1), next = toRuta.get(pos);
                double dOld = Mapa.getInstance().aStar(prev, next).size();
                double dNew = Mapa.getInstance().aStar(prev, ped).size() + Mapa.getInstance().aStar(ped, next).size();
                double delta = dNew - dOld;
                if (delta < bestCost) {
                    bestCost = delta; bestGen = gTo; bestPos = pos;
                }
            }
        }
        // Insertar donde mejor
        if (bestGen >= 0) {
            cromosoma.get(bestGen).getNodos().add(bestPos, ped);
        } else {
            // si no hay mejor, devolver al origen antes del final
            fromRuta.add(idx, ped);
        }
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
