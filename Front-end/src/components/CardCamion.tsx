import { useSimulacion } from '../context/SimulacionContext';
import Camion from '../assets/camion.svg';

const CardsCamiones: React.FC = () => {
  const { camiones, rutasCamiones } = useSimulacion();

  const camionesConPedidos = camiones.filter(camion => {
    const ruta = rutasCamiones.find(r => r.id === camion.id);
    return ruta && ruta.pedidos.length > 0;
  });

  return (
    <>
      {camionesConPedidos.map((camion) => {
        const ruta = rutasCamiones.find(r => r.id === camion.id);
        if (!ruta) return null;

        const rutaLength = ruta.ruta.length;
        // Evitar división por cero, y calcular porcentaje real de progreso (0-100)
        const porcentajeReal = rutaLength > 1 ? (camion.porcentaje / (rutaLength - 1)) * 100 : 100;
        // Limitar porcentaje a 100%
        const porcentajeLimitado = Math.min(Math.max(porcentajeReal, 0), 100);

        return (
          <div key={camion.id} className="flex flex-col w-full rounded-2xl bg-[#FAFAFA]">
            <div className="inline-flex justify-start items-center pt-2 px-4">
              <img src={Camion} alt="Camion" className="w-6 h-6" />
              <span className="font-medium ml-2">{camion.id}</span>
            </div>
            <div className="flex items-center gap-2 px-4 pb-4">
              <div className="w-full h-3 bg-gray-200 rounded-full">
                <div
                  className="h-3 bg-blue-500 rounded-full transition-all duration-300"
                  style={{ width: `${porcentajeLimitado}%` }}
                ></div>
              </div>
              <span className="text-sm font-medium text-gray-700 w-10 text-right">{porcentajeLimitado.toFixed(0)}%</span>
            </div>
          </div>
        );
      })}
    </>
  );
};

export default CardsCamiones;
