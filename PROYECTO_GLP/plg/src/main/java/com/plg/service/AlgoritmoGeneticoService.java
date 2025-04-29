package com.plg.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.plg.dto.AlgoritmoGeneticoResultadoDTO;
import com.plg.dto.PedidoDTO;
import com.plg.dto.PuntoRutaDTO;
import com.plg.dto.RutaDTO;
import com.plg.entity.Almacen;
import com.plg.entity.Camion;
import com.plg.entity.EstadoCamion;
import com.plg.entity.EstadoPedido;
import com.plg.entity.NodoRuta;
import com.plg.entity.Pedido;
import com.plg.entity.Ruta;
import com.plg.repository.AlmacenRepository;
import com.plg.repository.CamionRepository;
import com.plg.repository.PedidoRepository;
import com.plg.repository.RutaRepository;

@Service
public class AlgoritmoGeneticoService {

    private static final Logger logger = LoggerFactory.getLogger(AlgoritmoGeneticoService.class);
    @Autowired
    private MapaReticularService mapaReticularService;
    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private CamionRepository camionRepository;

    @Autowired
    private RutaRepository rutaRepository;

    @Autowired
    private AlmacenRepository almacenRepository;

    // Parámetros configurables del algoritmo genético
    private final int POBLACION_INICIAL = 50;
    private final int MAX_GENERACIONES = 100;
    private final double TASA_MUTACION = 0.1;
    private final double TASA_CRUCE = 0.8;

