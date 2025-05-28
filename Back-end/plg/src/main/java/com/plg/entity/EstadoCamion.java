package com.plg.entity;

public enum EstadoCamion {

    DISPONIBLE("Camión listo para operar y sin ninguna entrega en progreso", "#00FF00"),
    NO_DISPONIBLE("Camión no listo para operar", "#FF0000"),
    EN_RUTA("Camión actualmente en camino a realizar una entrega en movimiento", "#0000FF"),

    ENTREGANDO_GLP_A_CLIENTE("Camión en proceso de descarga de GLP al cliente", "#0066CC"),

    //!MANTENIMIENTO
    EN_MANTENIMIENTO("Camión en mantenimiento por diferentes motivos", "#000000"),
    
    EN_MANTENIMIENTO_PREVENTIVO("Mantenimiento preventivo programado (1 día)", "#FFCC00"),

    EN_MANTENIMIENTO_CORRECTIVO("Aun no especificado por el profesor", "#FF9900"),

    EN_MANTENIMIENTO_POR_AVERIA("Camión fuera de operación por avería (taller)", "#990000"), 
    //!INMOVILIZADO
    INMOVILIZADO_POR_AVERIA("Detenido en nodo por avería menor o incidente (2h o 4h)", "#CC3300"),

    SIN_COMBUSTIBLE("Camión sin gasolina suficiente para continuar", "#808080"),

    //!RECARGANDO_O_ENTREGANDO_COMBUSTIBLE
    RECIBIENDO_COMBUSTIBLE("Recargando gasolina (combustible de motor) en planta central o intermedio o desde otro camion ", "#6666FF"),
    
	ENTREGANDO_COMBUSTIBLE_A_CAMION(" Dando combustible a otrocamion ", "#6666FF"),

    RECIBIENDO_GLP("Recargando GLP para entregas en planta central o intermedio o desde otro camion", "#66CC00"),

    ENTREGANDO_GLP_A_CAMION("Transfiriendo GLP hacia  otro camión", "#33CCCC"), 

    ALMACEN_TEMPORAL("Unidad averiada actuando como depósito temporal de GLP", "#9933CC");

    private final String descripcion;
    private final String colorHex;

    EstadoCamion(String descripcion, String colorHex) {
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
