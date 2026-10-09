@echo off
echo ============================================
echo HOME AUTOMATION - DUAL PORTAL SYSTEM
echo ============================================
echo Admin Portal: http://localhost:8081
echo User Portal:  http://localhost:8080
echo ============================================
echo.

echo Step 1: Stopping any existing applications...
taskkill /F /IM java.exe >nul 2>&1
timeout /t 2 /nobreak >nul

echo Step 2: Starting Admin Portal (port 8081)...
start "Admin Portal" cmd /c "mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081"
echo Admin Portal starting on port 8081...
timeout /t 8 /nobreak >nul

echo Step 3: Starting User Portal (port 8080)...
start "User Portal" cmd /c "mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8080"
echo User Portal starting on port 8080...
timeout /t 8 /nobreak >nul

echo Step 4: Opening portals in browser...
start "" "http://localhost:8081/admin-login.html"
start "" "http://localhost:8080/user-login.html"

echo ============================================
echo PORTALS ARE RUNNING!
echo ============================================
echo Admin Portal: http://localhost:8081/admin-login.html
echo User Portal:  http://localhost:8080/user-login.html
echo.
echo Default Credentials:
echo   Admin: admin / admin123
echo   User:  user  / user123
echo ============================================
echo.
echo IMPORTANT: Each portal runs on separate port
echo Admin features available only on port 8081
echo.
pause