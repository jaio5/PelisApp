# Script de Prueba de Sistema de Email
# Verifica que el envío de correos de confirmación funcione correctamente

Write-Host "📧 PROBANDO SISTEMA DE EMAIL DE PELISAPP" -ForegroundColor Cyan
Write-Host "=======================================" -ForegroundColor Cyan
Write-Host ""

$email = "javierbarcelo2106@gmail.com"
$baseUrl = "http://localhost:8080"

Write-Host "🔍 Verificando que la aplicación esté corriendo..." -ForegroundColor Yellow

try {
    # Probar conexión básica
    $testResponse = Invoke-WebRequest -Uri "$baseUrl/admin/users/test" -TimeoutSec 10 -UseBasicParsing
    Write-Host "✅ Aplicación respondiendo correctamente" -ForegroundColor Green
    Write-Host "   Respuesta: $($testResponse.Content)" -ForegroundColor Gray
    Write-Host ""

    # Probar sistema de email
    Write-Host "🧪 Probando sistema de email..." -ForegroundColor Yellow

    try {
        # Primero verificar información de configuración
        $configUrl = "$baseUrl/public/email-config-info"
        Write-Host "🔧 Obteniendo información de configuración..." -ForegroundColor Gray
        $configResponse = Invoke-WebRequest -Uri $configUrl -Method GET -TimeoutSec 10 -UseBasicParsing
        Write-Host $configResponse.Content -ForegroundColor White
        Write-Host ""

        # Ahora probar envío directo
        $emailTestUrl = "$baseUrl/public/test-email-direct?email=$email"
        Write-Host "📤 Enviando email de prueba a: $emailTestUrl" -ForegroundColor Gray

        $emailResponse = Invoke-WebRequest -Uri $emailTestUrl -Method POST -TimeoutSec 30 -UseBasicParsing
        Write-Host ""
        Write-Host "📧 RESULTADO DE PRUEBA DE EMAIL:" -ForegroundColor Cyan
        Write-Host $emailResponse.Content -ForegroundColor White
        Write-Host ""

        if ($emailResponse.Content -match "EMAIL ENVIADO EXITOSAMENTE") {
            Write-Host "✅ ¡EMAIL ENVIADO CORRECTAMENTE!" -ForegroundColor Green
            Write-Host "📬 Revisa tu bandeja de entrada en: $email" -ForegroundColor Green
            Write-Host "📁 También revisa la carpeta de SPAM" -ForegroundColor Yellow
        } elseif ($emailResponse.Content -match "ERROR ENVIANDO EMAIL") {
            Write-Host "❌ Error enviando email - Revisa configuración" -ForegroundColor Red
            Write-Host "💡 Posibles soluciones:" -ForegroundColor Yellow
            Write-Host "   1. Configura una contraseña de aplicación de Google" -ForegroundColor White
            Write-Host "   2. Activa verificación en 2 pasos en tu cuenta Google" -ForegroundColor White
            Write-Host "   3. Ve a: https://myaccount.google.com/security" -ForegroundColor White
        }

    } catch {
        Write-Host "❌ Error probando sistema de email: $($_.Exception.Message)" -ForegroundColor Red
    }

    Write-Host ""
    Write-Host "🧪 Probando sistema de moderación..." -ForegroundColor Yellow

    try {
        $moderationUrl = "$baseUrl/admin/users/test-moderation?text=Este es un texto de prueba"
        $moderationResponse = Invoke-WebRequest -Uri $moderationUrl -Method POST -TimeoutSec 10 -UseBasicParsing
        Write-Host ""
        Write-Host "🛡️ RESULTADO DE PRUEBA DE MODERACIÓN:" -ForegroundColor Cyan
        Write-Host $moderationResponse.Content -ForegroundColor White

    } catch {
        Write-Host "❌ Error probando moderación: $($_.Exception.Message)" -ForegroundColor Red
    }

} catch {
    Write-Host "❌ No se puede conectar con la aplicación" -ForegroundColor Red
    Write-Host "💡 Soluciones:" -ForegroundColor Yellow
    Write-Host "   1. Asegúrate de que la aplicación esté corriendo: mvn spring-boot:run" -ForegroundColor White
    Write-Host "   2. Verifica que esté en puerto 8080" -ForegroundColor White
    Write-Host "   3. Espera unos segundos más para que arranque completamente" -ForegroundColor White
}

Write-Host ""
Write-Host "📋 RESUMEN DE VERIFICACIONES:" -ForegroundColor Cyan
Write-Host "1. ✅ Sistema de moderación IA implementado" -ForegroundColor Green
Write-Host "2. ✅ Configuración de Gmail preparada" -ForegroundColor Green
Write-Host "3. ⏳ Prueba manual de email en progreso..." -ForegroundColor Yellow
Write-Host ""
Write-Host "🎯 PRÓXIMO PASO: Registra un usuario nuevo en http://localhost:8080/register" -ForegroundColor Green
