# EXPLICACION_DETALLADO.md

## Índice
1. Introducción
2. Orden y flujo general del proyecto
3. Back-end: descripción de archivos y funciones en orden de ejecución
4. Front-end: descripción de archivos y funciones en orden de ejecución
5. Ejemplo de flujo completo (petición a simulación)
6. Recomendaciones para navegar el código

---

## 1. Introducción

Este documento explica en detalle el flujo de ejecución del proyecto, describiendo los archivos y funciones principales de cada módulo (back-end y front-end) en el orden en que intervienen durante el ciclo de vida de la aplicación. El objetivo es que cualquier persona pueda seguir el recorrido de una petición o funcionalidad desde el inicio hasta el final, entendiendo el rol de cada archivo y función.

---

## 2. Orden y flujo general del proyecto

### 2.1. Inicio
- El usuario ejecuta el backend (`mvnw spring-boot:run`) y el frontend (`npm run dev`).
- El frontend se conecta al backend y solicita datos iniciales.

### 2.2. Flujo típico
1. El usuario interactúa con la interfaz (ej. inicia una simulación).
2. El frontend realiza una petición HTTP a la API REST del backend.
3. El backend procesa la petición, ejecuta lógica de negocio y responde.
4. El frontend recibe la respuesta, actualiza el estado y renderiza la UI.

---

## 3. Back-end: descripción de archivos y funciones en orden de ejecución

### 3.1. Archivos raíz
- **pom.xml**: Configuración de dependencias y plugins Maven. Define todas las librerías necesarias (Spring Boot, JPA, H2, Swagger, etc.) y los plugins de build y test.
- **mvnw / mvnw.cmd**: Wrappers para ejecutar Maven sin necesidad de instalarlo globalmente.

### 3.2. Configuración y arranque
- **src/main/resources/application.properties**: Configuración de base de datos (H2), CORS, logging, rutas de archivos de datos, parámetros de simulación, etc. Aquí se definen los puertos, credenciales, rutas de archivos y límites de la aplicación.
- **src/main/java/com/plg/PlgApplication.java**: Punto de entrada. Inicializa Spring Boot, escanea componentes y lanza la aplicación. Aquí se puede encontrar el método `main` y la inicialización de la simulación.

