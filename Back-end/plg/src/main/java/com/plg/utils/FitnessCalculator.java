package com.plg.utils;

import java.util.List;

import com.plg.entity.Camion;
import com.plg.entity.Pedido;

/**
 * Calculadora de fitness multinivel para algoritmo genético
 * Reemplaza el fitness infinito con penalizaciones graduales
 */
public class FitnessCalculator {

    // Pesos para diferentes componentes del fitness
    private static final double PESO_DISTANCIA = 0.4;
    private static final double PESO_TIEMPO = 0.3;
    private static final double PESO_COMBUSTIBLE = 0.2;
    private static final double PESO_EQUILIBRIO = 0.1;

    // Penalizaciones graduales
    private static final double PENALIZACION_BASE = 100000.0;
    private static final double PENALIZACION_RESTRICCION_VIOLADA = 50000.0;
    private static final double PENALIZACION_PEDIDO_NO_ENTREGADO = 25000.0;
    private static final double PENALIZACION_CAMION_SOBRECARGADO = 15000.0;

    /**
     * Calcula fitness multinivel para un individuo
     */
    public static double calcularFitnessMultinivel(Individuo individuo) {
        if (individuo == null || individuo.getCromosoma() == null) {
            return PENALIZACION_BASE;
        }

        // Verificar si es una solución completamente inválida
        if (esSolucionCompletamenteInvalida(individuo)) {
            return aplicarPenalizacionGradual(individuo);
        }

        // Calcular fitness normal si es válida
        double fitnessTotal = 0.0;

        try {
            double fitnessDistancia = calcularFitnessDistancia(individuo);
            double fitnessTiempo = calcularFitnessTiempo(individuo);
            double fitnessCombustible = calcularFitnessCombustible(individuo);
            double fitnessEquilibrio = calcularFitnessEquilibrio(individuo);

            fitnessTotal = PESO_DISTANCIA * fitnessDistancia +
                    PESO_TIEMPO * fitnessTiempo +
                    PESO_COMBUSTIBLE * fitnessCombustible +
                    PESO_EQUILIBRIO * fitnessEquilibrio;

            // Aplicar penalizaciones menores si hay violaciones parciales
            fitnessTotal += calcularPenalizacionesMenores(individuo);

        } catch (Exception e) {
            LoggerUtil.logError("Error calculando fitness: " + e.getMessage());
            return PENALIZACION_BASE * 0.8; // Penalización por error
        }

        return Math.max(0.0, fitnessTotal);
    }

    /**
     * Verifica si la solución es completamente inválida
     */
    private static boolean esSolucionCompletamenteInvalida(Individuo individuo) {
        try {
            // Verificar si hay genes válidos
            if (individuo.getCromosoma().isEmpty()) {
                return true;
            }

            // Verificar si al menos un gen puede ejecutarse
            for (Gen gen : individuo.getCromosoma()) {
                if (gen.getFitness() != Double.POSITIVE_INFINITY) {
                    return false; // Al menos un gen es válido
                }
            }

            return true; // Todos los genes son inválidos
        } catch (Exception e) {
            return true; // Error indica invalidez
        }
    }

    /**
     * Aplica penalización gradual según qué tan "mala" es la solución
     */
    private static double aplicarPenalizacionGradual(Individuo individuo) {
        double penalizacion = PENALIZACION_BASE;

        try {
            // Reducir penalización según cumplimiento parcial
            if (cumpleRestricciones80Porciento(individuo)) {
                penalizacion *= 0.8;
                LoggerUtil.log("📉 Penalización reducida - cumple 80% restricciones");
            }

            if (entregariaAlgunPedido(individuo)) {
                penalizacion *= 0.6;
                LoggerUtil.log("📉 Penalización reducida - entrega algunos pedidos");
            }

            if (usaCamionesDisponibles(individuo)) {
                penalizacion *= 0.7;
                LoggerUtil.log("📉 Penalización reducida - usa camiones disponibles");
            }

            // Nunca devolver infinito
            penalizacion = Math.min(penalizacion, PENALIZACION_BASE * 0.9);

        } catch (Exception e) {
            LoggerUtil.logError("Error aplicando penalización gradual: " + e.getMessage());
        }

        return penalizacion;
    }

    /**
     * Calcula fitness basado en distancia total
     */
    private static double calcularFitnessDistancia(Individuo individuo) {
        double distanciaTotal = 0.0;

        for (Gen gen : individuo.getCromosoma()) {
            if (gen.getRutaFinal() != null) {
                distanciaTotal += gen.getRutaFinal().size();
            }
        }

        return distanciaTotal;
    }

    /**
     * Calcula fitness basado en tiempo total estimado
     */
    private static double calcularFitnessTiempo(Individuo individuo) {
        double tiempoTotal = 0.0;

        for (Gen gen : individuo.getCromosoma()) {
            // Estimar tiempo basado en distancia y paradas
            if (gen.getRutaFinal() != null && gen.getPedidos() != null) {
                tiempoTotal += gen.getRutaFinal().size() * 0.5; // 0.5 min por km
                tiempoTotal += gen.getPedidos().size() * 15.0; // 15 min por entrega
            }
        }

        return tiempoTotal;
    }

