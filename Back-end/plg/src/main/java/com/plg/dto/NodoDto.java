package com.plg.dto;

import com.plg.entity.Almacen;
import com.plg.entity.Camion;
import com.plg.entity.Nodo;
import com.plg.entity.Pedido;
import com.plg.entity.TipoNodo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class NodoDto {
    private CoordenadaDto coordenada;
    private TipoNodo tipo;
    public NodoDto(Nodo nodo) {
        this.coordenada = new CoordenadaDto(nodo.getCoordenada());
        if(nodo instanceof Camion) {
            this.tipo = TipoNodo.CAMION_AVERIADO;
        } else if(nodo instanceof Almacen) {
            this.tipo = TipoNodo.ALMACEN;
        } else if(nodo instanceof Pedido) {
            this.tipo = TipoNodo.PEDIDO;
        }else {
            this.tipo = TipoNodo.NORMAL;
        }
    }

    public NodoDto(Nodo nodo, TipoNodo tipoNodo){
        this.coordenada = new CoordenadaDto(nodo.getCoordenada());
        this.tipo = tipoNodo;
    }
}
