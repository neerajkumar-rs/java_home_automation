@echo off
color 0A
title HOME AUTOMATION - RESTART NOW
mode con: cols=80 lines=25

echo ===============================================
echo        HOME AUTOMATION RESTART
echo ===============================================
echo.
echo This will:                        [STATUS]
echo 1. Kill all Java processes        [   ]
echo 2. Clear port 8081                [   ]
echo 3. Clean project                  [   ]
echo 4. Compile project               [   ]
echo 5. Start application             [   ]
echo.
echo ===============================================
echo.

set step=0

echo Step 1/5: Killing Java processes...
set /a step+=1
tasklist | findstr java.exe >nul && (
    taskkill /F /IM java.exe >nul
    echo [✅] Java processes killed
) || (
    echo [✅] No Java processes found
)

echo Step 2/5: Clearing port 8081...
set /a step+=1
powershell -Command "Get-NetTCPConnection -LocalPort 8081 | ForEach-Object { Stop-Process -Id $_.OwningProcess -Force }" >nul 2>&1
echo [✅] Port 8081 cleared

echo Step 3/5: Cleaning project...
set /a step+=1
mvn clean -q
if %errorlevel% equ 0 (
    echo [✅] Project cleaned
) else (
    echo [❌] Clean failed! Check Maven.
    pause
    exit
)

echo Step 4/5: Compiling...
set /a step+=1
mvn compile -q
if %errorlevel% equ 0 (
    echo [✅] Project compiled
) else (
    echo [❌] Compile failed! Fix errors.
    pause
    exit
)

echo.
echo ===============================================
echo       APPLICATION STARTING...
echo ===============================================
echo.
echo 🎉 READY! Application starting on port 8081
echo.
echo ⚠️  DO NOT CLOSE THIS WINDOW!
echo    It shows live application logs.
echo.
echo ✅ Open browser to: http://localhost:8081
echo.
echo 🛑 Press Ctrl+C to stop application
echo ===============================================
echo.

mvn spring-boot:run