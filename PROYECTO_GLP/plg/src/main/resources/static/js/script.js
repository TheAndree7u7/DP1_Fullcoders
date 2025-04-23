// Variables globales para el estado de la aplicación
let stompClient = null;
let mapaReticular = [];
let camiones = [];
let almacenes = [];
let pedidos = [];
let rutas = [];
let simulacionEnCurso = false;
let velocidadSimulacion = 1;

// Nuevas variables para algoritmos de optimización
let clusters = [];
let mostrarClusters = true;
let etapaOptimizacion = 'ninguna'; // 'ap', 'genetico', 'rutas', 'completo'
let progresoPorcentaje = 0;

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

// Colores para clusters (para visualizar grupos del AP)
const COLORES_CLUSTER = [
    '#1abc9c', '#2ecc71', '#3498db', '#9b59b6', '#f1c40f',
    '#e67e22', '#e74c3c', '#34495e', '#16a085', '#27ae60',
    '#2980b9', '#8e44ad', '#f39c12', '#d35400', '#c0392b',
    '#7f8c8d', '#2c3e50'
];

// Inicializar la aplicación cuando se carga la página
document.addEventListener('DOMContentLoaded', function() {
    inicializarUI();
    cargarDatosIniciales();
    
    // Establecer manejadores de eventos para los botones
    document.getElementById('btn-iniciar-simulacion').addEventListener('click', iniciarSimulacion);
    document.getElementById('btn-detener-simulacion').addEventListener('click', detenerSimulacion);
    document.getElementById('btn-ajustar-velocidad').addEventListener('click', cambiarVelocidad);
    document.getElementById('btn-generar-rutas').addEventListener('click', generarRutas);
    
    // Nuevos botones para algoritmos de optimización
    document.getElementById('btn-ejecutar-ap').addEventListener('click', ejecutarAffinityPropagation);
    document.getElementById('btn-ejecutar-genetico').addEventListener('click', ejecutarAlgoritmoGenetico);
    document.getElementById('btn-optimizacion-completa').addEventListener('click', ejecutarOptimizacionCompleta);
    document.getElementById('btn-toggle-clusters').addEventListener('click', toggleVisualizacionClusters);
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
    
    // Botón para mostrar/ocultar bloqueos
    const toggleBlocksBtn = document.createElement('button');
    toggleBlocksBtn.className = 'control-btn';
    toggleBlocksBtn.innerHTML = '🚫';
    toggleBlocksBtn.title = 'Mostrar/Ocultar bloqueos';
    toggleBlocksBtn.addEventListener('click', toggleBloqueos);
    
    // Añadir botones al panel
    controlesDiv.appendChild(zoomInBtn);
    controlesDiv.appendChild(zoomOutBtn);
    controlesDiv.appendChild(resetBtn);
    controlesDiv.appendChild(fullscreenBtn);
    controlesDiv.appendChild(toggleBlocksBtn);
    
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
            // Inicializar propiedades de seguimiento de ruta para cada camión
            camiones.forEach(camion => {
                camion.nodoActualIndex = 0;
                camion.progresoRuta = 0;
                camion.siguienteNodo = null;
            });
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
    
    // Cargar rutas existentes
    cargarRutasExistentes();
        
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
            document.getElementId('btn-detener-simulacion').disabled = true;
        });
}

