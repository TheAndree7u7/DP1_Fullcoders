package com.plg.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.plg.entity.Almacen;
import com.plg.entity.Camion;
import com.plg.entity.Mapa;
import com.plg.entity.Nodo;
import com.plg.entity.Pedido;

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
    private double fitness;

    public Gen(Camion camion, List<Nodo> nodosOriginal) {
        this.camion = camion;
        this.nodos = nodosOriginal;
        this.rutaFinal = new ArrayList<>();
        this.pedidos = new ArrayList<>();
    }

    public double calcularFitness() {
        this.rutaFinal.clear();
        double fitness = 0.0;
        Nodo posicionActual = camion;
        List<Nodo> rutaEntradaBloqueada = null;
        for (int i = 0; i < nodos.size(); i++) {
            Nodo destino = nodos.get(i);
            List<Nodo> rutaAstar = Mapa.getInstance().aStar(posicionActual, destino);
            double distanciaCalculada = rutaAstar.size();
            double distanciaMaxima = camion.calcularDistanciaMaxima();
            if (distanciaMaxima < distanciaCalculada) {
                fitness = Double.POSITIVE_INFINITY;
                this.descripcion = descripcionDistanciaLejana(distanciaMaxima, distanciaCalculada, posicionActual, destino);
                break;
            }
            if (destino instanceof Pedido) {
                Pedido pedido = (Pedido) destino;
                double tiempoEntregaLimite = pedido.getHorasLimite();
                double tiempoLlegada = distanciaCalculada / camion.getVelocidadPromedio() + 0.25; // +15 minutos (0.25 horas) de tiempo de despacho
                boolean tiempoMenorQueLimite = tiempoLlegada <= tiempoEntregaLimite;
                boolean volumenGLPAsignado = camion.getCapacidadActualGLP() >= pedido.getVolumenGLPAsignado();
                if (tiempoMenorQueLimite && volumenGLPAsignado) {
                    fitness += distanciaCalculada;
                    camion.actualizarCombustible(distanciaCalculada);
                    camion.entregarVolumenGLP(pedido.getVolumenGLPAsignado());
                    if (i > 0 && rutaAstar.size() > 1) {
                        rutaAstar.remove(0);
                    }
                    rutaFinal.addAll(rutaAstar);
                    
                    // Agregar tiempo de parada de 15 minutos (repetir el nodo del pedido)
                    // Como la velocidad promedio es 50 km/h y cada nodo representa 1km,
                    // en 15 minutos (0.25 horas) el camión recorrería 50 * 0.25 = 12.5 nodos
                    // Aproximamos a 13 nodos adicionales para representar la parada
                    int nodosParada = (int) Math.ceil(camion.getVelocidadPromedio() * 0.25); // 15 minutos en nodos
                    for (int j = 0; j < nodosParada; j++) {
                        rutaFinal.add(destino); // Repetir el nodo del pedido
                    }
                    
                    // Si el pedido está en un nodo bloqueado, guardar la ruta de entrada
                    if (destino.isBloqueado()) {
                        rutaEntradaBloqueada = new ArrayList<>(rutaAstar);
                    } else {
                        rutaEntradaBloqueada = null;
                    }
                    posicionActual = destino;
                } else {
                    fitness = Double.POSITIVE_INFINITY;
                    this.descripcion = descripcionError(pedido, camion, tiempoEntregaLimite, tiempoLlegada, tiempoMenorQueLimite, volumenGLPAsignado);
                    break;
                }
            } else if (destino instanceof Almacen || destino instanceof Camion) {
                recargarCamion(camion, destino);
                if (i > 0 && rutaAstar.size() > 1) {
                    rutaAstar.remove(0);
                }
                rutaFinal.addAll(rutaAstar);
                rutaEntradaBloqueada = null;
                posicionActual = destino;
            } else {
                // Otro tipo de nodo
                if (i > 0 && rutaAstar.size() > 1) {
                    rutaAstar.remove(0);
                }
                rutaFinal.addAll(rutaAstar);
                posicionActual = destino;
            }
            // Si el nodo anterior era bloqueado y hay que salir, regresar por la ruta invertida
            if (rutaEntradaBloqueada != null && i + 1 < nodos.size()) {
                // No incluir el nodo bloqueado al salir
                List<Nodo> rutaSalida = new ArrayList<>(rutaEntradaBloqueada);
                Collections.reverse(rutaSalida);
                rutaSalida.remove(0); // Quitar el nodo bloqueado
                rutaFinal.addAll(rutaSalida);
                fitness += rutaSalida.size();
                posicionActual = rutaSalida.get(rutaSalida.size() - 1); // Último nodo no bloqueado
                rutaEntradaBloqueada = null;
            }
        }
        this.fitness = fitness;
        return fitness;
    }

    private void recargarCamion(Camion camion, Nodo nodo) {
        if (nodo instanceof Almacen || nodo instanceof Camion) {
            camion.setCombustibleActual(camion.getCombustibleMaximo());
            camion.setCapacidadActualGLP(camion.getCapacidadMaximaGLP());
            if (nodo instanceof Almacen) {
                Almacen almacen = (Almacen) nodo;
                almacen.setCapacidadActualGLP(almacen.getCapacidadActualGLP() - camion.getCapacidadMaximaGLP());
                almacen.setCapacidadCombustible(almacen.getCapacidadCombustible() - camion.getCombustibleMaximo());

            }
        }
    }

    public String descripcionDistanciaLejana(double distanciaMaxima, double distanciaCalculada, Nodo nodo1, Nodo nodo2) {
        return "El camion con código " + camion.getCodigo()
                + " no puede recorrer la distancia de " + distanciaCalculada
                + " km. La distancia máxima es de " + distanciaMaxima
                + " km." + " El camión se encuentra en la posición " + nodo1.getCoordenada()
                + " y se dirige a la posición " + nodo2.getCoordenada() + ".";
    }

    public String descripcionError(Pedido pedido, Camion camion, double tiempoEntregaLimite, double tiempoLlegada, boolean tiempoMenorQueLimite, boolean volumenGLPAsignado) {
        String respuesta = "";
        if (!tiempoMenorQueLimite) {
            respuesta = "El camion con código " + camion.getCodigo()
                    + " no puede llegar a tiempo al pedido "
                    + pedido.getCodigo() + ". Tiempo de entrega: "
                    + tiempoEntregaLimite + " horas. Tiempo de llegada: "
                    + tiempoLlegada + " horas.";

        } else if (!volumenGLPAsignado) {
            respuesta = "El camion con código " + camion.getCodigo()
                    + " no tiene suficiente GLP para entregar el pedido "
                    + pedido.getCodigo() + ". Volumen de GLP asignado: "
                    + pedido.getVolumenGLPAsignado() + " m³. Capacidad actual de GLP: "
                    + camion.getCapacidadActualGLP() + " m³.";
        }
        return respuesta;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(camion.toString()).append(" ");
        for (int i = 0; i < nodos.size() - 1; i++) {
            sb.append(nodos.get(i).toString()).append(" ");
        }
        return sb.toString();
    }
}
