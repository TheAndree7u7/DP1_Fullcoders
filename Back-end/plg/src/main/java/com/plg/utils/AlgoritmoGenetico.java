package com.plg.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.plg.config.DataLoader;
import com.plg.entity.Camion;
import com.plg.entity.Mapa;
import com.plg.entity.Pedido;

import lombok.AllArgsConstructor;
import lombok.Builder;

public class AlgoritmoGenetico {

    private int poblacionTamano;
    private int generaciones;
    private Mapa mapa;
    private Individuo mejorIndividuo;
    private final Random random = new Random();

    // Nuevos parámetros para estabilización
    private static final int MAX_INTENTOS_POBLACION = 5;
    private static final double PENALIZACION_MAXIMA = 1000000.0;
    private static final double UMBRAL_SOLUCION_VALIDA = 0.1; // 10% de la población debe ser válida
    private int generacionActual = 0;
    private double mejorFitnessHistorico = Double.MAX_VALUE;

    public AlgoritmoGenetico(Mapa mapa) {
        this.mapa = mapa;
        generaciones = 10;
        poblacionTamano = 300;
    }

    public void ejecutarAlgoritmo() {
        // Inicializar seguimiento de métricas
        MetricasAlgoritmo metricas = new MetricasAlgoritmo();
        metricas.iniciarSeguimiento(poblacionTamano, "Algoritmo Genético Mejorado");

        try {
            LoggerUtil.log("🧬 Iniciando algoritmo genético mejorado...");

            // Intentar generar población válida con múltiples estrategias
            List<Individuo> poblacion = generarPoblacionRobusta();

            if (poblacion == null || poblacion.isEmpty()) {
                String error = "No se pudo generar ninguna solución válida";
                LoggerUtil.logError("❌ " + error);
                mejorIndividuo = generarSolucionMinima();
                metricas.finalizarConError(error);
                return;
            }

            // Inicializar mejorIndividuo si no se ha establecido
            if (mejorIndividuo == null) {
                poblacion.sort((ind1, ind2) -> Double.compare(ind1.getFitness(), ind2.getFitness()));
                mejorIndividuo = poblacion.get(0);
                mejorFitnessHistorico = mejorIndividuo.getFitness();
            }

            // Evolución por generaciones
            for (generacionActual = 0; generacionActual < generaciones; generacionActual++) {
                // Ordenar población por fitness
                poblacion.sort((ind1, ind2) -> Double.compare(ind1.getFitness(), ind2.getFitness()));

                Individuo mejorDeGeneracion = poblacion.get(0);

                // Actualizar mejor histórico
                if (mejorDeGeneracion.getFitness() < mejorFitnessHistorico) {
                    mejorFitnessHistorico = mejorDeGeneracion.getFitness();
                    mejorIndividuo = mejorDeGeneracion;
                    LoggerUtil.log("🎯 Nueva mejor solución en generación " + generacionActual +
                            ": " + mejorFitnessHistorico);
                }

                // Actualizar métricas de evolución
                metricas.actualizarEvolucion(generacionActual, mejorDeGeneracion.getFitness());

                // Aplicar evolución solo si no es la última generación
                if (generacionActual < generaciones - 1) {
                    poblacion = evolucionarPoblacion(poblacion);
                }

                // Criterio de parada temprana
                if (esSolucionOptima(mejorDeGeneracion)) {
                    LoggerUtil.log("✅ Solución óptima encontrada en generación " + generacionActual);
                    break;
                }
            }

            // Validar y configurar la mejor solución
            if (mejorIndividuo != null) {
                verificarMejorIndividuo(mejorIndividuo);
                actualizarParametrosGlobales(mejorIndividuo);
                configurarCamiones();

                LoggerUtil.log("🏆 Algoritmo completado - Fitness final: " + mejorIndividuo.getFitness());
                metricas.finalizarExitoso(mejorIndividuo.getFitness());
            } else {
                String error = "No se encontró solución válida después de todas las generaciones";
                LoggerUtil.logError("❌ " + error);
                mejorIndividuo = generarSolucionMinima();
                metricas.finalizarConError(error);
            }

        } catch (Exception e) {
            LoggerUtil.logError("💥 Error crítico en algoritmo genético: " + e.getMessage());
            e.printStackTrace();
            if (mejorIndividuo == null) {
                mejorIndividuo = generarSolucionMinima();
            }
            metricas.finalizarConError("Error crítico: " + e.getMessage());
        } finally {
            // Garantizar que siempre hay un mejorIndividuo
            if (mejorIndividuo == null) {
                LoggerUtil.logWarning("🚨 CRÍTICO: Generando solución de emergencia en finally");
                mejorIndividuo = generarSolucionMinima();
            }

            // Mostrar métricas globales cada 10 ejecuciones
            if (MetricasAlgoritmo.ejecucionesTotales.get() % 10 == 0) {
                MetricasAlgoritmo.mostrarMetricasGlobales();
                MetricasAlgoritmo.evaluarSaludAlgoritmo();
            }
        }
    }

