# create_image_dirs.ps1
param(
    [string]$Dest = "./data/images",
    [string]$User = "",
    [string]$Group = ""
)

Write-Host "Creando carpeta base: $Dest"
New-Item -ItemType Directory -Force -Path (Join-Path $Dest 'actors') | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $Dest 'posters') | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $Dest 'backdrops') | Out-Null

if ($User -ne "") {
    try {
        # Intentar cambiar propietario (sólo si se tiene permiso)
        Write-Host "Intentando asignar propietario: $User"
        & icacls $Dest /setowner $User /T | Out-Null
    } catch {
        Write-Host "No se pudo asignar propietario: $_"
    }
}

# Establecer permisos: read/execute para todos, write para propietario
Write-Host "Estableciendo permisos básicos"
& icacls $Dest /grant "Users:(RX)" /T | Out-Null

Write-Host "Estructura creada en: $Dest"
Get-ChildItem -Path $Dest -Recurse | Format-Table -AutoSize

