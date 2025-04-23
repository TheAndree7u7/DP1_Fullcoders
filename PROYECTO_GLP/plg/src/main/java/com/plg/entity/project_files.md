# Proyecto - Contenido de Archivos

## Estructura

```
Listado de rutas de carpetas
El número de serie del volumen es 000000CC BCAB:BE00
E:\PROYECTOS\DP1\DP1_2025\DP1_FULLCODERS\PROYECTO_GLP\PLG\SRC\MAIN\JAVA\COM\PLG\ENTITY
    Almacen.java
    AsignacionCamion.java
    Averia.java
    Bloqueo.java
    Camion.java
    Cliente.java
    EntregaParcial.java
    EstadoCamion.java
    EstadoPedido.java
    Mantenimiento.java
    NodoRuta.java
    Pedido.java
    project_files.md
    Ruta.java
    
No existe ninguna subcarpeta 


```

## Almacen.java

```java
package com.plg.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;
//importar column default
import org.hibernate.annotations.ColumnDefault;
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Almacen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nombre;
    private double posX; // Posición X en el mapa
    private double posY; // Posición Y en el mapa
    
    // !Capacidades para GLPP
    private double capacidadGLP; // Capacidad total de GLP en m3
    private double capacidadActualGLP; // Capacidad actual disponible de GLP en m3
    private double capacidadMaximaGLP; // Capacidad máxima para restaurar en reabastecimiento
    
    //!Capacidades para combustible (gasolina/petróleo)
    private double capacidadCombustible; // Capacidad total de combustible en galones
    private double capacidadActualCombustible; // Capacidad actual disponible de combustible en galones 
    private double capacidadMaximaCombustible; // Capacidad máxima para restaurar en reabastecimiento
    

    //! Tipo de almacén
    private boolean esCentral; // Indica si es el almacén central (true) o intermedio (false)
    private boolean permiteCamionesEstacionados; // Solo el central permite esto por defecto
    private String tipo;
    //el tio se asigna segun el b
    // Hora de reabastecimiento para almacenes intermedios
    private LocalTime horaReabastecimiento = LocalTime.MIDNIGHT; // Por defecto a las 00:00
    private boolean ultimoReabastecimientoRealizado = false; // Indica si ya se realizó el reabastecimiento hoy
    @Column(name = "activo")
    @ColumnDefault("true")
    private boolean activo; // Estado del almacén (activo/inactivo)
     
    //!Puede recargar?
    // Método para verificar si el almacén puede recargar combustible
    public boolean puedeRecargarCombustible(double cantidadRequerida) {
        return capacidadActualCombustible >= cantidadRequerida && activo;
    }
    
    // Método para verificar si el almacén puede suplir GLP
    public boolean puedeProveerGLP(double cantidadRequerida) {
        return capacidadActualGLP >= cantidadRequerida && activo;
    }
    
    // Método para recargar combustible a un camión
    public boolean recargarCombustible(Camion camion, double cantidad) {
        if (!puedeRecargarCombustible(cantidad)) {
            return false;
        }
        
        // Verificar capacidad disponible en el tanque del camión
        double espacioDisponibleCamion = camion.getCapacidadTanque() - camion.getCombustibleActual();
        double cantidadEfectiva = Math.min(cantidad, Math.min(espacioDisponibleCamion, capacidadActualCombustible));
        
        if (cantidadEfectiva <= 0) {
            return false;
        }
        
        // Realizar la recarga
        this.capacidadActualCombustible -= cantidadEfectiva;
        camion.setCombustibleActual(camion.getCombustibleActual() + cantidadEfectiva);
        return true;
    }
    
    // Método para calcular la distancia desde este almacén hasta una posición
    public double calcularDistancia(double posX2, double posY2) {
        // Distancia Manhattan: suma de las diferencias absolutas en cada dimensión
        return Math.abs(posX - posX2) + Math.abs(posY - posY2);
    }
    
    // Método para reabastecer el almacén
    public void reabastecer() {
        if (!esCentral) {
            // Solo reabastecemos los almacenes intermedios
            this.capacidadActualGLP = this.capacidadMaximaGLP;
            this.capacidadActualCombustible = this.capacidadMaximaCombustible;
            this.ultimoReabastecimientoRealizado = true;
        }
    }
}
```

## AsignacionCamion.java

```java
package com.plg.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignacionCamion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "camion_id")
    @JsonBackReference(value="camion-asignacion")
    private Camion camion;
    
    @ManyToOne
    @JoinColumn(name = "pedido_id")
    @JsonBackReference(value="pedido-asignacion")
    private Pedido pedido;
    
    private double volumenAsignado;
    private double porcentajeAsignado;
    
    @ManyToOne
    @JoinColumn(name = "ruta_id")
    @JsonBackReference(value="ruta-asignacion")
    private Ruta ruta;
    
    private boolean entregado;
    private LocalDateTime fechaEntregaParcial;
    
    // Constructor para compatibilidad con código existente
    public AsignacionCamion(Camion camion, double volumenAsignado, double porcentajeAsignado) {
        this.camion = camion;
        this.volumenAsignado = volumenAsignado;
        this.porcentajeAsignado = porcentajeAsignado;
        this.entregado = false;
    }
}
```

## Averia.java

```java
package com.plg.entity;

import java.time.LocalDateTime;
import java.util.Random;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Averia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "camion_codigo")
    @JsonBackReference(value="camion-averia")
    private Camion camion;
    
    private LocalDateTime fechaHoraReporte;
    private String descripcion;
    private String turno; // T1, T2, T3
    private String tipoIncidente; // TI1, TI2, TI3
    private double posX;
    private double posY;
    private double kilometroOcurrencia; // Punto del trayecto donde ocurre la avería
    private int estado; // 0: reportada, 1: atendida, 2: reparada
    private boolean conCarga; // Indica si el camión llevaba carga cuando ocurrió la avería
    
    // Campos adicionales según las nuevas especificaciones
    private LocalDateTime tiempoInmovilizacion; // Tiempo que permanece inmovilizado
    private LocalDateTime tiempoFinInoperatividad; // Tiempo en que estará disponible nuevamente
    private boolean requiereTraslado; // Si debe ser trasladado al almacén (tipo 2 y 3)
    private boolean esValida; // Indica si la avería es válida (unidad en operación y con carga)
    
    /**
     * Calcula si una avería es válida según las condiciones:
     * - La unidad debe estar en operación
     * - La avería solo tiene sentido si lleva carga
     * @param estaEnOperacion indica si el camión está en operación
     * @return true si la avería es válida, false en caso contrario
     */
    public boolean calcularValidezAveria(boolean estaEnOperacion) {
        esValida = estaEnOperacion && conCarga;
        return esValida;
    }
    
    /**
     * Calcula el kilómetro de ocurrencia de la avería en el rango de 5% a 35% del tramo total
     * @param distanciaTotal la distancia total del recorrido (ida y vuelta)
     */
    public void calcularKilometroOcurrencia(double distanciaTotal) {
        if (!esValida) return;
        
        Random random = new Random();
        // Calcular kilómetro de ocurrencia entre 5% y 35% del tramo total
        double minKm = distanciaTotal * 0.05;
        double maxKm = distanciaTotal * 0.35;
        kilometroOcurrencia = minKm + (maxKm - minKm) * random.nextDouble();
    }
    
    // Método para generar el formato de registro según especificación
    public String generarRegistro() {
        if (camion == null) return "";
        return String.format("%s_%s_%s", 
                            turno, 
                            camion.getCodigo(), 
                            tipoIncidente);
    }
    
    /**
     * Método para calcular tiempo de inmovilización según tipo de incidente
     * Considera la duración de los turnos (por defecto 8 horas)
     * @param duracionTurnoHoras duración de cada turno en horas (por defecto 8)
     */
    public void calcularTiemposInoperatividad(int duracionTurnoHoras) {
        if (tipoIncidente == null || fechaHoraReporte == null) return;

        LocalDateTime ahora = fechaHoraReporte;

        switch (tipoIncidente) {
            case "TI1" -> {
                // Incidente tipo 1: inmoviliza 2 horas, continúa ruta
                tiempoInmovilizacion = ahora.plusHours(2);
                tiempoFinInoperatividad = tiempoInmovilizacion;
                requiereTraslado = false;
            }
            case "TI2" -> {
                // Incidente tipo 2: inmoviliza 2 horas + un turno completo
                tiempoInmovilizacion = ahora.plusHours(2);
                requiereTraslado = true;
                tiempoFinInoperatividad = ahora.plusHours(duracionTurnoHoras * 2);  
            }
            case "TI3" -> {
                // Incidente tipo 3: inmoviliza 4 horas + tres días completos
                tiempoInmovilizacion = ahora.plusHours(4);
                requiereTraslado = true;
                // Disponible en turno 1 del día A+3
                tiempoFinInoperatividad = ahora.plusDays(3).withHour(0).plusHours(duracionTurnoHoras);
            }
            default -> {
            }
        }
        // Tipo de incidente no reconocido, no se realiza ninguna acción
            }
    
    /**
     * Sobrecarga del método para usar la duración de turno por defecto (8 horas)
     */
    public void calcularTiemposInoperatividad() {
        calcularTiemposInoperatividad(8);
    }
}
```

