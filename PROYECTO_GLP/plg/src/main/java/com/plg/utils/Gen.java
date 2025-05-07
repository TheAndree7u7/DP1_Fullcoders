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
        double fitness = 0.0;

        Camion camion = this.camion.clone(); 
        rutaFinal = new ArrayList<>();

        for (int i = 0; i < nodos.size() - 1; i++) {
            Nodo nodo1 = nodos.get(i);
            Nodo nodo2 = nodos.get(i + 1);
            List<Nodo> ruta = Mapa.getInstance().aStar(nodo1, nodo2);
            double distanciaCalculada = ruta.size();
            double distanciaMaxima = camion.calcularDistanciaMaxima();

            if (!validarRuta(distanciaMaxima, distanciaCalculada)) {
                fitness = Double.MIN_VALUE;
                break;
            }

            if (nodo2 instanceof Pedido) {
                Pedido pedido = (Pedido) nodo2;
                double tiempoEntregaLimite = pedido.getHorasLimite();
                double tiempoLlegada = distanciaCalculada / camion.getVelocidadPromedio();

                if (tiempoLlegada <= tiempoEntregaLimite) {
                    fitness += tiempoEntregaLimite - tiempoLlegada;
                    camion.actualizarCombustible(distanciaCalculada);
                    camion.actualizarCargaPedido(pedido.getVolumenGLPAsignado());
                    rutaFinal.addAll(ruta);
                } else {
                    fitness = Double.MIN_VALUE;
                    break;
                }
            } else if (nodo2 instanceof Almacen || nodo2 instanceof Camion) {
                recargarCamion(camion, nodo2);
                rutaFinal.addAll(ruta);
            }
        }
        this.fitness = fitness;
        return fitness;
    }

    private boolean validarRuta(double distanciaMaxima, double distanciaCalculada) {
        return distanciaMaxima >= distanciaCalculada;
    }

    private void recargarCamion(Camion camion, Nodo nodo) {
        if (nodo instanceof Almacen || nodo instanceof Camion) {
            camion.setCombustibleActual(camion.getCapacidadTanque());
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(camion.toString()).append(" ");
        for (Nodo nodo : nodos) {
            sb.append(nodo.toString()).append(" ");
        }
        return sb.toString();
    }
}