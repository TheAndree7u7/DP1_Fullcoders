package com.plg.dto.request;

import com.plg.entity.TipoCamion;
import lombok.Data;

/**
 * DTO para la creación de un nuevo camión.
 */
@Data
public class CamionRequest {
    private TipoCamion tipo;
    private boolean operativo = true;
    private int x;
    private int y;
}
