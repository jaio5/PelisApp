# Script PowerShell para eliminar usuarios de PelisApp via MySQL
Write-Host "🗑️ ELIMINAR USUARIOS DE PELISAPP" -ForegroundColor Red
Write-Host "=================================" -ForegroundColor Red
Write-Host ""

# Configuración de la base de datos
$dbHost = "localhost"
$dbPort = "3306"
$dbName = "pelisapp"
$dbUser = "root"
$dbPassword = "Iirne322*"

# Verificar que mysql está disponible
try {
    $mysqlVersion = mysql --version 2>$null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ MySQL encontrado: $mysqlVersion" -ForegroundColor Green
    } else {
        throw "MySQL no encontrado"
    }
} catch {
    Write-Host "❌ ERROR: MySQL no está disponible en el PATH" -ForegroundColor Red
    Write-Host "💡 Asegúrate de tener MySQL instalado y en el PATH del sistema" -ForegroundColor Yellow
    Write-Host "   O ejecuta el script SQL manualmente en tu cliente de MySQL" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "📁 Script SQL ubicado en: .\scripts\delete-all-users.sql" -ForegroundColor Cyan
    return
}

# Confirmar la acción
Write-Host "⚠️ ADVERTENCIA: Esta acción eliminará TODOS los usuarios de la base de datos" -ForegroundColor Yellow
Write-Host "   - Base de datos: $dbName" -ForegroundColor White
Write-Host "   - Host: $dbHost" -ForegroundColor White
Write-Host "   - Esta acción es IRREVERSIBLE" -ForegroundColor Red
Write-Host ""

$confirmation = Read-Host "¿Estás seguro de que quieres continuar? (escribe 'SI_BORRAR' para confirmar)"

if ($confirmation -ne "SI_BORRAR") {
    Write-Host "❌ Operación cancelada" -ForegroundColor Yellow
    return
}

Write-Host ""
Write-Host "🔄 Ejecutando eliminación de usuarios..." -ForegroundColor Yellow

try {
    # Ejecutar el script SQL
    $sqlScript = Get-Content ".\scripts\delete-all-users.sql" -Raw

    # Crear archivo temporal para la salida
    $tempOutput = [System.IO.Path]::GetTempFileName()

    # Ejecutar MySQL con el script
    $mysqlCommand = "mysql -h $dbHost -P $dbPort -u $dbUser -p$dbPassword -D $dbName"
    Write-Host "📤 Conectando a MySQL..." -ForegroundColor Cyan

    # Usar Here-String para enviar el SQL
    $sqlScript | & mysql -h $dbHost -P $dbPort -u $dbUser "-p$dbPassword" -D $dbName 2>&1 | Tee-Object -FilePath $tempOutput

    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "✅ USUARIOS ELIMINADOS EXITOSAMENTE" -ForegroundColor Green
        Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Green

        # Mostrar el contenido del archivo de salida
        $output = Get-Content $tempOutput
        foreach ($line in $output) {
            if ($line -match "USUARIOS ANTES DEL BORRADO|USUARIOS DESPUÉS DEL BORRADO|OPERACIÓN COMPLETADA") {
                Write-Host $line -ForegroundColor Yellow
            } else {
                Write-Host $line -ForegroundColor White
            }
        }

        Write-Host ""
        Write-Host "🎯 SIGUIENTE PASO:" -ForegroundColor Green
        Write-Host "   Reinicia tu aplicación Spring Boot para que los cambios surtan efecto" -ForegroundColor Cyan

    } else {
        Write-Host "❌ Error ejecutando el script SQL" -ForegroundColor Red
        Write-Host "📄 Revisa la salida arriba para más detalles" -ForegroundColor Yellow
    }

    # Limpiar archivo temporal
    Remove-Item $tempOutput -ErrorAction SilentlyContinue

} catch {
    Write-Host "❌ ERROR CRÍTICO: $_" -ForegroundColor Red
    Write-Host ""
    Write-Host "🔧 SOLUCIÓN ALTERNATIVA:" -ForegroundColor Yellow
    Write-Host "1. Abre tu cliente de MySQL (MySQL Workbench, phpMyAdmin, etc.)" -ForegroundColor Cyan
    Write-Host "2. Conecta a la base de datos '$dbName'" -ForegroundColor Cyan
    Write-Host "3. Ejecuta manualmente el contenido de: .\scripts\delete-all-users.sql" -ForegroundColor Cyan
}

Write-Host ""
Write-Host "📝 LOGS DE LA OPERACIÓN:" -ForegroundColor Blue
Write-Host "   Timestamp: $(Get-Date)" -ForegroundColor White
Write-Host "   Base de datos: $dbName" -ForegroundColor White
Write-Host "   Script: .\scripts\delete-all-users.sql" -ForegroundColor White
