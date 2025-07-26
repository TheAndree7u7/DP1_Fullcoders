import React from 'react';
import { useSimulacion } from '../context/SimulacionContext';
import { colorSemaforoGLP } from './mapa/utils';

const IndicadorFlotaFuncional: React.FC = () => {
  const { camiones } = useSimulacion();

  // Eliminar duplicados basándose en el ID como lo hace DatosCamionesTable
  const camionesUnicos = React.useMemo(() => {
    return camiones.filter((camion, index, array) => 
      array.findIndex(c => c.id === camion.id) === index
    );
  }, [camiones]);

  // Calcular camiones funcionales (excluir estados de avería)
  const { camionesTotales, camionesFuncionales } = React.useMemo(() => {
    const totales = camionesUnicos.length;
    
    // Estados que indican camión no funcional (averiado)
    const estadosNoFuncionales = [
      'Averiado', // INMOVILIZADO_POR_AVERIA transformado
      'En Mantenimiento por Avería' // EN_MANTENIMIENTO_POR_AVERIA transformado
    ];
    
    const funcionales = camionesUnicos.filter(camion => 
      !estadosNoFuncionales.includes(camion.estado)
    ).length;
    
    return {
      camionesTotales: totales,
      camionesFuncionales: funcionales
    };
  }, [camionesUnicos]);

  // Calcular el porcentaje de flota funcional
  const porcentajeFlotaFuncional = React.useMemo(() => {
    if (camionesTotales === 0) return 0;
    return (camionesFuncionales / camionesTotales) * 100;
  }, [camionesFuncionales, camionesTotales]);

  // Obtener el color semáforo según el porcentaje
  const colorFlotaFuncional = colorSemaforoGLP(porcentajeFlotaFuncional);

  return (
    <>
      <span className="mr-2">Flota funcional:</span>
      <span className="font-bold" style={{ color: colorFlotaFuncional }}>
        {porcentajeFlotaFuncional.toFixed(1)}%
      </span>
    </>
  );
};

export default IndicadorFlotaFuncional;