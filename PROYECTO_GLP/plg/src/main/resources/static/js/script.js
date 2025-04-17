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

// Variables para el control de la vista del mapa
let escalaActual = 1.0;      // Escala actual (zoom)
let offsetX = 0;             // Desplazamiento horizontal
let offsetY = 0;             // Desplazamiento vertical
let arrastrando = false;     // Control si se está arrastrando el mapa
let ultimaX = 0;             // Última posición X para cálculo de arrastre
let ultimaY = 0;             // Última posición Y para cálculo de arrastre

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

// Modificar la función inicializarUI

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
    
    // Eventos para zoom y desplazamiento
    mapaCanvas.addEventListener('wheel', manejarZoom);
    mapaCanvas.addEventListener('mousedown', iniciarArrastre);
    mapaCanvas.addEventListener('mousemove', arrastrarMapa);
    mapaCanvas.addEventListener('mouseup', finalizarArrastre);
    mapaCanvas.addEventListener('mouseleave', finalizarArrastre);
    
    // Agregar controles de mapa al contenedor
    agregarControlesMapa(contenedorMapa);
    
    // Inicializar panel de información
    actualizarPanelInformacion({
        camiones: 0,
        almacenes: 0,
        pedidos: 0,
        rutas: 0,
        simulacionEnCurso: false
    });
}

// Agregar controles visuales para el mapa
function agregarControlesMapa(contenedor) {
    // Panel de controles
    const controlesDiv = document.createElement('div');
    controlesDiv.className = 'controles-mapa';
    
    // Botón zoom in
    const zoomInBtn = document.createElement('button');
    zoomInBtn.className = 'control-btn';
    zoomInBtn.innerHTML = '➕';
    zoomInBtn.title = 'Acercar';
    zoomInBtn.addEventListener('click', () => {
        cambiarZoom(0.1);
    });
    
    // Botón zoom out
    const zoomOutBtn = document.createElement('button');
    zoomOutBtn.className = 'control-btn';
    zoomOutBtn.innerHTML = '➖';
    zoomOutBtn.title = 'Alejar';
    zoomOutBtn.addEventListener('click', () => {
        cambiarZoom(-0.1);
    });
    
    // Botón reset vista
    const resetBtn = document.createElement('button');
    resetBtn.className = 'control-btn';
    resetBtn.innerHTML = '🔄';
    resetBtn.title = 'Restablecer vista';
    resetBtn.addEventListener('click', resetearVista);
    
    // Botón pantalla completa
    const fullscreenBtn = document.createElement('button');
    fullscreenBtn.className = 'control-btn';
    fullscreenBtn.innerHTML = '⛶';
    fullscreenBtn.title = 'Pantalla completa';
    fullscreenBtn.addEventListener('click', toggleFullscreen);
    
    // Añadir botones al panel
    controlesDiv.appendChild(zoomInBtn);
    controlesDiv.appendChild(zoomOutBtn);
    controlesDiv.appendChild(resetBtn);
    controlesDiv.appendChild(fullscreenBtn);
    
    // Añadir panel al contenedor
    contenedor.appendChild(controlesDiv);
}

// Manejar evento de rueda para zoom
function manejarZoom(event) {
    event.preventDefault();
    const delta = -Math.sign(event.deltaY) * 0.1;
    cambiarZoom(delta, event.offsetX, event.offsetY);
}

// Cambiar nivel de zoom
function cambiarZoom(delta, centerX, centerY) {
    // Obtener centro si no se proporciona
    const mapaCanvas = document.getElementById('mapa-canvas');
    centerX = centerX || mapaCanvas.width / 2;
    centerY = centerY || mapaCanvas.height / 2;
    
    // Calcular nueva escala con límites
    const escalaAnterior = escalaActual;
    escalaActual += delta;
    escalaActual = Math.max(0.5, Math.min(3.0, escalaActual)); // Limitar entre 0.5x y 3x
    
    // Ajustar offset para mantener el punto de zoom como centro
    if (escalaActual !== escalaAnterior) {
        const factor = escalaActual / escalaAnterior;
        offsetX = centerX - (centerX - offsetX) * factor;
        offsetY = centerY - (centerY - offsetY) * factor;
    }
    
    // Actualizar la visualización
    dibujarMapa();
}