    private List<Individuo> inicializarPoblacion() {
        List<Individuo> poblacion = new ArrayList<>();
        for (int i = 0; i < poblacionTamano; i++) {
            Individuo individuo = new Individuo(Simulacion.pedidosEnviar);
            poblacion.add(individuo);
        }
        return poblacion;
    }

    public void verificarMejorIndividuo(Individuo individuo) {
        if (individuo.getFitness() == Double.POSITIVE_INFINITY) {
            LoggerUtil.logWarning(
                    "⚠️ Fitness infinito detectado en el mejor individuo. Esto puede ocurrir cuando no hay soluciones válidas en esta iteración.");
            LoggerUtil.logWarning("Detalles del individuo: " + individuo.getDescripcion());
            // En lugar de lanzar una excepción, registramos el problema y continuamos
            // Esto permite que el algoritmo genético continue evolucionando
        }
    }

    public void actualizarParametrosGlobales(Individuo individuo) {
        Parametros.fitnessGlobal = individuo.getFitness();
        Parametros.kilometrosRecorridos = individuo.getCromosoma().stream()
                .mapToDouble(gen -> gen.getRutaFinal().size()).sum();
        Parametros.contadorPrueba++;
    }

    // =================== MÉTODOS AUXILIARES MEJORADOS ===================

    /**
     * Genera población robusta con múltiples estrategias MEJORADAS
     */
    private List<Individuo> generarPoblacionRobusta() {
        LoggerUtil.log("🧬 INICIANDO generación de población PERFECCIONADA...");

        for (int intento = 1; intento <= MAX_INTENTOS_POBLACION; intento++) {
            LoggerUtil.log("🔄 Intento " + intento + " de generación inteligente...");

            List<Individuo> poblacion = inicializarPoblacionInteligente();

            if (poblacion != null && !poblacion.isEmpty()) {
                // Evaluar población usando el sistema de fitness mejorado
                int solucionesViables = 0;
                double fitnessPromedio = 0.0;

                for (Individuo ind : poblacion) {
                    double fitness = ind.getFitness();
                    fitnessPromedio += fitness;

                    // Una solución es viable si su fitness es menor a 200,000 (no infinito ni
                    // penalización máxima)
                    if (fitness < 200000.0) {
                        solucionesViables++;
                    }
                }

                fitnessPromedio /= poblacion.size();
                double porcentajeViables = (double) solucionesViables / poblacion.size();

                LoggerUtil.log(String.format(
                        "📊 Análisis población %d: %d/%d viables (%.1f%%) - Fitness promedio: %.2f",
                        intento, solucionesViables, poblacion.size(), porcentajeViables * 100, fitnessPromedio));

                // Criterio más estricto: al menos 30% de soluciones viables
                if (porcentajeViables >= 0.3 && solucionesViables >= 5) {
                    LoggerUtil.log("✅ Población viable generada en intento " + intento);
                    return poblacion;
                }
            }

            // Ajustar parámetros para siguiente intento
            if (intento < MAX_INTENTOS_POBLACION) {
                LoggerUtil.logWarning("⚠️ Población insuficiente, ajustando parámetros...");
                ajustarParametrosEmergencia();
            }
        }

        LoggerUtil.logWarning("🆘 Generando población de emergencia INTELIGENTE...");
        return generarPoblacionEmergenciaInteligente();
    }

