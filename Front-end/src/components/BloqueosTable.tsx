import React, { useState } from "react";
import { useSimulacion } from "../hooks/useSimulacionContext";
import type { Coordenada } from "../types";



const BloqueosTable: React.FC = () => {
  const { bloqueos } = useSimulacion();
  const [expandedIndex, setExpandedIndex] = useState<number | null>(null);

  const handleExpand = (idx: number) => {
    setExpandedIndex(expandedIndex === idx ? null : idx);
  };

  return (
    <div className="rounded-lg border border-gray-200 max-h-96 overflow-y-auto">
      <table className="min-w-full table-auto text-sm text-left">
        <thead className="bg-gray-100 sticky top-0 z-10">
          <tr>
            <th className="px-4 py-2 font-semibold text-gray-700">#</th>
            <th className="px-4 py-2 font-semibold text-gray-700">Fecha inicio</th>
            <th className="px-4 py-2 font-semibold text-gray-700">Fecha fin</th>
            <th className="px-4 py-2 font-semibold text-gray-700">Nodos bloqueados</th>
          </tr>
        </thead>
        <tbody>
          {bloqueos.length === 0 && (
            <tr>
              <td colSpan={4} className="px-4 py-2 text-gray-500 text-center">No hay bloqueos</td>
            </tr>
          )}
          {bloqueos.map((bloqueo, idx) => (
            <React.Fragment key={idx}>
              <tr
                className="border-t border-gray-100 hover:bg-gray-50 cursor-pointer"
                onClick={() => handleExpand(idx)}
              >
                <td className="px-4 py-2">{idx + 1}</td>
                <td className="px-4 py-2">{new Date(bloqueo.fechaInicio).toLocaleString()}</td>
                <td className="px-4 py-2">{new Date(bloqueo.fechaFin).toLocaleString()}</td>
                <td className="px-4 py-2">{bloqueo.coordenadas.length} nodos</td>
              </tr>
              {expandedIndex === idx && (
                <tr className="bg-gray-50">
                  <td colSpan={4} className="px-4 py-2">
                    <div className="flex flex-wrap gap-2">
                      {bloqueo.coordenadas.map((coord: Coordenada, i: number) => (
                        <span
                          key={i}
                          className="inline-block bg-blue-100 text-blue-800 rounded px-2 py-1 text-xs font-mono"
                        >
                          ({coord.x},{coord.y})
                        </span>
                      ))}
                    </div>
                  </td>
                </tr>
              )}
            </React.Fragment>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default BloqueosTable;
