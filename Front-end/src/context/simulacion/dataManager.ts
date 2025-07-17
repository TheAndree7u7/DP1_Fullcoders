/**
 * @file dataManager.ts
 * @description Manejo de carga y gestión de datos de simulación
 */

import { getMejorIndividuo, reiniciarSimulacion } from "../../services/simulacionApiService";
import type { 
  CamionEstado, 
  RutaCamion, 
  Bloqueo, 
  IndividuoConBloqueos
} from "./types";
import type { Almacen } from "../../types";
import type { Gen, Nodo } from "../../types";




/**
 * @function cargarDatos
 * @description Carga los datos de simulación desde el backend
 */
export const cargarDatos = async (
  fechaInicioSimulacion: string | null
): Promise<{
  nuevasRutas: RutaCamion[];
  nuevosCamiones: CamionEstado[];
  bloqueos: Bloqueo[];
  almacenes: Almacen[];
  fechaHoraSimulacion: string | null;
  fechaHoraInicioIntervalo: string | null;
  fechaHoraFinIntervalo: string | null;
  diaSimulacion: number | null;
}> => {
  try {
    console.log("🔄 SOLICITUD: Iniciando solicitud de nueva solución al servidor...");
    
    const data = (await getMejorIndividuo(fechaInicioSimulacion ?? "")) as IndividuoConBloqueos;
    console.log("✅ RESPUESTA: Datos de nueva solución recibidos del servidor:", data);
    
    // Extraer fechas del paquete actual
    const fechaHoraInicioIntervalo = data.fechaHoraInicioIntervalo || null;
    const fechaHoraFinIntervalo = data.fechaHoraFinIntervalo || null;
    const fechaHoraSimulacion = data.fechaHoraSimulacion || null;
    
    // Calcular día de simulación
    let diaSimulacion: number | null = null;
    if (fechaHoraSimulacion) {
      const fecha = new Date(fechaHoraSimulacion);
      diaSimulacion = fecha.getDate();
    }

    // Procesar rutas de camiones
    const nuevasRutas: RutaCamion[] = data.cromosoma.map((gen: Gen) => ({
      id: gen.camion.codigo,
      ruta: gen.nodos.map((n: Nodo) => `(${n.coordenada.x},${n.coordenada.y})`),
      puntoDestino: `(${gen.destino.x},${gen.destino.y})`,
      pedidos: gen.pedidos,
    }));

    // Log para verificar los pedidos que llegan del backend
    console.log("🔍 Verificando pedidos en las rutas:");
    nuevasRutas.forEach((ruta) => {
      if (ruta.pedidos && ruta.pedidos.length > 0) {
        console.log(`Camión ${ruta.id} tiene ${ruta.pedidos.length} pedidos:`, ruta.pedidos);
        ruta.pedidos.forEach((pedido, index) => {
          console.log(`  Pedido ${index + 1}:`, {
            codigo: pedido.codigo,
            coordenada: pedido.coordenada,
            volumenGLPAsignado: pedido.volumenGLPAsignado,
            estado: pedido.estado,
          });
        });
      } else {
        console.log(`Camión ${ruta.id} no tiene pedidos asignados`);
      }
    });

    // Procesar estado de camiones con validaciones
    console.log("🔍 VALIDACIÓN: Procesando estado de camiones desde backend...");
    const nuevosCamiones: CamionEstado[] = nuevasRutas.map((ruta, index) => {
      try {
        const gen = data.cromosoma.find((g: Gen) => g.camion.codigo === ruta.id);
        const camion = gen?.camion;
        
        // Validar que el camión existe
        if (!camion) {
          console.error(`❌ ERROR: No se encontró camión para ruta ${ruta.id} en el cromosoma`);
          throw new Error(`Camión no encontrado para ruta ${ruta.id}`);
        }
        
        // Validar propiedades críticas del camión
        if (!camion.codigo) {
          console.error(`❌ ERROR: Camión en índice ${index} no tiene código:`, camion);
        }
        
        if (typeof camion.capacidadActualGLP !== 'number' || isNaN(camion.capacidadActualGLP)) {
          console.error(`❌ ERROR: Camión ${camion.codigo} tiene capacidadActualGLP inválida:`, camion.capacidadActualGLP);
        }
        
        if (typeof camion.capacidadMaximaGLP !== 'number' || isNaN(camion.capacidadMaximaGLP)) {
          console.error(`❌ ERROR: Camión ${camion.codigo} tiene capacidadMaximaGLP inválida:`, camion.capacidadMaximaGLP);
        }
        
        if (typeof camion.combustibleActual !== 'number' || isNaN(camion.combustibleActual)) {
          console.error(`❌ ERROR: Camión ${camion.codigo} tiene combustibleActual inválido:`, camion.combustibleActual);
        }
        
        if (typeof camion.combustibleMaximo !== 'number' || isNaN(camion.combustibleMaximo)) {
          console.error(`❌ ERROR: Camión ${camion.codigo} tiene combustibleMaximo inválido:`, camion.combustibleMaximo);
        }
        
        // Validar ruta
        if (!ruta.ruta || ruta.ruta.length === 0) {
          console.error(`❌ ERROR: Ruta ${ruta.id} está vacía:`, ruta);
        }
        
        const camionEstado: CamionEstado = {
          id: ruta.id,
          ubicacion: ruta.ruta[0] || '(0,0)',
          porcentaje: 0,
          estado: camion?.estado === "DISPONIBLE" ? "Disponible" : "En Camino",
          capacidadActualGLP: typeof camion.capacidadActualGLP === 'number' && !isNaN(camion.capacidadActualGLP) ? camion.capacidadActualGLP : 0,
          capacidadMaximaGLP: typeof camion.capacidadMaximaGLP === 'number' && !isNaN(camion.capacidadMaximaGLP) ? camion.capacidadMaximaGLP : 0,
          combustibleActual: typeof camion.combustibleActual === 'number' && !isNaN(camion.combustibleActual) ? camion.combustibleActual : 0,
          combustibleMaximo: typeof camion.combustibleMaximo === 'number' && !isNaN(camion.combustibleMaximo) ? camion.combustibleMaximo : 0,
          distanciaMaxima: typeof camion.distanciaMaxima === 'number' && !isNaN(camion.distanciaMaxima) ? camion.distanciaMaxima : 0,
          pesoCarga: typeof camion.pesoCarga === 'number' && !isNaN(camion.pesoCarga) ? camion.pesoCarga : 0,
          pesoCombinado: typeof camion.pesoCombinado === 'number' && !isNaN(camion.pesoCombinado) ? camion.pesoCombinado : 0,
          tara: typeof camion.tara === 'number' && !isNaN(camion.tara) ? camion.tara : 0,
          tipo: camion?.tipo ?? "",
          velocidadPromedio: typeof camion.velocidadPromedio === 'number' && !isNaN(camion.velocidadPromedio) ? camion.velocidadPromedio : 0,
        };
        
        console.log(`✅ Camión ${camionEstado.id} procesado correctamente:`, {
          estado: camionEstado.estado,
          capacidadGLP: `${camionEstado.capacidadActualGLP}/${camionEstado.capacidadMaximaGLP}`,
          combustible: `${camionEstado.combustibleActual}/${camionEstado.combustibleMaximo}`,
          ubicacion: camionEstado.ubicacion
        });
        
        return camionEstado;
        
      } catch (error) {
        console.error(`❌ ERROR al procesar camión en índice ${index}:`, error, { ruta, gen: data.cromosoma[index] });
        // Retornar un camión por defecto para evitar errores de renderizado
        return {
          id: ruta.id || `error-${index}`,
          ubicacion: '(0,0)',
          porcentaje: 0,
          estado: 'Averiado' as const,
          capacidadActualGLP: 0,
          capacidadMaximaGLP: 0,
          combustibleActual: 0,
          combustibleMaximo: 0,
          distanciaMaxima: 0,
          pesoCarga: 0,
          pesoCombinado: 0,
          tara: 0,
          tipo: 'ERROR',
          velocidadPromedio: 0,
        };
      }
    });

    // Extraer bloqueos
    const bloqueos: Bloqueo[] = data.bloqueos || [];

    // Gestionar almacenes
    const almacenes: Almacen[] = data.almacenes || [];

    return {
      nuevasRutas,
      nuevosCamiones,
      bloqueos,
      almacenes,
      fechaHoraSimulacion,
      fechaHoraInicioIntervalo,
      fechaHoraFinIntervalo,
      diaSimulacion,
    };
  } catch (error) {
    console.error("Error al cargar datos de simulación:", error);
    throw error;
  }
};

/**
 * @function cargarSolucionAnticipada
 * @description Carga anticipadamente la siguiente solución para transición suave
 */
export const cargarSolucionAnticipada = async (
  fechaHoraFinIntervalo: string | null
): Promise<IndividuoConBloqueos> => {
  try {
    console.log("🚀 ANTICIPADA: Cargando solución anticipada en background...");
    const data = await getMejorIndividuo(fechaHoraFinIntervalo || "") as IndividuoConBloqueos;
    console.log("✨ ANTICIPADA: Solución anticipada cargada y lista:", data);
    return data;
  } catch (error) {
    console.error("⚠️ ANTICIPADA: Error al cargar solución anticipada:", error);
    throw error;
  }
};

/**
 * @function reiniciarSimulacionBackend
 * @description Reinicia los paquetes en el backend
 */
export const reiniciarSimulacionBackend = async (): Promise<void> => {
  try {
    await reiniciarSimulacion();
    console.log("✅ REINICIO: Paquetes del backend reiniciados exitosamente");
  } catch (error) {
    console.error("❌ REINICIO: Error al reiniciar paquetes del backend:", error);
    throw error;
  }
}; 