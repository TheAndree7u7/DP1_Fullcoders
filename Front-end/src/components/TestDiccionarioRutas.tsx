import React from 'react';
import { useSimulacion } from '../context/SimulacionContext';

const TestDiccionarioRutas: React.FC = () => {
  const { 
    diccionarioRutasCamiones, 
    camiones,
    verificarCamionEnNodoAveria,
    obtenerNodosAveriaEnRuta 
  } = useSimulacion();

  // Función para mostrar información del diccionario
  const mostrarInfoDiccionario = () => {
    const totalCamiones = Object.keys(diccionarioRutasCamiones).length;
    const camionesConAverias = Object.keys(diccionarioRutasCamiones).filter(idCamion => {
      const nodosAveria = obtenerNodosAveriaEnRuta(idCamion);
      return nodosAveria.length > 0;
    });

    return {
      totalCamiones,
      camionesConAverias: camionesConAverias.length,
      camionesEnAveriaActual: camiones.filter(camion => 
        verificarCamionEnNodoAveria(camion.id, camion.porcentaje)
      ).length
    };
  };

  const info = mostrarInfoDiccionario();

  return (
    <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-4">
      <h3 className="text-lg font-semibold mb-3 text-blue-800">
        🧪 Test Diccionario de Rutas
      </h3>
      
      <div className="space-y-2 text-sm">
        <p><strong>Total de camiones con rutas:</strong> {info.totalCamiones}</p>
        <p><strong>Camiones con nodos de avería en ruta:</strong> {info.camionesConAverias}</p>
        <p><strong>Camiones actualmente en nodo de avería:</strong> {info.camionesEnAveriaActual}</p>
      </div>

      {/* Mostrar detalles de cada camión */}
      <div className="mt-4">
        <h4 className="text-md font-medium mb-2 text-blue-700">
          Detalles por Camión:
        </h4>
        <div className="space-y-2 max-h-40 overflow-y-auto">
          {Object.entries(diccionarioRutasCamiones).map(([idCamion, rutaCompleta]) => {
            const camion = camiones.find(c => c.id === idCamion);
            const porcentajeAvance = camion?.porcentaje || 0;
            const estaEnNodoAveria = verificarCamionEnNodoAveria(idCamion, porcentajeAvance);
            const nodosAveria = obtenerNodosAveriaEnRuta(idCamion);
            
            return (
              <div key={idCamion} className="border border-blue-200 rounded p-2 text-xs">
                <div className="flex justify-between items-center mb-1">
                  <span className="font-medium">Camión {idCamion}</span>
                  <span className={`px-2 py-1 rounded text-xs ${
                    estaEnNodoAveria 
                      ? 'bg-red-100 text-red-800' 
                      : 'bg-green-100 text-green-800'
                  }`}>
                    {estaEnNodoAveria ? '🚨 En avería' : '✅ Normal'}
                  </span>
                </div>
                <div className="text-gray-600">
                  <p>Nodos: {rutaCompleta.ruta.length} | Avance: {Math.round(porcentajeAvance * 100)}%</p>
                  <p>Nodos de avería: {nodosAveria.length}</p>
                  {nodosAveria.length > 0 && (
                    <p className="text-xs text-red-600">
                      Tipos: {nodosAveria.map(n => n.tipo).join(', ')}
                    </p>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Mostrar estructura del diccionario */}
      <div className="mt-4">
        <h4 className="text-md font-medium mb-2 text-blue-700">
          Estructura del Diccionario:
        </h4>
        <pre className="text-xs bg-gray-100 p-2 rounded overflow-x-auto">
          {JSON.stringify(diccionarioRutasCamiones, null, 2)}
        </pre>
      </div>
    </div>
  );
};

export default TestDiccionarioRutas; 