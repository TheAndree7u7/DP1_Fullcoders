// --- Tipos del backend ---

export interface Camion {
  codigo: string;
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
}

export interface Gen {
  camion: Camion;
  nodos: Nodo[];
  destino: Coordenada;
  pedidos: Pedido[];
}

export interface Individuo {
  cromosoma: Gen[];
}

// --- Tipos del contexto de simulación ---

export interface CamionEstado {
  id: string; // Corresponde a camion.codigo
  ubicacion: string; // Ejemplo: "(12,8)"
  porcentaje: number; // Avance de la ruta
  estado: 'En Camino' | 'Entregado';
}

export interface RutaCamion {
  id: string; // Corresponde a camion.codigo
  ruta: string[]; // Ejemplo: ["(12,8)", "(13,8)"]
  puntoDestino: string; // Ejemplo: "(57,17)"
  pedidos: Pedido[];
}