// Iniciar arrastre del mapa
function iniciarArrastre(event) {
    arrastrando = true;
    ultimaX = event.clientX;
    ultimaY = event.clientY;
    document.getElementById('mapa-canvas').style.cursor = 'grabbing';
}

// Arrastrar el mapa
function arrastrarMapa(event) {
    if (!arrastrando) return;
    
    const deltaX = event.clientX - ultimaX;
    const deltaY = event.clientY - ultimaY;
    
    offsetX += deltaX;
    offsetY += deltaY;
    
    ultimaX = event.clientX;
    ultimaY = event.clientY;
    
    dibujarMapa();
}

// Finalizar arrastre
function finalizarArrastre() {
    arrastrando = false;
    document.getElementById('mapa-canvas').style.cursor = 'grab';
}

// Resetear la vista al estado inicial
function resetearVista() {
    escalaActual = 1.0;
    offsetX = 0;
    offsetY = 0;
    dibujarMapa();
}

// Alternar pantalla completa para el mapa
function toggleFullscreen() {
    const contenedorMapa = document.getElementById('contenedor-mapa');
    
    if (!document.fullscreenElement) {
        // Entrar a pantalla completa
        if (contenedorMapa.requestFullscreen) {
            contenedorMapa.requestFullscreen();
        } else if (contenedorMapa.mozRequestFullScreen) { /* Firefox */
            contenedorMapa.mozRequestFullScreen();
        } else if (contenedorMapa.webkitRequestFullscreen) { /* Chrome, Safari, Opera */
            contenedorMapa.webkitRequestFullscreen();
        } else if (contenedorMapa.msRequestFullscreen) { /* IE/Edge */
            contenedorMapa.msRequestFullscreen();
        }
    } else {
        // Salir de pantalla completa
        if (document.exitFullscreen) {
            document.exitFullscreen();
        } else if (document.mozCancelFullScreen) {
            document.mozCancelFullScreen();
        } else if (document.webkitExitFullscreen) {
            document.webkitExitFullscreen();
        } else if (document.msExitFullscreen) {
            document.msExitFullscreen();
        }
    }
}

