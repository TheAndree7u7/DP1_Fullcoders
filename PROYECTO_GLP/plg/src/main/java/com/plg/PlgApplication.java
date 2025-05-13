package com.plg;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.plg.entity.Almacen;
import com.plg.entity.Averia;
import com.plg.entity.Bloqueo;
import com.plg.entity.Camion;
import com.plg.entity.CamionFactory;
import com.plg.entity.Coordenada;
import com.plg.entity.EstadoPedido;
import com.plg.entity.Mantenimiento;
import com.plg.entity.Mapa;
import com.plg.entity.Nodo;
import com.plg.entity.Pedido;
import com.plg.entity.PedidoFactory;
import com.plg.entity.TipoCamion;
import com.plg.utils.AlgoritmoGenetico;
import com.plg.utils.Individuo;
import com.plg.utils.Parametros;
import com.plg.config.DataLoader;

@SpringBootApplication
public class PlgApplication implements CommandLineRunner {

    @Autowired
    private DataLoader dataLoader;

    public static void main(String[] args) {
        SpringApplication.run(PlgApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Mapa mapa = Mapa.getInstance();
        List<Almacen> almacenes = dataLoader.initializeAlmacenes();
        List<Camion> camiones = dataLoader.initializeCamiones();
        List<Pedido> pedidos = dataLoader.initializePedidos();

        // Verificar si hay pedidos
        if (pedidos.isEmpty()) {
            System.out.println("No hay pedidos para procesar.");
            return;
        }

        // Obtener la fecha del primer pedido como punto de partida
        final LocalDateTime[] tiempoActualWrapper = { pedidos.get(0).getFecha() }; // Suponiendo que Pedido tiene un atributo 'fecha'
        int bloqueMinutos = 120; // minutos
        final LocalDateTime[] tiempoFinalWrapper = { tiempoActualWrapper[0].plusMinutes(bloqueMinutos) };

        int bloque = 1;

        while (!pedidos.isEmpty()) {
            System.out.println("Bloque " + bloque + ": intervalo de tiempo: " + tiempoActualWrapper[0] + " - " + tiempoFinalWrapper[0]);

            // Filtrar pedidos dentro del bloque de tiempo actual
            List<Pedido> pedidosBloque = pedidos.stream()
                .filter(p -> !p.getFecha().isBefore(tiempoActualWrapper[0]) && p.getFecha().isBefore(tiempoFinalWrapper[0]))
                .toList();

            // Imprimir pedidos del bloque
            pedidosBloque.forEach(System.out::println);

            // Ejecutar el algoritmo genético con los pedidos del bloque actual
            if (!pedidosBloque.isEmpty()) {
                AlgoritmoGenetico algoritmoGenetico = new AlgoritmoGenetico(mapa, pedidosBloque, camiones, almacenes);
                algoritmoGenetico.ejecutarAlgoritmo();
                Individuo mejorIndividuo = algoritmoGenetico.getMejorIndividuo();
                mapa.imprimirMapa(mejorIndividuo);

                // Simular el movimiento de los camiones
                simularMovimientoCamiones(camiones, mejorIndividuo, tiempoActualWrapper[0], tiempoFinalWrapper[0], pedidosBloque);
            }

            // Eliminar pedidos procesados
            pedidos.removeAll(pedidosBloque);

            // Avanzar al siguiente bloque de tiempo
            tiempoActualWrapper[0] = tiempoFinalWrapper[0];
            tiempoFinalWrapper[0] = tiempoActualWrapper[0].plusMinutes(bloqueMinutos); // Avanzar 120 minutos
            bloque++;
        }
    }

    /**
     * Simula el movimiento de los camiones hacia los pedidos asignados.
     */
    private void simularMovimientoCamiones(List<Camion> camiones, Individuo mejorIndividuo, LocalDateTime tiempoInicio, LocalDateTime tiempoFin, List<Pedido> pedidosBloque) {
        for (Camion camion : camiones) {
            // Obtener la ruta asignada al camión en la solución
            List<Nodo> ruta = mejorIndividuo.getRuta(camion);

            if (ruta == null || ruta.isEmpty()) {
                continue; // Si no hay ruta asignada, pasar al siguiente camión
            }

            // Simular el movimiento del camión
            for (Nodo nodo : ruta) {
                // Calcular el tiempo necesario para llegar al nodo
                long tiempoNecesario = calcularTiempoMovimiento(camion.getCoordenada(), nodo.getCoordenada());

                // Si el camión puede llegar al nodo dentro del tiempo del bloque
                if (tiempoInicio.plusMinutes(tiempoNecesario).isBefore(tiempoFin)) {
                    camion.setCoordenada(nodo.getCoordenada()); // Actualizar la posición del camión
                    tiempoInicio = tiempoInicio.plusMinutes(tiempoNecesario);

                    // Si el nodo es un pedido, marcarlo como entregado
                    if (nodo instanceof Pedido pedido) {
                        pedido.setEstado(EstadoPedido.ENTREGADO_TOTALMENTE);
                        System.out.println("Pedido entregado: " + pedido.getCodigo());
                    }
                } else {
                    // Si no puede llegar, reprogramar el pedido
                    if (nodo instanceof Pedido pedido) {
                        pedido.setEstado(EstadoPedido.EN_RUTA);
                        System.out.println("Pedido reprogramado: " + pedido.getCodigo());
                    }
                    break; // Salir del bucle si no puede continuar
                }
            }
        }
    }

    /**
     * Calcula el tiempo necesario para que un camión se mueva de una coordenada a otra.
     */
    private long calcularTiempoMovimiento(Coordenada origen, Coordenada destino) {
        int distancia = Math.abs(origen.getFila() - destino.getFila()) + Math.abs(origen.getColumna() - destino.getColumna());
        return distancia * 60; // 1 nodo por hora = 60 minutos por nodo
    }
}
