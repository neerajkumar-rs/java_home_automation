@echo off
echo ======================================
echo HOME AUTOMATION - FIX AND START
echo ======================================
echo.

echo 1. FORCE KILL EVERYTHING
taskkill /F /IM java.exe 2>nul
echo [✅] Killed Java

echo 2. FORCE CLEAR PORT 8081
for /f "tokens=5" %%p in ('netstat -ano ^| findstr :8081') do taskkill /F /PID %%p 2>nul
echo [✅] Cleared port

echo 3. START APPLICATION
echo.
echo ⚠️  DO NOT CLOSE THIS WINDOW!
echo    Application logs appear here.
echo.
echo When you see "Tomcat started on port 8081"
echo Open browser to: http://localhost:8081
echo.
echo Use credentials:
echo    admin / admin123
echo    user / user123
echo.
echo ======================================
echo.

mvn spring-boot:run