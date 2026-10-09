@echo off
echo ============================================
echo HOME AUTOMATION - CLEAN RESTART
echo ============================================
echo.

echo Step 1: Stopping all existing applications...
call scripts\stop-app.bat

echo.
echo Step 2: Waiting for system to settle...
timeout /t 2 /nobreak >nul

echo.
echo Step 3: Starting Admin Portal (port 8081)...
start "Admin Portal" cmd /c "cd /d %~dp0.. && mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081"
echo Admin Portal starting... Please wait 10 seconds.
timeout /t 10 /nobreak >nul

echo Step 4: Starting User Portal (port 8080)...
start "User Portal" cmd /c "cd /d %~dp0.. && mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8080"
echo User Portal starting... Please wait 10 seconds.
timeout /t 10 /nobreak >nul

echo.
echo Step 5: Opening portals in browser...
start "" "http://localhost:8081/admin-login.html"
start "" "http://localhost:8080/user-login.html"

echo ============================================
echo RESTART COMPLETE!
echo ============================================
echo Port Status:
echo   Admin Portal: http://localhost:8081
echo   User Portal:  http://localhost:8080
echo.
echo Checking portal availability...
curl -s -o nul -w "Admin Portal: %%{http_code}\n" "http://localhost:8081/admin-login.html" 2>nul
curl -s -o nul -w "User Portal:  %%{http_code}\n" "http://localhost:8080/user-login.html" 2>nul
echo.
echo Default Credentials:
echo   Admin: admin / admin123
echo   User:  user  / user123
echo ============================================
pause