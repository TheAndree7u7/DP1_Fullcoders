/**
 * @file servicios-carga-datos-simulacion.ts
 * @description Servicios para cargar datos desde el backend para la simulación
 * Incluye funciones para cargar almacenes, datos de simulación y soluciones anticipadas
 */

import { getMejorIndividuo } from "../../services/simulacionApiService";
import { getAlmacenes } from "../../services/almacenApiService";
import type { Almacen, Individuo, Gen, Nodo } from "../../types";
import type { CamionEstado, RutaCamion, Bloqueo } from "../SimulacionContext";
import { 
  extraerDiaDeFecha
} from "./utilidades-calculo-simulacion";

/**
 * @interface IndividuoConDatosComplementarios
 * @description Extensión del tipo Individuo con datos adicionales de la simulación
 */
export interface IndividuoConDatosComplementarios extends Individuo {
  bloqueos?: Bloqueo[];
  almacenes?: Almacen[];
  fechaHoraSimulacion?: string;
}

/**
 * @interface ResultadoCargaDatos
 * @description Resultado de la carga de datos de simulación
 */
export interface ResultadoCargaDatos {
  rutasActualizadas: RutaCamion[];
  camionesActualizados: CamionEstado[];
  bloqueosActualizados: Bloqueo[];
  almacenesActualizados: Almacen[];
  fechaHoraSimulacion: string | null;
  diaSimulacion: number | null;
}

/**
 * @function cargarDatosAlmacenesDesdeBackend
 * @description Carga la información de almacenes desde el backend
 * @returns {Promise<Almacen[]>} Lista de almacenes
 */
export const cargarDatosAlmacenesDesdeBackend = async (): Promise<Almacen[]> => {
  try {
    console.log('🔄 ALMACENES: Solicitando datos de almacenes al backend...');
    const datosAlmacenes = await getAlmacenes();
    console.log('✅ ALMACENES: Datos recibidos exitosamente:', datosAlmacenes);
    console.log('💾 ALMACENES: Cargados', datosAlmacenes.length, 'almacenes');
    return datosAlmacenes;
  } catch (error) {
    console.error("❌ ALMACENES: Error al cargar almacenes desde backend:", error);
    throw error;
  }
};

/**
 * @function cargarMejorSolucionDesdeBackend
 * @description Carga la mejor solución de simulación desde el backend
 * @returns {Promise<IndividuoConDatosComplementarios>} Solución completa
 */
export const cargarMejorSolucionDesdeBackend = async (): Promise<IndividuoConDatosComplementarios> => {
  try {
    console.log('🔄 SIMULACIÓN: Solicitando mejor solución al backend...');
    const mejorSolucion = await getMejorIndividuo() as IndividuoConDatosComplementarios;
    console.log('✅ SIMULACIÓN: Mejor solución recibida:', mejorSolucion);
    return mejorSolucion;
  } catch (error) {
    console.error("❌ SIMULACIÓN: Error al cargar mejor solución:", error);
    throw error;
  }
};

/**
 * @function procesarDatosSolucionParaSimulacion
 * @description Procesa los datos de una solución para convertirlos en formato de simulación
 * @param {IndividuoConDatosComplementarios} solucion - Solución a procesar
 * @param {CamionEstado[]} camionesEstadoAnterior - Estado anterior de los camiones
 * @returns {ResultadoCargaDatos} Datos procesados para la simulación
 */
