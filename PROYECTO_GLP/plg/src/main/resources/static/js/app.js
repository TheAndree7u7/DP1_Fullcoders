// Módulo principal de la aplicación

import { inicializarUI, manejarZoom, cambiarZoom, iniciarArrastre, arrastrarMapa, finalizarArrastre } from './ui.js';
import { cargarDatosIniciales } from './datos.js';
import { dibujarMapa } from './mapa.js';
import { iniciarSimulacion, detenerSimulacion, cambiarVelocidadSimulacion, diagnosticarAvanceCamiones } from './simulacion.js';
import { ejecutarAffinityPropagation, ejecutarAlgoritmoGenetico, ejecutarOptimizacionCompleta, generarRutas, toggleVisualizacionClusters } from './optimizacion.js';
import { inicializarHerramientasDepuracion } from './depuracion.js';
import { configurarGuardadoAutomaticoDiagnostico, recuperarDiagnosticosGuardados } from './utils.js';

// Variables globales compartidas
window.app = {
    // Estado de la aplicación
    stompClient: null,
    mapaReticular: [],
    camiones: [],
    almacenes: [],
    pedidos: [],
    rutas: [],
    clusters: [],
    bloqueos: [],
    simulacionEnCurso: false,
    velocidadSimulacion: 1,
    mostrarClusters: false,
    
    // Configuración del mapa
    TAMANO_CELDA: 10,
    escalaActual: 1.0,
    offsetX: 0,
    offsetY: 0,
    arrastrando: false,
    ultimaX: 0,
    ultimaY: 0,
    
    // Colores para elementos gráficos
    COLORES: {
        FONDO: '#f5f5f5',
        CUADRICULA: '#e0e0e0',
        CUADRICULA_PRINCIPAL: '#cccccc',
        CAMION: '#3498db',
        ALMACEN: '#27ae60',
        ALMACEN_INTERMEDIO: '#16a085',
        PEDIDO_PENDIENTE: '#e74c3c',
        PEDIDO_EN_RUTA: '#f39c12',
        PEDIDO_ENTREGADO: '#27ae60',
        RUTA: 'rgba(52, 152, 219, 0.7)',
        BLOQUEO: 'rgba(142, 68, 173, 0.7)'
    },
    
    // Estado de optimización
    etapaOptimizacion: 'ninguna',  // 'ninguna', 'ap', 'genetico', 'completo', 'rutas'
    progresoPorcentaje: 0,
    
    // Utilidades de diagnóstico
    diagnostico: {
        logs: [],
        maxLogs: 1000,
        
        // Registra un evento para diagnóstico
        log: function(tipo, mensaje, datos) {
            const evento = {
                timestamp: Date.now(),
                tipo: tipo,
                mensaje: mensaje,
                datos: datos
            };
            
            this.logs.unshift(evento);
            
            // Mantener tamaño máximo
            if (this.logs.length > this.maxLogs) {
                this.logs.pop();
            }
            
            // También mostrar en consola para debugging
            console.log(`[${tipo}] ${mensaje}`, datos || '');
        },
        
        // Exportar logs para análisis
        exportar: function() {
            const dataStr = "data:text/json;charset=utf-8," + 
                encodeURIComponent(JSON.stringify(this.logs, null, 2));
            const downloadAnchorNode = document.createElement('a');
            downloadAnchorNode.setAttribute("href", dataStr);
            downloadAnchorNode.setAttribute("download", "diagnostico_glp_" + Date.now() + ".json");
            document.body.appendChild(downloadAnchorNode);
            downloadAnchorNode.click();
            downloadAnchorNode.remove();
        },
        
        // Limpiar logs
        limpiar: function() {
            this.logs = [];
            console.log('Logs de diagnóstico limpiados');
        }
    }
};

// Funciones para configurar los eventos de los botones
function configurarEventos() {
    // Configurar eventos para botones
    document.getElementById('btn-iniciar-simulacion').addEventListener('click', iniciarSimulacion);
    document.getElementById('btn-detener-simulacion').addEventListener('click', detenerSimulacion);
    document.getElementById('btn-ajustar-velocidad').addEventListener('click', cambiarVelocidadSimulacion);
    
    // Botones de optimización
    document.getElementById('btn-ejecutar-ap').addEventListener('click', ejecutarAffinityPropagation);
    document.getElementById('btn-ejecutar-genetico').addEventListener('click', ejecutarAlgoritmoGenetico);
    document.getElementById('btn-optimizacion-completa').addEventListener('click', ejecutarOptimizacionCompleta);
    document.getElementById('btn-generar-rutas').addEventListener('click', generarRutas);
    document.getElementById('btn-toggle-clusters').addEventListener('click', toggleVisualizacionClusters);
    
    // Ajuste para evitar que un botón desencadene eventos múltiples veces
    const botones = document.querySelectorAll('.btn');
    botones.forEach(boton => {
        boton.addEventListener('click', function(event) {
            // Si el botón ya estaba deshabilitado, no hacer nada
            if (this.disabled) return;
            
            // Deshabilitar temporalmente el botón
            this.disabled = true;
            
            // Habilitar después de un breve tiempo
            setTimeout(() => {
                this.disabled = false;
            }, 1000);
        });
    });
}

// Inicializar la aplicación cuando se carga la página
document.addEventListener('DOMContentLoaded', async function() {
    inicializarUI();
    inicializarHerramientasDepuracion();
    
    try {
        // Configurar guardado automático de diagnóstico
        configurarGuardadoAutomaticoDiagnostico();
        
        // Registrar inicio de aplicación en diagnóstico
        window.app.diagnostico.log('INICIO', 'Aplicación iniciada', {
            timestamp: Date.now(),
            userAgent: navigator.userAgent,
            plataforma: navigator.platform,
            resolución: `${window.innerWidth}x${window.innerHeight}`
        });
        
        await cargarDatosIniciales();
        configurarEventos();
        
        // Crear botón de diagnóstico en el panel de control
        const controlButtons = document.querySelector('.control-buttons');
        if (controlButtons) {
            const btnDiagnostico = document.createElement('button');
            btnDiagnostico.id = 'btn-diagnostico';
            btnDiagnostico.className = 'btn warning';
            btnDiagnostico.textContent = '🔍 Diagnosticar';
            btnDiagnostico.title = 'Ejecutar diagnóstico de avance de camiones';
            btnDiagnostico.addEventListener('click', function() {
                diagnosticarAvanceCamiones();
            });
            
            controlButtons.appendChild(btnDiagnostico);
        }
        
    } catch (error) {
        console.error('Error inicializando la aplicación:', error);
        window.app.diagnostico.log('ERROR', 'Error inicializando la aplicación', error);
    }
});

// Exponer diagnóstico a nivel global
window.diagnostico = {
    verCamiones: function() {
        console.table(window.app.camiones);
        return window.app.camiones;
    },
    verRutas: function() {
        console.table(window.app.rutas);
        return window.app.rutas;
    },
    exportarLogs: function() {
        window.app.diagnostico.exportar();
    }
};
