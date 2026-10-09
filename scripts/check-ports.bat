@echo off
echo ============================================
echo PORT AVAILABILITY CHECK
echo ============================================
echo.

echo Checking port 8080 (User Portal)...
netstat -ano | findstr :8080 >nul
if %errorlevel% equ 0 (
    echo ❌ Port 8080 is IN USE!
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080') do (
        echo   Process PID: %%a
        tasklist /FI "PID eq %%a" 2>nul | findstr /B /C:"Image Name"
    )
) else (
    echo ✅ Port 8080 is AVAILABLE
)

echo.
echo Checking port 8081 (Admin Portal)...
netstat -ano | findstr :8081 >nul
if %errorlevel% equ 0 (
    echo ❌ Port 8081 is IN USE!
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8081') do (
        echo   Process PID: %%a
        tasklist /FI "PID eq %%a" 2>nul | findstr /B /C:"Image Name"
    )
) else (
    echo ✅ Port 8081 is AVAILABLE
)

echo.
echo ============================================
echo QUICK FIXES:
echo 1. Run scripts\stop-app.bat to kill processes
echo 2. Run scripts\clean-restart.bat to restart
echo 3. Or manually kill the PIDs listed above
echo ============================================
pause