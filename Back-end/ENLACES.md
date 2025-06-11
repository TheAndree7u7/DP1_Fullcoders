# Enlaces y Dependencias del Back-end

## Dependencias y Referencias

### 1. Controladores (`src/main/java/com/plg/controller/`)
- **`SimulacionController.java`**
  - Expone los endpoints principales para la simulación logística.
  - Gestiona la entrada y salida de datos entre el frontend y los servicios del backend.
  - Métodos destacados:
    - `calcularSimulacion()`: Recibe parámetros de simulación (por ejemplo, configuración de camiones, pedidos, bloqueos, etc.), ejecuta la lógica principal (invoca a `SimulacionService`), y retorna los resultados de la simulación (rutas, asignaciones, métricas, etc.).
    - `getEstadoSimulacion()`: Devuelve el estado actual de la simulación, incluyendo la posición y estado de cada camión, pedidos pendientes y entregados, rutas activas, hora de simulación, bloqueos activos, etc. Es fundamental para refrescar la UI del frontend en tiempo real.
    - `actualizarSimulacion()`: Permite avanzar la simulación (por ejemplo, avanzar una hora o un paso), recalcula rutas si hay bloqueos o cambios, y actualiza el estado global. Puede disparar eventos de mantenimiento, averías o bloqueos dinámicos.
  - Consumido por: `Front-end/src/services/simulacionApiService.ts` (todas las llamadas a simulación pasan por aquí, centralizando la comunicación entre UI y lógica de negocio).
  - Es usado en:
    - `Front-end/src/views/SimulacionSemanal.tsx` (para iniciar/calcular/actualizar simulación semanal, mostrar resultados y métricas)
    - `Front-end/src/context/SimulacionContext.tsx` (para mantener sincronizado el estado global de la simulación, refrescar datos periódicamente, manejar errores y estados de carga)
  - Endpoints:
    - POST `/api/simulacion/calcular` → Lógica de cálculo de rutas y asignación de pedidos. Recibe parámetros de configuración y retorna el mejor individuo (solución óptima).
    - GET `/api/simulacion/estado` → Consulta el estado actual de la simulación (usado para refrescar la UI, mostrar progreso, actualizar métricas en tiempo real).
    - POST `/api/simulacion/actualizar` → Avanza la simulación y actualiza el estado de camiones, pedidos, rutas, bloqueos, etc. Puede devolver eventos ocurridos en ese paso (ej. averías, bloqueos nuevos, entregas realizadas).
  - Detalles adicionales:
    - Cada endpoint valida los datos de entrada usando anotaciones y lógica personalizada. Si hay errores, responde con un DTO de error estructurado.
    - Los endpoints están documentados con Swagger/OpenAPI, permitiendo su prueba y consulta desde `/api-docs`.
    - El controlador puede lanzar excepciones personalizadas (ej. `SimulacionException`) que son capturadas por un handler global para mantener la consistencia de los errores.

### 2. Entidades y DTOs
- **`entity/`**
  - Contiene todas las clases que representan la estructura de la base de datos y el dominio del problema. Cada entidad está anotada con JPA (`@Entity`) y define relaciones (OneToMany, ManyToOne, etc.).
  - Consumido por: `Front-end/src/types.ts` (los tipos TypeScript reflejan la estructura de estas entidades para mantener la compatibilidad de datos; cualquier cambio aquí debe reflejarse en el frontend).
  - Es usado en:
    - `Front-end/src/components/Mapa.tsx` (visualización de rutas, almacenes, camiones; por ejemplo, renderiza la posición de cada camión y almacén en el mapa)
    - `Front-end/src/components/TablaPedidos.tsx` (listado y gestión de pedidos, mostrando estado, hora límite, camión asignado, etc.)
    - `Front-end/src/components/CardCamion.tsx` (estado y progreso de camiones, incluyendo pedidos asignados, incidencias, bloqueos en ruta)
  - Entidades principales:
    - `Almacen.java`: Define almacenes, tipo (central/intermedio), ubicación, capacidad y estado. Puede tener relación con pedidos y camiones.
    - `Camion.java`: Define camiones, capacidad, estado, ubicación, historial de entregas y asignaciones. Puede tener relación con mantenimientos y averías.
    - `Pedido.java`: Define pedidos, cliente, volumen, hora límite, estado, camión asignado. Puede tener relación con almacenes y camiones.
    - `Mapa.java`: Representa el grafo de la ciudad, nodos, conexiones y distancias. Es fundamental para el cálculo de rutas.
    - `Bloqueo.java`, `Averia.java`, `Mantenimiento.java`: Modelan eventos que afectan la logística. Incluyen información de tiempo, ubicación, duración, impacto, etc.
  - DTOs: Estructuras intermedias para transferir datos entre backend y frontend, asegurando que solo viajen los datos necesarios y en el formato correcto. Ejemplo: `SimulacionDTO`, `PedidoDTO`, `CamionDTO`, etc. Los DTOs pueden agrupar información de varias entidades y transformar los datos para facilitar su consumo en la UI.
  - Detalles adicionales:
    - Las entidades pueden tener validaciones a nivel de campo (ej. `@NotNull`, `@Size`, etc.) y lógica de negocio en métodos auxiliares.
    - Los DTOs suelen ser mapeados desde las entidades usando librerías como MapStruct o manualmente en los servicios.

