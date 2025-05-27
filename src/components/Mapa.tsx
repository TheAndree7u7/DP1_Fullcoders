import React, { useEffect, useRef, useState } from 'react';
import { informacion } from '../data/informacion';

const GRID_WIDTH = 70;
const GRID_HEIGHT = 50;
const CELL_SIZE = 14;
const SVG_WIDTH = GRID_WIDTH * CELL_SIZE;
const SVG_HEIGHT = GRID_HEIGHT * CELL_SIZE;

type Coord = { x: number; y: number };

type SimCamion = {
  id: string;
  color: string;
  ruta: Coord[];
  indiceRuta: number;
  posicion: Coord;
  rotacion: number;
};

const parseCoord = (s: string): Coord => {
  const match = s.match(/\((\d+),\s*(\d+)\)/);
  if (!match) throw new Error(`Coordenada inválida: ${s}`);
  return { x: parseInt(match[1]), y: parseInt(match[2]) };
};

const colors = ['#ef4444', '#3b82f6', '#10b981', '#f59e0b']; // rojo, azul, verde, amarillo

const calcularRotacion = (prev: Coord, next: Coord): number => {
  const dx = next.x - prev.x;
  const dy = next.y - prev.y;
  if (dx === 1) return 0;
  if (dx === -1) return 180;
  if (dy === 1) return 90;
  if (dy === -1) return 270;
  return 0;
};

const Mapa: React.FC = () => {
  const [camiones, setCamiones] = useState<SimCamion[]>([]);
  const [running, setRunning] = useState(false);
  const [intervalo, setIntervalo] = useState(1000);
  const intervalRef = useRef<NodeJS.Timeout | null>(null);

  useEffect(() => {
    const iniciales: SimCamion[] = informacion.map((info, idx) => {
      const ruta = info.ruta.map(parseCoord);
      return {
        id: info.id,
        color: colors[idx % colors.length],
        ruta,
        indiceRuta: 0,
        posicion: ruta[0],
        rotacion: 0,
      };
    });
    setCamiones(iniciales);
  }, []);

  useEffect(() => {
    if (running) {
      intervalRef.current = setInterval(() => {
        setCamiones(prev =>
          prev.map(camion => {
            if (camion.indiceRuta < camion.ruta.length - 1) {
              const nextIndex = camion.indiceRuta + 1;
              const nextCoord = camion.ruta[nextIndex];
              const angle = calcularRotacion(camion.posicion, nextCoord);
              return {
                ...camion,
                indiceRuta: nextIndex,
                posicion: nextCoord,
                rotacion: angle,
              };
            }
            return camion;
          })
        );
      }, intervalo);
    } else {
      if (intervalRef.current) clearInterval(intervalRef.current);
    }
    return () => {
      if (intervalRef.current) clearInterval(intervalRef.current);
    };
  }, [running, intervalo]);

  return (
    <div className="flex flex-col items-center gap-2">
      <svg
        width={SVG_WIDTH}
        height={SVG_HEIGHT}
        className="border border-gray-500 bg-white rounded-xl"
      >
        {/* Grid */}
        {[...Array(GRID_WIDTH + 1)].map((_, i) => (
          <line key={`v-${i}`} x1={i * CELL_SIZE} y1={0} x2={i * CELL_SIZE} y2={SVG_HEIGHT} stroke="#d1d5db" strokeWidth={1} />
        ))}
        {[...Array(GRID_HEIGHT + 1)].map((_, i) => (
          <line key={`h-${i}`} x1={0} y1={i * CELL_SIZE} x2={SVG_WIDTH} y2={i * CELL_SIZE} stroke="#d1d5db" strokeWidth={1} />
        ))}

        {/* Rutas */}
        {camiones.map(camion => (
          <polyline
            key={`ruta-${camion.id}`}
            fill="none"
            stroke={camion.color}
            strokeWidth={2}
            strokeDasharray="4 2"
            points={camion.ruta.map(p => `${p.x * CELL_SIZE},${p.y * CELL_SIZE}`).join(' ')}
          />
        ))}

        {/* Camiones */}
        {camiones.map(camion => {
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
