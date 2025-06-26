// components/TablaPedidos.tsx
import { useState } from 'react';
import { ChevronUp, ChevronDown, ChevronsUpDown } from 'lucide-react';
import { useSimulacion } from '../context/SimulacionContext';
import type { CamionEstado } from '../context/SimulacionContext';

type SortDirection = 'asc' | 'desc' | null;
type SortColumn = 'numero' | 'cantidad' | 'ubicacion' | 'estado' | null;

const TablaPedidos: React.FC = () => {
  const { camiones } = useSimulacion();
  const [sortColumn, setSortColumn] = useState<SortColumn>(null);
  const [sortDirection, setSortDirection] = useState<SortDirection>(null);

  const headers = [
    { key: 'numero', label: 'Número' },
    { key: 'cantidad', label: 'Cantidad' },
    { key: 'ubicacion', label: 'Ubicación' },
    { key: 'estado', label: 'Estado' }
  ];

  // Función para obtener el valor de ordenamiento
  const getSortValue = (camion: CamionEstado, column: string) => {
    switch (column) {
      case 'numero':
        return camion.id;
      case 'cantidad':
        return camion.capacidadActualGLP;
      case 'ubicacion':
        return camion.ubicacion;
      case 'estado':
        return camion.estado;
      default:
        return '';
    }
  };

  // Función para manejar el ordenamiento
  const handleSort = (column: SortColumn) => {
    let newDirection: SortDirection = 'asc';
    
    if (sortColumn === column) {
      if (sortDirection === 'asc') {
        newDirection = 'desc';
      } else if (sortDirection === 'desc') {
        newDirection = null;
      } else {
        newDirection = 'asc';
      }
    }
    
    setSortColumn(newDirection ? column : null);
    setSortDirection(newDirection);
  };

  // Datos ordenados
  const sortedCamiones = [...camiones].sort((a, b) => {
    if (!sortColumn || !sortDirection) return 0;
    
    const aValue = getSortValue(a, sortColumn);
    const bValue = getSortValue(b, sortColumn);
    
    let comparison = 0;
    
    if (sortColumn === 'cantidad') {
      // Ordenamiento numérico para cantidad
      comparison = Number(aValue) - Number(bValue);
    } else {
      // Ordenamiento alfabético para texto
      comparison = String(aValue).localeCompare(String(bValue));
    }
    
    return sortDirection === 'asc' ? comparison : -comparison;
  });

  const data = sortedCamiones.map((camion) => [
    camion.id,
    camion.capacidadActualGLP,
    camion.ubicacion,
    camion.estado,
  ]);

  // Función para renderizar el icono de ordenamiento
  const renderSortIcon = (columnKey: string) => {
    if (sortColumn !== columnKey) {
      return <ChevronsUpDown size={14} className="text-gray-400 hover:text-gray-600" />;
    }
    
    if (sortDirection === 'asc') {
      return <ChevronUp size={14} className="text-blue-600" />;
    } else {
      return <ChevronDown size={14} className="text-blue-600" />;
    }
  };

  return (
    <div className="rounded-lg border border-gray-200 max-h-64">
      {/* Cabecera fija */}
      <div className="bg-gray-100 sticky top-0 z-10">
        <table className="min-w-full table-auto text-sm text-left">
          <thead>
            <tr>
              {headers.map((header) => (
                <th key={header.key} className="px-4 py-2 font-semibold text-gray-700">
                  <button
                    onClick={() => handleSort(header.key as SortColumn)}
                    className="flex items-center gap-1 hover:text-blue-600 transition-colors w-full text-left"
                    title={`Ordenar por ${header.label}`}
                  >
                    <span>{header.label}</span>
                    {renderSortIcon(header.key)}
                  </button>
                </th>
              ))}
            </tr>
          </thead>
        </table>
      </div>
      
      {/* Cuerpo de la tabla con scroll */}
      <div className="overflow-y-auto max-h-[calc(100%-36px)]">
        <table className="min-w-full table-auto text-sm text-left">
          <tbody>
            {data.map((fila, filaIndex) => (
              <tr key={filaIndex} className="border-t border-gray-100 hover:bg-gray-50">
                {fila.map((celda, celdaIndex) => (
                  <td key={celdaIndex} className="px-4 py-2 text-gray-700">
                    {celda}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default TablaPedidos;
