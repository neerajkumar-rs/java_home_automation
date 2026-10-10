# 🏠 Home Automation System

A secure, multi-user smart-home control app built with **Spring Boot**, **SQLite** and a dark-neon **vanilla JS** dashboard. Every user sees and controls only their own devices; admins manage everything.

![Java](https://img.shields.io/badge/Java-21-orange) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-brightgreen) ![SQLite](https://img.shields.io/badge/DB-SQLite-blue) ![License](https://img.shields.io/badge/License-MIT-yellow)

<!-- Add screenshots here: docs/images/login.png, dashboard.png, admin.png -->

## ✨ Features

- **JWT authentication** with role-based access (`USER` / `ADMIN`)
- **Per-user data isolation**: users can only access their own devices (see [`docs/DataIsolationStrategy.md`](docs/DataIsolationStrategy.md))
- **Device management**: add, update, delete, toggle on/off, change colour, share with other users
- **Activity logs** for every device state change
- **Automation rules** (triggers and actions)
- **Admin panel**: manage users (activate / deactivate / edit / delete), view all devices and system stats
- **Responsive UI**: stats, room filters, device tiles, activity terminal

## 🧰 Tech Stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot 3.5.5, Spring Web, Spring Security, Spring Data JPA, Validation |
| Auth | JWT (jjwt 0.11.5), 60-minute token expiry |
| Database | SQLite (`sqlite-jdbc`, Hibernate community dialect) |
| Frontend | HTML, CSS, vanilla JavaScript (no frameworks) |
| Build | Maven, Java 21 |

## 🏗️ Architecture

```
Browser (static HTML/JS)
        │  Authorization: Bearer <JWT>
        ▼
 JwtAuthenticationFilter ─► SecurityConfig (role rules)
        ▼
 Controllers ─► Services ─► Repositories ─► SQLite (./data/home_automation.db)
```

## 🚀 Quick Start

**Prerequisites:** JDK 21, Maven 3.8+ (the helper scripts are Windows `.bat`; on Linux/macOS use the manual commands).

```bash
git clone https://github.com/neerajkumar-rs/java_home_automation.git
cd java_home_automation
```

**Option A: Windows script** (sets `JWT_SECRET` automatically, runs on port 8081)

```bat
scripts\start-app-8081.bat
```

**Option B: Manual**

```bash
# Windows (cmd):   set JWT_SECRET=<random-32+-char-string>
# Linux/macOS:     export JWT_SECRET=<random-32+-char-string>

mvn clean package -DskipTests
java -jar target/home-automation-0.0.1-SNAPSHOT.jar --server.port=8081
```

Open **http://localhost:8081** → you'll land on the login page.

## ⚙️ Configuration

| Variable | Required | Description |
|---|---|---|
| `JWT_SECRET` | ✅ | Signing key, 32+ characters. The app will not start without it. |
| `APP_ADMIN_EMAIL` / `APP_ADMIN_PASSWORD` | Optional | Creates the first admin on startup (only if the users table is empty). |
| `PORT` | Optional | Server port (default `8080`). |

Profile files in `src/main/resources/`: `application-admin`, `application-user`, `application-prod`. Never commit real secrets (`.env` is git-ignored).

## 📖 Usage

1. **Register** at `/register.html`, then **log in** at `/login.html`.
2. Normal users go to `dashboard.html`, admins go to `admin.html`.
3. Add devices, toggle them, change colours, check the activity log.

## 🔌 API Overview

All endpoints need `Authorization: Bearer <token>` except register, login and validate.

| Area | Base path | Examples |
|---|---|---|
| Auth | `/api/auth` | `POST /register`, `POST /login`, `GET /validate`, `GET /me` |
| Devices | `/api/devices` | list owned / shared, `POST /{id}/state`, `POST /{id}/color`, `GET /{id}/logs` |
| Secure devices | `/api/v1/secure/devices` | ownership-checked device operations |
| Automation | `/api/automation-rules` | CRUD, `POST /{id}/activate`, `POST /{id}/deactivate` |
| Admin 🔒 `ADMIN` | `/api/admin` | `GET /users`, `PUT /users/{id}`, `POST /users/{id}/activate`, `GET /devices`, `GET /system/stats` |

Quick test:

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"you@example.com","password":"yourPassword"}'
```

## 🗄️ Database

SQLite file is created at `./data/home_automation.db` on first run (tables are auto-managed by Hibernate). Main tables: `users`, `user_roles`, `devices`, `device_shares`, `device_state_logs`, `automation_rules`, `user_logs`.

## 📁 Project Structure

```
├── src/main/java/com/example/homeautomation/
│   ├── user/        # auth, JWT, users
│   ├── device/      # devices and logs
│   ├── security/    # ownership-checked entities, repos, services
│   ├── automation/  # automation rules
│   ├── admin/       # admin endpoints
│   └── config/      # security, JWT filter, error handlers
├── src/main/resources/
│   ├── static/      # HTML, CSS, JS frontend
│   └── *.properties / *.sql
├── scripts/         # Windows start/stop/test helpers
├── docs/            # design docs
└── pom.xml
```

## 🧪 Testing

Manual test scripts and pages live in `scripts/manual-tests/` (e.g. `test_auth.py`, `test_system.bat`). Start the app first, then run them.

## 🛠️ Troubleshooting

| Problem | Fix |
|---|---|
| App won't start, JWT error | Set `JWT_SECRET` (32+ chars) |
| Port already in use | `scripts\stop-app.bat` or change `--server.port` |
| Build fails | Check `java -version` is 21, then `mvn clean package -DskipTests` |
| 401 on pages or CSS | Hard-refresh (Ctrl+Shift+R) and log in again |

## 🗺️ Roadmap

- [ ] Real device/MQTT integration
- [ ] Unit and integration tests
- [ ] Docker support
- [ ] Cross-platform start scripts

## 🤝 Contributing

Fork → create a branch → commit → open a Pull Request.

## 📄 License

MIT. See [LICENSE](LICENSE). © 2026 Neeraj Kumar
