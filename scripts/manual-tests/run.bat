@echo off
setlocal enabledelayedexpansion

echo ==============================================
echo   HOME AUTOMATION SYSTEM - MAIN MENU
echo ==============================================
echo.
echo Choose an option:
echo 1. Start Application
echo 2. Stop Application
echo 3. Check Status
echo 4. Run Tests
echo 5. Run Virtual Devices Simulator
echo 6. Monitor Live Status
echo 7. Force Restart Everything
echo 8. Exit
echo.
choice /c 12345678 /n /m "Enter your choice (1-8): "

if errorlevel 8 goto :exit
if errorlevel 7 goto :restart_all
if errorlevel 6 goto :monitor
if errorlevel 5 goto :virtual_devices
if errorlevel 4 goto :run_tests
if errorlevel 3 goto :check_status
if errorlevel 2 goto :stop_app
if errorlevel 1 goto :start_app

:start_app
  echo Starting application...
  cd scripts
  call start-app.bat
  goto :exit

:stop_app
  echo Stopping application...
  cd scripts
  call stop-app.bat
  goto :exit

:check_status
  echo Checking system status...
  cd scripts
  call check-status.bat
  goto :exit

:run_tests
  echo Running comprehensive tests...
  cd scripts
  call test_system.bat
  goto :exit

:virtual_devices
  echo Running virtual devices simulator...
  cd scripts
  call working-virtual-devices.bat
  goto :exit

:monitor
  echo Starting live monitor...
  cd scripts
  call monitor.bat
  goto :exit

:restart_all
  echo Force restarting everything...
  cd scripts
  call KILL_AND_START.bat
  goto :exit

:exit
  echo.
  echo Done.