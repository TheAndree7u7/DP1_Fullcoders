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

        // Validar y extraer parámetros
        List<?> clusters = (List<?>) params.get("clusters");
        if (clusters == null || clusters.isEmpty()) {
            logger.error("No se proporcionaron clusters válidos");
            throw new IllegalArgumentException("Se requieren clusters válidos para generar rutas");
        }

        int numeroRutas = params.containsKey("numeroRutas") ? 
            Integer.parseInt(params.get("numeroRutas").toString()) : clusters.size();
        
        logger.info("Generando {} rutas para {} clusters", numeroRutas, clusters.size());

        // Obtener camiones disponibles
        List<Camion> camionesDisponibles = camionRepository.findByEstado(EstadoCamion.DISPONIBLE);
        logger.info("Camiones disponibles encontrados: {}", camionesDisponibles.size());

        if (camionesDisponibles.isEmpty()) {
            logger.warn("No hay camiones disponibles para asignar a las rutas");
            return AlgoritmoGeneticoResultadoDTO.builder()
                    .metodo("algoritmoGenetico")
                    .totalPedidos(0)
                    .pedidosAsignados(0)
                    .mensaje("No hay camiones disponibles para asignar")
                    .rutas(Collections.emptyList())
                    .build();
        }

        // Ajustar el número de rutas según los camiones disponibles
        numeroRutas = Math.min(numeroRutas, camionesDisponibles.size());

        // Obtener almacén central
        List<Almacen> almacenesCentrales = almacenRepository.findByEsCentralAndActivo(true, true);
        if (almacenesCentrales.isEmpty()) {
            logger.error("No se encontró ningún almacén central activo");
            return AlgoritmoGeneticoResultadoDTO.builder()
                    .metodo("algoritmoGenetico")
                    .totalPedidos(0)
                    .pedidosAsignados(0)
                    .mensaje("Error: No se encontró ningún almacén central activo")
                    .rutas(Collections.emptyList())
                    .build();
        }
        
        // Usar el primer almacén central encontrado
        Almacen almacenCentral = almacenesCentrales.get(0);
        logger.info("Usando almacén central: {}", almacenCentral.getId());

        // Convertir clusters a grupos de pedidos
        List<List<Pedido>> gruposPedidos = new ArrayList<>();
        for (Object cluster : clusters) {
            if (cluster instanceof List) {
                List<?> pedidosCluster = (List<?>) cluster;
                List<Pedido> pedidos = pedidosCluster.stream()
                    .map(p -> {
                        if (p instanceof Map) {
                            Map<?, ?> pedidoMap = (Map<?, ?>) p;
                            Long id = Long.valueOf(pedidoMap.get("id").toString());
                            return pedidoRepository.findById(id).orElse(null);
                        }
                        return null;
                    })
                    .filter(p -> p != null)
                    .collect(Collectors.toList());
                gruposPedidos.add(pedidos);
            }
        }

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
            
            // Agregar nodos para cada pedido en el grupo
            for (Pedido pedido : pedidosGrupo) {
                ruta.agregarNodoCliente(pedido.getPosX(), pedido.getPosY(), pedido, pedido.getVolumenGLPAsignado(), 100.0);
            }
            
            // Volver al almacén central
            ruta.agregarNodo(almacenCentral.getPosX(), almacenCentral.getPosY(), "ALMACEN");
            
            // Guardar la ruta
            ruta = rutaRepository.save(ruta);
            rutasCreadas.add(ruta);
            
            // Actualizar estado del camión
            camion.setEstado(EstadoCamion.EN_RUTA);
            camionRepository.save(camion);
            
            // Actualizar estado de los pedidos
            for (Pedido pedido : pedidosGrupo) {
                pedido.setEstado(EstadoPedido.EN_RUTA);
                pedidoRepository.save(pedido);
            }

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
                    .tiempoEstimado((int)ruta.getTiempoEstimadoMinutos())
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

        logger.info("Generación de rutas completada. Rutas creadas: {}, Pedidos asignados: {}",
                rutasCreadas.size(), pedidosAsignados, pedidosAsignados);

        // Preparamos el resultado usando DTO
        AlgoritmoGeneticoResultadoDTO resultado = AlgoritmoGeneticoResultadoDTO.builder()
                .rutas(rutasDTO)
                .metodo("algoritmoGenetico")
                .totalPedidos(pedidosAsignados)
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
