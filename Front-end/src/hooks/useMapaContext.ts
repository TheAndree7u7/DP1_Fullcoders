import { useSimulacion } from '../context/SimulacionContext';
import { useTiempoReal } from '../context/TiempoRealContext';
import type { CamionEstado, RutaCamion, Almacen, Bloqueo } from '../types';

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
    camiones,
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
  if (contextType === 'simulacion') {
    return useMapaWithSimulacion();
  } else {
    return useMapaWithTiempoReal();
  }
};