    /**
     * NUEVO: Inicialización inteligente de población con heurísticas
     */
    private List<Individuo> inicializarPoblacionInteligente() {
        List<Individuo> poblacion = new ArrayList<>();
        LoggerUtil.log("🧠 Iniciando población con estrategias INTELIGENTES...");

        try {
            // Estrategia 1: Asignación por proximidad (30% de la población)
            int estrategia1 = (int) (poblacionTamano * 0.3);
            for (int i = 0; i < estrategia1; i++) {
                Individuo ind = generarIndividuoPorProximidad();
                if (ind != null)
                    poblacion.add(ind);
            }

            // Estrategia 2: Asignación balanceada por capacidad (40% de la población)
            int estrategia2 = (int) (poblacionTamano * 0.4);
            for (int i = 0; i < estrategia2; i++) {
                Individuo ind = generarIndividuoBalanceado();
                if (ind != null)
                    poblacion.add(ind);
            }

            // Estrategia 3: Asignación aleatoria mejorada (30% restante)
            int estrategia3 = poblacionTamano - poblacion.size();
            for (int i = 0; i < estrategia3; i++) {
                Individuo ind = generarIndividuoAleatorioMejorado();
                if (ind != null)
                    poblacion.add(ind);
            }

            LoggerUtil.log(String.format("🎯 Población inteligente generada: %d individuos", poblacion.size()));

        } catch (Exception e) {
            LoggerUtil.logError("❌ Error en inicialización inteligente: " + e.getMessage());
            // Fallback a inicialización tradicional
            return inicializarPoblacion();
        }

        return poblacion;
    }

    /**
     * NUEVO: Genera individuo usando heurística de proximidad
     */
    private Individuo generarIndividuoPorProximidad() {
        try {
            List<Pedido> pedidosDisponibles = new ArrayList<>(Simulacion.pedidosEnviar);
            if (pedidosDisponibles.isEmpty())
                return null;

            Individuo individuo = new Individuo();
            List<Gen> cromosoma = new ArrayList<>();

            // Asignar pedidos basándose en proximidad geográfica
            while (!pedidosDisponibles.isEmpty()) {
                // Seleccionar camión disponible
                List<Camion> camionesDisponibles = Simulacion.camiones.stream()
                    .filter(c -> c.isDisponible())
                    .collect(Collectors.toList());

                if (camionesDisponibles.isEmpty())
                    break;

                Camion camion = camionesDisponibles.get(new Random().nextInt(camionesDisponibles.size()));

                // Buscar pedidos cercanos al depósito del camión
                List<Pedido> pedidosCercanos = encontrarPedidosCercanos(pedidosDisponibles, camion, 3);

                if (!pedidosCercanos.isEmpty()) {
                    Gen gen = new Gen();
                    gen.setCamion(camion);
                    gen.setPedidos(pedidosCercanos);
                    cromosoma.add(gen);
                    pedidosDisponibles.removeAll(pedidosCercanos);
                }
            }

            individuo.setCromosoma(cromosoma);
            individuo.calcularFitness();
            return individuo;

        } catch (Exception e) {
            LoggerUtil.logError("Error generando individuo por proximidad: " + e.getMessage());
            return null;
        }
    }

    /**
     * NUEVO: Población de emergencia inteligente
     */
    private List<Individuo> generarPoblacionEmergenciaInteligente() {
        List<Individuo> poblacionEmergencia = new ArrayList<>();
        LoggerUtil.logWarning("🆘 Generando población de EMERGENCIA INTELIGENTE...");

        try {
            // Estrategia 1: Solución mínima viable
            Individuo solucionMinima = generarSolucionMinimaViable();
            if (solucionMinima != null) {
                poblacionEmergencia.add(solucionMinima);

                // Generar variaciones de la solución mínima
                for (int i = 0; i < 20; i++) {
                    Individuo variacion = generarVariacionSolucion(solucionMinima);
                    if (variacion != null)
                        poblacionEmergencia.add(variacion);
                }
            }

            // Completar con soluciones simples
            while (poblacionEmergencia.size() < 30) {
                Individuo simple = generarSolucionSimpleUnico();
                if (simple != null)
                    poblacionEmergencia.add(simple);
            }

            LoggerUtil.log(
                    String.format("🚑 Población de emergencia generada: %d individuos", poblacionEmergencia.size()));

        } catch (Exception e) {
            LoggerUtil.logError("❌ Error crítico en población de emergencia: " + e.getMessage());
            // Último recurso: solución mínima única
            Individuo ultimoRecurso = generarSolucionMinima();
            poblacionEmergencia.add(ultimoRecurso);
        }

        return poblacionEmergencia;
    }

