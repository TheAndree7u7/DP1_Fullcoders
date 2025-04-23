// Módulo para manejar las animaciones visuales

/**
 * Muestra una animación de entrega exitosa en las coordenadas especificadas
 */
export function mostrarAnimacionEntrega(x, y) {
    // Convertir coordenadas de mapa a coordenadas de canvas considerando zoom y desplazamiento
    const posX = x * window.app.TAMANO_CELDA * window.app.escalaActual + 
                window.app.offsetX + window.app.TAMANO_CELDA/2 * window.app.escalaActual;
    const posY = y * window.app.TAMANO_CELDA * window.app.escalaActual + 
                window.app.offsetY + window.app.TAMANO_CELDA/2 * window.app.escalaActual;
    
    // Crear elemento de animación
    const animacion = document.createElement('div');
    animacion.className = 'animacion-entrega';
    animacion.style.left = posX + 'px';
    animacion.style.top = posY + 'px';
    animacion.style.transform = `scale(${window.app.escalaActual})`;
    animacion.textContent = '✓';
    
    // Agregar al contenedor del mapa
    const contenedorMapa = document.getElementById('contenedor-mapa');
    if (contenedorMapa) {
        contenedorMapa.appendChild(animacion);
        
        // Eliminar después de la animación
        setTimeout(() => {
            if (animacion.parentNode) {
                animacion.remove();
            }
        }, 2000);
    }
}

/**
 * Muestra una animación de recarga en las coordenadas especificadas
 */
export function mostrarAnimacionRecarga(x, y) {
    // Convertir coordenadas de mapa a coordenadas de canvas considerando zoom y desplazamiento
    const posX = x * window.app.TAMANO_CELDA * window.app.escalaActual + 
                window.app.offsetX + window.app.TAMANO_CELDA/2 * window.app.escalaActual;
    const posY = y * window.app.TAMANO_CELDA * window.app.escalaActual + 
                window.app.offsetY + window.app.TAMANO_CELDA/2 * window.app.escalaActual;
    
    // Crear elemento de animación
    const animacion = document.createElement('div');
    animacion.className = 'animacion-recarga';
    animacion.style.left = posX + 'px';
    animacion.style.top = posY + 'px';
    animacion.style.transform = `scale(${window.app.escalaActual})`;
    animacion.textContent = '⛽';
    
    // Agregar al contenedor del mapa
    const contenedorMapa = document.getElementById('contenedor-mapa');
    if (contenedorMapa) {
        contenedorMapa.appendChild(animacion);
        
        // Eliminar después de la animación
        setTimeout(() => {
            if (animacion.parentNode) {
                animacion.remove();
            }
        }, 2000);
    }
}
