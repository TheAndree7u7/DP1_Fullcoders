// Variables globales para el estado de la aplicación
let stompClient = null;
let mapaReticular = [];
let camiones = [];
let almacenes = [];
let pedidos = [];
let rutas = [];
let simulacionEnCurso = false;
let velocidadSimulacion = 1;

// Tamaño de la celda para visualización en el mapa
const TAMANO_CELDA = 30;

// Colores para diferentes tipos de elementos
const COLORES = {
    CAMION: '#3498db',
    ALMACEN: '#2ecc71',
    PEDIDO_PENDIENTE: '#e74c3c',
    PEDIDO_EN_RUTA: '#f39c12',
    PEDIDO_ENTREGADO: '#27ae60',
    RUTA: '#9b59b6',
    BLOQUEO: '#95a5a6'
};

// Inicializar la aplicación cuando se carga la página
document.addEventListener('DOMContentLoaded', function() {
    inicializarUI();
    cargarDatosIniciales();
    
    // Establecer manejadores de eventos para los botones
    document.getElementById('btn-iniciar-simulacion').addEventListener('click', iniciarSimulacion);
    document.getElementById('btn-detener-simulacion').addEventListener('click', detenerSimulacion);
    document.getElementById('btn-ajustar-velocidad').addEventListener('click', cambiarVelocidad);
    document.getElementById('btn-generar-rutas').addEventListener('click', generarRutas);
});

// Inicializar la interfaz de usuario
function inicializarUI() {
    // Configurar el mapa reticular (canvas)
    const mapaCanvas = document.getElementById('mapa-canvas');
    const ctx = mapaCanvas.getContext('2d');
    
    // Configurar el tamaño del canvas según el contenedor
    const contenedorMapa = document.getElementById('contenedor-mapa');
    mapaCanvas.width = contenedorMapa.clientWidth;
    mapaCanvas.height = contenedorMapa.clientHeight;
    
    // Configurar listeners para cambio de tamaño de ventana
    window.addEventListener('resize', function() {
        mapaCanvas.width = contenedorMapa.clientWidth;
        mapaCanvas.height = contenedorMapa.clientHeight;
        dibujarMapa();
    });
    
    // Inicializar panel de información
    actualizarPanelInformacion({
        camiones: 0,
        almacenes: 0,
        pedidos: 0,
        rutas: 0,
        simulacionEnCurso: false
    });
}

// Cargar datos iniciales desde el servidor
function cargarDatosIniciales() {
    // Cargar datos del mapa reticular
    fetch('/api/mapa/configuracion')
        .then(response => response.json())
        .then(data => {
            mapaReticular = data;
            dibujarMapa();
        })
        .catch(error => console.error('Error cargando configuración del mapa:', error));
    
    // Cargar camiones
    fetch('/api/camiones')
        .then(response => response.json())
        .then(data => {
            camiones = data;
            dibujarMapa();
        })
        .catch(error => console.error('Error cargando camiones:', error));
    
    // Cargar almacenes
    fetch('/api/almacenes')
        .then(response => response.json())
        .then(data => {
            almacenes = data;
            dibujarMapa();
        })
        .catch(error => console.error('Error cargando almacenes:', error));
    
    // Cargar pedidos
    fetch('/api/pedidos')
        .then(response => response.json())
        .then(data => {
            pedidos = data;
            dibujarMapa();
        })
        .catch(error => console.error('Error cargando pedidos:', error));
        
    // Verificar estado de la simulación
    fetch('/api/simulacion/estado')
        .then(response => response.json())
        .then(data => {
            simulacionEnCurso = data.simulacionEnCurso;
            velocidadSimulacion = data.factorVelocidad || 1;
            
            // Actualizar UI según el estado
            document.getElementById('btn-iniciar-simulacion').disabled = simulacionEnCurso;
            document.getElementById('btn-detener-simulacion').disabled = !simulacionEnCurso;
            document.getElementById('velocidad-simulacion').value = velocidadSimulacion;
            
            // Si la simulación ya está en curso, conectar al WebSocket
            if (simulacionEnCurso) {
                conectarWebSocket();
            }
            
            actualizarPanelInformacion({
                camionesEnRuta: data.camionesEnRuta || 0,
                rutasActivas: data.rutasActivas || 0,
                pedidosPendientes: data.pedidosPendientes || 0,
                pedidosEnRuta: data.pedidosEnRuta || 0,
                simulacionEnCurso: simulacionEnCurso
            });
        })
        .catch(error => console.error('Error verificando estado de simulación:', error));
}

