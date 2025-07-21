/**
 * @file autoAverias.ts
 * @description Lógica para averías automáticas durante la simulación
 */

import { AVERIAS_AUTOMATICAS_CONFIG } from '../../config/constants';
import type { CamionEstado, RutaCamion, Bloqueo, IndividuoConBloqueos } from './types';
import type { Almacen } from '../../types';
import { handleAveriar } from '../../components/mapa/utils/averias';
import { toast } from 'react-toastify';

/**
 * @interface EstadoAveriasAutomaticas
 * @description Estado para controlar las averías automáticas
 */
export interface EstadoAveriasAutomaticas {
  contadorPaquetes: number;
  ultimoPaqueteConAveria: number;
  averiasRealizadas: string[]; // IDs de camiones que ya tuvieron avería automática
}

/**
 * @function inicializarEstadoAveriasAutomaticas
 * @description Inicializa el estado de averías automáticas
 */
export const inicializarEstadoAveriasAutomaticas = (): EstadoAveriasAutomaticas => ({
  contadorPaquetes: 0,
  ultimoPaqueteConAveria: 0,
  averiasRealizadas: []
});

/**
 * @function incrementarContadorPaquetes
 * @description Incrementa el contador de paquetes y verifica si debe ocurrir una avería
 */
export const incrementarContadorPaquetes = (
  estadoAverias: EstadoAveriasAutomaticas,
  paqueteActual: number
): EstadoAveriasAutomaticas => {
  const nuevoEstado = {
    ...estadoAverias,
    contadorPaquetes: estadoAverias.contadorPaquetes + 1
  };

  // Verificar si debe ocurrir una avería automática
  if (debeOcurrirAveriaAutomatica(nuevoEstado, paqueteActual)) {
    nuevoEstado.ultimoPaqueteConAveria = paqueteActual;
  }

  return nuevoEstado;
};

/**
 * @function debeOcurrirAveriaAutomatica
 * @description Determina si debe ocurrir una avería automática basada en el contador de paquetes
 */
export const debeOcurrirAveriaAutomatica = (
  estadoAverias: EstadoAveriasAutomaticas,
  paqueteActual: number
): boolean => {
  if (!AVERIAS_AUTOMATICAS_CONFIG.ACTIVADO) {
    return false;
  }

  // Verificar si ya ocurrió una avería en este paquete
  if (estadoAverias.ultimoPaqueteConAveria === paqueteActual) {
    return false;
  }

  // Verificar si es el momento de ocurrir una avería (cada X paquetes)
  return estadoAverias.contadorPaquetes % AVERIAS_AUTOMATICAS_CONFIG.PAQUETES_PARA_AVERIA === 0;
};

/**
 * @function obtenerCamionesCandidatosAveria
 * @description Obtiene los camiones candidatos para avería automática
 */
export const obtenerCamionesCandidatosAveria = (
  camiones: CamionEstado[],
  rutasCamiones: RutaCamion[],
  estadoAverias: EstadoAveriasAutomaticas
): CamionEstado[] => {
  if (!AVERIAS_AUTOMATICAS_CONFIG.ACTIVADO) {
    return [];
  }

  // Filtrar camiones que cumplan los criterios
  const candidatos = camiones.filter(camion => {
    // Verificar estado válido
    if (camion.estado !== 'En Ruta') {
      return false;
    }

    // Verificar que no haya tenido avería automática recientemente
    if (estadoAverias.averiasRealizadas.includes(camion.id)) {
      return false;
    }

    // Verificar que esté en ruta
    const ruta = rutasCamiones.find(r => r.id === camion.id);
    if (!ruta || ruta.ruta.length <= 1) {
      return false;
    }

    // Verificar que esté en un nodo válido (no PEDIDO)
    const posicionActual = Math.floor(camion.porcentaje);
    if (posicionActual >= ruta.ruta.length) {
      return false;
    }

    const nodoActual = ruta.ruta[posicionActual];
    const coordenada = parseCoord(nodoActual);
    
    // Verificar que no esté en un pedido
    const esPedido = ruta.pedidos.some(pedido => 
      pedido.coordenada.x === coordenada.x && pedido.coordenada.y === coordenada.y
    );

    if (esPedido) {
      return false;
    }

    return true;
  });

  // Ordenar por capacidad (menor primero si está habilitado)
  if (AVERIAS_AUTOMATICAS_CONFIG.PRIORIDAD_CAPACIDAD_MINIMA) {
    candidatos.sort((a, b) => a.capacidadActualGLP - b.capacidadActualGLP);
  }

  return candidatos;
};

