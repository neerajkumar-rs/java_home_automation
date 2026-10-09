@echo off
echo ============================================
echo HOME AUTOMATION - QUICK START
echo ============================================
echo.

echo Step 1: Killing existing Java processes...
taskkill /F /IM java.exe >nul 2>&1
echo Done!

echo Step 2: Starting Admin Portal on port 8081...
start "Admin Portal" cmd /k "mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081"
timeout /t 5 >nul
echo Admin Portal starting (waiting 10 seconds)...

echo Step 3: Starting User Portal on port 8080...
start "User Portal" cmd /k "mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8080"
timeout /t 5 >nul
echo User Portal starting (waiting 10 seconds)...

timeout /t 10 >nul

echo Step 4: Opening portals...
start "" "http://localhost:8081/admin-login.html"
start "" "http://localhost:8080/user-login.html"

echo ============================================
echo PORTS STARTED!
echo ============================================
echo Admin Portal: http://localhost:8081/admin-login.html
echo User Portal:  http://localhost:8080/user-login.html
echo.
echo Default Credentials:
echo   Admin: admin / admin123
echo   User:  user  / user123
echo ============================================
echo NOTE: Keep both terminal windows open!
echo ============================================