### 3.3. Carga de datos y configuración
- **config/**: Incluye clases como `WebConfig.java` (CORS y recursos estáticos), `SwaggerConfig.java` (documentación de API), y configuraciones de seguridad y JPA. También puede incluir `DataLoader.java` para cargar datos iniciales desde archivos al iniciar la app.
- **factory/**: Clases como `PedidoFactory.java`, `CamionFactory.java`, etc., que leen archivos de datos (en `/resources/data/`) y crean entidades listas para ser persistidas o usadas en la simulación.

### 3.4. Capa de datos
- **entity/**: Entidades JPA que representan la estructura de la base de datos. Ejemplos:
  - `Pedido.java`: Define atributos, relaciones y estados de un pedido.
  - `Camion.java`: Define la flota, capacidad, estado y relación con pedidos.
  - `Almacen.java`: Define almacenes, tipo, ubicación y capacidad.
  - `Bloqueo.java`, `Averia.java`, `Mantenimiento.java`, `Nodo.java`, etc.: Modelan eventos y nodos del sistema.
- **dto/**: Objetos de transferencia de datos. Ejemplo: `SimulacionDTO.java`, `PedidoDTO.java`, etc. Se usan para estructurar la información que viaja entre backend y frontend, y entre capas internas.
- **repository/**: Interfaces JPA como `PedidoRepository.java`, `CamionRepository.java`, etc. Permiten operaciones CRUD y queries personalizadas sobre las entidades.

### 3.5. Lógica de negocio
- **service/**: Servicios como `SimulacionService.java`, `RutaService.java`, `PedidoService.java`, etc. Aquí se implementa la lógica principal: asignación de pedidos, ejecución de algoritmos genéticos, cálculo de rutas, validación de reglas de negocio, simulación de escenarios, etc. Ejemplo de funciones:
  - `SimulacionService.ejecutarSimulacion()`: Orquesta la simulación completa.
  - `RutaService.calcularRutaOptima()`: Calcula rutas considerando bloqueos y restricciones.
  - `PedidoService.crearPedido()`: Valida y registra un nuevo pedido.
- **utils/**: Algoritmos auxiliares y utilidades. Ejemplo:
  - `AlgoritmoGenetico.java`: Implementa el algoritmo genético para optimización de rutas.
  - `Simulacion.java`: Controla el ciclo de simulación, colas de eventos, sincronización, etc.
  - Utilidades de distancia, validación, generación de datos de prueba, etc.

### 3.6. Capa de presentación
- **controller/**: Controladores REST como `SimulacionController.java`, `PedidoController.java`, `AlmacenController.java`, etc. Cada uno expone endpoints (`/api/simulacion`, `/api/pedidos`, etc.), recibe peticiones, valida datos, llama a los servicios y retorna respuestas estructuradas (DTOs serializados a JSON). Ejemplo:
  - `SimulacionController.getMejorIndividuo()`: Endpoint para obtener la mejor solución de simulación.
  - `PedidoController.crearPedido()`: Endpoint para registrar un pedido nuevo.

### 3.7. Pruebas
- **src/test/**: Tests unitarios (por clase y función) y de integración (simulan peticiones reales a la API). Ejemplo: `SimulacionControllerTest.java`, `PedidoServiceTest.java`.

### 3.8. Flujo de una petición típica
1. El controlador recibe la petición (ej. `/api/simulacion/mejor`).
2. Valida los datos y llama al servicio correspondiente.
3. El servicio ejecuta la lógica (puede consultar entidades, ejecutar algoritmos, etc.).
4. El servicio retorna un DTO al controlador.
5. El controlador responde al frontend con el DTO serializado en JSON.

---

## 4. Front-end: descripción de archivos y funciones en orden de ejecución

### 4.1. Archivos raíz
- **package.json**: Lista todas las dependencias (React, Vite, Axios, etc.), scripts de desarrollo, build y test.
- **vite.config.ts**: Configuración de Vite (servidor de desarrollo, alias, plugins, etc.).

### 4.2. Punto de entrada y estructura
- **src/main.tsx**: Punto de entrada de React. Monta el componente raíz (`App`) en el DOM. Aquí se inicializan los contextos globales y se aplica el CSS global.
- **src/App.tsx**: Componente raíz. Define las rutas principales de la app (usando React Router), renderiza la barra de navegación y el layout general.

### 4.3. Contexto global
- **src/context/SimulacionContext.tsx**: Define el contexto global de simulación. Aquí se almacenan y exponen funciones como `iniciarSimulacion`, `cargarDatos`, `actualizarSimulacion`, así como el estado global (`camiones`, `pedidos`, `rutas`, `horaActual`, etc.). Provee el contexto a todos los componentes hijos.

### 4.4. Servicios de API
- **src/services/simulacionApiService.ts**: Define funciones como `getMejorIndividuo`, `calcularSimulacion`, `actualizarSimulacion`, que hacen peticiones HTTP a los endpoints del backend usando Axios o fetch. Maneja errores y transforma respuestas.
- **src/services/almacenApiService.ts**: Funciones para obtener almacenes desde el backend.
- **src/services/pedidoApiService.ts**: Funciones para crear, listar y actualizar pedidos.

### 4.5. Componentes principales
- **src/views/SimulacionSemanal.tsx**: Vista principal de la simulación semanal. Orquesta la carga de datos inicial, renderiza el mapa, la tabla de pedidos, el menú lateral y las métricas. Gestiona el ciclo de simulación semanal.
- **src/views/TiempoReal.tsx**: Vista para simulación en tiempo real. Similar a la anterior pero con actualizaciones más frecuentes y animaciones.
- **src/components/Mapa.tsx**: Renderiza el mapa de la ciudad, almacenes, camiones y rutas. Usa librerías de mapas o SVG. Recibe datos del contexto y actualiza la visualización en tiempo real.
- **src/components/TablaPedidos.tsx**: Muestra la lista de pedidos, su estado, cliente, volumen, hora límite, camión asignado, etc. Permite filtrar y ordenar.
- **src/components/CardCamion.tsx**: Tarjeta para cada camión, mostrando su estado, ruta, pedidos asignados y progreso.
- **src/components/MetricasRendimiento.tsx**: Muestra métricas como pedidos entregados, distancia recorrida, eficiencia, etc.
- **src/components/Navbar.tsx**: Barra superior de navegación, enlaces y branding.
- **src/components/RightMenu.tsx**: Menú lateral con controles para iniciar/pausar simulación, filtros, selección de escenarios, etc.

### 4.6. Tipos y utilidades
- **src/types.ts**: Define todos los tipos TypeScript usados en la app: `Pedido`, `Camion`, `Almacen`, `RutaCamion`, `Simulacion`, etc. Estos tipos deben estar sincronizados con los DTOs del backend.
- **src/data/informacion.ts**: Datos estáticos, ejemplos o mocks para desarrollo y pruebas.
- **src/logControl.ts**: Utilidad para logging y debugging en desarrollo.

### 4.7. Flujo de una acción típica
1. El usuario hace clic en "Iniciar Simulación" en la UI (`SimulacionSemanal.tsx`).
2. Se llama a una función del contexto (`SimulacionContext`) que usa un servicio de API para pedir datos al backend.
3. El backend responde con los datos de la simulación.
4. El contexto actualiza el estado global (`useState` o `useReducer`).
5. Los componentes que consumen el contexto (ej. Mapa, TablaPedidos, CardCamion) se re-renderizan automáticamente con los nuevos datos.
6. Si ocurre un error, se muestra un mensaje amigable al usuario y se registra en el log.

---

## 5. Ejemplo de flujo completo: petición de simulación

1. **Usuario**: Hace clic en "Iniciar Simulación" en la vista principal (`SimulacionSemanal.tsx`).
2. **Contexto**: `SimulacionContext.tsx` ejecuta `iniciarSimulacion()`, que a su vez llama a `getMejorIndividuo()` del servicio de API.
3. **Servicio**: `simulacionApiService.ts` realiza un `GET` a `/api/simulacion/mejor`.
4. **Backend**: `SimulacionController.java` recibe la petición y llama a `SimulacionService.java`.
5. **Lógica**: `SimulacionService` ejecuta el algoritmo genético, consulta entidades (`PedidoRepository`, `CamionRepository`, etc.), arma la respuesta (`SimulacionDTO`).
6. **Respuesta**: El backend responde con un DTO serializado en JSON.
7. **Frontend**: El contexto actualiza el estado y los componentes visualizan la simulación (mapa, tabla, tarjetas, métricas).

---

## 6. Recomendaciones para navegar el código

- Comienza por los puntos de entrada: `PlgApplication.java` (backend) y `main.tsx` (frontend).
- Sigue el flujo de una petición desde el controlador hasta el servicio y la entidad (backend).
- En el frontend, sigue el flujo desde la vista, pasando por el contexto y los servicios, hasta los componentes.
- Consulta los archivos de tipos (`types.ts` y DTOs) para entender la estructura de los datos.
- Usa los archivos `ORDEN.md`, `INICIACION.md` y `README_V2.md` de cada módulo para referencias rápidas.
- Para entender los algoritmos, revisa `AlgoritmoGenetico.java`, `Simulacion.java` y los servicios relacionados.
- Para pruebas, revisa los tests en `src/test/` y usa herramientas como Postman para probar los endpoints.

---

**Este archivo está pensado como guía de referencia rápida para entender el flujo y la función de cada archivo relevante en el proyecto. Si necesitas aún más detalle, consulta los README, los archivos de iniciación y la documentación inline en cada clase o función.**
