# Script para configurar Ollama para moderación de contenido en PelisApp
Write-Host "🦙 CONFIGURACIÓN DE OLLAMA PARA PELISAPP" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""

# Verificar si Ollama está instalado
$ollamaInstalled = $false
try {
    $ollamaVersion = ollama --version 2>$null
    if ($LASTEXITCODE -eq 0) {
        $ollamaInstalled = $true
        Write-Host "✅ Ollama ya está instalado: $ollamaVersion" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ Ollama no está instalado" -ForegroundColor Red
}

if (-not $ollamaInstalled) {
    Write-Host "📦 INSTALANDO OLLAMA..." -ForegroundColor Yellow
    Write-Host "1. Ve a: https://ollama.ai/download" -ForegroundColor Cyan
    Write-Host "2. Descarga Ollama para Windows" -ForegroundColor Cyan
    Write-Host "3. Instálalo y vuelve a ejecutar este script" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "🔗 Enlace directo: https://github.com/ollama/ollama/releases/latest" -ForegroundColor Green
    Write-Host ""

    # Intentar descargar automáticamente (opcional)
    $downloadOption = Read-Host "¿Quieres que intente descargar Ollama automáticamente? (y/n)"
    if ($downloadOption -eq "y" -or $downloadOption -eq "Y") {
        try {
            Write-Host "⬇️ Descargando Ollama..." -ForegroundColor Yellow
            $url = "https://github.com/ollama/ollama/releases/download/v0.1.26/ollama-windows-amd64.zip"
            $output = "$env:TEMP\ollama-windows.zip"
            Invoke-WebRequest -Uri $url -OutFile $output

            Write-Host "✅ Descarga completada: $output" -ForegroundColor Green
            Write-Host "📝 Extrae el archivo y ejecuta ollama.exe" -ForegroundColor Yellow
        } catch {
            Write-Host "❌ Error descargando: $_" -ForegroundColor Red
            Write-Host "📝 Por favor, descarga manualmente desde https://ollama.ai/download" -ForegroundColor Yellow
        }
    }
    return
}

Write-Host "🧠 CONFIGURANDO MODELOS PARA MODERACIÓN..." -ForegroundColor Yellow
Write-Host "-------------------------------------------" -ForegroundColor Yellow

# Verificar si el servicio Ollama está ejecutándose
Write-Host "🔍 Verificando servicio Ollama..." -ForegroundColor Cyan
try {
    $response = Invoke-RestMethod -Uri "http://localhost:11434/api/version" -Method GET -ErrorAction SilentlyContinue
    Write-Host "✅ Ollama está ejecutándose en localhost:11434" -ForegroundColor Green
} catch {
    Write-Host "⚠️ Ollama no está ejecutándose. Iniciándolo..." -ForegroundColor Yellow
    Start-Process "ollama" -ArgumentList "serve" -WindowStyle Hidden
    Start-Sleep -Seconds 5

    try {
        $response = Invoke-RestMethod -Uri "http://localhost:11434/api/version" -Method GET -ErrorAction SilentlyContinue
        Write-Host "✅ Ollama iniciado correctamente" -ForegroundColor Green
    } catch {
        Write-Host "❌ No se pudo iniciar Ollama automáticamente" -ForegroundColor Red
        Write-Host "📝 Ejecuta manualmente: ollama serve" -ForegroundColor Yellow
        return
    }
}

# Listar modelos disponibles
Write-Host ""
Write-Host "📋 MODELOS DISPONIBLES:" -ForegroundColor Yellow
try {
    ollama list
} catch {
    Write-Host "❌ Error listando modelos" -ForegroundColor Red
}

Write-Host ""
Write-Host "⬇️ DESCARGANDO MODELOS RECOMENDADOS..." -ForegroundColor Yellow

# Modelos recomendados para moderación
$modelos = @(
    @{name="llama3"; description="Modelo principal para moderación (4GB)"},
    @{name="phi3"; description="Modelo ligero alternativo (2.3GB)"}
)

foreach ($modelo in $modelos) {
    Write-Host ""
    Write-Host "📦 Descargando $($modelo.name) - $($modelo.description)" -ForegroundColor Cyan

    try {
        ollama pull $modelo.name
        Write-Host "✅ $($modelo.name) descargado exitosamente" -ForegroundColor Green
    } catch {
        Write-Host "❌ Error descargando $($modelo.name): $_" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "🧪 PROBANDO MODERACIÓN..." -ForegroundColor Yellow
Write-Host "-------------------------" -ForegroundColor Yellow

# Test de moderación
$testTexts = @(
    "Esta película es excelente, la recomiendo mucho",
    "Esta mierda de película es una puta basura",
    "No me gustó nada, muy aburrida"
)

foreach ($text in $testTexts) {
    Write-Host ""
    Write-Host "🔍 Probando: '$text'" -ForegroundColor Cyan

    $prompt = @"
Eres un moderador de contenido experto. Analiza el siguiente texto de una reseña de película y determina si contiene:

1. Lenguaje malsonante u ofensivo
2. Insultos o ataques personales
3. Discurso de odio
4. Contenido inapropiado

Texto a analizar: "$text"

Responde únicamente en formato JSON con esta estructura:
{
  "toxicity_score": [número entre 0.0 y 1.0, donde 1.0 es más tóxico],
  "is_toxic": [true/false],
  "reason": "[explicación breve de por qué es tóxico o limpio]",
  "detected_issues": ["lista", "de", "problemas", "detectados"]
}

Respuesta:
"@

    try {
        $result = ollama generate llama3 $prompt --format json
        Write-Host "📊 Resultado: $result" -ForegroundColor White
    } catch {
        Write-Host "❌ Error en test: $_" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "🎯 CONFIGURACIÓN DE PELISAPP" -ForegroundColor Green
Write-Host "============================" -ForegroundColor Green
Write-Host ""
Write-Host "✅ Ollama configurado correctamente" -ForegroundColor Green
Write-Host "📝 Configuración actual en application.properties:" -ForegroundColor Cyan
Write-Host ""
Write-Host "app.moderation.enabled=true" -ForegroundColor White
Write-Host "app.moderation.ollama.url=http://localhost:11434" -ForegroundColor White
Write-Host "app.moderation.ollama.model=llama3" -ForegroundColor White
Write-Host "app.moderation.toxicity.threshold=0.7" -ForegroundColor White
Write-Host "app.moderation.fallback.enabled=true" -ForegroundColor White
Write-Host ""
Write-Host "🚀 PASOS SIGUIENTES:" -ForegroundColor Yellow
Write-Host "1. Ejecuta tu aplicación Spring Boot" -ForegroundColor Cyan
Write-Host "2. Prueba crear una reseña con contenido tóxico" -ForegroundColor Cyan
Write-Host "3. Verifica los logs de moderación" -ForegroundColor Cyan
Write-Host "4. Usa los endpoints de admin para monitorear:" -ForegroundColor Cyan
Write-Host "   - GET /admin/moderation/stats" -ForegroundColor White
Write-Host "   - GET /admin/moderation/ollama-status" -ForegroundColor White
Write-Host "   - POST /admin/moderation/test?text=tu_texto" -ForegroundColor White
Write-Host ""
Write-Host "🎉 ¡CONFIGURACIÓN COMPLETADA!" -ForegroundColor Green
