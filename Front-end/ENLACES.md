# Enlaces y Dependencias del Front-end

## Dependencias y Referencias

### 1. Servicios de API (`src/services/`)
- **`api.ts`**
  - Depende de: `Back-end/src/routes/api.ts`
  - Es usado en:
    - `src/services/auth.service.ts`
    - `src/services/user.service.ts`
    - `src/context/AuthContext.tsx`
    - `src/views/Login.tsx`
    - `src/views/Register.tsx`
    - `src/views/Profile.tsx`
  - Configuración base de Axios y endpoints
  - URL base: `http://localhost:3000/api`

- **`auth.service.ts`**
  - Depende de: `Back-end/src/controllers/auth.controller.ts`
  - Es usado en:
    - `src/context/AuthContext.tsx`
    - `src/views/Login.tsx`
    - `src/views/Register.tsx`
    - `src/components/ProtectedRoute.tsx`
  - Endpoints:
    - POST `/auth/login`
    - POST `/auth/register`
    - GET `/auth/me`

- **`user.service.ts`**
  - Depende de: `Back-end/src/controllers/user.controller.ts`
  - Es usado en:
    - `src/views/Profile.tsx`
    - `src/components/UserCard.tsx`
    - `src/context/UserContext.tsx`
  - Endpoints:
    - GET `/users/profile`
    - PUT `/users/profile`
    - GET `/users/:id`

### 2. Tipos Compartidos
- **`src/types.ts`**
  - Depende de: `Back-end/src/types/index.ts`
  - Es usado en:
    - `src/services/auth.service.ts`
    - `src/services/user.service.ts`
    - `src/context/AuthContext.tsx`
    - `src/context/UserContext.tsx`
    - `src/views/Profile.tsx`
    - `src/components/UserCard.tsx`
  - Interfaces compartidas:
    - `User`
    - `AuthResponse`
    - `ApiResponse`

### 3. Contextos
- **`src/context/AuthContext.tsx`**
  - Depende de: `Back-end/src/middleware/auth.middleware.ts`
  - Es usado en:
    - `src/components/ProtectedRoute.tsx`
    - `src/App.tsx`
    - `src/views/Login.tsx`
    - `src/views/Register.tsx`
    - `src/views/Profile.tsx`
  - Maneja tokens JWT y autenticación

### 4. Componentes que Consumen API
- **`src/views/Login.tsx`**
  - Depende de: `Back-end/src/controllers/auth.controller.ts`
  - Es usado en: `src/App.tsx` (rutas)
  - Endpoint: POST `/auth/login`

- **`src/views/Register.tsx`**
  - Depende de: `Back-end/src/controllers/auth.controller.ts`
  - Es usado en: `src/App.tsx` (rutas)
  - Endpoint: POST `/auth/register`

- **`src/views/Profile.tsx`**
  - Depende de: `Back-end/src/controllers/user.controller.ts`
  - Es usado en: `src/App.tsx` (rutas)
  - Endpoints:
    - GET `/users/profile`
    - PUT `/users/profile`

## Variables de Entorno
- **`.env`**
  - `VITE_API_URL`: URL del backend
    - Es usado en: `src/services/api.ts`
  - `VITE_API_VERSION`: Versión de la API
    - Es usado en: `src/services/api.ts`

## Notas Importantes
1. Cualquier cambio en los endpoints del backend debe reflejarse en los servicios correspondientes
2. Los tipos compartidos deben mantenerse sincronizados entre frontend y backend
3. Las respuestas de la API deben seguir el formato definido en `types.ts`

## Manejo de Errores
- Los errores del backend son manejados en `src/services/api.ts`
  - Es usado en: Todos los servicios y componentes que hacen llamadas API
- Los códigos de error deben coincidir con los definidos en el backend

## Seguridad
- Los tokens JWT son manejados en `AuthContext.tsx`
  - Es usado en: Todos los componentes que requieren autenticación
- Las peticiones autenticadas incluyen el header `Authorization`
- Las rutas protegidas dependen del middleware de autenticación del backend 