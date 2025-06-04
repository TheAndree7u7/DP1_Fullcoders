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
  const [nodosRestantesAntesDeActualizar, setNodosRestantesAntesDeActualizar] = useState<number>(25);
  const [esperandoActualizacion, setEsperandoActualizacion] = useState<boolean>(false);

  useEffect(() => {
    cargarDatos(true);
  }, []);

  const cargarDatos = async (esInicial: boolean = false) => {
    if (esInicial) setCargando(true);
    try {
      console.log("Iniciando solicitud al servidor...");
      const data: Individuo = await getMejorIndividuo();
      console.log("Datos recibidos:", data);
      const nuevasRutas: RutaCamion[] = data.cromosoma.map((gen) => ({
        id: gen.camion.codigo,
        ruta: gen.nodos.map(n => `(${n.coordenada.x},${n.coordenada.y})`),
        puntoDestino: `(${gen.destino.x},${gen.destino.y})`,
        pedidos: gen.pedidos,
      }));

      setRutasCamiones(nuevasRutas);

      const nuevosCamiones: CamionEstado[] = nuevasRutas.map((ruta) => {
        const anterior = camiones.find(c => c.id === ruta.id);
        const ubicacion = anterior?.ubicacion ?? ruta.ruta[0];
        return {
          id: ruta.id,
          ubicacion,
          porcentaje: 0,
          estado: 'En Camino',
        };
      });

      setCamiones(nuevosCamiones);
      if (esInicial) setHoraActual(1);
      setNodosRestantesAntesDeActualizar(25);
      setEsperandoActualizacion(false);
    } catch (error) {
      console.error("Error al cargar datos de simulación:", error);
    } finally {
      if (esInicial) setCargando(false);
    }
  };

  const avanzarHora = async () => {
    if (esperandoActualizacion) return;

    const nuevosCamiones = camiones.map((camion) => {
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
    });

    const quedan = nodosRestantesAntesDeActualizar - 1;
    setNodosRestantesAntesDeActualizar(quedan);

    if (quedan <= 0) {
      setEsperandoActualizacion(true);
      setCamiones(nuevosCamiones);
      setHoraActual(prev => prev + 1);
      await cargarDatos(false);
    } else {
      setCamiones(nuevosCamiones);
      setHoraActual(prev => prev + 1);
    }
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
    setNodosRestantesAntesDeActualizar(25);
    setEsperandoActualizacion(false);
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