## Bloqueo.java

```java
package com.plg.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bloqueo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    
    @ElementCollection
    @CollectionTable(name = "bloqueo_coordenadas", joinColumns = @JoinColumn(name = "bloqueo_id"))
    private List<Coordenada> coordenadas = new ArrayList<>();
    
    
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String descripcion;
    private boolean activo;
    
    /**
     * Método para determinar si un punto está en un tramo bloqueado
     * @param x Coordenada X a verificar
     * @param y Coordenada Y a verificar
     * @return true si el punto está en un tramo bloqueado
     */
    public boolean contienePunto(double x, double y) {
        // Si hay menos de 2 coordenadas, no hay tramos
        if (coordenadas.size() < 2) {
            return false;
        }
        
        // Verificar cada tramo (par de coordenadas consecutivas)
        for (int i = 0; i < coordenadas.size() - 1; i++) {
            Coordenada inicio = coordenadas.get(i);
            Coordenada fin = coordenadas.get(i + 1);
            
            // Verificar si el punto está en la línea entre inicio y fin
            if (estaPuntoEnLinea(x, y, inicio.getX(), inicio.getY(), fin.getX(), fin.getY())) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Verifica si un punto está en una línea dentro de un mapa reticular
     */
    private boolean estaPuntoEnLinea(double puntoX, double puntoY, double lineaInicioX, double lineaInicioY, double lineaFinX, double lineaFinY) {
        // En un mapa reticular, verificamos si el punto está exactamente en la línea
        return estaPuntoEnSegmento(puntoX, puntoY, lineaInicioX, lineaInicioY, lineaFinX, lineaFinY);
    }

    /**
     * Verifica si un punto está dentro del segmento de línea
     */
    private boolean estaPuntoEnSegmento(double puntoX, double puntoY, double segmentoInicioX, double segmentoInicioY, double segmentoFinX, double segmentoFinY) {
        // Calculamos el rango de coordenadas del segmento
        double rangoMinX = Math.min(segmentoInicioX, segmentoFinX);
        double rangoMaxX = Math.max(segmentoInicioX, segmentoFinX);
        double rangoMinY = Math.min(segmentoInicioY, segmentoFinY);
        double rangoMaxY = Math.max(segmentoInicioY, segmentoFinY);

        // Verificamos si el punto está dentro del rango del segmento
        return puntoX >= rangoMinX && puntoX <= rangoMaxX && puntoY >= rangoMinY && puntoY <= rangoMaxY;
    }
    /**
     * Convierte el bloqueo a formato de registro para archivo
     */
    public String convertirARegistro() {
        // Formato: ##d##h##m-##d##h##m:x1,y1,x2,y2,...,xn,yn
        StringBuilder registro = new StringBuilder();
        
        // Formatear fechas
        registro.append(formatearFecha(fechaInicio));
        registro.append("-");
        registro.append(formatearFecha(fechaFin));
        registro.append(":");
        
        // Añadir coordenadas
        for (int i = 0; i < coordenadas.size(); i++) {
            Coordenada coord = coordenadas.get(i);
            registro.append(coord.getX()).append(",").append(coord.getY());
            
            // Añadir coma si no es la última coordenada
            if (i < coordenadas.size() - 1) {
                registro.append(",");
            }
        }
        
        return registro.toString();
    }
    
    /**
     * Formatea una fecha en el formato ##d##h##m
     */
    private String formatearFecha(LocalDateTime fecha) {
        return String.format("%02dd%02dh%02dm", 
            fecha.getDayOfMonth(), fecha.getHour(), fecha.getMinute());
    }
        /**
     * Verifica si un segmento de ruta intersecta con este bloqueo
     * @param x1 Coordenada X del punto inicial del segmento
     * @param y1 Coordenada Y del punto inicial del segmento
     * @param x2 Coordenada X del punto final del segmento
     * @param y2 Coordenada Y del punto final del segmento
     * @return true si el segmento intersecta con algún tramo bloqueado
     */
    public boolean intersectaConSegmento(double x1, double y1, double x2, double y2) {
        // Si hay menos de 2 coordenadas, no hay tramos
        if (coordenadas.size() < 2) {
            return false;
        }
        
        // Verificar cada tramo del bloqueo (par de coordenadas consecutivas)
        for (int i = 0; i < coordenadas.size() - 1; i++) {
            Coordenada inicioBloqueo = coordenadas.get(i);
            Coordenada finBloqueo = coordenadas.get(i + 1);
            
            // Verificar si los segmentos se intersecan
            if (seIntersecaConSegmento(
                    x1, y1, x2, y2,
                    inicioBloqueo.getX(), inicioBloqueo.getY(),
                    finBloqueo.getX(), finBloqueo.getY())) {
                return true;
            }
        }
        
        // Verificar también si algún extremo del segmento está dentro del bloqueo
        if (contienePunto(x1, y1) || contienePunto(x2, y2)) {
            return true;
        }
        
        return false;
    }

    /**
     * Verifica si dos segmentos de línea se intersecan
     */
    private boolean seIntersecaConSegmento(
        double x1, double y1, double x2, double y2,
        double x3, double y3, double x4, double y4) {
        
 
        
        // Primero calculamos los denominadores para las ecuaciones
        double denominator = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
        
        // Si el denominador es 0, las líneas son paralelas
        if (denominator == 0) {
            // Verificamos si un punto del segmento 1 está en el segmento 2 (colineales)
            return estaPuntoEnSegmento(x1, y1, x3, y3, x4, y4) || 
                estaPuntoEnSegmento(x2, y2, x3, y3, x4, y4) ||
                estaPuntoEnSegmento(x3, y3, x1, y1, x2, y2) || 
                estaPuntoEnSegmento(x4, y4, x1, y1, x2, y2);
        }
        
        // Calculamos los valores de t y u
        double t = ((x3 - x1) * (y4 - y3) - (y3 - y1) * (x4 - x3)) / (double) denominator;
        double u = ((x3 - x1) * (y2 - y1) - (y3 - y1) * (x2 - x1)) / (double) denominator;
        
        // Si t y u están entre 0 y 1, los segmentos se intersecan
        return t >= 0 && t <= 1 && u >= 0 && u <= 1;
    }
    // Clase interna para representar una coordenada
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Coordenada {
        private double x;
        private double y;
    }
    
}
```

## Camion.java

