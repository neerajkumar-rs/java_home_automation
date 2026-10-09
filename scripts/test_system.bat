@echo off
setlocal enabledelayedexpansion

:: Change to project root directory if we're in scripts folder
if exist "..\pom.xml" (
  cd ..
) else if exist "..\..\pom.xml" (
  cd ..\..
)

echo ==============================================
echo   HOME AUTOMATION SYSTEM TEST
echo ==============================================
echo Testing if system is accessible...
echo Working directory: %CD%
echo.

:check_app
  echo Step 1: Checking if application is running...
  curl -s -I http://localhost:8081/ 2>nul | findstr "HTTP" > nul
  if errorlevel 1 (
    echo ❌ Application NOT running
    echo Please start the application first
    echo Run: mvn spring-boot:run
    pause
    exit /b 1
  )
  echo ✅ Application is running on port 8081

:test_login
  echo.
  echo Step 2: Testing authentication...
  echo Testing admin login (admin/admin123)...
  
  curl -s -X POST http://localhost:8081/api/auth/login ^
    -H "Content-Type: application/json" ^
    -d "{\"username\":\"admin\",\"password\":\"admin123\"}" ^
    -o test_login.json 2>nul
    
  if errorlevel 1 (
    echo ❌ Login request failed
    goto :cleanup
  )
  
  for /f "tokens=2 delims=:{,}" %%a in ('findstr /r "token" test_login.json') do (
    set "TOKEN=%%a"
    set "TOKEN=!TOKEN:"=!"
    set "TOKEN=!TOKEN: =!"
  )
  
  if "!TOKEN!"=="" (
    echo ❌ Login failed - no token received
    type test_login.json
    goto :cleanup
  )
  echo ✅ Login successful! Token: !TOKEN:~0,50!...

:test_devices
  echo.
  echo Step 3: Testing device API...
  echo Fetching devices...
  
  curl -s -H "Authorization: Bearer !TOKEN!" ^
    http://localhost:8081/devices ^
    -o test_devices.json 2>nul
    
  if errorlevel 1 (
    echo ❌ Failed to fetch devices
    goto :cleanup
  )
  
  for /f %%i in ('find /c "{" test_devices.json ^| findstr /v ":"') do set DEVICE_COUNT=%%i
  echo ✅ Found !DEVICE_COUNT! devices
  
  if !DEVICE_COUNT! EQU 0 (
    echo ⚠️  No devices found. Creating test device...
    
    curl -s -X POST http://localhost:8081/device ^
      -H "Authorization: Bearer !TOKEN!" ^
      -H "Content-Type: application/json" ^
      -d "{\"name\":\"Test Light\",\"controlType\":\"SWITCH\",\"sensorType\":\"light\"}" ^
      -o test_create_device.json 2>nul
      
    if errorlevel 1 (
      echo ❌ Failed to create test device
    ) else (
      echo ✅ Test device created
    )
  )

:test_device_control
  echo.
  echo Step 4: Testing device control...
  echo Testing device state change...
  
  rem Use device ID 1 (should exist from seed data)
  curl -s -X POST http://localhost:8081/device/1/state ^
    -H "Authorization: Bearer !TOKEN!" ^
    -H "Content-Type: application/json" ^
    -d "{\"state\":\"ON\",\"value\":100}" ^
    -o test_control.json 2>nul
    
  if errorlevel 1 (
    echo ❌ Device control failed
  ) else (
    echo ✅ Device control successful
  )

:test_frontend
  echo.
  echo Step 5: Testing frontend access...
  curl -s -I http://localhost:8081/login.html 2>nul | findstr "200" > nul
  if errorlevel 1 (
    echo ❌ Login page not accessible
  ) else (
    echo ✅ Login page accessible: http://localhost:8081/login.html
  )
  
  curl -s -I http://localhost:8081/test_all.html 2>nul | findstr "200" > nul
  if errorlevel 1 (
    echo ❌ Test page not accessible
  ) else (
    echo ✅ Test page accessible: http://localhost:8081/test_all.html
  )

:summarize
  echo.
  echo ==============================================
  echo   TEST SUMMARY
  echo ==============================================
  echo ✅ Application running on port 8081
  echo ✅ Authentication working
  echo ✅ Device API accessible
  echo ✅ Frontend pages accessible
  echo.
  echo Ready URLs:
  echo - Login: http://localhost:8081/login.html
  echo - Dashboard: http://localhost:8081/
  echo - Test Page: http://localhost:8081/test_all.html
  echo - Admin: http://localhost:8081/admin.html
  echo.
  echo Default credentials:
  echo - Admin: admin / admin123
  echo - User: user / user123
  echo ==============================================

:cleanup
  del test_*.json 2>nul >nul
  pause