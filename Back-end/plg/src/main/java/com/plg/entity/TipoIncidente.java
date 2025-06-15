package com.plg.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class TipoIncidente {

    private String tipo; // "TI1", "TI2", "TI3"
    private int horasInmovilizado;

    public TipoIncidente(String tipo) {
        this.tipo = tipo;
        switch (tipo) {
            case "TI1":
                this.horasInmovilizado = 2;
                break;
            case "TI2":
                this.horasInmovilizado = 4;
                break;
            case "TI3":
                this.horasInmovilizado = 8;
                break;
            default:
                throw new IllegalArgumentException("Tipo de incidente no válido: " + tipo);
        }
    }
}
