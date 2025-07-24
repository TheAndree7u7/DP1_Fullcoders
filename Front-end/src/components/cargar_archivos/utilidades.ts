import type { EstadoCargaArchivos } from "../../types";

export const puedenCargarseArchivos = (estadoCarga: EstadoCargaArchivos): boolean => {
  return (
    estadoCarga.ventas.cargado &&
    estadoCarga.bloqueos.cargado &&
    estadoCarga.camiones.cargado &&
    estadoCarga.mantenimiento.cargado
  );
};

export const formatearTamanoArchivo = (bytes: number): string => {
  if (bytes === 0) return '0 Bytes';
  const k = 1024;
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)).toString();
};

// Función para descargar un archivo cargado
export const descargarArchivoCargado = (archivo: { nombre: string; contenido: string }) => {
  const blob = new Blob([archivo.contenido], { type: 'text/plain' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = archivo.nombre;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
}; 