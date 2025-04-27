package com.plg.model;

import com.plg.model.enums.TipoIncidente;
import com.plg.model.enums.TipoTurno;

public class Averia {
    private TipoTurno turno;
    private String codigoCamion;
    private TipoIncidente tipoIncidente;

    public Averia(TipoTurno turno, String codigoCamion, TipoIncidente tipoIncidente) {
        this.turno = turno;
        this.codigoCamion = codigoCamion;
        this.tipoIncidente = tipoIncidente;
    }

    public Averia(String line) {
        // Formato esperado: T1_TA01_TI2
        String[] partes = line.split("_");
        if (partes.length == 3) {
            this.turno = TipoTurno.valueOf(partes[0]);
            this.codigoCamion = partes[1];
            this.tipoIncidente = TipoIncidente.valueOf(partes[2]);
        } else {
            throw new IllegalArgumentException("Formato inválido de avería: " + line);
        }
    }

    public TipoTurno getTurno() {
        return turno;
    }

    public String getCodigoCamion() {
        return codigoCamion;
    }

    public TipoIncidente getTipoIncidente() {
        return tipoIncidente;
    }

    @Override
    public String toString() {
        return "Averia{" +
                "turno=" + turno +
                ", codigoCamion='" + codigoCamion + '\'' +
                ", tipoIncidente=" + tipoIncidente +
                '}';
    }
}
