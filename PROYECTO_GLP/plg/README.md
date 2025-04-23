# Sistema de Gestión Logística de Pedidos y Gas Licuado (PLG)

## Índice
1. [Introducción](#introducción)
2. [Arquitectura del Sistema](#arquitectura-del-sistema)
3. [Modelo de Datos](#modelo-de-datos)
4. [Algoritmos Implementados](#algoritmos-implementados)
5. [Cómo Iniciar el Sistema](#cómo-iniciar-el-sistema)
6. [API REST](#api-rest)
7. [Escenarios de Simulación](#escenarios-de-simulación)
8. [Estructura de Directorios](#estructura-de-directorios)
9. [Archivos de Datos](#archivos-de-datos)
10. [Extensibilidad y Configuración](#extensibilidad-y-configuración)

## Introducción

El Sistema de Gestión Logística de Pedidos y Gas Licuado (PLG) es una aplicación backend desarrollada con Spring Boot que gestiona la logística de distribución de gas licuado. El sistema permite:

- Gestión de pedidos de clientes
- Optimización de rutas de entrega
- Gestión de la flota de camiones
- Control de mantenimientos preventivos y correctivos
- Manejo de averías en ruta
- Consideración de bloqueos viales en la planificación
- Visualización del estado del sistema
- Simulación de diversos escenarios logísticos

Esta aplicación está diseñada como una API RESTful que puede ser consumida por interfaces de usuario o sistemas externos.

## Arquitectura del Sistema

El sistema sigue una arquitectura en capas basada en Spring Boot:

1. **Capa de Presentación**: Controladores REST que exponen endpoints para interactuar con el sistema.
2. **Capa de Servicio**: Lógica de negocio, algoritmos de optimización y procesamiento de datos.
3. **Capa de Repositorio**: Interfaz con la base de datos para la persistencia de datos.
4. **Capa de Modelo**: Entidades de dominio que representan los objetos del negocio.

### Diagrama simplificado de la arquitectura:

```
[Cliente HTTP] <---> [Controladores REST] <---> [Servicios] <---> [Repositorios] <---> [Base de Datos H2]
                                                    |
                                                    ↓
                           [Algoritmos de Optimización y Simulación]
```

## Modelo de Datos

El sistema utiliza las siguientes entidades principales:

### Pedido
Representa una solicitud de entrega de gas licuado.
- **Atributos**: ID, fechaHora, posición (X,Y), cliente, volumen en m3, horas límite, estado, camión asignado.
- **Estados**: 0 (pendiente), 1 (asignado), 2 (en ruta), 3 (entregado), 4 (cancelado).

### Cliente
Representa al solicitante del pedido.
- **Atributos**: ID, nombre, dirección, teléfono, email, posición (X,Y).

### Camion
Representa los vehículos de distribución.
- **Atributos**: código, tipo, capacidad, tara, peso de carga, peso combinado, estado.
- **Estados**: 0 (disponible), 1 (en ruta), 2 (en mantenimiento), 3 (averiado).
- **Tipos**: TA, TB, TC, TD (con diferentes capacidades y características).

### Mantenimiento
Registra los mantenimientos programados o realizados a los camiones.
- **Atributos**: ID, camión, fecha inicio, fecha fin, tipo, descripción, estado.
- **Estados**: 0 (programado), 1 (en proceso), 2 (finalizado).
- **Tipos**: preventivo, correctivo.

### Averia
Registra incidentes o fallas en los camiones.
- **Atributos**: ID, camión, fecha/hora reporte, descripción, severidad, posición (X,Y), estado.
- **Severidad**: 1 (leve), 2 (moderada), 3 (grave).
- **Estados**: 0 (reportada), 1 (atendida), 2 (reparada).

### Bloqueo
Representa obstáculos en las rutas que deben ser considerados en la planificación.
- **Atributos**: ID, posición inicial (X,Y), posición final (X,Y), fecha inicio, fecha fin, descripción, estado activo.

### Almacen
Representa los puntos de partida de los camiones donde se carga el gas.
- **Atributos**: ID, nombre, posición (X,Y), capacidad total, capacidad actual, estado activo.

## Algoritmos Implementados

El sistema implementa varios algoritmos para la optimización y gestión de la logística:

### 1. Algoritmo Genético para Optimización de Rutas

El servicio `AlgoritmoGeneticoService` implementa un algoritmo genético para la optimización de rutas de entrega, considerando:

- Capacidad de los camiones
- Tiempos límite de entrega
- Distancias entre puntos
- Bloqueos en las rutas

Funcionamiento:
1. **Población Inicial**: Se genera una población inicial de soluciones (rutas posibles).
2. **Evaluación**: Se evalúa cada solución según una función de aptitud que minimiza distancia y tiempo.
3. **Selección**: Se seleccionan las mejores soluciones para reproducción.
4. **Cruce**: Se combinan soluciones para generar nuevas posibilidades.
5. **Mutación**: Se introducen cambios aleatorios para explorar más el espacio de soluciones.
6. **Iteración**: Se repite el proceso durante varias generaciones.

Parámetros configurables:
- Tamaño de población inicial: 50
- Máximo de generaciones: 100
- Tasa de mutación: 0.1
- Tasa de cruce: 0.8

### 2. Algoritmo de Agrupamiento por Propagación de Afinidad (AP)

El servicio `AgrupamientoAPService` implementa un algoritmo de clustering para agrupar pedidos cercanos geográficamente:

Funcionamiento:
1. **Matriz de Similitud**: Se calcula la similitud entre todos los pedidos basada en distancia.
2. **Propagación de Responsabilidad**: Los puntos intercambian mensajes sobre cómo representarse mutuamente.
3. **Propagación de Disponibilidad**: Los puntos intercambian mensajes sobre su disponibilidad para ser ejemplares.
4. **Convergencia**: El algoritmo converge para identificar ejemplares y asignar puntos a clusters.

Parámetros configurables:
- Factor de amortiguación (DAMPING): 0.9
- Máximo de iteraciones: 200
- Umbral de convergencia: 0.001

### 3. Sistema de Cálculo de Rutas

El servicio `RutaService` implementa algoritmos para:

- **Cálculo de Distancias**: Utiliza fórmula de distancia euclidiana.
- **Detección de Intersecciones**: Determina si una ruta atraviesa un bloqueo.
- **Optimización de Segmentos**: Reorganiza segmentos de ruta para minimizar distancia total.

### 4. Sistema de Simulación

El sistema permite simular diferentes escenarios logísticos mediante:

- **Simulación Diaria**: Genera un día típico de operaciones.
- **Simulación Semanal**: Proyecta operaciones durante varios días.
- **Simulación de Colapso**: Representa escenarios de alta demanda y múltiples incidentes.

## Cómo Iniciar el Sistema

### Requisitos Previos
- Java 21 o superior
- Maven 3.8.x o superior
- IDE compatible con Spring Boot (recomendado: IntelliJ IDEA, Eclipse, VSCode)

### Pasos para Ejecutar

1. **Clonar el Repositorio**
   ```bash
   git clone [URL_DEL_REPOSITORIO]
   cd plg
   ```

2. **Compilar el Proyecto**
   ```bash
   mvnw clean install
   ```

3. **Ejecutar la Aplicación**
   ```bash
   mvnw spring-boot:run
   ```
   
   Alternativamente, desde una IDE, ejecutar la clase `com.plg.PlgApplication`.

4. **Verificar Funcionamiento**
   Abrir en el navegador: http://localhost:8080/h2-console
   
   Parámetros de conexión:
   - JDBC URL: jdbc:h2:mem:plgdb
   - Username: sa
   - Password: password

### Carga Inicial de Datos

El sistema está configurado para cargar datos de muestra automáticamente al iniciar:
- Camiones de diversos tipos
- Pedidos desde archivos en `/data/pedidos/`
- Mantenimientos programados desde archivos en `/data/mantenimientos/`

## API REST

El sistema expone los siguientes endpoints principales:

### Gestión de Pedidos
- `GET /api/pedidos`: Obtiene todos los pedidos
- `GET /api/pedidos/{id}`: Obtiene un pedido específico
- `POST /api/pedidos`: Crea un nuevo pedido
- `PUT /api/pedidos/{id}`: Actualiza un pedido
- `DELETE /api/pedidos/{id}`: Elimina un pedido
- `GET /api/pedidos/estado/{estado}`: Filtra pedidos por estado

### Gestión de Rutas
- `POST /api/rutas/generar`: Genera rutas optimizadas
- `GET /api/rutas/optimizar/{idRuta}`: Optimiza una ruta específica
- `GET /api/rutas/distancia`: Calcula distancia entre coordenadas

### Gestión de Mantenimientos
- `GET /api/mantenimientos`: Obtiene todos los mantenimientos
- `GET /api/mantenimientos/{id}`: Obtiene un mantenimiento específico
- `GET /api/mantenimientos/camion/{codigoCamion}`: Filtra por camión
- `GET /api/mantenimientos/periodo`: Filtra por período temporal
- `POST /api/mantenimientos`: Registra un nuevo mantenimiento
- `POST /api/mantenimientos/programar`: Programa mantenimientos preventivos

### Visualización
- `GET /api/visualizador/mapa`: Obtiene datos para visualización en mapa
- `GET /api/visualizador/estado-general`: Obtiene estadísticas del sistema
- `POST /api/visualizador/filtrar`: Aplica filtros de visualización

### Simulación
- `GET /api/simulacion/diario`: Simula operaciones de un día
- `GET /api/simulacion/semanal`: Simula operaciones de varios días
- `GET /api/simulacion/colapso`: Simula un escenario de colapso
- `POST /api/simulacion/personalizado`: Simula escenario personalizado

## Escenarios de Simulación

El sistema permite simular distintos escenarios logísticos para evaluar el comportamiento ante diferentes situaciones:

### Escenario Diario
Simula un día típico de operaciones con:
- Generación de 15-30 pedidos aleatorios
- Asignación de camiones y rutas
- Posibles eventos aleatorios (averías, bloqueos)
- Cálculo de métricas de rendimiento

### Escenario Semanal
Extiende la simulación a varios días consecutivos, permitiendo:
- Análisis de tendencias
- Evaluación de capacidad a mediano plazo
- Acumulación de métricas operativas
- Detección de patrones cíclicos

### Escenario de Colapso
Simula situaciones críticas con:
- Gran número de pedidos (60-80)
- Alta tasa de averías (30% de la flota)
- Múltiples bloqueos en rutas
- Cálculo de tiempo estimado de normalización
- Evaluación de saturación del sistema

## Estructura de Directorios

```
plg/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── plg/
│   │   │           ├── config/           # Configuraciones del sistema
│   │   │           ├── controller/       # Controladores REST
│   │   │           ├── dto/              # Objetos de transferencia de datos
│   │   │           ├── entity/           # Entidades persistentes
│   │   │           ├── repository/       # Repositorios para persistencia
│   │   │           └── service/          # Servicios de lógica de negocio
│   │   └── resources/
│   │       ├── application.properties    # Configuración de la aplicación
│   │       └── data/                    # Datos de inicialización
│   │           ├── pedidos/             # Datos de pedidos
│   │           ├── mantenimientos/      # Datos de mantenimientos
│   │           ├── bloqueos/            # Datos de bloqueos viales
│   │           └── averias/             # Datos de averías
│   └── test/                           # Pruebas unitarias e integración
└── pom.xml                             # Configuración de Maven
```

## Archivos de Datos

El sistema utiliza varios archivos para cargar datos iniciales:

### Archivo de Pedidos (`ventas202504.txt`)
Formato: `fechaHora,posX,posY,idCliente,m3,horasLimite`

Ejemplo:
```
11d13h31m,45,67,CLI001,10,24
11d14h15m,30,42,CLI002,15,12
```

### Archivo de Mantenimientos (`mantpreventivo.txt`)
Formato: `codigoCamion,fechaInicio,fechaFin`

Ejemplo:
```
TA01,2025-04-15,2025-04-16
TB02,2025-04-18,2025-04-19
```

### Archivo de Bloqueos (`202504.bloqueadas`)
Formato: `posXInicio,posYInicio,posXFin,posYFin,fechaInicio,fechaFin,descripción`

Ejemplo:
```
20,30,35,45,2025-04-10,2025-04-15,Obras en avenida principal
50,25,65,25,2025-04-12,2025-04-20,Cierre por desfile
```

### Archivo de Averías (`averias.v1.txt`)
Formato: `codigoCamion,fechaHoraReporte,posX,posY,severidad,descripción`

Ejemplo:
```
TA01,2025-04-10T08:30:00,45,28,2,Problema en sistema de frenos
TB03,2025-04-10T10:15:00,32,64,1,Falla en sistema eléctrico
```

## Extensibilidad y Configuración

El sistema está diseñado para ser extensible y configurable:

### Configuración en `application.properties`
- **Puerto**: `server.port=8080`
- **Base de datos**: Configuración de H2 en memoria
- **Rutas de datos**: Ubicaciones de archivos de datos
- **Logging**: Niveles de log configurables

### Extensiones Posibles
1. **Base de Datos Persistente**: Cambiar H2 por MySQL, PostgreSQL, etc.
2. **Nuevos Algoritmos**: Implementar algoritmos adicionales extendiendo los servicios.
3. **Integración Externa**: Agregar clientes HTTP para sistemas externos (GPS, meteorología).
4. **Frontend**: Desarrollar interfaz web consumiendo la API REST.
5. **Seguridad**: Implementar autenticación y autorización vía Spring Security.

### Personalización de Algoritmos
Los parámetros de los algoritmos pueden ser ajustados en los respectivos servicios:
- `AlgoritmoGeneticoService`: Parámetros genéticos (población, generaciones)
- `AgrupamientoAPService`: Parámetros de clustering (factor de amortiguación)
- `SimulacionService`: Variantes de escenarios

# Sistema de Gestión y Simulación de Transporte de GLP

## Estructura del Backend

### Capas de la Aplicación
El sistema está construido siguiendo una arquitectura por capas que separa claramente las responsabilidades:

- **Capa de Presentación**: Controllers que exponen endpoints REST API
- **Capa de Negocio**: Services que implementan la lógica de negocio
- **Capa de Acceso a Datos**: Repositories que interactúan con la base de datos
- **Capa de Dominio**: Entities que representan los objetos del dominio

### Estructura de Carpetas
```
plg/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── plg/
│   │   │           ├── config/       # Configuración de la aplicación
│   │   │           ├── controller/   # Endpoints REST API
│   │   │           ├── dto/          # Objetos de transferencia de datos
│   │   │           ├── entity/       # Entidades del dominio
│   │   │           ├── repository/   # Interfaces de acceso a datos
│   │   │           ├── service/      # Lógica de negocio
│   │   │           └── util/         # Clases utilitarias
│   │   └── resources/
│   │       ├── application.properties # Configuración de la aplicación
│   │       ├── logback-spring.xml    # Configuración de logs
│   │       ├── data/                 # Datos para cargar en la aplicación
│   │       └── static/               # Archivos estáticos (frontend)
│   └── test/                         # Pruebas unitarias e integración
```

### Convenciones de Nomenclatura
- **Clases y Tipos**: PascalCase (ej: `CamionController`, `MapaReticularService`)
- **Métodos y Funciones**: camelCase (ej: `obtenerAlmacenesPorTipo()`, `calcularRutaOptima()`)
- **Variables y Parámetros**: camelCase (ej: `codigoCamion`, `posicionActual`)
- **Constantes**: SCREAMING_SNAKE_CASE (ej: `MAX_ITERATIONS`, `TAMANO_CELDA`)
- **Sufijos**: Controller, Service, Repository (ej: `AlmacenController`, `BloqueoService`)
- **Archivos**: PascalCase para clases (ej: `CamionService.java`, `AlmacenRepository.java`)

## Arquitectura del Sistema

### Diagrama de Flujo del Sistema
```
Cliente Web <--> Controllers <--> Services <--> Repositories <--> Base de Datos
      ^                ^
      |                |
      v                v
WebSockets <----> Simulación en Tiempo Real
```

### Componentes Principales

#### 1. Entidades (Entities)
- **Camion**: Representa un camión de transporte de GLP con propiedades como capacidad, estado, posición actual y combustible.
- **Almacen**: Representa un almacén de GLP y combustible (central o intermedio).
- **Pedido**: Representa un pedido de GLP con ubicación, volumen y estado.
- **Ruta**: Representa una ruta planificada para entrega de pedidos.
- **Bloqueo**: Representa bloqueos en el mapa reticular (obstáculos).
- **Averia**: Representa una avería de un camión.
- **Mantenimiento**: Representa un mantenimiento programado o correctivo.

#### 2. Servicios (Services)
- **MapaReticularService**: Gestiona la navegación en el mapa reticular y cálculo de rutas óptimas utilizando el algoritmo A*.
- **SimulacionService**: Gestiona las simulaciones por escenarios (diario, semanal, colapso).
- **SimulacionTiempoRealService**: Gestiona la simulación en tiempo real con actualización vía WebSockets.
- **CamionService**: Gestiona operaciones relacionadas con los camiones.
- **AlmacenService**: Gestiona operaciones relacionadas con los almacenes.
- **PedidoService**: Gestiona operaciones relacionadas con los pedidos.
- **BloqueoService**: Gestiona bloqueos en el mapa reticular.
- **AlgoritmoGeneticoService**: Implementa algoritmo genético para optimización de rutas.
- **AgrupamientoAPService**: Implementa algoritmo de Affinity Propagation para agrupar pedidos.

#### 3. Controladores (Controllers)
- **VisualizadorController**: Endpoints para visualización del estado del sistema.
- **MapaReticularController**: Endpoints para interactuar con el mapa y rutas.
- **SimulacionController**: Endpoints para iniciar/detener simulaciones.
- **AlmacenController**: Endpoints para gestionar almacenes.
- **CamionController**: Endpoints para gestionar camiones.
- **PedidoController**: Endpoints para gestionar pedidos.
- **MantenimientoController**: Endpoints para gestionar mantenimientos.
- **AlgoritmosController**: Endpoints para ejecutar algoritmos de optimización.

#### 4. Repositorios (Repositories)
- Interfaces que extienden JpaRepository para interactuar con la base de datos.
- Ejemplos: CamionRepository, AlmacenRepository, PedidoRepository.

#### 5. Configuración (Config)
- **WebSocketConfig**: Configuración para comunicación en tiempo real.
- **MapaConfig**: Configuración del mapa reticular (dimensiones, distancias).
- **DataLoaderConfig**: Carga inicial de datos desde archivos.

## Flujos Principales del Sistema

### 1. Carga y Gestión de Datos
- El sistema carga datos iniciales desde archivos en la carpeta `/data` (almacenes, camiones, etc.).
- Los controllers exponen endpoints para listar, crear, actualizar y eliminar entidades.

### 2. Visualización del Estado
- `VisualizadorService` proporciona métodos para obtener datos del estado actual.
- `VisualizadorController` expone endpoints para acceder a estos datos.
- El frontend consume estos endpoints para mostrar el estado actual del sistema.

### 3. Cálculo de Rutas
- `MapaReticularService` implementa el algoritmo A* para encontrar rutas óptimas.
- Considera bloqueos y distancias para calcular rutas eficientes.
- `MapaReticularController` expone endpoints para calcular rutas.

### 4. Simulación en Tiempo Real
1. El cliente inicia una simulación a través del endpoint `/api/simulacion/iniciar-tiempo-real`.
2. `SimulacionTiempoRealService` comienza a actualizar el estado de camiones y pedidos a intervalos regulares.
3. Los cambios de estado se envían al frontend a través de WebSockets.
4. El frontend actualiza la visualización en tiempo real.
5. El cliente puede ajustar la velocidad de simulación o detenerla.

### 5. Optimización de Rutas
1. `AgrupamientoAPService` agrupa pedidos basados en proximidad geográfica.
2. `AlgoritmoGeneticoService` genera rutas optimizadas para cada grupo de pedidos.
3. Los resultados se devuelven como DTOs estructurados.

### 6. Gestión de Bloqueos y Eventos
- El sistema carga bloqueos desde archivos por mes.
- `SimulacionService` puede generar eventos aleatorios (averías, bloqueos).
- `MapaReticularService` considera los bloqueos al calcular rutas.

### 7. Monitoreo de Recursos
- `AlmacenCombustibleService` gestiona niveles de GLP y combustible.
- `CamionService` monitorea el estado, capacidad y combustible de camiones.
- Los almacenes intermedios se reabastecen automáticamente o manualmente.

## Comunicación con el Frontend

### API REST
- Todos los controladores exponen endpoints RESTful para interactuar con el sistema.
- Los endpoints siguen convenciones RESTful (GET para consultas, POST para creación, etc.).

### WebSockets
- `SimulacionTiempoRealService` utiliza WebSockets para transmitir actualizaciones en tiempo real.
- El frontend se suscribe a estos eventos para mostrar cambios sin recargar la página.

### Visualización en el Mapa
- El frontend utiliza un mapa reticular para representar camiones, almacenes, pedidos y bloqueos.
- Se utilizan colores distintos para representar diferentes estados (COLORES en script.js).
- El mapa admite zoom y desplazamiento para una mejor visualización.

## Algoritmos Implementados

### Algoritmo A* (A-Star)
- Implementado en `MapaReticularService` para encontrar rutas óptimas.
- Utiliza una heurística (distancia Manhattan) para guiar la búsqueda.
- Considera bloqueos y distancias para encontrar el camino más corto.

### Algoritmo de Affinity Propagation
- Implementado en `AgrupamientoAPService` para agrupar pedidos cercanos.
- Identifica ejemplares (pedidos representativos) para cada grupo.
- Proporciona métricas como densidad y radio para cada grupo.

### Algoritmo Genético
- Implementado en `AlgoritmoGeneticoService` para optimizar rutas.
- Utiliza operadores genéticos (selección, cruce, mutación) para evolucionar soluciones.
- Optimiza para minimizar distancias totales y maximizar capacidad utilizada.