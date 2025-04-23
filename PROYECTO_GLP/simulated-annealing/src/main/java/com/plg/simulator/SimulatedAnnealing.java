// === src/main/java/com/plg/simulator/SimulatedAnnealing.java ===
package com.plg.simulator;

import com.plg.model.*;

import java.util.*;
import java.util.stream.Collectors;

public class SimulatedAnnealing {

    private final double temperaturaInicial = 1000;
    private final double factorEnfriamiento = 0.95;
    private final int iteracionesMaximas = 1000;
    private final double VELOCIDAD_KMH = 50.0;

    private final Set<String> camionesEnMantenimiento = Set.of("TA01", "TD05");
    private final List<Bloqueo> bloqueos = FileParser.leerBloqueos("src/main/resources/data/202504.bloqueadas");

    public void planificar() {
        List<Camion> camiones = TestData.obtenerCamionesDePrueba();
        List<Pedido> pedidos = FileParser.leerPedidos("src/main/resources/data/ventas202504.txt");

        Map<Camion, List<Pedido>> solucionActual = generarSolucionInicial(camiones, pedidos);
        double costoActual = calcularCosto(solucionActual);

        Map<Camion, List<Pedido>> mejorSolucion = solucionActual;
        double mejorCosto = costoActual;

        double temperatura = temperaturaInicial;

        for (int iter = 0; iter < iteracionesMaximas; iter++) {
            Map<Camion, List<Pedido>> nuevaSolucion = generarVecino(solucionActual);
            double nuevoCosto = calcularCosto(nuevaSolucion);

            if (aceptarSolucion(costoActual, nuevoCosto, temperatura)) {
                solucionActual = nuevaSolucion;
                costoActual = nuevoCosto;
                if (nuevoCosto < mejorCosto) {
                    mejorSolucion = nuevaSolucion;
                    mejorCosto = nuevoCosto;
                }
            }

            temperatura *= factorEnfriamiento;
        }

        System.out.println("Mejor solución encontrada:");
        mostrarAsignaciones(mejorSolucion);
        System.out.println("Costo total: " + mejorCosto);

        // Mostrar pedidos no entregados
        Set<Pedido> entregados = new HashSet<>();
        for (List<Pedido> lista : mejorSolucion.values()) {
            entregados.addAll(lista);
        }

System.out.println("\nPedidos no entregados:");
        for (Pedido pedido : pedidos) {
            if (!entregados.contains(pedido)) {
                System.out.println("❌ " + pedido);
            }
        }
    }

    private Map<Camion, List<Pedido>> generarSolucionInicial(List<Camion> camiones, List<Pedido> pedidos) {
        Map<Camion, List<Pedido>> asignaciones = new HashMap<>();
        Random rand = new Random();
        for (Camion c : camiones) asignaciones.put(c, new ArrayList<>());

        for (Pedido p : pedidos) {
            List<Camion> opciones = camiones.stream()
                    .filter(c -> puedeAsignar(c, asignaciones.get(c), p))
                    .collect(Collectors.toList());
            if (!opciones.isEmpty()) {
                Camion elegido = opciones.get(rand.nextInt(opciones.size()));
                asignaciones.get(elegido).add(p);
            }
        }
        return asignaciones;
    }

    private double calcularCosto(Map<Camion, List<Pedido>> asignaciones) {
        double costo = 0;
        for (Map.Entry<Camion, List<Pedido>> entry : asignaciones.entrySet()) {
            Camion camion = entry.getKey();
            for (Pedido pedido : entry.getValue()) {
                double peso = camion.getPesoCombinado(pedido.getVolumen());
                double distancia = Math.sqrt(Math.pow(pedido.getDestino().getX(), 2) + Math.pow(pedido.getDestino().getY(), 2));
                costo += (distancia * peso) / 180;
            }
        }
        return costo;
    }

    private Map<Camion, List<Pedido>> generarVecino(Map<Camion, List<Pedido>> original) {
        Map<Camion, List<Pedido>> copia = new HashMap<>();
        for (Camion c : original.keySet()) {
            copia.put(c, new ArrayList<>(original.get(c)));
        }

        List<Camion> camiones = new ArrayList<>(copia.keySet());
        Random rand = new Random();

        Camion origen = camiones.get(rand.nextInt(camiones.size()));
        if (copia.get(origen).isEmpty()) return copia;

        Pedido p = copia.get(origen).remove(rand.nextInt(copia.get(origen).size()));

        List<Camion> opciones = camiones.stream()
                .filter(c -> puedeAsignar(c, copia.get(c), p))
                .collect(Collectors.toList());

        if (!opciones.isEmpty()) {
            Camion destino = opciones.get(rand.nextInt(opciones.size()));
            copia.get(destino).add(p);
        } else {
            copia.get(origen).add(p);
        }

        return copia;
    }

    private boolean aceptarSolucion(double actual, double nuevo, double temperatura) {
        if (nuevo < actual) return true;
        double prob = Math.exp((actual - nuevo) / temperatura);
        return Math.random() < prob;
    }

    private boolean puedeAsignar(Camion camion, List<Pedido> pedidosAsignados, Pedido nuevo) {
        if (camionesEnMantenimiento.contains(camion.getCodigo())) return false;
        if (excedeCapacidad(camion, pedidosAsignados, nuevo)) return false;
        if (!dentroDelPlazo(nuevo)) return false;
        return true;
    }

    private boolean excedeCapacidad(Camion camion, List<Pedido> pedidos, Pedido nuevo) {
        int total = nuevo.getVolumen();
        for (Pedido p : pedidos) total += p.getVolumen();
        return total > camion.getCapacidad();
    }

    private boolean dentroDelPlazo(Pedido pedido) {
        double distancia = Math.sqrt(Math.pow(pedido.getDestino().getX(), 2) + Math.pow(pedido.getDestino().getY(), 2));
        double tiempoMin = (distancia / VELOCIDAD_KMH) * 60.0;
        int llegada = pedido.getHoraTotal() + (int) tiempoMin;
        int plazoMax = pedido.getHoraTotal() + pedido.getPlazoLimiteEnMinutos();
        return llegada <= plazoMax;
    }

    private void mostrarAsignaciones(Map<Camion, List<Pedido>> asignaciones) {
        for (Map.Entry<Camion, List<Pedido>> entry : asignaciones.entrySet()) {
            System.out.println("Camión " + entry.getKey().getCodigo() + ":");
            for (Pedido p : entry.getValue()) {
                System.out.println("  → " + p);
            }
        }
    }
}
