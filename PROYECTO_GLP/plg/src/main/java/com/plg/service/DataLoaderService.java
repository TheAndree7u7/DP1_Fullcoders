package com.plg.service;

import com.plg.model.*;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DataLoaderService {

    public List<Almacen> cargarAlmacenes() {
        List<Almacen> almacenes = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream("data/almacenes/almacenes.txt")))) {
            String line;
            while ((line = br.readLine()) != null) {
                almacenes.add(new Almacen(line));
            }
        } catch (Exception e) {
            System.err.println("Error cargando almacenes: " + e.getMessage());
        }
        return almacenes;
    }

    public List<Camion> cargarCamiones() {
        List<Camion> camiones = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream("data/camiones/camiones.txt")))) {
            String line;
            boolean primeraLinea = true;
            while ((line = br.readLine()) != null) {
                if (primeraLinea) { // saltar headers
                    primeraLinea = false;
                    continue;
                }
                camiones.add(new Camion(line));
            }
        } catch (Exception e) {
            System.err.println("Error cargando camiones: " + e.getMessage());
        }
        return camiones;
    }

    public List<Averia> cargarAverias() {
        List<Averia> averias = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream("data/averias/averias.v1.txt")))) {
            String line;
            while ((line = br.readLine()) != null) {
                averias.add(new Averia(line));
            }
        } catch (Exception e) {
            System.err.println("Error cargando averias: " + e.getMessage());
        }
        return averias;
    }

    public List<Bloqueo> cargarBloqueos() {
        List<Bloqueo> bloqueos = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream("data/bloqueos/202504.bloqueadas")))) {
            String line;
            while ((line = br.readLine()) != null) {
                bloqueos.add(new Bloqueo(line));
            }
        } catch (Exception e) {
            System.err.println("Error cargando bloqueos: " + e.getMessage());
        }
        return bloqueos;
    }

    public List<Mantenimiento> cargarMantenimientos() {
        List<Mantenimiento> mantenimientos = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream("data/mantenimientos/mantpreventivo.txt")))) {
            String line;
            while ((line = br.readLine()) != null) {
                mantenimientos.add(new Mantenimiento(line));
            }
        } catch (Exception e) {
            System.err.println("Error cargando mantenimientos: " + e.getMessage());
        }
        return mantenimientos;
    }

    public List<Pedido> cargarPedidos() {
        List<Pedido> pedidos = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream("data/pedidos/ventas202504.txt")))) {
            String line;
            LocalDateTime fechaInicial = LocalDateTime.of(2025, 4, 1, 0, 0);
            while ((line = br.readLine()) != null) {
                pedidos.add(new Pedido(line, fechaInicial));
            }
        } catch (Exception e) {
            System.err.println("Error cargando pedidos: " + e.getMessage());
        }
        return pedidos;
    }

}
