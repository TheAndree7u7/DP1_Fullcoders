// components/MetricasRendimiento.tsx
import { useSimulacion } from '../context/SimulacionContext';
import checkIcon from '../assets/checkIcon.svg';
import gasolinaIcon from '../assets/gasolinaIcon.svg';

const MetricasRendimiento: React.FC = () => {
  const { camiones } = useSimulacion();
  const porcentajePromedio = (camiones.reduce((acc, c) => acc + c.porcentaje, 0) / camiones.length).toFixed(0);
  const capacidadTotalGLP = camiones.reduce((acc, c) => acc + c.capacidadActualGLP, 0).toFixed(1);

  return (
    <div className="flex flex-row gap-4 justify-center">
      <div className="flex flex-col items-center">
        <img src={checkIcon} alt="Check icon" className="w-[42px] h-[46px]" />
        {porcentajePromedio}%
        <span className="text-sm text-center">Cumplimiento</span>
      </div>

      <div className="flex flex-col items-center">
        <img src={gasolinaIcon} alt="Gasolina icon" className="w-[42px] h-[46px]" />
        {camiones.length * 40} m³
        <span className="text-sm text-center">Capacidad total</span>
      </div>

      <div className="flex flex-col items-center">
        {/* Icono de tanque/capacidad usando SVG */}
        <div className="w-[42px] h-[46px] flex items-center justify-center">
          <svg 
            width="38" 
            height="42" 
            viewBox="0 0 38 42" 
            fill="none" 
            xmlns="http://www.w3.org/2000/svg"
            className="text-blue-600"
          >
            {/* Tanque principal */}
            <rect 
              x="6" 
              y="8" 
              width="26" 
              height="28" 
              rx="4" 
              stroke="currentColor" 
              strokeWidth="2" 
              fill="rgba(59, 130, 246, 0.1)"
            />
            {/* Nivel de líquido */}
            <rect 
              x="8" 
              y="20" 
              width="22" 
              height="14" 
              rx="2" 
              fill="currentColor" 
              opacity="0.7"
            />
            {/* Válvula superior */}
            <rect 
              x="16" 
              y="4" 
              width="6" 
              height="6" 
              rx="1" 
              stroke="currentColor" 
              strokeWidth="2" 
              fill="none"
            />
            {/* Línea de conexión */}
            <line 
              x1="19" 
              y1="8" 
              x2="19" 
              y2="10" 
              stroke="currentColor" 
              strokeWidth="2"
            />
          </svg>
        </div>
        {capacidadTotalGLP} L
        <span className="text-sm text-center">Capacidad GLP</span>
      </div>
    </div>
  );
};

export default MetricasRendimiento;
