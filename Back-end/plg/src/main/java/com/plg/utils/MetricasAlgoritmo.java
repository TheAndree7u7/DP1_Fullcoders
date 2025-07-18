package com.plg.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Clase para recopilar y mostrar métricas del algoritmo genético
 * Ayuda a monitorear el rendimiento y detectar problemas
 */
public class MetricasAlgoritmo {
    
    // Contadores globales
    public static final AtomicInteger ejecucionesTotales = new AtomicInteger(0);
    private static final AtomicInteger ejecucionesExitosas = new AtomicInteger(0);
    private static final AtomicInteger ejecucionesConError = new AtomicInteger(0);
    private static final AtomicLong tiempoTotalMs = new AtomicLong(0);
    
    // Historial de fitness
    private static final List<Double> historialFitness = new ArrayList<>();
    private static final List<String> historialErrores = new ArrayList<>();
    
    // Métricas de la ejecución actual
    private long tiempoInicio;
    private long tiempoFin;
    private double mejorFitness = Double.MAX_VALUE;
    private int generacionesEjecutadas = 0;
    private int poblacionTamano = 0;
    private String estrategiaUsada = "";
    private boolean exitosa = false;
    
    /**
     * Inicia el seguimiento de una nueva ejecución
     */
    public void iniciarSeguimiento(int poblacion, String estrategia) {
        this.tiempoInicio = System.currentTimeMillis();
        this.poblacionTamano = poblacion;
        this.estrategiaUsada = estrategia;
        this.exitosa = false;
        
        ejecucionesTotales.incrementAndGet();
        
        LoggerUtil.log("📊 INICIANDO SEGUIMIENTO - " + 
                      "Ejecución #" + ejecucionesTotales.get() + 
                      " | Población: " + poblacion + 
                      " | Estrategia: " + estrategia);
    }
    
    /**
     * Actualiza métricas durante la evolución
     */
    public void actualizarEvolucion(int generacion, double fitness) {
        this.generacionesEjecutadas = generacion + 1;
        
        if (fitness < mejorFitness) {
            mejorFitness = fitness;
            LoggerUtil.log("🎯 Nuevo mejor fitness en generación " + generacion + ": " + fitness);
        }
    }
    
    /**
     * Finaliza el seguimiento marcando como exitosa
     */
    public void finalizarExitoso(double fitnessResultante) {
        finalizarSeguimiento();
        this.exitosa = true;
        ejecucionesExitosas.incrementAndGet();
        
        // Guardar en historial
        synchronized (historialFitness) {
            historialFitness.add(fitnessResultante);
            
            // Mantener solo los últimos 100 registros
            if (historialFitness.size() > 100) {
                historialFitness.remove(0);
            }
        }
        
        LoggerUtil.log("✅ EJECUCIÓN EXITOSA - Fitness final: " + fitnessResultante);
    }
    
    /**
     * Finaliza el seguimiento marcando como error
     */
    public void finalizarConError(String mensajeError) {
        finalizarSeguimiento();
        this.exitosa = false;
        ejecucionesConError.incrementAndGet();
        
        // Guardar error en historial
        synchronized (historialErrores) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            historialErrores.add(timestamp + ": " + mensajeError);
            
            // Mantener solo los últimos 50 errores
            if (historialErrores.size() > 50) {
                historialErrores.remove(0);
            }
        }
        