export const procesarDatosSolucionParaSimulacion = (
  solucion: IndividuoConDatosComplementarios,
  camionesEstadoAnterior: CamionEstado[]
): ResultadoCargaDatos => {
  console.log('⚙️ PROCESAMIENTO: Iniciando procesamiento de datos de solución...');
  
  // Procesar fecha y hora de simulación
  let fechaHoraSimulacion: string | null = null;
  let diaSimulacion: number | null = null;
  
  if (solucion.fechaHoraSimulacion) {
    fechaHoraSimulacion = solucion.fechaHoraSimulacion;
    diaSimulacion = extraerDiaDeFecha(solucion.fechaHoraSimulacion);
    console.log('📅 PROCESAMIENTO: Fecha actualizada:', fechaHoraSimulacion, 'Día:', diaSimulacion);
  }

  // Procesar rutas de camiones
  const rutasActualizadas: RutaCamion[] = solucion.cromosoma.map((gen: Gen) => ({
    id: gen.camion.codigo,
    ruta: gen.nodos.map((nodo: Nodo) => `(${nodo.coordenada.x},${nodo.coordenada.y})`),
    puntoDestino: `(${gen.destino.x},${gen.destino.y})`,
    pedidos: gen.pedidos,
  }));

  // Log detallado de pedidos para debugging
  console.log('🔍 PROCESAMIENTO: Verificando pedidos en rutas:');
  rutasActualizadas.forEach((ruta) => {
    if (ruta.pedidos && ruta.pedidos.length > 0) {
      console.log(`Camión ${ruta.id} - ${ruta.pedidos.length} pedidos:`, ruta.pedidos);
      ruta.pedidos.forEach((pedido, index) => {
        console.log(`  Pedido ${index + 1}:`, {
          codigo: pedido.codigo,
          coordenada: pedido.coordenada,
          volumenGLPAsignado: pedido.volumenGLPAsignado,
          estado: pedido.estado,
        });
      });
    } else {
      console.log(`Camión ${ruta.id} - Sin pedidos asignados`);
    }
  });

  // Procesar estado de camiones
  const camionesActualizados: CamionEstado[] = rutasActualizadas.map((ruta) => {
    const camionEstadoAnterior = camionesEstadoAnterior.find((c) => c.id === ruta.id);
    const genCorrespondiente = solucion.cromosoma.find((g: Gen) => g.camion.codigo === ruta.id);
    const datosCamion = genCorrespondiente?.camion;
    
    const ubicacionInicial = camionEstadoAnterior?.ubicacion ?? ruta.ruta[0];
    
    return {
      id: ruta.id,
      ubicacion: ubicacionInicial,
      porcentaje: 0,
      estado: datosCamion?.estado === "DISPONIBLE" ? "Disponible" : "En Camino",
      capacidadActualGLP: datosCamion?.capacidadActualGLP ?? 0,
      capacidadMaximaGLP: datosCamion?.capacidadMaximaGLP ?? 0,
      combustibleActual: datosCamion?.combustibleActual ?? 0,
      combustibleMaximo: datosCamion?.combustibleMaximo ?? 0,
      distanciaMaxima: datosCamion?.distanciaMaxima ?? 0,
      pesoCarga: datosCamion?.pesoCarga ?? 0,
      pesoCombinado: datosCamion?.pesoCombinado ?? 0,
      tara: datosCamion?.tara ?? 0,
      tipo: datosCamion?.tipo ?? "",
      velocidadPromedio: datosCamion?.velocidadPromedio ?? 0,
    };
  });

  // Procesar bloqueos
  const bloqueosActualizados: Bloqueo[] = solucion.bloqueos ?? [];

  // Procesar almacenes
  const almacenesActualizados: Almacen[] = solucion.almacenes ?? [];
  if (almacenesActualizados.length > 0) {
    console.log('🏪 PROCESAMIENTO: Almacenes incluidos en solución:', almacenesActualizados);
  }

  console.log('📋 PROCESAMIENTO: Datos procesados exitosamente');
  console.log(`✅ PROCESAMIENTO: ${rutasActualizadas.length} rutas, ${camionesActualizados.length} camiones, ${bloqueosActualizados.length} bloqueos`);

  return {
    rutasActualizadas,
    camionesActualizados,
    bloqueosActualizados,
    almacenesActualizados,
    fechaHoraSimulacion,
    diaSimulacion,
  };
};

/**
 * @function cargarYProcesarDatosCompletos
 * @description Carga y procesa una solución completa de simulación
 * @param {CamionEstado[]} camionesEstadoAnterior - Estado anterior de los camiones
 * @param {boolean} esInicial - Indica si es la carga inicial
 * @returns {Promise<ResultadoCargaDatos>} Datos procesados para la simulación
 */
export const cargarYProcesarDatosCompletos = async (
  camionesEstadoAnterior: CamionEstado[],
  esInicial: boolean = false
): Promise<ResultadoCargaDatos> => {
  const tipoOperacion = esInicial ? "INICIAL" : "ACTUALIZACIÓN";
  console.log(`🚀 ${tipoOperacion}: Iniciando carga y procesamiento de datos...`);
  
  try {
    const solucionCompleta = await cargarMejorSolucionDesdeBackend();
    const datosProcessados = procesarDatosSolucionParaSimulacion(solucionCompleta, camionesEstadoAnterior);
    
    console.log(`✅ ${tipoOperacion}: Datos cargados y procesados exitosamente`);
    return datosProcessados;
  } catch (error) {
    console.error(`❌ ${tipoOperacion}: Error en carga y procesamiento:`, error);
    throw error;
  }
};

/**
 * @function cargarSolucionAnticipadaEnBackground
 * @description Carga una solución de forma anticipada para transición suave
 * @returns {Promise<IndividuoConDatosComplementarios>} Solución anticipada
 */
export const cargarSolucionAnticipadaEnBackground = async (): Promise<IndividuoConDatosComplementarios> => {
  try {
    console.log("🚀 ANTICIPADA: Iniciando carga de solución anticipada en background...");
    const solucionAnticipada = await cargarMejorSolucionDesdeBackend();
    console.log("✨ ANTICIPADA: Solución anticipada cargada y lista para uso:", solucionAnticipada);
    return solucionAnticipada;
  } catch (error) {
    console.error("⚠️ ANTICIPADA: Error al cargar solución anticipada:", error);
    throw error;
  }
}; 