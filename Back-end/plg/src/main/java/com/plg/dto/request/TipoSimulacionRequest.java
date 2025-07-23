package com.plg.dto.request;

import com.plg.utils.TipoDeSimulacion;

public class TipoSimulacionRequest {
    private TipoDeSimulacion tipoSimulacion;

    public TipoSimulacionRequest() {
    }

    public TipoSimulacionRequest(TipoDeSimulacion tipoSimulacion) {
        this.tipoSimulacion = tipoSimulacion;
    }

    // Getters y setters
    public TipoDeSimulacion getTipoSimulacion() {
        return tipoSimulacion;
    }

    public void setTipoSimulacion(TipoDeSimulacion tipoSimulacion) {
        this.tipoSimulacion = tipoSimulacion;
    }

    @Override
    public String toString() {
        return "TipoSimulacionRequest{" +
                "tipoSimulacion=" + tipoSimulacion +
                '}';
    }
}