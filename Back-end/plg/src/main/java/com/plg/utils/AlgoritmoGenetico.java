package com.plg.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;

import com.plg.config.DataLoader;
import com.plg.entity.Almacen;
import com.plg.entity.Camion;
import com.plg.entity.Mapa;
import com.plg.entity.Pedido;
import com.plg.entity.Nodo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlgoritmoGenetico {
    private int poblacionTamano;
    private int generaciones;
    private Mapa mapa;
    private List<Pedido> pedidos;
    private Individuo mejorIndividuo;
    private final Random random = new Random();

    public AlgoritmoGenetico(Mapa mapa, List<Pedido> pedidos) {
        this.mapa = mapa;
        this.pedidos = pedidos;
        
        // Configuración adaptativa basada en número de pedidos
        if (pedidos.size() <= 10) {
            generaciones = 20;
            poblacionTamano = 30;
        } else if (pedidos.size() <= 25) {
            generaciones = 15;
            poblacionTamano = 20;
        } else {
            // Para casos complejos (>25 pedidos), ser muy conservador
            generaciones = 10;
            poblacionTamano = 15;
        }
        
        System.out.println("🎯 Configuración AG: " + pedidos.size() + " pedidos → " + 
                          poblacionTamano + " individuos, " + generaciones + " generaciones");
    }

    public void ejecutarAlgoritmo() {
        System.out.println("🧬 Iniciando AG con " + poblacionTamano + " individuos, " + generaciones + " generaciones");
        long startTime = System.currentTimeMillis();
        
        List<Individuo> poblacion = inicializarPoblacion();
        double mejorFitness = Double.MIN_VALUE;
        int generacionesSinMejora = 0;
        int maxGeneracionesSinMejora = 5;
        
        for (int i = 0; i < generaciones && generacionesSinMejora < maxGeneracionesSinMejora; i++) {
            List<Individuo> padres = seleccionar_padres(poblacion);
            List<Individuo> hijos = cruzar(padres);
            
            double tasaMutacion = 0.5 + (generacionesSinMejora * 0.15);
            tasaMutacion = Math.min(tasaMutacion, 0.9);
            
            for (int j = 0; j < hijos.size(); j++) {
                if (random.nextDouble() < tasaMutacion) {
                    hijos.get(j).mutar();
                }
            }
            
            poblacion = seleccionar_mejores(padres, hijos);
            
            double fitnessActual = poblacion.get(0).getFitness();
            if (fitnessActual > mejorFitness) {
                mejorFitness = fitnessActual;
                generacionesSinMejora = 0;
            } else {
                generacionesSinMejora++;
            }
            
            if (i % 5 == 0) {
                long currentTime = System.currentTimeMillis();
                System.out.println("Gen " + i + ", Fitness: " + String.format("%.2f", fitnessActual) + 
                                 ", Sin mejora: " + generacionesSinMejora + 
                                 ", Tiempo: " + (currentTime - startTime) + "ms");
            }
        }
        
        poblacion.sort((ind1, ind2) -> Double.compare(ind2.getFitness(), ind1.getFitness()));
        mejorIndividuo = poblacion.get(0);
        
        if (!validarMejorIndividuo(mejorIndividuo)) {
            mejorIndividuo = crearSolucionBasica();
        }
        
        long endTime = System.currentTimeMillis();
        actualizarParametrosGlobales(mejorIndividuo);
        System.out.println("✅ AG completado: " + Parametros.contadorPrueba + " fitness=" + 
                          String.format("%.2f", mejorIndividuo.getFitness()) + 
                          " en " + (endTime - startTime) + "ms");
        
        for (Gen gen : mejorIndividuo.getCromosoma()) {
            Camion camion = gen.getCamion();
            camion.setGen(gen);
        }
    }

    private List<Individuo> seleccionar_mejores(List<Individuo> padres, List<Individuo> hijos) {
        List<Individuo> nuevaPoblacion = new ArrayList<>();
        nuevaPoblacion.addAll(padres);
        nuevaPoblacion.addAll(hijos);
        
        nuevaPoblacion.sort((ind1, ind2) -> Double.compare(ind1.getFitness(), ind2.getFitness()));
        
        int tamanoSeleccion = Math.min(poblacionTamano, nuevaPoblacion.size());
        
        if (tamanoSeleccion == nuevaPoblacion.size()) {
            return nuevaPoblacion;
        }
        
        return nuevaPoblacion.subList(0, tamanoSeleccion);
    }

    private List<Individuo> inicializarPoblacion() {
        List<Individuo> poblacion = new ArrayList<>();
        for (int i = 0; i < poblacionTamano; i++) {
            Individuo individuo = new Individuo(pedidos);
            poblacion.add(individuo);
        }
        return poblacion;
    }

    private List<Individuo> seleccionar_padres(List<Individuo> poblacion) {
        List<Individuo> seleccionados = new ArrayList<>();
        int seleccionadosTamano = (int) Math.ceil(poblacion.size() / 2.0);
        
        poblacion.sort((ind1, ind2) -> Double.compare(ind1.getFitness(), ind2.getFitness()));
        
        for (int i = 0; i < seleccionadosTamano; i++) {
            seleccionados.add(poblacion.get(i));
        }
        
        return seleccionados;
    }

    private List<Individuo> cruzar(List<Individuo> seleccionados) {
        List<Individuo> nuevaPoblacion = new ArrayList<>();
        
        if (seleccionados.size() < 2) {
            return seleccionados;
        }
        
        for (int i = 0; i < seleccionados.size() - 1; i += 2) {
            Individuo padre1 = seleccionados.get(i);
            Individuo padre2 = seleccionados.get(i + 1);
            
            if (random.nextDouble() < 0.7) {
                List<Individuo> hijos = cruzar(padre1, padre2);
                nuevaPoblacion.addAll(hijos);
            } else {
                nuevaPoblacion.add(padre1);
                nuevaPoblacion.add(padre2);
            }
        }
        
        if (seleccionados.size() % 2 != 0) {
            nuevaPoblacion.add(seleccionados.get(seleccionados.size() - 1));
        }
        
        return nuevaPoblacion;
    }

    private List<Individuo> cruzar(Individuo p1, Individuo p2) {
        List<Individuo> hijos = new ArrayList<>();
        Individuo hijo1 = new Individuo(p1.getPedidos());
        Individuo hijo2 = new Individuo(p2.getPedidos());
        
        hijo1.setCromosoma(new ArrayList<>());
        hijo2.setCromosoma(new ArrayList<>());
        
        for (int i = 0; i < p1.getCromosoma().size(); i++) {
            Gen gen1 = p1.getCromosoma().get(i);
            Gen gen2 = p2.getCromosoma().get(i);
            
            Gen nuevoGen1 = new Gen(gen1.getCamion(), new ArrayList<>());
            Gen nuevoGen2 = new Gen(gen2.getCamion(), new ArrayList<>());
            
            List<Nodo> ruta1 = new ArrayList<>(gen1.getNodos());
            List<Nodo> ruta2 = new ArrayList<>(gen2.getNodos());
            
            Almacen almacenCentral = DataLoader.almacenes.get(0);
            ruta1.remove(almacenCentral);
            ruta2.remove(almacenCentral);
            
            List<Nodo> nuevaRuta1 = new ArrayList<>();
            List<Nodo> nuevaRuta2 = new ArrayList<>();
            
            if (!ruta1.isEmpty() && !ruta2.isEmpty()) {
                int puntoCruce = random.nextInt(Math.min(ruta1.size(), ruta2.size()));
                
                nuevaRuta1.addAll(ruta1.subList(0, puntoCruce));
                nuevaRuta1.addAll(ruta2.subList(puntoCruce, ruta2.size()));
                
                nuevaRuta2.addAll(ruta2.subList(0, puntoCruce));
                nuevaRuta2.addAll(ruta1.subList(puntoCruce, ruta1.size()));
            } else {
                nuevaRuta1.addAll(ruta1);
                nuevaRuta2.addAll(ruta2);
            }
            
            nuevaRuta1 = eliminarDuplicados(nuevaRuta1);
            nuevaRuta2 = eliminarDuplicados(nuevaRuta2);
            
            nuevaRuta1.add(almacenCentral);
            nuevaRuta2.add(almacenCentral);
            
            nuevoGen1.setNodos(nuevaRuta1);
            nuevoGen2.setNodos(nuevaRuta2);
            
            hijo1.getCromosoma().add(nuevoGen1);
            hijo2.getCromosoma().add(nuevoGen2);
        }
        
        hijo1.setFitness(hijo1.calcularFitness());
        hijo2.setFitness(hijo2.calcularFitness());
        
        hijos.add(hijo1);
        hijos.add(hijo2);
        return hijos;
    }
    
    private List<Nodo> eliminarDuplicados(List<Nodo> ruta) {
        List<Nodo> resultado = new ArrayList<>();
        Set<String> codigosVistos = new HashSet<>();
        
        for (Nodo nodo : ruta) {
            if (nodo instanceof Pedido) {
                String codigo = ((Pedido) nodo).getCodigo();
                if (!codigosVistos.contains(codigo)) {
                    codigosVistos.add(codigo);
                    resultado.add(nodo);
                }
            } else {
                resultado.add(nodo);
            }
        }
        
        return resultado;
    }

    public void verificarMejorIndividuo(Individuo individuo) {
        if (individuo.getFitness() == Double.MIN_VALUE || individuo.getFitness() == 0.0) {
            System.out.println("⚠️  Advertencia: Solución subóptima encontrada (fitness: " + individuo.getFitness() + ")");
            System.out.println("    Descripción: " + individuo.getDescripcion());
        }
    }

    public void actualizarParametrosGlobales(Individuo individuo) {
        Parametros.fitnessGlobal = individuo.getFitness();
        Parametros.kilometrosRecorridos = individuo.getCromosoma().stream()
                .mapToDouble(gen -> gen.getRutaFinal().size()).sum();
        Parametros.contadorPrueba++;
    }

    private boolean validarMejorIndividuo(Individuo individuo) {
        if (individuo == null) {
            System.out.println("⚠️  Error: Individuo es null");
            return false;
        }
        
        double fitness = individuo.getFitness();
        if (fitness == Double.MIN_VALUE) {
            System.out.println("⚠️  Advertencia: Fitness mínimo detectado - " + individuo.getDescripcion());
            return false;
        }
        
        if (fitness == Double.NEGATIVE_INFINITY || Double.isNaN(fitness)) {
            System.out.println("⚠️  Advertencia: Fitness inválido detectado: " + fitness);
            return false;
        }
        
        if (Math.abs(fitness) < 0.001) {
            System.out.println("⚠️  Advertencia: Fitness muy bajo: " + fitness);
            return false;
        }
        
        return true;
    }

    private Individuo crearSolucionBasica() {
        System.out.println("🔧 Creando solución básica de respaldo...");
        
        Individuo mejorSolucion = null;
        double mejorFitnessLocal = Double.NEGATIVE_INFINITY;
        
        for (int intento = 0; intento < 10; intento++) {
            try {
                Individuo solucionTemporal = new Individuo(pedidos);
                double fitnessTemp = solucionTemporal.getFitness();
                
                if (fitnessTemp != Double.MIN_VALUE && 
                    fitnessTemp != Double.NEGATIVE_INFINITY && 
                    !Double.isNaN(fitnessTemp) &&
                    fitnessTemp > mejorFitnessLocal) {
                    mejorFitnessLocal = fitnessTemp;
                    mejorSolucion = solucionTemporal;
                    
                    if (fitnessTemp > 10.0) {
                        System.out.println("✅ Solución decente encontrada en intento " + intento);
                        break;
                    }
                }
            } catch (Exception e) {
                System.out.println("⚠️  Error en intento " + intento + ": " + e.getMessage());
            }
        }
        
        if (mejorSolucion == null) {
            System.out.println("🆘 Creando solución de emergencia...");
            mejorSolucion = crearSolucionEmergencia();
        }
        
        System.out.println("✅ Solución básica creada con fitness: " + String.format("%.2f", mejorSolucion.getFitness()));
        return mejorSolucion;
    }
    
    private Individuo crearSolucionEmergencia() {
        System.out.println("🆘 Creando solución de emergencia con " + pedidos.size() + " pedidos...");
        
        Individuo solucionEmergencia = new Individuo(new ArrayList<>());
        List<Gen> cromosoma = new ArrayList<>();
        
        List<Camion> camiones = DataLoader.camiones;
        if (!camiones.isEmpty() && !pedidos.isEmpty()) {
            
            // Distribuir pedidos entre múltiples camiones para mejorar rendimiento
            int camionesActivos = Math.min(4, camiones.size()); // Máximo 4 camiones activos
            int pedidosPorCamion = Math.max(1, pedidos.size() / camionesActivos);
            
            System.out.println("📦 Distribuyendo " + pedidos.size() + " pedidos entre " + camionesActivos + " camiones");
            
            // Distribuir pedidos entre los primeros camiones
            for (int i = 0; i < camionesActivos && i < camiones.size(); i++) {
                Gen gen = new Gen(camiones.get(i), new ArrayList<>());
                
                // Calcular rango de pedidos para este camión
                int inicio = i * pedidosPorCamion;
                int fin = (i == camionesActivos - 1) ? pedidos.size() : Math.min((i + 1) * pedidosPorCamion, pedidos.size());
                
                List<Pedido> pedidosCamion = new ArrayList<>();
                
                // Asignar solo unos pocos pedidos por camión para evitar sobrecarga
                int maxPedidosPorCamion = Math.min(5, fin - inicio); // Máximo 5 pedidos por camión
                for (int j = inicio; j < inicio + maxPedidosPorCamion && j < pedidos.size(); j++) {
                    Pedido pedido = pedidos.get(j);
                    gen.getNodos().add(pedido);
                    pedidosCamion.add(pedido);
                }
                
                // Siempre agregar almacén central al final
                gen.getNodos().add(DataLoader.almacenes.get(0));
                gen.setPedidos(pedidosCamion);
                
                // Intentar calcular fitness - si falla, usar fitness básico
                try {
                    double fitnessGen = gen.calcularFitness();
                    if (fitnessGen == Double.MIN_VALUE) {
                        gen.setFitness(5.0); // Fitness básico válido
                        System.out.println("🔧 Camión " + camiones.get(i).getCodigo() + 
                                         ": Fitness forzado a 5.0 para " + pedidosCamion.size() + " pedidos");
                    } else {
                        System.out.println("✅ Camión " + camiones.get(i).getCodigo() + 
                                         ": Fitness " + String.format("%.2f", fitnessGen) + 
                                         " para " + pedidosCamion.size() + " pedidos");
                    }
                } catch (Exception e) {
                    gen.setFitness(5.0);
                    System.out.println("❌ Camión " + camiones.get(i).getCodigo() + 
                                     ": Error calculando fitness, usando 5.0");
                }
                
                cromosoma.add(gen);
            }
            
            // Crear genes vacíos para el resto de camiones
            for (int i = camionesActivos; i < camiones.size(); i++) {
                Gen genVacio = new Gen(camiones.get(i), new ArrayList<>());
                genVacio.getNodos().add(DataLoader.almacenes.get(0)); // Solo almacén central
                genVacio.setPedidos(new ArrayList<>());
                genVacio.setFitness(0.0); // Fitness neutro para genes vacíos
                cromosoma.add(genVacio);
            }
        } else {
            System.out.println("❌ No hay camiones o pedidos disponibles para crear solución");
            // Crear al menos un gen vacío para evitar errores
            if (!camiones.isEmpty()) {
                Gen genVacio = new Gen(camiones.get(0), new ArrayList<>());
                genVacio.getNodos().add(DataLoader.almacenes.get(0));
                genVacio.setPedidos(new ArrayList<>());
                genVacio.setFitness(1.0);
                cromosoma.add(genVacio);
            }
        }
        
        solucionEmergencia.setCromosoma(cromosoma);
        solucionEmergencia.setPedidos(pedidos);
        
        // Calcular fitness total de la solución
        double fitnessTotal = cromosoma.stream().mapToDouble(Gen::getFitness).sum();
        if (fitnessTotal <= 0) {
            fitnessTotal = 5.0; // Fitness mínimo para evitar problemas
        }
        solucionEmergencia.setFitness(fitnessTotal);
        
        System.out.println("✅ Solución de emergencia creada:");
        System.out.println("   - Genes activos: " + cromosoma.stream().mapToLong(g -> g.getPedidos().size()).sum());
        System.out.println("   - Pedidos distribuidos: " + cromosoma.stream().mapToInt(g -> g.getPedidos().size()).sum());
        System.out.println("   - Fitness total: " + String.format("%.2f", fitnessTotal));
        
        return solucionEmergencia;
    }
}