// Cargar datos iniciales desde el servidor
function cargarDatosIniciales() {
    // Inicializar arrays vacíos para evitar errores
    almacenes = [];
    camiones = [];
    pedidos = [];
    rutas = [];

    // Cargar datos del mapa reticular
    fetch('/api/mapa/configuracion')
        .then(response => {
            if (!response.ok) {
                throw new Error(`Error HTTP: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            mapaReticular = data;
            dibujarMapa();
        })
        .catch(error => {
            console.error('Error cargando configuración del mapa:', error);
            // Si falla, usar un valor predeterminado básico
            mapaReticular = { 
                ancho: 50, 
                alto: 50, 
                tamano: 1 
            };
        });
    
    // Cargar camiones
    fetch('/api/camiones')
        .then(response => {
            if (!response.ok) {
                throw new Error(`Error HTTP: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            camiones = Array.isArray(data) ? data : [];
            dibujarMapa();
        })
        .catch(error => {
            console.error('Error cargando camiones:', error);
            camiones = [];
        });
    
    // Cargar almacenes
    fetch('/api/almacenes')
        .then(response => {
            if (!response.ok) {
                throw new Error(`Error HTTP: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            almacenes = Array.isArray(data) ? data : [];
            dibujarMapa();
        })
        .catch(error => {
            console.error('Error cargando almacenes:', error);
            almacenes = [];
        });
    
    // Cargar pedidos
    fetch('/api/pedidos')
        .then(response => {
            if (!response.ok) {
                throw new Error(`Error HTTP: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            pedidos = Array.isArray(data) ? data : [];
            dibujarMapa();
        })
        .catch(error => {
            console.error('Error cargando pedidos:', error);
            pedidos = [];
        });
        
    // Verificar estado de la simulación
    fetch('/api/simulacion/estado')
        .then(response => {
            if (!response.ok) {
                throw new Error(`Error HTTP: ${response.status}`);
            }
            return response.json();
        })
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
        .catch(error => {
            console.error('Error verificando estado de simulación:', error);
            simulacionEnCurso = false;
            document.getElementById('btn-iniciar-simulacion').disabled = false;
            document.getElementById('btn-detener-simulacion').disabled = true;
        });
}

// Dibujar el mapa con todos los elementos
function dibujarMapa() {
    const mapaCanvas = document.getElementById('mapa-canvas');
    const ctx = mapaCanvas.getContext('2d');
    
    // Limpiar el canvas
    ctx.clearRect(0, 0, mapaCanvas.width, mapaCanvas.height);
    
    // Guardar el estado actual del contexto
    ctx.save();
    
    // Aplicar transformaciones para zoom y desplazamiento
    ctx.translate(offsetX, offsetY);
    ctx.scale(escalaActual, escalaActual);
    
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
    
    // Restaurar el estado del contexto
    ctx.restore();
}

// Dibujar la cuadrícula base
function dibujarCuadricula(ctx) {
    const mapaCanvas = document.getElementById('mapa-canvas');
    const ancho = mapaCanvas.width / escalaActual;
    const alto = mapaCanvas.height / escalaActual;
    
    // Aplicar estilo de línea más visible para la cuadrícula
    ctx.strokeStyle = getComputedStyle(document.documentElement).getPropertyValue('--color-grid').trim() || '#a0a0a0';
    ctx.lineWidth = 1.5; // Aumentar significativamente el grosor de las líneas
    
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
    
    // Añadir números en los ejes para mejor referencia
    ctx.fillStyle = '#555';
    ctx.font = '10px Arial';
    
    // Números en eje horizontal (cada 5 celdas)
    for (let x = 0; x <= ancho; x += TAMANO_CELDA * 5) {
        if (x > 0) { // Evitar el 0,0
            ctx.fillText(x / TAMANO_CELDA, x + 2, 10);
        }
    }
    
    // Números en eje vertical (cada 5 celdas)
    for (let y = 0; y <= alto; y += TAMANO_CELDA * 5) {
        if (y > 0) { // Evitar el 0,0
            ctx.fillText(y / TAMANO_CELDA, 2, y + 10);
        }
    }
}

// Dibujar almacenes en el mapa
function dibujarAlmacenes(ctx) {
    almacenes.forEach(almacen => {
        // Calcular posición en el canvas (colocar en la intersección de las líneas)
        const x = almacen.posX * TAMANO_CELDA;
        const y = almacen.posY * TAMANO_CELDA;
        
        // Dibujar almacén como un punto exactamente en la intersección de las líneas
        ctx.fillStyle = COLORES.ALMACEN;
        const tamanoPunto = TAMANO_CELDA / 4; // Un cuarto del tamaño de la celda
        
        // Dibujar un círculo pequeño en la intersección
        ctx.beginPath();
        ctx.arc(x, y, tamanoPunto, 0, 2 * Math.PI);
        ctx.fill();
        
        // Agregar contorno
        ctx.strokeStyle = '#27ae60';
        ctx.lineWidth = 1;
        ctx.stroke();
        
        // Agregar etiqueta
        ctx.fillStyle = '#fff';
        ctx.font = '10px Arial';
        ctx.fillText('A', x - 3, y + 3);
    });
}

// Dibujar pedidos en el mapa
function dibujarPedidos(ctx) {
    pedidos.forEach(pedido => {
        // Calcular posición en el canvas (colocar en la intersección de las líneas)
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
        
        // Dibujar pedido como un pequeño rombo en la intersección
        const tamanoPunto = TAMANO_CELDA / 4;
        ctx.fillStyle = color;
        
        // Dibujar un rombo exactamente en la intersección
        ctx.beginPath();
        ctx.moveTo(x, y - tamanoPunto); // Arriba
        ctx.lineTo(x + tamanoPunto, y); // Derecha
        ctx.lineTo(x, y + tamanoPunto); // Abajo
        ctx.lineTo(x - tamanoPunto, y); // Izquierda
        ctx.closePath();
        ctx.fill();
        
        // Agregar contorno
        ctx.strokeStyle = '#000';
        ctx.lineWidth = 1;
        ctx.stroke();
        
        // Mostrar un indicador de pedido
        ctx.fillStyle = '#fff';
        ctx.font = '8px Arial';
        ctx.fillText('P', x - 3, y + 3);
    });
}

// Dibujar camiones en el mapa
function dibujarCamiones(ctx) {
    camiones.forEach(camion => {
        // Calcular posición en el canvas (colocar exactamente en la intersección de las líneas)
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
        
        // Dibujar camión como un pequeño círculo exactamente en la intersección
        const tamanoPunto = TAMANO_CELDA / 4;
        ctx.fillStyle = color;
        ctx.beginPath();
        ctx.arc(x, y, tamanoPunto, 0, 2 * Math.PI);
        ctx.fill();
        
        // Agregar contorno
        ctx.strokeStyle = '#2c3e50';
        ctx.lineWidth = 1;
        ctx.stroke();
        
        // Mostrar código del camión
        ctx.fillStyle = '#fff';
        ctx.font = '9px Arial';
        ctx.fillText('C', x - 3, y + 3);
        
        // Si está en ruta, mostrar indicador de progreso debajo del camión
        if (camion.estado === 1 && camion.progresoRuta !== undefined) {
            // Barra de progreso debajo del camión
            const anchoTotal = TAMANO_CELDA / 2;
            const anchoProgreso = (camion.progresoRuta / 100) * anchoTotal;
            
            ctx.fillStyle = '#2ecc71';
            ctx.fillRect(x - anchoTotal/2, 
                        y + tamanoPunto + 2, 
                        anchoProgreso, 2);
            
            ctx.strokeStyle = '#bdc3c7';
            ctx.strokeRect(x - anchoTotal/2, 
                          y + tamanoPunto + 2, 
                          anchoTotal, 2);
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
            primerNodo.posX * TAMANO_CELDA, 
            primerNodo.posY * TAMANO_CELDA
        );
        
        for (let i = 1; i < ruta.nodos.length; i++) {
            const nodo = ruta.nodos[i];
            ctx.lineTo(
                nodo.posX * TAMANO_CELDA, 
                nodo.posY * TAMANO_CELDA
            );
        }
        
        ctx.stroke();
        ctx.setLineDash([]); // Restaurar línea continua
        
        // Dibujar puntos en cada nodo de la ruta para mayor claridad
        ruta.nodos.forEach((nodo, index) => {
            const x = nodo.posX * TAMANO_CELDA;
            const y = nodo.posY * TAMANO_CELDA;
            
            // Dibujar un pequeño círculo en cada nodo de la ruta
            ctx.fillStyle = index === 0 ? '#27ae60' : '#e74c3c'; // Verde para origen, rojo para destino
            ctx.beginPath();
            ctx.arc(x, y, 3, 0, 2 * Math.PI);
            ctx.fill();
            
            // Añadir número de orden si hay más de 2 nodos
            if (ruta.nodos.length > 2 && index > 0 && index < ruta.nodos.length - 1) {
                ctx.fillStyle = '#fff';
                ctx.font = '8px Arial';
                ctx.fillText(index.toString(), x - 2, y + 3);
            }
        });
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
    const btnGenerarRutas = document.getElementById('btn-generar-rutas');
    const indicadorCarga = document.getElementById('indicador-carga');
    
    if (btnGenerarRutas) {
        btnGenerarRutas.disabled = true;
    }
    
    if (indicadorCarga) {
        indicadorCarga.style.display = 'inline-block';
    }
    
    // Mostrar notificación inicial
    mostrarNotificacion('Generando rutas...', 'info');
    
    // Llamar a la API para generar rutas
    fetch('/api/rutas/generar', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            algoritmo: 'genetico',
            numeroRutas: 3
        })
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`Error HTTP: ${response.status}`);
        }
        return response.json();
    })
    .then(data => {
        console.log('Rutas generadas:', data);
        
        // Actualizar la UI con las nuevas rutas
        cargarRutasGeneradas();
        
        // Ocultar indicador de carga
        if (btnGenerarRutas) {
            btnGenerarRutas.disabled = false;
        }
        
        if (indicadorCarga) {
            indicadorCarga.style.display = 'none';
        }
        
        // Mostrar mensaje de éxito
        mostrarNotificacion('Rutas generadas correctamente', 'success');
    })
    .catch(error => {
        console.error('Error generando rutas:', error);
        
        if (btnGenerarRutas) {
            btnGenerarRutas.disabled = false;
        }
        
        if (indicadorCarga) {
            indicadorCarga.style.display = 'none';
        }
        
        mostrarNotificacion('Error al generar rutas: ' + error.message, 'error');
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
    
    // Actualizar panel de información con los datos proporcionados por el servidor
    actualizarPanelInformacion({
        camiones: camiones.length,
        almacenes: almacenes.length,
        pedidos: pedidos.length,
        rutas: rutas.length,
        simulacionEnCurso: simulacionEnCurso,
        camionesEnRuta: data.camionesEnRuta || camiones.filter(c => c.estado === 1).length,
        pedidosPendientes: data.pedidosPendientes || pedidos.filter(p => p.estado === 0).length,
        pedidosEnRuta: data.pedidosEnRuta || pedidos.filter(p => p.estado === 1).length,
        rutasActivas: data.rutasActivas || rutas.filter(r => r.estado === 1).length
    });
    
    // Actualizar contador de elementos en tiempo real
    document.getElementById('contador-camiones').textContent = camiones.length;
    document.getElementById('contador-almacenes').textContent = almacenes.length;
    document.getElementById('contador-pedidos').textContent = pedidos.length;
    document.getElementById('contador-rutas').textContent = rutas.length;
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
    // Convertir coordenadas de mapa a coordenadas de canvas considerando zoom y desplazamiento
    const posX = x * TAMANO_CELDA * escalaActual + offsetX + TAMANO_CELDA/2 * escalaActual;
    const posY = y * TAMANO_CELDA * escalaActual + offsetY + TAMANO_CELDA/2 * escalaActual;
    
    // Crear elemento de animación
    const animacion = document.createElement('div');
    animacion.className = 'animacion-entrega';
    animacion.style.left = posX + 'px';
    animacion.style.top = posY + 'px';
    animacion.style.transform = `scale(${escalaActual})`;
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
    // Convertir coordenadas de mapa a coordenadas de canvas considerando zoom y desplazamiento
    const posX = x * TAMANO_CELDA * escalaActual + offsetX + TAMANO_CELDA/2 * escalaActual;
    const posY = y * TAMANO_CELDA * escalaActual + offsetY + TAMANO_CELDA/2 * escalaActual;
    
    // Crear elemento de animación
    const animacion = document.createElement('div');
    animacion.className = 'animacion-recarga';
    animacion.style.left = posX + 'px';
    animacion.style.top = posY + 'px';
    animacion.style.transform = `scale(${escalaActual})`;
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