package com.plg.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.plg.entity.Camion;
import com.plg.entity.EstadoCamion;
import com.plg.entity.Mantenimiento;

/**
 * DTO para transferir información segura de Camiones evitando problemas de LazyInitializationException
 */
public class CamionDTO {
    
    private Long id;
    private String codigo;
    private String tipo;
    private double capacidad;
    private double capacidadDisponible;
    private double tara;
    private double pesoCarga;
    private double pesoCombinado;
    private EstadoCamion estado;
    private String estadoTexto;
    private double capacidadTanque;
    private double combustibleActual;
    private double velocidadPromedio;
    private double posX;
    private double posY;
    private double porcentajeUso;
    private List<MantenimientoResumenDTO> mantenimientos;
    
    /**
     * Constructor vacío
     */
    public CamionDTO() {
        this.mantenimientos = new ArrayList<>();
    }
    
    /**
     * Construye un DTO a partir de una entidad Camion
     * @param camion Entidad Camion
     * @param incluirMantenimientos Si se deben incluir los mantenimientos
     */
    public CamionDTO(Camion camion, boolean incluirMantenimientos) {
        this.id = camion.getId();
        this.codigo = camion.getCodigo();
        this.tipo = camion.getTipo();
        this.capacidad = camion.getCapacidad();
        this.capacidadDisponible = camion.getCapacidadDisponible();
        this.tara = camion.getTara();
        this.pesoCarga = camion.getPesoCarga();
        this.pesoCombinado = camion.getPesoCombinado();
        this.estado = camion.getEstado();
        this.estadoTexto = camion.getEstadoTexto();
        this.capacidadTanque = camion.getCapacidadTanque();
        this.combustibleActual = camion.getCombustibleActual();
        this.velocidadPromedio = camion.getVelocidadPromedio();
        this.posX = camion.getPosX();
        this.posY = camion.getPosY();
        this.porcentajeUso = camion.getPorcentajeUso();
        
        if (incluirMantenimientos && camion.getMantenimientos() != null) {
            this.mantenimientos = new ArrayList<>();
            for (Mantenimiento m : camion.getMantenimientos()) {
                this.mantenimientos.add(new MantenimientoResumenDTO(m));
            }
        } else {
            this.mantenimientos = new ArrayList<>();
        }
    }
    
    /**
     * Convierte una lista de entidades Camion a lista de DTOs
     */
    public static List<CamionDTO> fromEntities(List<Camion> camiones, boolean incluirMantenimientos) {
        List<CamionDTO> dtos = new ArrayList<>();
        for (Camion camion : camiones) {
            dtos.add(new CamionDTO(camion, incluirMantenimientos));
        }
        return dtos;
    }
    
    /**
     * Convierte todos los datos a un Map para APIs
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", this.id);
        map.put("codigo", this.codigo);
        map.put("tipo", this.tipo);
        map.put("capacidad", this.capacidad);
        map.put("capacidadDisponible", this.capacidadDisponible);
        map.put("tara", this.tara);
        map.put("pesoCarga", this.pesoCarga);
        map.put("pesoCombinado", this.pesoCombinado);
        map.put("estado", this.estado);
        map.put("estadoTexto", this.estadoTexto);
        map.put("capacidadTanque", this.capacidadTanque);
        map.put("combustibleActual", this.combustibleActual);
        map.put("velocidadPromedio", this.velocidadPromedio);
        map.put("posX", this.posX);
        map.put("posY", this.posY);
        map.put("porcentajeUso", this.porcentajeUso);
        map.put("mantenimientos", this.mantenimientos);
        
        return map;
    }
    
    // Getters y setters
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public double getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(double capacidad) {
        this.capacidad = capacidad;
    }

    public double getCapacidadDisponible() {
        return capacidadDisponible;
    }

    public void setCapacidadDisponible(double capacidadDisponible) {
        this.capacidadDisponible = capacidadDisponible;
    }

    public double getTara() {
        return tara;
    }

    public void setTara(double tara) {
        this.tara = tara;
    }

    public double getPesoCarga() {
        return pesoCarga;
    }

    public void setPesoCarga(double pesoCarga) {
        this.pesoCarga = pesoCarga;
    }

    public double getPesoCombinado() {
        return pesoCombinado;
    }

    public void setPesoCombinado(double pesoCombinado) {
        this.pesoCombinado = pesoCombinado;
    }

    public EstadoCamion getEstado() {
        return estado;
    }

    public void setEstado(EstadoCamion estado) {
        this.estado = estado;
    }

    public String getEstadoTexto() {
        return estadoTexto;
    }

    public void setEstadoTexto(String estadoTexto) {
        this.estadoTexto = estadoTexto;
    }

    public double getCapacidadTanque() {
        return capacidadTanque;
    }

    public void setCapacidadTanque(double capacidadTanque) {
        this.capacidadTanque = capacidadTanque;
    }

    public double getCombustibleActual() {
        return combustibleActual;
    }

    public void setCombustibleActual(double combustibleActual) {
        this.combustibleActual = combustibleActual;
    }

    public double getVelocidadPromedio() {
        return velocidadPromedio;
    }

    public void setVelocidadPromedio(double velocidadPromedio) {
        this.velocidadPromedio = velocidadPromedio;
    }

    public double getPosX() {
        return posX;
    }

    public void setPosX(double posX) {
        this.posX = posX;
    }

    public double getPosY() {
        return posY;
    }

    public void setPosY(double posY) {
        this.posY = posY;
    }

    public double getPorcentajeUso() {
        return porcentajeUso;
    }

    public void setPorcentajeUso(double porcentajeUso) {
        this.porcentajeUso = porcentajeUso;
    }

    public List<MantenimientoResumenDTO> getMantenimientos() {
        return mantenimientos;
    }

    public void setMantenimientos(List<MantenimientoResumenDTO> mantenimientos) {
        this.mantenimientos = mantenimientos;
    }
}