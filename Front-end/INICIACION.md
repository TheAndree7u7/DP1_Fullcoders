# Guía de Iniciación al Front-end

## Estructura del Proyecto Front-end

El Front-end está construido con React + TypeScript + Vite, siguiendo una arquitectura modular y escalable.

### Estructura de Carpetas y Archivos

#### Archivos Raíz
- **`main.tsx`**
  - Punto de entrada de la aplicación
  - Configura React y el DOM
  - Inicializa providers globales

- **`App.tsx`**
  - Componente raíz de la aplicación
  - Define el layout principal
  - Configura las rutas principales

- **`types.ts`**
  - Definiciones de tipos TypeScript
  - Interfaces compartidas
  - Tipos de datos comunes

- **`vite-env.d.ts`**
  - Declaraciones de tipos para Vite
  - Configuración del entorno

- **`index.css`**
  - Estilos globales
  - Variables CSS
  - Reset de estilos

#### Carpetas Principales

##### 1. `assets/`
Contiene recursos estáticos:
- Imágenes
- Iconos
- Fuentes
- Otros recursos multimedia

##### 2. `components/`
Componentes reutilizables:
- **`RightMenu.tsx`**
  - Menú lateral derecho
  - Navegación secundaria
  - Acciones rápidas

- **`TablaPedidos.tsx`**
  - Tabla de pedidos
  - Visualización de datos
  - Ordenamiento y filtrado

- **`MetricasRendimiento.tsx`**
  - Métricas de rendimiento
  - Gráficos y estadísticas
  - KPIs principales

- **`Navbar.tsx`**
  - Barra de navegación principal
  - Menú de usuario
  - Búsqueda y acciones

- **`Mapa.tsx`**
  - Visualización de mapas
  - Geolocalización
  - Marcadores y rutas

- **`CardCamion.tsx`**
  - Tarjeta de información de camión
  - Estado y detalles
  - Acciones rápidas

##### 3. `context/`
Contextos de React para estado global:
- **`AuthContext.tsx`**
  - Manejo de autenticación
  - Estado del usuario
  - Tokens JWT

- **`UserContext.tsx`**
  - Datos del usuario
  - Preferencias
  - Configuraciones

##### 4. `data/`
Datos estáticos y mocks:
- **`mockData.ts`**
  - Datos de prueba
  - Simulaciones de API

- **`constants.ts`**
  - Constantes globales
  - Configuraciones

##### 5. `services/`
Servicios para comunicación con el backend:
- **`simulacionApiService.ts`**
  - Servicios de simulación
  - Llamadas a API de simulación
  - Manejo de datos de simulación

##### 6. `views/`
Vistas principales:
- **`SimulacionSemanal.tsx`**
  - Simulación de rutas semanales
  - Planificación de entregas
  - Visualización de resultados

## Orden Recomendado para Entender el Código

### 1. Configuración Inicial
1. **`main.tsx`**
   - Punto de entrada de la aplicación
   - Configuración de React y providers principales

2. **`App.tsx`**
   - Componente raíz
   - Configuración de rutas principales
   - Layout base de la aplicación

3. **`types.ts`**
   - Definiciones de tipos TypeScript
   - Interfaces y tipos compartidos

### 2. Componentes Base (`components/`)
Los componentes están organizados por funcionalidad:
- `common/` - Componentes de uso general (botones, inputs, etc.)
- `layout/` - Componentes de estructura (header, footer, etc.)
- `forms/` - Componentes relacionados con formularios
- `cards/` - Componentes de tipo tarjeta
- `modals/` - Componentes de ventanas modales

### 3. Vistas (`views/`)
Cada vista representa una página completa de la aplicación:
- `Home/` - Página principal
- `Login/` - Página de inicio de sesión
- `Register/` - Página de registro
- `Dashboard/` - Panel de control
- `Profile/` - Perfil de usuario

### 4. Lógica de Negocio
1. **`services/`**
   - `simulacionApiService.ts` - Servicios de simulación
   - Otros servicios específicos

2. **`context/`**
   - `AuthContext.tsx` - Manejo de estado de autenticación
   - `UserContext.tsx` - Manejo de estado de usuario
   - `SimulacionContext.tsx` - Estado de simulación
   - Otros contextos según necesidad

## Flujo de Datos

1. **Interacción del Usuario**
   - El usuario interactúa con componentes en las vistas
   - Los eventos son manejados por los componentes

2. **Manejo de Estado**
   - Estado local: `useState` y `useReducer`
   - Estado global: Context API

3. **Comunicación con Backend**
   - Los servicios en `services/` manejan las llamadas API
   - Axios para peticiones HTTP
   - Manejo de errores y loading states

## Tecnologías y Herramientas

- **React 18+** - Biblioteca principal
- **TypeScript** - Tipado estático
- **Vite** - Build tool y servidor de desarrollo
- **React Router** - Manejo de rutas
- **Axios** - Cliente HTTP
- **Context API** - Manejo de estado global

## Comandos Importantes

```bash
# Instalar dependencias
npm install

# Iniciar servidor de desarrollo
npm run dev

# Construir para producción
npm run build

# Ejecutar tests
npm run test

# Linting
npm run lint
```

## Convenciones de Código

1. **Nombrado de Archivos**
   - Componentes: PascalCase (ej: `UserProfile.tsx`)
   - Utilidades: camelCase (ej: `formatDate.ts`)
   - Estilos: kebab-case (ej: `user-profile.css`)

2. **Estructura de Componentes**
   ```typescript
   // Importaciones
   import React from 'react';
   import { useAuth } from '../context/AuthContext';
   
   // Tipos
   interface Props {
     // ...
   }
   
   // Componente
   export const ComponentName: React.FC<Props> = ({ prop1, prop2 }) => {
     // Hooks
     // Lógica
     // Render
   };
   ```

3. **Manejo de Estilos**
   - CSS Modules para estilos específicos
   - Styled Components para componentes reutilizables
   - Variables CSS para temas y colores

## Buenas Prácticas

1. **Componentes**
   - Mantener componentes pequeños y enfocados
   - Usar composición en lugar de herencia
   - Implementar PropTypes o TypeScript

2. **Estado**
   - Usar Context API para estado global
   - Mantener estado local cuando sea posible
   - Implementar memoización cuando sea necesario

3. **Rendimiento**
   - Usar React.memo para componentes puros
   - Implementar lazy loading para rutas
   - Optimizar re-renders

## Debugging

1. **Herramientas**
   - React Developer Tools
   - Redux DevTools (si se usa Redux)
   - Chrome DevTools

2. **Logging**
   - Usar `console.log` en desarrollo
   - Implementar un sistema de logging en producción

## Recursos Adicionales

- [Documentación de React](https://reactjs.org/docs/getting-started.html)
- [Documentación de TypeScript](https://www.typescriptlang.org/docs/)
- [Documentación de Vite](https://vitejs.dev/guide/) 