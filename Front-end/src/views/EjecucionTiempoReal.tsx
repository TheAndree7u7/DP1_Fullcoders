import React, { useState } from "react";
import Mapa from "../components/Mapa";
import Navbar from "../components/Navbar";
import { PlayCircle, PauseCircle, RefreshCw, Clock, MapPin } from "lucide-react";

const EjecucionTiempoReal: React.FC = () => {
  const [ejecutando, setEjecutando] = useState(false);
  const [tiempoReal, setTiempoReal] = useState(new Date());

  const toggleEjecucion = () => {
    setEjecutando(!ejecutando);
  };

  const reiniciarEjecucion = () => {
    setEjecutando(false);
    setTiempoReal(new Date());
  };

  return (
    <div className="bg-[#F5F5F5] w-screen h-screen flex flex-col pt-16">
      <Navbar />
      
      {/* Header de la vista */}
      <div className="bg-[#1E293B] text-white py-2 px-4 flex justify-between items-center">
        <h1 className="font-bold">Ejecución en Tiempo Real</h1>
        <div className="flex items-center gap-4">
          <div className="flex items-center gap-2">
            <Clock size={16} />
            <span className="text-sm">
              {tiempoReal.toLocaleTimeString('es-ES')}
            </span>
          </div>
          <div className={`flex items-center gap-2 px-3 py-1 rounded-full text-sm ${
            ejecutando ? 'bg-green-600' : 'bg-gray-600'
          }`}>
            <div className={`w-2 h-2 rounded-full ${ejecutando ? 'bg-green-300 animate-pulse' : 'bg-gray-300'}`}></div>
            <span>{ejecutando ? 'Ejecutándose' : 'Pausado'}</span>
          </div>
        </div>
      </div>

      {/* Panel de control */}
      <div className="bg-white px-4 py-3 border-b border-gray-200">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <button
              onClick={toggleEjecucion}
              className={`flex items-center gap-2 px-4 py-2 rounded-lg transition-colors font-medium ${
                ejecutando 
                  ? 'bg-orange-600 hover:bg-orange-700 text-white' 
                  : 'bg-green-600 hover:bg-green-700 text-white'
              }`}
            >
              {ejecutando ? (
                <>
                  <PauseCircle size={18} />
                  Pausar
                </>
              ) : (
                <>
                  <PlayCircle size={18} />
                  Iniciar
                </>
              )}
            </button>
            
            <button
              onClick={reiniciarEjecucion}
              className="flex items-center gap-2 bg-gray-600 hover:bg-gray-700 text-white px-4 py-2 rounded-lg transition-colors font-medium"
            >
              <RefreshCw size={18} />
              Reiniciar
            </button>
          </div>

          <div className="flex items-center gap-4 text-sm text-gray-600">
            <div className="flex items-center gap-2">
              <MapPin size={16} />
              <span>Vista en tiempo real</span>
            </div>
          </div>
        </div>
      </div>

      {/* Contenido principal - Mapa */}
      <div className="flex-1 p-4">
        <div className="bg-white rounded-xl shadow-lg h-full p-4">
          <div className="h-full">
            <Mapa />
          </div>
        </div>
      </div>

      {/* Footer con información */}
      <div className="bg-white border-t border-gray-200 px-4 py-2">
        <div className="flex items-center justify-between text-sm text-gray-600">
          <div className="flex items-center gap-4">
            <span>🚛 Camiones activos: --</span>
            <span>📦 Pedidos pendientes: --</span>
            <span>🏭 Almacenes: --</span>
          </div>
          <div className="text-xs">
            Actualización automática cada 30 segundos
          </div>
        </div>
      </div>
    </div>
  );
};

export default EjecucionTiempoReal;