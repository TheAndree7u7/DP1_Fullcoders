import React from 'react';
import { ChevronRight, Search, ChevronUp, ChevronDown, ChevronsUpDown, Building2, Plus } from 'lucide-react';
import { useSimulacion, type CamionEstado } from '../context/SimulacionContext';
import MetricasRendimiento from './MetricasRendimiento';
import CardsCamiones from './CardCamion';
import IndicadoresCamiones from './IndicadoresCamiones';
import BloqueosTable from './BloqueosTable';
import TablaPedidos from './TablaPedidos';
import AgregarPedidosPanel from './AgregarPedidosPanel';
import { formatearCapacidadGLP, formatearCombustible, obtenerClaseColorValor, esValorValido } from '../utils/validacionCamiones';

// Tabla simple de datos de camiones usando el contexto
function DatosCamionesTable({ onElementoSeleccionado }: { onElementoSeleccionado: (elemento: {tipo: 'camion' | 'pedido' | 'almacen', id: string} | null) => void }) {
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
    <div className="flex flex-col flex-1 min-h-0">
      <div className="text-lg font-bold text-black mb-3 flex items-center gap-2">
        Datos de los camiones
      </div>
      
      {/* Campo de búsqueda */}
      <div className="mb-3">
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
      <div className="mb-3 p-2 bg-blue-50 border border-blue-200 rounded-lg">
        <p className="text-xs text-blue-700">
          💡 <strong>Tip:</strong> Haz clic en cualquier fila para resaltar el camión en el mapa
        </p>
      </div>

      <div className="flex-1 min-h-0 overflow-y-auto rounded-lg shadow border border-gray-200 bg-white">
        <table className="min-w-full table-auto text-sm bg-white">
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
                      <tr key={`error-${index}`} className="border-b last:border-b-0 bg-red-50">
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
                      key={`camion-table-${camion.id}-${index}`} 
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
                      <td className="px-4 py-2 text-gray-600">{camion.ubicacion || 'N/A'}</td>
                      <td className={
                        `px-4 py-2 font-semibold ` +
                        (camion.estado === 'Averiado' ? 'text-red-600' :
                        camion.estado === 'En Mantenimiento' ? 'text-yellow-600' :
                        camion.estado === 'En Mantenimiento por Avería' ? 'text-orange-600' :
                        camion.estado === 'Entregado' ? 'text-emerald-600' :
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
                    <tr key={`error-${index}`} className="border-b last:border-b-0 bg-red-50">
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
      <div className="mt-3 p-3 bg-gray-50 rounded-lg">
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
}

// Tabla de almacenes usando el contexto
function TablaAlmacenes({ onElementoSeleccionado }: { onElementoSeleccionado: (elemento: {tipo: 'camion' | 'pedido' | 'almacen', id: string} | null) => void }) {
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
                    <td className="px-4 py-2 text-blue-700 font-bold">
                      {almacen.capacidadActualGLP.toFixed(2)} / {almacen.capacidadMaximaGLP}
                    </td>
                    <td className="px-4 py-2">
                      <div className="flex items-center gap-2">
                        <div className="w-16 h-2 bg-gray-200 rounded-full overflow-hidden">
                          <div 
                            className={`h-full transition-all duration-300 ${
                              porcentajeGLP > 80 ? 'bg-green-500' :
                              porcentajeGLP > 50 ? 'bg-yellow-500' :
                              porcentajeGLP > 20 ? 'bg-orange-500' :
                              'bg-red-500'
                            }`}
                            style={{ width: `${Math.min(porcentajeGLP, 100)}%` }}
                          />
                        </div>
                        <span className="text-xs font-medium text-gray-700 w-10">
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
}

interface RightMenuProps {
  expanded: boolean;
  setExpanded: (value: boolean) => void;
  onElementoSeleccionado: (elemento: {tipo: 'camion' | 'pedido' | 'almacen', id: string} | null) => void;
}

const RightMenu: React.FC<RightMenuProps> = ({ expanded, setExpanded, onElementoSeleccionado }) => {
  const [panel, setPanel] = React.useState<'camiones' | 'bloqueos' | 'metricas' | 'estadoCamiones' | 'pedidos' | 'almacenes' | 'agregarPedidos'>('camiones');
  if (!expanded) return null;

  return (
    <div className="transition-all duration-300 bg-white rounded-xl p-4 shadow-md h-full flex flex-col w-full min-w-80 max-w-full">
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
        <button
          className={`px-3 py-1 rounded font-semibold transition ${panel === 'almacenes' ? 'bg-blue-500 text-white' : 'bg-gray-200 text-gray-800 hover:bg-blue-100'}`}
          onClick={() => setPanel('almacenes')}
        >
          Almacenes
        </button>
        <button
          className={`px-3 py-1 rounded font-semibold transition ${panel === 'agregarPedidos' ? 'bg-blue-500 text-white' : 'bg-gray-200 text-gray-800 hover:bg-blue-100'}`}
          onClick={() => setPanel('agregarPedidos')}
        >
          <Plus className="w-4 h-4 inline mr-1" />
          Agregar Pedidos
        </button>
      </div>

      <div className="flex flex-col flex-1 min-h-0">
        {panel === 'camiones' && (
          <DatosCamionesTable onElementoSeleccionado={onElementoSeleccionado} />
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
          <TablaPedidos onElementoSeleccionado={onElementoSeleccionado} />
        )}
        {panel === 'almacenes' && (
          <TablaAlmacenes onElementoSeleccionado={onElementoSeleccionado} />
        )}
        {panel === 'agregarPedidos' && (
          <AgregarPedidosPanel />
        )}
      </div>
    </div>
  );
};

export default RightMenu;
