# Diagnóstico de configuración de email para PelisApp
Write-Host "🔍 DIAGNÓSTICO DE CONFIGURACIÓN DE EMAIL" -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan
Write-Host ""

# Verificar archivo de propiedades
$propsFile = "src\main\resources\application.properties"
if (Test-Path $propsFile) {
    Write-Host "✅ Archivo de propiedades encontrado: $propsFile" -ForegroundColor Green

    # Buscar configuraciones de email
    Write-Host ""
    Write-Host "📧 CONFIGURACIONES DE EMAIL:" -ForegroundColor Yellow
    Write-Host "-----------------------------" -ForegroundColor Yellow

    $content = Get-Content $propsFile
    foreach ($line in $content) {
        if ($line -match "^(app\.email\.|spring\.mail\.)" -and $line -notmatch "password") {
            Write-Host "  $line" -ForegroundColor White
        }
        elseif ($line -match "spring\.mail\.password") {
            if ($line -match ":\$\{") {
                Write-Host "  $line" -ForegroundColor White
            } else {
                Write-Host "  spring.mail.password=****** [CONFIGURADA]" -ForegroundColor Green
            }
        }
    }
} else {
    Write-Host "❌ Archivo de propiedades NO encontrado" -ForegroundColor Red
}

Write-Host ""
Write-Host "⚠️  IMPORTANTE - CONFIGURACIÓN DE GMAIL:" -ForegroundColor Yellow
Write-Host "=========================================" -ForegroundColor Yellow
Write-Host ""
Write-Host "Para usar Gmail necesitas:" -ForegroundColor White
Write-Host "1. Activar verificación en 2 pasos en tu cuenta de Gmail" -ForegroundColor Cyan
Write-Host "2. Generar una 'Contraseña de aplicación' específica" -ForegroundColor Cyan
Write-Host "3. Usar esa contraseña de aplicación en lugar de tu contraseña normal" -ForegroundColor Cyan
Write-Host ""
Write-Host "📋 PASOS PARA GENERAR CONTRASEÑA DE APLICACIÓN:" -ForegroundColor Green
Write-Host "1. Ve a https://myaccount.google.com/security" -ForegroundColor White
Write-Host "2. En 'Verificación en 2 pasos', haz clic en 'Contraseñas de aplicaciones'" -ForegroundColor White
Write-Host "3. Selecciona 'Correo' y 'Otro (personalizado)'" -ForegroundColor White
Write-Host "4. Escribe 'PelisApp' como nombre" -ForegroundColor White
Write-Host "5. Copia la contraseña de 16 caracteres generada" -ForegroundColor White
Write-Host "6. Úsala en spring.mail.password en lugar de tu contraseña normal" -ForegroundColor White
Write-Host ""

# Verificar si hay servicios de email compilados
$emailServiceFiles = @(
    "src\main\java\alicanteweb\pelisapp\service\RealEmailService.java",
    "src\main\java\alicanteweb\pelisapp\service\MockEmailService.java",
    "src\main\java\alicanteweb\pelisapp\service\EmailService.java"
)

Write-Host "📁 SERVICIOS DE EMAIL ENCONTRADOS:" -ForegroundColor Yellow
Write-Host "-----------------------------------" -ForegroundColor Yellow
foreach ($file in $emailServiceFiles) {
    if (Test-Path $file) {
        Write-Host "  ✅ $file" -ForegroundColor Green
    } else {
        Write-Host "  ❌ $file" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "🚀 PARA PROBAR EL SISTEMA:" -ForegroundColor Green
Write-Host "1. Ejecuta: mvn spring-boot:run" -ForegroundColor Cyan
Write-Host "2. Abre: http://localhost:8080/register" -ForegroundColor Cyan
Write-Host "3. Registra un usuario de prueba" -ForegroundColor Cyan
Write-Host "4. Verifica los logs en la consola" -ForegroundColor Cyan
Write-Host ""
Write-Host "🔗 ENDPOINTS DE PRUEBA:" -ForegroundColor Green
Write-Host "- http://localhost:8080/api/test/email-config (diagnóstico)" -ForegroundColor Cyan
Write-Host "- http://localhost:8080/api/test/send-email?email=tu@email.com (envío de prueba)" -ForegroundColor Cyan
Write-Host ""
