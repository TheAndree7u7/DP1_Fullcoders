package com.plg.utils.simulacion;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.plg.config.DataLoader;
import com.plg.dto.IndividuoDto;
import com.plg.dto.request.AveriaConEstadoRequest;
import com.plg.entity.Almacen;
import com.plg.entity.Bloqueo;
import com.plg.entity.Camion;
import com.plg.entity.Coordenada;
import com.plg.entity.EstadoPedido;
import com.plg.entity.Pedido;
import com.plg.repository.AlmacenRepository;
import com.plg.repository.CamionRepository;
import com.plg.utils.AlgoritmoGenetico;
import com.plg.utils.Individuo;
import com.plg.utils.Parametros;
import com.plg.utils.Simulacion;
import com.plg.utils.TipoIndividuo;

/**
 * Clase para manejar las averías y la creación de paquetes parche.
 * Proporciona funcionalidad para procesar averías y generar nuevos paquetes
 * después de una avería.
 */
public class AveriaManager {

    private static CamionRepository camionRepository = new CamionRepository();
    private static AlmacenRepository almacenRepository = new AlmacenRepository();

    /**
     * Crea un paquete parche después de una avería.
     * 
     * @param estadoCapturado   Estado de la simulación capturado desde el frontend
     * @param fechaInicioParche Fecha de inicio del parche
     * @param fechaFinParche    Fecha de fin del parche
     * @param pedidosSemanal    Lista de pedidos semanales
     * @param fechaActual       Fecha actual de la simulación
     */
    public static void crearPaqueteParche(
            AveriaConEstadoRequest.EstadoSimulacion estadoCapturado,
            LocalDateTime fechaInicioParche,
            LocalDateTime fechaFinParche,
            List<Pedido> pedidosSemanal,
            LocalDateTime fechaActual) {
        System.out.println("🩹 ===================GENERANDO PAQUETE PARCHE  : ===================");
        // Actualizar posiciones de camiones usando datos del frontend
        if (estadoCapturado.getCamiones() != null) {
            System.out.println("🚛 Actualizando posiciones de " + estadoCapturado.getCamiones().size() + " camiones");
            actualizarCamionesDesdeEstadoCapturado(estadoCapturado.getCamiones());
        }

        // Actualizar almacenes usando datos del frontend
        if (estadoCapturado.getAlmacenes() != null) {
            System.out.println("🏪 Actualizando " + estadoCapturado.getAlmacenes().size() + " almacenes");
            actualizarAlmacenesDesdeEstadoCapturado(estadoCapturado.getAlmacenes());
        }

        System.out.println("PEDIDOS DEL PARCHE: " + fechaInicioParche + " - " + fechaFinParche);

        List<Pedido> pedidosEnviar = pedidosSemanal.stream()
                .filter(pedido -> pedido.getFechaRegistro().isAfter(fechaInicioParche)
                        && pedido.getFechaRegistro().isBefore(fechaFinParche))
                .collect(Collectors.toList());

        System.out.println("🔍 DIAGNÓSTICO: Posiciones DESPUÉS de actualizar desde frontend:");
        Camion.imprimirDatosCamiones(DataLoader.camiones);

        // Numero de pedidos antes de filtrar
        System.out.println("NUmero de pedidos antes de filtrar: " + pedidosEnviar.size());

        // Filtra los pedidos con el estado ENTREGADO
        pedidosEnviar = pedidosEnviar.stream()
                .filter(pedido -> pedido.getEstado().equals(EstadoPedido.ENTREGADO))
                .collect(Collectors.toList());

        // NUmero de pedidos en el nuevo rango
        System.out.println("NUmero de pedidos en el nuevo rango: " + pedidosEnviar.size());

        // todos los pedidos en el nuevo rango
        List<Bloqueo> bloqueosActivos = EstadoManager.actualizarBloqueos(fechaActual);

        System.out.println("Tiempo actual: " + fechaActual);

        try {
            AlgoritmoGenetico algoritmoGenetico = new AlgoritmoGenetico(com.plg.entity.Mapa.getInstance(),
                    pedidosEnviar);
            algoritmoGenetico.ejecutarAlgoritmo();

            IndividuoDto mejorIndividuoDto = new IndividuoDto(algoritmoGenetico.getMejorIndividuo(),
                    pedidosEnviar, bloqueosActivos, fechaActual);

            System.out.println("🔍 DIAGNÓSTICO: Posiciones DESPUÉS de ejecutar algoritmo genético:");
            Camion.imprimirDatosCamiones(DataLoader.camiones);

            mejorIndividuoDto.setFechaHoraInicioIntervalo(fechaInicioParche);
            mejorIndividuoDto.setFechaHoraFinIntervalo(fechaFinParche);
            mejorIndividuoDto.setTipoIndividuo(TipoIndividuo.PARCHE_AVERIA);
            // Agregar al historial para el frontend
            GestorHistorialSimulacion.agregarPaquete(mejorIndividuoDto);

            // !Actualizar
            EstadoManager.actualizarEstadoGlobal(fechaActual, pedidosEnviar);
        } catch (Exception e) {
            System.err.println("❌ Error al crear paquete parche en tiempo " + fechaActual + ": "
                    + e.getMessage());
            e.printStackTrace();

            // Crear un paquete de emergencia en lugar de no generar nada
            try {
                System.out.println("🚑 Creando paquete de emergencia para tiempo " + fechaActual);
                Individuo individuoEmergencia = IndividuoFactory.crearIndividuoVacio();
                IndividuoDto paqueteEmergencia = new IndividuoDto(individuoEmergencia,
                        pedidosEnviar, bloqueosActivos, fechaActual);
                paqueteEmergencia.setFechaHoraInicioIntervalo(fechaInicioParche);
                paqueteEmergencia.setFechaHoraFinIntervalo(
                        fechaFinParche);
                paqueteEmergencia.setTipoIndividuo(TipoIndividuo.EMERGENCIA);
                GestorHistorialSimulacion.agregarPaquete(paqueteEmergencia);

                System.out.println("🔍 DIAGNÓSTICO: Posiciones DESPUÉS de crear paquete de emergencia:");
                Camion.imprimirDatosCamiones(DataLoader.camiones);
                Simulacion.desactivarFaltaCrearParche();
            } catch (Exception e2) {
                System.err.println("❌ Error al crear paquete de emergencia:  de paquete parche 🩹" + e2.getMessage());
                e2.printStackTrace();
            }
        }

        System.out.println("📊 Estado: Pedidos semanales restantes: " + pedidosSemanal.size());

        // Imprimir resumen detallado de estados
        EstadoManager.imprimirResumenEstados();
        System.out.println("ESPERANDO 1 HORA");

        System.out.println("🔍 DIAGNÓSTICO: Posiciones FINALES antes de salir de crearPaqueteParche:");
        Camion.imprimirDatosCamiones(DataLoader.camiones);
        Simulacion.desactivarFaltaCrearParche();
        System.out.println("====================FIN PAQUTETE PARCHE=======================");
    }

