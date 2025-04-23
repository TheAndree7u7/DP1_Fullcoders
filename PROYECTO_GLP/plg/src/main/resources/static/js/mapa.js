// Módulo para manejar el dibujo y visualización del mapa

import { obtenerColorPorEstadoPedido, encontrarClusterDelPedido } from './utils.js';

// Dibujar el mapa con todos los elementos
export function dibujarMapa() {
    const mapaCanvas = document.getElementById('mapa-canvas');
    if (!mapaCanvas) return;
    
    const ctx = mapaCanvas.getContext('2d');
    
    // Limpiar el canvas
    ctx.clearRect(0, 0, mapaCanvas.width, mapaCanvas.height);
    
    // Guardar el estado actual del contexto
    ctx.save();
    
    // Aplicar transformaciones para zoom y desplazamiento
    ctx.translate(window.app.offsetX, window.app.offsetY);
    ctx.scale(window.app.escalaActual, window.app.escalaActual);
    
    // Dibujar cuadrícula
    dibujarCuadricula(ctx);
    
    // Dibujar rutas
    dibujarRutas(ctx);
    
    // Dibujar almacenes
    if (window.app.almacenes && Array.isArray(window.app.almacenes)) {
        dibujarAlmacenes(ctx);
    }
    
    // Dibujar pedidos (ahora considerando clusters)
    if (window.app.pedidos && Array.isArray(window.app.pedidos)) {
        dibujarPedidos(ctx);
    }
    
    // Dibujar camiones (último para que estén por encima)
    if (window.app.camiones && Array.isArray(window.app.camiones)) {
        dibujarCamiones(ctx);
    }
    
    // Dibujar bloqueos
    if (window.app.bloqueos && Array.isArray(window.app.bloqueos)) {
        dibujarBloqueos(ctx);
    }
    
    // Dibujar información de optimización si está en curso
    if (window.app.etapaOptimizacion !== 'ninguna') {
        dibujarEstadoOptimizacion(ctx);
    }
    
    // Restaurar el estado del contexto
    ctx.restore();
}

// Dibujar la cuadrícula base
function dibujarCuadricula(ctx) {
    const mapaCanvas = document.getElementById('mapa-canvas');
    const ancho = mapaCanvas.width / window.app.escalaActual;
    const alto = mapaCanvas.height / window.app.escalaActual;
    
    // Aplicar estilo de línea más visible para la cuadrícula
    ctx.strokeStyle = getComputedStyle(document.documentElement).getPropertyValue('--color-grid').trim() || '#a0a0a0';
    ctx.lineWidth = 1.5; // Aumentar significativamente el grosor de las líneas
    
    // Dibujar líneas horizontales
    for (let y = 0; y <= alto; y += window.app.TAMANO_CELDA) {
        ctx.beginPath();
        ctx.moveTo(0, y);
        ctx.lineTo(ancho, y);
        ctx.stroke();
    }
    
    // Dibujar líneas verticales
    for (let x = 0; x <= ancho; x += window.app.TAMANO_CELDA) {
        ctx.beginPath();
        ctx.moveTo(x, 0);
        ctx.lineTo(x, alto);
        ctx.stroke();
    }
    
    // Añadir números en los ejes para mejor referencia
    ctx.fillStyle = '#555';
    ctx.font = '10px Arial';
    
    // Números en eje horizontal (cada 5 celdas)
    for (let x = 0; x <= ancho; x += window.app.TAMANO_CELDA * 5) {
        if (x > 0) { // Evitar el 0,0
            ctx.fillText(x / window.app.TAMANO_CELDA, x + 2, 10);
        }
    }
    
    // Números en eje vertical (cada 5 celdas)
    for (let y = 0; y <= alto; y += window.app.TAMANO_CELDA * 5) {
        if (y > 0) { // Evitar el 0,0
            ctx.fillText(y / window.app.TAMANO_CELDA, 2, y + 10);
        }
    }
}

