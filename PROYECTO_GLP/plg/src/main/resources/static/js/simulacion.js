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
export function cambiarVelocidad() {
    const nuevaVelocidad = parseInt(document.getElementById('velocidad-simulacion').value);
    
    // Validar que sea un número entre 1 y 10
    if (isNaN(nuevaVelocidad) || nuevaVelocidad < 1 || nuevaVelocidad > 10) {
        mostrarNotificacion('La velocidad debe ser un número entre 1 y 10', 'warning');
        return;
    }
    
    // Llamar a la API para ajustar velocidad
    fetch(`/api/simulacion/ajustar-velocidad?factor=${nuevaVelocidad}`, {
        method: 'POST'
    })
    .then(response => response.json())
    .then(data => {
        console.log('Velocidad ajustada:', data);
        window.app.velocidadSimulacion = data.factorVelocidad || nuevaVelocidad;
        
        // Mostrar mensaje de éxito
        mostrarNotificacion('Velocidad ajustada correctamente', 'success');
    })
    .catch(error => {
        console.error('Error ajustando velocidad:', error);
        mostrarNotificacion('Error al ajustar velocidad', 'error');
    });
}

// Conectar al WebSocket para recibir actualizaciones en tiempo real
export function conectarWebSocket() {
    // Si ya hay una conexión, desconectar primero
    if (window.app.stompClient !== null) {
        desconectarWebSocket();
    }
    
    // Crear nueva conexión
    const socket = new SockJS('/ws');
    window.app.stompClient = Stomp.over(socket);
    
    window.app.stompClient.connect({}, function(frame) {
        console.log('Conectado a WebSocket: ' + frame);
        
        // Suscribirse a actualizaciones de posiciones
        window.app.stompClient.subscribe('/topic/posiciones', function(message) {
            const data = JSON.parse(message.body);
            actualizarPosiciones(data);
        });
        
        // Suscribirse a notificaciones de entregas
        window.app.stompClient.subscribe('/topic/entregas', function(message) {
            const data = JSON.parse(message.body);
            procesarEntrega(data);
        });
        
        // Suscribirse a notificaciones de rutas
        window.app.stompClient.subscribe('/topic/rutas', function(message) {
            const data = JSON.parse(message.body);
            procesarEventoRuta(data);
        });
        
        // Suscribirse a notificaciones de llegadas a nodos
        window.app.stompClient.subscribe('/topic/nodos', function(message) {
            const data = JSON.parse(message.body);
            procesarLlegadaNodo(data);
        });
        
        // Suscribirse a notificaciones de recargas
        window.app.stompClient.subscribe('/topic/recargas', function(message) {
            const data = JSON.parse(message.body);
            procesarRecarga(data);
        });
    }, function(error) {
        console.error('Error conectando a WebSocket:', error);
        mostrarNotificacion('Error conectando a la simulación en tiempo real', 'error');
    });
}

// Desconectar del WebSocket
export function desconectarWebSocket() {
    if (window.app.stompClient !== null) {
        window.app.stompClient.disconnect();
        window.app.stompClient = null;
    }
}

