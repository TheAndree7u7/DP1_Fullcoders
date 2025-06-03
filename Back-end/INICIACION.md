# Guía de Iniciación al Proyecto

## Estructura del Proyecto

El proyecto está organizado en dos partes principales:

### Front-end
Ubicado en la carpeta `Front-end/`, contiene toda la interfaz de usuario y la lógica del cliente.

### Back-end
Ubicado en la carpeta `Back-end/`, contiene toda la lógica del servidor y la base de datos.

## Orden Recomendado para Entender el Código

### 1. Front-end

#### Estructura de Carpetas
```
Front-end/src/
├── assets/         # Imágenes, iconos y recursos estáticos
├── components/     # Componentes reutilizables
├── context/        # Contextos de React para manejo de estado global
├── data/          # Datos estáticos y mocks
├── services/      # Servicios para comunicación con el backend
├── views/         # Componentes principales de cada vista
└── types.ts       # Definiciones de tipos TypeScript
```

#### Orden de Lectura Recomendado:

1. **Configuración Inicial**
   - `main.tsx` - Punto de entrada de la aplicación
   - `App.tsx` - Componente raíz
   - `types.ts` - Definiciones de tipos

2. **Componentes Base**
   - Revisar `components/` para entender los componentes reutilizables
   - Cada componente tiene su propio archivo de estilos

3. **Vistas Principales**
   - Revisar `views/` para entender las páginas principales
   - Cada vista puede tener sus propios componentes

4. **Lógica de Negocio**
   - `services/` - Comunicación con el backend
   - `context/` - Manejo de estado global

### 2. Back-end

#### Estructura de Carpetas
```
Back-end/
├── src/
│   ├── controllers/    # Controladores de la lógica de negocio
│   ├── models/         # Modelos de datos
│   ├── routes/         # Definición de rutas
│   ├── services/       # Servicios y lógica de negocio
│   └── utils/          # Utilidades y helpers
└── prisma/            # Configuración y esquemas de la base de datos
```

#### Orden de Lectura Recomendado:

1. **Configuración de Base de Datos**
   - Revisar `prisma/schema.prisma` para entender la estructura de datos

2. **API y Rutas**
   - Revisar `routes/` para entender los endpoints disponibles
   - Cada ruta está conectada a un controlador específico

3. **Lógica de Negocio**
   - `controllers/` - Manejo de peticiones
   - `services/` - Lógica de negocio
   - `models/` - Interacción con la base de datos

## Flujo de Datos

1. El usuario interactúa con la interfaz en el Front-end
2. Las acciones del usuario son manejadas por componentes en `views/`
3. Los componentes utilizan servicios de `services/` para comunicarse con el Back-end
4. El Back-end procesa las peticiones a través de sus rutas y controladores
5. Los datos son almacenados/recuperados de la base de datos
6. La respuesta vuelve al Front-end para actualizar la interfaz

## Tecnologías Principales

- **Front-end**: React, TypeScript, Vite
- **Back-end**: Node.js, Express, Prisma
- **Base de Datos**: PostgreSQL

## Comandos Importantes

### Front-end
```bash
npm install    # Instalar dependencias
npm run dev    # Iniciar servidor de desarrollo
```

### Back-end
```bash
npm install    # Instalar dependencias
npm run dev    # Iniciar servidor de desarrollo
```

## Notas Adicionales

- El proyecto utiliza TypeScript para mejor tipado y mantenibilidad
- Se implementa un sistema de autenticación JWT
- La comunicación entre Front-end y Back-end se realiza mediante API REST
- Se utiliza Prisma como ORM para la base de datos 