import React from 'react';
import { useSimulacion } from '../context/SimulacionContext';
import { esValorValido } from '../utils/validacionCamiones';
import { colorSemaforoGLP } from './mapa/utils';

const IndicadorGLPTotal: React.FC = () => {
  const { camiones } = useSimulacion();

  // Calcular el GLP actual total de todos los camiones
  const glpActualTotal = React.useMemo(() => {
    return camiones.reduce((total, camion) => {
      // Solo sumar si el valor es válido
      if (esValorValido(camion.capacidadActualGLP)) {
        return total + camion.capacidadActualGLP;
      }
      return total;
    }, 0);
  }, [camiones]);

  // Calcular la capacidad máxima total de todos los camiones
  const glpMaximoTotal = React.useMemo(() => {
    return camiones.reduce((total, camion) => {
      // Solo sumar si el valor es válido
      if (esValorValido(camion.capacidadMaximaGLP)) {
        return total + camion.capacidadMaximaGLP;
      }
      return total;
    }, 0);
  }, [camiones]);

  // Calcular el porcentaje de capacidad utilizada
  const porcentajeCapacidad = React.useMemo(() => {
    if (glpMaximoTotal === 0) return 0;
    return (glpActualTotal / glpMaximoTotal) * 100;
  }, [glpActualTotal, glpMaximoTotal]);

  // Obtener el color semáforo según el porcentaje
  const colorCapacidad = colorSemaforoGLP(porcentajeCapacidad);

  return (
    <>
      <span className="mr-2">Capacidad de la flota:</span>
      <span className="font-bold" style={{ color: colorCapacidad }}>
        {porcentajeCapacidad.toFixed(1)}%
      </span>
    </>
  );
};

export default IndicadorGLPTotal;