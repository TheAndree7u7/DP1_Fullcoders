// Módulo para manejar la simulación en tiempo real

import { dibujarMapa } from './mapa.js';
import { actualizarPanelInformacion, agregarEventoAlHistorial, mostrarNotificacion } from './ui.js';
import { mostrarAnimacionEntrega, mostrarAnimacionRecarga } from './animaciones.js';

// Variable para almacenar el timestamp de la última actualización de cada camión
const ultimaActualizacion = new Map();
// Variable para almacenar el último nodo visitado por cada camión
const ultimoNodo = new Map();
// Variable para almacenar el índice del nodo actual en la ruta para cada camión
const indiceNodoActual = new Map();
// Variable para almacenar el historial de nodos recorridos por camión
const historicoNodos = new Map();

let stompClient = null;

// Iniciar simulación en tiempo real
export function iniciarSimulacion() {
    // Llamar a la API para iniciar la simulación
    fetch('/api/simulacion/iniciar-tiempo-real', {
        method: 'POST'
    })
    .then(response => response.json())
    .then(data => {
        console.log('Simulación iniciada:', data);
        
        // Actualizar estado y UI
        window.app.simulacionEnCurso = true;
        document.getElementById('btn-iniciar-simulacion').disabled = true;
        document.getElementById('btn-detener-simulacion').disabled = false;
        
        // Resetear variables de tracking
        ultimaActualizacion.clear();
        ultimoNodo.clear();
        indiceNodoActual.clear();
        historicoNodos.clear();
        
        // Conectar al WebSocket para recibir actualizaciones en tiempo real
        conectarWebSocket();
        
        // Mostrar mensaje de éxito
        mostrarNotificacion('Simulación iniciada correctamente', 'success');
    })
    .catch(error => {
        console.error('Error iniciando simulación:', error);
        mostrarNotificacion('Error al iniciar simulación', 'error');
    });
}

// Detener simulación en tiempo real
export function detenerSimulacion() {
    // Llamar a la API para detener la simulación
    fetch('/api/simulacion/detener-tiempo-real', {
        method: 'POST'
    })
    .then(response => response.json())
    .then(data => {
        console.log('Simulación detenida:', data);
        
        // Actualizar estado y UI
        window.app.simulacionEnCurso = false;
        document.getElementById('btn-iniciar-simulacion').disabled = false;
        document.getElementById('btn-detener-simulacion').disabled = true;
        
        // Desconectar del WebSocket
        desconectarWebSocket();
        
        // Mostrar mensaje de éxito
        mostrarNotificacion('Simulación detenida correctamente', 'success');
    })
    .catch(error => {
        console.error('Error deteniendo simulación:', error);
        mostrarNotificacion('Error al detener simulación', 'error');
    });
}

// Cambiar velocidad de la simulación
export function cambiarVelocidadSimulacion(factor) {
    // Validación básica del factor
    if (factor < 1 || factor > 10 || isNaN(factor)) {
        mostrarNotificacion('Factor de velocidad inválido. Debe ser un número entre 1 y 10.', 'warning');
        return;
    }
    
    // Llamar a la API para ajustar la velocidad
    fetch(`/api/simulacion/ajustar-velocidad?factor=${factor}`, {
        method: 'POST'
    })
    .then(response => response.json())
    .then(data => {
        console.log('Velocidad ajustada:', data);
        
        // Mostrar mensaje de éxito
        mostrarNotificacion(`Velocidad de simulación ajustada a ${factor}x`, 'info');
    })
    .catch(error => {
        console.error('Error ajustando velocidad:', error);
        mostrarNotificacion('Error al ajustar velocidad', 'error');
    });
}

// Conectar al WebSocket para recibir actualizaciones en tiempo real
function conectarWebSocket() {
    const socket = new SockJS('/websocket-app');
    stompClient = Stomp.over(socket);
    
    stompClient.connect({}, frame => {
        console.log('Conectado al WebSocket:', frame);
        
        // Suscribirse al tema de actualizaciones de simulación
        stompClient.subscribe('/topic/simulacion', message => {
            const actualizacion = JSON.parse(message.body);
            procesarActualizacionSimulacion(actualizacion);
        });
        
        // Suscribirse al tema de actualizaciones de camiones
        stompClient.subscribe('/topic/camiones', message => {
            const actualizacion = JSON.parse(message.body);
            procesarActualizacionCamion(actualizacion);
        });
        
        // Suscribirse al tema de eventos
        stompClient.subscribe('/topic/eventos', message => {
            const evento = JSON.parse(message.body);
            procesarEvento(evento);
        });
        
        // Suscribirse al tema de estadísticas
        stompClient.subscribe('/topic/estadisticas', message => {
            const estadisticas = JSON.parse(message.body);
            actualizarEstadisticas(estadisticas);
        });
    }, error => {
        console.error('Error de conexión WebSocket:', error);
        mostrarNotificacion('Error de conexión con el servidor de simulación', 'error');
    });
}

// Desconectar del WebSocket
function desconectarWebSocket() {
    if (stompClient !== null) {
        stompClient.disconnect();
        stompClient = null;
        console.log('Desconectado del WebSocket');
    }
}

