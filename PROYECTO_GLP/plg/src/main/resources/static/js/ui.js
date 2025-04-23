// Funciones relacionadas con la interfaz de usuario

import { dibujarMapa } from './mapa.js';
import { cargarBloqueos } from './datos.js';

// Inicializar la interfaz de usuario
export function inicializarUI() {
    // Configurar el mapa reticular (canvas)
    const mapaCanvas = document.getElementById('mapa-canvas');
    if (!mapaCanvas) return;
    
    const ctx = mapaCanvas.getContext('2d');
    ctx.imageSmoothingEnabled = true; // Mejora la calidad de renderizado
    
    // Configurar el tamaño del canvas según el contenedor
    const contenedorMapa = document.getElementById('contenedor-mapa');
    ajustarTamanoCanvas(mapaCanvas, contenedorMapa);
    
    // Configurar listeners para cambio de tamaño de ventana con debounce
    let resizeTimeout;
    window.addEventListener('resize', function() {
        clearTimeout(resizeTimeout);
        resizeTimeout = setTimeout(() => {
            ajustarTamanoCanvas(mapaCanvas, contenedorMapa);
            dibujarMapa();
        }, 100);
    });
    
    // Eventos para zoom y desplazamiento
    mapaCanvas.addEventListener('wheel', manejarZoom);
    mapaCanvas.addEventListener('mousedown', iniciarArrastre);
    
    // Usar requestAnimationFrame para suavizar el arrastre
    mapaCanvas.addEventListener('mousemove', function(event) {
        if (window.app.arrastrando) {
            window.requestAnimationFrame(() => {
                arrastrarMapa(event);
            });
        }
    });
    
    mapaCanvas.addEventListener('mouseup', finalizarArrastre);
    mapaCanvas.addEventListener('mouseleave', finalizarArrastre);
    
    // Establecer cursor inicial
    mapaCanvas.style.cursor = 'grab';
    
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
    
    // Configurar btn-cargar-bloqueos si existe
    const btnCargarBloqueos = document.getElementById('btn-cargar-bloqueos');
    if (btnCargarBloqueos) {
        btnCargarBloqueos.addEventListener('click', async () => {
            window.app.bloqueos = await cargarBloqueos();
            dibujarMapa();
        });
    }
}

