// Utilidades de depuración para el sistema GLP

// Exportar función diagnóstico para uso global
export function inicializarHerramientasDepuracion() {
    // Agregar al objeto global para acceso desde consola
    window.glpDebug = {
        // Analizar estado completo de la aplicación
        estadoCompleto: () => {
            console.group('Estado de la aplicación GLP');
            
            // Info general
            console.log('Simulación en curso:', window.app.simulacionEnCurso);
            console.log('Velocidad simulación:', window.app.velocidadSimulacion);
            console.log('Escala de visualización:', window.app.escalaActual);
            
            // Estadísticas básicas
            console.group('Conteo de entidades');
            console.log('Camiones:', window.app.camiones?.length || 0);
            console.log('Almacenes:', window.app.almacenes?.length || 0);
            console.log('Pedidos:', window.app.pedidos?.length || 0);
            console.log('Rutas:', window.app.rutas?.length || 0);
            console.log('Clusters:', window.app.clusters?.length || 0);
            console.groupEnd();
            
            diagnosticarRutas();
            diagnosticarCamiones();
            diagnosticarRelacionesCamionRuta();
            
            console.groupEnd();
            return true;
        },
        
        // Diagnóstico específico de rutas
        diagnosticarRutas: diagnosticarRutas,
        
        // Diagnóstico específico de camiones
        diagnosticarCamiones: diagnosticarCamiones,
        
        // Diagnóstico de relación entre camiones y rutas
        diagnosticarRelacionesCamionRuta: diagnosticarRelacionesCamionRuta,
        
        // Corregir problemas comunes
        corregirProblemas: corregirProblemasComunes,
        
        // Generar mapa de calor de camiones
        generarMapaCalor: generarMapaCalorCamiones
    };
    
    console.log('📊 Herramientas de depuración GLP inicializadas. Usa window.glpDebug para acceder a las funciones.');
}

// Diagnosticar las rutas disponibles
function diagnosticarRutas() {
    console.group('Diagnóstico de Rutas');
    
    if (!window.app.rutas || !Array.isArray(window.app.rutas)) {
        console.error('❌ No hay rutas definidas o no es un array');
        console.groupEnd();
        return;
    }
    
    console.log(`Total rutas: ${window.app.rutas.length}`);
    
    // Analizar cada ruta
    window.app.rutas.forEach((ruta, idx) => {
        console.group(`Ruta #${idx+1}: ${ruta.idRuta || 'Sin ID'}`);
        
        // Verificar propiedades básicas
        console.log('Camión asignado:', ruta.camionCodigo || 'No especificado');
        console.log('Estado:', ruta.estado);
        
        // Verificar puntos/nodos
        if (ruta.puntos && Array.isArray(ruta.puntos) && ruta.puntos.length > 0) {
            console.log(`✅ Tiene ${ruta.puntos.length} puntos`);
            console.log('Primer punto:', ruta.puntos[0]);
            console.log('Último punto:', ruta.puntos[ruta.puntos.length-1]);
        } else {
            console.warn('⚠️ No tiene puntos definidos');
        }
        
        // Verificar si tiene nodos aunque no tenga puntos
        if (ruta.nodos && Array.isArray(ruta.nodos) && ruta.nodos.length > 0) {
            console.log(`✅ Tiene ${ruta.nodos.length} nodos (alternativa a puntos)`);
        }
        
        console.groupEnd();
    });
    
    console.groupEnd();
}

// Diagnosticar los camiones disponibles
function diagnosticarCamiones() {
    console.group('Diagnóstico de Camiones');
    
    if (!window.app.camiones || !Array.isArray(window.app.camiones)) {
        console.error('❌ No hay camiones definidos o no es un array');
        console.groupEnd();
        return;
    }
    
    console.log(`Total camiones: ${window.app.camiones.length}`);
    
    // Estadísticas por estado
    const estadisticas = {
        DISPONIBLE: 0,
        EN_RUTA: 0,
        EN_MANTENIMIENTO: 0,
        AVERIADO: 0,
        OTRO: 0
    };
    
    // Analizar cada camión
    window.app.camiones.forEach((camion, idx) => {
        console.group(`Camión #${idx+1}: ${camion.codigo || 'Sin código'}`);
        
        // Determinar estado real
        let estadoTexto;
        if (typeof camion.estado === 'string') {
            estadoTexto = camion.estado;
        } else {
            switch(camion.estado) {
                case 0: estadoTexto = 'DISPONIBLE'; estadisticas.DISPONIBLE++; break;
                case 1: estadoTexto = 'EN_RUTA'; estadisticas.EN_RUTA++; break;
                case 2: estadoTexto = 'EN_MANTENIMIENTO'; estadisticas.EN_MANTENIMIENTO++; break;
                case 3: estadoTexto = 'AVERIADO'; estadisticas.AVERIADO++; break;
                default: estadoTexto = `DESCONOCIDO (${camion.estado})`; estadisticas.OTRO++;
            }
        }
        
        console.log('ID:', camion.id);
        console.log('Código:', camion.codigo);
        console.log('Posición:', `(${camion.posX}, ${camion.posY})`);
        console.log('Estado:', estadoTexto);
        console.log('Nodo actual:', camion.nodoActualIndex);
        
        console.groupEnd();
    });
    
    console.log('Estadísticas por estado:', estadisticas);
    console.groupEnd();
}

