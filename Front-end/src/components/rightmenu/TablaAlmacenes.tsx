import React from 'react';
import { Search, ChevronUp, ChevronDown, ChevronsUpDown, Building2 } from 'lucide-react';
import { useSimulacion } from '../../context/SimulacionContext';
import { colorSemaforoGLP } from '../mapa/utils';

interface TablaAlmacenesProps {
  onElementoSeleccionado: (elemento: {tipo: 'camion' | 'pedido' | 'almacen', id: string} | null) => void;
}

const TablaAlmacenes: React.FC<TablaAlmacenesProps> = ({ onElementoSeleccionado }) => {
  const { almacenes } = useSimulacion();
  const [busquedaAlmacen, setBusquedaAlmacen] = React.useState<string>('');
  const [sortColumn, setSortColumn] = React.useState<string>('');
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
  const getSortValue = (almacen: { nombre: string; tipo: string; coordenada: { x: number; y: number }; capacidadActualGLP: number; capacidadMaximaGLP: number }, column: string) => {
    switch (column) {
      case 'nombre':
        return almacen.nombre;
      case 'tipo':
        return almacen.tipo;
      case 'ubicacion':
        return `${almacen.coordenada.x},${almacen.coordenada.y}`;
      case 'glp':
        return almacen.capacidadActualGLP || 0;
      case 'porcentaje':
        return almacen.capacidadMaximaGLP > 0 ? (almacen.capacidadActualGLP / almacen.capacidadMaximaGLP) * 100 : 0;
      default:
        return '';
    }
  };

  // Filtrar y ordenar almacenes
  const almacenesFiltrados = React.useMemo(() => {
    let result = almacenes;

    // Filtrar por búsqueda
    if (busquedaAlmacen.trim() !== '') {
      const terminoBusqueda = busquedaAlmacen.toLowerCase().trim();
      result = result.filter(almacen => 
        almacen.nombre.toLowerCase().includes(terminoBusqueda) ||
        almacen.tipo.toLowerCase().includes(terminoBusqueda) ||
        `${almacen.coordenada.x},${almacen.coordenada.y}`.includes(terminoBusqueda) ||
        `(${almacen.coordenada.x},${almacen.coordenada.y})`.includes(terminoBusqueda)
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
  }, [almacenes, busquedaAlmacen, sortColumn, sortDirection]);

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
    <div className="flex flex-col flex-1 min-h-0">
      <div className="text-lg font-bold text-black mb-3 flex items-center gap-2">
        <Building2 className="w-5 h-5" />
        Lista de Almacenes
      </div>
      
      {/* Campo de búsqueda */}
      <div className="mb-3">
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Buscar almacenes:
        </label>
        <div className="relative">
          <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
            <Search className="h-4 w-4 text-gray-400" />
          </div>
          <input
            type="text"
            placeholder="Buscar por nombre, tipo o ubicación..."
            value={busquedaAlmacen}
            onChange={(e) => setBusquedaAlmacen(e.target.value)}
            className="w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
          />
        </div>
      </div>

      {/* Tip para seleccionar almacén */}
      <div className="mb-3 p-2 bg-green-50 border border-green-200 rounded-lg">
        <p className="text-xs text-green-700">
          💡 <strong>Tip:</strong> Haz clic en cualquier fila para resaltar el almacén en el mapa
        </p>
      </div>

      <div className="flex-1 min-h-0 overflow-y-auto rounded-lg shadow border border-gray-200 bg-white">
        <table className="min-w-full table-auto text-sm bg-white">
          <thead className="sticky top-0 bg-gray-50 z-10">
            <tr className="border-b border-gray-200">
              <th className="px-4 py-2 text-left font-semibold text-black">
                <button
                  onClick={() => handleSort('nombre')}
                  className="flex items-center gap-1 hover:text-blue-600 transition-colors"
                  title="Ordenar por Nombre"
                >
                  <span>Nombre</span>
                  {renderSortIcon('nombre')}
                </button>
              </th>
              <th className="px-4 py-2 text-left font-semibold text-black">
                <button
                  onClick={() => handleSort('tipo')}
                  className="flex items-center gap-1 hover:text-blue-600 transition-colors"
                  title="Ordenar por Tipo"
                >
                  <span>Tipo</span>
                  {renderSortIcon('tipo')}
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
                  onClick={() => handleSort('glp')}
                  className="flex items-center gap-1 hover:text-blue-600 transition-colors"
                  title="Ordenar por GLP"
                >
                  <span>GLP</span>
                  {renderSortIcon('glp')}
                </button>
              </th>
              <th className="px-4 py-2 text-left font-semibold text-black">
                <button
                  onClick={() => handleSort('porcentaje')}
                  className="flex items-center gap-1 hover:text-blue-600 transition-colors"
                  title="Ordenar por Porcentaje"
                >
                  <span>%</span>
                  {renderSortIcon('porcentaje')}
                </button>
              </th>
            </tr>
          </thead>
          <tbody>
            {almacenesFiltrados.length === 0 ? (
              <tr>
                <td colSpan={5} className="px-4 py-8 text-center text-gray-500">
                  {busquedaAlmacen.trim() === '' ? 'No hay almacenes disponibles' : 'No se encontraron almacenes que coincidan con la búsqueda'}
                </td>
              </tr>
            ) : (
              almacenesFiltrados.map((almacen) => {
                const porcentajeGLP = almacen.capacidadMaximaGLP > 0 ? (almacen.capacidadActualGLP / almacen.capacidadMaximaGLP) * 100 : 0;
                return (
                  <tr 
                    key={almacen.id} 
                    onClick={() => onElementoSeleccionado({tipo: 'almacen', id: almacen.id})}
                    className="border-b last:border-b-0 bg-white hover:bg-green-50 hover:cursor-pointer transition-colors"
                    title="Clic para resaltar en el mapa"
                  >
                    <td className="px-4 py-2 text-gray-800 font-semibold">{almacen.nombre}</td>
                    <td className="px-4 py-2">
                      <span className={`px-2 py-1 rounded-full text-xs font-medium ${
                        almacen.tipo === 'CENTRAL' 
                          ? 'bg-blue-100 text-blue-800' 
                          : 'bg-green-100 text-green-800'
                      }`}>
                        {almacen.tipo === 'CENTRAL' ? 'Central' : 'Intermedio'}
                      </span>
                    </td>
                    <td className="px-4 py-2 text-gray-600">
                      ({almacen.coordenada.x}, {almacen.coordenada.y})
                    </td>
                    <td className="px-4 py-2 font-bold" style={{ color: colorSemaforoGLP(porcentajeGLP) }}>
                      {almacen.capacidadActualGLP.toFixed(2)} / {almacen.capacidadMaximaGLP}
                    </td>
                    <td className="px-4 py-2">
                      <div className="flex items-center gap-2">
                        <div className="w-16 h-2 bg-gray-200 rounded-full overflow-hidden">
                          <div 
                            className="h-full transition-all duration-300"
                            style={{ 
                              width: `${Math.min(porcentajeGLP, 100)}%`,
                              backgroundColor: colorSemaforoGLP(porcentajeGLP)
                            }}
                          />
                        </div>
                        <span 
                          className="text-xs font-medium w-10"
                          style={{ color: colorSemaforoGLP(porcentajeGLP) }}
                        >
                          {porcentajeGLP.toFixed(0)}%
                        </span>
                      </div>
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>
      
      {/* Resumen */}
      <div className="mt-3 p-3 bg-gray-50 rounded-lg">
        <div className="flex justify-between items-center text-sm">
          <span className="font-medium text-gray-700">
            Total de almacenes: <span className="font-bold text-blue-600">{almacenes.length}</span>
          </span>
          <span className="font-medium text-gray-700">
            Mostrando: <span className="font-bold text-purple-600">{almacenesFiltrados.length}</span>
          </span>
        </div>
      </div>
    </div>
  );
};

export default TablaAlmacenes; 