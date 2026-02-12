# PelisApp 🎬

Una aplicación web moderna para la gestión y descubrimiento de películas, construida con Spring Boot y tecnologías avanzadas de IA para moderación de contenido.

## 📖 Características Principales

### 🎯 Funcionalidades Core
- **Catálogo de Películas** - Navegación y búsqueda de películas con integración TMDB
- **Sistema de Reseñas** - Usuarios pueden valorar y comentar películas con sistema de likes
- **Autenticación Segura** - Sistema JWT con roles multinivel (USER, MODERATOR, ADMIN, SUPERADMIN)
- **Moderación Inteligente** - IA integrada (Ollama) para moderación automática de contenido
- **Panel de Administración** - Gestión completa de usuarios, películas y contenido
- **Gestión de Imágenes** - Almacenamiento local de posters y perfiles con fallbacks
- **Sistema de Email** - Confirmación de cuentas y notificaciones automatizadas

### 🛠️ Stack Tecnológico

| Categoría | Tecnologías |
|-----------|-------------|
| **Backend Framework** | Spring Boot 3.2.10, Spring Security, Spring Data JPA |
| **Lenguaje** | Java 17 (OpenJDK) |
| **Base de Datos** | MySQL 8.0+ con Hibernate ORM |
| **Autenticación** | JWT (JSON Web Tokens) con refresh tokens |
| **APIs Externas** | TMDB (The Movie Database) API v3 |
| **IA/ML** | Ollama para moderación de contenido |
| **Email** | Spring Mail con Gmail SMTP |
| **Cache** | Caffeine para optimización de rendimiento |
| **Frontend** | Thymeleaf, HTML5, CSS3, JavaScript ES6+ |
| **Build Tool** | Maven 3.9+ |
| **Documentation** | Comprehensive Markdown docs |

### 🏛️ Arquitectura del Sistema

```
┌─────────────────────────────────────────────┐
│               CAPA DE PRESENTACIÓN           │
│  Controllers (Web + API + Admin)           │
├─────────────────────────────────────────────┤
│               CAPA DE APLICACIÓN             │
│  Services (Movie, Auth, Review, etc)       │
├─────────────────────────────────────────────┤
│               CAPA DE DOMINIO               │
│  Entities + DTOs + Business Logic          │
├─────────────────────────────────────────────┤
│               CAPA DE PERSISTENCIA          │
│  Repositories (JPA) + MySQL                │
├─────────────────────────────────────────────┤
│               CAPA DE INFRAESTRUCTURA       │
│  TMDB API + Ollama AI + Email SMTP         │
└─────────────────────────────────────────────┘
```

### 🎨 Características Técnicas Avanzadas

- **Controladores Unificados** - Arquitectura simplificada de 3 controladores principales
- **API REST Completa** - Endpoints públicos y administrativos bien documentados
- **Servicios Especializados** - Cada dominio con su servicio dedicado
- **DTOs Optimizados** - Transferencia de datos eficiente entre capas
- **Manejo Centralizado de Excepciones** - Error handling robusto y consistente
- **Moderación con IA** - Sistema automático de detección de contenido tóxico
- **Carga Masiva TMDB** - Importación automatizada de películas populares
- **Sistema de Roles Jerárquico** - Permisos granulares por funcionalidad

---

## 🚀 Quick Start

### Prerrequisitos
- Java 17+
- Maven 3.9+
- MySQL 8.0+
- Cuenta TMDB (para API key)

### Instalación Rápida
```bash
# 1. Clonar repositorio
git clone https://github.com/tu-usuario/PelisApp.git
cd PelisApp

# 2. Configurar base de datos
mysql -u root -p -e "CREATE DATABASE pelisapp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 3. Configurar variables de entorno
export JWT_SECRET="mySecretKey123456789012345678901234567890"
export TMDB_BEARER_TOKEN="your_tmdb_bearer_token"
export DB_PASSWORD="your_mysql_password"

# 4. Ejecutar aplicación
mvn spring-boot:run
```