/**
 * @function calcularMomentoAveria
 * @description Calcula el momento exacto dentro del intervalo donde debe ocurrir la avería
 */
export const calcularMomentoAveria = (
  fechaInicioIntervalo: string,
  fechaFinIntervalo: string
): string => {
  const inicio = new Date(fechaInicioIntervalo);
  const fin = new Date(fechaFinIntervalo);
  
  // Calcular duración total del intervalo
  const duracionTotal = fin.getTime() - inicio.getTime();
  
  // Calcular porcentaje aleatorio entre el mínimo y máximo configurado
  const porcentajeAleatorio = Math.random() * 
    (AVERIAS_AUTOMATICAS_CONFIG.PORCENTAJE_MAXIMO_TIEMPO - AVERIAS_AUTOMATICAS_CONFIG.PORCENTAJE_MINIMO_TIEMPO) + 
    AVERIAS_AUTOMATICAS_CONFIG.PORCENTAJE_MINIMO_TIEMPO;
  
  // Calcular el momento exacto
  const momentoAveria = new Date(inicio.getTime() + (duracionTotal * porcentajeAleatorio));
  
  return momentoAveria.toISOString();
};

/**
 * @function seleccionarTipoAveria
 * @description Selecciona aleatoriamente un tipo de avería
 */
export const seleccionarTipoAveria = (): number => {
  const tipos = AVERIAS_AUTOMATICAS_CONFIG.TIPOS_AVERIA_DISPONIBLES;
  const indiceAleatorio = Math.floor(Math.random() * tipos.length);
  return tipos[indiceAleatorio];
};

/**
 * @function ejecutarAveriaAutomatica
 * @description Ejecuta una avería automática en un camión seleccionado
 */
export const ejecutarAveriaAutomatica = async (
  camionSeleccionado: CamionEstado,
  tipoAveria: number,
  momentoAveria: string,
  estadoSimulacion: {
    horaActual: number;
    horaSimulacion: string;
    fechaHoraSimulacion: string | null;
    fechaInicioSimulacion: string | null;
    diaSimulacion: number | null;
    tiempoRealSimulacion: string;
    tiempoTranscurridoSimulado: string;
    camiones: CamionEstado[];
    rutasCamiones: RutaCamion[];
    almacenes: Almacen[];
    bloqueos: Bloqueo[];
  },
  funciones: {
    marcarCamionAveriado: (camionId: string) => void;
    setAveriando: (value: string | null) => void;
    setClickedCamion: (value: string | null) => void;
    setSimulacionActiva: (value: boolean) => void;
    setPollingActivo?: (value: boolean) => void;
    aplicarNuevaSolucionDespuesAveria?: (data: IndividuoConBloqueos) => Promise<void>;
  }
): Promise<boolean> => {
  try {
    console.log(`🚛💥 AVERÍA AUTOMÁTICA: Iniciando avería automática para camión ${camionSeleccionado.id} (Tipo ${tipoAveria})`);
    
    // Ejecutar la avería usando la función existente
    await handleAveriar(
      camionSeleccionado.id,
      tipoAveria,
      funciones.marcarCamionAveriado,
      funciones.setAveriando,
      funciones.setClickedCamion,
      funciones.setSimulacionActiva,
      estadoSimulacion,
      funciones.setPollingActivo,
      funciones.aplicarNuevaSolucionDespuesAveria
    );

    console.log(`✅ AVERÍA AUTOMÁTICA: Avería exitosa para camión ${camionSeleccionado.id}`);
    
    // Mostrar notificación
    toast.info(`🚛💥 Avería automática: Camión ${camionSeleccionado.id} averiado (Tipo ${tipoAveria})`, {
      position: "top-right",
      autoClose: 5000,
      hideProgressBar: false,
      closeOnClick: true,
      pauseOnHover: true,
      draggable: true,
      progress: undefined,
      theme: "light",
    });

    return true;
  } catch (error) {
    console.error(`❌ AVERÍA AUTOMÁTICA: Error al ejecutar avería para camión ${camionSeleccionado.id}:`, error);
    
    toast.error(`❌ Error en avería automática: ${error instanceof Error ? error.message : 'Error desconocido'}`, {
      position: "top-right",
      autoClose: 8000,
      hideProgressBar: false,
      closeOnClick: true,
      pauseOnHover: true,
      draggable: true,
      progress: undefined,
      theme: "light",
    });

    return false;
  }
};

