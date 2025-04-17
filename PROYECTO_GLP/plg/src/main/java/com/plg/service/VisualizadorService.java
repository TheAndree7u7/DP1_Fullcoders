package com.plg.service;

import com.plg.entity.Almacen;
import com.plg.entity.Bloqueo;
import com.plg.entity.Camion;
import com.plg.entity.Pedido;
import com.plg.repository.AlmacenRepository;
import com.plg.repository.CamionRepository;
import com.plg.repository.PedidoRepository;
import com.plg.enums.EstadoCamion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VisualizadorService {

    @Autowired
    private PedidoRepository pedidoRepository;
    
    @Autowired
    private CamionRepository camionRepository;
    
    @Autowired
    private AlmacenRepository almacenRepository;

    public Map<String, Object> obtenerDatosMapa(boolean mostrarPedidos, boolean mostrarCamiones, boolean mostrarBloqueos, boolean mostrarAlmacenes) {
        Map<String, Object> datos = new HashMap<>();
        
        if (mostrarPedidos) {
            List<Pedido> pedidos = pedidoRepository.findAll();
            List<Map<String, Object>> pedidosData = pedidos.stream()
                .map(this::convertirPedidoAMapa)
                .collect(Collectors.toList());
            datos.put("pedidos", pedidosData);
        }
        
        if (mostrarCamiones) {
            List<Camion> camiones = camionRepository.findAll();
            List<Map<String, Object>> camionesData = camiones.stream()
                .map(this::convertirCamionAMapa)
                .collect(Collectors.toList());
            datos.put("camiones", camionesData);
        }
        
        if (mostrarBloqueos) {
            List<Map<String, Object>> bloqueosData = obtenerBloqueosEjemplo();
            datos.put("bloqueos", bloqueosData);
        }
        
        if (mostrarAlmacenes) {
            List<Almacen> almacenes = almacenRepository.findByActivo(true);
            List<Map<String, Object>> almacenesData = almacenes.stream()
                .map(this::convertirAlmacenAMapa)
                .collect(Collectors.toList());
            datos.put("almacenes", almacenesData);
        }
        
        return datos;
    }
    
    public Map<String, Object> obtenerEstadoGeneral() {
        Map<String, Object> estado = new HashMap<>();
        
        List<Pedido> pedidos = pedidoRepository.findAll();
        estado.put("totalPedidos", pedidos.size());
        estado.put("pedidosPendientes", pedidos.stream().filter(p -> p.getEstado() == 0).count());
        estado.put("pedidosEntregados", pedidos.stream().filter(p -> p.getEstado() == 3).count());
        
        List<Camion> camiones = camionRepository.findAll();
        estado.put("totalCamiones", camiones.size());
        estado.put("camionesDisponibles", camiones.stream().filter(c -> c.getEstado() == EstadoCamion.DISPONIBLE).count());
        estado.put("camionesEnRuta", camiones.stream().filter(c -> c.getEstado() == EstadoCamion.EN_RUTA).count());
        estado.put("camionesEnMantenimiento", camiones.stream().filter(c -> 
            c.getEstado() == EstadoCamion.EN_MANTENIMIENTO_PREVENTIVO || 
            c.getEstado() == EstadoCamion.EN_MANTENIMIENTO_CORRECTIVO || 
            c.getEstado() == EstadoCamion.EN_MANTENIMIENTO_POR_AVERIA).count());
        estado.put("camionesAveriados", camiones.stream().filter(c -> 
            c.getEstado() == EstadoCamion.INMOVILIZADO_POR_AVERIA).count());
        
        return estado;
    }
    
    public Map<String, Object> aplicarFiltros(Map<String, Object> filtros) {
        Map<String, Object> datosConFiltro = new HashMap<>();
        
        if (filtros.containsKey("estadoPedidos")) {
            List<Integer> estados = (List<Integer>) filtros.get("estadoPedidos");
            List<Pedido> pedidosFiltrados = pedidoRepository.findAll().stream()
                .filter(p -> estados.contains(p.getEstado()))
                .collect(Collectors.toList());
            datosConFiltro.put("pedidos", pedidosFiltrados.stream()
                .map(this::convertirPedidoAMapa)
                .collect(Collectors.toList()));
        }
        
        if (filtros.containsKey("tipoCamiones")) {
            List<String> tipos = (List<String>) filtros.get("tipoCamiones");
            List<Camion> camionesFiltrados = camionRepository.findAll().stream()
                .filter(c -> tipos.contains(c.getTipo()))
                .collect(Collectors.toList());
            datosConFiltro.put("camiones", camionesFiltrados.stream()
                .map(this::convertirCamionAMapa)
                .collect(Collectors.toList()));
        }
        
        return datosConFiltro;
    }
    
    private Map<String, Object> convertirPedidoAMapa(Pedido pedido) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", pedido.getId());
        map.put("posX", pedido.getPosX());
        map.put("posY", pedido.getPosY());
        map.put("fechaHora", pedido.getFechaHora());
        map.put("cliente", pedido.getCliente() != null ? pedido.getCliente().getId() : null);
        map.put("estado", pedido.getEstado());
        map.put("m3", pedido.getVolumenGLPAsignado());
        return map;
    }
    
    private Map<String, Object> convertirCamionAMapa(Camion camion) {
        Map<String, Object> map = new HashMap<>();
        map.put("codigo", camion.getCodigo());
        map.put("tipo", camion.getTipo());
        map.put("estado", camion.getEstado()); // Ahora retorna el enum directamente
        map.put("estadoTexto", camion.getEstadoTexto());
        map.put("estadoColorHex", camion.getEstado().getColorHex()); // Podemos usar el color asociado al enum
        map.put("capacidad", camion.getCapacidad());
        map.put("pesoCarga", camion.getPesoCarga());
        map.put("posX", 0);
        map.put("posY", 0);
        return map;
    }
    
    private Map<String, Object> convertirAlmacenAMapa(Almacen almacen) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", almacen.getId());
        map.put("nombre", almacen.getNombre());
        map.put("posX", almacen.getPosX());
        map.put("posY", almacen.getPosY());
        map.put("esCentral", almacen.isEsCentral());
        map.put("capacidadGLP", almacen.getCapacidadGLP());
        map.put("capacidadActualGLP", almacen.getCapacidadActualGLP());
        map.put("capacidadCombustible", almacen.getCapacidadCombustible());
        map.put("capacidadActualCombustible", almacen.getCapacidadActualCombustible());
        return map;
    }
    
    private List<Map<String, Object>> obtenerBloqueosEjemplo() {
        List<Map<String, Object>> bloqueos = new ArrayList<>();
        
        Map<String, Object> bloqueo1 = new HashMap<>();
        bloqueo1.put("posXInicio", 10);
        bloqueo1.put("posYInicio", 10);
        bloqueo1.put("posXFin", 20);
        bloqueo1.put("posYFin", 20);
        bloqueo1.put("descripcion", "Construcción de vía");
        bloqueo1.put("activo", true);
        bloqueos.add(bloqueo1);
        
        Map<String, Object> bloqueo2 = new HashMap<>();
        bloqueo2.put("posXInicio", 30);
        bloqueo2.put("posYInicio", 30);
        bloqueo2.put("posXFin", 35);
        bloqueo2.put("posYFin", 40);
        bloqueo2.put("descripcion", "Deslave en carretera");
        bloqueo2.put("activo", true);
        bloqueos.add(bloqueo2);
        
        return bloqueos;
    }
}