package com.plg.factory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.plg.utils.ExcepcionesPerzonalizadas.InvalidDataFormatException;
import com.plg.entity.Coordenada;
import com.plg.entity.EstadoPedido;
import com.plg.entity.Pedido;
import com.plg.entity.TipoNodo;
import com.plg.utils.Herramientas;

/**
 * Patrón fábrica para crear objetos Pedido con configuración predeterminada.
 */
public class PedidoFactory {

    // Lista estática para almacenar todos los pedidos creados
    public static final List<Pedido> pedidos = new ArrayList<>();

    /**
     * Crea un pedido básico.
     *
     * @param coordenada  Coordenada del pedido
     * @param volumenGLP  Volumen de GLP a entregar (m3)
     * @param horasLimite Horas límite para entrega
     * @return instancia de Pedido con estado REGISTRADO y tipoNodo CLIENTE
     */
    public static Pedido crearPedido(
            Coordenada coordenada,
            double volumenGLP,
            double horasLimite) {

        // Crear el pedido
        Pedido pedido = Pedido.builder()
                // campos heredados de Nodo
                .coordenada(coordenada)
                .bloqueado(false)
                .gScore(0)
                .fScore(0)
                .tipoNodo(TipoNodo.PEDIDO)
                // campos de Pedido
                .codigo("PEDIDO-" + coordenada.getFila() + "-" + coordenada.getColumna())
                .horasLimite(horasLimite)
                .volumenGLPAsignado(volumenGLP)
                .estado(EstadoPedido.REGISTRADO)
                .build();

        // Agregar el pedido a la lista
        pedidos.add(pedido);
        return pedido;
    }

    /**
     * Crea un pedido a partir de una línea de texto y una fecha inicial.
     *
     * @param line         Línea de texto con los datos del pedido
     * @param fechaInicial Fecha inicial para calcular la fecha de registro
     * @return instancia de Pedido creada
     */
    public static Pedido crearPedido(String line) throws InvalidDataFormatException {
        String[] partes = line.split(":");
        if (partes.length != 2) {
            throw new InvalidDataFormatException(
                    "Formato de línea de pedido incorrecto. Se esperaban 2 partes separadas por ':'. Línea: " + line);
        }

        LocalDateTime fechaRegistro = Herramientas.readFecha(partes[0]);

        // Extraer datos del pedido
        String[] datosPedido = partes[1].split(",");
        if (datosPedido.length != 5) { // posx,posY,c-idCliente, m3, hLímite
            throw new InvalidDataFormatException(
                    "Formato de datos del pedido incorrecto. Se esperaban 5 partes separadas por ','. Datos: "
                            + partes[1] + ". Línea: " + line);
        }

        Coordenada coordenada;
        int m3;
        int horaLimite;
        try {
            int posX = Integer.parseInt(datosPedido[0].trim());
            int posY = Integer.parseInt(datosPedido[1].trim());
            coordenada = new Coordenada(posY, posX); // Coordenada(fila, columna) -> (Y,X)

            String m3Str = datosPedido[3].trim();
            if (!m3Str.toLowerCase().endsWith("m3")) {
                throw new InvalidDataFormatException(
                        "Formato de volumen (m3) incorrecto. Debe terminar con 'm3'. Valor: " + m3Str + ". Línea: "
                                + line);
            }
            m3 = Integer.parseInt(m3Str.substring(0, m3Str.toLowerCase().indexOf('m')));

            String horaLimiteStr = datosPedido[4].trim();
            if (!horaLimiteStr.toLowerCase().endsWith("h")) {
                throw new InvalidDataFormatException(
                        "Formato de hora límite (h) incorrecto. Debe terminar con 'h'. Valor: " + horaLimiteStr
                                + ". Línea: " + line);
            }
            horaLimite = Integer.parseInt(horaLimiteStr.substring(0, horaLimiteStr.toLowerCase().indexOf('h')));

            if (m3 <= 0)
                throw new InvalidDataFormatException(
                        "El volumen (m3) debe ser positivo. Valor: " + m3 + ". Línea: " + line);
            if (horaLimite <= 0)
                throw new InvalidDataFormatException(
                        "La hora límite debe ser positiva. Valor: " + horaLimite + ". Línea: " + line);

        } catch (NumberFormatException e) {
            throw new InvalidDataFormatException(
                    "Error al parsear valores numéricos en datos del pedido: " + partes[1] + ". Línea: " + line, e);
        } catch (StringIndexOutOfBoundsException e) {
            throw new InvalidDataFormatException(
                    "Error en el formato de 'm3' o 'hLímite' (faltan caracteres 'm' o 'h' o valor numérico). Datos: "
                            + partes[1] + ". Línea: " + line,
                    e);
        }

        // Crear el pedido
        Pedido pedido = Pedido.builder()
                .coordenada(coordenada)
                .bloqueado(false)
                .gScore(0)
                .fScore(0)
                .tipoNodo(TipoNodo.PEDIDO)
                .codigo("PEDIDO-" + coordenada.getFila() + "-" + coordenada.getColumna())
                .horasLimite((double) horaLimite)
                .volumenGLPAsignado((double) m3)
                .estado(EstadoPedido.REGISTRADO)
                .fechaRegistro(fechaRegistro)
                .fechaLimite(fechaRegistro.plusHours((long)horaLimite))
                .build();
        // NOTA: No agregamos el pedido a la lista aquí, el DataLoader se encarga de esto
        // pedidos.add(pedido);
        return pedido;
    }

}