    /**
     * Calcula fitness basado en consumo de combustible
     */
    private static double calcularFitnessCombustible(Individuo individuo) {
        double consumoTotal = 0.0;

        for (Gen gen : individuo.getCromosoma()) {
            if (gen.getCamion() != null && gen.getRutaFinal() != null) {
                // Estimar consumo basado en distancia y capacidad del camión

                consumoTotal += gen.getCamion().getCombustibleActual() / gen.getCamion().getCombustibleMaximo();
            }
        }

        return consumoTotal;
    }

    /**
     * Calcula fitness basado en equilibrio de carga
     */
    private static double calcularFitnessEquilibrio(Individuo individuo) {
        if (individuo.getCromosoma().isEmpty()) {
            return 0.0;
        }

        // Calcular desviación estándar de pedidos por camión
        double promedioPedidos = individuo.getPedidos().size() / (double) individuo.getCromosoma().size();
        double sumaCuadrados = 0.0;

        for (Gen gen : individuo.getCromosoma()) {
            double diferencia = gen.getPedidos().size() - promedioPedidos;
            sumaCuadrados += diferencia * diferencia;
        }

        return Math.sqrt(sumaCuadrados / individuo.getCromosoma().size());
    }

    /**
     * Calcula penalizaciones menores por violaciones parciales
     */
    private static double calcularPenalizacionesMenores(Individuo individuo) {
        double penalizaciones = 0.0;

        // Penalizar pedidos no completamente entregados
        for (Pedido pedido : individuo.getPedidos()) {
            if (pedido.getVolumenGLPEntregado() < pedido.getVolumenGLPAsignado()) {
                double porcentajeFaltante = 1.0 - (pedido.getVolumenGLPEntregado() / pedido.getVolumenGLPAsignado());
                penalizaciones += PENALIZACION_PEDIDO_NO_ENTREGADO * porcentajeFaltante;
            }
        }

        // Penalizar camiones sobrecargados
        for (Gen gen : individuo.getCromosoma()) {
            if (gen.getCamion() != null) {
                double cargaTotal = gen.getPedidos().stream()
                        .mapToDouble(p -> p.getVolumenGLPAsignado() - p.getVolumenGLPEntregado())
                        .sum();

                if (cargaTotal > gen.getCamion().getCapacidadMaximaGLP()) {
                    double sobrecarga = cargaTotal - gen.getCamion().getCapacidadMaximaGLP();
                    penalizaciones += PENALIZACION_CAMION_SOBRECARGADO
                            * (sobrecarga / gen.getCamion().getCapacidadMaximaGLP());
                }
            }
        }

        return penalizaciones;
    }

    /**
     * Verifica si cumple al menos 80% de las restricciones
     */
    private static boolean cumpleRestricciones80Porciento(Individuo individuo) {
        if (individuo.getCromosoma() == null)
            return false;

        int restriccionesCumplidas = 0;
        int restriccionesTotales = 0;

        for (Gen gen : individuo.getCromosoma()) {
            restriccionesTotales += 3; // distancia, capacidad, disponibilidad

            // Verificar distancia
            if (gen.getRutaFinal() != null && gen.getCamion() != null) {
                if (gen.getRutaFinal().size() <= gen.getCamion().calcularDistanciaMaxima()) {
                    restriccionesCumplidas++;
                }
            }

            // Verificar capacidad
            if (gen.getCamion() != null && gen.getPedidos() != null) {
                double carga = gen.getPedidos().stream()
                        .mapToDouble(p -> p.getVolumenGLPAsignado())
                        .sum();
                if (carga <= gen.getCamion().getCapacidadMaximaGLP()) {
                    restriccionesCumplidas++;
                }
            }

            // Verificar disponibilidad
            if (gen.getCamion() != null &&
                    gen.getCamion().getEstado() != com.plg.entity.EstadoCamion.EN_MANTENIMIENTO_PREVENTIVO) {
                restriccionesCumplidas++;
            }
        }

        return restriccionesTotales > 0 && (restriccionesCumplidas / (double) restriccionesTotales) >= 0.8;
    }

    /**
     * Verifica si la solución entregaría al menos algún pedido
     */
    private static boolean entregariaAlgunPedido(Individuo individuo) {
        if (individuo.getPedidos() == null)
            return false;

        return individuo.getPedidos().stream()
                .anyMatch(p -> p.getVolumenGLPEntregado() > 0);
    }

    /**
     * Verifica si usa camiones disponibles (no en mantenimiento)
     */
    private static boolean usaCamionesDisponibles(Individuo individuo) {
        if (individuo.getCromosoma() == null)
            return false;

        return individuo.getCromosoma().stream()
                .anyMatch(gen -> gen.getCamion() != null &&
                        gen.getCamion().getEstado() != com.plg.entity.EstadoCamion.EN_MANTENIMIENTO_PREVENTIVO);
    }
}
