@echo off
cls
echo ================================================================================
echo                    CONFIGURACION DE EMAIL REAL - PELISAPP
echo                           VERSION CORREGIDA v2.0
echo ================================================================================
echo.
echo Este script configura Gmail para enviar emails reales desde PelisApp.
echo IMPORTANTE: Primero debes configurar Gmail, luego ejecutar este script.
echo.
echo ================================================================================
echo                              PASO PREVIO: GMAIL
echo ================================================================================
echo.
echo 1. Ve a: https://myaccount.google.com/security
echo 2. Activa "Verificacion en 2 pasos" (si no la tienes)
echo 3. Busca "Contraseñas de aplicaciones"
echo 4. Crea nueva contraseña para "PelisApp"
echo 5. Copia la contraseña de 16 caracteres (ej: abcd efgh ijkl mnop)
echo.
echo ¿YA tienes la contraseña de aplicacion de Gmail? (s/n)
set /p GMAIL_READY=
if /i not "%GMAIL_READY%"=="s" (
    echo.
    echo Primero configura Gmail y luego ejecuta este script de nuevo.
    pause
    exit /b 1
)

echo.
echo ================================================================================
echo                        CONFIGURACION DE VARIABLES
echo ================================================================================
echo.

set /p EMAIL_USER="Tu email de Gmail (ej: tuusuario@gmail.com): "
echo.
set /p EMAIL_PASS="Contraseña de aplicacion (16 caracteres): "
echo.

echo ================================================================================
echo                           CONFIGURANDO SISTEMA...
echo ================================================================================
echo.

REM Configurar variables de entorno para esta sesión
set EMAIL_USERNAME=%EMAIL_USER%
set EMAIL_PASSWORD=%EMAIL_PASS%
set EMAIL_FROM=noreply@pelisapp.com

echo ✅ Variables configuradas:
echo    EMAIL_USERNAME = %EMAIL_USERNAME%
echo    EMAIL_PASSWORD = [OCULTA POR SEGURIDAD]
echo    EMAIL_FROM     = %EMAIL_FROM%
echo.

echo ================================================================================
echo                         HABILITANDO EMAILS REALES...
echo ================================================================================
echo.

REM Hacer backup del application.properties original
copy "src\main\resources\application.properties" "src\main\resources\application.properties.backup" >nul 2>&1

REM Habilitar emails reales
powershell -Command "(Get-Content 'src\main\resources\application.properties') -replace 'app.email.enabled=false', 'app.email.enabled=true' | Set-Content 'src\main\resources\application.properties'" 2>nul

echo ✅ Emails reales habilitados en application.properties
echo ✅ Backup creado: application.properties.backup
echo.

echo ================================================================================
echo                           INICIANDO APLICACION...
echo ================================================================================
echo.

echo Deteniendo aplicacion anterior si existe...
taskkill /F /IM java.exe >nul 2>&1

echo Compilando con nueva configuracion...
mvn compile >nul

if %ERRORLEVEL% neq 0 (
    echo ❌ Error en compilacion. Revisa el codigo fuente.
    pause
    exit /b 1
)

echo.
echo ✅ Compilacion exitosa
echo.
echo Iniciando aplicacion con emails reales...
echo.
echo ⚠️  IMPORTANTE: NO cierres esta ventana mientras usas la aplicacion.
echo    Las variables de entorno solo funcionan en esta sesion.
echo.
echo 🌐 Aplicacion disponible en: http://localhost:8080
echo 📝 Para probar: http://localhost:8080/register
echo.
echo ================================================================================

mvn spring-boot:run
