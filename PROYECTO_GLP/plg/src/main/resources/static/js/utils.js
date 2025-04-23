// Módulo para funciones de utilidad generales

/**
 * Devuelve el color correspondiente al estado del pedido
 */
export function obtenerColorPorEstadoPedido(pedido) {
    if (!pedido || pedido.estado === undefined) return '#999';
    
    switch (pedido.estado) {
        case 0: // Pendiente
            return window.app.COLORES.PEDIDO_PENDIENTE;
        case 1: // En ruta
            return window.app.COLORES.PEDIDO_EN_RUTA;
        case 2: // Entregado
            return window.app.COLORES.PEDIDO_ENTREGADO;
        default:
            return '#999';
    }
}

/**
 * Encuentra el cluster al que pertenece un pedido
 */
export function encontrarClusterDelPedido(pedidoId) {
    if (!window.app.clusters || !Array.isArray(window.app.clusters)) return -1;
    
    for (let i = 0; i < window.app.clusters.length; i++) {
        const cluster = window.app.clusters[i];
        if (!cluster.pedidos || !Array.isArray(cluster.pedidos)) continue;
        
        for (let j = 0; j < cluster.pedidos.length; j++) {
            if (cluster.pedidos[j].id === pedidoId) {
                return i;  // Devuelve el índice del cluster
            }
        }
    }
    return -1;  // No está en ningún cluster
}

/**
 * Comprueba si un objeto es definido y no nulo
 */
export function isDefined(obj) {
    return obj !== undefined && obj !== null;
}

/**
 * Comprueba si un objeto es un array no vacío
 */
export function isValidArray(arr) {
    return Array.isArray(arr) && arr.length > 0;
}

/**
 * Genera un color aleatorio en formato hexadecimal
 */
export function getRandomColor() {
    const letters = '0123456789ABCDEF';
    let color = '#';
    for (let i = 0; i < 6; i++) {
        color += letters[Math.floor(Math.random() * 16)];
    }
    return color;
}