### 3. Servicios
- **`service/`**
  - Implementan la lógica de negocio y orquestan la interacción entre entidades, repositorios y algoritmos. Cada servicio suele estar anotado con `@Service` y puede ser inyectado en controladores y otros servicios.
  - Consumido por: `Front-end/src/services/simulacionApiService.ts` (a través de los controladores, nunca directamente).
  - Es usado en:
    - `Front-end/src/context/SimulacionContext.tsx` (para cargar y actualizar datos de simulación, iniciar, pausar, avanzar, etc.)
    - `Front-end/src/views/SimulacionSemanal.tsx` (para ejecutar simulaciones y mostrar resultados, métricas, incidencias)
  - Servicios principales:
    - `SimulacionService.java`: Orquesta la simulación, ejecuta el algoritmo genético, gestiona el ciclo de vida de la simulación, controla el avance del tiempo, actualiza el estado global, y coordina la interacción entre camiones, pedidos, almacenes y eventos.
    - `RutaService.java`: Calcula rutas óptimas usando algoritmos como Dijkstra, A*, o heurísticas personalizadas. Detecta bloqueos, calcula desvíos, optimiza recorridos y minimiza costos.
    - `PedidoService.java`: Gestiona la creación, asignación y actualización de pedidos. Valida datos, asigna pedidos a camiones, actualiza estados y registra entregas.
    - `CamionService.java`: Gestiona el estado, asignación y seguimiento de camiones. Controla mantenimientos, incidencias, actualiza ubicación y estado operativo.
    - Otros servicios: gestión de almacenes (stock, capacidad), mantenimientos (programados y correctivos), averías (registro y resolución), bloqueos (creación y eliminación dinámica), etc.
  - Detalles adicionales:
    - Los servicios pueden lanzar excepciones personalizadas que son capturadas por los controladores.
    - Pueden usar utilidades y algoritmos auxiliares ubicados en el paquete `utils/`.
    - Los servicios suelen ser el lugar donde se implementan las reglas de negocio más complejas y la lógica de integración entre entidades.

### 4. Configuración
- **`config/`**
  - Contiene clases de configuración global del sistema, anotadas con `@Configuration`.
  - Consumido por: `Front-end/src/services/api.ts` (para saber cómo comunicarse con el backend, CORS, etc.)
  - Es usado en:
    - `Front-end/src/services/simulacionApiService.ts` (para definir la URL base y headers de las peticiones, especialmente en desarrollo y despliegue)
  - Configuraciones:
    - CORS: Permite que el frontend (en otro puerto/dominio) acceda a la API. Puede estar configurado para aceptar solo ciertos orígenes y métodos.
    - Seguridad: Configura autenticación/autorización si aplica (por ejemplo, con Spring Security, JWT, etc.).
    - Conexiones: Configuración de base de datos, rutas de archivos, límites de tamaño, etc.
    - Swagger/OpenAPI: Documentación automática de la API, accesible en `/api-docs`.
  - Detalles adicionales:
    - Las configuraciones pueden ser diferentes para desarrollo, testing y producción (usando perfiles de Spring).
    - Es importante mantener sincronizados los orígenes permitidos en CORS con la URL real del frontend.

## Variables de Entorno
- **`application.properties`**
  - Archivo central de configuración de la aplicación. Puede tener variantes por entorno (`application-dev.properties`, etc.).
  - Variables clave:
    - `FRONTEND_URL`: Define el origen permitido para CORS (debe coincidir con la URL del frontend en desarrollo y producción). Si no está bien configurado, el frontend no podrá comunicarse con la API.
    - `API_VERSION`: Versión de la API, útil para mantener compatibilidad y documentar cambios. Puede ser usada para mostrar la versión en la UI o en logs.
    - `DB_CONNECTION`: Cadena de conexión a la base de datos (H2, PostgreSQL, etc.). Incluye usuario, contraseña, URL, dialecto, etc.
    - Otros: rutas de archivos de datos (almacenes, pedidos, bloqueos, etc.), límites de tamaño de archivos, configuración de logs, zona horaria, etc.
  - Es usado en: configuración de CORS, JPA, logs, rutas de datos, etc. Cambios aquí pueden requerir reiniciar la aplicación.

