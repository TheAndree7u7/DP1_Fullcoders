// SimulacionSemanal.tsx
import React, { useRef, useEffect, useState } from 'react';

type Direction = 'up' | 'down' | 'left' | 'right';

type Cell = {
  x: number;
  y: number;
  streets: Direction[]; // conexiones disponibles
  isBlocked?: boolean;
  warehouse?: 'central' | 'intermediate';
};

type Truck = {
  id: string;
  x: number;
  y: number;
  color: string;
};

const GRID_SIZE = 50;
const CELL_SIZE = 16; // px
const CANVAS_SIZE = GRID_SIZE * CELL_SIZE;

const createEmptyMap = (): Cell[][] => {
  const grid: Cell[][] = [];
  for (let y = 0; y < GRID_SIZE; y++) {
    const row: Cell[] = [];
    for (let x = 0; x < GRID_SIZE; x++) {
      row.push({ x, y, streets: [] });
    }
    grid.push(row);
  }

  // Ejemplo de calles (puedes cargar desde JSON luego)
  grid[10][10].streets = ['right', 'down'];
  grid[10][11].streets = ['left', 'down'];
  grid[11][11].streets = ['up', 'down'];
  grid[12][11].streets = ['up'];

  // Almacén central
  grid[5][5].warehouse = 'central';
  // Almacén intermedio
  grid[45][45].warehouse = 'intermediate';

  return grid;
};

const SimulacionSemanal: React.FC = () => {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const [map, setMap] = useState<Cell[][]>(createEmptyMap());
  const [truck, setTruck] = useState<Truck>({ id: 'TA01', x: 10, y: 10, color: 'red' });

  const draw = () => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    ctx.clearRect(0, 0, CANVAS_SIZE, CANVAS_SIZE);

    // Dibujar grilla y calles
    for (let y = 0; y < GRID_SIZE; y++) {
      for (let x = 0; x < GRID_SIZE; x++) {
        const cell = map[y][x];
        const startX = x * CELL_SIZE;
        const startY = y * CELL_SIZE;

        // Calles
        ctx.strokeStyle = cell.isBlocked ? 'red' : '#d1d5db';
        ctx.strokeRect(startX, startY, CELL_SIZE, CELL_SIZE);

        ctx.strokeStyle = '#60a5fa'; // azul claro
        ctx.lineWidth = 2;
        cell.streets.forEach(dir => {
          ctx.beginPath();
          const cx = startX + CELL_SIZE / 2;
          const cy = startY + CELL_SIZE / 2;
          ctx.moveTo(cx, cy);
          switch (dir) {
            case 'up': ctx.lineTo(cx, startY); break;
            case 'down': ctx.lineTo(cx, startY + CELL_SIZE); break;
            case 'left': ctx.lineTo(startX, cy); break;
            case 'right': ctx.lineTo(startX + CELL_SIZE, cy); break;
          }
          ctx.stroke();
        });

        // Almacenes
        if (cell.warehouse) {
          ctx.fillStyle = cell.warehouse === 'central' ? '#1e3a8a' : '#0ea5e9';
          ctx.fillRect(startX + 4, startY + 4, CELL_SIZE - 8, CELL_SIZE - 8);
        }
      }
    }

    // Dibujar camión
    ctx.fillStyle = truck.color;
    ctx.beginPath();
    ctx.arc(
      truck.x * CELL_SIZE + CELL_SIZE / 2,
      truck.y * CELL_SIZE + CELL_SIZE / 2,
      CELL_SIZE / 3,
      0,
      Math.PI * 2
    );
    ctx.fill();
  };

  const moveTruck = (dx: number, dy: number) => {
    const current = map[truck.y][truck.x];
    const targetX = truck.x + dx;
    const targetY = truck.y + dy;

    if (targetX < 0 || targetX >= GRID_SIZE || targetY < 0 || targetY >= GRID_SIZE) return;

    const direction =
      dx === 1 ? 'right' :
        dx === -1 ? 'left' :
          dy === 1 ? 'down' :
            dy === -1 ? 'up' : null;

    const reverseDir: Record<Direction, Direction> = {
      up: 'down', down: 'up', left: 'right', right: 'left',
    };

    if (
      direction &&
      current.streets.includes(direction) &&
      map[targetY][targetX].streets.includes(reverseDir[direction])
    ) {
      setTruck(prev => ({ ...prev, x: targetX, y: targetY }));
    }
  };

  useEffect(() => {
    draw();
  }, [truck, map]);

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
    <div className="flex flex-col items-center p-4">
      <h1 className="text-xl font-bold mb-2">Simulación Semanal</h1>
      <canvas
        ref={canvasRef}
        width={CANVAS_SIZE}
        height={CANVAS_SIZE}
        className="border border-gray-500"
      />
    </div>
  );
};

export default SimulacionSemanal;
