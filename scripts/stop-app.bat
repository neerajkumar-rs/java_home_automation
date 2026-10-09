@echo off
echo ============================================
echo STOPPING ALL HOME AUTOMATION APPLICATIONS
echo ============================================
echo.

echo Step 1: Killing Java processes...
taskkill /F /IM java.exe >nul 2>&1
if %errorlevel% neq 0 (
    echo No Java processes found or already killed.
) else (
    echo Java processes terminated successfully.
)

echo Step 2: Checking for ports 8080 and 8081...
netstat -ano | findstr :8080 >nul 2>&1
if %errorlevel% equ 0 (
    echo Port 8080 is still in use.
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080') do (
        echo Killing process PID %%a...
        taskkill /F /PID %%a >nul 2>&1
    )
)

netstat -ano | findstr :8081 >nul 2>&1
if %errorlevel% equ 0 (
    echo Port 8081 is still in use.
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8081') do (
        echo Killing process PID %%a...
        taskkill /F /PID %%a >nul 2>&1
    )
)

echo Step 3: Waiting for ports to be released...
timeout /t 3 /nobreak >nul

echo Step 4: Final port check...
netstat -ano | findstr :8080 >nul 2>&1
if %errorlevel% equ 0 (
    echo WARNING: Port 8080 is still occupied!
) else (
    echo Port 8080 is free.
)

netstat -ano | findstr :8081 >nul 2>&1
if %errorlevel% equ 0 (
    echo WARNING: Port 8081 is still occupied!
) else (
    echo Port 8081 is free.
)

echo ============================================
echo CLEAN RESTART READY!
echo ============================================
echo Now you can run:
echo   1. scripts/run_two_portals.bat
echo   2. Or start manually:
echo      mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
echo      mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8080
echo ============================================
pause