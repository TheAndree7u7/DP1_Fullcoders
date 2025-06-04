package com.plg.dto.request;

import com.plg.entity.TipoAlmacen;
import lombok.Data;

/**
 * DTO para la creación de un nuevo almacén.
 */
@Data
public class AlmacenRequest {
    private TipoAlmacen tipo;
    private int x;
    private int y;
    private double capacidadMaxGLP;
    private double capacidadMaxCombustible;
}
