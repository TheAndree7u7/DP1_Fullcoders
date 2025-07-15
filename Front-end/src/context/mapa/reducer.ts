import type { CamionMapa, MapaState, PedidoMapa, AlmacenMapa, BloqueoMapa } from "./types";

interface DatosSimulacion {
  camiones?: CamionMapa[];
  pedidos?: PedidoMapa[];
  almacenes?: AlmacenMapa[];
  bloqueos?: BloqueoMapa[];
}

export type MapaAction =
  | { type: 'SET_LOADING'; payload: boolean }
  | { type: 'SET_ERROR'; payload: string }
  | { type: 'ACTUALIZAR_DATOS_SIMULACION'; payload: DatosSimulacion }
  | { type: 'ACTUALIZAR_POSICIONES_CAMIONES'; payload: CamionMapa[] }
  | { type: 'TOGGLE_ANIMACION' }
  | { type: 'RESET' };

export const mapaReducer = (state: MapaState, action: MapaAction): MapaState => {
  switch (action.type) {
    case 'SET_LOADING':
      return { ...state, isLoading: action.payload };
    
    case 'SET_ERROR':
      return { ...state, error: action.payload, isLoading: false };
    
    case 'ACTUALIZAR_DATOS_SIMULACION':
      return {
        ...state,
        camiones: action.payload.camiones || [],
        pedidos: action.payload.pedidos || [],
        almacenes: action.payload.almacenes || [],
        bloqueos: action.payload.bloqueos || [],
        ultimaActualizacion: new Date(),
        isLoading: false,
        error: null
      };
    
    case 'ACTUALIZAR_POSICIONES_CAMIONES':
      return { ...state, camiones: action.payload };
    
    case 'RESET':
      return {
        camiones: [],
        pedidos: [],
        almacenes: [],
        bloqueos: [],
        isLoading: false,
        error: null,
        ultimaActualizacion: null
      };
    
    default:
      return state;
  }
};
