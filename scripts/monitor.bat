@echo off
echo =======================================
echo  Home Automation Live Monitor
echo =======================================
echo.
echo This monitor will check app status every 10 seconds
echo Press Ctrl+C to stop monitoring
echo.

:loop
cls
echo [%time%] Status Check
echo ======================

echo 1. Java Processes:
tasklist | findstr java.exe > java.tmp
set /a javaCount=0
for /f %%i in (java.tmp) do set /a javaCount+=1
if %javaCount% gtr 0 (
    echo ✅ %javaCount% Java process(es) running
) else (
    echo ❌ No Java processes
)

echo.
echo 2. Port 8081:
netstat -ano | findstr :8081 > port.tmp
if %errorlevel% equ 0 (
    echo ✅ Port 8081 is in use
) else (
    echo ❌ Port 8081 is not in use
)

echo.
echo 3. Application Response:
curl -s -I http://localhost:8081/ 2>nul | findstr "HTTP" > response.tmp
if %errorlevel% equ 0 (
    type response.tmp
    echo ✅ Application is responding
) else (
    echo ❌ Application not responding
)

echo.
echo 4. Key Endpoints Status:
echo ------------------------
for %%e in (login.html test.html test_all.html) do (
    curl -s -I http://localhost:8081/%%e 2>nul | findstr "HTTP" > %%e.tmp
    if %errorlevel% equ 0 (
        echo ✅ %%e accessible
    ) else (
        echo ❌ %%e not accessible
    )
)

echo.
echo =======================================
echo Next check in 10 seconds...
echo Press Ctrl+C to exit monitor
echo =======================================

del *.tmp 2>nul >nul
timeout /t 10 /nobreak >nul
goto loop