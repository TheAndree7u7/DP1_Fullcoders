# EXPLICACIÓN DETALLADA DEL PROYECTO: SISTEMA DE GESTIÓN LOGÍSTICA DE PEDIDOS Y GAS LICUADO (PLG)

---

## 1. INTRODUCCIÓN GENERAL

Este proyecto es una solución integral para la gestión logística de la distribución de gas licuado, abarcando desde la recepción de pedidos hasta la optimización de rutas de entrega y la simulación de escenarios logísticos. Está compuesto por dos grandes módulos:

- **Back-end:** Desarrollado en Java con Spring Boot, expone una API RESTful para la gestión y simulación logística.
- **Front-end:** Desarrollado en React y TypeScript, proporciona una interfaz visual interactiva para la simulación y monitoreo en tiempo real.

El sistema está diseñado para ser fácilmente entendible, extensible y mantenible, permitiendo a cualquier desarrollador o usuario comprender su funcionamiento y arquitectura.

---

## 2. ARQUITECTURA GENERAL DEL SISTEMA

### 2.1. Visión Global

El sistema sigue una arquitectura en capas y un patrón cliente-servidor:

```
[Usuario] <-> [Front-end React] <-> [API REST Spring Boot] <-> [Base de Datos H2 y Archivos de Datos]
```

- **Front-end:** Consume la API, visualiza rutas, pedidos, camiones y almacenes, y permite la simulación.
- **Back-end:** Procesa la lógica de negocio, ejecuta algoritmos de optimización y simula escenarios.
- **Persistencia:** Utiliza una base de datos en memoria (H2) y archivos de datos para la carga inicial.

### 2.2. Diagrama Simplificado de Capas (Back-end)

```
[Controladores REST] <-> [Servicios] <-> [Repositorios] <-> [Entidades/DTOs] <-> [Base de Datos]
                                              |
                                              ↓
                        [Algoritmos de Optimización y Simulación]
```

---

## 3. FUNCIONALIDADES PRINCIPALES

- **Gestión de Pedidos:** Registro, consulta y seguimiento de pedidos de gas licuado.
- **Optimización de Rutas:** Algoritmos avanzados para asignar pedidos y calcular rutas óptimas considerando bloqueos, averías y mantenimientos.
- **Gestión de Flota:** Control de camiones, su estado, capacidad y asignación de rutas.
- **Simulación:** Permite simular escenarios diarios, semanales y de colapso logístico.
- **Visualización:** El usuario puede ver en tiempo real el estado de la simulación, rutas, entregas y eventos.

---

## 4. MODELO DE DATOS (ENTIDADES PRINCIPALES)

- **Pedido:** Solicitud de entrega de gas. Atributos: id, fecha/hora, posición, cliente, volumen, estado, camión asignado.
- **Cliente:** Datos del solicitante. Atributos: id, nombre, dirección, contacto, posición.
- **Camion:** Vehículo de reparto. Atributos: id, capacidad, estado, ubicación, pedidos asignados.
- **Almacen:** Punto de almacenamiento. Atributos: nombre, tipo (central/secundario), coordenadas, activo.
- **Mantenimiento:** Registro de mantenimientos programados o correctivos.
- **Averia:** Incidentes en ruta que afectan la entrega.
- **Bloqueo:** Obstáculos viales que afectan la planificación de rutas.

---

## 5. ALGORITMOS Y LÓGICA DE NEGOCIO

- **Algoritmo Genético:** Optimiza la asignación de pedidos y rutas de camiones.
- **Agrupamiento por Propagación de Afinidad (AP):** Agrupa pedidos para optimizar la logística.
- **Cálculo de Rutas:** Determina rutas óptimas considerando distancias, bloqueos y restricciones.
- **Simulación:** Permite simular diferentes escenarios logísticos (normal, alta demanda, colapso).

---

## 6. FLUJO DE EJECUCIÓN Y SINCRONIZACIÓN FRONTEND-BACKEND

### 6.1. Inicio del Sistema
- El backend se inicia y expone la API REST en `http://localhost:8085`.
- El frontend se conecta a la API y carga los datos iniciales (almacenes, pedidos, camiones).

### 6.2. Interacción Usuario-Sistema
- El usuario inicia la simulación desde la interfaz.
- El frontend solicita al backend el mejor individuo (solución óptima) y los datos de la simulación.
- El backend procesa la solicitud, ejecuta los algoritmos y responde con los datos transformados.
- El frontend visualiza rutas, estados y permite avanzar la simulación paso a paso o en tiempo real.

### 6.3. Sincronización y Actualización
- El frontend mantiene un contador de nodos para sincronizar la simulación.
- Cada cierto número de pasos, solicita una actualización al backend.
- El backend recalcula rutas y estados, devolviendo la información actualizada.
- Se mantiene la coherencia de estado entre cliente y servidor.

### 6.4. Manejo de Errores
- El backend responde con códigos y mensajes de error estructurados.
- El frontend valida las respuestas y muestra mensajes claros al usuario.

---

## 7. ENDPOINTS Y SERVICIOS PRINCIPALES

