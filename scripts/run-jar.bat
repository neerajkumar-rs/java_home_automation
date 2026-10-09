@echo off
echo ============================================
echo RUN USING JAR FILE
echo ============================================
echo.
echo Choose portal:
echo 1. Admin Portal (port 8081)
echo 2. User Portal (port 8080)
echo.
set /p choice="Enter choice (1-2): "

if "%choice%"=="1" goto admin
if "%choice%"=="2" goto user
goto invalid

:admin
echo Starting Admin Portal on port 8081...
echo.
java -jar target/home-automation-0.0.1-SNAPSHOT.jar --server.port=8081
goto end

:user
echo Starting User Portal on port 8080...
echo.
java -jar target/home-automation-0.0.1-SNAPSHOT.jar --server.port=8080
goto end

:invalid
echo Invalid choice! Please enter 1 or 2.

:end
pause