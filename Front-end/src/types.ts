// --- Tipos del backend ---

// Interfaces de Almacén
export interface AlmacenBackend {
  coordenada: { fila: number; columna: number };
  nombre: string;
  tipo: 'CENTRAL' | 'SECUNDARIO';
  activo: boolean;
}

export interface Almacen {
  id: string;
  nombre: string;
  tipo: 'CENTRAL' | 'INTERMEDIO';
  coordenada: { x: number; y: number };
  activo: boolean;
}

export interface Camion {
  codigo: string;
  capacidadActualGLP: number;  // Can be decimal
  capacidadMaximaGLP: number;  // Can be decimal
  combustibleActual: number;   // Can be decimal
  combustibleMaximo: number;   // Can be decimal
  distanciaMaxima: number;     // Can be decimal
  estado: string;
  pesoCarga: number;           // Can be decimal
  pesoCombinado: number;       // Can be decimal
  tara: number;                // Can be decimal
  tipo: string;
  velocidadPromedio: number;   // Can be decimal
}

// --- Funciones de cálculo para camiones ---
export function calcularPesoCarga(camion: Camion): number {
  if (camion.capacidadMaximaGLP === 0) return 0;
  return (camion.capacidadActualGLP / camion.capacidadMaximaGLP) * camion.pesoCarga;
}

export function calcularPesoCombinado(camion: Camion): number {
  const pesoCargaActual = calcularPesoCarga(camion);
  return camion.tara + pesoCargaActual;
}

export function calcularConsumoGalones(camion: Camion, distanciaKm: number): number {
  const pesoCombinado = calcularPesoCombinado(camion);
  return (distanciaKm * pesoCombinado) / 180;
}

export function calcularDistanciaMaxima(camion: Camion): number {
  const pesoCombinado = calcularPesoCombinado(camion);
  if (pesoCombinado === 0) return 0;
  // Usamos combustibleActual en lugar de combustibleMaximo para obtener la distancia restante
  return (camion.combustibleActual * 180) / pesoCombinado;
}

export function actualizarCamion(camion: Camion): void {
  camion.pesoCarga = calcularPesoCarga(camion);
  camion.pesoCombinado = camion.tara + camion.pesoCarga;
  camion.distanciaMaxima = calcularDistanciaMaxima(camion);
}

export interface Coordenada {
  x: number;
  y: number;
}

export interface Nodo {
  coordenada: Coordenada;
}

export interface Pedido {
  codigo: string;
  coordenada: Coordenada; // Heredado de Nodo
  horasLimite?: number;
  volumenGLPAsignado?: number;
  estado?: string;
}

export interface Gen {
  camion: Camion;
  nodos: Nodo[];
  destino: Coordenada;
  pedidos: Pedido[];
}

export interface Individuo {
  cromosoma: Gen[];
  fechaHoraSimulacion?: string; // Fecha y hora de la simulación
}

// --- Tipos del contexto de simulación ---
export interface CamionEstado {
  id: string; // Corresponde a camion.codigo
  ubicacion: string; // Ejemplo: "(12,8)"
  porcentaje: number; // Avance de la ruta
  estado: 'En Camino' | 'Entregado' | 'DISPONIBLE' | 'NO_DISPONIBLE' | 'EN_RUTA' | 'ENTREGANDO_GLP_A_CLIENTE' | 
          'EN_MANTENIMIENTO' | 'EN_MANTENIMIENTO_PREVENTIVO' | 'EN_MANTENIMIENTO_CORRECTIVO' | 
          'EN_MANTENIMIENTO_POR_AVERIA' | 'INMOVILIZADO_POR_AVERIA' | 'SIN_COMBUSTIBLE' | 
          'RECIBIENDO_COMBUSTIBLE' | 'ENTREGANDO_COMBUSTIBLE_A_CAMION' | 'RECIBIENDO_GLP' | 
          'ENTREGANDO_GLP_A_CAMION' | 'ALMACEN_TEMPORAL';
}

export interface RutaCamion {
  id: string; // Corresponde a camion.codigo
  ruta: string[]; // Ejemplo: ["(12,8)", "(13,8)"]
  puntoDestino: string; // Ejemplo: "(57,17)"
  pedidos: Pedido[];
}

export interface Simulacion {
  fechaHoraSimulacion: string; // JavaScript recibe LocalDateTime como string ISO
}