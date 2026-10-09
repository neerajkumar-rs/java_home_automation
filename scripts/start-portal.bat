@echo off
setlocal

echo ============================================
echo HOME AUTOMATION PORTAL STARTUP
echo ============================================
echo.
echo Choose portal to start:
echo 1. Admin Portal (port 8081)
echo 2. User Portal (port 8080) 
echo 3. Both Portals
echo 4. Kill All and Exit
echo.
set /p choice="Enter choice (1-4): "

if "%choice%"=="1" goto admin
if "%choice%"=="2" goto user
if "%choice%"=="3" goto both
if "%choice%"=="4" goto kill
goto invalid

:admin
echo Starting Admin Portal on port 8081...
echo Admin: http://localhost:8081/admin-login.html
echo Default: admin/admin123
timeout /t 2 >nul
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
goto end

:user
echo Starting User Portal on port 8080...
echo User: http://localhost:8080/user-login.html
echo Default: user/user123
timeout /t 2 >nul
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8080"
goto end

:both
echo Starting BOTH portals...
echo Please open TWO terminal windows and run:
echo.
echo Terminal 1: mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
echo Terminal 2: mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8080"
echo.
echo Will open browser windows in 3 seconds...
timeout /t 3 >nul
start "" "http://localhost:8081/admin-login.html"
start "" "http://localhost:8080/user-login.html"
goto end

:kill
echo Killing all Java processes and releasing ports...
taskkill /F /IM java.exe >nul 2>&1
echo Done! Ports 8080 and 8081 should now be free.
goto end

:invalid
echo Invalid choice! Please enter 1, 2, 3, or 4.

:end
endlocal
pause