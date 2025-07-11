import { useSimulacion } from '../context/SimulacionContext';
import { useTiempoReal } from '../context/TiempoRealContext';
import type { RutaCamion, Almacen } from '../types';
import type { CamionEstado, Bloqueo } from '../context/SimulacionContext';

export interface MapaContextInterface {
  // Datos principales
  camiones: CamionEstado[];
  rutasCamiones: RutaCamion[];
  almacenes: Almacen[];
  bloqueos: Bloqueo[];
  cargando: boolean;
  
  // Datos de tiempo
  horaSimulacion: string;
  fechaHoraSimulacion: string | null;
  
  // Estado de simulación
  simulacionActiva: boolean;
  
  // Funciones
  onTruckBreakdown: (camionId: string) => void;
  onTimeToggle?: () => void;
  
  // Tipo de contexto
  contextType: 'simulacion' | 'tiempo-real';
}

export const useMapaWithSimulacion = (): MapaContextInterface => {
  const {
    camiones,
    rutasCamiones,
    almacenes,
    bloqueos,
    cargando,
    horaSimulacion,
    fechaHoraSimulacion,
    simulacionActiva,
    marcarCamionAveriado,
    avanzarHora,
  } = useSimulacion();

  return {
    camiones,
    rutasCamiones,
    almacenes,
    bloqueos,
    cargando,
    horaSimulacion,
    fechaHoraSimulacion,
    simulacionActiva,
    onTruckBreakdown: marcarCamionAveriado,
    onTimeToggle: avanzarHora,
    contextType: 'simulacion',
  };
};

export const useMapaWithTiempoReal = (): MapaContextInterface => {
  const {
    camiones,
    rutasCamiones,
    almacenes,
    bloqueos,
    cargando,
    horaSimulacion,
    fechaHoraSimulacion,
    ejecutando,
    iniciar,
    pausar,
  } = useTiempoReal();

  const handleTimeToggle = () => {
    if (ejecutando) {
      pausar();
    } else {
      iniciar();
    }
  };

  return {
    camiones: camiones as unknown as CamionEstado[],
    rutasCamiones,
    almacenes,
    bloqueos,
    cargando,
    horaSimulacion,
    fechaHoraSimulacion,
    simulacionActiva: ejecutando,
    onTruckBreakdown: () => {
      // En tiempo real, no implementamos avería de camiones por ahora
      console.log('Avería de camiones no implementada en tiempo real');
    },
    onTimeToggle: handleTimeToggle,
    contextType: 'tiempo-real',
  };
};

export const useMapaContext = (contextType: 'simulacion' | 'tiempo-real'): MapaContextInterface => {
  const simulacionMap = useMapaWithSimulacion();
  
  // Por ahora, siempre usar simulacion hasta que se configure tiempo-real correctamente
  return simulacionMap;
};