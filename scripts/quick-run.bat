@echo off
echo ============================================
echo QUICK RUN - HOME AUTOMATION
echo ============================================
echo.

echo Step 1: Stopping existing instances...
taskkill /F /IM java.exe >nul 2>&1

echo.
echo Step 2: Starting application on port 8081...
echo.
echo IMPORTANT: The following will start the application.
echo If it starts successfully, you will see:
echo "Tomcat started on port 8081"
echo "Started HomeAutomationApplication"
echo.
echo After seeing "Application started successfully!", press Ctrl+C to stop.
echo.
echo ============================================
echo Starting application...

cd /d %~dp0..
java -jar target/home-automation-0.0.1-SNAPSHOT.jar --server.port=8081