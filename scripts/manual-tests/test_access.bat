@echo off
echo Testing if application is accessible on localhost:8081
timeout /t 2 /nobreak >nul
echo Trying to access the application...

REM Try to check if port is open
netstat -an | findstr 8081
if errorlevel 1 (
    echo Port 8081 is not listening
    exit /b 1
) else (
    echo Port 8081 appears to be open
)

echo.
echo Test URLs:
echo 1. Home page (should redirect to login): http://localhost:8081/
echo 2. Login page: http://localhost:8081/login.html
echo 3. Test auth page: http://localhost:8081/test-auth.html
echo 4. Test login redirect: http://localhost:8081/test-login-redirect.html
echo 5. Admin simple dashboard: http://localhost:8081/admin-simple.html
echo.
echo Open a browser and try these URLs.