    /**
     * Actualiza las posiciones de los camiones usando los datos del frontend.
     * 
     * @param camionesEstado Lista de estados de camiones del frontend
     */
    private static void actualizarCamionesDesdeEstadoCapturado(
            List<AveriaConEstadoRequest.CamionEstado> camionesEstado) {
        try {
            System.out.println("🔧 Iniciando actualización de camiones...");

            for (AveriaConEstadoRequest.CamionEstado camionEstado : camionesEstado) {
                System.out.println("🔧 Procesando camión: " + camionEstado.getId());

                // Buscar el camión directamente en la lista de DataLoader
                Camion camion = DataLoader.camiones.stream()
                        .filter(c -> c.getCodigo().equals(camionEstado.getId()))
                        .findFirst()
                        .orElse(null);

                if (camion != null) {
                    // Actualizar posición si está presente
                    String ubicacion = camionEstado.getUbicacion();
                    if (ubicacion != null && ubicacion.matches("\\(\\d+,\\d+\\)")) {
                        try {
                            String[] partes = ubicacion.replaceAll("[()]", "").split(",");
                            int x = Integer.parseInt(partes[0].trim());
                            int y = Integer.parseInt(partes[1].trim());

                            // Crear nueva coordenada (y = fila, x = columna)
                            Coordenada nuevaCoordenada = new Coordenada(y, x);
                            camion.setCoordenada(nuevaCoordenada);

                            System.out.println("✅ Camión " + camion.getCodigo() +
                                    " actualizado a posición: " + nuevaCoordenada);

                            // Actualizar otros campos si es necesario
                            if (camionEstado.getCapacidadActualGLP() != null) {
                                camion.setCapacidadActualGLP(camionEstado.getCapacidadActualGLP());
                            }
                            if (camionEstado.getCombustibleActual() != null) {
                                camion.setCombustibleActual(camionEstado.getCombustibleActual());
                            }

                        } catch (Exception e) {
                            System.err.println("❌ Error al actualizar posición del camión " +
                                    camion.getCodigo() + ": " + e.getMessage());
                        }
                    } else {
                        System.out.println("ℹ️ Ubicación no válida o faltante para camión: " +
                                camionEstado.getId());
                    }
                } else {
                    System.out.println("⚠️ Camión no encontrado: " + camionEstado.getId());
                    System.out.println("   Camiones disponibles: " +
                            DataLoader.camiones.stream()
                                    .map(Camion::getCodigo)
                                    .collect(Collectors.joining(", ")));
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error en actualizarCamionesDesdeEstadoCapturado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Actualiza los almacenes usando los datos del frontend.
     * 
     * @param almacenesEstado Lista de estados de almacenes del frontend
     */
    private static void actualizarAlmacenesDesdeEstadoCapturado(
            List<AveriaConEstadoRequest.AlmacenSimple> almacenesEstado) {
        try {
            for (AveriaConEstadoRequest.AlmacenSimple almacenEstado : almacenesEstado) {
                // Buscar el almacén en la lista de almacenes del sistema
                for (Almacen almacen : DataLoader.almacenes) {
                    // Corrección: coordenadaY -> fila, coordenadaX -> columna
                    if (almacen.getCoordenada().getFila() == almacenEstado.getCoordenadaY() &&
                            almacen.getCoordenada().getColumna() == almacenEstado.getCoordenadaX()) {

                        // Actualizar capacidades del almacén
                        if (almacenEstado.getCapacidadActualGLP() != null) {
                            almacen.setCapacidadActualGLP(almacenEstado.getCapacidadActualGLP());
                        }
                        if (almacenEstado.getCapacidadActualCombustible() != null) {
                            almacen.setCapacidadCombustible(almacenEstado.getCapacidadActualCombustible());
                        }
                        // Usar repository para actualizar el almacén
                        almacenRepository.update(almacen);
                        System.out.println("🏪 Almacén en (" + almacen.getCoordenada().getFila() + ","
                                + almacen.getCoordenada().getColumna() + ") actualizado");
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error al actualizar almacenes desde estado capturado: " + e.getMessage());
        }
    }

    /**
     * Obtiene la lista de pedidos desde el estado capturado.
     * 
     * @param estadoCapturado Estado de la simulación capturado
     * @return Lista de pedidos extraídos del estado capturado
     */
    public static List<Pedido> obtenerPedidosDesdeEstadoCapturado(
            AveriaConEstadoRequest.EstadoSimulacion estadoCapturado) {
        List<Pedido> pedidos = new ArrayList<>();

        try {
            // Extraer pedidos de las rutas de camiones capturadas
            if (estadoCapturado.getRutasCamiones() != null) {
                for (var rutaCamion : estadoCapturado.getRutasCamiones()) {
                    if (rutaCamion.getPedidos() != null) {
                        System.out.println("📦 Procesando " + rutaCamion.getPedidos().size() + " pedidos de camión "
                                + rutaCamion.getId());
                    }
                }
            }

            // Usar los pedidos actuales del sistema por ahora
            pedidos.addAll(Simulacion.pedidosPorAtender);
            pedidos.addAll(Simulacion.pedidosPlanificados);

            System.out.println("📋 Pedidos para paquete parche: " + pedidos.size());

        } catch (Exception e) {
            System.err.println("❌ Error al extraer pedidos del estado capturado: " + e.getMessage());
        }

        return pedidos;
    }

    /**
     * Obtiene la lista de bloqueos desde el estado capturado.
     * 
     * @param estadoCapturado Estado de la simulación capturado
     * @return Lista de bloqueos extraídos del estado capturado
     */
    public static List<Bloqueo> obtenerBloqueosDesdeEstadoCapturado(
            AveriaConEstadoRequest.EstadoSimulacion estadoCapturado) {
        List<Bloqueo> bloqueos = new ArrayList<>();

        try {
            if (estadoCapturado.getBloqueos() != null) {
                // Convertir bloqueos del estado capturado
                System.out.println(
                        "🚧 Procesando " + estadoCapturado.getBloqueos().size() + " bloqueos del estado capturado");
                // Por ahora retornamos lista vacía como fallback
            }

            System.out.println("🚧 Bloqueos extraídos del estado capturado: " + bloqueos.size());

        } catch (Exception e) {
            System.err.println("❌ Error al extraer bloqueos del estado capturado: " + e.getMessage());
        }

        return bloqueos;
    }
}