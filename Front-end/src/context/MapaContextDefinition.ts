import { createContext } from "react";
import type { MapaState, CamionMapa, BloqueoMapa } from "./mapa/types";
import type { Individuo, Almacen, Camion, RutaCamion } from "../types";

export interface MapaActions {
  actualizarDatosMapa: (paquete: Individuo, almacenesBase: Almacen[], bloqueosBase: BloqueoMapa[]) => void;
  actualizarDatosSimulacion: (camiones: Camion[], rutasCamiones: RutaCamion[], almacenes: Almacen[], bloqueos: BloqueoMapa[]) => void;
  limpiarMapa: () => void;
  setError: (error: string | null) => void;
  setLoading: (loading: boolean) => void;
  actualizarPosicionesCamiones: (camiones: CamionMapa[]) => void;
}

export type MapaContextType = MapaState & MapaActions;

export const MapaContext = createContext<MapaContextType | undefined>(undefined);
