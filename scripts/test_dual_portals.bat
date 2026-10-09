@echo off
echo ============================================
echo TESTING DUAL PORTAL SYSTEM
echo ============================================
echo Admin Portal: http://localhost:8081
echo User Portal:  http://localhost:8080
echo ============================================
echo.

echo Step 1: Starting Admin Portal (port 8081)...
start "Admin Portal Test" cmd /c "mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081"
echo Waiting for admin portal to start...
timeout /t 10 /nobreak >nul

echo Step 2: Testing Admin Login...
curl -X POST "http://localhost:8081/api/auth/login" ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"admin\",\"password\":\"admin123\"}" ^
  -w "`nAdmin Login: %%{http_code}`n" 2>nul
echo.

echo Step 3: Testing User Auth API...
curl -X POST "http://localhost:8081/api/auth/login" ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"user\",\"password\":\"user123\"}" ^
  -w "`nUser Login: %%{http_code}`n" 2>nul
echo.

echo Step 4: Testing Login Pages...
start "" "http://localhost:8081/admin-login.html"
echo Opening Admin Login: http://localhost:8081/admin-login.html
timeout /t 3 /nobreak >nul

echo Step 5: Starting User Portal (port 8080)...
start "User Portal Test" cmd /c "mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8080"
echo Waiting for user portal to start...
timeout /t 10 /nobreak >nul

echo Step 6: Testing User Portal...
start "" "http://localhost:8080/user-login.html"
echo Opening User Login: http://localhost:8080/user-login.html

echo Step 7: Testing Port Separation...
echo.
echo Testing Admin Portal on port 8081:
curl -I "http://localhost:8081/admin-login.html" ^
  -w "Admin Portal: %%{http_code}`n" 2>nul

echo.
echo Testing User Portal on port 8080:
curl -I "http://localhost:8080/user-login.html" ^
  -w "User Portal: %%{http_code}`n" 2>nul

echo.
echo ============================================
echo TEST COMPLETE!
echo ============================================
echo.
echo If both portals show 200 OK, the system is working!
echo.
echo To access:
echo   1. Admin Portal: http://localhost:8081/admin-login.html
echo   2. User Portal:  http://localhost:8080/user-login.html
echo.
echo Default credentials:
echo   Admin: admin / admin123
echo   User:  user  / user123
echo.
pause