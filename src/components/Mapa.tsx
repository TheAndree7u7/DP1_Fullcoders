import React, { useEffect, useState } from 'react';

type WarehouseType = 'central' | 'intermediate';

type Truck = {
  id: string;
  x: number; // vértice x
  y: number; // vértice y
  color: string;
};

const GRID_WIDTH = 70;
const GRID_HEIGHT = 50;
const CELL_SIZE = 16; // px
const SVG_WIDTH = GRID_WIDTH * CELL_SIZE;
const SVG_HEIGHT = GRID_HEIGHT * CELL_SIZE;

const warehouseVertices: { x: number; y: number; type: WarehouseType }[] = [
  { x: 5, y: 5, type: 'central' },
  { x: 60, y: 40, type: 'intermediate' },
];

const Mapa: React.FC = () => {
  const [truck, setTruck] = useState<Truck>({ id: 'TA01', x: 10, y: 10, color: 'red' });

  const moveTruck = (dx: number, dy: number) => {
    const newX = truck.x + dx;
    const newY = truck.y + dy;
    if (newX >= 0 && newX <= GRID_WIDTH && newY >= 0 && newY <= GRID_HEIGHT) {
      setTruck(prev => ({ ...prev, x: newX, y: newY }));
    }
  };

  useEffect(() => {
    const handleKey = (e: KeyboardEvent) => {
      if (e.key === 'ArrowUp') moveTruck(0, -1);
      if (e.key === 'ArrowDown') moveTruck(0, 1);
      if (e.key === 'ArrowLeft') moveTruck(-1, 0);
      if (e.key === 'ArrowRight') moveTruck(1, 0);
    };
    window.addEventListener('keydown', handleKey);
    return () => window.removeEventListener('keydown', handleKey);
  }, [truck]);

  return (
    <div className="flex flex-col items-center p-4 bg-white rounded-xl">
      <svg
        width={SVG_WIDTH}
        height={SVG_HEIGHT}
        className="border border-gray-500 bg-white"
      >
        {/* Líneas de grilla (calles) */}
        {[...Array(GRID_WIDTH + 1)].map((_, i) => (
          <line
            key={`v-${i}`}
            x1={i * CELL_SIZE}
            y1={0}
            x2={i * CELL_SIZE}
            y2={SVG_HEIGHT}
            stroke="#d1d5db"
            strokeWidth={1}
          />
        ))}
        {[...Array(GRID_HEIGHT + 1)].map((_, i) => (
          <line
            key={`h-${i}`}
            x1={0}
            y1={i * CELL_SIZE}
            x2={SVG_WIDTH}
            y2={i * CELL_SIZE}
            stroke="#d1d5db"
            strokeWidth={1}
          />
        ))}

        {/* Almacenes */}
        {warehouseVertices.map((w, index) => (
          <rect
            key={index}
            x={w.x * CELL_SIZE - 4}
            y={w.y * CELL_SIZE - 4}
            width={8}
            height={8}
            fill={w.type === 'central' ? '#1e3a8a' : '#0ea5e9'}
          />
        ))}

        {/* Camión */}
        <circle
          cx={truck.x * CELL_SIZE}
          cy={truck.y * CELL_SIZE}
          r={5}
          fill={truck.color}
        />
      </svg>
    </div>
  );
};

export default Mapa;