## Notas Importantes
1. Cualquier cambio en la estructura de respuesta (DTOs, entidades) debe ser comunicado al frontend para evitar incompatibilidades. Se recomienda usar herramientas de documentación automática (Swagger) y mantener reuniones de sincronización entre equipos.
2. Los DTOs deben mantenerse sincronizados con los tipos del frontend (`types.ts`). Si se agrega un campo nuevo, debe reflejarse en ambos lados.
3. Las validaciones de datos (campos requeridos, formatos, rangos) deben coincidir en backend y frontend para evitar errores de usuario. Usar anotaciones de validación en entidades y DTOs, y validaciones en formularios del frontend.
4. Si se agregan nuevos endpoints, documentarlos en Swagger y actualizar los servicios del frontend. Probar los endpoints con Postman o Swagger UI antes de integrarlos en la UI.

## Manejo de Errores
- Los errores deben seguir el formato estándar:
  ```java
  {
    "status": number, // Código HTTP o de aplicación
    "message": string, // Descripción del error
    "errors": any[]    // Detalles adicionales (opcional)
  }
  ```
  - Es usado en: Todos los controladores (mediante controladores de excepciones globales o locales, por ejemplo, usando `@ControllerAdvice`).
- Los códigos de error deben ser consistentes con el frontend para que la UI pueda mostrar mensajes claros y tomar acciones correctas (por ejemplo, mostrar un toast de error, bloquear botones, etc.).
- Ejemplo: Si un pedido no es válido (por volumen, hora límite, etc.), el backend debe retornar un 400 con un mensaje descriptivo y el frontend debe mostrarlo al usuario en el formulario correspondiente.
- Se recomienda registrar los errores en logs para facilitar el debugging y la trazabilidad.

## Seguridad
- CORS configurado para el dominio del frontend (solo se permite el origen definido en `application.properties`). Si el frontend cambia de dominio o puerto, debe actualizarse aquí.
- Validación de datos en todos los endpoints (usando anotaciones de validación y lógica en los servicios). No confiar nunca en los datos recibidos del frontend.
- Sanitización de inputs para evitar inyecciones o datos maliciosos (por ejemplo, usando librerías de sanitización o validando manualmente strings y números).
- Si se implementa autenticación, los tokens JWT o sesiones deben ser validados en cada petición. Los endpoints protegidos deben devolver 401/403 si el usuario no está autenticado/autorizado.
- Se recomienda usar HTTPS en producción para proteger la transmisión de datos sensibles.

## Base de Datos
- **Entidades JPA**
  - Cada entidad define la estructura de una tabla en la base de datos. Los cambios en las entidades pueden requerir migraciones o actualizaciones de la base de datos.
  - Las relaciones (OneToMany, ManyToOne, etc.) modelan la lógica del dominio (ej. un camión puede tener muchos pedidos asignados, un almacén puede tener muchos camiones asociados, etc.).
  - Cambios en las entidades pueden afectar a los tipos del frontend y a la estructura de los datos que se muestran en la UI. Es fundamental mantener la documentación y los tipos sincronizados.
  - Es usado en:
    - `Front-end/src/components/Mapa.tsx` (para mostrar rutas y ubicaciones, renderizar nodos y conexiones)
    - `Front-end/src/components/TablaPedidos.tsx` (para mostrar pedidos y su estado, filtrar por estado, camión, almacén, etc.)
    - `Front-end/src/components/CardCamion.tsx` (para mostrar el estado y progreso de los camiones, incidencias, bloqueos, mantenimientos)
  - Detalles adicionales:
    - Se recomienda usar migraciones controladas (Flyway, Liquibase) en producción para evitar inconsistencias.
    - Los datos de prueba pueden estar en archivos en `/resources/data/` y ser cargados automáticamente al iniciar la app.

## Testing
- Los tests deben considerar las respuestas esperadas por el frontend (estructura, tipos, mensajes de error, casos de éxito y error).
- Es usado en: `src/test/java/com/plg/controller/` (tests de endpoints y controladores, usando frameworks como JUnit y MockMvc)
- Mock de peticiones del frontend en tests de integración (`src/test/java/com/plg/integration/`). Permite simular escenarios reales y validar la integración completa.
- Se recomienda usar datos de prueba que cubran casos normales, límites y errores (por ejemplo, pedidos con volumen máximo, camiones sin capacidad, bloqueos en ruta, etc.).
- Los tests deben ejecutarse automáticamente en cada build para garantizar la calidad y evitar regresiones.

