package com.plg.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.plg.config.DataLoader;
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
        List<Almacen> almacenes = DataLoader.almacenes;
        List<Camion> camiones = DataLoader.camiones;

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

        cromosoma = new ArrayList<>();
        for (Camion camion : camionesDisponibles) {
            cromosoma.add(new Gen(camion, new ArrayList<>()));
        }
        // Asignación: si un pedido supera la capacidad, se asigna a varios camiones (misma instancia)
        Almacen almacenCentral = almacenes.get(0);
        List<Nodo> pedidosMezclados = new ArrayList<>();
        pedidosMezclados.addAll(pedidos);
        Collections.shuffle(pedidosMezclados, new Random(Parametros.semillaAleatoria));
        double capacidadDivisionPedido = 25.0; // parametrizable si lo deseas
        Random random = new Random(Parametros.semillaAleatoria + 42);
        for (Nodo pedidoNodo : pedidosMezclados) {
            if (!(pedidoNodo instanceof Pedido)) {
                continue;
            }
            Pedido pedido = (Pedido) pedidoNodo;
            double cantidad = pedido.getVolumenGLPAsignado();
            if (cantidad > capacidadDivisionPedido) {
                int partes = (int) Math.ceil(cantidad / capacidadDivisionPedido);
                List<Integer> indices = new ArrayList<>();
                for (int i = 0; i < cromosoma.size(); i++) indices.add(i);
                Collections.shuffle(indices, random);
                for (int i = 0; i < partes; i++) {
                    int idx = indices.get(i % indices.size());
                    Gen gen = cromosoma.get(idx);
                    gen.getPedidos().add(pedido);
                    gen.getNodos().add(pedido);
                }
            } else {
                int idx = random.nextInt(cromosoma.size());
                Gen gen = cromosoma.get(idx);
                gen.getPedidos().add(pedido);
                gen.getNodos().add(pedido);
            }
        }
        for (Gen gen : cromosoma) {
            gen.getNodos().add(almacenCentral);
        }
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
            fitness += fitnessGen; // Sumar el fitness de cada gen
        }
        // Comprobar que todos los pedidos están ENTREGADOS
        boolean todosEntregados = true;
        StringBuilder errores = new StringBuilder();
        for (Pedido pedido : pedidos) {
            if (pedido.getEstado() != EstadoPedido.ENTREGADO) {
                todosEntregados = false;
                errores.append("Pedido no entregado: ").append(pedido.getCodigo()).append("\n");
            }
        }
        if (!todosEntregados) {
            this.descripcion = errores.toString();
            restaurarEstadoActual();
            return Double.POSITIVE_INFINITY;
        }
        restaurarEstadoActual();
        return fitness;
    }

    public void guardarEstadoActual() {
        for (Pedido pedido : pedidos) {
            pedido.guardarCopia();
        }
        for (Almacen almacen : DataLoader.almacenes) {
            almacen.guardarCopia();
        }
        for (Camion camion : DataLoader.camiones) {
            camion.guardarCopia();
        }
    }

    public void restaurarEstadoActual() {
        for (Pedido pedido : pedidos) {
            pedido.restaurarCopia();
        }
        for (Almacen almacen : DataLoader.almacenes) {
            almacen.restaurarCopia();
        }
        for (Camion camion : DataLoader.camiones) {
            camion.restaurarCopia();
        }
    }

    public void mutar() {
        Random rnd = new Random(Parametros.semillaAleatoria + 3);
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