// Función para ajustar el tamaño del canvas
function ajustarTamanoCanvas(canvas, contenedor) {
    // Obtener dimensiones del contenedor
    const rect = contenedor.getBoundingClientRect();
    
    // Establecer tamaño del canvas con las dimensiones exactas del contenedor
    // Multiplicar por devicePixelRatio para pantallas de alta densidad
    const dpr = window.devicePixelRatio || 1;
    canvas.width = rect.width * dpr;
    canvas.height = rect.height * dpr;
    
    // Ajustar el estilo CSS para que coincida con el tamaño visual del contenedor
    canvas.style.width = `${rect.width}px`;
    canvas.style.height = `${rect.height}px`;
    
    // Escalar el contexto para compensar la densidad de píxeles
    const ctx = canvas.getContext('2d');
    ctx.scale(dpr, dpr);
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

// Manejar evento de rueda para zoom con debounce
let zoomTimeout;
export function manejarZoom(event) {
    event.preventDefault();
    
    clearTimeout(zoomTimeout);
    zoomTimeout = setTimeout(() => {
        const delta = -Math.sign(event.deltaY) * 0.1;
        cambiarZoom(delta, event.offsetX, event.offsetY);
    }, 10);
}

// Cambiar nivel de zoom
export function cambiarZoom(delta, centerX, centerY) {
    // Obtener centro si no se proporciona
    const mapaCanvas = document.getElementById('mapa-canvas');
    centerX = centerX || mapaCanvas.width / 2;
    centerY = centerY || mapaCanvas.height / 2;
    
    // Calcular nueva escala con límites
    const escalaAnterior = window.app.escalaActual;
    window.app.escalaActual += delta;
    window.app.escalaActual = Math.max(0.5, Math.min(3.0, window.app.escalaActual)); // Limitar entre 0.5x y 3x
    
    // Ajustar offset para mantener el punto de zoom como centro
    if (window.app.escalaActual !== escalaAnterior) {
        const factor = window.app.escalaActual / escalaAnterior;
        window.app.offsetX = centerX - (centerX - window.app.offsetX) * factor;
        window.app.offsetY = centerY - (centerY - window.app.offsetY) * factor;
    }
    
    // Actualizar la visualización
    dibujarMapa();
}

// Iniciar arrastre del mapa
export function iniciarArrastre(event) {
    window.app.arrastrando = true;
    window.app.ultimaX = event.clientX;
    window.app.ultimaY = event.clientY;
    
    const mapaCanvas = document.getElementById('mapa-canvas');
    if (mapaCanvas) {
        mapaCanvas.style.cursor = 'grabbing';
    }
    
    // Prevenir selección de texto mientras se arrastra
    event.preventDefault();
}

// Arrastrar el mapa con animación suavizada
export function arrastrarMapa(event) {
    if (!window.app.arrastrando) return;
    
    const deltaX = event.clientX - window.app.ultimaX;
    const deltaY = event.clientY - window.app.ultimaY;
    
    // Aplicar un factor de suavizado
    const factorSuavizado = 1.0;
    window.app.offsetX += deltaX * factorSuavizado;
    window.app.offsetY += deltaY * factorSuavizado;
    
    window.app.ultimaX = event.clientX;
    window.app.ultimaY = event.clientY;
    
    dibujarMapa();
}

// Finalizar arrastre
export function finalizarArrastre(event) {
    if (window.app.arrastrando) {
        window.app.arrastrando = false;
        
        const mapaCanvas = document.getElementById('mapa-canvas');
        if (mapaCanvas) {
            mapaCanvas.style.cursor = 'grab';
        }
    }
}

// Resetear la vista al estado inicial
export function resetearVista() {
    window.app.escalaActual = 1.0;
    window.app.offsetX = 0;
    window.app.offsetY = 0;
    dibujarMapa();
}

// Alternar pantalla completa para el mapa
export function toggleFullscreen() {
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

// Mostrar notificación
export function mostrarNotificacion(mensaje, tipo) {
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

// Función para alternar la visualización de los bloqueos
export function toggleBloqueos() {
    window.app.mostrarBloqueos = !window.app.mostrarBloqueos;
    dibujarMapa(); // Redibujar el canvas
}

// Actualizar el panel de información con estadísticas actuales
export function actualizarPanelInformacion(data) {
    const panel = document.getElementById('panel-informacion');
    if (!panel) return;
    
    // Actualizar número de elementos
    if (data.camiones !== undefined) {
        const contadorCamiones = document.getElementById('contador-camiones');
        if (contadorCamiones) contadorCamiones.textContent = data.camiones;
    }
    
    if (data.almacenes !== undefined) {
        const contadorAlmacenes = document.getElementById('contador-almacenes');
        if (contadorAlmacenes) contadorAlmacenes.textContent = data.almacenes;
    }
    
    if (data.pedidos !== undefined) {
        const contadorPedidos = document.getElementById('contador-pedidos');
        if (contadorPedidos) contadorPedidos.textContent = data.pedidos;
    }
    
    if (data.rutas !== undefined) {
        const contadorRutas = document.getElementById('contador-rutas');
        if (contadorRutas) contadorRutas.textContent = data.rutas;
    }
    
    // Actualizar estado de simulación
    if (data.simulacionEnCurso !== undefined) {
        const estadoSimulacion = document.getElementById('estado-simulacion');
        if (estadoSimulacion) {
            estadoSimulacion.textContent = data.simulacionEnCurso ? 'En curso' : 'Detenida';
            estadoSimulacion.className = data.simulacionEnCurso ? 'estado-activo' : 'estado-inactivo';
        }
    }
    
    // Actualizar estadísticas adicionales si están disponibles
    if (data.camionesEnRuta !== undefined) {
        const camionesEnRuta = document.getElementById('camiones-en-ruta');
        if (camionesEnRuta) camionesEnRuta.textContent = data.camionesEnRuta;
    }
    
    if (data.rutasActivas !== undefined) {
        const rutasActivas = document.getElementById('rutas-activas');
        if (rutasActivas) rutasActivas.textContent = data.rutasActivas;
    }
    
    if (data.pedidosPendientes !== undefined) {
        const pedidosPendientes = document.getElementById('pedidos-pendientes');
        if (pedidosPendientes) pedidosPendientes.textContent = data.pedidosPendientes;
    }
    
    if (data.pedidosEnRuta !== undefined) {
        const pedidosEnRuta = document.getElementById('pedidos-en-ruta');
        if (pedidosEnRuta) pedidosEnRuta.textContent = data.pedidosEnRuta;
    }

    // Actualizar estadísticas de optimización si están disponibles
    if (data.numeroClusters !== undefined) {
        const numeroClusters = document.getElementById('numero-clusters');
        if (numeroClusters) numeroClusters.textContent = data.numeroClusters;
    }
    
    if (data.etapaOptimizacion !== undefined) {
        const etapaOptimizacion = document.getElementById('etapa-optimizacion');
        if (etapaOptimizacion) etapaOptimizacion.textContent = traducirEtapaOptimizacion(data.etapaOptimizacion);
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
export function traducirEtapaOptimizacion(etapa) {
    switch (etapa) {
        case 'ninguna': return 'Ninguna';
        case 'ap': return 'Agrupamiento (AP)';
        case 'genetico': return 'Algoritmo Genético';
        case 'rutas': return 'Generando Rutas';
        case 'completo': return 'Optimización Completa';
        default: return etapa;
    }
}

// Agregar evento al historial
export function agregarEventoAlHistorial(evento) {
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
