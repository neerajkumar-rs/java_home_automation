@echo off
echo ========================================
echo Home Automation System - Quick Start
echo ========================================
echo.
echo 1. Compiling the project...
call mvn clean compile

echo.
echo 2. Starting the application...
echo    Application will run on: http://localhost:8081
echo    Press Ctrl+C to stop
echo.
call mvn spring-boot:run

pause