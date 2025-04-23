// Módulo para cargar datos y administrar su acceso

import { dibujarMapa } from './mapa.js';
import { actualizarPanelInformacion } from './ui.js';
import { conectarWebSocket } from './simulacion.js';

// Cargar datos iniciales desde el servidor
export async function cargarDatosIniciales() {
    try {
        // Inicializar arrays vacíos para evitar errores
        window.app.almacenes = [];
        window.app.camiones = [];
        window.app.pedidos = [];
        window.app.rutas = [];

        // Cargar datos del mapa reticular
        const respMapa = await fetch('/api/mapa/configuracion');
        if (respMapa.ok) {
            window.app.mapaReticular = await respMapa.json();
        } else {
            console.error('Error cargando configuración del mapa:', respMapa.statusText);
            window.app.mapaReticular = { ancho: 50, alto: 50, tamano: 1 };
        }
        
        try {
            // Cargar camiones
            const respCamiones = await fetch('/api/camiones');
            if (respCamiones.ok) {
                const camionesDatos = await respCamiones.json();
                if (Array.isArray(camionesDatos)) {
                    window.app.camiones = camionesDatos;
                    // Inicializar propiedades de seguimiento de ruta para cada camión
                    window.app.camiones.forEach(camion => {
                        camion.nodoActualIndex = 0;
                        camion.progresoRuta = 0;
                        camion.ultimoNodoVisitado = { x: camion.posX, y: camion.posY };
                    });
                }
            }
        } catch (error) {
            console.error('Error cargando camiones:', error);
            window.app.camiones = [];
        }
        
        try {
            // Cargar almacenes
            const respAlmacenes = await fetch('/api/almacenes');
            if (respAlmacenes.ok) {
                const almacenesDatos = await respAlmacenes.json();
                if (Array.isArray(almacenesDatos)) {
                    window.app.almacenes = almacenesDatos;
                }
            }
        } catch (error) {
            console.error('Error cargando almacenes:', error);
            window.app.almacenes = [];
        }
        
        try {
            // Cargar pedidos
            const respPedidos = await fetch('/api/pedidos');
            if (respPedidos.ok) {
                const pedidosDatos = await respPedidos.json();
                if (Array.isArray(pedidosDatos)) {
                    window.app.pedidos = pedidosDatos;
                }
            }
        } catch (error) {
            console.error('Error cargando pedidos:', error);
            window.app.pedidos = [];
        }
        
        try {
            // Cargar rutas existentes
            await cargarRutasExistentes();
        } catch (error) {
            console.error('Error cargando rutas:', error);
            window.app.rutas = [];
        }
        
        try {
            // Cargar bloqueos
            window.app.bloqueos = await cargarBloqueos();
        } catch (error) {
            console.error('Error cargando bloqueos:', error);
            window.app.bloqueos = [];
        }
        
        try {
            // Verificar estado de la simulación
            const respSimulacion = await fetch('/api/simulacion/estado');
            if (respSimulacion.ok) {
                const estadoSimulacion = await respSimulacion.json();
                window.app.simulacionEnCurso = estadoSimulacion.simulacionEnCurso;
                window.app.velocidadSimulacion = estadoSimulacion.factorVelocidad || 1;
                
                // Actualizar UI según el estado
                const btnIniciarSim = document.getElementById('btn-iniciar-simulacion');
                const btnDetenerSim = document.getElementById('btn-detener-simulacion');
                const velocidadSim = document.getElementById('velocidad-simulacion');
                
                if (btnIniciarSim) btnIniciarSim.disabled = window.app.simulacionEnCurso;
                if (btnDetenerSim) btnDetenerSim.disabled = !window.app.simulacionEnCurso;
                if (velocidadSim) velocidadSim.value = window.app.velocidadSimulacion;
                
                // Si la simulación ya está en curso, conectar al WebSocket
                if (window.app.simulacionEnCurso) {
                    conectarWebSocket();
                }
                
                // Actualizar panel con datos
                actualizarPanelInformacion({
                    camiones: window.app.camiones.length,
                    almacenes: window.app.almacenes.length,
                    pedidos: window.app.pedidos.length,
                    rutas: window.app.rutas.length,
                    simulacionEnCurso: window.app.simulacionEnCurso,
                    camionesEnRuta: estadoSimulacion.camionesEnRuta || 0,
                    rutasActivas: estadoSimulacion.rutasActivas || 0,
                    pedidosPendientes: estadoSimulacion.pedidosPendientes || 0,
                    pedidosEnRuta: estadoSimulacion.pedidosEnRuta || 0
                });
            }
        } catch (error) {
            console.error('Error verificando estado de simulación:', error);
            window.app.simulacionEnCurso = false;
            const btnIniciarSim = document.getElementById('btn-iniciar-simulacion');
            const btnDetenerSim = document.getElementById('btn-detener-simulacion');
            if (btnIniciarSim) btnIniciarSim.disabled = false;
            if (btnDetenerSim) btnDetenerSim.disabled = true;
        }
        
        // Dibujar el mapa con todos los datos cargados
        dibujarMapa();
        
    } catch (error) {
        console.error('Error en cargarDatosIniciales:', error);
    }
}

// Función para cargar las rutas existentes en el sistema
export async function cargarRutasExistentes() {
    try {
        const response = await fetch('/api/rutas');
        if (response.ok) {
            window.app.rutas = await response.json();
            console.log('Rutas cargadas:', window.app.rutas);
            return window.app.rutas;
        } else {
            throw new Error(`HTTP Error: ${response.status}`);
        }
    } catch (error) {
        console.error('Error cargando rutas:', error);
        window.app.rutas = [];
        return [];
    }
}

// Función para cargar los bloqueos
export async function cargarBloqueos() {
    try {
        // Primero actualizamos el estado de los bloqueos
        await fetch('/api/bloqueos/actualizar-estado', { method: 'POST' });
        
        // Luego obtenemos las rutas bloqueadas
        const response = await fetch('/api/rutas/bloqueadas');
        const bloqueos = await response.json();
        
        // Actualizar contador de bloqueos
        const countBloqueos = document.getElementById('count-bloqueos-activos');
        if (countBloqueos) {
            countBloqueos.textContent = bloqueos.length;
        }
        
        return bloqueos;
    } catch (error) {
        console.error('Error al cargar los bloqueos:', error);
        return [];
    }
}
