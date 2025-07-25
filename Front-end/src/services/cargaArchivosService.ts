import type { ArchivoCarga, EstadoCargaArchivos } from "../types";

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8085';

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
      console.log(`🚀 DEBUG: Enviando archivo de ventas al backend: ${archivo.nombre}`);
      console.log(`📄 DEBUG: Contenido (primeras 100 chars):`, archivo.contenido.substring(0, 100));
      
      const formData = new FormData();
      const blob = new Blob([archivo.contenido], { type: 'text/plain' });
      formData.append('archivo', blob, archivo.nombre);

      const url = `${API_BASE_URL}/api/simulacion/cargar-ventas`;
      console.log(`🌐 DEBUG: URL del endpoint:`, url);

      const response = await fetch(url, {
        method: 'POST',
        body: formData,
      });

      console.log(`📡 DEBUG: Respuesta del servidor:`, response.status, response.statusText);
      
      const data = await response.json();
      console.log(`📊 DEBUG: Datos de respuesta:`, data);
      
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

  // Cargar archivo de mantenimiento preventivo
  async cargarArchivoMantenimiento(archivo: ArchivoCarga): Promise<CargaArchivosResponse> {
    try {
      const formData = new FormData();
      const blob = new Blob([archivo.contenido], { type: 'text/plain' });
      formData.append('archivo', blob, archivo.nombre);

      const response = await fetch(`${API_BASE_URL}/api/simulacion/cargar-mantenimiento`, {
        method: 'POST',
        body: formData,
      });

      const data = await response.json();
      return data;
    } catch (error) {
      return {
        success: false,
        message: `Error al cargar archivo de mantenimiento: ${error}`,
        errores: [error instanceof Error ? error.message : 'Error desconocido']
      };
    }
  }

  // Cargar todos los archivos de una vez
  async cargarTodosLosArchivos(estadoCarga: EstadoCargaArchivos): Promise<CargaArchivosResponse> {
    try {
      console.log(`🚀 DEBUG: Iniciando carga de todos los archivos`);
      console.log(`📊 DEBUG: Estado de carga recibido:`, {
        ventas: estadoCarga.ventas.cargado,
        bloqueos: estadoCarga.bloqueos.cargado,
        camiones: estadoCarga.camiones.cargado,
        mantenimiento: estadoCarga.mantenimiento.cargado
      });
      
      // Primero limpiar archivos existentes
      console.log(`🧹 DEBUG: Limpiando archivos existentes...`);
      const limpieza = await this.limpiarArchivos();
      if (!limpieza.success) {
        console.log(`❌ DEBUG: Error al limpiar archivos:`, limpieza);
        return limpieza;
      }
      console.log(`✅ DEBUG: Archivos limpiados exitosamente`);

      const errores: string[] = [];

      // Cargar archivo de ventas si está disponible
      if (estadoCarga.ventas.cargado && estadoCarga.ventas.archivo) {
        console.log(`📤 DEBUG: Cargando archivo de ventas: ${estadoCarga.ventas.archivo.nombre}`);
        const resultadoVentas = await this.cargarArchivoVentas(estadoCarga.ventas.archivo);
        if (!resultadoVentas.success) {
          console.log(`❌ DEBUG: Error al cargar ventas:`, resultadoVentas);
          errores.push(`Ventas: ${resultadoVentas.message}`);
        } else {
          console.log(`✅ DEBUG: Ventas cargadas exitosamente`);
        }
      } else {
        console.log(`⚠️ DEBUG: No hay archivo de ventas para cargar`);
      }

      // Cargar archivo de bloqueos si está disponible
      if (estadoCarga.bloqueos.cargado && estadoCarga.bloqueos.archivo) {
        console.log(`📤 DEBUG: Cargando archivo de bloqueos: ${estadoCarga.bloqueos.archivo.nombre}`);
        const resultadoBloqueos = await this.cargarArchivoBloqueos(estadoCarga.bloqueos.archivo);
        if (!resultadoBloqueos.success) {
          console.log(`❌ DEBUG: Error al cargar bloqueos:`, resultadoBloqueos);
          errores.push(`Bloqueos: ${resultadoBloqueos.message}`);
        } else {
          console.log(`✅ DEBUG: Bloqueos cargados exitosamente`);
        }
      } else {
        console.log(`⚠️ DEBUG: No hay archivo de bloqueos para cargar`);
      }

      // Cargar archivo de camiones si está disponible
      if (estadoCarga.camiones.cargado && estadoCarga.camiones.archivo) {
        console.log(`📤 DEBUG: Cargando archivo de camiones: ${estadoCarga.camiones.archivo.nombre}`);
        const resultadoCamiones = await this.cargarArchivoCamiones(estadoCarga.camiones.archivo);
        if (!resultadoCamiones.success) {
          console.log(`❌ DEBUG: Error al cargar camiones:`, resultadoCamiones);
          errores.push(`Camiones: ${resultadoCamiones.message}`);
        } else {
          console.log(`✅ DEBUG: Camiones cargados exitosamente`);
        }
      } else {
        console.log(`⚠️ DEBUG: No hay archivo de camiones para cargar`);
      }

      // Cargar archivo de mantenimiento si está disponible
      if (estadoCarga.mantenimiento.cargado && estadoCarga.mantenimiento.archivo) {
        console.log(`📤 DEBUG: Cargando archivo de mantenimiento: ${estadoCarga.mantenimiento.archivo.nombre}`);
        const resultadoMantenimiento = await this.cargarArchivoMantenimiento(estadoCarga.mantenimiento.archivo);
        if (!resultadoMantenimiento.success) {
          console.log(`❌ DEBUG: Error al cargar mantenimiento:`, resultadoMantenimiento);
          errores.push(`Mantenimiento: ${resultadoMantenimiento.message}`);
        } else {
          console.log(`✅ DEBUG: Mantenimiento cargado exitosamente`);
        }
      } else {
        console.log(`⚠️ DEBUG: No hay archivo de mantenimiento para cargar`);
      }

      if (errores.length > 0) {
        console.log(`❌ DEBUG: Errores encontrados:`, errores);
        return {
          success: false,
          message: 'Error al cargar algunos archivos',
          errores
        };
      }

      console.log(`🎉 DEBUG: Todos los archivos cargados exitosamente`);
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
    mantenimiento: boolean;
  }> {
    try {
      const response = await fetch(`${API_BASE_URL}/api/simulacion/estado-archivos`);
      const data = await response.json();
      return data;
    } catch {
      return {
        ventas: false,
        bloqueos: false,
        camiones: false,
        mantenimiento: false
      };
    }
  }
}

export const cargaArchivosService = new CargaArchivosService(); 