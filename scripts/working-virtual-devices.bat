@echo off
setlocal enabledelayedexpansion

echo ==============================================
echo   WORKING VIRTUAL DEVICES SIMULATOR
echo ==============================================
echo This script simulates random device changes
echo Application must be running on http://localhost:8081
echo Press Ctrl+C to stop
echo ==============================================

:main
  rem Step 1: Use PowerShell or curl to authenticate
  echo Attempting to authenticate...
  
  rem Try with curl first
  curl -s -X POST http://localhost:8081/api/auth/login ^
    -H "Content-Type: application/json" ^
    -d "{\"username\":\"admin\",\"password\":\"admin123\"}" ^
    -o auth_response.json 2>nul
    
  if errorlevel 1 (
    echo ERROR: Application not running or unreachable
    echo Waiting 10 seconds before retry...
    timeout /t 10 /nobreak >nul
    goto :main
  )
  
  rem Extract token from response
  for /f "tokens=2 delims=:{,}" %%a in ('findstr /r "token" auth_response.json') do (
    set "TOKEN=%%a"
    set "TOKEN=!TOKEN:"=!"
    set "TOKEN=!TOKEN: =!"
  )
  
  if "!TOKEN!"=="" (
    echo ERROR: Authentication failed
    echo Response was:
    type auth_response.json
    del auth_response.json 2>nul
    timeout /t 5 /nobreak >nul
    goto :main
  )
  
  echo Successfully authenticated!
  echo Token: !TOKEN:~0,50!...
  
:simulation_loop
  echo [%time%] Fetching devices...
  
  rem Get devices
  curl -s -H "Authorization: Bearer !TOKEN!" ^
    http://localhost:8081/devices ^
    -o devices.json 2>nul
    
  if errorlevel 1 (
    echo [%time%] Failed to get devices
    goto :refresh_token
  )
  
  rem Simple device count check
  for /f %%i in ('find /c "{" devices.json ^| findstr /v ":"') do set DEVICE_COUNT=%%i
  
  if !DEVICE_COUNT! EQU 0 (
    echo [%time%] No devices found
    goto :sleep
  )
  
  rem Simulate random device update
  set /a RANDOM_ID=!random! %% !DEVICE_COUNT! + 1
  set /a RANDOM_VALUE=!random! %% 101
  
  if !RANDOM_VALUE! GTR 50 (
    set STATE=ON
  ) else (
    set STATE=OFF
    set /a RANDOM_VALUE=0
  )
  
  echo [%time%] Simulating device !RANDOM_ID!: !STATE! (!RANDOM_VALUE!%^)
  
  rem Update device
  curl -s -X POST http://localhost:8081/device/!RANDOM_ID!/state ^
    -H "Authorization: Bearer !TOKEN!" ^
    -H "Content-Type: application/json" ^
    -d "{\"state\":\"!STATE!\",\"value\":!RANDOM_VALUE!}" ^
    -o update_response.json 2>nul
    
  if errorlevel 1 (
    echo [%time%] Failed to update device
  )
  
:sleep
  del auth_response.json devices.json update_response.json 2>nul
  timeout /t 5 /nobreak >nul
  
goto :simulation_loop

:refresh_token
  echo [%time%] Token may have expired, refreshing...
  del auth_response.json devices.json update_response.json 2>nul
  timeout /t 2 /nobreak >nul
  goto :main