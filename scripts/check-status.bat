@echo off
echo =======================================
echo  Home Automation System Status Check
echo =======================================
echo.

echo 1. Checking Java processes:
echo ---------------------------
tasklist | findstr java.exe
if %errorlevel% neq 0 (
    echo No Java processes running.
) else (
    echo Java processes are running.
)

echo.
echo 2. Checking port 8081:
echo ----------------------
netstat -ano | findstr :8081
if %errorlevel% neq 0 (
    echo Port 8081 is NOT in use.
) else (
    echo Port 8081 IS in use.
)

echo.
echo 3. Checking application response:
echo ----------------------------------
curl -s -I http://localhost:8081/ 2>nul | findstr "HTTP" > check.tmp
if %errorlevel% equ 0 (
    type check.tmp
    echo Application is responding!
) else (
    echo Application is NOT responding or not running.
)

del check.tmp 2>nul >nul

echo.
echo 4. Quick availability test:
echo --------------------------
echo Testing access to login page...
powershell -Command "$response = Invoke-WebRequest -Uri 'http://localhost:8081/login.html' -Method Head -ErrorAction SilentlyContinue; if ($response.StatusCode -eq 200) { '✅ Login page is accessible' } else { '❌ Login page not accessible' }" 2>nul

echo.
echo =======================================
echo Recommended actions:
echo 1. If app is not running: Run start-app.bat
echo 2. If port conflict: Run stop-app.bat then start-app.bat
echo 3. If still issues: Restart your computer
echo =======================================
pause