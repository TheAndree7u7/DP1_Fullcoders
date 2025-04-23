package com.plg.simulator;

import com.plg.model.*;

import java.util.ArrayList;
import java.util.List;

public class TestData {

    public static List<Camion> obtenerCamionesDePrueba() {
        List<Camion> camiones = new ArrayList<>();
        camiones.add(new Camion("TA01", "TA", 2.5, 25, 12.5));
        camiones.add(new Camion("TB01", "TB", 2.0, 15, 7.5));
        camiones.add(new Camion("TC01", "TC", 1.5, 10, 5.0));
        camiones.add(new Camion("TD01", "TD", 1.0, 5, 2.5));
        return camiones;
    }

    public static List<Pedido> obtenerPedidosDePrueba() {
        List<Pedido> pedidos = new ArrayList<>();
        pedidos.add(new Pedido("c-101", new Coordenada(10, 10), 5, 1, 8, 30, 6));
        pedidos.add(new Pedido("c-102", new Coordenada(15, 12), 10, 1, 9, 15, 4));
        pedidos.add(new Pedido("c-103", new Coordenada(20, 20), 15, 1, 10, 0, 8));
        pedidos.add(new Pedido("c-104", new Coordenada(25, 18), 20, 1, 10, 30, 12));
        pedidos.add(new Pedido("c-105", new Coordenada(5, 5), 8, 1, 11, 0, 5));
        pedidos.add(new Pedido("c-106", new Coordenada(30, 30), 25, 1, 11, 45, 10));
        return pedidos;
    }
}
