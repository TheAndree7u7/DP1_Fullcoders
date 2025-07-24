// Servicio para recargar todos los datos desde el backend

export interface DataReloadResponse {
  cantidadAlmacenes: number;
  cantidadCamiones: number;
  cantidadPedidos: number;
  cantidadAverias: number;
  cantidadMantenimientos: number;
  cantidadBloqueos: number;
  mensaje: string;
  exito: boolean;
}

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8085';

export const dataReloadService = {
  async recargarTodos(): Promise<DataReloadResponse> {
    const response = await fetch(`${API_BASE_URL}/api/data-reload/recargar-todos`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
    });
    if (!response.ok) {
      throw new Error('Error al recargar los datos');
    }
    return response.json();
  },
}; 