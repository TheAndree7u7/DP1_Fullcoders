package com.plg.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.plg.entity.Pedido;
import com.plg.entity.EstadoPedido;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticasPedidosDto {

    private LocalDateTime fechaSimulacion;
    private int totalPedidosAsignados;
    private int pedidosEntregadosCompletamente;
    private int pedidosEntregadosParcialmente;
    private int pedidosNoEntregados;
    private double volumenTotalAsignado;
    private double volumenTotalEntregado;
    private double porcentajeEntrega;
    private Map<EstadoPedido, Integer> pedidosPorEstado;
    private List<String> pedidosNoEntregadosCodigos;
    private List<String> pedidosEntregadosCodigos;

    public EstadisticasPedidosDto(List<Pedido> pedidos, LocalDateTime fechaSimulacion) {
        this.fechaSimulacion = fechaSimulacion;
        this.totalPedidosAsignados = pedidos.size();

        // Calcular estadísticas
        this.volumenTotalAsignado = pedidos.stream()
                .mapToDouble(Pedido::getVolumenGLPAsignado)
                .sum();

        this.volumenTotalEntregado = pedidos.stream()
                .mapToDouble(Pedido::getVolumenGLPEntregado)
                .sum();

        // Contar pedidos por estado de entrega
        this.pedidosEntregadosCompletamente = (int) pedidos.stream()
                .filter(p -> p.getVolumenGLPEntregado() >= p.getVolumenGLPAsignado())
                .count();

        this.pedidosEntregadosParcialmente = (int) pedidos.stream()
                .filter(p -> p.getVolumenGLPEntregado() > 0 && p.getVolumenGLPEntregado() < p.getVolumenGLPAsignado())
                .count();

        this.pedidosNoEntregados = (int) pedidos.stream()
                .filter(p -> p.getVolumenGLPEntregado() == 0)
                .count();

        // Calcular porcentaje de entrega
        this.porcentajeEntrega = this.volumenTotalAsignado > 0
                ? (this.volumenTotalEntregado / this.volumenTotalAsignado) * 100
                : 0;

        // Agrupar por estado
        this.pedidosPorEstado = pedidos.stream()
                .collect(Collectors.groupingBy(
                        Pedido::getEstado,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));

        // Listar códigos de pedidos no entregados
        this.pedidosNoEntregadosCodigos = pedidos.stream()
                .filter(p -> p.getVolumenGLPEntregado() == 0)
                .map(Pedido::getCodigo)
                .collect(Collectors.toList());

        // Listar códigos de pedidos entregados (completamente o parcialmente)
        this.pedidosEntregadosCodigos = pedidos.stream()
                .filter(p -> p.getVolumenGLPEntregado() > 0)
                .map(Pedido::getCodigo)
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return String.format(
                "📊 Estadísticas de Pedidos - %s%n" +
                        "   • Total asignados: %d%n" +
                        "   • Entregados completamente: %d%n" +
                        "   • Entregados parcialmente: %d%n" +
                        "   • No entregados: %d%n" +
                        "   • Volumen asignado: %.2f m³%n" +
                        "   • Volumen entregado: %.2f m³%n" +
                        "   • Porcentaje entrega: %.2f%%%n" +
                        "   • Por estado: %s",
                fechaSimulacion,
                totalPedidosAsignados,
                pedidosEntregadosCompletamente,
                pedidosEntregadosParcialmente,
                pedidosNoEntregados,
                volumenTotalAsignado,
                volumenTotalEntregado,
                porcentajeEntrega,
                pedidosPorEstado);
    }
}