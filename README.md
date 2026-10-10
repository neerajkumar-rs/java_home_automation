# Home Automation System

A secure home-automation app with a Spring Boot backend, SQLite database, and a dark-neon developer-dashboard frontend (plain HTML/CSS/vanilla JS).

## Features
- JWT auth with role-based access (admin vs user)
- Per-user data isolation — a user only ever sees their own devices
- Add / toggle / delete devices, recent activity log, automation rules
- Responsive dark dashboard UI (stats, room filters, device tiles, activity terminal)

## Tech
Spring Boot 3.5.5 · Java 17+ · SQLite · Maven · vanilla JS (no frameworks)

## Quick Start
```bash
# Build + run on port 8081 (script sets JWT_SECRET automatically)
scripts\start-app-8081.bat
```
Then open **http://localhost:8081** → you'll be sent to the login page.

Or run manually:
```bash
mvn clean package -DskipTests
java -jar target/home-automation-0.0.1-SNAPSHOT.jar --server.port=8081 --spring.profiles.active=local
```
> JWT: the app needs a secret of 32+ chars. The start script reads/creates a git-ignored `.env` with `JWT_SECRET`. A `local` profile also supplies a dev-only secret via `application-local.properties` (also git-ignored). Never commit a real secret.

## Using the app
- **Register** an account at `/register.html`, then **sign in** at `/login.html`.
- Non-admin → `dashboard.html` (your devices). Admin → `admin.html` (users + all devices).
- To seed an admin on first run, set `APP_ADMIN_EMAIL` and `APP_ADMIN_PASSWORD` env vars before starting (only applies when the users table is empty).

## Project structure
```
src/main/java/com/example/homeautomation/
├── user/        # auth, JWT, users
├── device/      # devices, logs
├── security/    # secure entities, repos, services
├── admin/       # admin endpoints
└── config/      # security/web config
src/main/resources/static/
├── login.html / register.html / index.html
├── dashboard.html / admin.html      # user + admin UIs
├── theme.css                        # shared dark-neon design system
└── js/ (auth.js, ui.js, dashboard.js, admin.js)
```

## Troubleshooting
- **Port in use:** `scripts\stop-app.bat`
- **Build fails:** `mvn clean package -DskipTests`
- **401 on pages/css:** hard-refresh; static assets are permitted in `SecurityConfig`.
