# Enlaces y Dependencias del Back-end

## Dependencias y Referencias

### 1. Controladores (`src/main/java/com/plg/controller/`)
- **`SimulacionController.java`**
  - Consumido por: `Front-end/src/services/simulacionApiService.ts`
  - Es usado en:
    - `Front-end/src/views/SimulacionSemanal.tsx`
    - `Front-end/src/context/SimulacionContext.tsx`
  - Endpoints:
    - POST `/api/simulacion/calcular` → `SimulacionSemanal.tsx`
    - GET `/api/simulacion/estado` → `SimulacionContext.tsx`
    - POST `/api/simulacion/actualizar` → `SimulacionSemanal.tsx`

### 2. Entidades y DTOs
- **`entity/`**
  - Consumido por: `Front-end/src/types.ts`
  - Es usado en:
    - `Front-end/src/components/Mapa.tsx`
    - `Front-end/src/components/TablaPedidos.tsx`
    - `Front-end/src/components/CardCamion.tsx`
  - Entidades principales:
    - `Almacen.java` → Gestión de almacenes
    - `Camion.java` → Información de camiones
    - `Pedido.java` → Gestión de pedidos
    - `Mapa.java` → Representación del mapa

### 3. Servicios
- **`service/`**
  - Consumido por: `Front-end/src/services/simulacionApiService.ts`
  - Es usado en:
    - `Front-end/src/context/SimulacionContext.tsx`
    - `Front-end/src/views/SimulacionSemanal.tsx`
  - Servicios principales:
    - Cálculo de rutas
    - Gestión de pedidos
    - Simulación de entregas

### 4. Configuración
- **`config/`**
  - Consumido por: `Front-end/src/services/api.ts`
  - Es usado en:
    - `Front-end/src/services/simulacionApiService.ts`
  - Configuraciones:
    - CORS
    - Seguridad
    - Conexiones

## Variables de Entorno
- **`application.properties`**
  - `FRONTEND_URL`: URL del frontend para CORS
    - Es usado en: Configuración de CORS
  - `API_VERSION`: Versión de la API
    - Es usado en: `SimulacionController.java`
  - `DB_CONNECTION`: Conexión a base de datos
    - Es usado en: Configuración de JPA

## Notas Importantes
1. Cualquier cambio en la estructura de respuesta debe ser comunicado al frontend
2. Los DTOs deben mantenerse sincronizados con los tipos del frontend
3. Las validaciones deben coincidir con las del frontend

## Manejo de Errores
- Los errores deben seguir el formato:
  ```java
  {
    "status": number,
    "message": string,
    "errors": any[]
  }
  ```
  - Es usado en: Todos los controladores
- Los códigos de error deben ser consistentes con el frontend

## Seguridad
- CORS configurado para el dominio del frontend
  - Es usado en: Configuración de seguridad
- Validación de datos
  - Es usado en: Todos los controladores
- Sanitización de inputs
  - Es usado en: Todos los controladores

## Base de Datos
- **Entidades JPA**
  - Define modelos que se reflejan en los tipos del frontend
  - Es usado en:
    - `Front-end/src/components/Mapa.tsx`
    - `Front-end/src/components/TablaPedidos.tsx`
    - `Front-end/src/components/CardCamion.tsx`
  - Cambios en las entidades pueden afectar a los tipos del frontend

## Testing
- Los tests deben considerar las respuestas esperadas por el frontend
  - Es usado en: `src/test/java/com/plg/controller/`
- Mock de peticiones del frontend en tests de integración
  - Es usado en: `src/test/java/com/plg/integration/`

## Documentación
- Swagger/OpenAPI en `/api-docs`
  - Consumido por el frontend para documentación
  - Es usado en: `SimulacionController.java`
  - Mantener actualizado con cambios en endpoints

## Dependencias con Front-end

### Componentes que Consumen la API
1. **`SimulacionSemanal.tsx`**
   - Consume: `SimulacionController.java`
   - Endpoints:
     - POST `/api/simulacion/calcular`
     - POST `/api/simulacion/actualizar`

2. **`Mapa.tsx`**
   - Consume: Datos de `Mapa.java` y `Nodo.java`
   - Visualización de rutas y ubicaciones

3. **`TablaPedidos.tsx`**
   - Consume: Datos de `Pedido.java`
   - Visualización y gestión de pedidos

4. **`CardCamion.tsx`**
   - Consume: Datos de `Camion.java`
   - Visualización de estado de camiones

### Contextos que Interactúan
1. **`SimulacionContext.tsx`**
   - Consume: `SimulacionController.java`
   - Maneja estado global de simulación
   - Sincroniza datos con el backend

### Servicios que Comunican
1. **`simulacionApiService.ts`**
   - Consume: Todos los endpoints de `SimulacionController.java`
   - Maneja la comunicación con el backend
   - Procesa respuestas y errores 