// Actualizar posiciones de todos los elementos
export function actualizarPosiciones(data) {
    const ahora = Date.now();
    
    // Actualizar camiones
    if (data.camiones && Array.isArray(data.camiones)) {
        window.app.camiones = data.camiones.map(nuevoCamion => {
            // Inicializar el tracking de este camión si no existe
            if (!ultimoNodo.has(nuevoCamion.id)) {
                ultimoNodo.set(nuevoCamion.id, { posX: nuevoCamion.posX, posY: nuevoCamion.posY });
                ultimaActualizacion.set(nuevoCamion.id, ahora);
                indiceNodoActual.set(nuevoCamion.id, 0);
            }
            
            const posicionAnterior = ultimoNodo.get(nuevoCamion.id);
            
            // Verificar si realmente hubo un cambio de posición significativo
            const hayCambioReal = 
                Math.abs(nuevoCamion.posX - posicionAnterior.posX) > 0.01 || 
                Math.abs(nuevoCamion.posY - posicionAnterior.posY) > 0.01;
                
            // Solo actualizar si el cambio es real y diferente al anterior
            if (hayCambioReal) {
                ultimoNodo.set(nuevoCamion.id, { posX: nuevoCamion.posX, posY: nuevoCamion.posY });
                ultimaActualizacion.set(nuevoCamion.id, ahora);
                
                // Cuando hay un cambio real de posición, incrementamos el índice del nodo
                // pero solo si no viene especificado en el mensaje
                if (nuevoCamion.nodoActualIndex === undefined) {
                    const indiceActual = indiceNodoActual.get(nuevoCamion.id) || 0;
                    const nuevoIndice = indiceActual + 1;
                    indiceNodoActual.set(nuevoCamion.id, nuevoIndice);
                    nuevoCamion.nodoActualIndex = nuevoIndice;
                    
                    console.log(`Camión ${nuevoCamion.codigo || nuevoCamion.id} avanzó al nodo ${nuevoIndice}, posición: (${nuevoCamion.posX},${nuevoCamion.posY})`);
                } else {
                    // Si viene especificado, usamos ese valor pero comprobamos que sea válido
                    const indiceActual = indiceNodoActual.get(nuevoCamion.id) || 0;
                    // Solo aceptamos índices mayores para asegurar el avance
                    if (nuevoCamion.nodoActualIndex >= indiceActual) {
                        indiceNodoActual.set(nuevoCamion.id, nuevoCamion.nodoActualIndex);
                    } else {
                        nuevoCamion.nodoActualIndex = indiceActual;
                    }
                }
            } else {
                // Si no hay cambio real, mantenemos el último índice conocido
                nuevoCamion.nodoActualIndex = indiceNodoActual.get(nuevoCamion.id) || 0;
            }
            
            return nuevoCamion;
        });
    }
    
    // Actualizar almacenes
    if (data.almacenes && Array.isArray(data.almacenes)) {
        window.app.almacenes = data.almacenes;
    }
    
    // Actualizar pedidos
    if (data.pedidos && Array.isArray(data.pedidos)) {
        window.app.pedidos = data.pedidos;
    }
    
    // Actualizar rutas
    if (data.rutas && Array.isArray(data.rutas)) {
        window.app.rutas = data.rutas;
    }
    
    // Redibujar el mapa con las nuevas posiciones
    dibujarMapa();
    
    // Actualizar panel de información con los datos proporcionados por el servidor
    actualizarPanelInformacion({
        camiones: window.app.camiones ? window.app.camiones.length : 0,
        almacenes: window.app.almacenes ? window.app.almacenes.length : 0,
        pedidos: window.app.pedidos ? window.app.pedidos.length : 0,
        rutas: window.app.rutas ? window.app.rutas.length : 0,
        simulacionEnCurso: window.app.simulacionEnCurso,
        camionesEnRuta: data.camionesEnRuta || (window.app.camiones ? window.app.camiones.filter(c => c.estado === 1).length : 0),
        pedidosPendientes: data.pedidosPendientes || (window.app.pedidos ? window.app.pedidos.filter(p => p.estado === 0).length : 0),
        pedidosEnRuta: data.pedidosEnRuta || (window.app.pedidos ? window.app.pedidos.filter(p => p.estado === 1).length : 0),
        rutasActivas: data.rutasActivas || (window.app.rutas ? window.app.rutas.filter(r => r.estado === 1).length : 0)
    });
    
    // Actualizar contador de elementos en tiempo real con comprobación null
    const contadorCamiones = document.getElementById('contador-camiones');
    if (contadorCamiones) contadorCamiones.textContent = window.app.camiones ? window.app.camiones.length : 0;
    
    const contadorAlmacenes = document.getElementById('contador-almacenes');
    if (contadorAlmacenes) contadorAlmacenes.textContent = window.app.almacenes ? window.app.almacenes.length : 0;
    
    const contadorPedidos = document.getElementById('contador-pedidos');
    if (contadorPedidos) contadorPedidos.textContent = window.app.pedidos ? window.app.pedidos.length : 0;
    
    const contadorRutas = document.getElementById('contador-rutas');
    if (contadorRutas) contadorRutas.textContent = window.app.rutas ? window.app.rutas.length : 0;
}

