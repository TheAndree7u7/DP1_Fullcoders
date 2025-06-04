import { useEffect, useRef, useState } from 'react';
import { useSimulacion } from '../context/SimulacionContext';
import type { Coordenada } from '../types';

interface CamionVisual {
  id: string;
  color: string;
  ruta: Coordenada[];
  posicion: Coordenada;
  rotacion: number;
}

const GRID_WIDTH = 70;
const GRID_HEIGHT = 50;
const CELL_SIZE = 14;
const SVG_WIDTH = GRID_WIDTH * CELL_SIZE;
const SVG_HEIGHT = GRID_HEIGHT * CELL_SIZE;

const parseCoord = (s: string): Coordenada => {
  const match = s.match(/\((\d+),\s*(\d+)\)/);
  if (!match) throw new Error(`Coordenada inválida: ${s}`);
  return { x: parseInt(match[1]), y: parseInt(match[2]) };
};

const colors = [
  '#ef4444', '#3b82f6', '#10b981', '#f59e0b', '#8b5cf6',
  '#ec4899', '#22d3ee', '#a3e635', '#eab308', '#f43f5e',
  '#06b6d4', '#84cc16', '#e879f9', '#4ade80', '#f97316',
  '#c084fc', '#2dd4bf', '#fde047', '#facc15', '#7dd3fc'
];

const calcularRotacion = (prev: Coordenada, next: Coordenada): number => {
  const dx = next.x - prev.x;
  const dy = next.y - prev.y;
  if (dx === 1) return 0;
  if (dx === -1) return 180;
  if (dy === 1) return 90;
  if (dy === -1) return 270;
  return 0;
};

const Mapa = () => {
  const [camionesVisuales, setCamionesVisuales] = useState<CamionVisual[]>([]);
  const [running, setRunning] = useState(false);
  const [intervalo, setIntervalo] = useState(1000);
  const intervalRef = useRef<number | null>(null);
  const { camiones, rutasCamiones, avanzarHora, cargando } = useSimulacion();

  useEffect(() => {
    const iniciales = rutasCamiones.map((info, idx) => {
      const ruta = info.ruta.map(parseCoord);
      return {
        id: info.id,
        color: colors[idx % colors.length],
        ruta,
        posicion: ruta[0],
        rotacion: 0,
      };
    });
    setCamionesVisuales(iniciales);
  }, [rutasCamiones]);

  useEffect(() => {
    if (running) {
      intervalRef.current = window.setInterval(() => {
        avanzarHora();
      }, intervalo);
    } else {
      if (intervalRef.current) window.clearInterval(intervalRef.current);
    }
    return () => {
      if (intervalRef.current) window.clearInterval(intervalRef.current);
    };
  }, [running, intervalo, avanzarHora]);

  useEffect(() => {
    setCamionesVisuales((prevCamiones) =>
      prevCamiones.map(camion => {
        const nuevo = camiones.find(c => c.id === camion.id);
        if (!nuevo) return camion;
        const nuevaCoord = parseCoord(nuevo.ubicacion);
        const rot = calcularRotacion(camion.posicion, nuevaCoord);

        // Encontrar el índice de la posición actual en la ruta
        const rutaActual = camion.ruta;
        const posicionActual = nuevaCoord;
        const indiceActual = rutaActual.findIndex(
          (punto: Coordenada) => punto.x === posicionActual.x && punto.y === posicionActual.y
        );

        // Filtrar la ruta para mostrar solo los puntos que faltan por recorrer
        const rutaRestante = rutaActual.slice(indiceActual);

        return {
          ...camion,
          posicion: nuevaCoord,
          rotacion: rot,
          ruta: rutaRestante
        };
      })
    );
  }, [camiones]);

  if (cargando) {
    return <p>Cargando simulación...</p>;
  }

  return (
    <div className="flex flex-col items-center gap-2">
      <svg
        width={SVG_WIDTH}
        height={SVG_HEIGHT}
        className="border border-gray-500 bg-white rounded-xl"
      >
        {[...Array(GRID_WIDTH + 1)].map((_, i) => (
          <line key={`v-${i}`} x1={i * CELL_SIZE} y1={0} x2={i * CELL_SIZE} y2={SVG_HEIGHT} stroke="#d1d5db" strokeWidth={1} />
        ))}
        {[...Array(GRID_HEIGHT + 1)].map((_, i) => (
          <line key={`h-${i}`} x1={0} y1={i * CELL_SIZE} x2={SVG_WIDTH} y2={i * CELL_SIZE} stroke="#d1d5db" strokeWidth={1} />
        ))}

        {camionesVisuales.map(camion => (
          <polyline
            key={`ruta-${camion.id}`}
            fill="none"
            stroke={camion.color}
            strokeWidth={2}
            strokeDasharray="4 2"
            points={camion.ruta.map((p: Coordenada) => `${p.x * CELL_SIZE},${p.y * CELL_SIZE}`).join(' ')}
          />
        ))}

        {camionesVisuales.map(camion => {
          const { posicion, rotacion, color } = camion;
          const cx = posicion.x * CELL_SIZE;
          const cy = posicion.y * CELL_SIZE;
          return (
            <g
              key={camion.id}
              transform={`translate(${cx}, ${cy}) rotate(${rotacion})`}
              style={{ transition: 'transform 0.8s linear' }}
            >
              <rect x={-6} y={-4} width={12} height={8} rx={2} fill={color} stroke="black" strokeWidth={0.5} />
              <circle cx={-4} cy={5} r={1.5} fill="black" />
              <circle cx={4} cy={5} r={1.5} fill="black" />
            </g>
          );
        })}
      </svg>

      <div className="flex items-center gap-4 mt-2">
        <button
          onClick={() => setRunning(prev => !prev)}
          className="bg-blue-500 hover:bg-blue-600 text-white px-4 py-1 rounded"
        >
          {running ? 'Pausar' : 'Iniciar'}
        </button>
        <label className="flex items-center gap-1 text-sm">
          Velocidad:
          <input
            type="number"
            min={100}
            step={100}
            value={intervalo}
            onChange={e => setIntervalo(parseInt(e.target.value))}
            className="border rounded px-2 py-0.5 w-20"
          />
          ms
        </label>
      </div>
    </div>
  );
};

export default Mapa;
