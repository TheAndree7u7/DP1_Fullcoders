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
        try {
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
        System.out.println("====================FIN PAQUTETE PARCHE=======================");
        } finally {
            Simulacion.desactivarFaltaCrearParche();
        }
    }

    /**
     * Actualiza las posiciones de los camiones usando los datos del frontend.
     * 
     * @param camionesEstado Lista de estados de camiones del frontend
     */
    private static void actualizarCamionesDesdeEstadoCapturado(
            List<AveriaConEstadoRequest.CamionEstado> camionesEstado) {
        try {
            System.out.println("🔧 DEPURACIÓN: Iniciando actualización de camiones desde estado capturado");
            for (AveriaConEstadoRequest.CamionEstado camionEstado : camionesEstado) {
                System.out.println("🔧 Procesando camión: " + camionEstado.getId() + " con ubicación: "
                        + camionEstado.getUbicacion());
                // Buscar el camión en la lista de camiones del sistema
                boolean camionEncontrado = false;
                for (Camion camion : DataLoader.camiones) {
                    if (camion.getCodigo().equals(camionEstado.getId())) {
                        camionEncontrado = true;
                        System.out.println("🔧 Camión encontrado: " + camion.getCodigo() + ", posición actual: "
                                + camion.getCoordenada());
                        // Actualizar posición del camión
                        String ubicacion = camionEstado.getUbicacion();
                        System.out.println("🔧 Validando ubicación: '" + ubicacion + "'");
                        System.out.println("🔧 Ubicación no nula: " + (ubicacion != null));
                        System.out.println("🔧 Ubicación match regex: "
                                + (ubicacion != null && ubicacion.matches("\\(\\d+,\\d+\\)")));
                        if (ubicacion != null && ubicacion.matches("\\(\\d+,\\d+\\)")) {
                            String coords = ubicacion.substring(1, ubicacion.length() - 1);
                            String[] parts = coords.split(",");
                            int x = Integer.parseInt(parts[0]);
                            int y = Integer.parseInt(parts[1]);

                            System.out.println("🔧 Coordenadas parseadas: x=" + x + ", y=" + y);
                            System.out.println("🔧 Posición ANTES de actualizar: " + camion.getCoordenada());

                            camion.setCoordenada(new Coordenada(x, y));

                            System.out.println("🔧 Posición DESPUÉS de setCoordenada: " + camion.getCoordenada());

                            // Actualizar otros estados del camión
                            if (camionEstado.getCapacidadActualGLP() != null) {
                                camion.setCapacidadActualGLP(camionEstado.getCapacidadActualGLP());
                            }
                            if (camionEstado.getCombustibleActual() != null) {
                                camion.setCombustibleActual(camionEstado.getCombustibleActual());
                            }

                            // Usar repository para actualizar el camion
                            System.out.println("🔧 Llamando a repository.update() para camión: " + camion.getCodigo());
                            Camion camionActualizado = camionRepository.update(camion);

                            System.out.println(
                                    "🔧 Posición DESPUÉS de repository.update(): " + camionActualizado.getCoordenada());

                            System.out.println("🚛 Camión " + camion.getCodigo() + " actualizado a posición (" + x + ","
                                    + y + ")");
                        } else {
                            System.out.println(
                                    "🔧 ❌ Ubicación inválida para camión " + camionEstado.getId() + ": " + ubicacion);
                        }
                        break;
                    }
                }

                if (!camionEncontrado) {
                    System.out.println("🔧 ❌ Camión NO encontrado en DataLoader.camiones: " + camionEstado.getId());
                    System.out.println("🔧 Camiones disponibles en DataLoader:");
                    for (Camion c : DataLoader.camiones) {
                        System.out.println("🔧   - " + c.getCodigo());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error al actualizar camiones desde estado capturado: " + e.getMessage());
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
                    if (almacen.getCoordenada().getFila() == almacenEstado.getCoordenadaX() &&
                            almacen.getCoordenada().getColumna() == almacenEstado.getCoordenadaY()) {

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