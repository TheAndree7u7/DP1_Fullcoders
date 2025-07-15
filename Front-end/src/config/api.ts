import axios from 'axios';

// Archivo de configuración para la URL base de la API
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8085/api';

// Configuración centralizada de la API
export const API_CONFIG = {
  BASE_URL: API_BASE_URL,
  ENDPOINTS: {
    SIMULACION: "/simulacion",
    ALMACENES: "/almacenes",
    PEDIDOS: "/pedidos",
    MEJOR_INDIVIDUO: "/simulacion/mejor",
    INICIAR_SIMULACION: "/simulacion/iniciar",
    REINICIAR_SIMULACION: "/simulacion/reiniciar",
    INFO_SIMULACION: "/simulacion/info",
    ELIMINAR_PAQUETES_FUTUROS: "/simulacion/eliminar-paquetes-futuros"
  }
};

// URLs completas para uso directo
export const API_URLS = {
  SIMULACION_BASE: `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.SIMULACION}`,
  ALMACENES: `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.ALMACENES}`,
  PEDIDOS: `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.PEDIDOS}`,
  MEJOR_INDIVIDUO: `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.MEJOR_INDIVIDUO}`,
  INICIAR_SIMULACION: `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.INICIAR_SIMULACION}`,
  REINICIAR_SIMULACION: `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.REINICIAR_SIMULACION}`,
  INFO_SIMULACION: `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.INFO_SIMULACION}`,
  ELIMINAR_PAQUETES_FUTUROS: `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.ELIMINAR_PAQUETES_FUTUROS}`
};

// Crear instancia de axios configurada
const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor para requests
api.interceptors.request.use(
  (config) => {
    console.log(`🌐 API Request: ${config.method?.toUpperCase()} ${config.url}`);
    return config;
  },
  (error) => {
    console.error('❌ API Request Error:', error);
    return Promise.reject(error);
  }
);

// Interceptor para responses
api.interceptors.response.use(
  (response) => {
    console.log(`✅ API Response: ${response.status} ${response.config.url}`);
    return response;
  },
  (error) => {
    console.error('❌ API Response Error:', error.response?.status, error.response?.data || error.message);
    return Promise.reject(error);
  }
);

// Exportar como default
export default api;
