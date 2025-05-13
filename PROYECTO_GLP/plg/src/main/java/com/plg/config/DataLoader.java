package com.plg.config;

import com.plg.entity.*;
import com.plg.utils.Herramientas;
import com.plg.utils.Parametros;

import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Component
public class DataLoader {

    private static String pathAverias = "data/averias/averias.v1.txt";
    private static String pathPedidos = "data/pedidos/ventas" + Parametros.anho +  Parametros.mes + ".txt";
    private static String pathMantenimientos = "data/mantenimientos/mantpreventivo.txt";
    private static String pathBloqueos = "data/bloqueos/" + Parametros.anho + Parametros.mes + ".bloqueos.txt";

    private static Mapa mapa = Mapa.getInstance();
    private static Coordenada coordenadaCentral = new Coordenada(8, 12); 
    
    public static final List<Mantenimiento> mantenimientos = new ArrayList<>();
    public static List<Pedido> pedidos = new ArrayList<>();
    public static List<Almacen> almacenes = new ArrayList<>();
    public static List<Camion> camiones = new ArrayList<>();
    public static final List<Averia> averias = new ArrayList<>();
    public static final List<Bloqueo> bloqueos = new ArrayList<>();

    public static List<Almacen> initializeAlmacenes() {
        AlmacenFactory.crearAlmacen(TipoAlmacen.CENTRAL, coordenadaCentral, 1_000_000_000,
                1_000_000_000);
        AlmacenFactory.crearAlmacen(TipoAlmacen.SECUNDARIO, new Coordenada(42, 42), 160.0, 0.0);
        AlmacenFactory.crearAlmacen(TipoAlmacen.SECUNDARIO, new Coordenada(3, 63), 160.0, 0.0);
        almacenes = AlmacenFactory.almacenes;
        return almacenes;
    }

    public static List<Camion> initializeCamiones() {
        for (int i=0; i<2; i++){
            CamionFactory.crearCamionesPorTipo(TipoCamion.TA, true, coordenadaCentral);
        }
        for (int i=0; i<4; i++){
            CamionFactory.crearCamionesPorTipo(TipoCamion.TB, true, coordenadaCentral);
        }
        for (int i=0; i<4; i++){
            CamionFactory.crearCamionesPorTipo(TipoCamion.TC, true, coordenadaCentral);
        }
        for (int i=0; i<10; i++){
            CamionFactory.crearCamionesPorTipo(TipoCamion.TD, true, coordenadaCentral);
        }
        camiones = CamionFactory.camiones;
        return camiones;
    }

    public static List<Averia> initializeAverias() {
        List<String> lines = Herramientas.readAllLines(pathAverias);
        for (String line : lines) {
            Averia averia = new Averia(line);  
            averias.add(averia);
        }
        return averias;
    }

    public static List<Pedido> initializePedidos() {
        List<String> lines = Herramientas.readAllLines(pathPedidos);
        for(String line: lines){
            PedidoFactory.crearPedido(line);
        }
        pedidos = PedidoFactory.pedidos;
        return pedidos;
    }

    public static List<Mantenimiento> initializeMantenimientos() {
        List<String> lines = Herramientas.readAllLines(pathMantenimientos);
        for (String line : lines) {
        
            String[] partes = line.split(":");
            String fechaSinAnio = partes[0].substring(4);
            int mes = Integer.parseInt(fechaSinAnio.substring(0, 2));
            int dia = Integer.parseInt(fechaSinAnio.substring(2));
            String codigo = partes[1].substring(0, 4);
            Camion camion = camiones.stream()
                    .filter(c -> c.getCodigo().equals(codigo))
                    .findFirst()
                    .orElse(null);
            int ini, fin;
            if (mes %2 == 0){
                ini = 2;
                fin = 12;
            } else { 
                ini = 1;
                fin = 11;
            }
            for (int i = ini; i <= fin; i += 2) {
                Mantenimiento mantenimiento = Mantenimiento.builder()
                        .dia(dia)
                        .mes(i)
                        .camion(camion)
                        .build();
                mantenimientos.add(mantenimiento);
            }  
        }
        return mantenimientos;
    }



    public static void initializeBloqueos() {
        List<String> lines = Herramientas.readAllLines(pathBloqueos);
        for (String line : lines) {
            Bloqueo bloqueo = new Bloqueo(line);
            bloqueos.add(bloqueo);
        }
    }
}