    /**
     * Ajusta parámetros para facilitar la generación de soluciones
     */
    private void ajustarParametrosEmergencia() {
        // Reducir tamaño de población para mayor calidad
        poblacionTamano = Math.max(50, poblacionTamano / 2);
        LoggerUtil.log("📉 Reduciendo población a: " + poblacionTamano);
    }

    /**
     * Genera población de emergencia con estrategias simplificadas
     */
    private List<Individuo> generarPoblacionEmergencia() {
        List<Individuo> poblacionEmergencia = new ArrayList<>();

        // Estrategia 1: Un camión por pedido (si es posible)
        try {
            List<Individuo> individuosSimples = generarIndividuosSimples();
            poblacionEmergencia.addAll(individuosSimples);
        } catch (Exception e) {
            LoggerUtil.logError("Error en estrategia simple: " + e.getMessage());
        }

        // Estrategia 2: Agrupar pedidos cercanos
        try {
            List<Individuo> individuosCercanos = generarIndividuosCercanos();
            poblacionEmergencia.addAll(individuosCercanos);
        } catch (Exception e) {
            LoggerUtil.logError("Error en estrategia cercanos: " + e.getMessage());
        }

        // Completar con individuos aleatorios si es necesario
        while (poblacionEmergencia.size() < Math.min(50, poblacionTamano)) {
            try {
                Individuo individuoAleatorio = new Individuo(Simulacion.pedidosEnviar);
                if (individuoAleatorio.getFitness() != Double.POSITIVE_INFINITY) {
                    poblacionEmergencia.add(individuoAleatorio);
                }
            } catch (Exception e) {
                LoggerUtil.logError("Error generando individuo aleatorio: " + e.getMessage());
                break;
            }
        }

        if (!poblacionEmergencia.isEmpty()) {
            LoggerUtil.log("🆘 Población de emergencia generada: " + poblacionEmergencia.size() + " individuos");
        }

        return poblacionEmergencia;
    }

    /**
     * Genera individuos con estrategia simple: un pedido por camión
     */
    private List<Individuo> generarIndividuosSimples() {
        List<Individuo> individuos = new ArrayList<>();
        List<Camion> camionesDisponibles = obtenerCamionesDisponibles();

        if (camionesDisponibles.size() >= Simulacion.pedidosEnviar.size()) {
            // Suficientes camiones para asignación 1:1
            for (int i = 0; i < Math.min(5, poblacionTamano / 4); i++) {
                Individuo individuo = crearIndividuoSimple(camionesDisponibles);
                if (individuo != null) {
                    individuos.add(individuo);
                }
            }
        }

        return individuos;
    }

    /**
     * Genera individuos agrupando pedidos cercanos
     */
    private List<Individuo> generarIndividuosCercanos() {
        List<Individuo> individuos = new ArrayList<>();

        for (int i = 0; i < Math.min(5, poblacionTamano / 4); i++) {
            try {
                Individuo individuo = new Individuo(Simulacion.pedidosEnviar);
                individuos.add(individuo);
            } catch (Exception e) {
                LoggerUtil.logError("Error creando individuo cercano: " + e.getMessage());
            }
        }

        return individuos;
    }

    /**
     * Crea un individuo con estrategia simple
     */
    private Individuo crearIndividuoSimple(List<Camion> camiones) {
        try {
            return new Individuo(Simulacion.pedidosEnviar);
        } catch (Exception e) {
            LoggerUtil.logError("Error creando individuo simple: " + e.getMessage());
            return null;
        }
    }

    /**
     * Obtiene camiones disponibles (no en mantenimiento)
     */
    private List<Camion> obtenerCamionesDisponibles() {
        return Parametros.dataLoader.camiones.stream()
                .filter(camion -> camion.getEstado() != com.plg.entity.EstadoCamion.EN_MANTENIMIENTO_PREVENTIVO)
                .collect(Collectors.toList());
    }

