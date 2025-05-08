package com.plg.entity;

import java.time.LocalDateTime;

/**
 * Patrón fábrica para crear objetos Pedido con configuración predeterminada.
 */
public class PedidoFactory {

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
        LocalDateTime ahora = LocalDateTime.now();
        return Pedido.builder()
                // campos heredados de Nodo
                .coordenada(coordenada)
                .bloqueado(false)
                .gScore(0)
                .fScore(0)
                .tipoNodo(TipoNodo.CLIENTE)
                // campos de Pedido
                .codigo("PEDIDO-" + coordenada.getFila() + "-" + coordenada.getColumna())
                .horasLimite(horasLimite)
                .fechaRegistro(ahora)
                .volumenGLPAsignado(volumenGLP)
                .volumenGLPEntregado(0.0)
                .volumenGLPPendiente(volumenGLP)
                .prioridad(1)
                .estado(EstadoPedido.REGISTRADO)
                .build();
    }
}