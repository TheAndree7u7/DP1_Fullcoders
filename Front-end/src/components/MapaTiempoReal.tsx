import { useEffect, useRef, useState } from 'react';
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


const calcularRotacion = (prev: Coordenada, next: Coordenada): number => {
  const dx = next.x - prev.x;
  const dy = next.y - prev.y;
  if (dx === 1) return 0;
  if (dx === -1) return 180;
  if (dy === 1) return 90;
  if (dy === -1) return 270;
  return 0;
};

const colors = [
  '#ef4444', '#3b82f6', '#10b981', '#f59e0b', '#8b5cf6',
  '#ec4899', '#22d3ee', '#a3e635', '#eab308', '#f43f5e',
  '#06b6d4', '#84cc16', '#e879f9', '#4ade80', '#f97316',
  '#c084fc', '#2dd4bf', '#fde047', '#facc15', '#7dd3fc'
];

const MapaTiempoReal = () => {
  const [camionesVisuales, setCamionesVisuales] = useState<CamionVisual[]>([]);
  const socketRef = useRef<WebSocket | null>(null);

  useEffect(() => {
    socketRef.current = new WebSocket('ws://localhost:8080');

    socketRef.current.onopen = () => {
      console.log('Conectado al WebSocket');
    };

    socketRef.current.onmessage = (event) => {
      const data: { id: string; posicion: { x: number; y: number } }[] = JSON.parse(event.data);

      setCamionesVisuales(prev => {
        const actualizados = data.map((nuevoCamion) => {
          const existente = prev.find(c => c.id === nuevoCamion.id);
          const rotacion = existente
            ? calcularRotacion(existente.posicion, nuevoCamion.posicion)
            : 0;
          const color =
            existente?.color ??
            colors[Math.abs(nuevoCamion.id.length) % colors.length];

          return {
            id: nuevoCamion.id,
            posicion: nuevoCamion.posicion,
            rotacion,
            color,
            ruta: existente?.ruta ?? [],
          };
        });

        return actualizados;
      });
    };

    socketRef.current.onclose = () => {
      console.log('Conexión WebSocket cerrada');
    };

    return () => {
      socketRef.current?.close();
    };
  }, []);

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
            points={camion.ruta.map(p => `${p.x * CELL_SIZE},${p.y * CELL_SIZE}`).join(' ')}
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
    </div>
  );
};

export default MapaTiempoReal;
