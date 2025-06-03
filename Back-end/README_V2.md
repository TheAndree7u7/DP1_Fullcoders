# Guía Detallada del Backend

Este documento explica en profundidad el código de la carpeta **Back-end** para
usuarios que no han visto el proyecto ni conocen el algoritmo implementado. El
backend es una aplicación Spring Boot ubicada en `Back-end/plg`.

## Orden recomendado de revisión

1. **`PlgApplication`** – Punto de entrada que arranca la simulación en `run()`.
   Revisa las líneas 15‑31 para ver cómo se crea el hilo de simulación y el
   mensaje informativo al usuario
   【F:Back-end/plg/src/main/java/com/plg/PlgApplication.java†L15-L31】.
2. **`DataLoader`** – Encargado de cargar camiones, almacenes, pedidos y
   bloqueos desde `src/main/resources/data`. Observa la configuración inicial y
   las listas estáticas entre las líneas 19‑37
   【F:Back-end/plg/src/main/java/com/plg/config/DataLoader.java†L19-L37】.
3. **`entity/` y `dto/`** – Contienen las clases de dominio y sus
   equivalentes de transferencia. Conviene repasarlos para entender qué datos
   pasan entre capas.
4. **`Simulacion`** – Gestiona la lógica de avance hora por hora. Las líneas
   25‑37 muestran las colas y semáforos que sincronizan con el algoritmo
   genético
   【F:Back-end/plg/src/main/java/com/plg/utils/Simulacion.java†L25-L37】.
5. **`AlgoritmoGenetico`** – Implementa la optimización de rutas. Estudia su
   constructor y la función `ejecutarAlgoritmo()` a partir de la línea 23 para
   comprender cómo genera y cruza poblaciones
   【F:Back-end/plg/src/main/java/com/plg/utils/AlgoritmoGenetico.java†L23-L108】.
6. **`controller/`** – Finalmente revisa `SimulacionController` para entender el
   endpoint `/api/simulacion/mejor`, el cual libera los semáforos anteriores y
   devuelve un `IndividuoDto` al frontend
   【F:Back-end/plg/src/main/java/com/plg/controller/SimulacionController.java†L9-L27】.

## 1. Arquitectura General

El proyecto sigue una estructura tradicional de Spring Boot. En la carpeta
`src/main/java/com/plg` encontramos los siguientes paquetes:

- `config/` – Configuraciones y carga inicial de datos.
- `controller/` – Controladores REST expuestos a los clientes.
- `dto/` – Objetos de transferencia de datos.
- `entity/` – Entidades de dominio persistentes.
- `factory/` – Clases utilitarias para crear entidades a partir de archivos.
- `utils/` – Lógica auxiliar como algoritmos y clases de simulación.

La entrada principal del sistema es la clase `PlgApplication`, que arranca la
simulación al ejecutar la aplicación. Las líneas 14‑29 muestran cómo se crea un
hilo para ejecutar `Simulacion.ejecutarSimulacion()`【F:Back-end/plg/src/main/java/com/plg/PlgApplication.java†L14-L29】.

## 2. Carga de Datos

El componente `DataLoader` se encarga de leer archivos con información inicial de
camiones, pedidos, mantenimientos y bloqueos. En las líneas 19‑33 se declaran las
rutas de los archivos y las listas que almacenarán los datos cargados
【F:Back-end/plg/src/main/java/com/plg/config/DataLoader.java†L19-L33】.

Posteriormente, los métodos `initializeAlmacenes`, `initializeCamiones` y otros
crean las instancias correspondientes. Por ejemplo, entre las líneas 35‑58 se
configuran varios camiones de distintos tipos
【F:Back-end/plg/src/main/java/com/plg/config/DataLoader.java†L35-L58】.

## 3. Clase de Simulación

La lógica de simulación se concentra en la clase `Simulacion`. Allí se definen
estructuras para coordinar el algoritmo genético mediante colas y semáforos. Las
líneas 25‑37 muestran las colas de comunicación y variables estáticas utilizadas
para llevar el control de la simulación【F:Back-end/plg/src/main/java/com/plg/utils/Simulacion.java†L25-L37】.

Durante la simulación se lee la lista de pedidos semanal y, en cada iteración, se
invoca al algoritmo genético para planificar rutas eficientes.

## 4. Algoritmo Genético

El núcleo de la optimización es la clase `AlgoritmoGenetico`. En las líneas
23‑48 se definen sus atributos principales, incluyendo el tamaño de población y
las referencias a pedidos, camiones y almacenes
【F:Back-end/plg/src/main/java/com/plg/utils/AlgoritmoGenetico.java†L23-L48】.

Dentro de `ejecutarAlgoritmo` se generan poblaciones, se seleccionan padres y se
crean hijos hasta obtener el mejor individuo. Las líneas 80‑108 muestran parte
de la lógica de selección y cruce de individuos
【F:Back-end/plg/src/main/java/com/plg/utils/AlgoritmoGenetico.java†L80-L108】.

## 5. Modelo de Datos

El README original describe cada entidad en detalle. Entre las líneas 61‑85 se
explican los atributos y estados de `Camion`, `Mantenimiento`, `Averia`,
`Bloqueo` y `Almacen`【F:Back-end/plg/README.md†L61-L85】.

## 6. API REST

Los controladores exponen numerosos endpoints para gestionar pedidos, rutas y
mantenimientos. La lista completa se encuentra en el README desde la línea 191 a
la 218, donde se muestra cada operación disponible
【F:Back-end/plg/README.md†L191-L218】.

## 7. Ejecución del Backend

Para ejecutar la aplicación se necesita Java 21 y Maven. Los pasos típicos son:

```bash
cd Back-end/plg
./mvnw spring-boot:run
```

Esto compilará el proyecto y arrancará el servidor en el puerto 8085. Al
iniciar, se cargarán los archivos de datos definidos en `DataLoader`.

## 8. Estructura de Archivos

```
Back-end/
└── plg/
    ├── README.md       # Documentación original con más detalles
    ├── src/
    │   ├── main/
    │   │   ├── java/com/plg/...
    │   │   └── resources/data/
    │   └── test/
    └── pom.xml
```

Este archivo pretende servir de guía rápida para comprender cómo está organizado
el backend y qué componentes intervienen en la simulación y optimización de
rutas. Para información más profunda se recomienda leer `plg/README.md` por
completo.

## 9. Comunicación con el Frontend

El frontend realiza peticiones HTTP al endpoint
`http://localhost:8085/api/simulacion/mejor`. Al recibirse la solicitud, el
controlador libera el semáforo `iniciar`, espera el resultado del algoritmo en
`gaResultQueue` y responde con un `IndividuoDto`. El tipo devuelto contiene la
ruta de cada camión y se define en `dto/`. Este mismo objeto es mapeado en el
frontend para animar el avance sobre el mapa.
