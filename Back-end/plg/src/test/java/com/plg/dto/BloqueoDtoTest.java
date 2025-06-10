package com.plg.dto;

import com.plg.entity.Bloqueo;
import com.plg.entity.Nodo;
import com.plg.entity.Coordenada;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class BloqueoDtoTest {

    @Test
    public void testBloqueoDtoLlenaCoordenadasCorrectamente() {
        // Crear nodos de prueba
        Nodo nodo1 = new Nodo(new Coordenada(1, 2), false, 0, 0, null);
        Nodo nodo2 = new Nodo(new Coordenada(3, 4), false, 0, 0, null);
        List<Nodo> nodos = Arrays.asList(nodo1, nodo2);

        // Crear bloqueo con nodos bloqueados
        Bloqueo bloqueo = new Bloqueo();
        bloqueo.setNodosBloqueados(nodos);

        // Crear el DTO
        BloqueoDto dto = new BloqueoDto(bloqueo);
        List<CoordenadaDto> coordenadas = dto.getCoordenadas();

        // Verificar que la lista no está vacía y contiene las coordenadas correctas
        assertNotNull(coordenadas);
        assertEquals(2, coordenadas.size());
        assertEquals(2, coordenadas.get(0).getX());
        assertEquals(1, coordenadas.get(0).getY());
        assertEquals(4, coordenadas.get(1).getX());
        assertEquals(3, coordenadas.get(1).getY());
    }
}
