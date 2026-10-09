@echo off
echo ====================================
echo Starting Home Automation Application
echo ====================================
echo.

echo 1. Checking for existing JAR...
if not exist "target\home-automation-0.0.1-SNAPSHOT.jar" (
    echo ! JAR not found! Building application first...
    call mvn package -DskipTests -q
    if %errorlevel% neq 0 (
        echo Failed to build application!
        pause
        exit /b 1
    )
)

echo ✓ Application JAR found/created
echo.

echo 2. Stopping any existing instance on port 8081...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8081') do (
    taskkill /PID %%a /F >nul 2>&1
    echo Killed PID %%a
)

echo.
echo 3. Starting Spring Boot on port 8081...
echo Starting... This may take a few seconds...
echo.

java -jar target/home-automation-0.0.1-SNAPSHOT.jar --server.port=8081

echo.
echo Application stopped.
echo ====================================