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
    private Camion camion;
    private List<Nodo> nodos;
    private List<Nodo> rutaFinal;
    private double fitness;

    public Gen(Camion camion, List<Nodo> nodos) {
        this.camion = camion;
        this.nodos = nodos;
        this.rutaFinal = new ArrayList<>();
    }

    public double calcularFitness() {
        this.rutaFinal.clear();
        double fitness = 0.0;
        Camion camion = this.camion.clone(); 

        for (int i = 0; i < nodos.size(); i++) {
            Nodo nodo1, nodo2;
            if(i == 0){
                nodo1 = camion;
                nodo2 = nodos.get(i);
            }else{
                nodo1 = nodos.get(i - 1);
                nodo2 = nodos.get(i);
            }
            List<Nodo> ruta = Mapa.getInstance().aStar(nodo1, nodo2);

            double distanciaCalculada = ruta.size();
            double distanciaMaxima = camion.calcularDistanciaMaxima();

            if (distanciaMaxima < distanciaCalculada) {
                fitness = Double.MIN_VALUE;
                break;
            }

            if (nodo2 instanceof Pedido) {
                Pedido pedido = (Pedido) nodo2;
                double tiempoEntregaLimite = pedido.getHorasLimite();
                double tiempoLlegada = distanciaCalculada / camion.getVelocidadPromedio();

                boolean tiempoMenorQueLimite = tiempoLlegada <= tiempoEntregaLimite;
                boolean volumenGLPAsignado = camion.getCapacidadActualGLP() >= pedido.getVolumenGLPAsignado();

                if (tiempoMenorQueLimite && volumenGLPAsignado) {
                    fitness += tiempoEntregaLimite - tiempoLlegada;
                    camion.actualizarCombustible(distanciaCalculada);
                    camion.actualizarCargaPedido(pedido.getVolumenGLPAsignado());

                    if (i > 0){
                        ruta.remove(0);
                    }
                    rutaFinal.addAll(ruta);
                } else {
                    fitness = Double.MIN_VALUE;
                    break;
                }
            } else if (nodo2 instanceof Almacen || nodo2 instanceof Camion) {
                recargarCamion(camion, nodo2);

                if (i > 0){
                    ruta.remove(0);
                }

                rutaFinal.addAll(ruta);
            }
        }
        this.fitness = fitness;
        return fitness;
    }

    private void recargarCamion(Camion camion, Nodo nodo) {
        if (nodo instanceof Almacen || nodo instanceof Camion) {
            camion.setCombustibleActual(camion.getCombustibleMaximo());
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(camion.toString()).append(" ");
        // // No se imprime el ultimo nodo porque es el almacén central
        // for (int i = 0; i < nodos.size(); i++) {
        //     sb.append(nodos.get(i).toString()).append(" ");
        // }
        for (int i = 0; i < nodos.size() - 1; i++) {
            sb.append(nodos.get(i).toString()).append(" ");
        }
        return sb.toString();
    }
}