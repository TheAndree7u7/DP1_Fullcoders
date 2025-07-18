import type { EstadoCargaArchivos } from "../../types";

// Función para verificar si se pueden cargar todos los archivos
export const puedenCargarseArchivos = (estadoCarga: EstadoCargaArchivos): boolean => {
  return estadoCarga.ventas.cargado && estadoCarga.bloqueos.cargado && estadoCarga.camiones.cargado && estadoCarga.mantenimiento.cargado;
};

// Función para formatear el tamaño del archivo en KB
export const formatearTamanoArchivo = (bytes: number): string => {
  return (bytes / 1024).toFixed(2);
}; 