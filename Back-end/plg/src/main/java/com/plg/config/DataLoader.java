package com.plg.config;

import com.plg.entity.*;
import com.plg.factory.AlmacenFactory;
import com.plg.factory.CamionFactory;
import com.plg.factory.PedidoFactory;
import com.plg.utils.Herramientas;
import com.plg.utils.Parametros;
import com.plg.utils.ExcepcionesPerzonalizadas.InvalidDataFormatException;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class DataLoader {

    private String pathAverias = "data/averias/averias.v1.txt";
    private String pathMantenimientos = "data/mantenimientos/mantpreventivo.txt";

    // Métodos para generar paths dinámicamente basándose en parámetros actuales
    private String getPathPedidos() {
        return "data/pedidos/ventas" + Parametros.anho + Parametros.mes + ".txt";
    }

    private String getPathBloqueos() {
        return "data/bloqueos/" + Parametros.anho + Parametros.mes + ".bloqueos.txt";
    }

    private Coordenada coordenadaCentral = new Coordenada(8, 12);

    public final List<Mantenimiento> mantenimientos = new ArrayList<>();
    public List<Pedido> pedidos = new ArrayList<>();
    public List<Almacen> almacenes = new ArrayList<>();
    public List<Camion> camiones = new ArrayList<>();
    public List<Averia> averias = new ArrayList<>();
    public List<Bloqueo> bloqueos = new ArrayList<>();

    public DataLoader() {
        initializeAlmacenes();
        initializeCamiones();
        try {
            initializePedidos();
            initializeMantenimientos();
            initializeAverias();
            initializeBloqueos();
        } catch (InvalidDataFormatException | IOException e) {
            // Manejo simple: imprimir el error, puedes personalizar según tus necesidades
            e.printStackTrace();
        }
    }

    public List<Almacen> initializeAlmacenes() {
        Almacen central = AlmacenFactory.crearAlmacen(TipoAlmacen.CENTRAL, coordenadaCentral, 1_000_000_000,
                1_000_000_000);
        Almacen secundario1 = AlmacenFactory.crearAlmacen(TipoAlmacen.SECUNDARIO, new Coordenada(42, 42), 160.0, 50);
        Almacen secundario2 = AlmacenFactory.crearAlmacen(TipoAlmacen.SECUNDARIO, new Coordenada(3, 63), 160.0, 50);
        this.almacenes.add(central);
        this.almacenes.add(secundario1);
        this.almacenes.add(secundario2);
        return this.almacenes;
    }

    public List<Camion> initializeCamiones() {
        CamionFactory.limpiarFactory(); 
        for (int i = 0; i < 2; i++) {
            Camion camion = CamionFactory.crearCamionesPorTipo(TipoCamion.TA, true, coordenadaCentral);
            this.camiones.add(camion);
        }
        for (int i = 0; i < 4; i++) {
            Camion camion = CamionFactory.crearCamionesPorTipo(TipoCamion.TB, true, coordenadaCentral);
            this.camiones.add(camion);
        }
        for (int i = 0; i < 4; i++) {
            Camion camion = CamionFactory.crearCamionesPorTipo(TipoCamion.TC, true, coordenadaCentral);
            this.camiones.add(camion);
        }
        for (int i = 0; i < 10; i++) {
            Camion camion = CamionFactory.crearCamionesPorTipo(TipoCamion.TD, true, coordenadaCentral);
            this.camiones.add(camion);
        }
        return this.camiones;
    }

    public List<Averia> initializeAverias() throws InvalidDataFormatException, IOException {
        List<String> lines = Herramientas.readAllLines(pathAverias);
        for (String line : lines) {
            Averia averia = new Averia(line);
            this.averias.add(averia);
        }
        return this.averias;
    }

    public List<Pedido> initializePedidos() throws InvalidDataFormatException, IOException {
        List<String> lines = Herramientas.readAllLines(getPathPedidos());

        for (String line : lines) {
            Pedido pedido = PedidoFactory.crearPedido(line);
            this.pedidos.add(pedido);
        }

        return this.pedidos;
    }

    public List<Mantenimiento> initializeMantenimientos()
            throws IOException, InvalidDataFormatException {
        List<String> lines = Herramientas.readAllLines(pathMantenimientos); // Propaga IOException si ocurre

        for (String line : lines) {

            String[] partes = line.split(":");
            if (partes.length != 2) {
                throw new InvalidDataFormatException(
                        "Error de formato en línea de mantenimiento: Se esperaban 2 partes separadas por ':'. Línea: "
                                + line);
            }

            String fechaCompleta = partes[0]; // aaaammdd
            String codigoTipoCamion = partes[1]; // TTNN (Tipo y Número, ej. TA01)

            if (fechaCompleta.length() != 8) {
                throw new InvalidDataFormatException(
                        "Error de formato en fecha de mantenimiento: Se esperaba formato aaaammdd. Valor: "
                                + fechaCompleta + ". Línea: " + line);
            }
            if (codigoTipoCamion.length() != 4) { // Asumiendo TTNN donde TT es tipo y NN es número.
                throw new InvalidDataFormatException(
                        "Error de formato en código/tipo de camión de mantenimiento: Se esperaba formato TTNN. Valor: "
                                + codigoTipoCamion + ". Línea: " + line);
            }

            int mes, dia;
            try {
                // anio = Integer.parseInt(fechaCompleta.substring(0, 4)); // Año no se usa
                mes = Integer.parseInt(fechaCompleta.substring(4, 6));
                dia = Integer.parseInt(fechaCompleta.substring(6, 8));
            } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
                throw new InvalidDataFormatException("Error al parsear fecha de mantenimiento: " + fechaCompleta
                        + ". Línea: " + line + ". Detalles: " + e.getMessage());
            }

            Camion camion;
            try {
                camion = CamionFactory.getCamionPorCodigo(codigoTipoCamion);

            } catch (NoSuchElementException e) {
                throw new InvalidDataFormatException(
                        "Error en línea de mantenimiento: " + line + ". Detalles: " + e.getMessage());
            }

            int ini, fin;
            if (mes % 2 == 0) {
                ini = mes;
                fin = 12;
            } else {
                ini = mes;
                fin = 11;
            }

            for (int i = ini; i <= fin; i += 2) {
                Mantenimiento mantenimiento = Mantenimiento.builder()
                        .dia(dia)
                        .mes(i)
                        .camion(camion)
                        .build();
                this.mantenimientos.add(mantenimiento);
            }
        }
        return this.mantenimientos;
    }

    public void initializeBloqueos() throws InvalidDataFormatException, IOException {
        List<String> lines = Herramientas.readAllLines(getPathBloqueos());
        for (String line : lines) {
            Bloqueo bloqueo = new Bloqueo(line);
            this.bloqueos.add(bloqueo);
        }
    }
}