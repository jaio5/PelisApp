# PelisApp API Documentation

## Descripción General

PelisApp es una aplicación web de gestión de películas desarrollada con Spring Boot. Permite a los usuarios explorar películas, escribir reseñas, gestionar usuarios y administrar el sistema. Incluye integración con TMDB y moderación automática de comentarios mediante IA.

## URL Base

```
http://localhost:8080
```

## Estructura de la API

- **API REST Pública** (`/api`): Endpoints para usuarios, películas, reseñas, autenticación y utilidades.
- **API REST de Administración** (`/api/admin`): Endpoints para gestión de usuarios, carga masiva, moderación, imágenes, reparto, diagnóstico y salud del sistema.
- **Controlador de Imágenes** (`/images`): Servir imágenes estáticas.

## Autenticación

La aplicación utiliza JWT. Los tokens deben incluirse en la cabecera:
```
Authorization: Bearer <token>
```

### Roles de Usuario
- USER
- ADMIN
- SUPERADMIN

---

## Endpoints Principales

### Películas
- `GET /api/movies` — Lista de películas
- `GET /api/movies/{id}/details` — Detalles completos
- `GET /api/movies/tmdb/{tmdbId}/details` — Detalles por TMDB
- `GET /api/movies/{id}/files` — Archivos disponibles
- `GET /movies/download/{movieId}/{fileName}` — Descarga
- `GET /movies/stream/{movieId}/{fileName}` — Streaming

### Reseñas
- `POST /api/reviews` — Crear reseña
- `POST /api/reviews/{id}/like` — Like a reseña

### Autenticación
- `POST /api/auth/register` — Registro
- `POST /api/auth/login` — Login
- `POST /api/auth/refresh` — Refrescar token

### Utilidades
- `GET /api/health` — Estado de la app
- `POST /api/test-email` — Email de prueba

### Administración (`/api/admin`)
#### Gestión de Usuarios
- `POST /api/admin/users/{userId}/confirm-email` — Confirmar email
- `POST /api/admin/users/{userId}/ban` — Banear usuario
- `POST /api/admin/users/{userId}/unban` — Desbanear usuario
- `POST /api/admin/users/{userId}/delete` — Eliminar usuario
- `GET /api/admin/users/search/email?value=` — Buscar usuarios por email
- `GET /api/admin/users/search/username?value=` — Buscar usuarios por nombre de usuario
- `GET /api/admin/users/by-email?email=` — Buscar usuario exacto por email
- `GET /api/admin/users/by-username?username=` — Buscar usuario exacto por nombre de usuario

#### TMDB y Películas
- `POST /api/admin/tmdb/load-movie/{tmdbId}` — Cargar película TMDB
- `POST /api/admin/tmdb/bulk-load` — Carga masiva TMDB
- `POST /api/admin/images/reload` — Recargar posters de películas

#### Reparto
- `GET /api/admin/cast/movie/{movieId}` — Obtener reparto de película
- `POST /api/admin/cast/movie/{movieId}/reload` — Recargar reparto de película

#### Moderación
- `GET /api/admin/moderation/stats` — Estadísticas de moderación
- `GET /api/admin/moderation/pending` — Moderaciones pendientes
- `POST /api/admin/moderation/{moderationId}/approve` — Aprobar moderación manualmente
- `POST /api/admin/moderation/{moderationId}/reject` — Rechazar moderación manualmente

#### Diagnóstico y Salud del Sistema
- `GET /api/admin/debug/movie/{id}` — Debug de película
- `GET /api/admin/email/diagnostic` — Diagnóstico de email
- `GET /api/admin/system/health` — Estado de todas las conexiones del sistema
- `GET /api/admin/system/health/{service}` — Estado de un servicio concreto (database, tmdb, ollama, email, server)

### Imágenes
- `GET /images/{fileName}` — Imagen estática

---

## Modelos de Datos

Incluye MovieListDTO, MovieDetailsDTO, CastDTO, CrewDTO, CommentDTO, LoginRequest, RegisterRequest, LoginResponse, ReviewCreateRequest, ConnectionStatus, User, CommentModeration, etc.

---

## Ejemplos de Uso

Incluye ejemplos de registro, login, obtener películas, crear reseña, carga TMDB, etc.

---

## Notas de Desarrollo

- Autenticación JWT
- Moderación IA (Ollama)
- Integración TMDB
- Almacenamiento local de imágenes
- Validación robusta
- Logging detallado

---

Para más información, consulta los demás documentos en `/docs`.
