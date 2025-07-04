// components/TablaPedidos.tsx
import React, { useState } from "react";
import { useSimulacion } from "../context/SimulacionContext";
import { Package, MapPin, Truck, Search, ChevronUp, ChevronDown, ChevronsUpDown } from "lucide-react";
import type { Pedido } from "../types";

// Función para obtener el color según el estado del pedido
const getColorByEstado = (estado: string) => {
  switch (estado) {
    case 'PENDIENTE':
      return 'bg-yellow-100 text-yellow-800 border-yellow-300';
    case 'EN_TRANSITO':
      return 'bg-blue-100 text-blue-800 border-blue-300';
    case 'ENTREGADO':
      return 'bg-green-100 text-green-800 border-green-300';
    case 'CANCELADO':
      return 'bg-red-100 text-red-800 border-red-300';
    case 'RETRASO':
      return 'bg-orange-100 text-orange-800 border-orange-300';
    default:
      return 'bg-gray-100 text-gray-800 border-gray-300';
  }
};

// Función para obtener el ícono según el estado del pedido
const getIconByEstado = (estado: string) => {
  switch (estado) {
    case 'PENDIENTE':
      return <Package className="w-4 h-4" />;
    case 'EN_TRANSITO':
      return <Truck className="w-4 h-4" />;
    case 'ENTREGADO':
      return <MapPin className="w-4 h-4" />;
    case 'CANCELADO':
      return <Package className="w-4 h-4" />;
    case 'RETRASO':
      return <Package className="w-4 h-4" />;
    default:
      return <Package className="w-4 h-4" />;
  }
};

interface TablaPedidosProps {
  onElementoSeleccionado?: (elemento: {tipo: 'camion' | 'pedido' | 'almacen', id: string} | null) => void;
}

