@echo off
setlocal enabledelayedexpansion

set BASE_URL=http://localhost:8081
set INTERVAL=5

echo ==============================================
echo   SIMPLE VIRTUAL DEVICES SIMULATOR
echo ==============================================
echo URL: %BASE_URL%
echo Interval: %INTERVAL% seconds
echo Press Ctrl+C to stop
echo ==============================================

:loop
  echo [%time%] Checking if application is running...
  
  rem Try to get a simple response first
  curl -s "%BASE_URL%/api/auth/validate" > nul 2>&1
  if errorlevel 1 (
    echo [%time%] Application not responding. Waiting...
    timeout /t %INTERVAL% /nobreak >nul
    goto :loop
  )
  
  echo [%time%] Application is running. Starting simulation...
  
  rem Run PowerShell script directly
  powershell -Command "
    # Authenticate first
    $loginBody = @{username='admin'; password='admin123'} | ConvertTo-Json;
    try {
      $response = Invoke-RestMethod -Uri '%BASE_URL%/api/auth/login' -Method Post -Body $loginBody -ContentType 'application/json';
      $token = 'Bearer ' + $response.token;
      
      # Get devices
      $headers = @{'Authorization' = $token; 'Content-Type' = 'application/json'};
      $devices = Invoke-RestMethod -Uri '%BASE_URL%/device/s' -Method Get -Headers $headers;
      $activeDevices = @($devices | Where-Object { $_.active });
      
      if ($activeDevices.Count -eq 0) {
        echo '[%time%] No active devices to simulate.';
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
        Invoke-RestMethod -Uri '%BASE_URL%/device/$($device.id)/state' -Method Post -Body $body -Headers $headers -ContentType 'application/json';
        echo '[%time%] VIRTUAL $($device.name) -> $state ($value%)';
      }
    } catch {
      echo '[%time%] Error: ' + $_.Exception.Message;
    }
  "
  
  timeout /t %INTERVAL% /nobreak >nul
goto :loop