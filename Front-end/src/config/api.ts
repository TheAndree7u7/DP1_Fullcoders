// Archivo de configuración para la URL base de la API
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

// Configuración centralizada de la API
export const API_CONFIG = {
  BASE_URL: API_BASE_URL,
  ENDPOINTS: {
    SIMULACION: "/simulacion",
    ALMACENES: "/almacenes",
    MEJOR_INDIVIDUO: "/simulacion/mejor"
  }
};

// URLs completas para uso directo
export const API_URLS = {
  SIMULACION_BASE: `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.SIMULACION}`,
  ALMACENES: `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.ALMACENES}`,
  MEJOR_INDIVIDUO: `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.MEJOR_INDIVIDUO}`
};
