import React from 'react';
import { Search, ChevronUp, ChevronDown, ChevronsUpDown } from 'lucide-react';
import { useSimulacion, type CamionEstado } from '../../context/SimulacionContext';
import { formatearCapacidadGLP, formatearCombustible, obtenerClaseColorValor, esValorValido } from '../../utils/validacionCamiones';

interface DatosCamionesTableProps {
  onElementoSeleccionado: (elemento: {tipo: 'camion' | 'pedido' | 'almacen', id: string} | null) => void;
}

const DatosCamionesTable: React.FC<DatosCamionesTableProps> = ({ onElementoSeleccionado }) => {
  const { camiones } = useSimulacion();
  const [busquedaCamion, setBusquedaCamion] = React.useState<string>('');
  const [sortColumn, setSortColumn] = React.useState<string | null>(null);
  const [sortDirection, setSortDirection] = React.useState<'asc' | 'desc'>('asc');

  // Función para manejar el ordenamiento
  const handleSort = (column: string) => {
    if (sortColumn === column) {
      setSortDirection(sortDirection === 'asc' ? 'desc' : 'asc');
    } else {
      setSortColumn(column);
      setSortDirection('asc');
    }
  };

  // Función para obtener el valor a ordenar
  const getSortValue = (camion: CamionEstado, column: string) => {
    switch (column) {
      case 'id':
        return camion.id;
      case 'glp':
        return camion.capacidadActualGLP;
      case 'combustible':
        return camion.combustibleActual;
      case 'ubicacion':
        return camion.ubicacion;
      case 'estado':
        return camion.estado;
      default:
        return '';
    }
  };

  // Filtrar y ordenar camiones (eliminar duplicados primero)
  const camionesFiltrados = React.useMemo(() => {
    // Eliminar duplicados basándose en el ID
    const camionesUnicos = camiones.filter((camion, index, array) => 
      array.findIndex(c => c.id === camion.id) === index
    );
    let result = camionesUnicos;
    
    // Filtrar por búsqueda
    if (busquedaCamion.trim() !== '') {
      const terminoBusqueda = busquedaCamion.toLowerCase().trim();
      result = result.filter(camion => 
        camion.id.toLowerCase().includes(terminoBusqueda) ||
        camion.ubicacion.toLowerCase().includes(terminoBusqueda) ||
        camion.estado.toLowerCase().includes(terminoBusqueda) ||
        camion.capacidadActualGLP.toString().includes(terminoBusqueda) ||
        camion.combustibleActual.toString().includes(terminoBusqueda) ||
        camion.combustibleMaximo.toString().includes(terminoBusqueda)
      );
    }

    // Ordenar
    if (sortColumn) {
      result = [...result].sort((a, b) => {
        const aValue = getSortValue(a, sortColumn);
        const bValue = getSortValue(b, sortColumn);
        
        let comparison = 0;
        if (typeof aValue === 'number' && typeof bValue === 'number') {
          comparison = aValue - bValue;
        } else {
          comparison = String(aValue).localeCompare(String(bValue));
        }
        
        return sortDirection === 'asc' ? comparison : -comparison;
      });
    }

    return result;
  }, [camiones, busquedaCamion, sortColumn, sortDirection]);

  // Función para renderizar el ícono de ordenamiento
  const renderSortIcon = (column: string) => {
    if (sortColumn !== column) {
      return <ChevronsUpDown size={14} className="text-gray-400 hover:text-gray-600" />;
    }
    
    if (sortDirection === 'asc') {
      return <ChevronUp size={14} className="text-blue-600" />;
    } else {
      return <ChevronDown size={14} className="text-blue-600" />;
    }
  };

  return (
    <div className="flex flex-col flex-1 min-h-0 overflow-hidden">
      <div className="text-lg font-bold text-black mb-3 flex items-center gap-2 flex-shrink-0">
        Datos de los camiones
      </div>
      
      {/* Campo de búsqueda */}
      <div className="mb-3 flex-shrink-0">
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Buscar camiones:
        </label>
        <div className="relative">
          <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
            <Search className="h-4 w-4 text-gray-400" />
          </div>
          <input
            type="text"
            placeholder="Buscar por ID, ubicación, estado, capacidad o combustible..."
            value={busquedaCamion}
            onChange={(e) => setBusquedaCamion(e.target.value)}
            className="w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
          />
        </div>
      </div>

      {/* Tip para seleccionar camión */}
      <div className="mb-3 p-2 bg-blue-50 border border-blue-200 rounded-lg flex-shrink-0">
        <p className="text-xs text-blue-700">
          💡 <strong>Tip:</strong> Haz clic en cualquier fila para resaltar el camión en el mapa
        </p>
      </div>

      <div className="right-menu-table-scroll rounded-lg shadow border border-gray-200 bg-white">
        <table className="min-w-full table-auto text-sm bg-white" style={{ minWidth: '600px' }}>
          <thead>
            <tr className="border-b border-gray-200">
              <th className="px-4 py-2 text-left font-semibold text-black">
                <button
                  onClick={() => handleSort('id')}
                  className="flex items-center gap-1 hover:text-blue-600 transition-colors"
                  title="Ordenar por ID"
                >
                  <span>ID</span>
                  {renderSortIcon('id')}
                </button>
              </th>
              <th className="px-4 py-2 text-left font-semibold text-black">
                <button
                  onClick={() => handleSort('glp')}
                  className="flex items-center gap-1 hover:text-blue-600 transition-colors"
                  title="Ordenar por GLP Actual"
                >
                  <span>GLP Actual</span>
                  {renderSortIcon('glp')}
                </button>
              </th>
              <th className="px-4 py-2 text-left font-semibold text-black">
                <button
                  onClick={() => handleSort('combustible')}
                  className="flex items-center gap-1 hover:text-blue-600 transition-colors"
                  title="Ordenar por Combustible"
                >
                  <span>Combustible</span>
                  {renderSortIcon('combustible')}
                </button>
              </th>
              <th className="px-4 py-2 text-left font-semibold text-black">
                <button
                  onClick={() => handleSort('ubicacion')}
                  className="flex items-center gap-1 hover:text-blue-600 transition-colors"
                  title="Ordenar por Ubicación"
                >
                  <span>Ubicación</span>
                  {renderSortIcon('ubicacion')}
                </button>
              </th>
              <th className="px-4 py-2 text-left font-semibold text-black">
                <button
                  onClick={() => handleSort('estado')}
                  className="flex items-center gap-1 hover:text-blue-600 transition-colors"
                  title="Ordenar por Estado"
                >
                  <span>Estado</span>
                  {renderSortIcon('estado')}
                </button>
              </th>
            </tr>
          </thead>
          <tbody>
            {camionesFiltrados.length === 0 ? (
              <tr>
                <td colSpan={5} className="px-4 py-8 text-center text-gray-500">
                  {busquedaCamion.trim() === '' ? 'No hay camiones disponibles' : 'No se encontraron camiones que coincidan con la búsqueda'}
                </td>
              </tr>
            ) : (
              camionesFiltrados.map((camion, index) => {
                try {
                  // Validación antes de renderizar cada fila
                  if (!camion.id) {
                    console.error(`❌ ERROR: Camión en índice ${index} no tiene ID:`, camion);
                    return (
                      <tr key={`error-no-id-${index}`} className="border-b last:border-b-0 bg-red-50">
                        <td colSpan={5} className="px-4 py-2 text-red-600 text-center">
                          Error: Camión sin ID
                        </td>
                      </tr>
                    );
                  }

                  // Log de validación si hay problemas
                  if (!esValorValido(camion.capacidadActualGLP)) {
                    console.error(`❌ ERROR: Camión ${camion.id} tiene capacidadActualGLP inválida:`, camion.capacidadActualGLP);
                  }
                  if (!esValorValido(camion.capacidadMaximaGLP)) {
                    console.error(`❌ ERROR: Camión ${camion.id} tiene capacidadMaximaGLP inválida:`, camion.capacidadMaximaGLP);
                  }
                  if (!esValorValido(camion.combustibleActual)) {
                    console.error(`❌ ERROR: Camión ${camion.id} tiene combustibleActual inválido:`, camion.combustibleActual);
                  }
                  if (!esValorValido(camion.combustibleMaximo)) {
                    console.error(`❌ ERROR: Camión ${camion.id} tiene combustibleMaximo inválido:`, camion.combustibleMaximo);
                  }

                  return (
                    <tr 
                      key={`camion-table-${camion.id}`} 
                      onClick={() => onElementoSeleccionado({tipo: 'camion', id: camion.id})}
                      className={
                        `border-b last:border-b-0 bg-white hover:bg-blue-50 hover:cursor-pointer transition-colors`
                      }
                      title="Clic para resaltar en el mapa"
                    >
                      <td className="px-4 py-2 text-gray-800 font-mono font-semibold">{camion.id}</td>
                      <td className={`px-4 py-2 font-bold ${obtenerClaseColorValor(camion.capacidadActualGLP)}`}>
                        {formatearCapacidadGLP(camion.capacidadActualGLP, camion.capacidadMaximaGLP)}
                      </td>
                      <td className={`px-4 py-2 font-bold ${obtenerClaseColorValor(camion.combustibleActual)}`}>
                        {formatearCombustible(camion.combustibleActual, camion.combustibleMaximo)}
                      </td>
                      <td className="px-4 py-2 text-gray-600">
                        {camion.ubicacion || 'N/A'}
                      </td>
                      <td className={
                        `px-4 py-2 font-semibold ` +
                        (camion.estado === 'Averiado' ? 'text-red-600' :
                        camion.estado === 'En Mantenimiento' ? 'text-yellow-600' :
                        camion.estado === 'En Mantenimiento Preventivo' ? 'text-amber-600' :
                        camion.estado === 'En Mantenimiento por Avería' ? 'text-orange-600' :
                        camion.estado === 'En Ruta' ? 'text-purple-600' :
                        camion.estado === 'Disponible' ? 'text-blue-600' :
                        'text-gray-700')
                      }>
                        {camion.estado || 'Desconocido'}
                      </td>
                    </tr>
                  );
                } catch (error) {
                  console.error(`❌ ERROR al renderizar fila de camión ${index}:`, error, camion);
                  return (
                    <tr key={`error-render-${index}`} className="border-b last:border-b-0 bg-red-50">
                      <td colSpan={5} className="px-4 py-2 text-red-600 text-center">
                        Error al renderizar camión: {error instanceof Error ? error.message : 'Error desconocido'}
                      </td>
                    </tr>
                  );
                }
              })
            )}
          </tbody>
        </table>
      </div>
      
      {/* Resumen */}
      <div className="mt-3 p-3 bg-gray-50 rounded-lg flex-shrink-0">
        <div className="flex justify-between items-center text-sm">
          <span className="font-medium text-gray-700">
            Total de camiones: <span className="font-bold text-blue-600">{camiones.length}</span>
          </span>
          <span className="font-medium text-gray-700">
            Mostrando: <span className="font-bold text-purple-600">{camionesFiltrados.length}</span>
          </span>
        </div>
      </div>
    </div>
  );
};

export default DatosCamionesTable; 