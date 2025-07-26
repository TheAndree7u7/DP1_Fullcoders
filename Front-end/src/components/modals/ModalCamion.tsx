import React from 'react';
import { formatearCapacidadGLP, formatearCombustible, calcularGLPEntregaPorCamion } from '../../utils/validacionCamiones';
import { colorSemaforoGLP } from '../mapa/utils';

interface ModalCamionProps {
  clickedCamion: string | null;
  camiones: any[];
  rutasCamiones: any[];
  averiando: string | null;
  onClose: () => void;
  onAveriar: (
    camionId: string,
    tipoAveria: number,
    marcarCamionAveriado: any,
    setAveriando: any,
    setClickedCamion: any,
    setSimulacionActiva: any,
    datosSimulacion: any,
    setPollingActivo: any,
    aplicarNuevaSolucionDespuesAveria: any
  ) => void;
  marcarCamionAveriado: any;
  setAveriando: any;
  setClickedCamion: any;
  setSimulacionActiva: any;
  datosSimulacion: any;
  setPollingActivo: any;
  aplicarNuevaSolucionDespuesAveria: any;
}

const ModalCamion: React.FC<ModalCamionProps> = ({
  clickedCamion,
  camiones,
  rutasCamiones,
  averiando,
  onClose,
  onAveriar,
  marcarCamionAveriado,
  setAveriando,
  setClickedCamion,
  setSimulacionActiva,
  datosSimulacion,
  setPollingActivo,
  aplicarNuevaSolucionDespuesAveria
}) => {
  if (!clickedCamion) return null;

  const camion = camiones.find((c) => c.id === clickedCamion);
  const ruta = rutasCamiones.find((r) => r.id === clickedCamion);
  const numPedidos = ruta?.pedidos?.length || 0;
  const esAveriado = camion?.estado === "Averiado";
  const glpEntrega = calcularGLPEntregaPorCamion(
    clickedCamion,
    rutasCamiones,
    camiones
  );

  // Calcular porcentajes para colores
  const porcentajeGLP = camion?.capacidadMaximaGLP > 0 
    ? (camion.capacidadActualGLP / camion.capacidadMaximaGLP) * 100 
    : 0;
  
  const porcentajeCombustible = camion?.combustibleMaximo > 0 
    ? (camion.combustibleActual / camion.combustibleMaximo) * 100 
    : 0;

  // Obtener colores según la leyenda
  const colorGLP = colorSemaforoGLP(porcentajeGLP);
  const colorCombustible = colorSemaforoGLP(porcentajeCombustible);

  return (
    <div
      className="fixed top-1/2 right-0 transform -translate-y-1/2 bg-white border-l border-gray-300 rounded-l-lg shadow-2xl z-50 w-96 max-w-full p-6 flex flex-col"
      style={{ minHeight: "400px", maxHeight: "90vh" }}
    >
      {/* Header con título y botón cerrar */}
      <div className="flex items-center justify-between mb-4">
        <div className="font-bold text-lg">Camión: {clickedCamion}</div>
        <button
          className="text-gray-500 hover:text-black text-2xl font-bold"
          onClick={onClose}
          title="Cerrar"
        >
          ×
        </button>
      </div>

      {camion && (
        <div className="text-sm mb-4 space-y-2">
          {/* Estado */}
          <div>
            <span className="font-medium text-gray-700">Estado: </span>
            <span className="font-bold">{camion.estado}</span>
          </div>

          {/* Pedidos asignados */}
          <div>
            <span className="font-medium text-gray-700">Pedidos asignados: </span>
            <span className="font-bold text-blue-600">{numPedidos}</span>
          </div>

          {/* Nombres de pedidos */}
          {ruta?.pedidos && ruta.pedidos.length > 0 && (
            <div>
              <span className="font-medium text-gray-700">Nombres pedidos: </span>
              <span className="font-bold text-purple-600">
                {ruta.pedidos.map(p => p.codigo).join(', ')}
              </span>
            </div>
          )}

          {/* Capacidad GLP con color */}
          <div>
            <span className="font-medium text-gray-700">Capacidad GLP: </span>
            <span 
              className="font-bold"
              style={{ color: colorGLP }}
            >
              {formatearCapacidadGLP(
                camion.capacidadActualGLP,
                camion.capacidadMaximaGLP
              )}
            </span>
          </div>

          {/* GLP a entregar */}
          <div>
            <span className="font-medium text-gray-700">GLP a entregar: </span>
            <span className="font-bold text-orange-600">
              {glpEntrega.toFixed(2)} m³
            </span>
          </div>

          {/* Combustible con color */}
          <div>
            <span className="font-medium text-gray-700">Combustible: </span>
            <span 
              className="font-bold"
              style={{ color: colorCombustible }}
            >
              {formatearCombustible(
                camion.combustibleActual,
                camion.combustibleMaximo
              )}
            </span>
          </div>

          {/* Distancia máxima */}
          <div>
            <span className="font-medium text-gray-700">Distancia máxima: </span>
            <span className="font-bold text-gray-800">
              {camion.distanciaMaxima.toFixed(2)} km
            </span>
          </div>

          {/* Peso carga */}
          <div>
            <span className="font-medium text-gray-700">Peso carga: </span>
            <span className="font-bold text-gray-800">
              {camion.pesoCarga.toFixed(2)}
            </span>
          </div>

          {/* Peso combinado */}
          <div>
            <span className="font-medium text-gray-700">Peso combinado: </span>
            <span className="font-bold text-gray-800">
              {camion.pesoCombinado.toFixed(2)}
            </span>
          </div>

          {/* Tara */}
          <div>
            <span className="font-medium text-gray-700">Tara: </span>
            <span className="font-bold text-gray-800">{camion.tara}</span>
          </div>

          {/* Tipo */}
          <div>
            <span className="font-medium text-gray-700">Tipo: </span>
            <span className="font-bold text-gray-800">{camion.tipo}</span>
          </div>

          {/* Ubicación */}
          <div>
            <span className="font-medium text-gray-700">Ubicación: </span>
            <span className="font-bold text-gray-800">{camion.ubicacion}</span>
          </div>

          {/* Progreso */}
          <div>
            <span className="font-medium text-gray-700">Progreso: </span>
            <span className="font-bold text-gray-800">{camion.porcentaje}</span>
          </div>
        </div>
      )}

      {/* Botones de avería */}
      {esAveriado ? (
        <div className="text-red-600 font-bold text-center py-2">
          🚛💥 CAMIÓN AVERIADO
        </div>
      ) : (
        <div className="flex flex-col gap-2">
          <button
            className="bg-yellow-500 hover:bg-yellow-600 text-white px-3 py-1 rounded disabled:opacity-50"
            disabled={averiando === clickedCamion + "-1"}
            onClick={() =>
              onAveriar(
                clickedCamion,
                1,
                marcarCamionAveriado,
                setAveriando,
                setClickedCamion,
                setSimulacionActiva,
                datosSimulacion,
                setPollingActivo,
                aplicarNuevaSolucionDespuesAveria
              )
            }
          >
            {averiando === clickedCamion + "-1"
              ? "Averiando..."
              : "Avería tipo 1"}
          </button>
          <button
            className="bg-orange-500 hover:bg-orange-600 text-white px-3 py-1 rounded disabled:opacity-50"
            disabled={averiando === clickedCamion + "-2"}
            onClick={() =>
              onAveriar(
                clickedCamion,
                2,
                marcarCamionAveriado,
                setAveriando,
                setClickedCamion,
                setSimulacionActiva,
                datosSimulacion,
                setPollingActivo,
                aplicarNuevaSolucionDespuesAveria
              )
            }
          >
            {averiando === clickedCamion + "-2"
              ? "Averiando..."
              : "Avería tipo 2"}
          </button>
          <button
            className="bg-red-500 hover:bg-red-600 text-white px-3 py-1 rounded disabled:opacity-50"
            disabled={averiando === clickedCamion + "-3"}
            onClick={() =>
              onAveriar(
                clickedCamion,
                3,
                marcarCamionAveriado,
                setAveriando,
                setClickedCamion,
                setSimulacionActiva,
                datosSimulacion,
                setPollingActivo,
                aplicarNuevaSolucionDespuesAveria
              )
            }
          >
            {averiando === clickedCamion + "-3"
              ? "Averiando..."
              : "Avería tipo 3"}
          </button>
        </div>
      )}

      {/* Botón mostrar ruta */}
      <button
        className="mt-4 bg-blue-500 hover:bg-blue-600 text-white px-3 py-1 rounded w-full"
        onClick={() => {
          // Aquí se activa el menú inferior para mostrar la ruta
          const event = new CustomEvent("mostrarRutaCamion", {
            detail: { camionId: clickedCamion },
          });
          window.dispatchEvent(event);
          onClose();
        }}
      >
        📍 Mostrar ruta del camión
      </button>
    </div>
  );
};

export default ModalCamion; 