```java
package com.plg.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "camiones")
public class Camion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ID único del camión

    private String codigo;
    
    private String tipo; // TA, TB, TC, TD, etc.

    //!GLP
    private double capacidad; // Capacidad en m3 de GLP
    private double capacidadDisponible; // Capacidad disponible actual (m3)
    private double tara; // Peso del camión vacío en toneladas
    private double pesoCarga; // Peso actual de la carga en toneladas
    private double pesoCombinado; // Peso total (tara + carga)
    
 
    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoCamion estado; 
    
    //!combustible Atributos relacionados con 
    @Column(name = "capacidad_tanque")
    private double capacidadTanque = 25.0; // Capacidad del tanque en galones
    
    @Column(name = "combustible_actual")
    private double combustibleActual; // Combustible actual en galones
    
    @Column(name = "velocidad_promedio")
    private double velocidadPromedio = 50.0; // Velocidad promedio en km/h
    
    // Posición actual del camión (para calcular distancia a recorrer)
    @Column(name = "pos_x")
    private double posX;
    
    @Column(name = "pos_y")
    private double posY;
    
    // Último almacén visitado
    @ManyToOne
    @JoinColumn(name = "ultimo_almacen_id")
    private Almacen ultimoAlmacen;
    
    // Fecha de la última carga de GLP
    @Column(name = "fecha_ultima_carga")
    private LocalDateTime fechaUltimaCarga;
    
    // Porcentaje de uso actual
    @Column(name = "porcentaje_uso")
    private double porcentajeUso;
    
    @OneToMany(mappedBy = "camion", cascade = CascadeType.ALL)
    @JsonManagedReference(value="camion-mantenimiento")
    private List<Mantenimiento> mantenimientos = new ArrayList<>();
    
    @OneToMany(mappedBy = "camion", cascade = CascadeType.ALL)
    @JsonManagedReference(value="camion-averia")
    private List<Averia> averias = new ArrayList<>();
    
    @OneToMany(mappedBy = "camion", cascade = CascadeType.ALL)
    @JsonManagedReference(value="camion-pedido")
    private List<Pedido> pedidos = new ArrayList<>();
    
    @OneToMany(mappedBy = "camion", cascade = CascadeType.ALL)
    @JsonManagedReference(value="camion-ruta")
    private List<Ruta> rutas = new ArrayList<>();
    
    @OneToMany(mappedBy = "camion", cascade = CascadeType.ALL)
    @JsonManagedReference(value="camion-entregaparcial")
    private List<EntregaParcial> entregasParciales = new ArrayList<>();
    
    /**
     * Constructor con parámetros básicos
     */
    public Camion(String codigo, String tipo, double capacidad, double tara) {
        this.codigo = codigo;
        this.tipo = tipo;
        this.capacidad = capacidad;
        this.capacidadDisponible = capacidad;
        this.tara = tara;
        this.setEstado(EstadoCamion.DISPONIBLE); // Disponible por defecto
        this.porcentajeUso = 0.0;
        inicializar();
    }
 
 

    /**
     * Asigna un volumen parcial de GLP de un pedido a este camión
     * @param pedido Pedido a asignar
     * @param volumen Volumen a entregar (en m3)
     * @param porcentaje Porcentaje del pedido que representa
     * @return true si se pudo asignar, false si no hay capacidad suficiente
     */
    public boolean asignarPedidoParcial(Pedido pedido, double volumen, double porcentaje) {
        // Verificar si hay capacidad disponible
        if (capacidadDisponible < volumen) {
            return false;
        }
        
        // Actualizar capacidad disponible
        capacidadDisponible -= volumen;
        
        // Actualizar porcentaje de uso
        actualizarPorcentajeUso();
        
        // Actualizar peso de carga y combinado
        actualizarPeso();
        
        // Crear nueva entrega parcial
        EntregaParcial entrega = new EntregaParcial();
        entrega.setCamion(this);
        entrega.setPedido(pedido);
        entrega.setVolumenGLP(volumen);
        entrega.setPorcentajePedido(porcentaje);
        entrega.setFechaAsignacion(LocalDateTime.now());
        entrega.setEstado(0); // Asignado
        
        // Agregar a la lista de entregas parciales
        entregasParciales.add(entrega);
        
        return true;
    }
    
    /**
     * Libera capacidad después de una entrega
     * @param volumen Volumen liberado (en m3)
     */
    public void liberarCapacidad(double volumen) {
        capacidadDisponible += volumen;
        if (capacidadDisponible > capacidad) {
            capacidadDisponible = capacidad;
        }
        
        // Actualizar porcentaje de uso
        actualizarPorcentajeUso();
        
        // Actualizar peso después de liberar capacidad
        actualizarPeso();
    }
    
    /**
     * Actualiza el porcentaje de uso
     */
    private void actualizarPorcentajeUso() {
        porcentajeUso = ((capacidad - capacidadDisponible) / capacidad) * 100;
    }
    
    /**
     * Actualiza el peso de carga y combinado
     * El peso del GLP es aproximadamente 0.55 ton/m3
     */
    private void actualizarPeso() {
        this.pesoCarga = (capacidad - capacidadDisponible) * 0.5; // Peso del GLP en toneladas
        this.pesoCombinado = tara + pesoCarga;
    }
    
    /**
     * Realiza una recarga   de GLP
     */
    public void recargarGLP(double volumenGLP) {
        capacidadDisponible += volumenGLP;
        if (capacidadDisponible > capacidad) {
            capacidadDisponible = capacidad;
        }
        
        // Actualizar porcentaje de uso
        actualizarPorcentajeUso();
        
        // Actualizar peso después de recargar
        actualizarPeso();
    }
    
    /**
     * Realiza una recarga de combustible
     * @param cantidadGalones Cantidad a recargar en galones
     */
    public void recargarCombustible(double cantidadGalones) {
        combustibleActual += cantidadGalones;
        if (combustibleActual > capacidadTanque) {
            combustibleActual = capacidadTanque;
        }
        
        // Si estaba sin combustible, actualizar su estado
        if (getEstado() == EstadoCamion.SIN_COMBUSTIBLE) {
            setEstado(EstadoCamion.DISPONIBLE);
        }
    }
    
    /**
     * Consume combustible durante un recorrido
     * @param cantidadGalones Cantidad a consumir en galones
     * @return true si se pudo consumir, false si no hay suficiente
     */
    public boolean consumirCombustible(double cantidadGalones) {
        if (combustibleActual < cantidadGalones) {
            return false;
        }
        
        combustibleActual -= cantidadGalones;
        
        // Si se quedó sin combustible, actualizar su estado
        if (combustibleActual <= 0.1) {
            setEstado(EstadoCamion.SIN_COMBUSTIBLE);
        }
        
        return true;
    }
    
    /**
     * Obtiene las entregas parciales pendientes
     */
    public List<EntregaParcial> getEntregasPendientes() {
        List<EntregaParcial> pendientes = new ArrayList<>();
        for (EntregaParcial entrega : entregasParciales) {
            if (entrega.getEstado() != 2) { // No entregado
                pendientes.add(entrega);
            }
        }
        
        return pendientes;
    }
    
    /**
     * Obtiene el volumen total de GLP asignado actualmente
     */
    public double getVolumenTotalAsignado() {
        double total = 0.0;
        for (EntregaParcial entrega : entregasParciales) {
            if (entrega.getEstado() != 2) { // No entregado
                total += entrega.getVolumenGLP();
            }
        }
        
        return total;
    }
    
    /**
     * Marca una entrega parcial como completada
     */
    public boolean completarEntregaParcial(Long pedidoId) {
        for (EntregaParcial entrega : entregasParciales) {
            if (entrega.getPedido().getId().equals(pedidoId) && entrega.getEstado() != 2) {
                entrega.setEstado(2); // Entregado
                entrega.setFechaEntrega(LocalDateTime.now());
                liberarCapacidad(entrega.getVolumenGLP());
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Verifica si el camión tiene capacidad para un volumen adicional
     */
    public boolean tieneCapacidadPara(double volumenAdicional) {
        return capacidadDisponible >= volumenAdicional;
    }
    
    /**
     * Calcula consumo de combustible para una distancia
     * @param distanciaKm Distancia a recorrer en kilómetros
     * @return Consumo en galones
     */
    public double calcularConsumoCombustible(double distanciaKm) {
        return distanciaKm * pesoCombinado / 180.0;
    }
    
    /**
     * Calcula la distancia máxima que puede recorrer con el combustible actual
     * @return Distancia máxima en kilómetros
     */
    public double calcularDistanciaMaxima() {
        if (pesoCombinado <= 0) {
            return 0.0; // Evitar división por cero
        }
        return combustibleActual * 180.0 / pesoCombinado;
    }
    
    /**
     * Inicializa el camión con valores por defecto
     */
    public void inicializar() {
        if (capacidadDisponible <= 0) {
            capacidadDisponible = capacidad;
        }
        
        if (combustibleActual <= 0) {
            combustibleActual = capacidadTanque * 1; // Inicializa con 100% del tanque
        }
        
        actualizarPorcentajeUso();
        actualizarPeso();
    }
    
    /**
     * Reporta una avería y cambia el estado del camión
     */
    public Averia reportarAveria(String descripcion) {
        Averia averia = new Averia();
        averia.setCamion(this);
        averia.setDescripcion(descripcion);
        averia.setFechaHoraReporte(LocalDateTime.now());
        averia.setEstado(0); // Pendiente
        
        this.setEstado(EstadoCamion.EN_MANTENIMIENTO_POR_AVERIA); // Averiado
        
        if (this.averias == null) {
            this.averias = new ArrayList<>();
        }
        this.averias.add(averia);
        
        return averia;
    }
    
 
    /**
     * Mover el camión a nuevas coordenadas
     */
    public void moverA(int nuevoX, int nuevoY) {
        this.posX = nuevoX;
        this.posY = nuevoY;
    }
    
    /**
     * Calcula la distancia desde la posición actual hasta un punto
     * usando distancia Manhattan
     */
    public double calcularDistanciaHasta(int destinoX, int destinoY) {
        return Math.abs(destinoX - this.posX) + Math.abs(destinoY - this.posY);
    }
    
    /**
     * Obtiene el estado del camión como texto
     */
    @Transient
    public String getEstadoTexto() {
        return getEstado().getDescripcion();
    }
    
    /**
     * Actualiza el estado de las entregas parciales cuando la ruta está en curso
     */
    public void actualizarEstadoEntregasARuta() {
        for (EntregaParcial entrega : entregasParciales) {
            if (entrega.getEstado() == 0) { // Si está asignada
                entrega.setEstado(1); // Cambiar a "En ruta"
            }
        }
    }
    
    /**
     * Obtiene información básica del camión para APIs
     */
    @Transient
    public Map<String, Object> getInfoBasica() {
        Map<String, Object> info = new HashMap<>();
        info.put("codigo", this.codigo);
        info.put("tipo", this.tipo);
        info.put("capacidad", this.capacidad);
        info.put("capacidadDisponible", this.capacidadDisponible);
        info.put("porcentajeUso", this.porcentajeUso); 
        info.put("estado", this.estado);
        info.put("estadoTexto", this.getEstadoTexto());
        info.put("posX", this.posX);
        info.put("posY", this.posY);
        info.put("combustibleActual", this.combustibleActual);
        info.put("distanciaMaxima", this.calcularDistanciaMaxima());
        return info;
    }
    
    /**
     * Obtiene información detallada de las entregas parciales para APIs
     */
    @Transient
    public List<Map<String, Object>> getInfoEntregasParciales() {
        List<Map<String, Object>> listaEntregas = new ArrayList<>();
        
        for (EntregaParcial entrega : entregasParciales) {
            Map<String, Object> infoEntrega = new HashMap<>();
            infoEntrega.put("id", entrega.getId());
            infoEntrega.put("pedidoId", entrega.getPedido().getId());
            infoEntrega.put("codigoPedido", entrega.getPedido().getCodigo());
            infoEntrega.put("volumenGLP", entrega.getVolumenGLP());
            infoEntrega.put("porcentaje", entrega.getPorcentajePedido());
            infoEntrega.put("estado", entrega.getEstado());
            
            switch (entrega.getEstado()) {
                case 0:
                    infoEntrega.put("estadoTexto", "Asignado");
                    break;
                case 1:
                    infoEntrega.put("estadoTexto", "En ruta");
                    break;
                case 2:
                    infoEntrega.put("estadoTexto", "Entregado");
                    break;
                case 3:
                    infoEntrega.put("estadoTexto", "Cancelado");
                    break;
                default:
                    infoEntrega.put("estadoTexto", "Desconocido");
            }
            
            if (entrega.getFechaAsignacion() != null) {
                infoEntrega.put("fechaAsignacion", entrega.getFechaAsignacion().toString());
            }
            
            if (entrega.getFechaEntrega() != null) {
                infoEntrega.put("fechaEntrega", entrega.getFechaEntrega().toString());
            }
            
            listaEntregas.add(infoEntrega);
        }
        
        return listaEntregas;
    }
}
```

