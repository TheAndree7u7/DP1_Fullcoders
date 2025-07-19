package com.plg.utils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

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
        asignarAlmacenesIntermediosEntrePedidos();

        for (Gen gen : cromosoma) {
            gen.getNodos().add(almacenCentral);
        }
    }

    private void asignarAlmacenesIntermediosEntrePedidos() {
        List<Almacen> almacenes = Parametros.dataLoader.almacenes;
        Random selectorDeGen = new Random();
        for (Gen gen : cromosoma) {
            List<Nodo> nodos = gen.getNodos();
            if (nodos.size() > 3) { // al menos dos pedidos y un almacén central
                // Insertar almacén intermedio con cierta probabilidad entre dos pedidos
                for (int i = 1; i < nodos.size() - 2; i++) { // entre el primer pedido y el penúltimo
                    if (selectorDeGen.nextDouble() < 0.7) { // 70% de probabilidad
                        // Elegir aleatoriamente entre almacén 1 o 2 si existen
                        Almacen almacenIntermedio = almacenes.size() > 2 ? almacenes.get(1 + selectorDeGen.nextInt(2))
                                : almacenes.get(1);
                        nodos.add(i + 1, almacenIntermedio);
                        i++; // saltar el almacén recién insertado para evitar inserciones consecutivas
                    }
                }
            }
        }
    }

    /**
     * Asigna pedidos a camiones según la lógica definida.
     * Valida la asignación usando un Gen temporal y calcularFitness.
     */
    private void asignarPedidosACamiones(List<Camion> camionesDisponibles, List<Pedido> pedidosMezclados,
            List<Gen> cromosoma, LocalDateTime fechaActual) {
        int maxPedidosPorCamion = 5;
        Random random = new Random();
        guardarEstadoActual();
        Collections.shuffle(camionesDisponibles);
        for (Camion camion : camionesDisponibles) {
            double distanciaCamion = 0;
            // Validación si el camión tiene poco GLP disponible
            if (camion.getCapacidadActualGLP() <= 5) {
                List<Almacen> almacenes = Parametros.dataLoader.almacenes;
                Almacen almacenSecundario1 = almacenes.get(1);
                Almacen almacenSecundario2 = almacenes.get(2);

                // Determinar el almacén secundario más cercano
                Almacen almacenCercano = Mapa.calcularDistancia(camion.getCoordenada(),
                        almacenSecundario1.getCoordenada()) < Mapa.calcularDistancia(camion.getCoordenada(),
                                almacenSecundario2.getCoordenada())
                                        ? almacenSecundario1
                                        : almacenSecundario2;

                // Calcular distancia total: hasta el almacén cercano y regreso al central
                double distanciaAlmacenCercano = Mapa.calcularDistancia(camion.getCoordenada(),
                        almacenCercano.getCoordenada());
                double distanciaRegreso = Mapa.calcularDistancia(almacenCercano.getCoordenada(),
                        almacenes.get(0).getCoordenada());
                double distanciaTotal = distanciaAlmacenCercano + distanciaRegreso;

                // Verificar si el camión puede realizar el recorrido
                if (distanciaTotal > camion.calcularDistanciaMaxima()) {
                    continue; // No asignar pedidos si no puede llegar y regresar
                }else{
                    distanciaCamion = distanciaTotal;
                }
            }

            int intentos = 0;
            boolean asignado = false;
            while (intentos < 10 && !asignado && !pedidosMezclados.isEmpty()) {
                List<Pedido> seleccionados = new ArrayList<>();
                List<Pedido> copiaPedidos = new ArrayList<>(pedidosMezclados);
                Collections.shuffle(copiaPedidos, random);
                for (Pedido pedido : copiaPedidos) {
                    if (pedido.getVolumenGLPAsignado() - pedido.getVolumenGLPEntregado() > 0) {
                        seleccionados.add(pedido);
                        if (seleccionados.size() == maxPedidosPorCamion)
                            break;
                    }
                }
                if (seleccionados.isEmpty())
                    break;
                // Asignar pedidos al camión y simular entrega de GLP
                // Ordenamos pedidos seleccionados por distancia al camión
                seleccionados.sort((p1, p2) -> {
                    double distanciaP1 = Mapa.calcularDistancia(camion.getCoordenada(), p1.getCoordenada());
                    double distanciaP2 = Mapa.calcularDistancia(camion.getCoordenada(), p2.getCoordenada());
                    return Double.compare(distanciaP1, distanciaP2);
                });

                // Verificamos si el camion tiene suficiente combustible para llegar a todos los
                // pedidos y volver al almacen central
                double distanciaTotal = 0;
                for (Pedido pedido : seleccionados) {
                    distanciaTotal += Mapa.calcularDistancia(camion.getCoordenada(), pedido.getCoordenada());
                }
                Almacen almacenCentral = Parametros.dataLoader.almacenes.get(0);
                distanciaTotal += Mapa.calcularDistancia(almacenCentral.getCoordenada(), camion.getCoordenada());

                distanciaCamion += distanciaTotal;
                if (distanciaCamion > camion.calcularDistanciaMaxima()) {
                    intentos++;
                    continue; // No asignar si el camión no puede cubrir la distancia total
                }

                double glpPorPedido = camion.getCapacidadActualGLP() / seleccionados.size();
                for (Pedido pedido : seleccionados) {
                    double pendiente = pedido.getVolumenGLPAsignado() - pedido.getVolumenGLPEntregado();
                    double entregar = Math.min(glpPorPedido, pendiente);
                    pedido.setVolumenGLPEntregado(pedido.getVolumenGLPEntregado() + entregar);
                    Gen gen = cromosoma.stream().filter(g -> g.getCamion().getCodigo().equals(camion.getCodigo()))
                            .findFirst().orElse(null);
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
