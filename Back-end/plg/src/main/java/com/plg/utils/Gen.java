package com.plg.utils;

import com.plg.entity.Almacen;
import com.plg.entity.Camion;
import com.plg.entity.Mapa;
import com.plg.entity.Nodo;
import com.plg.entity.Pedido;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@Getter
@Setter
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

        // ✅ VALIDACIÓN PREVIA: Verificar si puede completar toda la ruta del cromosoma
        if (!puedeCompletarRutaCompleta()) {
            this.fitness = Double.POSITIVE_INFINITY;
            return this.fitness;
        }

        double fitness = 0.0;

        for (int i = 0; i < nodos.size(); i++) {
            Nodo nodo1, nodo2;
            if (i == 0) {
                nodo1 = camion;
                nodo2 = nodos.get(i);
            } else {
                nodo1 = nodos.get(i - 1);
                nodo2 = nodos.get(i);
            }
            List<Nodo> rutaAstar = Mapa.getInstance().aStar(nodo1, nodo2);

            double distanciaCalculada = rutaAstar.size();
            double distanciaMaxima = camion.calcularDistanciaMaxima();
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
                    if (i > 0) {
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
                if (i > 0) {
                    rutaAstar.remove(0);
                }
                rutaFinal.addAll(rutaAstar);
            }
        }

        // ✅ VALIDACIÓN FINAL: Verificar regreso al almacén si completó toda la ruta
        if (fitness != Double.POSITIVE_INFINITY && !nodos.isEmpty()) {
            if (!puedeRegresarAlAlmacen()) {
                fitness = Double.POSITIVE_INFINITY;
                return this.fitness;
            }
        }

        this.fitness = fitness;
        return fitness;
    }

// ✅ NUEVO MÉTODO: Validar toda la ruta del cromosoma antes de ejecutarla
    private boolean puedeCompletarRutaCompleta() {
        if (nodos.isEmpty()) {
            return true;
        }

        // Crear una copia del camión para simulación sin modificar el original
        Camion camionSimulacion = crearCopiaCamion();
        double distanciaTotalRecorrida = 0.0;
        Nodo nodoAnterior = camionSimulacion;

        // Simular toda la ruta
        for (Nodo nodoActual : nodos) {
            List<Nodo> rutaParcial = Mapa.getInstance().aStar(nodoAnterior, nodoActual);
            double distanciaTramo = rutaParcial.size();
            distanciaTotalRecorrida += distanciaTramo;

            // Verificar si puede llegar a este nodo
            double distanciaMaximaPosible = camionSimulacion.calcularDistanciaMaxima();
            if (distanciaMaximaPosible < distanciaTramo) {
                this.descripcion = "El camión " + camion.getCodigo()
                        + " no puede completar la ruta del cromosoma. "
                        + "No puede llegar al nodo " + nodoActual.getCoordenada()
                        + " desde " + nodoAnterior.getCoordenada() + ". "
                        + "Distancia requerida: " + distanciaTramo + " km, "
                        + "distancia máxima posible: " + distanciaMaximaPosible + " km.";
                return false;
            }

            // Simular el consumo de combustible y GLP
            camionSimulacion.actualizarCombustible(distanciaTramo);

            if (nodoActual instanceof Pedido) {
                Pedido pedido = (Pedido) nodoActual;
                camionSimulacion.entregarVolumenGLP(pedido.getVolumenGLPAsignado());
            } else if (nodoActual instanceof Almacen || nodoActual instanceof Camion) {
                // Simular recarga
                camionSimulacion.setCombustibleActual(camionSimulacion.getCombustibleMaximo());
                camionSimulacion.setCapacidadActualGLP(camionSimulacion.getCapacidadMaximaGLP());
            }

            nodoAnterior = nodoActual;
        }

        // ✅ CRUCIAL: Verificar si puede regresar al almacén de origen
        List<Nodo> rutaRegreso = Mapa.getInstance().aStar(nodoAnterior, camion);
        double distanciaRegreso = rutaRegreso.size();
        double distanciaMaximaParaRegreso = camionSimulacion.calcularDistanciaMaxima();

        if (distanciaMaximaParaRegreso < distanciaRegreso) {
            this.descripcion = "El camión " + camion.getCodigo()
                    + " no puede regresar al almacén después de completar todos los pedidos. "
                    + "Distancia de regreso requerida: " + distanciaRegreso + " km, "
                    + "distancia máxima posible: " + distanciaMaximaParaRegreso + " km. "
                    + "Posición final: " + nodoAnterior.getCoordenada()
                    + ", almacén: " + camion.getCoordenada() + ".";
            return false;
        }

        return true;
    }

// ✅ NUEVO MÉTODO: Verificar si puede regresar al almacén al final
    private boolean puedeRegresarAlAlmacen() {
        if (nodos.isEmpty()) {
            return true;
        }

        Nodo ultimoNodo = nodos.get(nodos.size() - 1);
        List<Nodo> rutaRegreso = Mapa.getInstance().aStar(ultimoNodo, camion);
        double distanciaRegreso = rutaRegreso.size();
        double distanciaMaxima = camion.calcularDistanciaMaxima();

        if (distanciaMaxima < distanciaRegreso) {
            this.descripcion = "El camión " + camion.getCodigo()
                    + " no puede regresar al almacén desde " + ultimoNodo.getCoordenada()
                    + " al almacén " + camion.getCoordenada() + ". "
                    + "Distancia de regreso: " + distanciaRegreso + " km, "
                    + "distancia máxima: " + distanciaMaxima + " km.";
            return false;
        }

        return true;
    }

// ✅ NUEVO MÉTODO: Crear copia del camión para simulación
    private Camion crearCopiaCamion() {
        Camion copia = new Camion();
        copia.setCodigo(camion.getCodigo());
        copia.setTipo(camion.getTipo());
        copia.setCapacidadMaximaGLP(camion.getCapacidadMaximaGLP());
        copia.setCapacidadActualGLP(camion.getCapacidadActualGLP());
        copia.setTara(camion.getTara());
        copia.setPesoCarga(camion.getPesoCarga());
        copia.setPesoCombinado(camion.getPesoCombinado());
        copia.setEstado(camion.getEstado());
        copia.setCombustibleMaximo(camion.getCombustibleMaximo());
        copia.setCombustibleActual(camion.getCombustibleActual());
        copia.setVelocidadPromedio(camion.getVelocidadPromedio());
        copia.setDistanciaMaxima(camion.getDistanciaMaxima());
        copia.setCoordenada(camion.getCoordenada());
        return copia;
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
