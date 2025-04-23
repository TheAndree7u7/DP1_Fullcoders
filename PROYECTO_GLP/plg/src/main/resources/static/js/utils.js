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

/**
 * Guardar diagnóstico antes de cerrar la página
 */
export function configurarGuardadoAutomaticoDiagnostico() {
    window.addEventListener('beforeunload', function(e) {
        // Verificar si hay información de diagnóstico para guardar
        if (window.app && window.app.diagnostico && window.app.diagnostico.logs.length > 0) {
            // Guardar logs en localStorage para recuperación posterior
            try {
                localStorage.setItem('glp_diagnostico_' + Date.now(), 
                    JSON.stringify(window.app.diagnostico.logs));
                console.log('Diagnóstico guardado en localStorage antes de cerrar la página');
            } catch (error) {
                console.error('Error al guardar diagnóstico:', error);
            }
        }
    });
}

/**
 * Añadir función para recuperar diagnóstico guardado
 */
export function recuperarDiagnosticosGuardados() {
    const diagnosticos = [];
    
    // Buscar todas las entradas de diagnóstico en localStorage
    for (let i = 0; i < localStorage.length; i++) {
        const key = localStorage.key(i);
        if (key && key.startsWith('glp_diagnostico_')) {
            try {
                const logs = JSON.parse(localStorage.getItem(key));
                diagnosticos.push({
                    key: key,
                    timestamp: parseInt(key.replace('glp_diagnostico_', '')),
                    cantidadLogs: logs.length,
                    logs: logs
                });
            } catch (error) {
                console.error(`Error al recuperar diagnóstico ${key}:`, error);
            }
        }
    }
    
    // Ordenar por timestamp descendente (más reciente primero)
    diagnosticos.sort((a, b) => b.timestamp - a.timestamp);
    
    return diagnosticos;
}

/**
 * Corrige problemas estructurales en las rutas
 */
export function normalizarRutas(rutas) {
    if (!Array.isArray(rutas)) return [];
    
    return rutas.map(ruta => {
        // Si la ruta ya tiene puntos definidos correctamente, no hacer nada
        if (ruta.puntos && Array.isArray(ruta.puntos) && ruta.puntos.length > 1) {
            return ruta;
        }
        
        // Si tiene nodos pero no puntos, convertirlos
        if (ruta.nodos && Array.isArray(ruta.nodos) && ruta.nodos.length > 0) {
            ruta.puntos = ruta.nodos.map(nodo => ({
                tipo: nodo.tipo || "INTERMEDIO",
                posX: nodo.posX || nodo.x,
                posY: nodo.posY || nodo.y,
                idPedido: nodo.pedidoId || (nodo.pedido ? nodo.pedido.id : null)
            }));
            console.log(`Normalizada ruta ${ruta.idRuta || 'sin ID'}: convertidos ${ruta.puntos.length} nodos a puntos`);
        }
        
        // De todas formas asegurar que puntos sea un array
        if (!ruta.puntos) {
            ruta.puntos = [];
        }
        
        return ruta;
    });
}

/**
 * Encuentra una ruta para un camión específico
 */
export function buscarRutaParaCamion(camion, rutas) {
    if (!camion || !Array.isArray(rutas) || rutas.length === 0) {
        return null;
    }
    
    // Primero buscamos por camionCodigo (formato más común)
    let ruta = rutas.find(r => 
        r.camionCodigo === camion.codigo && r.estado !== 2); // No completada
    
    // Si no encontramos, probamos otras posibles propiedades
    if (!ruta) {
        ruta = rutas.find(r => 
            r.camion && (r.camion.id === camion.id || r.camion.codigo === camion.codigo) && r.estado !== 2);
    }
    
    // Como última opción, buscamos por camionId
    if (!ruta && camion.id) {
        ruta = rutas.find(r => r.camionId === camion.id && r.estado !== 2);
    }
    
    return ruta;
}
