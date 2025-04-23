// Módulo para operaciones de optimización (AP, Genético)

import { dibujarMapa } from './mapa.js';
import { actualizarPanelInformacion, agregarEventoAlHistorial, mostrarNotificacion } from './ui.js';

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
        alpha: 1.0,
        beta: 0.5,
        damping: 0.9,
        maxIter: 100
    };

    console.log('Enviando solicitud AP con parámetros:', params);
    
    // Llamar a la API para ejecutar AP
    return fetch('/api/optimizacion/ap', {
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
        window.app.clusters = data.grupos || [];
        
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
    if (window.app.clusters.length === 0) {
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
    
    // Llamar a la API para ejecutar GA
    return fetch('/api/optimizacion/genetico', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            poblacionInicial: 50,
            maxGeneraciones: 100,
            tasaMutacion: 0.1,
            tasaCruce: 0.8,
            clusters: window.app.clusters.map(c => c.idGrupo)
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
    if (window.app.clusters.length > 0) {
        parametros.clusters = window.app.clusters.map(c => c.idGrupo);
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
        
        // Actualizar la UI con las nuevas rutas
        return cargarRutasGeneradas().then(() => data);
    })
    .then(data => {
        // Actualizar estado
        window.app.progresoPorcentaje = 100;
        actualizarPanelInformacion({
            etapaOptimizacion: window.app.etapaOptimizacion,
            progresoOptimizacion: window.app.progresoPorcentaje
        });
        
        // Mostrar mensaje de éxito
        mostrarNotificacion('Rutas generadas correctamente', 'success');
        return data;
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
            console.log('Rutas generadas cargadas:', data);
            window.app.rutas = Array.isArray(data) ? data : [];
            
            // Actualizar contador de rutas en la UI
            document.getElementById('contador-rutas').textContent = window.app.rutas.length;
            
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
