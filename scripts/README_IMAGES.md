# Scripts para crear carpetas de imágenes

Incluye scripts para crear la estructura de carpetas donde se almacenarán las imágenes descargadas (actores, posters, backdrops).

Archivos:
- create_image_dirs.sh: script para Linux/macOS
- create_image_dirs.ps1: script para Windows PowerShell

Uso (Linux/macOS):

```bash
cd scripts
./create_image_dirs.sh ./data/images
```

Opcionalmente pasar usuario y grupo para chown:

```bash
sudo ./create_image_dirs.sh /var/www/pelis/images www-data www-data
```

Uso (Windows PowerShell):

```powershell
cd scripts
.\create_image_dirs.ps1 -Dest .\data\images -User "MiUsuario"
```

Notas:
- Asegúrate de ejecutar con permisos suficientes para crear carpetas en la ruta deseada.
- En producción recomendamos usar almacenamiento compartido o un servicio de objetos (S3) y no el disco local del servidor de aplicaciones.