// Procesar entrega de pedido
export function procesarEntrega(data) {
    console.log('Entrega realizada:', data);
    
    // Mostrar animación de entrega
    mostrarAnimacionEntrega(data.posX, data.posY);
    
    // Agregar a historial de eventos
    agregarEventoAlHistorial({
        tipo: 'Entrega',
        mensaje: `Camión ${data.camionCodigo} entregó pedido ${data.pedidoCodigo}`,
        fecha: new Date().toLocaleTimeString(),
        detalles: `Volumen: ${data.volumenEntregado.toFixed(2)} m³ (${data.porcentajeEntregado.toFixed(0)}%)`
    });
    
    // Mostrar notificación de entrega
    mostrarNotificacion(`Entrega realizada: Camión ${data.camionCodigo} - Pedido ${data.pedidoCodigo}`, 'info');
}

// Procesar evento de ruta (inicio, fin)
export function procesarEventoRuta(data) {
    console.log('Evento ruta:', data);
    
    let mensaje = '';
    
    switch(data.tipo) {
        case 'inicioRuta':
            mensaje = `Camión ${data.camionCodigo} inició ruta ${data.rutaCodigo}`;
            break;
        case 'finRutas':
            mensaje = `Camión ${data.camionCodigo} completó todas sus rutas`;
            break;
    }
    
    // Agregar a historial de eventos
    agregarEventoAlHistorial({
        tipo: 'Ruta',
        mensaje: mensaje,
        fecha: new Date().toLocaleTimeString()
    });
}

// Procesar llegada a un nodo de la ruta
export function procesarLlegadaNodo(data) {
    console.log('Llegada a nodo:', data);
    
    // Actualizar el índice de nodo del camión para evitar retrocesos
    const camion = window.app.camiones.find(c => c.id === data.camionId);
    if (camion) {
        // Asegurar avance consistente incrementando el nodo actual
        const indiceActual = indiceNodoActual.get(camion.id) || 0;
        const nuevoIndice = data.nodoIndex !== undefined ? data.nodoIndex : indiceActual + 1;
        
        // Solo actualizar si realmente avanza
        if (nuevoIndice > indiceActual) {
            indiceNodoActual.set(camion.id, nuevoIndice);
            camion.nodoActualIndex = nuevoIndice;
            
            // También actualizamos la posición
            if (data.posX !== undefined && data.posY !== undefined) {
                ultimoNodo.set(camion.id, { posX: data.posX, posY: data.posY });
                camion.posX = data.posX;
                camion.posY = data.posY;
            }
            
            console.log(`Camión ${data.camionCodigo} avanzó al nodo ${nuevoIndice}, posición: (${camion.posX},${camion.posY})`);
        }
    }
    
    // Solo mostrar notificación si es un nodo importante (cliente o almacén)
    if (data.nodoTipo === 'CLIENTE' || data.nodoTipo === 'ALMACEN') {
        let mensaje = `Camión ${data.camionCodigo} llegó a ${data.nodoTipo.toLowerCase()}`;
        
        if (data.pedidoCodigo) {
            mensaje += ` - Pedido ${data.pedidoCodigo}`;
        }
        
        // Agregar a historial de eventos
        agregarEventoAlHistorial({
            tipo: 'Llegada',
            mensaje: mensaje,
            fecha: new Date().toLocaleTimeString(),
            detalles: `Posición: (${data.posX}, ${data.posY})`
        });
    }
    
    // Redibujar el mapa después de actualizar la posición
    dibujarMapa();
}

// Procesar recarga de combustible y GLP
export function procesarRecarga(data) {
    console.log('Recarga realizada:', data);
    
    // Mostrar animación de recarga
    const camion = window.app.camiones.find(c => c.id === data.camionId);
    if (camion) {
        mostrarAnimacionRecarga(camion.posX, camion.posY);
    }
    
    // Agregar a historial de eventos
    agregarEventoAlHistorial({
        tipo: 'Recarga',
        mensaje: `Camión ${data.camionCodigo} recargó en ${data.almacenNombre}`,
        fecha: new Date().toLocaleTimeString(),
        detalles: `Combustible: ${data.combustibleRecargado.toFixed(2)} galones`
    });
}