const TablaPedidos: React.FC<TablaPedidosProps> = ({ onElementoSeleccionado }) => {
  const { rutasCamiones, camiones } = useSimulacion();
  const [filtroEstado, setFiltroEstado] = useState<string>('TODOS');
  const [busqueda, setBusqueda] = useState<string>('');
  const [sortColumn, setSortColumn] = useState<string | null>(null);
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('asc');

  // Extraer todos los pedidos de las rutas de camiones
  const todosPedidos = React.useMemo(() => {
    const pedidos: Array<Pedido & { camionId: string; estadoCamion: string }> = [];
    
    rutasCamiones.forEach(ruta => {
      const camion = camiones.find(c => c.id === ruta.id);
      const estadoCamion = camion?.estado || 'Desconocido';
      
      ruta.pedidos.forEach(pedido => {
        // Inferir el estado del pedido basado en el estado del camión y otros factores
        let estadoPedido = pedido.estado || 'PENDIENTE';
        
        if (estadoCamion === 'Entregado') {
          estadoPedido = 'ENTREGADO';
        } else if (estadoCamion === 'En Camino' || estadoCamion === 'Disponible') {
          estadoPedido = 'EN_TRANSITO';
        } else if (estadoCamion === 'Averiado') {
          estadoPedido = 'RETRASO';
        }
        
        pedidos.push({
          ...pedido,
          estado: estadoPedido,
          camionId: ruta.id,
          estadoCamion
        });
      });
    });
    
    return pedidos;
  }, [rutasCamiones, camiones]);

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
   const getSortValue = (pedido: Pedido & { camionId: string; estadoCamion: string }, column: string) => {
     switch (column) {
       case 'codigo':
         return pedido.codigo;
       case 'estado':
         return pedido.estado || 'PENDIENTE';
       case 'ubicacion':
         return `${pedido.coordenada.x},${pedido.coordenada.y}`;
       case 'camion':
         return pedido.camionId;
       case 'glp':
         return pedido.volumenGLPAsignado || 0;
       default:
         return '';
     }
   };

   // Filtrar y ordenar pedidos según el estado seleccionado y la búsqueda
   const pedidosFiltrados = React.useMemo(() => {
     let result = todosPedidos;
     
     // Filtrar por estado
     if (filtroEstado !== 'TODOS') {
       result = result.filter(pedido => (pedido.estado || 'PENDIENTE') === filtroEstado);
     }
     
     // Filtrar por búsqueda
     if (busqueda.trim() !== '') {
       const terminoBusqueda = busqueda.toLowerCase().trim();
       result = result.filter(pedido => 
         pedido.codigo.toLowerCase().includes(terminoBusqueda) ||
         `${pedido.coordenada.x},${pedido.coordenada.y}`.includes(terminoBusqueda) ||
         `(${pedido.coordenada.x},${pedido.coordenada.y})`.includes(terminoBusqueda) ||
         pedido.camionId.toLowerCase().includes(terminoBusqueda) ||
         (pedido.estado || 'PENDIENTE').toLowerCase().includes(terminoBusqueda)
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
   }, [todosPedidos, filtroEstado, busqueda, sortColumn, sortDirection]);

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

   // Obtener estados únicos para el filtro
   const estadosDisponibles = React.useMemo(() => {
     const estados = new Set(todosPedidos.map(pedido => pedido.estado || 'PENDIENTE'));
     return Array.from(estados);
   }, [todosPedidos]);

  return (
    <div className="flex flex-col flex-1 min-h-0">
      <div className="text-lg font-bold text-black mb-3 flex items-center gap-2">
        <Package className="w-5 h-5" />
        Lista de Pedidos
      </div>

             {/* Filtros */}
       <div className="mb-3 space-y-3">
         {/* Campo de búsqueda */}
         <div>
           <label className="block text-sm font-medium text-gray-700 mb-2">
             Buscar pedidos:
           </label>
           <div className="relative">
             <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
               <Search className="h-4 w-4 text-gray-400" />
             </div>
             <input
               type="text"
               placeholder="Buscar por código, ubicación, camión o estado..."
               value={busqueda}
               onChange={(e) => setBusqueda(e.target.value)}
               className="w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
             />
           </div>
         </div>

         {/* Filtro por estado */}
         <div>
           <label className="block text-sm font-medium text-gray-700 mb-2">
             Filtrar por estado:
           </label>
           <select
             value={filtroEstado}
             onChange={(e) => setFiltroEstado(e.target.value)}
             className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
           >
             <option value="TODOS">Todos los estados ({todosPedidos.length})</option>
             {estadosDisponibles.map(estado => (
               <option key={estado} value={estado}>
                 {estado} ({todosPedidos.filter(p => (p.estado || 'PENDIENTE') === estado).length})
               </option>
             ))}
           </select>
         </div>
       </div>

      {/* Tip para seleccionar pedido */}
      <div className="mb-3 p-2 bg-amber-50 border border-amber-200 rounded-lg">
        <p className="text-xs text-amber-700">
          💡 <strong>Tip:</strong> Haz clic en cualquier fila para resaltar el pedido en el mapa
        </p>
      </div>

      {/* Tabla de pedidos */}
      <div className="flex-1 min-h-0 overflow-y-auto rounded-lg shadow border border-gray-200 bg-white">
                 <table className="min-w-full table-auto text-sm bg-white">
           <thead className="sticky top-0 bg-gray-50 z-10">
             <tr className="border-b border-gray-200">
               <th className="px-4 py-2 text-left font-semibold text-black">
                 <button
                   onClick={() => handleSort('codigo')}
                   className="flex items-center gap-1 hover:text-blue-600 transition-colors"
                   title="Ordenar por Código"
                 >
                   <span>Código</span>
                   {renderSortIcon('codigo')}
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
                   onClick={() => handleSort('camion')}
                   className="flex items-center gap-1 hover:text-blue-600 transition-colors"
                   title="Ordenar por Camión"
                 >
                   <span>Camión</span>
                   {renderSortIcon('camion')}
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
             </tr>
           </thead>
          <tbody>
            {pedidosFiltrados.length === 0 ? (
              <tr>
                <td colSpan={5} className="px-4 py-8 text-center text-gray-500">
                  No hay pedidos para mostrar
                </td>
              </tr>
            ) : (
                             pedidosFiltrados.map((pedido, idx) => (
                 <tr 
                   key={`${pedido.codigo}-${idx}`} 
                   onClick={() => onElementoSeleccionado && onElementoSeleccionado({tipo: 'pedido', id: pedido.codigo})}
                   className="border-b last:border-b-0 bg-white hover:bg-yellow-50 hover:cursor-pointer transition-colors"
                   title="Clic para resaltar en el mapa"
                 >
                   <td className="px-4 py-2 text-gray-800 font-mono font-semibold">
                     {pedido.codigo}
                   </td>
                   <td className="px-4 py-2">
                     <span className={`inline-flex items-center gap-1 px-2 py-1 rounded-full text-xs font-medium border ${getColorByEstado(pedido.estado || 'PENDIENTE')}`}>
                       {getIconByEstado(pedido.estado || 'PENDIENTE')}
                       {pedido.estado || 'PENDIENTE'}
                     </span>
                   </td>
                   <td className="px-4 py-2 text-gray-600">
                     ({pedido.coordenada.x}, {pedido.coordenada.y})
                   </td>
                   <td className="px-4 py-2 text-blue-700 font-bold">
                     {pedido.camionId}
                   </td>
                   <td className="px-4 py-2 text-purple-700 font-bold">
                     {pedido.volumenGLPAsignado?.toFixed(2) || 'N/A'}
                   </td>
                 </tr>
               ))
            )}
          </tbody>
        </table>
      </div>

      {/* Resumen */}
      <div className="mt-3 p-3 bg-gray-50 rounded-lg">
        <div className="flex justify-between items-center text-sm">
          <span className="font-medium text-gray-700">
            Total de pedidos: <span className="font-bold text-blue-600">{todosPedidos.length}</span>
          </span>
          <span className="font-medium text-gray-700">
            Mostrando: <span className="font-bold text-purple-600">{pedidosFiltrados.length}</span>
          </span>
        </div>
      </div>
    </div>
  );
};

export default TablaPedidos;
