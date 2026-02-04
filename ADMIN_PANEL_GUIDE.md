# 🎬 PELISAPP - GUÍA COMPLETA DEL PANEL ADMIN
# ==============================================

## 🚀 GUÍA DE USO DEL PANEL DE ADMINISTRACIÓN

### 📋 ACCESO AL PANEL ADMIN

#### **🔐 Credenciales de Administrador:**
- **Usuario:** `admin`
- **Contraseña:** `admin123`

#### **🌐 URLs de Acceso:**
- **Panel Principal:** `http://localhost:8080/admin`
- **Login Admin:** `http://localhost:8080/login`

### 🗺️ NAVEGACIÓN DEL PANEL

#### **📊 Dashboards Disponibles:**

1. **Dashboard Principal** (`/admin`)
   - Vista general del sistema
   - Métricas básicas
   - Acceso rápido a funciones principales

2. **Dashboard Detallado** (`/admin/dashboard`)
   - Estadísticas avanzadas en tiempo real
   - Gráficos interactivos con Chart.js
   - Actividad reciente del sistema
   - Herramientas de mantenimiento

3. **Reportes y Análisis** (`/admin/reports`)
   - Análisis profundo de usuarios
   - Gráficos de crecimiento
   - Métricas de rendimiento
   - Exportación de datos

### 👥 GESTIÓN DE USUARIOS

#### **Funcionalidades en `/admin/users`:**

**🔍 Búsqueda y Filtrado:**
- Búsqueda por nombre de usuario o email
- Filtros por rol (ADMIN, MODERATOR, USER)
- Filtros por estado (activo, pendiente, bloqueado)
- Filtros por fecha de registro

**📊 Estadísticas de Usuarios:**
- Total de usuarios registrados
- Usuarios activos (email confirmado)
- Usuarios pendientes de confirmación
- Nuevos usuarios este mes

**⚙️ Acciones de Moderación:**
- Ver detalles completos del usuario
- Activar/desactivar cuentas
- Resetear contraseñas
- Eliminar usuarios
- Gestionar roles

**📋 Vista de Lista:**
- Tabla paginada con DataTables
- Selección múltiple para acciones en lote
- Avatar y información básica
- Estado de verificación de email
- Roles asignados
- Fecha de último acceso

### 🛡️ SISTEMA DE MODERACIÓN

#### **Panel de Moderación (`/admin/moderation`):**

**📝 Cola de Revisión:**
- Reseñas pendientes de aprobación
- Contenido reportado por usuarios
- Detección automática de toxicidad
- Filtros por nivel de toxicidad

**🤖 IA de Moderación:**
- Análisis automático de toxicidad
- Umbral configurable (0.7 por defecto)
- Indicadores visuales de riesgo
- Sugerencias de acción

**⚡ Acciones Disponibles:**
- Aprobar reseñas individuales
- Rechazar contenido inapropiado
- Acciones masivas (bulk actions)
- Revisión detallada con contexto

**📊 Métricas de Moderación:**
- Reseñas pendientes
- Contenido reportado
- Aprobaciones del día
- Total de reseñas en el sistema

### 🎬 GESTIÓN DE PELÍCULAS

#### **Administración de Contenido (`/admin/movies`):**

**📥 Carga desde TMDB:**
- Películas populares
- Películas mejor valoradas
- Películas en tendencia
- Configuración de páginas a cargar

**🔧 Herramientas de Gestión:**
- Ver total de películas en BD
- Estadísticas de carga
- Historial de importaciones
- Gestión de imágenes/posters

#### **Carga Masiva (`/admin/bulk-loader`):**

**📦 Presets de Carga:**
- Carga básica (100 películas)
- Carga completa (500+ películas)
- Carga personalizada
- Monitoreo en tiempo real

### ⚙️ CONFIGURACIÓN DEL SISTEMA

#### **Configuración General (`/admin/system-config`):**

**🗄️ Base de Datos:**
- Estado de conexión
- Configuración MySQL
- Estadísticas de uso
- Herramientas de optimización