/**
 * @function verificarYEjecutarAveriaAutomatica
 * @description Función principal que verifica y ejecuta averías automáticas
 */
export const verificarYEjecutarAveriaAutomatica = async (
  estadoAverias: EstadoAveriasAutomaticas,
  paqueteActual: number,
  camiones: CamionEstado[],
  rutasCamiones: RutaCamion[],
  fechaHoraInicioIntervalo: string | null,
  fechaHoraFinIntervalo: string | null,
  estadoSimulacion: {
    horaActual: number;
    horaSimulacion: string;
    fechaHoraSimulacion: string | null;
    fechaInicioSimulacion: string | null;
    diaSimulacion: number | null;
    tiempoRealSimulacion: string;
    tiempoTranscurridoSimulado: string;
    camiones: CamionEstado[];
    rutasCamiones: RutaCamion[];
    almacenes: Almacen[];
    bloqueos: Bloqueo[];
  },
  funciones: {
    marcarCamionAveriado: (camionId: string) => void;
    setAveriando: (value: string | null) => void;
    setClickedCamion: (value: string | null) => void;
    setSimulacionActiva: (value: boolean) => void;
    setPollingActivo?: (value: boolean) => void;
    aplicarNuevaSolucionDespuesAveria?: (data: IndividuoConBloqueos) => Promise<void>;
  }
): Promise<{ nuevoEstado: EstadoAveriasAutomaticas; averiaEjecutada: boolean }> => {
  // Verificar si debe ocurrir una avería
  if (!debeOcurrirAveriaAutomatica(estadoAverias, paqueteActual)) {
    return { nuevoEstado: estadoAverias, averiaEjecutada: false };
  }

  // Verificar que tengamos las fechas del intervalo
  if (!fechaHoraInicioIntervalo || !fechaHoraFinIntervalo) {
    console.warn('⚠️ AVERÍA AUTOMÁTICA: No se pueden obtener fechas del intervalo');
    return { nuevoEstado: estadoAverias, averiaEjecutada: false };
  }

  // Obtener camiones candidatos
  const candidatos = obtenerCamionesCandidatosAveria(camiones, rutasCamiones, estadoAverias);
  
  if (candidatos.length === 0) {
    console.log('⚠️ AVERÍA AUTOMÁTICA: No hay camiones candidatos para avería automática');
    return { nuevoEstado: estadoAverias, averiaEjecutada: false };
  }

  // Seleccionar camión (el primero después del ordenamiento)
  const camionSeleccionado = candidatos[0];
  
  // Calcular momento de la avería
  const momentoAveria = calcularMomentoAveria(fechaHoraInicioIntervalo, fechaHoraFinIntervalo);
  
  // Seleccionar tipo de avería
  const tipoAveria = seleccionarTipoAveria();

  console.log(`🎯 AVERÍA AUTOMÁTICA: Seleccionado camión ${camionSeleccionado.id} para avería automática`);
  console.log(`📅 AVERÍA AUTOMÁTICA: Momento calculado: ${momentoAveria}`);
  console.log(`🔧 AVERÍA AUTOMÁTICA: Tipo seleccionado: ${tipoAveria}`);

  // Ejecutar la avería
  const averiaExitosa = await ejecutarAveriaAutomatica(
    camionSeleccionado,
    tipoAveria,
    momentoAveria,
    estadoSimulacion,
    funciones
  );

  // Actualizar estado
  const nuevoEstado: EstadoAveriasAutomaticas = {
    ...estadoAverias,
    ultimoPaqueteConAveria: paqueteActual,
    averiasRealizadas: averiaExitosa 
      ? [...estadoAverias.averiasRealizadas, camionSeleccionado.id]
      : estadoAverias.averiasRealizadas
  };

  return { nuevoEstado, averiaEjecutada: averiaExitosa };
};

/**
 * @function parseCoord
 * @description Función auxiliar para parsear coordenadas (importada desde utils)
 */
function parseCoord(coordStr: string): { x: number; y: number } {
  const match = coordStr.match(/\((\d+),(\d+)\)/);
  if (!match) {
    throw new Error(`Formato de coordenada inválido: ${coordStr}`);
  }
  return {
    x: parseInt(match[1]),
    y: parseInt(match[2])
  };
} 