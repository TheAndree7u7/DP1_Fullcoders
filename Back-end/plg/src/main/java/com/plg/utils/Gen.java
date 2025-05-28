package com.plg.utils;

import com.fasterxml.jackson.databind.ser.std.MapProperty;
import com.plg.entity.Almacen;
import com.plg.entity.Camion;
import com.plg.entity.Mapa;
import com.plg.entity.Nodo;
import com.plg.entity.Pedido;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Gen {
    private int posNodo = 0;
    private String descripcion; 
    private Camion camion;
    private List<Nodo> nodos;
    private List<Nodo> rutaFinal;
    private double fitness;

    public Gen(Camion camionOriginal, List<Nodo> nodosOriginal) {
        this.camion = camionOriginal;
        this.nodos = nodosOriginal;
        this.rutaFinal = new ArrayList<>();
    }

    public double calcularFitness() {
        this.rutaFinal.clear();
        double fitness = 0.0;
        Camion camionClone = this.camion.getClone(); 

        List<Nodo> nodosClone = new ArrayList<>();
        for (Nodo nodo : nodos) {
            nodosClone.add(nodo.getClone());
        }

        for (int i = 0; i < nodosClone.size(); i++) {
            Nodo nodo1, nodo2;
            if(i == 0){
                nodo1 = camionClone;
                nodo2 = nodosClone.get(i);
            }else{
                nodo1 = nodosClone.get(i - 1);
                nodo2 = nodosClone.get(i);
            }
            List<Nodo> ruta = Mapa.getInstance().aStar(nodo1, nodo2);

            double distanciaCalculada = ruta.size();
            double distanciaMaxima = camionClone.calcularDistanciaMaxima();

            // La distancia maxima que puede recorrer el camion es menor a la distancia calculada
            if (distanciaMaxima < distanciaCalculada) {
                fitness = Double.MIN_VALUE;
                this.descripcion = "El camion con código " + camionClone.getCodigo() + " no puede recorrer la distancia de " + distanciaCalculada + " km. La distancia máxima es de " + distanciaMaxima + " km." + " El camión se encuentra en la posición " + nodo1.getCoordenada() + " y se dirige a la posición " + nodo2.getCoordenada() + ".";
                break;
            }

            if (nodo2 instanceof Pedido) {
                Pedido pedido = (Pedido) nodo2;

                // Si se trata de un pedido verificamos que lleguemos a tiempo 
                double tiempoEntregaLimite = pedido.getHorasLimite();
                double tiempoLlegada = distanciaCalculada / camionClone.getVelocidadPromedio();
                boolean tiempoMenorQueLimite = tiempoLlegada <= tiempoEntregaLimite;

                // Verificamos que el camion tenga sufiente combustible
                boolean volumenGLPAsignado = camionClone.getCapacidadActualGLP() >= pedido.getVolumenGLPAsignado();

                if (tiempoMenorQueLimite && volumenGLPAsignado) {
                    fitness += tiempoEntregaLimite - tiempoLlegada;
                    // Combustible gastado
                    camionClone.actualizarCombustible(distanciaCalculada);

                    // Actualizamos el volumen de GLP del camion
                    camionClone.actualizarCargaPedido(pedido.getVolumenGLPAsignado());

                    if (i > 0){
                        ruta.remove(0);
                    }
                    rutaFinal.addAll(ruta);
                } else {
                    fitness = Double.MIN_VALUE;
                    if(!tiempoMenorQueLimite) {
                        this.descripcion = "El camion con código " + camionClone.getCodigo() + " no puede llegar a tiempo al pedido " + pedido.getCodigo() + ". Tiempo de entrega: " + tiempoEntregaLimite + " horas. Tiempo de llegada: " + tiempoLlegada + " horas.";
                    } else if (!volumenGLPAsignado) {
                        this.descripcion = "El camion con código " + camionClone.getCodigo() + " no tiene suficiente GLP para entregar el pedido " + pedido.getCodigo() + ". Volumen GLP asignado: " + pedido.getVolumenGLPAsignado() + ". Volumen GLP disponible: " + camionClone.getCapacidadActualGLP();
                    }
                    break;
                }
            } else if (nodo2 instanceof Almacen || nodo2 instanceof Camion) {
                // Se regarga si es almacen central, intermediario o camion
                recargarCamion(camionClone, nodo2);

                if (i > 0){
                    ruta.remove(0);
                }

                rutaFinal.addAll(ruta);
            }
        }
        this.fitness = fitness;
        return fitness;
    }

    private void recargarCamion(Camion camionClone, Nodo nodo) {
        if (nodo instanceof Almacen || nodo instanceof Camion) {
            camionClone.setCombustibleActual(camionClone.getCombustibleMaximo());
            camionClone.setCapacidadActualGLP(camionClone.getCapacidadMaximaGLP());
            if (nodo instanceof Almacen) {
                Almacen almacen = (Almacen) nodo;
                // Restamos el volumen de GLP del camion al almacen
                almacen.setCapacidadActualGLP(almacen.getCapacidadActualGLP()- camionClone.getCapacidadMaximaGLP());
                // Restamos el combustible del camion al almacen
                almacen.setCapacidadCombustible(almacen.getCapacidadCombustible()- camionClone.getCombustibleMaximo());

            }
        }
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