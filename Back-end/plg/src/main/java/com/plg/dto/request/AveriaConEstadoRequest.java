package com.plg.dto.request;

import java.time.LocalDateTime;
import java.util.List;

import com.plg.entity.TipoIncidente;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para la creación de una nueva avería con estado completo de la
 * simulación.
 * Esta clase incluye tanto los datos básicos de la avería como el estado
 * completo
 * de la simulación en el momento de la avería.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AveriaConEstadoRequest {

    // Datos básicos de la avería
    private String codigoCamion;
    private TipoIncidente tipoIncidente;
    private String fechaHoraReporte;

    // Estado completo de la simulación
    private EstadoSimulacion estadoSimulacion;

    /**
     * Clase interna que representa el estado completo de la simulación
     * en el momento de la avería.
     */
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class EstadoSimulacion {

        // Datos temporales de la simulación
        private String timestamp;
        private Integer horaActual;
        private String horaSimulacion;
        private String fechaHoraSimulacion;
        private String fechaInicioSimulacion;
        private Integer diaSimulacion;
        private String tiempoRealSimulacion;
        private String tiempoTranscurridoSimulado;

        // Estados de entidades
        private List<CamionEstado> camiones;
        private List<RutaCamion> rutasCamiones;
        private List<AlmacenSimple> almacenes;
        private List<BloqueoSimple> bloqueos;
    }

    /**
     * Clase interna que representa el estado de un camión en la simulación.
     */
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class CamionEstado {
        private String id;
        private String ubicacion;
        private Double porcentaje;
        private String estado;
        private Double capacidadActualGLP;
        private Double capacidadMaximaGLP;
        private Double combustibleActual;
        private Double combustibleMaximo;
        private Double distanciaMaxima;
        private Double pesoCarga;
        private Double pesoCombinado;
        private Double tara;
        private String tipo;
        private Double velocidadPromedio;
    }

    /**
     * Clase interna que representa la ruta de un camión con sus pedidos.
     */
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class RutaCamion {
        private String id;
        private List<String> ruta;
        private String puntoDestino;
        private List<PedidoSimple> pedidos;
    }

    /**
     * Clase interna simplificada que representa un pedido.
     */
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class PedidoSimple {
        private String codigo;
        private Integer coordenadaX;
        private Integer coordenadaY;
        private Double horasLimite;
        private Double volumenGLPAsignado;
        private String estado;
        private String fechaRegistro;
        private String fechaLimite;
    }

    /**
     * Clase interna simplificada que representa un almacén.
     */
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class AlmacenSimple {
        private Integer coordenadaX;
        private Integer coordenadaY;
        private String nombre;
        private Double capacidadActualGLP;
        private Double capacidadMaximaGLP;
        private Double capacidadCombustible;
        private Double capacidadActualCombustible;
        private Double capacidadMaximaCombustible;
        private Boolean esCentral;
        private Boolean permiteCamionesEstacionados;
        private String tipo;
        private Boolean activo;
    }

    /**
     * Clase interna simplificada que representa un bloqueo.
     */
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class BloqueoSimple {
        private List<CoordenadaSimple> coordenadas;
        private String fechaInicio;
        private String fechaFin;
    }

    /**
     * Clase interna simplificada que representa una coordenada.
     */
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class CoordenadaSimple {
        private Integer x;
        private Integer y;
    }

    /**
     * Convierte este request en un AveriaRequest básico.
     * 
     * @return AveriaRequest con los datos básicos de la avería
     */
    public AveriaRequest toAveriaRequest() {
        AveriaRequest request = new AveriaRequest();
        request.setCodigoCamion(this.codigoCamion);
        request.setTipoIncidente(this.tipoIncidente);

        // Convertir la fecha string a LocalDateTime
        if (this.fechaHoraReporte != null) {
            try {
                request.setFechaHoraReporte(LocalDateTime.parse(this.fechaHoraReporte));
            } catch (Exception e) {
                // Si hay error en el parsing, usar la fecha actual
                request.setFechaHoraReporte(LocalDateTime.now());
            }
        } else {
            request.setFechaHoraReporte(LocalDateTime.now());
        }

        request.setEstado(true);
        return request;
    }
}