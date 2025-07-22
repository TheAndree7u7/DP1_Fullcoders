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
    private List<Pedido> pedidos;

    public Individuo(List<Pedido> pedidos) {
        this.pedidos = pedidos;
        this.descripcion = "";
        this.fitness = 0.0;
        inicializarCromosoma();
    }

    private void inicializarCromosoma() {
        List<Camion> camionesDisponibles = obtenerCamionesDisponibles();

        cromosoma = new ArrayList<>();
        for (Camion camion : camionesDisponibles) {
            cromosoma.add(new Gen(camion, new ArrayList<>()));
        }
        LocalDateTime fechaActual = Parametros.fecha_inicial;
        List<Pedido> pedidosEvaluar = new ArrayList<>(pedidos);
        asignarPedidosACamiones(camionesDisponibles, pedidosEvaluar, cromosoma, fechaActual);
        crearGenesParaCamionesNoDisponibles();
    }

    public void crearGenesParaCamionesNoDisponibles() {
        List<Camion> camionesNoDisponibles = Parametros.dataLoader.camiones.stream()
                .filter(c -> c.getEstado() == EstadoCamion.EN_MANTENIMIENTO_POR_AVERIA
                        || c.getEstado() == EstadoCamion.INMOVILIZADO_POR_AVERIA
                        || c.getEstado() == EstadoCamion.EN_MANTENIMIENTO_PREVENTIVO)
                .toList();

        for (Camion camion : camionesNoDisponibles) {
            Gen gen = new Gen(camion, new ArrayList<>());
            gen.getNodos().add(camion);
            gen.setRutaFinal(new ArrayList<>());
            gen.getRutaFinal().add(camion);
            gen.setFitness(0.0); // Inicializar fitness a 0 para camiones
            cromosoma.add(gen);
        }

    }

    private void asignarPedidosACamiones(List<Camion> camionesDisponibles, List<Pedido> pedidosEvaluar,
            List<Gen> cromosoma, LocalDateTime fechaActual) {
        guardarEstadoActual();
        List<Camion> cp1 = Parametros.dataLoader.camiones.stream()
                .filter(c -> c.getEstado() == EstadoCamion.INMOVILIZADO_POR_AVERIA)
                .toList();
        List<Camion> camionesAveriados = new ArrayList<>(cp1);
        List<Almacen> almacenesDisponibles = new ArrayList<>(Parametros.dataLoader.almacenes);
        Collections.shuffle(camionesDisponibles, new Random());
        for (Camion camion : camionesDisponibles) {
            if (camion.getCapacidadActualGLP() < 5) {
                boolean planificado = capaDensaAveriado(camionesAveriados, almacenesDisponibles, camion);
                if (!planificado) {
                    capaDensaAlmacen(camion, almacenesDisponibles, false);
                }
            }
            capaDensaPedidos(camion, pedidosEvaluar, almacenesDisponibles, false);
            fitness += getGenPorCamion(camion.getCodigo()).getFitness();
        }
        Collections.shuffle(camionesDisponibles, new Random());
        for (Camion camion : camionesDisponibles) {
            capaDensaPedidos(camion, pedidosEvaluar, almacenesDisponibles, false);
            capaDensaPedidos(camion, pedidosEvaluar, almacenesDisponibles, true);            
            fitness += getGenPorCamion(camion.getCodigo()).getFitness();
        }
        restaurarEstadoActual();

    }

    private void capaDensaPedidos(Camion camion, List<Pedido> pedidosEvaluar,
            List<Almacen> almacenesDisponibles, boolean esCapaFinal) {
        if (esCapaFinal) {
            // Si es final solo sirve el primer almacén
            List<Almacen> almacenes = new ArrayList<>();
            almacenes.add(almacenesDisponibles.get(0)); // Solo el primer almacén
            capaDensaPedidos(camion, pedidosEvaluar, almacenes);
        } else {
            capaDensaPedidos(camion, pedidosEvaluar, almacenesDisponibles);
        }
    }

    private void capaDensaPedidos(Camion camion, List<Pedido> pedidosEvaluar,
            List<Almacen> almacenesDisponibles) {

        int maxPedidosPorCamion = obtenerMaxPedidos(camion);
        int maxIntentos = 30; // Número máximo de intentos para asignar pedidos
        List<Pedido> seleccionadosFinal = new ArrayList<>();
        double distanciaFinal = 0.0;
        Gen gen = getGenPorCamion(camion.getCodigo());
        for (int i = 0; i < maxIntentos; i++) {
            List<Pedido> seleccionados = seleccionarPedidosParaCamion(camion, pedidosEvaluar,
                    maxPedidosPorCamion);
            if (seleccionados.isEmpty()) {
                continue; // No hay pedidos para asignar
            }
            double distanciaTotal = calculoDistanciaTotalRecorrido(camion,
                    seleccionados, almacenesDisponibles);
            if (gen.distanciaRecorrida() + distanciaTotal > camion.calcularDistanciaMaxima()) {
                continue;
            }
            if (i == 0) {
                distanciaFinal = distanciaTotal;
                seleccionadosFinal = seleccionados;
            } else {
                if (distanciaFinal > distanciaTotal) {
                    distanciaFinal = distanciaTotal;
                    seleccionadosFinal = seleccionados;
                }
            }
        }
        if (distanciaFinal == 0.0) {
            capaDensaAlmacen(camion, almacenesDisponibles);
            return; // No se pudo asignar ningún pedido
        }
        realizarProcesoAsignacionPedidos(camion, pedidosEvaluar, seleccionadosFinal, almacenesDisponibles,
                distanciaFinal);
    }

    public void realizarProcesoAsignacionPedidos(Camion camion, List<Pedido> pedidosEvaluar,
            List<Pedido> seleccionados, List<Almacen> almacenesDisponibles, double distanciaFinal) {
        double glpPorPedido = camion.getCapacidadActualGLP() / seleccionados.size();
        // IMPORTANTE COMBUSTIBLE SE ACTUALIZA PRIMERO
        camion.actualizarCombustible(distanciaFinal);
        for (int i = 0; i < seleccionados.size(); i++) {
            Pedido pedido = seleccionados.get(i);
            actualizarGenConPedido(camion, pedido, glpPorPedido);
        }
        capaDensaAlmacen(camion, almacenesDisponibles, false);
        pedidosEvaluar.removeIf(p -> Math.abs(
                p.getVolumenGLPAsignado() - p.getVolumenGLPEntregado()) < Parametros.diferenciaParaPedidoEntregado);
    }

    public void capaDensaAlmacen(Camion camion, List<Almacen> almacenesDisponibles,
            boolean esCapaFinal) {
        if (esCapaFinal) {
            // Si es final solo sirve el primer almacén
            List<Almacen> almacenes = new ArrayList<>();
            almacenes.add(almacenesDisponibles.get(0)); // Solo el primer almacén
            capaDensaAlmacen(camion, almacenes);
        } else {
            capaDensaAlmacen(camion, almacenesDisponibles);
        }
    }

    public void capaDensaAlmacen(Camion camion, List<Almacen> almacenesDisponibles) {
        Almacen almacenCercano = hallarAlmacenMasCercano(camion, almacenesDisponibles);
        boolean valido2 = almacenCercano.getCapacidadActualGLP() > 0;
        if (!valido2) {
            // No vale la pena pues dicho almacen no tiene GLP
            return;
        }
        actualizarGenConAlmacen(camion, almacenCercano, almacenesDisponibles);
    }

    public int obtenerMaxPedidos(Camion camion) {
        if (camion.getTipo() == TipoCamion.TD) {
            return 1;
        } else if (camion.getTipo() == TipoCamion.TA) {
            return 4;
        } else if (camion.getTipo() == TipoCamion.TB) {
            return 2;
        } else if (camion.getTipo() == TipoCamion.TC) {
            return 2;
        }
        return 1; // Por defecto
    }

    public Gen getGenPorCamion(String codigoCamion) {
        return cromosoma.stream()
                .filter(g -> g.getCamion().getCodigo().equals(codigoCamion))
                .findFirst()
                .orElse(null);
    }

    public boolean capaDensaAveriado(List<Camion> camionesAveriados, List<Almacen> almacenesDisponibles,
            Camion camion) {
        if (camionesAveriados.isEmpty()) {
            return false; // No hay camiones averiados para planificar
        }

        Gen gen = getGenPorCamion(camion.getCodigo());

        Random random = new Random();
        Camion camionAveriado = camionesAveriados.get(random.nextInt(camionesAveriados.size()));
        double distanciaCamionAveriado = Mapa.calcularDistancia(camion.getCoordenada(),
                camionAveriado.getCoordenada());
        Almacen almacenCercano = hallarAlmacenCercanoDadoUnNodo(camionAveriado, almacenesDisponibles);
        double distanciaRegreso = Mapa.calcularDistancia(camionAveriado.getCoordenada(),
                almacenCercano.getCoordenada());
        double distanciaTotal = distanciaCamionAveriado + distanciaRegreso;

        boolean valido2 = camionAveriado.getCapacidadActualGLP() > 0;

        if (gen.distanciaRecorrida() + distanciaTotal > camion.calcularDistanciaMaxima() || !valido2) {
            return false; // No puede realizar el recorrido
        }

        actualizarGenConCamionAveriado(camion, camionAveriado, camionesAveriados, distanciaCamionAveriado);
        return true;
    }

    public void actualizarGenConCamionAveriado(Camion camion, Camion camionAveriado, List<Camion> camionesAveriados,
            double distanciaCamionAveriado) {

        Gen gen = getGenPorCamion(camion.getCodigo());
        gen.getNodos().add(camionAveriado);
        gen.getCamionesAveriados().add(camionAveriado);

        camionAveriado.recargarGlPSiAveriado(camion);
        camion.actualizarCombustible(distanciaCamionAveriado);
        // IMPORTANTE: Siempre removemos si no hay GLP suficiente
        if (camionAveriado.getCapacidadActualGLP() <= 0) {
            camionesAveriados.remove(camionAveriado);
        }
        actualizarGenConRutaFitness(camion, camionAveriado);
    }

    public void actualizarGenConAlmacen(Camion camion, Almacen almacenCercano, List<Almacen> almacenesDisponibles) {
        // ACTUALIZACIÓN GEN DEL CAMIÓN
        Gen gen = getGenPorCamion(camion.getCodigo());
        gen.getNodos().add(almacenCercano);
        gen.getAlmacenesIntermedios().add(almacenCercano);

        // ACTUALIZACIÓN ESTADOS
        almacenCercano.recargarGlPCamion(camion);
        almacenCercano.recargarCombustible(camion);
        if (almacenCercano.getCapacidadActualGLP() <= 0) {
            almacenesDisponibles.remove(almacenCercano);
        }
        // ACTULIZAR RUTA Y FITNESS
        actualizarGenConRutaFitness(camion, almacenCercano);
    }

    public void actualizarGenConPedido(Camion camion, Pedido pedido, double glpPorPedido) {
        Gen gen = getGenPorCamion(camion.getCodigo());
        double pendiente = pedido.getVolumenGLPAsignado() - pedido.getVolumenGLPEntregado();
        double entregar = Math.min(glpPorPedido, pendiente);
        pedido.setVolumenGLPEntregado(pedido.getVolumenGLPEntregado() + entregar);
        gen.getPedidos().add(pedido);
        gen.getNodos().add(pedido);
        camion.entregarVolumenGLP(entregar);
        actualizarGenConRutaFitness(camion, pedido);
    }

    public void actualizarGenConRutaFitness(Camion camion, Nodo fin) {
        Gen gen = getGenPorCamion(camion.getCodigo());
        boolean primera_vez = gen.getRutaFinal().isEmpty();
        Nodo inicio = gen.ultimoNodo();
        List<Nodo> ruta = Mapa.getInstance().aStar(inicio, fin);
        if (!primera_vez) {
            // Quitamos de la ruta el primer nodo (inicio)
            ruta.remove(0);
        }
        gen.getRutaFinal().addAll(ruta);
        gen.setFitness(gen.getRutaFinal().size());
    }

    private List<Pedido> seleccionarPedidosParaCamion(Camion camion, List<Pedido> pedidosEvaluar,
            int maxPedidosPorCamion) {
        List<Pedido> seleccionados = new ArrayList<>();
        List<Pedido> pedidosMezclados = new ArrayList<>(pedidosEvaluar);
        for (Pedido pedido : pedidosMezclados) {
            if (pedido.getVolumenGLPAsignado() - pedido.getVolumenGLPEntregado() > 0) {
                seleccionados.add(pedido);
                if (seleccionados.size() == maxPedidosPorCamion)
                    break;
            }
        }
        return seleccionados;
    }

    private double calculoDistanciaTotalRecorrido(Camion camion, List<Pedido> seleccionados,
            List<Almacen> almacenesDisponibles) {
        seleccionados.sort((p1, p2) -> {
            double distanciaP1 = Mapa.calcularDistancia(camion.getCoordenada(), p1.getCoordenada());
            double distanciaP2 = Mapa.calcularDistancia(camion.getCoordenada(), p2.getCoordenada());
            return Double.compare(distanciaP1, distanciaP2);
        });
        double distanciaTotal = 0;
        Gen gen = getGenPorCamion(camion.getCodigo());
        Nodo ultimoNodo = gen.ultimoNodo();
        for (int i = 0; i < seleccionados.size(); i++) {
            Pedido pedido = seleccionados.get(i);
            double distancia = Mapa.calcularDistancia(ultimoNodo.getCoordenada(), pedido.getCoordenada());
            distanciaTotal += distancia;
            ultimoNodo = pedido;
        }
        Almacen almacenCercano = hallarAlmacenCercanoDadoUnNodo(ultimoNodo, almacenesDisponibles);
        distanciaTotal += Mapa.calcularDistancia(ultimoNodo.getCoordenada(),
                almacenCercano.getCoordenada());
        return distanciaTotal;
    }

    public Almacen hallarAlmacenMasCercano(Camion camion, List<Almacen> almacenesDisponibles) {
        Almacen almacenCercano = null;
        double distanciaMinima = Double.MAX_VALUE;
        Gen gen = getGenPorCamion(camion.getCodigo());
        Nodo nodo = gen.ultimoNodo();
        for (Almacen almacen : almacenesDisponibles) {
            double distancia = Mapa.calcularDistancia(nodo.getCoordenada(), almacen.getCoordenada());
            if (distancia < distanciaMinima) {
                distanciaMinima = distancia;
                almacenCercano = almacen;
            }
        }
        return almacenCercano;
    }

    public Almacen hallarAlmacenCercanoDadoUnNodo(Nodo nodo, List<Almacen> almacenesDisponibles) {
        Almacen almacenCercano = null;
        double distanciaMinima = Double.MAX_VALUE;
        for (Almacen almacen : almacenesDisponibles) {
            double distancia = Mapa.calcularDistancia(nodo.getCoordenada(), almacen.getCoordenada());
            if (distancia < distanciaMinima) {
                distanciaMinima = distancia;
                almacenCercano = almacen;
            }
        }
        return almacenCercano;
    }

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
