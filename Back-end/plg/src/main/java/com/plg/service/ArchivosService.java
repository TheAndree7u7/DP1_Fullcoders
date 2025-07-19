package com.plg.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.plg.dto.DatosVentas;
import com.plg.dto.request.ArchivoPedidosRequest;
import com.plg.dto.response.ArchivoPedidosResponse;
import com.plg.entity.Coordenada;
import com.plg.entity.Pedido;
import com.plg.factory.PedidoFactory;
import com.plg.repository.PedidoRepository;
import com.plg.utils.Herramientas;
import com.plg.utils.Parametros;
import com.plg.utils.ExcepcionesPerzonalizadas.InvalidDataFormatException;

/**
 * Servicio para manejar archivos de pedidos.
 */
@Service
public class ArchivosService {

    private final PedidoRepository pedidoRepository;

    public ArchivosService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    /**
     * Procesa un archivo de pedidos y agrega los pedidos al sistema.
     * 
     * @param request Solicitud con el archivo de pedidos
     * @return Respuesta con los pedidos agregados
     */
    public ArchivoPedidosResponse procesarArchivoPedidos(ArchivoPedidosRequest request) {
        try {
            // Validar el nombre del archivo
            validarNombreArchivo(request.getNombre());

            // Extraer año y mes del nombre del archivo
            int año = extraerAño(request.getNombre());
            int mes = extraerMes(request.getNombre());

            // Actualizar parámetros globales
            Parametros.anho = String.valueOf(año);
            Parametros.mes = String.format("%02d", mes);

            // Procesar cada línea del contenido
            List<Pedido> pedidosAgregados = new ArrayList<>();
            String[] lineas = request.getContenido().split("\n");

            for (String linea : lineas) {
                if (!linea.trim().isEmpty()) {
                    try {
                        // Crear pedido usando el factory existente
                        Pedido pedido = PedidoFactory.crearPedido(linea.trim());

                        // Guardar el pedido
                        Pedido pedidoGuardado = pedidoRepository.save(pedido);
                        pedidosAgregados.add(pedidoGuardado);

                        // Agregar al DataLoader de parámetros
                        Parametros.dataLoader.pedidos.add(pedidoGuardado);

                    } catch (InvalidDataFormatException e) {
                        System.err.println("Error procesando línea: " + linea + " - " + e.getMessage());
                        // Continuar con la siguiente línea
                    }
                }
            }

            // Crear respuesta
            String mensaje = String.format("Archivo '%s' procesado exitosamente. %d pedidos agregados.",
                    request.getNombre(), pedidosAgregados.size());

            return new ArchivoPedidosResponse(
                    request.getNombre(),
                    pedidosAgregados.size(),
                    pedidosAgregados,
                    mensaje);

        } catch (Exception e) {
            throw new RuntimeException("Error procesando archivo de pedidos: " + e.getMessage(), e);
        }
    }

    /**
     * Valida que el nombre del archivo siga el formato correcto.
     * 
     * @param nombreArchivo Nombre del archivo a validar
     * @throws IllegalArgumentException Si el formato no es correcto
     */
    private void validarNombreArchivo(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del archivo no puede estar vacío");
        }

        // Validar formato: ventasYYYYMM.txt
        if (!nombreArchivo.matches("^ventas\\d{6}\\.txt$")) {
            throw new IllegalArgumentException(
                    "El nombre del archivo debe seguir el formato: ventasYYYYMM.txt. " +
                            "Ejemplo: ventas202507.txt. Archivo recibido: " + nombreArchivo);
        }
    }

    /**
     * Extrae el año del nombre del archivo.
     * 
     * @param nombreArchivo Nombre del archivo (formato: ventasYYYYMM.txt)
     * @return Año extraído
     */
    private int extraerAño(String nombreArchivo) {
        try {
            return Integer.parseInt(nombreArchivo.substring(6, 10));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("No se pudo extraer el año del nombre del archivo: " + nombreArchivo);
        }
    }

    /**
     * Extrae el mes del nombre del archivo.
     * 
     * @param nombreArchivo Nombre del archivo (formato: ventasYYYYMM.txt)
     * @return Mes extraído
     */
    private int extraerMes(String nombreArchivo) {
        try {
            return Integer.parseInt(nombreArchivo.substring(10, 12));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("No se pudo extraer el mes del nombre del archivo: " + nombreArchivo);
        }
    }

    /**
     * Procesa pedidos individuales usando los datos parseados.
     * 
     * @param request Solicitud con los datos de pedidos
     * @return Respuesta con los pedidos agregados
     */
    public ArchivoPedidosResponse procesarPedidosIndividuales(ArchivoPedidosRequest request) {
        try {
            // Validar el nombre del archivo
            validarNombreArchivo(request.getNombre());

            // Extraer año y mes del nombre del archivo
            int año = extraerAño(request.getNombre());
            int mes = extraerMes(request.getNombre());

            // Actualizar parámetros globales
            Parametros.anho = String.valueOf(año);
            Parametros.mes = String.format("%02d", mes);

            // Procesar cada dato de ventas
            List<Pedido> pedidosAgregados = new ArrayList<>();

            for (DatosVentas datosVentas : request.getDatos()) {
                try {
                    // Crear coordenada
                    Coordenada coordenada = new Coordenada(datosVentas.getCoordenadaY(), datosVentas.getCoordenadaX());

                    // Crear fecha de registro
                    LocalDateTime fechaRegistro = Herramientas.readFecha(datosVentas.getFechaHora());

                    // Crear pedido usando el factory
                    Pedido pedido = PedidoFactory.crearPedido(
                            coordenada,
                            datosVentas.getVolumenGLP(),
                            datosVentas.getHorasLimite(),
                            fechaRegistro);

                    // Guardar el pedido
                    Pedido pedidoGuardado = pedidoRepository.save(pedido);
                    pedidosAgregados.add(pedidoGuardado);

                    // Agregar al DataLoader de parámetros
                    Parametros.dataLoader.pedidos.add(pedidoGuardado);

                } catch (Exception e) {
                    System.err.println("Error procesando datos de ventas: " + datosVentas + " - " + e.getMessage());
                    // Continuar con el siguiente pedido
                }
            }

            // Crear respuesta
            String mensaje = String.format("Pedidos del archivo '%s' procesados exitosamente. %d pedidos agregados.",
                    request.getNombre(), pedidosAgregados.size());

            return new ArchivoPedidosResponse(
                    request.getNombre(),
                    pedidosAgregados.size(),
                    pedidosAgregados,
                    mensaje);

        } catch (Exception e) {
            throw new RuntimeException("Error procesando pedidos individuales: " + e.getMessage(), e);
        }
    }
}