package com.plg.dto;

import java.util.ArrayList;
import java.util.List;


import com.plg.entity.Nodo;
import com.plg.entity.Pedido;
import com.plg.utils.Gen;

import lombok.Data;

@Data
public class GenDto {
    private CamionDto camion;
    private List<NodoDto> nodos;
    private CoordenadaDto destino;
    private List<PedidoDto> pedidos;

    public GenDto(Gen gen) {
        this.camion = new CamionDto(gen.getCamion());
        this.nodos = new ArrayList<>();
        for (Nodo nodo : gen.construirRutaFinalApi()) {
            this.nodos.add(new NodoDto(nodo));
        }
        // EL destino siempre el último nodo de la ruta
        this.destino = new CoordenadaDto(gen.getRutaFinal().getLast().getCoordenada());
        this.pedidos = new ArrayList<>();
        for (Pedido pedido : gen.getPedidos()) {
            this.pedidos.add(new PedidoDto(pedido));
        }
 
    }
}
