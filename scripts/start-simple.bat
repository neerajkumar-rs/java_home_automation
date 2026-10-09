@echo off
echo ============================================
echo SIMPLE START - NO COMMAND LINE ARGUMENTS
echo ============================================
echo.
echo This will start the application with default port 8080
echo You can change the port in application.properties
echo.
echo Starting application...
echo.

cd /d %~dp0..

echo Running: mvn spring-boot:run
mvn spring-boot:run

pause