## Cliente.java

```java
package com.plg.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Cliente {
    @Id
    private String id; // código único del cliente
    
    private String nombre;
    private String direccion;
    private String telefono;
    private String email;
    private double posX;
    private double posY;
    
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    @JsonManagedReference(value="cliente-pedido")
    private List<Pedido> pedidos;
}
```

## EntregaParcial.java

```java
package com.plg.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidad que representa una entrega parcial de un pedido.
 * Permite que un pedido pueda ser entregado en partes por diferentes camiones.
 */
@Entity
@AllArgsConstructor
@Getter
@Setter
@Table(name = "entregas_parciales")
public class EntregaParcial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "camion_id")
    @JsonBackReference(value="camion-entregaparcial")
    private Camion camion;
    
    @ManyToOne
    @JoinColumn(name = "pedido_id")
    @JsonBackReference(value="pedido-entregaparcial")
    private Pedido pedido;
    
    @Column(name = "volumen_glp")
    private double volumenGLP; // Volumen asignado para esta entrega parcial
    
    @Column(name = "porcentaje_pedido")
    private double porcentajePedido; // Porcentaje del pedido que representa esta entrega
    
    @Column(name = "fecha_asignacion")
    private LocalDateTime fechaAsignacion;
    
    @Column(name = "fecha_entrega")
    private LocalDateTime fechaEntrega;
    
    @Column(name = "estado")
    private int estado; // 0: Asignado, 1: En ruta, 2: Entregado, 3: Cancelado
    
    @Column(name = "observaciones")
    private String observaciones;
    
    /**
     * Constructor por defecto
     */
    public EntregaParcial() {
        this.fechaAsignacion = LocalDateTime.now();
        this.estado = 0; // Asignado por defecto
    }
    
    /**
     * Constructor con información básica
     */
    public EntregaParcial(Pedido pedido, double volumenGLP, double porcentajePedido) {
        this();
        this.pedido = pedido;
        this.volumenGLP = volumenGLP;
        this.porcentajePedido = porcentajePedido;
    } 
  
  
    /**
     * Builder pattern para crear instancias de EntregaParcial
     */
    public static EntregaParcialBuilder builder() {
        return new EntregaParcialBuilder();
    }
    
    /**
     * Builder para EntregaParcial
     */
    public static class EntregaParcialBuilder {
        private Pedido pedido;
        private double volumenGLP;
        private double porcentajePedido;
        private int estado;
        private String observaciones;
        
        public EntregaParcialBuilder pedido(Pedido pedido) {
            this.pedido = pedido;
            return this;
        }
        
        public EntregaParcialBuilder volumenGLP(double volumenGLP) {
            this.volumenGLP = volumenGLP;
            return this;
        }
        
        public EntregaParcialBuilder porcentajePedido(double porcentajePedido) {
            this.porcentajePedido = porcentajePedido;
            return this;
        }
        
        public EntregaParcialBuilder estado(int estado) {
            this.estado = estado;
            return this;
        }
        
        public EntregaParcialBuilder observaciones(String observaciones) {
            this.observaciones = observaciones;
            return this;
        }
        
        public EntregaParcial build() {
            EntregaParcial entrega = new EntregaParcial(pedido, volumenGLP, porcentajePedido);
            if (estado != 0) {
                entrega.setEstado(estado);
            }
            if (observaciones != null) {
                entrega.setObservaciones(observaciones);
            }
            return entrega;
        }
    }
}
```

## EstadoCamion.java

```java
package com.plg.entity;

public enum EstadoCamion {

    DISPONIBLE("Camión listo para operar y sin ninguna entrega en progreso", "#00FF00"),
    NO_DISPONIBLE("Camión no listo para operar", "#FF0000"),
    EN_RUTA("Camión actualmente en camino a realizar una entrega en movimiento", "#0000FF"),

    ENTREGANDO_GLP_A_CLIENTE("Camión en proceso de descarga de GLP al cliente", "#0066CC"),

    //!MANTENIMIENTO
    EN_MANTENIMIENTO("Camión en mantenimiento por diferentes motivos", "#000000"),
    
    EN_MANTENIMIENTO_PREVENTIVO("Mantenimiento preventivo programado (1 día)", "#FFCC00"),

    EN_MANTENIMIENTO_CORRECTIVO("Aun no especificado por el profesor", "#FF9900"),

    EN_MANTENIMIENTO_POR_AVERIA("Camión fuera de operación por avería (taller)", "#990000"), 
    //!INMOVILIZADO
    INMOVILIZADO_POR_AVERIA("Detenido en nodo por avería menor o incidente (2h o 4h)", "#CC3300"),

    SIN_COMBUSTIBLE("Camión sin gasolina suficiente para continuar", "#808080"),

    //!RECARGANDO_O_ENTREGANDO_COMBUSTIBLE
    RECIBIENDO_COMBUSTIBLE("Recargando gasolina (combustible de motor) en planta central o intermedio o desde otro camion ", "#6666FF"),
    
	ENTREGANDO_COMBUSTIBLE_A_CAMION(" Dando combustible a otrocamion ", "#6666FF"),

    RECIBIENDO_GLP("Recargando GLP para entregas en planta central o intermedio o desde otro camion", "#66CC00"),

    ENTREGANDO_GLP_A_CAMION("Transfiriendo GLP hacia  otro camión", "#33CCCC"), 

    ALMACEN_TEMPORAL("Unidad averiada actuando como depósito temporal de GLP", "#9933CC");

    private final String descripcion;
    private final String colorHex;

    EstadoCamion(String descripcion, String colorHex) {
        this.descripcion = descripcion;
        this.colorHex = colorHex;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getColorHex() {
        return colorHex;
    }
}

```

