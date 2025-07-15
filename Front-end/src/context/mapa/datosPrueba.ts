import type { CamionMapa } from '../mapa/types';

export const generarCamionesConRuta = (): CamionMapa[] => {
  const rutas = [
    // Ruta 1: Movimiento en L desde esquina superior izquierda
    [
      { x: 10, y: 10 }, { x: 15, y: 10 }, { x: 20, y: 10 }, { x: 25, y: 10 },
      { x: 25, y: 15 }, { x: 25, y: 20 }, { x: 25, y: 25 }, { x: 25, y: 30 },
      { x: 30, y: 30 }, { x: 35, y: 30 }, { x: 40, y: 30 }
    ],
    // Ruta 2: Movimiento zigzag horizontal-vertical
    [
      { x: 50, y: 10 }, { x: 45, y: 10 }, { x: 40, y: 10 }, { x: 35, y: 10 },
      { x: 35, y: 15 }, { x: 35, y: 20 }, { x: 30, y: 20 }, { x: 25, y: 20 },
      { x: 25, y: 25 }, { x: 25, y: 30 }, { x: 20, y: 30 }
    ],
    // Ruta 3: Movimiento rectangular
    [
      { x: 15, y: 35 }, { x: 20, y: 35 }, { x: 25, y: 35 }, { x: 30, y: 35 },
      { x: 30, y: 30 }, { x: 30, y: 25 }, { x: 35, y: 25 }, { x: 40, y: 25 },
      { x: 40, y: 30 }, { x: 40, y: 35 }, { x: 45, y: 35 }
    ]
  ];

  const colores = ['#3b82f6', '#ef4444', '#10b981'];

  return rutas.map((ruta, index) => ({
    id: `camion-${index + 1}`,
    codigo: `C${index + 1}`,
    color: colores[index],
    ruta,
    posicion: ruta[0],
    rotacion: 0,
    destino: ruta[ruta.length - 1],
    pedidosAsignados: [],
    estado: 'EN_RUTA',
    progreso: 0,
    nodoActual: 0,
    posicionInterpolada: ruta[0],
    enMovimiento: true,
    tiempoInicioMovimiento: 0,
  }));
};
