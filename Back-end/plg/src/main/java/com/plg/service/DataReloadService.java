package com.plg.service;

import org.springframework.stereotype.Service;

import com.plg.config.DataLoader;
import com.plg.entity.Almacen;
import com.plg.entity.Averia;
import com.plg.entity.Bloqueo;
import com.plg.entity.Camion;
import com.plg.entity.Mantenimiento;
import com.plg.entity.Pedido;
import com.plg.utils.ExcepcionesPerzonalizadas.InvalidDataFormatException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio para recargar todos los datos del sistema.
 * Implementa el principio de responsabilidad única (SRP) al encargarse
 * únicamente de la recarga de datos.
 */
@Service
public class DataReloadService {

    /**
     * Recarga todos los datos del sistema limpiando las listas existentes
     * y cargando nuevos datos desde los archivos.
     * 
     * @return Resultado de la recarga con estadísticas
     * @throws InvalidDataFormatException si hay errores en el formato de los datos
     * @throws IOException                si hay errores de lectura de archivos
     */
    public DataReloadResult recargarTodosLosDatos() throws InvalidDataFormatException, IOException {
        System.out.println("🔄 Iniciando recarga completa de datos del sistema...");

        try {
            // Limpiar todas las listas existentes
            limpiarDatosExistentes();

            // Recargar datos en orden de dependencia
            List<Almacen> almacenes = recargarAlmacenes();
            List<Camion> camiones = recargarCamiones();
            List<Pedido> pedidos = recargarPedidos();
            List<Averia> averias = recargarAverias();
            List<Mantenimiento> mantenimientos = recargarMantenimientos();
            recargarBloqueos();

            // Calcular fechas mínima y máxima de los pedidos
            LocalDateTime fechaMinimaPedidos = null;
            LocalDateTime fechaMaximaPedidos = null;

            if (!pedidos.isEmpty()) {
                fechaMinimaPedidos = pedidos.stream()
                        .map(Pedido::getFechaRegistro)
                        .filter(fecha -> fecha != null)
                        .min(LocalDateTime::compareTo)
                        .orElse(null);

                fechaMaximaPedidos = pedidos.stream()
                        .map(Pedido::getFechaRegistro)
                        .filter(fecha -> fecha != null)
                        .max(LocalDateTime::compareTo)
                        .orElse(null);
            }

            // Crear resultado con estadísticas
            DataReloadResult resultado = new DataReloadResult(
                    almacenes.size(),
                    camiones.size(),
                    pedidos.size(),
                    averias.size(),
                    mantenimientos.size(),
                    DataLoader.bloqueos.size(),
                    fechaMinimaPedidos,
                    fechaMaximaPedidos);

            System.out.println("✅ Recarga de datos completada exitosamente");
            System.out.println("📊 Estadísticas de recarga: " + resultado);

            return resultado;

        } catch (Exception e) {
            System.err.println("❌ Error durante la recarga de datos: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Limpia todas las listas de datos existentes.
     * Implementa el principio de inversión de dependencias (DIP) al
     * trabajar con las abstracciones del DataLoader.
     */
    private void limpiarDatosExistentes() {
        System.out.println("🧹 Limpiando datos existentes...");

        DataLoader.almacenes.clear();
        DataLoader.camiones.clear();
        DataLoader.pedidos.clear();
        DataLoader.averias.clear();
        DataLoader.mantenimientos.clear();
        DataLoader.bloqueos.clear();

        // Limpiar también las listas de las factories
        com.plg.factory.AlmacenFactory.almacenes.clear();
        com.plg.factory.CamionFactory.camiones.clear();
        com.plg.factory.PedidoFactory.pedidos.clear();

        System.out.println("✅ Datos existentes limpiados");
    }

    /**
     * Recarga los almacenes del sistema.
     */
    private List<Almacen> recargarAlmacenes() {
        System.out.println("🏢 Recargando almacenes...");
        return DataLoader.initializeAlmacenes();
    }

    /**
     * Recarga los camiones del sistema.
     */
    private List<Camion> recargarCamiones() {
        System.out.println("🚛 Recargando camiones...");
        return DataLoader.initializeCamiones();
    }

    /**
     * Recarga los pedidos del sistema.
     */
    private List<Pedido> recargarPedidos() throws InvalidDataFormatException, IOException {
        System.out.println("📦 Recargando pedidos...");
        return DataLoader.initializePedidos();
    }

    /**
     * Recarga las averías del sistema.
     */
    private List<Averia> recargarAverias() throws InvalidDataFormatException, IOException {
        System.out.println("🔧 Recargando averías...");
        return DataLoader.initializeAverias();
    }

    /**
     * Recarga los mantenimientos del sistema.
     */
    private List<Mantenimiento> recargarMantenimientos() throws InvalidDataFormatException, IOException {
        System.out.println("🔧 Recargando mantenimientos...");
        return DataLoader.initializeMantenimientos();
    }

    /**
     * Recarga los bloqueos del sistema.
     */
    private void recargarBloqueos() throws InvalidDataFormatException, IOException {
        System.out.println("🚧 Recargando bloqueos...");
        DataLoader.initializeBloqueos();
    }

    /**
     * Clase interna para representar el resultado de la recarga.
     * Implementa el principio de encapsulación al agrupar datos relacionados.
     */
    public static class DataReloadResult {
        private final int cantidadAlmacenes;
        private final int cantidadCamiones;
        private final int cantidadPedidos;
        private final int cantidadAverias;
        private final int cantidadMantenimientos;
        private final int cantidadBloqueos;
        private final LocalDateTime fechaMinimaPedidos;
        private final LocalDateTime fechaMaximaPedidos;

        public DataReloadResult(int cantidadAlmacenes, int cantidadCamiones, int cantidadPedidos,
                int cantidadAverias, int cantidadMantenimientos, int cantidadBloqueos,
                LocalDateTime fechaMinimaPedidos, LocalDateTime fechaMaximaPedidos) {
            this.cantidadAlmacenes = cantidadAlmacenes;
            this.cantidadCamiones = cantidadCamiones;
            this.cantidadPedidos = cantidadPedidos;
            this.cantidadAverias = cantidadAverias;
            this.cantidadMantenimientos = cantidadMantenimientos;
            this.cantidadBloqueos = cantidadBloqueos;
            this.fechaMinimaPedidos = fechaMinimaPedidos;
            this.fechaMaximaPedidos = fechaMaximaPedidos;
        }

        // Getters
        public int getCantidadAlmacenes() {
            return cantidadAlmacenes;
        }

        public int getCantidadCamiones() {
            return cantidadCamiones;
        }

        public int getCantidadPedidos() {
            return cantidadPedidos;
        }

        public int getCantidadAverias() {
            return cantidadAverias;
        }

        public int getCantidadMantenimientos() {
            return cantidadMantenimientos;
        }

        public int getCantidadBloqueos() {
            return cantidadBloqueos;
        }

        public LocalDateTime getFechaMinimaPedidos() {
            return fechaMinimaPedidos;
        }

        public LocalDateTime getFechaMaximaPedidos() {
            return fechaMaximaPedidos;
        }

        @Override
        public String toString() {
            return String.format(
                    "DataReloadResult{almacenes=%d, camiones=%d, pedidos=%d, averias=%d, mantenimientos=%d, bloqueos=%d, fechaMinimaPedidos=%s, fechaMaximaPedidos=%s}",
                    cantidadAlmacenes, cantidadCamiones, cantidadPedidos,
                    cantidadAverias, cantidadMantenimientos, cantidadBloqueos,
                    fechaMinimaPedidos, fechaMaximaPedidos);
        }
    }
}