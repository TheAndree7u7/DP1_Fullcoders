package com.plg.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.plg.config.DataLoader;
import com.plg.entity.Almacen;
import com.plg.entity.Camion;
import com.plg.entity.Coordenada;
import com.plg.entity.Mapa;
import com.plg.entity.Nodo;
import com.plg.entity.Pedido;

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
    private List<Pedido> pedidos; // Lista de pedidos
    @Builder.Default
    private double porcentajeAsignacionCercana = 0.9; // Porcentaje de camiones a usar para asignación por cercanía

    public Individuo(List<Pedido> pedidos) {
        this.pedidos = pedidos;
        this.descripcion = "";
        inicializarCromosoma();
        this.fitness = calcularFitness();
    }

    private void inicializarCromosoma() {
        List<Almacen> almacenes = DataLoader.almacenes;
        List<Camion> camiones = DataLoader.camiones;

        // FILTRAR CAMIONES EN MANTENIMIENTO - Ubicación más eficiente
        List<Camion> camionesDisponibles = camiones.stream()
                .filter(camion -> camion.getEstado() != com.plg.entity.EstadoCamion.EN_MANTENIMIENTO_PREVENTIVO)
                .collect(java.util.stream.Collectors.toList());

        // Verificar que tengamos camiones disponibles
        if (camionesDisponibles.isEmpty()) {
            LoggerUtil.logError("⚠️  ADVERTENCIA: No hay camiones disponibles (todos en mantenimiento)");
            LoggerUtil.logWarning("Se usará la lista completa de camiones, incluyendo los que están en mantenimiento.");
            camionesDisponibles = camiones;
        }

        cromosoma = new ArrayList<>();
        for (Camion camion : camionesDisponibles) {
            cromosoma.add(new Gen(camion, new ArrayList<>()));
        }
        Almacen almacenCentral = almacenes.get(0);
        List<Nodo> pedidosMezclados = new ArrayList<>();
        pedidosMezclados.addAll(pedidos);
        Collections.shuffle(pedidosMezclados, new Random());
        List<Gen> genesMezclados = new ArrayList<>(cromosoma);
        Collections.shuffle(genesMezclados, new Random());

        // NUEVO: Para cada pedido, selecciona un subconjunto random de genes y asigna al más cercano
        Random selectorDeGen = new Random();
        for (Nodo pedido : pedidosMezclados) {
            if (!(pedido instanceof Pedido)) {
                continue;
            }
            int cantidadCercanos = (int) Math.ceil(genesMezclados.size() * porcentajeAsignacionCercana);
            // Seleccionar subconjunto random de genes
            List<Gen> subconjuntoGenes = new ArrayList<>(genesMezclados);
            Collections.shuffle(subconjuntoGenes, selectorDeGen);
            subconjuntoGenes = subconjuntoGenes.subList(0, Math.max(1, cantidadCercanos));
            double minDist = Double.POSITIVE_INFINITY;
            Gen mejorGen = null;
            
            for (Gen gen : subconjuntoGenes) {
                Nodo nodoCamion = gen.getCamion();
                double dist = Mapa.getInstance().calcularHeuristica(nodoCamion, pedido);
                double autonomia = gen.getCamion().calcularDistanciaMaxima();
                
                // Validar si la ruta completa sería válida al agregar este pedido
                List<Nodo> rutaTemporal = new ArrayList<>(gen.getNodos());
                rutaTemporal.add(pedido);
                rutaTemporal.add(almacenCentral); // Agregar almacén central al final

                // Simular la inserción de almacenes intermedios con validaciones completas
                if (simularInsercionAlmacenesIntermediosConValidacion(rutaTemporal, almacenes, selectorDeGen, gen.getCamion())) {
                    if (dist < minDist && esRutaValida(gen.getCamion(), rutaTemporal)) {
                        minDist = dist;
                        mejorGen = gen;
                    }
                }
            }
            
            // Solo asignar si hay un camión con autonomía suficiente
            if (mejorGen != null) {
                mejorGen.getPedidos().add((Pedido) pedido);
                mejorGen.getNodos().add(pedido);
            } else {
                // Si ningún camión puede llegar, el pedido se ignora en esta generación
                LoggerUtil.logWarning("⚠️ Pedido ignorado en esta generación por falta de autonomía suficiente");
            }
        }
        for (Gen gen : cromosoma) {
            gen.getNodos().add(almacenCentral);
        }
        // Insertar almacenes intermedios (almacenes index 1 y 2) en rutas largas
        for (Gen gen : cromosoma) {
            List<Nodo> nodos = gen.getNodos();
            if (nodos.size() > 3) { // al menos dos pedidos y un almacén central
                // Insertar almacén intermedio con cierta probabilidad entre dos pedidos
                for (int i = 1; i < nodos.size() - 2; i++) { // entre el primer pedido y el penúltimo
                    if (selectorDeGen.nextDouble() < 0.5) { // 50% de probabilidad
                        // Elegir aleatoriamente entre almacén 1 o 2 si existen
                        Almacen almacenIntermedio = almacenes.size() > 2 ? almacenes.get(1 + selectorDeGen.nextInt(2)) : almacenes.get(1);
                        nodos.add(i + 1, almacenIntermedio);
                        i++; // saltar el almacén recién insertado para evitar inserciones consecutivas
                    }
                }
            }
        }
    }

    public double calcularFitness() {
        this.fitness = 0.0;
        this.descripcion = ""; // Reiniciar la descripción
        guardarEstadoActual(); // Guardar el estado actual antes de calcular el fitness
        for (Gen gen : cromosoma) {
            double fitnessGen = gen.calcularFitness();
            if (fitnessGen == Double.POSITIVE_INFINITY) {
                this.descripcion = gen.getDescripcion(); // Guardar la descripción del error
                return Double.POSITIVE_INFINITY; // Si algún gen tiene fitness máximo, el individuo es inválido
            }
            fitness += fitnessGen; // Sumar el fitness de cada gen
        }
        restaurarEstadoActual();
        return fitness;
    }

    public void guardarEstadoActual() {
        for (Pedido pedido : pedidos) {
            pedido.guardarCopia();
        }
        for (Almacen almacen : DataLoader.almacenes) {
            almacen.guardarCopia();
        }
        for (Camion camion : DataLoader.camiones) {
            camion.guardarCopia();
        }
    }

    public void restaurarEstadoActual() {
        for (Pedido pedido : pedidos) {
            pedido.restaurarCopia();
        }
        for (Almacen almacen : DataLoader.almacenes) {
            almacen.restaurarCopia();
        }
        for (Camion camion : DataLoader.camiones) {
            camion.restaurarCopia();
        }
    }

    public void mutar() {
        Random rnd = new Random();
        int g1 = rnd.nextInt(cromosoma.size());
        int g2 = rnd.nextInt(cromosoma.size());
        while (g2 == g1) {
            g2 = rnd.nextInt(cromosoma.size());
        }
        Gen gen1 = cromosoma.get(g1);
        Gen gen2 = cromosoma.get(g2);
        
        List<Nodo> route1 = new ArrayList<>(gen1.getNodos()); // Crear copias para no modificar original
        List<Nodo> route2 = new ArrayList<>(gen2.getNodos());
        
        if (route1.size() > 1 && route2.size() > 1) {
            boolean mutacionValida = false;
            int intentosMaximos = 10; // Máximo 10 intentos de mutación válida
            
            for (int intento = 0; intento < intentosMaximos && !mutacionValida; intento++) {
                // Crear copias temporales para probar la mutación
                List<Nodo> tempRoute1 = new ArrayList<>(route1);
                List<Nodo> tempRoute2 = new ArrayList<>(route2);
                
                if (rnd.nextBoolean()) {
                    int i1 = rnd.nextInt(tempRoute1.size() - 1);
                    int i2 = rnd.nextInt(tempRoute2.size() - 1);
                    Nodo temp = tempRoute1.get(i1);
                    tempRoute1.set(i1, tempRoute2.get(i2));
                    tempRoute2.set(i2, temp);
                } else {
                    if (tempRoute1.size() > 2) {
                        int i1 = rnd.nextInt(tempRoute1.size() - 1);
                        Nodo nodo = tempRoute1.remove(i1);
                        tempRoute2.add(tempRoute2.size() - 1, nodo);
                    }
                }
                
                // Validar si la mutación es válida (verificar autonomía)
                if (esMutacionValida(gen1.getCamion(), tempRoute1) &&
                    esMutacionValida(gen2.getCamion(), tempRoute2)) {
                    
                    // Aplicar la mutación válida
                    gen1.setNodos(tempRoute1);
                    gen2.setNodos(tempRoute2);
                    
                    // Actualizar listas de pedidos
                    List<Pedido> nuevosPedidos1 = new ArrayList<>();
                    for (Nodo n : tempRoute1) {
                        if (n instanceof Pedido) {
                            nuevosPedidos1.add((Pedido) n);
                        }
                    }
                    gen1.setPedidos(nuevosPedidos1);
                    
                    List<Pedido> nuevosPedidos2 = new ArrayList<>();
                    for (Nodo n : tempRoute2) {
                        if (n instanceof Pedido) {
                            nuevosPedidos2.add((Pedido) n);
                        }
                    }
                    gen2.setPedidos(nuevosPedidos2);
                    
                    mutacionValida = true;
                }
            }
            
            // Solo recalcular fitness si la mutación fue válida
            if (mutacionValida) {
                this.fitness = calcularFitness();
            }
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

    private boolean esMutacionValida(Camion camion, List<Nodo> ruta) {
        if (ruta.size() < 2) return true; // Ruta vacía o solo con almacén central
        
        double autonomia = camion.calcularDistanciaMaxima();
        
        for (int i = 0; i < ruta.size() - 1; i++) {
            Nodo nodoActual = ruta.get(i);
            Nodo nodoSiguiente = ruta.get(i + 1);
            
            double distancia = Mapa.getInstance().calcularHeuristica(nodoActual, nodoSiguiente);
            if (distancia > autonomia) {
                return false; // La ruta excede la autonomía
            }
        }
        
        return true;
    }

    public boolean esIndividuoValido() {
        for (Gen gen : cromosoma) {
            if (!esMutacionValida(gen.getCamion(), gen.getNodos())) {
                return false;
            }
        }
        return true;
    }

    private boolean esRutaValida(Camion camion, List<Nodo> ruta) {
        if (ruta.size() < 2) return true; // Ruta vacía o solo con almacén central
        
        double autonomia = camion.calcularDistanciaMaxima();
        double distanciaTotal = 0.0;
        
        for (int i = 0; i < ruta.size() - 1; i++) {
            Nodo nodoActual = ruta.get(i);
            Nodo nodoSiguiente = ruta.get(i + 1);
            
            double distancia = Mapa.getInstance().calcularHeuristica(nodoActual, nodoSiguiente);
            distanciaTotal += distancia;
            
            if (distanciaTotal > autonomia) {
                return false; // La ruta excede la autonomía
            }
        }
        
        return true;
    }

    private boolean simularInsercionAlmacenesIntermediosConValidacion(List<Nodo> ruta, List<Almacen> almacenes, Random selectorDeGen, Camion camion) {
        if (ruta.size() <= 3) return true; // Ruta muy corta, no necesita almacenes intermedios
        
        double autonomiaActual = camion.calcularDistanciaMaxima();
        double combustibleActual = camion.getCombustibleActual();
        double glpActual = camion.getCapacidadActualGLP();
        
        // Simular la ruta paso a paso, verificando si necesita recargas
        for (int i = 1; i < ruta.size() - 2; i++) { // Entre el primer pedido y el penúltimo
            Nodo nodoActual = ruta.get(i);
            Nodo nodoSiguiente = ruta.get(i + 1);
            
            // Calcular distancia hasta el siguiente nodo
            double distanciaHastaSiguiente = Mapa.getInstance().calcularHeuristica(nodoActual, nodoSiguiente);
            
            // Verificar si necesita recargar antes de continuar
            if (distanciaHastaSiguiente > autonomiaActual) {
                // Necesita recargar, buscar almacén intermedio válido
                Almacen almacenIntermedio = buscarAlmacenIntermedioValido(almacenes, camion, autonomiaActual);
                
                if (almacenIntermedio != null) {
                    // Verificar que el almacén tenga combustible y GLP suficientes
                    if (almacenIntermedio.getCapacidadActualCombustible() > 0 && almacenIntermedio.getCapacidadActualGLP() > 0) {
                        // Calcular cuánto combustible necesita para completar la ruta restante
                        double combustibleNecesario = calcularCombustibleNecesarioParaRutaRestante(ruta, i + 1, camion);
                        
                        // Verificar que el almacén tenga suficiente combustible
                        if (almacenIntermedio.getCapacidadActualCombustible() >= combustibleNecesario) {
                            // Insertar el almacén intermedio
                            ruta.add(i + 1, almacenIntermedio);
                            
                            // Actualizar autonomía (recarga completa)
                            autonomiaActual = camion.getCombustibleMaximo() * 180 / (camion.getTara() + camion.getPesoCarga());
                            combustibleActual = camion.getCombustibleMaximo();
                            
                            i++; // Saltar el almacén recién insertado
                        } else {
                            return false; // Almacén no tiene suficiente combustible
                        }
                    } else {
                        return false; // Almacén no tiene combustible o GLP
                    }
                } else {
                    return false; // No hay almacén intermedio accesible
                }
            } else {
                // Consumir combustible en el trayecto
                double combustibleConsumido = distanciaHastaSiguiente * (camion.getTara() + camion.getPesoCarga()) / 180;
                combustibleActual -= combustibleConsumido;
                autonomiaActual = combustibleActual * 180 / (camion.getTara() + camion.getPesoCarga());
            }
        }
        
        return true; // Ruta válida con recargas
    }
    
    private Almacen buscarAlmacenIntermedioValido(List<Almacen> almacenes, Camion camion, double autonomiaActual) {
        // Buscar el almacén intermedio más cercano que esté dentro del alcance
        Almacen almacenMasCercano = null;
        double distanciaMinima = Double.POSITIVE_INFINITY;
        
        for (int i = 1; i < almacenes.size(); i++) { // Empezar desde el índice 1 (almacenes intermedios)
            Almacen almacen = almacenes.get(i);
            double distancia = Mapa.getInstance().calcularHeuristica(camion, almacen);
            
            if (distancia <= autonomiaActual && distancia < distanciaMinima) {
                distanciaMinima = distancia;
                almacenMasCercano = almacen;
            }
        }
        
        return almacenMasCercano;
    }
    
    private double calcularCombustibleNecesarioParaRutaRestante(List<Nodo> ruta, int indiceInicio, Camion camion) {
        double combustibleNecesario = 0.0;
        
        for (int i = indiceInicio; i < ruta.size() - 1; i++) {
            Nodo nodoActual = ruta.get(i);
            Nodo nodoSiguiente = ruta.get(i + 1);
            
            double distancia = Mapa.getInstance().calcularHeuristica(nodoActual, nodoSiguiente);
            double combustibleConsumido = distancia * (camion.getTara() + camion.getPesoCarga()) / 180;
            
            combustibleNecesario += combustibleConsumido;
        }
        
        return combustibleNecesario;
    }
}
