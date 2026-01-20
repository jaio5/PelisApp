#!/usr/bin/env bash
set -euo pipefail

# create_image_dirs.sh
# Crea la estructura de carpetas para almacenar imágenes descargadas (actors, posters, backdrops)
# Uso: ./create_image_dirs.sh [DEST_PATH] [USER] [GROUP]
# Ejemplos:
#  ./create_image_dirs.sh ./data/images myappuser myappgroup
#  sudo ./create_image_dirs.sh /var/www/pelis/images www-data www-data

DEST=${1:-./data/images}
USER=${2:-}
GROUP=${3:-}

echo "Creando carpeta base: $DEST"
mkdir -p "$DEST/actors" "$DEST/posters" "$DEST/backdrops"

if [ -n "$USER" ]; then
  if [ -n "$GROUP" ]; then
    echo "Asignando propietario: $USER:$GROUP"
    chown -R "$USER:$GROUP" "$DEST"
  else
    echo "Asignando propietario: $USER"
    chown -R "$USER" "$DEST"
  fi
fi

# Permisos generales seguros: lectura/execution público, escritura para propietario
chmod -R 0755 "$DEST"

# Si setfacl está disponible, añade permisos rwx adicionales para el usuario indicado
if command -v setfacl >/dev/null 2>&1 && [ -n "$USER" ]; then
  echo "Añadiendo ACL para $USER"
  setfacl -R -m u:"$USER":rwx "$DEST" || true
fi

echo "Estructura creada en: $DEST"
ls -la "$DEST"