### Verificar Instalación
```bash
# Health check
curl http://localhost:8080/api/health

# Listar películas
curl http://localhost:8080/api/movies
```

---

## 📚 Documentación Completa

### 📖 Documentación Principal
| Documento | Descripción | Audiencia |
|-----------|-------------|-----------|
| **[📋 Índice General](docs/INDEX.md)** | Portal central de documentación | Todos |
| **[🔌 API Reference](docs/API.md)** | Documentación completa de endpoints REST | Desarrolladores, Integradores |
| **[🏗️ Arquitectura](docs/ARCHITECTURE.md)** | Diseño técnico y patrones utilizados | Desarrolladores Senior, Arquitectos |
| **[📦 Instalación](docs/INSTALLATION.md)** | Guía paso a paso de setup | Nuevos Desarrolladores |
| **[⚙️ Configuración](docs/CONFIGURATION.md)** | Variables de entorno y configuraciones | DevOps, Administradores |
| **[🗄️ Base de Datos](docs/DATABASE.md)** | Modelo de datos y esquemas | Backend Developers, DBAs |
| **[🚀 Deployment](docs/DEPLOYMENT.md)** | Guías de producción y despliegue | DevOps, SRE |
| **[👨‍💻 Desarrollo](docs/DEVELOPER.md)** | Estándares y mejores prácticas | Desarrolladores |
| **[🧪 Testing](docs/TESTING.md)** | Guía de testing y debugging | QA, Desarrolladores |

### 📋 Documentación de Proyecto
| Documento | Descripción |
|-----------|-------------|
| **[🤝 Contribución](CONTRIBUTING.md)** | Guía para contribuir al proyecto |
| **[🔄 Changelog](CHANGELOG.md)** | Historial de cambios y versiones |

### 🎯 Inicio Rápido por Rol

**🆕 Nuevo Desarrollador:**
1. [📦 Instalación](docs/INSTALLATION.md)
2. [👨‍💻 Desarrollo](docs/DEVELOPER.md) 
3. [🧪 Testing](docs/TESTING.md)

**🔧 DevOps/Admin:**
1. [⚙️ Configuración](docs/CONFIGURATION.md)
2. [🚀 Deployment](docs/DEPLOYMENT.md)
3. [🗄️ Base de Datos](docs/DATABASE.md)

**💻 Frontend/Integrador:**
1. [🔌 API Reference](docs/API.md)
2. [📋 Índice General](docs/INDEX.md)
3. [🧪 Testing](docs/TESTING.md)

**🏗️ Arquitecto/Senior:**
1. [🏗️ Arquitectura](docs/ARCHITECTURE.md)
2. [🔌 API Reference](docs/API.md)
3. [🗄️ Base de Datos](docs/DATABASE.md)

---

## 🚀 Inicio Rápido

