package com.plg.utils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.plg.utils.Parametros;
import com.plg.entity.Almacen;
import com.plg.entity.Camion;
import com.plg.entity.Nodo;
import com.plg.entity.Pedido;
import com.plg.entity.EstadoPedido;

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
    @Builder.Default
    private double porcentajeAsignacionCercana = 0.9; // Porcentaje de camiones a usar para asignación por cercanía
        
    public Individuo(List<Pedido> pedidos) {
        this.pedidos = pedidos;
        this.descripcion = "";
        inicializarCromosoma();
        this.fitness = calcularFitness();
    }

    private void inicializarCromosoma() {
        List<Almacen> almacenes = Parametros.dataLoader.almacenes;
        List<Camion> camionesDisponibles = obtenerCamionesDisponibles();

        cromosoma = new ArrayList<>();
        for (Camion camion : camionesDisponibles) {
            cromosoma.add(new Gen(camion, new ArrayList<>()));
        }
        Almacen almacenCentral = almacenes.get(0);
        List<Pedido> pedidosMezclados = new ArrayList<>();
        pedidosMezclados.addAll(pedidos);
        Collections.shuffle(pedidosMezclados);

        LocalDateTime fechaActual = Parametros.fecha_inicial;

        asignarPedidosACamiones(camionesDisponibles, pedidosMezclados, cromosoma, fechaActual);

        for (Gen gen : cromosoma) {
            gen.getNodos().add(almacenCentral);
        }
    }

    /**
     * Asigna pedidos a camiones según la lógica definida.
     * Valida la asignación usando un Gen temporal y calcularFitness.
     */
    private void asignarPedidosACamiones(List<Camion> camionesDisponibles, List<Pedido> pedidosMezclados, List<Gen> cromosoma, LocalDateTime fechaActual) {
        int maxPedidosPorCamion = 3;
        Random random = new Random();
        guardarEstadoActual();
        Collections.shuffle(camionesDisponibles);
        for (Camion camion : camionesDisponibles) {
            int intentos = 0;
            boolean asignado = false;
            while (intentos < 10 && !asignado && !pedidosMezclados.isEmpty()) {
                List<Pedido> seleccionados = new ArrayList<>();
                List<Pedido> copiaPedidos = new ArrayList<>(pedidosMezclados);
                Collections.shuffle(copiaPedidos, random);
                for (Pedido pedido : copiaPedidos) {
                    if (pedido.getVolumenGLPAsignado() - pedido.getVolumenGLPEntregado() > 0) {
                        seleccionados.add(pedido);
                        if (seleccionados.size() == maxPedidosPorCamion) break;
                    }
                }
                if (seleccionados.isEmpty()) break;
                // Asignar pedidos al camión y simular entrega de GLP
                double glpPorPedido = camion.getCapacidadActualGLP() / seleccionados.size();
                for (Pedido pedido : seleccionados) {
                    double pendiente = pedido.getVolumenGLPAsignado() - pedido.getVolumenGLPEntregado();
                    double entregar = Math.min(glpPorPedido, pendiente);
                    pedido.setVolumenGLPEntregado(pedido.getVolumenGLPEntregado() + entregar);
                    Gen gen = cromosoma.stream().filter(g -> g.getCamion().equals(camion)).findFirst().orElse(null);
                    if (gen != null) {
                        gen.getPedidos().add(pedido);
                        gen.getNodos().add(pedido);
                    }
                }
                pedidosMezclados.removeIf(p -> p.getVolumenGLPAsignado() - p.getVolumenGLPEntregado() == 0);
                asignado = true;
                intentos++;
            }
        }
        restaurarEstadoActual();
    }

    /**
     * Filtra los camiones que no están en mantenimiento preventivo.
     * Si todos los camiones están en mantenimiento, devuelve la lista completa.
     * 
     * @return Lista de camiones disponibles para asignación
     */
    private List<Camion> obtenerCamionesDisponibles() {
        List<Camion> camiones = Parametros.dataLoader.camiones;
        
        // FILTRAR CAMIONES EN MANTENIMIENTO - Ubicación más eficiente
        List<Camion> camionesDisponibles = camiones.stream()
                .filter(camion -> camion.getEstado() != com.plg.entity.EstadoCamion.EN_MANTENIMIENTO_PREVENTIVO)
                .collect(java.util.stream.Collectors.toList());

        // Verificar que tengamos camiones disponibles
        if (camionesDisponibles.isEmpty()) {
            LoggerUtil.logError("⚠️  ADVERTENCIA: No hay camiones disponibles (todos en mantenimiento)");
            LoggerUtil.logWarning("Se usará la lista completa de camiones, incluyendo los que están en mantenimiento.");
            camionesDisponibles = camiones;
        } else {
            LoggerUtil.log("🚛 Camiones disponibles para algoritmo: " + camionesDisponibles.size()
                    + " de " + camiones.size() + " totales");
        }
        
        return camionesDisponibles;
    }

    public double calcularFitness() {
        this.fitness = 0.0;
        this.descripcion = ""; // Reiniciar la descripción
        guardarEstadoActual(); // Guardar el estado actual antes de calcular el fitness
        for (Gen gen : cromosoma) {
            double fitnessGen = gen.calcularFitness();
            if (fitnessGen == Double.POSITIVE_INFINITY) {
                this.descripcion = gen.getDescripcion(); // Guardar la descripción del error
                restaurarEstadoActual();
                return Double.POSITIVE_INFINITY; // Si algún gen tiene fitness máximo, el individuo es inválido
            }
            fitness += fitnessGen; 
        }
        restaurarEstadoActual();
        return fitness;
    }

    public void guardarEstadoActual() {
        for (Pedido pedido : pedidos) {
            pedido.guardarCopia();
        }
        for (Almacen almacen : Parametros.dataLoader.almacenes) {
            almacen.guardarCopia();
        }
        for (Camion camion : Parametros.dataLoader.camiones) {
            camion.guardarCopia();
        }
    }

    public void restaurarEstadoActual() {
        for (Pedido pedido : pedidos) {
            pedido.restaurarCopia();
        }
        for (Almacen almacen : Parametros.dataLoader.almacenes) {
            almacen.restaurarCopia();
        }
        for (Camion camion : Parametros.dataLoader.camiones) {
            camion.restaurarCopia();
        }
    }

    public void mutar() {
        Random rnd = new Random();
        int g1 = rnd.nextInt(cromosoma.size());
        int g2 = rnd.nextInt(cromosoma.size());
        while (g2 == g1) {
            g2 = rnd.nextInt(cromosoma.size());
        }
        Gen gen1 = cromosoma.get(g1);
        Gen gen2 = cromosoma.get(g2);
        List<Nodo> route1 = gen1.getNodos();
        List<Nodo> route2 = gen2.getNodos();
        if (route1.size() > 1 && route2.size() > 1) {
            if (rnd.nextBoolean()) {
                int i1 = rnd.nextInt(route1.size() - 1);
                int i2 = rnd.nextInt(route2.size() - 1);
                Nodo temp = route1.get(i1);
                route1.set(i1, route2.get(i2));
                route2.set(i2, temp);
            } else {
                if (route1.size() > 2) {
                    int i1 = rnd.nextInt(route1.size() - 1);
                    Nodo nodo = route1.remove(i1);
                    route2.add(route2.size() - 1, nodo);
                }
            }
            List<Pedido> nuevosPedidos1 = new ArrayList<>();
            for (Nodo n : route1) {
                if (n instanceof Pedido) {
                    nuevosPedidos1.add((Pedido) n);
                }
            }
            gen1.setPedidos(nuevosPedidos1);
            List<Pedido> nuevosPedidos2 = new ArrayList<>();
            for (Nodo n : route2) {
                if (n instanceof Pedido) {
                    nuevosPedidos2.add((Pedido) n);
                }
            }
            gen2.setPedidos(nuevosPedidos2);
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


}
