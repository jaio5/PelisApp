# Controladores Unificados - PelisApp

Este directorio contiene la nueva estructura de controladores unificados para una mejor organización y mantenimiento del código.

## Estructura Actual

### Controladores Principales
- **WebController.java** - Todas las vistas web HTML (páginas que devuelven templates)
  - Página principal (/)
  - Detalles de película (/pelicula/{id})
  - Login y registro (/login, /register, /confirm-email)
  - Perfil de usuario (/perfil)
  - Panel de administración (/admin/*)

- **ApiController.java** - API REST pública (/api/*)
  - API de películas (/api/movies/*)
  - API de reseñas (/api/reviews/*)
  - API de autenticación (/api/auth/*)
  - Health check (/api/health)
  - Testing de email (/api/test-email)

- **AdminApiController.java** - API REST de administración (/api/admin/*)
  - Gestión de usuarios (/api/admin/users/*)
  - Integración TMDB (/api/admin/tmdb/*)
  - Gestión de reparto (/api/admin/cast/*)
  - Sistema de moderación (/api/admin/moderation/*)
  - Debug y diagnósticos (/api/admin/debug/*, /api/admin/email/*)

### Controladores Mantenidos
- **RestExceptionHandler.java** - Manejo global de excepciones
- **ImageController.java** - Servir imágenes estáticas

### Controllers en backup_old_controllers/
Los siguientes controladores han sido unificados y movidos a backup:
- MovieController.java → Unificado en ApiController.java
- ReviewController.java → Unificado en ApiController.java
- AuthController.java → Unificado en ApiController.java
- HomeController.java → Unificado en WebController.java
- LoginController.java → Unificado en WebController.java
- RegisterController.java → Unificado en WebController.java
- PerfilController.java → Unificado en WebController.java
- AdminController.java → Unificado en WebController.java
- MovieDetailController.java → Unificado en WebController.java
- TMDBLoadController.java → Unificado en AdminApiController.java
- ModerationController.java → Unificado en AdminApiController.java
- CastController.java → Unificado en AdminApiController.java
- HealthController.java → Unificado en ApiController.java
- EmailTestController.java → Unificado en ApiController.java
- DebugController.java → Unificado en AdminApiController.java
- AdminUserController.java → Unificado en AdminApiController.java

## Ventajas de la Nueva Estructura

1. **Menos archivos** - De 16+ controladores a solo 3 principales
2. **Separación clara de responsabilidades**:
   - WebController: Solo vistas HTML
   - ApiController: API REST pública
   - AdminApiController: API REST administrativa
3. **Fácil mantenimiento** - Toda la lógica relacionada está en un lugar
4. **Mejor organización** - Rutas agrupadas lógicamente
5. **Menos duplicación** - Código común centralizado

## Rutas Principales

### Web (HTML)
```
GET  /                    - Página principal
GET  /pelicula/{id}       - Detalle de película
POST /pelicula/{id}/review - Añadir reseña
GET  /login              - Login
GET  /register           - Registro
POST /register           - Procesar registro
GET  /confirm-email      - Confirmación email
GET  /perfil             - Perfil usuario
GET  /admin              - Panel admin
GET  /admin/users        - Gestión usuarios
```

### API REST Pública
```
GET  /api/movies/{id}/details        - Detalles película
GET  /api/movies/tmdb/{id}/details   - Detalles por TMDB ID
POST /api/reviews                    - Crear reseña
POST /api/reviews/{id}/like         - Like reseña
POST /api/auth/register             - Registro API
POST /api/auth/login                - Login API
POST /api/auth/refresh              - Refresh token
GET  /api/health                    - Health check
```

### API REST Administrativa
```
POST /api/admin/users/{id}/roles        - Asignar rol
DELETE /api/admin/users/{id}/roles/{id}  - Quitar rol
GET  /api/admin/tmdb/test               - Test TMDB
POST /api/admin/tmdb/load-movie/{id}    - Cargar película
POST /api/admin/tmdb/bulk-load          - Carga masiva
GET  /api/admin/moderation/stats        - Stats moderación
POST /api/admin/images/reload           - Recargar imágenes
```

## Notas de Migración

- Todos los endpoints mantienen la misma funcionalidad
- Los métodos de servicio se mantienen igual
- La compilación es exitosa
- Los controllers antiguos están en backup por seguridad
