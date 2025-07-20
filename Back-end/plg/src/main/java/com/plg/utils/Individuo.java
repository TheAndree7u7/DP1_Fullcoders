package com.plg.utils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.time.temporal.ChronoUnit;

import com.plg.entity.Almacen;
import com.plg.entity.Camion;
import com.plg.entity.Mapa;
import com.plg.entity.Nodo;
import com.plg.entity.Pedido;
import com.plg.entity.TipoCamion;
import com.plg.entity.EstadoCamion;
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

        LocalDateTime fechaActual = Parametros.fecha_inicial;

        List<Pedido> pedidosOrdenados = new ArrayList<>(pedidos);

        ordenarPedidosPorFechaVencimiento(pedidosOrdenados, fechaActual);
        asignarPedidosACamiones(camionesDisponibles, pedidosOrdenados, cromosoma, fechaActual);
        // asignarAlmacenesIntermediosEntrePedidos();

        for (Gen gen : cromosoma) {
            gen.getNodos().add(almacenCentral);
        }

        // Para camiones averiados generamos genes pero con un unico nodo que es el
        // mismo camión
        for (Camion camion : Parametros.dataLoader.camiones) {
            if (camion.getEstado() == EstadoCamion.EN_MANTENIMIENTO_POR_AVERIA
                    || camion.getEstado() == EstadoCamion.INMOVILIZADO_POR_AVERIA) {
                Gen gen = new Gen(camion, new ArrayList<>());
                gen.getNodos().add(camion);
                cromosoma.add(gen);
            }
        }
    }

    private void ordenarPedidosPorFechaVencimiento(List<Pedido> pedidosOrdenados, LocalDateTime fechaActual) {
        // Los que tienen menor valor de : pedido.getFechaLimite() - fechaActual
        // deben ir primero
        pedidosOrdenados.sort((p1, p2) -> {
            long diff1 = p1.getFechaLimite().until(fechaActual, ChronoUnit.MINUTES);
            long diff2 = p2.getFechaLimite().until(fechaActual, ChronoUnit.MINUTES);
            return Long.compare(diff1, diff2);
        });

    }



    /**
     * Asigna pedidos a camiones según la lógica definida.
     * Valida la asignación usando un Gen temporal y calcularFitness.
     */
    private void asignarPedidosACamiones(List<Camion> camionesDisponibles, List<Pedido> pedidosOrdenados,
            List<Gen> cromosoma, LocalDateTime fechaActual) {

        

        int maxPedidosPorCamion = 2;
        guardarEstadoActual();
        // Creamos una lista con los camiones averiados para ir quitando 
        // conforme los camiones disponibles les vayan
        // quitando glp
        List<Camion> cp1 = Parametros.dataLoader.camiones.stream()
                .filter(c -> c.getEstado() == EstadoCamion.INMOVILIZADO_POR_AVERIA)
                .toList();
        List<Camion> camionesAveriados = new ArrayList<>(cp1);
        List<Almacen> almacenesDisponibles = new ArrayList<>(Parametros.dataLoader.almacenes);
        Collections.shuffle(camionesDisponibles);
        for (Camion camion : camionesDisponibles) {
            double distanciaCamion = 0;
            // Validación si el camión tiene poco GLP disponible
            if (camion.getCapacidadActualGLP() < 5) {
                // Priorizamos los camiones averiados
                double distanciaTotal = planificarCamionesAveriados(camionesAveriados, camion);
                if (distanciaTotal < 0) {
                    distanciaTotal = planificarAlmacenSecundarioCercano(almacenesDisponibles, camion);
                    if (distanciaTotal < 0) {
                        // Si no puede ir al almacén secundario, no asignamos pedidos a este camión
                        continue;
                    }
                } 
                distanciaCamion += distanciaTotal;
            }
            if (camion.getTipo() == TipoCamion.TD) {
                maxPedidosPorCamion = 1;
            } else if (camion.getTipo() == TipoCamion.TA) {
                maxPedidosPorCamion = 2;
            } else if (camion.getTipo() == TipoCamion.TB) {
                maxPedidosPorCamion = 2;
            } else if (camion.getTipo() == TipoCamion.TC) {
                maxPedidosPorCamion = 2;
            }
            List<Pedido> seleccionados = seleccionarPedidosParaCamion(camion, pedidosOrdenados,
                    maxPedidosPorCamion);

            if (seleccionados.isEmpty())
                break;

            distanciaCamion += calculoDistanciaTotalRecorrido(camion, seleccionados);
            if (distanciaCamion > camion.calcularDistanciaMaxima()) {
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
            pedidosOrdenados.removeIf(p -> Math.abs(p.getVolumenGLPAsignado() - p.getVolumenGLPEntregado()) < Parametros.diferenciaParaPedidoEntregado);
        }
        restaurarEstadoActual();

    }
    public double planificarCamionesAveriados(List<Camion> camionesAveriados, Camion camion) {
        // Planifica el traslado a un almacén secundario cercano
        if (camionesAveriados.isEmpty()) {
            return -1; // No hay camiones averiados para planificar
        }
        // Seleccionamos un camión averiado aleatorio
        Random random = new Random();
        Camion camionAveriado = camionesAveriados.get(random.nextInt(camionesAveriados.size()));

        // Calcular distancia total: hasta el camión averiado y regreso al central
        double distanciaCamionAveriado = Mapa.calcularDistancia(camion.getCoordenada(),
                camionAveriado.getCoordenada());
        double distanciaRegreso = Mapa.calcularDistancia(camionAveriado.getCoordenada(),
                Parametros.dataLoader.almacenes.get(0).getCoordenada());
        double distanciaTotal = distanciaCamionAveriado + distanciaRegreso;


        // Verificar si el camión puede realizar el recorrido
        boolean valido2 = camionAveriado.getCapacidadActualGLP() > 0;
        // Ahora debemos validar que dicho almacen tenga GLP suficiente

        if (distanciaTotal > camion.calcularDistanciaMaxima() || !valido2) {
            return -1;
        } else {
            Gen gen = cromosoma.stream()
                    .filter(g -> g.getCamion().getCodigo().equals(camion.getCodigo())).findFirst().orElse(null);
            if (gen != null) {
                gen.getNodos().add(camionAveriado);
                gen.getCamionesAveriados().add(camionAveriado);
            }
        }
        camionAveriado.recargarGlPSiAveriado(camion);
        if(camionAveriado.getCapacidadActualGLP() <= 0) {
            // Si el camión averiado no tiene GLP, lo removemos de la lista de camiones averiados
            camionesAveriados.remove(camionAveriado);
        }
        return distanciaTotal; // Retorna la distancia total del recorrido
    }

    public double planificarAlmacenSecundarioCercano(List<Almacen> almacenes, Camion camion) {
        //  Puede que algunos de los almacenes ya no exista
        Almacen almacenCercano = null;
        if (almacenes.size() == 3) {
            almacenCercano = Mapa.calcularDistancia(camion.getCoordenada(),
                    almacenes.get(1).getCoordenada()) < Mapa.calcularDistancia(camion.getCoordenada(),
                            almacenes.get(2).getCoordenada())
                                    ? almacenes.get(1)
                                    : almacenes.get(2);
        }else if (almacenes.size() == 2){
            // Si hay dos almacenes, planificamos al más cercano
            almacenCercano = almacenes.get(1);
        }else {
            // Si solo hay un almacén, no podemos planificar
            return -1;
        }
        // Calcular distancia total: hasta el almacén cercano y regreso al central
        double distanciaAlmacenCercano = Mapa.calcularDistancia(camion.getCoordenada(),
                almacenCercano.getCoordenada());
        double distanciaRegreso = Mapa.calcularDistancia(almacenCercano.getCoordenada(),
                almacenes.get(0).getCoordenada());
        double distanciaTotal = distanciaAlmacenCercano + distanciaRegreso;
        // Verificar si el camión puede realizar el recorrido
        boolean valido2 = almacenCercano.getCapacidadActualGLP() > 0;
        // Ahora debemos validar que dicho almacen tenga GLP suficiente
        if (distanciaTotal > camion.calcularDistanciaMaxima() || !valido2) {
            return -1;
        } else {
            Gen gen = cromosoma.stream()
                    .filter(g -> g.getCamion().getCodigo().equals(camion.getCodigo())).findFirst().orElse(null);
            if (gen != null) {
                gen.getNodos().add(almacenCercano);
                gen.getAlmacenesIntermedios().add(almacenCercano);
            }
        }
        almacenCercano.recargarGlPCamion(camion);
        if (almacenCercano.getCapacidadActualGLP() <= 0) {
            almacenes.remove(almacenCercano);
        }
        return distanciaTotal; // Retorna la distancia total del recorrido
    }

    private List<Pedido> seleccionarPedidosParaCamion(Camion camion, List<Pedido> pedidosOrdenados,
            int maxPedidosPorCamion) {
        List<Pedido> seleccionados = new ArrayList<>();
        for (Pedido pedido : pedidosOrdenados) {
            if (pedido.getVolumenGLPAsignado() - pedido.getVolumenGLPEntregado() > 0) {
                seleccionados.add(pedido);
                if (seleccionados.size() == maxPedidosPorCamion)
                    break;
            }
        }
        return seleccionados;
    }

    private double calculoDistanciaTotalRecorrido(Camion camion, List<Pedido> seleccionados) {
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
        return distanciaTotal;
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
                .filter(camion -> camion.getEstado() == EstadoCamion.DISPONIBLE)
                .collect(java.util.stream.Collectors.toList());

        // Verificar que tengamos camiones disponibles
        if (camionesDisponibles.isEmpty()) {
            LoggerUtil.logError("⚠️  ADVERTENCIA: No hay camiones disponibles (todos en mantenimiento)");
            // Si no hay camiones disponibles, error y no se puede continuar
            throw new RuntimeException("No hay camiones disponibles para asignación");
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
