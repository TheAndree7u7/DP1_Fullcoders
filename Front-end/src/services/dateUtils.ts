/**
 * Utility functions for date formatting and manipulation
 */

/**
 * Formats a date input to the API expected format (YYYY-MM-DDTHH:MM:SS)
 * @param fechaInput - Date string from datetime-local input
 * @returns Formatted date string without timezone information
 */
export const formatearFechaParaAPI = (fechaInput: string): string => {
  const fecha = new Date(fechaInput);
  
  const year = fecha.getFullYear();
  const month = String(fecha.getMonth() + 1).padStart(2, '0');
  const day = String(fecha.getDate()).padStart(2, '0');
  const hours = String(fecha.getHours()).padStart(2, '0');
  const minutes = String(fecha.getMinutes()).padStart(2, '0');
  const seconds = String(fecha.getSeconds()).padStart(2, '0');
  
  return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`;
};

/**
 * Gets the default simulation start date (January 1st, 2025 at 00:00)
 * @returns Date string in datetime-local format
 */
export const obtenerFechaInicioDefault = (): string => {
  return '2025-01-01T00:00';
};

/**
 * Validates if a date string is in the correct format for the API
 * @param fecha - Date string to validate
 * @returns True if the format is correct
 */
export const validarFormatoFechaAPI = (fecha: string): boolean => {
  const regex = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}$/;
  return regex.test(fecha);
};
