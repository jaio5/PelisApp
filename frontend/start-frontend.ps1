# PowerShell helper para arrancar el frontend Angular en Windows sin Angular CLI global
# Comprueba Node/NPM, instala dependencias si hace falta y ejecuta el dev server usando npx.

param(
    [int]$Port = 4200
)

function Write-ErrAndExit($msg) {
    Write-Host "ERROR: $msg" -ForegroundColor Red
    exit 1
}

# Comprobar node
try {
    $nodeVersion = (& node -v) -join ''
} catch {
    Write-ErrAndExit "Node.js no está instalado o no está en PATH. Descárgalo e instálalo desde https://nodejs.org/"
}

# Comprobar npm
try {
    $npmVersion = (& npm -v) -join ''
} catch {
    Write-ErrAndExit "npm no está disponible. Instala Node.js (viene con npm)."
}

Write-Host "Node: $nodeVersion | npm: $npmVersion"

# Ir a la carpeta frontend
Push-Location -Path (Join-Path $PSScriptRoot '.')

# Instalar dependencias si no existe node_modules
if (-not (Test-Path "node_modules")) {
    Write-Host "node_modules no encontrada. Ejecutando npm install..." -ForegroundColor Yellow
    & npm install
    if ($LASTEXITCODE -ne 0) { Write-ErrAndExit "npm install ha fallado." }
}

# Ejecutar el servidor de desarrollo con proxy
Write-Host "Iniciando Angular dev server en http://localhost:$Port (proxy a backend en /api y /images)" -ForegroundColor Green
& npx ng serve --host 0.0.0.0 --port $Port --proxy-config proxy.conf.json

# Restaurar carpeta
Pop-Location