    @Transactional
    public AlgoritmoGeneticoResultadoDTO generarRutas(Map<String, Object> params) {
        logger.info("Iniciando generación de rutas con algoritmo genético. Parámetros: {}", params);

        // Obtener pedidos pendientes
        List<Pedido> pedidos = pedidoRepository.findByEstado(EstadoPedido.PENDIENTE_PLANIFICACION);
        logger.info("Pedidos pendientes encontrados: {}", pedidos.size());

        // Verificar si hay suficientes pedidos para optimizar
        if (pedidos.isEmpty()) {
            logger.warn("No hay pedidos pendientes para generar rutas");
            return AlgoritmoGeneticoResultadoDTO.builder()
                    .metodo("algoritmoGenetico")
                    .totalPedidos(0)
                    .pedidosAsignados(0)
                    .rutas(Collections.emptyList())
                    .build();
        }

        // Parámetros opcionales
        int numeroRutas = params.containsKey("numeroRutas")
                ? Integer.parseInt(params.get("numeroRutas").toString()) : 3;
        logger.info("Generando {} rutas para {} pedidos", numeroRutas, pedidos.size());

        // Obtener camiones disponibles
        List<Camion> camionesDisponibles = camionRepository.findByEstado(EstadoCamion.DISPONIBLE); // Usar el enum
        logger.info("Camiones disponibles encontrados: {}", camionesDisponibles.size());

        if (camionesDisponibles.isEmpty()) {
            logger.warn("No hay camiones disponibles para asignar a las rutas");
            return AlgoritmoGeneticoResultadoDTO.builder()
                    .metodo("algoritmoGenetico")
                    .totalPedidos(pedidos.size())
                    .pedidosAsignados(0)
                    .mensaje("No hay camiones disponibles para asignar")
                    .rutas(Collections.emptyList())
                    .build();
        }

        // Ajustar el número de rutas según los camiones disponibles
        numeroRutas = Math.min(numeroRutas, camionesDisponibles.size());

        // Obtener almacén central
        Almacen almacenCentral = almacenRepository.findByEsCentralAndActivoTrue(true);
        if (almacenCentral == null) {
            logger.error("No se encontró el almacén central activo");
            return AlgoritmoGeneticoResultadoDTO.builder()
                    .metodo("algoritmoGenetico")
                    .totalPedidos(pedidos.size())
                    .pedidosAsignados(0)
                    .mensaje("Error: No se encontró el almacén central")
                    .rutas(Collections.emptyList())
                    .build();
        }

        // Generamos grupos de pedidos para las rutas
        List<List<Pedido>> gruposPedidos = dividirEnGrupos(pedidos, numeroRutas);
        logger.info("Pedidos divididos en {} grupos", gruposPedidos.size());

        // Crear rutas y asignar camiones
        List<RutaDTO> rutasDTO = new ArrayList<>();
        List<Ruta> rutasCreadas = new ArrayList<>();

        for (int i = 0; i < gruposPedidos.size(); i++) {
            if (i >= camionesDisponibles.size()) {
                break;
            }

            List<Pedido> pedidosGrupo = gruposPedidos.get(i);
            Camion camion = camionesDisponibles.get(i);

            logger.info("Creando ruta {} con {} pedidos para camión {}", (i + 1), pedidosGrupo.size(), camion.getCodigo());

            // Crear una nueva entidad Ruta
            Ruta ruta = new Ruta("R" + System.currentTimeMillis() + "-" + (i + 1));
            ruta.setCamion(camion);
            ruta.setEstado(1); // Estado 1 = En curso
            ruta.setFechaCreacion(LocalDateTime.now());
            ruta.setFechaInicioRuta(LocalDateTime.now());
            ruta.setConsideraBloqueos(true);

            // Agregar cada tramo como una ruta reticular considerando bloqueos
            double xPrev = almacenCentral.getPosX(), yPrev = almacenCentral.getPosY();
            
            // Agrega el nodo de origen (almacén central) primero
            ruta.agregarNodo(almacenCentral.getPosX(), almacenCentral.getPosY(), "ALMACEN");

            for (Pedido pedido : pedidosGrupo) {
                logger.debug("  → Trazando tramo de ({},{}) a pedido {} en ({},{})",
                        xPrev, yPrev,
                        pedido.getCodigo(), pedido.getPosX(), pedido.getPosY());
                long t0 = System.currentTimeMillis();

                List<double[]> tramo = mapaReticularService
                        .calcularRutaOptimaConsiderandoBloqueos(xPrev, yPrev,
                                pedido.getPosX(), pedido.getPosY());

                long dt = System.currentTimeMillis() - t0;
                logger.debug("    ← tramo calculado en {} ms ({} nodos)", dt, tramo.size());
                
                // Agregar puntos intermedios del tramo excepto el último (que será el punto del cliente)
                for (int j = 0; j < tramo.size() - 1; j++) {
                    double[] nodo = tramo.get(j);
                    ruta.agregarNodo(nodo[0], nodo[1], "INTERMEDIO");
                }
                
                // Agregar el nodo del cliente con el pedido asociado
                ruta.agregarNodoCliente(pedido.getPosX(), pedido.getPosY(), pedido, pedido.getVolumenGLPAsignado(), 100.0);
                
                // IMPORTANTE: Actualizar el estado del pedido a EN_RUTA y asignarle el camión
                pedido.setEstado(EstadoPedido.EN_RUTA);
                pedido.setCamion(camion);
                // Las siguientes líneas dan error porque estos métodos no existen en la clase Pedido
                // pedido.setFechaAsignacion(LocalDateTime.now());
                // pedido.setRutaAsignada(ruta);
                
                // En lugar de usar los métodos anteriores, actualizamos la fechaRegistro que sí existe
                pedido.setFechaRegistro(LocalDateTime.now());
                pedidoRepository.save(pedido);
                
                logger.info("Pedido {} actualizado a estado EN_RUTA y asignado a camión {}", 
                    pedido.getCodigo(), camion.getCodigo());
                
                xPrev = pedido.getPosX();
                yPrev = pedido.getPosY();
            }
            
            // Trazar el tramo de regreso al almacén
            logger.debug("  → Trazando tramo de regreso a almacén en ({},{})",
                    xPrev, yPrev);
            long t1 = System.currentTimeMillis();
            List<double[]> regreso = mapaReticularService
                    .calcularRutaOptimaConsiderandoBloqueos(
                            xPrev, yPrev,
                            almacenCentral.getPosX(), almacenCentral.getPosY()
                    );
            long dt2 = System.currentTimeMillis() - t1;
            logger.debug("    ← regreso calculado en {} ms ({} nodos)", dt2, regreso.size());
            
            // Agregar puntos del tramo de regreso
            for (int j = 0; j < regreso.size(); j++) {
                double[] nodo = regreso.get(j);
                String tipo = (j == regreso.size() - 1) ? "ALMACEN" : "INTERMEDIO";
                ruta.agregarNodo(nodo[0], nodo[1], tipo);
            }

            // Calcular distancia total y optimizar ruta si es necesario
            ruta.calcularDistanciaTotal();
            
            // Estimar tiempos de llegada con velocidad promedio de 30 km/h
            ruta.estimarTiemposLlegada(30.0, LocalDateTime.now());

            // Cambiar estado del camión a "En ruta"
            camion.setEstado(EstadoCamion.EN_RUTA);
            camion.setPosX(almacenCentral.getPosX());
            camion.setPosY(almacenCentral.getPosY());
            camion.setUltimoAlmacen(almacenCentral);
            camion.setFechaUltimaCarga(LocalDateTime.now());
            camionRepository.save(camion);

            // Guardar la ruta
            rutaRepository.save(ruta);
            rutasCreadas.add(ruta);

            // Crear DTO para la respuesta
            List<PedidoDTO> pedidosDTO = pedidosGrupo.stream()
                    .map(this::convertirAPedidoDTO)
                    .collect(Collectors.toList());

            List<PuntoRutaDTO> puntosRuta = new ArrayList<>();
            for (NodoRuta nodo : ruta.getNodos()) {
                PuntoRutaDTO punto = PuntoRutaDTO.builder()
                        .tipo(nodo.getTipo())
                        .posX(nodo.getPosX())
                        .posY(nodo.getPosY())
                        .idPedido(nodo.getPedido() != null ? nodo.getPedido().getId() : null)
                        .build();
                puntosRuta.add(punto);
            }

            RutaDTO rutaDTO = RutaDTO.builder()
                    .idRuta(ruta.getCodigo())
                    .distanciaTotal(ruta.getDistanciaTotal())
                    .tiempoEstimado((int)ruta.getTiempoEstimadoMinutos()) // Convertir a int para resolver el error de tipo
                    .pedidos(pedidosDTO)
                    .numeroPedidos(pedidosGrupo.size())
                    .puntos(puntosRuta)
                    .camionCodigo(camion.getCodigo())
                    .build();

            rutasDTO.add(rutaDTO);
            logger.info("Ruta {} creada exitosamente con id: {}, {} pedidos asignados", 
                (i + 1), ruta.getId(), pedidosGrupo.size());
        }

        int pedidosAsignados = gruposPedidos.stream()
                .mapToInt(List::size)
                .sum();

        logger.info("Generación de rutas completada. Rutas creadas: {}, Pedidos asignados: {}/{}",
                rutasCreadas.size(), pedidosAsignados, pedidos.size());

        // Preparamos el resultado usando DTO
        AlgoritmoGeneticoResultadoDTO resultado = AlgoritmoGeneticoResultadoDTO.builder()
                .rutas(rutasDTO)
                .metodo("algoritmoGenetico")
                .totalPedidos(pedidos.size())
                .pedidosAsignados(pedidosAsignados)
                .rutasGeneradas(rutasCreadas.size())
                .build();

        return resultado;
    }

