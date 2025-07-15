import React, { useReducer, useCallback } from "react";
import { MapaContext } from "../MapaContextDefinition";
import { mapaReducer } from "./reducer";
import { transformarDatos } from "./transformers";
import type { MapaState, CamionMapa, BloqueoMapa } from "./types";
import type { Individuo, Almacen, Camion, RutaCamion } from "../../types";

const initialState: MapaState = {
  camiones: [],
  pedidos: [],
  almacenes: [],
  bloqueos: [],
  isLoading: false,
  error: null,
  ultimaActualizacion: null,
};

export const MapaProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [state, dispatch] = useReducer(mapaReducer, initialState);

  const actualizarDatosMapa = useCallback((_: Individuo, almacenesBase: Almacen[], bloqueosBase: BloqueoMapa[]) => {
    const datosTransformados = transformarDatos({
      camiones: [],
      almacenes: almacenesBase,
      bloqueos: bloqueosBase
    });
    dispatch({ type: 'ACTUALIZAR_DATOS_SIMULACION', payload: datosTransformados });
  }, []);

  const actualizarDatosSimulacion = useCallback((
    camiones: Camion[], 
    rutasCamiones: RutaCamion[], 
    almacenes: Almacen[], 
    bloqueos: BloqueoMapa[]
  ) => {
    console.log("🔄 MapaProvider.actualizarDatosSimulacion recibió:", {
      camiones: camiones.length,
      rutasCamiones: rutasCamiones.length,
      almacenes: almacenes.length,
      bloqueos: bloqueos.length,
      datosCamiones: camiones.map(c => ({ id: c.id, codigo: c.codigo, coordenada: c.coordenada })),
      datosRutas: rutasCamiones.map(r => ({ id: r.id, rutaLength: r.ruta.length, pedidos: r.pedidos.length }))
    });

    const datosTransformados = transformarDatos({
      camiones,
      rutasCamiones,
      almacenes,
      bloqueos
    });

    console.log("✅ Datos transformados en MapaProvider:", {
      camiones: datosTransformados.camiones.length,
      pedidos: datosTransformados.pedidos.length,
      almacenes: datosTransformados.almacenes.length,
      bloqueos: datosTransformados.bloqueos.length,
      camionesSample: datosTransformados.camiones.slice(0, 2),
      pedidosSample: datosTransformados.pedidos.slice(0, 2)
    });

    dispatch({ type: 'ACTUALIZAR_DATOS_SIMULACION', payload: datosTransformados });
  }, []);

  const limpiarMapa = useCallback(() => {
    dispatch({ type: 'RESET' });
  }, []);

  const setError = useCallback((error: string | null) => {
    if (error) {
      dispatch({ type: 'SET_ERROR', payload: error });
    }
  }, []);

  const setLoading = useCallback((loading: boolean) => {
    dispatch({ type: 'SET_LOADING', payload: loading });
  }, []);

  const actualizarPosicionesCamiones = useCallback((camiones: CamionMapa[]) => {
    dispatch({ type: 'ACTUALIZAR_POSICIONES_CAMIONES', payload: camiones });
  }, []);

  const value = {
    ...state,
    actualizarDatosMapa,
    actualizarDatosSimulacion,
    limpiarMapa,
    setError,
    setLoading,
    actualizarPosicionesCamiones,
  };

  return (
    <MapaContext.Provider value={value}>
      {children}
    </MapaContext.Provider>
  );
};
