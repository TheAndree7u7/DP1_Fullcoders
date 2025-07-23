package com.plg.dto.response;

import com.plg.utils.TipoDeSimulacion;

public class TipoSimulacionResponse {
    private TipoDeSimulacion tipoSimulacionAnterior;
    private TipoDeSimulacion tipoSimulacionNuevo;
    private String mensaje;
    private boolean exito;

    public TipoSimulacionResponse() {
    }

    public TipoSimulacionResponse(TipoDeSimulacion tipoSimulacionAnterior, TipoDeSimulacion tipoSimulacionNuevo,
            String mensaje, boolean exito) {
        this.tipoSimulacionAnterior = tipoSimulacionAnterior;
        this.tipoSimulacionNuevo = tipoSimulacionNuevo;
        this.mensaje = mensaje;
        this.exito = exito;
    }

    // Getters y setters
    public TipoDeSimulacion getTipoSimulacionAnterior() {
        return tipoSimulacionAnterior;
    }

    public void setTipoSimulacionAnterior(TipoDeSimulacion tipoSimulacionAnterior) {
        this.tipoSimulacionAnterior = tipoSimulacionAnterior;
    }

    public TipoDeSimulacion getTipoSimulacionNuevo() {
        return tipoSimulacionNuevo;
    }

    public void setTipoSimulacionNuevo(TipoDeSimulacion tipoSimulacionNuevo) {
        this.tipoSimulacionNuevo = tipoSimulacionNuevo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public boolean isExito() {
        return exito;
    }

    public void setExito(boolean exito) {
        this.exito = exito;
    }

    @Override
    public String toString() {
        return "TipoSimulacionResponse{" +
                "tipoSimulacionAnterior=" + tipoSimulacionAnterior +
                ", tipoSimulacionNuevo=" + tipoSimulacionNuevo +
                ", mensaje='" + mensaje + '\'' +
                ", exito=" + exito +
                '}';
    }
}