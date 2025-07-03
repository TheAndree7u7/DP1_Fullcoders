package com.plg.utils.simulacion;

import com.plg.dto.IndividuoDto;
import com.plg.entity.Bloqueo;
import com.plg.entity.Pedido;
import com.plg.utils.AlgoritmoGenetico;
import com.plg.entity.Mapa;

import java.time.LocalDateTime;
import java.util.List;

public class SimulacionAlgoritmoUtils {

    public static AlgoritmoGenetico crearYEjecutarAlgoritmoGenetico(List<Pedido> pedidosEnviar) {
        AlgoritmoGenetico algoritmoGenetico = new AlgoritmoGenetico(Mapa.getInstance(), pedidosEnviar);
        algoritmoGenetico.ejecutarAlgoritmo();
        return algoritmoGenetico;
    }

    public static IndividuoDto crearIndividuoDtoConResultados(AlgoritmoGenetico algoritmoGenetico, List<Pedido> pedidosEnviar, List<Bloqueo> bloqueosActivos, LocalDateTime fechaActual) {
        return new IndividuoDto(algoritmoGenetico.getMejorIndividuo(), pedidosEnviar, bloqueosActivos, fechaActual);
    }

} 