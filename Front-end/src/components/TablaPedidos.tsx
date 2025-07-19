// components/TablaPedidos.tsx
import React, { useState } from "react";
import { useSimulacion } from "../context/SimulacionContext";
import { Package, MapPin, Truck, Search, ChevronUp, ChevronDown, ChevronsUpDown, Calendar } from "lucide-react";
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
    case 'NO_ASIGNADO':
      return 'bg-gray-100 text-gray-800 border-gray-300';
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
    case 'NO_ASIGNADO':
      return <Package className="w-4 h-4" />;
    default:
      return <Package className="w-4 h-4" />;
  }
};

// Función para formatear fecha
const formatearFecha = (fecha: string | undefined) => {
  if (!fecha) return 'N/A';
  try {
    const date = new Date(fecha);
    return date.toLocaleDateString('es-ES', {
      day: '2-digit',
      month: '2-digit',
      year: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  } catch {
    return 'N/A';
  }
};

interface TablaPedidosProps {
  onElementoSeleccionado?: (elemento: {tipo: 'camion' | 'pedido' | 'almacen', id: string} | null) => void;
}

const TablaPedidos: React.FC<TablaPedidosProps> = ({ onElementoSeleccionado }) => {
  const { rutasCamiones, camiones, pedidosNoAsignados } = useSimulacion();
  const [filtroEstado, setFiltroEstado] = useState<string>('TODOS');
  const [busqueda, setBusqueda] = useState<string>('');
  const [sortColumn, setSortColumn] = useState<string | null>(null);
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('asc');

  // Extraer y agrupar pedidos por código para evitar duplicados
  const todosPedidos = React.useMemo(() => {
    const pedidosMap = new Map<string, Pedido & { 
      camionesAsignados: string[]; 
      estadosCamiones: string[];
      volumenTotal: number;
      volumenPendiente: number;
      estadoPedido: string;
    }>();
    
    // Procesar pedidos asignados a camiones
    rutasCamiones.forEach(ruta => {
      const camion = camiones.find(c => c.id === ruta.id);
      const estadoCamion = camion?.estado || 'Desconocido';
      
      ruta.pedidos.forEach(pedido => {
        if (!pedidosMap.has(pedido.codigo)) {
          // Crear nuevo pedido agrupado
          pedidosMap.set(pedido.codigo, {
            ...pedido,
            camionesAsignados: [ruta.id],
            estadosCamiones: [estadoCamion],
            volumenTotal: pedido.volumenGLPAsignado,
            volumenPendiente: pedido.volumenGLPAsignado,
            estadoPedido: 'PENDIENTE'
          });
        } else {
          // Agregar camión al pedido existente
          const pedidoExistente = pedidosMap.get(pedido.codigo)!;
          pedidoExistente.camionesAsignados.push(ruta.id);
          pedidoExistente.estadosCamiones.push(estadoCamion);
        }
      });
    });

    // Procesar pedidos no asignados
    pedidosNoAsignados.forEach(pedido => {
      if (!pedidosMap.has(pedido.codigo)) {
        // Crear nuevo pedido no asignado
        pedidosMap.set(pedido.codigo, {
          ...pedido,
          camionesAsignados: [],
          estadosCamiones: [],
          volumenTotal: pedido.volumenGLPAsignado,
          volumenPendiente: pedido.volumenGLPAsignado,
          estadoPedido: 'NO_ASIGNADO'
        });
      }
    });

    // Calcular estado final de cada pedido basado en los estados de sus camiones
    const pedidosAgrupados = Array.from(pedidosMap.values()).map(pedido => {
      // Si no tiene camiones asignados, es NO_ASIGNADO
      if (pedido.camionesAsignados.length === 0) {
        return {
          ...pedido,
          estado: 'NO_ASIGNADO',
          estadoPedido: 'NO_ASIGNADO'
        };
      }

      const todosEntregados = pedido.estadosCamiones.every(estado => estado === 'Entregado');
      const algunoEnCamino = pedido.estadosCamiones.some(estado => estado === 'En Camino' || estado === 'Disponible');
      const algunoAveriado = pedido.estadosCamiones.some(estado => estado === 'Averiado');
      
      let estadoFinal = 'PENDIENTE';
      if (todosEntregados) {
        estadoFinal = 'ENTREGADO';
      } else if (algunoEnCamino) {
        estadoFinal = 'EN_TRANSITO';
      } else if (algunoAveriado) {
        estadoFinal = 'RETRASO';
      }
      
      return {
        ...pedido,
        estado: estadoFinal,
        estadoPedido: estadoFinal
      };
    });
    
    return pedidosAgrupados;
  }, [rutasCamiones, camiones, pedidosNoAsignados]);

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
   const getSortValue = (pedido: Pedido & { 
     camionesAsignados: string[]; 
     estadosCamiones: string[];
     volumenTotal: number;
     volumenPendiente: number;
     estadoPedido: string;
   }, column: string) => {
     switch (column) {
       case 'codigo':
         return pedido.codigo;
       case 'estado':
         return pedido.estado || 'PENDIENTE';
       case 'ubicacion':
         return `${pedido.coordenada.x},${pedido.coordenada.y}`;
       case 'camion':
         return pedido.camionesAsignados ? pedido.camionesAsignados.join(', ') : '';
       case 'glp':
         return pedido.volumenGLPAsignado || 0;
       case 'fechaRegistro':
         return pedido.fechaRegistro || '';
       case 'fechaLimite':
         return pedido.fechaLimite || '';
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
         (pedido.camionesAsignados && pedido.camionesAsignados.some((camion: string) => camion.toLowerCase().includes(terminoBusqueda))) ||
         (pedido.estado || 'PENDIENTE').toLowerCase().includes(terminoBusqueda) ||
         formatearFecha(pedido.fechaRegistro).toLowerCase().includes(terminoBusqueda) ||
         formatearFecha(pedido.fechaLimite).toLowerCase().includes(terminoBusqueda)
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
               placeholder="Buscar por código, ubicación, camión, estado o fechas..."
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
      <div className="flex-1 min-h-0 overflow-auto rounded-lg shadow border border-gray-200 bg-white">
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
                   <span>Camiones Asignados</span>
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
               <th className="px-4 py-2 text-left font-semibold text-black">
                 <button
                   onClick={() => handleSort('fechaRegistro')}
                   className="flex items-center gap-1 hover:text-blue-600 transition-colors"
                   title="Ordenar por Fecha de Registro"
                 >
                   <span>Fecha Registro</span>
                   {renderSortIcon('fechaRegistro')}
                 </button>
               </th>
               <th className="px-4 py-2 text-left font-semibold text-black">
                 <button
                   onClick={() => handleSort('fechaLimite')}
                   className="flex items-center gap-1 hover:text-blue-600 transition-colors"
                   title="Ordenar por Fecha Límite"
                 >
                   <span>Fecha Límite</span>
                   {renderSortIcon('fechaLimite')}
                 </button>
               </th>
             </tr>
           </thead>
          <tbody>
            {pedidosFiltrados.length === 0 ? (
              <tr>
                <td colSpan={7} className="px-4 py-8 text-center text-gray-500">
                  No hay pedidos para mostrar
                </td>
              </tr>
            ) : (
                             pedidosFiltrados.map((pedido) => (
                 <tr 
                   key={pedido.codigo} 
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
                   <td className="px-4 py-2">
                     {pedido.camionesAsignados && pedido.camionesAsignados.length > 0 ? (
                       <div className="space-y-1">
                         <div className="text-xs text-gray-600">
                           {pedido.camionesAsignados.length} camión{pedido.camionesAsignados.length > 1 ? 'es' : ''}
                         </div>
                         <div className="flex flex-wrap gap-1">
                           {pedido.camionesAsignados.map((camionId: string) => (
                             <span
                               key={camionId}
                               className="px-2 py-1 bg-blue-100 text-blue-700 rounded text-xs font-bold"
                             >
                               {camionId}
                             </span>
                           ))}
                         </div>
                       </div>
                     ) : (
                       <span className="text-gray-500">Sin asignar</span>
                     )}
                   </td>
                   <td className="px-4 py-2 text-purple-700 font-bold">
                     {pedido.volumenGLPAsignado?.toFixed(2) || 'N/A'}
                   </td>
                   <td className="px-4 py-2 text-gray-600">
                     <div className="flex items-center gap-1">
                       <Calendar className="w-4 h-4 text-gray-400" />
                       {formatearFecha(pedido.fechaRegistro)}
                     </div>
                   </td>
                   <td className="px-4 py-2 text-gray-600">
                     <div className="flex items-center gap-1">
                       <Calendar className="w-4 h-4 text-red-400" />
                       {formatearFecha(pedido.fechaLimite)}
                     </div>
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