## EstadoPedido.java

```java
package com.plg.entity;

 
public enum EstadoPedido {
    //!SIN_ASIGNAR
    SIN_ASIGNAR("Pedido sin asignar, ya que recien seregistro el pedido", "#000000"),
    REGISTRADO(
            "Pedido ingresado. Aún no planificado", "#CCCCCC"),

    PENDIENTE_PLANIFICACION("En espera de planificación", "#FFCC00"),

    //!PLANIFICADO
    PLANIFICADO("Pedido planificado ya sea total o parcialmente", "#009900"),  

    PLANIFICADO_PARCIALMENTE("Solo una parte del pedido ha sido planificada", "#FFDD66"),

    PLANIFICADO_TOTALMENTE("El 100% del pedido ha sido planificado", "#00BFFF"),

    EN_RUTA("Pedido en tránsito hacia el cliente", "#3399FF"),

    //!RECIBIENDO GLP
    RECIBIENDO("El cliente está recibiendo el GLP solicitado ya sea todo el GLP o de forma parcialmente ", "#FFCC33"),
    RECIBIENDO_PARCIALMENTE("El cliente está recibiendo una parte del GLP solicitado", "#6699FF"),

    RECIBIENDO_TOTALMENTE("El cliente está recibiendo todo el GLP del pedido", "#0066CC"),
    //!ENTREGADO
    ENTREGADO_PARCIALMENTE("Entrega parcial completada. Aún falta parte del pedido", "#FF9966"),

    ENTREGADO_TOTALMENTE("Pedido completado. Se entregó el 100%", "#00CC66"),

    REPROGRAMADO("El pedido fue replanificado por logística o incidente", "#FF9900"),

    NO_ENTREGADO_EN_TIEMPO("El pedido no se cumplió en el plazo indicado", "#FF3333");

    private final String descripcion;
    private final String colorHex;

    EstadoPedido(String descripcion, String colorHex) {
        this.descripcion = descripcion;
        this.colorHex = colorHex;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getColorHex() {
        return colorHex;
    }
}

```

## Mantenimiento.java

```java
package com.plg.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
 
import java.time.LocalDate;

@Entity
@Data
@Getter
@Setter
@Builder
@Table(name = "mantenimiento")
@NoArgsConstructor
@AllArgsConstructor
public class Mantenimiento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "camion_codigo")
    @JsonBackReference(value="camion-mantenimiento")
    private Camion camion;
    
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String tipo; // preventivo, correctivo
    private String descripcion;
    private int estado; // 0: programado, 1: en proceso, 2: finalizado
    
    
}
```

## NodoRuta.java

```java
package com.plg.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidad que representa un punto o nodo en una ruta.
 * Un nodo puede ser un punto de origen (almacén), destino (cliente) o punto intermedio.
 */
@Getter
@Setter
@Entity
@Table(name = "nodos_ruta")
public class NodoRuta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "ruta_id")
    @JsonBackReference(value="ruta-nodo")
    private Ruta ruta;
    
    @Column(name = "orden")
    private int orden;
    
    @Column(name = "pos_x")
    private double posX;
    
    @Column(name = "pos_y")
    private double posY;
    
    @Column(name = "tipo")
    private String tipo; // "ALMACEN", "CLIENTE", "INTERMEDIO"
    
    @ManyToOne
    @JoinColumn(name = "pedido_id")
    @JsonBackReference(value="pedido-nodo")
    private Pedido pedido; // Solo para nodos de tipo "CLIENTE"
    
    @Column(name = "volumen_glp")
    private double volumenGLP; // Volumen a entregar en este nodo
    
    @Column(name = "porcentaje_pedido")
    private double porcentajePedido; // Porcentaje del pedido que se entrega en este nodo
    
    @Column(name = "entregado")
    private boolean entregado;
    
    @Column(name = "tiempo_llegada_estimado")
    private LocalDateTime tiempoLlegadaEstimado;
    
    @Column(name = "tiempo_llegada_real")
    private LocalDateTime tiempoLlegadaReal;
    
    @Column(name = "observaciones")
    private String observaciones;
    
    /**
     * Constructor por defecto
     */
    public NodoRuta() {
        this.entregado = false;
    }
    
    /**
     * Constructor con coordenadas y tipo
     */
    public NodoRuta(double posX, double posY, String tipo) {
        this();
        this.posX = posX;
        this.posY = posY;
        this.tipo = tipo;
    }
    
    /**
     * Constructor con todos los campos relevantes para un nodo de cliente
     */
    public NodoRuta(double posX, double posY, String tipo, Pedido pedido, double volumenGLP, double porcentajePedido) {
        this(posX, posY, tipo);
        this.pedido = pedido;
        this.volumenGLP = volumenGLP;
        this.porcentajePedido = porcentajePedido;
    }
    
    
     
    /**
     * Calcula la distancia Manhattan a otro nodo
     */
    public double distanciaA(NodoRuta otro) {
        return Math.abs(this.posX - otro.posX) + Math.abs(this.posY - otro.posY);
    }
    
    /**
     * Convierte a representación de cadena
     */
    @Override
    public String toString() {
        return "NodoRuta{" +
               "id=" + id +
               ", orden=" + orden +
               ", pos=(" + posX + "," + posY + ")" +
               ", tipo='" + tipo + '\'' +
               (pedido != null ? ", pedidoId=" + pedido.getId() : "") +
               '}';
    }
}
```

## Pedido.java

