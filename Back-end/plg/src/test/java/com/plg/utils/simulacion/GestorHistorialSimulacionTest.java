package com.plg.utils.simulacion;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.plg.dto.IndividuoDto;
import com.plg.entity.Pedido;
import com.plg.entity.Bloqueo;
import com.plg.utils.Individuo;
import com.plg.utils.simulacion.IndividuoFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Test para verificar que el método getPaqueteActual() funciona correctamente
 * después de la corrección del desfase de índices.
 */
public class GestorHistorialSimulacionTest {

    @BeforeEach
    void setUp() {
        // Limpiar el historial antes de cada test
        GestorHistorialSimulacion.limpiarHistorialCompleto();
    }

    @Test
    void testPaqueteActualInicialEsCero() {
        // Cuando no se ha consumido ningún paquete
        assertEquals(0, GestorHistorialSimulacion.getPaqueteActual(),
                "El paquete actual debe ser 0 cuando no se ha consumido ningún paquete");
    }

    @Test
    void testPaqueteActualDespuesDeConsumir() {
        // Agregar algunos paquetes de prueba
        LocalDateTime tiempo1 = LocalDateTime.now();
        LocalDateTime tiempo2 = tiempo1.plusHours(2);
        LocalDateTime tiempo3 = tiempo2.plusHours(2);

        IndividuoDto paquete1 = crearPaquetePrueba(tiempo1);
        IndividuoDto paquete2 = crearPaquetePrueba(tiempo2);
        IndividuoDto paquete3 = crearPaquetePrueba(tiempo3);

        GestorHistorialSimulacion.agregarPaquete(paquete1);
        GestorHistorialSimulacion.agregarPaquete(paquete2);
        GestorHistorialSimulacion.agregarPaquete(paquete3);

        // Verificar que inicialmente no hay paquete actual
        assertEquals(0, GestorHistorialSimulacion.getPaqueteActual(),
                "Antes de consumir, el paquete actual debe ser 0");

        // Consumir el primer paquete
        IndividuoDto paqueteConsumido1 = GestorHistorialSimulacion.obtenerSiguientePaquete();
        assertNotNull(paqueteConsumido1, "El primer paquete debe ser consumido");
        assertEquals(0, GestorHistorialSimulacion.getPaqueteActual(),
                "Después de consumir el primer paquete, el paquete actual debe ser 0");

        // Consumir el segundo paquete
        IndividuoDto paqueteConsumido2 = GestorHistorialSimulacion.obtenerSiguientePaquete();
        assertNotNull(paqueteConsumido2, "El segundo paquete debe ser consumido");
        assertEquals(1, GestorHistorialSimulacion.getPaqueteActual(),
                "Después de consumir el segundo paquete, el paquete actual debe ser 1");

        // Consumir el tercer paquete
        IndividuoDto paqueteConsumido3 = GestorHistorialSimulacion.obtenerSiguientePaquete();
        assertNotNull(paqueteConsumido3, "El tercer paquete debe ser consumido");
        assertEquals(2, GestorHistorialSimulacion.getPaqueteActual(),
                "Después de consumir el tercer paquete, el paquete actual debe ser 2");

        // Verificar que no hay más paquetes
        IndividuoDto paqueteConsumido4 = GestorHistorialSimulacion.obtenerSiguientePaquete();
        assertNull(paqueteConsumido4, "No debe haber más paquetes para consumir");
        assertEquals(2, GestorHistorialSimulacion.getPaqueteActual(),
                "El paquete actual debe seguir siendo 2 después de intentar consumir un paquete inexistente");
    }

    @Test
    void testSincronizacionConTotalPaquetes() {
        // Agregar paquetes y verificar que el total coincide
        LocalDateTime tiempo = LocalDateTime.now();

        GestorHistorialSimulacion.agregarPaquete(crearPaquetePrueba(tiempo));
        GestorHistorialSimulacion.agregarPaquete(crearPaquetePrueba(tiempo.plusHours(2)));
        GestorHistorialSimulacion.agregarPaquete(crearPaquetePrueba(tiempo.plusHours(4)));

        assertEquals(3, GestorHistorialSimulacion.getTotalPaquetes(),
                "El total de paquetes debe ser 3");
        assertEquals(0, GestorHistorialSimulacion.getPaqueteActual(),
                "El paquete actual debe ser 0 antes de consumir");

        // Consumir todos los paquetes
        GestorHistorialSimulacion.obtenerSiguientePaquete(); // Consumir paquete 0
        GestorHistorialSimulacion.obtenerSiguientePaquete(); // Consumir paquete 1
        GestorHistorialSimulacion.obtenerSiguientePaquete(); // Consumir paquete 2

        assertEquals(3, GestorHistorialSimulacion.getTotalPaquetes(),
                "El total de paquetes debe seguir siendo 3");
        assertEquals(2, GestorHistorialSimulacion.getPaqueteActual(),
                "El paquete actual debe ser 2 después de consumir todos");
    }

    @Test
    void testReiniciariReproduccion() {
        // Agregar y consumir algunos paquetes
        LocalDateTime tiempo = LocalDateTime.now();

        GestorHistorialSimulacion.agregarPaquete(crearPaquetePrueba(tiempo));
        GestorHistorialSimulacion.agregarPaquete(crearPaquetePrueba(tiempo.plusHours(2)));

        GestorHistorialSimulacion.obtenerSiguientePaquete(); // Consumir paquete 0
        GestorHistorialSimulacion.obtenerSiguientePaquete(); // Consumir paquete 1

        assertEquals(1, GestorHistorialSimulacion.getPaqueteActual(),
                "El paquete actual debe ser 1 después de consumir 2 paquetes");

        // Reiniciar reproducción
        GestorHistorialSimulacion.reiniciarReproduccion();

        assertEquals(0, GestorHistorialSimulacion.getPaqueteActual(),
                "El paquete actual debe ser 0 después de reiniciar la reproducción");
    }

    /**
     * Crea un paquete de prueba con datos mínimos necesarios.
     */
    private IndividuoDto crearPaquetePrueba(LocalDateTime tiempo) {
        try {
            // Crear un individuo vacío
            Individuo individuo = IndividuoFactory.crearIndividuoVacio();

            // Crear listas vacías para pedidos y bloqueos
            List<Pedido> pedidos = new ArrayList<>();
            List<Bloqueo> bloqueos = new ArrayList<>();

            // Crear y devolver el paquete
            return new IndividuoDto(individuo, pedidos, bloqueos, tiempo);
        } catch (Exception e) {
            // Fallback en caso de error
            return new IndividuoDto(null, new ArrayList<>(), new ArrayList<>(), tiempo);
        }
    }
}