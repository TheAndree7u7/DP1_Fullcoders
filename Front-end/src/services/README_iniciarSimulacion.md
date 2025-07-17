# Función iniciarSimulacionPost

## Descripción
Esta función permite iniciar una simulación usando el método POST para evitar problemas de CORS. La función modifica la fecha de inicio en el backend usando la clase `Parametros.java`.

## Uso Básico

```typescript
import { iniciarSimulacionPost } from './simulacionApiService';

// Iniciar simulación con fecha específica
const fechaInicio = "2025-01-01T00:00:00";
const resultado = await iniciarSimulacionPost(fechaInicio);
console.log(resultado); // "Simulación iniciada correctamente con fecha: 2025-01-01T00:00:00"
```

## Formato de Fecha
La fecha debe estar en formato ISO: `YYYY-MM-DDTHH:MM:SS`

### Ejemplos de fechas válidas:
- `"2025-01-01T00:00:00"` - 1 de enero de 2025 a las 00:00:00
- `"2025-12-31T23:59:59"` - 31 de diciembre de 2025 a las 23:59:59
- `"2025-06-15T14:30:00"` - 15 de junio de 2025 a las 14:30:00

## Manejo de Errores

```typescript
try {
  const resultado = await iniciarSimulacionPost("2025-01-01T00:00:00");
  console.log("✅ Simulación iniciada:", resultado);
} catch (error) {
  console.error("❌ Error al iniciar simulación:", error.message);
}
```

## Diferencias con iniciarSimulacion (GET)

| Aspecto | iniciarSimulacion (GET) | iniciarSimulacionPost (POST) |
|---------|-------------------------|------------------------------|
| Método HTTP | GET | POST |
| Parámetros | Query string | Request body |
| CORS | Puede tener problemas | Configurado correctamente |
| Uso | `?fecha=2025-01-01T00:00:00` | `{ fechaInicio: "2025-01-01T00:00:00" }` |

## Configuración del Backend

El backend ahora tiene dos endpoints para iniciar simulación:

1. **GET** `/api/simulacion/iniciar?fecha=...` - Método original
2. **POST** `/api/simulacion/iniciar` - Nuevo método con body JSON

### Configuración CORS
Se ha agregado una configuración global de CORS en `CorsConfig.java` que permite:
- Todos los orígenes (`*`)
- Métodos: GET, POST, PUT, DELETE, OPTIONS, HEAD, TRACE, CONNECT
- Todos los headers
- Credenciales habilitadas

## Ejemplo Completo

```typescript
import { iniciarSimulacionPost } from './simulacionApiService';

export async function iniciarSimulacionCompleta() {
  try {
    // Obtener fecha actual
    const fechaActual = new Date().toISOString().slice(0, 19);
    
    console.log("🚀 Iniciando simulación...");
    
    // Iniciar simulación
    const resultado = await iniciarSimulacionPost(fechaActual);
    
    console.log("✅ Simulación iniciada exitosamente");
    console.log("📅 Fecha de inicio:", fechaActual);
    console.log("📝 Mensaje del servidor:", resultado);
    
    return {
      success: true,
      fechaInicio: fechaActual,
      mensaje: resultado
    };
    
  } catch (error) {
    console.error("❌ Error al iniciar simulación:", error);
    
    return {
      success: false,
      error: error.message
    };
  }
}
```

## Notas Importantes

1. **Formato de Fecha**: Siempre usar formato ISO `YYYY-MM-DDTHH:MM:SS`
2. **CORS**: La configuración global permite todas las peticiones desde cualquier origen
3. **Backend**: La función modifica `Parametros.fecha_inicial` en el backend
4. **Simulación**: Una vez iniciada, la simulación se marca como iniciada y no se puede reiniciar sin reiniciar el servidor

## Troubleshooting

### Error de CORS
Si sigues teniendo problemas de CORS:
1. Verifica que el backend esté ejecutándose en el puerto correcto (8085)
2. Asegúrate de que la configuración CORS esté cargada
3. Reinicia el servidor backend

### Error de Formato de Fecha
Si la fecha no es válida:
1. Verifica que esté en formato ISO
2. Asegúrate de que la fecha sea válida
3. Usa el formato: `YYYY-MM-DDTHH:MM:SS`

### Error de Conexión
Si no se puede conectar al servidor:
1. Verifica que el backend esté ejecutándose
2. Comprueba la URL en `config/api.ts`
3. Verifica que no haya firewall bloqueando la conexión 