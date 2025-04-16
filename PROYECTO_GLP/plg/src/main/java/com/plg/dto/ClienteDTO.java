package com.plg.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDTO {
    private String id; // Cambiado de Long a String para coincidir con la entidad
    private String nombre;
    private String dni;
    private String telefono;
    private String email;
    private String direccion;
    private Integer posX; // Añadido para completitud
    private Integer posY; // Añadido para completitud
}