## Documentación
- Swagger/OpenAPI disponible en `/api-docs`.
  - Consumido por el frontend para autogenerar documentación y probar endpoints. Permite a los desarrolladores del frontend ver los parámetros, respuestas y ejemplos de cada endpoint.
  - Es usado en: `SimulacionController.java` (anotaciones Swagger para documentar cada endpoint y sus parámetros, ejemplos de request/response, posibles errores).
  - Mantener actualizado con cambios en endpoints, DTOs y respuestas. Es recomendable revisar la documentación antes de cada release.
  - Se recomienda incluir ejemplos de uso, descripciones claras y posibles errores en la documentación.

## Dependencias con Front-end

### Componentes que Consumen la API
1. **`SimulacionSemanal.tsx`**
   - Consume: `SimulacionController.java` (a través de `simulacionApiService.ts`)
   - Endpoints:
     - POST `/api/simulacion/calcular`: Inicia una nueva simulación semanal. Recibe parámetros de configuración y retorna el estado inicial de la simulación.
     - POST `/api/simulacion/actualizar`: Avanza la simulación y actualiza el estado global. Puede devolver eventos ocurridos en ese paso.
   - Recibe datos de rutas, camiones, pedidos y métricas para renderizar la UI principal. Actualiza la visualización del mapa, la tabla de pedidos y las tarjetas de camiones.
   - Puede mostrar mensajes de éxito/error según la respuesta del backend.

2. **`Mapa.tsx`**
   - Consume: Datos de `Mapa.java` y `Nodo.java` (a través del contexto y servicios)
   - Visualiza rutas, almacenes, camiones y bloqueos en el mapa. Usa los datos de coordenadas y conexiones para renderizar el grafo de la ciudad.
   - Se actualiza automáticamente cuando cambian los datos globales de simulación (por ejemplo, cuando un camión avanza o un bloqueo aparece/desaparece).
   - Puede mostrar rutas alternativas, incidencias en tiempo real y animaciones de movimiento de camiones.

3. **`TablaPedidos.tsx`**
   - Consume: Datos de `Pedido.java` (a través del contexto y servicios)
   - Muestra la lista de pedidos, su estado, camión asignado, hora límite, almacén de origen/destino, etc.
   - Permite filtrar, ordenar y ver detalles de cada pedido. Puede mostrar alertas si un pedido está retrasado o en riesgo.
   - Se actualiza automáticamente cuando cambian los datos de la simulación.

4. **`CardCamion.tsx`**
   - Consume: Datos de `Camion.java` (a través del contexto y servicios)
   - Muestra el estado, ruta, pedidos asignados y progreso de cada camión. Puede mostrar incidencias, bloqueos y mantenimientos asociados.
   - Permite visualizar el historial de entregas, el estado de mantenimiento y la eficiencia del camión.
   - Se actualiza en tiempo real según avanza la simulación.

### Contextos que Interactúan
1. **`SimulacionContext.tsx`**
   - Consume: `SimulacionController.java` (a través de los servicios de API)
   - Maneja el estado global de la simulación (camiones, pedidos, rutas, hora, métricas, incidencias, bloqueos, etc.)
   - Sincroniza datos con el backend en cada acción relevante (inicio, avance, actualización, error). Puede manejar polling o WebSocket para actualizaciones en tiempo real.
   - Expone funciones y datos a todos los componentes hijos para mantener la UI actualizada y reactiva.
   - Gestiona estados de carga, errores y mensajes globales.

### Servicios que Comunican
1. **`simulacionApiService.ts`**
   - Consume: Todos los endpoints de `SimulacionController.java` (calcular, actualizar, obtener estado, etc.)
   - Maneja la comunicación con el backend, incluyendo headers, manejo de errores, transformación de respuestas y reintentos si es necesario.
   - Procesa respuestas y errores para que el contexto y los componentes reciban datos limpios y estructurados. Puede transformar los datos del backend al formato esperado por la UI.
   - Permite desacoplar la lógica de comunicación de la lógica de presentación y estado. Facilita el testing y el mantenimiento.
   - Puede incluir lógica para mostrar mensajes globales, logs de errores y métricas de uso.

---

**Este archivo es clave para entender cómo se relacionan los módulos del backend y frontend, qué archivos dependen unos de otros y cómo mantener la compatibilidad y robustez del sistema. Si necesitas rastrear un bug, agregar una funcionalidad o entender el impacto de un cambio, consulta aquí primero.**