// Dibujar el mapa con todos los elementos
function dibujarMapa() {
    const mapaCanvas = document.getElementById('mapa-canvas');
    const ctx = mapaCanvas.getContext('2d');
    
    // Limpiar el canvas
    ctx.clearRect(0, 0, mapaCanvas.width, mapaCanvas.height);
    
    // Dibujar cuadrícula
    dibujarCuadricula(ctx);
    
    // Dibujar rutas
    dibujarRutas(ctx);
    
    // Dibujar almacenes
    dibujarAlmacenes(ctx);
    
    // Dibujar pedidos
    dibujarPedidos(ctx);
    
    // Dibujar camiones (último para que estén por encima)
    dibujarCamiones(ctx);
}

// Dibujar la cuadrícula base
function dibujarCuadricula(ctx) {
    const mapaCanvas = document.getElementById('mapa-canvas');
    const ancho = mapaCanvas.width;
    const alto = mapaCanvas.height;
    
    // Configurar estilo de línea para la cuadrícula
    ctx.strokeStyle = '#ddd';
    ctx.lineWidth = 0.5;
    
    // Dibujar líneas horizontales
    for (let y = 0; y <= alto; y += TAMANO_CELDA) {
        ctx.beginPath();
        ctx.moveTo(0, y);
        ctx.lineTo(ancho, y);
        ctx.stroke();
    }
    
    // Dibujar líneas verticales
    for (let x = 0; x <= ancho; x += TAMANO_CELDA) {
        ctx.beginPath();
        ctx.moveTo(x, 0);
        ctx.lineTo(x, alto);
        ctx.stroke();
    }
}

// Dibujar almacenes en el mapa
function dibujarAlmacenes(ctx) {
    almacenes.forEach(almacen => {
        // Calcular posición en el canvas
        const x = almacen.posX * TAMANO_CELDA;
        const y = almacen.posY * TAMANO_CELDA;
        
        // Dibujar almacén como un rectángulo verde
        ctx.fillStyle = COLORES.ALMACEN;
        ctx.fillRect(x, y, TAMANO_CELDA, TAMANO_CELDA);
        
        // Agregar contorno
        ctx.strokeStyle = '#27ae60';
        ctx.lineWidth = 2;
        ctx.strokeRect(x, y, TAMANO_CELDA, TAMANO_CELDA);
        
        // Agregar etiqueta
        ctx.fillStyle = '#fff';
        ctx.font = '10px Arial';
        ctx.fillText('A', x + TAMANO_CELDA/2 - 3, y + TAMANO_CELDA/2 + 3);
    });
}

