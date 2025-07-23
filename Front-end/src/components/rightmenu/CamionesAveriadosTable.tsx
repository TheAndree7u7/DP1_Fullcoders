import React from 'react';
import { Search, ChevronUp, ChevronDown, ChevronsUpDown, Wrench, AlertTriangle, Settings } from 'lucide-react';
import { useSimulacion, type CamionEstado } from '../../context/SimulacionContext';
import { handleAveriar } from '../mapa/utils/averias';
import { toast, Bounce } from 'react-toastify';

interface CamionesAveriadosTableProps {
  onElementoSeleccionado: (elemento: {tipo: 'camion' | 'pedido' | 'almacen', id: string} | null) => void;
}

const CamionesAveriadosTable: React.FC<CamionesAveriadosTableProps> = ({ onElementoSeleccionado }) => {
  const { 
    camiones, 
    marcarCamionAveriado, 
    setSimulacionActiva, 
    setPollingActivo, 
    aplicarNuevaSolucionDespuesAveria,
    horaActual,
    horaSimulacion,
    fechaHoraSimulacion,
    fechaInicioSimulacion,
    diaSimulacion,
    tiempoRealSimulacion,
    tiempoTranscurridoSimulado,
    rutasCamiones,
    almacenes,
    bloqueos
  } = useSimulacion();
  
  const [busquedaCamion, setBusquedaCamion] = React.useState<string>('');
  const [sortColumn, setSortColumn] = React.useState<string | null>(null);
  const [sortDirection, setSortDirection] = React.useState<'asc' | 'desc'>('asc');
  const [averiando, setAveriando] = React.useState<string | null>(null);

  // Función para manejar la avería de un camión usando la misma lógica del mapa
  const handleAveriarCamion = async (camionId: string, tipoAveria: number) => {
    try {
      console.log(`🛠️ Aplicando avería tipo ${tipoAveria} al camión ${camionId}`);
      
      // Usar la misma función que usa el mapa
      await handleAveriar(
        camionId, 
        tipoAveria, 
        marcarCamionAveriado, 
        setAveriando, 
        () => {}, // setClickedCamion (no necesario en la tabla)
        setSimulacionActiva, 
        {
          horaActual,
          horaSimulacion,
          fechaHoraSimulacion,
          fechaInicioSimulacion,
          diaSimulacion,
          tiempoRealSimulacion,
          tiempoTranscurridoSimulado,
          camiones,
          rutasCamiones,
          almacenes,
          bloqueos
        }, 
        setPollingActivo, 
        aplicarNuevaSolucionDespuesAveria
      );
      
      toast.success(`🚛💥 Camión ${camionId} averiado (Tipo ${tipoAveria}) exitosamente`, {
        position: "top-right",
        autoClose: 4000,
        hideProgressBar: false,
        closeOnClick: true,
        pauseOnHover: true,
        draggable: true,
        progress: undefined,
        theme: "light",
        transition: Bounce,
      });
      
    } catch (error) {
      console.error('❌ Error al aplicar avería:', error);
      toast.error(`❌ Error al aplicar avería tipo ${tipoAveria} al camión ${camionId}`, {
        position: "top-right",
        autoClose: 6000,
        hideProgressBar: false,
        closeOnClick: true,
        pauseOnHover: true,
        draggable: true,
        progress: undefined,
        theme: "light",
        transition: Bounce,
      });
    }
  };

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
      case 'estado':
        return camion.estado;
      default:
        return '';
    }
  };

  // Filtrar todos los camiones y ordenar
  const camionesAveriados = React.useMemo(() => {
    // Eliminar duplicados basándose en el ID
    const camionesUnicos = camiones.filter((camion, index, array) => 
      array.findIndex(c => c.id === camion.id) === index
    );
    
    // Mostrar todos los camiones
    let result = camionesUnicos;
    
    // Filtrar por búsqueda
    if (busquedaCamion.trim() !== '') {
      const terminoBusqueda = busquedaCamion.toLowerCase().trim();
      result = result.filter(camion => 
        camion.id.toLowerCase().includes(terminoBusqueda) ||
        camion.estado.toLowerCase().includes(terminoBusqueda)
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
        Averiar Camion
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
            placeholder="Buscar por ID o estado..."
            value={busquedaCamion}
            onChange={(e) => setBusquedaCamion(e.target.value)}
            className="w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
          />
        </div>
      </div>

             {/* Tip para seleccionar camión */}
       <div className="mb-3 p-2 bg-blue-50 border border-blue-200 rounded-lg">
         <p className="text-xs text-blue-700">
           💡 <strong>Camiones:</strong> Haz clic en cualquier fila para resaltar el camión en el mapa
         </p>
       </div>

             <div className="flex-1 min-h-0 overflow-y-auto rounded-lg shadow border border-gray-200 bg-white">
         <table className="w-full table-fixed text-sm bg-white">
          <thead>
                         <tr className="border-b border-gray-200">
               <th className="w-1/4 px-4 py-2 text-left font-semibold text-black">
                 <button
                   onClick={() => handleSort('id')}
                   className="flex items-center gap-1 hover:text-blue-600 transition-colors"
                   title="Ordenar por ID"
                 >
                   <span>ID</span>
                   {renderSortIcon('id')}
                 </button>
               </th>
               <th className="w-1/4 px-4 py-2 text-left font-semibold text-black">
                 <button
                   onClick={() => handleSort('estado')}
                   className="flex items-center gap-1 hover:text-blue-600 transition-colors"
                   title="Ordenar por Estado"
                 >
                   <span>Estado</span>
                   {renderSortIcon('estado')}
                 </button>
               </th>
               <th className="w-1/2 px-4 py-2 text-left font-semibold text-black">
                 <span>Acciones</span>
               </th>
             </tr>
          </thead>
          <tbody>
                         {camionesAveriados.length === 0 ? (
               <tr>
                 <td colSpan={3} className="px-4 py-8 text-center text-gray-500">
                   {busquedaCamion.trim() === '' ? 'No hay camiones disponibles' : 'No se encontraron camiones que coincidan con la búsqueda'}
                 </td>
               </tr>
             ) : (
              camionesAveriados.map((camion, index) => {
                try {
                                     // Validación antes de renderizar cada fila
                   if (!camion.id) {
                     console.error(`❌ ERROR: Camión en índice ${index} no tiene ID:`, camion);
                     return (
                       <tr key={`error-no-id-${index}`} className="border-b last:border-b-0 bg-red-50">
                         <td colSpan={3} className="px-4 py-2 text-red-600 text-center">
                           Error: Camión sin ID
                         </td>
                       </tr>
                     );
                   }

                  return (
                                                              <tr 
                       key={`camion-averiado-${camion.id}`} 
                       className={
                         `border-b last:border-b-0 bg-white hover:bg-blue-50 transition-colors`
                       }
                     >
                       <td 
                         className="px-4 py-2 text-gray-800 font-mono font-semibold cursor-pointer"
                         onClick={() => onElementoSeleccionado({tipo: 'camion', id: camion.id})}
                         title="Clic para resaltar en el mapa"
                       >
                         {camion.id}
                       </td>
                       <td 
                         className={
                           `px-4 py-2 font-semibold cursor-pointer ` +
                           (camion.estado === 'Averiado' ? 'text-red-600' :
                           camion.estado === 'En Mantenimiento' ? 'text-yellow-600' :
                           camion.estado === 'En Mantenimiento Preventivo' ? 'text-amber-600' :
                           camion.estado === 'En Mantenimiento por Avería' ? 'text-orange-600' :
                           camion.estado === 'En Ruta' ? 'text-purple-600' :
                           camion.estado === 'Disponible' ? 'text-blue-600' :
                           'text-gray-700')
                         }
                         onClick={() => onElementoSeleccionado({tipo: 'camion', id: camion.id})}
                         title="Clic para resaltar en el mapa"
                       >
                         {camion.estado || 'Desconocido'}
                       </td>
                                               <td className="px-4 py-2 w-1/2">
                                                     <div className="flex gap-2 flex-wrap justify-start">
                             <button
                               onClick={(e) => {
                                 e.stopPropagation();
                                 handleAveriarCamion(camion.id, 1);
                               }}
                               disabled={averiando === camion.id + '-1' || camion.estado === 'Averiado' || camion.estado === 'En Mantenimiento' || camion.estado === 'En Mantenimiento Preventivo' || camion.estado === 'En Mantenimiento por Avería'}
                               className="px-3 py-1 text-xs bg-yellow-500 hover:bg-yellow-600 text-white rounded transition-colors font-semibold disabled:opacity-50"
                               title={camion.estado === 'Averiado' || camion.estado === 'En Mantenimiento' || camion.estado === 'En Mantenimiento Preventivo' || camion.estado === 'En Mantenimiento por Avería' ? 'Camión no disponible para averías' : 'Avería tipo 1 (Menor)'}
                             >
                               {averiando === camion.id + '-1' ? 'Averiando...' : 'Tipo 1'}
                             </button>
                             <button
                               onClick={(e) => {
                                 e.stopPropagation();
                                 handleAveriarCamion(camion.id, 2);
                               }}
                               disabled={averiando === camion.id + '-2' || camion.estado === 'Averiado' || camion.estado === 'En Mantenimiento' || camion.estado === 'En Mantenimiento Preventivo' || camion.estado === 'En Mantenimiento por Avería'}
                               className="px-3 py-1 text-xs bg-orange-500 hover:bg-orange-600 text-white rounded transition-colors font-semibold disabled:opacity-50"
                               title={camion.estado === 'Averiado' || camion.estado === 'En Mantenimiento' || camion.estado === 'En Mantenimiento Preventivo' || camion.estado === 'En Mantenimiento por Avería' ? 'Camión no disponible para averías' : 'Avería tipo 2 (Media)'}
                             >
                               {averiando === camion.id + '-2' ? 'Averiando...' : 'Tipo 2'}
                             </button>
                             <button
                               onClick={(e) => {
                                 e.stopPropagation();
                                 handleAveriarCamion(camion.id, 3);
                               }}
                               disabled={averiando === camion.id + '-3' || camion.estado === 'Averiado' || camion.estado === 'En Mantenimiento' || camion.estado === 'En Mantenimiento Preventivo' || camion.estado === 'En Mantenimiento por Avería'}
                               className="px-3 py-1 text-xs bg-red-500 hover:bg-red-600 text-white rounded transition-colors font-semibold disabled:opacity-50"
                               title={camion.estado === 'Averiado' || camion.estado === 'En Mantenimiento' || camion.estado === 'En Mantenimiento Preventivo' || camion.estado === 'En Mantenimiento por Avería' ? 'Camión no disponible para averías' : 'Avería tipo 3 (Grave)'}
                             >
                               {averiando === camion.id + '-3' ? 'Averiando...' : 'Tipo 3'}
                             </button>
                           </div>
                        </td>
                     </tr>
                  );
                                 } catch (error) {
                   console.error(`❌ ERROR al renderizar fila de camión averiado ${index}:`, error, camion);
                   return (
                     <tr key={`error-render-${index}`} className="border-b last:border-b-0 bg-red-50">
                       <td colSpan={3} className="px-4 py-2 text-red-600 text-center">
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
       <div className="mt-3 p-3 bg-blue-50 rounded-lg">
         <div className="flex justify-between items-center text-sm">
           <span className="font-medium text-gray-700">
             Total de camiones: <span className="font-bold text-blue-600">{camionesAveriados.length}</span>
           </span>
           <span className="font-medium text-gray-700">
             Mostrando: <span className="font-bold text-blue-600">{camionesAveriados.length}</span>
           </span>
         </div>
       </div>
    </div>
  );
};

export default CamionesAveriadosTable; 