package com.plg.utils.simulacion;

import java.util.ArrayList;
import java.util.List;

import com.plg.config.DataLoader;
import com.plg.entity.Camion;
import com.plg.entity.EstadoCamion;
import com.plg.entity.Nodo;
import com.plg.utils.Gen;
import com.plg.utils.Individuo;
import com.plg.entity.Pedido;

/**
 * Fábrica de objetos {@link Individuo} para casos especiales.
 */
public class IndividuoFactory {

    /**
     * Crea un Individuo vacío que refleja el estado actual de los camiones cuando
     * no
     * existen pedidos por atender.
     */
    public static Individuo crearIndividuoVacio() {
        try {
            System.out.println("🔧 Creando individuo vacío...");
            List<Pedido> pedidosVacios = new ArrayList<>();
            Individuo individuoVacio = new Individuo(pedidosVacios);

            List<Gen> cromosoma = new ArrayList<>();
            for (Camion camion : DataLoader.camiones) {
                if (camion.getEstado() == EstadoCamion.DISPONIBLE) {
                    // Validar que el camión tenga coordenadas válidas
                    if (camion.getCoordenada() == null) {
                        System.err.println("⚠️ Camión " + camion.getCodigo() + " tiene coordenadas nulas");
                        continue;
                    }

                    Gen gen = new Gen(camion, new ArrayList<>());
                    List<Nodo> rutaActual = new ArrayList<>();
                    rutaActual.add(camion); // posición actual
                    gen.setRutaFinal(rutaActual);
                    gen.setPedidos(new ArrayList<>());
                    gen.setFitness(0.0);
                    cromosoma.add(gen);
                }
            }

            individuoVacio.setCromosoma(cromosoma);
            individuoVacio.setFitness(0.0);
            System.out.println("✅ Individuo vacío creado exitosamente con " + cromosoma.size() + " genes");
            return individuoVacio;
        } catch (Exception e) {
            System.err.println("❌ Error al crear individuo vacío: " + e.getMessage());
            e.printStackTrace();

            // Crear un individuo mínimo de emergencia
            Individuo individuoEmergencia = new Individuo(new ArrayList<>());
            individuoEmergencia.setCromosoma(new ArrayList<>());
            individuoEmergencia.setFitness(0.0);
            return individuoEmergencia;
        }
    }
}