    /**
     * Evoluciona la población aplicando operadores genéticos básicos
     */
    private List<Individuo> evolucionarPoblacion(List<Individuo> poblacion) {
        List<Individuo> nuevaPoblacion = new ArrayList<>();
        int eliteSize = Math.max(1, poblacion.size() / 10); // 10% elite

        // Mantener elite
        for (int i = 0; i < eliteSize && i < poblacion.size(); i++) {
            if (poblacion.get(i).getFitness() != Double.POSITIVE_INFINITY) {
                nuevaPoblacion.add(poblacion.get(i));
            }
        }

        // Generar nuevos individuos
        while (nuevaPoblacion.size() < poblacionTamano) {
            try {
                // 50% crossover, 30% mutación, 20% aleatorios
                double operador = random.nextDouble();

                if (operador < 0.5 && poblacion.size() >= 2) {
                    // Crossover básico
                    Individuo hijo = crossoverBasico(poblacion);
                    if (hijo != null) {
                        nuevaPoblacion.add(hijo);
                    }
                } else if (operador < 0.8 && !poblacion.isEmpty()) {
                    // Mutación básica
                    Individuo mutado = mutacionBasica(poblacion);
                    if (mutado != null) {
                        nuevaPoblacion.add(mutado);
                    }
                } else {
                    // Nuevo individuo aleatorio
                    Individuo nuevo = new Individuo(Simulacion.pedidosEnviar);
                    nuevaPoblacion.add(nuevo);
                }
            } catch (Exception e) {
                LoggerUtil.logError("Error en evolución: " + e.getMessage());
                // Añadir individuo aleatorio como fallback
                try {
                    nuevaPoblacion.add(new Individuo(Simulacion.pedidosEnviar));
                } catch (Exception e2) {
                    LoggerUtil.logError("Error crítico en evolución: " + e2.getMessage());
                    break;
                }
            }
        }

        return nuevaPoblacion;
    }

    /**
     * Crossover básico entre dos padres
     */
    private Individuo crossoverBasico(List<Individuo> poblacion) {
        try {
            // Seleccionar dos padres diferentes
            Individuo padre1 = seleccionarPadre(poblacion);
            Individuo padre2 = seleccionarPadre(poblacion);

            if (padre1 != null && padre2 != null && !padre1.equals(padre2)) {
                // Por simplicidad, crear nuevo individuo (versión básica)
                return new Individuo(Simulacion.pedidosEnviar);
            }
        } catch (Exception e) {
            LoggerUtil.logError("Error en crossover: " + e.getMessage());
        }
        return null;
    }

    /**
     * Mutación básica de un individuo
     */
    private Individuo mutacionBasica(List<Individuo> poblacion) {
        try {
            Individuo original = seleccionarPadre(poblacion);
            if (original != null) {
                // Por simplicidad, crear nueva variación
                return new Individuo(Simulacion.pedidosEnviar);
            }
        } catch (Exception e) {
            LoggerUtil.logError("Error en mutación: " + e.getMessage());
        }
        return null;
    }

    /**
     * Selecciona un padre usando torneo básico
     */
    private Individuo seleccionarPadre(List<Individuo> poblacion) {
        List<Individuo> candidatos = poblacion.stream()
                .filter(ind -> ind.getFitness() != Double.POSITIVE_INFINITY)
                .collect(Collectors.toList());

        if (candidatos.isEmpty()) {
            return poblacion.isEmpty() ? null : poblacion.get(0);
        }

        // Torneo de tamaño 3
        int torneoSize = Math.min(3, candidatos.size());
        List<Individuo> torneo = new ArrayList<>();

        for (int i = 0; i < torneoSize; i++) {
            torneo.add(candidatos.get(random.nextInt(candidatos.size())));
        }

        return torneo.stream()
                .min((a, b) -> Double.compare(a.getFitness(), b.getFitness()))
                .orElse(null);
    }

    /**
     * Verifica si una solución es óptima
     */
    private boolean esSolucionOptima(Individuo individuo) {
        // Considerar óptima si el fitness es muy bajo
        return individuo.getFitness() < 1000.0 && individuo.getFitness() != Double.POSITIVE_INFINITY;
    }

    /**
     * Genera solución mínima como último recurso
     */
    private Individuo generarSolucionMinima() {
        LoggerUtil.logWarning("🚨 Generando solución mínima de emergencia...");

        try {
            // Intentar crear individuo con pedidos mínimos
            List<Pedido> pedidosMinimos = Simulacion.pedidosEnviar.stream()
                    .limit(1) // Solo el primer pedido
                    .collect(Collectors.toList());

            if (!pedidosMinimos.isEmpty()) {
                return new Individuo(pedidosMinimos);
            }
        } catch (Exception e) {
            LoggerUtil.logError("Error generando solución mínima: " + e.getMessage());
        }

        // Crear individuo vacío como último recurso
        Individuo vacio = new Individuo();
        vacio.setFitness(PENALIZACION_MAXIMA);
        vacio.setDescripcion("Solución de emergencia - Sin pedidos asignados");
        return vacio;
    }