### Prerrequisitos
- Java 17+
- MySQL 8.0+
- Maven 3.9+
- [Ollama](https://ollama.ai/) (opcional, para moderación IA)

### Instalación
```bash
# Clonar el repositorio
git clone [url-del-repositorio]
cd PelisApp

# Configurar base de datos MySQL
mysql -u root -p < scripts/create-database.sql

# Configurar variables de entorno
cp .env.example .env
# Editar .env con tus configuraciones

# Instalar dependencias y ejecutar
mvn clean install
mvn spring-boot:run
```

La aplicación estará disponible en: `http://localhost:8080`

## 📚 Documentación

### 📋 Guías Principales
- **[📚 Índice de Documentación](docs/INDEX.md)** - Centro de navegación de toda la documentación
- **[📦 Guía de Instalación](docs/INSTALLATION.md)** - Setup completo paso a paso
- **[📡 Documentación de API](docs/API.md)** - Endpoints REST y ejemplos
- **[🏗️ Arquitectura del Sistema](docs/ARCHITECTURE.md)** - Diseño y estructura del proyecto
- **[👨‍💻 Guía para Desarrolladores](docs/DEVELOPER.md)** - Desarrollo y contribución

### 🗃️ Referencias Técnicas
- **[🗄️ Base de Datos](docs/DATABASE.md)** - Esquema y modelo de datos
- **[🚀 Deployment](docs/DEPLOYMENT.md)** - Despliegue en producción
- **[⚙️ Configuración](docs/CONFIGURATION.md)** - Variables y parámetros

### 🔧 Utilidades
- **[🛠️ Scripts](scripts/)** - Scripts de mantenimiento y configuración
- **[🗂️ Controladores Legacy](backup_old_controllers/)** - Controladores anteriores (backup)

## 🏗️ Estructura del Proyecto

```
PelisApp/
├── src/main/java/alicanteweb/pelisapp/
│   ├── controller/          # Controladores REST y Web
│   ├── service/            # Lógica de negocio
│   ├── entity/             # Entidades JPA
│   ├── dto/                # Data Transfer Objects
│   ├── repository/         # Repositorios JPA
│   ├── security/           # Configuración de seguridad
│   ├── config/             # Configuración de Spring
│   ├── exception/          # Manejo de excepciones
│   ├── tmdb/               # Integración TMDB
│   └── util/               # Utilidades
├── src/main/resources/
│   ├── templates/          # Templates Thymeleaf
│   ├── static/             # CSS, JS, recursos estáticos
│   └── sql/                # Scripts SQL iniciales
├── data/images/            # Almacenamiento de imágenes
├── scripts/                # Scripts de utilidad
└── docs/                   # Documentación
```

## 🌟 Funcionalidades Destacadas

### 🔐 Sistema de Autenticación
- Registro con confirmación por email
- Login seguro con JWT tokens
- Refresh tokens para sesiones persistentes
- Roles y permisos granulares

### 🎬 Gestión de Películas
- Importación automática desde TMDB
- Descarga y optimización de posters
- Gestión de reparto y equipo técnico
- Búsqueda y filtrado avanzado

### 💬 Sistema de Reseñas
- Valoraciones numéricas (1-10)
- Comentarios textuales
- Sistema de likes para reseñas
- Moderación automática con IA

### 🛡️ Moderación Inteligente
- Análisis de toxicidad con Ollama
- Moderación automática de contenido
- Panel de administración para revisión manual
- Configuración de umbrales personalizables

### 👥 Gestión de Usuarios
- Perfiles de usuario personalizables
- Sistema de seguimiento entre usuarios
- Historial de actividad
- Panel administrativo completo

## 🔧 API Endpoints

### Endpoints Públicos
```
GET    /                           # Página principal
GET    /pelicula/{id}              # Detalle de película
POST   /api/auth/login             # Autenticación
GET    /api/movies/{id}/details    # Información de película
POST   /api/reviews                # Crear reseña
```

### Endpoints Administrativos
```
POST   /api/admin/tmdb/load-movie/{id}    # Cargar película desde TMDB
GET    /api/admin/users                   # Listar usuarios
POST   /api/admin/moderation/review       # Moderar contenido
```

## 🚀 Estado del Proyecto

- ✅ **Core completo** - Funcionalidades principales implementadas
- ✅ **API REST** - Endpoints públicos y administrativos funcionales
- ✅ **Integración TMDB** - Importación automática de datos
- ✅ **Moderación IA** - Sistema Ollama integrado
- ✅ **Panel Admin** - Gestión completa implementada
- 🔄 **En desarrollo** - Optimizaciones y nuevas características

## 🤝 Contribución

1. Fork el proyecto
2. Crear rama para feature (`git checkout -b feature/nueva-funcionalidad`)
3. Commit cambios (`git commit -am 'Añadir nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Crear Pull Request

Ver [DEVELOPER.md](docs/DEVELOPER.md) para guías detalladas de desarrollo.

## 📄 Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para detalles.

## 📞 Soporte

- **Issues**: [GitHub Issues](../../issues)
- **Documentación**: Consultar carpeta `docs/`
- **Email**: [tu-email@ejemplo.com]

---

**PelisApp** - Descubre, valora y comparte tu pasión por el cine 🎬✨