// Diagnosticar relaciones entre camiones y rutas
function diagnosticarRelacionesCamionRuta() {
    console.group('Diagnóstico de Relaciones Camión-Ruta');
    
    if (!window.app.camiones || !window.app.rutas || 
        !Array.isArray(window.app.camiones) || !Array.isArray(window.app.rutas)) {
        console.error('❌ No hay camiones o rutas definidas');
        console.groupEnd();
        return;
    }
    
    // Camiones en ruta sin ruta asignada
    const camionesEnRutaSinRuta = window.app.camiones.filter(camion => 
        (camion.estado === 1 || camion.estado === 'EN_RUTA') && 
        !window.app.rutas.some(ruta => 
            ruta.camionCodigo === camion.codigo || 
            (ruta.camion && ruta.camion.id === camion.id) ||
            ruta.camionId === camion.id)
    );
    
    if (camionesEnRutaSinRuta.length > 0) {
        console.warn(`⚠️ Hay ${camionesEnRutaSinRuta.length} camiones en estado "EN_RUTA" sin ruta asignada`);
        console.log('Camiones afectados:', camionesEnRutaSinRuta);
    } else {
        console.log('✅ Todos los camiones en estado "EN_RUTA" tienen una ruta asignada');
    }
    
    // Rutas sin camión asignado
    const rutasSinCamion = window.app.rutas.filter(ruta => 
        !ruta.camionCodigo && 
        (!ruta.camion || !ruta.camion.id) && 
        !ruta.camionId
    );
    
    if (rutasSinCamion.length > 0) {
        console.warn(`⚠️ Hay ${rutasSinCamion.length} rutas sin camión asignado`);
        console.log('Rutas afectadas:', rutasSinCamion);
    } else {
        console.log('✅ Todas las rutas tienen un camión asignado');
    }
    
    // Rutas con camión que no existe
    const rutasConCamionInexistente = window.app.rutas.filter(ruta => {
        if (ruta.camionCodigo) {
            return !window.app.camiones.some(c => c.codigo === ruta.camionCodigo);
        }
        if (ruta.camionId) {
            return !window.app.camiones.some(c => c.id === ruta.camionId);
        }
        if (ruta.camion && ruta.camion.id) {
            return !window.app.camiones.some(c => c.id === ruta.camion.id);
        }
        return false;
    });
    
    if (rutasConCamionInexistente.length > 0) {
        console.warn(`⚠️ Hay ${rutasConCamionInexistente.length} rutas con camión que no existe`);
        console.log('Rutas afectadas:', rutasConCamionInexistente);
    } else {
        console.log('✅ Todas las rutas tienen un camión que existe');
    }
    
    console.groupEnd();
}

// Corregir problemas comunes
function corregirProblemasComunes() {
    console.group('Corrección de problemas comunes');
    
    // 1. Arreglar rutas sin puntos
    let rutasCorregidas = 0;
    if (window.app.rutas && Array.isArray(window.app.rutas)) {
        window.app.rutas.forEach((ruta, idx) => {
            // Si no tiene puntos pero sí tiene nodos, usar nodos como puntos
            if ((!ruta.puntos || !Array.isArray(ruta.puntos) || ruta.puntos.length === 0) && 
                ruta.nodos && Array.isArray(ruta.nodos) && ruta.nodos.length > 0) {
                console.log(`Corrigiendo ruta #${idx+1}: copiando ${ruta.nodos.length} nodos a puntos`);
                ruta.puntos = [...ruta.nodos];
                rutasCorregidas++;
            }
        });
    }
    
    // 2. Arreglar estado de camiones 
    let camionesCorregidos = 0;
    if (window.app.camiones && Array.isArray(window.app.camiones)) {
        window.app.camiones.forEach(camion => {
            // Si el estado es texto en lugar de número
            if (typeof camion.estado === 'string') {
                const estadoOriginal = camion.estado;
                switch(camion.estado.toUpperCase()) {
                    case 'DISPONIBLE': camion.estado = 0; break;
                    case 'EN_RUTA': camion.estado = 1; break;
                    case 'EN_MANTENIMIENTO': camion.estado = 2; break;
                    case 'AVERIADO': camion.estado = 3; break;
                }
                console.log(`Camión ${camion.codigo}: estado corregido de ${estadoOriginal} a ${camion.estado}`);
                camionesCorregidos++;
            }
        });
    }
    
    console.log(`Correcciones realizadas: ${rutasCorregidas} rutas, ${camionesCorregidos} camiones`);
    console.groupEnd();
    
    // Redibujar mapa para aplicar cambios
    if (typeof dibujarMapa === 'function') {
        dibujarMapa();
    }
    
    return {
        rutasCorregidas,
        camionesCorregidos
    };
}

// Generar mapa de calor de posiciones de camiones
function generarMapaCalorCamiones() {
    // Recopilar todas las posiciones históricas de los camiones
    const posiciones = [];
    
    window.historicoNodos?.forEach((historico, camionId) => {
        if (Array.isArray(historico)) {
            historico.forEach(registro => {
                posiciones.push({
                    x: registro.posX,
                    y: registro.posY,
                    timestamp: registro.timestamp
                });
            });
        }
    });
    
    console.log(`Generado mapa de calor con ${posiciones.length} posiciones históricas`);
    
    // Si hay una función para visualizar el mapa de calor, se llamaría aquí
    return posiciones;
}
