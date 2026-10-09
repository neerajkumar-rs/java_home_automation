@echo off
echo ============================================
echo RUN WITH SPRING PROFILES
echo ============================================
echo.
echo Choose profile:
echo 1. admin (port 8081)
echo 2. user (port 8080)
echo.
set /p choice="Enter choice (1-2): "

if "%choice%"=="1" goto admin
if "%choice%"=="2" goto user
goto invalid

:admin
echo Starting Admin Portal on port 8081...
echo Using profile: admin
echo Opening: http://localhost:8081/admin-login.html
echo.
mvn spring-boot:run -Dspring-boot.run.profiles=admin
goto end

:user
echo Starting User Portal on port 8080...
echo Using profile: user
echo Opening: http://localhost:8080/user-login.html
echo.
mvn spring-boot:run -Dspring-boot.run.profiles=user
goto end

:invalid
echo Invalid choice! Please enter 1 or 2.

:end
pause