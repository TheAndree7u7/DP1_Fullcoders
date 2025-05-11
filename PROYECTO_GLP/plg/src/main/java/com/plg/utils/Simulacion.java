package com.plg.utils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.plg.config.DataLoader;
import com.plg.entity.Mapa;
import com.plg.entity.Pedido;

public class Simulacion {

    private List<Pedido> pedidosSemanal;
    private Mapa mapa = Mapa.getInstance();
    private LocalDateTime fechaActual;

    public static void configurarSimulacion(LocalDateTime startDate){
        // Cargar datos
        fechaActual = startDate;

        // Tarea 1
        // En el dataLoader las variables de camiones, almacenes, mantenimientos y averias
        // tienen que ser stati

        DataLoader.initializeAlmacenes();
        DataLoader.initializeCamiones();
        DataLoader.initializeMantenimientos();
        DataLoader.initializeAverias();
        
        LocalDateTime fechaFin = fechaActual.plusDays(7);

        // Filtrar 
        pedidosSemanal = DataLoader.pedidos.stream()
                .filter(pedido -> pedido.getFecha().isAfter(fechaActual) && pedido.getFecha().isBefore(fechaFin))
                .collect(Collectors.toList());

    }

    // Pedido no tiene fecha

    public static void ejecutarSimulacion(){
        // Ejecutar simulacion
        // 1. Inicializar el mapa
        // 2. Inicializar los camiones
        // 3. Inicializar los almacenes
        // 4. Inicializar los pedidos
        // 5. Ejecutar el algoritmo genetico
        // 6. Imprimir el mapa con la mejor ruta encontrada
        fechaFin = pedidosSemanal.get(pedidosSemanal.size() - 1).getFecha();
        mapa.actualizarBloqueos(fechaActual);

        ArrayList<Pedido> pedidosPorAtender = new ArrayList<>(); // Grupo pequeño para atender
        LinkedList<Pedido> pedidosPlanificados = new LinkedList<>(); // Se pueden replanificar
        ArrayList<Pedido> pedidosEntregados = new ArrayList<>();

        while(!pedidosSemanal.isEmpty()){
            Pedido pedido = pedidosSemanal.get(0);
            if(!pedido.getFecha().isAfter(fechaActual)){
                pedidosSemanal.remove(0);
                pedidosPorAtender.add(pedido);
            }else{
                actualizarEstadoDelSistema(pedidosPorAtender, pedidosPlanificados, pedidosEntregados, fechaActual);
                fechaActual = fechaActual.plusHours(3);
            }
        }
    }

    public static void actualizarEstadoDelSistema(ArrayList<Pedido> pedidosPorAtender, LinkedList<Pedido> pedidosPlanificados, ArrayList<Pedido> pedidosEntregados, LocalDateTime fechaActual){
        // Actualizar el estado del sistema
        // 1. Actualizar el mapa
        // 2. Actualizar los camiones
        // 3. Actualizar los almacenes
        // 4. Actualizar los pedidos
        // 5. Ejecutar el algoritmo genetico
        // 6. Imprimir el mapa con la mejor ruta encontrada
    }

}
