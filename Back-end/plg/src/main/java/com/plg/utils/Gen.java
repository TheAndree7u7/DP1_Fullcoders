package com.plg.utils;

import com.plg.entity.Almacen;
import com.plg.entity.Camion;
import com.plg.entity.Mapa;
import com.plg.entity.Nodo;
import com.plg.entity.Pedido;
import com.plg.entity.Coordenada;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private List<Pedido> pedidos;
    private double fitness;

    public Gen(Camion camion, List<Nodo> nodosOriginal) {
        this.camion = camion;
        this.nodos = nodosOriginal;
        this.rutaFinal = new ArrayList<>();
        this.pedidos = new ArrayList<>();
    }

    private boolean detectarTeletransporte(Nodo nodo1, Nodo nodo2, List<Nodo> rutaAstar) {
        // Si la ruta está vacía, es un teletransporte
        if (rutaAstar.isEmpty()) {
            this.descripcion = "Teletransporte detectado: No existe ruta entre " + 
                nodo1.getCoordenada() + " y " + nodo2.getCoordenada();
            return true;
        }

        // Verificar que la ruta sea continua
        for (int i = 0; i < rutaAstar.size() - 1; i++) {
            Nodo actual = rutaAstar.get(i);
            Nodo siguiente = rutaAstar.get(i + 1);
            
            // Calcular distancia entre nodos adyacentes
            double distancia = Math.sqrt(
                Math.pow(actual.getCoordenada().getFila() - siguiente.getCoordenada().getFila(), 2) +
                Math.pow(actual.getCoordenada().getColumna() - siguiente.getCoordenada().getColumna(), 2)
            );
            
            // Si la distancia es mayor a 1, es un teletransporte
            if (distancia > 1.5) { // Usamos 1.5 para permitir movimientos diagonales
                this.descripcion = "Teletransporte detectado en la ruta: " +
                    actual.getCoordenada() + " -> " + siguiente.getCoordenada() +
                    " (distancia: " + distancia + ")";
                return true;
            }
        }
        return false;
    }

    public boolean validarRuta() {
        double combustibleDisponible = camion.getCombustibleActual();
        double distanciaTotal = 0;
        Coordenada posicionActual = camion.getCoordenada();
        
        for (int i = 0; i < nodos.size(); i++) {
            Nodo nodo1, nodo2;
            if(i == 0){
                nodo1 = camion;
                nodo2 = nodos.get(i);
            }else{
                nodo1 = nodos.get(i - 1);
                nodo2 = nodos.get(i);
            }

            // Obtener la ruta entre los nodos
            List<Nodo> rutaAstar = Mapa.getInstance().aStar(nodo1, nodo2);
            
            // Detectar teletransportes
            if (detectarTeletransporte(nodo1, nodo2, rutaAstar)) {
                return false;
            }

            double distanciaCalculada = rutaAstar.size();
            
            // Verificar si hay suficiente combustible para esta parte de la ruta
            double combustibleNecesario = (distanciaCalculada * (camion.getTara() + camion.getPesoCarga())) / 180;
            if (combustibleNecesario > combustibleDisponible) {
                this.descripcion = descripcionDistanciaLejana(
                    (combustibleDisponible * 180) / (camion.getTara() + camion.getPesoCarga()),
                    distanciaCalculada,
                    nodo1,
                    nodo2
                );
                return false;
            }
            
            distanciaTotal += distanciaCalculada;
            combustibleDisponible -= combustibleNecesario;
            
            // Si llegamos a un almacén, recargamos combustible
            if (nodo2 instanceof Almacen) {
                combustibleDisponible = camion.getCombustibleMaximo();
            }

            // Actualizar la posición actual
            posicionActual = nodo2.getCoordenada();
        }
        return true;
    }

    public double calcularFitness() {
        this.rutaFinal.clear();
        double fitness = 0.0;
        Coordenada posicionActual = camion.getCoordenada();

        // Primero validamos la ruta completa
        if (!validarRuta()) {
            return Double.POSITIVE_INFINITY;
        }

        for (int i = 0; i < nodos.size(); i++) {
            Nodo nodo1, nodo2;
            if(i == 0){
                nodo1 = camion;
                nodo2 = nodos.get(i);
            }else{
                nodo1 = nodos.get(i - 1);
                nodo2 = nodos.get(i);
            }

            // Verificar que los nodos sean adyacentes o tengan una ruta válida
            List<Nodo> rutaAstar = Mapa.getInstance().aStar(nodo1, nodo2);
            if (rutaAstar.isEmpty()) {
                this.descripcion = "No existe una ruta válida entre " + nodo1.getCoordenada() + " y " + nodo2.getCoordenada();
                return Double.POSITIVE_INFINITY;
            }

            double distanciaCalculada = rutaAstar.size();

            if (nodo2 instanceof Pedido) {
                Pedido pedido = (Pedido) nodo2;

                // Si se trata de un pedido verificamos que lleguemos a tiempo 
                double tiempoEntregaLimite = pedido.getHorasLimite();
                double tiempoLlegada = distanciaCalculada / camion.getVelocidadPromedio();
                boolean tiempoMenorQueLimite = tiempoLlegada <= tiempoEntregaLimite;

                // Verificamos que el camion tenga sufiente combustible
                boolean volumenGLPAsignado = camion.getCapacidadActualGLP() >= pedido.getVolumenGLPAsignado();

                if (tiempoMenorQueLimite && volumenGLPAsignado) {
                    fitness += distanciaCalculada; // Aumentamos el fitness por la distancia recorrida
                    // Combustible gastado
                    camion.actualizarCombustible(distanciaCalculada);

                    // Actualizamos el volumen de GLP del camion
                    camion.entregarVolumenGLP(pedido.getVolumenGLPAsignado());
                    if (i > 0){
                        rutaAstar.remove(0);
                    }
                    rutaFinal.addAll(rutaAstar);
                } else {
                    fitness = Double.POSITIVE_INFINITY;
                    this.descripcion = descripcionError(pedido, camion, tiempoEntregaLimite, tiempoLlegada, tiempoMenorQueLimite, volumenGLPAsignado);
                    break;
                }
            } else if (nodo2 instanceof Almacen || nodo2 instanceof Camion) {
                recargarCamion(camion, nodo2);
                if (i > 0){
                    rutaAstar.remove(0);
                }
                rutaFinal.addAll(rutaAstar);
            }

            // Actualizar la posición actual
            posicionActual = nodo2.getCoordenada();
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
                almacen.setCapacidadActualGLP(almacen.getCapacidadActualGLP()- camion.getCapacidadMaximaGLP());
                almacen.setCapacidadCombustible(almacen.getCapacidadCombustible()- camion.getCombustibleMaximo());

            }
        }
    }

    public String descripcionDistanciaLejana(double distanciaMaxima, double distanciaCalculada, Nodo nodo1, Nodo nodo2) {
        return "El camion con código " + camion.getCodigo() + 
                    " no puede recorrer la distancia de " + distanciaCalculada + 
                    " km. La distancia máxima es de " + distanciaMaxima + 
                    " km." + " El camión se encuentra en la posición " + nodo1.getCoordenada() + 
                    " y se dirige a la posición " + nodo2.getCoordenada() + ".";
    }

    public String descripcionError(Pedido pedido, Camion camion, double tiempoEntregaLimite, double tiempoLlegada, boolean tiempoMenorQueLimite, boolean volumenGLPAsignado) {
        String respuesta = "";
        if (!tiempoMenorQueLimite) {
            respuesta =  "El camion con código " + camion.getCodigo() +
             " no puede llegar a tiempo al pedido "
              + pedido.getCodigo() + ". Tiempo de entrega: "
               + tiempoEntregaLimite + " horas. Tiempo de llegada: "
                + tiempoLlegada + " horas.";
          
        } else if (!volumenGLPAsignado) {
            respuesta = "El camion con código " + camion.getCodigo() +
             " no tiene suficiente GLP para entregar el pedido "
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