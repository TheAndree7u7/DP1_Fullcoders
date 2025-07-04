package com.plg.service;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Test que demuestra las diferentes estrategias de división de pedidos
 */
public class EstrategiasDivisionTest {

    @Test
    void demostrarEstrategias() {
        // Capacidades disponibles: TB(150) x6, TC(200) x4, TD(250) x10
        List<Double> capacidades = Arrays.asList(
            250.0, 250.0, 250.0, 250.0, 250.0, 250.0, 250.0, 250.0, 250.0, 250.0, // 10 TD
            200.0, 200.0, 200.0, 200.0, // 4 TC  
            150.0, 150.0, 150.0, 150.0, 150.0, 150.0  // 6 TB
        );

        System.out.println("=".repeat(80));
        System.out.println("🎯 ANÁLISIS DE ESTRATEGIAS DE DIVISIÓN DE PEDIDOS");
        System.out.println("=".repeat(80));
        
        // Casos de prueba
        double[] volumenesTest = {500, 1000, 1500, 3000, 4200};
        
        for (double volumen : volumenesTest) {
            System.out.println("\n📦 PEDIDO DE " + volumen + " m³:");
            System.out.println("-".repeat(50));
            
            analizarEstrategias(volumen, capacidades);
            System.out.println();
        }
    }

    private void analizarEstrategias(double volumenTotal, List<Double> capacidades) {
        // Estrategia 1: Greedy Simple
        List<Double> greedy = estrategiaGreedy(volumenTotal, capacidades);
        System.out.println("🔵 Greedy Simple: " + formatearDivision(greedy));
        
        // Estrategia 2: Balanceada  
        List<Double> balanceada = estrategiaBalanceada(volumenTotal, capacidades);
        System.out.println("🟢 Balanceada: " + formatearDivision(balanceada));
        
        // Estrategia 3: Minimizar Camiones
        List<Double> minima = estrategiaMinima(volumenTotal, capacidades);
        System.out.println("🟡 Minimizar: " + formatearDivision(minima));
        
        // Análisis comparativo
        System.out.println("📊 Análisis:");
        System.out.println("   • Greedy: " + greedy.size() + " camiones, " + 
                          calcularEficiencia(greedy, volumenTotal) + "% eficiencia");
        System.out.println("   • Balanceada: " + balanceada.size() + " camiones, " + 
                          calcularEficiencia(balanceada, volumenTotal) + "% eficiencia");
        System.out.println("   • Minimizar: " + minima.size() + " camiones, " + 
                          calcularEficiencia(minima, volumenTotal) + "% eficiencia");
    }

    private List<Double> estrategiaGreedy(double volumen, List<Double> capacidades) {
        return java.util.ArrayList<Double>() {{
            double restante = volumen;
            int i = 0;
            while (restante > 0 && i < capacidades.size()) {
                double asignado = Math.min(restante, capacidades.get(i));
                add(asignado);
                restante -= asignado;
                i++;
                if (i >= capacidades.size() && restante > 0) i = 0; // Ciclar
            }
        }};
    }

    private List<Double> estrategiaBalanceada(double volumen, List<Double> capacidades) {
        return java.util.ArrayList<Double>() {{
            double restante = volumen;
            // Alternar entre tipos de camiones
            int[] indices = {0, 10, 14}; // TD, TC, TB
            int tipoActual = 0;
            
            while (restante > 0) {
                boolean asignado = false;
                for (int i = 0; i < 3 && restante > 0; i++) {
                    int indice = indices[i];
                    if (indice < capacidades.size()) {
                        double capacidad = capacidades.get(indice);
                        double asignacion = Math.min(restante, capacidad);
                        add(asignacion);
                        restante -= asignacion;
                        indices[i]++;
                        asignado = true;
                    }
                }
                if (!asignado) break;
            }
        }};
    }

    private List<Double> estrategiaMinima(double volumen, List<Double> capacidades) {
        return java.util.ArrayList<Double>() {{
            double restante = volumen;
            // Usar solo camiones más grandes hasta que no sea eficiente
            while (restante > 0) {
                if (restante >= 250) {
                    add(250.0);
                    restante -= 250;
                } else if (restante >= 200) {
                    add(200.0);
                    restante -= 200;
                } else if (restante >= 150) {
                    add(150.0);
                    restante -= 150;
                } else if (restante > 0) {
                    add(restante);
                    restante = 0;
                }
            }
        }};
    }

    private String formatearDivision(List<Double> division) {
        if (division.isEmpty()) return "[]";
        
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        
        // Agrupar por capacidad
        java.util.Map<Double, Integer> grupos = new java.util.HashMap<>();
        for (Double vol : division) {
            grupos.put(vol, grupos.getOrDefault(vol, 0) + 1);
        }
        
        boolean primero = true;
        for (java.util.Map.Entry<Double, Integer> entry : grupos.entrySet()) {
            if (!primero) sb.append(", ");
            if (entry.getValue() > 1) {
                sb.append(entry.getValue()).append("x").append(entry.getKey().intValue());
            } else {
                sb.append(entry.getKey().intValue());
            }
            primero = false;
        }
        
        sb.append("]");
        return sb.toString();
    }

    private String calcularEficiencia(List<Double> division, double volumenTotal) {
        double suma = division.stream().mapToDouble(Double::doubleValue).sum();
        return String.format("%.1f", (suma / volumenTotal) * 100);
    }
} 