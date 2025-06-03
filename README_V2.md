# Descripción del Proyecto PLG

El repositorio contiene una aplicación completa para la gestión logística de pedidos de gas licuado (GLP).
Incluye un **backend** desarrollado con Spring Boot y un **frontend** basado en React + TypeScript.
El objetivo principal es planificar de forma óptima la distribución de pedidos considerando
restricciones de flota, mantenimientos, averías y bloqueos viales.

## Estructura general

```
DP1_Fullcoders/
├── Back-end/          # Código del servidor Spring Boot
│   └── plg/
│       ├── README.md  # Documentación detallada del backend
│       └── src/       # Código Java y recursos
├── Front-end/         # Interfaz de usuario React
│   └── src/
│       ├── components/  # Componentes visuales (Mapa, tablas, etc.)
│       ├── context/     # Contexto de simulación
│       └── views/       # Vistas principales
├── entity.uml         # Diagrama de entidades
└── README.md          # Archivo breve original
```

## Backend

El backend se documenta en profundidad en [`Back-end/plg/README.md`](Back-end/plg/README.md). Algunos
puntos clave son:

- **Modelo de datos**: entidades como `Pedido`, `Camion`, `Mantenimiento`, `Averia`, `Bloqueo`, `Almacen`.
  Las descripciones de atributos y estados se encuentran entre las líneas 61 a 89 del README original
  donde se explica el significado de cada entidad y sus posibles estados y tipos【F:Back-end/plg/README.md†L61-L89】.
- **Algoritmos**: se implementa un algoritmo genético para optimizar rutas, más un algoritmo de
  agrupamiento por propagación de afinidad y rutinas de simulación. En el README se describe el flujo
  del algoritmo genético y sus parámetros desde la línea 98 en adelante【F:Back-end/plg/README.md†L98-L124】.
- **Ejecución**: la clase de entrada es `PlgApplication` (líneas 4‑26 del archivo Java principal) que arranca
  la simulación en un hilo y expone un endpoint REST para consultar el mejor individuo generado【F:Back-end/plg/src/main/java/com/plg/PlgApplication.java†L1-L26】.
- **API REST**: los endpoints están listados en el README en la sección correspondiente a partir de
  la línea 190【F:Back-end/plg/README.md†L190-L219】.
- **Carga de datos**: la clase `DataLoader` lee archivos en `src/main/resources/data/` para poblar
  camiones, pedidos, mantenimientos y bloqueos; este comportamiento se observa en sus métodos de inicialización
  (líneas 28‑93 del código)【F:Back-end/plg/src/main/java/com/plg/config/DataLoader.java†L19-L93】.

La simulación se coordina mediante la clase `Simulacion`, que mantiene colas de comunicación y semáforos
para interactuar con el algoritmo genético (líneas 20‑37)【F:Back-end/plg/src/main/java/com/plg/utils/Simulacion.java†L20-L37】.
El algoritmo genético en sí está implementado en `AlgoritmoGenetico` y genera poblaciones, selecciona, cruza y
muta individuos, calculando el fitness según tiempo de entrega y consumo de combustible (ver líneas 23‑48, 74‑108
por ejemplo)【F:Back-end/plg/src/main/java/com/plg/utils/AlgoritmoGenetico.java†L23-L48】【F:Back-end/plg/src/main/java/com/plg/utils/AlgoritmoGenetico.java†L80-L108】.

## Frontend

El frontend es una SPA creada con Vite. La carpeta `src` contiene:

- `components/` con elementos como `Mapa.tsx`, `CardCamion.tsx` y `TablaPedidos.tsx`.
  `Mapa.tsx` dibuja la malla y representa gráficamente la posición de los camiones; utiliza
  un intervalo para avanzar la simulación y actualiza la posición de cada camión
  (véase la lógica desde la línea 41 hasta la 122)【F:Front-end/src/components/Mapa.tsx†L1-L122】.
- `context/SimulacionContext.tsx` gestiona el estado global de la simulación. Carga los datos del
  backend mediante `getMejorIndividuo` y ofrece funciones para avanzar hora a hora
  (líneas 3‑60 contienen la inicialización y carga de datos)【F:Front-end/src/context/SimulacionContext.tsx†L1-L60】.
- `views/SimulacionSemanal.tsx` muestra la interfaz principal con un mapa y un panel lateral
  (líneas 1‑30 del archivo)【F:Front-end/src/views/SimulacionSemanal.tsx†L1-L30】.

El servicio `simulacionApiService.ts` define la función `getMejorIndividuo` que consulta `http://localhost:8085/api/simulacion/mejor` para obtener la mejor solución calculada por el backend (líneas 1‑23)【F:Front-end/src/services/simulacionApiService.ts†L1-L23】.

## Ejecución rápida

1. **Backend**
   ```bash
   cd Back-end/plg
   ./mvnw spring-boot:run
   ```
   Esto compilará y arrancará el servidor Spring Boot en el puerto 8085.
2. **Frontend**
   ```bash
   cd Front-end
   npm install
   npm run dev
   ```
   Abre `http://localhost:5173` para ver la aplicación.

La vista inicial carga la simulación semanal y muestra el avance de los camiones sobre un mapa de cuadrícula.

## Recursos adicionales

- **Diagrama UML** (`entity.uml`): describe las relaciones entre entidades de dominio.
- **Pruebas**: se incluyen algunas clases de prueba básicas en `Back-end/plg/src/test/java`.

Esta documentación es complementaria al README original del backend, proporcionando una
visión completa para quienes no han visto el código ni el algoritmo.