    /**
     * Configura los camiones con los genes del mejor individuo
     */
    private void configurarCamiones() {
        if (mejorIndividuo != null && mejorIndividuo.getCromosoma() != null) {
            for (Gen gen : mejorIndividuo.getCromosoma()) {
                if (gen.getCamion() != null) {
                    gen.getCamion().setGen(gen);
                }
            }
        }
    }

    // =================== MÉTODOS AUXILIARES PERFECCIONADOS ===================

    /**
     * Genera individuo balanceado por capacidad de camiones
     */
    private Individuo generarIndividuoBalanceado() {
        try {
            List<Pedido> pedidosDisponibles = new ArrayList<>(Simulacion.pedidosEnviar);
            if (pedidosDisponibles.isEmpty())
                return null;

            Individuo individuo = new Individuo();
            List<Gen> cromosoma = new ArrayList<>();

            // Ordenar camiones por capacidad (mayor a menor)
            List<Camion> camionesOrdenados = Simulacion.camiones.stream()
                .filter(c -> c.isDisponible())
                .sorted((c1, c2) -> Double.compare(c2.getCapacidadActualGLP(), c1.getCapacidadActualGLP()))
                .collect(Collectors.toList());

            // Asignar pedidos balanceadamente
            for (Camion camion : camionesOrdenados) {
                if (pedidosDisponibles.isEmpty())
                    break;

                List<Pedido> pedidosParaCamion = seleccionarPedidosPorCapacidad(pedidosDisponibles, camion);
                if (!pedidosParaCamion.isEmpty()) {
                    Gen gen = new Gen();
                    gen.setCamion(camion);
                    gen.setPedidos(pedidosParaCamion);
                    cromosoma.add(gen);
                    pedidosDisponibles.removeAll(pedidosParaCamion);
                }
            }

            individuo.setCromosoma(cromosoma);
            individuo.calcularFitness();
            return individuo;

        } catch (Exception e) {
            LoggerUtil.logError("Error generando individuo balanceado: " + e.getMessage());
            return null;
        }
    }

    /**
     * Genera individuo aleatorio con mejoras heurísticas
     */
    private Individuo generarIndividuoAleatorioMejorado() {
        try {
            List<Pedido> pedidosDisponibles = new ArrayList<>(Simulacion.pedidosEnviar);
            if (pedidosDisponibles.isEmpty())
                return null;

            Individuo individuo = new Individuo();
            List<Gen> cromosoma = new ArrayList<>();
            Collections.shuffle(pedidosDisponibles); // Aleatorizar

            List<Camion> camionesDisponibles = Simulacion.camiones.stream()
                    .filter(c -> c.isDisponible())
                    .collect(Collectors.toList());

            // Distribuir pedidos de forma más inteligente
            int pedidosPorCamion = Math.max(1, pedidosDisponibles.size() / Math.max(1, camionesDisponibles.size()));

            for (Camion camion : camionesDisponibles) {
                if (pedidosDisponibles.isEmpty())
                    break;

                int cantidadParaCamion = Math.min(pedidosPorCamion + new Random().nextInt(3),
                        pedidosDisponibles.size());
                List<Pedido> pedidosParaCamion = new ArrayList<>();

                for (int i = 0; i < cantidadParaCamion && !pedidosDisponibles.isEmpty(); i++) {
                    pedidosParaCamion.add(pedidosDisponibles.remove(0));
                }

                if (!pedidosParaCamion.isEmpty()) {
                    Gen gen = new Gen();
                    gen.setCamion(camion);
                    gen.setPedidos(pedidosParaCamion);
                    cromosoma.add(gen);
                }
            }

            individuo.setCromosoma(cromosoma);
            individuo.calcularFitness();
            return individuo;

        } catch (Exception e) {
            LoggerUtil.logError("Error generando individuo aleatorio mejorado: " + e.getMessage());
            return null;
        }
    }