// Dibujar pedidos en el mapa
function dibujarPedidos(ctx) {
    pedidos.forEach(pedido => {
        // Calcular posición en el canvas
        const x = pedido.posX * TAMANO_CELDA;
        const y = pedido.posY * TAMANO_CELDA;
        
        // Color según el estado del pedido
        let color;
        switch (pedido.estado) {
            case 0: // Pendiente
                color = COLORES.PEDIDO_PENDIENTE;
                break;
            case 1: // En ruta
                color = COLORES.PEDIDO_EN_RUTA;
                break;
            case 2: // Entregado
                color = COLORES.PEDIDO_ENTREGADO;
                break;
            default:
                color = '#999';
        }
        
        // Dibujar pedido como un círculo
        ctx.fillStyle = color;
        ctx.beginPath();
        ctx.arc(x + TAMANO_CELDA/2, y + TAMANO_CELDA/2, TAMANO_CELDA/3, 0, 2 * Math.PI);
        ctx.fill();
        
        // Agregar contorno
        ctx.strokeStyle = '#000';
        ctx.lineWidth = 1;
        ctx.stroke();
        
        // Mostrar volumen del pedido
        ctx.fillStyle = '#fff';
        ctx.font = '8px Arial';
        ctx.fillText(pedido.m3.toFixed(1), x + TAMANO_CELDA/2 - 6, y + TAMANO_CELDA/2 + 3);
    });
}

// Dibujar camiones en el mapa
function dibujarCamiones(ctx) {
    camiones.forEach(camion => {
        // Calcular posición en el canvas (pueden tener posiciones intermedias no enteras)
        const x = camion.posX * TAMANO_CELDA;
        const y = camion.posY * TAMANO_CELDA;
        
        // Color según el estado del camión
        let color;
        switch (camion.estado) {
            case 0: // Disponible
                color = '#3498db';
                break;
            case 1: // En ruta
                color = '#e67e22';
                break;
            case 2: // En mantenimiento
                color = '#f1c40f';
                break;
            case 3: // Averiado
                color = '#e74c3c';
                break;
            default:
                color = '#95a5a6';
        }
        
        // Dibujar camión como un triángulo
        ctx.fillStyle = color;
        ctx.beginPath();
        ctx.moveTo(x + TAMANO_CELDA/2, y);
        ctx.lineTo(x, y + TAMANO_CELDA);
        ctx.lineTo(x + TAMANO_CELDA, y + TAMANO_CELDA);
        ctx.closePath();
        ctx.fill();
        
        // Agregar contorno
        ctx.strokeStyle = '#2c3e50';
        ctx.lineWidth = 1;
        ctx.stroke();
        
        // Mostrar código del camión
        ctx.fillStyle = '#fff';
        ctx.font = '9px Arial';
        ctx.fillText(camion.codigo, x + TAMANO_CELDA/2 - 7, y + TAMANO_CELDA/2 + 5);
        
        // Si está en ruta, mostrar indicador de progreso
        if (camion.estado === 1 && camion.progresoRuta !== undefined) {
            // Barra de progreso debajo del camión
            const anchoTotal = TAMANO_CELDA;
            const anchoProgreso = (camion.progresoRuta / 100) * anchoTotal;
            
            ctx.fillStyle = '#2ecc71';
            ctx.fillRect(x, y + TAMANO_CELDA + 2, anchoProgreso, 3);
            
            ctx.strokeStyle = '#bdc3c7';
            ctx.strokeRect(x, y + TAMANO_CELDA + 2, anchoTotal, 3);
        }
    });
}

// Dibujar rutas en el mapa
function dibujarRutas(ctx) {
    rutas.forEach(ruta => {
        if (!ruta.nodos || ruta.nodos.length < 2) return;
        
        // Configurar estilo para la línea de ruta
        ctx.strokeStyle = COLORES.RUTA;
        ctx.lineWidth = 2;
        ctx.setLineDash([5, 3]); // Línea punteada
        
        // Dibujar línea que conecta los nodos
        ctx.beginPath();
        
        const primerNodo = ruta.nodos[0];
        ctx.moveTo(
            primerNodo.posX * TAMANO_CELDA + TAMANO_CELDA/2, 
            primerNodo.posY * TAMANO_CELDA + TAMANO_CELDA/2
        );
        
        for (let i = 1; i < ruta.nodos.length; i++) {
            const nodo = ruta.nodos[i];
            ctx.lineTo(
                nodo.posX * TAMANO_CELDA + TAMANO_CELDA/2, 
                nodo.posY * TAMANO_CELDA + TAMANO_CELDA/2
            );
        }
        
        ctx.stroke();
        ctx.setLineDash([]); // Restaurar línea continua
    });
}