```java
package com.plg.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "pedidos")
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String codigo;
    
    @ManyToOne
    @JoinColumn(name = "cliente_id")
    @JsonBackReference(value="cliente-pedido")
    private Cliente cliente;
    private double posX; // Coordenada X del cliente
    private double posY; // Coordenada Y del cliente
    //horas limite
    private double horasLimite; // Hora límite para la entrega (en horas)
    //m3
    //fecha creacion
    // private LocalDateTime fechaCreacion; // Fecha de creación del pedido
    //fecha entrega
    private LocalDateTime fechaRegistro; // Fecha de registro del pedido
    
    private LocalDateTime fechaEntregaRequerida;
    private LocalDateTime fechaEntregaReal;
    
    private double volumenGLPAsignado; // Volumen total requerido (m3)
    private double volumenGLPEntregado; // Volumen ya entregado (m3)
    private double volumenGLPPendiente; // Volumen restante por asignar (m3) 
    private int prioridad; // 1: alta, 2: media, 3: baja
    
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "estado")
    private EstadoPedido estado;
    
 

    private String fechaHora; //formato "ddmmyyyy hh:mm:ss"
    private String fechaAsignaciones; //formato "ddmmyyyy hh:mm:ss" 
     
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
    @JsonManagedReference(value="pedido-entregaparcial")
    private List<EntregaParcial> entregasParciales = new ArrayList<>();
    
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
    @JsonManagedReference(value="pedido-nodo")
    private List<NodoRuta> nodos = new ArrayList<>();
    
    @ManyToOne
    @JoinColumn(name = "camion_id")
    @JsonBackReference(value="camion-pedido")
    private Camion camion;
    //Asignaciones
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
    @JsonManagedReference(value="pedido-asignacion")
    private List<AsignacionCamion> asignaciones = new ArrayList<>();

 

    /**
     * Asigna una parte del pedido a un camión
     * @param camion Camión al que se asigna
     * @param volumen Volumen asignado en m3
     * @return true si se pudo asignar, false si no hay volumen pendiente suficiente
     */
    public boolean asignarACamion(Camion camion, double volumen) {
        // Validar que haya volumen pendiente y que el camión tenga capacidad
        if (volumenGLPAsignado - volumenGLPEntregado < volumen || !camion.tieneCapacidadPara(volumen)) {
            return false;
        }
        
        // Calcular el porcentaje que representa del total
        double porcentaje = (volumen / volumenGLPAsignado) * 100;
        
        // Crear y agregar la asignación
        AsignacionCamion asignacion = new AsignacionCamion(camion, volumen, porcentaje);
        asignacion.setPedido(this);
        
        // Actualizar los volúmenes
        volumenGLPEntregado += volumen;
        
        // Asignar el volumen al camión
        camion.asignarPedidoParcial(this, volumen, porcentaje);
        
        // Actualizar estado del pedido
        actualizarEstadoDePedido();
        
        return true;
    }
    
    /**
     * Registra la entrega de una parte del pedido por un camión
     */
    public boolean registrarEntregaParcial(String codigoCamion, double volumenEntregado, LocalDateTime fechaEntrega) {
        if (volumenEntregado > volumenGLPAsignado) {
            return false; // No puede entregar más de lo solicitado
        }
        
        // Actualizar volúmenes
        this.volumenGLPEntregado += volumenEntregado;
        
        // Liberar capacidad del camión
        if (camion != null) {
            camion.liberarCapacidad(volumenEntregado);
        }
        
        // Actualizar estado del pedido
        actualizarEstadoDePedido();
        
        // Si el pedido está completamente entregado, actualizar la fecha de entrega
        if (estado == EstadoPedido.ENTREGADO_TOTALMENTE) {
            this.fechaEntregaReal = fechaEntrega;
        }
        
        return true;
    }
    
    /**
     * Actualiza el estado del pedido según las entregas
     */
    private void actualizarEstadoDePedido() {
        if (volumenGLPEntregado == 0) {
            estado = EstadoPedido.REGISTRADO;
        } else if (volumenGLPEntregado < volumenGLPAsignado) {
            // Si está parcialmente entregado
            if (volumenGLPEntregado > 0) {
                estado = EstadoPedido.ENTREGADO_PARCIALMENTE;
            } else {
                estado = EstadoPedido.PENDIENTE_PLANIFICACION;
            }
        } else if (volumenGLPEntregado == volumenGLPAsignado) {
            estado = EstadoPedido.ENTREGADO_TOTALMENTE;
        }
        
        // Actualizar el estadoInt para mantener compatibilidad
         
    }
    
    /**
     * Cancela el pedido, liberando capacidad del camión asignado
     */
    public void cancelar() {
        if (camion != null) {
            camion.liberarCapacidad(volumenGLPAsignado - volumenGLPEntregado);
        }
        
        estado = EstadoPedido.NO_ENTREGADO_EN_TIEMPO;
        
    }
    
    /**
     * Obtiene el porcentaje total entregado del pedido
     */
    public double getPorcentajeEntregado() {
        return (volumenGLPEntregado / volumenGLPAsignado) * 100;
    }
    
    /**
     * Verifica si el pedido está completamente entregado
     */
    public boolean isCompletamenteEntregado() {
        return Math.abs(volumenGLPEntregado - volumenGLPAsignado) < 0.01; // Comparación con tolerancia
    }
    
    /**
     * Método de utilidad para obtener la descripción del estado
     */
    @Transient
    public String getEstadoTexto() {
        return estado != null ? estado.getDescripcion() : "Desconocido";
    }
    
    /**
     * Método de utilidad para obtener el color asociado al estado
     */
    @Transient
    public String getEstadoColorHex() {
        return estado != null ? estado.getColorHex() : "#CCCCCC"; // Color por defecto
    }
}
```

## Ruta.java

