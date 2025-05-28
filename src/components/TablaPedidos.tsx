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
    <div className="overflow-auto rounded-lg border border-gray-200">
      <table className="min-w-full table-auto text-sm text-left">
        <thead className="bg-gray-100">
          <tr>
            {headers.map((header, index) => (
              <th key={index} className="px-4 py-2 font-semibold text-gray-700">
                {header}
              </th>
            ))}
          </tr>
        </thead>
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
  );
};

export default TablaPedidos;
