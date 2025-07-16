import React, { useReducer, useEffect } from "react";
import type { Camion, RutaCamion, Almacen } from "../types";
import { getAlmacenes } from "../services/almacenApiService";

// Types
export interface Bloqueo {
  coordenadas: { x: number; y: number }[];
  fechaInicio: string; // ISO string
  fechaFin: string;    // ISO string
}

export interface SimulacionState {
  // Tiempo y estado de simulación
  tiempoTranscurridoSimulado: number;
  tiempoRealSimulacion: Date | null;
  fechaHoraSimulacion: string | null;
  horaActual: number;
  
  // Estado de ejecución
  isRunning: boolean;
  isPaused: boolean;
  speed: number;
  
  // Datos de simulación
  bloqueos: Bloqueo[];
  rutasCamiones: RutaCamion[];
  camiones: Camion[];
  almacenes: Almacen[];
  
  // Estado de carga
  isLoading: boolean;
  error: string | null;
}

export interface SimulacionActions {
  startSimulation: () => void;
  pauseSimulation: () => void;
  stopSimulation: () => void;
  resetSimulation: () => void;
  setSpeed: (speed: number) => void;
  updateTime: (tiempo: number) => void;
  setError: (error: string | null) => void;
  setLoading: (loading: boolean) => void;
  setBloqueos: (bloqueos: Bloqueo[]) => void;
  addBloqueo: (bloqueo: Bloqueo) => void;
  removeBloqueo: (index: number) => void;
  setRutasCamiones: (rutas: RutaCamion[]) => void;
  setCamiones: (camiones: Camion[]) => void;
  setAlmacenes: (almacenes: Almacen[]) => void;
}

export type SimulacionContextType = SimulacionState & SimulacionActions;

// Reducer for simulation state management
type SimulacionAction =
  | { type: 'START_SIMULATION' }
  | { type: 'PAUSE_SIMULATION' }
  | { type: 'STOP_SIMULATION' }
  | { type: 'RESET_SIMULATION' }
  | { type: 'SET_SPEED'; payload: number }
  | { type: 'UPDATE_TIME'; payload: number }
  | { type: 'SET_ERROR'; payload: string | null }
  | { type: 'SET_LOADING'; payload: boolean }
  | { type: 'UPDATE_REAL_TIME'; payload: Date }
  | { type: 'SET_BLOQUEOS'; payload: Bloqueo[] }
  | { type: 'ADD_BLOQUEO'; payload: Bloqueo }
  | { type: 'REMOVE_BLOQUEO'; payload: number }
  | { type: 'SET_RUTAS_CAMIONES'; payload: RutaCamion[] }
  | { type: 'SET_CAMIONES'; payload: Camion[] }
  | { type: 'SET_ALMACENES'; payload: Almacen[] };

const initialState: SimulacionState = {
  tiempoTranscurridoSimulado: 0,
  tiempoRealSimulacion: null,
  fechaHoraSimulacion: null,
  horaActual: 0,
  isRunning: false,
  isPaused: false,
  speed: 1,
  bloqueos: [],
  rutasCamiones: [],
  camiones: [],
  almacenes: [],
  isLoading: false,
  error: null,
};

const simulacionReducer = (state: SimulacionState, action: SimulacionAction): SimulacionState => {
  switch (action.type) {
    case 'START_SIMULATION':
      return { ...state, isRunning: true, isPaused: false, error: null };
    
    case 'PAUSE_SIMULATION':
      return { ...state, isPaused: !state.isPaused };
    
    case 'STOP_SIMULATION':
      return { ...state, isRunning: false, isPaused: false };
    
    case 'RESET_SIMULATION':
      return { 
        ...initialState, 
        tiempoRealSimulacion: new Date(),
        fechaHoraSimulacion: new Date().toISOString()
      };
    
    case 'SET_SPEED':
      return { ...state, speed: Math.max(0.1, Math.min(10, action.payload)) };
    
    case 'UPDATE_TIME':
      return { 
        ...state, 
        tiempoTranscurridoSimulado: action.payload,
        horaActual: Math.floor(action.payload / 36) // Assuming 36 seconds per node
      };
    
    case 'SET_ERROR':
      return { ...state, error: action.payload, isLoading: false };
    
    case 'SET_LOADING':
      return { ...state, isLoading: action.payload };
    
    case 'UPDATE_REAL_TIME':
      return { 
        ...state, 
        tiempoRealSimulacion: action.payload,
        fechaHoraSimulacion: action.payload.toISOString()
      };
    
    case 'SET_BLOQUEOS':
      return { ...state, bloqueos: action.payload };
    
    case 'ADD_BLOQUEO':
      return { ...state, bloqueos: [...state.bloqueos, action.payload] };
    
    case 'REMOVE_BLOQUEO':
      return { 
        ...state, 
        bloqueos: state.bloqueos.filter((_, index) => index !== action.payload) 
      };
    
    case 'SET_RUTAS_CAMIONES':
      console.log("🔄 Reducer SET_RUTAS_CAMIONES:", action.payload.length);
      return { ...state, rutasCamiones: action.payload };
    
    case 'SET_CAMIONES':
      console.log("🔄 Reducer SET_CAMIONES:", action.payload.length);
      return { ...state, camiones: action.payload };
    
    case 'SET_ALMACENES':
      return { ...state, almacenes: action.payload };
    
    default:
      return state;
  }
};

