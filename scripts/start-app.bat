@echo off
echo =======================================
echo  Home Automation System Startup
echo =======================================
echo.

echo Checking for existing Java processes on port 8081...
netstat -ano | findstr :8081 > nul
if %errorlevel% equ 0 (
    echo Port 8081 is in use! Killing processes...
    for /f "tokens=5" %%p in ('netstat -ano ^| findstr :8081') do (
        echo Killing process PID: %%p
        taskkill /F /PID %%p > nul
    )
    timeout /t 2 /nobreak > nul
) else (
    echo Port 8081 is free.
)

echo.
echo Checking Maven installation...
mvn --version > nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Maven is not installed or not in PATH!
    echo Please install Maven 3.9+ and add it to PATH
    pause
    exit /b 1
)

echo.
echo =======================================
echo Step 1: Cleaning previous build
echo =======================================
mvn clean

echo.
echo =======================================
echo Step 2: Compiling the project
echo =======================================
mvn compile
if %errorlevel% neq 0 (
    echo ERROR: Compilation failed!
    pause
    exit /b 1
)

echo.
echo =======================================
echo Step 3: Starting Spring Boot Application
echo =======================================
echo Application will start on: http://localhost:8081
echo.
echo IMPORTANT: This window will show application logs.
echo DO NOT CLOSE this window while the app is running.
echo.
echo To test the application, open browser to:
echo - Login: http://localhost:8081/login.html
echo - Test Page: http://localhost:8081/test_all.html
echo.
echo Press Ctrl+C to stop the application
echo =======================================
echo.

mvn spring-boot:run