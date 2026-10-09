@echo off
setlocal enabledelayedexpansion

set INTERVAL_SECONDS=5
set BASE_URL=http://localhost:8081
set USERNAME=admin
set PASSWORD=admin123

echo ==============================================
echo   VIRTUAL HOME DEVICES SIMULATOR
echo ==============================================
echo Base URL: %BASE_URL%
echo Username: %USERNAME%
echo Polling every %INTERVAL_SECONDS% seconds...
echo Press Ctrl+C to stop.

:main
  rem Step 1: Authenticate and get JWT token
  echo [%time%] Authenticating...
  powershell -Command "
    $loginBody = @{username='%USERNAME%'; password='%PASSWORD%'} | ConvertTo-Json;
    try {
      $response = Invoke-RestMethod -Uri '%BASE_URL%/api/auth/login' -Method Post -Body $loginBody -ContentType 'application/json';
      $accessToken = 'Bearer ' + $response.accessToken;
      $accessToken | Out-File -FilePath 'auth_token.txt' -Encoding ASCII;
      '[%time%] Authenticated successfully!';
    } catch {
      '[%time%] Authentication failed: ' + $_.Exception.Message;
      exit 1;
    }
  "
  
  if errorlevel 1 (
    echo [%time%] Failed to authenticate. Exiting.
    timeout /t 5
    exit /b 1
  )
  
  rem Read the token
  set /p AUTH_TOKEN=<auth_token.txt 2>nul
  
  if "%AUTH_TOKEN%"=="" (
    echo [%time%] No auth token received. Exiting.
    timeout /t 2
    exit /b 1
  )
  
:simulation_loop
  rem Check if we should get new token (every 30 iterations)
  set /a ITERATION_COUNT+=1
  set /a TOKEN_CHECK=!ITERATION_COUNT! %% 30
  
  if !TOKEN_CHECK! equ 0 (
    echo [%time%] Refreshing authentication token...
    goto :main
  )
  
  echo [%time%] Running simulation iteration...
  
  rem Fetch devices with auth token
  curl -s -H "Authorization: %AUTH_TOKEN%" "%BASE_URL%/api/devices" > devices.json 2>nul
  
  if not exist devices.json (
    echo [%time%] Error: Cannot fetch devices
    goto :refresh_auth
  )
  
  rem Simple simulation logic
  powershell -Command "
    $devices = Get-Content devices.json | ConvertFrom-Json;
    $activeDevices = @($devices | Where-Object { $_.active });
    
    if ($activeDevices.Count -eq 0) {
      '[%time%] No active devices to simulate.';
    } else {
      $device = $activeDevices | Get-Random;
      if ($device.controlType -eq 'SWITCH') {
        $state = @('ON', 'OFF') | Get-Random;
        $value = if ($state -eq 'ON') { 100 } else { 0 };
      } else {
        $value = Get-Random -Minimum 0 -Maximum 101;
        $state = if ($value -gt 0) { 'ON' } else { 'OFF' };
      }
      
      $body = @{state=$state; value=$value} | ConvertTo-Json;
      
      try {
        Invoke-RestMethod -Uri '%BASE_URL%/api/devices/$($device.id)/control' -Method Post -Body $body -Headers @{'Authorization'='%AUTH_TOKEN%'} -ContentType 'application/json';
        '[%time%] VIRTUAL $($device.name) [$($device.controlType)] for#$state ($value%)';
      } catch {
        '[%time%] Error controlling device: ' + $_.Exception.Message;
      }
    }
  "
  
  if errorlevel 1 (
    echo [%time%] PowerShell execution error
    goto :refresh_auth
  )
  
  rem Clean up
  del devices.json 2>nul
  
  goto :sleep

:refresh_auth
  echo [%time%] Attempting to re-authenticate...
  del auth_token.txt 2>nul
  timeout /t 2
  goto :main

:sleep
  timeout /t %INTERVAL_SECONDS% /nobreak >nul
  
goto :simulation_loop