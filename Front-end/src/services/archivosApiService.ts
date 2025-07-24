import { API_CONFIG } from '../config/api';
import type { DatosVentas } from '../types';

// Tipos para las solicitudes y respuestas
export interface ArchivoPedidosRequest {
  nombre: string;
  contenido: string;
  datos: DatosVentas[];
}

export interface Pedido {
  codigo: string;
  coordenada: {
    fila: number;
    columna: number;
  };
  horasLimite: number;
  volumenGLPAsignado: number;
  estado: string;
  fechaRegistro: string;
  fechaLimite: string;
}

export interface ArchivoPedidosResponse {
  nombreArchivo: string;
  totalPedidosAgregados: number;
  pedidosAgregados: Pedido[];
  mensaje: string;
}

/**
 * Servicio para manejar archivos de pedidos
 */
export class ArchivosApiService {
  private static readonly BASE_URL = `${API_CONFIG.BASE_URL}/archivos`;

  /**
   * Procesa un archivo completo de pedidos
   */
  static async procesarArchivoPedidos(request: ArchivoPedidosRequest): Promise<ArchivoPedidosResponse> {
    try {
      const response = await fetch(`${this.BASE_URL}/pedidos`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(request),
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`Error ${response.status}: ${errorText}`);
      }

      return await response.json();
    } catch (error) {
      console.error('Error procesando archivo de pedidos:', error);
      throw error;
    }
  }

  /**
   * Procesa pedidos individuales
   */
  static async procesarPedidosIndividuales(request: ArchivoPedidosRequest): Promise<ArchivoPedidosResponse> {
    try {
      const response = await fetch(`${this.BASE_URL}/pedidos/individuales`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(request),
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`Error ${response.status}: ${errorText}`);
      }

      return await response.json();
    } catch (error) {
      console.error('Error procesando pedidos individuales:', error);
      throw error;
    }
  }
} 