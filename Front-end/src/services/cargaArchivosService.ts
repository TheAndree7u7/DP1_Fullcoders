import type { ArchivoCarga, EstadoCargaArchivos } from "../types";

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

export interface CargaArchivosResponse {
  success: boolean;
  message: string;
  errores?: string[];
}

class CargaArchivosService {
  // Limpiar todos los archivos existentes
  async limpiarArchivos(): Promise<CargaArchivosResponse> {
    try {
      const response = await fetch(`${API_BASE_URL}/api/simulacion/limpiar-archivos`, {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      const data = await response.json();
      return data;
    } catch (error) {
      return {
        success: false,
        message: `Error al limpiar archivos: ${error}`,
        errores: [error instanceof Error ? error.message : 'Error desconocido']
      };
    }
  }

  // Cargar archivo de ventas
  async cargarArchivoVentas(archivo: ArchivoCarga): Promise<CargaArchivosResponse> {
    try {
      const formData = new FormData();
      const blob = new Blob([archivo.contenido], { type: 'text/plain' });
      formData.append('archivo', blob, archivo.nombre);

      const response = await fetch(`${API_BASE_URL}/api/simulacion/cargar-ventas`, {
        method: 'POST',
        body: formData,
      });

      const data = await response.json();
      return data;
    } catch (error) {
      return {
        success: false,
        message: `Error al cargar archivo de ventas: ${error}`,
        errores: [error instanceof Error ? error.message : 'Error desconocido']
      };
    }
  }

  // Cargar archivo de bloqueos
  async cargarArchivoBloqueos(archivo: ArchivoCarga): Promise<CargaArchivosResponse> {
    try {
      const formData = new FormData();
      const blob = new Blob([archivo.contenido], { type: 'text/plain' });
      formData.append('archivo', blob, archivo.nombre);

      const response = await fetch(`${API_BASE_URL}/api/simulacion/cargar-bloqueos`, {
        method: 'POST',
        body: formData,
      });

      const data = await response.json();
      return data;
    } catch (error) {
      return {
        success: false,
        message: `Error al cargar archivo de bloqueos: ${error}`,
        errores: [error instanceof Error ? error.message : 'Error desconocido']
      };
    }
  }

  // Cargar archivo de camiones
  async cargarArchivoCamiones(archivo: ArchivoCarga): Promise<CargaArchivosResponse> {
    try {
      const formData = new FormData();
      const blob = new Blob([archivo.contenido], { type: 'text/plain' });
      formData.append('archivo', blob, archivo.nombre);

      const response = await fetch(`${API_BASE_URL}/api/simulacion/cargar-camiones`, {
        method: 'POST',
        body: formData,
      });

      const data = await response.json();
      return data;
    } catch (error) {
      return {
        success: false,
        message: `Error al cargar archivo de camiones: ${error}`,
        errores: [error instanceof Error ? error.message : 'Error desconocido']
      };
    }
  }

  // Cargar todos los archivos de una vez
  async cargarTodosLosArchivos(estadoCarga: EstadoCargaArchivos): Promise<CargaArchivosResponse> {
    try {
      // Primero limpiar archivos existentes
      const limpieza = await this.limpiarArchivos();
      if (!limpieza.success) {
        return limpieza;
      }

      const errores: string[] = [];

      // Cargar archivo de ventas si está disponible
      if (estadoCarga.ventas.cargado && estadoCarga.ventas.archivo) {
        const resultadoVentas = await this.cargarArchivoVentas(estadoCarga.ventas.archivo);
        if (!resultadoVentas.success) {
          errores.push(`Ventas: ${resultadoVentas.message}`);
        }
      }

      // Cargar archivo de bloqueos si está disponible
      if (estadoCarga.bloqueos.cargado && estadoCarga.bloqueos.archivo) {
        const resultadoBloqueos = await this.cargarArchivoBloqueos(estadoCarga.bloqueos.archivo);
        if (!resultadoBloqueos.success) {
          errores.push(`Bloqueos: ${resultadoBloqueos.message}`);
        }
      }

      // Cargar archivo de camiones si está disponible
      if (estadoCarga.camiones.cargado && estadoCarga.camiones.archivo) {
        const resultadoCamiones = await this.cargarArchivoCamiones(estadoCarga.camiones.archivo);
        if (!resultadoCamiones.success) {
          errores.push(`Camiones: ${resultadoCamiones.message}`);
        }
      }

      if (errores.length > 0) {
        return {
          success: false,
          message: 'Error al cargar algunos archivos',
          errores
        };
      }

      return {
        success: true,
        message: 'Todos los archivos cargados exitosamente'
      };
    } catch (error) {
      return {
        success: false,
        message: `Error al cargar archivos: ${error}`,
        errores: [error instanceof Error ? error.message : 'Error desconocido']
      };
    }
  }

  // Verificar estado de carga
  async verificarEstadoCarga(): Promise<{
    ventas: boolean;
    bloqueos: boolean;
    camiones: boolean;
  }> {
    try {
      const response = await fetch(`${API_BASE_URL}/api/simulacion/estado-archivos`);
      const data = await response.json();
      return data;
    } catch {
      return {
        ventas: false,
        bloqueos: false,
        camiones: false
      };
    }
  }
}

export const cargaArchivosService = new CargaArchivosService(); 