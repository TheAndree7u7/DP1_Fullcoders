package com.plg.utils.simulacion;

import com.plg.config.DataLoader;
import com.plg.entity.Camion;
import com.plg.entity.Coordenada;
import com.plg.entity.Nodo;
import com.plg.utils.Gen;
import com.plg.utils.Individuo;

/**
 * Aplica el estado final de los camiones de un Individuo al conjunto global
 * (DataLoader.camiones) para mantener continuidad entre paquetes.
 */
public class CamionStateApplier {

    public static void aplicarEstadoFinalCamiones(Individuo mejorIndividuo) {
        try {
            for (Gen gen : mejorIndividuo.getCromosoma()) {
                Camion camion = gen.getCamion();
                if (gen.getRutaFinal() != null && !gen.getRutaFinal().isEmpty()) {
                    Nodo posicionFinal = gen.getRutaFinal().get(gen.getRutaFinal().size() - 1);
                    Coordenada nuevaPos = posicionFinal.getCoordenada();

                    for (Camion camionGlobal : DataLoader.camiones) {
                        if (camionGlobal.getCodigo().equals(camion.getCodigo())) {
                            camionGlobal.setCoordenada(nuevaPos);
                            camionGlobal.setCombustibleActual(camion.getCombustibleActual());
                            camionGlobal.setCapacidadActualGLP(camion.getCapacidadActualGLP());
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error aplicando estado final de camiones: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 