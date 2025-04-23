// Módulo para manejar los bloqueos en el mapa

import { dibujarMapa } from './mapa.js';

// Función para alternar la visualización de los bloqueos
export function toggleBloqueos() {
    window.app.mostrarBloqueos = !window.app.mostrarBloqueos;
    dibujarMapa(); // Redibujar el canvas
}

// Cargar los bloqueos desde el servidor
export async function cargarBloqueos() {
    try {
        // Primero actualizamos el estado de los bloqueos
        await fetch('/api/bloqueos/actualizar-estado', { method: 'POST' });
        
        // Luego obtenemos las rutas bloqueadas
        const response = await fetch('/api/rutas/bloqueadas');
        if (!response.ok) {
            throw new Error(`Error HTTP: ${response.status}`);
        }
        
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