### 7.1. Simulación API
- `GET /api/simulacion/mejor`: Obtiene la mejor solución de simulación.
- `POST /api/simulacion/calcular`: Calcula una nueva simulación con los datos actuales.
- `POST /api/simulacion/actualizar`: Actualiza el estado de la simulación en curso.

### 7.2. Almacenes API
- `GET /api/almacenes`: Lista los almacenes disponibles.

### 7.3. Otros Endpoints
- Gestión de pedidos, camiones, mantenimientos, bloqueos y averías.

---

## 8. ESTRUCTURA DE CARPETAS Y ARCHIVOS

### 8.1. Back-end
```
Back-end/
└── plg/
    ├── src/
    │   ├── main/java/com/plg/ (código fuente principal)
    │   ├── main/resources/ (archivos de configuración y datos)
    │   └── test/ (tests)
    ├── pom.xml (configuración Maven)
    └── README.md (documentación)
```

### 8.2. Front-end
```
Front-end/
├── src/
│   ├── components/ (componentes visuales)
│   ├── context/ (gestión de estado global)
│   ├── services/ (llamadas a la API)
│   ├── views/ (páginas principales)
│   └── ...
├── public/ (recursos estáticos)
├── package.json (dependencias y scripts)
└── README.md (documentación)
```

---

## 9. INTERCONEXIÓN ENTRE BACKEND Y FRONTEND

### 9.1. Comunicación
- El frontend consume la API REST del backend mediante peticiones HTTP (fetch/axios).
- Los datos se transfieren en formato JSON.
- Los tipos de datos y DTOs están sincronizados entre ambos módulos para evitar errores de interpretación.
- Se utiliza CORS para permitir la comunicación entre dominios.

### 9.2. Flujo de Datos
1. **Inicialización:**
   - El frontend solicita almacenes y datos iniciales al backend.
   - El backend responde con la estructura de datos esperada.
2. **Simulación:**
   - El usuario interactúa con la interfaz (iniciar, pausar, avanzar simulación).
   - El frontend envía solicitudes de actualización/calculación al backend.
   - El backend procesa y responde con el nuevo estado del sistema.
3. **Visualización:**
   - El frontend transforma y muestra los datos (mapas, rutas, tablas, métricas).

### 9.3. Sincronización de Tipos y Validaciones
- Los modelos de datos (entidades y DTOs) del backend se reflejan en los tipos TypeScript del frontend.
- Cualquier cambio en la estructura de datos debe sincronizarse en ambos lados.

---

## 10. ESCENARIOS DE SIMULACIÓN

- **Simulación Diaria:** Un día típico de operaciones logísticas.
- **Simulación Semanal:** Proyección de operaciones a lo largo de varios días.
- **Simulación de Colapso:** Escenario de alta demanda y múltiples incidentes.

---

## 11. EXTENSIBILIDAD Y CONFIGURACIÓN

- El sistema permite agregar nuevos algoritmos, entidades y endpoints fácilmente.
- La configuración se realiza mediante archivos `application.properties` y archivos de datos en `resources/data/`.
- El frontend es modular y permite agregar nuevas vistas y componentes.

---

## 12. TECNOLOGÍAS UTILIZADAS

- **Back-end:** Java 21+, Spring Boot, Maven, H2 Database, JPA, WebSocket (opcional), Swagger/OpenAPI.
- **Front-end:** React, TypeScript, Vite, CSS Modules, Axios/fetch.

---

## 13. RECOMENDACIONES PARA NUEVOS DESARROLLADORES

1. Leer primero este archivo y los README de cada módulo.
2. Seguir el orden recomendado para entender el código: configuración, entidades, servicios, controladores, frontend.
3. Mantener sincronizados los tipos y DTOs entre backend y frontend.
4. Probar la API con herramientas como Postman.
5. Usar los scripts de desarrollo (`mvnw spring-boot:run` para backend, `npm run dev` para frontend).
6. Consultar la documentación adicional en los archivos `INICIACION.md`, `ENLACES.md`, `NARRATIVA.md` e `INTERACCION.md`.

---

## 14. RECURSOS ADICIONALES

- [Documentación de Spring Boot](https://spring.io/projects/spring-boot)
- [Documentación de React](https://reactjs.org/docs/getting-started.html)
- [Documentación de TypeScript](https://www.typescriptlang.org/docs/)
- [Documentación de Vite](https://vitejs.dev/guide/)
- [Documentación de Maven](https://maven.apache.org/guides/)
- [Swagger/OpenAPI](http://localhost:8085/api-docs)

---

## 15. CONCLUSIÓN

Este sistema implementa una arquitectura robusta y modular para la gestión logística de pedidos de gas licuado, permitiendo la simulación, optimización y visualización en tiempo real. La separación clara entre backend y frontend, junto con la sincronización de tipos y la documentación exhaustiva, facilita el mantenimiento, la escalabilidad y la incorporación de nuevos desarrolladores al proyecto.

---

**Para cualquier duda, consulta los archivos de documentación incluidos en cada módulo o contacta con el equipo de desarrollo.**
