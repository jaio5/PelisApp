# Diagnóstico Específico de Gmail para PelisApp
# Identifica problemas comunes con Gmail SMTP

Write-Host "🔧 DIAGNÓSTICO DE CONFIGURACIÓN GMAIL" -ForegroundColor Cyan
Write-Host "====================================" -ForegroundColor Cyan
Write-Host ""

# Información de la cuenta
$cuenta = "javierbarcelo2106@gmail.com"
Write-Host "📧 Cuenta configurada: $cuenta" -ForegroundColor Green
Write-Host ""

Write-Host "🔍 VERIFICANDO CONFIGURACIÓN ACTUAL..." -ForegroundColor Yellow

# Leer configuración del application.properties
$configPath = "src\main\resources\application.properties"
if (Test-Path $configPath) {
    Write-Host "✅ Archivo application.properties encontrado" -ForegroundColor Green

    $config = Get-Content $configPath
    $emailConfig = $config | Where-Object { $_ -match "spring.mail" -or $_ -match "app.email" }

    Write-Host ""
    Write-Host "📋 Configuración actual de email:" -ForegroundColor Cyan
    foreach ($line in $emailConfig) {
        if ($line -match "password") {
            Write-Host "   $($line -replace 'Iirlmnye322\*', '***OCULTA***')" -ForegroundColor Gray
        } else {
            Write-Host "   $line" -ForegroundColor Gray
        }
    }

} else {
    Write-Host "❌ No se encuentra application.properties" -ForegroundColor Red
}

Write-Host ""
Write-Host "🚨 PROBLEMA DETECTADO - AUTENTICACIÓN GMAIL" -ForegroundColor Red
Write-Host ""
Write-Host "📌 El problema más común es usar la contraseña normal de Gmail" -ForegroundColor Yellow
Write-Host "   Gmail requiere 'Contraseña de aplicación' para aplicaciones externas" -ForegroundColor White
Write-Host ""

Write-Host "✅ SOLUCIÓN PASO A PASO:" -ForegroundColor Green
Write-Host ""
Write-Host "1️⃣  Ve a tu cuenta de Google:" -ForegroundColor Cyan
Write-Host "     https://myaccount.google.com/security" -ForegroundColor Blue
Write-Host ""

Write-Host "2️⃣  Activa la verificación en 2 pasos (si no está activa):" -ForegroundColor Cyan
Write-Host "     • Busca 'Verificación en 2 pasos'" -ForegroundColor White
Write-Host "     • Sigue las instrucciones para activarla" -ForegroundColor White
Write-Host ""

Write-Host "3️⃣  Genera una contraseña de aplicación:" -ForegroundColor Cyan
Write-Host "     • Busca 'Contraseñas de aplicación'" -ForegroundColor White
Write-Host "     • Selecciona 'Correo' y 'Otra (nombre personalizado)'" -ForegroundColor White
Write-Host "     • Escribe: 'PelisApp'" -ForegroundColor White
Write-Host "     • Google generará una contraseña de 16 caracteres" -ForegroundColor White
Write-Host ""

Write-Host "4️⃣  Actualiza application.properties:" -ForegroundColor Cyan
Write-Host "     Reemplaza la línea:" -ForegroundColor White
Write-Host "     spring.mail.password=Iirlmnye322*" -ForegroundColor Red
Write-Host "     Por:" -ForegroundColor White
Write-Host "     spring.mail.password=[nueva_contraseña_de_16_caracteres]" -ForegroundColor Green
Write-Host ""

Write-Host "5️⃣  Reinicia la aplicación:" -ForegroundColor Cyan
Write-Host "     mvn spring-boot:run" -ForegroundColor Gray
Write-Host ""

Write-Host "🧪 PARA PROBAR DESPUÉS DE CONFIGURAR:" -ForegroundColor Yellow
Write-Host "     .\TEST_EMAIL.ps1" -ForegroundColor Gray
Write-Host ""

Write-Host "💡 ALTERNATIVA - VERIFICAR CONTRASEÑA ACTUAL:" -ForegroundColor Blue
Write-Host ""
Write-Host "Si la contraseña 'Iirlmnye322*' YA ES una contraseña de aplicación," -ForegroundColor White
Write-Host "entonces el problema puede ser:" -ForegroundColor White
Write-Host "• La cuenta no tiene verificación en 2 pasos activada" -ForegroundColor White
Write-Host "• Gmail está bloqueando la aplicación por políticas de seguridad" -ForegroundColor White
Write-Host "• Hay un problema de red o firewall" -ForegroundColor White
Write-Host ""

Write-Host "🎯 PRÓXIMO PASO:" -ForegroundColor Green
Write-Host "   Configura la contraseña de aplicación y prueba de nuevo" -ForegroundColor White
