import React, { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { useSimulacion } from "../context/SimulacionContext";
import { iniciarSimulacion } from "../services/simulacionApiService";
import { formatearFechaParaBackend } from "../context/simulacion/utils/tiempo";
import { useCurrentDateTime } from "../hooks/useCurrentDateTime";
import logo from "../assets/logo.png";

const CargaSimulacionDiaria: React.FC = () => {
  const navigate = useNavigate();
  const [paso, setPaso] = useState(1);
  const [mensaje, setMensaje] = useState("Iniciando simulación diaria...");
  const [error, setError] = useState<string | null>(null);
  const [fechaInicio, setFechaInicio] = useState<string>("");
  const hasInitialized = useRef(false);
  
  const { 
    setFechaInicioSimulacion,
    limpiarEstadoParaNuevaSimulacion,
    iniciarPollingPrimerPaquete
  } = useSimulacion();
  
  const currentDateTime = useCurrentDateTime();

  useEffect(() => {
    // Evitar múltiples ejecuciones
    if (hasInitialized.current) {
      return;
    }
    
    hasInitialized.current = true;

    const iniciarSimulacionDiaria = async () => {
      try {
        // Paso 1: Preparar fecha y hora actual
        setPaso(1);
        setMensaje("Obteniendo fecha y hora actual...");
        await new Promise(resolve => setTimeout(resolve, 1000));
        
        const fechaHoraActual = formatearFechaParaBackend(currentDateTime);
        setFechaInicio(fechaHoraActual);
        console.log("📅 FRONTEND: Fecha y hora actual para simulación diaria:", fechaHoraActual);
        
        // Paso 2: Guardar fecha en contexto global
        setPaso(2);
        setMensaje("Configurando parámetros de simulación...");
        await new Promise(resolve => setTimeout(resolve, 1000));
        
        setFechaInicioSimulacion(fechaHoraActual);
        
        // Paso 3: Iniciar simulación en backend
        setPaso(3);
        setMensaje("Iniciando simulación en el servidor...");
        await new Promise(resolve => setTimeout(resolve, 1000));
        
        console.log("🔄 FRONTEND: Configurando simulación en el backend...");
        await iniciarSimulacion(fechaHoraActual);
        
        // Paso 4: Limpiar estado y cargar datos
        setPaso(4);
        setMensaje("Cargando datos de simulación...");
        await new Promise(resolve => setTimeout(resolve, 1000));
        
        console.log("✅ FRONTEND: Simulación diaria iniciada en backend, limpiando estado...");
        await limpiarEstadoParaNuevaSimulacion();
        // console.log("🧹 FRONTEND: Estado limpiado y datos cargados para simulación diaria");
        
        // Paso 5: Iniciar polling
        setPaso(5);
        setMensaje("Configurando actualizaciones en tiempo real...");
        await new Promise(resolve => setTimeout(resolve, 1000));
        
        iniciarPollingPrimerPaquete();
        console.log("🔄 FRONTEND: Polling iniciado para simulación diaria en tiempo real");
        
        // Paso 6: Completado
        setPaso(6);
        setMensaje("¡Simulación diaria iniciada exitosamente!");
        await new Promise(resolve => setTimeout(resolve, 1500));
        
        console.log("✅ FRONTEND: Simulación diaria iniciada exitosamente");
        
        // Navegar a la vista principal
        navigate("/ejecucion-tiempo-real");
        
      } catch (error) {
        console.error("❌ FRONTEND: Error al iniciar simulación diaria:", error);
        setError(error instanceof Error ? error.message : "Error desconocido al iniciar la simulación");
      }
    };

    iniciarSimulacionDiaria();
  }, []); // Array de dependencias vacío para que solo se ejecute una vez

  const pasos = [
    "Obteniendo fecha y hora actual",
    "Configurando parámetros de simulación",
    "Iniciando simulación en el servidor",
    "Cargando datos de simulación",
    "Configurando actualizaciones en tiempo real",
    "¡Simulación diaria iniciada exitosamente!"
  ];

  if (error) {
    return (
      <div className="bg-gradient-to-br from-gray-50 to-gray-100 w-screen h-screen flex flex-col">
        {/* Header */}
        <div className="bg-white shadow-sm border-b border-gray-200">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="flex justify-between items-center py-4">
              <div className="flex items-center">
                <img src={logo} alt="Logo" className="h-8 w-auto mr-3" />
                <h1 className="text-2xl font-bold text-gray-900">Sistema de Simulación Logística</h1>
              </div>
            </div>
          </div>
        </div>

        {/* Contenido de error */}
        <div className="flex-1 flex flex-col justify-center items-center p-8">
          <div className="bg-white rounded-2xl shadow-lg p-8 max-w-md w-full text-center">
            <div className="text-red-500 text-6xl mb-4">❌</div>
            <h2 className="text-2xl font-bold text-gray-900 mb-4">Error al Iniciar Simulación</h2>
            <p className="text-gray-600 mb-6">{error}</p>
            
            <div className="space-y-3">
              <button
                onClick={() => window.location.reload()}
                className="w-full bg-blue-600 hover:bg-blue-700 text-white py-3 px-6 rounded-lg font-semibold transition-colors"
              >
                Reintentar
              </button>
              <button
                onClick={() => navigate("/")}
                className="w-full bg-gray-600 hover:bg-gray-700 text-white py-3 px-6 rounded-lg font-semibold transition-colors"
              >
                Volver al Inicio
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-gradient-to-br from-gray-50 to-gray-100 w-screen h-screen flex flex-col">
      {/* Header */}
      <div className="bg-white shadow-sm border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center py-4">
            <div className="flex items-center">
              <img src={logo} alt="Logo" className="h-8 w-auto mr-3" />
              <h1 className="text-2xl font-bold text-gray-900">Sistema de Simulación Logística</h1>
            </div>
          </div>
        </div>
      </div>

      {/* Contenido principal */}
      <div className="flex-1 flex flex-col justify-center items-center p-8">
        <div className="bg-white rounded-2xl shadow-lg p-8 max-w-md w-full text-center">
          {/* Logo y título */}
          <div className="text-blue-500 text-6xl mb-4">⚡</div>
          <h2 className="text-2xl font-bold text-gray-900 mb-2">Iniciando Simulación Diaria</h2>
          <p className="text-gray-600 mb-6">Configurando ejecución en tiempo real...</p>
          
          {/* Fecha y hora actual */}
          {fechaInicio && (
            <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-6">
              <p className="text-sm text-blue-800 font-medium">Fecha y hora de inicio:</p>
              <p className="text-lg text-blue-900 font-bold">{fechaInicio.replace('T', ' ')}</p>
            </div>
          )}
          
          {/* Barra de progreso */}
          <div className="mb-6">
            <div className="flex justify-between text-sm text-gray-600 mb-2">
              <span>Progreso</span>
              <span>{paso}/6</span>
            </div>
            <div className="w-full bg-gray-200 rounded-full h-2">
              <div 
                className="bg-blue-600 h-2 rounded-full transition-all duration-500"
                style={{ width: `${(paso / 6) * 100}%` }}
              ></div>
            </div>
          </div>
          
          {/* Mensaje actual */}
          <div className="bg-gray-50 border border-gray-200 rounded-lg p-4 mb-6">
            <div className="flex items-center justify-center">
              <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-blue-600 mr-3"></div>
              <span className="text-gray-700 font-medium">{mensaje}</span>
            </div>
          </div>
          
          {/* Lista de pasos */}
          <div className="text-left">
            <h3 className="text-sm font-semibold text-gray-700 mb-3">Pasos de configuración:</h3>
            <div className="space-y-2">
              {pasos.map((pasoTexto, index) => (
                <div key={index} className="flex items-center text-sm">
                  <div className={`w-4 h-4 rounded-full mr-3 flex items-center justify-center ${
                    index + 1 < paso 
                      ? 'bg-green-500 text-white' 
                      : index + 1 === paso 
                        ? 'bg-blue-500 text-white animate-pulse' 
                        : 'bg-gray-300 text-gray-600'
                  }`}>
                    {index + 1 < paso ? '✓' : index + 1 === paso ? '●' : index + 1}
                  </div>
                  <span className={index + 1 <= paso ? 'text-gray-900' : 'text-gray-500'}>
                    {pasoTexto}
                  </span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Footer */}
      <div className="bg-white border-t border-gray-200 py-4">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center text-sm text-gray-500">
            © 2025 Sistema de Simulación Logística - Desarrollado para DP1 Fullcoders
          </div>
        </div>
      </div>
    </div>
  );
};

export default CargaSimulacionDiaria; 