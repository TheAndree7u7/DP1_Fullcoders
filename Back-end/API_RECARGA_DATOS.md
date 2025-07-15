# API de Recarga de Datos

## Descripción

Esta API permite recargar todos los datos del sistema desde los archivos de configuración, limpiando las listas existentes y cargando nuevos datos. Es útil para reinicializar el sistema o aplicar cambios en los archivos de datos sin necesidad de reiniciar la aplicación.

## Endpoints

### POST /api/data-reload/recargar-todos

Recarga todos los datos del sistema.

**URL:** `http://localhost:8080/api/data-reload/recargar-todos`

**Método:** `POST`

**Headers:**
```
Content-Type: application/json
```

**Respuesta de éxito (200 OK):**
```json
{
  "cantidadAlmacenes": 3,
  "cantidadCamiones": 20,
  "cantidadPedidos": 150,
  "cantidadAverias": 5,
  "cantidadMantenimientos": 12,
  "cantidadBloqueos": 8,
  "mensaje": "Recarga de datos completada exitosamente",
  "exito": true
}
```

**Respuesta de error (400 Bad Request):**
```json
{
  "cantidadAlmacenes": 0,
  "cantidadCamiones": 0,
  "cantidadPedidos": 0,
  "cantidadAverias": 0,
  "cantidadMantenimientos": 0,
  "cantidadBloqueos": 0,
  "mensaje": "Error de formato en los datos: Formato inválido en línea 15",
  "exito": false
}
```

**Respuesta de error (500 Internal Server Error):**
```json
{
  "cantidadAlmacenes": 0,
  "cantidadCamiones": 0,
  "cantidadPedidos": 0,
  "cantidadAverias": 0,
  "cantidadMantenimientos": 0,
  "cantidadBloqueos": 0,
  "mensaje": "Error de lectura de archivos: No se pudo leer el archivo de pedidos",
  "exito": false
}
```

### POST /api/data-reload/health

Verifica que el controlador está funcionando correctamente.

**URL:** `http://localhost:8080/api/data-reload/health`

**Método:** `POST`

**Respuesta (200 OK):**
```
DataReloadController funcionando correctamente
```

## Datos que se recargan

La recarga incluye todos los siguientes tipos de datos:

1. **Almacenes** - Desde `data/almacenes/almacenes.txt`
2. **Camiones** - Generados automáticamente por tipo
3. **Pedidos** - Desde `data/pedidos/ventas{anho}{mes}.txt`
4. **Averías** - Desde `data/averias/averias.v1.txt`
5. **Mantenimientos** - Desde `data/mantenimientos/mantpreventivo.txt`
6. **Bloqueos** - Desde `data/bloqueos/{anho}{mes}.bloqueos.txt`

## Orden de carga

Los datos se cargan en el siguiente orden para respetar las dependencias:

1. Almacenes (base del sistema)
2. Camiones (dependen de almacenes)
3. Pedidos (pueden ser divididos según capacidad de camiones)
4. Averías
5. Mantenimientos
6. Bloqueos

## Características

- **Limpieza automática**: Antes de cargar nuevos datos, se limpian todas las listas existentes
- **Manejo de errores**: Errores específicos para problemas de formato vs problemas de archivo
- **Logging detallado**: Información completa en los logs del servidor
- **Estadísticas**: Retorna el número de elementos cargados de cada tipo
- **Thread-safe**: La operación es segura para múltiples hilos

## Ejemplo de uso con cURL

```bash
# Recargar todos los datos
curl -X POST http://localhost:8080/api/data-reload/recargar-todos

# Verificar salud del controlador
curl -X POST http://localhost:8080/api/data-reload/health
```

## Ejemplo de uso con JavaScript/Fetch

```javascript
// Recargar todos los datos
async function recargarDatos() {
    try {
        const response = await fetch('http://localhost:8080/api/data-reload/recargar-todos', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        const resultado = await response.json();
        
        if (resultado.exito) {
            console.log('✅ Recarga exitosa:', resultado);
            console.log(`📊 Datos cargados: ${resultado.cantidadPedidos} pedidos, ${resultado.cantidadCamiones} camiones`);
        } else {
            console.error('❌ Error en recarga:', resultado.mensaje);
        }
    } catch (error) {
        console.error('❌ Error de conexión:', error);
    }
}

// Llamar la función
recargarDatos();
```

## Consideraciones importantes

1. **Tiempo de ejecución**: La recarga puede tomar varios segundos dependiendo del tamaño de los archivos
2. **Datos en memoria**: Todos los datos se mantienen en memoria, no se persisten en base de datos
3. **Parámetros**: Los archivos de pedidos y bloqueos se cargan según los parámetros actuales (año/mes)
4. **División de pedidos**: Los pedidos grandes se dividen automáticamente según la capacidad de los camiones
5. **Validación**: Se validan las coordenadas de los pedidos y se corrigen si están fuera del rango del mapa

## Logs del servidor

Durante la recarga, el servidor mostrará logs detallados como:

```
🔄 Iniciando recarga completa de datos del sistema...
🧹 Limpiando datos existentes...
✅ Datos existentes limpiados
🏢 Recargando almacenes...
🚛 Recargando camiones...
📦 Recargando pedidos...
🔧 Recargando averías...
🔧 Recargando mantenimientos...
🚧 Recargando bloqueos...
✅ Recarga de datos completada exitosamente
📊 Estadísticas de recarga: DataReloadResult{almacenes=3, camiones=20, pedidos=150, averias=5, mantenimientos=12, bloqueos=8}
``` 