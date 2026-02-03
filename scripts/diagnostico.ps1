#!/usr/bin/env pwsh

Write-Host ""
Write-Host "═══════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "   🔍 DIAGNÓSTICO COMPLETO DE PELISAPP" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

$projectPath = "C:\Programación\segundoJAVA\springboot\demo\PelisApp"

# 1. Verificar que existe el proyecto
Write-Host "1️⃣  Verificando proyecto..." -ForegroundColor Yellow
if (Test-Path $projectPath) {
    Write-Host "   ✅ Proyecto encontrado" -ForegroundColor Green
} else {
    Write-Host "   ❌ Proyecto NO encontrado en: $projectPath" -ForegroundColor Red
    exit 1
}
Write-Host ""

# 2. Verificar procesos Java
Write-Host "2️⃣  Verificando procesos Java..." -ForegroundColor Yellow
$javaProcesses = Get-Process -Name "java" -ErrorAction SilentlyContinue
if ($javaProcesses) {
    Write-Host "   ✅ Hay $($javaProcesses.Count) proceso(s) Java corriendo" -ForegroundColor Green
    $javaProcesses | ForEach-Object {
        Write-Host "      PID: $($_.Id) - Iniciado: $($_.StartTime)" -ForegroundColor Gray
    }
} else {
    Write-Host "   ⚠️  NO hay procesos Java corriendo" -ForegroundColor Yellow
}
Write-Host ""

# 3. Verificar servidor HTTP
Write-Host "3️⃣  Verificando servidor en puerto 8080..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -TimeoutSec 3 -ErrorAction Stop
    Write-Host "   ✅ Servidor ACTIVO en puerto 8080" -ForegroundColor Green
} catch {
    Write-Host "   ❌ Servidor NO responde en puerto 8080" -ForegroundColor Red
    Write-Host "      Error: $($_.Exception.Message)" -ForegroundColor Gray
}
Write-Host ""

# 4. Verificar películas
Write-Host "4️⃣  Verificando películas en BD..." -ForegroundColor Yellow
try {
    $count = Invoke-RestMethod -Uri "http://localhost:8080/api/admin/movie-count" -TimeoutSec 5 -ErrorAction Stop
    Write-Host "   ✅ Endpoint responde:" -ForegroundColor Green
    Write-Host $count

    # Extraer número
    if ($count -match "(\d+)") {
        $numPeliculas = [int]$Matches[1]
        if ($numPeliculas -eq 0) {
            Write-Host ""
            Write-Host "   ⚠️  HAY 0 PELÍCULAS - ForceMovieLoader NO se ejecutó o falló" -ForegroundColor Yellow
        } elseif ($numPeliculas -gt 0) {
            Write-Host ""
            Write-Host "   ✅ ¡HAY $numPeliculas PELÍCULAS!" -ForegroundColor Green
        }
    }
} catch {
    Write-Host "   ❌ No se puede consultar películas" -ForegroundColor Red
    Write-Host "      Error: $($_.Exception.Message)" -ForegroundColor Gray
}
Write-Host ""

# 5. Verificar logs si existen
Write-Host "5️⃣  Buscando logs de inicio..." -ForegroundColor Yellow
$logFile = Join-Path $projectPath "startup.log"
if (Test-Path $logFile) {
    Write-Host "   ✅ Archivo startup.log encontrado" -ForegroundColor Green
    Write-Host "   📄 Últimas líneas relevantes:" -ForegroundColor Cyan
    Get-Content $logFile | Select-String -Pattern "FORCE|ERROR|Exception|TMDB|Película" | Select-Object -Last 20
} else {
    Write-Host "   ⚠️  No hay archivo startup.log" -ForegroundColor Yellow
}
Write-Host ""

# 6. Probar TMDB
Write-Host "6️⃣  Probando conexión TMDB..." -ForegroundColor Yellow
try {
    $tmdb = Invoke-RestMethod -Uri "http://localhost:8080/api/admin/test-tmdb" -TimeoutSec 10 -ErrorAction Stop
    if ($tmdb -like "*conectado correctamente*") {
        Write-Host "   ✅ TMDB funciona" -ForegroundColor Green
    } else {
        Write-Host "   ❌ Problema con TMDB:" -ForegroundColor Red
        Write-Host $tmdb
    }
} catch {
    Write-Host "   ❌ No se puede probar TMDB" -ForegroundColor Red
}
Write-Host ""

# Resumen y recomendaciones
Write-Host "═══════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "   📋 RESUMEN Y RECOMENDACIONES" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

if (-not $javaProcesses) {
    Write-Host "❌ LA APLICACIÓN NO ESTÁ CORRIENDO" -ForegroundColor Red
    Write-Host ""
    Write-Host "🔧 SOLUCIÓN:" -ForegroundColor Yellow
    Write-Host "   1. Abre una terminal en:" -ForegroundColor White
    Write-Host "      $projectPath" -ForegroundColor Gray
    Write-Host "   2. Ejecuta:" -ForegroundColor White
    Write-Host "      mvn spring-boot:run" -ForegroundColor Gray
    Write-Host "   3. Espera 1-2 minutos" -ForegroundColor White
    Write-Host "   4. Ejecuta este script de nuevo" -ForegroundColor White
    Write-Host ""
} else {
    try {
        $testServer = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -TimeoutSec 2 -ErrorAction Stop
        Write-Host "✅ APLICACIÓN CORRIENDO CORRECTAMENTE" -ForegroundColor Green
        Write-Host ""

        # Verificar películas
        try {
            $countTest = Invoke-RestMethod -Uri "http://localhost:8080/api/admin/movie-count" -TimeoutSec 3
            if ($countTest -match "(\d+)") {
                $num = [int]$Matches[1]
                if ($num -eq 0) {
                    Write-Host "⚠️  PROBLEMA: 0 PELÍCULAS EN LA BD" -ForegroundColor Yellow
                    Write-Host ""
                    Write-Host "🔧 POSIBLES CAUSAS:" -ForegroundColor Yellow
                    Write-Host "   1. ForceMovieLoader no se ejecutó (verificar logs)" -ForegroundColor White
                    Write-Host "   2. Bearer Token de TMDB expirado" -ForegroundColor White
                    Write-Host "   3. Error de conexión con TMDB" -ForegroundColor White
                    Write-Host ""
                    Write-Host "🔧 SOLUCIÓN:" -ForegroundColor Yellow
                    Write-Host "   Ejecuta manualmente:" -ForegroundColor White
                    Write-Host "   Invoke-RestMethod 'http://localhost:8080/api/admin/load-movies' -TimeoutSec 180" -ForegroundColor Gray
                    Write-Host ""
                } else {
                    Write-Host "✅ ¡TODO FUNCIONA! HAY $num PELÍCULAS" -ForegroundColor Green
                    Write-Host ""
                    Write-Host "🎬 Accede a la aplicación:" -ForegroundColor Cyan
                    Write-Host "   http://localhost:8080/peliculas" -ForegroundColor White
                    Write-Host ""
                }
            }
        } catch {}

    } catch {
        Write-Host "⚠️  APLICACIÓN CORRIENDO PERO NO RESPONDE" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "🔧 SOLUCIÓN:" -ForegroundColor Yellow
        Write-Host "   Espera 30 segundos más y ejecuta este script de nuevo" -ForegroundColor White
        Write-Host ""
    }
}

Write-Host "═══════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""
