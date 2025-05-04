package com.plg.config;

import com.plg.entity.*;
import com.plg.utils.Parametros;

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
public class DataLoader {

    private String pathAverias = "data/averias/averias.v1.txt";
    private String pathPedidos = "data/pedidos/ventas202504.txt";
    private String pathMantenimientos = "data/mantenimientos/mantpreventivo.txt";
    private String pathBloqueos = "data/bloqueos/202504.bloqueadas";

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

    // Método génerico para leer fechas del siguiente formato ##d##h##m
    private LocalDateTime readFecha(String fecha) {
        String[] partes = fecha.split("[dhm]");
        Long minutosAcumulados = (Integer.parseInt(partes[0]) - 1) * 24 * 60L +
                Integer.parseInt(partes[1]) * 60L +
                Integer.parseInt(partes[2]);
        return Parametros.getInstance().fecha_inicial.plusMinutes(minutosAcumulados);
    }

    public List<Camion> initializeCamiones() {
        List<Camion> camiones = new ArrayList<>();
        String[] tipos = { "TA", "TB"};
        double[] capacidades = { 25.0, 20.0, 15.0, 10.0 };
    
        double[] taras = { 15.0, 12.0, 9.0, 7.0 };

        Coordenada coordenada = new Coordenada(12, 12);


        for (int i = 0; i < tipos.length; i++) {
            for (int j = 1; j <= 3; j++) {

                double distanciaMaxima = 25 * 180 / (taras[i] + capacidades[i]);

                Camion camion = new Camion();
                camion.setCodigo(tipos[i] + "0" + j);
                camion.setTipo(tipos[i]);
                camion.setCapacidad(capacidades[i]);
                camion.setTara(taras[i]);
                camion.setCoordenada(coordenada);
                camion.setPesoCarga(0);
                camion.setPesoCombinado(taras[i]);
                camion.setEstado(null); 
                camion.setDistanciaMaxima(distanciaMaxima);
                camiones.add(camion);
            }
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
        List<String> lines = readAllLines(pathPedidos);
        int i=0;
        for (String line : lines) {
            String[] partes = line.split(":");
            double m3, h_limite;
            LocalDateTime fecha_registro = readFecha(partes[0]);
            String[] datosPedido = partes[1].split(",");
            Coordenada coordenada = new Coordenada(
                    Integer.parseInt(datosPedido[0]),
                    Integer.parseInt(datosPedido[1]));
            String codigo_cliente = datosPedido[2];
            m3 = Double.parseDouble(datosPedido[3].substring(0, datosPedido[3].indexOf('m')));
            h_limite = Double.parseDouble(datosPedido[4].substring(0, datosPedido[4].indexOf('h')));

            Pedido pedido = Pedido.builder()
                    .id(i++)
                    .codigo(codigo_cliente)
                    .coordenada(coordenada)
                    .horasLimite(h_limite)
                    .fechaRegistro(fecha_registro)
                    .volumenGLPAsignado(m3)
                    .volumenGLPEntregado(0.0)
                    .volumenGLPPendiente(m3)
                    .prioridad(1) // Prioridad por defecto
                    .estado(EstadoPedido.REGISTRADO) // Estado inicial
                    .build();
            pedidos.add(pedido);
        }
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
                            .fecha(fecha)
                            .camion(camion)
                            .build();
                    break;
                }
            }
            mantenimientos.add(mantenimiento);
        }
        return mantenimientos;
    }

    public void initializeBloqueos(Mapa mapa) {
        List<String> lines = readAllLines(pathBloqueos);
        for(String line : lines){
            String[] partes = line.split(":");
            String[] partesFecha = partes[0].split("-");
            LocalDateTime fecha1 = readFecha(partesFecha[0]);
            LocalDateTime fecha2 = readFecha(partesFecha[1]);

            List<Coordenada> coordenadas = new ArrayList<>();
            String[] coordenadasBloqueo = partes[1].split(",");
            if (coordenadasBloqueo.length > 0 && coordenadasBloqueo.length %2 == 0) {
                for(int i = 0; i < coordenadasBloqueo.length; i += 2) {
                    int x = Integer.parseInt(coordenadasBloqueo[i]);
                    int y = Integer.parseInt(coordenadasBloqueo[i + 1]);
                    Coordenada coordenada = new Coordenada(x, y);
                    coordenadas.add(coordenada);
                }
            }else{
                System.out.println("Error en el formato de coordenadas de bloqueo: " + partes[1]);
                return ;
            }
            // Realizamos el bloqueo de los nodos en el mapa


            for(int i=0; i<coordenadas.size()-1; i++){
                Coordenada start = coordenadas.get(i);
                Coordenada end = coordenadas.get(i+1);             
                if(start.getColumna() == end.getColumna()){
                    for(int j = start.getFila(); j <= end.getFila(); j++){
                        Nodo nodo = mapa.getNodo(j, start.getColumna());
                        nodo.setBloqueado(true);
                    }
                }else if(start.getFila() == end.getFila()){
                    for(int j = start.getColumna(); j <= end.getColumna(); j++){
                        Nodo nodo = mapa.getNodo(start.getFila(), j);
                        nodo.setBloqueado(true);
                    }
                }else{
                    System.out.println("Error: Las coordenadas no son válidas para un bloqueo lineal.");
                    return;
                }
            }

        }
    }
}