// Nueva función para cargar las rutas existentes en el sistema
function cargarRutasExistentes() {
    fetch('/api/rutas')
        .then(response => {
            if (!response.ok) {
                throw new Error(`Error HTTP: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            console.log('Rutas cargadas:', data);
            rutas = Array.isArray(data) ? data : [];
            dibujarMapa();
        })
        .catch(error => {
            console.error('Error cargando rutas:', error);
            rutas = [];
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
    
    // Dibujar pedidos (ahora considerando clusters)
    dibujarPedidos(ctx);
    
    // Dibujar camiones (último para que estén por encima)
    dibujarCamiones(ctx);
    
    // Dibujar bloqueos
    dibujarBloqueos(ctx, bloqueos, escalaActual, offsetX, offsetY);
    
    // Dibujar información de optimización si está en curso
    if (etapaOptimizacion !== 'ninguna') {
        dibujarEstadoOptimizacion(ctx);
    }
    
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

// Dibujar almacenes en el mapa (modificado para distinguir entre tipos)
function dibujarAlmacenes(ctx) {
    almacenes.forEach(almacen => {
        // Calcular posición en el canvas (colocar en la intersección de las líneas)
        const x = almacen.posX * TAMANO_CELDA;
        const y = almacen.posY * TAMANO_CELDA;
        
        // Distinguir entre almacén central e intermedio
        const esCentral = almacen.esCentral === true;
        const tamanoPunto = TAMANO_CELDA / 3; // Un tercio del tamaño de la celda
        
        // Color diferente para cada tipo de almacén
        if (esCentral) {
            // Almacén central - círculo grande con color principal
            ctx.fillStyle = COLORES.ALMACEN;
            ctx.beginPath();
            ctx.arc(x, y, tamanoPunto, 0, 2 * Math.PI);
            ctx.fill();
            ctx.strokeStyle = '#27ae60'; // Borde verde oscuro
            ctx.lineWidth = 2;
            ctx.stroke();
            
            // Agregar símbolo central
            ctx.fillStyle = '#fff';
            ctx.font = '10px Arial';
            ctx.fillText('C', x - 3, y + 3);
        } else {
            // Almacén intermedio - cuadrado con color secundario
            ctx.fillStyle = '#16a085'; // Color diferente para intermedios
            const mitadTamano = tamanoPunto * 0.8;
            ctx.fillRect(x - mitadTamano, y - mitadTamano, mitadTamano * 2, mitadTamano * 2);
            
            // Agregar contorno
            ctx.strokeStyle = '#27ae60';
            ctx.lineWidth = 1;
            ctx.strokeRect(x - mitadTamano, y - mitadTamano, mitadTamano * 2, mitadTamano * 2);
            
            // Agregar etiqueta
            ctx.fillStyle = '#fff';
            ctx.font = '10px Arial';
            ctx.fillText('I', x - 3, y + 3);
        }
    });
}

// Dibujar pedidos en el mapa (modificado para considerar clusters)
function dibujarPedidos(ctx) {
    pedidos.forEach(pedido => {
        // Calcular posición en el canvas (colocar en la intersección de las líneas)
        const x = pedido.posX * TAMANO_CELDA;
        const y = pedido.posY * TAMANO_CELDA;
        
        // Color según el estado del pedido o cluster
        let color;
        
        if (clusters.length > 0 && mostrarClusters) {
            // Si hay clusters y está activada la visualización de clusters
            const cluster = encontrarClusterDelPedido(pedido.id);
            if (cluster !== -1) {
                // Usar color del cluster (con índice módulo para reutilizar colores)
                color = COLORES_CLUSTER[cluster % COLORES_CLUSTER.length];
            } else {
                // Si no está en ningún cluster, usar colores normales
                color = obtenerColorPorEstadoPedido(pedido);
            }
        } else {
            // Usar colores según estado normal
            color = obtenerColorPorEstadoPedido(pedido);
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

// Función auxiliar para obtener el color según estado del pedido
function obtenerColorPorEstadoPedido(pedido) {
    switch (pedido.estado) {
        case 0: // Pendiente
            return COLORES.PEDIDO_PENDIENTE;
        case 1: // En ruta
            return COLORES.PEDIDO_EN_RUTA;
        case 2: // Entregado
            return COLORES.PEDIDO_ENTREGADO;
        default:
            return '#999';
    }
}

// Función para encontrar el cluster al que pertenece un pedido
function encontrarClusterDelPedido(pedidoId) {
    for (let i = 0; i < clusters.length; i++) {
        const pedidosEnCluster = clusters[i].pedidos;
        for (let j = 0; j < pedidosEnCluster.length; j++) {
            if (pedidosEnCluster[j].id === pedidoId) {
                return i;  // Devuelve el índice del cluster
            }
        }
    }
    return -1;  // No está en ningún cluster
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

// Dibujar rutas en el mapa (mejorado para manejar diferentes formatos de rutas)
function dibujarRutas(ctx) {
    if (!rutas || rutas.length === 0) return;
    
    rutas.forEach(ruta => {
        // Verificar si la ruta tiene puntos o nodos
        const puntosRuta = ruta.puntos || ruta.nodos;
        if (!puntosRuta || puntosRuta.length < 2) return;
        
        // Configurar estilo para la línea de ruta
        ctx.strokeStyle = COLORES.RUTA;
        ctx.lineWidth = 2;
        ctx.setLineDash([5, 3]); // Línea punteada
        
        // Dibujar línea que conecta los puntos
        ctx.beginPath();
        
        // Extraer coordenadas del primer punto
        const primerPunto = puntosRuta[0];
        const x1 = (primerPunto.posX !== undefined ? primerPunto.posX : primerPunto.x) * TAMANO_CELDA;
        const y1 = (primerPunto.posY !== undefined ? primerPunto.posY : primerPunto.y) * TAMANO_CELDA;
        ctx.moveTo(x1, y1);
        
        // Dibujar resto de puntos
        for (let i = 1; i < puntosRuta.length; i++) {
            const punto = puntosRuta[i];
            const x = (punto.posX !== undefined ? punto.posX : punto.x) * TAMANO_CELDA;
            const y = (punto.posY !== undefined ? punto.posY : punto.y) * TAMANO_CELDA;
            ctx.lineTo(x, y);
        }
        
        ctx.stroke();
        ctx.setLineDash([]); // Restaurar línea continua
        
        // Dibujar puntos en cada nodo de la ruta para mayor claridad
        puntosRuta.forEach((punto, index) => {
            const x = (punto.posX !== undefined ? punto.posX : punto.x) * TAMANO_CELDA;
            const y = (punto.posY !== undefined ? punto.posY : punto.y) * TAMANO_CELDA;
            
            // Personalizar visualización según el tipo de punto
            const tipo = punto.tipo || (index === 0 ? 'INICIO' : 
                        (index === puntosRuta.length - 1 ? 'FINAL' : 'INTERMEDIO'));
            
            if (tipo === 'ALMACEN' || tipo === 'INICIO' || tipo === 'FINAL') {
                // Punto de origen o destino - círculo verde/rojo
                ctx.fillStyle = index === 0 ? '#27ae60' : '#e74c3c';
                ctx.beginPath();
                ctx.arc(x, y, 3, 0, 2 * Math.PI);
                ctx.fill();
            } else if (tipo === 'CLIENTE') {
                // Punto de cliente - rombo amarillo
                ctx.fillStyle = '#f1c40f';
                const tamanoPunto = 4;
                ctx.beginPath();
                ctx.moveTo(x, y - tamanoPunto);
                ctx.lineTo(x + tamanoPunto, y);
                ctx.lineTo(x, y + tamanoPunto);
                ctx.lineTo(x - tamanoPunto, y);
                ctx.closePath();
                ctx.fill();
            } else {
                // Punto intermedio - pequeño punto gris
                ctx.fillStyle = '#95a5a6';
                ctx.beginPath();
                ctx.arc(x, y, 2, 0, 2 * Math.PI);
                ctx.fill();
            }
            
            // Añadir número de orden si hay más de 2 nodos y no es un nodo intermedio
            if (puntosRuta.length > 2 && index > 0 && 
                index < puntosRuta.length - 1 && tipo !== 'INTERMEDIO') {
                ctx.fillStyle = '#fff';
                ctx.font = '8px Arial';
                ctx.fillText(index.toString(), x - 2, y + 3);
            }
        });
    });
}

// Dibujar bloqueos en el mapa
function dibujarBloqueos(context, bloqueos, escala, offsetX, offsetY) {
    if (!mostrarBloqueos || !bloqueos || bloqueos.length === 0) return;

    context.save();
    context.strokeStyle = 'red';
    context.lineWidth = 4;
    
    bloqueos.forEach(bloqueo => {
        context.beginPath();
        const x1 = (bloqueo.x1 * escala) + offsetX;
        const y1 = (bloqueo.y1 * escala) + offsetY;
        const x2 = (bloqueo.x2 * escala) + offsetX;
        const y2 = (bloqueo.y2 * escala) + offsetY;
        
        context.moveTo(x1, y1);
        context.lineTo(x2, y2);
        context.stroke();
        
        // Agregar un tooltip o indicador visual adicional
        const midX = (x1 + x2) / 2;
        const midY = (y1 + y2) / 2;
        
        context.fillStyle = 'rgba(255, 0, 0, 0.3)';
        context.beginPath();
        context.arc(midX, midY, 5, 0, 2 * Math.PI);
        context.fill();
    });
    
    context.restore();
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

    // Actualizar estadísticas de optimización si están disponibles
    if (data.numeroClusters !== undefined) {
        document.getElementById('numero-clusters').textContent = data.numeroClusters;
    }
    
    if (data.etapaOptimizacion !== undefined) {
        document.getElementById('etapa-optimizacion').textContent = traducirEtapaOptimizacion(data.etapaOptimizacion);
    }
    
    if (data.progresoOptimizacion !== undefined) {
        const progresoBar = document.getElementById('progreso-optimizacion');
        if (progresoBar) {
            progresoBar.style.width = data.progresoOptimizacion + '%';
            progresoBar.setAttribute('aria-valuenow', data.progresoOptimizacion);
        }
    }
}

// Traducir etapa de optimización a texto amigable
function traducirEtapaOptimizacion(etapa) {
    switch (etapa) {
        case 'ninguna': return 'Ninguna';
        case 'ap': return 'Agrupamiento (AP)';
        case 'genetico': return 'Algoritmo Genético';
        case 'rutas': return 'Generando Rutas';
        case 'completo': return 'Optimización Completa';
        default: return etapa;
    }
}

// Dibujar estado de optimización en el canvas
function dibujarEstadoOptimizacion(ctx) {
    // Calcular posición en la parte superior del canvas considerando zoom y offset
    const mapaCanvas = document.getElementById('mapa-canvas');
    const x = (10 - offsetX) / escalaActual;
    const y = (30 - offsetY) / escalaActual;
    
    // Configurar estilo de texto
    ctx.font = `${14/escalaActual}px Arial`;
    ctx.fillStyle = '#2c3e50';
    ctx.strokeStyle = '#fff';
    ctx.lineWidth = 2 / escalaActual;
    
    // Texto a mostrar
    let textoEtapa = traducirEtapaOptimizacion(etapaOptimizacion);
    let texto = `Optimización: ${textoEtapa} (${progresoPorcentaje}%)`;
    
    // Aplicar stroke para hacer más visible el texto
    ctx.strokeText(texto, x, y);
    ctx.fillText(texto, x, y);
}

// Ejecutar el algoritmo de Affinity Propagation
function ejecutarAffinityPropagation() {
    // Mostrar indicador de carga y deshabilitar botón
    const btnExecutarAP = document.getElementById('btn-ejecutar-ap');
    btnExecutarAP.disabled = true;
    mostrarNotificacion('Ejecutando algoritmo de Affinity Propagation...', 'info');
    
    // Actualizar estado de optimización
    etapaOptimizacion = 'ap';
    progresoPorcentaje = 10;
    actualizarPanelInformacion({
        etapaOptimizacion: etapaOptimizacion,
        progresoOptimizacion: progresoPorcentaje
    });
    
    // Construir parámetros para la API con valores por defecto seguros
    const params = {
        alpha: 1.0,
        beta: 0.5,
        damping: 0.9,
        maxIter: 100
    };

    console.log('Enviando solicitud AP con parámetros:', params);
    
    // Llamar a la API para ejecutar AP
    fetch('/api/optimizacion/ap', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(params)
    })
    .then(response => {
        if (!response.ok) {
            // Intentar obtener más información del error
            return response.text().then(text => {
                console.error('Error response:', text);
                throw new Error(`Error HTTP: ${response.status} - ${text || 'No details available'}`);
            });
        }
        return response.json();
    })
    .then(data => {
        console.log('Respuesta AP recibida:', data);
        
        // Verificar que la respuesta contiene la estructura esperada
        if (!data || !data.grupos) {
            throw new Error('La respuesta no contiene información de clusters válida');
        }
        
        // Almacenar los clusters recibidos
        clusters = data.grupos || [];
        
        // Actualizar estado
        progresoPorcentaje = 100;
        actualizarPanelInformacion({
            numeroClusters: clusters.length,
            etapaOptimizacion: etapaOptimizacion,
            progresoOptimizacion: progresoPorcentaje
        });
        
        // Agregar evento al historial
        agregarEventoAlHistorial({
            tipo: 'Optimización',
            mensaje: `Agrupamiento completado: ${clusters.length} grupos generados`,
            fecha: new Date().toLocaleTimeString()
        });
        
        // Redibujar el mapa para mostrar los clusters
        dibujarMapa();
        
        // Mostrar notificación de éxito
        mostrarNotificacion(`Agrupamiento completado: ${clusters.length} grupos generados`, 'success');
    })
    .catch(error => {
        console.error('Error ejecutando Affinity Propagation:', error);
        
        // Actualizar estado para reflejar el error
        etapaOptimizacion = 'ninguna';
        progresoPorcentaje = 0;
        actualizarPanelInformacion({
            etapaOptimizacion: 'ninguna',
            progresoOptimizacion: 0
        });
        
        // Agregar evento al historial
        agregarEventoAlHistorial({
            tipo: 'Error',
            mensaje: `Error en agrupamiento: ${error.message}`,
            fecha: new Date().toLocaleTimeString()
        });
        
        mostrarNotificacion('Error en el agrupamiento: ' + error.message, 'error');
    })
    .finally(() => {
        // Habilitar botón nuevamente
        btnExecutarAP.disabled = false;
    });
}

// Ejecutar el algoritmo Genético
function ejecutarAlgoritmoGenetico() {
    // Verificar que existan clusters previos
    if (clusters.length === 0) {
        mostrarNotificacion('Primero debe ejecutar Affinity Propagation', 'warning');
        return;
    }
    
    // Mostrar indicador de carga y deshabilitar botón
    const btnEjecutarGA = document.getElementById('btn-ejecutar-genetico');
    btnEjecutarGA.disabled = true;
    mostrarNotificacion('Ejecutando algoritmo Genético...', 'info');
    
    // Actualizar estado de optimización
    etapaOptimizacion = 'genetico';
    progresoPorcentaje = 10;
    actualizarPanelInformacion({
        etapaOptimizacion: etapaOptimizacion,
        progresoOptimizacion: progresoPorcentaje
    });
    
    // Llamar a la API para ejecutar GA
    fetch('/api/optimizacion/genetico', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            poblacionInicial: 50,
            maxGeneraciones: 100,
            tasaMutacion: 0.1,
            tasaCruce: 0.8,
            clusters: clusters.map(c => c.idGrupo)
        })
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`Error HTTP: ${response.status}`);
        }
        return response.json();
    })
    .then(data => {
        // Actualizamos progreso
        progresoPorcentaje = 100;
        actualizarPanelInformacion({
            etapaOptimizacion: etapaOptimizacion,
            progresoOptimizacion: progresoPorcentaje
        });
        
        // Mostrar notificación de éxito
        mostrarNotificacion(`Optimización genética completada: ${data.fitness} fitness`, 'success');
        
        // La función generarRutas se puede llamar automáticamente después
        generarRutas();
    })
    .catch(error => {
        console.error('Error ejecutando Algoritmo Genético:', error);
        mostrarNotificacion('Error en la optimización genética: ' + error.message, 'error');
    })
    .finally(() => {
        // Habilitar botón nuevamente
        btnEjecutarGA.disabled = false;
    });
}

// Ejecutar el proceso completo de optimización: AP → GA → Rutas
function ejecutarOptimizacionCompleta() {
    // Mostrar indicador de carga y deshabilitar botón
    const btnOptimizacionCompleta = document.getElementById('btn-optimizacion-completa');
    btnOptimizacionCompleta.disabled = true;
    mostrarNotificacion('Iniciando proceso de optimización completa...', 'info');
    
    // Actualizar estado
    etapaOptimizacion = 'completo';
    progresoPorcentaje = 0;
    actualizarPanelInformacion({
        etapaOptimizacion: etapaOptimizacion,
        progresoOptimizacion: progresoPorcentaje
    });
    
    // Paso 1: Ejecutar AP
    ejecutarAffinityPropagation()
        .then(() => {
            // Paso 2: Ejecutar GA
            progresoPorcentaje = 33;
            actualizarPanelInformacion({
                progresoOptimizacion: progresoPorcentaje
            });
            return ejecutarAlgoritmoGenetico();
        })
        .then(() => {
            // Paso 3: Generar Rutas
            progresoPorcentaje = 66;
            actualizarPanelInformacion({
                progresoOptimizacion: progresoPorcentaje
            });
            return generarRutas();
        })
        .then(() => {
            // Proceso completo
            progresoPorcentaje = 100;
            actualizarPanelInformacion({
                progresoOptimizacion: progresoPorcentaje
            });
            mostrarNotificacion('Optimización completa finalizada', 'success');
        })
        .catch(error => {
            console.error('Error en proceso de optimización completa:', error);
            mostrarNotificacion('Error en optimización completa: ' + error.message, 'error');
        })
        .finally(() => {
            // Habilitar botón nuevamente
            btnOptimizacionCompleta.disabled = false;
        });
}

// Alternar visualización de clusters
function toggleVisualizacionClusters() {
    mostrarClusters = !mostrarClusters;
    // Actualizar botón
    const btnToggleClusters = document.getElementById('btn-toggle-clusters');
    if (btnToggleClusters) {
        btnToggleClusters.textContent = mostrarClusters ? 'Ocultar Clusters' : 'Mostrar Clusters';
    }
    // Redibujar mapa
    dibujarMapa();
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
    
    // Actualizar estado de optimización
    etapaOptimizacion = 'rutas';
    progresoPorcentaje = 10;
    actualizarPanelInformacion({
        etapaOptimizacion: etapaOptimizacion,
        progresoOptimizacion: progresoPorcentaje
    });
    
    // Llamar a la API para generar rutas
    const parametros = {
        algoritmo: 'genetico',
        numeroRutas: 3
    };
    
    // Si hay clusters, usarlos como input
    if (clusters.length > 0) {
        parametros.clusters = clusters.map(c => c.idGrupo);
    }
    
    fetch('/api/rutas/generar', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(parametros)
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
        
        // Actualizar estado
        progresoPorcentaje = 100;
        actualizarPanelInformacion({
            etapaOptimizacion: etapaOptimizacion,
            progresoOptimizacion: progresoPorcentaje
        });
        
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

// Cargar las rutas generadas (modificado para actualizar la UI correctamente)
function cargarRutasGeneradas() {
    fetch('/api/rutas')
        .then(response => {
            if (!response.ok) {
                throw new Error(`Error HTTP: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            console.log('Rutas generadas cargadas:', data);
            rutas = Array.isArray(data) ? data : [];
            
            // Actualizar contador de rutas en la UI
            document.getElementById('contador-rutas').textContent = rutas.length;
            
            // Agregar evento al historial
            agregarEventoAlHistorial({
                tipo: 'Sistema',
                mensaje: `Rutas cargadas: ${rutas.length}`,
                fecha: new Date().toLocaleTimeString()
            });
            
            // Redibujar mapa
            dibujarMapa();
        })
        .catch(error => {
            console.error('Error cargando rutas:', error);
            mostrarNotificacion('Error cargando rutas: ' + error.message, 'error');
        });
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
    // Guardar estado previo de camiones para mantener información de nodo y progreso
    const camionesAnteriores = [...camiones];
    
    // Actualizar camiones
    if (data.camiones) {
        camiones = data.camiones.map(nuevoCamion => {
            const camionAnterior = camionesAnteriores.find(c => c.id === nuevoCamion.id);
            
            // Si el camión ya existe, conservar datos de progreso si no vienen en el mensaje
            if (camionAnterior) {
                nuevoCamion.nodoActualIndex = 
                    nuevoCamion.nodoActualIndex !== undefined ? 
                    nuevoCamion.nodoActualIndex : camionAnterior.nodoActualIndex || 0;
                
                nuevoCamion.progresoRuta = 
                    nuevoCamion.progresoRuta !== undefined ? 
                    nuevoCamion.progresoRuta : camionAnterior.progresoRuta || 0;
                    
                // Si no viene especificado, mantener el último nodo visitado
                nuevoCamion.ultimoCambioNodo = camionAnterior.ultimoCambioNodo;
                nuevoCamion.ultimoProgreso = camionAnterior.ultimoProgreso;
            } else {
                // Si es un camión nuevo, inicializar valores
                nuevoCamion.nodoActualIndex = 0;
                nuevoCamion.progresoRuta = 0;
            }
            
            return nuevoCamion;
        });
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
    
    // Actualizar el índice de nodo del camión
    const camion = camiones.find(c => c.id === data.camionId);
    if (camion) {
        // Incrementar el nodo actual para avanzar en la ruta
        camion.nodoActualIndex = data.nodoIndex || camion.nodoActualIndex + 1;
        camion.ultimoCambioNodo = new Date().getTime();
        
        console.log(`Camión ${data.camionCodigo} avanzó al nodo ${camion.nodoActualIndex}, progreso: ${camion.progresoRuta}%`);
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

// Variable para controlar la visibilidad de los bloqueos
let mostrarBloqueos = true;

// Función para alternar la visualización de los bloqueos
function toggleBloqueos() {
    mostrarBloqueos = !mostrarBloqueos;
    dibujarMapa(); // Redibujar el canvas
}

// Agregar esta función para cargar los bloqueos
async function cargarBloqueos() {
    try {
        // Primero actualizamos el estado de los bloqueos
        await fetch('/api/bloqueos/actualizar-estado', { method: 'POST' });
        
        // Luego obtenemos las rutas bloqueadas
        const response = await fetch('/api/rutas/bloqueadas');
        const bloqueos = await response.json();
        
        return bloqueos;
    } catch (error) {
        console.error('Error al cargar los bloqueos:', error);
        return [];
    }
}

// Inicialización: Cargar los bloqueos cuando se carga la página
let bloqueos = [];
window.addEventListener('DOMContentLoaded', async () => {
    // ... código existente ...
    
    // Cargar los bloqueos
    bloqueos = await cargarBloqueos();
    
    // Redibujar el canvas para mostrar los bloqueos
    if (typeof dibujarMapa === 'function') {
        dibujarMapa();
    }
});


