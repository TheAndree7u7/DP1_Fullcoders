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
    public boolean agregarAveriasAutomaticas(List<Averia> averiasAutomaticas) {
        // Validación de parámetros de entrada
        if (averiasAutomaticas == null || averiasAutomaticas.isEmpty()) {
            System.out.println("❌ No hay averías automáticas para aplicar");
            return false;
        }

        if (fechaHoraInicioIntervalo == null || fechaHoraFinIntervalo == null) {
            System.out.println("❌ Fechas de intervalo no están inicializadas");
            return false;
        }

        // Validación de que el intervalo sea válido
        if (fechaHoraFinIntervalo.isBefore(fechaHoraInicioIntervalo)) {
            System.out.println("❌ Intervalo de fechas inválido: fecha fin antes que fecha inicio");
            return false;
        }

        try {
            // sacar la fecha en medio del intervalo de simulacion
            long segundosTotales = java.time.Duration.between(fechaHoraInicioIntervalo, fechaHoraFinIntervalo)
                    .getSeconds();
            LocalDateTime fechaMedio = fechaHoraInicioIntervalo.plusSeconds(segundosTotales / 2);

            int turno = Herramientas.detectarTurno(fechaMedio);

            // Validación del turno
            if (turno < 1 || turno > 3) {
                System.out.println("❌ Turno inválido detectado: " + turno);
                return false;
            }

            // Sacar la lista de averias automaticas del turno
            List<Averia> averiasAutomaticasTurno = averiasAutomaticas.stream()
                    .filter(averia -> averia.getTurnoOcurrencia() == turno)
                    .toList();

            if (averiasAutomaticasTurno.isEmpty()) {
                System.out.println("✅ No hay averías automáticas para el turno " + turno);
                return true; // No es un error, simplemente no hay averías para este turno
            }

            // Sacar los camiones que se encuentran en el gen del mejor individuo tal que
            // esten dentro de las averias automaticas tenr una lista
            List<CamionDto> camiones_para_averiar_automaticamente = new ArrayList<>();
            for (GenDto gen : cromosoma) {
                if (gen == null || gen.getCamion() == null) {
                    System.out.println("❌ Gen o camión nulo encontrado");
                    continue;
                }

                boolean camion_en_averias_automaticas = averiasAutomaticasTurno.stream()
                        .anyMatch(averia -> averia.getCamion() != null &&
                                averia.getCamion().getCodigo() != null &&
                                averia.getCamion().getCodigo().equals(gen.getCamion().getCodigo()));
                boolean camion_estado_disponible = gen.getCamion().getEstado() != null &&
                        gen.getCamion().getEstado().equals(EstadoCamion.DISPONIBLE.toString());

                if (camion_en_averias_automaticas && camion_estado_disponible) {
                    camiones_para_averiar_automaticamente.add(gen.getCamion());
                }
            }

            System.out
                    .println("Camiones para averiar automaticamente: " + camiones_para_averiar_automaticamente.size());

            // !Ahora por cada camion elegir un nodo aleatorio dentro de los nodos que puede
            // recorrer el camion pero que este en el rango de averiasa
            for (GenDto gen : cromosoma) {
                if (camiones_para_averiar_automaticamente.contains(gen.getCamion())) {
                    boolean resultado = gen.colocar_nodo_de_averia_automatica();
                    if (!resultado) {
                        System.out.println(
                                "❌ Error al colocar avería automática en camión: " + gen.getCamion().getCodigo());
                        return false;
                    }
                }
            }

            return true;

        } catch (Exception e) {
            System.out.println("❌ Error en agregarAveriasAutomaticas: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // !Corta los nodos que van despues del ultimo nodo que puede recorrer el camion
    public void cortarNodosQueVanDespuesDelUltimoNodoQuePuedeRecorrerElCamion() {
        for (GenDto gen : cromosoma) {
            gen.cortarNodosQueVanDespuesDelUltimoNodoQuePuedeRecorrerElCamion();
        }
    }
}
