PowerShell commands to run the frontend (development) and build (production).

Development (with proxy to backend running on http://localhost:8080):

# from project root
cd frontend;
# install deps if not yet installed
npm install;
# run dev server with proxy config (works without global Angular CLI)
# Option 1 (recommended on Windows): run the helper script
powershell -ExecutionPolicy Bypass -File .\start-frontend.ps1 -Port 4200

# Option 2 (manual): use npx
npx ng serve --host 0.0.0.0 --port 4200 --proxy-config proxy.conf.json

Production build (output into frontend/dist):

cd frontend;
npm ci
npx ng build --configuration production

Serve production build from Spring Boot (option A - static):
- Copy frontend/dist/<app-name> contents into src/main/resources/static or configure Maven to run the build and copy artifacts.

Notes:
- Ensure NodeJS (>=18) and npm are installed. The provided PowerShell helper checks node and npm and will install dependencies when necessary.
- The proxy file forwards /api and /images to backend so you can use relative paths in frontend code.
