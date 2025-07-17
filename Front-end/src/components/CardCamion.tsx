// components/CardCamion.tsx
import { useSimulacion } from '../context/SimulacionContext';
import Camion from '../assets/camion.svg';
import { getCamionColorByState } from '../config/colors';
import { useEffect } from 'react';

const CardsCamiones: React.FC = () => {
  const { camiones, rutasCamiones } = useSimulacion();
  
  // Validación de datos de camiones
  useEffect(() => {
    try {
      console.log('🔍 VALIDACIÓN: Verificando consistencia de datos de camiones...');
      
      if (!camiones || !Array.isArray(camiones)) {
        console.error('❌ ERROR: camiones no es un array válido:', camiones);
        return;
      }
      
      if (!rutasCamiones || !Array.isArray(rutasCamiones)) {
        console.error('❌ ERROR: rutasCamiones no es un array válido:', rutasCamiones);
        return;
      }
      
      // Verificar cada camión
      camiones.forEach((camion, index) => {
        try {
          // Validar propiedades críticas
          if (!camion.id) {
            console.error(`❌ ERROR: Camión ${index} no tiene ID:`, camion);
          }
          
          if (typeof camion.capacidadActualGLP !== 'number' || isNaN(camion.capacidadActualGLP)) {
            console.error(`❌ ERROR: Camión ${camion.id} tiene capacidadActualGLP inválida:`, camion.capacidadActualGLP);
          }
          
          if (typeof camion.capacidadMaximaGLP !== 'number' || isNaN(camion.capacidadMaximaGLP)) {
            console.error(`❌ ERROR: Camión ${camion.id} tiene capacidadMaximaGLP inválida:`, camion.capacidadMaximaGLP);
          }
          
          if (typeof camion.combustibleActual !== 'number' || isNaN(camion.combustibleActual)) {
            console.error(`❌ ERROR: Camión ${camion.id} tiene combustibleActual inválido:`, camion.combustibleActual);
          }
          
          if (typeof camion.combustibleMaximo !== 'number' || isNaN(camion.combustibleMaximo)) {
            console.error(`❌ ERROR: Camión ${camion.id} tiene combustibleMaximo inválido:`, camion.combustibleMaximo);
          }
          
          if (typeof camion.porcentaje !== 'number' || isNaN(camion.porcentaje)) {
            console.error(`❌ ERROR: Camión ${camion.id} tiene porcentaje inválido:`, camion.porcentaje);
          }
          
          if (!camion.estado) {
            console.error(`❌ ERROR: Camión ${camion.id} no tiene estado:`, camion);
          }
          
          // Verificar consistencia entre camiones y rutas
          const rutaCorrespondiente = rutasCamiones.find(ruta => ruta.id === camion.id);
          if (!rutaCorrespondiente) {
            console.warn(`⚠️ ADVERTENCIA: Camión ${camion.id} no tiene ruta correspondiente`);
          }
          
        } catch (error) {
          console.error(`❌ ERROR al validar camión ${index}:`, error, camion);
        }
      });
      
      // Verificar rutas sin camiones correspondientes
      rutasCamiones.forEach((ruta, index) => {
        try {
          const camionCorrespondiente = camiones.find(camion => camion.id === ruta.id);
          if (!camionCorrespondiente) {
            console.warn(`⚠️ ADVERTENCIA: Ruta ${ruta.id} no tiene camión correspondiente`);
          }
          
          if (!ruta.pedidos || !Array.isArray(ruta.pedidos)) {
            console.error(`❌ ERROR: Ruta ${ruta.id} no tiene pedidos válidos:`, ruta.pedidos);
          }
        } catch (error) {
          console.error(`❌ ERROR al validar ruta ${index}:`, error, ruta);
        }
      });
      
      console.log(`✅ VALIDACIÓN COMPLETADA: ${camiones.length} camiones, ${rutasCamiones.length} rutas`);
      
    } catch (error) {
      console.error('❌ ERROR CRÍTICO en validación de camiones:', error);
    }
  }, [camiones, rutasCamiones]);
  
  // Validación antes del renderizado
  if (!camiones || !Array.isArray(camiones)) {
    console.error('❌ ERROR: No se pueden renderizar camiones - datos inválidos:', camiones);
    return (
      <div className="max-h-96 overflow-y-auto pr-1 -mr-1 space-y-2">
        <div className="text-red-600 text-center p-4">
          Error: Datos de camiones no disponibles
        </div>
      </div>
    );
  }
  
  return (
    <div className="max-h-96 overflow-y-auto pr-1 -mr-1 space-y-2">
      {camiones.map((camion, index) => {
        try {
          const indexRuta = rutasCamiones.findIndex(ruta => ruta.id === camion.id);
          const camionColor = getCamionColorByState(camion.estado, indexRuta);
          
          // Validación adicional antes de renderizar cada camión
          if (!camion.id) {
            console.error(`❌ ERROR: Intentando renderizar camión sin ID en índice ${index}:`, camion);
            return null;
          }
          
          if (typeof camion.porcentaje !== 'number' || isNaN(camion.porcentaje)) {
            console.error(`❌ ERROR: Camión ${camion.id} tiene porcentaje inválido para renderizar:`, camion.porcentaje);
            return null;
          }
          
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
                  {camion.estado || 'Desconocido'}
                </span>
              </div>
              <div className="flex items-center gap-2 px-4 pb-4">
                <div className="w-full h-3 bg-gray-200 rounded-full">
                  <div
                    className="h-3 rounded-full transition-all duration-300"
                    style={{ 
                      width: `${Math.max(0, Math.min(100, camion.porcentaje))}%`,
                      backgroundColor: camionColor
                    }}
                  ></div>
                </div>
                <span className="text-sm font-medium text-gray-700 w-10 text-right">
                  {Math.max(0, Math.min(100, camion.porcentaje)).toFixed(0)}%
                </span>
              </div>
            </div>
          );
        } catch (error) {
          console.error(`❌ ERROR al renderizar camión ${index}:`, error, camion);
          return (
            <div key={`error-${index}`} className="flex flex-col w-full rounded-2xl bg-red-50 border border-red-200 p-4">
              <div className="text-red-600 text-sm">
                Error al renderizar camión: {error instanceof Error ? error.message : 'Error desconocido'}
              </div>
            </div>
          );
        }
      })}
      {camiones.length > 4 && (
        <div className="text-center pt-1 text-xs text-gray-500">
          {camiones.length} camiones en total
        </div>
      )}
    </div>
  );
};

export default CardsCamiones;