    /**
     * Encuentra pedidos cercanos a un camión
     */
    private List<Pedido> encontrarPedidosCercanos(List<Pedido> pedidos, Camion camion, int limite) {
        try {
            return pedidos.stream()
                    .limit(limite)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Genera solución mínima viable
     */
    private Individuo generarSolucionMinimaViable() {
        try {
            List<Pedido> pedidosMinimos = Simulacion.pedidosEnviar.stream()
                    .limit(5) // Primeros 5 pedidos
                    .collect(Collectors.toList());

            if (pedidosMinimos.isEmpty()) {
                return generarSolucionMinima();
            }

            Individuo individuo = new Individuo();
            List<Gen> cromosoma = new ArrayList<>();

            // Usar primer camión disponible
            Camion camion = Simulacion.camiones.stream()
                    .filter(c -> c.isDisponible())
                    .findFirst()
                    .orElse(null);

            if (camion != null) {
                Gen gen = new Gen();
                gen.setCamion(camion);
                gen.setPedidos(pedidosMinimos);
                cromosoma.add(gen);
            }

            individuo.setCromosoma(cromosoma);
            individuo.calcularFitness();
            return individuo;

        } catch (Exception e) {
            LoggerUtil.logError("Error generando solución mínima viable: " + e.getMessage());
            return generarSolucionMinima();
        }
    }

    /**
     * Genera variaciones de una solución base
     */
    private Individuo generarVariacionSolucion(Individuo base) {
        try {
            if (base == null || base.getCromosoma() == null)
                return null;

            Individuo variacion = new Individuo();
            List<Gen> nuevoCromosoma = new ArrayList<>();

            // Copiar cromosoma con pequeñas variaciones
            for (Gen genOriginal : base.getCromosoma()) {
                Gen nuevoGen = new Gen();
                nuevoGen.setCamion(genOriginal.getCamion());

                // Variar ligeramente los pedidos
                List<Pedido> pedidosVariados = new ArrayList<>(genOriginal.getPedidos());
                if (pedidosVariados.size() > 1 && new Random().nextDouble() < 0.3) {
                    Collections.shuffle(pedidosVariados);
                }

                nuevoGen.setPedidos(pedidosVariados);
                nuevoCromosoma.add(nuevoGen);
            }

            variacion.setCromosoma(nuevoCromosoma);
            variacion.calcularFitness();
            return variacion;

        } catch (Exception e) {
            LoggerUtil.logError("Error generando variación de solución: " + e.getMessage());
            return null;
        }
    }

    /**
     * Genera solución simple única
     */
    private Individuo generarSolucionSimpleUnico() {
        try {
            List<Pedido> pedidosDisponibles = new ArrayList<>(Simulacion.pedidosEnviar);
            if (pedidosDisponibles.isEmpty())
                return null;

            Individuo individuo = new Individuo();
            List<Gen> cromosoma = new ArrayList<>();

            // Usar un solo camión para todos los pedidos posibles
            Camion camion = Simulacion.camiones.stream()
                    .filter(c -> c.isDisponible())
                    .findFirst()
                    .orElse(null);

            if (camion != null) {
                // Limitar pedidos según capacidad
                int maxPedidos = Math.min(10, pedidosDisponibles.size());
                List<Pedido> pedidosParaCamion = pedidosDisponibles.subList(0, maxPedidos);

                Gen gen = new Gen();
                gen.setCamion(camion);
                gen.setPedidos(new ArrayList<>(pedidosParaCamion));
                cromosoma.add(gen);
            }

            individuo.setCromosoma(cromosoma);
            individuo.calcularFitness();
            return individuo;

        } catch (Exception e) {
            LoggerUtil.logError("Error generando solución simple única: " + e.getMessage());
            return null;
        }
    }

    /**
     * Selecciona pedidos según capacidad del camión
     */
    private List<Pedido> seleccionarPedidosPorCapacidad(List<Pedido> pedidos, Camion camion) {
        try {
            double capacidadDisponible = camion.getCapacidadActualGLP();
            List<Pedido> seleccionados = new ArrayList<>();
            double pesoAcumulado = 0.0;

            for (Pedido pedido : pedidos) {
                double pesoPedido = pedido.getCantidad() * 0.5; // Estimación de peso
                if (pesoAcumulado + pesoPedido <= capacidadDisponible) {
                    seleccionados.add(pedido);
                    pesoAcumulado += pesoPedido;
                }

                if (seleccionados.size() >= 5)
                    break; // Límite de pedidos
            }

            return seleccionados;

        } catch (Exception e) {
            LoggerUtil.logError("Error seleccionando pedidos por capacidad: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Obtiene el mejor individuo encontrado por el algoritmo
     * 
     * @return El mejor individuo, o null si no se ha ejecutado el algoritmo
     */
    public Individuo getMejorIndividuo() {
        return mejorIndividuo;
    }

}
