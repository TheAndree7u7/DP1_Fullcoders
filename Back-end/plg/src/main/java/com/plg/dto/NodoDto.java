package com.plg.dto;

import com.plg.entity.Nodo;
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
        this.tipo = nodo.getTipoNodo();
    }
}
