import React from 'react';
import { useSimulacion } from '../../context/SimulacionContext';

const coloresAveria = [
  'bg-yellow-400 hover:bg-yellow-500',
  'bg-orange-500 hover:bg-orange-600',
  'bg-red-500 hover:bg-red-600',
];

const nombresAveria = [
  'Avería tipo 1',
  'Avería tipo 2',
  'Avería tipo 3',
];

interface AveriarCamionesProps {
  onElementoSeleccionado: (elemento: {tipo: 'camion', id: string} | null) => void;
}

const AveriarCamiones: React.FC<AveriarCamionesProps> = ({ onElementoSeleccionado }) => {
  const { camiones, marcarCamionAveriado } = useSimulacion();
  const [busqueda, setBusqueda] = React.useState('');
  const [camionSeleccionado, setCamionSeleccionado] = React.useState<string | null>(null);

  // Filtrar camiones por búsqueda (ID o estado)
  const camionesFiltrados = React.useMemo(() => {
    if (!busqueda.trim()) return camiones;
    const termino = busqueda.toLowerCase();
    return camiones.filter(c =>
      c.id.toLowerCase().includes(termino) ||
      (c.estado && c.estado.toLowerCase().includes(termino))
    );
  }, [camiones, busqueda]);

  // Al hacer click en la fila, resalta y selecciona el camión en el mapa
  const handleSeleccionarCamion = (camionId: string) => {
    setCamionSeleccionado(camionId);
    onElementoSeleccionado({ tipo: 'camion', id: camionId });
  };

  return (
    <div className="flex flex-col flex-1 min-h-0">
      <div className="text-lg font-bold text-black mb-3 flex items-center gap-2">
        Averiar Camiones
      </div>
      {/* Panel de búsqueda */}
      <div className="mb-3">
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Buscar camiones:
        </label>
        <input
          type="text"
          placeholder="Buscar por ID o estado..."
          value={busqueda}
          onChange={e => setBusqueda(e.target.value)}
          className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
        />
      </div>
      <div className="flex-1 min-h-0 overflow-y-auto rounded-lg shadow border border-gray-200 bg-white">
        <table className="min-w-full table-auto text-sm bg-white">
          <thead>
            <tr className="border-b border-gray-200">
              <th className="px-4 py-2 text-left font-semibold text-black">ID</th>
              <th className="px-4 py-2 text-left font-semibold text-black">Estado</th>
              <th className="px-4 py-2 text-left font-semibold text-black">Acciones</th>
            </tr>
          </thead>
          <tbody>
            {camionesFiltrados.length === 0 ? (
              <tr>
                <td colSpan={3} className="px-4 py-8 text-center text-gray-500">
                  No hay camiones disponibles
                </td>
              </tr>
            ) : (
              camionesFiltrados.map((camion) => (
                <tr
                  key={camion.id}
                  className={`border-b last:border-b-0 bg-white hover:bg-blue-50 hover:cursor-pointer transition-colors ${camionSeleccionado === camion.id ? 'bg-blue-100' : ''}`}
                  onClick={() => handleSeleccionarCamion(camion.id)}
                  title="Clic para resaltar camión"
                >
                  <td className="px-4 py-2 text-gray-800 font-mono font-semibold">{camion.id}</td>
                  <td className="px-4 py-2 font-semibold">{camion.estado}</td>
                  <td className="px-4 py-2">
                    {camion.estado === 'En Ruta' ? (
                      <div className="flex flex-row gap-2 flex-wrap">
                        {[0, 1, 2].map((tipo) => (
                          <button
                            key={tipo}
                            className={`text-white font-semibold rounded px-2 py-2 transition ${coloresAveria[tipo]}`}
                            onClick={e => { e.stopPropagation(); marcarCamionAveriado(camion.id); }}
                          >
                            {nombresAveria[tipo]}
                          </button>
                        ))}
                      </div>
                    ) : (
                      <span className="text-gray-400 italic">No disponible</span>
                    )}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default AveriarCamiones; 