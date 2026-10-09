@echo off
echo ===============================================
echo  HOME AUTOMATION - COMPLETE RESTART
echo ===============================================
echo.

echo STEP 1: KILLING EVERYTHING
echo ---------------------------
echo.

echo Checking for Java processes...
tasklist | findstr java.exe
if %errorlevel% equ 0 (
    echo.
    echo Killing ALL Java processes...
    taskkill /F /IM java.exe
    echo ✅ All Java processes terminated!
) else (
    echo No Java processes found.
)

timeout /t 2 /nobreak > nul
echo.

echo Checking port 8081...
netstat -ano | findstr :8081 > port_check.txt
if %errorlevel% equ 0 (
    echo Port 8081 is STILL in use! Forcing cleanup...
    for /f "tokens=5" %%p in ('netstat -ano ^| findstr :8081') do (
        echo Killing process PID: %%p
        taskkill /F /PID %%p > nul
    )
    echo ✅ Port 8081 cleared.
    del port_check.txt
) else (
    echo ✅ Port 8081 is free and clear.
)

timeout /t 2 /nobreak > nul
echo.

echo ===============================================
echo STEP 2: CLEAN AND COMPILE
echo ===============================================
echo.

echo Cleaning project...
mvn clean
if %errorlevel% neq 0 (
    echo ❌ Clean failed! Check Maven installation.
    pause
    exit /b 1
)
echo ✅ Clean successful.

echo.
echo Compiling project...
mvn compile
if %errorlevel% neq 0 (
    echo ❌ Compilation failed! Fix compilation errors first.
    pause
    exit /b 1
)
echo ✅ Compilation successful.

echo.
echo ===============================================
echo STEP 3: STARTING APPLICATION
echo ===============================================
echo.
echo ⚠️  IMPORTANT: DO NOT CLOSE THIS WINDOW!
echo    This window shows application logs.
echo    Press Ctrl+C to stop when done.
echo.
echo Starting Spring Boot on port 8081...
echo.
echo When you see "Tomcat started on port 8081", 
echo your application is ready at:
echo    http://localhost:8081
echo.
echo ===============================================
echo.

mvn spring-boot:run