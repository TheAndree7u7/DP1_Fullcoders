package com.plg.service;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.plg.config.DataLoader;
import com.plg.dto.request.PedidoRequest;
import com.plg.entity.Camion;
import com.plg.entity.Coordenada;
import com.plg.entity.EstadoCamion;
import com.plg.entity.Pedido;
import com.plg.entity.TipoCamion;
import com.plg.repository.PedidoRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PedidoDivisionTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @InjectMocks
    private PedidoService pedidoService;

    @BeforeEach
    void setUp() {
        // Configurar camiones de prueba
        DataLoader.camiones = Arrays.asList(
            crearCamion("C001", TipoCamion.TA, 100.0, EstadoCamion.DISPONIBLE),
            crearCamion("C002", TipoCamion.TB, 150.0, EstadoCamion.DISPONIBLE),
            crearCamion("C003", TipoCamion.TC, 200.0, EstadoCamion.DISPONIBLE),
            crearCamion("C004", TipoCamion.TD, 250.0, EstadoCamion.DISPONIBLE)
        );
    }

    @Test
    void testPedidoNormalNoDivision() {
        // Arrange
        PedidoRequest request = new PedidoRequest();
        request.setX(10);
        request.setY(5);
        request.setVolumenGLP(150.0);
        request.setHorasLimite(8.0);

        Pedido pedidoMock = crearPedidoMock("PEDIDO-5-10", 150.0);
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoMock);

        // Act
        List<Pedido> resultado = pedidoService.agregar(request);

        // Assert
        assertEquals(1, resultado.size());
        assertEquals(150.0, resultado.get(0).getVolumenGLPAsignado());
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
    }

    @Test
    void testPedidoGrandeConDivision() {
        // Arrange
        PedidoRequest request = new PedidoRequest();
        request.setX(10);
        request.setY(5);
        request.setVolumenGLP(1000.0); // Mayor que la capacidad máxima (250)
        request.setHorasLimite(8.0);

        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido pedido = invocation.getArgument(0);
            return pedido;
        });

        // Act
        List<Pedido> resultado = pedidoService.agregar(request);

        // Assert
        assertTrue(resultado.size() > 1, "El pedido debería haberse dividido");
        
        // Verificar que la suma de volúmenes es igual al total
        double volumenTotal = resultado.stream()
                .mapToDouble(Pedido::getVolumenGLPAsignado)
                .sum();
        assertEquals(1000.0, volumenTotal, 0.001);

        // Verificar que ningún pedido excede la capacidad máxima
        for (Pedido pedido : resultado) {
            assertTrue(pedido.getVolumenGLPAsignado() <= 250.0, 
                "Ningún pedido dividido debería exceder la capacidad máxima");
        }

        // Verificar que todos los pedidos tienen códigos únicos
        long codigosUnicos = resultado.stream()
                .map(Pedido::getCodigo)
                .distinct()
                .count();
        assertEquals(resultado.size(), codigosUnicos, "Todos los códigos deben ser únicos");
    }

    @Test
    void testPedidoMuyGrandeMultiplesCiclos() {
        // Arrange
        PedidoRequest request = new PedidoRequest();
        request.setX(10);
        request.setY(5);
        request.setVolumenGLP(2000.0); // Requiere múltiples ciclos
        request.setHorasLimite(8.0);

        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido pedido = invocation.getArgument(0);
            return pedido;
        });

        // Act
        List<Pedido> resultado = pedidoService.agregar(request);

        // Assert
        assertTrue(resultado.size() > 4, "El pedido debería requerir múltiples ciclos");
        
        // Verificar que la suma de volúmenes es igual al total
        double volumenTotal = resultado.stream()
                .mapToDouble(Pedido::getVolumenGLPAsignado)
                .sum();
        assertEquals(2000.0, volumenTotal, 0.001);
    }

    @Test
    void testPedidoConCamionesEnMantenimiento() {
        // Arrange
        DataLoader.camiones = Arrays.asList(
            crearCamion("C001", TipoCamion.TA, 100.0, EstadoCamion.EN_MANTENIMIENTO_PREVENTIVO),
            crearCamion("C002", TipoCamion.TB, 150.0, EstadoCamion.DISPONIBLE)
        );

        PedidoRequest request = new PedidoRequest();
        request.setX(10);
        request.setY(5);
        request.setVolumenGLP(200.0); // Mayor que la capacidad disponible (150)
        request.setHorasLimite(8.0);

        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido pedido = invocation.getArgument(0);
            return pedido;
        });

        // Act
        List<Pedido> resultado = pedidoService.agregar(request);

        // Assert
        assertTrue(resultado.size() > 1, "El pedido debería haberse dividido");
        
        double volumenTotal = resultado.stream()
                .mapToDouble(Pedido::getVolumenGLPAsignado)
                .sum();
        assertEquals(200.0, volumenTotal, 0.001);
    }

    private Camion crearCamion(String codigo, TipoCamion tipo, double capacidad, EstadoCamion estado) {
        return Camion.builder()
                .codigo(codigo)
                .tipo(tipo)
                .capacidadMaximaGLP(capacidad)
                .capacidadActualGLP(capacidad)
                .estado(estado)
                .coordenada(new Coordenada(0, 0))
                .build();
    }

    private Pedido crearPedidoMock(String codigo, double volumen) {
        return Pedido.builder()
                .codigo(codigo)
                .volumenGLPAsignado(volumen)
                .build();
    }
} 