// Context creation - import from separate file for fast refresh compatibility
import { SimulacionContext } from './SimulacionContextDefinition';

// Custom hook for time management
const useSimulationTimer = (
  state: SimulacionState,
  dispatch: React.Dispatch<SimulacionAction>
) => {
  useEffect(() => {
    let interval: number;
    
    if (state.isRunning && !state.isPaused) {
      interval = window.setInterval(() => {
        dispatch({ type: 'UPDATE_REAL_TIME', payload: new Date() });
        dispatch({ 
          type: 'UPDATE_TIME', 
          payload: state.tiempoTranscurridoSimulado + state.speed 
        });
      }, 1000); // Update every second
    }
    
    return () => {
      if (interval) window.clearInterval(interval);
    };
  }, [state.isRunning, state.isPaused, state.speed, state.tiempoTranscurridoSimulado, dispatch]);
};

// Provider component
export const SimulacionProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  const [state, dispatch] = useReducer(simulacionReducer, {
    ...initialState,
    tiempoRealSimulacion: new Date(),
    fechaHoraSimulacion: new Date().toISOString(),
  });

  // Use the timer hook
  useSimulationTimer(state, dispatch);

  // Cargar almacenes al inicializar
  useEffect(() => {
    const cargarAlmacenesIniciales = async () => {
      try {
        dispatch({ type: 'SET_LOADING', payload: true });
        const almacenes = await getAlmacenes();
        dispatch({ type: 'SET_ALMACENES', payload: almacenes });
      } catch (error) {
        console.error('Error al cargar almacenes:', error);
        dispatch({ type: 'SET_ERROR', payload: 'Error al cargar los almacenes' });
      } finally {
        dispatch({ type: 'SET_LOADING', payload: false });
      }
    };

    cargarAlmacenesIniciales();
  }, []);

  // Actions
  const actions: SimulacionActions = {
    startSimulation: () => dispatch({ type: 'START_SIMULATION' }),
    pauseSimulation: () => dispatch({ type: 'PAUSE_SIMULATION' }),
    stopSimulation: () => dispatch({ type: 'STOP_SIMULATION' }),
    resetSimulation: () => dispatch({ type: 'RESET_SIMULATION' }),
    setSpeed: (speed: number) => dispatch({ type: 'SET_SPEED', payload: speed }),
    updateTime: (tiempo: number) => dispatch({ type: 'UPDATE_TIME', payload: tiempo }),
    setError: (error: string | null) => dispatch({ type: 'SET_ERROR', payload: error }),
    setLoading: (loading: boolean) => dispatch({ type: 'SET_LOADING', payload: loading }),
    setBloqueos: (bloqueos: Bloqueo[]) => dispatch({ type: 'SET_BLOQUEOS', payload: bloqueos }),
    addBloqueo: (bloqueo: Bloqueo) => dispatch({ type: 'ADD_BLOQUEO', payload: bloqueo }),
    removeBloqueo: (index: number) => dispatch({ type: 'REMOVE_BLOQUEO', payload: index }),
    setRutasCamiones: (rutas: RutaCamion[]) => dispatch({ type: 'SET_RUTAS_CAMIONES', payload: rutas }),
    setCamiones: (camiones: Camion[]) => dispatch({ type: 'SET_CAMIONES', payload: camiones }),
    setAlmacenes: (almacenes: Almacen[]) => dispatch({ type: 'SET_ALMACENES', payload: almacenes }),
  };

  const contextValue: SimulacionContextType = {
    ...state,
    ...actions,
  };

  return (
    <SimulacionContext.Provider value={contextValue}>
      {children}
    </SimulacionContext.Provider>
  );
};






