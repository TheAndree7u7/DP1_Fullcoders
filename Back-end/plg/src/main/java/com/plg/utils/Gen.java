package com.plg.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.time.LocalDateTime;
import com.plg.utils.Simulacion;

import com.plg.entity.Almacen;
import com.plg.entity.Camion;
import com.plg.entity.Mapa;
import com.plg.entity.Nodo;
import com.plg.entity.Pedido;
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
    }

    private void recargarCamion(Camion camion, Nodo nodo) {
        if (nodo instanceof Almacen){
            Almacen almacenRecarga = (Almacen) nodo;
            almacenRecarga.recargarGlPCamion(camion);
        }

        if(nodo instanceof Camion) {
            Camion camionRecarga = (Camion) nodo;
            camionRecarga.recargarGlPSiAveriado(camion);
        }
    }

    public String descripcionDistanciaLejana(double distanciaMaxima, double distanciaCalculada, Nodo nodo1, Nodo nodo2) {
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
            // double distanciaCalculada = rutaAstar.size();
            // double distanciaMaxima = camion.calcularDistanciaMaxima();
            // if (distanciaMaxima < distanciaCalculada) {
            //     fitness = Double.POSITIVE_INFINITY;
            //     this.descripcion = descripcionDistanciaLejana(distanciaMaxima, distanciaCalculada, posicionActual, destino);
            //     break;
            // }
            if (destino instanceof Pedido) {
                ResultadoEntrega resultado = procesarEntregaPedido((Pedido) destino, rutaAstar, fechaLlegada, fitness, posicionActual, i);
                fitness = resultado.fitness;
                fechaLlegada = resultado.fechaLlegada;
                posicionActual = resultado.posicionActual;
                rutaEntradaBloqueada = resultado.rutaEntradaBloqueada;
                if (fitness == Double.POSITIVE_INFINITY) break;
            } else if (destino instanceof Almacen || destino instanceof Camion) {
                posicionActual = procesarNodoRecarga(destino, rutaAstar, i);
                rutaEntradaBloqueada = null;
            } else {
                posicionActual = procesarNodoNormal(destino, rutaAstar, i);
            }
            if (rutaEntradaBloqueada != null && i + 1 < nodos.size()) {
                ResultadoSalidaBloqueo resultadoSalida = procesarRutaSalidaBloqueo(rutaEntradaBloqueada, fitness, posicionActual);
                fitness = resultadoSalida.fitness;
                posicionActual = resultadoSalida.posicionActual;
                rutaEntradaBloqueada = null;
            }
        }
        this.fitness = fitness;
        return fitness;
    }

    // Auxiliar para procesar la entrega de un pedido
    private ResultadoEntrega procesarEntregaPedido(Pedido pedido, List<Nodo> rutaAstar, LocalDateTime fechaLlegada, double fitness, Nodo posicionActual, int i) {
        double tiempoLlegadaHoras = rutaAstar.size() / camion.getVelocidadPromedio() + 0.25;
        LocalDateTime nuevaFechaLlegada = fechaLlegada.plusMinutes((long) (tiempoLlegadaHoras * 60));
        boolean dentroDeLimite = pedido.getFechaLimite() == null || !nuevaFechaLlegada.isAfter(pedido.getFechaLimite());
        // Calcular la cantidad de pedidos asignados a este camión
        int cantidadPedidosAsignados = this.pedidos != null && !this.pedidos.isEmpty() ? this.pedidos.size() : 1;
        double glpPorPedido = camion.getCapacidadActualGLP() / cantidadPedidosAsignados;
        double volumenPendiente = pedido.getVolumenGLPAsignado() - pedido.getVolumenGLPEntregado();
        double volumenAEntregar = Math.min(glpPorPedido, volumenPendiente);
        boolean entregadoCompleto = (pedido.getVolumenGLPEntregado() + volumenAEntregar) >= pedido.getVolumenGLPAsignado();
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
            this.descripcion = "El pedido " + pedido.getCodigo() + " no puede ser entregado a tiempo. Fecha límite: " + pedido.getFechaLimite() + ", fecha llegada: " + nuevaFechaLlegada;
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

    public List<Nodo> construirRutaFinalApi(){
        List <Nodo> rutaApi = new ArrayList<>();
        for (Nodo nodo : rutaFinal) {
            if (nodo instanceof Pedido && this.getPedidos().contains(nodo)) {
                // Agregamos 12 veces el mismo
                for (int j = 0; j < 12; j++) {
                    rutaApi.add(nodo);
                }
            } else if (nodo instanceof Camion && this.getCamionesAveriados().contains(nodo)) {
                for (int j = 0; j < 12; j++) {
                    rutaApi.add(nodo);
                }
            } else {
                // Si es un nodo normal, lo agregamos una sola vez
                rutaApi.add(nodo);
            }
        }
        return rutaApi;
    }

    // Auxiliar para procesar rutas de salida de bloqueos
    private ResultadoSalidaBloqueo procesarRutaSalidaBloqueo(List<Nodo> rutaEntradaBloqueada, double fitness, Nodo posicionActual) {
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

    // Clases auxiliares para devolver múltiples valores
    private static class ResultadoEntrega {
        double fitness;
        LocalDateTime fechaLlegada;
        Nodo posicionActual;
        List<Nodo> rutaEntradaBloqueada;
        ResultadoEntrega(double fitness, LocalDateTime fechaLlegada, Nodo posicionActual, List<Nodo> rutaEntradaBloqueada) {
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
}
