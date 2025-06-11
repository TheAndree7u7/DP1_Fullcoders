package com.plg.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import com.plg.config.DataLoader;
import com.plg.entity.Almacen;
import com.plg.entity.Camion;
import com.plg.entity.Mapa;
import com.plg.entity.Nodo;
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
    private double fitness;
    private String descripcion;
    private List<Gen> cromosoma;
    private List<Pedido> pedidos; // Lista de pedidos

    public Individuo(List<Pedido> pedidos) {
        this.pedidos = pedidos;
        this.descripcion = "";
        
        // Validar que los camiones tengan combustible suficiente
        if (!validarCombustibleCamiones()) {
            this.fitness = Double.POSITIVE_INFINITY;
            return;
        }
        
        inicializarCromosoma();
        this.fitness = calcularFitness();
    }

    private void inicializarCromosoma() {
        List<Almacen> almacenes = DataLoader.almacenes;
        List<Camion> camiones = DataLoader.camiones;
        cromosoma = new ArrayList<>();
        for (Camion camion : camiones) {
            cromosoma.add(new Gen(camion, new ArrayList<>()));
        }
        Almacen almacenCentral = almacenes.get(0);
        List<Nodo> pedidosMezclados = new ArrayList<>();
        pedidosMezclados.addAll(pedidos);
        Collections.shuffle(pedidosMezclados, new Random());

        // Estrategia mejorada: asignar pedidos considerando la capacidad de combustible
        asignarPedidosConValidacionDeCombustible(pedidosMezclados, almacenCentral);
    }

    /**
     * Asigna pedidos a camiones considerando su capacidad de combustible
     * para evitar rutas no factibles desde el inicio
     */
    private void asignarPedidosConValidacionDeCombustible(List<Nodo> pedidosMezclados, Almacen almacenCentral) {
        Random selectorDeGen = new Random();
        
        for (Nodo pedido : pedidosMezclados) {
            boolean pedidoAsignado = false;
            int intentos = 0;
            final int MAX_INTENTOS = cromosoma.size() * 2;
            
            // Intentar asignar el pedido a un camión que pueda manejarlo
            while (!pedidoAsignado && intentos < MAX_INTENTOS) {
                Gen gen = cromosoma.get(selectorDeGen.nextInt(cromosoma.size()));
                
                // Crear una copia temporal para verificar factibilidad
                List<Nodo> nodosTemp = new ArrayList<>(gen.getNodos());
                nodosTemp.add(pedido);
                nodosTemp.add(almacenCentral);
                
                // Verificar si esta asignación sería factible
                if (esRutaFactible(gen.getCamion(), nodosTemp)) {
                    if (pedido instanceof Pedido) {
                        gen.getPedidos().add((Pedido) pedido);
                    }
                    gen.getNodos().add(pedido);
                    pedidoAsignado = true;
                } else {
                    intentos++;
                }
            }
            
            // Si no se pudo asignar de manera factible, asignar al primer camión disponible
            // (esto generará un fitness infinito y será rechazado por el algoritmo genético)
            if (!pedidoAsignado) {
                Gen gen = cromosoma.get(0);
                if (pedido instanceof Pedido) {
                    gen.getPedidos().add((Pedido) pedido);
                }
                gen.getNodos().add(pedido);
            }
        }
        
        // Agregar almacén central al final de todas las rutas
        for (Gen gen : cromosoma) {
            gen.getNodos().add(almacenCentral);
        }
    }

    /**
     * Verifica si una ruta es factible para un camión dado su combustible disponible
     */
    private boolean esRutaFactible(Camion camion, List<Nodo> nodosRuta) {
        if (nodosRuta == null || nodosRuta.isEmpty()) {
            return true;
        }
        
        double distanciaTotal = 0.0;
        
        for (int i = 0; i < nodosRuta.size(); i++) {
            Nodo nodo1, nodo2;
            if (i == 0) {
                nodo1 = camion;
                nodo2 = nodosRuta.get(i);
            } else {
                nodo1 = nodosRuta.get(i - 1);
                nodo2 = nodosRuta.get(i);
            }
            
            List<Nodo> rutaAstar = Mapa.getInstance().aStar(nodo1, nodo2);
            distanciaTotal += rutaAstar.size();
        }
        
        camion.calcularDistanciaMaxima();
        return distanciaTotal <= camion.getDistanciaMaxima();
    }

    public double calcularFitness() {
        // Validar combustible antes de calcular fitness
        if (!validarCombustibleCamiones()) {
            this.fitness = Double.POSITIVE_INFINITY;
            this.descripcion = "Uno o más camiones no tienen combustible suficiente para continuar";
            return this.fitness;
        }
        
        this.fitness = 0.0; 
        this.descripcion = ""; // Reiniciar la descripción
        guardarEstadoActual(); // Guardar el estado actual antes de calcular el fitness
        for (Gen gen : cromosoma) {
            double fitnessGen = gen.calcularFitness();
            if (fitnessGen == Double.POSITIVE_INFINITY) {
                this.descripcion = gen.getDescripcion(); // Guardar la descripción del error
                return Double.POSITIVE_INFINITY; // Si algún gen tiene fitness máximo, el individuo es inválido
            }
            fitness += fitnessGen; // Sumar el fitness de cada gen
        }
        restaurarEstadoActual();
        return fitness;
    }

    public void guardarEstadoActual(){
        for(Pedido pedido : pedidos){
            pedido.guardarCopia();
        }
        for(Almacen almacen : DataLoader.almacenes){
            almacen.guardarCopia();
        }
        for(Camion camion : DataLoader.camiones){
            camion.guardarCopia();
        }
    }

    public void restaurarEstadoActual(){
        for(Pedido pedido : pedidos){
            pedido.restaurarCopia();
        }
        for(Almacen almacen : DataLoader.almacenes){
            almacen.restaurarCopia();
        }
        for(Camion camion : DataLoader.camiones){
            camion.restaurarCopia();
        }
    }

 
    public void mutar() {
        // Simplified mutation: swap a random pedido between two random routes
        Random rnd = new Random();
        int g1 = rnd.nextInt(cromosoma.size());
        int g2 = rnd.nextInt(cromosoma.size());
        while (g2 == g1) {
            g2 = rnd.nextInt(cromosoma.size());
        }
        var route1 = cromosoma.get(g1).getNodos();
        var route2 = cromosoma.get(g2).getNodos();
        if (route1.size() > 2 && route2.size() > 2) {
            int i1 = rnd.nextInt(route1.size() - 2) + 1;
            int i2 = rnd.nextInt(route2.size() - 2) + 1;
            var temp = route1.get(i1);
            route1.set(i1, route2.get(i2));
            route2.set(i2, temp);
            this.fitness = calcularFitness();
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

    /**
     * Valida que todos los camiones en el cromosoma tengan combustible suficiente
     * @return true si todos los camiones tienen combustible positivo, false en caso contrario
     */
    private boolean validarCombustibleCamiones() {
        if (cromosoma == null) {
            return true; // Si no hay cromosoma aún, permitir la inicialización
        }
        
        for (Gen gen : cromosoma) {
            Camion camion = gen.getCamion();
            if (camion != null) {
                camion.calcularDistanciaMaxima();
                if (camion.getDistanciaMaxima() <= 0) {
                    this.descripcion = "El camión " + camion.getCodigo() + " no tiene combustible suficiente. " +
                        "Combustible actual: " + camion.getCombustibleActual() + " galones. " +
                        "Distancia máxima: " + camion.getDistanciaMaxima() + " km.";
                    return false;
                }
            }
        }
        return true;
    }
}
