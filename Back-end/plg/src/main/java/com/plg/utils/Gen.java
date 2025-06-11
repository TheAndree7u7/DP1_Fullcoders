package com.plg.utils;

import com.plg.entity.Almacen;
import com.plg.entity.Camion;
import com.plg.entity.Mapa;
import com.plg.entity.Nodo;
import com.plg.entity.Pedido;

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

    public double calcularFitness() {
        this.rutaFinal.clear();
        double fitness = 0.0;

        // Validación inicial: verificar que el camión tenga combustible
        camion.calcularDistanciaMaxima();
        if (camion.getDistanciaMaxima() <= 0) {
            fitness = Double.POSITIVE_INFINITY;
            this.descripcion = "El camión " + camion.getCodigo() + " no tiene combustible suficiente para iniciar la ruta. " +
                "Combustible actual: " + camion.getCombustibleActual() + " galones. " +
                "Distancia máxima calculada: " + camion.getDistanciaMaxima() + " km.";
            return fitness;
        }

        // VALIDACIÓN PREVIA: Calcular la distancia total de toda la ruta antes de ejecutarla
        double distanciaTotalRuta = calcularDistanciaTotalRuta();
        double distanciaMaximaInicial = camion.getDistanciaMaxima();
        
        if (distanciaTotalRuta > distanciaMaximaInicial) {
            fitness = Double.POSITIVE_INFINITY;
            this.descripcion = "⚠️ RUTA NO FACTIBLE ⚠️\n" +
                "Camión: " + camion.getCodigo() + "\n" +
                "• Distancia total de la ruta: " + String.format("%.2f", distanciaTotalRuta) + " km\n" +
                "• Distancia máxima del camión: " + String.format("%.2f", distanciaMaximaInicial) + " km\n" +
                "• Déficit de combustible: " + String.format("%.2f", distanciaTotalRuta - distanciaMaximaInicial) + " km\n" +
                "💡 Esta ruta se rechaza desde la planificación para evitar que el camión se quede sin combustible.";
            return fitness;
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
            List<Nodo> rutaAstar = Mapa.getInstance().aStar(nodo1, nodo2);

            double distanciaCalculada = rutaAstar.size();
            double distanciaMaxima = camion.calcularDistanciaMaxima();
            
            // Mejorar la validación de distancia
            if (distanciaMaxima <= 0) {
                fitness = Double.POSITIVE_INFINITY;
                this.descripcion = "El camión " + camion.getCodigo() + " se quedó sin combustible durante la ruta. " +
                    "Combustible actual: " + camion.getCombustibleActual() + " galones. " +
                    "Posición actual: " + nodo1.getCoordenada() + ", Destino: " + nodo2.getCoordenada();
                break;
            }
            
            if (distanciaMaxima < distanciaCalculada) {
                fitness = Double.POSITIVE_INFINITY;
                this.descripcion = descripcionDistanciaLejana(distanciaMaxima, distanciaCalculada, nodo1, nodo2);
                break;
            }
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
        return "⚠️  COMBUSTIBLE INSUFICIENTE ⚠️" + "\n" +
               "Camión: " + camion.getCodigo() + "\n" + 
               "• Distancia requerida: " + String.format("%.2f", distanciaCalculada) + " km\n" +
               "• Distancia máxima disponible: " + String.format("%.2f", distanciaMaxima) + " km\n" +
               "• Combustible actual: " + String.format("%.2f", camion.getCombustibleActual()) + " galones\n" +
               "• Posición actual: " + nodo1.getCoordenada() + "\n" +
               "• Destino: " + nodo2.getCoordenada() + "\n" +
               "💡 Sugerencia: El camión necesita reabastecerse en el almacén central.";
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

    /**
     * Calcula la distancia total que debe recorrer el camión para completar toda la ruta
     * incluyendo todos los nodos y el regreso al almacén central
     * @return distancia total en kilómetros
     */
    private double calcularDistanciaTotalRuta() {
        if (nodos == null || nodos.isEmpty()) {
            return 0.0;
        }
        
        double distanciaTotal = 0.0;
        
        // Calcular distancia desde la posición actual del camión hasta todos los nodos
        for (int i = 0; i < nodos.size(); i++) {
            Nodo nodo1, nodo2;
            if (i == 0) {
                nodo1 = camion;
                nodo2 = nodos.get(i);
            } else {
                nodo1 = nodos.get(i - 1);
                nodo2 = nodos.get(i);
            }
            
            // Calcular la ruta A* entre los nodos
            List<Nodo> rutaAstar = Mapa.getInstance().aStar(nodo1, nodo2);
            distanciaTotal += rutaAstar.size();
        }
        
        return distanciaTotal;
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