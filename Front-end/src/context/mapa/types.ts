import type { Coordenada } from "../../types";

export interface CamionMapa {
  id: string;
  codigo: string;
  color: string;
  ruta: Coordenada[];
  posicion: Coordenada;
  rotacion: number;
  destino: Coordenada | null;
  pedidosAsignados: string[];
  estado: string;
  progreso: number;
  nodoActual: number;
  posicionInterpolada: Coordenada;
  enMovimiento: boolean;
  tiempoInicioMovimiento: number;
}

export interface PedidoMapa {
  codigo: string;
  coordenada: Coordenada;
  estado: string;
  volumenGLP: number;
  horasLimite: number;
  camionAsignado: string | null;
}

export interface AlmacenMapa {
  id: string;
  nombre: string;
  tipo: string;
  coordenada: Coordenada;
  capacidadActualGLP: number;
  capacidadMaximaGLP: number;
  capacidadCombustible: number;
  capacidadMaximaCombustible: number;
  esCentral: boolean;
}

export interface BloqueoMapa {
  coordenadas: Coordenada[];
  activo: boolean;
  fechaInicio: string;
  fechaFin: string;
}

export interface MapaState {
  camiones: CamionMapa[];
  pedidos: PedidoMapa[];
  almacenes: AlmacenMapa[];
  bloqueos: BloqueoMapa[];
  isLoading: boolean;
  error: string | null;
  ultimaActualizacion: Date | null;
}