// Dibujar almacenes en el mapa (modificado para distinguir entre tipos)
function dibujarAlmacenes(ctx) {
    if (!window.app.almacenes || !Array.isArray(window.app.almacenes)) return;
    
    window.app.almacenes.forEach(almacen => {
        // Calcular posición en el canvas
        const x = almacen.posX * window.app.TAMANO_CELDA;
        const y = almacen.posY * window.app.TAMANO_CELDA;
        
        // Distinguir entre almacén central e intermedio
        const esCentral = almacen.esCentral === true;
        const tamanoPunto = window.app.TAMANO_CELDA / 2.5; // Aumentar tamaño para mejor visibilidad
        
        // Color diferente para cada tipo de almacén
        if (esCentral) {
            // Almacén central - círculo grande con color principal y borde más visible
            ctx.fillStyle = window.app.COLORES.ALMACEN;
            ctx.beginPath();
            ctx.arc(x, y, tamanoPunto, 0, 2 * Math.PI);
            ctx.fill();
            
            // Borde más prominente
            ctx.strokeStyle = '#27ae60'; // Borde verde oscuro
            ctx.lineWidth = 3; // Aumentar ancho del borde
            ctx.stroke();
            
            // Agregar símbolo central más grande y centrado
            ctx.fillStyle = '#fff';
            ctx.font = 'bold 14px Arial'; // Texto más grande
            // Calcular posición para centrar texto
            const metrics = ctx.measureText('C');
            const textWidth = metrics.width;
            ctx.fillText('C', x - textWidth/2, y + 5); 
        } else {
            // Almacén intermedio - cuadrado con color secundario
            ctx.fillStyle = '#16a085'; // Color diferente para intermedios
            const mitadTamano = tamanoPunto * 0.8;
            ctx.fillRect(x - mitadTamano, y - mitadTamano, mitadTamano * 2, mitadTamano * 2);
            
            // Agregar contorno
            ctx.strokeStyle = '#27ae60';
            ctx.lineWidth = 2; // Aumentar ancho del borde
            ctx.strokeRect(x - mitadTamano, y - mitadTamano, mitadTamano * 2, mitadTamano * 2);
            
            // Agregar etiqueta más visible
            ctx.fillStyle = '#fff';
            ctx.font = 'bold 12px Arial';
            // Calcular posición para centrar texto
            const metrics = ctx.measureText('I');
            const textWidth = metrics.width;
            ctx.fillText('I', x - textWidth/2, y + 4);
        }
    });
}

