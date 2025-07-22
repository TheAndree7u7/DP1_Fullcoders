package com.plg.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.plg.utils.Parametros;
import com.plg.entity.Almacen;
import com.plg.entity.Averia;
import com.plg.entity.Bloqueo;
import com.plg.entity.EstadoCamion;
import com.plg.entity.Pedido;
import com.plg.utils.Gen;
import com.plg.utils.Herramientas;
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
    // verificando cada cromosoma
    public void agregarAveriasAutomaticas(List<Averia> averiasAutomaticas) {
        //
        // sacar la fecha en medio del intervalo de simulacion
        LocalDateTime fechaMedio = fechaHoraInicioIntervalo
                .plusSeconds(fechaHoraFinIntervalo.getSecond() - fechaHoraInicioIntervalo.getSecond() / 2);
        int turno = Herramientas.detectarTurno(fechaMedio);
        // Sacar la lista de averias automaticas del turno
        List<Averia> averiasAutomaticasTurno = averiasAutomaticas.stream()
                .filter(averia -> averia.getTurnoOcurrencia() == turno)
                .toList();

        // Sacar los camiones que se encuentran en el gen del mejor individuo tal que
        // esten dentro de las averias automaticas tenr una lista
        List<CamionDto> camiones_para_averiar_automaticamente = new ArrayList<>();
        for (GenDto gen : cromosoma) {
            boolean camion_en_averias_automaticas = averiasAutomaticasTurno.stream()
                    .anyMatch(averia -> averia.getCamion().getCodigo().equals(gen.getCamion().getCodigo()));
            boolean camion_estado_disponible = gen.getCamion().getEstado().equals(EstadoCamion.DISPONIBLE);

            if (camion_en_averias_automaticas && camion_estado_disponible) {
                camiones_para_averiar_automaticamente.add(gen.getCamion());
            }
        }

        System.out.println("Camiones para averiar automaticamente: " + camiones_para_averiar_automaticamente.size());
        // !Ahora por cada camion elegir un nodo aleatorio dentro de los nodos que puede
        // recorrer el camion pero que este en el rango de averias
    }
}
