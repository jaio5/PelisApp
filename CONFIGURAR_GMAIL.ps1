# Script de Configuración de Gmail para PelisApp
# Autor: Sistema de Configuración Automática
# Fecha: 2026-02-05

Write-Host "🔧 CONFIGURACIÓN DE GMAIL PARA PELISAPP" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "📧 Email configurado: javierbarcelo2106@gmail.com" -ForegroundColor Green
Write-Host ""

Write-Host "⚠️  IMPORTANTE - CONFIGURACIÓN DE SEGURIDAD GMAIL:" -ForegroundColor Yellow
Write-Host "1. Para usar Gmail con aplicaciones, necesitas una 'Contraseña de aplicación'" -ForegroundColor White
Write-Host "2. NO uses tu contraseña normal de Gmail" -ForegroundColor Red
Write-Host ""

Write-Host "📝 PASOS PARA CONFIGURAR CONTRASEÑA DE APLICACIÓN:" -ForegroundColor Cyan
Write-Host "1. Ve a tu cuenta de Google: https://myaccount.google.com" -ForegroundColor White
Write-Host "2. Activa la verificación en 2 pasos (si no está activada)" -ForegroundColor White
Write-Host "3. Ve a 'Seguridad' > 'Contraseñas de aplicación'" -ForegroundColor White
Write-Host "4. Selecciona 'Correo' y 'Otra (nombre personalizado)'" -ForegroundColor White
Write-Host "5. Escribe 'PelisApp' como nombre" -ForegroundColor White
Write-Host "6. Google generará una contraseña de 16 caracteres" -ForegroundColor White
Write-Host "7. Usa ESA contraseña en lugar de 'Iirlmnye322*'" -ForegroundColor Yellow
Write-Host ""

Write-Host "🔍 VERIFICANDO CONFIGURACIÓN ACTUAL..." -ForegroundColor Cyan

# Verificar si la aplicación está corriendo
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/admin/users/test" -TimeoutSec 5 -UseBasicParsing
    Write-Host "✅ Aplicación corriendo en puerto 8080" -ForegroundColor Green
} catch {
    Write-Host "❌ Aplicación no está corriendo en puerto 8080" -ForegroundColor Red
    Write-Host "   Ejecuta: mvn spring-boot:run" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "🧪 COMANDOS PARA PROBAR EMAIL:" -ForegroundColor Cyan
Write-Host "1. Probar configuración:" -ForegroundColor White
Write-Host "   POST http://localhost:8080/admin/users/test-email?email=javierbarcelo2106@gmail.com" -ForegroundColor Gray
Write-Host ""
Write-Host "2. Registrar usuario nuevo (enviará email automáticamente):" -ForegroundColor White
Write-Host "   http://localhost:8080/register" -ForegroundColor Gray
Write-Host ""

Write-Host "📋 CONFIGURACIÓN ACTUAL EN APPLICATION.PROPERTIES:" -ForegroundColor Cyan
Write-Host "app.email.enabled=true" -ForegroundColor Gray
Write-Host "spring.mail.host=smtp.gmail.com" -ForegroundColor Gray
Write-Host "spring.mail.port=587" -ForegroundColor Gray
Write-Host "spring.mail.username=javierbarcelo2106@gmail.com" -ForegroundColor Gray
Write-Host "spring.mail.password=Iirlmnye322*  ← CAMBIAR POR CONTRASEÑA DE APLICACIÓN" -ForegroundColor Red
Write-Host "spring.mail.properties.mail.smtp.auth=true" -ForegroundColor Gray
Write-Host "spring.mail.properties.mail.smtp.starttls.enable=true" -ForegroundColor Gray
Write-Host ""

Write-Host "💡 PRÓXIMOS PASOS:" -ForegroundColor Green
Write-Host "1. Configura la contraseña de aplicación de Google" -ForegroundColor White
Write-Host "2. Actualiza application.properties con la nueva contraseña" -ForegroundColor White
Write-Host "3. Reinicia la aplicación" -ForegroundColor White
Write-Host "4. Prueba el registro de un usuario nuevo" -ForegroundColor White
Write-Host ""

Write-Host "🚀 ¡Configuración lista! Ejecuta los pasos anteriores." -ForegroundColor Green
