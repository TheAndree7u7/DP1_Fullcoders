// context/SimulacionContext.tsx
import React, { createContext, useContext, useEffect, useState } from 'react';
import { rutasCamiones, estadosPorInstante } from '../data/informacion';

// Tipos
export interface CamionEstado {
  id: string;
  ubicacion: string;
  porcentaje: number;
  estado: string;
}

export interface RutaCamion {
  id: string;
  ruta: string[];
  puntoDestino: string;
  pedido: string;
}

interface SimulacionContextType {
  horaActual: number;
  camiones: CamionEstado[];
  rutasCamiones: RutaCamion[];
  avanzarHora: () => void;
  reiniciar: () => void;
}

// Crear contexto
const SimulacionContext = createContext<SimulacionContextType | undefined>(undefined);

// Provider
export const SimulacionProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [horaActual, setHoraActual] = useState<number>(1);
  const [camiones, setCamiones] = useState<CamionEstado[]>([]);

  useEffect(() => {
    const estadoInicial = estadosPorInstante.find(e => e.timestamp === 1);
    if (estadoInicial) {
      setCamiones(estadoInicial.camiones);
    }
  }, []);

  const avanzarHora = () => {
    const siguiente = estadosPorInstante.find(e => e.timestamp === horaActual + 1);
    if (siguiente) {
      setHoraActual(siguiente.timestamp);
      setCamiones(siguiente.camiones);
    }
  };

  const reiniciar = () => {
    const inicial = estadosPorInstante.find(e => e.timestamp === 1);
    if (inicial) {
      setHoraActual(1);
      setCamiones(inicial.camiones);
    }
  };

  return (
    <SimulacionContext.Provider value={{ horaActual, camiones, rutasCamiones, avanzarHora, reiniciar }}>
      {children}
    </SimulacionContext.Provider>
  );
};

// Hook de acceso
export const useSimulacion = (): SimulacionContextType => {
  const context = useContext(SimulacionContext);
  if (!context) {
    throw new Error('useSimulacion debe usarse dentro de SimulacionProvider');
  }
  return context;
};
