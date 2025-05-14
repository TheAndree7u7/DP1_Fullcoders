// Módulo para operaciones de optimización (AP, Genético)

import { dibujarMapa } from './mapa.js';
import { actualizarPanelInformacion, agregarEventoAlHistorial, mostrarNotificacion } from './ui.js';
import { normalizarRutas } from './utils.js';

// Ejecutar el algoritmo de Affinity Propagation
export function ejecutarAffinityPropagation() {
    // Mostrar indicador de carga y deshabilitar botón
    const btnExecutarAP = document.getElementById('btn-ejecutar-ap');
    btnExecutarAP.disabled = true;
    mostrarNotificacion('Ejecutando algoritmo de Affinity Propagation...', 'info');
    
    // Actualizar estado de optimización
    window.app.etapaOptimizacion = 'ap';
    window.app.progresoPorcentaje = 10;
    actualizarPanelInformacion({
        etapaOptimizacion: window.app.etapaOptimizacion,
        progresoOptimizacion: window.app.progresoPorcentaje
    });
    
    // Construir parámetros para la API con valores por defecto seguros
    const params = {
        algoritmo: 'agrupamiento',
        alpha: 1.0,
        beta: 0.5,
        damping: 0.9,
        maxIter: 100
    };

    console.log('Enviando solicitud AP con parámetros:', params);
    
    // Llamar a la API para ejecutar AP - Corregido para usar el endpoint correcto
    return fetch('/api/rutas/generar', {
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
        if (!data || !data.clusters) {
            throw new Error('La respuesta no contiene información de clusters válida');
        }
        
        // Almacenar los clusters recibidos
        window.app.clusters = data.clusters || [];
        
        // Actualizar estado
        window.app.progresoPorcentaje = 100;
        actualizarPanelInformacion({
            numeroClusters: window.app.clusters.length,
            etapaOptimizacion: window.app.etapaOptimizacion,
            progresoOptimizacion: window.app.progresoPorcentaje
        });
        
        // Agregar evento al historial
        agregarEventoAlHistorial({
            tipo: 'Optimización',
            mensaje: `Agrupamiento completado: ${window.app.clusters.length} grupos generados`,
            fecha: new Date().toLocaleTimeString()
        });
        
        // Redibujar el mapa para mostrar los clusters
        dibujarMapa();
        
        // Mostrar notificación de éxito
        mostrarNotificacion(`Agrupamiento completado: ${window.app.clusters.length} grupos generados`, 'success');
        
        // Devolver datos para encadenamiento
        return data;
    })
    .catch(error => {
        console.error('Error ejecutando Affinity Propagation:', error);
        
        // Actualizar estado para reflejar el error
        window.app.etapaOptimizacion = 'ninguna';
        window.app.progresoPorcentaje = 0;
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
        
        // Re-lanzar el error para manejo en cadenas de promesas superiores
        throw error;
    })
    .finally(() => {
        // Habilitar botón nuevamente
        btnExecutarAP.disabled = false;
    });
}

// Ejecutar el algoritmo Genético
export function ejecutarAlgoritmoGenetico() {
    // Verificar que existan clusters previos
    if (!window.app.clusters || window.app.clusters.length === 0) {
        mostrarNotificacion('Primero debe ejecutar Affinity Propagation', 'warning');
        return Promise.reject(new Error('No hay clusters disponibles'));
    }
    
    // Mostrar indicador de carga y deshabilitar botón
    const btnEjecutarGA = document.getElementById('btn-ejecutar-genetico');
    btnEjecutarGA.disabled = true;
    mostrarNotificacion('Ejecutando algoritmo Genético...', 'info');
    
    // Actualizar estado de optimización
    window.app.etapaOptimizacion = 'genetico';
    window.app.progresoPorcentaje = 10;
    actualizarPanelInformacion({
        etapaOptimizacion: window.app.etapaOptimizacion,
        progresoOptimizacion: window.app.progresoPorcentaje
    });
    
    // Preparar datos para la API
    const requestData = {
        poblacionInicial: 50,
        maxGeneraciones: 100,
        tasaMutacion: 0.1,
        tasaCruce: 0.8,
        clusters: window.app.clusters.map((cluster, index) => ({
            idGrupo: index + 1,
            pedidos: [cluster].map(pedido => ({
                id: pedido.id,
                codigo: pedido.codigo,
                posX: pedido.posX,
                posY: pedido.posY,
                volumenGLPAsignado: pedido.volumenGLPAsignado,
                horasLimite: pedido.horasLimite
            }))
        }))
    };
    
    console.log('Enviando datos al algoritmo genético:', requestData);
    
    // Llamar a la API para ejecutar GA
    return fetch('/api/optimizacion/genetico', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(requestData)
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
        console.log('Respuesta del algoritmo genético:', data);
        
        // Actualizamos progreso
        window.app.progresoPorcentaje = 100;
        actualizarPanelInformacion({
            etapaOptimizacion: window.app.etapaOptimizacion,
            progresoOptimizacion: window.app.progresoPorcentaje
        });
        
        // Mostrar notificación de éxito
        mostrarNotificacion(`Optimización genética completada: ${data.fitness || 'N/A'} fitness`, 'success');
        
        return data;
    })
    .catch(error => {
        console.error('Error ejecutando Algoritmo Genético:', error);
        mostrarNotificacion('Error en la optimización genética: ' + error.message, 'error');
        throw error;
    })
    .finally(() => {
        // Habilitar botón nuevamente
        btnEjecutarGA.disabled = false;
    });
}

