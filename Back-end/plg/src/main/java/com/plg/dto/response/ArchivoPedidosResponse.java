package com.plg.dto.response;

import java.util.List;

import com.plg.entity.Pedido;

/**
 * DTO para la respuesta de archivos de pedidos procesados.
 */
public class ArchivoPedidosResponse {

    private String nombreArchivo;
    private int totalPedidosAgregados;
    private List<Pedido> pedidosAgregados;
    private String mensaje;

    // Constructores
    public ArchivoPedidosResponse() {
    }

    public ArchivoPedidosResponse(String nombreArchivo, int totalPedidosAgregados, List<Pedido> pedidosAgregados,
            String mensaje) {
        this.nombreArchivo = nombreArchivo;
        this.totalPedidosAgregados = totalPedidosAgregados;
        this.pedidosAgregados = pedidosAgregados;
        this.mensaje = mensaje;
    }

    // Getters y Setters
    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public int getTotalPedidosAgregados() {
        return totalPedidosAgregados;
    }

    public void setTotalPedidosAgregados(int totalPedidosAgregados) {
        this.totalPedidosAgregados = totalPedidosAgregados;
    }

    public List<Pedido> getPedidosAgregados() {
        return pedidosAgregados;
    }

    public void setPedidosAgregados(List<Pedido> pedidosAgregados) {
        this.pedidosAgregados = pedidosAgregados;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    @Override
    public String toString() {
        return "ArchivoPedidosResponse{" +
                "nombreArchivo='" + nombreArchivo + '\'' +
                ", totalPedidosAgregados=" + totalPedidosAgregados +
                ", pedidosAgregados=" + pedidosAgregados +
                ", mensaje='" + mensaje + '\'' +
                '}';
    }
}