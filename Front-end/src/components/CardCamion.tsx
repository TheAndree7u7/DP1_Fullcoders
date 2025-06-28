// components/CardCamion.tsx
import { useSimulacion } from '../context/SimulacionContext';
import Camion from '../assets/camion.svg';
import { getCamionColorByState } from '../config/colors';
import React from 'react';

const CardsCamiones: React.FC = () => {
  const { camiones, rutasCamiones } = useSimulacion();
  
  return (
    <div className="max-h-96 overflow-y-auto pr-1 -mr-1 space-y-2">
      {camiones.map((camion) => {
        const index = rutasCamiones.findIndex(ruta => ruta.id === camion.id);
        const camionColor = getCamionColorByState(camion.estado, index);
        
        return (
          <div key={camion.id} className="flex flex-col w-full rounded-2xl bg-[#FAFAFA]">
            <div className="inline-flex justify-start items-center pt-2 px-4">
              <div 
                className="w-6 h-6 flex items-center justify-center rounded-md"
                style={{ backgroundColor: camionColor }}
              >
                <img src={Camion} alt="Camion" className="w-4 h-4 filter brightness-0 invert" />
              </div>
              <span className="font-medium ml-2">{camion.id}</span>
              <span 
                className={`ml-auto text-xs font-medium px-2 py-0.5 rounded-full 
                  ${camion.estado === 'Entregado' ? 'bg-green-100 text-green-800' : 
                    camion.estado === 'Averiado' ? 'bg-red-100 text-red-800' : 
                    camion.estado === 'Disponible' ? 'bg-blue-100 text-blue-800' : 
                    camion.estado === 'En Mantenimiento' ? 'bg-yellow-100 text-yellow-800' : 
                    'bg-blue-100 text-blue-800'}`}
              >
                {camion.estado}
              </span>
            </div>
            <div className="flex items-center gap-2 px-4 pb-4">
              <div className="w-full h-3 bg-gray-200 rounded-full">
                <div
                  className="h-3 rounded-full transition-all duration-300"
                  style={{ 
                    width: `${camion.porcentaje}%`,
                    backgroundColor: camionColor
                  }}
                ></div>
              </div>
              <span className="text-sm font-medium text-gray-700 w-10 text-right">{camion.porcentaje}%</span>
            </div>
          </div>
        );
      })}
      {camiones.length > 4 && (
        <div className="text-center pt-1 text-xs text-gray-500">
          {camiones.length} camiones en total
        </div>
      )}
    </div>
  );
};

export default React.memo(CardsCamiones);
