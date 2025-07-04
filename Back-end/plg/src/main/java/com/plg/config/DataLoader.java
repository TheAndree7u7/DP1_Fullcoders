package com.plg.config;

import com.plg.entity.*;
import com.plg.factory.AlmacenFactory;
import com.plg.factory.CamionFactory;
import com.plg.factory.PedidoFactory;
import com.plg.utils.Herramientas;
import com.plg.utils.Parametros;
import com.plg.utils.ExcepcionesPerzonalizadas.InvalidDataFormatException;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Component
public class DataLoader {

    private static String pathAverias = "data/averias/averias.v1.txt";
    private static String pathMantenimientos = "data/mantenimientos/mantpreventivo.txt";
    
    // Métodos para generar paths dinámicamente basándose en parámetros actuales
    private static String getPathPedidos() {
        return "data/pedidos/ventas" + Parametros.anho + Parametros.mes + ".txt";
    }
    
    private static String getPathBloqueos() {
        return "data/bloqueos/" + Parametros.anho + Parametros.mes + ".bloqueos.txt";
    }

    private static Coordenada coordenadaCentral = new Coordenada(8, 12);

    public static final List<Mantenimiento> mantenimientos = new ArrayList<>();
    public static List<Pedido> pedidos = new ArrayList<>();
    public static List<Almacen> almacenes = new ArrayList<>();
    public static List<Camion> camiones = new ArrayList<>();
    public static List<Averia> averias = new ArrayList<>();
    public static List<Bloqueo> bloqueos = new ArrayList<>();

    public static List<Almacen> initializeAlmacenes() {
        AlmacenFactory.crearAlmacen(TipoAlmacen.CENTRAL, coordenadaCentral, 1_000_000_000,
                1_000_000_000);
        AlmacenFactory.crearAlmacen(TipoAlmacen.SECUNDARIO, new Coordenada(42, 42), 160.0, 50);
        AlmacenFactory.crearAlmacen(TipoAlmacen.SECUNDARIO, new Coordenada(3, 63), 160.0, 50);
        almacenes = AlmacenFactory.almacenes;
        return almacenes;
    }

    public static List<Camion> initializeCamiones() {
        for (int i = 0; i < 2; i++) {
            CamionFactory.crearCamionesPorTipo(TipoCamion.TB, true, coordenadaCentral);
        }
        for (int i = 0; i < 4; i++) {
            CamionFactory.crearCamionesPorTipo(TipoCamion.TB, true, coordenadaCentral);
        }
        for (int i = 0; i < 4; i++) {
            CamionFactory.crearCamionesPorTipo(TipoCamion.TC, true, coordenadaCentral);
        }
        for (int i = 0; i < 10; i++) {
            CamionFactory.crearCamionesPorTipo(TipoCamion.TD, true, coordenadaCentral);
        }
        camiones = CamionFactory.camiones;
        return camiones;
    }

    public static List<Averia> initializeAverias() throws InvalidDataFormatException, IOException {
        List<String> lines = Herramientas.readAllLines(pathAverias);
        for (String line : lines) {
            Averia averia = new Averia(line);
 
        }
        return averias;
    }

    public static List<Pedido> initializePedidos() throws InvalidDataFormatException, IOException {
        List<String> lines = Herramientas.readAllLines(getPathPedidos());
        for (String line : lines) {
            PedidoFactory.crearPedido(line);
        }
        pedidos = PedidoFactory.pedidos;
        return pedidos;
    }

    /**
     * Inicializa la lista de mantenimientos a partir del archivo de mantenimientos.
     *
     * @return Lista de mantenimientos inicializados.
     * @throws IOException                si ocurre un error al leer el archivo.
     * @throws InvalidDataFormatException si el formato de los datos es inválido.
     */
    public static List<Mantenimiento> initializeMantenimientos()
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
                mantenimientos.add(mantenimiento);
            }
        }
        return mantenimientos;
    }

    public static void initializeBloqueos() throws InvalidDataFormatException, IOException {
        List<String> lines = Herramientas.readAllLines(getPathBloqueos());
        for (String line : lines) {
            Bloqueo bloqueo = new Bloqueo(line);
            bloqueos.add(bloqueo);
        }
    }
}