// Ejecutar el proceso completo de optimización: AP → GA → Rutas
export function ejecutarOptimizacionCompleta() {
    // Mostrar indicador de carga y deshabilitar botón
    const btnOptimizacionCompleta = document.getElementById('btn-optimizacion-completa');
    btnOptimizacionCompleta.disabled = true;
    mostrarNotificacion('Iniciando proceso de optimización completa...', 'info');
    
    // Actualizar estado
    window.app.etapaOptimizacion = 'completo';
    window.app.progresoPorcentaje = 0;
    actualizarPanelInformacion({
        etapaOptimizacion: window.app.etapaOptimizacion,
        progresoOptimizacion: window.app.progresoPorcentaje
    });
    
    // Cadena de promesas para ejecutar el proceso completo
    return ejecutarAffinityPropagation()
        .then(() => {
            // Paso 2: Ejecutar GA
            window.app.progresoPorcentaje = 33;
            actualizarPanelInformacion({
                progresoOptimizacion: window.app.progresoPorcentaje
            });
            return ejecutarAlgoritmoGenetico();
        })
        .then(() => {
            // Paso 3: Generar Rutas
            window.app.progresoPorcentaje = 66;
            actualizarPanelInformacion({
                progresoOptimizacion: window.app.progresoPorcentaje
            });
            return generarRutas();
        })
        .then(() => {
            // Proceso completo
            window.app.progresoPorcentaje = 100;
            actualizarPanelInformacion({
                progresoOptimizacion: window.app.progresoPorcentaje
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
export function toggleVisualizacionClusters() {
    window.app.mostrarClusters = !window.app.mostrarClusters;
    
    // Actualizar botón
    const btnToggleClusters = document.getElementById('btn-toggle-clusters');
    if (btnToggleClusters) {
        btnToggleClusters.textContent = window.app.mostrarClusters ? 'Ocultar Clusters' : 'Mostrar Clusters';
    }
    
    // Redibujar mapa
    dibujarMapa();
}

// Generar rutas utilizando el algoritmo de ruteo
export function generarRutas() {
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
    window.app.etapaOptimizacion = 'rutas';
    window.app.progresoPorcentaje = 10;
    actualizarPanelInformacion({
        etapaOptimizacion: window.app.etapaOptimizacion,
        progresoOptimizacion: window.app.progresoPorcentaje
    });
    
    // Llamar a la API para generar rutas
    const parametros = {
        algoritmo: 'genetico',
        numeroRutas: 3
    };
    
    // Si hay clusters, usarlos como input
    if (window.app.clusters && window.app.clusters.length > 0) {
        parametros.clusters = window.app.clusters.map(c => c.idGrupo);
        console.log('[OPTIMIZACIÓN] Usando clusters para generación de rutas:', parametros.clusters);
    } else {
        console.log('[OPTIMIZACIÓN] ⚠️ No hay clusters disponibles, se generarán rutas sin agrupamiento');
    }
    
    return fetch('/api/rutas/generar', {
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
        
        // Validar que las rutas tengan la estructura correcta
        if (data && data.rutas && Array.isArray(data.rutas)) {
            // Extraer las rutas generadas y guardarlas temporalmente
            const rutasGeneradas = data.rutas;
            
            // Verificar cada ruta recibida
            rutasGeneradas.forEach((ruta, index) => {
                console.log(`[OPTIMIZACIÓN] Ruta generada #${index+1}: ${ruta.idRuta}, camión: ${ruta.camionCodigo}, ${ruta.numeroPedidos} pedidos, ${ruta.puntos ? ruta.puntos.length : 0} puntos`);
                
                if (!ruta.puntos || !Array.isArray(ruta.puntos) || ruta.puntos.length < 2) {
                    console.warn(`[OPTIMIZACIÓN] ⚠️ Ruta generada ${index+1} sin puntos o con menos de 2 puntos`);
                }
            });
        }
        
        // Actualizar la UI con las nuevas rutas
        return cargarRutasGeneradas().then(() => {
            // Mostrar notificación
            mostrarNotificacion(`Rutas generadas: ${window.app.rutas.length}`, 'success');
            return data;
        });
    })
    .catch(error => {
        console.error('Error generando rutas:', error);
        mostrarNotificacion('Error al generar rutas: ' + error.message, 'error');
        throw error;
    })
    .finally(() => {
        // Ocultar indicador de carga
        if (btnGenerarRutas) {
            btnGenerarRutas.disabled = false;
        }
        
        if (indicadorCarga) {
            indicadorCarga.style.display = 'none';
        }
    });
}

// Cargar las rutas generadas
export function cargarRutasGeneradas() {
    return fetch('/api/rutas')
        .then(response => {
            if (!response.ok) {
                throw new Error(`Error HTTP: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            console.log('Rutas cargadas desde API:', data);
            
            // Normalizar estructura de rutas antes de guardar
            if (Array.isArray(data)) {
                let rutasNormalizadas = normalizarRutas(data);
                
                // Asegurar que todas las rutas tengan puntos definidos
                rutasNormalizadas = rutasNormalizadas.map((ruta, index) => {
                    // Si después de normalizar sigue sin puntos, crear puntos vacíos
                    if (!ruta.puntos || !Array.isArray(ruta.puntos) || ruta.puntos.length < 2) {
                        console.warn(`[OPTIMIZACIÓN] ⚠️ Ruta ${index+1} (${ruta.idRuta || 'sin ID'}) sin puntos suficientes`);
                        
                        // Asegurar que hay un array para trabajar
                        if (!ruta.puntos) {
                            ruta.puntos = [];
                        }
                        
                        // Si la ruta tiene camión asignado, intentar recuperar info de camión
                        if (ruta.camionCodigo) {
                            const camion = window.app.camiones.find(c => c.codigo === ruta.camionCodigo);
                            if (camion && ruta.puntos.length === 0) {
                                // Al menos agregar la posición actual del camión
                                ruta.puntos.push({
                                    tipo: "INICIO",
                                    posX: camion.posX,
                                    posY: camion.posY
                                });
                                
                                // Si hay almacén central, agregar como punto final
                                const almacenCentral = window.app.almacenes.find(a => a.esCentral === true);
                                if (almacenCentral) {
                                    ruta.puntos.push({
                                        tipo: "ALMACEN",
                                        posX: almacenCentral.posX,
                                        posY: almacenCentral.posY
                                    });
                                }
                                
                                console.log(`[OPTIMIZACIÓN] 🛠️ Creados puntos básicos para ruta ${index+1} basado en camión ${camion.codigo}`);
                            }
                        }
                    }
                    return ruta;
                });
                
                // Log para debug
                rutasNormalizadas.forEach((ruta, idx) => {
                    console.log(`[OPTIMIZACIÓN] Ruta #${idx+1}: ${ruta.idRuta || 'Sin ID'}, ${ruta.puntos ? ruta.puntos.length : 0} puntos, camión: ${ruta.camionCodigo || 'No asignado'}`);
                });
                
                window.app.rutas = rutasNormalizadas;
            } else {
                console.error("[OPTIMIZACIÓN] API devolvió datos que no son un array");
                window.app.rutas = [];
            }
            
            // Actualizar contador de rutas en la UI
            const contadorRutas = document.getElementById('contador-rutas');
            if (contadorRutas) contadorRutas.textContent = window.app.rutas.length;
            
            // Agregar evento al historial
            agregarEventoAlHistorial({
                tipo: 'Sistema',
                mensaje: `Rutas cargadas: ${window.app.rutas.length}`,
                fecha: new Date().toLocaleTimeString()
            });
            
            // Redibujar mapa
            dibujarMapa();
            
            return window.app.rutas;
        })
        .catch(error => {
            console.error('Error cargando rutas:', error);
            mostrarNotificacion('Error cargando rutas: ' + error.message, 'error');
            throw error;
        });
}