// Procesar las actualizaciones de la simulación
function procesarActualizacionSimulacion(actualizacion) {
    console.log('Actualización de simulación recibida:', actualizacion);
    
    // Si es una notificación de inicio o fin, no hay que hacer más
    if (actualizacion.accion === 'iniciada' || actualizacion.accion === 'detenida') {
        return;
    }
    
    // Actualizar información general
    if (actualizacion.estadisticas) {
        actualizarEstadisticas(actualizacion.estadisticas);
    }
}

// Procesar las actualizaciones de un camión específico
function procesarActualizacionCamion(actualizacion) {
    console.log('Actualización de camión recibida:', actualizacion);
    
    const camionId = actualizacion.camionId;
    const posicionActual = {
        x: actualizacion.posX,
        y: actualizacion.posY
    };
    
    // Registrar última actualización para este camión
    ultimaActualizacion.set(camionId, Date.now());
    
    // Si es la primera actualización, inicializar el histórico
    if (!historicoNodos.has(camionId)) {
        historicoNodos.set(camionId, [posicionActual]);
        ultimoNodo.set(camionId, posicionActual);
    } else {
        const ultimaPosicion = ultimoNodo.get(camionId);
        
        // Si la posición cambió, actualizar
        if (ultimaPosicion.x !== posicionActual.x || ultimaPosicion.y !== posicionActual.y) {
            historicoNodos.get(camionId).push(posicionActual);
            ultimoNodo.set(camionId, posicionActual);
            
            // Actualizar índice del nodo actual si corresponde
            if (actualizacion.indiceNodoActual !== undefined) {
                indiceNodoActual.set(camionId, actualizacion.indiceNodoActual);
            }
            
            // Actualizar mapa con la nueva posición del camión
            dibujarMapa();
        }
    }
    
    // Si hay evento de entrega, mostrar animación
    if (actualizacion.evento === 'entrega') {
        mostrarAnimacionEntrega(posicionActual.x, posicionActual.y);
        agregarEventoAlHistorial(`Camión ${actualizacion.camionCodigo} entregó pedido ${actualizacion.pedidoCodigo}`);
    }
    
    // Si hay evento de recarga, mostrar animación
    if (actualizacion.evento === 'recarga') {
        mostrarAnimacionRecarga(posicionActual.x, posicionActual.y);
        agregarEventoAlHistorial(`Camión ${actualizacion.camionCodigo} recargó en almacén`);
    }
    
    // Actualizar panel de información con datos del camión
    actualizarPanelInformacion({
        camion: {
            id: camionId,
            codigo: actualizacion.camionCodigo,
            posicion: posicionActual,
            combustible: actualizacion.combustibleActual,
            carga: actualizacion.cargaActual,
            estado: actualizacion.estado
        }
    });
}

// Procesar eventos especiales (bloqueos, averías, etc.)
function procesarEvento(evento) {
    console.log('Evento recibido:', evento);
    
    // Mostrar notificación según tipo de evento
    let mensaje = '';
    let tipo = 'info';
    
    switch (evento.tipo) {
        case 'bloqueo':
            mensaje = `Nuevo bloqueo detectado en (${evento.posX}, ${evento.posY}): ${evento.descripcion}`;
            tipo = 'warning';
            break;
        case 'averia':
            mensaje = `Avería en camión ${evento.camionCodigo}: ${evento.descripcion}`;
            tipo = 'error';
            break;
        case 'mantenimiento':
            mensaje = `Mantenimiento programado para camión ${evento.camionCodigo}`;
            tipo = 'info';
            break;
        case 'pedido_entregado':
            mensaje = `Pedido ${evento.pedidoCodigo} entregado exitosamente`;
            tipo = 'success';
            break;
    }
    
    // Mostrar la notificación
    if (mensaje) {
        mostrarNotificacion(mensaje, tipo);
        agregarEventoAlHistorial(mensaje);
    }
    
    // Actualizar mapa si es necesario (bloqueos, nuevos pedidos, etc.)
    if (['bloqueo', 'pedido_nuevo'].includes(evento.tipo)) {
        dibujarMapa();
    }
}

// Actualizar estadísticas generales
function actualizarEstadisticas(estadisticas) {
    console.log('Estadísticas recibidas:', estadisticas);
    
    // Actualizar panel de información con las estadísticas
    actualizarPanelInformacion({
        estadisticas: {
            camionesTotal: estadisticas.camionesTotal,
            camionesEnRuta: estadisticas.camionesEnRuta,
            pedidosTotal: estadisticas.pedidosTotal,
            pedidosPendientes: estadisticas.pedidosPendientes,
            pedidosEnRuta: estadisticas.pedidosEnRuta,
            pedidosEntregados: estadisticas.pedidosEntregados,
            timestamp: estadisticas.timestamp
        }
    });
}

// Exportar funciones y variables útiles para debugging
export const debug = {
    getUltimaActualizacion: () => ultimaActualizacion,
    getUltimoNodo: () => ultimoNodo,
    getHistoricoNodos: () => historicoNodos,
    getIndiceNodoActual: () => indiceNodoActual
};
