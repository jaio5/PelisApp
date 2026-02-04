# Test de Email para PelisApp
Write-Host "🧪 PROBANDO SISTEMA DE EMAIL DE PELISAPP" -ForegroundColor Cyan
Write-Host "=" * 50 -ForegroundColor Cyan

# Verificar que la aplicación está corriendo
Write-Host "`n🔍 Verificando aplicación..."
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/" -Method GET -TimeoutSec 10 -UseBasicParsing
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ Aplicación funcionando en puerto 8080" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ Error: La aplicación no responde en puerto 8080" -ForegroundColor Red
    Write-Host "   Asegúrate de que esté ejecutándose con: mvn spring-boot:run" -ForegroundColor Yellow
    exit
}

Write-Host "`n📧 INSTRUCCIONES PARA PROBAR EL EMAIL:"
Write-Host "1. Abre tu navegador y ve a: http://localhost:8080/register"
Write-Host "2. Completa el formulario de registro con:"
Write-Host "   - Username: testuser123"
Write-Host "   - Email: javierbarcelo2106@gmail.com"
Write-Host "   - Contraseña: TestPass123*"
Write-Host "   - Confirmar contraseña: TestPass123*"
Write-Host "3. Haz clic en 'Registrarse'"

Write-Host "`n🔍 VERIFICAR RESULTADO:"
Write-Host "Si el email está configurado correctamente:"
Write-Host "✅ Aparecerá mensaje: 'Usuario registrado exitosamente'"
Write-Host "✅ Recibirás un email en javierbarcelo2106@gmail.com"
Write-Host "✅ El email tendrá un enlace de confirmación"

Write-Host "`nSi hay problemas de autenticación:"
Write-Host "❌ Verás error: 'Error enviando email de confirmación'"
Write-Host "❌ En los logs aparecerá: 'Authentication failed'"
Write-Host "💡 SOLUCIÓN: Necesitas una contraseña de aplicación de Gmail"

Write-Host "`n🔧 PANEL DE ADMINISTRACIÓN:"
Write-Host "También puedes probar en:"
Write-Host "http://localhost:8080/admin/email-config"
Write-Host "(Necesitas estar logueado como admin)"

Write-Host "`n📋 CREDENCIALES DE ADMIN:"
Write-Host "Username: admin"
Write-Host "Password: admin123"

Write-Host "`n💡 SI NECESITAS CONTRASEÑA DE APLICACIÓN:"
Write-Host "Ejecuta: .\CONFIGURAR_GMAIL.ps1"
Write-Host "Para ver la guía completa paso a paso"

Write-Host "`n✨ PRÓXIMO PASO:"
Write-Host "¡Prueba el registro ahora y verifica si llega el email!"
Write-Host "=" * 50 -ForegroundColor Cyan
