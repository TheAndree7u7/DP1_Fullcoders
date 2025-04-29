package com.plg.config;

import com.plg.entity.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.plg.utils.Parametros;

@Component
public class DataLoader {

    private String pathAverias = "data/averias/averias.v1.txt";
    private String pathPedidos = "data/pedidos/ventas202504.txt";
    private String pathMantenimientos = "data/mantenimientos/mantpreventivo.txt";
    private String pathBloqueos = "data/bloqueos/bloqueos.v1.txt";

    public List<Camion> initializeCamiones() {
        List<Camion> camiones = new ArrayList<>();

        // Create different truck types
        String[] tipos = { "TA", "TB", "TC", "TD" };
        double[] capacidades = { 25.0, 20.0, 15.0, 10.0 };
        double[] taras = { 15.0, 12.0, 9.0, 7.0 };

        for (int i = 0; i < tipos.length; i++) {
            for (int j = 1; j <= 3; j++) {
                Camion camion = new Camion();
                camion.setCodigo(tipos[i] + "0" + j);
                camion.setTipo(tipos[i]);
                camion.setCapacidad(capacidades[i]);
                camion.setTara(taras[i]);
                camion.setPesoCarga(0);
                camion.setPesoCombinado(taras[i]);
                camion.setEstado(null); // Set to null for now
                camiones.add(camion);
            }
        }
        return camiones;
    }

    public List<Averia> initializeAverias(List<Camion> camiones) {
        List<Averia> averias = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream(pathAverias)))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] partes = line.split("_");
                String turno = partes[0];
                String codigoCamion = partes[1];
                String tipoIncidente = partes[2];
                TipoTurno tipoTurno = new TipoTurno(turno);
                TipoIncidente tipoIncidenteObj = new TipoIncidente(tipoIncidente);

                Camion camion = camiones.stream()
                        .filter(c -> c.getCodigo().equals(codigoCamion))
                        .findFirst()
                        .orElse(null);

                Averia averia = Averia.builder()
                        .camion(camion)
                        .turno(tipoTurno)
                        .tipoIncidente(tipoIncidenteObj)
                        .build();
                averias.add(averia);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return averias;
    }

    public List<Pedido> initializePedidos() {
        List<Pedido> pedidos = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream(pathPedidos)))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] partes = line.split(":");
                double m3, h_limite;

                String[] horaRegistro = partes[0].split("[dhm]");
                Long l = (Integer.parseInt(horaRegistro[0]) - 1) * 24 * 60L +
                        Integer.parseInt(horaRegistro[1]) * 60L +
                        Integer.parseInt(horaRegistro[2]);

                LocalDateTime fecha_registro = Parametros.getInstance().fecha_inicial.plusMinutes(l);

                String[] datosPedido = partes[1].split(",");
                Coordenada coordenada = new Coordenada(Integer.parseInt(datosPedido[0]),
                        Integer.parseInt(datosPedido[1]));
                String codigo_cliente = datosPedido[2];
                m3 = (double) (Integer.parseInt(datosPedido[3].substring(0, datosPedido[3].indexOf('m'))));
                h_limite = (double) Integer.parseInt(datosPedido[4].substring(0, datosPedido[4].indexOf('h')));

                Pedido pedido = Pedido.builder()
                        .codigo(codigo_cliente)
                        .coordenada(coordenada)
                        .horasLimite(h_limite)
                        .fechaRegistro(fecha_registro)
                        .volumenGLPAsignado(m3)
                        .volumenGLPEntregado(0.0)
                        .volumenGLPPendiente(m3)
                        .prioridad(1) // Asignar prioridad por defecto
                        .estado(EstadoPedido.REGISTRADO) // Estado inicial
                        .build();
                pedidos.add(pedido);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pedidos;

    }

    public List<Mantenimiento> initializeMantenimientos(List<Camion> camiones) {
        List<Mantenimiento> mantenimientos = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream(pathMantenimientos)))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] partes = line.split(":");
                String fechaSinAnio = partes[0].substring(4);
                int mes = Integer.parseInt(fechaSinAnio.substring(0, 2));
                int dia = Integer.parseInt(fechaSinAnio.substring(2));
                int mesInicial = Parametros.getInstance().fecha_inicial.getMonthValue();
    
                LocalDateTime fecha = Parametros.getInstance().fecha_inicial.plusDays(dia - 1).plusMonths(mes - mesInicial);
    
                String codigo = partes[1].substring(0, 4);
                Mantenimiento mantenimiento = null;
                for (Camion camion : camiones){
                    if (camion.getCodigo().equals(codigo)){
                        mantenimiento = Mantenimiento.builder()
                                .fecha(fecha)
                                .camion(camion)
                                .build();
                        break;
                    }
                }
                mantenimientos.add(mantenimiento);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mantenimientos;
    }


    public void initializarBloqueos(Mapa mapa) {
       
    }
}

