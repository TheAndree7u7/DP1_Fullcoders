package com.plg.entity;

 
public enum EstadoPedido {
    //!SIN_ASIGNAR
    SIN_ASIGNAR("Pedido sin asignar, ya que recien seregistro el pedido", "#000000"),
    REGISTRADO(
            "Pedido ingresado. Aún no planificado", "#CCCCCC"),

    PENDIENTE_PLANIFICACION("En espera de planificación", "#FFCC00"),

    //!PLANIFICADO
    PLANIFICADO("Pedido planificado ya sea total o parcialmente", "#009900"),  

    PLANIFICADO_PARCIALMENTE("Solo una parte del pedido ha sido planificada", "#FFDD66"),

    PLANIFICADO_TOTALMENTE("El 100% del pedido ha sido planificado", "#00BFFF"),

    EN_RUTA("Pedido en tránsito hacia el cliente", "#3399FF"),

    //!RECIBIENDO GLP
    RECIBIENDO("El cliente está recibiendo el GLP solicitado ya sea todo el GLP o de forma parcialmente ", "#FFCC33"),
    RECIBIENDO_PARCIALMENTE("El cliente está recibiendo una parte del GLP solicitado", "#6699FF"),

    RECIBIENDO_TOTALMENTE("El cliente está recibiendo todo el GLP del pedido", "#0066CC"),
    //!ENTREGADO
    ENTREGADO_PARCIALMENTE("Entrega parcial completada. Aún falta parte del pedido", "#FF9966"),

    ENTREGADO_TOTALMENTE("Pedido completado. Se entregó el 100%", "#00CC66"),

    REPROGRAMADO("El pedido fue replanificado por logística o incidente", "#FF9900"),

    NO_ENTREGADO_EN_TIEMPO("El pedido no se cumplió en el plazo indicado", "#FF3333");

    private final String descripcion;
    private final String colorHex;

    EstadoPedido(String descripcion, String colorHex) {
        this.descripcion = descripcion;
        this.colorHex = colorHex;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getColorHex() {
        return colorHex;
    }
}
