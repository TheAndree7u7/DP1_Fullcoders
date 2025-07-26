package com.plg.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.time.LocalDateTime;

import com.plg.entity.Almacen;
import com.plg.entity.Averia;
import com.plg.entity.Camion;
import com.plg.entity.EstadoCamion;
import com.plg.entity.Mapa;
import com.plg.entity.Nodo;
import com.plg.entity.Pedido;
import com.plg.entity.TipoNodo;
import com.plg.entity.EstadoPedido;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Gen {

    private int posNodo = 0;
    private String descripcion;
    private Camion camion;
    private List<Nodo> nodos;
    private List<Nodo> rutaFinal;
    private List<Pedido> pedidos;
    private List<Camion> camionesAveriados;
    private List<Almacen> almacenesIntermedios;
    private double fitness;

    public Gen(Camion camion, List<Nodo> nodosOriginal) {
        this.camion = camion;
        this.nodos = nodosOriginal;
        this.rutaFinal = new ArrayList<>();
        this.pedidos = new ArrayList<>();
        this.camionesAveriados = new ArrayList<>();
        this.almacenesIntermedios = new ArrayList<>();
        this.fitness = 0.0;
    }

    public double distanciaRecorrida() {
        return (double) rutaFinal.size();
    }

    private void recargarCamion(Camion camion, Nodo nodo) {
        if (nodo instanceof Almacen) {
            Almacen almacenRecarga = (Almacen) nodo;
            almacenRecarga.recargarGlPCamion(camion);
        }

        if (nodo instanceof Camion) {
            Camion camionRecarga = (Camion) nodo;
            camionRecarga.recargarGlPSiAveriado(camion);
        }
    }

    public String descripcionDistanciaLejana(double distanciaMaxima, double distanciaCalculada, Nodo nodo1,
            Nodo nodo2) {
        return "El camion con código " + camion.getCodigo()
                + " no puede recorrer la distancia de " + distanciaCalculada
                + " km. La distancia máxima es de " + distanciaMaxima
                + " km." + " El camión se encuentra en la posición " + nodo1.getCoordenada()
                + " y se dirige a la posición " + nodo2.getCoordenada() + ".";
    }

    public double calcularFitness() {
        this.rutaFinal.clear();
        double fitness = 0.0;
        Nodo posicionActual = camion;
        List<Nodo> rutaEntradaBloqueada = null;
        LocalDateTime fechaActual = Parametros.fecha_inicial;
        LocalDateTime fechaLlegada = fechaActual;
        for (int i = 0; i < nodos.size(); i++) {
            Nodo destino = nodos.get(i);
            List<Nodo> rutaAstar = Mapa.getInstance().aStar(posicionActual, destino);
            if (destino instanceof Pedido) {
                ResultadoEntrega resultado = procesarEntregaPedido((Pedido) destino, rutaAstar, fechaLlegada, fitness,
                        posicionActual, i);
                fitness = resultado.fitness;
                fechaLlegada = resultado.fechaLlegada;
                posicionActual = resultado.posicionActual;
                rutaEntradaBloqueada = resultado.rutaEntradaBloqueada;
                if (fitness == Double.POSITIVE_INFINITY)
                    break;
            } else if (destino instanceof Almacen || destino instanceof Camion) {
                posicionActual = procesarNodoRecarga(destino, rutaAstar, i);
                rutaEntradaBloqueada = null;
            } else {
                posicionActual = procesarNodoNormal(destino, rutaAstar, i);
            }
            if (rutaEntradaBloqueada != null && i + 1 < nodos.size()) {
                ResultadoSalidaBloqueo resultadoSalida = procesarRutaSalidaBloqueo(rutaEntradaBloqueada, fitness,
                        posicionActual);
                fitness = resultadoSalida.fitness;
                posicionActual = resultadoSalida.posicionActual;
                rutaEntradaBloqueada = null;
            }
        }
        this.fitness = fitness;
        return fitness;
    }

    // Auxiliar para procesar la entrega de un pedido
    private ResultadoEntrega procesarEntregaPedido(Pedido pedido, List<Nodo> rutaAstar, LocalDateTime fechaLlegada,
            double fitness, Nodo posicionActual, int i) {
        double tiempoLlegadaHoras = rutaAstar.size() / camion.getVelocidadPromedio() + 0.25;
        LocalDateTime nuevaFechaLlegada = fechaLlegada.plusMinutes((long) (tiempoLlegadaHoras * 60));
        boolean dentroDeLimite = pedido.getFechaLimite() == null || !nuevaFechaLlegada.isAfter(pedido.getFechaLimite());
        // Calcular la cantidad de pedidos asignados a este camión
        int cantidadPedidosAsignados = this.pedidos != null && !this.pedidos.isEmpty() ? this.pedidos.size() : 1;
        double glpPorPedido = camion.getCapacidadActualGLP() / cantidadPedidosAsignados;
        double volumenPendiente = pedido.getVolumenGLPAsignado() - pedido.getVolumenGLPEntregado();
        double volumenAEntregar = Math.min(glpPorPedido, volumenPendiente);
        boolean entregadoCompleto = (pedido.getVolumenGLPEntregado() + volumenAEntregar) >= pedido
                .getVolumenGLPAsignado() - Parametros.diferenciaParaPedidoEntregado;
        if (dentroDeLimite) {
            fitness += rutaAstar.size();
            camion.actualizarCombustible(rutaAstar.size());
            camion.entregarVolumenGLP(volumenAEntregar);
            pedido.setVolumenGLPEntregado(pedido.getVolumenGLPEntregado() + volumenAEntregar);
            if (entregadoCompleto) {
                pedido.setVolumenGLPEntregado(pedido.getVolumenGLPAsignado());
                pedido.setEstado(EstadoPedido.ENTREGADO);
            }
            if (i > 0 && rutaAstar.size() > 1) {
                rutaAstar.remove(0);
            }
            rutaFinal.addAll(rutaAstar);
            List<Nodo> rutaEntradaBloqueada = pedido.isBloqueado() ? new ArrayList<>(rutaAstar) : null;
            return new ResultadoEntrega(fitness, nuevaFechaLlegada, pedido, rutaEntradaBloqueada);
        } else {
            this.descripcion = "El pedido " + pedido.getCodigo() + " no puede ser entregado a tiempo. Fecha límite: "
                    + pedido.getFechaLimite() + ", fecha llegada: " + nuevaFechaLlegada;
            return new ResultadoEntrega(Double.POSITIVE_INFINITY, nuevaFechaLlegada, posicionActual, null);
        }
    }

    // Auxiliar para procesar nodos de tipo almacén o camión
    private Nodo procesarNodoRecarga(Nodo destino, List<Nodo> rutaAstar, int i) {
        recargarCamion(camion, destino);
        if (i > 0 && rutaAstar.size() > 1) {
            rutaAstar.remove(0);
        }
        rutaFinal.addAll(rutaAstar);
        return destino;
    }

    // Auxiliar para procesar nodos normales
    private Nodo procesarNodoNormal(Nodo destino, List<Nodo> rutaAstar, int i) {
        if (i > 0 && rutaAstar.size() > 1) {
            rutaAstar.remove(0);
        }
        rutaFinal.addAll(rutaAstar);
        return destino;
    }

    public List<Nodo> construirRutaFinalApi() {
        List<Nodo> rutaApi = new ArrayList<>();
        for (Nodo nodo : rutaFinal) {
            if (nodo instanceof Pedido && this.getPedidos().contains(nodo)) {
                Pedido pedido = (Pedido) nodo;
                for (int j = 0; j < Parametros.cantNodosEnPedidos; j++) {
                    rutaApi.add(pedido);
                }
            } else if (nodo instanceof Camion && this.getCamionesAveriados().contains(nodo)) {
                Camion camion = (Camion) nodo;
                if (camion.getEstado() == EstadoCamion.INMOVILIZADO_POR_AVERIA) {
                    for (int j = 0; j < Parametros.cantNodosEnPedidos; j++) {
                        rutaApi.add(nodo);
                    }
                }
            } else {
                rutaApi.add(nodo);
            }
        }
        int cantNodosRecorridos = (int) (Parametros.velocidadCamion * Parametros.intervaloTiempo / 60);
        if (rutaApi.size() < cantNodosRecorridos) {
            Nodo ultimoNodo = rutaApi.get(rutaApi.size() - 1);
            int nodosFaltantes = cantNodosRecorridos - rutaApi.size();
            for (int i = 0; i < nodosFaltantes; i++) {
                rutaApi.add(ultimoNodo);
            }
        }
        return rutaApi;
    }

    // Auxiliar para procesar rutas de salida de bloqueos
    private ResultadoSalidaBloqueo procesarRutaSalidaBloqueo(List<Nodo> rutaEntradaBloqueada, double fitness,
            Nodo posicionActual) {
        List<Nodo> rutaSalida = new ArrayList<>(rutaEntradaBloqueada);
        Collections.reverse(rutaSalida);
        rutaSalida.remove(0);
        if (!rutaSalida.isEmpty()) {
            rutaFinal.addAll(rutaSalida);
            fitness += rutaSalida.size();
            posicionActual = rutaSalida.get(rutaSalida.size() - 1);
        }
        return new ResultadoSalidaBloqueo(fitness, posicionActual);
    }

    public Nodo ultimoNodo() {
        if (rutaFinal.isEmpty()) {
            return camion;
        }
        return rutaFinal.getLast();
    }

    // Clases auxiliares para devolver múltiples valores
    private static class ResultadoEntrega {
        double fitness;
        LocalDateTime fechaLlegada;
        Nodo posicionActual;
        List<Nodo> rutaEntradaBloqueada;

        ResultadoEntrega(double fitness, LocalDateTime fechaLlegada, Nodo posicionActual,
                List<Nodo> rutaEntradaBloqueada) {
            this.fitness = fitness;
            this.fechaLlegada = fechaLlegada;
            this.posicionActual = posicionActual;
            this.rutaEntradaBloqueada = rutaEntradaBloqueada;
        }
    }

    private static class ResultadoSalidaBloqueo {
        double fitness;
        Nodo posicionActual;

        ResultadoSalidaBloqueo(double fitness, Nodo posicionActual) {
            this.fitness = fitness;
            this.posicionActual = posicionActual;
        }
    }

    public int colocar_nodo_de_averia_automatica(Averia averia) {

        int cantidad_nodos_que_puede_recorrer_el_camion = this.getCamion().calcularCantidadDeNodos(Parametros.intervaloTiempo);
        int posicion_inicial = (int) (cantidad_nodos_que_puede_recorrer_el_camion
                * Parametros.rango_inicial_tramo_averia);
        int posicion_final = (int) (cantidad_nodos_que_puede_recorrer_el_camion * Parametros.rango_final_tramo_averia);

        List<Integer> posiciones_normales = new ArrayList<>();
        for (int i = posicion_inicial; i <= posicion_final; i++) {
            if (i < rutaFinal.size() && rutaFinal.get(i).getTipoNodo().equals(TipoNodo.NORMAL)) {
                posiciones_normales.add(i);
            }
        }
        if (posiciones_normales.isEmpty()) {
            return -1;
        }
        // elige una posicion aleatoria dentro de los rangos
        int posicion_aleatoria = new Random().nextInt(posiciones_normales.size());

        Nodo nodoSeleccionado = rutaFinal.get(posiciones_normales.get(posicion_aleatoria));
        averia.setCoordenada(nodoSeleccionado.getCoordenada());
        averia.setEstado(false);
        return posiciones_normales.get(posicion_aleatoria);
    }

}
