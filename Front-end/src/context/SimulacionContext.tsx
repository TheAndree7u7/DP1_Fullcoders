import React, { createContext, useContext, useEffect, useState } from 'react';
import { getMejorIndividuo } from '../services/simulacionApiService';
import type { Individuo, Pedido } from '../types';

export interface CamionEstado {
  id: string;
  ubicacion: string; // "(x,y)"
  porcentaje: number;
  estado: 'En Camino' | 'Entregado';
}

export interface RutaCamion {
  id: string; // camion.codigo
  ruta: string[]; // ["(12,8)", "(13,8)", ...]
  puntoDestino: string; // "(x,y)"
  pedidos: Pedido[];
}

interface SimulacionContextType {
  horaActual: number;
  camiones: CamionEstado[];
  rutasCamiones: RutaCamion[];
  avanzarHora: () => void;
  reiniciar: () => void;
  cargando: boolean;
}

const SimulacionContext = createContext<SimulacionContextType | undefined>(undefined);

export const SimulacionProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [horaActual, setHoraActual] = useState<number>(0);
  const [camiones, setCamiones] = useState<CamionEstado[]>([]);
  const [rutasCamiones, setRutasCamiones] = useState<RutaCamion[]>([]);
  const [cargando, setCargando] = useState<boolean>(true);

  useEffect(() => {
    const cargarDatos = async () => {
      setCargando(true);
      try {
        const data: Individuo = await getMejorIndividuo();
        console.log(data);
        const nuevasRutas: RutaCamion[] = data.cromosoma.map((gen) => ({
          id: gen.camion.codigo,
          ruta: gen.nodos.map(n => `(${n.coordenada.x},${n.coordenada.y})`),
          puntoDestino: `(${gen.destino.x},${gen.destino.y})`,
          pedidos: gen.pedidos,
        }));

        const nuevosCamiones: CamionEstado[] = nuevasRutas.map((ruta) => ({
          id: ruta.id,
          ubicacion: ruta.ruta[0],
          porcentaje: 0,
          estado: 'En Camino',
        }));

        setRutasCamiones(nuevasRutas);
        setCamiones(nuevosCamiones);
        setHoraActual(1);
      } catch (error) {
        console.error("Error al cargar datos de simulación:", error);
      } finally {
        setCargando(false);
      }
    };

    cargarDatos();
  }, []);

  const avanzarHora = () => {
    setCamiones((prevCamiones) =>
      prevCamiones.map((camion) => {
        const ruta = rutasCamiones.find(r => r.id === camion.id);
        if (!ruta) return camion;

        const siguientePaso = camion.porcentaje + 1;
        const rutaLength = ruta.ruta.length;

        if (siguientePaso >= rutaLength) {
          return { ...camion, estado: 'Entregado', porcentaje: rutaLength - 1 };
        }

        return {
          ...camion,
          porcentaje: siguientePaso,
          ubicacion: ruta.ruta[siguientePaso],
        };
      })
    );
    setHoraActual(prev => prev + 1);
  };

  const reiniciar = () => {
    const nuevosCamiones: CamionEstado[] = rutasCamiones.map((ruta) => ({
      id: ruta.id,
      ubicacion: ruta.ruta[0],
      porcentaje: 0,
      estado: 'En Camino',
    }));
    setCamiones(nuevosCamiones);
    setHoraActual(1);
  };

  return (
    <SimulacionContext.Provider
      value={{ horaActual, camiones, rutasCamiones, avanzarHora, reiniciar, cargando }}
    >
      {children}
    </SimulacionContext.Provider>
  );
};

export const useSimulacion = (): SimulacionContextType => {
  const context = useContext(SimulacionContext);
  if (!context) throw new Error('useSimulacion debe usarse dentro de SimulacionProvider');
  return context;
};
