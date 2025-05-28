// components/MetricasRendimiento.tsx
import { useSimulacion } from '../context/SimulacionContext';
import checkIcon from '../assets/checkIcon.svg';
import gasolinaIcon from '../assets/gasolinaIcon.svg';

const MetricasRendimiento: React.FC = () => {
  const { camiones } = useSimulacion();
  const porcentajePromedio = (camiones.reduce((acc, c) => acc + c.porcentaje, 0) / camiones.length).toFixed(0);
  const entregados = camiones.filter(c => c.estado === 'Entregado').length * 40;

  return (
    <div className="flex flex-row gap-4 justify-center">
      <div className="flex flex-col items-center">
        <img src={checkIcon} alt="Check icon" className="w-[42px] h-[46px]" />
        {porcentajePromedio}%
        <span className="text-sm text-center">Cumplimiento</span>
      </div>

      <div className="flex flex-col items-center">
        <img src={gasolinaIcon} alt="Gasolina icon" className="w-[42px] h-[46px]" />
        {entregados} m³
        <span className="text-sm text-center">Galones entregados</span>
      </div>
    </div>
  );
};

export default MetricasRendimiento;