```java
package com.plg.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa una ruta en el sistema.
 * Una ruta consiste en una secuencia ordenada de nodos que forman un camino
 * desde un origen hasta un destino, pasando por puntos intermedios.
 * 
 * La ruta permite gestionar entregas parciales de GLP a cada cliente,
 * permitiendo que un pedido pueda ser atendido por diferentes camiones
 * con porcentajes variables de la cantidad total solicitada.
 */
@Entity
@Table(name = "rutas")
@Getter
@Setter
@NoArgsConstructor
public class Ruta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", unique = true)
    private String codigo;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @ManyToOne
    @JoinColumn(name = "camion_id")
    @JsonBackReference(value="camion-ruta")
    private Camion camion;

    @Column(name = "estado")
    private int estado; // 0: Planificada, 1: En curso, 2: Completada, 3: Cancelada

    @Column(name = "distancia_total")
    private double distanciaTotal;
    
    @Column(name = "tiempo_estimado")
    private double tiempoEstimadoMinutos;
    
    @Column(name = "considera_bloqueos")
    private boolean consideraBloqueos;
    
    @Column(name = "volumen_total_glp")
    private double volumenTotalGLP;
    
    @Column(name = "capacidad_utilizada_porcentaje")
    private double capacidadUtilizadaPorcentaje;
    
    @Column(name = "fecha_inicio_ruta")
    private LocalDateTime fechaInicioRuta;
    
    @Column(name = "fecha_fin_ruta")
    private LocalDateTime fechaFinRuta;
    
    @OneToMany(mappedBy = "ruta", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "orden")
    @JsonManagedReference(value="ruta-nodo")
    private List<NodoRuta> nodos = new ArrayList<>();
    
    @OneToMany(mappedBy = "ruta", cascade = CascadeType.ALL)
    @JsonManagedReference(value="ruta-asignacion")
    private List<AsignacionCamion> asignaciones = new ArrayList<>();
    
    // Para almacenar IDs de bloqueos que afectan a esta ruta
    @Column(name = "bloqueos_ids", length = 255)
    private String bloqueosIds;
    
    /**
     * Constructor con código
     */
    public Ruta(String codigo) {
        this.codigo = codigo;
        this.fechaCreacion = LocalDateTime.now();
        this.estado = 0; // Planificada por defecto
        this.volumenTotalGLP = 0.0;
        this.capacidadUtilizadaPorcentaje = 0.0;
        this.consideraBloqueos = true; // Por defecto consideramos bloqueos
    }
    
    /**
     * Añade un nodo a la ruta
     */
    public void agregarNodo(double posX, double posY, String tipo) {
        NodoRuta nodo = new NodoRuta(posX, posY, tipo);
        nodo.setRuta(this);
        nodo.setOrden(nodos.size());
        nodos.add(nodo);
    }
    
    /**
     * Añade un nodo a la ruta con un pedido asociado y una entrega parcial
     */
    public void agregarNodoCliente(double posX, double posY, Pedido pedido, double volumenGLP, double porcentajePedido) {
        NodoRuta nodo = new NodoRuta(posX, posY, "CLIENTE");
        nodo.setRuta(this);
        nodo.setOrden(nodos.size());
        nodo.setPedido(pedido);
        nodo.setVolumenGLP(volumenGLP);
        nodo.setPorcentajePedido(porcentajePedido);
        nodos.add(nodo);
        
        // Actualizar el volumen total de GLP de la ruta
        this.volumenTotalGLP += volumenGLP;
        
        // Actualizar porcentaje de capacidad utilizada del camión
        actualizarCapacidadUtilizada();
    }
    
    /**
     * Método sobrecargado para mantener compatibilidad con código existente
     */
    public void agregarNodoCliente(int posX, int posY, Pedido pedido) {
        // Si no especificamos volumen ni porcentaje, asumimos que se entrega el pedido completo (100%)
        agregarNodoCliente(posX, posY, pedido, pedido.getVolumenGLPAsignado(), 100.0);
    }
    
    /**
     * Actualiza la información de entrega de un nodo específico
     */
    public boolean actualizarEntregaPedido(Long pedidoId, double nuevoVolumenGLP, double nuevoPorcentaje) {
        for (NodoRuta nodo : nodos) {
            if (nodo.getPedido() != null && nodo.getPedido().getId().equals(pedidoId)) {
                // Restar el volumen antiguo del total
                this.volumenTotalGLP -= nodo.getVolumenGLP();
                
                // Actualizar nodo
                nodo.setVolumenGLP(nuevoVolumenGLP);
                nodo.setPorcentajePedido(nuevoPorcentaje);
                
                // Actualizar el total
                this.volumenTotalGLP += nuevoVolumenGLP;
                
                // Recalcular el porcentaje de capacidad utilizada
                actualizarCapacidadUtilizada();
                
                return true;
            }
        }
        return false;
    }
    
    /**
     * Registra la entrega de un pedido
     */
    public boolean marcarPedidoComoEntregado(Long pedidoId, LocalDateTime fechaEntrega, String observaciones) {
        for (NodoRuta nodo : nodos) {
            if (nodo.getPedido() != null && nodo.getPedido().getId().equals(pedidoId)) {
                nodo.setEntregado(true);
                nodo.setTiempoLlegadaReal(fechaEntrega);
                nodo.setObservaciones(observaciones);
                
                // Si el camión está presente, liberamos su capacidad
                if (camion != null) {
                    camion.liberarCapacidad(nodo.getVolumenGLP());
                    // Actualizar EntregaParcial relacionada
                    camion.completarEntregaParcial(pedidoId);
                }
                
                return true;
            }
        }
        return false;
    }
    
    /**
     * Actualiza el porcentaje de capacidad utilizada del camión
     */
    private void actualizarCapacidadUtilizada() {
        if (camion != null && camion.getCapacidad() > 0) {
            this.capacidadUtilizadaPorcentaje = (this.volumenTotalGLP / camion.getCapacidad()) * 100;
        }
    }
    
    /**
     * Verifica si el camión tiene capacidad suficiente para la cantidad de GLP asignada
     */
    public boolean verificarCapacidadSuficiente() {
        if (camion == null) return false;
        return camion.getCapacidad() >= volumenTotalGLP;
    }
    
    /**
     * Obtiene las entregas pendientes de esta ruta
     */
    @Transient
    public List<NodoRuta> getEntregasPendientes() {
        return nodos.stream()
            .filter(n -> n.getPedido() != null && !n.isEntregado())
            .collect(Collectors.toList());
    }
    
    /**
     * Obtiene información agrupada por pedido, incluyendo las entregas parciales
     */
    @Transient
    public List<Map<String, Object>> getInfoEntregasPorPedido() {
        Map<Long, Map<String, Object>> infoPorPedido = new HashMap<>();
        
        for (NodoRuta nodo : nodos) {
            if (nodo.getPedido() != null) {
                Long pedidoId = nodo.getPedido().getId();
                
                // Si no existe entrada para este pedido, crear una nueva
                if (!infoPorPedido.containsKey(pedidoId)) {
                    Map<String, Object> info = new HashMap<>();
                    info.put("pedidoId", pedidoId);
                    info.put("codigoPedido", nodo.getPedido().getCodigo());
                    info.put("volumenTotalPedido", nodo.getPedido().getVolumenGLPAsignado());
                    info.put("volumenAsignado", 0.0);
                    info.put("porcentajeAsignado", 0.0);
                    info.put("entregas", new ArrayList<Map<String, Object>>());
                    
                    infoPorPedido.put(pedidoId, info);
                }
                
                // Actualizar información del pedido
                Map<String, Object> info = infoPorPedido.get(pedidoId);
                double volumenAsignado = (double) info.get("volumenAsignado") + nodo.getVolumenGLP();
                double porcentajeAsignado = (double) info.get("porcentajeAsignado") + nodo.getPorcentajePedido();
                
                info.put("volumenAsignado", volumenAsignado);
                info.put("porcentajeAsignado", porcentajeAsignado);
                
                // Agregar información de esta entrega específica
                Map<String, Object> infoEntrega = new HashMap<>();
                infoEntrega.put("nodoId", nodo.getId());
                infoEntrega.put("posX", nodo.getPosX());
                infoEntrega.put("posY", nodo.getPosY());
                infoEntrega.put("volumenGLP", nodo.getVolumenGLP());
                infoEntrega.put("porcentaje", nodo.getPorcentajePedido());
                infoEntrega.put("entregado", nodo.isEntregado());
                
                ((List<Map<String, Object>>) info.get("entregas")).add(infoEntrega);
            }
        }
        
        return new ArrayList<>(infoPorPedido.values());
    }
    
    /**
     * Obtiene la ruta como array bidimensional para algoritmos de navegación
     */
    @Transient
    public double[][] obtenerRutaComoArray() {
        double[][] rutaArray = new double[nodos.size()][2];
        for (int i = 0; i < nodos.size(); i++) {
            NodoRuta nodo = nodos.get(i);
            rutaArray[i][0] = nodo.getPosX();
            rutaArray[i][1] = nodo.getPosY();
        }
        return rutaArray;
    }
    
    /**
     * Calcula la distancia total de la ruta basada en distancia Manhattan
     * (adecuada para un mapa reticular)
     */
    public void calcularDistanciaTotal() {
        if (nodos.size() < 2) {
            this.distanciaTotal = 0;
            return;
        }
        
        double distancia = 0;
        for (int i = 0; i < nodos.size() - 1; i++) {
            NodoRuta nodoActual = nodos.get(i);
            NodoRuta nodoSiguiente = nodos.get(i + 1);
            
            // Distancia Manhattan: |x2-x1| + |y2-y1|
            distancia += Math.abs(nodoSiguiente.getPosX() - nodoActual.getPosX()) +
                         Math.abs(nodoSiguiente.getPosY() - nodoActual.getPosY());
        }
        
        this.distanciaTotal = distancia;
    }
    
    /**
     * Estima los tiempos de llegada para cada nodo
     * @param velocidadKmPorHora Velocidad promedio del vehículo en km/h
     * @param tiempoInicio Tiempo de inicio del recorrido
     */
    public void estimarTiemposLlegada(double velocidadKmPorHora, LocalDateTime tiempoInicio) {
        if (nodos.isEmpty()) return;
        
        double velocidadKmPorMinuto = velocidadKmPorHora / 60.0;
        LocalDateTime tiempoActual = tiempoInicio;
        
        // Para el primer nodo (origen), el tiempo estimado es el tiempo de inicio
        nodos.get(0).setTiempoLlegadaEstimado(tiempoActual);
        
        // Para los nodos siguientes, calcular en base a la distancia
        for (int i = 0; i < nodos.size() - 1; i++) {
            NodoRuta nodoActual = nodos.get(i);
            NodoRuta nodoSiguiente = nodos.get(i + 1);
            
            // Calcular distancia Manhattan entre los nodos
            double distanciaTramo = Math.abs(nodoSiguiente.getPosX() - nodoActual.getPosX()) +
                                    Math.abs(nodoSiguiente.getPosY() - nodoActual.getPosY());
            
            // Calcular tiempo en minutos para recorrer el tramo
            double tiempoTramoMinutos = distanciaTramo / velocidadKmPorMinuto;
            
            // Actualizar tiempo actual
            tiempoActual = tiempoActual.plusMinutes((long)tiempoTramoMinutos);
            
            // Establecer tiempo estimado de llegada al nodo siguiente
            nodoSiguiente.setTiempoLlegadaEstimado(tiempoActual);
        }
        
        // Actualizar tiempo estimado total en minutos
        if (nodos.size() > 1) {
            LocalDateTime tiempoFinal = nodos.get(nodos.size() - 1).getTiempoLlegadaEstimado();
            long minutosTotal = java.time.Duration.between(tiempoInicio, tiempoFinal).toMinutes();
            this.tiempoEstimadoMinutos = (int) minutosTotal;
        }
    }
    
    /**
     * Registra el tiempo real de llegada a un nodo
     */
    public void registrarLlegadaReal(int indiceNodo, LocalDateTime tiempoLlegada) {
        if (indiceNodo >= 0 && indiceNodo < nodos.size()) {
            nodos.get(indiceNodo).setTiempoLlegadaReal(tiempoLlegada);
        }
    }
    
    /**
     * Verifica si la ruta pasa por un punto específico
     */
    public boolean pasaPorPunto(int x, int y) {
        for (NodoRuta nodo : nodos) {
            if (nodo.getPosX() == x && nodo.getPosY() == y) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Obtiene los pedidos asociados a esta ruta
     */
    @Transient
    public List<Pedido> getPedidosAsociados() {
        return nodos.stream()
            .filter(n -> n.getPedido() != null)
            .map(NodoRuta::getPedido)
            .distinct()
            .collect(Collectors.toList());
    }
    
    /**
     * Verifica si un tramo de la ruta cruza un bloqueo
     */
    public boolean verificarInterseccionConBloqueos(List<Bloqueo> bloqueos) {
        if (nodos.size() < 2 || bloqueos == null || bloqueos.isEmpty()) {
            return false;
        }
        
        // Lista para almacenar los IDs de los bloqueos que afectan la ruta
        List<Long> idsBloqueos = new ArrayList<>();
        
        for (int i = 0; i < nodos.size() - 1; i++) {
            NodoRuta nodoActual = nodos.get(i);
            NodoRuta nodoSiguiente = nodos.get(i + 1);
            
            for (Bloqueo bloqueo : bloqueos) {
                if (bloqueo.isActivo() && intersectaBloqueo(
                        nodoActual.getPosX(), nodoActual.getPosY(),
                        nodoSiguiente.getPosX(), nodoSiguiente.getPosY(), bloqueo)) {
                    idsBloqueos.add(bloqueo.getId());
                }
            }
        }
        
        // Si encontramos bloqueos, almacenar sus IDs
        if (!idsBloqueos.isEmpty()) {
            this.bloqueosIds = idsBloqueos.stream()
                    .distinct()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
            return true;
        }
        
        return false;
    }
     
    /**
     * Verifica si un segmento de ruta intersecta con un bloqueo
     */
    private boolean intersectaBloqueo(double x1, double y1, double x2, double y2, Bloqueo bloqueo) {
        try {
            // Primero intentamos usar el método específico del bloqueo
            return bloqueo.intersectaConSegmento(x1, y1, x2, y2);
        } catch (Exception e) {
            // Implementación alternativa si hay error
            // Calculamos los puntos intermedios del segmento
            List<double[]> puntosIntermedios = generarPuntosIntermedios(x1, y1, x2, y2);
            
            // Verificamos si algún punto intermedio está en el bloqueo
            for (double[] punto : puntosIntermedios) {
                if (bloqueo.contienePunto(punto[0], punto[1])) {
                    return true;
                }
            }
            
            return false;
        }
    }
    
    /**
     * Genera una lista de puntos intermedios entre dos puntos (incluidos los extremos)
     */
    private List<double[]> generarPuntosIntermedios(double x1, double y1, double x2, double y2) {
        List<double[]> puntos = new ArrayList<>();
        
        // Añadir el punto inicial
        puntos.add(new double[]{x1, y1});
        
        // Si los puntos son iguales, no hay más que añadir
        if (x1 == x2 && y1 == y2) {
            return puntos;
        }
        
        // Calcular dirección del movimiento
        double dx = Double.compare(x2, x1);
        double dy = Double.compare(y2, y1);
        
        // Generar puntos intermedios
        double x = x1;
        double y = y1;
        
        while (x != x2 || y != y2) {
            // Si aún no hemos llegado al destino en X, movernos
            if (x != x2) {
                x += dx;
            }
            
            // Si aún no hemos llegado al destino en Y, movernos
            if (y != y2) {
                y += dy;
            }
            
            // Añadir el punto generado
            puntos.add(new double[]{x, y});
        }
        
        return puntos;
    }
    
    /**
     * Obtiene la lista de IDs de bloqueos que afectan a esta ruta
     */
    @Transient
    public List<Long> getBloqueoIdsComoLista() {
        if (bloqueosIds == null || bloqueosIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        return java.util.Arrays.stream(bloqueosIds.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }
    
    /**
     * Convierte la ruta a una representación para APIs REST
     */
    @Transient
    public List<Map<String, Object>> convertirARutaAPI() {
        List<Map<String, Object>> rutaAPI = new ArrayList<>();
        
        for (NodoRuta nodo : nodos) {
            Map<String, Object> punto = new HashMap<>();
            punto.put("x", nodo.getPosX());
            punto.put("y", nodo.getPosY());
            punto.put("tipo", nodo.getTipo());
            
            if (nodo.getPedido() != null) {
                punto.put("pedidoId", nodo.getPedido().getId());
                punto.put("volumenGLP", nodo.getVolumenGLP());
                punto.put("porcentajePedido", nodo.getPorcentajePedido());
                punto.put("entregado", nodo.isEntregado());
            }
            
            if (nodo.getTiempoLlegadaEstimado() != null) {
                punto.put("tiempoEstimado", nodo.getTiempoLlegadaEstimado().toString());
            }
            
            if (nodo.getTiempoLlegadaReal() != null) {
                punto.put("tiempoReal", nodo.getTiempoLlegadaReal().toString());
            }
            
            if (nodo.getObservaciones() != null && !nodo.getObservaciones().isEmpty()) {
                punto.put("observaciones", nodo.getObservaciones());
            }
            
            rutaAPI.add(punto);
        }
        
        return rutaAPI;
    }
    
    /**
     * Iniciar la ruta, cambiando su estado y registrando la fecha de inicio
     */
    public void iniciarRuta() {
        this.estado = 1; // En curso
        this.fechaInicioRuta = LocalDateTime.now();
        
        // Si hay camión asignado, actualizar su estado
        if (this.camion != null) {
            this.camion.setEstado(EstadoCamion.EN_RUTA); // Usar el enum
            this.camion.actualizarEstadoEntregasARuta();
        }
    }
    
    /**
     * Completar la ruta, cambiando su estado y registrando la fecha de fin
     */
    public void completarRuta() {
        this.estado = 2; // Completada
        this.fechaFinRuta = LocalDateTime.now();
        
        // Si hay camión asignado, actualizar su estado
        if (this.camion != null) {
            this.camion.setEstado(EstadoCamion.DISPONIBLE); // Usar el enum
        }
    }
    
    /**
     * Cancelar la ruta
     */
    public void cancelarRuta(String motivo) {
        this.estado = 3; // Cancelada
        
        // Si hay camión asignado, actualizar su estado
        if (this.camion != null) {
            this.camion.setEstado(EstadoCamion.DISPONIBLE); // Usar el enum
        }
    }
    
    /**
     * Obtiene el estado de la ruta como texto
     */
    @Transient
    public String getEstadoTexto() {
        switch (this.estado) {
            case 0: return "Planificada";
            case 1: return "En curso";
            case 2: return "Completada";
            case 3: return "Cancelada";
            default: return "Desconocido";
        }
    }
    
    /**
     * Actualiza el camión asignado a la ruta
     */
    public void setCamion(Camion camion) {
        this.camion = camion;
        actualizarCapacidadUtilizada();
    }
    
    /**
     * Genera información resumida de la ruta para APIs
     */
    @Transient
    public Map<String, Object> getResumenRuta() {
        Map<String, Object> resumen = new HashMap<>();
        resumen.put("id", this.id);
        resumen.put("codigo", this.codigo);
        resumen.put("fechaCreacion", this.fechaCreacion);
        resumen.put("estado", this.estado);
        resumen.put("estadoTexto", this.getEstadoTexto());
        resumen.put("distanciaTotal", this.distanciaTotal);
        resumen.put("tiempoEstimadoMinutos", this.tiempoEstimadoMinutos);
        resumen.put("volumenTotalGLP", this.volumenTotalGLP);
        
        if (this.camion != null) {
            resumen.put("camionCodigo", this.camion.getCodigo());
            resumen.put("capacidadUtilizadaPorcentaje", this.capacidadUtilizadaPorcentaje);
        }
        
        resumen.put("cantidadNodos", this.nodos.size());
        resumen.put("cantidadEntregas", this.getEntregasPendientes().size());
        
        if (this.fechaInicioRuta != null) {
            resumen.put("fechaInicioRuta", this.fechaInicioRuta.toString());
        }
        
        if (this.fechaFinRuta != null) {
            resumen.put("fechaFinRuta", this.fechaFinRuta.toString());
        }
        
        if (this.bloqueosIds != null && !this.bloqueosIds.isEmpty()) {
            resumen.put("tieneBloqueos", true);
            resumen.put("bloqueosIds", this.getBloqueoIdsComoLista());
        } else {
            resumen.put("tieneBloqueos", false);
        }
        
        return resumen;
    }
    
    /**
     * Verifica si la ruta tiene bloqueos activos para la hora actual
     */
    public boolean tieneBloqueoActivo(List<Bloqueo> bloqueos) {
        LocalDateTime ahora = LocalDateTime.now();
        
        // Obtener la lista de IDs de bloqueos que afectan a esta ruta
        List<Long> idsBloqueos = getBloqueoIdsComoLista();
        
        if (idsBloqueos.isEmpty() || bloqueos == null) {
            return false;
        }
        
        // Filtrar los bloqueos por ID y verificar si están activos en este momento
        return bloqueos.stream()
            .filter(b -> idsBloqueos.contains(b.getId()))
            .anyMatch(b -> b.isActivo() && 
                    ahora.isAfter(b.getFechaInicio()) && 
                    ahora.isBefore(b.getFechaFin()));
    }
}
```