**🔌 APIs y Servicios:**
- Estado de TMDB API
- Configuración de endpoints
- Límites de rate limiting
- URLs de servicios

**📧 Servicio de Email:**
- Estado de configuración SMTP
- Credenciales de Gmail
- Test de conectividad
- Estadísticas de envío

**💾 Almacenamiento:**
- Uso del espacio en disco
- Gestión de imágenes
- Limpieza de archivos temporales
- Configuración de paths

#### **Configuración de Email (`/admin/email-config`):**

**📨 Panel de Control:**
- Estado actual del servicio
- Configuración SMTP detallada
- Test de conexión
- Envío de emails de prueba

**🔧 Herramientas de Diagnóstico:**
- Verificación de credenciales
- Test de autenticación Gmail
- Monitoreo de entregas
- Logs de errores

### 📊 REPORTES Y ANÁLISIS

#### **Analytics Avanzado (`/admin/reports`):**

**📈 Métricas Principales:**
- Crecimiento de usuarios
- Actividad de la plataforma
- Distribución demográfica
- Análisis de engagement

**🎯 Reportes Específicos:**
- Películas más populares
- Usuarios más activos
- Tendencias de reseñas
- Análisis de toxicidad

**📋 Exportación de Datos:**
- Reportes en CSV
- Análisis de actividad
- Métricas de rendimiento
- Datos históricos

### 🛠️ HERRAMIENTAS DE MANTENIMIENTO

#### **Funciones Automáticas:**

**🔍 Verificación de Salud:**
- Estado de todos los servicios
- Conectividad de BD
- Disponibilidad de APIs
- Métricas de rendimiento

**🧹 Limpieza del Sistema:**
- Limpiar caché de aplicación
- Eliminar archivos temporales
- Optimizar base de datos
- Compactar logs

**📊 Optimización:**
- Reindexación de BD
- Compresión de imágenes
- Limpieza de sesiones
- Actualización de estadísticas

### 🔐 SEGURIDAD Y PERMISOS

#### **Control de Acceso:**
- Autenticación obligatoria
- Verificación de rol ADMIN
- Protección CSRF
- Sesiones seguras

#### **Auditoría:**
- Logs de acciones admin
- Historial de cambios
- Seguimiento de actividad
- Alertas de seguridad

### 📱 COMPATIBILIDAD Y RESPONSIVE

#### **Dispositivos Soportados:**
- ✅ Desktop (1920x1080+)
- ✅ Tablet (768x1024)
- ✅ Mobile (375x667+)

#### **Navegadores Compatibles:**
- ✅ Chrome 90+
- ✅ Firefox 88+
- ✅ Safari 14+
- ✅ Edge 90+

---

## 🎯 FLUJOS DE TRABAJO TÍPICOS

### 📝 **Moderación Diaria:**
1. Acceder a `/admin/moderation`
2. Revisar cola de contenido pendiente
3. Filtrar por alta toxicidad
4. Aprobar/rechazar según políticas
5. Revisar reportes de usuarios

### 👥 **Gestión de Usuarios:**
1. Ir a `/admin/users`
2. Buscar usuarios problemáticos
3. Revisar actividad reciente
4. Aplicar acciones correctivas
5. Verificar confirmaciones de email

### 📊 **Análisis Semanal:**
1. Acceder a `/admin/reports`
2. Revisar métricas de crecimiento
3. Analizar top películas
4. Exportar datos para reuniones
5. Identificar tendencias

### 🔧 **Mantenimiento Mensual:**
1. Verificar `/admin/system-config`
2. Ejecutar limpieza de sistema
3. Optimizar base de datos
4. Revisar uso de almacenamiento
5. Actualizar configuraciones

---

## 🎉 **¡PANEL ADMIN LISTO PARA USAR!**

El panel de administración de PelisApp está **100% completo** y **listo para producción** con todas las funcionalidades necesarias para gestionar eficientemente la plataforma.
