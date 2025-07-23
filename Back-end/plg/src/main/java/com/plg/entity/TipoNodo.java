package com.plg.entity;

public enum TipoNodo {
    ALMACEN("ALMACEN"),
    INTERMEDIO("INTERMEDIO"),
    NORMAL("NORMAL"),
    CAMION_AVERIADO("CAMION_AVERIADO"),
    PEDIDO("PEDIDO"),
    ALMACEN_RECARGA("ALMACEN_RECARGA"),
    AVERIA_AUTOMATICA_T1("AVERIA_AUTOMATICA_T1"),
    AVERIA_AUTOMATICA_T2("AVERIA_AUTOMATICA_T2"),
    AVERIA_AUTOMATICA_T3("AVERIA_AUTOMATICA_T3");

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
