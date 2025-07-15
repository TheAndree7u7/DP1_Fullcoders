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
 * @function cargarDatosIniciales
 * @description Carga los datos iniciales de la simulación con reintentos
 */
export const cargarDatosIniciales = async (): Promise<void> => {
  let intentos = 0;
  const maxIntentos = 10;

  while (intentos < maxIntentos) {
    try {
      console.log(`🔄 CONTEXTO: Intento ${intentos + 1}/${maxIntentos} de carga inicial...`);
      
      // No intentar cargar datos de simulación automáticamente para evitar consumir paquetes
      // Los datos se cargarán a través del polling cuando estén disponibles
      console.log("ℹ️ CONTEXTO: Datos de simulación se cargarán vía polling cuando estén disponibles");
      
      // Si llegamos aquí, al menos los almacenes se cargaron correctamente
      break;
    } catch (error) {
      intentos++;
      console.log(`⚠️ CONTEXTO: Intento ${intentos} fallido:`, error);

      if (intentos < maxIntentos) {
        // Esperar antes del siguiente intento
        await new Promise(resolve => setTimeout(resolve, 2000));
      } else {
        console.error("❌ CONTEXTO: No se pudieron cargar los datos iniciales después de", maxIntentos, "intentos");
      }
    }
  }
};

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

    // Procesar estado de camiones
    const nuevosCamiones: CamionEstado[] = nuevasRutas.map((ruta) => {
      const gen = data.cromosoma.find((g: Gen) => g.camion.codigo === ruta.id);
      const camion = gen?.camion;
      
      return {
        id: ruta.id,
        ubicacion: ruta.ruta[0],
        porcentaje: 0,
        estado: camion?.estado === "DISPONIBLE" ? "Disponible" : "En Camino",
        capacidadActualGLP: camion?.capacidadActualGLP ?? 0,
        capacidadMaximaGLP: camion?.capacidadMaximaGLP ?? 0,
        combustibleActual: camion?.combustibleActual ?? 0,
        combustibleMaximo: camion?.combustibleMaximo ?? 0,
        distanciaMaxima: camion?.distanciaMaxima ?? 0,
        pesoCarga: camion?.pesoCarga ?? 0,
        pesoCombinado: camion?.pesoCombinado ?? 0,
        tara: camion?.tara ?? 0,
        tipo: camion?.tipo ?? "",
        velocidadPromedio: camion?.velocidadPromedio ?? 0,
      };
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