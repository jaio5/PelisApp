# 📧 GUÍA COMPLETA: CONFIGURAR GMAIL PARA PELISAPP
# ===================================================

Write-Host "🎬 PELISAPP - CONFIGURACIÓN DE GMAIL PASO A PASO" -ForegroundColor Yellow
Write-Host "=" * 60 -ForegroundColor Yellow

Write-Host "`n🔐 PASO 1: ACTIVAR VERIFICACIÓN EN 2 PASOS" -ForegroundColor Cyan
Write-Host "1. Ve a: https://myaccount.google.com/security"
Write-Host "2. En la sección 'Iniciar sesión en Google'"
Write-Host "3. Busca 'Verificación en 2 pasos'"
Write-Host "4. Haz clic en 'Verificación en 2 pasos'"
Write-Host "5. Sigue las instrucciones para activarla"
Write-Host "   (necesitarás tu teléfono para verificación)"

Write-Host "`n🔑 PASO 2: GENERAR CONTRASEÑA DE APLICACIÓN" -ForegroundColor Cyan
Write-Host "1. Una vez activada la verificación en 2 pasos"
Write-Host "2. Ve de nuevo a: https://myaccount.google.com/security"
Write-Host "3. Busca 'Contraseñas de aplicaciones'"
Write-Host "4. Haz clic en 'Contraseñas de aplicaciones'"
Write-Host "5. Selecciona 'Correo' como aplicación"
Write-Host "6. Selecciona 'Otro (nombre personalizado)'"
Write-Host "7. Escribe 'PelisApp' como nombre"
Write-Host "8. Haz clic en 'Generar'"
Write-Host "9. COPIA la contraseña de 16 caracteres que aparece"
Write-Host "   ¡Esta contraseña solo aparece UNA VEZ!"

Write-Host "`n⚙️ PASO 3: ACTUALIZAR CONFIGURATION" -ForegroundColor Cyan
Write-Host "1. Abre el archivo: src/main/resources/application.properties"
Write-Host "2. Busca la línea: spring.mail.password=Iirlmnye322*"
Write-Host "3. Reemplaza 'Iirlmnye322*' por tu nueva contraseña de aplicación"
Write-Host "4. Guarda el archivo"

Write-Host "`n🚀 PASO 4: REINICIAR Y PROBAR" -ForegroundColor Cyan
Write-Host "1. Detén la aplicación (Ctrl+C en la consola)"
Write-Host "2. Reinicia: mvn spring-boot:run"
Write-Host "3. Ve a: http://localhost:8080/admin/email-config"
Write-Host "4. Haz clic en 'Probar Conexión SMTP'"
Write-Host "5. Si funciona, haz clic en 'Enviar Email de Prueba'"

Write-Host "`n✨ VERIFICACIÓN FINAL:" -ForegroundColor Green
Write-Host "1. Ve a http://localhost:8080/register"
Write-Host "2. Crea una cuenta de prueba"
Write-Host "3. Verifica que llegue el email de confirmación"
Write-Host "4. Haz clic en el enlace del email para confirmar"

Write-Host "`n💡 CONSEJOS IMPORTANTES:" -ForegroundColor Yellow
Write-Host "• La contraseña de aplicación tiene exactamente 16 caracteres"
Write-Host "• NO incluye espacios ni caracteres especiales"
Write-Host "• Es diferente a tu contraseña normal de Gmail"
Write-Host "• Se puede generar una nueva si se pierde"
Write-Host "• Revisa la carpeta de spam si no llega el email"

Write-Host "`n🔍 TROUBLESHOOTING:" -ForegroundColor Red
Write-Host "Si aún no funciona:"
Write-Host "1. Verifica que la verificación en 2 pasos esté ACTIVA"
Write-Host "2. Asegúrate de copiar la contraseña de aplicación correctamente"
Write-Host "3. Reinicia completamente la aplicación"
Write-Host "4. Verifica los logs en la consola para errores específicos"
Write-Host "5. Prueba con otro email de destino"

Write-Host "`n📞 CONTACTOS ÚTILES:" -ForegroundColor Cyan
Write-Host "• API Test: http://localhost:8080/api/email-test/test-connection"
Write-Host "• Admin Panel: http://localhost:8080/admin/email-config"
Write-Host "• Logs detallados: Consola del servidor"

Write-Host "`n✅ UNA VEZ CONFIGURADO:" -ForegroundColor Green
Write-Host "• Los usuarios podrán registrarse normalmente"
Write-Host "• Recibirán emails de confirmación automáticamente"
Write-Host "• Podrán confirmar sus cuentas haciendo clic en el enlace"
Write-Host "• El sistema funcionará de forma completamente automática"

Write-Host "`n" + "=" * 60 -ForegroundColor Yellow
Write-Host "🎬 ¡PELISAPP - SISTEMA DE EMAIL LISTO!" -ForegroundColor Yellow
