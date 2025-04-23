package com.plg;

import com.plg.planner.Planner;

public class Main {
    public static void main(String[] args) {
        System.out.println("Iniciando planificación logística de PLG...");
        Planner planner = new Planner();
        planner.ejecutar();
    }
}
