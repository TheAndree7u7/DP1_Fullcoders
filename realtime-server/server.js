import { WebSocketServer } from 'ws';

const wss = new WebSocketServer({ port: 8080 });
console.log('Servidor WebSocket corriendo en ws://localhost:8080');

// Camiones simulados
const camiones = [
  { id: 'camion-1', posicion: { x: 5, y: 5 } },
  { id: 'camion-2', posicion: { x: 10, y: 10 } }
];

// Simula movimiento aleatorio
function moverCamiones() {
  for (const camion of camiones) {
    const dx = Math.floor(Math.random() * 3) - 1; // -1, 0, 1
    const dy = Math.floor(Math.random() * 3) - 1;
    camion.posicion.x = Math.max(0, Math.min(69, camion.posicion.x + dx));
    camion.posicion.y = Math.max(0, Math.min(49, camion.posicion.y + dy));
  }
}

setInterval(() => {
  moverCamiones();

  const data = camiones.map(c => ({
    id: c.id,
    posicion: c.posicion
  }));

  const message = JSON.stringify(data);

  wss.clients.forEach(client => {
    if (client.readyState === 1) {
      client.send(message);
    }
  });
}, 1000);
