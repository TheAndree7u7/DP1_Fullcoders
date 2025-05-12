package com.plg.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Patrón fábrica para crear objetos Pedido con configuración predeterminada.
 */
public class PedidoFactory {

    // Lista estática para almacenar todos los pedidos creados
    public static final List<Pedido> pedidos = new ArrayList<>();

    // Referencia al mapa (singleton)
    private static final Mapa mapa = Mapa.getInstance();

    /**
     * Crea un pedido básico.
     *
     * @param coordenada     Coordenada del pedido
     * @param volumenGLP     Volumen de GLP a entregar (m3)
     * @param horasLimite    Horas límite para entrega
     * @return instancia de Pedido con estado REGISTRADO y tipoNodo CLIENTE
     */
    public static Pedido crearPedido(
            Coordenada coordenada,
            double volumenGLP,
            double horasLimite) {

        // Crear el pedido
        Pedido pedido = Pedido.builder()
                // campos heredados de Nodo
                .coordenada(coordenada)
                .bloqueado(false)
                .gScore(0)
                .fScore(0)
                .tipoNodo(TipoNodo.PEDIDO)
                // campos de Pedido
                .codigo("PEDIDO-" + coordenada.getFila() + "-" + coordenada.getColumna())
                .horasLimite(horasLimite)
                .volumenGLPAsignado(volumenGLP)
                .estado(EstadoPedido.REGISTRADO)
                .build();

        // Agregar el pedido a la lista
        pedidos.add(pedido);

        // Actualizar el mapa con el nuevo pedido
        mapa.setNodo(coordenada, pedido);

        return pedido;
    }

    /**
     * Crea un pedido a partir de una línea de texto y una fecha inicial.
     *
     * @param line          Línea de texto con los datos del pedido
     * @param fechaInicial  Fecha inicial para calcular la fecha de registro
     * @return instancia de Pedido creada
     */
    public static Pedido crearPedido(String line, LocalDateTime fechaInicial) {
        String[] partes = line.split(":");
        if (partes.length != 2) {
            throw new IllegalArgumentException("Formato de línea inválido: " + line);
        }

        // Calcular la fecha de registro
        String[] horaRegistro = partes[0].split("[dhm]");
        long minutosDesdeInicio = (Integer.parseInt(horaRegistro[0]) - 1) * 24 * 60L +
                                  Integer.parseInt(horaRegistro[1]) * 60L +
                                  Integer.parseInt(horaRegistro[2]);
        LocalDateTime fechaRegistro = fechaInicial.plusMinutes(minutosDesdeInicio);

        // Extraer datos del pedido
        String[] datosPedido = partes[1].split(",");
        Coordenada coordenada = new Coordenada(
                Integer.parseInt(datosPedido[0]),
                Integer.parseInt(datosPedido[1])
        );
        String idCliente = datosPedido[2];
        int m3 = Integer.parseInt(datosPedido[3].substring(0, datosPedido[3].indexOf('m')));
        int horaLimite = Integer.parseInt(datosPedido[4].substring(0, datosPedido[4].indexOf('h')));

        // Crear el pedido
        Pedido pedido = Pedido.builder()
                .coordenada(coordenada)
                .bloqueado(false)
                .gScore(0)
                .fScore(0)
                .tipoNodo(TipoNodo.PEDIDO)
                .codigo("PEDIDO-" + coordenada.getFila() + "-" + coordenada.getColumna())
                .horasLimite((double) horaLimite)
                .volumenGLPAsignado((double) m3)
                .estado(EstadoPedido.REGISTRADO)
                .fechaRegistro(fechaRegistro)
                .build();
        // Agregar el pedido a la lista
        pedidos.add(pedido);
        // Actualizar el mapa con el nuevo pedido
        mapa.setNodo(coordenada, pedido);
        return pedido;
    }

}