        LoggerUtil.logError("❌ EJECUCIÓN CON ERROR - " + mensajeError);
    }
    
    /**
     * Finaliza el seguimiento básico
     */
    private void finalizarSeguimiento() {
        this.tiempoFin = System.currentTimeMillis();
        long duracion = tiempoFin - tiempoInicio;
        tiempoTotalMs.addAndGet(duracion);
        
        mostrarResumenEjecucion(duracion);
    }
    
    /**
     * Muestra resumen de la ejecución actual
     */
    private void mostrarResumenEjecucion(long duracion) {
        LoggerUtil.log("📋 RESUMEN EJECUCIÓN:");
        LoggerUtil.log("   ⏱️  Duración: " + duracion + "ms");
        LoggerUtil.log("   🧬 Generaciones: " + generacionesEjecutadas);
        LoggerUtil.log("   👥 Población: " + poblacionTamano);
        LoggerUtil.log("   🎯 Mejor fitness: " + (mejorFitness == Double.MAX_VALUE ? "N/A" : mejorFitness));
        LoggerUtil.log("   ✅ Exitosa: " + (exitosa ? "Sí" : "No"));
    }
    
    /**
     * Muestra métricas globales del algoritmo
     */
    public static void mostrarMetricasGlobales() {
        int total = ejecucionesTotales.get();
        int exitosas = ejecucionesExitosas.get();
        int errores = ejecucionesConError.get();
        long tiempoTotal = tiempoTotalMs.get();
        
        double tasaExito = total > 0 ? (exitosas * 100.0 / total) : 0.0;
        double tiempoPromedio = total > 0 ? (tiempoTotal / (double) total) : 0.0;
        
        LoggerUtil.log("📊 MÉTRICAS GLOBALES DEL ALGORITMO:");
        LoggerUtil.log("   🔢 Ejecuciones totales: " + total);
        LoggerUtil.log("   ✅ Ejecuciones exitosas: " + exitosas + " (" + String.format("%.1f%%", tasaExito) + ")");
        LoggerUtil.log("   ❌ Ejecuciones con error: " + errores);
        LoggerUtil.log("   ⏱️  Tiempo promedio: " + String.format("%.1f", tiempoPromedio) + "ms");
        LoggerUtil.log("   ⏰ Tiempo total: " + (tiempoTotal / 1000.0) + "s");
        
        // Mostrar estadísticas de fitness si hay datos
        mostrarEstadisticasFitness();
        
        // Mostrar errores recientes si los hay
        mostrarErroresRecientes();
    }
    
    /**
     * Muestra estadísticas de fitness histórico
     */
    private static void mostrarEstadisticasFitness() {
        synchronized (historialFitness) {
            if (historialFitness.isEmpty()) {
                LoggerUtil.log("   📈 Sin datos de fitness disponibles");
                return;
            }
            
            double promedio = historialFitness.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            double minimo = historialFitness.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
            double maximo = historialFitness.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
            
            LoggerUtil.log("   📈 FITNESS HISTÓRICO:");
            LoggerUtil.log("      📊 Promedio: " + String.format("%.2f", promedio));
            LoggerUtil.log("      ⬇️  Mínimo: " + String.format("%.2f", minimo));
            LoggerUtil.log("      ⬆️  Máximo: " + String.format("%.2f", maximo));
            LoggerUtil.log("      📋 Muestras: " + historialFitness.size());
        }
    }
    
    /**
     * Muestra errores recientes
     */
    private static void mostrarErroresRecientes() {
        synchronized (historialErrores) {
            if (historialErrores.isEmpty()) {
                LoggerUtil.log("   ✅ Sin errores recientes");
                return;
            }
            
            LoggerUtil.log("   ⚠️  ERRORES RECIENTES:");
            int mostrar = Math.min(5, historialErrores.size());
            for (int i = historialErrores.size() - mostrar; i < historialErrores.size(); i++) {
                LoggerUtil.log("      " + historialErrores.get(i));
            }
        }
    }
    
    /**
     * Evalúa la salud general del algoritmo
     */
    public static void evaluarSaludAlgoritmo() {
        int total = ejecucionesTotales.get();
        int exitosas = ejecucionesExitosas.get();
        
        if (total == 0) {
            LoggerUtil.log("🔍 EVALUACIÓN: Sin ejecuciones registradas");
            return;
        }
        
        double tasaExito = (exitosas * 100.0 / total);
        
        LoggerUtil.log("🏥 EVALUACIÓN DE SALUD DEL ALGORITMO:");
        
        if (tasaExito >= 90) {
            LoggerUtil.log("   💚 EXCELENTE - Tasa de éxito: " + String.format("%.1f%%", tasaExito));
        } else if (tasaExito >= 75) {
            LoggerUtil.log("   💛 BUENO - Tasa de éxito: " + String.format("%.1f%%", tasaExito));
        } else if (tasaExito >= 50) {
            LoggerUtil.log("   🧡 REGULAR - Tasa de éxito: " + String.format("%.1f%%", tasaExito));
            LoggerUtil.logWarning("   ⚠️  Considerar revisar parámetros o estrategias");
        } else {
            LoggerUtil.log("   ❤️  CRÍTICO - Tasa de éxito: " + String.format("%.1f%%", tasaExito));
            LoggerUtil.logError("   🚨 ACCIÓN REQUERIDA - Revisar algoritmo urgentemente");
        }
        
        // Recomendaciones específicas
        darRecomendaciones(tasaExito);
    }
    
    /**
     * Da recomendaciones basadas en las métricas
     */
    private static void darRecomendaciones(double tasaExito) {
        LoggerUtil.log("💡 RECOMENDACIONES:");
        
        if (tasaExito < 75) {
            LoggerUtil.log("   - Aumentar población inicial");
            LoggerUtil.log("   - Revisar restricciones de los pedidos");
            LoggerUtil.log("   - Verificar disponibilidad de camiones");
        }
        
        if (tasaExito < 50) {
            LoggerUtil.log("   - Considerar relajar algunas restricciones");
            LoggerUtil.log("   - Implementar más estrategias de emergencia");
            LoggerUtil.log("   - Revisar datos de entrada por inconsistencias");
        }
        
        synchronized (historialFitness) {
            if (!historialFitness.isEmpty()) {
                double promedioReciente = historialFitness.stream()
                    .skip(Math.max(0, historialFitness.size() - 10))
                    .mapToDouble(Double::doubleValue)
                    .average().orElse(0.0);
                    
                if (promedioReciente > 50000) {
                    LoggerUtil.log("   - Fitness promedio alto, optimizar función objetivo");
                }
            }
        }
    }
    
    /**
     * Resetea todas las métricas
     */
    public static void resetearMetricas() {
        ejecucionesTotales.set(0);
        ejecucionesExitosas.set(0);
        ejecucionesConError.set(0);
        tiempoTotalMs.set(0);
        
        synchronized (historialFitness) {
            historialFitness.clear();
        }
        
        synchronized (historialErrores) {
            historialErrores.clear();
        }
        
        LoggerUtil.log("🔄 Métricas del algoritmo reseteadas");
    }
}
