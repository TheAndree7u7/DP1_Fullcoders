// components/TablaPedidos.tsx
import { useSimulacion } from '../context/SimulacionContext';

const TablaPedidos: React.FC = () => {
  const { camiones } = useSimulacion();
  const headers = ["Número", "Cantidad", "Ubicación", "Estado"];
  const data = camiones.map((camion) => [
    camion.id,
    Math.floor(Math.random() * 10 + 1).toString(),
    camion.ubicacion,
    camion.estado,
  ]);

  return (
    <div className="rounded-lg border border-gray-200 max-h-80">
      {/* Cabecera fija */}
      <div className="bg-gray-100 sticky top-0 z-10">
        <table className="min-w-full table-auto text-sm text-left">
          <thead>
            <tr>
              {headers.map((header, index) => (
                <th key={index} className="px-4 py-2 font-semibold text-gray-700">
                  {header}
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
