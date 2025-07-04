import React from 'react';
import { ChevronRight } from 'lucide-react';
import { useSimulacion } from '../context/SimulacionContext';
import MetricasRendimiento from './MetricasRendimiento';
import CardsCamiones from './CardCamion';
import IndicadoresCamiones from './IndicadoresCamiones';
import BloqueosTable from './BloqueosTable';
import TablaPedidos from './TablaPedidos';

// Tabla simple de datos de camiones usando el contexto
function DatosCamionesTable() {
  const { camiones } = useSimulacion();
  return (
    <div className="flex flex-col flex-1 min-h-0">
      <div className="text-lg font-bold text-black mb-3 flex items-center gap-2">
        Datos de los camiones
      </div>
      <div className="flex-1 min-h-0 overflow-y-auto rounded-lg shadow border border-gray-200 bg-white">
        <table className="min-w-full table-auto text-sm bg-white">
          <thead>
            <tr className="border-b border-gray-200">
              <th className="px-4 py-2 text-left font-semibold text-black">ID</th>
              <th className="px-4 py-2 text-left font-semibold text-black">GLP Actual</th>
              <th className="px-4 py-2 text-left font-semibold text-black">Ubicación</th>
              <th className="px-4 py-2 text-left font-semibold text-black">Estado</th>
            </tr>
          </thead>
          <tbody>
            {camiones.map((camion) => (
              <tr key={camion.id} className={
                `border-b last:border-b-0 bg-white hover:bg-gray-100 transition-colors`
              }>
                <td className="px-4 py-2 text-gray-800 font-mono font-semibold">{camion.id}</td>
                <td className="px-4 py-2 text-blue-700 font-bold">{camion.capacidadActualGLP}</td>
                <td className="px-4 py-2 text-gray-600">{camion.ubicacion}</td>
                <td className={
                  `px-4 py-2 font-semibold ` +
                  (camion.estado === 'Averiado' ? 'text-red-600' :
                  camion.estado === 'En Mantenimiento' ? 'text-yellow-600' :
                  camion.estado === 'Entregado' ? 'text-emerald-600' :
                  camion.estado === 'Disponible' ? 'text-blue-600' :
                  'text-gray-700')
                }>
                  {camion.estado}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

interface RightMenuProps {
  expanded: boolean;
  setExpanded: (value: boolean) => void;
}

const RightMenu: React.FC<RightMenuProps> = ({ expanded, setExpanded }) => {
  const [panel, setPanel] = React.useState<'camiones' | 'bloqueos' | 'metricas' | 'estadoCamiones' | 'pedidos'>('camiones');
  // Estados para expandir/contraer secciones en panel camiones
  // Solo una sección expandida a la vez (acordeón)
  // const [expandedSection, setExpandedSection] = React.useState<'estado' | 'datos'>('estado');
  if (!expanded) return null;

  return (
    <div className="transition-all duration-300 bg-white rounded-xl p-4 shadow-md h-full flex flex-col">
      <button
        onClick={() => setExpanded(false)}
        className="self-end mb-2"
        title="Ocultar menú"
      >
        <ChevronRight size={12} />
      </button>

      {/* Botones de alternancia de panel */}
      <div className="flex gap-2 mb-4 flex-wrap">
        <button
          className={`px-3 py-1 rounded font-semibold transition ${panel === 'camiones' ? 'bg-blue-500 text-white' : 'bg-gray-200 text-gray-800 hover:bg-blue-100'}`}
          onClick={() => setPanel('camiones')}
        >
          Camiones
        </button>
        <button
          className={`px-3 py-1 rounded font-semibold transition ${panel === 'bloqueos' ? 'bg-blue-500 text-white' : 'bg-gray-200 text-gray-800 hover:bg-blue-100'}`}
          onClick={() => setPanel('bloqueos')}
        >
          Bloqueos
        </button>
        <button
          className={`px-3 py-1 rounded font-semibold transition ${panel === 'metricas' ? 'bg-blue-500 text-white' : 'bg-gray-200 text-gray-800 hover:bg-blue-100'}`}
          onClick={() => setPanel('metricas')}
        >
          Métricas
        </button>
        <button
          className={`px-3 py-1 rounded font-semibold transition ${panel === 'estadoCamiones' ? 'bg-blue-500 text-white' : 'bg-gray-200 text-gray-800 hover:bg-blue-100'}`}
          onClick={() => setPanel('estadoCamiones')}
        >
          Estado de los camiones
        </button>
        <button
          className={`px-3 py-1 rounded font-semibold transition ${panel === 'pedidos' ? 'bg-blue-500 text-white' : 'bg-gray-200 text-gray-800 hover:bg-blue-100'}`}
          onClick={() => setPanel('pedidos')}
        >
          Pedidos
        </button>
      </div>

      <div className="flex flex-col flex-1 min-h-0">
        {panel === 'camiones' && (
          <DatosCamionesTable />
        )}
        {panel === 'estadoCamiones' && (
          <div className="flex flex-col flex-1 min-h-0">
            <div className="text-md font-bold text-gray-700 mb-2">Estado de los camiones</div>
            <CardsCamiones />
          </div>
        )}
        {panel === 'bloqueos' && (
          <>
            <div className="text-md font-bold text-gray-700">Bloqueos activos</div>
            <BloqueosTable />
          </>
        )}
        {panel === 'metricas' && (
          <>
            <div className="text-md font-bold text-gray-700">Métricas de rendimiento</div>
            <MetricasRendimiento />
            <div className="text-xs font-bold text-gray-700">Indicadores de camiones</div>
            <IndicadoresCamiones />
          </>
        )}
        {panel === 'pedidos' && (
          <TablaPedidos />
        )}
      </div>
    </div>
  );
};

export default RightMenu;