    // Método auxiliar para dividir pedidos en grupos (simulado)
    private List<List<Pedido>> dividirEnGrupos(List<Pedido> pedidos, int numeroGrupos) {
        logger.debug("Dividiendo {} pedidos en {} grupos", pedidos.size(), numeroGrupos);
        List<List<Pedido>> grupos = new ArrayList<>();

        // Asegurar que no intentamos crear más grupos que pedidos disponibles
        numeroGrupos = Math.min(numeroGrupos, pedidos.size());

        // Crear grupos vacíos
        for (int i = 0; i < numeroGrupos; i++) {
            grupos.add(new ArrayList<>());
        }

        // Distribuir pedidos (enfoque simple por turnos)
        for (int i = 0; i < pedidos.size(); i++) {
            grupos.get(i % numeroGrupos).add(pedidos.get(i));
        }

        return grupos;
    }

    // Convierte un pedido a un DTO
    private PedidoDTO convertirAPedidoDTO(Pedido pedido) {
        return PedidoDTO.builder()
                .id(pedido.getId())
                .codigo(pedido.getCodigo())
                .posX(pedido.getPosX())
                .posY(pedido.getPosY())
                .volumenGLPAsignado(pedido.getVolumenGLPAsignado())
                .horasLimite(pedido.getHorasLimite())
                .clienteId(pedido.getCliente() != null ? pedido.getCliente().getId() : null)
                .clienteNombre(pedido.getCliente() != null ? pedido.getCliente().getNombre() : null)
                .build();
    }
}