// Actualizar el panel de información con estadísticas actuales
function actualizarPanelInformacion(data) {
    const panel = document.getElementById('panel-informacion');
    if (!panel) return;
    
    // Actualizar número de elementos
    if (data.camiones !== undefined) {
        document.getElementById('contador-camiones').textContent = data.camiones;
    }
    
    if (data.almacenes !== undefined) {
        document.getElementById('contador-almacenes').textContent = data.almacenes;
    }
    
    if (data.pedidos !== undefined) {
        document.getElementById('contador-pedidos').textContent = data.pedidos;
    }
    
    if (data.rutas !== undefined) {
        document.getElementById('contador-rutas').textContent = data.rutas;
    }
    
    // Actualizar estado de simulación
    if (data.simulacionEnCurso !== undefined) {
        const estadoSimulacion = document.getElementById('estado-simulacion');
        estadoSimulacion.textContent = data.simulacionEnCurso ? 'En curso' : 'Detenida';
        estadoSimulacion.className = data.simulacionEnCurso ? 'estado-activo' : 'estado-inactivo';
    }
    
    // Actualizar estadísticas adicionales si están disponibles
    if (data.camionesEnRuta !== undefined) {
        document.getElementById('camiones-en-ruta').textContent = data.camionesEnRuta;
    }
    
    if (data.rutasActivas !== undefined) {
        document.getElementById('rutas-activas').textContent = data.rutasActivas;
    }
    
    if (data.pedidosPendientes !== undefined) {
        document.getElementById('pedidos-pendientes').textContent = data.pedidosPendientes;
    }
    
    if (data.pedidosEnRuta !== undefined) {
        document.getElementById('pedidos-en-ruta').textContent = data.pedidosEnRuta;
    }
}

// Generar rutas utilizando el algoritmo de ruteo
function generarRutas() {
    // Mostrar indicador de carga
    document.getElementById('btn-generar-rutas').disabled = true;
    document.getElementById('indicador-carga').style.display = 'inline-block';
    
    // Llamar a la API para generar rutas
    fetch('/api/rutas/generar', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            metodo: 'algoritmoGenetico',
            numeroRutas: 3
        })
    })
    .then(response => response.json())
    .then(data => {
        console.log('Rutas generadas:', data);
        
        // Actualizar la UI con las nuevas rutas
        cargarRutasGeneradas();
        
        // Ocultar indicador de carga
        document.getElementById('btn-generar-rutas').disabled = false;
        document.getElementById('indicador-carga').style.display = 'none';
        
        // Mostrar mensaje de éxito
        mostrarNotificacion('Rutas generadas correctamente', 'success');
    })
    .catch(error => {
        console.error('Error generando rutas:', error);
        document.getElementById('btn-generar-rutas').disabled = false;
        document.getElementById('indicador-carga').style.display = 'none';
        mostrarNotificacion('Error al generar rutas', 'error');
    });
}

// Cargar las rutas generadas
function cargarRutasGeneradas() {
    fetch('/api/rutas')
        .then(response => response.json())
        .then(data => {
            rutas = data;
            dibujarMapa();
        })
        .catch(error => console.error('Error cargando rutas:', error));
}

