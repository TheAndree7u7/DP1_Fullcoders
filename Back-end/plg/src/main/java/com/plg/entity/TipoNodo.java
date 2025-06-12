package com.plg.entity;

public enum TipoNodo {
    ALMACEN("ALMACEN"),
    INTERMEDIO("INTERMEDIO"),
    NORMAL("NORMAL"),
    CAMION_AVERIADO("CAMION_AVERIADO"),
    PEDIDO("PEDIDO");

    private String tipo;

    TipoNodo(String tipo) {
        this.tipo = tipo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
