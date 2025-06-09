# Narrativa de Interacción Frontend-Backend

## Introducción
Este documento describe detalladamente la interacción entre el frontend (desarrollado en React/TypeScript) y el backend (desarrollado en Java/Spring Boot) del sistema de simulación de rutas de camiones. La arquitectura implementa un patrón cliente-servidor donde el backend actúa como un servicio de procesamiento y el frontend como interfaz de usuario interactiva.

## Flujo de Ejecución

### 1. Inicio del Sistema

#### Backend (Servidor)
1. **Inicialización del Servidor**
   - El servidor Spring Boot se inicia en `localhost:8085`
   - Se cargan las configuraciones del sistema
   - Se inicializan los servicios necesarios
   - El servidor queda en estado de espera (listening)

2. **Configuración de Endpoints**
   ```java
   @RestController
   @RequestMapping("/api")
   @CrossOrigin(origins = "http://localhost:8085")
   public class SimulacionController {
       // Endpoints configurados y listos para recibir peticiones
   }
   ```

#### Frontend (Cliente)
1. **Carga Inicial**
   - La aplicación React se inicia
   - Se monta el contexto de simulación (`SimulacionContext`)
   - Se inicializan los estados globales
   - La interfaz muestra el estado inicial de espera

### 2. Interacción Usuario-Sistema

#### Estado de Espera
1. **Backend**
   - Permanece en estado de espera
   - Los endpoints están activos pero no procesan datos
   - Mantiene la configuración inicial del sistema

2. **Frontend**
   - Muestra la interfaz de usuario
   - Presenta un botón de inicio
   - El usuario puede ver la configuración inicial
   - No hay comunicación activa con el backend

#### Inicio de la Simulación
1. **Acción del Usuario**
   - El usuario hace clic en el botón de inicio
   - Se dispara el evento de inicio de simulación

2. **Frontend**
   ```typescript
   // En SimulacionContext.tsx
   const iniciarSimulacion = async () => {
     setCargando(true);
     try {
       // Primera comunicación con el backend
       await cargarAlmacenes();
       await cargarDatos(true);
     } catch (error) {
       console.error("Error al iniciar simulación:", error);
     } finally {
       setCargando(false);
     }
   };
   ```

### 3. Comunicación Bidireccional

#### Frontend → Backend
1. **Petición de Almacenes**
   ```typescript
   // En almacenApiService.ts
   export const getAlmacenes = async (): Promise<Almacen[]> => {
     const response = await axios.get<AlmacenBackend[]>(
       'http://localhost:8085/api/almacenes'
     );
     // Transformación de datos
     return response.data.map(a => ({
       id: a.nombre.replace(/\s+/g, '-').toLowerCase(),
       nombre: a.nombre,
       tipo: a.tipo === 'CENTRAL' ? 'CENTRAL' : 'INTERMEDIO',
       coordenada: { 
         x: a.coordenada.columna, 
         y: a.coordenada.fila 
       },
       activo: a.activo,
     }));
   };
   ```

2. **Petición de Mejor Individuo**
   ```typescript
   // En simulacionApiService.ts
   export async function getMejorIndividuo(): Promise<Individuo> {
     const response = await fetch(`${API_BASE_URL}/mejor`);
     // Validación y procesamiento de respuesta
     return await response.json();
   }
   ```

#### Backend → Frontend
1. **Respuesta de Almacenes**
   ```json
   [
     {
       "coordenada": {
         "fila": 10,
         "columna": 15
       },
       "nombre": "Almacén Central",
       "tipo": "CENTRAL",
       "activo": true
     }
   ]
   ```

2. **Respuesta de Simulación**
   ```json
   {
     "cromosoma": [
       {
         "camion": {
           "codigo": "CAM001",
           "capacidad": 1000
         },
         "nodos": [
           {
             "coordenada": {
               "x": 10,
               "y": 15
             },
             "tipo": "ALMACEN"
           }
         ],
         "destino": {
           "x": 20,
           "y": 25
         },
         "pedidos": [
           {
             "id": "PED001",
             "origen": {
               "x": 10,
               "y": 15
             },
             "destino": {
               "x": 20,
               "y": 25
             },
             "peso": 100,
             "volumen": 50,
             "prioridad": 1
           }
         ]
       }
     ],
     "fitness": 0.85
   }
   ```

### 4. Proceso de Simulación

#### Ciclo de Actualización
1. **Frontend**
   ```typescript
   const avanzarHora = async () => {
     if (esperandoActualizacion) return;

     // Actualización local
     const nuevosCamiones = camiones.map((camion) => {
       const ruta = rutasCamiones.find(r => r.id === camion.id);
       if (!ruta) return camion;

       const siguientePaso = camion.porcentaje + INCREMENTO_PORCENTAJE;
       // Lógica de actualización...
     });

     // Control de sincronización
     const quedan = nodosRestantesAntesDeActualizar - 1;
     if (quedan <= 0) {
       setEsperandoActualizacion(true);
       await cargarDatos(false);
     }
   };
   ```

2. **Backend**
   - Procesa las peticiones de actualización
   - Ejecuta el algoritmo genético
   - Devuelve los nuevos datos optimizados

### 5. Manejo de Errores y Sincronización

#### Frontend
1. **Validación de Respuestas**
   ```typescript
   if (!contentType || !contentType.includes("application/json")) {
     throw new Error("La respuesta del servidor no es JSON válido");
   }
   ```

2. **Control de Estado**
   ```typescript
   const [esperandoActualizacion, setEsperandoActualizacion] = useState<boolean>(false);
   const [nodosRestantesAntesDeActualizar, setNodosRestantesAntesDeActualizar] = 
     useState<number>(NODOS_PARA_ACTUALIZACION);
   ```

#### Backend
1. **Manejo de Excepciones**
   ```java
   @ExceptionHandler(Exception.class)
   public ResponseEntity<ErrorResponse> handleException(Exception e) {
     // Lógica de manejo de errores
   }
   ```

2. **Validación de Datos**
   ```java
   @Validated
   public class SimulacionRequest {
     // Validaciones de datos
   }
   ```

## Conclusión
El sistema implementa una arquitectura cliente-servidor robusta donde:
1. El backend actúa como un servicio de procesamiento en espera
2. El frontend proporciona la interfaz de usuario interactiva
3. La comunicación se realiza mediante peticiones HTTP RESTful
4. Los datos se transforman y validan en ambas direcciones
5. Se mantiene un estado sincronizado entre cliente y servidor
6. Se implementa un sistema de manejo de errores robusto
7. La simulación avanza de manera controlada y eficiente

Esta arquitectura permite una separación clara de responsabilidades y un flujo de datos bien definido, facilitando el mantenimiento y la escalabilidad del sistema. 