// Iniciar simulación en tiempo real
function iniciarSimulacion() {
    // Llamar a la API para iniciar la simulación
    fetch('/api/simulacion/iniciar-tiempo-real', {
        method: 'POST'
    })
    .then(response => response.json())
    .then(data => {
        console.log('Simulación iniciada:', data);
        
        // Actualizar estado y UI
        simulacionEnCurso = true;
        document.getElementById('btn-iniciar-simulacion').disabled = true;
        document.getElementById('btn-detener-simulacion').disabled = false;
        
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
function detenerSimulacion() {
    // Llamar a la API para detener la simulación
    fetch('/api/simulacion/detener-tiempo-real', {
        method: 'POST'
    })
    .then(response => response.json())
    .then(data => {
        console.log('Simulación detenida:', data);
        
        // Actualizar estado y UI
        simulacionEnCurso = false;
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
function cambiarVelocidad() {
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
        velocidadSimulacion = data.factorVelocidad || nuevaVelocidad;
        
        // Mostrar mensaje de éxito
        mostrarNotificacion('Velocidad ajustada correctamente', 'success');
    })
    .catch(error => {
        console.error('Error ajustando velocidad:', error);
        mostrarNotificacion('Error al ajustar velocidad', 'error');
    });
}

// Conectar al WebSocket para recibir actualizaciones en tiempo real
function conectarWebSocket() {
    // Si ya hay una conexión, desconectar primero
    if (stompClient !== null) {
        desconectarWebSocket();
    }
    
    // Crear nueva conexión
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    
    stompClient.connect({}, function(frame) {
        console.log('Conectado a WebSocket: ' + frame);
        
        // Suscribirse a actualizaciones de posiciones
        stompClient.subscribe('/topic/posiciones', function(message) {
            const data = JSON.parse(message.body);
            actualizarPosiciones(data);
        });
        
        // Suscribirse a notificaciones de entregas
        stompClient.subscribe('/topic/entregas', function(message) {
            const data = JSON.parse(message.body);
            procesarEntrega(data);
        });
        
        // Suscribirse a notificaciones de rutas
        stompClient.subscribe('/topic/rutas', function(message) {
            const data = JSON.parse(message.body);
            procesarEventoRuta(data);
        });
        
        // Suscribirse a notificaciones de llegadas a nodos
        stompClient.subscribe('/topic/nodos', function(message) {
            const data = JSON.parse(message.body);
            procesarLlegadaNodo(data);
        });
        
        // Suscribirse a notificaciones de recargas
        stompClient.subscribe('/topic/recargas', function(message) {
            const data = JSON.parse(message.body);
            procesarRecarga(data);
        });
    }, function(error) {
        console.error('Error conectando a WebSocket:', error);
        mostrarNotificacion('Error conectando a la simulación en tiempo real', 'error');
    });
}

// Desconectar del WebSocket
function desconectarWebSocket() {
    if (stompClient !== null) {
        stompClient.disconnect();
        stompClient = null;
    }
}

// Actualizar posiciones de todos los elementos
function actualizarPosiciones(data) {
    // Actualizar camiones
    if (data.camiones) {
        camiones = data.camiones;
    }
    
    // Actualizar almacenes
    if (data.almacenes) {
        almacenes = data.almacenes;
    }
    
    // Actualizar pedidos
    if (data.pedidos) {
        pedidos = data.pedidos;
    }
    
    // Actualizar rutas
    if (data.rutas) {
        rutas = data.rutas;
    }
    
    // Redibujar el mapa con las nuevas posiciones
    dibujarMapa();
    
    // Actualizar panel de información
    actualizarPanelInformacion({
        camiones: camiones.length,
        almacenes: almacenes.length,
        pedidos: pedidos.length,
        rutas: rutas.length,
        simulacionEnCurso: simulacionEnCurso,
        camionesEnRuta: camiones.filter(c => c.estado === 1).length,
        pedidosPendientes: pedidos.filter(p => p.estado === 0).length,
        pedidosEnRuta: pedidos.filter(p => p.estado === 1).length,
        rutasActivas: rutas.filter(r => r.estado === 1).length
    });
}

// Procesar entrega de pedido
function procesarEntrega(data) {
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
function procesarEventoRuta(data) {
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
function procesarLlegadaNodo(data) {
    console.log('Llegada a nodo:', data);
    
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
}

// Procesar recarga de combustible y GLP
function procesarRecarga(data) {
    console.log('Recarga realizada:', data);
    
    // Mostrar animación de recarga
    const camion = camiones.find(c => c.id === data.camionId);
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

// Agregar evento al historial
function agregarEventoAlHistorial(evento) {
    const historial = document.getElementById('historial-eventos');
    if (!historial) return;
    
    // Crear elemento de historial
    const item = document.createElement('div');
    item.className = 'evento-historial';
    
    // Aplicar clase según tipo de evento
    switch(evento.tipo) {
        case 'Entrega':
            item.classList.add('evento-entrega');
            break;
        case 'Ruta':
            item.classList.add('evento-ruta');
            break;
        case 'Llegada':
            item.classList.add('evento-llegada');
            break;
        case 'Recarga':
            item.classList.add('evento-recarga');
            break;
    }
    
    // Crear contenido del evento
    item.innerHTML = `
        <div class="evento-cabecera">
            <span class="evento-tipo">${evento.tipo}</span>
            <span class="evento-hora">${evento.fecha}</span>
        </div>
        <div class="evento-mensaje">${evento.mensaje}</div>
        ${evento.detalles ? `<div class="evento-detalles">${evento.detalles}</div>` : ''}
    `;
    
    // Agregar al inicio del historial
    historial.insertBefore(item, historial.firstChild);
    
    // Limitar historial a los últimos 20 eventos
    while (historial.children.length > 20) {
        historial.removeChild(historial.lastChild);
    }
}

// Mostrar animación de entrega
function mostrarAnimacionEntrega(x, y) {
    // Convertir coordenadas de mapa a coordenadas de canvas
    const posX = x * TAMANO_CELDA + TAMANO_CELDA/2;
    const posY = y * TAMANO_CELDA + TAMANO_CELDA/2;
    
    // Crear elemento de animación
    const animacion = document.createElement('div');
    animacion.className = 'animacion-entrega';
    animacion.style.left = posX + 'px';
    animacion.style.top = posY + 'px';
    animacion.textContent = '✓';
    
    // Agregar al contenedor del mapa
    document.getElementById('contenedor-mapa').appendChild(animacion);
    
    // Eliminar después de la animación
    setTimeout(() => {
        animacion.remove();
    }, 2000);
}

// Mostrar animación de recarga
function mostrarAnimacionRecarga(x, y) {
    // Convertir coordenadas de mapa a coordenadas de canvas
    const posX = x * TAMANO_CELDA + TAMANO_CELDA/2;
    const posY = y * TAMANO_CELDA + TAMANO_CELDA/2;
    
    // Crear elemento de animación
    const animacion = document.createElement('div');
    animacion.className = 'animacion-recarga';
    animacion.style.left = posX + 'px';
    animacion.style.top = posY + 'px';
    animacion.textContent = '⛽';
    
    // Agregar al contenedor del mapa
    document.getElementById('contenedor-mapa').appendChild(animacion);
    
    // Eliminar después de la animación
    setTimeout(() => {
        animacion.remove();
    }, 2000);
}

// Mostrar notificación
function mostrarNotificacion(mensaje, tipo) {
    // Crear elemento de notificación
    const notificacion = document.createElement('div');
    notificacion.className = 'notificacion';
    notificacion.classList.add(`notificacion-${tipo}`);
    notificacion.textContent = mensaje;
    
    // Agregar al contenedor de notificaciones
    const contenedor = document.getElementById('contenedor-notificaciones');
    if (!contenedor) {
        // Si no existe el contenedor, crear uno
        const nuevoContenedor = document.createElement('div');
        nuevoContenedor.id = 'contenedor-notificaciones';
        document.body.appendChild(nuevoContenedor);
        nuevoContenedor.appendChild(notificacion);
    } else {
        contenedor.appendChild(notificacion);
    }
    
    // Eliminar después de 3 segundos
    setTimeout(() => {
        notificacion.classList.add('notificacion-salida');
        setTimeout(() => {
            notificacion.remove();
        }, 300);
    }, 3000);
}