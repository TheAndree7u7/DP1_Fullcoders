package com.plg.dto;

import java.util.ArrayList;
import java.util.List;

import com.plg.utils.Parametros;
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
    //Almacenes 
    private List<AlmacenDto> almacenes;
    public GenDto(Gen gen) {
        this.camion = new CamionDto(gen.getCamion());
        this.nodos = new ArrayList<>();
        for (Nodo nodo : gen.getRutaFinal()) {
            this.nodos.add(new NodoDto(nodo));
        }
        if(gen.getPedidos().isEmpty()) {
            this.destino = new CoordenadaDto(Parametros.dataLoader.almacenes.get(0).getCoordenada());
        }else{
            this.destino = new CoordenadaDto(gen.getPedidos().getLast().getCoordenada());
        }
        this.pedidos = new ArrayList<>();
        for (Pedido pedido : gen.getPedidos()) {
            this.pedidos.add(new PedidoDto(pedido));
        }
 
    }
}
