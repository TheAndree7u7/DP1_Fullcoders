package com.plg.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;  
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoIncidente {
    private String tipo;
    private int horasInmovilizado;
}