// Dibujar pedidos en el mapa (considerando clusters)
function dibujarPedidos(ctx) {
    window.app.pedidos.forEach(pedido => {
        // Calcular posición en el canvas (colocar en la intersección de las líneas)
        const x = pedido.posX * window.app.TAMANO_CELDA;
        const y = pedido.posY * window.app.TAMANO_CELDA;
        
        // Color según el estado del pedido o cluster
        let color;
        
        if (window.app.clusters.length > 0 && window.app.mostrarClusters) {
            // Si hay clusters y está activada la visualización de clusters
            const cluster = encontrarClusterDelPedido(pedido.id);
            if (cluster !== -1) {
                // Usar color del cluster (con índice módulo para reutilizar colores)
                color = window.app.COLORES_CLUSTER[cluster % window.app.COLORES_CLUSTER.length];
            } else {
                // Si no está en ningún cluster, usar colores normales
                color = obtenerColorPorEstadoPedido(pedido);
            }
        } else {
            // Usar colores según estado normal
            color = obtenerColorPorEstadoPedido(pedido);
        }
        
        // Dibujar pedido como un pequeño rombo en la intersección
        const tamanoPunto = window.app.TAMANO_CELDA / 4;
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
    window.app.camiones.forEach(camion => {
        // Calcular posición en el canvas (colocar exactamente en la intersección de las líneas)
        const x = camion.posX * window.app.TAMANO_CELDA;
        const y = camion.posY * window.app.TAMANO_CELDA;
        
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
        const tamanoPunto = window.app.TAMANO_CELDA / 4;
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
            const anchoTotal = window.app.TAMANO_CELDA / 2;
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
    if (!window.app.rutas || window.app.rutas.length === 0) return;
    
    window.app.rutas.forEach(ruta => {
        // Verificar si la ruta tiene puntos o nodos
        const puntosRuta = ruta.puntos || ruta.nodos;
        if (!puntosRuta || puntosRuta.length < 2) return;
        
        // Configurar estilo para la línea de ruta
        ctx.strokeStyle = window.app.COLORES.RUTA;
        ctx.lineWidth = 2;
        ctx.setLineDash([5, 3]); // Línea punteada
        
        // Dibujar línea que conecta los puntos
        ctx.beginPath();
        
        // Extraer coordenadas del primer punto
        const primerPunto = puntosRuta[0];
        const x1 = (primerPunto.posX !== undefined ? primerPunto.posX : primerPunto.x) * window.app.TAMANO_CELDA;
        const y1 = (primerPunto.posY !== undefined ? primerPunto.posY : primerPunto.y) * window.app.TAMANO_CELDA;
        ctx.moveTo(x1, y1);
        
        // Dibujar resto de puntos
        for (let i = 1; i < puntosRuta.length; i++) {
            const punto = puntosRuta[i];
            const x = (punto.posX !== undefined ? punto.posX : punto.x) * window.app.TAMANO_CELDA;
            const y = (punto.posY !== undefined ? punto.posY : punto.y) * window.app.TAMANO_CELDA;
            ctx.lineTo(x, y);
        }
        
        ctx.stroke();
        ctx.setLineDash([]); // Restaurar línea continua
        
        // Dibujar puntos en cada nodo de la ruta para mayor claridad
        puntosRuta.forEach((punto, index) => {
            const x = (punto.posX !== undefined ? punto.posX : punto.x) * window.app.TAMANO_CELDA;
            const y = (punto.posY !== undefined ? punto.posY : punto.y) * window.app.TAMANO_CELDA;
            
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
function dibujarBloqueos(ctx) {
    if (!window.app.mostrarBloqueos || !window.app.bloqueos || window.app.bloqueos.length === 0) return;

    ctx.save();
    ctx.strokeStyle = 'red';
    ctx.lineWidth = 4;
    
    window.app.bloqueos.forEach(bloqueo => {
        ctx.beginPath();
        // Convertir coordenadas a posición de la celda
        const x1 = bloqueo.x1 * window.app.TAMANO_CELDA;
        const y1 = bloqueo.y1 * window.app.TAMANO_CELDA;
        const x2 = bloqueo.x2 * window.app.TAMANO_CELDA;
        const y2 = bloqueo.y2 * window.app.TAMANO_CELDA;
        
        ctx.moveTo(x1, y1);
        ctx.lineTo(x2, y2);
        ctx.stroke();
        
        // Agregar un tooltip o indicador visual adicional
        const midX = (x1 + x2) / 2;
        const midY = (y1 + y2) / 2;
        
        ctx.fillStyle = 'rgba(255, 0, 0, 0.3)';
        ctx.beginPath();
        ctx.arc(midX, midY, 5, 0, 2 * Math.PI);
        ctx.fill();
    });
    
    ctx.restore();
}

// Dibujar estado de optimización en el canvas
function dibujarEstadoOptimizacion(ctx) {
    // Calcular posición en la parte superior del canvas considerando zoom y offset
    const x = (10 - window.app.offsetX) / window.app.escalaActual;
    const y = (30 - window.app.offsetY) / window.app.escalaActual;
    
    // Configurar estilo de texto
    ctx.font = `${14/window.app.escalaActual}px Arial`;
    ctx.fillStyle = '#2c3e50';
    ctx.strokeStyle = '#fff';
    ctx.lineWidth = 2 / window.app.escalaActual;
    
    // Traducir etapa de optimización
    let textoEtapa = '';
    switch (window.app.etapaOptimizacion) {
        case 'ninguna': textoEtapa = 'Ninguna'; break;
        case 'ap': textoEtapa = 'Agrupamiento (AP)'; break;
        case 'genetico': textoEtapa = 'Algoritmo Genético'; break;
        case 'rutas': textoEtapa = 'Generando Rutas'; break;
        case 'completo': textoEtapa = 'Optimización Completa'; break;
        default: textoEtapa = window.app.etapaOptimizacion;
    }
    
    // Texto a mostrar
    let texto = `Optimización: ${textoEtapa} (${window.app.progresoPorcentaje}%)`;
    
    // Aplicar stroke para hacer más visible el texto
    ctx.strokeText(texto, x, y);
    ctx.fillText(texto, x, y);
}
