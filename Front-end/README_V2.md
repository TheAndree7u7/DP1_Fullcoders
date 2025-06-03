# Guía Detallada del Frontend

En esta carpeta se encuentra la interfaz de usuario desarrollada con **React** y
**TypeScript**. El objetivo principal del frontend es mostrar de forma visual la
simulación calculada por el backend. A continuación se describe cada parte con
detalle.

## 1. Estructura Principal

```
Front-end/
├── src/
│   ├── components/   # Componentes de la interfaz
│   ├── context/      # Contexto global de simulación
│   ├── services/     # Llamadas al backend
│   ├── views/        # Páginas principales
│   └── ...           # Archivos de configuración
└── package.json
```

La aplicación se creó con Vite, por lo que los scripts de desarrollo habituales
son `npm install` y `npm run dev` para iniciar el servidor local.

## 2. Contexto de Simulación

El estado global se gestiona en `SimulacionContext.tsx`. Allí se cargan los datos
del mejor individuo obtenido desde el backend mediante la función
`getMejorIndividuo`. Las líneas 30‑58 muestran cómo se procesan los datos
obtenidos y se inicializan los camiones y rutas
【F:Front-end/src/context/SimulacionContext.tsx†L30-L58】.

Las funciones `avanzarHora` y `reiniciar` actualizan la posición de cada camión y
permiten reiniciar la simulación, tal como se observa entre las líneas 69‑101
【F:Front-end/src/context/SimulacionContext.tsx†L69-L101】.

## 3. Componente `Mapa`

El archivo `Mapa.tsx` dibuja una cuadrícula SVG donde se representan los camiones
avanzando por sus rutas. Desde la línea 38 hasta la 78 se inicializan los
camiones visuales y se actualizan cada vez que cambia la simulación
【F:Front-end/src/components/Mapa.tsx†L38-L78】.

Posteriormente, entre las líneas 86‑149 se renderiza el SVG y se ofrecen
controles para iniciar o pausar la animación
【F:Front-end/src/components/Mapa.tsx†L86-L149】.

## 4. Otros Componentes

- `CardCamion.tsx`: muestra tarjetas con el avance de cada camión.
- `TablaPedidos.tsx`: lista los pedidos asignados a las rutas.
- `RightMenu.tsx`: panel lateral con diferentes secciones.
- `Navbar.tsx`: barra superior de navegación.

Todos estos componentes consumen el contexto de simulación para mostrar la
información actualizada.

## 5. Vista Principal

`SimulacionSemanal.tsx` orquesta la página principal combinando el mapa y el
menú derecho. En las líneas 9‑28 se estructura la interfaz con un botón para
expandir u ocultar el menú【F:Front-end/src/views/SimulacionSemanal.tsx†L9-L28】.

## 6. Servicio de API

`simulacionApiService.ts` define la función que consulta el backend en
`http://localhost:8085/api/simulacion/mejor` para obtener la mejor solución. El
código completo se encuentra en las líneas 1‑23
【F:Front-end/src/services/simulacionApiService.ts†L1-L23】.

## 7. Ejecutar el Frontend

Instale las dependencias y ejecute el servidor de desarrollo:

```bash
cd Front-end
npm install
npm run dev
```

La aplicación se abrirá normalmente en `http://localhost:5173` y consumirá el
backend en el puerto 8085.

Este README busca ofrecer una visión amplia de todos los archivos para que
cualquier persona pueda comprender la estructura sin haber revisado el código
previamente.
