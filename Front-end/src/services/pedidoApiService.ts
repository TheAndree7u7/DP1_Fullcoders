import { API_URLS } from '../config/api';

/**
 * Interfaz para la respuesta de limpieza de pedidos
 */
export interface LimpiarPedidosResponse {
  mensaje: string;
}

/**
 * Limpia todos los pedidos almacenados en el sistema
 * @returns Promise con el mensaje de confirmación
 */
export async function limpiarPedidos(): Promise<string> {
  try {
    console.log('🧹 FRONTEND: Iniciando limpieza de pedidos...');
    
    const response = await fetch(`${API_URLS.PEDIDOS}/limpiar`, {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json',
      }
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Error ${response.status}: ${errorText}`);
    }

    const data = await response.json();
    console.log('✅ FRONTEND: Pedidos limpiados exitosamente:', data);
    
    return data;
  } catch (error) {
    console.error('❌ FRONTEND: Error al limpiar pedidos:', error);
    throw error;
  }
}

 