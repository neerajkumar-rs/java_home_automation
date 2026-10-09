@echo off
echo ============================================
echo TEST APPLICATION
echo ============================================
echo.

echo Step 1: Compiling project...
cd /d %~dp0..
mvn clean compile -q
if %errorlevel% neq 0 (
    echo ❌ Compilation FAILED
    exit /b 1
)
echo ✅ Compilation successful

echo.
echo Step 2: Creating JAR file...
mvn package -DskipTests -q
if %errorlevel% neq 0 (
    echo ❌ Packaging FAILED
    exit /b 1
)
echo ✅ JAR created successfully

echo.
echo Step 3: Stopping any existing instances...
taskkill /F /IM java.exe >nul 2>&1
echo ✅ Existing instances stopped

echo.
echo Step 4: Starting application on port 8081...
start /B "Home Automation Test" java -jar target/home-automation-0.0.1-SNAPSHOT.jar --server.port=8081 > test-log.txt 2>&1
echo "Application starting... Waiting 10 seconds"
timeout /t 10 /nobreak >nul

echo.
echo Step 5: Testing application...
echo Testing port 8081...
curl -s http://localhost:8081/ >nul
if %errorlevel% equ 0 (
    echo ✅ Application is running on port 8081
    start "" "http://localhost:8081/admin-login.html"
    echo "Opening admin login page..."
) else (
    echo ❌ Application failed to start
    type test-log.txt
)

echo.
echo Step 6: Testing admin login page...
curl -s -o nul -w "Admin Login: %%{http_code}\\n" "http://localhost:8081/admin-login.html"
if %errorlevel% neq 0 (
    echo ❌ Admin login page not accessible
)

echo.
echo Step 7: Testing user login page...
start "" "http://localhost:8080/user-login.html" >nul 2>&1
echo "Opening user login page (might fail if application not on 8080)..."

echo.
echo ============================================
echo TEST COMPLETE
echo ============================================
echo Application Status: ✅ COMPILATION SUCCESSFUL
echo.
echo To access:
echo Admin: http://localhost:8081/admin-login.html
echo User:  http://localhost:8080/user-login.html
echo.
echo Default credentials:
echo - Admin: admin / admin123
echo - User:  user  / user123
echo.
echo Press any key to stop the application...
pause >nul

taskkill /F /IM java.exe >nul 2>&1
echo.
echo Application stopped.
del test-log.txt >nul 2>&1