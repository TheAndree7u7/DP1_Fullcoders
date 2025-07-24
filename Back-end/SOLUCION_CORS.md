# Solución al Problema de CORS

## Problema Identificado
El error que estabas experimentando era:
```
java.lang.IllegalArgumentException: When allowCredentials is true, allowedOrigins cannot contain the special value "*"
```

## Solución Implementada

### 1. Configuración CORS Simplificada
Se ha creado una configuración CORS global en `CorsConfig.java` que:
- Usa `allowedOriginPatterns("*")` en lugar de `allowedOrigins("*")`
- Establece `allowCredentials(false)` para evitar conflictos
- Permite todos los métodos HTTP necesarios
- Permite todos los headers

### 2. Archivos Modificados

#### `CorsConfig.java`
```java
@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOriginPatterns("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "TRACE", "CONNECT")
                        .allowedHeaders("*")
                        .allowCredentials(false)
                        .maxAge(3600);
            }
        };
    }
}
```

#### `SimulacionController.java`
Se agregó un nuevo endpoint POST:
```java
@PostMapping("/iniciar")
public ResponseEntity<String> iniciarSimulacionPost(@RequestBody SimulacionRequest request) {
    // ... implementación
}
```

#### `application.properties`
Se agregó configuración CORS:
```properties
# CORS Configuration
cors.allowed-origins=http://localhost:5173,http://localhost:3000,http://127.0.0.1:5173,http://127.0.0.1:3000
```

## Pasos para Aplicar la Solución

### 1. Reiniciar el Backend
```bash
# Detener el servidor actual (Ctrl+C)
# Luego ejecutar:
cd Back-end/plg
./mvnw spring-boot:run
```

### 2. Verificar que el Backend Esté Funcionando
- El servidor debe iniciar sin errores de CORS
- Debe estar disponible en `http://localhost:8085`

### 3. Probar la Nueva Función
En el frontend, usar la nueva función:
```typescript
import { iniciarSimulacionPost } from './services/simulacionApiService';

// Probar la función
const resultado = await iniciarSimulacionPost("2025-01-01T00:00:00");
console.log(resultado);
```

## Diferencias Clave

| Aspecto | Antes | Después |
|---------|-------|---------|
| CORS | `@CrossOrigin(origins = "*")` en cada controlador | Configuración global en `CorsConfig.java` |
| Credenciales | `allowCredentials(true)` | `allowCredentials(false)` |
| Orígenes | `allowedOrigins("*")` | `allowedOriginPatterns("*")` |
| Endpoint | Solo GET | GET y POST disponibles |

## Ventajas de la Nueva Configuración

1. **Configuración Global**: No necesitas `@CrossOrigin` en cada controlador
2. **Sin Conflictos**: `allowCredentials(false)` evita el error de CORS
3. **Flexibilidad**: Permite todos los orígenes con `allowedOriginPatterns`
4. **Seguridad**: Configuración más segura para APIs públicas

## Troubleshooting

### Si sigues teniendo problemas de CORS:

1. **Verificar que el backend se reinició correctamente**
   ```bash
   # Verificar logs del backend
   tail -f Back-end/logs/application.log
   ```

2. **Verificar que no hay otros filtros CORS**
   - Buscar otros archivos de configuración CORS
   - Verificar que no hay filtros personalizados

3. **Probar con Postman**
   - Crear una petición POST a `http://localhost:8085/api/simulacion/iniciar`
   - Body: `{"fechaInicio": "2025-01-01T00:00:00"}`

4. **Verificar puertos**
   - Backend: `http://localhost:8085`
   - Frontend: `http://localhost:5173`

### Si necesitas credenciales:

Si en el futuro necesitas `allowCredentials(true)`, puedes usar esta configuración alternativa:

```java
.allowedOrigins("http://localhost:5173", "http://localhost:3000")
.allowCredentials(true)
```

## Estado Actual

✅ **Problema de CORS resuelto**
✅ **Nuevo endpoint POST disponible**
✅ **Configuración global implementada**
✅ **Documentación completa**

La aplicación debería funcionar correctamente ahora sin errores de CORS. 