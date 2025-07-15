import React, { useReducer, useCallback } from "react";
import type { Individuo } from "../types";
import { PaqueteContext } from "./PaqueteContextDefinition";

// ============================
// TYPES Y INTERFACES
// ============================

export interface PaqueteState {
  paqueteActual: Individuo | null;
  isLoading: boolean;
  error: string | null;
  ultimaActualizacion: Date | null;
}

export interface PaqueteActions {
  setPaquete: (paquete: Individuo | null) => void;
  setLoading: (loading: boolean) => void;
  setError: (error: string | null) => void;
  limpiarPaquete: () => void;
}

export type PaqueteContextType = PaqueteState & PaqueteActions;

// ============================
// REDUCER
// ============================

type PaqueteAction =
  | { type: 'SET_PAQUETE'; payload: Individuo | null }
  | { type: 'SET_LOADING'; payload: boolean }
  | { type: 'SET_ERROR'; payload: string | null }
  | { type: 'LIMPIAR_PAQUETE' };

const initialState: PaqueteState = {
  paqueteActual: null,
  isLoading: false,
  error: null,
  ultimaActualizacion: null,
};

const paqueteReducer = (state: PaqueteState, action: PaqueteAction): PaqueteState => {
  switch (action.type) {
    case 'SET_PAQUETE':
      return {
        ...state,
        paqueteActual: action.payload,
        ultimaActualizacion: action.payload ? new Date() : null,
        error: null,
        isLoading: false,
      };
    
    case 'SET_LOADING':
      return {
        ...state,
        isLoading: action.payload,
        error: action.payload ? null : state.error,
      };
    
    case 'SET_ERROR':
      return {
        ...state,
        error: action.payload,
        isLoading: false,
      };
    
    case 'LIMPIAR_PAQUETE':
      return {
        ...initialState,
      };
    
    default:
      return state;
  }
};

// ============================
// PROVIDER
// ============================

export const PaqueteProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  const [state, dispatch] = useReducer(paqueteReducer, initialState);

  // Actions
  const setPaquete = useCallback((paquete: Individuo | null) => {
    dispatch({ type: 'SET_PAQUETE', payload: paquete });
  }, []);

  const setLoading = useCallback((loading: boolean) => {
    dispatch({ type: 'SET_LOADING', payload: loading });
  }, []);

  const setError = useCallback((error: string | null) => {
    dispatch({ type: 'SET_ERROR', payload: error });
  }, []);

  const limpiarPaquete = useCallback(() => {
    dispatch({ type: 'LIMPIAR_PAQUETE' });
  }, []);

  const actions: PaqueteActions = {
    setPaquete,
    setLoading,
    setError,
    limpiarPaquete,
  };

  const contextValue: PaqueteContextType = {
    ...state,
    ...actions,
  };

  return (
    <PaqueteContext.Provider value={contextValue}>
      {children}
    </PaqueteContext.Provider>
  );
};
