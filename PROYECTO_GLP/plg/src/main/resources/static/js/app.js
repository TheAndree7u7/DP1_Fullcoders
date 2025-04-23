// Archivo principal que inicializa la aplicación

// Importar módulos
import { inicializarUI, actualizarPanelInformacion } from './ui.js';
import { cargarDatosIniciales } from './datos.js';
import { 
    iniciarSimulacion, 
    detenerSimulacion, 
    cambiarVelocidad 
} from './simulacion.js';
import { 
    ejecutarAffinityPropagation, 
    ejecutarAlgoritmoGenetico,
    ejecutarOptimizacionCompleta,
    toggleVisualizacionClusters,
    generarRutas
} from './optimizacion.js';

// Variables globales compartidas
window.app = {
    // Estado de la aplicación
    stompClient: null,
    mapaReticular: [],
    camiones: [],
    almacenes: [],
    pedidos: [],
    rutas: [],
    simulacionEnCurso: false,
    velocidadSimulacion: 1,
    
    // Optimización
    clusters: [],
    mostrarClusters: true,
    etapaOptimizacion: 'ninguna',
    progresoPorcentaje: 0,
    
    // Mapa
    TAMANO_CELDA: 30,
    escalaActual: 1.0,
    offsetX: 0,
    offsetY: 0,
    arrastrando: false,
    ultimaX: 0,
    ultimaY: 0,
    
    // Bloqueos
    bloqueos: [],
    mostrarBloqueos: true,
    
    // Colores
    COLORES: {
        CAMION: '#3498db',
        ALMACEN: '#2ecc71',
        PEDIDO_PENDIENTE: '#e74c3c',
        PEDIDO_EN_RUTA: '#f39c12',
        PEDIDO_ENTREGADO: '#27ae60',
        RUTA: '#9b59b6',
        BLOQUEO: '#95a5a6'
    },
    
    COLORES_CLUSTER: [
        '#1abc9c', '#2ecc71', '#3498db', '#9b59b6', '#f1c40f',
        '#e67e22', '#e74c3c', '#34495e', '#16a085', '#27ae60',
        '#2980b9', '#8e44ad', '#f39c12', '#d35400', '#c0392b',
        '#7f8c8d', '#2c3e50'
    ]
};

// Inicializar la aplicación cuando se carga la página
document.addEventListener('DOMContentLoaded', async function() {
    inicializarUI();
    await cargarDatosIniciales();
    
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
    
    // Agregar botón para monitorear camiones (si existe la función)
    if (typeof agregarBotonMonitoreo === 'function') {
        agregarBotonMonitoreo();
    }
});
