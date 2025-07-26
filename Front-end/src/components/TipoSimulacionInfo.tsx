import React, { useState, useEffect } from 'react';
import { obtenerTipoSimulacionActual, type TipoSimulacionActualResponse } from '../services/simulacionApiService';
import { useSimulacion } from "../context/SimulacionContext";

interface TipoSimulacionInfoProps {
  className?: string;
}

const TipoSimulacionInfo: React.FC<TipoSimulacionInfoProps> = ({ className = '' }) => {
  const [tipoActual, setTipoActual] = useState<TipoSimulacionActualResponse | null>(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const { 
    tipoSimulacion, 
    diccionarioRutasCamiones, 
    camiones,
    verificarCamionEnNodoAveria,
    obtenerNodosAveriaEnRuta 
  } = useSimulacion();

  useEffect(() => {
    cargarTipoSimulacion();
  }, []);

  const cargarTipoSimulacion = async () => {
    try {
      setCargando(true);
      setError(null);
      const data = await obtenerTipoSimulacionActual();
      setTipoActual(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error desconocido');
    } finally {
      setCargando(false);
    }
  };

  const obtenerIconoTipo = (tipo: string) => {
    switch (tipo) {
      case 'DIARIA':
        return '⚡';
      case 'SEMANAL':
        return '📊';
      case 'COLAPSO':
        return '🚨';
      default:
        return '❓';
    }
  };

  const obtenerColorTipo = (tipo: string) => {
    switch (tipo) {
      case 'DIARIA':
        return 'text-blue-600 bg-blue-100 border-blue-200';
      case 'SEMANAL':
        return 'text-green-600 bg-green-100 border-green-200';
      case 'COLAPSO':
        return 'text-red-600 bg-red-100 border-red-200';
      default:
        return 'text-gray-600 bg-gray-100 border-gray-200';
    }
  };

  // Función para obtener información de rutas de camiones
  const obtenerInfoRutasCamiones = () => {
    const info = Object.entries(diccionarioRutasCamiones).map(([idCamion, rutaCompleta]) => {
      const camion = camiones.find(c => c.id === idCamion);
      const porcentajeAvance = camion?.porcentaje || 0;
      const estaEnNodoAveria = verificarCamionEnNodoAveria(idCamion, porcentajeAvance);
      const nodosAveria = obtenerNodosAveriaEnRuta(idCamion);
      
      return {
        idCamion,
        totalNodos: rutaCompleta.ruta.length,
        porcentajeAvance,
        estaEnNodoAveria,
        nodosAveria: nodosAveria.length,
        tiposNodos: rutaCompleta.ruta.map(nodo => nodo.tipo),
        coordenadas: rutaCompleta.ruta.map(nodo => `(${nodo.coordenada.x},${nodo.coordenada.y})`)
      };
    });

    return info;
  };

  const infoRutas = obtenerInfoRutasCamiones();

  if (cargando) {
    return (
      <div className={`bg-white rounded-lg shadow-md p-4 ${className}`}>
        <div className="flex items-center justify-center">
          <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-blue-600 mr-2"></div>
          <span className="text-gray-600">Cargando tipo de simulación...</span>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className={`bg-white rounded-lg shadow-md p-4 ${className}`}>
        <div className="flex items-center justify-center text-red-600">
          <span className="mr-2">❌</span>
          <span>Error: {error}</span>
        </div>
      </div>
    );
  }

  if (!tipoActual) {
    return (
      <div className={`bg-white rounded-lg shadow-md p-4 ${className}`}>
        <div className="text-center text-gray-500">
          No hay información disponible
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow-md p-4 mb-4">
      <h3 className="text-lg font-semibold mb-3 text-gray-800">
        Información de Tipo de Simulación
      </h3>
      
      <div className="mb-4">
        <p className="text-sm text-gray-600 mb-2">
          <strong>Tipo actual:</strong> {tipoSimulacion}
        </p>
        <p className="text-sm text-gray-600">
          <strong>Total de camiones con rutas:</strong> {Object.keys(diccionarioRutasCamiones).length}
        </p>
      </div>

      {/* Nueva sección: Información de Rutas de Camiones */}
      <div className="mt-4">
        <h4 className="text-md font-medium mb-2 text-gray-700">
          📍 Información de Rutas de Camiones
        </h4>
        <div className="space-y-2 max-h-60 overflow-y-auto">
          {infoRutas.map((info) => (
            <div key={info.idCamion} className="border border-gray-200 rounded p-2 text-xs">
              <div className="flex justify-between items-center mb-1">
                <span className="font-medium">Camión {info.idCamion}</span>
                <span className={`px-2 py-1 rounded text-xs ${
                  info.estaEnNodoAveria 
                    ? 'bg-red-100 text-red-800' 
                    : 'bg-green-100 text-green-800'
                }`}>
                  {info.estaEnNodoAveria ? '🚨 En nodo avería' : '✅ Normal'}
                </span>
              </div>
              <div className="text-gray-600">
                <p>Nodos: {info.totalNodos} | Avance: {Math.round(info.porcentajeAvance * 100)}%</p>
                <p>Nodos de avería en ruta: {info.nodosAveria}</p>
                <p className="text-xs text-gray-500 mt-1">
                  Tipos: {info.tiposNodos.slice(0, 5).join(', ')}
                  {info.tiposNodos.length > 5 && '...'}
                </p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default TipoSimulacionInfo; 