package com.plg.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.plg.utils.Parametros;
import com.plg.entity.Almacen;
import com.plg.entity.Bloqueo;
import com.plg.entity.Pedido;
import com.plg.utils.Gen;
import com.plg.utils.Individuo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class IndividuoDto {

    private List<GenDto> cromosoma;
    private List<PedidoDto> pedidos;
    private List<BloqueoDto> bloqueos;
    private List<AlmacenDto> almacenes;
    private LocalDateTime fechaHoraSimulacion;
    private LocalDateTime fechaHoraInicioIntervalo;
    private LocalDateTime fechaHoraFinIntervalo;

    public IndividuoDto(Individuo individuo, List<Pedido> pedidos, List<Bloqueo> bloqueos) {
        this(individuo, pedidos, bloqueos, LocalDateTime.now());
    }

    public IndividuoDto(Individuo individuo, List<Pedido> pedidos, List<Bloqueo> bloqueos,
            LocalDateTime fechaSimulacion) {
        this.fechaHoraSimulacion = fechaSimulacion;

        this.cromosoma = new ArrayList<>();
        for (Gen gen : individuo.getCromosoma()) {
            cromosoma.add(new GenDto(gen));
        }

        this.pedidos = new ArrayList<>();
        for (Pedido pedido : pedidos) {
            this.pedidos.add(new PedidoDto(pedido));
        }

        this.bloqueos = new ArrayList<>();
        for (Bloqueo bloqueo : bloqueos) {
            this.bloqueos.add(new BloqueoDto(bloqueo));
        }

        // Agregar almacenes desde DataLoader
        this.almacenes = new ArrayList<>();
        for (Almacen almacen : Parametros.dataLoader.almacenes) {
            this.almacenes.add(new AlmacenDto(almacen));
        }
    }
    // !AVERIAS AUTOMATICAS
    // Metodo para agregar averias automaticas en la ruta del mejor individuo
}
