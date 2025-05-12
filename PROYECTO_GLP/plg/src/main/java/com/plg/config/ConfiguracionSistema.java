package com.plg.config;

import com.plg.entity.*;
import com.plg.utils.Parametros;
import com.plg.entity.PedidoFactory;

import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ConfiguracionSistema {

    private String pathAverias = "data/averias/averias.v1.txt";
    private String pathPedidos = "data/pedidos/ventas202504.txt";
    private String pathMantenimientos = "data/mantenimientos/mantpreventivo.txt";
    private String pathBloqueos = "data/bloqueos/202504.bloqueadas";

    private Mapa mapa = Mapa.getInstance();

    private Coordenada coordenadaCentral = new Coordenada(23, 26); 
    

    // Método genérico para leer todas las líneas de un archivo de recursos
    private List<String> readAllLines(String resourcePath) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream(resourcePath)))) {
            return reader.lines().collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public List<Almacen> initializeAlmacenes() {
        List<Almacen> almacenes = new ArrayList<>();
        // Almacen central
        Almacen almacenCentral = AlmacenFactory.crearAlmacen(TipoAlmacen.CENTRAL, coordenadaCentral, 1_000_000_000,
                1_000_000_000);
        Almacen almacen1 = AlmacenFactory.crearAlmacen(TipoAlmacen.SECUNDARIO, new Coordenada(19, 3), 1000.0, 1000.0);
        Almacen almacen2 = AlmacenFactory.crearAlmacen(TipoAlmacen.SECUNDARIO, new Coordenada(3, 19), 1000.0, 1000.0);

        // Agregamos los almacenes a la lista
        almacenes.add(almacenCentral);
        almacenes.add(almacen1);
        almacenes.add(almacen2);

        // Actualizamos el mapa con los almacenes
        mapa.setNodo(almacenCentral.getCoordenada(), almacenCentral);
        mapa.setNodo(almacen1.getCoordenada(), almacen1);
        mapa.setNodo(almacen2.getCoordenada(), almacen2);

        return almacenes;
    }

    // Método génerico para leer fechas del siguiente formato ##d##h##m
    private LocalDateTime readFecha(String fecha) {
        String[] partes = fecha.split("[dhm]");
        Long minutosAcumulados = (Integer.parseInt(partes[0]) - 1) * 24 * 60L +
                Integer.parseInt(partes[1]) * 60L +
                Integer.parseInt(partes[2]);
        return Parametros.getInstance().fecha_inicial.plusMinutes(minutosAcumulados);
    }

    public List<Camion> initializeCamiones() {

        // Camiones operativos
        List<Camion> camiones = new ArrayList<>();
        camiones.add(CamionFactory.crearCamionesPorTipo(TipoCamion.TA,
                true,
                coordenadaCentral));
        camiones.add(CamionFactory.crearCamionesPorTipo(TipoCamion.TB,
                true,
                coordenadaCentral));
        camiones.add(CamionFactory.crearCamionesPorTipo(TipoCamion.TC,
                true,
                coordenadaCentral));
        camiones.add(CamionFactory.crearCamionesPorTipo(TipoCamion.TD,
                true,
                coordenadaCentral));


        List<Camion> camionesAveriados = new ArrayList<>();
        camionesAveriados.add(CamionFactory.crearCamionesPorTipo(TipoCamion.TA,
                false,
                new Coordenada(10, 7)));
        camionesAveriados.add(CamionFactory.crearCamionesPorTipo(TipoCamion.TB,
                false,
                new Coordenada(5, 7)));
        camionesAveriados.add(CamionFactory.crearCamionesPorTipo(TipoCamion.TC,
                false,
                new Coordenada(5, 18)));

        // Actualizamos el mapa con los camiones averiados
        for (Camion camion : camionesAveriados) {
            mapa.setNodo(camion.getCoordenada(), camion);
        }
        return camiones;
    }

    public List<Averia> initializeAverias(List<Camion> camiones) {
        List<Averia> averias = new ArrayList<>();
        List<String> lines = readAllLines(pathAverias);
        for (String line : lines) {
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
        return averias;
    }

    public List<Pedido> initializePedidos() {
        List<Pedido> pedidos = new ArrayList<>();
        Pedido pedido1 = PedidoFactory.crearPedido(
                new Coordenada(38, 1),
                5,
                10.0);
        Pedido pedido2 = PedidoFactory.crearPedido(
                new Coordenada(1, 1),
                5,
                10.0);
        Pedido pedido3 = PedidoFactory.crearPedido(
                new Coordenada(1, 48),
                5,
                10.0);
        Pedido pedido4 = PedidoFactory.crearPedido(
                new Coordenada(38, 48),
                5,
                10.0);
        Pedido pedido5 = PedidoFactory.crearPedido(
                new Coordenada(20, 43),
                5,
                10.0);

        // Agregamos a la lista de pedidos
        pedidos.add(pedido1);
        pedidos.add(pedido2);
        pedidos.add(pedido3);
        pedidos.add(pedido4);
        pedidos.add(pedido5);

        // Actualizamos el mapa con los pedidos
        mapa.setNodo(pedido1.getCoordenada(), pedido1);
        mapa.setNodo(pedido2.getCoordenada(), pedido2);
        mapa.setNodo(pedido3.getCoordenada(), pedido3);
        mapa.setNodo(pedido4.getCoordenada(), pedido4);
        mapa.setNodo(pedido5.getCoordenada(), pedido5);

        return pedidos;
    }

    public List<Mantenimiento> initializeMantenimientos(List<Camion> camiones) {
        List<Mantenimiento> mantenimientos = new ArrayList<>();
        List<String> lines = readAllLines(pathMantenimientos);
        for (String line : lines) {
            String[] partes = line.split(":");
            String fechaSinAnio = partes[0].substring(4);
            int mes = Integer.parseInt(fechaSinAnio.substring(0, 2));
            int dia = Integer.parseInt(fechaSinAnio.substring(2));
            int mesInicial = Parametros.getInstance().fecha_inicial.getMonthValue();
            LocalDateTime fecha = Parametros.getInstance().fecha_inicial.plusDays(dia - 1)
                    .plusMonths(mes - mesInicial);
            String codigo = partes[1].substring(0, 4);
            Mantenimiento mantenimiento = null;
            for (Camion camion : camiones) {
                if (camion.getCodigo().equals(codigo)) {
                    mantenimiento = Mantenimiento.builder()
                            .dia(dia)
                            .mes(mes)
                            .camion(camion)
                            .build();
                    break;
                }
            }
            mantenimientos.add(mantenimiento);
        }
        return mantenimientos;
    }

    public void initializeBloqueos() {
        List<String> lines = readAllLines(pathBloqueos);
        for (String line : lines) {
            String[] partes = line.split(":");
            String[] partesFecha = partes[0].split("-");
            LocalDateTime fecha1 = readFecha(partesFecha[0]);
            LocalDateTime fecha2 = readFecha(partesFecha[1]);

            List<Coordenada> coordenadas = new ArrayList<>();
            String[] coordenadasBloqueo = partes[1].split(",");
            if (coordenadasBloqueo.length > 0 && coordenadasBloqueo.length % 2 == 0) {
                for (int i = 0; i < coordenadasBloqueo.length; i += 2) {
                    int x = Integer.parseInt(coordenadasBloqueo[i]);
                    int y = Integer.parseInt(coordenadasBloqueo[i + 1]);
                    Coordenada coordenada = new Coordenada(x, y);
                    coordenadas.add(coordenada);
                }
            } else {
                System.out.println("Error en el formato de coordenadas de bloqueo: " + partes[1]);
                return;
            }
            // Realizamos el bloqueo de los nodos en el mapa

            // for (int i = 0; i < coordenadas.size() - 1; i++) {
            //     Coordenada start = coordenadas.get(i);
            //     Coordenada end = coordenadas.get(i + 1);
            //     mapa.crearBloqueo(start, end);
            // }

        }
    }
}
