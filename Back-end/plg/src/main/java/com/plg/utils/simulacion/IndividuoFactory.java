package com.plg.utils.simulacion;

import java.util.ArrayList;
import java.util.List;

import com.plg.utils.Parametros;
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
     * Crea un Individuo vacío que refleja el estado actual de los camiones cuando no
     * existen pedidos por atender.
     */
    public static Individuo crearIndividuoVacio() {
        List<Pedido> pedidosVacios = new ArrayList<>();
        Individuo individuoVacio = new Individuo(pedidosVacios);

        List<Gen> cromosoma = new ArrayList<>();
        for (Camion camion : Parametros.dataLoader.camiones) {
            if (camion.getEstado() == EstadoCamion.DISPONIBLE) {
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
        return individuoVacio;
    }
} 