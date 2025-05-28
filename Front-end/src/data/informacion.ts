// Datos estáticos de rutas de camiones y sus pedidos asignados
export const rutasCamiones = [
  {
    id: 'TA01',
    ruta: ['(1,1)', '(1,2)', '(1,3)', '(1,4)', '(1,5)'],
    puntoDestino: '(1,5)',
    pedido: 'PED001',
  },
  {
    id: 'TA02',
    ruta: ['(5,5)', '(6,5)', '(7,5)', '(8,5)', '(9,5)', '(10,5)'],
    puntoDestino: '(10,5)',
    pedido: 'PED002',
  },
  {
    id: 'TA03',
    ruta: ['(10,10)', '(10,9)', '(10,8)', '(10,7)', '(10,6)', '(10,5)', '(10,4)'],
    puntoDestino: '(10,4)',
    pedido: 'PED003',
  },
  {
    id: 'TA04',
    ruta: ['(20,20)', '(20,21)', '(20,22)', '(20,23)', '(20,24)', '(20,25)', '(20,26)', '(20,27)', '(20,28)', '(20,29)', '(20,30)'],
    puntoDestino: '(20,30)',
    pedido: 'PED004',
  },
];

// Estado de los camiones por cada instante (1 nodo por hora)
export const estadosPorInstante = [
  // Hora 1
  {
    timestamp: 1,
    camiones: [
      { id: 'TA01', ubicacion: '(1,1)', porcentaje: 0, estado: 'En Camino' },
      { id: 'TA02', ubicacion: '(5,5)', porcentaje: 0, estado: 'En Camino' },
      { id: 'TA03', ubicacion: '(10,10)', porcentaje: 0, estado: 'En Camino' },
      { id: 'TA04', ubicacion: '(20,20)', porcentaje: 0, estado: 'En Camino' },
    ],
  },
  // Hora 2
  {
    timestamp: 2,
    camiones: [
      { id: 'TA01', ubicacion: '(1,2)', porcentaje: 25, estado: 'En Camino' },
      { id: 'TA02', ubicacion: '(6,5)', porcentaje: 16, estado: 'En Camino' },
      { id: 'TA03', ubicacion: '(10,9)', porcentaje: 16, estado: 'En Camino' },
      { id: 'TA04', ubicacion: '(20,21)', porcentaje: 9, estado: 'En Camino' },
    ],
  },
  // Hora 3
  {
    timestamp: 3,
    camiones: [
      { id: 'TA01', ubicacion: '(1,3)', porcentaje: 50, estado: 'En Camino' },
      { id: 'TA02', ubicacion: '(7,5)', porcentaje: 33, estado: 'En Camino' },
      { id: 'TA03', ubicacion: '(10,8)', porcentaje: 33, estado: 'En Camino' },
      { id: 'TA04', ubicacion: '(20,22)', porcentaje: 18, estado: 'En Camino' },
    ],
  },
  // Hora 4
  {
    timestamp: 4,
    camiones: [
      { id: 'TA01', ubicacion: '(1,4)', porcentaje: 75, estado: 'En Camino' },
      { id: 'TA02', ubicacion: '(8,5)', porcentaje: 50, estado: 'En Camino' },
      { id: 'TA03', ubicacion: '(10,7)', porcentaje: 50, estado: 'En Camino' },
      { id: 'TA04', ubicacion: '(20,23)', porcentaje: 27, estado: 'En Camino' },
    ],
  },
  // Hora 5
  {
    timestamp: 5,
    camiones: [
      { id: 'TA01', ubicacion: '(1,5)', porcentaje: 100, estado: 'Entregado' },
      { id: 'TA02', ubicacion: '(9,5)', porcentaje: 66, estado: 'En Camino' },
      { id: 'TA03', ubicacion: '(10,6)', porcentaje: 66, estado: 'En Camino' },
      { id: 'TA04', ubicacion: '(20,24)', porcentaje: 36, estado: 'En Camino' },
    ],
  },
  // Hora 6
  {
    timestamp: 6,
    camiones: [
      { id: 'TA02', ubicacion: '(10,5)', porcentaje: 83, estado: 'En Camino' },
      { id: 'TA03', ubicacion: '(10,5)', porcentaje: 83, estado: 'En Camino' },
      { id: 'TA04', ubicacion: '(20,25)', porcentaje: 45, estado: 'En Camino' },
    ],
  },
  // Hora 7
  {
    timestamp: 7,
    camiones: [
      { id: 'TA03', ubicacion: '(10,4)', porcentaje: 100, estado: 'Entregado' },
      { id: 'TA04', ubicacion: '(20,26)', porcentaje: 54, estado: 'En Camino' },
    ],
  },
  // Hora 8
  {
    timestamp: 8,
    camiones: [
      { id: 'TA04', ubicacion: '(20,27)', porcentaje: 63, estado: 'En Camino' },
    ],
  },
  // Hora 9
  {
    timestamp: 9,
    camiones: [
      { id: 'TA04', ubicacion: '(20,28)', porcentaje: 72, estado: 'En Camino' },
    ],
  },
  // Hora 10
  {
    timestamp: 10,
    camiones: [
      { id: 'TA04', ubicacion: '(20,29)', porcentaje: 81, estado: 'En Camino' },
    ],
  },
  // Hora 11
  {
    timestamp: 11,
    camiones: [
      { id: 'TA04', ubicacion: '(20,30)', porcentaje: 100, estado: 'Entregado' },
    ],
  },
];
